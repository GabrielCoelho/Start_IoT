/*
 * START IoT - ESP32 CHEGADA
 * WiFi AP + HTTP Server + LoRa Bridge
 * Interface seguindo fluxo REAL: polling -> largada -> paradas -> atribuir -> enviar
 */

#include <WiFi.h>
#include <WebServer.h>
#include <SPI.h>
#include <LoRa.h>
#include <ArduinoJson.h>

// ===== WiFi AP =====
const char* ssid = "START-IoT";
const char* password = "startiot2026";

// ===== LoRa =====
#define LORA_SS    5
#define LORA_RST   14
#define LORA_DIO0  26
#define LORA_FREQ  433E6
#define LORA_SF    10
#define LORA_BW    125E3

// ===== HTTP Server =====
WebServer server(80);

// ===== Timeout LoRa =====
const unsigned long TIMEOUT_LORA = 12000;

void setup() {
  Serial.begin(115200);
  
  Serial.println("\n🚀 ESP32 CHEGADA - START IoT");
  
  // WiFi AP
  WiFi.softAP(ssid, password);
  IPAddress IP = WiFi.softAPIP();
  Serial.print("📶 WiFi: "); Serial.println(ssid);
  Serial.print("🔐 Senha: "); Serial.println(password);
  Serial.print("📍 IP: "); Serial.println(IP);
  
  // LoRa
  LoRa.setPins(LORA_SS, LORA_RST, LORA_DIO0);
  if (!LoRa.begin(LORA_FREQ)) {
    Serial.println("❌ LoRa init failed!");
    while (1);
  }
  LoRa.setSpreadingFactor(LORA_SF);
  LoRa.setSignalBandwidth(LORA_BW);
  Serial.print("📡 LoRa OK - SF"); Serial.print(LORA_SF);
  Serial.print(" BW125 "); Serial.print(LORA_FREQ/1E6); Serial.println("MHz");
  
  // HTTP Server
  server.on("/", HTTP_GET, handleRoot);
  server.onNotFound(handleAPI);
  server.begin();
  Serial.println("🌐 HTTP Server porta 80");
  Serial.println("✅ Sistema pronto!\n");
}

void loop() {
  server.handleClient();
}

