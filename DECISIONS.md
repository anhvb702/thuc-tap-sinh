# DECISIONS

Mỗi quyết định là một ADR: **Bối cảnh · Phương án · Quyết định · Hệ quả**. Trạng thái: `Đã chốt` | `Đề xuất` | `[CẦN BẠN QUYẾT]`.
Quy trình: Opus nêu vấn đề → Brainstormer đưa phương án và phản biện (tối đa 3 vòng) → Opus chốt → ghi vào đây → bạn duyệt.

---

## Đã chốt (từ trao đổi với bạn)

### ADR-001 Đối tượng người dùng
- **Quyết định:** dân văn phòng và freelancer.
- **Hệ quả:** giọng trợ lý ấm áp, hơi dí dỏm, chuyên nghiệp. Tín hiệu kiệt sức chỉ để gợi ý nghỉ nhẹ, không chẩn đoán.
- Trạng thái: Đã chốt.

### ADR-002 Nền tảng
- **Quyết định:** Android trước. iOS chỉ làm nếu có Mac. Dùng Compose Multiplatform để giữ đường đi sang iOS.
- **Lý do:** bạn dùng Windows, không build iOS cục bộ được.
- Trạng thái: Đã chốt.

### ADR-003 Không backend ở v1
- **Quyết định:** dữ liệu lưu trên máy (SQLDelight), không tài khoản, không đồng bộ. Ngoại lệ duy nhất: proxy Cloudflare Workers cho Jev (tuần 4, tùy chọn).
- Trạng thái: Đã chốt.

### ADR-004 Vai trò của Jev
- **Quyết định:** Jev chỉ là lớp tăng cường (chọn tông lời nhắn, chấm độ khó, gắn nhãn) và công cụ định tuyến trong quy trình. Mọi tính năng chạy được khi không có Jev. Ghim `jev-1.13.0`. Chỉ gửi dữ liệu khi người dùng bật công tắc đồng ý.
- Trạng thái: Đã chốt. Đo thử với tiếng Việt ở tuần 4, cắt nếu kém.

### ADR-005 Không tạo file thừa
- **Quyết định:** chỉ giữ `CLAUDE.md`, `DECISIONS.md`, `.claude/agents/brainstormer.md`, `tools/jev-gate`, `.gitignore`. Bảng việc ở Trello.
- Trạng thái: Đã chốt.

---

## Kết quả tranh luận vòng 1 (Opus ↔ Brainstormer)

Trạng thái `Đề xuất` = Opus đã chốt sau khi cân nhắc phản biện, chờ bạn duyệt. Mục có **[CẦN BẠN QUYẾT]** phụ thuộc lựa chọn sản phẩm của bạn.

### ADR-006 Phạm vi MVP — **Đã chốt** (bạn đồng ý cắt streak)
- **Phản biện:** checklist "Hôm nay" đầy đủ thực chất là làm thêm app todo (việc tồn, sửa, ngày), người dùng đã có Todoist/Jira nên phải nhập hai lần; việc chưa tick gây cảm giác tội lỗi; streak đứt gây tội lỗi.
- **Quyết định:** lấy hướng *log-first lai tối giản*. Lõi v1: **thêm nhanh thành quả** (một ô nhập, gợi ý từ lịch sử) + danh sách "định làm hôm nay" kiểu dùng xong bỏ (không hạn chót, không chuyển sang hôm sau, việc chưa xong tự biến mất lúc nửa đêm; tick xong thì thành thành quả) + **recap sáng** + **tổng kết tuần** + cài đặt giờ nhắc. **Cắt hẳn streak.**
- **Hệ quả:** ít màn hình, bớt một phần logic ngày. Dữ liệu chuyển máy qua `dataExtractionRules` (xem ADR-013).
- **Kiểm chứng:** hỏi 5–10 người dùng thật ở beta (tuần 3) xem họ có dùng công cụ todo khác không; nếu đa số không, cân nhắc nâng danh sách "hôm nay" lên đầy đủ ở v1.1.

### ADR-007 Tính cách trợ lý — **Đã chốt**
- **Đã chốt với bạn:** tên app **MakeYourDay**; trợ lý mặc định tên **Luna**, người dùng tự đặt lại tên được; xưng hô mặc định "mình - bạn" (đổi được trong cài đặt); giọng ấm áp, dí dỏm nhẹ.
- **Phản biện:** 3 giọng × 5 ngữ cảnh × 10 biến thể ≈ 150 mẫu viết tay; giọng dí dỏm tiếng Việt rất khó viết hay; vấn đề thật là cách xưng hô, không phải số giọng.
- **Quyết định:** **một giọng làm thật tốt** (ấm áp, dí dỏm nhẹ), người dùng **chọn cách xưng hô** (ví dụ bạn/mình, anh/em, chị/em), **2 mức độ dài** (ngắn gọn / trò chuyện). Mẫu có chỗ trống (slot) và nhãn (tông, ngữ cảnh), có chống lặp (không dùng lại một mẫu trong N ngày). Mục tiêu khoảng 40 mẫu cho v1. Giữ trường "tông" trong dữ liệu để thêm giọng ở v1.1.
- **Jev:** chọn mẫu theo ngữ cảnh; không có Jev thì chọn ngẫu nhiên có trọng số.
- **Bạn cần chọn:** tên mặc định của trợ lý, bộ xưng hô được hỗ trợ, và mức dí dỏm.

