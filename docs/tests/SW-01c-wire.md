# SW-01c-wire — Kết nối ViewModel vào ứng dụng

## Input và Expected Output

- Khởi tạo `WeeklyMealPlanRepositoryImpl` trên cùng database ứng dụng và inject `Clock.System` cùng `TimeZone.currentSystemDefault()` vào `WeeklyMealPlanViewModel`.
- `MainScaffold` đọc `WeeklyMealPlanState`; mọi thay đổi định hướng, khẩu vị, đổi món, đánh dấu nguyên liệu và nấu thêm phần trưa gửi action về ViewModel.
- Khi app resume, ViewModel kiểm tra lại tuần với múi giờ hiện tại.
- Không còn `remember weekly*` trong `MainScaffold`.

## Kiểm chứng

- Contract được ghi trước khi nối UI.
- Dùng `WeeklyMealPlanViewModelTest` làm contract cho load/action/persistence và chạy `:composeApp:testDebugUnitTest`; chạy compile/build Android để xác nhận wiring.
- Sáu mutation kiểm tra bằng cách bỏ từng kết nối state/action/lifecycle, khôi phục nguyên trạng sau mỗi lần.

## Sáu mutation

1. Khởi tạo ViewModel mà không dùng repository — test load/persistence phải đỏ.
2. Không truyền state vào scaffold — kiểm tra wiring phải thất bại.
3. Bỏ dispatch đổi định hướng — action persistence phải đỏ.
4. Bỏ dispatch khẩu vị/đổi món/đánh dấu nguyên liệu — action persistence phải đỏ.
5. Bỏ dispatch meal-prep — action persistence phải đỏ.
6. Bỏ refresh khi resume — kiểm tra lifecycle wiring phải thất bại.

## Biên bản

Chưa chạy kiểm chứng wiring và mutation.

## Biên bản cập nhật — 2026-11-11

- Kiểm tra source sau khi nối UI: repository/ViewModel dùng chung database; inject `Clock.System` và múi giờ hiện tại; khi resume gọi `refresh`; `MainScaffold` không còn biến `remember weekly*`.
- `:composeApp:testDebugUnitTest --rerun-tasks --no-configuration-cache --max-workers=1` PASS: 138 tests, 0 failures/errors; compile Compose xác nhận wiring.
- Không có Compose UI integration test dependency trong repo; chưa chạy mutation tự động xác nhận cả sáu đường wiring. Không ghi các probe source-only thành mutation test đã bắt được.
