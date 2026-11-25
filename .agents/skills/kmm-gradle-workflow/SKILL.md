---
name: kmm-gradle-workflow
description: Lệnh chuẩn và môi trường chạy test, compile cho Kotlin Multiplatform trên Windows
---

# KMM Gradle Workflow Guide

## Môi trường chuẩn (Windows PowerShell)
- JAVA_HOME: `C:\Setups\JavaJDK\jdk-21.0.9`
- ANDROID_HOME: `C:\Users\anhvb2\AppData\Local\Android\Sdk`
- ADB: `C:\Users\anhvb2\AppData\Local\Android\Sdk\platform-tools\adb.exe`

## Lệnh kiểm thử & Build
```powershell
$env:JAVA_HOME = "C:\Setups\JavaJDK\jdk-21.0.9"
$env:ANDROID_HOME = "C:\Users\anhvb2\AppData\Local\Android\Sdk"

# Chạy Unit test cho cả shared + composeApp và build APK (chỉ chạy 1 tiến trình Gradle tại một thời điểm)
.\gradlew.bat :shared:testDebugUnitTest :composeApp:testDebugUnitTest :composeApp:assembleDebug --no-configuration-cache --max-workers=1
```

## Chống lỗi thực chiến Windows PowerShell 5.1 & Đa CLI
- **Gradle:** Chỉ chạy 1 tiến trình Gradle tại một thời điểm, luôn thêm `--no-configuration-cache --max-workers=1`.
- **Chụp ảnh ADB:** Tuyệt đối KHÔNG dùng `adb exec-out screencap -p > out.png` (PowerShell 5.1 ghi UTF-16LE làm hỏng PNG). Luôn dùng `adb shell screencap -p /sdcard/out.png` rồi `adb pull` và xóa file trên `/sdcard/`.
- **PowerShell 5.1:** Không có `&&` (dùng `;` và kiểm `$LASTEXITCODE`); không đặt tên biến `$pid` (biến hệ thống chỉ đọc).
- **Claude CLI:** Mặc định là `plan` mode → thêm `--permission-mode acceptEdits` khi sửa file; khi review/xem ảnh chỉ đọc dùng `--allowedTools "Read,Grep,Glob,Bash(git diff:*)"`.
- **Codex CLI:** Truyền prompt qua stdin `-`, dùng `-m gpt-6.2-sol -c 'model_reasoning_effort="medium|high"'` (`-s workspace-write` hoặc `-s read-only`), gắn ảnh kiểm chứng bằng `--image <path.png>`.
- **JEV (`scripts/jev.ps1`):** Đặt `[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12` trước khi gọi; nếu API mạng ngoài gặp `EPROTO`, ghi nhận minh bạch thay vì retry vô hạn.
- **Copy lint (`scripts/lint-copy.ps1`):** Luôn truyền `-Path <danh sách file>`, không pipe stdin.

## Lưu ý về Android Studio vs CLI
- Khi người dùng muốn xem kết quả trực tiếp trong Android Studio: Cung cấp mã test hoặc hướng dẫn vị trí file để người dùng bấm nút Play / Run test trong Android Studio nếu cần.
- Báo cáo kết quả phải trung thực: Chỉ xác nhận xanh khi lệnh CLI thực sự trả về 0 hoặc có log/ảnh chụp xác nhận từ người dùng.
