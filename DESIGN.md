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

### Token tương phản W4-03

Giữ các giá trị đồng bộ với `composeApp/.../ui/theme/Theme.kt` và `AccessibilityContrastTest`: `DeepCharcoal #343B34`, `MutedGray #596057`, `SageGreenText #536C51`, `TerracottaText #8C4A35` trên bề mặt sáng; nút chính dùng `SageGreenStrong #506950` với chữ trắng. `SageGreen #789477` là màu thương hiệu, không phải màu chữ chính.

---

## 2. Typography & Khoảng Cách (Spacing)

- **Font chữ:** Hỗ trợ tiếng Việt đầy đủ, bo tròn nhẹ hoặc humanist sans-serif (Inter / Roboto / Nunito).
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
1. **🏠 Trang chủ (Home):**
   - Header: Lời chào ấm áp đổi theo buổi (`Chào buổi sáng`, `Chiều nay nấu gì?`).
   - Widget tổng quan: Số món đang có + 1 dải cuộn ngang các món cần dùng gấp.
   - 3 thẻ món ăn gợi ý hôm nay (lý do gợi ý, thời gian, tên món, trạng thái nấu được và nguyên liệu sẵn có/thiếu).
2. **🧊 Tủ lạnh (Pantry):**
   - Bộ lọc **Tất cả món**, **Ngăn mát**, **Ngăn đông** và công tắc **Chỉ xem món đang có**.
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
