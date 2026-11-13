# SW-01c-weeklogic — Tính thứ Hai và rollover settings tuần

## Mục tiêu

Tách logic thuần xác định week start từ `Instant`/`TimeZone` và reset state theo tuần, để ViewModel không phụ thuộc giờ hệ thống.

## Input và Expected Output

- `weeklyPlanWeekStart(instant, timeZone)` trả về thứ Hai cùng tuần theo lịch địa phương.
- `rollWeeklyPlanSettings(settings, newWeekStart)` giữ nguyên settings nếu cùng tuần; khi đổi tuần, cập nhật weekStart và xoá swap offsets/unchecked IDs.
- Khi rollover, goal, preferences và meal-prep giữ nguyên.

## Biên và rủi ro

- Thứ Hai, Chủ nhật, ranh giới năm và cùng instant với timezone UTC−05.
- LocalDate arithmetic phải theo ngày lịch, không cộng/trừ cố định 7×24 giờ.
- Không gọi `Clock.System` hay timezone hệ thống trong hàm thuần.

## Sáu mutation cần bị test bắt

1. Trả Chủ nhật thay vì thứ Hai — tuần Chủ nhật thất bại.
2. Bỏ `TimeZone` và tính theo UTC — test timezone qua ranh giới tuần thất bại.
3. Lệch ngày qua năm — test Chủ nhật đầu năm thất bại.
4. Không xoá swaps khi đổi tuần — rollover thất bại.
5. Không xoá unchecked IDs khi đổi tuần — rollover thất bại.
6. Reset goal/preferences/meal-prep khi đổi tuần — rollover thất bại.

## Biên bản thực thi

Contract lập ngày 2026-11-11 trước test/code. Ghi test đỏ/xanh và sáu mutation sau khi hoàn tất.
