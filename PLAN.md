# PLAN.md — Kế Hoạch 5 Tuần Phát Triển YourRefri

> **Tầm nhìn:** Phát triển YourRefri thành **"Người trợ lý nhỏ trong căn bếp gia đình"** xoay quanh 4 tab chính: **Trang chủ, Tủ lạnh, Món ăn, Cá nhân**, mang phong cách **Warm Minimalism** và đảm bảo 5 kỳ vọng thiết thực của người nội trợ.

---

## 1. Kiến Trúc 4 Tab Đi Vào Hoạt Động (Bottom Navigation)

```
        ┌────────────────────────────────────────────────────────┐
        │                 YOURREFRI (ComposeApp)                 │
        │        Theme Warm Minimalism (DESIGN.md Tokens)        │
        └──────────────────────────┬─────────────────────────────┘
                                   │
      ┌────────────────┬───────────┴────┬─────────────────┐
      ▼                ▼                 ▼                 ▼
 🏠 TRANG CHỦ     🧊 TỦ LẠNH        🍲 MÓN ĂN         ⚙️ CÁ NHÂN
 (5 giây nắm bắt) (Ngăn mát/đông)   (3 gợi ý thông minh) (Nhắc 17:30)
 - Lời chào ấm áp - Thẻ có icon      - Đồ sắp hỏng lên đầu - 1 lần/ngày
 - Tổng quan tủ   - Nút +/- số lượng - Công thức 4 bước    - Sao lưu
 - Cần dùng gấp   - Chạm 1 lần Có/Hết - Checkmark từng bước- Quyền riêng tư
 - 3 gợi ý tối nay- Nút Thêm FAB     - Cập nhật sau nấu
```

---

## 2. Lộ Trình 5 Tuần Chi Tiết

### 🟢 Tuần 1: Nền tảng Dữ liệu & Quản lý Tủ lạnh (Nền tảng hoàn tất; UX follow-up đang làm)
- **W1-00 đến W1-06 (ĐÃ XONG):** Toolchain, schema SQLDelight in-memory, ShelfTable 25 món, PantryRepository, PantryGridReducer.
- **W1-07a/b/c (ĐÃ XONG BẢN GỐC):**
  - Sửa lỗi ViewModel test, nối SQLite `yourrefri.db` vào `MainActivity.kt`.
- **W1-07d (Nâng cấp Tủ lạnh theo định hướng mới):**
  - Bổ sung vị trí bảo quản: **Ngăn mát** (Fridge) & **Ngăn đông** (Freezer).
  - Hỗ trợ số lượng và nút tăng/giảm `+` / `-` trực tiếp trên thẻ (ví dụ: dùng 1 phần thịt bò thì giảm số lượng, không mặc định xóa hết).
  - FAB mở hộp thoại nhập tên, kèm tối đa 6 chip gợi ý theo catalog; chạm chip điền ô nhập, xác nhận riêng mới thêm món.
- **W1-08 (Lưu số lượng bền vững — ĐÃ XONG):**
  - Lưu override số lượng trong bảng SQLDelight riêng `PantryQuantityEntity`, có migration từ schema v1 sang v2.
  - Mặc định là 1; quantity 1 xóa override. Tăng/giảm số lượng không sửa `have`/`markedAt`; riêng bấm giảm ở 1 chuyển món sang “Hết” bằng hành động trạng thái hiện có, không tạo quantity 0.
  - ViewModel nạp lại số lượng từ repository; nút `+`/`−` có vùng chạm 48dp.

---

### ✅ Tuần 2: Trang Chủ "5 Giây Nắm Bắt", Theme Warm Minimalism & Nhắc Nhở 17:30
- **W2-01 (Theme Warm Minimalism):**
  - Triển khai bộ màu theo `DESIGN.md`: Warm Ivory `#F7F5EF`, Sage Green `#789477`, Soft Green `#DDE8D9`, Warm Beige `#E8D8B9`, Terracotta `#D98C70`, Deep Charcoal `#343B34`.
  - Dựng Bottom Navigation chuẩn 4 tab.
