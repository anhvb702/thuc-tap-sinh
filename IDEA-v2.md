# Tủ Lạnh Trong Túi (tên làm việc) — Phiên bản 7

> Cập nhật theo nhận xét của Antigravity CLI (argon), báo cáo product và các vòng review UX từ `E:\tools\expert-review\review.ps1`, cùng phản biện của advisor. Phiên bản 1 là bản gốc đầy đủ. Phiên bản 2 sửa theo nhận xét lượt 1 và 2. Phiên bản 3 sửa theo báo cáo product. Phiên bản 4 áp dụng đề xuất UX vòng 1. Phiên bản 5 áp dụng vòng 2–5. Phiên bản 6 chốt ba quyết định mở sau cuộc trao đổi giữa argon và advisor. Phiên bản 7 áp dụng vòng review UX thứ 7.

## 1. Vấn đề cần giải

- Quên mình đang có gì trong tủ, nên mua thừa hoặc mua trùng.
- Đồ tươi (rau, thịt, sữa) hỏng trước khi kịp dùng.
- Quyết định "hôm nay nấu gì" tốn năng lượng, thường kết thúc bằng đồ ăn ngoài.
- Danh sách đi chợ thường thiếu, nhớ ra thì đã ở siêu thị hoặc đã về nhà.

## 2. Người dùng mục tiêu (ICP)

Người đi làm tự nấu ăn tại nhà, đi siêu thị hoặc chợ tích trữ thực phẩm 1–3 lần mỗi tuần. Loại trừ nhóm đi chợ mua tươi nấu ngay trong ngày, vì nhóm này hầu như không cần quản lý tồn kho.

> Đây là lựa chọn làm việc được đề xuất từ báo cáo, chưa được xác nhận bằng nghiên cứu người dùng. Xem mục 8.

## 3. Nguyên tắc thiết kế

1. **Một mô hình trạng thái cho mọi nguyên liệu.** Mỗi nguyên liệu, kể cả trứng, quả và vỉ, đều có ba trạng thái: `Còn nhiều`, `Còn ít`, `Hết`. Không theo dõi gram và không có bộ đếm số lượng riêng trong MVP.
2. **Hạ bậc bằng một chạm, chỉnh bằng giữ.** Chạm để hạ bậc là thao tác chính. Muốn đặt trạng thái bất kỳ thì giữ dòng để mở menu chọn trực tiếp.
3. **Dữ liệu sai thì người dùng mất niềm tin, và dữ liệu sai về thực phẩm có thể gây hại.** Mọi thay đổi đều có Hoàn tác. Khi không chắc chắn, app chọn hướng an toàn: ngăn Mát và hạn ngắn hơn, kèm dấu `?`. Không bao giờ mặc định ngăn Đông khi không chắc.
4. **Giải quyết cho một người nấu chính trước.** Chia sẻ gia đình là giai đoạn sau.

## 4. Tính năng MVP

### 4.1 Nhập mua mới
- Nhắn một câu tự nhiên, ví dụ "mua 1kg cà chua, 2 vỉ trứng, 500g thịt bò". App tách thành từng món và đổi khối lượng sang trạng thái (1kg cà chua → `Còn nhiều`). Khối lượng gốc chỉ lưu trong lịch sử nhập, không dùng để trừ kho.
- **Lưu thẳng, không có màn hình xác nhận chắn ngang.** Các món vừa nhập hiện trong thẻ `Vừa thêm n món` ở đầu tab Tồn kho. Thẻ tồn tại cho đến khi người dùng đóng thẻ hoặc mở app lần sau. Thẻ mặc định hiện tối đa 3 món, ưu tiên món cần chọn ngăn hoặc có dấu `?`; phần còn lại thu vào `Xem thêm k món`. Với mỗi món, thẻ có:
  - chip ngăn `Mát / Đông / Khô` đổi được bằng một chạm, và hạn dùng đổi theo ngăn (ví dụ thịt ngăn mát 2 ngày, ngăn đông 30 ngày);
  - câu gốc người dùng đã nhập, và nút `Hoàn tác cả lần nhập`.
