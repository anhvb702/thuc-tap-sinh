# YourRefri

App Kotlin Multiplatform (Compose Multiplatform), Android trước. Màn chính "Hôm nay nấu gì": nhập đồ trong tủ bằng lưới chạm, nhắc đồ nên dùng sớm, gợi ý 3 món, công thức từng bước. Đối tượng: phụ huynh. Một người làm trong 5 tuần. Chi tiết kế hoạch ở `PLAN.md`; spec test ở `docs/tests/`.

`applicationId`: `com.loitu.yourrefri`.

## Ràng buộc cứng
- Dữ liệu chỉ nằm trên máy (SQLDelight). Không tài khoản, không backend, không analytics/crash SDK bên thứ ba.
- Mọi tính năng chạy đầy đủ offline; không phụ thuộc dịch vụ ngoài.
- Hạn dùng luôn ghi là "ước tính". Không khẳng định an toàn thực phẩm.
- Không tạo file ngoài danh sách cần thiết nếu chưa hỏi người dùng.

## Văn phong
Ấm, ngắn, gợi ý không ra lệnh, luôn có đường lui.
- Cấm: "bạn quên", "lãng phí", "bỏ lỡ", "hết hạn", điểm số, so sánh, streak, ngôn ngữ gây tội lỗi.
- Dùng: "ước tính", "khoảng", "nên dùng trước". Đồ quá hạn: "kiểm tra lại xem còn dùng được không".

## Kiến trúc
- Hai module: `:shared` (data + logic thuần) và `:composeApp` (UI, ViewModel).
- Package: `com.loitu.yourrefri.feature.<tên>.{data,logic}` trong `:shared`; `...feature.<tên>.ui` trong `:composeApp`.
- `logic` chỉ hàm thuần, không import SQLDelight.
- MVI: State / Action / Effect + ViewModel + StateFlow.
- Điều hướng: Navigation Compose type-safe; tham số route chỉ dùng kiểu nguyên thủy (id `Long`, ngày dạng chuỗi ISO); mọi route nằm trong `composeApp/.../nav/Routes.kt`.
- Thời gian: luôn tiêm `Clock` / `TimeZone` (kotlinx-datetime), không gọi giờ hệ thống trực tiếp.
- Công thức là JSON bundle đọc lúc chạy, không vào DB.

## Thông báo
- Tối đa 1 thông báo mỗi ngày.
- Dùng `AlarmManager` không chính xác (`setAndAllowWhileIdle` / `setWindow`), không exact alarm.
- Lên lịch lại sau khởi động máy và khi đổi múi giờ.
- Mở app luôn thấy nội dung, dù OS chặn thông báo.

## Quy trình mỗi task
1. Opus thiết kế unit test trước: tên test, đầu vào, kết quả cần đạt.
2. Haiku viết code cho test xanh. Không sửa hay làm yếu test.
3. Sonnet kiểm: đọc diff, chạy test, kiểm mutation, đối chiếu tiêu chí, ghi biên bản ngắn.
- Mỗi task ≤ ~300 dòng diff; vượt thì tách.
- Nhánh `feat/<mã-thẻ>-<slug>`, mỗi task một PR, squash merge.
- Người dùng đã cho phép commit/push từng feature lên fork để bot review; không merge khi chưa được hỏi.
- DoD: `:shared` test xanh, build `:composeApp` xanh, test khớp tiêu chí trong `docs/tests/`, UI có ảnh chụp emulator, thông báo và thời gian có checklist tay.
- Không xóa hoặc `@Ignore` test cũ.

## Môi trường (Windows)
- JDK 21: `E:\Dev\Java\jdk-21.0.13+9` (`JAVA_HOME`). Android SDK: `E:\Dev\Android\Sdk` (`ANDROID_HOME`).
- Chỉ build Android cục bộ.
- Test: `.\gradlew.bat :shared:testDebugUnitTest`. Build: `.\gradlew.bat :composeApp:assembleDebug`.
- File tải về đặt trong `downloads\` (mỗi lần một thư mục con riêng, đã `.gitignore`), hỏi người dùng trước khi tải.
- Không đọc hay in nội dung `.env*`, keystore, `local.properties`, file service account.

