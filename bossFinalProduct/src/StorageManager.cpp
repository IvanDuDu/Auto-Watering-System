#include "StorageManager.h"
#include "Config.h"

StorageManager storageManager;

StorageManager::StorageManager() {
}

StorageManager::~StorageManager() {
    preferences.end();
}

void StorageManager::begin() {
    EEPROM.begin(EEPROM_SIZE);
        pinMode(13, OUTPUT);
        digitalWrite(13, LOW);

}

void StorageManager::clearEEPROM() {
    EEPROM.begin(EEPROM_SIZE);
    for (int i = 0; i < EEPROM_SIZE; i++) {
        EEPROM.write(i, 0xFF);
    }
    EEPROM.commit();
    Serial.println("EEPROM cleared!");
    digitalWrite(13,HIGH);
    delay(500);
    digitalWrite(13,LOW);
    delay(500);
    digitalWrite(13,HIGH);
    delay(500);
    digitalWrite(13,LOW);
    delay(500);
}

void StorageManager::writeMACsToPROM(char MACaddress[][18],char tokenAddress[][80],int humidity[], int count) {
    EEPROM.begin(EEPROM_SIZE);
    clearEEPROM();

    int addr = 0;
    for (int i = 0; i < count; i++) {
        String entry = String(i) + "=" + String(MACaddress[i])+ "?" +String(tokenAddress[i]) + "|"+String(humidity[i])+"/";
        for (int j = 0; j < entry.length(); j++) {
            EEPROM.write(addr++, entry[j]);
        }
    }

    EEPROM.write(addr, '\0');
    EEPROM.commit();
    Serial.println("MAC addresses and token saved to EEPROM!");
    digitalWrite(13,HIGH);
    delay(500);
    digitalWrite(13,LOW);
    delay(500);
    digitalWrite(13,HIGH);
    delay(500);
    digitalWrite(13,LOW);
    delay(500);


}

int StorageManager::readMACsFromPROM(char MACaddress[][18], char tokenAddress[][80], int Humidities[]) {
    EEPROM.begin(EEPROM_SIZE);

    char buffer[EEPROM_SIZE + 1]; // +1 để thêm null terminator
    for (int i = 0; i < EEPROM_SIZE; i++) {
        buffer[i] = EEPROM.read(i);
    }
    buffer[EEPROM_SIZE] = '\0'; // đảm bảo kết thúc chuỗi

    Serial.println("EEPROM Data:");
    Serial.println(buffer);

    int count = 0;
    char *token = strtok(buffer, "/");  // tách từng cặp "index=MAC:TOKEN|humidity"
    while (token != NULL) {
        int index;
        char mac[18];
        char t[80];
        int humidity;

        // parse dữ liệu
        if (sscanf(token, "%d=%17[^?]?%79[^|]|%d", &index, mac, t, &humidity) == 4) {
            if (index >= 0 && index < MAX_PIN) {
                strcpy(MACaddress[index], mac);
                strcpy(tokenAddress[index], t);
                Humidities[index] = humidity;
                count++;
            }
        } else {
            Serial.print("Parse lỗi ở token: ");
            Serial.println(token);
        }

        token = strtok(NULL, "/");
    }

    Serial.printf("Loaded %d devices from EEPROM\n", count);
     digitalWrite(13,HIGH);
    delay(500);
    digitalWrite(13,LOW);
    delay(500);
    digitalWrite(13,HIGH);
    delay(500);
    digitalWrite(13,LOW);
    delay(500);
    return count;
}




void StorageManager::writeToNVS(const char* key, const char* value) {
    preferences.begin("storage", false);
    preferences.putString(key, value);
    preferences.end();
    Serial.printf("NVS: Saved %s = %s\n", key, value);
    digitalWrite(13,HIGH);
    delay(500);
    digitalWrite(13,LOW);

}

String StorageManager::readFromNVS(const char* key) {
    preferences.begin("storage", true);
    String value = preferences.getString(key, "");
    preferences.end();
    return value;
}

void StorageManager::clearWiFiCredentials() {
    Serial.println("Clearing WiFi credentials...");
    preferences.begin("storage", false);
    preferences.remove("ssid");
    preferences.remove("password");
    preferences.remove("mqtt_token");
    preferences.remove("mqttName");
    preferences.remove("bossID");

    preferences.end();
    Serial.println("WiFi credentials cleared!");
    digitalWrite(13,HIGH);
    delay(500);
    digitalWrite(13,LOW);
}
