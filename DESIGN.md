# DESIGN.md — Design Tokens & Warm Minimalism System

> **Phong cách:** **Warm Minimalism (Tối giản ấm áp)**  
> **Cảm xúc chủ đạo:** Như một cuốn sổ tay căn bếp gia đình hiện đại — mộc mạc, tĩnh lặng, ấm áp, không mang cảm giác phần mềm doanh nghiệp hay kho bãi.

---

## 1. Bảng Màu Đặc Trưng (Signature Palette)

```
┌─────────────────────────────────────────────────────────────────┐
│ Warm Ivory (#F7F5EF)    - Nền chính (Background / Surface)      │
│ Sage Green (#789477)    - Màu chủ đạo (Primary / Brand Accent)  │
│ Soft Green (#DDE8D9)    - Nền thẻ phụ (Card Surface / Chip)     │
│ Warm Beige (#E8D8B9)    - Điểm nhấn ấm (Secondary / Highlight)  │
│ Terracotta (#D98C70)    - Cảnh báo nhẹ (Urgent / Warning Soft)   │
│ Deep Charcoal (#343B34) - Màu chữ chính (High Contrast Text)     │
│ SageGreenText (#536C51) - Chữ xanh trên nền sáng                  │
│ SageGreenStrong (#506950) - Nền hành động chính, chữ trắng       │
│ SoftGreenLight (#F0F5EF) - Nền nhẹ cho món đang có                │
│ TerracottaText (#8C4A35) - Chữ nhấn cảnh báo trên nền sáng        │
│ MutedGray (#596057) - Chữ phụ đạt tương phản trên nền sáng         │
└─────────────────────────────────────────────────────────────────┘
```

| Tên Token | Mã Hex | Ứng dụng cụ thể trong Jetpack Compose |
| :--- | :--- | :--- |
| `WarmIvory` | `#F7F5EF` | `MaterialTheme.colorScheme.background`, nền màn hình chính. |
| `SageGreen` | `#789477` | Màu thương hiệu và điểm nhấn xanh; không dùng làm chữ nhỏ trên nền sáng. |
| `SoftGreen` | `#DDE8D9` | Nền thẻ thực phẩm (Card Container), badge trạng thái "Đang có trong tủ". |
| `WarmBeige` | `#E8D8B9` | Badge "Ước tính còn 3 ngày", viền nhấn, chip lọc danh mục. |
| `Terracotta` | `#D98C70` | Cảnh báo đồ sắp hỏng (0–1 ngày), nút xóa/hủy, thẻ ưu tiên cao. Không dùng đỏ gắt. |
| `DeepCharcoal` | `#343B34` | Màu văn bản chính, tiêu đề, icon chính. Dịu mắt hơn màu đen tuyền `#000000`. |
| `MutedGray` | `#596057` | Văn bản phụ, nhãn đơn vị, ngày tháng; màu đã tăng tương phản. |
| `SageGreenText` | `#536C51` | Chữ hành động hoặc trạng thái trên nền sáng. |
| `SageGreenStrong` | `#506950` | Nền nút hành động chính đi với chữ trắng. |
| `SoftGreenLight` | `#F0F5EF` | Nền nhẹ cho thẻ món đang có. |
| `TerracottaText` | `#8C4A35` | Chữ cảnh báo/ưu tiên trên nền sáng. |
| `SurfaceWhite` | `#FFFFFF` | Nền bề mặt/thẻ. |

### Token bổ sung (giao diện mới)

| Token | Mã Hex | Ứng dụng |
| :--- | :--- | :--- |
| `TerracottaSoft` | `#F7E4DA` | Nền nhãn "nên dùng trước", chữ `TerracottaText`. |
| `WarmBeigeSoft` / `WarmBeigeText` | `#EFE4CB` / `#5E4B22` | Nhãn "khoảng còn vài ngày". |
| `LineSoft` | `#E3E7DC` | Viền thẻ 1dp, thay màu viền viết cứng. |

