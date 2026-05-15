#!/usr/bin/env python3
"""
START IoT - Bridge LoRa <-> Spring Boot API
- Login JWT automatico
- Endpoints custom: /cronometragem/atual, /cronometragem/buzina
- Resposta resumida para caber no LoRa (<255 bytes)
"""

import serial
import requests
import json
import time
from datetime import datetime
from pathlib import Path


class LoRaBridge:
    def __init__(self, config_file='config.json'):
        print("🚀 LoRa Bridge - Iniciando...")
        
        self.config = self._carregar_config(config_file)
        
        # Conectar Serial
        self.serial = serial.Serial(
            self.config['serial']['port'],
            self.config['serial']['baudrate'],
            timeout=1
        )
        time.sleep(2)
        
        # Autenticação
        self.token = None
        self.token_expiracao = None
        self.usuario_id = None
        self._fazer_login()
        
        print("✅ Bridge conectada!")
        print(f"📡 Serial: {self.config['serial']['port']}")
        print(f"🌐 API: {self.config['api']['base_url']}")
        print(f"👤 Usuário: {self.config['auth']['email']} (ID: {self.usuario_id})")
        print("=" * 60)
    
    def _carregar_config(self, arquivo):
        caminho = Path(__file__).parent / arquivo
        if not caminho.exists():
            print(f"❌ Arquivo não encontrado: {caminho}")
            exit(1)
        with open(caminho) as f:
            return json.load(f)
    
    def _fazer_login(self):
        try:
            print("\n🔐 Fazendo login...")
            url = f"{self.config['api']['base_url']}/api/auth/login"
            dados = {
                "email": self.config['auth']['email'],
                "senha": self.config['auth']['senha']
            }
            response = requests.post(url, json=dados, timeout=5)
            response.raise_for_status()
            
            auth_data = response.json()
            self.token = auth_data['token']
            self.usuario_id = auth_data['usuarioId']
            
            expiracao_str = auth_data['expiracao']
            self.token_expiracao = datetime.fromisoformat(expiracao_str.replace('Z', '+00:00'))
            
            perfil = auth_data.get('perfil', 'N/A')
            usuario = auth_data.get('nomeUsuario', 'N/A')
            
            print(f"✅ Login OK - {usuario} | {perfil}")
            print(f"⏰ Válido até: {self.token_expiracao.strftime('%H:%M:%S')}")
            
        except requests.RequestException as e:
            print(f"❌ Erro login: {e}")
            exit(1)
    
    def _verificar_token(self):
        if not self.token_expiracao:
            return
        agora = datetime.now(self.token_expiracao.tzinfo)
        tempo_restante = (self.token_expiracao - agora).total_seconds()
        if tempo_restante < 300:
            print("\n🔄 Renovando token...")
            self._fazer_login()
    
    def _headers(self):
        return {
            'Authorization': f'Bearer {self.token}',
            'Content-Type': 'application/json',
            'X-Usuario-Id': str(self.usuario_id)
        }
    
    def _http(self, metodo, endpoint, body=None):
        """Faz requisicao HTTP simples e retorna (status, body)"""
        try:
            self._verificar_token()
            url = self.config['api']['base_url'] + endpoint
            timeout = self.config['api']['timeout']
            
            if metodo == 'GET':
                r = requests.get(url, headers=self._headers(), timeout=timeout)
            elif metodo == 'POST':
                r = requests.post(url, json=body, headers=self._headers(), timeout=timeout)
            elif metodo == 'PATCH':
                r = requests.patch(url, json=body, headers=self._headers(), timeout=timeout)
            elif metodo == 'DELETE':
                r = requests.delete(url, headers=self._headers(), timeout=timeout)
            else:
                return 500, {"error": "metodo nao suportado"}
            
            return r.status_code, (r.json() if r.text else {})
        except requests.Timeout:
            return 504, {"error": "Gateway Timeout"}
        except requests.RequestException as e:
            return 502, {"error": str(e)[:80]}
        except Exception as e:
            return 500, {"error": str(e)[:80]}
    
    # ===== BUZINA =====
    def _acionar_buzina(self):
        """Aciona buzina por 1 segundo via Serial -> ESP32_Topo"""
        print("🔔 BUZINA!")
        self.serial.write((json.dumps({"cmd": "BUZINA_ON"}) + '\n').encode())
        time.sleep(1.0)
        self.serial.write((json.dumps({"cmd": "BUZINA_OFF"}) + '\n').encode())
    
    # ===== ENDPOINT CUSTOM: /cronometragem/atual =====
    def _buscar_corrida_atual(self):
        """
        Busca a corrida EM_ANDAMENTO atual no sistema.
        Cascata: eventos -> edicao EM_ANDAMENTO -> bateria EM_ANDAMENTO -> corrida EM_ANDAMENTO
        Retorna resposta resumida para LoRa.
        """
        # 1. Listar eventos
        status, eventos = self._http('GET', '/api/eventos')
        if status != 200 or not isinstance(eventos, list) or not eventos:
            return {"ok": 0, "msg": "Sem eventos"}
        
        # Tentar achar edicao EM_ANDAMENTO em qualquer evento
        edicao_ativa = None
        for ev in eventos:
            status, edicoes = self._http('GET', f"/api/eventos/{ev['id']}/edicoes")
            if status == 200 and isinstance(edicoes, list):
                for ed in edicoes:
                    if ed.get('status') == 'EM_ANDAMENTO':
                        edicao_ativa = ed
                        break
            if edicao_ativa:
                break
        
        if not edicao_ativa:
            return {"ok": 0, "msg": "Sem edicao ativa"}
        
        # 2. Buscar bateria EM_ANDAMENTO
        status, baterias = self._http('GET', f"/api/baterias?edicaoId={edicao_ativa['id']}")
        if status != 200 or not isinstance(baterias, list):
            return {"ok": 0, "msg": "Sem baterias"}
        
        bateria_ativa = next((b for b in baterias if b.get('status') == 'EM_ANDAMENTO'), None)
        if not bateria_ativa:
            return {"ok": 0, "msg": "Sem bateria ativa"}
        
        # 3. Buscar corrida EM_ANDAMENTO
        status, corridas = self._http('GET', f"/api/corridas?bateriaId={bateria_ativa['id']}")
        if status != 200 or not isinstance(corridas, list):
            return {"ok": 0, "msg": "Sem corridas"}
        
        corrida_ativa = next((c for c in corridas if c.get('status') == 'EM_ANDAMENTO'), None)
        if not corrida_ativa:
            # Tem bateria mas sem corrida iniciada
            return {"ok": 0, "msg": "Aguardando corrida", "b": bateria_ativa.get('numero')}
        
        # 4. Buscar equipes alocadas
        status, alocacoes = self._http('GET', f"/api/corridas/{corrida_ativa['id']}/equipes")
        equipes_compact = []
        if status == 200 and isinstance(alocacoes, list):
            for a in alocacoes[:8]:  # Maximo 8 equipes
                nome = a.get('equipeNome', '')[:20]  # Truncar nome
                equipes_compact.append([a.get('equipeId'), nome])
        
        # Resposta compacta
        return {
            "ok": 1,
            "id": corrida_ativa['id'],
            "o": corrida_ativa.get('ordem'),
            "b": bateria_ativa.get('numero'),
            "e": equipes_compact
        }
    
    # ===== RESUMO DE RESPOSTAS PADRAO =====
    def _resumir_resposta(self, metodo, endpoint, status, body):
        """Resume resposta padrao para caber no LoRa"""
        if status >= 400:
            erro = body.get('detail') or body.get('error') or 'Erro'
            return {"status": status, "body": {"error": str(erro)[:80]}}
        
        if not body:
            return {"status": status, "body": {"ok": True}}
        
        # POST /api/registros-tempo
        if '/registros-tempo' in endpoint and metodo == 'POST':
            return {
                "status": status,
                "body": {
                    "id": body.get('id'),
                    "t": body.get('tempoMilissegundos'),
                    "eq": body.get('equipeId')
                }
            }
        
        # PATCH /api/corridas/{id}/finalizar
        if '/finalizar' in endpoint and metodo == 'PATCH':
            return {
                "status": status,
                "body": {
                    "id": body.get('id'),
                    "s": body.get('status')
                }
            }
        
        # PATCH /api/corridas/{id}/iniciar (caso ainda usem)
        if '/iniciar' in endpoint and metodo == 'PATCH':
            return {
                "status": status,
                "body": {
                    "id": body.get('id'),
                    "s": body.get('status')
                }
            }
        
        # Fallback generico
        if isinstance(body, dict):
            essenciais = ['id', 'status']
            resumo = {k: body[k] for k in essenciais if k in body}
            return {"status": status, "body": resumo if resumo else {"ok": True}}
        
        return {"status": status, "body": {"ok": True}}
    
    # ===== PROCESSAMENTO PRINCIPAL =====
    def processar_mensagem(self, linha):
        try:
            dados = json.loads(linha)
            
            # Ignorar respostas que vem da Serial (echo de LoRa)
            if 'status' in dados and 'body' in dados:
                return
            
            # Ignorar confirmacoes ESP32_Topo
            if dados.get('status') in ('buzina_on', 'buzina_off', 'ready'):
                return
            
            # Processar requisicao
            if 'method' in dados and 'endpoint' in dados:
                metodo = dados['method'].upper()
                endpoint = dados['endpoint']
                body = dados.get('body', None)
                
                ts = datetime.now().strftime('%H:%M:%S')
                print(f"\n📥 [{ts}] {metodo} {endpoint}")
                
                # === ENDPOINTS CUSTOM ===
                if endpoint == '/cronometragem/atual' and metodo == 'GET':
                    resultado = self._buscar_corrida_atual()
                    resposta_compacta = {"status": 200, "body": resultado}
                    print(f"📤 Corrida atual: ok={resultado.get('ok')}")
                
                elif endpoint == '/cronometragem/buzina' and metodo == 'POST':
                    self._acionar_buzina()
                    resposta_compacta = {"status": 200, "body": {"ok": True}}
                
                # === ENDPOINTS NORMAIS DA API ===
                else:
                    status, resp_body = self._http(metodo, endpoint, body)
                    print(f"📤 Status: {status}")
                    resposta_compacta = self._resumir_resposta(metodo, endpoint, status, resp_body)
                
                json_resp = json.dumps(resposta_compacta, ensure_ascii=False)
                print(f"📦 Resposta: {len(json_resp.encode('utf-8'))} bytes")
                
                # Enviar via Serial -> ESP32_Topo -> LoRa -> ESP32_Chegada
                self.serial.write((json_resp + '\n').encode('utf-8'))
                return
            
            print(f"ℹ️ Msg: {linha[:80]}")
        
        except json.JSONDecodeError:
            print(f"📝 Serial: {linha[:80]}")
        except Exception as e:
            print(f"❌ Erro: {e}")
    
    def executar(self):
        print("\n🔄 Aguardando requisições...\n")
        try:
            while True:
                if self.serial.in_waiting:
                    linha = self.serial.readline().decode('utf-8', errors='ignore').strip()
                    if linha:
                        self.processar_mensagem(linha)
                time.sleep(0.01)
        except KeyboardInterrupt:
            print("\n\n👋 Bridge encerrada")
        finally:
            self.serial.close()


if __name__ == "__main__":
    bridge = LoRaBridge()
    bridge.executar()