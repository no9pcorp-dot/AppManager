# AppManager

Ứng dụng Android Kotlin/Jetpack Compose để xem các ứng dụng được PackageManager cho phép truy vấn và thông tin dung lượng.

## Build

- JDK 17. Trong Android Studio, chọn Gradle JDK 17 trong Settings > Build Tools > Gradle.
- Android SDK Platform 35 và Build Tools 34.0.0.
- Gradle Wrapper 8.10.2, Android Gradle Plugin 8.7.3, Kotlin 2.0.21.
- Mở thư mục gốc trong Android Studio rồi Sync, hoặc chạy trên Windows:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

APK debug được tạo tại `app/build/outputs/apk/debug/app-debug.apk`. Lần build đầu cần mạng để tải Gradle và dependencies. `local.properties` là cấu hình SDK riêng của máy, đã được bỏ qua trong Git.

## Phạm vi hiện tại

- Danh sách được giới hạn bởi package visibility trên Android 11 trở lên; `queries` không đảm bảo nhìn thấy mọi ứng dụng hệ thống hoặc ứng dụng không có launcher.
- Usage Access cần người dùng cấp trong Settings để đọc dung lượng của ứng dụng khác. API StorageStats có từ Android 8.0.
- Dung lượng APK gồm APK chính và split APK. Dung lượng ứng dụng do Android báo có thể gồm thư viện và mã tối ưu, nên khác dung lượng riêng các file APK.
- Sao lưu/chia sẻ APK, màn File, Thống kê và Cài đặt đang phát triển.