- **Mặc định an toàn:**
  - Khi LLM không chắc ngăn, gán ngăn Mát với hạn ngắn hơn và đánh dấu `?` màu vàng trên chip ngăn. Không bao giờ mặc định ngăn Đông khi không chắc.
  - Nhóm nhạy cảm bắt buộc có ngăn rõ ràng: thịt tươi và gia cầm, cá và hải sản tươi, đậu phụ, sữa thanh trùng, giò chả. Nếu câu nhập chưa nêu ngăn, thẻ hiện chip `Cần chọn ngăn` và tạm tính hạn 1–2 ngày cho đến khi người dùng chọn.
  - Rau lá và trứng không cần hỏi ngăn. Đồ khô và gia vị mặc định ngăn Khô.
- Nếu món trùng tên với món đang còn trong tủ, app đặt trạng thái `Còn nhiều`, giữ hạn **ngắn hơn** giữa hạn cũ và hạn mới (để ưu tiên dùng phần cũ trước), và gắn nhãn nhỏ `Đã gộp (giữ hạn ngắn)`. Không tạo dòng trùng.
- Nhập không kèm số lượng (ví dụ "mua thịt bò, rau cải, cà chua"): mặc định `Còn nhiều`, trừ nhóm nhạy cảm nêu trên.
- Hạn dùng mặc định theo loại (rau lá 3–5 ngày, trứng 3–4 tuần, thịt sống 1–2 ngày ngăn mát). Người dùng chỉ sửa khi khác.
- Nếu mất mạng khi gửi câu nhập, app lưu câu đó thành thẻ `Đang chờ xử lý` ở đầu tab Tồn kho. Khi có mạng, LLM tách món và đưa vào thẻ `Vừa thêm` như trên.
- Ô nhập hỗ trợ nút mic có sẵn của bàn phím điện thoại, để nhập khi tay đang bận.
- Giọng nói, chụp hóa đơn và ảnh thực phẩm: giai đoạn sau.

### 4.2 Tồn kho
- Tab Tồn kho là màn hình mặc định của app. Trên cùng là một ô duy nhất. Ô này **luôn lọc** danh sách theo tên, và lọc được nhiều tên cùng lúc khi ngăn cách bằng dấu phẩy (ví dụ "thịt, hành, cà chua" hiện đúng ba dòng đó để chạm hạ bậc liên tiếp).
- Khi ô có chữ, ngay dưới ô hiện dòng `+ Thêm vào tủ: "[nội dung đang gõ]"`. Dòng này ẩn khi ô trống. Món chỉ được thêm khi người dùng chạm dòng này hoặc bấm phím Gửi. Không có chế độ ẩn nào, và không có chế độ tự đổi theo từ khóa.
- Kết quả lọc bao gồm cả các món đang ở `Hết` (hiện ở cuối kết quả, mờ). Chạm một lần để nâng thẳng từ `Hết` lên `Còn nhiều`.
- Dưới ô lọc là thẻ `Vừa thêm n món` (nếu có, xem 4.1), rồi đến thẻ món đang nấu (nếu có), rồi hàng chip ngăn `Tất cả | Mát | Đông | Khô`.
- Nút `Đi chợ (n)` nằm riêng ở thanh tiêu đề, không nằm trong hàng chip. `n` là số món đang ở `Hết` hoặc `Còn ít`.
- Danh sách mặc định xếp theo hạn vàng hoặc đỏ trước, rồi đến món hay dùng. Màu theo hạn: xanh (còn lâu), vàng (còn tối đa 2 ngày), đỏ (quá hạn).
- Mỗi dòng có một nút trạng thái lớn có nhãn rõ ràng (ví dụ `Còn nhiều ›`). Chạm để hạ bậc: `Còn nhiều` → `Còn ít` → `Hết`.
- Khi chạm sang `Hết`, dòng mờ và gạch ngang tại chỗ trong 3 giây, rồi ẩn khỏi danh sách chính. Trong 3 giây đó, chạm lần nữa vào nút sẽ đưa món về `Còn nhiều`.
- **Giữ dòng** để mở menu với ba lựa chọn `Còn nhiều / Còn ít / Hết`. Đây là cách đặt trực tiếp một trạng thái. Lần đầu người dùng chạm hạ bậc, app hiện gợi ý nhỏ ở thanh đáy: `Mẹo: giữ dòng để đặt lại trạng thái`.
- **Hoàn tác theo phiên:** các thao tác hạ bậc liên tiếp cách nhau dưới 5 giây được gom vào một thanh ở đáy màn hình, ghi `Đã cập nhật n món — Hoàn tác`. Chạm `Hoàn tác` khôi phục toàn bộ thao tác trong phiên đó. Thanh luôn chỉ có một. Khi phiên kết thúc (quá 5 giây không thao tác), thanh biến mất.
- **Đi chợ** (mở bằng nút `Đi chợ (n)`): hai nhóm, `Đã hết (cần mua)` ở trên và `Còn ít (mua thêm)` ở dưới.
  - Chạm một món để đánh dấu `Đã mua`. Món được gạch ngang và chuyển xuống cuối nhóm; chạm lại để bỏ chọn.
  - Dấu `Đã mua` được lưu trên máy cho đến khi bấm `Hoàn tất đi chợ`, nên không mất khi thoát app.
  - `Hoàn tất đi chợ`: các món đã đánh dấu chuyển về `Còn nhiều` và hạn dùng đặt lại từ hôm nay. Món chưa đánh dấu giữ nguyên trạng thái.
  - Nút `Đã mua tất cả` đánh dấu cả danh sách.
  - Trong màn hình Đi chợ, khi ô trên cùng có chữ, hiện hai nút một chạm ngay dưới ô: `+ Cần mua` (vào nhóm `Đã hết`) và `+ Đã bỏ giỏ` (vào thẳng với dấu `Đã mua`). Không đi qua thẻ `Vừa thêm`.

