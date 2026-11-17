# YourRefri — sổ quyết định

Chỉ ghi quyết định có nguồn. Mục đang đề xuất hoặc chờ người dùng được đánh dấu riêng, không áp dụng ngầm.

## Quyết định được người dùng xác nhận

| Quyết định | Nguồn |
|---|---|
| Dữ liệu chỉ lưu trên thiết bị; app hoạt động offline, không tài khoản/backend/analytics/crash SDK. | Ràng buộc cứng trong `AGENTS.override.md`, do người dùng cung cấp. |
| Trang chủ đưa ra đúng 3 gợi ý món thiết thực. | Triết lý sản phẩm trong `AGENTS.override.md`. |
| ~~Nhắc nhở cố định 17:30~~, tối đa một lần/ngày. **Phần giờ cố định đã bị thay bởi quyết định 2026-11-16 (mặc định 16:00, chỉnh được); phần tối đa một lần/ngày vẫn giữ.** | Ràng buộc văn phong/sản phẩm trong `AGENTS.override.md`. |
| Tự chạy unit test qua Gradle CLI; không cài package KMM/Android test chỉ để chạy test. | Chỉ dẫn người dùng và `AGENTS.override.md`. |
| Bộ lọc thời gian nấu hiển thị “Tối đa 15 phút” / “Tối đa 30 phút” và gồm món đúng 15/30 phút. | Người dùng chọn rõ trong hội thoại ngày 2026-11-08; hợp đồng và bằng chứng tại `docs/tests/W5-02B.md`. |
| Chip quick-add chọn “Theo gợi ý”, áp dụng `userShelfDays ?: defaultShelfDays`; chỉ lưu món sau khi người dùng xác nhận. | Yêu cầu người dùng ngày 2026-11-08; `docs/tests/UX-04.md`. |
| Xác nhận sau nấu nêu nguyên liệu đang có sẽ giảm; không tuyên bố cập nhật khi không có món phù hợp; “Để sau” là TextButton. **Hộp hỏi đã bị thay bởi quyết định 2026-11-16 (một chạm trừ hết); dòng nêu nguyên liệu sẽ giảm chuyển sang màn chúc mừng.** | Yêu cầu người dùng ngày 2026-11-08; `docs/tests/UX-04.md`. |
| Chạm thẻ gợi ý Home mở đúng recipe ID trong tab Món ăn. | Yêu cầu người dùng ngày 2026-11-08; `docs/tests/UX-04.md`. |
| Lưới Pantry dành 88dp phía dưới cho FAB; chỉ món đang có có nút sửa hạn; nút +/- rõ và dòng nhắc căn giữa. | Yêu cầu người dùng ngày 2026-11-08; `docs/tests/UX-04.md`. |
| Qodo đã phụ trách review PR; chuyển finding thành task sửa thay vì lặp lại lượt review Qodo. | Quy trình trong `AGENTS.override.md`. |
| Thông báo tối đa một lần mỗi ngày ~~lúc 17:30~~; nhắc Thứ 4 và cuối tuần gộp vào thông báo ngày đó, kèm thẻ trên Trang chủ. **Giờ 17:30 đã bị thay bởi Q5 (2026-11-16); phần "cuối tuần" đã bị thay thế bởi dòng ngay dưới (2026-11-13); phần Thứ 4 vẫn giữ.** | 2026-11-11 — Người dùng chốt theo đề xuất trong hội thoại ngày 2026-11-11. |
| Chủ Nhật: thông báo trong ngày được thay bằng "Nay là cuối tuần rồi nè, đã sẵn sàng đi chợ cho 1 tuần mới chưa nào!", mặc định 8:00, đổi được trong Cài đặt; các ngày khác theo giờ nhắc ngày thường (từ 2026-11-16: mặc định 16:00, chỉnh được). Vẫn tối đa một thông báo mỗi ngày, đồ nên dùng trước vẫn thấy trong app. | 2026-11-13 — Người dùng chốt trong hội thoại (góp ý #10): "Chủ Nhật thay thông báo thường, mặc định 8:00". Cài đặt trong `A-05`, `A-06`. |
| Thông báo "món ngon hôm nay" (đợt B) dùng chung kênh thông báo ngày; đồ nên dùng sớm luôn thắng. | 2026-11-13 — Người dùng chốt trong hội thoại (góp ý #5): "Đồ nên dùng sớm luôn thắng". Chưa làm, thuộc đợt B. |
| Lịch sử nấu lưu thêm bữa (Trưa/Tối) trong bảng phụ `CookLogMealEntity` (schema v7); màn "Đã nấu" chỉ liệt kê món, không thống kê. | 2026-11-13 — Người dùng chốt "Chỉ danh sách món" và cho phép đổi test ghim version. Chi tiết `docs/tests/A-11.md`. |
| App gợi ý ngăn bảo quản theo thực đơn, người dùng đổi được; món cấp đông dùng hạn ước tính riêng. | 2026-11-11 — Người dùng chốt theo đề xuất trong hội thoại ngày 2026-11-11. |
| ~~Mở rộng catalog lên khoảng 20–24 công thức và thêm vài nguyên liệu bền~~ **Đã bị thay bởi quyết định 2026-11-16 (50–60 nguyên liệu, 25–30 công thức).** | 2026-11-11 — Người dùng chốt theo đề xuất trong hội thoại ngày 2026-11-11. |
| Chưa có bữa sáng; preset “Nấu đủ cả ngày” chỉ gồm Trưa + Tối. | 2026-11-11 — Người dùng chốt theo đề xuất trong hội thoại ngày 2026-11-11. |
| Độ tươi gắn vào nguyên liệu trong `shelf_table.json`; công thức tính độ tươi từ dữ liệu đó. | 2026-11-11 — Người dùng chốt theo đề xuất trong hội thoại ngày 2026-11-11. |
| Trang chủ giữ đúng 3 gợi ý; bữa đã lên lịch hôm nay đứng đầu. | 2026-11-11 — Người dùng chốt theo đề xuất trong hội thoại ngày 2026-11-11. |
| Không dùng câu cam kết kiểu “không lo hỏng”; dùng “ước tính”, “nên dùng trước”. | 2026-11-11 — Người dùng chốt theo đề xuất trong hội thoại ngày 2026-11-11. |
| Giữ nguyên toolchain đã hạ: Gradle 8.5, AGP 8.3.2, compileSdk/targetSdk 34, minSdk 24, Kotlin 2.2.20, Compose Multiplatform 1.7.3, lifecycle 2.8.4; giữ truststore Windows-ROOT và timeout hiện tại. Không đổi phiên bản thư viện khác. **Cập nhật 2026-11-13 (review Qodo PR #5): `WINDOWS-ROOT` chuyển khỏi `gradle.properties` của dự án sang `%USERPROFILE%\.gradle\gradle.properties` của máy Windows; timeout vẫn ở file dự án.** | 2026-11-11 — Người dùng xác nhận giữ toolchain trong hội thoại ngày 2026-11-11. |

| Bữa trong ngày chỉ có Trưa và Tối; mỗi ngày bật/tắt từng bữa (ví dụ hôm ăn ngoài thì tắt), không thêm bữa Sáng. Chi tiết kỹ thuật (lưu trạng thái bật/tắt theo ngày, có đổi schema hay không) chưa chốt, cần planner. | 2026-11-12 — Người dùng dán ý kiến trả lời câu hỏi (văn bản viết theo góc nhìn khách hàng nội trợ). |
| Thực phẩm trong tủ là nguyên liệu thô (thịt, cá, rau củ); định lượng dùng đơn vị đời thường (quả, bó, miếng, lạng/g, khay), không bắt nhập gram chính xác. Danh sách đơn vị cụ thể chưa chốt. | 2026-11-12 — Người dùng dán ý kiến trả lời câu hỏi (cùng nguồn trên). |
| Lịch sử thực phẩm đã dùng để đợt sau. | 2026-11-12 — Người dùng dán ý kiến trả lời câu hỏi (cùng nguồn trên). |
| Ẩn bữa thay vì xóa: bữa đã ẩn không mất món và nguyên liệu đã chọn, bật lại là hiện lại; khi ẩn thì danh sách đi chợ bỏ nguyên liệu của bữa đó. | 2026-11-12 — Người dùng dán ý kiến trả lời câu hỏi (cùng nguồn trên). |

| "Cơm trưa hôm sau" (logic nấu thêm cho bữa hôm sau) đã bỏ; "bữa trưa" là slot trong ngày, tách hẳn khái niệm này. Bật/tắt Trưa-Tối theo ngày ("Nấu ở nhà / Ăn ngoài") đưa vào đợt này; được phép đổi schema và thiết kế cột mới. | 2026-11-12 — Người dùng xác nhận trong hội thoại. |
| Thêm thực phẩm ngoài danh mục: nới contract W1 có ghi chú, không tạo action mới. | 2026-11-12 — Người dùng chọn trong hội thoại. |
| Được phép sửa assert cũ của thông báo W2-03 để dùng câu mới (UX-18b); làm trọn gói UX-18a và UX-18b. | 2026-11-12 — Người dùng chọn trong hội thoại. |
| Tên món không khớp danh mục và hợp lệ được thêm như món ngoài danh mục; tên khớp nhiều món vẫn không làm gì. Kỳ vọng "Không có" trong `QuickAddConfirmTest` đổi theo. | 2026-11-13 — Người dùng yêu cầu làm theo review Qodo; PR #6. |

### Quyết định 2026-11-16 (buổi hỏi đáp Q1–Q22)

Nguồn chung: người dùng trả lời "đồng ý hết theo đề xuất" cho vòng hỏi Q1–Q22 trong hội thoại ngày 2026-11-16. Dòng nào ghi "Thay" thì ghi đè dòng cũ đã đánh dấu ở trên.

| # | Quyết định |
|---|---|
| Q1 | Sau buổi hỏi, cập nhật `DECISIONS.md` và `PLAN.md` kèm danh sách task, mở PR riêng. |
| Q2 | Chế độ ăn (không hải sản, không thịt đỏ, ăn chay) là bộ lọc cứng ở mọi nơi gợi ý món: nấu ngẫu nhiên, 3 gợi ý, thực đơn tuần. Thay contract `A-03` (trước: nấu ngẫu nhiên không lọc). Lọc xong không còn món thì hiện câu gợi ý đổi chế độ ăn, không lách bộ lọc. |
| Q3 | Sau khi nấu: một chạm "Đã nấu xong" trừ toàn bộ nguyên liệu chính; nút phụ "Giữ lại đồ còn dư" mở danh sách chọn như cũ. Thay hộp hỏi "Dùng hết hay còn lại?" (`A-12`, `A-15`). Dòng nêu nguyên liệu sẽ giảm chuyển sang màn chúc mừng. Có thanh Hoàn tác (Q10). |
| Q4 | Giữ ràng buộc cứng: tối đa 1 thông báo mỗi ngày, kể cả khi có nhắc rã đông. |
| Q5 | Giờ nhắc ngày thường: mặc định 16:00, chỉnh được trong Cài đặt trong khung 14:00–20:00; vẫn alarm không chính xác. Thay "cố định 17:30, không chỉnh giờ". |
| Q6 | Dữ liệu gốc: 50–60 nguyên liệu, 25–30 công thức, làm 2 đợt (khoảng 40 nguyên liệu và 20 món trước). Người dùng duyệt danh sách tên món trước khi ghi bundle. Thay mục "20–24 công thức". |
| Q7 | Ngoại lệ phân vai: Claude soạn nội dung `recipes.json` và `shelf_table.json`; Codex viết code và test kiểm tra bundle. |
| Q8 | Công thức gốc vẫn là JSON bundle chỉ đọc; món gia đình tự thêm lưu trong SQLite (bảng mới) và có trong sao lưu JSON. Thay ràng buộc "Công thức là JSON bundle, không vào DB" trong `CLAUDE.md` và `AGENTS.override.md`. |
| Q9 | Ngăn bảo quản lưu theo từng món trong tủ (bảng mới, có migration), mặc định theo gợi ý, người dùng đổi bằng một chạm trên thẻ. Thay việc gán cứng theo id trong `isFreezerItem`. |
| Q10 | Thanh Hoàn tác 5 giây sau khi trừ nguyên liệu, khôi phục cả số lượng trong tủ lẫn dòng lịch sử vừa ghi. Nút "Xóa dòng ghi nhầm" ở màn "Đã nấu" có xác nhận ngắn và không trả nguyên liệu về tủ. |
| Q11 | Giữ nguyên thực đơn đã xếp của tuần hiện tại, lưu vào `WeeklyMealPlanEntity`; chỉ tính lại khi sang tuần mới hoặc khi bấm "Đổi món". |
| Q12 | Mỗi ngày tối đa 2 mâm theo bữa đang bật (preset SW-03). Ẩn bữa nào thì danh sách đi chợ bỏ nguyên liệu của mâm đó; món vẫn giữ để bật lại. SW-03 làm trước hoặc cùng lúc. |
| Q13a | Thông báo liệt kê tối đa 3 món, sau đó là "và vài món khác". |
| Q13b | Món hết rồi có lại: hạn tính từ ngày bấm "Có" lại, xem như vừa mua. Gỡ chặn SW-10. |
| Q13c | Giữ ngưỡng "Cần dùng sớm" 0–2 ngày. |
| Q13d | Sửa contract `W4-03` cho đúng code: 180/320dp là chiều rộng tối thiểu cột, không đặt chiều cao thẻ cố định. |
| Q14 | Mở rộng tủ sang ba nhóm mới, làm lần lượt: đồ ăn thừa, rồi gia vị và sốt nền, rồi đồ ăn liền và đồ uống. |
| Q15 | Một danh mục `shelf_table.json` thêm trường `kind` (`fresh`, `seasoning`, `ready`); hành vi khác nhau nằm trong logic thuần theo `kind`. Đồ ăn thừa không thuộc danh mục mà là bản ghi người dùng tạo, lưu trong DB. |
| Q16 | Đồ ăn thừa tạo bằng nút phụ "Còn thừa, cất tủ" ở màn chúc mừng (một chạm, mang tên món) và bằng tay qua hộp thêm món (chọn nhóm "Đồ ăn thừa"). |
| Q17 | Đồ ăn thừa: hạn ước tính mặc định 2 ngày, sửa được; hiện trên dải "Cần dùng sớm"; nút "Đã ăn hết" xóa bản ghi; không vào giỏ đi chợ. |
| Q18 | Đồ ăn thừa có trong thông báo ngày và đứng đầu danh sách. |
| Q19 | Gia vị không bao giờ chặn "nấu được" và không bị trừ khi nấu; công thức chỉ ghi gia vị ở phần tùy chọn để hiển thị. |
| Q20 | Thêm mục "Cần mua thêm" lưu trong DB, hiện ở đầu danh sách đi chợ tuần; dùng cho mọi món người dùng bấm "Hết rồi → Đi chợ". "Đã mua xong · Cất vào tủ" đánh dấu "Còn" cho các món này. |
| Q21 | Đồ ăn liền và đồ uống: chỉ thêm tab lọc trong Tủ lạnh; chưa gợi ý bữa sáng (giữ quyết định "chưa có bữa sáng"). |
| Q22 | Lưu khảo sát 50 persona vào `docs/ux/` với dòng đầu ghi rõ là persona giả lập. Đã lưu tại `docs/ux/pantry-expansion-survey-2026-11-16.md`. |

### Quyết định 2026-11-16 (vòng 2, Q23–Q28)

Nguồn chung: người dùng trả lời "đồng ý hết theo đề xuất" cho vòng hỏi Q23–Q28 trong hội thoại ngày 2026-11-16, sau khi đọc khảo sát `docs/ux/pantry-expansion-survey-2026-11-16.md`.

| # | Quyết định |
|---|---|
| Q23 | Nhắc rã đông: ngày nào bữa tối đã xếp có nguyên liệu đang ở ngăn đông thì thông báo duy nhất của ngày đó dời sang 08:00 cố định, nhắc rã đông kèm đồ nên dùng sớm; các ngày khác nhắc theo giờ ngày thường. Có thẻ "Nhớ rã đông" trên Trang chủ buổi sáng. Chủ Nhật gộp một câu nhắc rã đông vào thông báo đi chợ. Vẫn tối đa 1 thông báo mỗi ngày (Q4). |
| Q24 | Hạn ngăn đông theo từng nguyên liệu: trường `freezerShelfDays` trong `shelf_table.json`, tính lại từ ngày chuyển ngăn. Món không có trường này không hiện lựa chọn "chuyển ngăn đông". Không dùng hệ số nhân chung. |
| Q25 | Đồ ngăn đông ẩn khỏi dải "Cần dùng sớm" ở Trang chủ và khỏi thông báo ngày, vẫn thấy trong tab Ngăn đông. Ngoại lệ: món ngăn đông đã quá hạn ước tính vẫn hiện với câu "kiểm tra lại xem còn dùng được không". |
| Q26 | Món ăn nhanh (món gia đình tự thêm): chỉ gồm tên và danh sách nguyên liệu chọn từ danh mục; bước nấu và ảnh không bắt buộc. Có trong 3 gợi ý, thực đơn tuần, nấu ngẫu nhiên và chịu bộ lọc chế độ ăn theo nguyên liệu. Thiếu bước nấu thì màn chi tiết ẩn phần các bước, chỉ còn nút "Đã nấu xong". |
| Q27 | Đồ ăn thừa chọn được ngăn, mặc định ngăn mát (2 ngày, Q17); ở ngăn đông hạn ước tính mặc định 14 ngày, sửa được, và áp dụng Q25. Nhắc rã đông (Q23) tính cả đồ ăn thừa ngăn đông nằm trong bữa đã xếp. |
| Q28 | Tủ lạnh mở mặc định ở bộ lọc "Đồ tươi"; có đồ ăn thừa nên dùng sớm thì hiện một dòng nhắc ở đầu tab, chạm để chuyển tab; không tự đổi tab. Thay "mặc định hiển thị toàn catalog" trong `SPEC.md`. |

## Đang triển khai theo spec (không đồng nghĩa người dùng đã chốt riêng từng giá trị)

| Phạm vi hiện có | Nguồn/ghi chú |
|---|---|
| Catalog hiện có 25 món và thời hạn mặc định trong `shelf_table.json`. | Giá trị đang triển khai; số lượng/catalog có thể đổi bằng task riêng, không phải quyết định người dùng đã chốt. |
| Ngưỡng ưu tiên dùng sớm 0–2 ngày. | Đang triển khai theo spec/test hiện tại; chưa có nguồn xác nhận riêng của người dùng cho chính các biên này. |
| Công thức và mapping thời lượng trong JSON bundle. | Dữ liệu triển khai hiện tại; việc mở rộng/chỉnh bundle cần task riêng. |

## Đề xuất/chưa được duyệt

| Mục | Tình trạng |
|---|---|
Bốn mục còn mở trước đây (số món trong thông báo, ngày tính khi món có lại, ngưỡng 0–2 ngày, chiều cao thẻ) đã chốt ở Q13 ngày 2026-11-16.

Bốn đề xuất planner (nhắc rã đông, hạn ngăn đông, ẩn đồ ngăn đông, món ăn nhanh) đã được duyệt ở Q23–Q26 ngày 2026-11-16. Hiện chỉ còn một đề xuất chờ duyệt:

| Mục | Đề xuất |
|---|---|
| Từ vựng thống nhất | Chọn một cụm "nên dùng trước" cho mọi chỗ (hiện có năm cách gọi: "Cần dùng sớm", "Nên dùng sớm", "Nên dùng trước", "ưu tiên dùng sớm", "Dùng đồ sắp dùng") và một nhãn cho dải ở Trang chủ; coi `CLAUDE.md` mục Văn phong là danh sách từ cấm gốc, các file khác chỉ trỏ tới đó. Cân nhắc "Cần dùng sớm" và "Cần mua thêm" nghe như mệnh lệnh nhẹ, ngược quy tắc "gợi ý không ra lệnh". Cần người dùng chọn trước khi đổi chuỗi trong app. |

## Quy ước ghi nhận

Khi người dùng quyết định một mục đang mở, thêm quyết định, ngày và nguồn hội thoại vào đây, đồng thời cập nhật `SPEC.md` hoặc spec task liên quan. Không suy ra người dùng đã xác nhận một con số chỉ vì code đã triển khai. Không ghi trạng thái kiểm thử nếu không có kết quả quan sát được; bằng chứng phiên hiện tại nằm ở đầu `HANDOFF.md`.