- **W2-02 (Trang Chủ — Mở app hiểu ngay trong 5 giây):**
  - Lời chào theo buổi tự động (Sáng / Chiều / Tối).
  - Widget tổng quan số món đang có và tối đa 3 tên nguyên liệu hiện có; Home không có nút thêm thực phẩm.
  - Thanh cuộn ngang các món cần ưu tiên dùng trong 0–2 ngày tới (nhãn Terracotta/Beige dịu mắt).
  - 3 Thẻ món ăn gợi ý hôm nay (xem nhanh nguyên liệu sẵn có).
- **W2-03 (Thông báo nhắc nhở nhẹ nhàng 17:30):**
  - Giờ nhắc cố định 17:30; chỉ bật/tắt, không chỉnh giờ. Dùng `AlarmManager` không chính xác, tối đa 1 thông báo/ngày.
  - Nội dung ấm áp, không phán xét: *"Tối nay nhà mình có [món A, B] nên dùng trước, bạn xem gợi ý nhé!"*.
  - **Hoàn tất:** toggle bền vững, xin quyền notification theo hành động bật, receiver kiểm tra dữ liệu và chống gửi lặp; đặt lại sau reboot/đổi giờ/múi giờ/cập nhật. Logic có 6 mutation tests. Thiết bị ADB đang kết nối nhưng notification thực tế chưa được thử.

---

### ✅ Tuần 3: Món Ăn (Recipes) — 3 Gợi Ý Thông Minh & Công Thức 4 Bước
- **W3-01 (Thuật toán 3 gợi ý thực tế):**
  - Gợi ý 1: Món tận dụng nhiều đồ sắp hỏng nhất.
  - Gợi ý 2: Món nấu được từ các nguyên liệu đang có sẵn.
  - Gợi ý 3: Món nhanh gọn, dễ làm dưới 20 phút.
- **W3-02 (Màn hình chi tiết công thức 4 bước):**
  - Bước 1: Chuẩn bị nguyên liệu | Bước 2: Sơ chế | Bước 3: Chế biến | Bước 4: Hoàn thành.
  - Có checkbox đánh dấu từng bước giúp người nấu không bị rối.
- **W3-03 (Đã hoàn tất — Hỏi sau khi nấu):** Nấu xong mở xác nhận trước khi cập nhật. Một lựa chọn sẵn cho mỗi slot; action riêng `ConsumeIngredient` kiểm tra `have` tại reducer khi xử lý, quantity >1 giảm mà vẫn Có, quantity 1 chuyển Hết. Sáu mutation W3-03 bị bắt; xem `docs/tests/W3-03.md` và follow-up `docs/tests/P12-02R.md`.
- **W3-01 (Đã hoàn tất):** Kiểm tra gợi ý theo tồn kho và alternative; không đếm hai lựa chọn trong slot như hai nguyên liệu riêng. Xem `docs/tests/W3-01.md`.
- **W3-04 (Hợp đồng lịch sử):** Hàm lọc strict `<15`/`<30` và 6 mutation ban đầu vẫn được giữ để bảo toàn bằng chứng lịch sử; xem `docs/tests/W3-04.md`.
- **W5-02B (Đã triển khai):** UI dùng “Tối đa 15 phút” / “Tối đa 30 phút”, gồm đúng 15/30 phút. Unit contract inclusive và sáu mutation được ghi trong `docs/tests/W5-02B.md`; các test lịch sử strict của W3-04 được giữ nguyên.

---

### ✅ Tuần 4: Trang Cá Nhân, Đơn Vị Đo & Sao Lưu Offline
- **W4-01 (Cá nhân & Cài đặt thông báo):**
  - Bật/tắt thông báo cố định lúc 17:30 theo ràng buộc sản phẩm; không cho chỉnh giờ.
  - Đơn vị đo lường chọn riêng trên từng thẻ ở Tủ lạnh (hộp, quả, g, kg, bó...).
- **W4-02 (Xuất/Nhập dữ liệu sao lưu JSON):**
  - **Đã hoàn tất:** xuất/khôi phục JSON qua Android Storage Access Framework; validate trước khi import và yêu cầu xác nhận trước khi thay dữ liệu. Restore transaction bảo toàn dữ liệu nếu lỗi. Ảnh chỉ giữ URI metadata, không đưa nội dung ảnh vào file. Android `allowBackup=false`; backup rules loại trừ dữ liệu khỏi cloud backup và device transfer. Chi tiết `docs/tests/W4-02.md`.
