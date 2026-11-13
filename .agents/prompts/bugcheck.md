# Bước: săn bug
Các file đổi: {{DIFF_FILES}}
Kết quả Gradle và mutation:
{{VERIFY}}
Kết quả chạy thiết bị:
{{DEVICE}}

Đọc code đã đổi và code liên quan. Tìm bug thật: ca biên, rỗng/null, ngày giờ và múi giờ, race trong Flow/StateFlow, rò quyền, chuỗi vi phạm văn phong, vùng chạm < 48dp, dữ liệu rời máy. Mỗi bug một dòng: `S1|S2|S3|S4 — file:dòng — mô tả — cách tái hiện`. S1 làm mất dữ liệu/crash, S2 sai hành vi chính, S3 sai phụ, S4 thẩm mỹ. Không bịa; không chắc thì ghi "giả thuyết".
Dòng cuối: `RESULT: FAIL` nếu có S1 hoặc S2, ngược lại `RESULT: PASS`.
