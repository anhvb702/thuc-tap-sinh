# Hợp Đồng Sản Phẩm Vòng 4 (UX-05 &UX-06): Xóa Bỏ "Nhập Chay" & Phủ Kín Kho Công Thức Gia Đình Việt — 2026-11-08

> **Bối cảnh từ Khách hàng (Chị Mai, Anh Nam, Cô Hạnh, Chị Vy/Anh Quân):**
> Sau 3 vòng tinh chỉnh, các luồng cơ bản đã đạt 100%, nhưng còn **2 rào cản thực tế lớn nhất** ngăn ứng dụng giải quyết trọn vẹn 100% kỳ vọng của gia đình:
> 1. **Nỗi bất tiện khi "nhập chay" trong hộp thoại `+` (`AddFoodDialog`):** Đi chợ về mua 4–5 món mà phải mở `+` gõ bàn phím hoặc mỗi lần mở `+` chỉ chọn được đúng 1 món rồi hộp thoại đóng lại; các món gia đình hay ăn vừa hết gần đây không được ưu tiên lên đầu; không có bộ lọc theo nhóm (*Thịt, Cá, Rau lá, Rau củ, Trái cây, Sữa trứng, Khác*) ngay trong hộp thoại như `SPEC.md` mong đợi.
> 2. **Kho công thức (`recipes.json`) mới có 3 món cho 25 nguyên liệu:** Có tới 19/25 nguyên liệu trong `shelf_table.json` (*Thịt bò, Thịt gà, Cá hồi, Tôm, Cải ngọt, Xà lách, Rau ngót, Cà rốt, Khoai tây, Bí xanh, Đậu phụ, Nấm tươi...*) chưa gắn với bất kỳ công thức nào. Khi các món này sắp hết hạn, mục *"Gợi ý bữa tối hôm nay"* vẫn chỉ xoay quanh 3 món cũ.

---

## HẠNG MỤC 1: `UX-05` — Chế Độ "Đi Chợ Về" Không Cần Gõ Phím (Zero-Typing Multi-Select & Smart Recent Picker)

### 1. Mục tiêu trải nghiệm (Customer Expectation)
- Mở hộp thoại `+` (`Thêm thực phẩm vào tủ 🥦`) ra là **không cần chạm vào bàn phím** vẫn thấy ngay:
  1. **Ưu tiên các món gia đình từng dùng nhưng hiện đã hết** (`pantry[id]?.have == false`, sắp xếp theo `markedAt` mới nhất trước) đứng đầu danh sách gợi ý, tiếp theo mới đến các món chưa từng thêm theo thứ tự catalog.
  2. **Các chip lọc nhóm thực phẩm 1 chạm** ngay trong hộp thoại: `Tất cả`, `Thịt`, `Cá & Tôm`, `Rau lá`, `Rau củ`, `Trái cây`, `Sữa & Trứng`, `Đậu & Nấm` (ánh xạ từ trường `group` đã có sẵn trong `shelf_table.json`: `"thit"`, `"ca"`, `"rau-la"`, `"rau-cu"`, `"trai-cay"`, `"sua-trung"`, `"khac"`).
  3. **Chọn nhiều món cùng lúc (Multi-select 1 chạm):** Khi đi chợ về mua *Thịt bò, Cà chua, Đậu phụ, Rau ngót*, Chị Mai chỉ cần chạm 4 chip (hoặc gõ tìm + chạm chip) để đánh dấu chọn cả 4 món trong một lượt mở hộp thoại, rồi bấm **`"Thêm 4 món vào tủ"`** (nếu chọn 1 món thì vẫn cho phép chọn hạn dùng ước tính như hiện tại; nếu chọn nhiều món thì mỗi món tự động lấy hạn gợi ý `userShelfDays ?: defaultShelfDays`, hoặc áp dụng hạn tùy chỉnh nếu người dùng chọn).

### 2. Hợp đồng Kỹ thuật (`UX-05`)
- **Input:**
  - `state: PantryGridState` (chứa `ingredients`, `pantry` với `have` và `markedAt`).
  - `selectedGroup: String?` (`null` = Tất cả, hoặc một trong 7 mã `group` của `shelf_table.json`).
  - `query: String` (từ khóa tìm kiếm không bắt buộc).
  - `selectedIds: Set<String>` (tập ID các nguyên liệu chưa có trong tủ được người dùng chạm chọn trong dialog).
- **Expected Output (Pure Presentation / Helper Functions trong `QuickAddSuggestions.kt`):**
  1. **Sắp xếp thông minh (`quickAddSuggestions` / `quickAddCandidates`):**
     - Chỉ lấy các món chưa có trong tủ (`state.pantry[it.id]?.have != true`).
     - Lọc theo `selectedGroup` (nếu khác `null`) và `query` (nếu không rỗng, không phân biệt dấu).
     - **Ưu tiên món vừa hết gần đây:** Các món có dòng trong `state.pantry` với `have == false` được xếp lên trước (ưu tiên `markedAt` giảm dần; nếu cùng `markedAt` thì giữ thứ tự catalog), sau đó đến các món chưa từng có trong `state.pantry` theo đúng thứ tự catalog.
  2. **Bật/tắt chọn nhiều món (`toggleQuickAddSelection(selectedIds, ingredientId)`):**
     - Chạm vào chip món ăn trong dialog sẽ thêm/bớt `ingredientId` khỏi `selectedIds` (thay vì chỉ ghi đè 1 chuỗi văn bản duy nhất).
     - Nếu người dùng không chạm chip mà chỉ gõ đúng tên 1 món duy nhất vào ô tìm kiếm (`quickAddMatch`), vẫn hỗ trợ thêm món đó như cũ (tương thích ngược 100% với luồng 1 món).
  3. **Nhãn nút xác nhận & Hành động lưu (`quickAddConfirmLabel(selectedCount)` & `quickAddIdsToSave(...)`):**
     - Khi chọn `n > 1` món: Nhãn nút xác nhận hiển thị `"Thêm $n món vào tủ"`, gửi tuần tự `PantryGridAction.AddIngredient(id, newFoodDays)` cho từng ID đã chọn theo thứ tự hiển thị.
     - Khi chọn `0` hoặc `1` món: Giữ nhãn `"Thêm vào tủ"`.
