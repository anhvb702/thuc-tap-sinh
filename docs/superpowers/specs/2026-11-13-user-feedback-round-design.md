# Thiết kế: vòng góp ý người dùng (10 mục) — 2026-11-13

Trạng thái: **chờ duyệt**. Chưa có code nào được viết.

## Ý định

Người dùng đưa 10 góp ý và 2 luồng thao tác gợi ý (thêm món thủ công, rảnh tay có ảnh). Mục đích: app hấp dẫn và dễ nấu hơn với người dùng phổ thông, nhìn nhiều hơn đọc, mà vẫn giữ ràng buộc của YourRefri (offline, dữ liệu trên máy, văn phong ấm, không so sánh/điểm số).

## Mục tiêu / không mục tiêu

Mục tiêu: 10 mục dưới đây, chia ba đợt.
Không mục tiêu: số lượng nguyên liệu theo khối lượng, tab thống kê "Đã dùng", GIF/video, tải ảnh từ mạng, confetti và câu chúc khi lưu món, điều khiển giọng nói cho rảnh tay, nhập món từ link web.
Điều làm thất bại dù chạy được: thêm quá một thông báo mỗi ngày; văn phong có "hết hạn", "lãng phí", điểm số, so sánh; tính năng cần mạng.

## Ràng buộc đang áp dụng

CLAUDE.md: offline, SQLDelight, tối đa 1 thông báo/ngày, `logic` thuần, MVI, route chỉ kiểu nguyên thủy, tiêm `Clock`/`TimeZone`. Mỗi task ≤ ~300 dòng diff. Nếu có contract trong `docs/tests/` thì test trước code.
Ngoại lệ cần ghi vào `DECISIONS.md` trước đợt C: món do người dùng tạo được lưu trong DB (hiện công thức chỉ là JSON bundle).

## Các cách đã cân nhắc (thứ tự làm)

1. **A → B → C (đã chọn).** Đợt A có giá trị thấy ngay, không đổi schema. B cần bộ ảnh. C đụng ràng buộc cứng nên để cuối.
2. B trước: ấn tượng nhất nhưng dễ bị chặn ở khâu tạo ảnh.
3. C trước: chỉ hợp nếu món gia truyền là điểm bán chính; rủi ro cao nhất.

Nguồn ảnh: **tự tạo bằng AI, gói sẵn trong app** (đã chọn). Loại: ảnh stock (phải kiểm giấy phép từng ảnh), tải ảnh khi mở app (vi phạm offline).

## Đợt A — UI và logic đã có sẵn

**#1 Chip "Nên dùng sớm"** (Tủ lạnh)
- Chip thứ ba cạnh Ngăn mát và Ngăn đông, có số món: `[ Nên dùng sớm · 3 ]`.
- Lọc bằng `expiringList()` (cùng nguồn với thông báo, để số liệu khớp). Đồ quá hạn ước tính xếp đầu, câu "Kiểm tra lại xem còn dùng được không".
- Không dùng chữ "hết hạn" trong giao diện.
- Cuối danh sách có nút "Nấu gì từ các món này?" chuyển về màn "Hôm nay nấu gì". `recommendRecipes` đã ưu tiên đồ có `daysLeft` 0..2 (lý do `USE_SOON`), nên không sửa thuật toán gợi ý.
- Chip rỗng vẫn hiện, kèm câu ấm ("Tủ đang ổn, chưa có món nào cần dùng sớm").
- Lệch đã biết và giữ nguyên: `recommendRecipes` bỏ đồ quá hạn (`daysLeft < 0`), `expiringList` thì giữ.

**#4 Nấu ngẫu nhiên**
- Bỏ qua `DietRestriction`. Nếu món trùng điều kiện ăn của người dùng thì có một dòng nhỏ báo.
- Ưu tiên món nấu được từ đồ đang có; chỉ khi không còn món nào thì rút từ toàn bộ công thức, kèm câu "Món này cần thêm vài thứ nữa nè", có "Thử món khác" và "Quay lại".
- Bấm lại rút món khác, không lặp món vừa ra.

**#6 Bớt chữ** — làm từng màn: Hôm nay nấu gì, thẻ món, chi tiết công thức, Tủ lạnh.
- Thẻ món: ảnh, tên, thời gian nấu, icon nguyên liệu, một nhãn ngắn (ví dụ "Dùng đồ sắp dùng").
- Bước nấu: một câu ngắn, chữ lớn.
- Không cắt câu đường lui và nhãn "ước tính".

