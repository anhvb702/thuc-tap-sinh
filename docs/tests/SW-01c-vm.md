# SW-01c-vm — MVI cho trạng thái thực đơn tuần

## Mục tiêu

Đưa state tuần vào `WeeklyMealPlanViewModel` dùng `WeeklyMealPlanRepository`, `StateFlow`, Action và Effect. Tiêm `kotlin.time.Clock`/`TimeZone`; không gọi giờ hệ thống trực tiếp.

## Input và Expected Output

- Khởi tạo với repository, fake clock, timezone và coroutine scope test.
- Nạp settings của tuần hiện tại vào `WeeklyMealPlanState`.
- Khi tuần lưu khác tuần hiện tại, chuyển weekStart sang thứ Hai hiện tại; reset swap offsets và unchecked IDs; giữ goal, preferences và meal-prep.
- Action chọn goal, bật/tắt preference, đổi món một ngày, đánh dấu nguyên liệu và meal-prep cập nhật StateFlow rồi lưu đầy đủ settings vào repository.
- `refresh(timeZone)` dùng cùng instant với timezone mới để kiểm tra đổi tuần; nếu đổi tuần áp dụng quy tắc rollover.
- Lỗi lưu phát `WeeklyMealPlanEffect.SettingsSaveFailed`; state optimistic vẫn quan sát được.

## Biên và rủi ro

- Database rỗng dùng goal `BALANCED`, preference rỗng, không có swap/uncheck, meal-prep tắt.
- Thứ Hai/Chủ nhật, đổi năm và đổi timezone qua ranh giới tuần.
- Action đến trước khi load repository hoàn tất phải được xử lý sau hydration, không ghi đè dữ liệu lưu.
- Unknown enum ID trong dữ liệu cũ được bỏ qua an toàn; không crash màn hình.
- State settings là nguồn duy nhất cho các trường `weekly*`; UI không giữ bản `remember` thứ hai ở `MainScaffold`.

## Sáu mutation cần bị test bắt

1. Đổi tuần không reset swaps — rollover test thất bại.
2. Đổi tuần không reset unchecked IDs — rollover test thất bại.
3. Reset goal hoặc preferences khi đổi tuần — rollover test thất bại.
4. Bỏ một action save — action persistence test thất bại.
5. Bỏ `Clock`/`TimeZone` hoặc tính tuần theo UTC — timezone boundary test thất bại.
6. Nuốt lỗi lưu thay vì phát Effect — save-failure test thất bại.

## Biên bản thực thi

Contract lập ngày 2026-11-11 trước khi thêm test/code. Ghi test đỏ/xanh, mutation và kết quả cuối sau khi thực thi.

## Biên bản cập nhật — 2026-11-11

- `WeeklyMealPlanViewModelTest` ban đầu đỏ vì các symbol ViewModel/action/state chưa tồn tại; sau đó suite chạy xanh. Thêm test cho action đến trước emission ban đầu của repository và lỗi đọc settings: action được áp dụng sau hydration, lỗi đọc không để state treo ở `isLoaded=false` và phát Effect.
- `:composeApp:testDebugUnitTest --rerun-tasks --no-configuration-cache --max-workers=1` PASS: 138 tests, 0 failures/errors.
- Lượt chạy trước test lỗi compile tại `WeeklyMealPlanViewModelTest.kt:168` do type inference ở assertion Effect; khai báo generic rõ ràng, chạy lại thành công.
- Mỗi thao tác từ UI nay dispatch action riêng, tránh tính delta trên snapshot Compose cũ. ViewModel được tạo qua `ViewModelProvider` để gắn với `ViewModelStore`; `MainActivity` collect Effect và hiển thị snackbar.
- Sáu mutation của contract vẫn chờ chạy tuần tự; chưa ghi nhận kết quả mutation.

### Bổ sung tiêu chí kiểm soát đồng thời

- Hành động gửi trước lần đọc đầu tiên phải xếp hàng; sau khi hydration xong, áp dụng lên trạng thái đã đọc rồi mới lưu.
- Lỗi đọc settings phải kết thúc trạng thái loading và phát `SettingsLoadFailed`, không để UI chờ vô hạn.
- Reload được gọi rõ ràng sau khi khôi phục backup; không nghe mỗi emission DB để state UI không bị emission cũ ghi đè action mới.
