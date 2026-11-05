# YourRefri — sổ quyết định

Chỉ ghi quyết định có nguồn. Mục đang đề xuất hoặc chờ người dùng được đánh dấu riêng, không áp dụng ngầm.

## Đã được người dùng xác nhận

| Quyết định | Nguồn |
|---|---|
| Làm ứng dụng tủ lạnh theo kế hoạch hiện hành; giữ dữ liệu trên thiết bị và hoạt động offline. | Chỉ dẫn dự án trong `AGENTS.override.md`, do người dùng cung cấp. |
| Dùng CodeRabbit và Qodo làm bot reviewer; nếu CodeRabbit hết lượt thì dùng Qodo. | Người dùng trong hội thoại. |
| Mỗi feature được cập nhật lên fork để bot review; đưa kết quả review lại cho agent xử lý. | Người dùng trong hội thoại. |
| Người dùng muốn sync và chạy trên Android Studio. Khi phiên không có Computer Use, nhờ người dùng bấm chạy test trong Android Studio và gửi kết quả; không thay bằng chạy CLI. | Người dùng và chỉ dẫn team trong `AGENTS.override.md`. |
| Luna viết tài liệu; Sol đề xuất nội dung và rà các quyết định quan trọng; người dùng chốt phần còn mở. | Người dùng trong hội thoại và `AGENTS.override.md`. |

## Đề xuất/chưa được duyệt

| Mục | Tình trạng |
|---|---|
| 25 nguyên liệu và hạn ước tính W1-03 | Chờ người dùng duyệt; xem `HANDOFF.md` và spec W1-03. |
| Giờ nhắc mặc định 17:30 | Đề xuất trong `HANDOFF.md`, chưa duyệt. |
| Ngưỡng SOON 0–2 ngày | Có trong kế hoạch/spec nhưng chưa có bằng chứng người dùng chốt. |
| Ràng buộc bổ sung cho Recipe/Slot tại W1-02 | Do thiết kế task đề xuất; chờ người dùng duyệt theo handoff. |
| Chính sách khi xác nhận món hết thành có lại: giữ `markedAt` cũ hay hỏi ngày mua | Còn mở; W4-01 là hàm thuần và hiện giữ ngày, chưa chứng minh quyết định tích hợp. |
| Danh sách/lô công thức và tiêu chí ưu tiên gợi ý chi tiết | Chờ duyệt theo `HANDOFF.md`/`PLAN.md`. |

## Quy ước ghi nhận

Khi người dùng quyết định một mục đang mở, thêm quyết định, ngày và nguồn hội thoại vào đây, đồng thời cập nhật `SPEC.md` hoặc spec task liên quan. Không ghi trạng thái kiểm thử nếu không có kết quả quan sát được; bằng chứng phiên hiện tại nằm ở đầu `HANDOFF.md`.
