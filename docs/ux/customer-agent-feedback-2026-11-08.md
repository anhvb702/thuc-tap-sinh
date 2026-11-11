# Báo Cáo Tổng Hợp Đánh Giá UX Từ Đội Ngũ Khách Hàng Agent — 2026-11-08

> **Phương pháp & Mức bằng chứng (Evidence Level):**
> Đây là báo cáo thẩm định **Persona Source & Screenshot Walkthrough** do hai nhóm Customer Persona Agent thực hiện độc lập dựa trên mã nguồn thực tế (`composeApp/src/main/`, `shared/src/commonMain/`) và các ảnh chụp màn hình runtime mới nhất trong `build/verification_reports/` (`p11-final-pantry.png`, `p11-current-only.png`, `ux-followup-home2.png`, `ux-followup-settings.png`, `ux-followup-pantry360-font13.png`, `ux-wave-api36-add-dialog.png`) sau các bản cập nhật **P11-01..04** và **UX-01..10**.
> *Lưu ý: Đây là góc nhìn giả lập theo kịch bản Persona gia đình, không thay thế nghiên cứu hoặc phỏng vấn trên người dùng thật.*

---

## 1. Chân Dung Các Khách Hàng Agent Tham Gia Đánh Giá

1. **Chị Mai (35 tuổi — Mẹ bận rộn có 2 con nhỏ):** Mệt mỏi sau giờ làm, muốn mở app 5 giây biết ngay tủ có gì và tối nay nấu gì, đi chợ về bấm 1–2 chạm để cất đồ vào tủ, rất ghét nhập liệu rườm rà.
2. **Anh Nam (38 tuổi — Chồng cùng nấu ăn):** Thực tế, quan tâm thời gian nấu (`< 15p`, `< 30p`), muốn nhìn rõ nguyên liệu nào *Đã có* vs *Cần mua thêm*, thích các bước nấu ngắn gọn có checklist, và muốn cập nhật trừ nguyên liệu sau khi nấu mà không sợ trừ nhầm.
3. **Cô Hạnh / Cô Lan (65–68 tuổi — Bà nội/ngoại phụ nấu cơm gia đình):** Mắt lão thị (bật cỡ chữ lớn `fontScale 1.3–2.0`, màn hình nhỏ `360dp`), sợ bấm nhầm làm mất dữ liệu con cháu đã nhập, không rành thuật ngữ công nghệ (`JSON`, `URI`), cần chữ to rõ và không bị cắt chữ.
4. **Chị Vy & Anh Quân (Gia đình nhiều người cùng đi chợ / Đổi điện thoại / Đi chợ tầng hầm mất sóng):** Quan tâm dùng offline 100%, chuyển dữ liệu sang máy mới hoặc chia sẻ trạng thái tủ lạnh qua Zalo/tệp, cần đơn vị đo lường sát thực tế và hạn dùng ước tính khách quan.

---

## 2. Chi Tiết Góp Ý Theo Từng Khách Hàng Agent

### 👩‍👧‍👦 2.1. Chị Mai (Phụ huynh bận rộn — Ưu tiên tốc độ & ít chạm)

