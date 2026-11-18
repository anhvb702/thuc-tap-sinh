# HANDOFF: YourRefri

> Cập nhật: **2026-11-17**. Repo `anhvb702/thuc-tap-sinh`; trạng thái mô tả là `origin/main` tại `9dd9433` (merge PR #6, 2026-11-16), cộng PR tài liệu này.
> Sứ mệnh: **Người trợ lý nhỏ trong căn bếp gia đình** — Warm Minimalism, dữ liệu chỉ lưu trên thiết bị.
> Đọc file này đầu tiên, rồi `DECISIONS.md` (quyết định Q1–Q28) và `PLAN.md` (đợt R0–R6). Đối chiếu `git log origin/main` trước khi tin bất kỳ dòng trạng thái nào.

## Cập nhật lượt 2026-11-18: pipeline agent (nhánh `chore/agent-pipeline-gates`)

Chi tiết vận hành: `docs/agents/operating-model.md`. Số liệu dưới đây đo trên nhánh hiện tại (từ `main`), không phải nhánh feature.

- **Môi trường Gradle:** `JAVA_HOME` = `C:\Setups\JavaJDK\jdk-21.0.9`, `ANDROID_HOME` = `C:\Users\anhvb2\AppData\Local\Android\Sdk`. Wrapper Gradle 8.5 chạy được, không tái hiện lỗi SSL nên chưa đặt `WINDOWS-ROOT`. `:shared:testDebugUnitTest --rerun-tasks --no-configuration-cache --max-workers=1`: 474 test, 0 lỗi, 0 bỏ qua.
- **Pipeline `scripts/orchestrate.ps1`:** DryRun đi hết 13 stage, kết luận `READY` (chưa phải bằng chứng thật). Chưa có lần chạy thật nào.
- **Vai Gemini chạy qua `agy`** (Antigravity CLI 1.5.11, đã cài). Đã kiểm với prompt có ngoặc kép, xuống dòng, tiếng Việt, `--mode plan`. Prompt truyền qua đối số dòng lệnh, giữ dưới khoảng 30.000 ký tự. Model: `gemini-3.8-flash-medium`; advisor `gemini-3.8-flash-high` (thay `gemini-3.1-pro-preview`, không có trong `agy`).
- **Không còn fallback Claude** ở vai Gemini trong `roster.json`. Thiếu `agy` thì lượt dừng `ERROR`. Claude chỉ làm planner.
- **Reviewer/mutation/bug hunter** dùng `gpt-6.2-sol` (đã phản hồi qua `codex exec`). `gpt-6.1-terra` và `gpt-6-luna` chưa kiểm trực tiếp.
- **Chưa mở:** `known_mismatches` trong `roster.json` (4 điểm); Jev không nằm trong pipeline; chưa đo thời gian và số vòng sửa.
- **PR #16** `docs/feature-ship-rule` (đầu `367699d`): rule ship `.agents/rules/feature-ship.md`. Codex review 3 vòng, vòng 3 đã sửa (`8d6df03`) nhưng chưa kiểm lại. Chưa merge, chờ người dùng nói "ok".
- **Nhánh `chore/agent-pipeline-gates`** (`f748b0f`, chồng lên PR #16): `orchestrate.ps1` (agy), `roster.json`, `operating-model.md`. Chưa có PR.

## Cập nhật lượt 2026-11-18: kiểm tra lại các feature

Lượt này đo trên nhánh `feat/zalo-share-reminder-test-now` (đầu `840d488`, có thay đổi chưa commit), không phải `main`. Các số dưới đây không áp dụng cho `main`.

**Gradle (tuần tự, một tiến trình tại một thời điểm, `--rerun-tasks --max-workers=1`):**
- `:shared:testDebugUnitTest`: BUILD SUCCESSFUL, 556 test, 0 lỗi, 0 bỏ qua.
- `:composeApp:testDebugUnitTest`: BUILD SUCCESSFUL, 428 test, 0 lỗi, 0 bỏ qua.
- `:composeApp:assembleDebug`: BUILD SUCCESSFUL.

**Tiêu chí "ok":** contract trong `docs/tests/` có dòng Đạt và có mutation Bị bắt. Phân loại dưới đây đọc lại từ contract, không chỉ từ PLAN.

**Đạt (có bằng chứng unit):**
- W2-02, W3-02, UX-02, UX-03, SW-01c-backup, SW-01c-data.
- W3-04: số test trong biên bản là số lịch sử tại thời điểm task.
- P12-03: 7 probe bị bắt; chưa kiểm tra chạm trên emulator.

**Đạt nhưng có ghi chú (cần xử lý trước khi coi là hoàn tất):**
- W1-08: logic và migration đạt; UI chưa có ảnh hoặc kiểm tra runtime.
- W2-01: `emptyInputs_areEmpty` không bị mutation nào làm đỏ.
- W2-04: một mutation (lấy ngày theo UTC, giữ `toInstant(tz)`) vẫn xanh, nằm ngoài bảng 6 dòng. Cần thêm ca gần nửa đêm địa phương.
- W4-01: `emptyPantry_returnsEmpty` không bị mutation nào làm đỏ; thời gian thao tác ≤30 giây và persistence chưa đo.
- UX-06: sau mutation có sửa wording nhưng không chạy lại mutation.

**Chưa đạt runtime:**
- W4-03: ở cỡ chữ 1,3, thẻ "Tủ lạnh hiện có" trên Trang chủ bị chèn (nút "Xem tủ" xuống hai dòng và đè chữ). Chưa sửa. TalkBack chưa kiểm tra.

**Có một phần:** UX-04, UX-05 (còn dòng "Chưa chạy"), W2-03 (đổi múi giờ chưa kiểm).

**Chưa chạy:** CR-01, PH3-02, PH3-03, W1-07a, SW-01c-wire.

**Thiếu bằng chứng mutation đủ (có dòng Đạt nhưng không đủ bảng mutation):** W1-04, W1-05, W4-02, W5-01, W5-02A, W5-02B, W6-01, W6-02, SW-01a, SW-01b, UX-01, UX-10, QODO-01, P11-01..04, P12-01, P12-02, P12-02R.

**Không có kết quả trong contract (không tính là xong):** khoảng 25 contract, gồm 4B-*, A-16, W1-01, W1-02, W1-03, W1-06, UX-07, UX-11, UX-12, PH5, SEED-02 và các contract tương tự. Phần lớn task R0–R5 và SW-03 đến SW-14 có code trên nhánh nhưng chưa có contract.

**R6-01a:** phần `:shared` (seam `ReminderSnapshot`) xong, 6 mutation bị bắt; chưa commit. Phần `:composeApp` (loader Android, nối scheduler và receiver, chuyển `AppGraph`) chưa làm. Kiểm tay trên máy chưa làm.

## Cập nhật lượt 2026-11-13: Batch 4B

- Đã triển khai UI Gacha ở Trang chủ, danh sách đi chợ hằng ngày lưu SharedPreferences, đi chợ cuối tuần mở riêng không khóa, xác nhận theo tuần, badge “Đã nấu”, chọn ngăn, sao chép Zalo, chỉnh sửa Lite recipe và phản hồi rung nhẹ. Contracts: `docs/tests/4B-home-gacha.md`, `docs/tests/4B-daily-shopping-recipe.md`, `docs/tests/4B-weekly-shopping.md`.
- Đã thêm test Compose cho logic Gacha, checklist, biên tập recipe và tuần; shared repository test có round-trip `upsert` cho Lite recipe. `NoEmojiInUiTest` chỉ miễn đúng chuỗi chúc mừng Gacha.
- **Chưa xác minh test/build:** `:shared:testDebugUnitTest` dừng trước Gradle vì wrapper không tải được Gradle 8.5 (lỗi khởi tạo SSL trust store); đường dẫn JDK trong hướng dẫn không tồn tại trên máy này. JDK khả dụng là `C:\Setups\JavaJDK\jdk-21.0.9`, Android SDK là `C:\Users\anhvb2\AppData\Local\Android\Sdk`. Cần chạy tuần tự shared/compose tests và sáu mutation khi toolchain khả dụng. Runtime reduced-motion, TalkBack, rung và UI shopping cũng chưa được xác minh.


## Đã có trên `main` (đã merge, PR #4–#7)

- **Bốn tab và màn phụ:** Trang chủ, Tủ lạnh, Món ăn, Cài đặt; chi tiết công thức, nấu rảnh tay, hộp "Dùng hết hay còn lại?", màn chúc mừng, lịch sử "Đã nấu" (đợt A: `A-01`–`A-15`; giao diện v2 và chế độ tối: `UI-01`, `UI-02`).
- **Tủ lạnh:** lưới chạm Có/Hết có Hoàn tác, bộ lọc Ngăn mát/Ngăn đông/Nên dùng sớm, +/− số lượng và đơn vị, sửa hạn ước tính, tìm kiếm, nhập bằng giọng nói (nhận dạng trên thiết bị, chỉ Android 13+), thêm món ngoài danh mục (PR #6). Danh mục 25 nguyên liệu.
- **Món ăn:** 3 gợi ý hôm nay, bộ lọc ≤ 15/30 phút, nấu ngẫu nhiên, yêu thích, 10 công thức, chế độ ăn.
- **Thực đơn tuần và đi chợ:** mâm 2 món mỗi ngày, danh sách đi chợ tự trừ đồ đã có, "Đã mua xong · Cất vào tủ". Thiết lập tuần (định hướng, khẩu vị, lần đổi món, món bỏ tick) lưu qua `WeeklyMealPlanViewModel` và SQLDelight (`SW-01a/b/c`, `SW-02`, `UX-11/12`). Danh sách món đã xếp chưa lưu: tính lại từ thiết lập mỗi lần.
- **Dữ liệu:** 6 migration (`1.sqm`–`6.sqm`, schema v7); sao lưu/khôi phục JSON version 2, đọc được version 1.
- **Thông báo:** 17:30 ngày thường (bật/tắt), Chủ Nhật giờ riêng mặc định 8:00; tối đa 1 thông báo/ngày; đặt lại sau khởi động máy, đổi giờ, cập nhật app.
- **Quy trình agent:** `.agents/`, `scripts/orchestrate.ps1`, `scripts/lint-copy.ps1` (PR #4, đã sửa theo review Qodo). Chưa có lần chạy thật của `orchestrate.ps1`. Jev chưa hoạt động theo ghi nhận 2026-11-12 (thiếu `TYPESAFE_API_KEY`), chưa kiểm lại.

## Đã chốt nhưng chưa làm (xem `PLAN.md`, đợt R0–R6)

Mọi mục dưới đây là **đích**, chưa có code. Tài liệu `SPEC.md`, `PRODUCT.md` ghi trạng thái đích kèm nhãn.

| Hiện tại trong app | Đã chốt (nguồn) | Task |
|---|---|---|
| Nấu ngẫu nhiên không lọc chế độ ăn | Chế độ ăn là bộ lọc cứng ở mọi nơi gợi ý (Q2) | R0-01 |
| Nhắc 17:30 cố định, thông báo liệt kê mọi món | Mặc định 16:00, chỉnh được 14:00–20:00, tối đa 3 món (Q5, Q13a) | R0-02 |
| Hộp hỏi "Dùng hết hay còn lại?" cho từng nguyên liệu | Một chạm trừ hết, nút phụ "Giữ lại đồ còn dư" (Q3) | R1-01 |
| Chưa có Hoàn tác 5 giây và xóa dòng ghi nhầm | Q10 | R1-02, R1-03 |
| Một mâm mỗi ngày, ẩn bữa chưa ảnh hưởng thực đơn | Tối đa 2 mâm theo bữa bật; lưu thực đơn đã xếp (Q11, Q12) | SW-03, R2-01 |
| Tủ lạnh mở ở "Tất cả"; ngăn đông gán cứng 7 id thịt cá | Mặc định "Đồ tươi"; ngăn lưu theo từng món (Q9, Q28) | R4-01, R4-04 |
| 25 nguyên liệu, 10 công thức | 50–60 nguyên liệu, 25–30 công thức (Q6) | R3 |
| Chưa có đồ ăn thừa, gia vị, món gia đình tự thêm | Q16–Q20, Q8, Q26 | R4, R5 |

Nút "Nấu thêm phần cơm trưa hôm sau" đã ẩn khỏi UI (`LUNCH_MEAL_PREP_ENABLED = false`, quyết định 2026-11-12); logic và cột lưu còn trong mã.

## Bằng chứng kiểm chứng (có ngày, không phải trạng thái hiện tại)

- 2026-11-13, JDK 21.0.9, đầu nhánh PR #5: `:shared:testDebugUnitTest` 474 test, `:composeApp:testDebugUnitTest` 349 test, `:composeApp:assembleDebug` thành công, 0 lỗi.
- 2026-11-16, PR #6: `:composeApp:testDebugUnitTest` thành công (351 test).
- Chưa chạy lại trên `main` sau các merge. PR #7 và PR này chỉ đổi tài liệu.
- Toolchain giữ nguyên theo `DECISIONS.md`: Gradle 8.5, AGP 8.3.2, compile/target SDK 34, minSdk 24, Kotlin 2.2.20, Compose Multiplatform 1.7.3, lifecycle 2.8.4. `WINDOWS-ROOT` đặt trong `%USERPROFILE%\.gradle\gradle.properties` của máy, không nằm trong repo. Cảnh báo duy nhất khi build: `libandroidx.graphics.path.so` không strip được.

## Runtime còn mở (chưa có bằng chứng trên thiết bị)

- **W2-03:** thông báo thật và đặt lại lịch sau khởi động máy đã đạt trên emulator targetSdk 34 (alarm xuất hiện sau khi broadcast `BOOT_COMPLETED` được giao, khoảng 63 giây sau `sys.boot_completed`; không phải lỗi). **Đổi múi giờ chưa kiểm:** `adb` không giả lập được broadcast. Chưa thử trên máy thật Xiaomi, Oppo, Vivo, Samsung, nơi hệ thống hay chặn nền. Chi tiết `docs/tests/W2-03.md`.
- **W4-03:** cỡ chữ 1,3 và 2,0, TalkBack, tên món dài, vùng chạm 48dp.
- **UX-04/05, UX-11/12:** checklist nghiệm thu trên thiết bị.
- Bằng chứng runtime ngày 2026-11-10 đo trên targetSdk 36, không dùng cho bản hiện tại.

## Rủi ro đã biết (đánh giá 2026-11-17, đọc mã, chưa chạy)

Đây là đánh giá do mô hình viết dựa trên đọc `origin/main` và nguồn web; chưa kiểm chứng bằng chạy app.

1. targetSdk 34 không đủ để phát hành Play (cần API 36); chưa có khóa ký và AAB; bản release hiện chỉ là APK chưa ký.
2. Bộ lọc chế độ ăn chỉ nhìn nguyên liệu chính, bỏ qua nước mắm, mắm tôm, dầu hào. Không được trình bày như cam kết dị ứng. Cần chuyên gia xem trước khi làm R0-01.
3. Không có CI; test chỉ chạy cục bộ.
4. `PantryGridViewModel` tạo bằng `remember` (không qua ViewModelStore) và không bắt lỗi ghi DB.
5. Thông báo dùng một nguồn "quá ngày ước tính", Trang chủ dùng nguồn khác (lọc 0–2 ngày) nên có thể lệch nhau.
6. Nấu xong trừ đúng 1 đơn vị mỗi nguyên liệu, kể cả khi đơn vị là gram.
7. Khảo sát persona trong `docs/ux/` là giả lập, không phải nghiên cứu người dùng thật.

## Việc tiếp theo

1. Quyết định chung: đặt tên và cụm từ thống nhất ("nên dùng trước" và nhãn dải Trang chủ); xem mục "Đề xuất" cuối `DECISIONS.md`.
2. R0 (lọc chế độ ăn cứng, giờ nhắc), rồi R1, R2 theo `PLAN.md`.
3. Phiên kiểm chứng trên thiết bị cho các mục "Runtime còn mở" và đưa 3–5 phụ huynh thật dùng thử.
4. Danh sách nguyên liệu và công thức đợt 1 (R3-02) cần người dùng duyệt tên món trước khi ghi vào bundle.

## Quy trình làm việc

- Mỗi tính năng một nhánh, một PR vào `main` của fork `anhvb702/thuc-tap-sinh`; commit/push/merge chỉ khi được yêu cầu.
- Vai trò và model: `AGENTS.override.md` (mục "Hạn mức sử dụng"). Contract trước, test đỏ trước, sáu mutation, ghi bằng chứng thật vào `docs/tests/<mã>.md`.
- Lịch sử chi tiết từng lượt trước 2026-11-17 nằm trong `git log` và các file `docs/tests/`; không chép lại ở đây.