// ============ INTERFACE HTML ============
void handleRoot() {
  String html = R"HTML(
<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>START IoT - Cronometrista</title>
<style>
*{box-sizing:border-box;margin:0;padding:0}
body{font-family:-apple-system,Arial,sans-serif;background:#1A1A2E;color:#fff;padding:15px;min-height:100vh}
h1{color:#00d4ff;text-align:center;margin-bottom:15px;font-size:22px}
.card{background:#16213e;border-radius:10px;padding:15px;margin-bottom:12px;box-shadow:0 4px 6px rgba(0,0,0,0.3)}
.hidden{display:none!important}

/* Estado AGUARDANDO */
.aguardando{text-align:center;padding:40px 20px}
.spinner{display:inline-block;width:60px;height:60px;border:4px solid rgba(0,212,255,0.2);border-top-color:#00d4ff;border-radius:50%;animation:spin 1s linear infinite;margin-bottom:20px}
@keyframes spin{to{transform:rotate(360deg)}}
.aguardando-msg{font-size:16px;opacity:0.7;margin-top:10px}
.aguardando-detalhe{font-size:13px;opacity:0.4;margin-top:8px;font-family:monospace}

/* Info da corrida */
.info-row{display:flex;justify-content:space-between;padding:6px 0;border-bottom:1px solid rgba(255,255,255,0.1)}
.info-row:last-child{border:0}
.info-label{opacity:0.6}
.info-value{font-weight:700;color:#00d4ff}

/* Equipes */
.equipes-list{display:flex;flex-wrap:wrap;gap:6px;margin-top:8px}
.equipe-chip{background:#0f3460;padding:6px 10px;border-radius:6px;font-size:13px;border:1px solid rgba(0,212,255,0.3)}

/* Botoes */
button{border:none;border-radius:8px;font-weight:700;cursor:pointer;width:100%;margin:5px 0;transition:0.15s;font-family:inherit}
button:active{transform:scale(0.97)}
button:disabled{opacity:0.4;cursor:not-allowed}
.btn-largada{background:#ff4757;color:#fff;font-size:24px;padding:30px;animation:pulse 2s infinite}
.btn-largada:disabled{animation:none}
@keyframes pulse{0%,100%{box-shadow:0 0 0 0 rgba(255,71,87,0.7)}50%{box-shadow:0 0 0 18px rgba(255,71,87,0)}}
.btn-parada{background:#F5A623;color:#1A1A2E;font-size:20px;padding:25px;animation:pulse-orange 1.5s infinite}
@keyframes pulse-orange{0%,100%{box-shadow:0 0 0 0 rgba(245,166,35,0.6)}50%{box-shadow:0 0 0 15px rgba(245,166,35,0)}}
.btn-enviar{background:#22B573;color:#fff;font-size:18px;padding:20px}
.btn-cancelar{background:transparent;color:#ff4757;border:1px solid #ff4757;padding:12px;font-size:14px}
.btn-novo{background:#1a5490;color:#fff;padding:15px;font-size:16px}

/* Cronometro */
.cronometro{font-size:54px;text-align:center;color:#4ade80;font-family:'Courier New',monospace;font-weight:900;padding:25px;background:#0f3460;border-radius:10px;margin:10px 0;letter-spacing:2px}

/* Lista de paradas */
.parada-item{display:flex;align-items:center;gap:10px;padding:10px;background:#0f3460;border-radius:6px;margin:5px 0;font-family:monospace}
.parada-pos{font-weight:900;color:#F5A623;min-width:25px}
.parada-tempo{font-weight:700;color:#4ade80;flex:1}
.parada-eq{background:#16213e;color:#fff;border:1px solid rgba(255,255,255,0.2);border-radius:5px;padding:6px;font-family:inherit;font-size:14px;min-width:120px}

/* Status */
.status-bar{padding:8px;border-radius:5px;text-align:center;font-weight:700;font-size:14px}
.status-aguardando{background:#F5A623;color:#1A1A2E}
.status-pronto{background:#22B573;color:#fff}
.status-andamento{background:#ff4757;color:#fff;animation:pulse-red 1s infinite}
@keyframes pulse-red{0%,100%{opacity:1}50%{opacity:0.7}}
.status-atribuindo{background:#1a5490;color:#fff}
.status-finalizada{background:#2E7D32;color:#fff}

/* Log */
.log{background:#0f3460;padding:8px;border-radius:5px;font-family:monospace;font-size:11px;max-height:120px;overflow-y:auto;margin-top:8px}
.log-item{margin:2px 0}
.log-ok{color:#4ade80}
.log-erro{color:#ff4757}
.log-info{color:#00d4ff}

/* Resultado final */
.resultado{padding:15px;background:#0f3460;border-radius:8px;margin:5px 0;display:flex;align-items:center;gap:12px}
.resultado-pos{font-weight:900;font-size:18px;min-width:30px}
.resultado-pos-1{color:#F5A623}
.resultado-pos-2{color:#A8A9AD}
.resultado-pos-3{color:#CD7F32}
.resultado-nome{flex:1;font-weight:700}
.resultado-tempo{font-family:monospace;font-weight:900;color:#4ade80}
</style>
</head>
<body>

<h1>🏁 START IoT</h1>

<!-- ====== TELA AGUARDANDO ====== -->
<div id="telaAguardando" class="card">
  <div class="aguardando">
    <div class="spinner"></div>
    <div style="font-size:20px;font-weight:700">Aguardando corrida</div>
    <div class="aguardando-msg" id="aguardandoMsg">Verificando estado...</div>
    <div class="aguardando-detalhe" id="aguardandoDetalhe"></div>
  </div>
</div>

<!-- ====== TELA PRONTO PARA LARGADA ====== -->
<div id="telaPronto" class="hidden">
  <div class="card">
    <div class="status-bar status-pronto">CORRIDA LIBERADA - PRONTA PARA LARGADA</div>
    <div style="margin-top:15px">
      <div class="info-row"><span class="info-label">Bateria</span><span class="info-value" id="prontoBateria">-</span></div>
      <div class="info-row"><span class="info-label">Corrida</span><span class="info-value" id="prontoCorrida">-</span></div>
      <div class="info-row"><span class="info-label">Equipes</span><span class="info-value" id="prontoNumEquipes">-</span></div>
    </div>
    <div class="equipes-list" id="prontoEquipes"></div>
  </div>
  
  <div class="card">
    <button class="btn-largada" id="btnLargada" onclick="darLargada()">
      🚦 DAR LARGADA
    </button>
  </div>
</div>

<!-- ====== TELA CRONOMETRANDO ====== -->
<div id="telaCronometrando" class="hidden">
  <div class="card">
    <div class="status-bar status-andamento">CORRIDA EM ANDAMENTO</div>
    <div class="cronometro" id="cronometro">00:00.00</div>
    <div style="text-align:center;font-size:13px;opacity:0.6" id="cronometroInfo">Chegadas: 0 / 0</div>
  </div>
  
  <div class="card">
    <button class="btn-parada" id="btnParada" onclick="registrarParada()">
      ⏱ PARAR (1ª chegada)
    </button>
  </div>
</div>

<!-- ====== TELA ATRIBUIR ====== -->
<div id="telaAtribuir" class="hidden">
  <div class="card">
    <div class="status-bar status-atribuindo">ATRIBUIR EQUIPES AOS TEMPOS</div>
    <div style="font-size:13px;opacity:0.7;margin-top:10px">
      Selecione qual equipe corresponde a cada tempo capturado:
    </div>
  </div>
  
  <div class="card" id="cardParadas">
    <!-- Paradas serão inseridas via JS -->
  </div>
  
  <div class="card">
    <button class="btn-enviar" id="btnEnviar" onclick="enviarRegistros()">
      📤 ENVIAR RESULTADOS
    </button>
  </div>
</div>

<!-- ====== TELA SUCESSO ====== -->
<div id="telaSucesso" class="hidden">
  <div class="card">
    <div class="status-bar status-finalizada">✅ RESULTADOS ENVIADOS!</div>
    <div id="resultadosFinais" style="margin-top:15px"></div>
    <button class="btn-novo" onclick="voltarAguardar()" style="margin-top:15px">
      ➡ PRÓXIMA CORRIDA
    </button>
  </div>
</div>

<!-- ====== LOG ====== -->
<div class="card">
  <div style="font-size:12px;opacity:0.5;margin-bottom:5px">Log do sistema</div>
  <div id="log" class="log"></div>
</div>

<script>
// ============ ESTADO GLOBAL ============
let estado = 'aguardando'; // aguardando, pronto, cronometrando, atribuindo, sucesso
let corridaAtual = null;   // { id, ordem, bateria, equipes: [[id, nome], ...] }
let timestampLargada = null;
let intervalCronometro = null;
let intervalPolling = null;
let tempos = [];           // [{ms, equipeId}]

// ============ LOG ============
function log(msg, tipo) {
  const el = document.getElementById('log');
  const item = document.createElement('div');
  const ts = new Date().toLocaleTimeString();
  item.className = 'log-item log-' + (tipo || 'info');
  item.textContent = '[' + ts + '] ' + msg;
  el.insertBefore(item, el.firstChild);
  while(el.children.length > 25) el.removeChild(el.lastChild);
}

// ============ FORMATAR TEMPO ============
function formatTempo(ms) {
  const min = Math.floor(ms / 60000);
  const seg = Math.floor((ms % 60000) / 1000);
  const cent = Math.floor((ms % 1000) / 10);
  return String(min).padStart(2,'0') + ':' + 
         String(seg).padStart(2,'0') + '.' + 
         String(cent).padStart(2,'0');
}

// ============ NAVEGAR ENTRE TELAS ============
function mostrarTela(t) {
  ['Aguardando','Pronto','Cronometrando','Atribuir','Sucesso'].forEach(nome => {
    document.getElementById('tela'+nome).classList.add('hidden');
  });
  const map = {aguardando:'Aguardando',pronto:'Pronto',cronometrando:'Cronometrando',atribuindo:'Atribuir',sucesso:'Sucesso'};
  document.getElementById('tela'+map[t]).classList.remove('hidden');
  estado = t;
}

// ============ POLLING: BUSCA CORRIDA ATUAL ============
async function buscarCorridaAtual() {
  try {
    const resp = await fetch('/cronometragem/atual');
    const data = await resp.json();
    
    document.getElementById('aguardandoDetalhe').textContent = 
      'Última verificação: ' + new Date().toLocaleTimeString();
    
    if (data.ok === 1) {
      // Corrida encontrada!
      corridaAtual = {
        id: data.id,
        ordem: data.o,
        bateria: data.b,
        equipes: data.e || []
      };
      log('🎯 Corrida ' + data.id + ' detectada (' + corridaAtual.equipes.length + ' equipes)', 'ok');
      pararPolling();
      irParaProntoLargada();
    } else {
      document.getElementById('aguardandoMsg').textContent = data.msg || 'Aguardando...';
    }
  } catch(e) {
    log('❌ Erro polling: ' + e.message, 'erro');
  }
}

function iniciarPolling() {
  if (intervalPolling) clearInterval(intervalPolling);
  buscarCorridaAtual(); // primeira chamada imediata
  intervalPolling = setInterval(buscarCorridaAtual, 5000);
  log('🔄 Polling iniciado (5s)', 'info');
}

function pararPolling() {
  if (intervalPolling) {
    clearInterval(intervalPolling);
    intervalPolling = null;
  }
}

// ============ FLUXO: PRONTO PARA LARGADA ============
function irParaProntoLargada() {
  document.getElementById('prontoBateria').textContent = '#' + corridaAtual.bateria;
  document.getElementById('prontoCorrida').textContent = corridaAtual.ordem;
  document.getElementById('prontoNumEquipes').textContent = corridaAtual.equipes.length;
  
  const equipesEl = document.getElementById('prontoEquipes');
  equipesEl.innerHTML = '';
  corridaAtual.equipes.forEach(([id, nome]) => {
    const chip = document.createElement('div');
    chip.className = 'equipe-chip';
    chip.textContent = nome;
    equipesEl.appendChild(chip);
  });
  
  document.getElementById('btnLargada').disabled = false;
  document.getElementById('btnLargada').textContent = '🚦 DAR LARGADA';
  
  tempos = [];
  mostrarTela('pronto');
}

// ============ FLUXO: DAR LARGADA ============
async function darLargada() {
  const btn = document.getElementById('btnLargada');
  btn.disabled = true;
  btn.textContent = '⏳ Tocando buzina...';
  log('🚦 Solicitando largada', 'info');
  
  try {
    const inicio = Date.now();
    const resp = await fetch('/cronometragem/buzina', { method: 'POST' });
    const data = await resp.json();
    const tempo = Date.now() - inicio;
    
    if (resp.ok && data.ok) {
      log('🔔 BUZINA TOCOU! (' + tempo + 'ms)', 'ok');
      timestampLargada = Date.now();
      tempos = [];
      irParaCronometrando();
    } else {
      btn.disabled = false;
      btn.textContent = '🚦 DAR LARGADA';
      log('❌ Erro ao tocar buzina', 'erro');
    }
  } catch(e) {
    btn.disabled = false;
    btn.textContent = '🚦 DAR LARGADA';
    log('❌ ' + e.message, 'erro');
  }
}

// ============ FLUXO: CRONOMETRANDO ============
function irParaCronometrando() {
  mostrarTela('cronometrando');
  atualizarBotaoParada();
  intervalCronometro = setInterval(atualizarCronometro, 50);
}

function atualizarCronometro() {
  if (!timestampLargada) return;
  const ms = Date.now() - timestampLargada;
  document.getElementById('cronometro').textContent = formatTempo(ms);
  document.getElementById('cronometroInfo').textContent = 
    'Chegadas: ' + tempos.length + ' / ' + corridaAtual.equipes.length;
}

function atualizarBotaoParada() {
  const proxima = tempos.length + 1;
  const total = corridaAtual.equipes.length;
  const btn = document.getElementById('btnParada');
  
  if (proxima > total) {
    btn.disabled = true;
    btn.textContent = '✓ TODAS CHEGADAS REGISTRADAS';
  } else {
    btn.disabled = false;
    const sufixo = proxima === 1 ? 'ª' : 'ª';
    btn.textContent = '⏱ PARAR (' + proxima + sufixo + ' chegada)';
  }
}

function registrarParada() {
  if (!timestampLargada) return;
  const ms = Date.now() - timestampLargada;
  tempos.push({ ms: ms, equipeId: null });
  log('⏱ Parada ' + tempos.length + ': ' + formatTempo(ms), 'ok');
  atualizarBotaoParada();
  
  if (tempos.length >= corridaAtual.equipes.length) {
    // Todas chegadas registradas -> ir para atribuir
    clearInterval(intervalCronometro);
    intervalCronometro = null;
    setTimeout(irParaAtribuir, 500);
  }
}

// ============ FLUXO: ATRIBUIR EQUIPES ============
function irParaAtribuir() {
  const card = document.getElementById('cardParadas');
  card.innerHTML = '';
  
  // Ordena tempos por velocidade (menor = primeiro)
  const temposOrdenados = tempos.map((t, idx) => ({...t, idx: idx}))
                                .sort((a, b) => a.ms - b.ms);
  
  temposOrdenados.forEach((t, i) => {
    const div = document.createElement('div');
    div.className = 'parada-item';
    
    const pos = document.createElement('div');
    pos.className = 'parada-pos';
    pos.textContent = (i+1) + 'º';
    div.appendChild(pos);
    
    const tempo = document.createElement('div');
    tempo.className = 'parada-tempo';
    tempo.textContent = formatTempo(t.ms);
    div.appendChild(tempo);
    
    const select = document.createElement('select');
    select.className = 'parada-eq';
    select.dataset.idx = t.idx;
    select.onchange = atualizarBotaoEnviar;
    
    const optPlaceholder = document.createElement('option');
    optPlaceholder.value = '';
    optPlaceholder.textContent = '-- Selecione --';
    select.appendChild(optPlaceholder);
    
    corridaAtual.equipes.forEach(([id, nome]) => {
      const opt = document.createElement('option');
      opt.value = id;
      opt.textContent = nome;
      select.appendChild(opt);
    });
    
    div.appendChild(select);
    card.appendChild(div);
  });
  
  mostrarTela('atribuindo');
  atualizarBotaoEnviar();
}

function atualizarBotaoEnviar() {
  const selects = document.querySelectorAll('.parada-eq');
  const valores = Array.from(selects).map(s => s.value).filter(v => v);
  const unicos = new Set(valores);
  
  const btn = document.getElementById('btnEnviar');
  const total = corridaAtual.equipes.length;
  
  if (valores.length === total && unicos.size === total) {
    btn.disabled = false;
    btn.textContent = '📤 ENVIAR RESULTADOS';
  } else {
    btn.disabled = true;
    btn.textContent = 'Atribuídas: ' + unicos.size + '/' + total + 
                      (unicos.size !== valores.length ? ' (duplicado!)' : '');
  }
}

// ============ FLUXO: ENVIAR ============
async function enviarRegistros() {
  const btn = document.getElementById('btnEnviar');
  btn.disabled = true;
  btn.textContent = '⏳ Enviando...';
  
  // Atualizar tempos com equipeIds
  const selects = document.querySelectorAll('.parada-eq');
  selects.forEach(s => {
    const idx = parseInt(s.dataset.idx);
    tempos[idx].equipeId = parseInt(s.value);
  });
  
  let sucessos = 0;
  for (let i = 0; i < tempos.length; i++) {
    const t = tempos[i];
    log('📤 Enviando ' + (i+1) + '/' + tempos.length + '...', 'info');
    
    try {
      const resp = await fetch('/api/registros-tempo', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
          corridaId: corridaAtual.id,
          equipeId: t.equipeId,
          tempoMilissegundos: t.ms,
          tipoRegistro: "CHEGADA"
        })
      });
      const data = await resp.json();
      
      if (resp.ok) {
        sucessos++;
        log('✓ Registro ' + (i+1) + ' OK', 'ok');
      } else {
        log('❌ Erro: ' + (data.error || resp.status), 'erro');
      }
    } catch(e) {
      log('❌ ' + e.message, 'erro');
    }
  }
  
  if (sucessos === tempos.length) {
    // Finalizar corrida
    log('📤 Finalizando corrida...', 'info');
    try {
      const resp = await fetch('/api/corridas/' + corridaAtual.id + '/finalizar', { method: 'PATCH' });
      if (resp.ok) {
        log('✅ Corrida finalizada!', 'ok');
        mostrarResultados();
      } else {
        log('⚠️ Tempos OK mas falha ao finalizar', 'erro');
        btn.disabled = false;
        btn.textContent = '🔄 TENTAR FINALIZAR';
      }
    } catch(e) {
      log('❌ ' + e.message, 'erro');
    }
  } else {
    btn.disabled = false;
    btn.textContent = '🔄 TENTAR NOVAMENTE (' + sucessos + '/' + tempos.length + ' OK)';
  }
}

function mostrarResultados() {
  const div = document.getElementById('resultadosFinais');
  div.innerHTML = '';
  
  const ranking = tempos.slice().sort((a, b) => a.ms - b.ms);
  
  ranking.forEach((t, i) => {
    const equipe = corridaAtual.equipes.find(([id]) => id === t.equipeId);
    const nome = equipe ? equipe[1] : 'Equipe ' + t.equipeId;
    
    const item = document.createElement('div');
    item.className = 'resultado';
    
    const pos = document.createElement('div');
    pos.className = 'resultado-pos resultado-pos-' + (i+1);
    pos.textContent = (i+1) + 'º';
    item.appendChild(pos);
    
    const nomeDiv = document.createElement('div');
    nomeDiv.className = 'resultado-nome';
    nomeDiv.textContent = nome;
    item.appendChild(nomeDiv);
    
    const tempoDiv = document.createElement('div');
    tempoDiv.className = 'resultado-tempo';
    tempoDiv.textContent = formatTempo(t.ms);
    item.appendChild(tempoDiv);
    
    div.appendChild(item);
  });
  
  mostrarTela('sucesso');
}

function voltarAguardar() {
  corridaAtual = null;
  timestampLargada = null;
  tempos = [];
  if (intervalCronometro) clearInterval(intervalCronometro);
  intervalCronometro = null;
  
  mostrarTela('aguardando');
  iniciarPolling();
}

// ============ INICIO ============
log('Sistema iniciado', 'info');
iniciarPolling();
</script>

</body>
</html>
)HTML";
  
  server.send(200, "text/html", html);
}

// ============ PROXY API LoRa ============
void handleAPI() {
  String path = server.uri();
  
  // Aceita /api/* e /cronometragem/*
  if (!path.startsWith("/api/") && !path.startsWith("/cronometragem/")) {
    server.send(404, "application/json", "{\"error\":\"Not Found\"}");
    return;
  }
  
  StaticJsonDocument<512> doc;
  
  // Metodo HTTP
  String metodo = "GET";
  if (server.method() == HTTP_POST) metodo = "POST";
  else if (server.method() == HTTP_PUT) metodo = "PUT";
  else if (server.method() == HTTP_DELETE) metodo = "DELETE";
  else if (server.method() == HTTP_PATCH) metodo = "PATCH";
  doc["method"] = metodo;
  
  // URL com query string (EXCLUI "plain" que e o body!)
  String urlCompleta = path;
  bool primeiraQuery = true;
  for (int i = 0; i < server.args(); i++) {
    if (server.argName(i) == "plain") continue;
    urlCompleta += primeiraQuery ? "?" : "&";
    urlCompleta += server.argName(i) + "=" + server.arg(i);
    primeiraQuery = false;
  }
  doc["endpoint"] = urlCompleta;
  
  // Body para POST/PUT/PATCH
  if (metodo == "POST" || metodo == "PUT" || metodo == "PATCH") {
    if (server.hasArg("plain") && server.arg("plain").length() > 0) {
      StaticJsonDocument<256> bodyDoc;
      DeserializationError erro = deserializeJson(bodyDoc, server.arg("plain"));
      if (!erro) {
        doc["body"] = bodyDoc;
      }
    }
  }
  
  String requisicaoJson;
  serializeJson(doc, requisicaoJson);
  
  Serial.print("\n📤 "); Serial.print(metodo);
  Serial.print(" "); Serial.println(urlCompleta);
  
  // Enviar via LoRa
  LoRa.beginPacket();
  LoRa.print(requisicaoJson);
  LoRa.endPacket();
  
  // Aguardar resposta com timeout
  String resposta = "";
  unsigned long inicio = millis();
  
  while (millis() - inicio < TIMEOUT_LORA) {
    int packetSize = LoRa.parsePacket();
    if (packetSize) {
      resposta = "";
      while (LoRa.available()) {
        resposta += (char)LoRa.read();
      }
      // Validar se e resposta (tem "status")
      if (resposta.indexOf("\"status\"") != -1) break;
      resposta = "";
    }
    delay(10);
  }
  
  if (resposta.length() > 0) {
    int rssi = LoRa.packetRssi();
    Serial.print("📥 "); Serial.print(resposta.length());
    Serial.print(" bytes RSSI:"); Serial.println(rssi);
    
    StaticJsonDocument<1024> respostaDoc;
    DeserializationError erro = deserializeJson(respostaDoc, resposta);
    
    if (!erro) {
      int statusCode = respostaDoc["status"] | 200;
      String body;
      serializeJson(respostaDoc["body"], body);
      server.send(statusCode, "application/json", body);
      Serial.print("📱 "); Serial.println(statusCode);
    } else {
      Serial.println("❌ Parse error");
      server.send(500, "application/json", "{\"error\":\"Parse error\"}");
    }
  } else {
    Serial.println("⏱️ Timeout");
    server.send(504, "application/json", "{\"error\":\"Gateway Timeout\"}");
  }
}
