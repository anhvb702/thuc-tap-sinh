# SW-01b — Backup JSON có thực đơn tuần

## Mục tiêu

Mở rộng backup/restore JSON để lưu các suất ăn tuần từ `WeeklyMealPlanEntity`, giữ tương thích với backup version 1 và dùng version 2 cho bản export mới.

## Input

- Cơ sở dữ liệu có pantry, overrides, cook logs và các weekly entries gồm ngày, `MealSlot`, recipe ID, portions.
- Backup JSON v1 không có trường weekly plan.
- Backup JSON v2 có thể có danh sách weekly entries rỗng hoặc nhiều ngày/bữa.
- Các bản ghi tuần có thể có hai suất cùng ngày (Trưa/Tối); khóa `(day, meal)` phải duy nhất.

## Expected Output

- `exportJson()` phát JSON format `yourrefri-backup`, version 2, gồm mọi weekly entry đã lưu cùng bốn trường ngày/bữa/recipe ID/portions; round-trip decode giữ nguyên giá trị và thứ tự.
- `decode()` tiếp tục chấp nhận version 1 hiện hành trước thay đổi; thiếu trường tuần được hiểu là danh sách rỗng. Version không hỗ trợ hoặc format sai tiếp tục bị từ chối.
- `restoreJson()` version 2 thay thế dữ liệu hiện tại của mọi bảng, gồm weekly plan; entry có cùng khóa ngày/bữa không nhân đôi.
- `restoreJson()` version 1 vẫn thay thế dữ liệu cũ và xóa weekly plan hiện có vì backup đó biểu diễn lịch tuần rỗng.
- JSON lỗi, ngày không hợp lệ, meal slot không hỗ trợ, recipe ID rỗng, portions nhỏ hơn 1 hoặc khóa tuần trùng lặp bị từ chối trước khi thay đổi DB.
- Lỗi khi ghi trong transaction rollback cả pantry data lẫn weekly plan.

## Biên và rủi ro

- Backup v1 có/không có trường `units`; trường mới không làm mất khả năng đọc backup v1.
- Version 2 không có weekly entries và version 2 có entries.
- Hai bữa trong cùng ngày được giữ riêng; cùng ngày/cùng bữa là khóa trùng.
- Ngày qua tháng/năm; meal slot chỉ nhận `LUNCH` hoặc `DINNER`.
- Restore snapshot rỗng xóa weekly plan cũ cùng các bảng dữ liệu khác.
- Backup vẫn là JSON cục bộ; không thêm network hoặc dependency.

## Sáu mutation cần bị test bắt

1. Bỏ weekly entries khỏi export; round-trip export test phải thất bại.
2. Bỏ ghi weekly entries trong restore v2; restore assertion phải thất bại.
3. Bỏ xóa weekly table trước restore; restore v1/empty phải thất bại vì entry cũ còn lại.
4. Thay recipe ID/portions khi decode hoặc restore; round-trip phải thất bại.
5. Từ chối version 1 hoặc diễn giải thiếu weekly field sai; legacy test phải thất bại.
6. Bỏ kiểm tra khóa trùng `(day, meal)`; validation test phải thất bại.

## Biên bản thực thi

Test-first: `:shared:testDebugUnitTest --no-configuration-cache --max-workers=1` initially failed at `compileDebugUnitTestKotlinAndroid` because `WeeklyMealBackup` was not yet implemented. After implementation, the suite passed. An intermediate compile exposed the missing SQLDelight `deleteAll` query for weekly plans; it was added to `WeeklyMealPlanEntity.sq` before tests passed.

`BackupCodec.VERSION` is 2; decoder accepts v1/v2, defaults omitted weekly entries to empty, validates supported `LUNCH`/`DINNER`, valid dates, nonblank recipe IDs, positive portions and unique `(day, meal)` keys. Export includes weekly rows in database order. Restore clears and replaces weekly rows inside the same transaction as existing backup tables; a v1 payload without a weekly field therefore clears the current plan.

Final `:shared:testDebugUnitTest --no-configuration-cache --max-workers=1`: **BUILD SUCCESSFUL**, 355 tests, 0 failures/errors (17 actionable tasks; 3 executed, 2 from cache, 12 up-to-date). Six independent mutations were run serially and restored immediately:

1. Export empty weekly list → `export_includesRowsFromAllTables` FAIL (1 failure).
2. Omit weekly writes during restore → `restore_replacesExistingRowsInEveryTable` FAIL (1).
3. Omit weekly `deleteAll` → replace, v1-clear and empty-snapshot tests FAIL (3).
4. Write a wrong recipe ID on restore → replace test FAIL (1).
5. Reject v1 in version validation → six legacy codec tests FAIL.
6. Remove duplicate `(day, meal)` validation → duplicate codec and invalid restore tests FAIL (2).

`git diff --check` was clean after final restoration. No UI or network changes; backup remains JSON and local-only.