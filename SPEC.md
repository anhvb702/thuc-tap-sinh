# SPEC.md — Hợp Đồng Sản Phẩm & Đặc Tả Kỹ Thuật YourRefri

> **Sứ mệnh:** YourRefri là **"Người trợ lý nhỏ trong căn bếp gia đình"** — đồng hành cùng người nội trợ quản lý thực phẩm, giảm lãng phí, tiết kiệm chi phí và xua tan nỗi lo *"Hôm nay ăn gì?"*.

---

## 1. Bốn Trụ Cột Trải Nghiệm (4 Main Areas)

### 1.1. Trang Chủ (Home Screen) — Nắm bắt trong 5 giây
- **Mục tiêu:** Mở app lên là biết ngay tình trạng tủ lạnh và việc cần làm, không cần cuộn nhiều hay tính toán phức tạp.
- **Thành phần:**
  1. *Lời chào ấm áp:* Đổi theo buổi (`Chào buổi sáng, hôm nay nhà mình thế nào?`, `Chiều nay nấu gì?`).
  2. *Thẻ tổng quan:* Tổng số nguyên liệu hiện có trong nhà.
  3. *Mục ưu tiên dùng sớm:* Danh sách lướt ngang các món cần dùng trong 0–2 ngày tới (badge màu Terracotta / Warm Beige).
  4. *3 Gợi ý bữa tối hôm nay:* 3 món ăn nổi bật được tính toán dựa trên đồ đang có sẵn.
  5. *Thao tác nhanh:* Nút mở Tủ lạnh và nút xem công thức; thẻ tổng quan hiển thị tối đa 3 tên nguyên liệu hiện có theo thứ tự catalog.
  - Chạm thẻ gợi ý mở đúng công thức được chọn ở tab Món ăn; request recipe ID đi theo thẻ và công thức được chọn, cuộn vào tầm nhìn.

### 1.2. Tủ Lạnh (Pantry Screen) — Quản lý trực quan & 1 chạm
- **Tổ chức không gian:** Bộ lọc **Tất cả món (n)**, **Ngăn mát**, **Ngăn đông**; có công tắc **Chỉ xem món đang có**. Mặc định hiển thị toàn catalog để người dùng thêm lại món.
- **Tóm tắt:** Dòng “Trong tủ: n món” đếm món đang có; `Tất cả món (n)` vẫn đếm toàn catalog. Khi bộ lọc rỗng có đường quay về tất cả món.
- **Thẻ thực phẩm trực quan:**
  - Hình ảnh/icon đồ ăn nhận diện nhanh.
  - Tên món + Số ngày ước tính còn lại.
  - Bộ nút tăng/giảm số lượng (`-` / `+`) ngay trên thẻ (ví dụ: dùng 1 phần thịt bò thì bấm trừ, không bắt buộc xóa hết thẻ).
- **Thao tác 1 chạm:**
  - Món đang có ghi “Có trong tủ” và hiện hạn dùng ước tính; món vắng ghi “Chưa có” cùng gợi ý chạm để thêm. Chạm thẻ đổi trạng thái.
  - Nút “Sửa hạn ước tính” mở hộp thoại chọn mốc ngày hoặc “Theo gợi ý”; chỉ hiện với món đang có.
  - Đơn vị chọn riêng theo món bằng nút “Đổi đơn vị · …” trên thẻ, không nằm trong Settings.
- **Thêm thực phẩm:** FAB mở hộp thoại nhập tên cùng tối đa 6 chip gợi ý món chưa có; query không dấu và bộ lọc nhóm có thể dùng cùng lúc. Chip gợi ý ưu tiên món đã từng tương tác nhưng đang hết, theo `markedAt` giảm dần, sau đó là món chưa từng tương tác theo thứ tự catalog. Reducer giữ `markedAt` cũ khi món được đánh dấu có lại, vì vậy thứ tự này không khẳng định thời điểm vừa hết thực tế.
  - Có thể chọn nhiều chip trước khi xác nhận; lựa chọn được giữ khi đổi query/nhóm. Nút nêu số món từ hai lựa chọn trở lên; xác nhận lưu từng món hợp lệ. Gõ đúng tên duy nhất vẫn hỗ trợ luồng thêm một món.
  - Placeholder chỉ dùng tên có trong catalog; chỉ thêm khi xác định được duy nhất một món, kết quả mơ hồ hướng người dùng chọn chip. Người dùng xác nhận riêng để thêm.
  - Hạn gợi ý được chọn mặc định theo `userShelfDays ?: defaultShelfDays` của món; “Theo gợi ý” không ghi đè hạn người dùng đã lưu. Người dùng có thể chọn hạn tùy chỉnh.
  - Hiện UI vẫn chỉ cho chọn nhanh các mốc 2/3/7/14 ngày ngoài hạn gợi ý; đây là giới hạn hiện tại của hộp thoại.

### 1.3. Món Ăn (Recipes Screen) — Gợi ý & Công thức nấu
- **3 Cấp độ gợi ý thông minh:**
  1. *Món ưu tiên số 1:* Tận dụng tối đa các nguyên liệu cần dùng sớm.
  2. *Món ưu tiên số 2:* Tận dụng các nguyên liệu đang có sẵn trong tủ.
  3. *Món ưu tiên số 3:* Món nhanh gọn, chế biến dưới 20 phút.
