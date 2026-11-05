# YourRefri: hướng dẫn cho Codex

File này dành cho Codex và thay `AGENTS.md` (bản sinh bởi pullboard, không dùng cho dự án này). Đọc `HANDOFF.md` đầu tiên để biết đang làm tới đâu. Không chạy lệnh `pullboard`.

## App
App Kotlin Multiplatform (Compose Multiplatform), Android trước: "Hôm nay nấu gì" cho phụ huynh. Nhập đồ trong tủ bằng lưới chạm (có/hết), hạn dùng ước tính, tối đa 1 thông báo mỗi ngày, gợi ý 3 món từ đồ sắp hỏng, công thức từng bước. Kế hoạch 5 tuần ở `PLAN.md`; spec test từng task ở `docs/tests/W1-0x.md`. `applicationId`: `com.loitu.yourrefri`.

## Ràng buộc cứng
- Dữ liệu chỉ trên máy (SQLDelight). Không tài khoản, backend, analytics hay crash SDK.
- Mọi tính năng chạy đầy đủ offline.
- Hạn dùng luôn ghi "ước tính"; không khẳng định an toàn thực phẩm.
- Không tạo file ngoài danh sách cần thiết hoặc merge khi chưa hỏi người dùng. Trong phiên hiện tại người dùng đã cho phép tải dependency Gradle cần thiết và commit/push từng feature lên fork để bot review.
- Không đọc hay in nội dung `.env*`, keystore, `local.properties`, file service account.

## Văn phong trong app
Ấm, ngắn, gợi ý không ra lệnh, luôn có đường lui. Cấm: "bạn quên", "lãng phí", "bỏ lỡ", "hết hạn", điểm số, so sánh, streak. Dùng: "ước tính", "khoảng", "nên dùng trước". Đồ quá hạn: "kiểm tra lại xem còn dùng được không".

## Kiến trúc
- `:shared` (data + logic thuần, chỉ androidTarget) và `:composeApp` (UI, ViewModel).
- Package `com.loitu.yourrefri.feature.<tên>.{data,logic}` trong `:shared`; `...feature.<tên>.ui` trong `:composeApp`. `logic` là hàm thuần, không import SQLDelight.
- MVI: State / Action / Effect + ViewModel + StateFlow.
- Navigation Compose type-safe, tham số route chỉ kiểu nguyên thủy, mọi route trong `composeApp/.../nav/Routes.kt`.
- Luôn tiêm `Clock` (là `kotlin.time.Clock`) và `TimeZone`; không gọi giờ hệ thống trực tiếp.
- Công thức là JSON bundle đọc lúc chạy, không vào DB.
- Thông báo: `AlarmManager` không chính xác (`setAndAllowWhileIdle` / `setWindow`), đặt lại sau reboot và đổi múi giờ; mở app luôn thấy nội dung.

## Cách làm mỗi task (ba bước theo thứ tự, phân vai theo Team agent)
1. **Thiết kế test trước.** Nếu `docs/tests/<mã>.md` và test đã có thì dùng nguyên, đó là hợp đồng. Nếu chưa có, viết spec (tên test, đầu vào, kết quả cần đạt, mutation nó bắt) rồi viết test, chạy thấy đỏ vì thiếu code.
2. **Viết code cho test xanh.** Không sửa, xóa, `@Ignore` hay làm yếu test. Nếu cho rằng test sai, dừng và báo người dùng. Task nhỏ, đúng chữ ký trong spec, tối đa khoảng 300 dòng diff.
3. **Tự kiểm.** Chạy test; làm sai code có chủ đích ít nhất 6 kiểu (đảo dấu, đổi biên, bỏ ghi đè, ...) để xem test có đỏ không; khôi phục nguyên trạng và chạy lại xanh. Test nào không bắt được mutation nào thì báo là test yếu. Ghi biên bản ngắn: tiêu chí | đạt | bằng chứng (lệnh đã chạy, kết quả thật).
- Thêm thư viện thì ghi một dòng: vì sao cần, kéo theo gì.
- Comment chỉ giải thích lý do, không mô tả lại code; không để code bị comment.
- Báo thật: test đỏ thì nói đỏ, chưa chạy thì nói chưa chạy.