### ADR-008 Kiến trúc — Đề xuất
- **Phản biện:** ranh giới tầng chỉ là quy ước nên agent dễ import chéo; domain cho mọi feature sinh use case một dòng; MVI "nhẹ" không có mẫu thì mỗi agent viết một kiểu; Koin lỗi lúc chạy.
- **Quyết định:** 2 module thư viện KMP (`:shared` và `:composeApp`). ViewModel và UI ở `:composeApp`; data và logic recap ở `:shared`. `:shared` **không** phụ thuộc Compose, nên ranh giới `ui` không import `data` được trình biên dịch giữ chứ không chỉ là quy ước. `:composeApp` phụ thuộc `:shared`, không ngược lại.
- **Chốt 2026-11-04 (lệch wizard):** wizard sinh `androidApp`, `iosApp`, `shared` (UI nằm trong `shared`). `androidApp` và `iosApp` chỉ là vỏ khởi chạy mỏng (Activity / app iOS), không tính vào hai module trên và không chứa logic. Vì AGP dùng plugin `androidMultiplatformLibrary` cho thư viện, `:composeApp` là thư viện KMP chứ không phải application; `androidApp` phụ thuộc `:composeApp`. Việc tách Compose khỏi `:shared` và tạo `:composeApp` làm trong một thẻ "tích hợp" riêng. Chỉ có tầng domain cho `recap` và `assistant`, các feature CRUD đi thẳng ViewModel → repository. Một file mẫu MVI (`State`, `Action`, `Effect`) để agent sao chép. Thêm test kiểm tra đồ thị Koin vào CI.
- **Test kiến trúc** (cấm `ui` import `data`): thử Konsist hoặc rule detekt tự viết (hỗ trợ KMP chưa kiểm chứng). Giới hạn **nửa ngày** ở tuần 1; quá thì bỏ và dựa vào Reviewer + quy ước package.

### ADR-009 Điều hướng — Đề xuất
- **Quyết định:** Navigation Compose type-safe (bản multiplatform), **với điều kiện:** tham số route chỉ dùng kiểu nguyên thủy (id `Long`, ngày dạng chuỗi ISO), tất cả route đặt trong một file.
- **Spike bắt buộc ngày 1–2 tuần 1:** chạm thông báo → mở `RecapRoute(date)`, cả khi app đang chạy lẫn khi đã bị kill. Nếu spike thất bại hoặc cần workaround quá nửa ngày thì chuyển sang tự viết (sealed class `Screen` + back stack bằng `StateFlow`).

### ADR-010 Quy ước — Đề xuất; `applicationId` đã chốt: **`com.loitu.makeyourday`**
- **Quyết định:** squash merge; **chỉ tiêu đề PR** theo Conventional Commits và CI kiểm tra, commit trong nhánh để tự do. Nhánh `feat/<mã-thẻ>-<slug>`. **Một công cụ lint** (chọn ở bước dựng khung), bật baseline và chỉ các rule quan trọng. Package `com.<id>.<app>.feature.<tên>.{data,ui}`.
- **Bổ sung:** PR template có tiêu chí chấp nhận lấy từ thẻ; CI chạy unit test và task verify migration SQLDelight (tên task chưa kiểm chứng); trần khoảng 400 dòng diff mỗi thẻ, vượt thì tách.
- **Bạn cần chọn ngay:** `applicationId` (ví dụ `com.<tên-bạn>.<tên-app>`), vì gần như không đổi được sau khi lên Play.

