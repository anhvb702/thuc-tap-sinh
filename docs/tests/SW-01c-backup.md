# SW-01c-backup — Sao lưu trạng thái tuỳ chọn tuần

## Mục tiêu

Mở rộng JSON backup version 2 hiện tại bằng trường settings tuần tuỳ chọn, giữ đọc backup version 1/2 và khôi phục nguyên tử cùng các bảng hiện có.

## Input và Expected Output

- Input: snapshot có weekly meals và settings gồm weekStart, goal ID, preference IDs, swap offsets, unchecked IDs và meal-prep.
- Export tiếp tục phát version 2, có `weeklySettings` khi settings đã lưu và round-trip giữ đủ settings cùng các bản ghi tuần.
- Decoder tiếp tục chấp nhận version 1/2; field `weeklySettings` vắng được hiểu là chưa có settings.
- Restore version 2 thay thế cả settings và weekly meals trong cùng transaction; restore v1/2 không có settings sẽ xóa settings hiện tại theo semantics replace-all.
- Payload settings sai ngày, goal trống, ID rỗng/trùng, day index ngoài 0..6, offset âm hoặc key swap trùng bị từ chối trước khi DB đổi.
- Lỗi ghi ở bất kỳ bảng nào rollback settings cùng toàn bộ dữ liệu backup.

## Biên và rủi ro

- Empty settings, empty collections, offset cho ngày 0 và 6.
- Backup cũ v1 không có weekly meals; v2 có weekly meals nhưng không có settings.
- Restore snapshot rỗng/legacy phải xóa settings cũ, không giữ sót trạng thái từ thiết bị trước.
- Không thêm dependency; dữ liệu vẫn là JSON cục bộ.
- Giữ version 2 để đọc được các backup hiện có và không đổi test version đã duyệt. Bản app trước SW-01c dùng `ignoreUnknownKeys=false`, vì vậy không đọc được file export mới khi file có `weeklySettings`; ghi rõ giới hạn này, không tuyên bố tương thích ngược theo chiều import vào app cũ.

## Sáu mutation cần bị test bắt

1. Bỏ settings khỏi export — export round-trip phải thất bại.
2. Bỏ ghi settings khi restore version 2 — restore phải thất bại.
3. Bỏ xóa settings trước restore v1/v2 — legacy replace test phải thất bại.
4. Đổi weekStart hoặc goal khi restore — round-trip phải thất bại.
5. Bỏ một collection (preferences, swaps hoặc unchecked) — round-trip phải thất bại.
6. Bỏ validation biên swap/IDs và ép meal-prep về false — validation/round-trip phải thất bại.

## Execution record

- Test-first RED: `:shared:testDebugUnitTest --no-configuration-cache --max-workers=1` failed at `:shared:compileDebugUnitTestKotlinAndroid` because `weeklySettings`, `WeeklyMealSettingsBackup` and `WeeklyMealSwapOffsetBackup` were not implemented.
- Six serial runs used `:shared:testDebugUnitTest --no-configuration-cache --max-workers=1`; each mutation was restored immediately before the next run.
  1. Omit settings from export — CAUGHT by `exportAndRestore_replaceSettingsAndWeeklyMeals` (367 tests, 1 failure).
  2. Omit settings restore write — CAUGHT by `exportAndRestore_replaceSettingsAndWeeklyMeals` (367 tests, 1 failure).
  3. Omit settings clearing before legacy v1/v2 restore — CAUGHT by `restoreLegacyVersionTwo_clearsExistingSettings` (367 tests, 1 failure).
  4. Alter exported goal — CAUGHT by `exportAndRestore_replaceSettingsAndWeeklyMeals` (367 tests, 1 failure).
  5. Drop exported preference collection — CAUGHT by `exportAndRestore_replaceSettingsAndWeeklyMeals` (367 tests, 1 failure).
  6. Remove settings validation and force meal-prep false — CAUGHT by `encode_rejectsInvalidWeeklySettings` and `versionTwo_roundTripsEveryWeeklySetting` (367 tests, 2 failures).
- Final restored `:shared:testDebugUnitTest --no-configuration-cache --max-workers=1` — PASS, `BUILD SUCCESSFUL` in 3s (17 actionable tasks; 3 executed, 2 from cache, 12 up-to-date). XML reports: 367 tests, 0 failures, 0 errors.
- No mutation survived; no existing tests were changed.