- **6 Mutations cần bị bắt bởi Unit Test (`QuickAddSuggestionsTest.kt`):**
  1. Bỏ ưu tiên món từng dùng (`have == false`) lên trước món chưa từng tương tác.
  2. Sắp xếp `markedAt` tăng dần thay vì giảm dần (món mới hết nhất phải lên đầu).
  3. Bỏ bộ lọc theo `group` (`"thit"`, `"rau-la"`...).
  4. `toggleQuickAddSelection` chỉ giữ 1 ID thay vì cho phép chọn nhiều ID (`Set<String>`).
  5. Khi chọn nhiều món (`selectedIds.size > 1`), chỉ trả về món đầu tiên khi lưu thay vì toàn bộ danh sách đã chọn.
  6. Nhãn nút xác nhận không hiển thị số lượng món khi chọn từ 2 món trở lên.

---

## HẠNG MỤC 2: `UX-06` — Mở Rộng Kho Công Thức Gia Đình Việt Phủ Kín 25 Nguyên Liệu (`recipes.json`)

### 1. Mục tiêu trải nghiệm (Customer Expectation)
- Khi trong tủ lạnh có bất kỳ nguyên liệu nấu ăn nào trong danh mục (*Thịt bò, Thịt gà, Cá hồi, Tôm, Đậu phụ, Nấm tươi, Cải ngọt, Rau ngót, Xà lách, Cà rốt, Khoai tây, Bí xanh...*), ứng dụng đều có món cơm nhà Việt Nam thiết thực (4 bước ngắn gọn, thời gian trải đều từ 10 phút đến 35 phút) để gợi ý ngay trong mục **"Gợi ý bữa tối hôm nay"** và bộ lọc thời gian (`Tối đa 15 phút` / `Tối đa 30 phút`).

### 2. Hợp đồng Kỹ thuật (`UX-06`)
- **Input:**
  - `composeApp/src/main/assets/recipes.json` và `composeApp/src/main/assets/shelf_table.json`.
- **Expected Output:**
  - Giữ nguyên 3 công thức hiện có (`canh-chua-ca-basa`, `thit-kho-trung`, `rau-muong-xao-hanh` — không làm hỏng bất kỳ test hiện hữu nào như `W5-02A`).
  - Bổ sung thêm **7 công thức cơm nhà Việt Nam** (nâng tổng số lên **10 công thức**) sử dụng các nguyên liệu còn lại trong `shelf_table.json`:
    1. `thit-bo-xao-hanh-tay` — **Thịt bò xào hành tây** (`15 phút`, required: `thit-bo`, `hanh-tay`, optional: `ca-rot`).
    2. `canh-rau-ngot-thit-bam` — **Canh rau ngót nấu thịt** (`15 phút`, required: `rau-ngot`, `thit-heo-xay` hoặc `thit-heo`).
    3. `dau-phu-sot-ca-chua` — **Đậu phụ sốt cà chua** (`15 phút`, required: `dau-phu`, `ca-chua`, `hanh-la`).
    4. `tom-rim-thit` — **Tôm rim mặn ngọt** (`20 phút`, required: `tom`, `hanh-la`, optional: `thit-heo`).
    5. `ga-kho-khoai-tay` — **Thịt gà kho khoai tây cà rốt** (`30 phút`, required: `thit-ga`, `khoai-tay` hoặc `ca-rot`).
    6. `canh-bi-xanh-nau-tom` — **Canh bí xanh nấu tôm nấm** (`20 phút`, required: `bi-xanh`, `tom` hoặc `thit-heo-xay`, optional: `nam`, `hanh-la`).
    7. `ca-hoi-ap-chao-xa-lach` — **Cá hồi áp chảo trộn xà lách** (`15 phút`, required: `ca-hoi`, `xa-lach` hoặc `cai-ngot`, optional: `ca-chua`).
  - Mỗi công thức có đúng **4 bước ngắn gọn**, dễ làm, chuẩn văn phong gia đình Việt.
  - Mọi nguyên liệu nấu mặn/canh/xào trong `shelf_table.json` (20/20 nguyên liệu trừ 5 món trái cây/sữa ăn liền là `chuoi`, `tao`, `cam`, `sua-tuoi`, `sua-chua`) đều xuất hiện trong ít nhất 1 công thức của `recipes.json`.
- **6 Mutations cần bị bắt bởi Unit Test (`ShippedRecipeBundleTest.kt`):**
  1. Tổng số công thức nhỏ hơn 10.
  2. Bất kỳ công thức nào không có đúng 4 bước (`steps.size != 4`) hoặc bước rỗng.
  3. Bất kỳ công thức nào có `minutes == null` hoặc `minutes <= 0`.
  4. Không có công thức nào thuộc nhóm nhanh `minutes <= 15` hoặc nhóm `16..30`.
  5. Bỏ sót một trong các nguyên liệu nấu ăn chính (`thit-bo`, `thit-ga`, `ca-hoi`, `tom`, `dau-phu`, `rau-ngot`, `bi-xanh`, `khoai-tay`, `ca-rot`, `xa-lach`, `cai-ngot`, `nam`) khỏi toàn bộ `recipes.json`.
  6. Trỏ tới một ingredient ID không tồn tại trong `shelf_table.json`.
