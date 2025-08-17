# 🌱 IoT Smart Plant Care System

Dự án này là hệ thống **IoT chăm sóc cây thông minh** sử dụng **ESP32**, **ESP32-S3**, **ThingsBoard Cloud** và **Android App (Jetpack Compose)**.  
Mục tiêu là giám sát độ ẩm đất và điều khiển hệ thống tưới cây từ xa thông qua ứng dụng di động.

---

## 📂 Cấu trúc dự án

Dự án được chia thành 3 phần chính:

### 1. Gateway (ESP32)
- **Phần cứng**:
  - Nguồn: sử dụng điện gia đình (AC → DC adapter).
  - Kết nối 4 cảm biến độ ẩm đất tại các chân:
    - GPIO26 → Sensor 1  
    - GPIO27 → Sensor 2  
    - GPIO32 → Sensor 3  
    - GPIO33 → Sensor 4  

- **Chức năng**:
  - Thu thập dữ liệu độ ẩm từ các sensor.
  - Đóng vai trò **Gateway** gửi dữ liệu lên **ThingsBoard** qua MQTT/HTTP.
  - Nhận lệnh RPC từ ThingsBoard để điều khiển bơm/tưới cây.

- **Cấu trúc file**:
/gateway
├── gateway.ino # Code chính ESP32
├── config.h # Cấu hình WiFi, MQTT, ThingsBoard token
├── sensors.h # Xử lý đọc giá trị 4 cảm biến độ ẩm
├── mqtt_client.h # Gửi/nhận dữ liệu từ ThingsBoard
└── actuator.h # Điều khiển bơm / relay

---

### 2. Sensor Node (ESP32-S3)
- **Phần cứng**:
- Nguồn: sử dụng pin (thiết bị di động, low-power).
- 1 cảm biến độ ẩm đất nối với:
  - GPIO4  

- **Chức năng**:
- Định kỳ thức dậy từ chế độ **Deep Sleep**, đo giá trị độ ẩm.
- Gửi dữ liệu tới **Gateway** hoặc trực tiếp lên **ThingsBoard**.
- Tiết kiệm năng lượng tối đa.

- **Cấu trúc file**:
/sensor
├── sensor.ino # Code chính ESP32-S3
├── config.h # Cấu hình WiFi, ThingsBoard token
├── sleep.h # Quản lý Deep Sleep
└── sensor_driver.h # Đọc dữ liệu cảm biến độ ẩm

---

### 3. Android App (Jetpack Compose, Kotlin)
- **Chức năng**:
- Đăng nhập / đăng ký user (ThingsBoard Customers).
- Hiển thị danh sách Gateway & Sensor (Asset / Device trên ThingsBoard).
- Hiển thị dữ liệu độ ẩm theo thời gian thực.
- Gửi lệnh điều khiển bơm thông qua API ThingsBoard.
- **Quản lý thiết bị**:
  - Thêm mới Boss/Staff:
    - Tạo Asset (Boss).
    - Tạo Device (Staff).
    - Lấy Device Credentials (token).
    - Gán quan hệ Asset–Device.
  - Xóa thiết bị/asset khi không sử dụng nữa.

- **Cấu trúc file**:
/android
├── app/
│ ├── ui/ # Giao diện Compose (BossListScreen, NewDeviceScreen...)
│ ├── network/ # Xử lý API call ThingsBoard
│ ├── model/ # Data class cho Asset, Device, Telemetry
│ ├── repository/ # Repository quản lý dữ liệu
│ └── MainActivity.kt

---

## 🔗 API sử dụng (ThingsBoard)

Ứng dụng Android và Gateway/Sensor giao tiếp với **ThingsBoard** thông qua các API:

- **Gửi dữ liệu telemetry từ thiết bị (ESP32/ESP32-S3):**

- **Điều khiển thiết bị từ App (RPC):**

- **Quản lý Customer/Asset/Device (App):**
- Lấy danh sách Asset theo Customer:
  ```
  GET /api/customer/{customerId}/assetInfos
  ```
- Lấy danh sách quan hệ (Asset → Device):
  ```
  GET /api/relations
  ```
- Lấy thông tin chi tiết thiết bị:
  ```
  GET /api/device/{deviceId}
  ```
- Lấy credentials (token) của thiết bị:
  ```
  GET /api/device/{deviceId}/credentials
  ```
- Gán thiết bị vào Asset:
  ```
  POST /api/relations
  ```
- **Thêm thiết bị:**
  ```
  POST /api/deviceProfile
  POST /api/device
  ```
- **Xóa thiết bị/asset:**
  ```
  DELETE /api/device/{deviceId}
  DELETE /api/asset/{assetId}
  ```

---

## ⚙️ Cơ chế hoạt động

1. **Sensor Node (ESP32-S3)**:
 - Thức dậy từ chế độ Deep Sleep theo chu kỳ.
 - Đọc giá trị độ ẩm tại GPIO4.
 - Gửi dữ liệu lên Gateway hoặc trực tiếp ThingsBoard.
 - Quay lại chế độ Deep Sleep để tiết kiệm pin.

2. **Gateway (ESP32)**:
 - Thu thập dữ liệu từ 4 cảm biến độ ẩm đất (GPIO26, 27, 32, 33).
 - Gửi dữ liệu telemetry lên ThingsBoard.
 - Lắng nghe lệnh điều khiển RPC từ App (qua ThingsBoard).
 - Thực thi lệnh (bật/tắt bơm tưới cây).

3. **Android App**:
 - Người dùng đăng nhập / đăng ký tài khoản.
 - App lấy danh sách Asset và Device từ ThingsBoard.
 - Hiển thị dữ liệu độ ẩm và trạng thái hệ thống.
 - Cho phép **thêm/xóa thiết bị** để mở rộng hoặc thay đổi cấu hình hệ thống.
 - Khi người dùng nhấn **Tưới cây**, App gửi lệnh RPC → ThingsBoard → Gateway → Bơm.

---

## 🚀 Kết quả
- Người dùng có thể theo dõi và điều khiển hệ thống tưới cây từ bất kỳ đâu.
- Sensor node tiết kiệm pin nhờ **Deep Sleep**.
- Gateway duy trì kết nối ổn định với cloud.
- App trực quan, dễ sử dụng, hỗ trợ **quản lý vòng đời thiết bị (Add/Delete)**.
