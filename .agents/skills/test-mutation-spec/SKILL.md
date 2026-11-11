---
name: test-mutation-spec
description: Thiết kế tiêu chí Unit Test chuẩn (Task, Input, Expected Output) và quy trình 6 Mutation Test cho KMM/Android
---

# Kỹ năng Thiết kế Tiêu chí Unit Test & Mutation Testing

## 1. Cấu trúc Tiêu chí Nghiệm thu (Test Contract)
Mỗi task do "Não" (Coordinator) thiết kế phải có định dạng rõ ràng:
- **Task ID & Tên:** Mã task (ví dụ W1-07a, W2-01)
- **Mục tiêu:** Hàm/Class/ViewModel cần kiểm thử
- **Đầu vào (Input):** State ban đầu, Event/Action, Dữ liệu tiêm (Clock, TimeZone, Repository mock/fake)
- **Đầu ra kỳ vọng (Expected Output):**
  + State thay đổi chính xác ra sao (StateFlow value)
  + Effect nào được phát ra (Single-shot effect)
  + Side-effect trong DB/Repository (nếu có)
- **Biên & Rủi ro (Edge cases):** Giá trị rỗng, giá trị âm, trùng lặp, mất kết nối, ngày giờ chuyển giao.

## 2. Quy chuẩn 6 Mutation bắt buộc
Để đảm bảo test không phải "test giả vờ", Reviewer/Tester phải thử làm sai code (mutations) ít nhất 6 kiểu sau:
1. **Đảo cờ logic:** Đổi `if (have)` thành `if (!have)` hoặc đổi `true` <-> `false`.
2. **Lệch cận thời gian/số:** Đổi `>= 0` thành `> 0`, đổi `shelfDays` từ `x` thành `x + 1` hoặc `x - 1`.
3. **Bỏ qua lưu trữ/phát sự kiện:** Comment dòng gọi `emit(effect)` hoặc `repository.setHave(...)`.
4. **Trả về giá trị mặc định/rỗng:** Hàm tính toán trả về `emptyList()` hoặc `null` thay vì kết quả thật.
5. **Đổi thứ tự ưu tiên:** Đảo ngược thứ tự sort (từ sắp hết hạn lên đầu sang món hạn còn dài lên đầu).
6. **Bỏ qua xử lý khôi phục (Undo/Rollback):** Chạm lần 2 không trả lại hạn cũ mà gán ngày mới.

> **Tiêu chuẩn đạt:** Mọi mutation cố ý này BẮT BUỘC phải làm Unit Test bị ĐỎ. Sau đó khôi phục code nguyên trạng thì test phải XANH.
