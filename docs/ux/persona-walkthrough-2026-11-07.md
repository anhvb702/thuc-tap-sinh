# Walkthrough UX theo persona — 2026-11-07

## Phạm vi và giới hạn

Đây là walkthrough persona từ source, chưa hoàn tất xác minh UX runtime. Mai, Nam và Hạnh là persona giả lập, không phải người tham gia nghiên cứu người dùng. `:composeApp:installDebug` đã thành công trên API 37, nhưng app launch gặp ANR/system-server issues; AVD API 36 hiện mở cửa sổ nhưng ADB vẫn offline. Vì vậy không kết luận runtime UX đã được xác minh.

## Chị Mai — thêm món nhanh, nắm được tủ

### Điều làm được
- Lưới nguyên liệu có thao tác chạm để đổi trạng thái Có/Hết; có vùng thêm món với nút “Thêm vào tủ”.
- Trang chủ có tổng số nguyên liệu sẵn sàng, nhóm món cần dùng sớm và gợi ý bữa tối.

### Khúc mắc
- Thêm món cần gõ tên rồi xác nhận; lựa chọn hạn dùng hiện là 2/3/7/14 ngày, chưa có lựa chọn điền sẵn theo món. Kịch bản mong ít chạm.
- Trang chủ có tên các món cần ưu tiên và công thức, nhưng muốn thấy vài món tiêu biểu rõ ràng hơn.
- Khi tên không khớp, có đường báo lỗi nhưng lời mời thử lại chưa đủ rõ.
- Nhãn “Dùng món cần ưu tiên” nghe như mệnh lệnh.

### Thiếu
- Chọn nhanh món/hạn dùng phổ biến và cách retry rõ ràng sau khi không tìm thấy tên.

### Mong muốn
- Chọn sẵn hợp lý, ít chạm; nhìn nhanh một vài tên món tiêu biểu trên Home; lời nhắc gợi ý nhẹ nhàng.

### Ưu tiên
1. Giảm thao tác nhập tên và chọn hạn.
2. Làm tên món tiêu biểu trên Home dễ nhận ra.
3. Sửa copy lỗi không khớp và nhãn gợi ý theo hướng mềm hơn.

### Căn cứ file
- `composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt`: hộp thêm món, nút xác nhận, trường hạn dùng và thông báo lỗi.
- `composeApp/src/main/kotlin/com/loitu/yourrefri/feature/home/ui/HomeScreen.kt`: danh sách món cần ưu tiên và ba gợi ý.
- `composeApp/src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/RecipeScreen.kt`: nhãn lý do gợi ý.
- `docs/tests/W5-01.md`: hợp đồng lookup tên món không có trong catalog phải giữ nguyên lựa chọn và mời thử lại.

## Anh Nam — chọn món nấu theo nguyên liệu và thời gian

### Điều làm được
- Có ba công thức; giao diện phân loại nguyên liệu “Đã có” và “Cần thêm”.
- Có bước xác nhận khi chọn “Đã nấu xong”.

### Khúc mắc
- Bộ lọc thời gian đang dùng ngưỡng exclusive (“Dưới 15 phút”, “Dưới 30 phút”), nên món đúng 15/30 phút bị loại khỏi ngưỡng tương ứng.
- Sau khi xác nhận nấu xong, không thấy thao tác hoàn tác trong flow.
- Trước follow-up, entry `rau-muong-xao-hanh` đặt title “Rau muống xào hành” trong khi steps/required gọi “Hành tây”. Đây là mơ hồ tên gọi, không phải nguyên liệu trái nhau: cả steps và required cùng chỉ `hanh-tay`. W5-02A đã làm rõ title thành “Rau muống xào hành tây”; không thêm nguyên liệu mới vì catalog không có tỏi.
- “Nấu được ngay” có thể được hiểu là đã có đủ mọi thứ, trong khi cách so khớp nguyên liệu/gia vị có thể để lại điều người dùng cần tự kiểm tra.

### Thiếu
- Quyết định có đổi hợp đồng lọc thời gian sang inclusive hay giữ cách lọc exclusive hiện hành; undo sau khi trừ nguyên liệu; diễn đạt rõ phạm vi “đủ nguyên liệu”.

### Mong muốn
- Nhãn “Tối đa 15 phút” / “Tối đa 30 phút” (đề xuất chưa được áp dụng), phản hồi có thể hoàn tác, và phân biệt nguyên liệu chính với gia vị/đồ cần thêm.

### Ưu tiên
1. Đã làm rõ tên recipe rau muống xào hành tây.
2. Xin chủ sản phẩm quyết định có sửa contract/test W3-04 để chuyển sang inclusive hay giữ exclusive; chưa triển khai do contract hiện hành yêu cầu exclusive.
3. Đánh giá undo và copy “Nấu được ngay” ở lượt follow-up.

### Căn cứ file
- `composeApp/src/main/assets/recipes.json`: ba recipe; entry `rau-muong-xao-hanh` đã có title “Rau muống xào hành tây”, steps và required dùng hành tây/`hanh-tay`. Catalog runtime chỉ có hành tây, không có tỏi.
- `composeApp/src/main/assets/shelf_table.json`: catalog runtime được ship cùng app; có ID `hanh-tay`, không có ID `toi`. `shared/src/commonMain/resources/shelf_table.json` cũng hiện không có `toi`.
- `composeApp/src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/RecipeScreen.kt`: nhãn lọc, trạng thái “Nấu được ngay” và xác nhận nấu xong.
- `shared/src/commonMain/kotlin/com/loitu/yourrefri/feature/recipe/logic/RecipeDurationFilter.kt`: so sánh hiện tại loại trừ ngưỡng.
- `docs/tests/W3-03.md`, `docs/tests/W3-04.md`: contract hiện hành quy định hành vi tiêu thụ sau xác nhận và ngưỡng lọc exclusive. Vì vậy W5-02B deferred/needs user decision; mutation probes của đề xuất inclusive trước đó không được tính là kiểm chứng trên contract được chấp nhận.

