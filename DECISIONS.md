# YourRefri — sổ quyết định

Chỉ ghi quyết định có nguồn. Mục đang đề xuất hoặc chờ người dùng được đánh dấu riêng, không áp dụng ngầm.

## Quyết định được người dùng xác nhận

| Quyết định | Nguồn |
|---|---|
| Dữ liệu chỉ lưu trên thiết bị; app hoạt động offline, không tài khoản/backend/analytics/crash SDK. | Ràng buộc cứng trong `AGENTS.override.md`, do người dùng cung cấp. |
| Trang chủ đưa ra đúng 3 gợi ý món thiết thực. | Triết lý sản phẩm trong `AGENTS.override.md`. |
| Nhắc nhở cố định 17:30, tối đa một lần/ngày. | Ràng buộc văn phong/sản phẩm trong `AGENTS.override.md`. |
| Tự chạy unit test qua Gradle CLI; không cài package KMM/Android test chỉ để chạy test. | Chỉ dẫn người dùng và `AGENTS.override.md`. |
| Bộ lọc thời gian nấu hiển thị “Tối đa 15 phút” / “Tối đa 30 phút” và gồm món đúng 15/30 phút. | Người dùng chọn rõ trong hội thoại ngày 2026-11-08; hợp đồng và bằng chứng tại `docs/tests/W5-02B.md`. |
| Chip quick-add chọn “Theo gợi ý”, áp dụng `userShelfDays ?: defaultShelfDays`; chỉ lưu món sau khi người dùng xác nhận. | Yêu cầu người dùng ngày 2026-11-08; `docs/tests/UX-04.md`. |
| Xác nhận sau nấu nêu nguyên liệu đang có sẽ giảm; không tuyên bố cập nhật khi không có món phù hợp; “Để sau” là TextButton. | Yêu cầu người dùng ngày 2026-11-08; `docs/tests/UX-04.md`. |
| Chạm thẻ gợi ý Home mở đúng recipe ID trong tab Món ăn. | Yêu cầu người dùng ngày 2026-11-08; `docs/tests/UX-04.md`. |
| Lưới Pantry dành 88dp phía dưới cho FAB; chỉ món đang có có nút sửa hạn; nút +/- rõ và dòng nhắc căn giữa. | Yêu cầu người dùng ngày 2026-11-08; `docs/tests/UX-04.md`. |
| Qodo đã phụ trách review PR; chuyển finding thành task sửa thay vì lặp lại lượt review Qodo. | Quy trình trong `AGENTS.override.md`. |

## Đang triển khai theo spec (không đồng nghĩa người dùng đã chốt riêng từng giá trị)

| Phạm vi hiện có | Nguồn/ghi chú |
|---|---|
| Catalog hiện có 25 món và thời hạn mặc định trong `shelf_table.json`. | Giá trị đang triển khai; số lượng/catalog có thể đổi bằng task riêng, không phải quyết định người dùng đã chốt. |
| Ngưỡng ưu tiên dùng sớm 0–2 ngày. | Đang triển khai theo spec/test hiện tại; chưa có nguồn xác nhận riêng của người dùng cho chính các biên này. |
| Công thức và mapping thời lượng trong JSON bundle. | Dữ liệu triển khai hiện tại; việc mở rộng/chỉnh bundle cần task riêng. |

## Đề xuất/chưa được duyệt

| Mục | Tình trạng |
|---|---|
| Chính sách khi xác nhận món hết thành có lại: giữ `markedAt` cũ hay hỏi ngày mua. | Còn mở; reducer có quy tắc hoàn tác trong phiên nhưng chưa có quyết định riêng về ngày mua. |
| Mở rộng catalog/công thức hoặc thay đổi ý nghĩa ngưỡng hạn. | Cần quyết định riêng nếu vượt contract/spec hiện hành. |

## Quy ước ghi nhận

Khi người dùng quyết định một mục đang mở, thêm quyết định, ngày và nguồn hội thoại vào đây, đồng thời cập nhật `SPEC.md` hoặc spec task liên quan. Không suy ra người dùng đã xác nhận một con số chỉ vì code đã triển khai. Không ghi trạng thái kiểm thử nếu không có kết quả quan sát được; bằng chứng phiên hiện tại nằm ở đầu `HANDOFF.md`.
