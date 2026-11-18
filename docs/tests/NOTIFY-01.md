# NOTIFY-01 — Kênh nhắc ăn uống có popup (heads-up)

## Hợp đồng

### Task
Kênh thông báo nhắc ăn uống dùng `IMPORTANCE_HIGH` để hiện popup, không chỉ nằm trên thanh thông báo. Mức quan trọng của kênh cũ `daily_reminder` không đổi được sau khi tạo, nên kênh mới dùng id `daily_reminder_hi`. Lựa chọn của người dùng ở kênh cũ được giữ: đã chặn thì không gửi, đã chọn im lặng thì kênh mới cũng im lặng.

### Input
Không có state UI. Đầu vào là mức quan trọng của kênh cũ (`Int?`, null nếu chưa có kênh cũ).

### Test

| Test | Input | Expected Output | Mutation bắt |
| --- | --- | --- | --- |
| `freshInstallGetsHighImportanceChannelSpec` | `IMPORTANCE`, `importanceFor(null)` | HIGH | đổi IMPORTANCE về DEFAULT |
| `upgradeKeepsUsersLegacyChoice` | `importanceFor(...)` cho DEFAULT, HIGH, LOW, MIN, NONE | HIGH, HIGH, LOW, LOW, NONE | đổi DEFAULT; bỏ giữ im lặng (LOW→HIGH) |
| `newChannelUsesNewIdBecauseOldChannelImportanceCannotChange` | `ID`, `LEGACY_ID` | khác nhau | đặt ID trùng LEGACY_ID |
| `notifyGateAllowsSendingWhenNoLegacyChannelOrLegacyChannelEnabled` | `shouldNotify(null)`, `shouldNotify(DEFAULT)` | true | đảo `shouldNotify` |
| `notifyGateBlocksSendingWhenUserBlockedLegacyChannel` | `shouldNotify(NONE)` | false | đảo `shouldNotify`; `shouldNotify` luôn true |

### Biên và rủi ro
- Người dùng đã chặn kênh cũ: không gửi, không tạo kênh mới.
- Người dùng đã chọn im lặng: kênh mới im lặng (không popup, không âm).
- Kênh cũ đang bật hoặc mặc định: kênh mới HIGH; kênh cũ bị xóa; âm thanh/rung đã chỉnh ở kênh cũ sẽ mất.
- Hãng máy (Xiaomi, Samsung, Oppo) có thể chặn popup; chưa kiểm trên máy thật.
- Test đơn vị không chạy `ReminderReceiver` (cần thiết bị hoặc Robolectric). Việc copy âm thanh và rung từ kênh cũ (`setSound`, `enableVibration`) chỉ kiểm được trên máy: người dùng đã chọn "Không có âm" ở kênh cũ phải vẫn không có âm ở kênh mới.

### Sáu mutation
1. Đổi `IMPORTANCE` về `IMPORTANCE_DEFAULT` — **bị bắt** (2 method đỏ).
2. Đặt `ID` trùng `LEGACY_ID` — **bị bắt** (1 method đỏ).
3. Đảo `shouldNotify` (`==` thay `!=`) — **bị bắt** (2 method đỏ).
4. `shouldNotify` luôn trả true — **bị bắt** (1 method đỏ).
5. `importanceFor` bỏ giữ im lặng (LOW → HIGH) — **bị bắt** (1 method đỏ).
6. Bỏ `setDefaults(DEFAULT_VIBRATE)` cho API < 26 — **sống sót (test yếu)**: nằm trong `ReminderReceiver.kt` (không phải `ReminderChannel.kt`), không có test đơn vị; chỉ kiểm được trên máy Android 7.

## Biên bản

| Kiểm tra | Kết quả | Bằng chứng |
| --- | --- | --- |
| Test đỏ trước khi có code | Đạt | `Unresolved reference 'ReminderChannel'` khi chạy `ReminderChannelTest` |
| `:shared:testDebugUnitTest` | Chưa chạy | Task không đụng `:shared` |
| `:composeApp:testDebugUnitTest` (uncached) | Đạt | 356 test method, 0 method lỗi (đếm từ XML); chạy lại sau khi khôi phục mutation và sau khi đổi thứ tự tạo/xóa kênh |
| Thứ tự thao tác kênh | Đạt | Tạo kênh mới trước, xóa kênh cũ sau (`ReminderReceiver.kt`), để gián đoạn giữa chừng không làm mất lựa chọn im lặng |
| Lệnh mutation | Ghi nhận | Mỗi mutation: sửa một chuỗi trong `ReminderChannel.kt` bằng PowerShell, chạy `:composeApp:testDebugUnitTest --tests ReminderChannelTest --rerun-tasks`, đếm method đỏ trong XML, rồi ghi lại file gốc |
| `:composeApp:assembleDebug` | Đạt | BUILD SUCCESSFUL |
| Mutation 1–5 | Bị bắt | Xem danh sách ở trên; file đã khôi phục (`restored=True`) |
| Mutation 6 | Sống sót (test yếu) | Chỉ kiểm được trên máy Android 7 |
| Popup thật trên emulator hoặc máy | Chưa xác minh | Cần gửi thử thông báo và chụp ảnh |
| Lint `scripts/lint-copy.ps1` | Đạt | LINT: PASS |
| Commit `90ada42` có subject 75 ký tự, vượt PB9 (72) | Ghi nhận | Không sửa bằng force-push; commit sau đã đúng quy định |

## Bổ sung: ảnh món trong popup

- Ảnh: 4 món từ `image/` (thư mục worktree `home-ui-mockups`), thu nhỏ 720 px, JPEG chất lượng 85, đóng gói tại `composeApp/src/main/assets/notify/` (tổng khoảng 500 KB). APK debug kiểm tra có đủ 4 file.
- Chọn ảnh theo ngày trong năm (`ReminderPicture.assetFor`), vì thông báo chưa gắn với một món cụ thể.
- Test: `ReminderPictureTest` (3 test). Mutation "luôn ảnh đầu" bị bắt (1 method đỏ). `:composeApp:testDebugUnitTest` (uncached): 359 method, 0 lỗi.
- Hiển thị bằng `BigPictureStyle` khi đọc được ảnh, nếu không dùng `BigTextStyle` như cũ. Việc hiện ảnh trên popup thật chưa kiểm.
- Chưa làm: nút hành động (ví dụ "Nấu ngay"), và chọn món theo nguyên liệu sắp hết hạn.
