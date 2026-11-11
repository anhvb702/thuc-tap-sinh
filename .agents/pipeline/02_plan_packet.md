# 02_PLAN_PACKET: Đặc Tả Hợp Đồng Triển Khai Vòng Lặp Thực Đơn Tuần & Đi Chợ 1 Chạm (`W6-01` → `W6-03`)

## 1. Chốt 6 Quyết Định Kiến Trúc & Sản Phẩm (Approved Decisions D1–D6)
1. **D1 (Vị trí UI):** Giữ nguyên Trang chủ hiển thị 3 món gợi ý hôm nay (thêm 1 lối tắt nhẹ `"Xem thực đơn tuần 📅"` mở thẳng chế độ Thực đơn tuần bên tab Món ăn). Tại tab `Món ăn 🍲`, dùng thanh chuyển 2 chế độ: `"🍲 Gợi ý hôm nay"` và `"📅 Thực đơn tuần & Đi chợ"`.
2. **D2 (Ổn định Thực đơn khi cất đồ vào tủ):** Trong phiên/màn hình lập thực đơn, danh sách 7 món của tuần (`weeklyPlanRecipeIds: List<String>`) được giữ ổn định trong state khi người dùng bấm `"Đã mua xong · Cất vào tủ lạnh 🧊"` (việc thêm nguyên liệu vào tủ chỉ làm giảm danh sách cần mua đi chợ, không làm nhảy/đổi món của các ngày Thứ 2 -> Chủ Nhật trừ khi người dùng chủ động đổi `DietGoal`, `FamilyPreferenceTag` hoặc bấm `"Đổi món ↻"`). Không cần tạo bảng SQLDelight mới để tránh phức tạp hóa migration v3.
3. **D3 (Kho 10 công thức & Chống lặp):** Suy diễn `DietGoal` và `FamilyPreferenceTag` thuần túy từ dữ liệu `Recipe` hiện có (thời gian `minutes` và danh sách `required`/`optional` ingredient IDs) — không sửa format JSON của `recipes.json` để bảo toàn 100% các bài test `ShippedRecipeBundleTest` hiện hữu. Cho phép xoay vòng trong 10 món sao cho **không bao giờ trùng món ở 2 ngày liên tiếp** (`day[i] != day[i-1]`).
4. **D4 (Văn phong trung tính, không khẳng định y tế/sức khỏe):**
   - Nhãn `DietGoal` chuẩn:
     - `BALANCED` -> `"Cân bằng gia đình"`
     - `LIGHT` -> `"Thanh đạm · Nhiều rau"`
     - `RICH_PROTEIN` -> `"Đậm đà · Đủ đạm"`
     - `QUICK` -> `"Nấu nhanh ≤ 20 phút"`
   - Nhãn `FamilyPreferenceTag` chuẩn:
     - `KID_FAVORITE` -> `"Món dễ ăn (trứng, gà, khoai)"`
     - `SEAFOOD` -> `"Cá & Tôm"`
     - `VEGGIE_TOFU` -> `"Rau, Đậu & Nấm"`
     - `MEAT` -> `"Thịt heo, bò, gà"`
   - Tuyệt đối không dùng từ "giảm cân", "tăng cơ", "lãng phí", "hết hạn".
5. **D5 (Quy tắc chọn nguyên liệu đi chợ từ `Slot.anyOf` & Cất vào tủ):**
   - Với mỗi `Slot` trong `recipe.required` của 7 ngày:
     - Nếu trong `slot.anyOf` đã có ít nhất 1 nguyên liệu `have == true` trong `pantry`: slot đó được tính là **đã có sẵn trong tủ** (lấy nguyên liệu `have == true` đầu tiên làm đại diện).
     - Nếu chưa có nguyên liệu nào `have == true`: chọn `slot.anyOf.first()` làm nguyên liệu cần mua đi chợ.
   - Khi bấm `"Đã mua xong · Cất vào tủ lạnh 🧊"`, chỉ gửi `PantryGridAction.AddIngredient(id, null)` cho các nguyên liệu được tích chọn mà hiện chưa có trong tủ (`have != true`), không ghi đè `markedAt` của món đã có.
6. **D6 (Đếm số bữa sử dụng):** Mỗi mục trong danh sách đi chợ hiển thị số bữa sử dụng trong tuần (`mealCount`, ví dụ: `"Dùng cho 2 bữa trong tuần"`). Khi cất vào tủ giữ nguyên quy ước số lượng mặc định `1` của `W1-08`.

---

## 2. Phân Chia 2 Task Thực Thi Tự Động

### Task A (`W6-LOGIC` — Định tuyến cho `codex-executor` trên `:shared` & `:composeApp` test)
- **File tạo mới:**
  1. `shared/src/commonMain/kotlin/com/loitu/yourrefri/feature/recipe/logic/WeeklyMealPlanner.kt`
  2. `shared/src/commonTest/kotlin/com/loitu/yourrefri/feature/recipe/logic/WeeklyMealPlannerTest.kt`
  3. `docs/tests/W6-01.md`
