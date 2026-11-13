# Checklist review đợt 1

Mỗi mục: `ĐẠT | KHÔNG ĐẠT | CHƯA XÁC MINH` + bằng chứng (lệnh đã chạy, tệp, dòng). Không có bằng chứng thật thì ghi CHƯA XÁC MINH. Mục (B) là bắt buộc.

## A. Phạm vi
1. (B) Mỗi task ≤ ~300 dòng diff; chỉ sửa file trong `owns` của task.
2. (B) Không có file test nào bị sửa/xóa/`@Ignore` sau bước viết test trước (script đã so sánh với commit tests-first).
3. Không thêm thư viện; nếu có, có một dòng lý do.

## B. Kiểm chứng
4. (B) `:shared:testDebugUnitTest` xanh, ghi số test; `:composeApp:testDebugUnitTest` xanh nếu đụng composeApp.
5. (B) `:composeApp:assembleDebug` xanh.
6. (B) Test đã đỏ trước khi có code (bước tests-first) và xanh sau code.
7. (B) 6 mutation mỗi task đều bị test bắt; mutation sống sót được ghi là "test yếu", không bị bỏ qua.
8. Sau mutation, working tree sạch (đã khôi phục).

## C. Khớp contract
9. (B) Mỗi dòng Expected Output trong `docs/tests/<ID>.md` có test tương ứng và test đó xanh.
10. Biên (rỗng, âm, trùng, múi giờ/DST nếu liên quan) có test.
11. Thời gian dùng `Clock`/`TimeZone` được tiêm, không gọi giờ hệ thống.

## D. Ràng buộc sản phẩm
12. (B) Dữ liệu chỉ trên máy; không backend/analytics/crash SDK; chạy offline.
13. (B) Hạn dùng luôn ghi "ước tính"; không khẳng định an toàn thực phẩm.
14. (B) Chuỗi hiển thị qua lint từ cấm (`scripts/lint-copy.ps1`) không có vi phạm.
15. Thông báo: tối đa 1/ngày, alarm không chính xác, mở app vẫn thấy nội dung khi OS chặn thông báo (nếu đụng).

## E. UI (khi có task ui)
16. Vùng chạm ≥ 48dp; chữ ổn ở font scale 1.3; có contentDescription.
17. Chỉ dùng token/component có trong `DESIGN.md`; không card lồng 3 tầng, không gradient tím xanh.
18. Có ảnh chụp thật từ emulator. Nếu không có: ghi CHƯA XÁC MINH, không suy ra từ unit test.

## F. Bug
19. (B) Không còn bug S1/S2 mở trong `10_bugs.md`.
20. Bug S3/S4 được liệt kê trong báo cáo cuối.
