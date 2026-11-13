# SW-01a — Lưu thực đơn tuần, migration v3→v4

## Mục tiêu

Thêm lưu trữ cục bộ cho thực đơn tuần trong SQLDelight và nâng schema từ v3 lên v4. Migration phải giữ nguyên dữ liệu pantry hiện có và cho phép đọc lại các suất ăn đã lưu sau khi mở lại database.

## Input

- Database ở schema v3, có dữ liệu hiện hữu trong cả `PantryEntity`, `UserShelfEntity`, `PantryQuantityEntity`, `PantryUnitEntity` và `CookLogEntity`.
- Các bản ghi kế hoạch tuần gồm ngày (`LocalDate`), bữa (`MealSlot`, Trưa hoặc Tối), recipe ID không rỗng và số phần nguyên dương; thời gian phải được tiêm qua `Clock`/`TimeZone` nếu API cần ngày hiện tại.
- Kiểm tra cả migration database cũ v3 và thao tác ghi/đọc trên database mới v4.

## Expected Output

- Sau migration, schema là v4; mọi bản ghi pantry trước migration giữ nguyên giá trị.
- Bản ghi tuần được ghi xuống repository/SQLDelight và đọc lại giống hệt theo ngày, bữa, recipe ID, số phần.
- Có thể lưu nhiều ngày và cả Trưa/Tối trong cùng tuần; lưu lại cùng ngày/bữa ghi đè lựa chọn cũ, còn các ngày/bữa khác không đổi.
- Recipe ID rỗng hoặc số phần nhỏ hơn 1 bị từ chối trước khi ghi; dữ liệu đã lưu trước đó vẫn nguyên vẹn.
- Không phát sinh Effect UI từ tầng data; lỗi ghi/đọc được trả theo quy ước repository hiện có và không làm mất dữ liệu pantry.

## Biên và rủi ro

- Thực đơn tuần rỗng.
- Ngày đầu/cuối tuần và ngày chuyển tháng/năm.
- Hai bản ghi cùng ngày/bữa: lần ghi sau ghi đè bản ghi trước; chỉ một hàng còn lại cho khóa đó.
- Recipe ID rỗng và số phần không dương phải bị từ chối theo quy ước repository hiện có; không yêu cầu tầng data xác minh ID có trong bundle.
- Migration chạy trên database v3 có dữ liệu pantry; migration không được giả định bảng mới đã tồn tại.
- Thực đơn tuần không được làm thay đổi dữ liệu pantry hoặc backup ở task này.

## Sáu mutation cần bị test bắt

1. Bỏ đăng ký/chạy migration v3→v4; migration test phải thất bại khi kiểm tra schema version 4 và khả dụng của bảng mới.
2. Xóa hoặc bỏ một cột quan trọng khi đọc bản ghi; round-trip phải thất bại.
3. Đổi ngày của bản ghi sang ngày kế tiếp; kiểm tra ngày phải thất bại.
4. Đổi `MealSlot` Trưa thành Tối (hoặc ngược lại); kiểm tra bữa phải thất bại.
5. Bỏ lưu recipe ID hoặc trả về danh sách rỗng từ query đọc; round-trip phải thất bại.
6. Bỏ bảo toàn dữ liệu của một trong năm bảng legacy trong migration; so sánh snapshot trước/sau migration phải thất bại.

Migration test phải seed và đối chiếu mọi cột của cả năm bảng legacy, không chỉ `PantryEntity`. Test ghi đè phải xác nhận một hàng cho đúng ngày/bữa và bảo toàn các suất ăn khác. Mô hình planner hiện có món chính kèm món phụ; contract SW-01a hiện chỉ lưu một recipe cho mỗi ngày/bữa, nên không mở rộng schema để lưu món phụ trong task này.

## Biên bản thực thi

Contract lập/cập nhật ngày 2026-11-11. Test-first ban đầu: `:shared:testDebugUnitTest --no-configuration-cache --max-workers=1` FAIL ở `compileDebugUnitTestKotlinAndroid` vì `MealSlot`, `WeeklyMealPlanEntry` và repository chưa tồn tại. Sau triển khai schema/repository, test PASS: 347 tests, 0 failures/errors.

Migration test tạo file SQLite v3, seed dữ liệu, đóng driver, mở lại cùng file rồi chạy `Schema.migrate(3, 4)`. Đối chiếu mọi cột của `PantryEntity`, `UserShelfEntity`, `PantryQuantityEntity`, `PantryUnitEntity`, `CookLogEntity`; ghi/đọc weekly entry sau migration. Không kiểm tra `PRAGMA user_version`, vì test gọi `Schema.migrate` trực tiếp chứ không dùng AndroidSqliteDriver open path.

Mutation ban đầu, mỗi probe chạy riêng bằng `:shared:testDebugUnitTest --no-configuration-cache --max-workers=1` và source được khôi phục ngay:

1. Bỏ nội dung migration v3→v4: migration test FAIL (`SQLiteException`, bảng kế hoạch không tồn tại).
2. Đổi portions khi đọc thành `1`: 4 test weekly FAIL.
3. Đổi ngày khi đọc thành `2000-01-01`: 4 test weekly FAIL.
4. Ép mọi meal thành `LUNCH`: 3 test weekly FAIL.
5. Trả recipe ID rỗng khi đọc: 4 test weekly FAIL.
6. Thêm xóa `PantryQuantityEntity` trong migration: migration snapshot FAIL.

Review bổ sung yêu cầu chứng minh lưu qua lần mở DB mới và phát emission cho subscriber đang sống. Đã thêm test empty schedule, file-backed save/close/reopen, subscriber emission cho initial empty + save + overwrite, và invalid recipe ID (`""`, whitespace) / portions (`0`, negative). Migration v3 fixture giờ cũng file-backed và được đóng/mở trước migrate. `:shared:testDebugUnitTest` PASS: **350 tests, 0 failures/errors**. Mutation bỏ query Flow invalidation bằng cách chuyển sang flow một lần FAIL chỉ tại `observeAll_subscriberReceivesInitialSaveAndReplacementEmissions`; source được khôi phục. Final shared test PASS trong 11s (17 actionable: 5 executed, 1 cached, 11 up-to-date).