# HANDOFF — MakeYourDay

Cập nhật: 2026-11-04. Đọc file này đầu tiên khi mở phiên mới, sau đó đọc `CLAUDE.md`, `DECISIONS.md`, `PLAN.md`.

## Dự án
App KMM **MakeYourDay**: mỗi sáng trợ lý **Luna** (đặt lại tên được) kể lại những việc người dùng đã làm hôm trước để tạo động lực. Đối tượng: dân văn phòng / freelancer. Làm trong 5 tuần, Android trước, dữ liệu nằm trên máy, không backend ở v1, không streak.
- applicationId: `com.loitu.makeyourday`
- Repo: https://github.com/anhvb702/makeyourday (private, còn trống, chưa commit/push gì)
- Trello: https://trello.com/b/P4IyEDiP/makeyourday

## Môi trường (Windows 11, không có Mac)
- JDK: `E:\Dev\Java\jdk-21.0.13+9` (`JAVA_HOME` đã đặt)
- Android Studio: `E:\Dev\Android\AndroidStudio`; SDK: `E:\Dev\Android\Sdk`; AVD: `E:\Dev\Android\avd` (Pixel 8, API 35)
- Git 2.56, Node 26 ở `C:\Program Files`
- `gh` ở `C:\Program Files\GitHub CLI\gh.exe`, đã đăng nhập `anhvb702`. Nếu `gh` không có trong PATH thì thoát hẳn và mở lại app.
- File tải về: `downloads\` (đã `.gitignore`), mỗi lần một thư mục con, hỏi người dùng trước khi tải.

## Trạng thái hiện tại
Xong:
- Quy ước: `CLAUDE.md`, `DECISIONS.md` (ADR-001..014), `PLAN.md`, `.claude/agents/brainstormer.md`, `.gitignore`.
- Trello có 6 cột (Backlog, Ready, In Progress, In Review, QA, Done) và 7 thẻ tuần 1.
- GitHub CLI đã đăng nhập, repo private đã tạo.
- Project KMP đã được giải nén vào `E:\KMM App\KotlinProject\KotlinProject\`.

Chưa xong / chưa kiểm chứng:
- Chưa biết project đã sync Gradle và chạy được trên emulator hay chưa (thẻ #1 "[BẠN] Sinh project...").
- Chưa `git init`, chưa commit, chưa push.
- Chưa tìm thấy file `tech_stack.md` mà người dùng nhắc tới. Nếu họ gửi lại thì đối chiếu với ADR-008/009 trước khi triển khai.

## Vấn đề cần xử lý ngay
1. **Project nằm lồng hai cấp** (`KotlinProject\KotlinProject`). Cần đưa nội dung lên `E:\KMM App` (cùng cấp `CLAUDE.md`), gộp `.gitignore` (giữ file của ta, thêm dòng wizard sinh ra), xóa thư mục rỗng. Hỏi người dùng trước khi di chuyển vì Android Studio có thể đang mở thư mục đó.
2. **Cấu trúc module lệch ADR-008**: wizard sinh `androidApp`, `iosApp`, `shared`, không có `composeApp`. ADR-008 ghi hai module `:shared` (data, recap) và `:composeApp` (UI, ViewModel). Phải chọn: tạo module `composeApp` hay sửa ADR cho khớp project sinh ra. Đây là quyết định kiến trúc, ghi lại vào `DECISIONS.md`.
3. Phiên cũ còn mở ở thư mục OneDrive cũ `C:\Users\loitu\OneDrive\Desktop\AI\KMM app` (còn hai thư mục rỗng không xóa được do bị từ chối quyền). Người dùng nên xóa tay và mở phiên mới ở `E:\KMM App`.

## Stack đã chốt (chi tiết ở DECISIONS.md)
Compose Multiplatform · Navigation Compose type-safe (`@Serializable`, tham số nguyên thủy, mọi route một file) · SQLDelight · Koin (có test đồ thị DI) · Coil · kotlinx-datetime (`Clock`/`TimeZone` inject) · ViewModel + StateFlow theo MVI (State/Action/Effect). Package: `com.loitu.makeyourday.feature.<name>.{data,ui}`. Tầng domain chỉ cho `recap` và `assistant`.
Thông báo: `AlarmManager` không chính xác (`setAndAllowWhileIdle`/`setWindow`), đặt lại sau boot và đổi múi giờ, tối đa 1 thông báo sáng/ngày, bỏ qua cuối tuần mặc định, nhắc tối tắt mặc định.
Phương án dự phòng điều hướng: sealed `Screen` tự viết nếu spike deep-link thông báo thất bại.

## Quy ước làm việc
- Squash merge, tiêu đề PR theo Conventional Commits, nhánh `feat/<card-id>-<slug>`, tối đa khoảng 400 dòng diff mỗi thẻ.
- Trailer commit: `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>`. Chỉ commit/push khi người dùng yêu cầu.
- Agent không có secrets, không merge, không kéo thẻ sang Done. File điểm nóng (DI, nav graph, strings, `.sq`) chỉ sửa qua thẻ "integration".
- Jev (TypeSafe AI) là tùy chọn và chỉ tư vấn; mọi tính năng phải chạy được khi không có Jev. Key không bao giờ dán vào chat hay đưa vào app.
- Không tạo file ngoài danh sách ở `PLAN.md` mục 8 nếu chưa hỏi. `HANDOFF.md` được tạo theo yêu cầu trực tiếp của người dùng.
- Người dùng muốn từng bước có "Xong khi", bạn nhắn xong thì mới làm tiếp.

## Bước tiếp theo
1. Hỏi người dùng đã sync Gradle và chạy app mẫu trên emulator chưa.
2. Giải quyết hai vấn đề ở trên (lồng thư mục, cấu trúc module).
3. `git init`, commit đầu tiên, push lên repo (khi được yêu cầu).
4. Làm các thẻ tuần 1 theo thứ tự: #2 spike thông báo mở màn Recap · #3 CI · #4 spike độ trễ AlarmManager trên máy thật · #5 mẫu MVI + Koin + test DI · #6 schema SQLDelight · #7 màn thêm nhanh.

## Việc còn lại của người dùng
- Bảo vệ nhánh `main` trên GitHub (bắt buộc PR và status checks).
- Đổi tên 6 nhãn Trello (xanh lá Dev-Shared, vàng Dev-UI, cam QA, đỏ Rủi ro cao, tím Cần Duyệt, xanh dương BẠN).
- Trước tuần 4: đăng ký Jev tại typesafe.ai, lưu key vào `.env.local`.
- Tuần 3 trở đi: tài khoản Google Play Console (25 USD), khoảng 12 người thử, thử thông báo trên máy thật. Tuần 4: tài khoản Cloudflare.
- Xác thực nhanh với 5–10 người dùng xem họ có dùng công cụ to-do khác không (quyết định phạm vi danh sách "định làm hôm nay").
- Tùy chọn: đặt `GRADLE_USER_HOME=E:\Dev\gradle`.
- `chrisbanes/skills` chưa cài; cần đọc, ghim commit và được người dùng đồng ý trước.
