# HANDOFF: YourRefri

> Cập nhật: **2026-11-11**. Repo `anhvb702/thuc-tap-sinh`; `HEAD` và `origin/main` cùng ở `c4d12d1`.
> Sứ mệnh: **Người trợ lý nhỏ trong căn bếp gia đình** — giao diện Warm Minimalism, dữ liệu chỉ lưu trên thiết bị.

- Đã dùng JDK 21.0.9 và Android SDK `C:\Users\anhvb2\AppData\Local\Android\Sdk`. Các mutation compile được chạy từng cái, source khôi phục ngay sau mỗi lần; cả sáu đều bắt được tại API experimental dự kiến. Sau đó `:composeApp:testDebugUnitTest` và `:composeApp:assembleDebug` chạy tuần tự và xanh.
- **Toolchain đã xác minh (2026-11-11):** Gradle 8.5, AGP 8.3.2, compile/target SDK 34, minSdk 24, Kotlin 2.2.20, Compose Multiplatform 1.7.3, lifecycle 2.8.4. `:composeApp:testDebugUnitTest` và `:composeApp:assembleDebug` đều thành công với `gradle.properties` Windows-ROOT trust store và HTTP timeout 120000/60000, không đặt `JAVA_TOOL_OPTIONS`; assemble hoàn tất dex/APK. Added function-level `ExperimentalLayoutApi` on PantryGridScreen, RecipeScreen, WeeklyMealPlanContent and `ExperimentalFoundationApi` on RecipeScreen. Warning duy nhất: `libandroidx.graphics.path.so` không strip được, được đóng gói nguyên trạng.
- **Xác nhận toolchain giữ nguyên (2026-11-11):** trên JDK 21.0.9/Android SDK cục bộ, chạy tuần tự `:shared:testDebugUnitTest --rerun-tasks --no-configuration-cache --max-workers=1` PASS (355 tests, 0 failures/errors; 17/17 actionable tasks thực thi); `:composeApp:testDebugUnitTest --rerun-tasks --no-configuration-cache --max-workers=1` PASS (118 tests, 0 failures/errors; 38/38 thực thi); `:composeApp:assembleDebug --rerun-tasks --no-configuration-cache --max-workers=1` PASS (54/54 thực thi). Không đổi phiên bản thư viện. Cảnh báo assemble: `libandroidx.graphics.path.so` không strip được, được đóng gói nguyên trạng.
- **Sáu mutation opt-in:** từng chạy riêng `:composeApp:compileDebugKotlin --no-configuration-cache --max-workers=1`, khôi phục source ngay sau lượt. (1) bỏ PantryGridScreen Layout → FAIL line 172/569; (2) bỏ RecipeScreen Layout → FAIL line 263; (3) bỏ RecipeScreen Foundation → FAIL Carousel lines 303/306/309/325; (4) bỏ WeeklyMealPlanContent Layout → FAIL line 545; (5) chuyển Pantry opt-in sang WarmFoodCard → FAIL line 172/569; (6) chuyển Weekly opt-in sang ShelfDayOption → FAIL line 545. Cả sáu bắt lỗi dự kiến. Sau khôi phục, Compose test PASS (38 actionable; 1 executed/37 up-to-date) và assemble PASS (54 actionable; 1 executed/53 up-to-date). Persistent timeout validation without `JAVA_TOOL_OPTIONS`: `:shared:testDebugUnitTest` PASS in 16s (17 actionable; 1 executed/16 up-to-date); `:composeApp:assembleDebug` PASS in 4s (54 actionable; 1 executed/53 up-to-date).
## Snapshot hiện tại

