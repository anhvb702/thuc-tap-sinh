# YourRefri

Ngôn ngữ chung cho tủ lạnh gia đình, công thức và thực đơn tuần. Chỉ ghi thuật ngữ đặc thù của dự án; không ghi chi tiết cài đặt.

## Tủ lạnh

**Nguyên liệu**:
Một loại thực phẩm có trong danh mục (ví dụ "Trứng gà"). Có nhóm, hạn ước tính mặc định, và có thể có hạn ước tính khi cấp đông.
_Avoid_: món, mặt hàng, sản phẩm

**Mục trong tủ**:
Một nguyên liệu đang có trong tủ của gia đình, kèm số lượng, đơn vị, ngày đánh dấu có, và ngăn đang đặt.
_Avoid_: thẻ (khi nói về dữ liệu; "thẻ" chỉ là hình thức hiển thị)

**Số lượng**:
Số đơn vị của một mục trong tủ. `+`/`−` tăng hoặc giảm 1 đơn vị; chạm vào con số để gõ số tùy ý.
_Avoid_: số phần

**Đơn vị**:
Cách đếm của một mục trong tủ (quả, bó, miếng, lạng/g, khay). Chọn riêng cho từng mục.

**Ngăn**:
Nơi một mục trong tủ được đặt: **Ngăn mát** hoặc **Ngăn đông**. Thuộc về mục trong tủ, không thuộc nguyên liệu.
_Avoid_: khu, tủ con

**Hạn ước tính**:
Ngày sau đó nên kiểm tra lại mục trong tủ. Luôn là ước tính, không khẳng định an toàn thực phẩm. Tính từ ngày đánh dấu có, không lưu trực tiếp.
_Avoid_: hạn dùng, hết hạn

**Có / Hết**:
Trạng thái lưu lâu dài của mục trong tủ. Hết hạn ước tính không phải trạng thái lưu; nó là phép tính từ ngày.

## Nấu

**Công thức**:
Một cách nấu gồm nguyên liệu bắt buộc, nguyên liệu tùy chọn, các bước và thời gian. Có hai nguồn: công thức gốc (chỉ đọc) và món gia đình tự thêm (R5, lưu trong DB).
_Avoid_: món, món ăn

**Nấu xong**:
Một lần nấu một công thức, gắn với một bữa và một ngày. Tạo dòng lịch sử và có thể trừ nguyên liệu khỏi tủ. Lựa chọn "còn dư" tại thời điểm này là thao tác, không phải trạng thái.
_Avoid_: hoàn thành, đã làm

**Hoàn tác**:
Khôi phục tủ và lịch sử về trước đúng một lần Nấu xong vừa xảy ra. Chỉ có trong khoảng 5 giây sau khi bấm; sau đó không còn.

**Còn dư**:
Lựa chọn tại thời điểm Nấu xong để giữ lại một phần nguyên liệu, thay vì trừ hết. Không phải trạng thái lưu lâu dài.

**Đồ ăn thừa**:
Một món đã nấu còn lại, được cất vào tủ như một mục riêng với hạn ngắn. Khác với nguyên liệu còn dư.
_Avoid_: đồ dư (khi nói về món đã nấu)

**Lịch sử**:
Danh sách các lần Nấu xong, nhóm theo tuần, kèm bữa. Chỉ liệt kê món, không thống kê.

**Bữa**:
Trưa hoặc Tối trong một ngày. Dùng để gắn với Nấu xong và thực đơn tuần.

## Thực đơn và đi chợ

**Thực đơn tuần**:
Kế hoạch bữa ăn cho bảy ngày, Thứ 2 đến Chủ nhật, theo thiết lập tuần. Đã xếp thì giữ nguyên cho đến khi sang tuần mới hoặc người dùng bấm đổi.

**Thiết lập tuần**:
Định hướng bữa ăn, khẩu vị gia đình và các lựa chọn của tuần đó (lần đổi món, mục đã bỏ tick).

**Danh sách đi chợ**:
Các nguyên liệu cần mua cho thực đơn tuần, đã trừ những gì đang có trong tủ.

**Cần mua thêm**:
Mục người dùng đánh dấu "Hết rồi" và cần mua, không gắn với thực đơn tuần (R2-02).

## Quan hệ chính

- Một **Mục trong tủ** tham chiếu một **Nguyên liệu**.
- Một **Công thức** cần nhiều **Nguyên liệu** (bắt buộc hoặc tùy chọn).
- Một **Nấu xong** tham chiếu một **Công thức**, một **Bữa** và một ngày; có thể trừ **Mục trong tủ**.
- Một **Thực đơn tuần** gồm nhiều **Công thức** theo ngày và bữa, cùng một **Thiết lập tuần**.
- **Danh sách đi chợ** được tính từ **Thực đơn tuần** và **Mục trong tủ**.
