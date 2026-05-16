/*
 * ESP32_Topo_Bridge.ino
 * 
 * Bridge: LoRa ↔ Serial (Python)
 * Buzina: GPIO25 (via relé)
 * 
 * Config LoRa: SF10 BW125 433MHz
 */

#include <LoRa.h>
#include <ArduinoJson.h>

// ===== CONFIG LoRa =====
#define LORA_SS    5
#define LORA_RST   14
#define LORA_DIO0  2
#define SF         10
#define BW         125000
#define FREQ       433E6

// ===== CONFIG Buzina =====
#define BUZINA_PIN 25

void setup() {
  Serial.begin(115200);
  
  // Buzina
  pinMode(BUZINA_PIN, OUTPUT);
  digitalWrite(BUZINA_PIN, LOW);
  
  // LoRa
  LoRa.setPins(LORA_SS, LORA_RST, LORA_DIO0);
  if (!LoRa.begin(FREQ)) {
    Serial.println("{\"erro\":\"LoRa init failed\"}");
    while (1);
  }
  LoRa.setSpreadingFactor(SF);
  LoRa.setSignalBandwidth(BW);
  
  Serial.println("{\"status\":\"ready\",\"sf\":10,\"bw\":125,\"freq\":433}");
}

void loop() {
  // ===== COMANDOS DO PYTHON (Serial) =====
  if (Serial.available()) {
    String linha = Serial.readStringUntil('\n');
    linha.trim();
    
    if (linha.length() == 0) return;
    
    // Parse JSON
    StaticJsonDocument<512> doc;
    DeserializationError error = deserializeJson(doc, linha);
    
    if (error) {
      Serial.println("{\"erro\":\"JSON inválido\"}");
      return;
    }
    
    // COMANDO: Ligar buzina
    if (doc.containsKey("cmd") && doc["cmd"] == "BUZINA_ON") {
      digitalWrite(BUZINA_PIN, HIGH);
      
      // Notifica chegada via LoRa
      LoRa.beginPacket();
      LoRa.print("{\"event\":\"LARGADA\"}");
      LoRa.endPacket();
      
      Serial.println("{\"buzina\":\"on\"}");
    }
    
    // COMANDO: Desligar buzina
    else if (doc.containsKey("cmd") && doc["cmd"] == "BUZINA_OFF") {
      digitalWrite(BUZINA_PIN, LOW);
      Serial.println("{\"buzina\":\"off\"}");
    }
    
    // RESPOSTA API (para enviar via LoRa)
    else if (doc.containsKey("status") && doc.containsKey("body")) {
      // Python mandou resposta da API
      // Envia via LoRa para chegada
      
      String response;
      serializeJson(doc, response);
      
      // Fragmenta se > 200 bytes
      if (response.length() > 200) {
        // TODO: fragmentação
        Serial.println("{\"aviso\":\"resposta grande: " + String(response.length()) + " bytes\"}");
      }
      
      LoRa.beginPacket();
      LoRa.print(response);
      LoRa.endPacket();
      
      Serial.println("{\"lora_tx\":\"ok\"}");
    }
  }
  
  // ===== MENSAGENS DO LoRa (Chegada) =====
  int packetSize = LoRa.parsePacket();
  if (packetSize) {
    String msg = "";
    while (LoRa.available()) {
      msg += (char)LoRa.read();
    }
    
    // Envia para Python processar
    Serial.println(msg);
  }
  
  delay(2);
}
