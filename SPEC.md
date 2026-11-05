# YourRefri — hợp đồng sản phẩm

Tài liệu này ghi phạm vi sản phẩm đã có căn cứ. Trạng thái mỗi nội dung được ghi rõ để đề xuất và phần đang chờ không bị hiểu thành quyết định đã duyệt. Quy tắc làm việc nằm ở `AGENTS.override.md`; kế hoạch task ở `PLAN.md`; tiêu chí kiểm tra từng task ở `docs/tests/`.

## Phạm vi đã được xác nhận

- Ứng dụng Kotlin Multiplatform dùng Compose, ưu tiên Android, tên sản phẩm “Hôm nay nấu gì”, hướng tới phụ huynh.
- Nhập đồ trong tủ bằng lưới chạm với trạng thái có/hết; ngày dùng được là **ước tính**.
- Dữ liệu lưu trên thiết bị bằng SQLDelight. Không có tài khoản, backend, analytics hay SDK crash; mọi tính năng phải dùng được offline.
- Tối đa một thông báo mỗi ngày; nội dung vẫn xem được khi mở ứng dụng.
- Gợi ý ba món từ đồ nên dùng sớm và hướng dẫn công thức từng bước.
- Không khẳng định an toàn thực phẩm. Với đồ đã qua ngày ước tính, hướng dẫn người dùng kiểm tra lại xem còn dùng được không.
- Văn phong giao diện ngắn, ấm, gợi ý chứ không ra lệnh, luôn có đường lui. Từ/cách nói bị cấm và từ nên dùng được liệt kê trong `AGENTS.override.md`.

## Kiến trúc đã được xác nhận

- `:shared` chứa dữ liệu và logic thuần, chỉ có Android target; `:composeApp` chứa UI và ViewModel.
- Logic nghiệp vụ thuần không import SQLDelight. UI theo MVI: State, Action, Effect, ViewModel và StateFlow.
- Mọi route khai báo trong `composeApp/.../nav/Routes.kt`, tham số route chỉ dùng kiểu nguyên thủy.
- Tiêm `kotlin.time.Clock` và `TimeZone`; không đọc giờ hệ thống trực tiếp.
- Công thức là JSON bundle đọc lúc chạy, không lưu trong DB.
- Thông báo dùng AlarmManager không chính xác, được đặt lại sau reboot và đổi múi giờ.

## Hành vi chi tiết đang chờ duyệt hoặc chốt

Các mục dưới đây xuất hiện như đề xuất trong kế hoạch/spec; chưa có bằng chứng người dùng duyệt nên không xem là hợp đồng sản phẩm đã chốt:

- Danh sách 25 nguyên liệu và số ngày ước tính ở W1-03.
- Giờ gửi thông báo mặc định (đề xuất 17:30) và ngưỡng “sắp dùng” 0–2 ngày.
- Các ràng buộc model bổ sung ở W1-02: slot `anyOf` không rỗng, từng bước công thức không rỗng, và danh sách `required` không rỗng.
- Cửa sổ ba ngày cho lịch sử nấu, số lượng/lô công thức và tiêu chí xếp hạng gợi ý chi tiết.
- Quy tắc ngày khi xác nhận lại một món đã đánh dấu hết. W4-01 hiện giữ nguyên ngày cũ; quyết định tích hợp UI/repository cần được xác nhận.

## Thứ tự nguồn khi có khác biệt

Ưu tiên chỉ dẫn mới nhất của người dùng; tiếp theo là quyết định được người dùng duyệt trong `DECISIONS.md` và hợp đồng task hiện hành trong `docs/tests/`. `PLAN.md` là lịch thực hiện, `HANDOFF.md` là ảnh chụp tiến độ; mã nguồn mô tả hành vi đang có nhưng tự nó không biến hành vi thành yêu cầu. Ý tưởng ở PR/fork không tự đổi phạm vi app.