- **Thẻ món ăn hiển thị:** Nhãn lý do gợi ý, thời gian chế biến, tên món, trạng thái có thể nấu ngay (nếu đủ điều kiện), và danh sách nguyên liệu đã có/còn thiếu.
- **Bundle công thức:** Có 10 recipe trong `recipes.json`; mỗi món có bốn bước và thời gian dương. Bảy công thức bổ sung giúp phủ nguyên liệu nấu ăn trong catalog; một số nguyên liệu phụ nằm ở optional slots, còn trái cây/sữa ăn liền không bắt buộc có công thức.
- **Lọc thời gian nấu:** “Tất cả”, “Tối đa 15 phút”, “Tối đa 30 phút”; gồm món đúng 15 hoặc 30 phút tương ứng, bỏ duration không xác định/không dương và giữ thứ tự gợi ý. Quyết định inclusive do người dùng chốt ngày 2026-11-08; implementation và unit contracts có trong `docs/tests/W5-02B.md`.
- **Thực đơn tuần 7 ngày & Đi chợ 1 chạm (`W6-01`, `W6-02`):**
  - Màn hình Món ăn có thanh chuyển 2 chế độ: `"🍲 Gợi ý hôm nay"` và `"📅 Thực đơn tuần & Đi chợ"`, kèm lối tắt `"Lập thực đơn tuần & đi chợ 📅"` từ Trang chủ.
  - Cá nhân hóa thực đơn cả tuần (`Thứ 2` → `Chủ nhật`) theo định hướng bữa ăn (`DietGoal`: Cân bằng gia đình, Thanh đạm · Nhiều rau, Đậm đà · Đủ đạm, Nấu nhanh ≤ 20 phút) và khẩu vị thành viên (`FamilyPreferenceTag`: Món dễ ăn, Cá & Tôm, Rau, Đậu & Nấm, Thịt heo, bò, gà). Không trùng món ở 2 ngày liên tiếp, ưu tiên nguyên liệu cần dùng sớm ở đầu tuần và cho phép `"Đổi món ↻"` từng ngày.
  - Tự động tổng hợp danh sách nguyên liệu cần mua (`buildWeeklyShoppingList`), trừ các nguyên liệu đã có sẵn trong tủ (`alreadyInPantry`), đếm số bữa sử dụng trong tuần (`mealCount`), và hỗ trợ nút 1 chạm `"Đã mua xong (n món) · Cất vào tủ lạnh 🧊"` tự động cập nhật nguyên liệu vào tủ lạnh mà không cần nhập tay.
- **Trang công thức 4 bước rõ ràng:**
  - Bước 1: Chuẩn bị nguyên liệu
  - Bước 2: Sơ chế
  - Bước 3: Chế biến
  - Bước 4: Hoàn thành
  - *Tính năng hỗ trợ:* Có ô checkmark đánh dấu từng bước đã làm. Hộp thoại sau nấu liệt kê tên nguyên liệu đang có sẽ giảm; “Để sau” dùng TextButton. Khi không có nguyên liệu phù hợp, lời xác nhận/kết quả nói rõ chưa có gì cần cập nhật và không dispatch ID. Action tiêu thụ kiểm tra trạng thái pantry trong reducer tại thời điểm xử lý để tránh bật lại món đã vắng.

### 1.4. Cá Nhân & Thiết Lập (Settings Screen)
- **Tôn trọng người dùng:**
  - Bật/tắt thông báo cố định lúc 17:30, tối đa 1 lần/ngày; người dùng không chỉnh giờ.
  - Sao lưu/khôi phục tệp trên thiết bị; định dạng dữ liệu nội bộ là JSON.
  - Đơn vị được chọn trực tiếp trên từng thẻ món ở Tủ lạnh.

---

## 2. Ràng Buộc Kỹ Thuật Cứng (Hard Constraints)

1. **Offline-First & Quyền riêng tư:**
   - Dữ liệu hoàn toàn trên máy (SQLDelight). Android `allowBackup=false`; `backup_rules.xml` và `data_extraction_rules.xml` loại trừ dữ liệu app khỏi cloud backup và device-to-device transfer. Sao lưu thủ công chỉ qua file JSON do người dùng chọn.
   - Không yêu cầu tạo tài khoản, không bắt đăng nhập, không gửi dữ liệu ra ngoài, không analytics hay crash SDK.
2. **Hạn dùng luôn là "Ước tính":**
   - Không cam kết hay khẳng định an toàn thực phẩm. Đồ qua hạn hiển thị: *"Kiểm tra lại xem còn dùng được không"*.
3. **Văn phong ấm áp (No Shaming):**
   - Tuyệt đối cấm từ ngữ phán xét: "Bạn đã quên", "Lãng phí đồ ăn", "Quá hạn", "Streak", "Điểm trừ".
   - Dùng: "ước tính", "khoảng", "nên dùng trước".
4. **Kiến trúc mã nguồn:**
   - Module `:shared` (Pure Kotlin + SQLDelight) và `:composeApp` (Jetpack Compose UI + MVI ViewModel).
   - Tiêm Clock (`kotlin.time.Clock`) và `TimeZone` trong toàn bộ hệ thống tính toán ngày.
   - Thư viện Design Theme: Áp dụng triệt để `Warm Minimalism` (`DESIGN.md`).