- **Chi tiết hàm cần có trong `WeeklyMealPlanner.kt`:**
  - `enum class DietGoal(val labelVi: String)` (`BALANCED`, `LIGHT`, `RICH_PROTEIN`, `QUICK`)
  - `enum class FamilyPreferenceTag(val labelVi: String)` (`KID_FAVORITE`, `SEAFOOD`, `VEGGIE_TOFU`, `MEAT`)
  - `data class PlannedDayMeal(val dayIndex: Int, val dayLabelVi: String, val recipe: Recipe, val matchReasonVi: String)` (7 ngày: `Thứ 2`, `Thứ 3`, `Thứ 4`, `Thứ 5`, `Thứ 6`, `Thứ 7`, `Chủ nhật`)
  - `data class ShoppingListItem(val ingredient: Ingredient, val mealCount: Int, val alreadyInPantry: Boolean)`
  - `fun buildWeeklyMealPlan(recipes: List<Recipe>, state: PantryGridState, goal: DietGoal = DietGoal.BALANCED, preferences: Set<FamilyPreferenceTag> = emptySet(), swappedOffsets: Map<Int, Int> = emptyMap()): List<PlannedDayMeal>`:
    - Chấm điểm từng `Recipe` dựa trên:
      1. Ở ngày đầu tuần (`dayIndex in 0..1`): cộng điểm ưu tiên lớn cho món có nguyên liệu `USE_SOON` (`daysLeft in 0..2`) hoặc `canCook == true` để giải quyết đồ có sẵn trong tủ trước.
      2. Điểm khớp `DietGoal`:
         - `QUICK`: ưu tiên `minutes != null && minutes <= 20`.
         - `LIGHT`: ưu tiên món có rau/đậu/nấm/cá/bí (`rau-muong`, `rau-ngot`, `cai-ngot`, `xa-lach`, `bi-xanh`, `dau-phu`, `nam`, `ca-basa`, `ca-hoi`).
         - `RICH_PROTEIN`: ưu tiên món có thịt/cá/tôm/trứng (`thit-heo`, `thit-heo-xay`, `thit-bo`, `thit-ga`, `ca-basa`, `ca-hoi`, `tom`, `trung-ga`).
         - `BALANCED`: điểm cơ sở cân bằng mọi món.
      3. Điểm khớp `preferences`: mỗi tag trong `preferences` khớp với nguyên liệu của món được cộng điểm thưởng.
    - Chọn lần lượt 7 ngày (`0..6`): không chọn trùng `recipe.id` với ngày liền trước (`dayIndex - 1`), ưu tiên các món chưa xuất hiện trong tuần trước khi lặp lại, và áp dụng `swappedOffsets[dayIndex]` (mặc định `0`; mỗi lần người dùng bấm `"Đổi món ↻"` ở `dayIndex` thì tăng offset để xoay sang ứng viên tiếp theo không trùng ngày kề!).
  - `fun buildWeeklyShoppingList(plan: List<PlannedDayMeal>, state: PantryGridState): List<ShoppingListItem>`:
    - Duyệt qua 7 `PlannedDayMeal`, với mỗi `recipe.required` slot: xác định nguyên liệu đại diện theo quy tắc D5, đếm tổng số bữa (`mealCount`) trong tuần cần nguyên liệu đó, đánh dấu `alreadyInPantry = (state.pantry[id]?.have == true)`.
    - Sắp xếp các món cần mua (`alreadyInPantry == false`) lên trước theo thứ tự catalog `state.ingredients`, sau đó đến các món đã có trong tủ.
  - `fun shoppingIdsToStock(items: List<ShoppingListItem>, uncheckedIds: Set<String> = emptySet()): List<String>`:
    - Trả về danh sách `ingredient.id` của các mục có `alreadyInPantry == false` và `ingredient.id !in uncheckedIds` để gửi `PantryGridAction.AddIngredient(id, null)`.

### Task B (`W6-UI` — Định tuyến cho `claude-haiku` / `codex-executor` trên `:composeApp`)
- **File cập nhật:**
  1. `composeApp/src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/RecipeScreen.kt`:
     - Thêm thanh chọn chế độ ở đầu màn hình: `"🍲 Gợi ý hôm nay"` và `"📅 Thực đơn tuần & Đi chợ"`.
     - Khi ở chế độ `"📅 Thực đơn tuần & Đi chợ"`:
       - Hiển thị FilterChip chọn `DietGoal` và `FamilyPreferenceTag` (vùng chạm `>= 48.dp`).
       - Hiển thị 7 thẻ ngày (`Thứ 2` -> `Chủ nhật`), mỗi thẻ có tên món, thời gian nấu, lý do gợi ý (`matchReasonVi`), nút `"Đổi món ↻"` (tăng offset của ngày đó) và nhấn vào xem được 4 bước nấu.
       - Hiển thị khu vực **"🛒 Danh sách đi chợ tuần này"**:
         - Tóm tắt số món cần mua vs số món đã có sẵn trong tủ.
         - Danh sách các món cần mua có `Checkbox` (mặc định tích chọn, hiển thị `"Dùng cho X bữa"`).
         - Nút hành động chính ấm áp: **`"Đã mua xong (n món) · Cất vào tủ lạnh 🧊"`** -> gọi callback thêm hàng loạt nguyên liệu vào tủ lạnh (`PantryGridAction.AddIngredient(id, null)`) và hiện thông báo xác nhận!
  2. `composeApp/src/main/kotlin/com/loitu/yourrefri/feature/home/ui/HomeScreen.kt` & `MainScaffold.kt`:
     - Thêm nút/lối tắt nhẹ nhàng trên `HomeScreen` (`"Lập thực đơn tuần & đi chợ 📅"`) mở sang tab Món ăn ở chế độ Thực đơn tuần.
