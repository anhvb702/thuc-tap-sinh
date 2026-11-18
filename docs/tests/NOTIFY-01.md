# NOTIFY-01 — Kênh nhắc ăn uống có popup (heads-up)

## Hợp đồng

### Task
Kênh thông báo nhắc ăn uống dùng `IMPORTANCE_HIGH` để thông báo hiện popup, không chỉ nằm trên thanh thông báo. Kênh cũ `daily_reminder` (DEFAULT) không đổi được mức quan trọng, nên kênh mới dùng id `daily_reminder_hi`; kênh cũ bị xóa khi app tạo kênh mới.

### Input
Không có state UI. Đầu vào là hằng số của `ReminderChannel`.

### Test

| Test | Input | Expected Output | Mutation bắt |
| --- | --- | --- | --- |
| `newChannelIsHighImportanceSoReminderShowsAsPopup` | `ReminderChannel.IMPORTANCE` | bằng `NotificationManager.IMPORTANCE_HIGH` | đổi về `IMPORTANCE_DEFAULT` |
| `newChannelUsesNewIdBecauseOldChannelImportanceCannotChange` | `ID`, `LEGACY_ID` | `daily_reminder_hi` và `daily_reminder`, khác nhau | đặt `ID` trùng `LEGACY_ID` |

### Biên và rủi ro
- Máy đã có kênh cũ: nếu người dùng đã chặn kênh cũ (`IMPORTANCE_NONE`), app không gửi thông báo, không tạo kênh mới. Nếu kênh cũ đang bật, app tạo kênh mới và xóa kênh cũ; âm thanh/rung đã chỉnh ở kênh cũ sẽ mất.
- Hãng máy (Xiaomi, Samsung, Oppo) có thể chặn popup; chưa kiểm trên máy thật.

### Sáu mutation
1. Đổi `IMPORTANCE` về `IMPORTANCE_DEFAULT` — **bị bắt** (1 test đỏ).
2. Đặt `ID` trùng `LEGACY_ID` — **bị bắt** (3 test đỏ).
3. Đảo `shouldNotify` (`==` thay `!=`) — **bị bắt** (4 test đỏ).
4. `shouldNotify` luôn trả true (bỏ kiểm tra người dùng đã chặn kênh cũ) — **bị bắt** (3 test đỏ).
5. Bỏ `deleteNotificationChannel(LEGACY_ID)` — **sống sót (không có test đơn vị)**; cần kiểm trên thiết bị hoặc Robolectric.
6. Bỏ `setDefaults(DEFAULT_VIBRATE)` cho API < 26 — **sống sót (không có test đơn vị)**; chỉ kiểm được trên máy Android 7.

### File sở hữu
- Code: `composeApp/src/main/kotlin/com/loitu/yourrefri/reminder/ReminderChannel.kt`, `ReminderReceiver.kt`
- Test: `composeApp/src/test/kotlin/com/loitu/yourrefri/reminder/ReminderChannelTest.kt`

## Biên bản

| Kiểm tra | Kết quả | Bằng chứng |
| --- | --- | --- |
| Test đỏ trước khi có code | Đạt | `:composeApp:testDebugUnitTest --tests ReminderChannelTest`: lỗi biên dịch `Unresolved reference 'ReminderChannel'` |
| `:shared:testDebugUnitTest` | Chưa chạy | Task không đụng `:shared` |
| `:composeApp:testDebugUnitTest` (uncached) | Đạt | 353 test, 0 fail, 0 error, 0 skip (đếm từ XML) |
| `:composeApp:assembleDebug` | Đạt | BUILD SUCCESSFUL |
| Mutation 1 (`IMPORTANCE_DEFAULT`) | Bị bắt | `newChannelIsHighImportanceSoReminderShowsAsPopup FAILED`; đã khôi phục HIGH |
| Mutation 2–4 | Bị bắt | M2: 3 test đỏ; M3: 4 test đỏ; M4: 3 test đỏ; file đã khôi phục (`restored=True`) |
| Mutation 5–6 | Sống sót (test yếu) | Không có test đơn vị cho `deleteNotificationChannel` và `setDefaults`; cần kiểm trên thiết bị |
| Popup thật trên emulator / máy | Chưa xác minh | Cần gửi thử thông báo và chụp ảnh |
| Lint `scripts/lint-copy.ps1` | Đạt | LINT: PASS |
