# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# YourRefri

App Kotlin Multiplatform (Compose Multiplatform), Android trước. Màn chính "Hôm nay nấu gì": nhập đồ trong tủ bằng lưới chạm, nhắc đồ nên dùng sớm, gợi ý 3 món, công thức từng bước. Đối tượng: phụ huynh. Chi tiết kế hoạch ở `PLAN.md` (đợt R0–R6); quyết định đã chốt ở `DECISIONS.md` (Q1–Q28); trạng thái phiên gần nhất ở `HANDOFF.md`; spec test ở `docs/tests/` (mẫu `docs/tests/_TEMPLATE.md`).

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
- `scripts/lint-copy.ps1 -Path <file>` quét từ cấm (file `.kt` chỉ quét chuỗi; dòng trích quy tắc ghi kèm `lint-copy: rule`).

## Lệnh thường dùng
Chạy trong PowerShell ở gốc repo, sau khi đặt `JAVA_HOME` (JDK 21) và `ANDROID_HOME`:
- Test logic và data: `.\gradlew.bat :shared:testDebugUnitTest`
- Test UI (presentation, ViewModel, theme): `.\gradlew.bat :composeApp:testDebugUnitTest`
- Một lớp test: thêm `--tests 'com.loitu.yourrefri.feature.pantry.ui.QuickAddConfirmTest'` (hoặc `'*QuickAddConfirmTest'`).
- Build APK debug: `.\gradlew.bat :composeApp:assembleDebug`
- Khi cần chạy lại thật, không lấy kết quả từ cache: thêm `--rerun-tasks --no-configuration-cache --max-workers=1`. Kết quả test đọc từ `<module>/build/test-results/testDebugUnitTest/*.xml`.
- Chỉ một tiến trình Gradle tại một thời điểm.

## Kiến trúc
- Hai module: `:shared` (data + logic thuần) và `:composeApp` (UI, ViewModel).
- Package: `com.loitu.yourrefri.feature.<tên>.{data,logic}` trong `:shared`; `...feature.<tên>.ui` trong `:composeApp`. Feature hiện có: `pantry`, `recipe`, `cooklog`, `notice`, `backup`, `home`, `settings`.
- `logic` chỉ hàm thuần, không import SQLDelight.
- MVI: State / Action / Effect + ViewModel + StateFlow.
- Điều hướng: hiện chưa dùng Navigation Compose và chưa có `nav/Routes.kt`. Bốn tab là enum `NavTab` trong `ui/MainScaffold.kt`; màn phụ (chi tiết công thức, nấu rảnh tay, chúc mừng, lịch sử "Đã nấu") mở bằng state trong scaffold. Nếu thêm Navigation Compose: route type-safe, tham số chỉ kiểu nguyên thủy (id `Long`, ngày dạng chuỗi ISO), mọi route trong `composeApp/.../nav/Routes.kt`.
- Thời gian: luôn tiêm `kotlin.time.Clock` và `kotlinx.datetime.TimeZone`, không gọi giờ hệ thống trực tiếp.
- Công thức gốc là JSON bundle đọc lúc chạy, chỉ đọc; món gia đình tự thêm lưu trong DB (SQLDelight) và có trong sao lưu JSON (quyết định 2026-11-16).

### Luồng phụ thuộc
- `MainActivity.kt` dựng một `AppGraph` cho cả tiến trình (driver SQLDelight, các repository, danh mục, công thức). Xoay màn hình không tạo driver mới; ViewModel nhận repository qua `ViewModelProvider.Factory`.
- `ui/MainScaffold.kt` giữ tab, các màn phụ và nối callback giữa các feature (ví dụ nấu xong thì trừ tủ qua `PantryGridAction` rồi ghi `CookHistoryViewModel`).
- `PantryGridViewModel` + `PantryGridReducer` (`:shared`) là nguồn trạng thái tủ; Trang chủ, gợi ý món và thực đơn tuần đều đọc `PantryGridState`.
- Chữ hiển thị và quy tắc trình bày tách ra hàm thuần trong file `*Presentation.kt` / `*Copy.kt` cạnh màn hình, để test JVM được mà không cần Compose UI test.

