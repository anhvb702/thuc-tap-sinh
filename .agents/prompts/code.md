# Bước: viết code cho test xanh
Task của bạn:
{{TASK}}
File bạn được sửa: {{OWNS}}
Test phải xanh (đã viết sẵn, không được sửa): {{TESTS}}
Đặc tả UI (nếu task ui):
{{HANDOFF}}
{{FAILURES}}

Chỉ sửa file trong danh sách được sửa; mỗi task ≤ 300 dòng diff; đúng chữ ký trong contract `docs/tests/<ID>.md`; dùng Clock và TimeZone được tiêm, không gọi giờ hệ thống. UI: Warm Minimalism, vùng chạm ≥ 48dp, không phát minh token. Không chạy Gradle; script sẽ chạy.
Nếu thấy test sai hoặc mâu thuẫn contract: KHÔNG sửa test, mô tả vấn đề rồi kết thúc bằng `RESULT: BLOCKED`.
`RESULT: DONE`
