# 01_IDEA_BRIEF: Đảo Ngược Luồng Trải Nghiệm — Từ "Nhập Tủ Lạnh Thủ Công" sang "Thực Đơn Tuần & Đi Chợ 1 Chạm Tự Động Đổ Vào Tủ" (Zero-Input Pantry Loop)

## 1. Pain Point Thực Tế Từ Người Dùng & Mentor (Vì sao người dùng bỏ cuộc?)
- **Vấn đề chí mạng của các app quản lý tủ lạnh truyền thống:** Bắt người dùng nhập và cập nhật nguyên liệu thủ công quá nhiều. Mỗi lần đi chợ về hoặc nấu ăn xong lại phải mở app tìm từng món để cộng/trừ -> Việc dùng app trở thành một "công việc làm thêm" mệt mỏi thay vì giúp cuộc sống nhẹ nhàng hơn.
- **Nỗi đau thực sự hàng ngày trong gia đình:**
  1. *"Hôm nay ăn gì, ngày mai ăn gì, cả tuần ăn gì?"* — câu hỏi gây đau đầu mỗi ngày.
  2. **Khẩu vị các thành viên khác nhau & mục tiêu sức khỏe khác nhau:** Bố thích món đậm đà/nhậu nhẹ, mẹ muốn ăn thanh đạm giữ dáng/giảm cân, con nhỏ cần món mềm dễ ăn giàu đạm, ông bà cần món thanh mát nhanh gọn.
  3. **Đi chợ thiếu trước hụt sau:** Không có danh sách tổng hợp nguyên liệu từ thực đơn tuần đối chiếu với đồ còn sẵn trong tủ lạnh.

---

## 2. Giải Pháp Cốt Lõi: Vòng Lặp Tự Động Không Cần Nhập Liệu (The Zero-Input Loop)

Thay vì bắt người dùng: `Nhập tủ lạnh bằng tay -> Mới có gợi ý món ăn`, chúng ta **đảo ngược luồng (Invert the Flow)** thành vòng lặp tự vận hành bằng 1 chạm:

```
[1. Chọn Sở thích Gia đình & Mục tiêu (1 chạm)]
               │
               ▼
[2. Tự động sinh Thực Đơn Cả Tuần (7 ngày cân đối khẩu vị & dinh dưỡng)]
               │
               ▼
[3. Tự động trừ đồ ĐÃ CÓ trong tủ -> Sinh "Danh sách Đi Chợ Tuần"]
               │
               ▼
[4. Đi chợ về bấm 1 nút: "Đã mua xong · Cất vào tủ 🧊"]
    ──► TỰ ĐỘNG CẬP NHẬT TẤT CẢ NGUYÊN LIỆU VÀO TỦ LẠNH (0 lần nhập tay!)
               │
               ▼
[5. Đến bữa tối bấm 1 nút: "Đã nấu xong món hôm nay 🎉"]
    ──► TỰ ĐỘNG TRỪ NGUYÊN LIỆU TRONG TỦ LẠNH
```

---

## 3. Đặc Tả Tính Năng Cần Triển Khai (`W6 / MP-01..03`)

### 3.1. Hồ sơ Khẩu vị Gia đình & Mục tiêu Dinh dưỡng (Family Preferences & Goals)
- **Không bắt điền form phức tạp:** Cung cấp các chip chọn nhanh 1 chạm ngay trên phần Lập Thực Đơn:
  - **Mục tiêu tuần này (`DietGoal`):**
    1. `BALANCED` — **Cân bằng gia đình** (Đủ mặn, canh, xào hài hòa cho cả nhà).
    2. `LIGHT_FIT` — **Thanh đạm / Giữ dáng** (Ưu tiên rau xanh, cá, đậu phụ, nấm, bí xanh; hạn chế kho đậm/nhiều mỡ).
    3. `HIGH_PROTEIN` — **Giàu đạm / Tăng cơ & Cho bé** (Ưu tiên thịt bò, cá hồi, trứng, tôm, thịt gà, thịt heo).
    4. `QUICK_EASY` — **Bận rộn / Nấu nhanh ≤ 20 phút** (Ưu tiên các món 12–20 phút cho tuần bận việc).
  - **Sở thích thành viên (`FamilyPreferenceTag` - chọn nhiều):**
    - `KID_FRIENDLY` (Món bé thích: trứng, gà, khoai tây, cà rốt, cá hồi, tôm).
    - `SEAFOOD_LOVER` (Thích cá/tôm: cá basa, cá hồi, tôm).
    - `VEGGIE_LOVER` (Nhiều rau/đậu/nấm: rau muống, rau ngót, bí xanh, đậu phụ, nấm).
    - `MEAT_LOVER` (Thích thịt: thịt heo, thịt bò, thịt gà).

