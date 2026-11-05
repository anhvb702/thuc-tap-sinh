# YourRefri: hướng dẫn cho Codex

File này dành cho Codex và thay `AGENTS.md` (bản sinh bởi pullboard, không dùng cho dự án này). Đọc `HANDOFF.md` đầu tiên để biết đang làm tới đâu. Không chạy lệnh `pullboard`.

## App
App Kotlin Multiplatform (Compose Multiplatform), Android trước: "Hôm nay nấu gì" cho phụ huynh. Nhập đồ trong tủ bằng lưới chạm (có/hết), hạn dùng ước tính, tối đa 1 thông báo mỗi ngày, gợi ý 3 món từ đồ sắp hỏng, công thức từng bước. Kế hoạch 5 tuần ở `PLAN.md`; spec test từng task ở `docs/tests/W1-0x.md`. `applicationId`: `com.loitu.yourrefri`.

## Ràng buộc cứng
- Dữ liệu chỉ trên máy (SQLDelight). Không tài khoản, backend, analytics hay crash SDK.
- Mọi tính năng chạy đầy đủ offline.
- Hạn dùng luôn ghi "ước tính"; không khẳng định an toàn thực phẩm.
- Không tạo file ngoài danh sách cần thiết, không commit/push/merge, không tải file, khi chưa hỏi người dùng.
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

## Cách làm mỗi task (một agent làm đủ ba bước, theo thứ tự)
1. **Thiết kế test trước.** Nếu `docs/tests/<mã>.md` và test đã có thì dùng nguyên, đó là hợp đồng. Nếu chưa có, viết spec (tên test, đầu vào, kết quả cần đạt, mutation nó bắt) rồi viết test, chạy thấy đỏ vì thiếu code.
2. **Viết code cho test xanh.** Không sửa, xóa, `@Ignore` hay làm yếu test. Nếu cho rằng test sai, dừng và báo người dùng. Task nhỏ, đúng chữ ký trong spec, tối đa khoảng 300 dòng diff.
3. **Tự kiểm.** Chạy test; làm sai code có chủ đích ít nhất 6 kiểu (đảo dấu, đổi biên, bỏ ghi đè, ...) để xem test có đỏ không; khôi phục nguyên trạng và chạy lại xanh. Test nào không bắt được mutation nào thì báo là test yếu. Ghi biên bản ngắn: tiêu chí | đạt | bằng chứng (lệnh đã chạy, kết quả thật).
- Thêm thư viện thì ghi một dòng: vì sao cần, kéo theo gì.
- Comment chỉ giải thích lý do, không mô tả lại code; không để code bị comment.
- Báo thật: test đỏ thì nói đỏ, chưa chạy thì nói chưa chạy.

## Team agent
- Root đọc handoff, chia task tối đa khoảng 300 dòng diff, điều phối phụ thuộc và tổng hợp bằng chứng. Mỗi task có một owner làm đủ ba bước ở trên theo thứ tự; reviewer độc lập chỉ đọc và báo phát hiện.
- Giao rõ file được sửa cho từng owner, không để hai agent sửa cùng file. Trên workspace dùng chung, chỉ một agent được chạy build/test/mutation tại một thời điểm; khôi phục mutation trước khi bàn giao lượt chạy.
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
Xem `HANDOFF.md`. Tóm tắt: W1-00 và W1-01 đã xong và được kiểm; W1-02/03/06 đã có code nhưng chưa kiểm; W1-04/05 mới có test, chưa có code; W1-07 (UI lưới) chưa bắt đầu.
