> Persona giả lập (50 persona, dựa trên source walkthrough), không thay thế nghiên cứu người dùng thật. Lưu ngày 2026-11-16 theo quyết định Q22 trong `DECISIONS.md`.

# Cao kiến & Khảo sát 50 Persona: Mở rộng Tủ lạnh (Gia vị · Đồ ăn thừa · Thực phẩm khác)

> [!IMPORTANT]
> **Mức bằng chứng:** Khảo sát giả lập 50 persona dựa trên **source walkthrough** đối chiếu với `shelf_table.json` và `PantryGridScreen.kt` hiện tại (không phải dữ liệu nghiên cứu người dùng thật).

---

## I. Vấn đề cốt lõi nếu chỉ "nhồi thêm món" vào Tủ lạnh hiện tại

Hiện tại [shelf_table.json](../../composeApp/src/main/assets/shelf_table.json) và luồng trừ kho hiện tại được thiết kế cho **nguyên liệu tươi nấu bữa** (mua về → dùng → bấm "Nấu xong" là trừ khỏi tủ). Nếu đưa thẳng **gia vị, đồ ăn thừa, đồ uống, đồ ăn liền** vào cùng cơ chế đó sẽ vướng 3 lỗi UX lớn:

1. **Gia vị không thể "trừ 1 lần là hết":** Nấu 1 bữa chỉ tốn 1–2 thìa nước mắm / tương ớt / dầu hào; nếu bấm "Nấu xong" mà biến mất khỏi tủ thì sai thực tế.
2. **Đồ ăn thừa (cơm nguội, thịt kho còn dư) không cần công thức 4 bước:** Người dùng chỉ cần **nhắc ăn/hâm lại trong 1–2 ngày** và đánh dấu *"Đã ăn hết"*, không cần lọt vào danh sách đi chợ.
3. **Quá nhiều đồ lặt vặt làm rối màn hình:** Trộn 40 chai lọ gia vị + đồ uống với rau thịt tươi khiến lúc tìm món nấu tối bị ngợp.

---

## II. 4 Cao kiến thiết kế cho YourRefri (Giữ triết lý "Ít chạm, không rối")

### 1. Chia Tủ lạnh thành 4 nhóm theo "Nhịp tiêu thụ" (thay vì gom chung)
- **Nguyên liệu tươi** *(hiện tại)*: Thịt, cá, rau, củ, trứng, đậu — trừ tự động khi nấu xong, hiện ở **Cần dùng sớm**.
- **Gia vị & Sốt (Dùng nhiều lần):** Nước mắm, dầu ăn, tương ớt, dầu hào, bơ, sốt mè, sa tế...
  - **Cơ chế:** Mặc định đánh dấu **"Còn"** (hoặc tick 1 lần giữ lâu dài), **KHÔNG tự trừ** khi bấm "Nấu xong". Chỉ khi hết mới bấm 1 chạm **"Hết rồi → Thêm vào Đi chợ hàng ngày"**.
- **Đồ nấu sẵn / Đồ ăn thừa (Ưu tiên ăn ngay):** Cơm nguội, thịt kho dư, canh dư, dưa muối, kim chi...
  - **Cơ chế:** Có nút nhanh **"+ Đồ dư hôm nay"** (từ màn `Nấu xong` khi chọn "Còn dư" hoặc thêm tay 1 chạm, hạn mặc định 1–2 ngày). Tự hiện lên đầu dải **Cần dùng sớm** với nhãn *"Hâm lại ăn trước"* và nút 1 chạm **"Đã ăn hết"** (không đưa vào giỏ đi chợ).
- **Đồ ăn liền & Đồ uống:** Giò/chả lụa, xúc xích, há cảo đông lạnh, bánh mì, phô mai, sữa, nước ép...
  - **Cơ chế:** Có thể ăn ngay (gợi ý cho **Bữa sáng** hoặc **Ăn nhẹ**) mà không bắt buộc qua bước nấu phức tạp.

### 2. Nối thẳng luồng "Giữ lại đồ còn dư" với mục Đồ ăn thừa
Hiện tại khi nấu xong, người dùng có nút *"Chọn nguyên liệu không trừ (còn dư)"*. Có thể bổ sung thêm 1 lựa chọn rất tự nhiên: **"Còn dư món đã nấu (cất hộp ăn bữa sau)"** → tự tạo 1 thẻ trong nhóm *Đồ nấu sẵn / Ăn lại* với hạn 2 ngày (Ngăn mát) hoặc 14 ngày (Ngăn đông).

### 3. Tab / Bộ lọc gọn trong Tủ lạnh (Không bắt cuộn dài)
Thay vì liệt kê 100+ ô cùng lúc, đầu màn `Tủ lạnh` có các chip lọc gọn:
`[Đồ tươi] · [Đồ ăn thừa] · [Ăn liền & Uống] · [Gia vị]`
Mặc định mở ở **Đồ tươi** (và **Đồ ăn thừa** nếu có đồ sắp quá hạn), giữ đúng trải nghiệm nhanh 5 giây như cũ.

