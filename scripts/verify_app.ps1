# Automated Verification Pipeline for YourRefri
$adb = "E:\Dev\Android\Sdk\platform-tools\adb.exe"
$outDir = "E:\KMM App\build\verification_reports"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

Write-Host "=== [1/4] Chạy Unit Tests ===" -ForegroundColor Cyan
& ".\gradlew.bat" :composeApp:testDebugUnitTest --build-cache
if ($LASTEXITCODE -ne 0) {
    Write-Error "Unit Tests thất bại!"
    exit 1
}

Write-Host "=== [2/4] Build Debug APK ===" -ForegroundColor Cyan
& ".\gradlew.bat" :composeApp:assembleDebug --build-cache
if ($LASTEXITCODE -ne 0) {
    Write-Error "Build APK thất bại!"
    exit 1
}

Write-Host "=== [3/4] Cài đặt & Khởi động trên Emulator ===" -ForegroundColor Cyan
& $adb install -r "E:\KMM App\composeApp\build\outputs\apk\debug\composeApp-debug.apk"
& $adb shell am start -n com.loitu.yourrefri/.MainActivity
Start-Sleep -Seconds 2

Write-Host "=== [4/4] Chạy Visual Verification Scenarios ===" -ForegroundColor Cyan

# 1. Capture Home
cmd.exe /c "`"$adb`" exec-out screencap -p > `"$outDir\verify_1_home.png`""
Write-Host "-> Captured: verify_1_home.png" -ForegroundColor Green

# 2. Tap Pantry Tab (X=405, Y=2250)
& $adb shell input tap 405 2250
Start-Sleep -Seconds 1
cmd.exe /c "`"$adb`" exec-out screencap -p > `"$outDir\verify_2_pantry.png`""
Write-Host "-> Captured: verify_2_pantry.png" -ForegroundColor Green

# 3. Tap Freezer Filter (X=650, Y=350)
& $adb shell input tap 650 350
Start-Sleep -Seconds 1
cmd.exe /c "`"$adb`" exec-out screencap -p > `"$outDir\verify_3_freezer.png`""
Write-Host "-> Captured: verify_3_freezer.png" -ForegroundColor Green

# 4. Tap Recipes Tab (X=675, Y=2250)
& $adb shell input tap 675 2250
Start-Sleep -Seconds 1
cmd.exe /c "`"$adb`" exec-out screencap -p > `"$outDir\verify_4_recipes.png`""
Write-Host "-> Captured: verify_4_recipes.png" -ForegroundColor Green

Write-Host "=== HOÀN TẤT TOÀN BỘ VERIFICATION ===" -ForegroundColor Green
