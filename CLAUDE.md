# MakeYourDay

App Kotlin Multiplatform (Compose Multiplatform), Android trước. Mỗi sáng trợ lý **Luna** (người dùng đặt lại tên được) kể lại những việc đã hoàn thành hôm trước cho dân văn phòng và freelancer. Một người làm trong 5 tuần. Chi tiết kế hoạch ở `PLAN.md`, quyết định ở `DECISIONS.md` (đọc ADR trước khi đề xuất đổi kiến trúc).

`applicationId`: `com.loitu.makeyourday` (đã chốt, không đổi).

## Ràng buộc cứng
- Dữ liệu chỉ nằm trên máy (SQLDelight). Không tài khoản, không backend, không analytics/crash SDK bên thứ ba ở v1.
- Jev (TypeSafe AI) chỉ là lớp tăng cường tùy chọn: mọi tính năng phải chạy khi không có Jev. Ghim `jev-1.13.0`. Gọi qua proxy, không để API key trong app. Chỉ gửi dữ liệu khi người dùng bật công tắc và đã xem trước nội dung gửi.
- Không chẩn đoán sức khỏe tinh thần. Tín hiệu kiệt sức chỉ để gợi ý nghỉ nhẹ.
- Không có streak. Không dùng ngôn ngữ gây tội lỗi.
- Không tạo file ngoài danh sách cần thiết nếu chưa hỏi người dùng.

## Kiến trúc (ADR-008, 009)
- Hai module: `:shared` (data, logic recap) và `:composeApp` (UI, ViewModel). Tầng domain chỉ cho `recap` và `assistant`.
- Package: `<applicationId>.feature.<tên>.{data,ui}`. `ui` không import `data`.
- MVI: `State`, `Action`, `Effect`, ViewModel + StateFlow. Sao chép từ file mẫu, không tự chế kiểu khác.
- Điều hướng: Navigation Compose type-safe; tham số route chỉ dùng kiểu nguyên thủy (id `Long`, ngày dạng chuỗi ISO); mọi route đặt trong một file.
- Thời gian: luôn tiêm `Clock`/`TimeZone` (kotlinx-datetime), không gọi giờ hệ thống trực tiếp.

## Thông báo (ADR-014)
`AlarmManager` không chính xác (`setAndAllowWhileIdle`/`setWindow`), không exact alarm. Xử lý lên lịch lại sau khởi động máy, đổi múi giờ. Tối đa 1 thông báo sáng mỗi ngày. Mở app luôn thấy recap.

## Quy ước (ADR-010, 011, 012)
- Nhánh `feat/<mã-thẻ>-<slug>`; mỗi thẻ một PR, tối đa khoảng 400 dòng diff; squash merge; tiêu đề PR theo Conventional Commits.
- Tiêu chí hoàn thành của thẻ viết thành tên test cụ thể. Không xóa hoặc `@Ignore` test cũ trừ khi thẻ ghi rõ.
- DoD: CI xanh · test khớp tiêu chí · Reviewer đồng ý · thẻ UI có ảnh chụp từ emulator · PR về thông báo, thời gian, migration do người dùng đọc kỹ.
- Không sửa CI, secrets, `.gitignore`, `DECISIONS.md` nếu không có thẻ nhãn "Cần Duyệt". `libs.versions.toml` sửa được với nhãn nhẹ. Các file điểm nóng (DI module, nav graph, strings, schema `.sq`) chỉ sửa qua thẻ "tích hợp". Agent không merge và không kéo thẻ sang Done.
- Không đọc hay in nội dung `.env*`, keystore, file service account.

## Môi trường (Windows)
- JDK 21: `E:\Dev\Java\jdk-21.0.13+9` (`JAVA_HOME`). Android SDK: `E:\Dev\Android\Sdk` (`ANDROID_HOME`). Android Studio: `E:\Dev\Android\AndroidStudio`.
- Chỉ build Android cục bộ. iOS cần Mac hoặc CI macOS.
- File tải về đặt trong `downloads\` (mỗi lần một thư mục con riêng, đã `.gitignore`), hỏi người dùng trước khi tải.

@AGENTS.md
