# Báo Cáo Thẩm Định Thực Tế & Khắt Khe (Unbiased Critical Audit) — 8 Persona Người Dùng
> **Ngày thực hiện:** 2026-11-09
> **Phương pháp:** Kiểm toán độc lập trực tiếp trên mã nguồn (`composeApp/` & `shared/`) kết hợp luồng chạy thực tế bởi 2 Hội đồng Persona độc lập (`strict-persona-panel-a` & `strict-persona-panel-b`).
> **Nguyên tắc:** Không tô hồng, không thiên vị sản phẩm, chấm điểm khắt khe theo tiêu chuẩn giữ chân người dùng thật (Day-3 & Week-2 Retention), chỉ rõ từng dòng code gây đứt gãy trải nghiệm.

---

## 1. Bảng Điểm Tổng Hợp Của 8 Người Dùng Thử Nghiệm (Trung bình: **3.4 / 10**)

| # | Persona & Hoàn cảnh thực tế | Điểm (/10) | Lý do cốt lõi khiến người dùng gỡ app sau 3–7 ngày |
| :-: | :--- | :---: | :--- |
| 1 | **Chị Lan (39t)** — Mẹ kỹ tính, đi chợ truyền thống 2–3 ngày/lần, nấu mâm cơm 2–3 món | **4.5 / 10** | Thực đơn tuần chỉ xếp **1 món đơn lẻ/ngày** (có ngày cả nhà chỉ ăn 1 đĩa rau muống xào); danh sách đi chợ phớt lờ số lượng (`quantity`), cất vào tủ không cộng theo `mealCount`. |
| 2 | **Anh Hoàng (33t)** — Kỹ sư phần mềm & người chồng nấu ăn, soi kỹ state & DB | **3.5 / 10** | Toàn bộ thực đơn tuần, bộ lọc khẩu vị, lần đổi món và dấu tích đi chợ đều dùng `remember` cục bộ — **chuyển sang tab Tủ lạnh xem đồ rồi quay lại là mất sạch 100%**. |
| 3 | **Bạn Minh (24t)** — Nhân viên văn phòng ở trọ một mình, hay mua đồ linh tinh ngoài siêu thị | **4.0 / 10** | Hộp thoại `+` (Thêm thực phẩm) **chặn cứng mọi món ngoài 25 nguyên liệu mặc định**; không cho phép bỏ qua ngày ăn ngoài trong thực đơn 7 ngày. |
| 4 | **Chị Thảo (36t)** — Mẹ có con dị ứng tôm/hải sản & chồng ăn kiêng tinh bột | **2.0 / 10** | Không có bộ lọc **loại trừ (Exclusion)**; vì kho chỉ có 10 món mà ép xếp đủ 7 ngày nên vẫn ép ăn Tôm/Cá. `slot.anyOf.first()` tự ý nhét `tom` vào giỏ đi chợ thay vì `thit-heo-xay`. |
| 5 | **Bác Tâm (66t)** — Bà nội mắt lão thị, không rành công nghệ, sợ bấm nhầm | **4.5 / 10** | Tên món trong Thực đơn tuần không có dấu hiệu nhận biết bấm được (không mũi tên, vùng chạm < 48dp, sát nút `"Đổi món ↻"`). Thẻ tủ lạnh xếp 5 vùng chạm sát nhau dễ bấm nhầm xóa mất đồ. |
| 6 | **Chị Hương (42t)** — Nội trợ nấu cho đại gia đình 6 người 3 thế hệ | **3.5 / 10** | Kho công thức chỉ có **10 món** (`recipes.json`), từ Thứ 4–Chủ nhật điểm số tĩnh nên tuần nào cũng lặp lại đúng một thứ tự. Sai ID `"khoai"` thay vì `"khoai-tay"` trong `KID_FAVORITE`. |
| 7 | **Anh Tuấn (30t)** — Tập gym / Meal-prep cuối tuần, cần định lượng & trừ kho chuẩn | **3.0 / 10** | Thực đơn tuần **không có nút `"Đã nấu xong"`** để trừ nguyên liệu sau khi nấu. Đơn vị tùy chỉnh (`gram`, `quả`) khi nấu xong chỉ trừ `-1` (`500 gram` nấu xong còn `499 gram`). |
| 8 | **Chị Ngọc (29t)** — Vợ mới cưới, hay còn đồ tồn 1 phần trong tủ từ tuần trước | **2.5 / 10** | Trong tủ còn `1 phần` thịt heo sắp hỏng nhưng tuần mới cần `3 bữa`, app báo *"Đã có sẵn trong tủ"* và gạch khỏi danh sách đi chợ. Mua đồ mới về bấm Cất vào tủ bị bỏ qua (no-op) nếu `have == true`. |

---

## 2. Chi Tiết 8 Lỗi Nghiêm Trọng Nhất (Kèm Bằng Chứng Mã Nguồn)

