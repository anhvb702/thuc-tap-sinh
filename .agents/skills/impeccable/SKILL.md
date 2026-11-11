---
name: impeccable
description: Design vocabulary, UI polish & anti-slop guidelines for creating beautiful, human, intentional interfaces (Paul Bakaus framework)
---

# Impeccable — Design Engine & Polishing Vocabulary

## 1. Triết lý Thiết kế (Anti-Slop)
Loại bỏ hoàn toàn phong cách "AI Slop" (màu tím xanh gradient na ná nhau, typography Inter đơn điệu, card lồng card vô nghĩa, hiệu ứng thừa thãi).
Mọi giao diện phải có linh hồn, phục vụ đúng tình huống thực tế của con người, mang cảm giác thủ công tinh tế.

## 2. Hệ Từ Vựng Điều Khiển UI (24 Commands)

### A. Định hình & Cảm xúc (Steer & Shape)
- `/shape`: Phác thảo kiến trúc thông tin, phân tầng thị giác (Hierarchy) theo mục tiêu sản phẩm.
- `/bolder`: Tăng độ tương phản, làm điểm nhấn (Primary Action) nổi bật hơn hẳn.
- `/quieter`: Giảm bớt kích thích thị giác, hạ tông màu nền, tạo cảm giác thư thái, tĩnh lặng (Warm Minimalism).
- `/distill`: Lược bỏ các yếu tố rườm rà, giữ lại đúng thông tin mà người dùng cần thấy trong 5 giây đầu.
- `/colorize`: Áp dụng bảng màu đặc trưng (Signature Palette), loại bỏ màu mặc định.
- `/typeset`: Căn chỉnh khoảng cách chữ, nhịp điệu dòng (Vertical Rhythm), tỷ lệ font cấp bậc.
- `/layout`: Tối ưu khoảng trắng (Whitespaces), lưới thích ứng (Adaptive Grid), vị trí thao tác một tay.
- `/delight`: Bổ sung chi tiết nhỏ tạo cảm xúc ấm áp (Micro-interactions, câu chào theo thời gian).

### B. Đánh giá & Rà soát (Review & Critique)
- `/critique`: Chấm điểm UX theo 10 nguyên lý Nielsen heuristics và trải nghiệm người dùng mục tiêu.
- `/audit`: Rà soát kỹ thuật giao diện (Độ tương phản màu WCAG AA, vùng chạm tối thiểu 48dp, hiển thị trên màn hình nhỏ 360dp, cắt chữ khi font phóng to).

### C. Hoàn thiện & Đóng gói (Finish & Harden)
- `/polish`: Tinh chỉnh từng viền bo (Border radius), bóng đổ nhẹ (Subtle elevation), padding nhất quán.
- `/harden`: Xử lý triệt để trạng thái biên: Danh sách rỗng (Empty state), dữ liệu quá dài, lỗi nhập liệu, xử lý ngoại tuyến (Offline mode).
- `/clarify`: Đơn giản hóa câu chữ trên UI, đảm bảo người già/trẻ nhỏ đọc đều hiểu ngay trong 1 giây.

## 3. Tích hợp trong Dự án YourRefri
- Đọc `PRODUCT.md` để hiểu bối cảnh và người dùng (Người nội trợ, bận rộn, cần ấm áp).
- Đọc `DESIGN.md` để lấy chính xác Design Tokens (Warm Ivory, Sage Green, Terracotta).
- Không tự ý sinh màu hoặc style ngoài bảng quy chuẩn.
