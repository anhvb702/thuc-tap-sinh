# Báo Cáo Nghiệm Thu Vòng 2 (Round 2 UX Verification) & Góp Ý Tiếp Theo — 2026-11-08

> **Phương pháp & Mức bằng chứng (Evidence Level):**
> Báo cáo thẩm định **Persona Source & Contract Walkthrough (Round 2)** do Đội ngũ Khách hàng Agent (**Chị Mai, Anh Nam, Cô Hạnh/Cô Lan, Chị Vy/Anh Quân**) và **Documentation Auditor** thực hiện sau khi Codex CLI triển khai `UX-04`, `W5-02B`, `P12-02R` và làm sạch hệ thống tài liệu.
> *Lưu ý: Đây là góc nhìn giả lập theo kịch bản Persona gia đình, không thay thế nghiên cứu hoặc phỏng vấn người dùng thật.*

---

## 1. Kết Quả Nghiệm Thu Các Hạng Mục P0 / P1 Từ Vòng 1

| Hạng mục P0 / P1 | Trạng thái | Đánh giá của Khách hàng Agent & Bằng chứng Code |
| :--- | :---: | :--- |
| **1. `[P0]` Placeholder trong `AddFoodDialog` khớp với danh mục thật** | **ĐÃ ĐẠT ✅** | Đã đổi thành `"Ví dụ: Thịt bò, Cá basa, Trứng gà..."` ([PantryGridScreen.kt#L441](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt#L441)). Chị Mai gõ thử theo ví dụ không còn bị báo lỗi từ chối như bản cũ (`"Bắp cải"`). |
| **2. `[P0]` Tìm kiếm không dấu, lọc chip động & cảnh báo truy vấn (`QuickAddSuggestions.kt`)** | **ĐẠT MỘT PHẦN 🟡** | - **Đã làm tốt:** `normalizeFoodQuery` hỗ trợ bỏ dấu tiếng Việt, lọc chip gợi ý thời gian thực và báo rõ *"Món này đã có trong tủ rồi"* / *"Không có món phù hợp"* ([QuickAddSuggestions.kt#L15-L76](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/QuickAddSuggestions.kt#L15-L76)).<br>- **Lỗ hổng UX mới phát hiện (Nút `"Thêm vào tủ"` bị "câm" khi từ khóa khớp nhiều món):** Khi Chị Mai gõ `"Thịt"`, `"Cá"` hoặc `"Sữa"` (khớp >= 2 món) mà chưa chạm vào chip gợi ý đã bấm luôn nút `"Thêm vào tủ"`, `quickAddMatch` trả về `null`, `addFoodError = true` nhưng `quickAddQueryMessage` trả về `null` ([QuickAddSuggestions.kt#L55-L59](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/QuickAddSuggestions.kt#L55-L59), [PantryGridScreen.kt#L471-L483](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt#L471-L483)). Kết quả là hộp thoại đứng im, không hiện bất kỳ dòng hướng dẫn nào. |
| **3. `[P0]` Hạn dùng gợi ý theo từng món khi thêm nhanh** | **ĐẠT MỘT PHẦN 🟡** | - **Đã làm tốt:** Thêm chip `"Theo gợi ý · X ngày"` lấy từ `userShelfDays ?: defaultShelfDays` ([PantryGridScreen.kt#L490-L507](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt#L490-L507)).<br>- **Cần khóa chặt thêm:** Khi món có hạn gợi ý trùng với `2, 3, 7, 14 ngày` (như *Thịt bò* 2 ngày, *Cải ngọt* 3 ngày), giao diện hiện lặp cả chip `"Theo gợi ý · 2 ngày"` lẫn chip `"2 ngày"`. Đồng thời **Mutation 1** trong [UX-04.md#L50](file:///e:/KMM%20App/docs/tests/UX-04.md#L50) vẫn đang sống sót. |
| **4. `[P1]` Chạm thẻ gợi ý ở Trang chủ mở đúng công thức bên tab Món ăn** | **ĐÃ ĐẠT ✅** *(Chờ khóa test M5)* | `HomeScreen` truyền đúng `recipe.id` -> `MainScaffold` -> `RecipeScreen` tự xóa bộ lọc thời gian cũ (`shouldClearDurationForRequest`), mở sẵn công thức và cuộn tới bằng `BringIntoViewRequester` ([HomeScreen.kt#L238](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/home/ui/HomeScreen.kt#L238), [MainScaffold.kt#L116-L119](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/ui/MainScaffold.kt#L116-L119), [RecipeScreen.kt#L79-L86](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/RecipeScreen.kt#L79-L86)). Cần khóa nốt **Mutation 5** trong [UX-04.md#L55](file:///e:/KMM%20App/docs/tests/UX-04.md#L55). |
| **5. `[P1]` Hộp thoại xác nhận `"Đã nấu xong"`: Liệt kê nguyên liệu, xử lý khi tủ trống & nút `"Để sau"`** | **ĐÃ ĐẠT ✅** | - Nêu đích danh nguyên liệu sẽ giảm (`"Mình sẽ giảm số lượng: ..."`).<br>- Khi `ingredientNames.isEmpty()`, đổi tiêu đề thành `"Chưa có nguyên liệu cần cập nhật"` và không báo cập nhật giả ([RecipeCookPresentation.kt#L33-L42](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/RecipeCookPresentation.kt#L33-L42)).<br>- Nút `"Để sau"` đã dùng `TextButton` ([RecipeScreen.kt#L139-L144](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/RecipeScreen.kt#L139-L144)). |
| **6. `[P1]` Nút `−`/`+` tròn `20.sp`, căn giữa dòng nhắc, ẩn `"Sửa hạn ⏱️"` khi `Chưa có` & đệm đáy `88.dp`** | **ĐÃ ĐẠT ✅** *(Chờ khóa test M6)* | - Nút `−`/`+` có nền tròn `SoftGreen` 48dp, chữ `20.sp` đậm ([PantryGridScreen.kt#L685-L732](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt#L685-L732)).<br>- Dòng `"− để đánh dấu hết"` đã căn giữa `TextAlign.Center` ([PantryGridScreen.kt#L733-L741](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt#L733-L741)).<br>- `"Sửa hạn ⏱️"` chỉ hiện khi `cell.have == true` ([PantryGridScreen.kt#L747-L763](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt#L747-L763)).<br>- `LazyVerticalGrid` có `bottom = 88.dp` ([PantryGridScreen.kt#L275](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt#L275)). Cần khóa nốt **Mutation 6**. |

---

## 2. Kết Quả Nghiệm Thu Hệ Thống Tài Liệu (Round 2 Docs Audit)
* **Đạt ~95%:** `HANDOFF.md`, `SPEC.md`, `PLAN.md`, `DESIGN.md`, `DECISIONS.md` và các biên bản `UX-02.md`, `UX-03.md`, `UX-04.md`, `W5-02B.md`, `P12-02R.md` đã đồng bộ hoàn toàn về số lượng test (**332 shared / 85 composeApp**), xóa sạch nhật ký chồng chéo và phản ánh trung thực 3 mutation còn sống của `UX-04`.
* **Điểm nhỏ còn sót:**
  * Dòng 22 của [PLAN.md](file:///e:/KMM%20App/PLAN.md#L22) còn dư một dấu gạch ngang `-` trống ở cột `⚙️ CÁ NHÂN`.
  * [SPEC.md#L39](file:///e:/KMM%20App/SPEC.md#L39) và [DESIGN.md#L66-L76](file:///e:/KMM%20App/DESIGN.md#L66-L76) còn sót mô tả cũ ("hình ảnh món, độ khó", "vị trí ngăn mát/đông in trên thẻ").

---

## 3. Danh Sách Việc Cần Xử Lý Tiếp Theo (Backlog Vòng 3 Cho Codex CLI)

### Ưu tiên 1: Khóa dứt điểm UX-04 (Logic & 3 Mutations còn sống)
1. **Xử lý nút `"Thêm vào tủ"` bị "câm" khi từ khóa khớp nhiều món (`"Thịt"`, `"Cá"`, `"Sữa"`):**
   * Trong [QuickAddSuggestions.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/QuickAddSuggestions.kt#L48-L60) và [PantryGridScreen.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt#L471-L483): Khi người dùng gõ từ khóa khớp từ 2 món trở lên (`uniqueMatch == null` nhưng `matchingIngredients.size > 1`) và bấm xác nhận (`addFoodError = true`), hiển thị thông báo nhẹ nhàng: `"Có vài món phù hợp, bạn chạm chọn 1 món gợi ý bên trên nhé."`.
2. **Bắt đủ 6/6 Mutations của `UX-04` ([docs/tests/UX-04.md](file:///e:/KMM%20App/docs/tests/UX-04.md#L49-L58)):**
   * Đưa các quyết định wiring (chọn hạn khi bấm chip gợi ý, điều hướng thẻ Home gửi đúng `recipe.id`, hằng số/helper cấu hình `contentPadding` đáy 88dp của lưới Pantry) vào các hàm/model trình bày mà UI gọi trực tiếp, để cả **Mutation 1, 5, 6** đều bị unit test bắt đỏ (6/6 mutations).

### Ưu tiên 2: Các tinh chỉnh UX P2/P3 nhẹ nhàng & Văn phong ấm áp
3. **Văn phong thông báo sau khi nấu ([RecipeCookPresentation.kt#L41](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/RecipeCookPresentation.kt#L41)):**
   * Đổi `"Đã gửi yêu cầu cập nhật: ..."` (nghe giống gọi server mạng) thành câu ấm áp, tức thì hơn: `"Đã cập nhật trong tủ: ..."` (và cập nhật test tương ứng).
4. **Bỏ thuật ngữ `"JSON"` còn sót trong [MainActivity.kt#L130-L152](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/MainActivity.kt#L130-L152) (Cô Hạnh/Cô Lan):**
   * Đổi `"Đã lưu bản sao lưu JSON trên thiết bị."` -> `"Đã lưu tệp bản sao trên điện thoại."`
   * Đổi `"Chưa thể đọc bản sao lưu này. Hãy chọn file JSON YourRefri hợp lệ."` -> `"Chưa thể đọc tệp này. Hãy chọn đúng tệp bản sao YourRefri."`
5. **Đồng nhất `TextButton` cho nút `"Để sau"` trong hộp thoại Khôi phục ở [SettingsScreen.kt#L59-L64](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/settings/ui/SettingsScreen.kt#L59-L64) và giữ 1 dòng cho nhãn Bottom Bar ở [MainScaffold.kt#L85-L90](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/ui/MainScaffold.kt#L85-L90) (`maxLines = 1`).**