### 4.3 Sau khi nấu
- Với món lấy từ gợi ý: chạm `Đã nấu xong` trên thẻ ghim. App lùi sẵn một bậc cho các nguyên liệu của món và hiện thanh ở đáy: `Đã trừ nguyên liệu [tên món] — Chỉnh sửa / Hoàn tác`. Thanh giữ 8 giây. Chạm `Chỉnh sửa` sẽ lọc danh sách Tồn kho đúng các nguyên liệu vừa trừ để người dùng chỉnh trực tiếp trên từng dòng. Một chạm là xong, không có bước xác nhận thêm.
- Với món tự nấu: không có bước riêng. Người dùng hạ bậc trực tiếp trên tab Tồn kho, lọc nhiều tên cùng lúc để chạm liên tiếp, hoặc dùng chip `Tất cả`.
- Không tự trừ gram. Không khớp món tự nấu bằng câu tự nhiên trong MVP.

### 4.4 Gợi ý món ăn
- Khi mở mục gợi ý: ưu tiên món có nguyên liệu `Còn ít` hoặc hạn vàng/đỏ, xếp lên đầu. Nếu không có nguyên liệu nào vàng hoặc đỏ, gợi ý món từ nguyên liệu đang có, xếp theo độ nhanh và dễ làm. Như vậy ngày đầu tiên người dùng vẫn có gợi ý.
- Gợi ý là tab thứ hai của app (tab đầu là Tồn kho). Mỗi lần mở, LLM trả về sẵn 3–5 món trong một lần gọi, để nút `Đổi món` chuyển ngay không phải chờ mạng. Kết quả được lưu đệm trên máy và chỉ gọi lại LLM khi tồn kho thay đổi hoặc khi hết lượt `Đổi món`.
- Khi chạm `Chọn nấu món này`, thẻ món được ghim lên đầu tab Tồn kho với nút `Đã nấu xong` và `Hủy`, cho đến khi người dùng xác nhận hoặc hủy. Thẻ tự hủy sau 24 giờ nếu không thao tác (đề xuất tạm, cần kiểm chứng). Trạng thái được lưu trên máy để không mất khi thoát app.
- Thông báo đẩy chỉ gửi khi có nguyên liệu vàng hoặc đỏ, buổi sáng hoặc buổi chiều.
- Món được gợi ý dựa trên LLM với danh sách nguyên liệu hiện có và gia vị mặc định của bếp Việt (muối, đường, nước mắm, dầu ăn, hành, tỏi, mắm, bột ngọt). Không xây kho công thức cứng trước khi có người dùng. Tên gọi vùng miền và từ đồng nghĩa (bột ngọt = mì chính, hành hoa = hành lá) được xử lý trong prompt của LLM, không xây danh mục riêng.

