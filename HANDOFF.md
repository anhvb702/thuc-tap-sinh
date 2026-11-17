# HANDOFF: YourRefri

> Cập nhật: **2026-11-17**. Repo `anhvb702/thuc-tap-sinh`; trạng thái mô tả là `origin/main` tại `9dd9433` (merge PR #6, 2026-11-16), cộng PR tài liệu này.
> Sứ mệnh: **Người trợ lý nhỏ trong căn bếp gia đình** — Warm Minimalism, dữ liệu chỉ lưu trên thiết bị.
> Đọc file này đầu tiên, rồi `DECISIONS.md` (quyết định Q1–Q28) và `PLAN.md` (đợt R0–R6). Đối chiếu `git log origin/main` trước khi tin bất kỳ dòng trạng thái nào.

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
