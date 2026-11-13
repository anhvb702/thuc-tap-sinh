# SW-01c-data — Persist trạng thái tuần trong repository

## Mục tiêu

Mở rộng `WeeklyMealPlanRepository` để lưu các tuỳ chọn của tuần bên cạnh các suất ăn hiện có. Database mới tạo bảng settings trong migration v3→v4 đang triển khai; database schema v4 cũ tạo bảng này theo cách idempotent khi mở DB, không đổi version và giữ nguyên weekly entries.

## Input và Expected Output

- Input: `WeeklyMealPlanSettings` gồm week start ISO (`LocalDate`), goal ID, preference IDs, swap offsets theo day index, unchecked ingredient IDs và `includeLunchMealPrep`.
- `observeSettings()` phát trạng thái đã lưu qua Flow; database mới có thể chưa có dòng settings.
- `saveSettings(settings)` ghi/ghi đè duy nhất trạng thái settings, round-trip giữ nguyên đủ sáu trường và phát giá trị mới cho subscriber.
- `saveSettings` từ chối goal/ID rỗng, swap day index ngoài 0..6 và offset âm trước khi thay đổi state đã lưu; các invariant này cũng giúp backup luôn encode được settings hợp lệ.
- Migration v3→v4 tạo cả bảng settings và bảng kế hoạch mà không đổi/xoá dữ liệu legacy.
- Mở database schema v4 hiện hữu chưa có bảng settings tạo được bảng thiếu và vẫn đọc nguyên weekly entries đã lưu.
- Không lưu giờ hệ thống hoặc tính tuần trong repository; ViewModel sẽ tiêm `Clock`/`TimeZone` và quản lý rollover.

## Biên và rủi ro

- Empty preferences/swaps/unchecked; nhiều IDs và offsets; bật/tắt meal prep.
- Tuần liền kề và LocalDate đầu tháng/năm được giữ đúng.
- Goal/preferences/uncheck IDs không rỗng; swap offset không âm và chỉ dùng ngày index 0..6.
- Mở database lại phải khôi phục settings và weekly entries.
- Không mã hoá settings vào `recipeId`/`portions`; không thêm dependency.
- Sao lưu/khôi phục JSON cho settings được tách thành contract SW-01c-backup để giữ mỗi task trong giới hạn diff.

## Sáu mutation cần bị test bắt

1. Bỏ query lưu settings — round-trip không tìm thấy giá trị mới.
2. Trả goal mặc định thay vì goal đã lưu — round-trip thất bại.
3. Bỏ preferences khi đọc — round-trip thất bại.
4. Bỏ swap offsets khi đọc — round-trip thất bại.
5. Bỏ unchecked IDs khi đọc — round-trip thất bại.
6. Ép `includeLunchMealPrep=false` — round-trip thất bại.

Probe bổ sung cho database v4 cũ: bỏ lệnh tạo bảng settings idempotent khi mở DB; test `openExistingV4Database_createsSettingsTableAndKeepsWeeklyEntries` phải thất bại, còn các suất ăn đã lưu vẫn phải nguyên vẹn.
Probe bổ sung cho validation: bỏ điều kiện biên save — test `saveSettings_invalidValuesAreRejectedWithoutChangingStoredState` phải thất bại.

## Execution record

- Test-first RED: `:shared:testDebugUnitTest --no-configuration-cache --max-workers=1` failed at `:shared:compileDebugUnitTestKotlinAndroid` (13 actionable tasks; 2 executed, 11 up-to-date). The new test referenced missing `WeeklyMealPlanSettings`, `saveSettings`, and `observeSettings`; the compiler also reported follow-on type-inference errors.
- Initial restored GREEN before the old-v4 compatibility test was added: same command passed, 360 tests, 0 failures/errors.
- Final restored GREEN after adding `openExistingV4Database_createsSettingsTableAndKeepsWeeklyEntries`: same command returned `BUILD SUCCESSFUL` in 4s (17 actionable tasks; 3 executed, 2 from cache, 12 up-to-date). XML reports: 361 tests, 0 failures, 0 errors.
- Seven serial mutations used the same shared test command. Source was restored immediately after each mutation.
  1. Omit settings upsert — CAUGHT; 5 failures: file-backed reopen, existing-v4 database, v3-to-v4 migration, full round-trip/replacement, and Flow emissions.
  2. Return default goal instead of saved goal — CAUGHT by the same 5 tests; 5 failures.
  3. Drop preference IDs on read — CAUGHT by the same 5 tests; 5 failures.
  4. Drop swapped offsets on read — CAUGHT by the same 5 tests; 5 failures.
  5. Drop unchecked IDs on read — CAUGHT by the same 5 tests; 5 failures.
  6. Force lunch meal prep false — CAUGHT by the same 5 tests; 5 failures.
  7. Remove `DatabaseFactory`'s idempotent settings-table creation — CAUGHT only by `openExistingV4Database_createsSettingsTableAndKeepsWeeklyEntries` (361 tests, 1 failure).
- Final source was restored before the final green run. No mutations survived.
