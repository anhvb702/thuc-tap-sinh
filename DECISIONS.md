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
| Thông báo tối đa một lần mỗi ngày lúc 17:30; nhắc Thứ 4 và cuối tuần gộp vào thông báo ngày đó, kèm thẻ trên Trang chủ. **Phần "cuối tuần" đã bị thay thế bởi dòng ngay dưới (2026-11-13); phần Thứ 4 vẫn giữ.** | 2026-11-11 — Người dùng chốt theo đề xuất trong hội thoại ngày 2026-11-11. |
| Chủ Nhật: thông báo trong ngày được thay bằng "Nay là cuối tuần rồi nè, đã sẵn sàng đi chợ cho 1 tuần mới chưa nào!", mặc định 8:00, đổi được trong Cài đặt; các ngày khác giữ 17:30. Vẫn tối đa một thông báo mỗi ngày, đồ nên dùng trước vẫn thấy trong app. | 2026-11-13 — Người dùng chốt trong hội thoại (góp ý #10): "Chủ Nhật thay thông báo thường, mặc định 8:00". Cài đặt trong `A-05`, `A-06`. |
| Thông báo "món ngon hôm nay" (đợt B) dùng chung kênh thông báo ngày; đồ nên dùng sớm luôn thắng. | 2026-11-13 — Người dùng chốt trong hội thoại (góp ý #5): "Đồ nên dùng sớm luôn thắng". Chưa làm, thuộc đợt B. |
| Lịch sử nấu lưu thêm bữa (Trưa/Tối) trong bảng phụ `CookLogMealEntity` (schema v7); màn "Đã nấu" chỉ liệt kê món, không thống kê. | 2026-11-13 — Người dùng chốt "Chỉ danh sách món" và cho phép đổi test ghim version. Chi tiết `docs/tests/A-11.md`. |
| App gợi ý ngăn bảo quản theo thực đơn, người dùng đổi được; món cấp đông dùng hạn ước tính riêng. | 2026-11-11 — Người dùng chốt theo đề xuất trong hội thoại ngày 2026-11-11. |
| Mở rộng catalog lên khoảng 20–24 công thức và thêm vài nguyên liệu bền; danh sách cụ thể chưa chốt. | 2026-11-11 — Người dùng chốt theo đề xuất trong hội thoại ngày 2026-11-11. |
| Chưa có bữa sáng; preset “Nấu đủ cả ngày” chỉ gồm Trưa + Tối. | 2026-11-11 — Người dùng chốt theo đề xuất trong hội thoại ngày 2026-11-11. |
| Độ tươi gắn vào nguyên liệu trong `shelf_table.json`; công thức tính độ tươi từ dữ liệu đó. | 2026-11-11 — Người dùng chốt theo đề xuất trong hội thoại ngày 2026-11-11. |
| Trang chủ giữ đúng 3 gợi ý; bữa đã lên lịch hôm nay đứng đầu. | 2026-11-11 — Người dùng chốt theo đề xuất trong hội thoại ngày 2026-11-11. |
| Không dùng câu cam kết kiểu “không lo hỏng”; dùng “ước tính”, “nên dùng trước”. | 2026-11-11 — Người dùng chốt theo đề xuất trong hội thoại ngày 2026-11-11. |
| Giữ nguyên toolchain đã hạ: Gradle 8.5, AGP 8.3.2, compileSdk/targetSdk 34, minSdk 24, Kotlin 2.2.20, Compose Multiplatform 1.7.3, lifecycle 2.8.4; giữ truststore Windows-ROOT và timeout hiện tại. Không đổi phiên bản thư viện khác. | 2026-11-11 — Người dùng xác nhận giữ toolchain trong hội thoại ngày 2026-11-11. |

| Bữa trong ngày chỉ có Trưa và Tối; mỗi ngày bật/tắt từng bữa (ví dụ hôm ăn ngoài thì tắt), không thêm bữa Sáng. Chi tiết kỹ thuật (lưu trạng thái bật/tắt theo ngày, có đổi schema hay không) chưa chốt, cần planner. | 2026-11-12 — Người dùng dán ý kiến trả lời câu hỏi (văn bản viết theo góc nhìn khách hàng nội trợ). |
| Thực phẩm trong tủ là nguyên liệu thô (thịt, cá, rau củ); định lượng dùng đơn vị đời thường (quả, bó, miếng, lạng/g, khay), không bắt nhập gram chính xác. Danh sách đơn vị cụ thể chưa chốt. | 2026-11-12 — Người dùng dán ý kiến trả lời câu hỏi (cùng nguồn trên). |
| Lịch sử thực phẩm đã dùng để đợt sau. | 2026-11-12 — Người dùng dán ý kiến trả lời câu hỏi (cùng nguồn trên). |
| Ẩn bữa thay vì xóa: bữa đã ẩn không mất món và nguyên liệu đã chọn, bật lại là hiện lại; khi ẩn thì danh sách đi chợ bỏ nguyên liệu của bữa đó. | 2026-11-12 — Người dùng dán ý kiến trả lời câu hỏi (cùng nguồn trên). |

| "Cơm trưa hôm sau" (logic nấu thêm cho bữa hôm sau) đã bỏ; "bữa trưa" là slot trong ngày, tách hẳn khái niệm này. Bật/tắt Trưa-Tối theo ngày ("Nấu ở nhà / Ăn ngoài") đưa vào đợt này; được phép đổi schema và thiết kế cột mới. | 2026-11-12 — Người dùng xác nhận trong hội thoại. |
| Thêm thực phẩm ngoài danh mục: nới contract W1 có ghi chú, không tạo action mới. | 2026-11-12 — Người dùng chọn trong hội thoại. |
| Được phép sửa assert cũ của thông báo W2-03 để dùng câu mới (UX-18b); làm trọn gói UX-18a và UX-18b. | 2026-11-12 — Người dùng chọn trong hội thoại. |

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
| Giới hạn số món trong thông báo. | Còn mở; chưa chốt số lượng tối đa. |
| Món đã hết rồi có lại thì tính ngày nào. | Còn mở; chặn SW-10 cho tới khi có quyết định. |
| Ngưỡng ưu tiên 0–2 ngày. | Còn mở; chưa chốt biên/ngưỡng mới. |
| Chiều cao thẻ lưới Tủ lạnh (180/320dp hiện đang dùng làm chiều rộng). | Còn mở; cần xác định chiều cao thẻ riêng. |

## Quy ước ghi nhận

Khi người dùng quyết định một mục đang mở, thêm quyết định, ngày và nguồn hội thoại vào đây, đồng thời cập nhật `SPEC.md` hoặc spec task liên quan. Không suy ra người dùng đã xác nhận một con số chỉ vì code đã triển khai. Không ghi trạng thái kiểm thử nếu không có kết quả quan sát được; bằng chứng phiên hiện tại nằm ở đầu `HANDOFF.md`.
