# Báo Cáo Thẩm Định Trải Nghiệm Khách Hàng Vòng 5 (Round 5 UX Evaluation) — 2026-11-09
## Đánh Giá Trải Nghiệm Tính Năng: "Vòng Lặp Thực Đơn Tuần & Đi Chợ 1 Chạm" (Zero-Input Pantry Loop)

> **Phương pháp & Mức bằng chứng (Evidence Level):**
> - **Phương pháp:** Persona Live Emulator Walkthrough & Source Inspection do Đội ngũ Khách hàng Agent (**Chị Mai, Anh Nam, Cô Hạnh**) thực hiện trên bản dựng Android thật (`com.loitu.yourrefri` chạy trên Emulator API 36).
> - **Bằng chứng thu thập:** 4 ảnh chụp màn hình tương tác thực tế lưu tại `build/ui_test/persona/` (`01_home.png`, `02_weekly_plan.png`, `03_expanded_steps.png`, `04_shopping_list.png`) cùng log tương tác và kết quả kiểm thử đơn vị tự động.
> - *Lưu ý: Báo cáo phản ánh góc nhìn giả lập theo tiêu chuẩn Persona gia đình Việt Nam (`.agents/skills/customer-ux-evaluation/SKILL.md`), không thay thế khảo sát phỏng vấn người dùng thực tế.*

---

## 1. Bối Cảnh & Bài Toán Thực Tế Cần Giải Quyết

Từ góp ý của người dùng/mentor tại PR #5:
> *"Vợ anh từng dùng app quản lý tủ lạnh nhưng sau một thời gian đã bỏ vì phải nhập và cập nhật dữ liệu quá nhiều mỗi lần đi chợ hoặc nấu ăn... Vấn đề lớn nhất hàng ngày là câu hỏi 'Hôm nay ăn gì, cả tuần ăn gì khi mỗi người trong nhà thích một kiểu?'."*

Phiên bản `W6-01` & `W6-02` đã đảo ngược luồng truyền thống: **Lên thực đơn tuần trước → Tự động tính đồ cần mua → Đi chợ về bấm 1 chạm cất vào tủ lạnh (Zero-typing)**.

---

## 2. Đánh Giá Trải Nghiệm Chi Tiết Theo Từng Persona

### 👩 Persona 1: Chị Mai (35 tuổi, mẹ bận rộn 2 con nhỏ, phụ trách chính chợ & bếp)

#### ✅ Điều đã làm được & Rất hài lòng:
1. **Giải quyết triệt để nỗi sợ nhập liệu thủ công:**
   - Không còn cảnh đi làm về mệt mỏi phải mở app gõ từng cọng hành, lạng thịt, quả trứng.
   - Khi đi siêu thị hoặc chợ về, chỉ cần mở mục **🛒 Danh sách đi chợ tuần này**, bấm nút **`"Đã mua xong (n món) · Cất vào tủ lạnh 🧊"`**, toàn bộ nguyên liệu vừa mua lập tức được nạp vào tủ lạnh trong đúng **1 chạm**. Chị kiểm tra sang tab Tủ lạnh thì thấy ngay trạng thái *"Có trong tủ"* kèm hạn dùng ước tính chuẩn.
2. **Lên thực đơn cho cả tuần chỉ mất 30 giây:**
   - Chị Mai chỉ cần chọn chip khẩu vị: `Món dễ ăn (trứng, gà, khoai)` cho 2 bé và `Cá & Tôm` cho cả nhà. Hệ thống tự động phân bổ 7 món ăn ngon miệng từ Thứ 2 đến Chủ nhật, không ngày nào bị trùng món với ngày trước đó.