**Màu nhấn cam đất (theo ảnh thiết kế v2):** `cta` (`TerracottaStrong #B4553A`, chữ trắng `onCta`; tối: `#F2BBA5` chữ `#3F2C24`), `ctaSoft` (`#F7E4DA`), `ctaText` (`#8C4A35`). Dùng cho tab đang chọn, nút chính ("Thêm bằng giọng nói", "Nấu xong", "Tiếp"), nút tim đã chọn, ngày hôm nay. Xanh sage vẫn là màu của chip đang chọn, công tắc, trạng thái "có trong tủ". Mọi cặp chữ/nền kiểm AA trong `AppColorsTest`.

**Bảng màu tối** (`DarkColorScheme`, chưa bật mặc định cho tới khi mọi màn hình bỏ màu viết cứng): nền `#1B1F1B`, thẻ `#252A25`, chính `#9DBB9C` (chữ trên nút `#14200F`), chữ `#ECEFE8`, chữ phụ `#AAB2A6`, cảnh báo `#F2BBA5` trên `#3F2C24`, beige `#E8D8B9` trên `#3A3322`. Mọi cặp chữ/nền đều kiểm WCAG AA bằng `DarkThemeContrastTest`.

### Token tương phản W4-03

Giữ các giá trị đồng bộ với `composeApp/.../ui/theme/Theme.kt` và `AccessibilityContrastTest`: `DeepCharcoal #343B34`, `MutedGray #596057`, `SageGreenText #536C51`, `TerracottaText #8C4A35` trên bề mặt sáng; nút chính dùng `SageGreenStrong #506950` với chữ trắng. `SageGreen #789477` là màu thương hiệu, không phải màu chữ chính.

---

## 2. Typography & Khoảng Cách (Spacing)

- **Font chữ:** Be Vietnam Pro (Regular, Medium, SemiBold, Bold; giấy phép OFL) nhúng trong `res/font`, dùng được khi không có mạng và đủ dấu tiếng Việt. Nunito đã cân nhắc cho tiêu đề nhưng bản tĩnh không có nguồn tải ổn định và bản biến thiên không chạy trên Android 7 (minSdk 24).
- **Icon:** bộ icon nét một màu (24dp, nét 1.8) trong `ui/theme/AppIcons.kt`, màu do `Icon(tint = ...)` quyết định. Không dùng emoji làm icon giao diện.
- **Cấp bậc văn bản (Type Scale):**
  - Title/Headline: 20sp - 24sp, SemiBold, Deep Charcoal. Không dùng chữ quá to gây áp lực.
  - Body Large: 16sp, Regular / Medium, line-height 24sp.
  - Body Small / Caption: tối thiểu 14sp trong theme hiện tại, Regular, `MutedGray` (cho nhãn "Hạn dùng là ước tính").
- **Spacing Grid:** Hệ số 8dp chuẩn (8dp, 12dp, 16dp, 24dp, 32dp).
- **Touch Target:** Tất cả nút bấm và vùng chạm thẻ tối thiểu **48dp x 48dp** (đảm bảo tay ướt khi nấu ăn vẫn chạm chuẩn).

---

## 3. Quy Chuẩn Thành Phần Giao Diện (Component Guidelines)

### A. Thẻ Thực Phẩm (Food Card)
- **Bo góc:** `RoundedCornerShape(16.dp)`.
- **Độ nổi (Elevation):** Rất nhẹ (1dp - 2dp) hoặc viền phẳng (Flat border 1dp với màu Soft Green). Không dùng bóng đổ đậm màu đen.
- **Bố cục thẻ:**
  - Icon/Hình ảnh thực phẩm thực tế, sắc nét ở góc trái/trên.
  - Tên món và hạn dùng ước tính rõ ràng; ngăn mát/ngăn đông được chọn bằng bộ lọc, không gắn nhãn vị trí riêng trên thẻ.
  - Trạng thái hạn dùng ước tính: Dùng nhãn màu Warm Beige / Terracotta.
  - Bộ điều khiển số lượng: Nút `[-]` và `[+]` ngay trên thẻ kèm số lượng hiện tại (ví dụ: `2 hộp`, `500g`).