* **Điều đã làm tốt (What works well):**
  * **Trang chủ 5 giây ấm áp và dễ quét mắt:** Lời chào theo buổi (`Chào buổi tối 🌿` / `Hôm nay nhà mình thế nào?`) cùng tông màu `WarmIvory` và `SoftGreenLight` mang lại cảm giác nhẹ nhàng ([HomeScreen.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/home/ui/HomeScreen.kt#L85-L95), [ux-followup-home2.png](file:///e:/KMM%20App/build/verification_reports/ux-followup-home2.png)).
  * **Xem trước nguyên liệu trên Home (UX-02):** Thẻ tổng quan hiện rõ số lượng nguyên liệu sẵn sàng kèm dòng tóm tắt tên các món đang có (`Thịt heo · ...`), không cần chuyển tab vẫn nhẩm được đồ trong nhà ([HomeScreen.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/home/ui/HomeScreen.kt#L119-L131)).
  * **Nhãn trạng thái rõ ràng sau P11:** Tách bạch rõ `Có trong tủ` (nền xanh đậm) vs `Chưa có` kèm hướng dẫn `Chạm để thêm vào tủ` ([PantryCardPresentation.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryCardPresentation.kt#L10-L25), [p11-final-pantry.png](file:///e:/KMM%20App/build/verification_reports/p11-final-pantry.png)). Công tắc `"Chỉ xem món đang có"` (`Trong tủ: 1 món`) giúp lọc gọn ngay những thứ thực sự có trong tủ ([p11-current-only.png](file:///e:/KMM%20App/build/verification_reports/p11-current-only.png)).

* **Pain Points (Khúc mắc thực tế):**
  1. **Placeholder trong hộp thoại Thêm món (`+`) gợi ý món không tồn tại trong danh mục:** Placeholder ghi *"Ví dụ: Thịt bò, Bắp cải, Sữa..."* ([PantryGridScreen.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt#L437)), nhưng trong [shelf_table.json](file:///e:/KMM%20App/composeApp/src/main/assets/shelf_table.json#L1-L27) **không có "Bắp cải"** và chỉ có `"Sữa tươi (đã mở)"` / `"Sữa chua"`. Do hàm `findIngredientByName` so khớp chính xác 100% toàn chuỗi ([PantryGridScreen.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt#L513)), nếu Chị Mai gõ đúng chữ `"Bắp cải"` hoặc `"Sữa"` theo ví dụ của app sẽ bị báo lỗi đỏ *"Chưa tìm thấy món trong danh sách"*.
  2. **Chạm vào món gợi ý ở Trang chủ nhưng không mở đúng món đó:** Khi ưng món thứ 2 hoặc thứ 3 ở Trang chủ (ví dụ *"Rau muống xào hành tây"*) và chạm vào thẻ, app chuyển sang tab Món ăn nhưng luôn tự động mở công thức số 1 (`recommendations.firstOrNull()?.recipe?.id`), bắt người dùng phải tìm và bấm lại ([HomeScreen.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/home/ui/HomeScreen.kt#L231-L238), [MainScaffold.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/ui/MainScaffold.kt#L108), [RecipeScreen.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/RecipeScreen.kt#L73-L75)).
  3. **Thẻ gợi ý ở Trang chủ chưa cho biết món đó đã đủ đồ nấu hay còn thiếu:** Trên [ux-followup-home2.png](file:///e:/KMM%20App/build/verification_reports/ux-followup-home2.png), món *"Thịt heo kho trứng"* hiện tag `Có món nên dùng sớm`, nhưng phải bấm sang tab Món ăn mới biết trong tủ còn thiếu `Trứng gà`.
  4. **Thẻ `Chưa có` vẫn hiện nút `"Sửa hạn ⏱️"` và công tắc lọc bị reset khi chuyển tab:** Trên [p11-final-pantry.png](file:///e:/KMM%20App/build/verification_reports/p11-final-pantry.png), các món `Chưa có` vẫn hiện dòng `Sửa hạn ⏱️` cao 48dp ([PantryGridScreen.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt#L714-L728)) gây thắc mắc *"Đồ chưa có trong tủ thì sửa hạn làm gì?"*. Ngoài ra bộ lọc `filters` dùng `remember` cục bộ ([PantryGridScreen.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt#L89)) nên chuyển qua tab Món ăn rồi quay lại là mất trạng thái lọc `"Chỉ xem món đang có"`.

* **Expectations (Kỳ vọng):**
  * Placeholder chỉ nêu các món thực sự có trong catalog (`"Ví dụ: Thịt bò, Cà chua, Trứng gà..."`) và khi gõ chữ (ví dụ `"Sữa"`) thì lọc gợi ý gần đúng các chip bên dưới thay vì bắt gõ khớp từng ký tự.
  * Bấm vào thẻ món ăn nào ở Trang chủ thì mở thẳng công thức món đó ở tab Món ăn, đồng thời hiện ngắn gọn trạng thái (`Đủ nguyên liệu` hoặc `Thiếu 1 món`) ngay trên thẻ ở Trang chủ.
  * Ẩn nút `"Sửa hạn ⏱️"` ở các thẻ `Chưa có` để thẻ gọn gàng.

---

### 👨‍🍳 2.2. Anh Nam (Người nấu ăn cùng — Ưu tiên rõ ràng, đúng thời gian & không trừ nhầm)

* **Điều đã làm tốt (What works well):**
  * **Phân loại nguyên liệu và thời gian nấu trực quan:** Có 3 chip lọc thời gian cao 48dp, thẻ món ăn tách màu rõ `Đã có: ...` (xanh `SageGreenText`) và `Cần thêm: ...` (cam đất `TerracottaText`) ([RecipeScreen.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/RecipeScreen.kt#L201-L260)).
  * **Công thức 4 bước thực tế:** Gia vị quen thuộc trong bếp Việt, có ô `Checkbox` đánh dấu từng bước khi đang đứng bếp ([RecipeScreen.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/RecipeScreen.kt#L271-L297)).
  * **Gợi ý hành động ở số lượng 1:** Dòng nhắc `− để đánh dấu hết` ([PantryCardPresentation.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryCardPresentation.kt#L16)) giúp biết trước nếu bấm `−` ở mức 1 thì món sẽ chuyển sang hết.

* **Pain Points (Khúc mắc thực tế):**
  1. **Hộp thoại xác nhận `"Đã nấu xong"` mập mờ và báo cập nhật thành công giả khi không có nguyên liệu:**
     * Khi bấm `"Đã nấu xong món này 🎉"`, hộp thoại chỉ nói chung chung: *"Mình sẽ giảm số lượng nguyên liệu đang có trong tủ cho món này."* ([RecipeScreen.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/RecipeScreen.kt#L82-L106)) mà không liệt kê tên món nào sẽ bị trừ (đặc biệt khi món có nguyên liệu thay thế như *Thịt heo* hoặc *Thịt heo xay*).
     * Ngay cả khi trong tủ **không có bất kỳ nguyên liệu nào** của món đó (`haveNames.isEmpty()`), nút `"Đã nấu xong món này 🎉"` vẫn cho bấm và sau khi xác nhận vẫn hiện thông báo xanh: **`"✨ Tuyệt vời! Đã tự động cập nhật nguyên liệu trong tủ và hoàn thành bữa ăn."`** ([RecipeScreen.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/RecipeScreen.kt#L314-L320)) dù thực tế không có gì trong tủ được cập nhật.
     * Cả hai nút `"Để sau"` và `"Cập nhật"` trong `AlertDialog` đều dùng `Button` đặc (`Filled Button`) giống hệt nhau ([RecipeScreen.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/RecipeScreen.kt#L90-L105)), dễ bấm nhầm khi tay ướt.
  2. **Kho công thức (`recipes.json`) mới có 3 món cho 25 nguyên liệu:** 20/25 nguyên liệu trong catalog (Thịt bò, Thịt gà, Tôm, Cá hồi, Đậu phụ, Nấm tươi, Cà rốt, Khoai tây...) chưa được gắn với bất kỳ công thức nào trong [recipes.json](file:///e:/KMM%20App/composeApp/src/main/assets/recipes.json#L1-L48).
  3. **Chạm nhầm vào phần thân thẻ `Có trong tủ` khi số lượng `> 1` làm mất món ngay lập tức:** Toàn bộ nửa trên thẻ là vùng `clickable` gọi `onTap` chuyển thẳng về `Chưa có` mà không có xác nhận hay nút Hoàn tác ([PantryGridScreen.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt#L574-L585)).

* **Expectations (Kỳ vọng):**
  * Hộp thoại xác nhận sau khi nấu liệt kê đích danh nguyên liệu sẽ giảm (ví dụ: *"Mình sẽ giảm 1 đơn vị của: Thịt heo, Trứng gà trong tủ nhé?"*) và nút `"Để sau"` dùng kiểu nút phụ (`TextButton` / `OutlinedButton`).
  * Nếu trong tủ chưa có nguyên liệu nào của món ăn đó thì ẩn thao tác trừ tủ hoặc thông báo đúng thực tế.
  * Bổ sung thêm các món cơm nhà quen thuộc (cho Thịt bò, Thịt gà, Đậu phụ, Tôm, Rau củ) để bộ lọc thời gian và gợi ý dùng sớm phát huy tối đa tác dụng.

---

### 👵 2.3. Cô Hạnh / Cô Lan (Người lớn tuổi 65–68 tuổi — Ưu tiên chữ to rõ, dễ hiểu, không sợ mất dữ liệu)

* **Điều đã làm tốt (What works well):**
  * **Tự động chuyển sang 1 cột to rõ khi bật chữ lớn (`fontScale >= 1.3f`):** Trên ảnh [ux-followup-pantry360-font13.png](file:///e:/KMM%20App/build/verification_reports/ux-followup-pantry360-font13.png), thẻ thực phẩm mở rộng toàn chiều ngang màn hình `360dp` ([PantryGridScreen.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt#L90)), tên món và hạn dùng hiển thị trọn vẹn, không còn bị cắt `...`.
  * **Chạm nhầm 2 lần không làm mất ngày mua cũ:** Cơ chế `sessionBase` trong [PantryGridReducer.kt](file:///e:/KMM%20App/shared/src/commonMain/kotlin/com/loitu/yourrefri/feature/pantry/logic/PantryGridReducer.kt#L180-L192) bảo toàn `markedAt` nếu lỡ tay chạm tắt rồi bật lại ngay.
  * **Đơn vị đo lường chợ Việt:** Hộp thoại chọn đơn vị (`phần`, `cái`, `quả`, `bó`, `hộp`, `chai`...) dùng `RadioButton` cao 48dp rất dễ chọn.

* **Pain Points (Khúc mắc thực tế):**
  1. **Nút tròn nổi `+` (FAB) đè che khuất thẻ thực phẩm ở góc dưới phải:** Trên cả hai ảnh [p11-final-pantry.png](file:///e:/KMM%20App/build/verification_reports/p11-final-pantry.png) và [ux-followup-pantry360-font13.png](file:///e:/KMM%20App/build/verification_reports/ux-followup-pantry360-font13.png), nút `+` che trực tiếp lên góc phải của thẻ thực phẩm (`Cá hồi` / `Thịt heo xay`) vì `LazyVerticalGrid` chỉ đặt `contentPadding = PaddingValues(bottom = 16.dp)` ([PantryGridScreen.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt#L276)).
  2. **Dấu `−` / `+` hiển thị quá mảnh (`16.sp`) và dòng nhắc `"- để đánh dấu hết"` bị lệch lề:**
     * Dù vùng chạm đạt 48dp, ký tự `"−"` và `"+"` chỉ có `fontSize = 16.sp` và không có nền/viền nút ([PantryGridScreen.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt#L657-L700)), nhìn chìm vào nền thẻ.
     * Dòng `"− để đánh dấu hết"` dùng `padding(start = 48.dp)` ([PantryGridScreen.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt#L701-L708)) nên bị lệch hẳn sang phải trên ảnh [p11-final-pantry.png](file:///e:/KMM%20App/build/verification_reports/p11-final-pantry.png), dấu `−` trông giống gạch đầu dòng khiến người lớn tuổi bối rối không biết bấm vào đâu thì bị xóa món.
  3. **Còn sót thuật ngữ kỹ thuật `"JSON"` trong thông báo kết quả sao lưu:** Dù màn hình Cài đặt đã Việt hóa rất tốt, nhưng thông báo kết quả trong [MainActivity.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/MainActivity.kt#L130) và [MainActivity.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/MainActivity.kt#L152) vẫn hiện: *"Đã lưu bản sao lưu JSON trên thiết bị"* và *"Hãy chọn file JSON YourRefri hợp lệ"*.
  4. **Nhãn `"Trang chủ"` ở thanh điều hướng đáy bị ngắt thành 2 dòng (`Trang` / `chủ`):** Quan sát thấy rõ trên ảnh màn hình nhỏ chữ lớn [ux-followup-pantry360-font13.png](file:///e:/KMM%20App/build/verification_reports/ux-followup-pantry360-font13.png) ([MainScaffold.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/ui/MainScaffold.kt#L81-L86)).

* **Expectations (Kỳ vọng):**
  * Tăng `bottom padding` của danh sách Tủ lạnh lên `88.dp` để nút `+` không bao giờ đè lên thẻ cuối.
  * Thêm nền tròn/khung nhẹ và tăng cỡ chữ cho 2 nút `−` / `+` (`20.sp`); căn giữa dòng nhắc dưới số lượng.
  * Thay chữ `"JSON"` trong thông báo ở `MainActivity.kt` bằng `"tệp sao lưu YourRefri"`.

---

### 🛒 2.4. Chị Vy & Anh Quân (Gia đình dùng chung / Đi chợ mất sóng / Đổi máy mới)

* **Điều đã làm tốt (What works well):**
  * **Cam kết Offline & Hướng dẫn chuyển máy 3 bước cực kỳ rõ ràng (UX-10):** Ảnh [ux-followup-settings.png](file:///e:/KMM%20App/build/verification_reports/ux-followup-settings.png) và [SettingsBackupCopy.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/settings/ui/SettingsBackupCopy.kt#L4-L16) giải thích mạch lạc việc dùng không cần mạng, lưu tệp → chuyển sang máy mới → mở tệp, kèm lưu ý ảnh đính kèm không nằm trong bản sao.
  * **An toàn tuyệt đối khi khôi phục:** Kiểm tra tệp hợp lệ trước khi hỏi xác nhận `"Thay dữ liệu hiện tại?"`, có nút `"Để sau"`, và giữ nguyên dữ liệu cũ nếu tệp lỗi ([MainActivity.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/MainActivity.kt#L148-L192)).

* **Pain Points (Khúc mắc thực tế):**
  1. **Đơn vị `"g"` (gram) nhưng mỗi lần bấm `+` chỉ tăng `1` đơn vị:** Trong [Models.kt](file:///e:/KMM%20App/shared/src/commonMain/kotlin/com/loitu/yourrefri/feature/pantry/data/Models.kt#L5) có đơn vị `"g"`, nhưng [PantryGridReducer.kt](file:///e:/KMM%20App/shared/src/commonMain/kotlin/com/loitu/yourrefri/feature/pantry/logic/PantryGridReducer.kt#L99) luôn cộng/trừ `1`. Khi chọn `"g"`, thẻ hiện `"1 g"`; muốn chỉnh lên `300 g` thịt phải bấm dấu `+` 299 lần (không khả thi trong thực tế).
  2. **Hộp thoại Thêm món (`+`) luôn ép mặc định hạn dùng là `3 ngày`:** Ở [PantryGridScreen.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt#L391), khi chọn chip gợi ý (ví dụ *Trứng gà* có hạn mặc định 14 ngày hoặc *Cá basa* 2 ngày), hộp thoại không tự chọn mốc hạn mặc định của món đó mà luôn giữ cố định ở chip `3 ngày`.
  3. **Tên tệp sao lưu cố định `yourrefri_backup.json` không kèm ngày:** Tại [MainActivity.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/MainActivity.kt#L178), khi hai vợ chồng xuất tệp gửi qua Zalo nhiều lần trong tuần, các tệp có cùng tên dễ bị nhầm lẫn bản cũ với bản mới.
  4. **Khi từ chối quyền thông báo Android, công tắc đứng im không có lời nhắc:** Tại [MainActivity.kt](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/MainActivity.kt#L67-L70), nếu lỡ bấm *Không cho phép*, lần sau gạt lại công tắc không phản hồi và không báo người dùng cần bật quyền trong Cài đặt máy.

* **Expectations (Kỳ vọng):**
  * Đổi đơn vị `"g"` thành `"lạng"` (hoặc `"gói"` / `"khay"`), hoặc tự động nhảy bước `100` khi đơn vị đang là `"g"`.
  * Khi bấm chọn một chip món ăn trong hộp thoại Thêm món, tự động gợi ý mốc hạn dùng sát với `defaultShelfDays` của món đó.
  * Gợi ý tên tệp sao lưu kèm ngày (ví dụ `yourrefri_backup_2026-11-08.json`).

---

## 3. Bảng Ma Trận Ưu Tiên Khắc Phục (Actionable Backlog Từ Khách Hàng)

| Ưu tiên | Hạng mục / Pain Point | Persona đại diện | Vị trí Code / Ảnh chụp | Giải pháp đề xuất |
| :---: | :--- | :---: | :--- | :--- |
| **P0** | **Placeholder & Tìm kiếm trong `AddFoodDialog` gây lỗi từ chối** khi gõ theo ví dụ (*"Bắp cải, Sữa..."*) | Chị Mai | [PantryGridScreen.kt#L437](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt#L437), [PantryGridScreen.kt#L513](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt#L513), [ux-wave-api36-add-dialog.png](file:///e:/KMM%20App/build/verification_reports/ux-wave-api36-add-dialog.png) | Đổi placeholder thành các món có thật trong catalog (`"Ví dụ: Thịt bò, Cà chua, Trứng gà..."`); lọc danh sách chip gợi ý theo từ khóa đang gõ và tự điền hạn mặc định theo món. |
| **P1** | **FAB `+` đè lên thẻ thực phẩm cuối cùng** ở góc dưới phải màn Tủ lạnh | Cô Hạnh, Chị Mai | [PantryGridScreen.kt#L276](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt#L276), [p11-final-pantry.png](file:///e:/KMM%20App/build/verification_reports/p11-final-pantry.png), [ux-followup-pantry360-font13.png](file:///e:/KMM%20App/build/verification_reports/ux-followup-pantry360-font13.png) | Tăng `contentPadding(bottom = 88.dp)` cho `LazyVerticalGrid`. |
| **P1** | **Hộp thoại `"Đã nấu xong"` không nêu tên món sẽ trừ & báo cập nhật giả** khi `haveNames` rỗng | Anh Nam | [RecipeScreen.kt#L82-L106](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/RecipeScreen.kt#L82-L106), [RecipeScreen.kt#L302-L321](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/RecipeScreen.kt#L302-L321) | Nêu rõ các nguyên liệu sẽ trừ trong `AlertDialog`; đổi nút `"Để sau"` thành `TextButton`; xử lý riêng trường hợp `haveNames.isEmpty()`. |
| **P1** | **Chạm món gợi ý ở Trang chủ không mở đúng món đó** khi chuyển sang tab Món ăn | Chị Mai, Anh Nam | [HomeScreen.kt#L231-L238](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/home/ui/HomeScreen.kt#L231-L238), [MainScaffold.kt#L108](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/ui/MainScaffold.kt#L108), [RecipeScreen.kt#L73-L75](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/RecipeScreen.kt#L73-L75) | Truyền `recipeId` từ `HomeScreen` qua `MainScaffold` vào `RecipeScreen`. |
| **P1** | **Nút `−`/`+` quá mảnh (`16.sp`), dòng `"- để đánh dấu hết"` lệch lề & thẻ `Chưa có` thừa nút `"Sửa hạn ⏱️"`** | Cô Hạnh, Chị Mai, Anh Quân | [PantryGridScreen.kt#L657-L728](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt#L657-L728), [p11-final-pantry.png](file:///e:/KMM%20App/build/verification_reports/p11-final-pantry.png) | Chỉ hiện `"Sửa hạn ⏱️"` khi `cell.have == true`; thêm nền nhẹ và tăng cỡ chữ `−`/`+` lên `20.sp`; căn giữa dòng nhắc dưới hàng số lượng. |
| **P2** | **Đơn vị `"g"` chỉ tăng/giảm `1` đơn vị mỗi lần bấm** khiến không dùng được cho 200g–500g | Chị Vy, Anh Quân | [Models.kt#L5](file:///e:/KMM%20App/shared/src/commonMain/kotlin/com/loitu/yourrefri/feature/pantry/data/Models.kt#L5), [PantryGridReducer.kt#L99](file:///e:/KMM%20App/shared/src/commonMain/kotlin/com/loitu/yourrefri/feature/pantry/logic/PantryGridReducer.kt#L99) | Đổi `"g"` thành `"lạng"` / `"gói"` hoặc dùng bước nhảy `100` khi đơn vị là `"g"`. |
| **P2** | **Còn thuật ngữ `"JSON"` trong thông báo kết quả sao lưu** ở `MainActivity.kt` | Cô Hạnh / Cô Lan | [MainActivity.kt#L130](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/MainActivity.kt#L130), [MainActivity.kt#L152](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/MainActivity.kt#L152) | Đổi thành `"tệp bản sao YourRefri"` đồng bộ với `SettingsBackupCopy.kt`. |
| **P3** | **Nhãn `"Trang chủ"` bị ngắt 2 dòng** ở màn 360dp + fontScale 1.3 | Cô Hạnh | [MainScaffold.kt#L81-L86](file:///e:/KMM%20App/composeApp/src/main/kotlin/com/loitu/yourrefri/ui/MainScaffold.kt#L81-L86), [ux-followup-pantry360-font13.png](file:///e:/KMM%20App/build/verification_reports/ux-followup-pantry360-font13.png) | Đặt `maxLines = 1` và tinh chỉnh style chữ trên `NavigationBarItem`. |