### 4. Tách bạch khỏi Giỏ đi chợ thông minh
- **Đồ ăn thừa:** Không bao giờ xuất hiện trong danh sách "Đi chợ" khi ăn hết.
- **Gia vị:** Chỉ vào giỏ **Đi chợ hàng ngày** khi người dùng chủ động bấm "Hết rồi".

---

## III. Kết quả Khảo sát 50 Persona về ý tưởng mở rộng này

> Câu hỏi khảo sát: *"Nếu YourRefri quản lý thêm Gia vị, Đồ ăn thừa và Thực phẩm khác (đồ ăn liền, đồ uống), bạn thấy hữu ích đến đâu, thích cơ chế nào nhất và sợ điều gì nhất?"*

### 1. Mức độ đồng thuận theo 5 nhóm (10 người/nhóm)
| Nhóm Persona | Tỷ lệ ủng hộ | Điểm kỳ vọng | Cơ chế được chọn nhiều nhất | Nỗi lo lớn nhất |
|---|---|---|---|---|
| **P01–P10:** Phụ huynh có con nhỏ | **90%** (9/10) | 8.8/10 | **Đồ ăn thừa tự nhắc trong 1–2 ngày** + **Bấm "Hết gia vị" đẩy vào giỏ chợ** | Sợ phải nhập từng chai gia vị ban đầu quá mất công |
| **P11–P20:** Người trẻ / Ở 1 mình / Ở ghép | **90%** (9/10) | 8.7/10 | **Đồ ăn liền (xúc xích, giò, há cảo)** ghép vào gợi ý **Bữa sáng 5 phút** | Sợ app biến thành app quản lý kho phức tạp, rối mắt |
| **P21–P30:** Người lớn tuổi (Exploratory) | **70%** (7/10) | 7.9/10 | **Nhắc đồ dư hôm qua / dưa cà muối** để không bị quên góc tủ | Sợ quá nhiều tab/nhóm nhỏ khó tìm nút |
| **P31–P40:** Gia đình có chế độ ăn riêng | **80%** (8/10) | 8.3/10 | **Theo dõi sốt/gia vị đặc thù** (nước tương chay, dầu ô-liu, sữa hạt) | Sợ gia vị mặn lẫn vào gợi ý bữa ăn chay |
| **P41–P50:** Hay quên / Đi chợ hộ | **90%** (9/10) | 8.8/10 | **1 chạm "Hết nước mắm/dầu ăn" → vào thẳng giỏ Đi chợ** | Sợ mất thời gian kiểm kê lọ gia vị còn bao nhiêu % |

### 2. Bảng tổng hợp bình chọn (50 Persona)
- **84% (42/50) ủng hộ mạnh** nếu tách rõ luồng *Đồ tươi*, *Đồ ăn thừa*, *Ăn liền & Uống*, và *Gia vị* — **nhưng 76% (38/50) đặt điều kiện: "Không bắt nhập chi tiết gram/ml cho gia vị hay đồ uống"**.
- **Top 4 tính năng được mong muốn nhất khi mở rộng:**
  1. **Mục "Đồ ăn thừa / Nấu sẵn" nhắc ăn trong 1–2 ngày + nút "Đã ăn hết"** — **78%** (39/50)
  2. **Mục "Gia vị cơ bản" mặc định có sẵn, chỉ bấm "Hết rồi → Đi chợ" (không tự trừ khi nấu)** — **74%** (37/50)
  3. **Nhóm "Ăn liền & Đồ uống" (Giò chả, Xúc xích, Há cảo, Sữa, Bánh mì) dùng cho Bữa sáng / Cơm lười** — **68%** (34/50)
  4. **Lưu trực tiếp món vừa nấu còn dư thành thẻ "Đồ ăn thừa" ngay lúc bấm Hoàn thành** — **62%** (31/50)
- **Top 3 ranh giới "KHÔNG ĐƯỢC LÀM" theo cảnh báo của Persona:**
  1. **Không bắt người dùng tick lại 20 lọ gia vị ngay từ đầu** (nên giả định gia vị phổ biến mặc định "đang có", chỉ đánh dấu khi hết) — **80%** (40/50)
  2. **Không đưa gia vị dùng lâu (nước mắm, tương ớt) vào dải cảnh báo "Cần dùng sớm"** — **74%** (37/50)
  3. **Không làm trôi mất danh sách Đồ tươi** (đồ tươi vẫn là mặc định đầu tiên khi mở Tủ lạnh) — **70%** (35/50)

---

## IV. Lộ trình triển khai gọn nếu bạn muốn làm

1. **Bước 1 (Ít chạm nhất, giá trị cao nhất):** Thêm mục **Đồ ăn thừa / Hâm lại** (tạo nhanh từ nút *"Cất phần dư bữa này"* sau khi nấu hoặc thêm tay 1 chạm) + đẩy thẳng vào dải **Cần dùng sớm** trên Trang chủ.
2. **Bước 2:** Thêm nhóm **Gia vị nền** (mặc định "Còn", không tự trừ, bấm 1 chạm `"Hết → Đi chợ"`).
3. **Bước 3:** Bổ sung nhóm **Đồ ăn liền & Đồ uống** (Giò lụa, Xúc xích, Há cảo, Bánh mì, Phô mai, Sữa hạt...) và vài món ăn sáng/nhanh 5 phút đi kèm.