- Nút `−`/`+` có đích chạm 48dp, ký tự 20sp và nền Soft Green; dòng `− để đánh dấu hết` căn giữa. “Sửa hạn ⏱️” chỉ hiện khi món đang có. Lưới giữ 88dp đệm dưới để chừa chỗ cho FAB.
- Quick-add gợi ý theo truy vấn không dấu, chỉ nhận diện duy nhất một món khi nhập tự do; chip chọn món giữ hạn “Theo gợi ý” theo user override hoặc catalog default.

### B. Bốn Khu Vực Chính (Bottom Navigation)

Thanh tab: icon nét + nhãn, **không có nền viên thuốc**; mục đang chọn tô `ctaText` (đậm), mục khác `muted`. Tên tab: Trang chủ, Tủ lạnh, Món ăn, **Cài đặt**.

Ảnh món: `FoodThumb` / `RecipeThumb` (`ui/components/FoodThumb.kt`) hiện ảnh thật nếu có trong `assets/food/<id>.webp|jpg|png` hoặc `assets/recipes/<id>...`, chưa có thì icon minh họa màu trong khung. Ảnh phải có bản quyền rõ ràng và đi kèm APK (chạy offline).
1. **🏠 Trang chủ (Home):**
   - Header: Lời chào ấm áp đổi theo buổi (`Chào buổi sáng`, `Chiều nay nấu gì?`) và tiêu đề "Hôm nay nhà mình ăn gì nhỉ?".
   - Thứ tự đọc: thanh "Trong tủ có N món" kèm nút "Xem tủ", dải cuộn ngang "Nên dùng trước" (ghi "Khoảng còn N ngày", tiêu đề mục nhắc hạn là ước tính), 3 thẻ gợi ý hôm nay (thẻ đầu nổi bật nền `SoftGreen`), thẻ phụ "Lên thực đơn cả tuần" ở cuối trang.
   - Thẻ gợi ý có lý do gợi ý dạng nhãn, thời gian và tên món.
2. **🧊 Tủ lạnh (Pantry):**
   - Một thanh chia đoạn **Tất cả | Ngăn mát | Ngăn đông** (nhãn xuống 2 dòng khi chữ lớn) và một dòng công tắc **Chỉ xem món đang có (N)**.
   - Mỗi món là một hàng: icon nét, tên, "Khoảng còn N ngày", nút `−`/`+` tròn 48dp; khi chữ phóng to từ 115% bộ đếm xuống dòng riêng. Bấm `−` khi còn 1 thì đánh dấu hết và hiện "Hoàn tác" vài giây.
   - FAB thêm thực phẩm ở góc phải dưới; cần vùng cuộn để người dùng tiếp cận được thẻ cuối.
   - Lưới thẻ thích ứng theo chiều rộng; font lớn có thể giảm số cột.
3. **🍲 Món ăn (Recipes):**
   - 3 món đề xuất thông minh:
     1. Món ưu tiên dùng đồ sắp hỏng.
     2. Món dùng đồ đang có sẵn trong tủ.
     3. Món nhanh gọn, đơn giản dưới 20 phút.
   - Trang chi tiết công thức 4 bước có checkbox đánh dấu đã làm.
4. **⚙️ Cá nhân (Settings):**
   - Nhắc thông báo, quyền riêng tư và sao lưu tệp offline.
   - Đơn vị chọn trên từng thẻ món ở Tủ lạnh, không đặt trong Settings.
   - Câu chữ hiển thị gọi là “tệp/bản sao lưu YourRefri”; JSON là chi tiết định dạng nội bộ.

---

## 4. Quy Tắc Chống "AI Slop"
- ❌ KHÔNG dùng gradient tím hồng vũ trụ.
- ❌ KHÔNG dùng card lồng trong card 3 tầng.
- ❌ KHÔNG dùng icon hoạt hình 3D sặc sỡ trẻ con.
- ❌ KHÔNG dùng từ ngữ công nghệ: "Hệ thống DB", "Đồng bộ hóa dữ liệu", "Phân tích AI".
- 🌿 DÙNG icon đường nét mộc mạc, hình ảnh đồ ăn Việt Nam ấm cúng, câu từ gần gũi như người nhà.