**#7 Bữa trưa**
- Màn "Hôm nay nấu gì" có chọn `[ Bữa trưa ] [ Bữa tối ]`, tự chọn theo giờ (trước 14:00 là Trưa; giờ lấy từ `Clock`/`TimeZone` được tiêm).
- Gợi ý, nấu, ghi lịch sử đều theo bữa đã chọn, khớp ô trưa của thực đơn tuần.
- Sau khi nấu, hỏi từng nguyên liệu "Dùng hết hay còn lại?". Còn lại thì ở lại trong tủ. Không có số lượng/khối lượng.

**#8 Lịch sử nấu**
- Màn "Đã nấu": danh sách món nhóm theo tuần (Tuần này / Tuần trước...), mỗi dòng gồm ảnh, tên, ngày, icon nguyên liệu đã dùng (`CookLog.savedIngredientIds`). Bấm vào mở công thức, có "Nấu lại".
- Không thống kê, không tab "Đã dùng". Lối vào chọn khi làm giao diện.

**#10 Nút thực đơn tuần + thông báo Chủ Nhật**
- Nút to "Lên thực đơn cả tuần" ngay dưới phần chào của "Hôm nay nấu gì", mở màn thực đơn tuần riêng. Bỏ khối thực đơn tuần đang nằm dưới gợi ý hôm nay.
- Chủ Nhật, thông báo trong ngày được thay bằng "Nay là cuối tuần rồi nè, đã sẵn sàng đi chợ cho 1 tuần mới chưa nào!". Vẫn đúng 1 thông báo/ngày.
- Giờ mặc định 8:00 vào Chủ Nhật, đổi được trong Cài đặt. Giữ cơ chế `AlarmManager` không chính xác, lên lịch lại sau reboot và đổi múi giờ.

## Đợt B — dữ liệu và ảnh

**#2 Ảnh bìa món**: thêm trường `image` (tùy chọn) vào `Recipe` và `recipes.json`; ảnh WebP trong assets; thiếu ảnh thì dùng ảnh mặc định.
**#3 Ảnh từng bước**: danh sách ảnh tùy chọn cho mỗi bước, chỉ làm cho bước khó hình dung. Rảnh tay: ảnh lớn phía trên, chữ lớn bên dưới, chạm nửa phải để tiếp, nửa trái để lùi. Không GIF/video. Ngân sách ảnh khoảng 30 MB cho cả bộ ở bản đầu (con số do tôi đặt). Cả bộ ảnh cùng góc chụp và ánh sáng.
**#5 Thông báo "món ngon hôm nay"**: chung `DailyNotice`; **đồ nên dùng sớm luôn thắng**. Ngày tủ không có đồ cần dùng thì gửi: "Hôm nay thử món {tên} nhé? Nấu đơn giản, nguyên liệu dễ kiếm. Bắt đầu nấu nè?". Món là món đầu của `recommendRecipes` nấu được từ đồ đang có; không có thì không gửi. Bấm thông báo mở công thức có "Bắt đầu nấu"; có "Gợi ý món khác"; tắt được trong Cài đặt.

## Đợt C — đổi kiến trúc

**#9 Thêm món thủ công**: món của người dùng lưu SQLDelight, công thức gốc vẫn là JSON bundle, gộp khi đọc. Nhập tên, thời gian, ảnh bìa (tùy chọn), nguyên liệu chọn từ danh mục sẵn có, các bước (mỗi bước có thể kèm ảnh). Nguyên liệu không có trong danh mục thì thêm vào "Khác" và không tự trừ khỏi tủ. Hiện `Recipe` giới hạn 1..8 bước và mỗi `Slot` cần id nguyên liệu; món thủ công phải theo cùng ràng buộc hoặc nới có chủ đích. Cần cập nhật backup (`BackupCodec`) và `DECISIONS.md`.

## Việc để người xây quyết

- Lối vào màn "Đã nấu" và vị trí chính xác của chọn Trưa/Tối.
- Chia nhỏ task để mỗi task ≤ ~300 dòng diff; đợt A tách theo từng mục.
- Cách nới giới hạn 8 bước cho món thủ công, và thể hiện "nguyên liệu Khác" trong tủ.
- Xung đột còn mở: `PLAN.md` đang có Smart Weekend Loop (SW-xx) chồng lên #10 và #7; cần đối chiếu để không làm đôi.

## Các quyết định do tôi tự đặt (cần bạn kiểm)

- Tên chip "Nên dùng sớm"; ngưỡng 14:00 chia Trưa/Tối; ngân sách ảnh 30 MB; bỏ confetti và câu chúc; WebP thay GIF.
