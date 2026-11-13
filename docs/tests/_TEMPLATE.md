# <ID> — <tên task>

## Hợp đồng

### Task
<Mục tiêu một đoạn: hàm/class/ViewModel nào, hành vi quan sát được.>

### Input
<State ban đầu, Action/Event, dữ liệu tiêm: Clock, TimeZone, fake repository.>

### Test

| Test | Input | Expected Output | Mutation bắt |
| --- | --- | --- | --- |
| `tenTest_dieuKien` | ... | StateFlow/Effect/DB quan sát được | ... |

### Biên và rủi ro
<Rỗng, âm, trùng, múi giờ/DST, mất quyền, dữ liệu lớn.>

### Sáu mutation
1. Đảo cờ logic: ...
2. Lệch cận số/thời gian: ...
3. Bỏ ghi/phát sự kiện: ...
4. Trả mặc định/rỗng: ...
5. Đổi thứ tự ưu tiên: ...
6. Bỏ khôi phục/undo: ...

### File sở hữu
- Code: ...
- Test: ...

## Biên bản

> Chỉ điền bằng kết quả thật. Chưa chạy thì ghi "Chưa chạy". Trạng thái cho phép: Đạt, Bị bắt, Sống sót (test yếu), Chưa xác minh.

| Kiểm tra | Kết quả | Bằng chứng (lệnh, kết quả) |
| --- | --- | --- |
| Test đỏ trước khi có code | | |
| `:shared:testDebugUnitTest` | | |
| `:composeApp:testDebugUnitTest` | | |
| `:composeApp:assembleDebug` | | |
| Mutation 1-6 | | |
| Ảnh emulator (task ui) | | |
