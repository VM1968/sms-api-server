# SMS API Server - Примеры запросов

## Запуск сервера
1. Установите приложение на Android
2. Нажмите кнопку "START"
3. Получите IP адрес из приложения
4. Используйте `http://IP:8080` для запросов

## API Endpoints

### 1. Отправка СМС
**POST** `/api/sms/send`

```bash
curl -X POST http://192.168.1.100:8080/api/sms/send \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+79991234567",
    "message": "Hello from SMS Server!"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "SMS sent successfully"
}
```

### 2. Статус сервера
**GET** `/api/server/status`

```bash
curl http://192.168.1.100:8080/api/server/status
```

**Response:**
```json
{
  "isRunning": true,
  "timestamp": "2026-05-19 14:30:45",
  "ipAddress": "192.168.1.100",
  "port": 8080,
  "uptime": "Active"
}
```

### 3. Информация об устройстве
**GET** `/api/device/info`

```bash
curl http://192.168.1.100:8080/api/device/info
```

**Response:**
```json
{
  "model": "Samsung Galaxy A12",
  "manufacturer": "samsung",
  "androidVersion": "12",
  "sdkInt": 31,
  "ipAddress": "192.168.1.100",
  "hostname": "localhost"
}
```

### 4. История СМС
**GET** `/api/sms/history`

```bash
curl http://192.168.1.100:8080/api/sms/history
```

### 5. Очистка истории
**DELETE** `/api/sms/history`

```bash
curl -X DELETE http://192.168.1.100:8080/api/sms/history
```

### 6. Проверка здоровья
**GET** `/api/health`

```bash
curl http://192.168.1.100:8080/api/health
```

## Python пример

```python
import requests
import json

BASE_URL = "http://192.168.1.100:8080"

def send_sms(phone, message):
    response = requests.post(
        f"{BASE_URL}/api/sms/send",
        json={"phoneNumber": phone, "message": message},
        headers={"Content-Type": "application/json"}
    )
    return response.json()

# Отправка СМС
result = send_sms("+79991234567", "Test message")
print(result)
```

## JavaScript пример

```javascript
const BASE_URL = "http://192.168.1.100:8080";

async function sendSms(phone, message) {
  const response = await fetch(`${BASE_URL}/api/sms/send`, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({phoneNumber: phone, message: message})
  });
  return response.json();
}

sendSms("+79991234567", "Test message").then(console.log);
```
