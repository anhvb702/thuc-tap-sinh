---
name: kmm-gradle-workflow
description: Lệnh chuẩn và môi trường chạy test, compile cho Kotlin Multiplatform trên Windows
---

# KMM Gradle Workflow Guide

## Môi trường chuẩn (Windows PowerShell)
- JAVA_HOME: `E:\Dev\Java\jdk-21.0.13+9`
- ANDROID_HOME: `E:\Dev\Android\Sdk`

## Lệnh kiểm thử & Build
```powershell
$env:JAVA_HOME = "E:\Dev\Java\jdk-21.0.13+9"
$env:ANDROID_HOME = "E:\Dev\Android\Sdk"

# Chạy Unit test cho module shared (data + logic)
.\gradlew.bat :shared:testDebugUnitTest

# Compile kiểm tra module UI
.\gradlew.bat :composeApp:assembleDebug
```

## Lưu ý về Android Studio vs CLI
- Khi người dùng muốn xem kết quả trực tiếp trong Android Studio: Cung cấp mã test hoặc hướng dẫn vị trí file để người dùng bấm nút Play / Run test trong Android Studio nếu cần.
- Báo cáo kết quả phải trung thực: Chỉ xác nhận xanh khi lệnh CLI thực sự trả về 0 hoặc có log/ảnh chụp xác nhận từ người dùng.
