# Walkthrough UX bổ sung — 10 persona giả lập — 2026-11-07

## Phạm vi và cách đọc

Đây là **source walkthrough mô phỏng**, không phải phỏng vấn hoặc nghiên cứu người dùng thật. Các ghi chú runtime bên dưới chỉ xác nhận những gì thấy được trên emulator; chúng không thay thế đánh giá với người dùng.

## Các persona

### 1. Linh — phụ huynh đơn thân, cần thêm món nhanh

- **Làm được:** Có thể chọn món từ chip gợi ý; chip điền tên vào ô và nút xác nhận lưu món.
- **Vướng mắc thấy trong source:** Thao tác vẫn cần chọn chip rồi xác nhận. Trang chủ chỉ xem trước tối đa ba tên; danh sách đầy đủ ở tab Tủ lạnh.
- **Suy luận/mong muốn:** Linh muốn ít chạm hơn và muốn quy đổi khẩu phần cho gia đình. Bundle hiện không có dữ liệu số người ăn hay định lượng chuẩn để nhân khẩu phần.
- **Căn cứ:** `PantryGridScreen.kt`, `HomeScreen.kt`, bundle công thức.

### 2. Bình — sinh viên, ưu tiên món ít nguyên liệu và tiết kiệm

- **Làm được:** Xem món và phân biệt nguyên liệu app nhận diện là “Đã có” hoặc “Cần thêm”.
- **Vướng mắc thấy trong source:** App đang đóng gói ba công thức; không có metadata giá hoặc chi phí.
- **Suy luận/mong muốn:** Bình muốn thêm món ít nguyên liệu, chi phí thấp. Không thể khẳng định món hiện tại đắt/rẻ nếu không có dữ liệu giá và khẩu phần đáng tin.
- **Điều chỉnh thông tin:** Ba món hiện có là canh chua cá basa, thịt heo kho trứng và rau muống xào hành tây; không có món bò.
- **Căn cứ:** `composeApp/src/main/assets/recipes.json`; `RecipeScreen.kt`.

### 3. Trang — ăn chay, dị ứng trứng

- **Làm được:** Xem thành phần cần có theo công thức và trạng thái có/thiếu dựa trên tủ.
- **Vướng mắc thấy trong source:** Recipe bundle không có metadata chế độ ăn hoặc dị ứng; `canCook` so khớp nguyên liệu được khai báo, không xác minh an toàn dị ứng hay mọi gia vị khi nấu.
- **Suy luận/mong muốn:** Trang muốn lọc món theo chế độ ăn và tránh trứng. Cần dữ liệu đầy đủ, có căn cứ trước khi thêm bộ lọc.
- **Giới hạn sản phẩm:** Không gắn nhãn hay đưa lời hứa “an toàn dị ứng” dựa trên dữ liệu hiện tại.
- **Căn cứ:** `recipes.json`, `RecipeScreen.kt`, logic `canCook`.

### 4. Dũng — nấu theo mẻ, có đồ để ngăn đá

- **Làm được:** Điều chỉnh số lượng bằng nút cộng/trừ; thao tác trừ ở mức một được mô tả là đánh dấu món hết.
- **Vướng mắc thấy trong source:** Phân loại ngăn đông dựa trên danh sách ID cố định, không có lựa chọn vị trí lưu riêng cho từng món.
- **Suy luận/mong muốn:** Dũng muốn quản lý vị trí ngăn đá và cần hiểu rõ kết quả khi bấm trừ. Thay đổi quantity sẽ đụng hợp đồng hiện tại nên không đổi semantics trong task này.
- **Điều chỉnh thông tin:** `−` tại số lượng 1 chuyển món sang “Hết” là hành vi đã có trong W1-08/W3-03; content description cũng nêu “Đánh dấu hết”. Đây không phải bug đã xác nhận.
- **Căn cứ:** `PantryGridScreen.kt`, `PantryGridReducer.kt`, `docs/tests/W3-03.md`.

### 5. Cô Lan — 68 tuổi, cần chữ dễ đọc

- **Làm được:** Có tên nguyên liệu và mô tả trợ năng cho một số nút thao tác.
- **Đã sửa trong source:** Giới hạn một dòng/ellipsis trên tên món đã được gỡ.
- **Đã kiểm tra runtime:** Ảnh Pantry API 36 ở 360dp và font scale 1.3 cho thấy các thẻ đang hiển thị vẫn cho phép tên/trạng thái/thao tác xuống dòng; nhãn Trang chủ ở bottom navigation xuống hai dòng nhưng vẫn nhìn thấy. Ảnh: `build/verification_reports/ux-followup-pantry360-font13.png`.
- **Chưa biết:** Món dài “Sữa tươi (đã mở)” không vào viewport nên chưa có bằng chứng trực quan cho tên dài. Kiểm chứng runtime W4-03 vẫn pending: chưa xác minh font scale 2.0, TalkBack, yêu cầu chiều cao thẻ hay tên dài; ảnh 360dp chỉ ghi nhận các mục đang nhìn thấy.
- **Suy luận/mong muốn:** Cô Lan muốn đọc trọn tên và nghe hành động nút rõ ràng.
- **Căn cứ:** `PantryGridScreen.kt`, `docs/tests/W4-03.md`.

### 6. Hải — lo ngày ước tính bị hiểu là bảo đảm an toàn