### Dữ liệu
- Bảng ở `shared/src/commonMain/sqldelight/com/loitu/yourrefri/shared/db/*.sq`; migration đánh số `migrations/<n>.sqm` (file mới lấy số kế tiếp trong thư mục). Mỗi migration có test mở DB cũ.
- Thêm bảng người dùng thì phải đưa vào sao lưu: `feature/backup/data/BackupCodec.kt` (JSON có `version`, vẫn đọc được bản cũ) và `PantryBackupRepository` (khôi phục thay toàn bộ trong một transaction).
- Danh mục nguyên liệu `shelf_table.json` có hai bản: `composeApp/src/main/assets/` (app đọc lúc chạy) và `shared/src/commonMain/resources/` (test). Sửa thì sửa cả hai. Công thức ở `composeApp/src/main/assets/recipes.json`.
- Test `:shared`: `commonTest` cho logic thuần, `androidUnitTest` cho SQLDelight bằng `JdbcSqliteDriver` in-memory.

### Thông báo
- Code ở `composeApp/.../reminder/` (`ReminderScheduler`, `ReminderReceiver`); nội dung và giờ tính trong `:shared` `feature/notice/logic` (`planDailyNotice`, `nextReminderTrigger`, `reminderTimeFor`).
- Tối đa 1 thông báo mỗi ngày.
- Dùng `AlarmManager` không chính xác (`setAndAllowWhileIdle` / `setWindow`), không exact alarm.
- Lên lịch lại sau khởi động máy và khi đổi múi giờ.
- Mở app luôn thấy nội dung, dù OS chặn thông báo.

## Quy trình mỗi task
- Vai trò, model và các bước hiện hành ở `AGENTS.override.md` (thay `AGENTS.md`). Không chạy `pullboard`.
- Nếu contract tồn tại trong `docs/tests/`, contract đó là tiêu chí nghiệm thu: thiết kế test trước code, không sửa/làm yếu test, chạy mutation và ghi bằng chứng thật.
- Mỗi task ≤ ~300 dòng diff; vượt thì tách.
- Không tự tạo nhánh, PR, commit/push hoặc merge ngoài phạm vi được yêu cầu. Khi được yêu cầu: mỗi tính năng một nhánh và một PR vào `main` của fork `anhvb702/thuc-tap-sinh`. Hướng dẫn nhánh `ytuong/...` trong `README.md` là quy trình nộp ý tưởng của lớp, không áp dụng cho code app.
- DoD: `:shared` test xanh, build `:composeApp` xanh, test khớp tiêu chí trong `docs/tests/`, UI có ảnh chụp emulator, thông báo và thời gian có checklist tay.
- Không xóa hoặc `@Ignore` test cũ.

## Môi trường (Windows)
- Đường dẫn JDK 21 và Android SDK khác nhau theo máy (ví dụ `C:\Setups\JavaJDK\jdk-21.0.9` và `C:\Users\anhvb2\AppData\Local\Android\Sdk`; máy khác dùng `E:\Dev\Java\jdk-21.0.13+9` và `E:\Dev\Android\Sdk`). Kiểm `JAVA_HOME`, `ANDROID_HOME` trước khi build.
- Toolchain giữ nguyên theo `DECISIONS.md`: Gradle 8.5, AGP 8.3.2, compile/target SDK 34, minSdk 24, Kotlin 2.2.20, Compose Multiplatform 1.7.3. Không nâng phiên bản.
- Máy Windows sau proxy/CA riêng: đặt `systemProp.javax.net.ssl.trustStoreType=WINDOWS-ROOT` trong `%USERPROFILE%\.gradle\gradle.properties`, không đặt trong file dự án.
- Chỉ build Android cục bộ.
- File tải về đặt trong `downloads\` (mỗi lần một thư mục con riêng, đã `.gitignore`), hỏi người dùng trước khi tải.
- Không đọc hay in nội dung `.env*`, keystore, `local.properties`, file service account.