3. **Thông minh trong việc tận dụng đồ có sẵn:**
   - Đầu tuần (Thứ 2, Thứ 3), thực đơn tự động ưu tiên các món tận dụng nguyên liệu sắp hết hạn hoặc đang có trong tủ (ví dụ: Canh chua cá basa khi trong tủ có sẵn cá basa và cà chua).
   - Danh sách đi chợ tự động trừ đi các món đã có sẵn trong tủ (`alreadyInPantry`), và hiển thị dòng chữ rõ ràng: *"Đã có sẵn trong tủ: Thịt heo, Cà chua..."* giúp chị không bị mua trùng.

#### ⚠️ Điều còn khúc mắc & Mong muốn cải thiện:
- **Định lượng nguyên liệu khi đi chợ:** Danh sách hiện ghi *"Hành lá: Dùng cho 3 bữa"*, *"Cà chua: Dùng cho 2 bữa"*. Chị Mai thấy số bữa rất hay, nhưng khi đứng ở quầy rau siêu thị chị vẫn phải tự nhẩm xem 3 bữa hành lá là mua 1 bó hay 2 bó. Chị mong muốn trong tương lai có thêm gợi ý định lượng mộc mạc (ví dụ: *1 bó nhỏ*, *3 quả*).
- **Trạng thái khi đã mua đủ:** Nút đổi thành *"Tuần này nhà mình đã đủ nguyên liệu 🎉"* rất dễ thương và tạo cảm giác nhẹ nhõm, an tâm.

---

### 👨 Persona 2: Anh Nam (38 tuổi, người chồng cùng chia sẻ việc nấu nướng)

#### ✅ Điều đã làm được & Rất hài lòng:
1. **Đổi món linh hoạt mà không vỡ kế hoạch cả tuần:**
   - Hôm nào anh về muộn không kịp nấu món 35 phút, anh bấm nút **`"Đổi món ↻"`** ngay trên thẻ ngày hôm đó. Hệ thống lập tức xoay sang một món khác nhanh hơn, trong khi các ngày khác trong tuần vẫn giữ nguyên vẹn.
2. **Xem nhanh cách nấu 4 bước ngay tại chỗ:**
   - Chạm vào tên món trên thẻ ngày là bung ngay 4 bước nấu chuẩn cơm nhà: *1. Chuẩn bị | 2. Sơ chế | 3. Chế biến | 4. Hoàn thành*. Anh Nam không cần phải bấm chuyển qua lại giữa các màn hình, vừa cầm điện thoại vừa nấu rất tiện.
3. **Thời gian nấu rõ ràng:**
   - Thẻ ngày nào cũng có thời gian nấu ước tính (`15 phút`, `20 phút`...), giúp anh chủ động canh giờ cắm cơm và đón con.

#### ⚠️ Điều còn khúc mắc & Mong muốn cải thiện:
- **Cập nhật sau khi nấu xong từ thẻ tuần:** Ở tab *Gợi ý hôm nay*, sau khi nấu xong có nút *"Đã nấu xong món này 🎉"* để tự động giảm nguyên liệu trong tủ. Ở thẻ *Thực đơn tuần*, anh bấm xem 4 bước nấu rất thích nhưng chưa thấy nút *"Đã nấu xong"* tích hợp ở chân thẻ để trừ đồ luôn. Anh mong muốn thẻ tuần cũng có nút này để nấu xong ngày nào bấm ngày đó.

---

### 👵 Persona 3: Cô Hạnh (62 tuổi, mẹ lớn tuổi trong gia đình, góc nhìn Trợ năng & Sự ấm áp)

#### ✅ Điều đã làm được & Rất hài lòng:
1. **Vùng chạm lớn, không sợ bấm trượt:**
   - Toàn bộ các chip định hướng (`Cân bằng gia đình`, `Thanh đạm · Nhiều rau`...), chip khẩu vị (`Cá & Tôm`, `Rau, Đậu & Nấm`...) và các ô tích chọn đều có chiều cao `>= 48.dp`. Cô Hạnh mắt kém, tay hơi run nhưng chạm rất chuẩn xác, không bị bấm nhầm.