### 3.2. Thuật toán Tự động Lập Thực Đơn 7 Ngày Cân Đối (`WeeklyMealPlanner` trong `:shared` hoặc `:composeApp`)
- **Input:**
  - Danh sách 10 công thức (`recipes.json`).
  - Trạng thái tủ lạnh hiện tại (`PantryGridState`: món nào đang `have=true`, món nào sắp hết hạn `daysLeft in 0..2`).
  - `DietGoal` và tập `Set<FamilyPreferenceTag>`.
- **Logic lập thực đơn 7 ngày (Thứ 2 -> Chủ Nhật):**
  1. **Ngày đầu tuần (Thứ 2, Thứ 3):** Ưu tiên xếp các món giải quyết **nguyên liệu đang có sẵn trong tủ** (đặc biệt nguyên liệu sắp hết hạn `USE_SOON`) để không lãng phí đồ cũ trong tủ!
  2. **Các ngày tiếp theo (Thứ 4 -> Chủ Nhật):** Chấm điểm và xoay vòng các công thức khớp với `DietGoal` và `FamilyPreferenceTag` của các thành viên sao cho **cả tuần đa dạng, không lặp lại 1 món ở 2 ngày liên tiếp**, cân bằng giữa các thành viên (ví dụ ngày có món bé thích, ngày có món thanh đạm cho mẹ, ngày có món đậm đà cho bố).
  3. **Đổi món 1 chạm (Swap Day Meal):** Nếu vợ/chồng không thích món của Thứ 5, bấm nút `"Đổi món ↻"` ngay trên dòng Thứ 5 sẽ tự động xoay sang món phù hợp tiếp theo và **tự động tính lại Danh sách đi chợ ngay lập tức**.

### 3.3. Tự động Tổng hợp Danh sách Đi Chợ & Cầu nối "1 Chạm Đổ Vào Tủ Lạnh" (`SmartShoppingList`)
- Từ thực đơn 7 ngày đã lập, hệ thống gom tất cả nguyên liệu bắt buộc (`required` slots):
  - Nếu một `Slot` đã có nguyên liệu `have == true` trong tủ lạnh -> Đánh dấu vào nhóm **`"Đã có sẵn trong tủ"`** (không bắt mua lại, hoặc chỉ nhắc kiểm tra số lượng nếu dùng cho nhiều bữa).
  - Nếu một `Slot` chưa có nguyên liệu nào trong tủ -> Đưa vào **`"Danh sách cần mua đi chợ"`** (gom nhóm theo quầy: *Thịt, Cá & Tôm, Rau củ...* và ghi rõ dùng cho mấy món trong tuần, ví dụ: *"Cà chua · dùng cho 2 món"*).
- **Nút "Phép thuật" giải quyết triệt để nỗi đau nhập liệu:**
  - Người dùng đi chợ có thể tích chọn các món đã mua (mặc định chọn sẵn toàn bộ món cần mua).
  - Bấm 1 nút duy nhất: **`"Đã mua xong (n món) · Cất vào tủ lạnh 🧊"`**.
  - Ứng dụng tự động gửi `PantryGridAction.AddIngredient(id, null)` cho toàn bộ các món vừa mua -> **Tủ lạnh tự động đầy đủ nguyên liệu với hạn dùng ước tính chuẩn mà người dùng KHÔNG PHẢI NHẬP TAY DÙ CHỈ 1 MÓN!**

---

## 4. Yêu Cầu Thiết Kế Giao Diện (Warm Minimalism & 4 Tab)
- Giữ nguyên cấu trúc 4 Tab (`Trang chủ`, `Tủ lạnh`, `Món ăn`, `Cá nhân`).
- Tại tab **`Món ăn 🍲` (`RecipeScreen.kt`)**:
  - Thêm thanh chuyển đổi 2 chế độ ở đầu màn hình: **`"🍲 Gợi ý hôm nay"`** và **`"📅 Thực đơn tuần & Đi chợ"`**.
  - Trong chế độ **`"📅 Thực đơn tuần & Đi chợ"`**:
    1. Khu vực chọn nhanh **Mục tiêu tuần** & **Khẩu vị thành viên** (các FilterChip cao >= 48dp).
    2. Danh sách **Thực đơn 7 ngày (Thứ 2 -> Chủ Nhật)**: mỗi ngày hiện tên món, thời gian nấu, nhãn phù hợp thành viên/nguyên liệu sẵn có, nút `"Đổi món ↻"` và nút `"Đã nấu xong"` (nếu là món hôm nay).
    3. Khu vực **Danh sách Đi Chợ Tự Động**: liệt kê các nguyên liệu còn thiếu cho cả tuần (có checkbox bật/tắt từng món) và nút chính nổi bật **`"Đã mua xong · Cất vào tủ lạnh 🧊"`** để đổ thẳng vào Tủ lạnh bằng 1 chạm.
- Tại tab **`Trang chủ 🏠` (`HomeScreen.kt`)**:
  - Có lối tắt hoặc hiển thị liên kết nhẹ nhàng sang Thực đơn tuần để người nội trợ nắm bắt ngay.
