#include <WiFi.h>
#include <WiFiUdp.h>
#include <EEPROM.h>

#define EEPROM_SIZE 256
#define SENSOR_PIN 0  
#define CLEAR_BUTTON 9 

const char* apSSID = "ESP_SETUP";
const char* apPassword = "12345678";

WiFiUDP udp;
IPAddress localIP(192, 168, 4, 1);
IPAddress gateway(192, 168, 4, 1);
IPAddress subnet(255, 255, 255, 0); 

const int udpPort = 1234;
char homeSSID[32], homePassword[32], token[64], humid[5];
bool wifiConfigured = false;

void clearEEPROM() {
    EEPROM.begin(EEPROM_SIZE);
    for (int i = 0; i < EEPROM_SIZE; i++) EEPROM.write(i, 0);
    EEPROM.commit();
    Serial.println("EEPROM cleared!");
}

void writeToEEPROM(const char* ssid, const char* password, const char* token_val, const char* humid) {
    EEPROM.begin(EEPROM_SIZE);
    for (int i = 0; i < EEPROM_SIZE; i++) EEPROM.write(i, 0);

    for (int i = 0; i < 31 && ssid[i]; i++) EEPROM.write(i, ssid[i]);
    EEPROM.write(31, 0);

    for (int i = 0; i < 31 && password[i]; i++) EEPROM.write(32 + i, password[i]);
    EEPROM.write(63, 0);

    for (int i = 0; i < 63 && token_val[i]; i++) EEPROM.write(64 + i, token_val[i]);
    EEPROM.write(127, 0);

    for (int i = 0; i < 4 && humid[i]; i++) EEPROM.write(128 + i, humid[i]);
    EEPROM.write(132, 0);

    EEPROM.commit();
    Serial.println("Saved to EEPROM.");
}

void readFromEEPROM() {
    EEPROM.begin(EEPROM_SIZE);
    for (int i = 0; i < 31; i++) {
        homeSSID[i] = EEPROM.read(i);
        if (homeSSID[i] == 0) break;
    }
    homeSSID[31] = 0;

    for (int i = 0; i < 31; i++) {
        homePassword[i] = EEPROM.read(32 + i);
        if (homePassword[i] == 0) break;
    }
    homePassword[31] = 0;

    for (int i = 0; i < 63; i++) {
        token[i] = EEPROM.read(64 + i);
        if (token[i] == 0) break;
    }
    token[63] = 0;

    for (int i = 0; i < 4; i++) {
        humid[i] = EEPROM.read(128 + i);
        if (humid[i] == 0) break;
    }
    humid[4] = 0;

    Serial.printf("SSID: %s | PASS: %s | TOKEN: %s | HUMID: %s\n", homeSSID, homePassword, token, humid);
}

volatile bool buttonPressed = false;
volatile unsigned long lastPressTime = 0;
volatile bool isHold = false;

void IRAM_ATTR clearInterrupt() {
    if (digitalRead(CLEAR_BUTTON) == LOW) {
        lastPressTime = millis();
        buttonPressed = true;
        isHold = false;
    } else {
        unsigned long pressDuration = millis() - lastPressTime;
        if (pressDuration >= 3000) isHold = true;
        buttonPressed = false;
    }

    if (!buttonPressed && lastPressTime > 0) {
        if (isHold) {
            Serial.println("HOLD");
            udp.beginPacket("192.168.1.8", udpPort);
            udp.println(WiFi.localIP().toString() + "//Setting");
            udp.endPacket();
        } else {
            Serial.println("CLICK");
            clearEEPROM();
        }
        lastPressTime = 0;
    }
}

void setupAPMode() {
    Serial.println("Starting AP mode...");
    WiFi.mode(WIFI_OFF);
    delay(100);
    WiFi.mode(WIFI_AP);
    WiFi.softAPConfig(localIP, gateway, subnet);
    WiFi.softAP(apSSID, apPassword);
    udp.begin(udpPort);
    Serial.println("AP Mode ready.");
}

void setup() {
    pinMode(CLEAR_BUTTON, INPUT_PULLUP);
    attachInterrupt(digitalPinToInterrupt(CLEAR_BUTTON), clearInterrupt, CHANGE);

    Serial.begin(115200);
    EEPROM.begin(EEPROM_SIZE);
    readFromEEPROM();


    if (strlen(homeSSID) > 0 && strlen(homePassword) > 0) wifiConfigured = true;
    if (!wifiConfigured) setupAPMode();
    else {
        WiFi.mode(WIFI_STA);
        WiFi.begin(homeSSID, homePassword);
        Serial.print("Connecting WiFi...");
        int timeout = 0;
        while (WiFi.status() != WL_CONNECTED && timeout++ < 20) {
            delay(1000);
            Serial.print(".");
        }
        if (WiFi.status() == WL_CONNECTED) {
            Serial.println("\nConnected: " + WiFi.localIP().toString());
        } else {
            Serial.println("\nFailed WiFi. Switching to AP mode.");
            wifiConfigured = false;
            setupAPMode();
        }
    }
}

void loop() {
    if (!wifiConfigured) {
        int packetSize = udp.parsePacket();
        if (packetSize) {
            char packetBuffer[256];
            int len = udp.read(packetBuffer, 255);
            if (len > 0) {
                packetBuffer[len] = 0;
                Serial.println("Received: " + String(packetBuffer));

                char* ssid = strtok(packetBuffer, "/");
                char* password = strtok(NULL, "/");
                char* token_val = strtok(NULL, "/");
                char* humid_val = strtok(NULL, "/");

                if (ssid && password && token_val) {
                    writeToEEPROM(ssid, password, token_val, humid_val);
                    String msg = WiFi.macAddress() + "//" + String(token_val) + "//" + String(analogRead(SENSOR_PIN)) + "//" + String(humid_val);
                    udp.beginPacket("192.168.1.8", udpPort);
                    udp.print(msg);
                    udp.endPacket();
                    Serial.println("Sent: " + msg);
                    ESP.restart();
                }
            }
        }
    } else {
        String msg = WiFi.macAddress() + "//" + String(token) + "//" + String(analogRead(SENSOR_PIN)) + "//" + String(humid);
        delay(3000);
        udp.beginPacket("192.168.1.8", udpPort);
        udp.print(msg);
        udp.endPacket();
        Serial.println("Sent: " + msg);
        delay(5000);

        esp_sleep_enable_timer_wakeup(7200000000); // 2 giờ
        esp_deep_sleep_start();
    }
}
