---
name: customer-ux-evaluation
description: Bộ tiêu chuẩn và kịch bản Persona khách hàng (phụ huynh gia đình) thẩm định UX và văn phong của YourRefri
---

# Customer Persona UX Evaluation Guide

## 1. Persona Khách Hàng
- **Chị Mai:** 30–45 tuổi, có con nhỏ, bận việc nhà; muốn thêm món ít chạm và mở app là thấy vài lựa chọn thiết thực.
- **Anh Nam:** 30–45 tuổi, cùng nấu ăn; muốn hiểu món cần gì, thời gian nấu, thao tác sau khi nấu và cách tránh trừ nhầm.
- **Cô Hạnh (exploratory):** thành viên lớn tuổi trong gia đình; tập trung vào chữ lớn, tên món không bị cắt, nhãn dễ hiểu, TalkBack và backup dễ theo. Đây là góc nhìn giả lập, không đại diện nghiên cứu hoặc bằng chứng người dùng thật.
- **Phân khúc chính Mai/Nam:** 30–45 tuổi, gia đình có con nhỏ, lịch nấu ăn bận rộn. **Hạnh:** nhóm exploratory lớn tuổi, không đồng nhất với phân khúc nghiên cứu chính.
- **Tâm lý tiếp nhận:** Mệt mỏi sau ngày dài làm việc, muốn mở app ra là biết ngay tối nay nấu được món gì từ đồ có sẵn. Không muốn suy nghĩ nhiều, không muốn nhập liệu phức tạp.

## 2. Tiêu chí Đánh giá (Chỉ đánh giá trải nghiệm, không đánh giá kỹ thuật)

### A. Tốc độ & Thao tác (Frictionless)
- Nhập đồ trong tủ lạnh có đúng 1 chạm (Có / Hết) trên lưới chạm không?
- Có bị bắt buộc điền ngày mua, số gram, nhãn mác phức tạp không? (Nếu có -> Đánh giá: Quá phiền phức, muốn bỏ app).

### B. Văn phong & Cảm xúc (Empathy & Tone)
- **Quy tắc vàng:** Ấm áp, gợi ý nhẹ nhàng, tôn trọng người dùng.
- **Cấm:** Tuyệt đối không phán xét ("Bạn đã quên", "Lãng phí đồ ăn", "Hết hạn"), không dùng cơ chế streak, điểm số gây áp lực.
- **Đạt:** Dùng từ "ước tính", "nên dùng trước", "kiểm tra lại xem còn dùng được không".

### C. Tính thực tế của gợi ý (Actionable)
- 3 món gợi ý có dùng được những thứ sắp ôi hỏng trong tủ không?
- Các bước nấu có ngắn gọn, gia vị thông dụng trong bếp Việt không?

### D. Thông báo (Notification)
- Chỉ tối đa 1 thông báo/ngày (khoảng 17h30 chiều khi chuẩn bị tan sở).
- Nội dung thông báo phải hữu ích, gợi ý món nhẹ nhàng, không spam.

## 3. Cách ghi nhận một lượt trải nghiệm

Mỗi persona chỉ đánh giá kịch bản được giao và không sửa code. Ghi ngắn gọn theo các mục:
- **Điều đã làm được:** việc persona hoàn tất và thông tin họ hiểu được.
- **Điều còn khúc mắc:** chỗ phải đoán, đọc lại, chạm nhiều hoặc sợ thao tác nhầm.
- **Điều còn thiếu:** việc persona muốn làm nhưng giao diện chưa hỗ trợ hoặc chưa giải thích.
- **Điều hi vọng được trải nghiệm:** mong muốn cụ thể, gắn với nhu cầu của persona.
- **Ưu tiên và bằng chứng:** mức ưu tiên cùng file/màn hình hoặc bước đã thực sự thử.

Luôn ghi rõ mức bằng chứng là source walkthrough, emulator walkthrough hay nghiên cứu với người dùng thật. Persona giả lập không được trình bày như phản hồi người dùng thật. Chỉ ghi đã thử runtime khi thực sự tương tác trên emulator/thiết bị.