## Hạnh — persona exploratory (không phải user research)

### Điều làm được
- Lưới có cấu trúc dễ hiểu theo kịch bản; vùng chạm chính có content description gọi tên nguyên liệu và hành động Có/Hết.
- Có lời giải thích khi bật nhắc thông báo, và có tính năng sao lưu/phục hồi JSON offline.

### Khúc mắc
- Khi fontScale >= 1.3, tên trong thẻ vẫn `maxLines = 1`, có thể bị cắt.
- Lời giải thích quyền thông báo dài.
- JSON backup có thể khó hiểu với người không quen định dạng dữ liệu.
- Mong có undo và xác nhận an toàn dễ nhận biết.

### Thiếu
- Kiểm chứng TalkBack, bố cục cỡ chữ lớn, flow quyền thực tế và khả năng hiểu/khôi phục backup bằng emulator hoặc người dùng thật.

### Mong muốn
- Tên món đọc trọn ở cỡ chữ lớn; nội dung quyền ngắn, dễ hiểu; hướng dẫn backup dễ theo; có lối lui sau thao tác ảnh hưởng dữ liệu.

### Ưu tiên
1. Kiểm tra tên món ở fontScale lớn và TalkBack trên thiết bị.
2. Rút gọn, chia bước giải thích quyền.
3. Đánh giá khả năng hiểu JSON và nhu cầu undo/xác nhận với nghiên cứu người dùng.

### Căn cứ file
- `composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt`: thay đổi độ rộng cột theo fontScale và tên thẻ giới hạn một dòng; semantics của thao tác.
- `composeApp/src/main/kotlin/com/loitu/yourrefri/feature/settings/ui/SettingsScreen.kt`, `composeApp/src/main/kotlin/com/loitu/yourrefri/MainActivity.kt`: lời giải thích và yêu cầu quyền thông báo.
- `shared/src/commonMain/kotlin/com/loitu/yourrefri/feature/backup/data/BackupCodec.kt`, `composeApp/src/main/kotlin/com/loitu/yourrefri/feature/settings/ui/SettingsScreen.kt`: codec và giao diện backup/restore.
- `docs/tests/W4-03.md`: hợp đồng trợ năng hiện có; test source không thay thế kiểm tra TalkBack/layout runtime.

## Hợp đồng follow-up và bằng chứng triển khai

### W5-02A — Đã triển khai: làm rõ recipe rau muống xào hành tây

Contract test-first và biên bản mutation/Gradle đầy đủ: `docs/tests/W5-02A.md`.

- **Mục tiêu:** Sửa entry `rau-muong-xao-hanh` trong JSON bundle để title là “Rau muống xào hành tây”; steps và required thống nhất về ID `hanh-tay`.
- **Input:** recipe ID hiện tại; catalog runtime `composeApp/src/main/assets/shelf_table.json`.
- **Expected Output:** JSON shipped parse được; title ghi “Rau muống xào hành tây”; steps nêu hành tây; required dùng `hanh-tay`; ID này có trong asset catalog runtime.
- **Biên/rủi ro:** Không đổi recipe ID nếu không có lý do tương thích; không để title/steps/required lệch nhau; kiểm tra catalog runtime tại `composeApp/src/main/assets/shelf_table.json`.
- **Mutation/evidence:** Sáu biến thể mutation trên asset recipe đều bị bắt sau khi tăng cường test: yêu cầu đúng 4 steps và xác nhận rõ hai step đầu nhắc hành tây. Test bao phủ asset shipped và ingredient IDs trong catalog.

### W5-02B — Deferred: copy và hành vi chip thời lượng (cần quyết định chủ sản phẩm)

- **Đề xuất persona:** Dùng nhãn “Tất cả”, “Tối đa 15 phút”, “Tối đa 30 phút” với ngưỡng inclusive. Chưa triển khai: `docs/tests/W3-04.md` và test hiện hành yêu cầu ngưỡng exclusive; thay đổi cần quyết định trước việc sửa contract/test.
- **Input dự kiến nếu được duyệt:** danh sách recipe giữ thứ tự; duration có thể null, dương, không dương hoặc recipe không nhận diện.
- **Expected Output dự kiến nếu contract được sửa:** 15 phút thuộc lựa chọn tối đa 15; 30 phút thuộc tối đa 30; null threshold trả đủ danh sách; thứ tự nguồn được giữ.
- **Trạng thái:** Deferred/needs user decision. Nhãn và quy tắc inclusive vẫn là đề xuất chưa được giải quyết.
- **Mutation:** Không ghi nhận mutation probes cho đề xuất inclusive như kiểm chứng được chấp nhận; chúng được thử trước khi phát hiện xung đột với W3-04, sau đó code/test/label đề xuất đã được revert.



## Bằng chứng kiểm chứng sau follow-up

- `:shared:testDebugUnitTest` — BUILD SUCCESSFUL, 312 tests.
- `:composeApp:testDebugUnitTest` — BUILD SUCCESSFUL, 34 tests.
- `:composeApp:installDebug` thành công trên API 37; không xác minh được launch/UX do ANR và lỗi system services. AVD API 36 mở được cửa sổ nhưng ADB còn offline. Runtime verification chưa hoàn tất.
- Sáu mutation cho shipped recipe asset đều bị bắt sau khi tăng cường kiểm tra đúng 4 steps và nội dung hành tây ở hai step đầu.