### 🔴 [P0-1] Mất sạch 100% Thực đơn tuần & Checklist đi chợ ngay khi chuyển Tab hoặc xoay màn hình
- **Người phát hiện:** Anh Hoàng (Kỹ sư PM)
- **Bằng chứng code:**
  - Tại [`RecipeScreen.kt#L84-L87`](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/RecipeScreen.kt#L84-L87) và [`RecipeScreen.kt#L438`](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/RecipeScreen.kt#L438):
    ```kotlin
    var mode by remember { mutableStateOf(initialMode) }
    var goal by remember { mutableStateOf(DietGoal.BALANCED) }
    var preferences by remember { mutableStateOf(emptySet<FamilyPreferenceTag>()) }
    var swappedOffsets by remember { mutableStateOf(emptyMap<Int, Int>()) }
    val unchecked = remember(shopping) { mutableStateMapOf<String, Boolean>() }
    ```
  - Khi người dùng đang ở siêu thị, chọn xong khẩu vị, bấm `"Đổi món ↻"` cho 3 ngày và tích chọn giỏ hàng, chỉ cần bấm sang tab **Tủ lạnh** (`when (currentTab)` trong [`MainScaffold.kt#L110-L157`](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/ui/MainScaffold.kt#L110-L157)) để kiểm tra đồ ở nhà rồi quay lại tab **Món ăn**, `RecipeScreen` bị khởi tạo lại hoàn toàn -> **mất sạch mọi lựa chọn vừa làm**.
  - Ngoài ra, tại [`RecipeScreen.kt#L438`](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/RecipeScreen.kt#L438), `remember(shopping)` làm toàn bộ các món người dùng đã bỏ tích trong danh sách đi chợ bị reset lại mỗi khi bấm `"Đổi món ↻"` ở bất kỳ ngày nào.

### 🔴 [P0-2] Sai lệch số lượng (`quantity` vs `mealCount`) trong Vòng lặp Đi chợ → Cất tủ
- **Người phát hiện:** Chị Ngọc, Chị Lan, Anh Hoàng
- **Bằng chứng code:**
  1. **Ảo tưởng đủ đồ (False Sufficiency):** Tại [`WeeklyMealPlanner.kt#L113`](file:///e:/KMM%20App/shared/src/commonMain/kotlin/com/loitu/yourrefri/feature/recipe/logic/WeeklyMealPlanner.kt#L113), `alreadyInPantry` chỉ kiểm tra `state.pantry[id]?.have == true` mà phớt lờ `state.quantities[id]`. Nếu trong tủ chỉ còn **1 phần** Thịt heo (thậm chí `daysLeft == 0`), nhưng cả tuần cần **3 bữa** (`mealCount = 3`), app vẫn loại Thịt heo khỏi danh sách cần mua.
  2. **Cất đồ cho 3 bữa nhưng tủ chỉ ghi nhận 1 phần:** Tại [`MainScaffold.kt#L145`](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/ui/MainScaffold.kt#L145), `onStockIngredients` gọi `PantryGridAction.AddIngredient(it, null)`. Hàm [`addIngredient`](file:///e:/KMM%20App/shared/src/commonMain/kotlin/com/loitu/yourrefri/feature/pantry/logic/PantryGridReducer.kt#L114-L134) chỉ bật `have = true` (mặc định `quantity = 1`) chứ không cập nhật số lượng theo `mealCount` (3 phần). Nấu xong bữa đầu tiên (trừ 1) là tủ báo **Hết sạch** đồ của 2 bữa còn lại trong tuần.
  3. **Rò rỉ số lượng cũ (Quantity State Leak):** Trong [`PantryGridReducer.kt#L189-L219`](file:///e:/KMM%20App/shared/src/commonMain/kotlin/com/loitu/yourrefri/feature/pantry/logic/PantryGridReducer.kt#L189-L219), khi chạm thẻ chuyển từ `Có` (ví dụ `quantity = 5`) sang `Hết`, `state.quantities[id]` không được xóa về 1. Lần sau mua đồ mới bấm `Có`, số lượng tự nhảy lại thành `5`.

### 🔴 [P0-3] Thực đơn tuần không có nút `"Đã nấu xong"` — Đứt gãy vòng lặp trừ nguyên liệu
- **Người phát hiện:** Anh Tuấn (Meal-prep), Chị Lan
- **Bằng chứng code:**
  - Tại [`RecipeScreen.kt#L425-L489`](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/RecipeScreen.kt#L425-L489), `WeeklyMealPlanContent` không nhận `onCookRecipe`. Khi mở một ngày ra xem 4 bước nấu (dòng 461), người dùng **không có nút `"Đã nấu xong món này 🎉"`** để trừ nguyên liệu trong tủ như ở tab Gợi ý hôm nay (dòng 376–386).

### 🔴 [P0-4] Lỗi UX/Trợ năng: Tên món trong Thực đơn tuần không có dấu hiệu bấm được (Zero Affordance)
- **Người phát hiện:** Bác Tâm (66 tuổi)
- **Bằng chứng code:**
  - Tại [`RecipeScreen.kt#L457-L460`](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/RecipeScreen.kt#L457-L460):
    ```kotlin
    Text(day.recipe.title, Modifier.clickable { openedDay = if (openedDay == day.dayIndex) null else day.dayIndex }, ...)
    ```
  - Dòng chữ tên món ăn không có icon mũi tên đóng/mở (`▼` / `▲`), không có nhãn *"Xem cách nấu"*, không đạt chiều cao tối thiểu `48.dp`, và nằm ngay sát nút `"Đổi món ↻"` bên dưới — người lớn tuổi hoặc không biết bấm vào đâu để xem công thức, hoặc bấm trượt sang nút Đổi món làm mất món đang xem.

### 🟠 [P1-1] Bug sai ID nguyên liệu `"khoai"` thay vì `"khoai-tay"` trong bộ lọc trẻ em
- **Người phát hiện:** Chị Hương, Bạn Minh
- **Bằng chứng code:**
  - Tại [`WeeklyMealPlanner.kt#L24`](file:///e:/KMM%20App/shared/src/commonMain/kotlin/com/loitu/yourrefri/feature/recipe/logic/WeeklyMealPlanner.kt#L24): `private val kidIds = setOf("trung-ga", "thit-ga", "khoai")`.
  - Trong khi [`shelf_table.json#L15`](file:///e:/KMM%20App/composeApp/src/main/assets/shelf_table.json#L15) và [`recipes.json#L127`](file:///e:/KMM%20App/composeApp/src/main/assets/recipes.json#L127) đặt ID là `"khoai-tay"`. Điều kiện `"khoai"` là dead code 100%, khiến món *"Thịt gà kho khoai tây cà rốt"* không được tính điểm khoai tây cho trẻ nhỏ.

### 🟠 [P1-2] Bẫy nguyên liệu thay thế (`slot.anyOf.first()`) và thiếu cơ chế Loại trừ dị ứng
- **Người phát hiện:** Chị Thảo (Mẹ có con dị ứng tôm)
- **Bằng chứng code:**
  - Tại [`WeeklyMealPlanner.kt#L107`](file:///e:/KMM%20App/shared/src/commonMain/kotlin/com/loitu/yourrefri/feature/recipe/logic/WeeklyMealPlanner.kt#L107):
    ```kotlin
    val selected = slot.anyOf.firstOrNull { state.pantry[it]?.have == true } ?: slot.anyOf.first()
    ```
  - Với món có nguyên liệu thay thế như `anyOf: ["tom", "thit-heo-xay"]` ([`recipes.json#L142`](file:///e:/KMM%20App/composeApp/src/main/assets/recipes.json#L142)), dù người dùng chỉ chọn tag khẩu vị `"Thịt heo, bò, gà"` (`MEAT`) mà không chọn `"Cá & Tôm"` (`SEAFOOD`), khi cả hai chưa có trong tủ, hàm vẫn ép lấy `slot.anyOf.first()` là `"tom"` bỏ vào giỏ đi chợ thay vì chọn nguyên liệu khớp với khẩu vị gia đình (`"thit-heo-xay"`).

### 🟠 [P1-3] Bữa cơm 1 món đơn lẻ & Thứ tự tĩnh từ Thứ 4 đến Chủ nhật
- **Người phát hiện:** Chị Lan, Chị Hương
- **Bằng chứng code:**
  - Trong [`WeeklyMealPlanner.kt#L59`](file:///e:/KMM%20App/shared/src/commonMain/kotlin/com/loitu/yourrefri/feature/recipe/logic/WeeklyMealPlanner.kt#L59), `pantryScore` chỉ được cộng cho `day <= 1` (Thứ 2, Thứ 3). Từ Thứ 4 đến Chủ nhật (`day` 2..6), điểm số hoàn toàn tĩnh theo thứ tự trong `recipes.json`. Hơn nữa, những ngày chỉ xếp 1 món như *"Rau muống xào hành tây"* làm bữa chính là chưa sát thực tế mâm cơm gia đình Việt.

### 🟡 [P2-1] Không cho phép thêm thực phẩm ngoài 25 món mặc định & Không cho bỏ qua ngày ăn ngoài
- **Người phát hiện:** Bạn Minh (24t)
- **Bằng chứng code:**
  - [`PantryGridReducer.kt#L114`](file:///e:/KMM%20App/shared/src/commonMain/kotlin/com/loitu/yourrefri/feature/pantry/logic/PantryGridReducer.kt#L114) chặn mọi ID không nằm trong `shelf_table.json` (25 món). Người dùng gõ tên món mới trong hộp thoại `+` sẽ bị báo lỗi đỏ không lưu được.