### 4.5 Đã bỏ khỏi MVP
- Danh sách đi chợ tự động theo ngưỡng tối thiểu. Thay bằng màn hình Đi chợ ở 4.2.
- Khớp món tự nấu bằng câu tự nhiên.
- Chế độ tự đổi giữa tìm kiếm và nhập liệu theo từ khóa.
- Màn hình xác nhận chắn ngang khi nhập mua mới.
- Chia sẻ gia đình và thông báo khi ai đã mua.
- Gửi danh sách sang app giao hàng.
- Xuất dữ liệu. MVP chỉ có xóa toàn bộ dữ liệu.
- Thống kê lãng phí bằng tiền.
- Chia sẻ đồ sắp hết hạn với hàng xóm.
- Giọng nói (ngoài mic có sẵn của bàn phím), chụp hóa đơn, ảnh thực phẩm.

### 4.6 Backlog sau MVP: hỏi ý kiến người dùng thật
- Mục tiêu: khi có quyết định sản phẩm chưa chắc chắn, hỏi trực tiếp người dùng thật thay vì chỉ dựa vào review của mô hình AI.
- Hình thức: khảo sát ngắn trong app, tối đa 3 câu, mở một lần mỗi tuần và có thể bỏ qua. Người dùng có thể đề xuất tính năng bằng một ô văn bản ngắn.
- Nguyên tắc: không quảng cáo, không chèn nội dung để giữ người dùng ở lại lâu hơn, không thu thập dữ liệu ngoài phạm vi câu hỏi. Chỉ hỏi khi người dùng đã đồng ý.
- Điều kiện mở: sau tuần 4, khi đã có người dùng dùng đều đặn.

## 5. Lộ trình MVP

| Giai đoạn | Nội dung | Mục tiêu kiểm chứng |
|---|---|---|
| Tuần 1 | Nhập mua mới và thẻ `Vừa thêm`, ô lọc nhiều tên, hạ bậc một chạm, giữ dòng để chỉnh, hoàn tác theo phiên, màn hình Đi chợ | Người dùng có cập nhật thường xuyên không |
| Cuối tuần 1 | Đưa cho 5–8 người dùng thật nhập bằng giọng nói sau khi đi chợ về | Tỷ lệ món bị gán sai ngăn hoặc hạn mà sau 24 giờ chưa ai sửa (xem mục 8) |
| Tuần 2 | Đánh dấu "đã nấu xong" cho món gợi ý, gợi ý món, ghim món đang nấu, thông báo buổi sáng hoặc chiều (bật cho nhóm người dùng đã thử) | Gợi ý có được chấp nhận không, thông báo có được bật không |
| Tuần 3–4 | Theo dõi và sửa theo phản hồi thật | Họ có mở app lúc 5–6h chiều không, có giảm được đồ bỏ đi không |

Chỉ số giữ chân 14 ngày cần thêm thời gian sau tuần 4. Lộ trình trên là 4 tuần đến khi có bản đầu đo được, chưa đủ để kết luận giữ chân.

## 6. Kiến trúc

- Client: Flutter hoặc React Native.
- Lưu trên máy: trạng thái ghim món đang nấu, dấu `Đã mua` chưa hoàn tất, bộ đệm gợi ý, câu nhập chưa gửi được, và thẻ `Vừa thêm` (đến khi người dùng đóng).
- Backend và đồng bộ: dùng dịch vụ có sẵn (Supabase hoặc Firebase) cho MVP, không tự viết đồng bộ SQLite và xử lý xung đột.
- AI: LLM nhỏ qua API để phân tách câu nhập mua mới và gợi ý món. Model on-device chưa dùng ở MVP.
- Dữ liệu cá nhân: cho phép xóa toàn bộ dữ liệu.

## 7. Rủi ro