### ADR-011 Test và Definition of Done — Đề xuất
- **Phản biện:** lỗi nguy hiểm nhất nằm ở thông báo bị Doze chặn, migration làm mất dữ liệu và ngày sai khi đổi múi giờ, không nằm ở logic recap; DoD cũ có điểm không kiểm được; Reviewer cùng họ mô hình với agent viết nên dễ mù cùng chỗ.
- **Quyết định:** test domain + test SQLDelight bằng driver in-memory + verify migration + **tiêm `Clock`/`TimeZone`** (kotlinx-datetime) để test DST và nửa đêm. Tiêu chí hoàn thành trong thẻ viết thành **tên test cụ thể**. Bước chạy emulator chỉ áp dụng cho thẻ UI, do bạn làm, kèm ảnh chụp màn hình. Cấm xóa hoặc `@Ignore` test cũ nếu thẻ không ghi rõ; CI đánh dấu diff xóa test. Chưa dùng Maestro/E2E ở v1.
- **DoD một thẻ:** CI xanh · test đặt tên khớp tiêu chí · Reviewer đồng ý · thẻ UI có ảnh chụp từ emulator · PR về thông báo, thời gian, migration do bạn đọc kỹ.

### ADR-012 Quyền hạn agent — **Đã chốt** (bạn đồng ý Jev chỉ tư vấn trong quy trình)
- **Phản biện:** quy tắc trong prompt không cưỡng chế được gì; agent bị ép CI xanh sẽ sửa test thay vì sửa lỗi; cấm sửa Gradle làm bạn thành nút cổ chai; đưa mô hình bên thứ ba chưa hiệu chỉnh vào vòng quản trị là thêm rủi ro.
- **Quyết định:** cưỡng chế bằng công cụ: job CI "path guard" fail nếu PR đụng vào đường dẫn được bảo vệ mà thiếu nhãn; CODEOWNERS; branch protection; **môi trường agent không có secrets.** Hai tầng quyền: `libs.versions.toml` sửa được với nhãn nhẹ; CI, secrets, `DECISIONS.md`, `.gitignore` chỉ bạn sửa. Các file điểm nóng (DI module, navigation graph, strings, schema `.sq`) được tuần tự hóa qua thẻ "tích hợp". Không agent nào merge hoặc kéo thẻ sang Done.
- **Điều chỉnh vai trò Jev trong quy trình (đổi so với kế hoạch trước):** `jev-gate` chỉ **ghi chú tư vấn** (thẻ đã sẵn sàng chưa, thẻ có quá to không), **không định tuyến và không chặn**. Làm sau, nếu còn thời gian, như một thử nghiệm nhỏ.

### ADR-013 Quyền riêng tư và lưu trữ — Đề xuất
- **Quyết định:** DB cục bộ không mã hóa ở v1 (sandbox Android đã che phần lớn rủi ro); SQLCipher để sau.
- **Bổ sung theo phản biện:**
  - Backup: dùng `dataExtractionRules` (Android 12+): loại khỏi cloud backup nhưng cho chuyển trực tiếp từ máy sang máy.
  - Crash: không SDK bên thứ ba; dùng **Android vitals** trên Play Console.
  - Thông báo: mặc định hiển thị nội dung chung chung trên màn hình khóa, có tùy chọn hiện chi tiết.
  - Xuất dữ liệu: có cảnh báo rõ vì file không mã hóa; có chức năng xóa toàn bộ dữ liệu.
  - Jev: câu việc có thể chứa tên khách hàng và đồng nghiệp, nên không gọi là "không định danh". Có màn hình **xem trước đúng nội dung sẽ gửi**; proxy Cloudflare không ghi log và giữ tối thiểu; khai báo trong Data safety.
- **Xem lại nếu:** nhóm mục tiêu là freelancer ghi việc khách hàng có NDA, khi đó cân nhắc SQLCipher.

### ADR-014 Thông báo — Đề xuất
- **Phản biện:** WorkManager không sinh ra cho việc "đúng giờ trong ngày" (trôi giờ, bị dồn trong Doze).
- **Quyết định:** `AlarmManager` không chính xác (`setAndAllowWhileIdle`/`setWindow`), **không dùng exact alarm** (rủi ro quyền và chính sách Play). Tự xử lý lên lịch lại sau khởi động máy (`BOOT_COMPLETED`), đổi múi giờ, chỉnh giờ. Nội dung recap sinh lúc gửi bằng truy vấn nhanh. Xin quyền `POST_NOTIFICATIONS` (Android 13+). **Mở app luôn thấy recap**, dù thông báo bị lỡ.
- **Giọng điệu và tần suất:** tối đa 1 thông báo sáng mỗi ngày; mặc định bỏ cuối tuần; nhắc tối mặc định tắt. Thông điệp "bắt đầu lại" giãn dần: sau 3 ngày không có dữ liệu thì giảm tần suất, sau 7 ngày thì dừng và nói rõ là đã dừng.
- **Kiểm chứng:** thử trên 2–3 máy thật (có máy Xiaomi/Oppo vì hay diệt tác vụ nền), kèm hướng dẫn tắt tối ưu pin. Nếu WorkManager trễ không quá 15 phút trên các máy đó thì cân nhắc lại.