2. **Văn phong ấm áp, không áp lực:**
   - Cô rất ưng vì app không dùng những từ gây áp lực như *"Bạn bỏ quên"*, *"Quá hạn"*, *"Lãng phí"* hay các thuật ngữ tiếng Anh. Lời nhắn *"Chọn định hướng và khẩu vị, mình lo phần còn lại."* và *"Tuần này nhà mình đã đủ nguyên liệu 🎉"* tạo cảm giác mộc mạc, gần gũi như một người trợ lý nhỏ thật sự trong nhà.
3. **Màu sắc dịu mắt:**
   - Tông màu be `WarmIvory` kết hợp xanh lá xám `SageGreen` và chữ đen than `DeepCharcoal` tương phản rõ ràng, đọc lâu không bị mỏi mắt.

#### ⚠️ Điều còn khúc mắc & Mong muốn cải thiện:
- **Cuộn trang khá dài:** Màn hình gồm cả 7 ngày trong tuần và danh sách đi chợ bên dưới. Trên điện thoại của cô, phải vuốt ngón tay 3-4 lần mới tới được danh sách đi chợ. Cô mong muốn ở đầu màn hình có thêm một nút nhảy nhanh xuống danh sách đi chợ (hoặc thanh phân đoạn ngắn) để người lớn tuổi xem danh sách đi chợ ngay khi cần.

---

## 3. Tổng Hợp Bảng Điểm Thẩm Định UX

| Tiêu chuẩn UX Gia Đình | Điểm Đánh Giá | Nhận Xét Của Đội Ngũ Khách Hàng |
| :--- | :---: | :--- |
| **1. Giải quyết câu hỏi "Hôm nay ăn gì?"** | **9.5 / 10** | Rất xuất sắc. Lên sẵn thực đơn 7 ngày không trùng lặp, giải phóng hoàn toàn gánh nặng suy nghĩ mỗi buổi chiều. |
| **2. Tối giản nhập liệu (Zero-typing)** | **9.5 / 10** | Đột phá lớn nhất so với bản cũ: đi chợ về chỉ cần 1 chạm cất vào tủ, không cần gõ bàn phím. |
| **3. Khả năng cá nhân hóa theo gia đình** | **9.0 / 10** | Dễ dàng cân bằng sở thích người lớn (ăn thanh đạm/cá) và trẻ nhỏ (món dễ ăn) qua hệ thống tag khẩu vị. |
| **4. Tính linh hoạt thực tế (Đổi món)** | **9.0 / 10** | Đổi món từng ngày độc lập mượt mà, không làm xáo trộn các ngày khác. |
| **5. Văn phong & Cảm xúc (Empathy)** | **10 / 10** | Hoàn toàn ấm áp, tôn trọng, không có yếu tố phán xét hay giục giã. |
| **6. Trợ năng & Thao tác (Accessibility)** | **8.5 / 10** | Vùng chạm đạt chuẩn 48dp, màu tương phản tốt. Cần tối ưu thêm thao tác cuộn trên màn hình dài. |

---

## 4. Đề Xuất Ưu Tiên Cho Các Vòng Cải Thiện Tiếp Theo (Backlog)

1. **Ưu tiên 1 (Nấu xong từ thẻ tuần):** Bổ sung nút *"Đã nấu xong món này"* ngay bên trong 4 bước nấu của thẻ tuần để đồng bộ luồng trừ nguyên liệu trong tủ mà không cần chuyển sang tab khác.
2. **Ưu tiên 2 (Điều hướng nhanh trong màn hình dài):** Thêm nút nhảy nhanh hoặc ghim nhẹ mục *"🛒 Xem danh sách đi chợ"* để người dùng lớn tuổi như Cô Hạnh không phải cuộn nhiều lần.
3. **Ưu tiên 3 (Gợi ý định lượng thân thiện):** Bổ sung gợi ý quy cách mua thông dụng (ví dụ: *bó, quả, khay*) bên cạnh số bữa sử dụng.