- Khi bắt đầu task ngày 2026-11-11, mọi thay đổi sản phẩm đã nằm trên `origin/main` tại commit `c4d12d1`. Chỉnh sửa tài liệu, toolchain, Compose opt-ins và implementation/tests SW-01a/SW-01b/SW-02 hiện ở working tree, chưa commit.
- Emulator đã chạy được từ **2026-11-10**. Điều này không xác nhận các tiêu chí runtime cụ thể liệt kê bên dưới.
- **W2-03:** xác minh lập lịch thông báo sau reboot và đổi múi giờ còn mở.
- **W4-03:** kiểm tra runtime với font scale 1.3, TalkBack và tên dài còn mở.
- **UX-04/05:** checklist nghiệm thu runtime còn mở.
- **SW-01a hoàn tất (2026-11-11):** schema v4 lưu một recipe/ngày/bữa với portions; repository SQLDelight hỗ trợ round-trip, overwrite theo composite key và live Flow emission. Migration v3→v4 được kiểm thử trên file DB sau close/reopen, đối chiếu mọi cột của năm bảng legacy. `:shared:testDebugUnitTest` PASS: **350 tests, 0 failures/errors**. Test-first đỏ do weekly symbols chưa tồn tại. Sáu contract mutation và probe bỏ Flow invalidation đều bị bắt; file persistence/empty schedule/invalid IDs và portions cũng có test. Chi tiết/lệnh tại `docs/tests/SW-01a.md`.
- **SW-01b hoàn tất (2026-11-11):** backup export hiện phát JSON version 2 với weekly entries; decoder giữ tương thích version 1 và mặc định lịch tuần rỗng. Restore v2 thay toàn bộ bảng trong transaction; restore v1/empty xóa weekly plan. `:shared:testDebugUnitTest` PASS: **355 tests, 0 failures/errors**. Sáu mutation contract bị bắt; chi tiết kết quả tại `docs/tests/SW-01b.md`.
- **SW-02 completed (2026-11-11):** moved `WeeklyMealPlanContent` into its same-package file, preserving its body/signature/callbacks; visibility is `internal`. `:composeApp:testDebugUnitTest` passed at baseline and after the move (118 tests, 0 failures). Five of six presentation mutations were caught. Pantry-stock/no-lunch subtitle mutation survived because the existing suite lacks that input case; recorded as a test gap in `docs/tests/SW-02.md`. No UI behavior or tests changed.
- Các quyết định Smart Weekend Loop ngày 2026-11-11 được ghi trong `DECISIONS.md`; kế hoạch các đợt nằm trong `PLAN.md`. Contract SW-01a và SW-01b đã được thực thi; contract tiếp theo sẽ viết theo task.

## Runtime và giới hạn bằng chứng

Emulator chạy được từ ngày 2026-11-10. Bằng chứng runtime ngày 2026-11-10 được đo khi targetSdk còn 36; sau khi giữ toolchain ở targetSdk 34 cần đo lại luồng xin quyền notification và đặt alarm. Lượt 2026-11-11 xác nhận emulator `emulator-5554` đang kết nối, nhưng chưa chạy kiểm tra runtime mới. Chưa có bằng chứng phiên này xác nhận W2-03 reboot/múi giờ, W4-03 font scale 1.3/TalkBack/tên dài hoặc checklist UX-04/05. SW-01a/SW-01b unit tests, migrations và mutations đã được xác minh; kết quả ghi ở các contract. Không suy diễn trạng thái runtime từ build/unit test.

## Trạng thái dự án

- `HEAD` vẫn ở `main` đồng bộ với `origin/main` tại `c4d12d1`. Working tree có các chỉnh sửa tài liệu, toolchain, Compose opt-ins và implementation/tests SW-01a/SW-01b/SW-02; chưa commit.
- Các mốc cũ trong tài liệu task, kể cả số test/build, là bằng chứng lịch sử tại thời điểm chạy; không phải kết quả xác minh hiện tại.

## Việc còn lại

1. Xác minh W2-03: lập lịch lại sau reboot và đổi múi giờ.
2. Xác minh W4-03 trên runtime: font scale 1.3, TalkBack và tên dài.
3. Thực hiện checklist UX-04/05 trên runtime.
4. SW-01a/SW-01b đã hoàn tất theo contract hiện có. Yêu cầu executor mới mô tả thêm lưu `DietGoal`, preferences, swap offsets, unchecked IDs và tùy chọn meal-prep; các giá trị này chưa thuộc contract/test SW-01a hiện tại (chỉ lưu suất ăn theo ngày/bữa). Đã dừng trước phần mở rộng để chờ xác nhận có thay contract và test hay giữ phạm vi đã duyệt.
5. Tiếp tục Smart Weekend Loop theo các đợt trong `PLAN.md`; SW-10 đang bị chặn bởi quyết định ngày tính lại khi món đã hết rồi có lại.

Giữ snapshot này làm điểm đồng bộ. Đối chiếu working tree và bằng chứng mới trước khi cập nhật trạng thái; không dùng số test hoặc trạng thái runtime cũ làm kết quả hiện tại.

## Cập nhật lượt 2026-11-11