- **W4-03 (Tối ưu Trợ năng & Màn hình nhỏ):**
  - **Logic/data hoàn tất; runtime còn mở:** font body tối thiểu 14sp, màu chữ kiểm tra tương phản, vùng chạm lựa chọn/nút chính ít nhất 48dp; unit override theo món, migration v2→v3, backup tương thích và test round-trip. Font scale, 360dp, TalkBack và bố cục runtime chưa được xác minh; xem `docs/tests/W4-03.md` và snapshot đầu `HANDOFF.md`.

### UX follow-up — 2026-11-08
- **UX-02/UX-03:** Hoàn tất sáu mutation cho từng contract; kết quả và lệnh trong `docs/tests/UX-02.md`, `docs/tests/UX-03.md`.
- **UX-04:** Source xử lý các pain point P0/P1; sáu mutation helper/presentation đều bị test bắt trong vòng 2. Call-site Compose, thao tác chạm và layout runtime chưa được instrumented; xem giới hạn cùng bằng chứng ở `docs/tests/UX-04.md`.
- **UX-05:** Quick-add có bộ lọc nhóm/query và multi-select; sáu mutation helper bị bắt. Sort theo `markedAt` giảm dần, nhưng timestamp có thể cũ hơn thời điểm món vừa hết; chưa có Compose interaction/runtime test. Xem `docs/tests/UX-05.md`.
- **UX-06:** Bundle mở rộng lên 10 công thức, bốn bước/món, thời gian hợp lệ và ingredient IDs đối chiếu catalog; sáu mutation bị bắt. Xem `docs/tests/UX-06.md`.

---

### 🟡 Tuần 5: Đánh Giá Cùng Persona Khách Hàng & Đóng Gói
- **W5-01 (Đánh giá cùng Customer Persona Chị Mai/Anh Nam):**
  - Đo 7 tiêu chuẩn UX:
    1. Hiểu tủ lạnh trong 5 giây.
    2. Thêm món trong 10-15 giây.
    3. Cập nhật đã dùng trong 1-2 chạm.
    4. Tìm món phù hợp trong 30 giây.
    5. Đọc công thức không bị rối.
    6. Tối đa 1 thông báo/ngày.
    7. 100% tính năng không cần tài khoản.
  - **Hoàn tất source review:** hai lượt đánh giá persona; sửa ngôn ngữ trạng thái, gợi ý không có dữ liệu, xếp hạng món và hành vi thêm món. Persona chưa đánh giá runtime; report tại `docs/tests/W5-01.md`.
- **W5-02:** Launcher icon hoàn tất; `:composeApp:assembleRelease` thành công lúc 2026-11-07 00:40 local, tạo APK unsigned 8,049,873 bytes. Cần signing key do chủ dự án quản lý để ký và cài release.
- **W6-01 & W6-02 (Zero-Input Pantry Loop — Thực đơn tuần & Đi chợ 1 chạm theo góp ý PR #5):**
  - Giải quyết triệt để rào cản "ngại nhập liệu thủ công mỗi lần đi chợ/nấu ăn" bằng vòng lặp ngược: Lập thực đơn tuần (cá nhân hóa theo `DietGoal` & `FamilyPreferenceTag` của các thành viên, chống lặp ngày liền kề, ưu tiên đồ sẵn có đầu tuần) → Tự động tổng hợp danh sách đi chợ tuần (trừ đồ đã có trong tủ, đếm số bữa dùng) → 1 chạm `"Đã mua xong · Cất vào tủ lạnh 🧊"` tự động nạp nguyên liệu vào tủ lạnh. Chi tiết tại `docs/tests/W6-01.md` và `docs/tests/W6-02.md`.

### Bằng chứng và snapshot

Test count, ảnh runtime, trạng thái emulator và giới hạn kiểm chứng thay đổi theo phiên; xem snapshot mới nhất ở đầu `HANDOFF.md`. Số test/build trong từng task là bằng chứng lịch sử tại thời điểm task chạy, không phải xác nhận trạng thái hiện tại. Release APK unsigned cần khóa ký do chủ dự án quản lý.
