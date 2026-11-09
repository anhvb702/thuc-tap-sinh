# QODO-01 — Hoàn tác món chưa từng có

## Finding

Nếu người dùng chạm một món chưa có trong tủ rồi chạm lần nữa để hoàn tác, lần chạm thứ hai phải xóa dòng pantry mới tạo. Không để lại `have=false` cho món chưa từng được ghi nhận.

## Contract

| Case | Input | Expected output |
| --- | --- | --- |
| Chạm lần đầu | item chưa có row | emit `SaveHave(id, true, today)` |
| Chạm lần hai để hoàn tác | item được tạo trong phiên, trạng thái quay về absent | emit `DeleteHave(id)`; state không có pantry row |
| Xóa row | repository nhận `DeleteHave(id)` | chỉ xóa `PantryEntity` theo đúng ID; giữ shelf, quantity và unit overrides |
| ViewModel effect | reducer phát `DeleteHave` | gọi repository delete; không giả lập bằng `setHave(id,false)` |

## Sáu mutation probes

| Mutation | Kết quả |
| --- | --- |
| Reducer phát `SaveHave(false)` thay vì delete | Test đỏ |
| Reducer bỏ effect delete | Test đỏ |
| Repository delete thành no-op | Test đỏ |
| Repository xóa sai ID | Test đỏ |
| ViewModel bỏ qua `DeleteHave` | Test đỏ |
| ViewModel gọi `setHave(false)` thay cho delete | Test đỏ |

## Bằng chứng cuối

- Test-first: `:shared:testDebugUnitTest` đỏ khi `deleteHave`/`DeleteHave` chưa được triển khai (compile failure do API còn thiếu).
- Sau implementation: `:shared:testDebugUnitTest --rerun-tasks` — 312/312 tests, BUILD SUCCESSFUL sau các sửa đổi gần nhất.
- `:composeApp:testDebugUnitTest --rerun-tasks` — 32/32 tests, BUILD SUCCESSFUL sau các sửa đổi gần nhất.
- Cả sáu mutation đều tạo test failure; code được khôi phục trước các suite cuối.
- `:composeApp:assembleRelease` — BUILD SUCCESSFUL; unsigned APK `composeApp/build/outputs/apk/release/composeApp-release-unsigned.apk`, 8,049,873 bytes, timestamp 2026-11-07 00:40 local. Cần signing key do chủ dự án quản lý; key không nằm trong repo.
- Android `allowBackup=false`; backup rules loại trừ dữ liệu khỏi cloud backup và device-to-device transfer.
- Emulator ADB 1080×2400, density 420: package đã cài và activity resumed. Screenshot không khả dụng; notification runtime chưa được xác minh.

Finding và reviewer fixes mới nhất đã được sửa tại working tree. Chưa push; PR review thread chưa được cập nhật. Không có commit/push trong phiên.
