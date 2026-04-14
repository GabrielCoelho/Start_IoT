# START IoT — Instruções de Configuração e Execução Local

## Pré-requisitos

| Ferramenta | Versão recomendada | Observação |
|---|---|---|
| Java (JDK) | **21.0.10** | Versões 22+ quebram o Lombok. Use sdkman: `sdk use java 21.0.10-amzn` |
| Maven | 3.9.x | Disponível via sdkman: `sdk use maven 3.9.13` |
| PostgreSQL | 12+ | Deve estar rodando localmente antes de iniciar a aplicação |

---

## 1. Configurar o banco de dados

Crie o banco no PostgreSQL antes de rodar a aplicação:

```sql
CREATE DATABASE startiot;
CREATE USER startiot_user WITH PASSWORD 'sua_senha';
GRANT ALL PRIVILEGES ON DATABASE startiot TO startiot_user;
```

> O Hibernate está configurado com `ddl-auto: update` — as tabelas são criadas/atualizadas automaticamente na primeira execução.

---

## 2. Criar o arquivo `application-local.yml`

Este arquivo **não está no repositório** (está no `.gitignore`) por conter credenciais. Cada desenvolvedor deve criá-lo manualmente.

Crie o arquivo em:
```
src/main/resources/application-local.yml
```

Com o seguinte conteúdo (ajuste os valores conforme seu ambiente):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/startiot
    username: startiot_user
    password: sua_senha
```

> **Atenção:** A URL deve começar com `jdbc:` (não `jbdc:`). Erros de digitação nessa string causam falha silenciosa no driver PostgreSQL.

---

## 3. Verificar se o PostgreSQL está acessível

```bash
psql -U startiot_user -d startiot -h localhost
```

Se conectar, está pronto. Se receber erro de conexão recusada, inicie o serviço:

```bash
# Linux (systemd)
sudo systemctl start postgresql

# macOS (Homebrew)
brew services start postgresql
```

---

## 4. Rodar a aplicação

Sempre execute com o profile `local` ativo para que o `application-local.yml` seja carregado:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

A aplicação sobe na porta `8080`. Acesse o Swagger UI em:

```
http://localhost:8080/swagger-ui.html
```

---

## 5. Rodar apenas a compilação (sem subir o servidor)

```bash
mvn compile
```

---

## Problemas comuns

### `Driver claims to not accept jdbcUrl, ${DB_URL}`
O profile `local` não está ativo. Rode com `-Dspring-boot.run.profiles=local`.

### `Driver claims to not accept jdbcUrl, jbdc:postgresql://...`
Typo na URL do `application-local.yml`. Corrija para `jdbc:` (sem trocar a ordem das letras).

### `cannot find symbol: method getNome()` (erros de Lombok)
O JDK ativo é incompatível com o Lombok. Mude para Java 21:
```bash
sdk use java 21.0.10-amzn
```

### `Connection refused` ao conectar no PostgreSQL
O serviço do PostgreSQL não está rodando. Veja o passo 3 acima.

### Porta 8080 já em uso
```bash
# Encontrar o processo
lsof -i :8080

# Encerrar
kill -9 <PID>
```