- Toolchain được giữ nguyên theo quyết định đã ghi ở `DECISIONS.md`: Gradle 8.5, AGP 8.3.2, compile/target SDK 34, Kotlin 2.2.20, Compose Multiplatform 1.7.3, lifecycle 2.8.4. Không đổi phiên bản thư viện khác.
- Kiểm tra nền đã chạy tuần tự trên JDK 21.0.9: `:shared:testDebugUnitTest` PASS (355 tests, 0 failures/errors), `:composeApp:testDebugUnitTest` PASS (118 tests, 0 failures/errors), `:composeApp:assembleDebug` PASS (54 actionable tasks; có warning native library không strip, APK đóng gói được). Đây là kết quả nền trước thay đổi SW-01c/UX-11/UX-12.
- Sau SW-01c/UX-11/UX-12, `:composeApp:testDebugUnitTest --rerun-tasks --no-configuration-cache --max-workers=1` PASS: 133 tests, 0 failures/errors. `:composeApp:assembleDebug --no-configuration-cache --max-workers=1` PASS. Ảnh UX-11/UX-12 được chụp từ emulator `emulator-5554`; chip cuối lộ một phần và phản hồi cơm trưa cùng nhãn ngày hiện thấy.
- Emulator đang dùng targetSdk 34. Lần đầu, quyền `POST_NOTIFICATIONS` bị từ chối và alarm không có. Bật nhắc mở hộp thoại quyền đúng; sau khi cấp, alarm `RTC_WAKEUP` lúc 17:30 xuất hiện. Sau reboot, alarm vắng lúc `sys.boot_completed=1`; logcat/dumpsys cho thấy `ReminderRescheduleReceiver` chỉ khởi chạy khi broadcast BOOT_COMPLETED được giao khoảng 63 giây sau đó, rồi alarm 17:30 xuất hiện. W2-03 không đổi code. Luồng timezone chưa kiểm chứng: broadcast adb bị hệ thống từ chối.
- Bằng chứng runtime ngày 2026-11-10 được đo trên targetSdk 36; không dùng làm kết quả hiện tại trên targetSdk 34.
- SW-01c ViewModel đã được nối vào `MainActivity`/`MainScaffold`, và state cũ `remember weekly*` đã được bỏ khỏi scaffold. SW-01c và UX mutation/biên bản đang được hoàn tất theo các contract `docs/tests/`.
- Các việc runtime còn mở: W2-03 đổi múi giờ; W4-03 font scale 1.3/TalkBack/tên dài; UX-04/05 checklist. Chưa kiểm UX-11/12 trên thiết bị thật.

## Cập nhật lượt 2026-11-12: phân vai và hạn mức agent

Mục này ghi thay đổi về hệ thống agent để đồng bộ với Jev. Không liên quan code hay runtime; không có test mới.

- **Đã ghi vào `AGENTS.override.md`:** mục mới "Hạn mức sử dụng" ở cuối file. Bảng phân vai theo provider cũ giữ nguyên.
  - Claude Haiku (khoảng 40%): tra cứu, phân loại, tóm tắt, đọc log.
  - Claude Sonnet (khoảng 15%): việc đã rõ, không phải code.
  - Claude Opus (khoảng 15%): planner.
  - Claude Fable (khoảng 5%): chỉ khi suy luận khó hoặc rủi ro cao.
  - Gemini và Codex có hạn mức riêng; việc nặng về code và nghiên cứu đẩy sang hai provider này.
  - Quy tắc: bắt đầu bằng model rẻ nhất đủ làm; Jev chỉ chọn vai và chấm điểm.
- **Đề xuất của người dùng, chưa chốt:**
  - Review sản phẩm, persona đánh giá, ý tưởng và nghiên cứu thị trường dùng Gemini 3.8. File hiện chưa có vai review sản phẩm cho Gemini; review diff đang giao cho Codex Reviewer.
  - Mọi việc code dùng Codex. Việc viết test và sửa lỗi có hướng của Sonnet cũng chuyển sang Codex.
  - Tên model đã cập nhật theo tra cứu extended: Codex dùng GPT-6 theo danh sách model trong `~/.codex/models_cache.json` của Codex CLI cài trên máy: `gpt-6.1-sol` (ưu tiên cao nhất), `gpt-6.1-terra`, `gpt-6-luna`; `gpt-6-astra` có trong danh sách nhưng chưa dùng. Gemini Flash dùng `gemini-3.8-flash`; Gemini Pro chỉ có `gemini-3.1-pro-preview` (còn preview). Lần cập nhật trước dùng GPT-5.6 là sai, đã sửa lại.
- **Jev:** chưa hoạt động. Biến môi trường `TYPESAFE_API_KEY` chưa đặt trên máy; `scripts/jev.ps1 -Models` chưa được chạy để kiểm tra kết nối. `.claude/agents/jev.md` chưa đổi theo mục hạn mức này; người dùng sẽ cập nhật Jev.
- **Trạng thái:** thay đổi nằm ở working tree, chưa commit, chưa push.

Việc cần người dùng quyết: (1) review sản phẩm đi cho Gemini hay Codex; (2) đặt `TYPESAFE_API_KEY` nếu muốn bật Jev; (3) Gemini Pro còn preview: chấp nhận cho vai advisor hay đổi sang Flash.