| Rủi ro | Cách xử lý |
|---|---|
| Gán sai ngăn làm hạn sai, nguy cơ an toàn thực phẩm | Mặc định ngăn Mát khi không chắc, nhóm nhạy cảm bắt buộc có ngăn, chip ngăn trên thẻ `Vừa thêm` |
| Gộp nhầm vào món cũ, ghi đè hạn mà không ai biết | Giữ hạn ngắn hơn, nhãn `Đã gộp (giữ hạn ngắn)` |
| Dữ liệu tồn kho lệch thực tế | Trạng thái định tính, nút sửa ngay, không tự trừ gram |
| Người dùng bỏ app sau vài tuần | Nhập một câu, không có bước bắt buộc, thông báo chỉ khi có việc cần làm |
| Ngày đầu không có gợi ý | Gợi ý theo nguyên liệu đang có khi không có gì vàng hoặc đỏ |
| Gợi ý không hợp khẩu vị lúc tối | Ưu tiên món nhanh và dễ làm, cho phép bỏ qua và đổi món |
| Chạm nhầm trạng thái khi thao tác nhanh | Hoàn tác theo phiên, giữ dòng để chỉnh, vùng chạm lớn |
| Màn hình đầu tab Tồn kho quá đông thẻ | Giới hạn thẻ `Vừa thêm` 3 món, dòng thêm chỉ hiện khi có chữ |
| Người dùng không chịu trả phí | Giữ miễn phí toàn bộ MVP |
| Chọn sai người dùng mục tiêu | Thử với cả hai nhóm ở tuần đầu, dữ liệu quyết định nhóm nào giữ lại |

## 8. Câu hỏi cần trả lời trước khi làm

1. ICP ở mục 2 có đúng không? Cần phỏng vấn 5–10 người trước khi code, không chờ đến tuần 3.
2. Giá trị giữ chân nằm ở nhắc hạn dùng hay ở gợi ý món? Nếu là nhắc hạn, làm sao để thông báo không bị tắt sau một tuần?
3. Người dùng có chịu mở app mỗi ngày, hay chỉ khi đi chợ về? Điều này quyết định tần suất thông báo.
4. Đội ngũ có bao nhiêu người và bao nhiêu giờ mỗi tuần? Chưa có thông tin này nên chưa đánh giá được tính khả thi của lộ trình.
5. Ghim "món đang nấu" tự hủy sau 24 giờ là đề xuất tạm. Cần kiểm chứng với người dùng thật.
6. Người dùng có hay mua món mới phát sinh ngoài danh sách không? Điều này quyết định mức ưu tiên của luồng thêm món trong màn hình Đi chợ.
7. Ngưỡng nhóm nhạy cảm (thịt, gia cầm, cá, hải sản, đậu phụ, sữa, giò chả) có đủ không, và LLM có phân loại đúng trong tiếng Việt không? Cần kiểm chứng bằng bộ câu nhập mẫu.
8. **Kiểm chứng trước khi tin bộ quyết định mục 4.1 và 4.2:** cho 5–8 người thật nhập bằng giọng nói sau khi đi chợ về. Đo tỷ lệ món bị gán sai ngăn hoặc hạn mà sau 24 giờ chưa ai sửa. Nếu tỷ lệ này không gần 0, cần đưa lại một bước xác nhận nhẹ, chỉ cho nhóm nhạy cảm.
9. Ngưỡng "lần đầu" để hiện gợi ý `Mẹo` (3 lần hay 1 lần) và thời gian thanh `Đã trừ nguyên liệu` (8 giây) là đề xuất tạm. Cần kiểm chứng.

## 9. Tiêu chí thành công

- Sau 14 ngày, khoảng 50% người dùng thử vẫn mở app ít nhất 3 ngày mỗi tuần.
- Mỗi người giảm được ít nhất một món bị bỏ đi mỗi tuần (tự báo cáo).
- Tỷ lệ món bị gán sai ngăn hoặc hạn mà sau 24 giờ chưa được sửa. Mục tiêu gần 0, theo dõi riêng cho nhóm nhạy cảm.
- Tỷ lệ người dùng phải sửa lại tên nguyên liệu, ngăn hoặc hạn dùng mặc định sau khi nhập mua mới. Mục tiêu giảm dần qua các tuần.
- Tỷ lệ người dùng bấm Hoàn tác sau khi hạ bậc, và tỷ lệ dùng menu giữ dòng để sửa. Theo dõi để phát hiện chạm nhầm thường xuyên.