## Team agent
- Điều phối theo yêu cầu người dùng: Sol chỉ phân tích và viết đề xuất: task là gì, đầu vào, tiêu chí đạt/không đạt, trường hợp biên và danh sách nội dung tài liệu cần có; Sol không trực tiếp triển khai. Luna thiết kế test theo tiêu chí, chạy test đỏ rồi viết code cho test xanh; Terra kiểm độc lập, đối chiếu spec, chạy test/mutation và báo bằng chứng. Luna phụ trách viết tài liệu theo đề xuất của Sol; Sol rà quyết định quan trọng, người dùng chốt điều còn mở.
- Agent tài liệu là vai trò riêng: đọc `HANDOFF.md` trước, đối chiếu `PLAN.md`, spec và code thực tế; cập nhật tài liệu hiện có sau mỗi thay đổi đã xác minh. Ghi rõ quyết định đã duyệt, giả định, mâu thuẫn, việc chờ duyệt, kết quả thật và bước kế tiếp; không tự biến giả định thành yêu cầu, không bịa trạng thái hoặc ghi đè quyết định người dùng. Khi bàn giao, nêu tệp nguồn và commit/PR liên quan.
- Dữ liệu sản phẩm trong PR ý tưởng có thể khác app: ưu tiên chỉ dẫn người dùng mới nhất và báo xung đột trước khi thay phạm vi. Test hiện hữu là hợp đồng; nếu test sai thì báo người dùng, không sửa, xóa hay làm yếu khi chưa được phép.
- Root đọc handoff, chia task tối đa khoảng 300 dòng diff, điều phối phụ thuộc và tổng hợp bằng chứng. Mỗi task có một owner chịu trách nhiệm hoàn tất ba bước theo thứ tự; các vai Sol, Luna, người viết code và Terra phối hợp theo phân công, reviewer độc lập báo phát hiện.
- Giao rõ file được sửa cho từng owner, không để hai agent sửa cùng file. Trên workspace dùng chung, chỉ một agent được chạy build/test/mutation tại một thời điểm; khôi phục mutation trước khi bàn giao lượt chạy.
- Người dùng muốn chạy unit test trực tiếp trong Android Studio để theo dõi trực quan. Khi không có Computer Use, nhờ người dùng bấm chạy test trong Android Studio và cung cấp kết quả; không tự thay bằng CLI. Chỉ báo đã kiểm qua Android Studio khi thực sự thấy kết quả hoặc nhận báo cáo của người dùng, ghi rõ nguồn bằng chứng.
- Mặc định kế thừa model của phiên; chỉ override khi người dùng chỉ rõ model hoặc có chỉ dẫn áp dụng yêu cầu. Dùng task gọn và tránh đọc/làm trùng để tiết kiệm chi phí.
- Tôn trọng số slot công cụ thực tế: phiên hiện tại có 4 slot đồng thời, gồm root. Task còn lại xếp hàng theo phụ thuộc, không hứa số agent đồng thời không giới hạn.

## Môi trường (Windows, PowerShell)
```powershell
$env:JAVA_HOME = "E:\Dev\Java\jdk-21.0.13+9"
$env:ANDROID_HOME = "E:\Dev\Android\Sdk"
.\gradlew.bat :shared:testDebugUnitTest      # test (không có allTests)
.\gradlew.bat :composeApp:assembleDebug      # build APK debug
```
- Chỉ build Android cục bộ. Cache Gradle chung ở `C:\Users\loitu\.gradle`, đừng xóa.
- Test của `:shared` nằm ở `commonTest` (logic thuần) và `androidUnitTest` (SQLDelight in-memory bằng `JdbcSqliteDriver`).
- Sandbox của Codex cần quyền ghi vào thư mục repo và `C:\Users\loitu\.gradle` để Gradle chạy được.
- File tải về đặt trong `downloads\` (mỗi lần một thư mục con) và phải hỏi trước.

## Mốc hiện tại
Xem phần tiến độ cập nhật ở đầu `HANDOFF.md`, rồi đối chiếu working tree, test và PR trước khi kết luận trạng thái. Không dùng các mốc cũ hơn trong handoff làm bằng chứng hiện tại.