- **Làm được:** Thấy trạng thái ưu tiên theo ngày và lời nhắc kiểm tra thực phẩm.
- **Điều chỉnh finding:** Nhận xét “thiếu chữ ước tính/cảnh báo sau ngày” là **false positive** khi đối chiếu source. `StatusText.kt` đã ghi mốc là ước tính và khi qua mốc có câu “kiểm tra lại xem còn dùng được không”.
- **Suy luận/mong muốn:** Hải muốn chắc ứng dụng không khẳng định độ an toàn. Không phát hiện thiếu sót copy mới trong lượt này.
- **Căn cứ:** `shared/src/commonMain/kotlin/com/loitu/yourrefri/feature/pantry/logic/StatusText.kt`.

### 7. Minh — làm việc theo ca

- **Làm được:** Settings nêu lời nhắc lúc 17:30, tối đa một thông báo mỗi ngày và có thể bật/tắt.
- **Vướng mắc thấy trong source:** Không có lựa chọn giờ khác.
- **Suy luận/mong muốn:** Minh muốn giờ nhắc theo ca làm. Đây là nhu cầu persona nhưng trái với yêu cầu sản phẩm cố định 17:30; không đưa vào backlog sửa hiện tại.
- **Căn cứ:** `SettingsScreen.kt`, `ReminderScheduler.kt`.

### 8. Vy — gia đình dùng chung tủ

- **Làm được:** Có thể xuất/khôi phục bản sao thủ công.
- **Vướng mắc thấy trong source:** Mỗi máy lưu dữ liệu cục bộ; không có tài khoản, đồng bộ hay ghi nhận thành viên cập nhật.
- **Suy luận/mong muốn:** Vy muốn trạng thái tủ dùng chung theo thời gian thực. Điều này vượt ràng buộc local-only/no backend; hướng gần nhất hiện có là người dùng tự chuyển tệp.
- **Căn cứ:** `SettingsScreen.kt`, luồng SAF trong `MainActivity.kt`, manifest.

### 9. Quân — thường dùng app khi không có mạng

- **Làm được:** Có thể xem dữ liệu tủ và bundle công thức được đóng gói trong app.
- **Đã sửa trong source:** Settings hiện nói rõ các chức năng chính dùng được khi không có mạng và nêu các bước chuyển tệp sao lưu thủ công.
- **Đã kiểm tra runtime:** Ảnh Settings trên API 36 cho thấy copy mới được hiển thị. Có thử mở app khi ngắt mạng, nhưng trạng thái mạng chưa đủ chắc để kết luận offline đã được kiểm chứng.
- **Suy luận/mong muốn:** Quân muốn yên tâm dùng app khi mất mạng; xác minh bằng source/copy không thay thế kiểm tra offline đáng tin cậy.
- **Căn cứ:** `SettingsScreen.kt`, `composeApp/src/main/AndroidManifest.xml`, `MainActivity.kt`, recipe assets.

### 10. Hoa — chuyển dữ liệu khi đổi điện thoại

- **Làm được:** Có luồng lưu và mở bản sao; trước khi khôi phục, app hỏi xác nhận và có lựa chọn “Để sau”.
- **Đã sửa trong source:** Settings dùng hướng dẫn phổ thông: lưu bản sao thành tệp, tự chuyển tệp sang điện thoại mới, rồi mở tệp trong YourRefri. Vẫn cảnh báo khôi phục sẽ thay dữ liệu hiện tại, có lựa chọn “Để sau”, và nói ảnh đính kèm không nằm trong bản sao.
- **Đã kiểm tra runtime:** Ảnh Settings trên API 36 cho thấy copy mới được hiển thị; luồng chọn tệp/restore end-to-end chưa được kiểm tra trong lượt này.
- **Suy luận/mong muốn:** Hoa muốn hướng dẫn ngắn và biết ảnh đính kèm không nằm trong bản sao. Người dùng chủ động chọn file; đây không phải đồng bộ tự động.
- **Căn cứ:** `SettingsScreen.kt`, `MainActivity.kt`, `docs/tests/W4-02.md`.

## Tổng hợp và hướng tiếp theo

- **UX-10 đã triển khai:** Settings làm rõ dùng offline và hướng dẫn chuyển tệp. `:composeApp:testDebugUnitTest` — 45 tests xanh; `:composeApp:assembleDebug` và `:composeApp:installDebug` thành công. Chi tiết contract và mutation tại `docs/tests/UX-10.md`.
- **Runtime API 36:** `MainActivity` resumed, logcat không thấy YourRefri FATAL/ANR; ảnh Home, Settings và Pantry ở `build/verification_reports/ux-followup-home2.png`, `build/verification_reports/ux-followup-settings.png`, `build/verification_reports/ux-followup-pantry.png`. Settings screenshot xác nhận copy hiển thị. Thêm ảnh Pantry ở 360dp/font scale 1.3: `build/verification_reports/ux-followup-pantry360-font13.png`; các thẻ có trong viewport và nhãn Home navigation wrap nhưng vẫn thấy. Chưa thêm mục tên dài vào viewport.
- **Offline:** chưa kết luận đã xác minh chế độ máy bay/mất mạng; lần thử hiện tại chưa đủ chắc.
- **Cần kiểm tra runtime tiếp:** món tên dài trong viewport, font scale 2.0, TalkBack, chiều cao thẻ theo W4-03 và luồng picker/restore trên emulator.
- **Cần dữ liệu hoặc quyết định sản phẩm trước khi mở rộng:** công thức mới/chi phí, thông tin khẩu phần, metadata chế độ ăn/dị ứng, vị trí ngăn đá.
- **Không sửa trong phạm vi hiện tại:** giờ nhắc khác 17:30, chia sẻ tủ nhiều thiết bị, quantity semantics đã có contract, hoặc lời cảnh báo hạn dùng vốn đã đúng.

Walkthrough này giúp lập giả thuyết và chọn điểm kiểm tra tiếp theo; tần suất persona nêu một pain point không phải số đo mức độ phổ biến.
