# Bước: thiết kế UI/UX và văn phong
Kế hoạch:
{{PLAN}}

Việc cần làm (chỉ viết đặc tả, không code):
1. Với mỗi màn/thành phần thay đổi: luồng chạm (tối đa 1-2 chạm cho việc chính), bố cục, và đủ trạng thái: bình thường, rỗng, lỗi/không có quyền, tên rất dài, font scale 1.3.
2. Dùng đúng token và component trong `DESIGN.md` (Warm Minimalism, vùng chạm ≥ 48dp, bo 16dp, không card lồng 3 tầng, không gradient tím xanh). Không tự đặt màu mới.
3. Mọi chuỗi hiển thị: liệt kê trong bảng `| key | tiếng Việt |`, đúng văn phong, không từ cấm, đồ quá hạn dùng "kiểm tra lại xem còn dùng được không". Đặc tả được quét từ cấm tự động: không nhắc lại từ cấm trong đặc tả; nếu buộc phải trích một quy tắc, ghi nó trên dòng riêng có kèm `lint-copy: rule`.
4. Nội dung cho TalkBack (contentDescription) của từng điều khiển chính.
Nếu kế hoạch không có task `ui`, chỉ ghi "Không có thay đổi giao diện".
`RESULT: DONE`
