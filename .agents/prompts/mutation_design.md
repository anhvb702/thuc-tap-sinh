# Bước: thiết kế mutation
Các file code đã đổi: {{DIFF_FILES}}
Kế hoạch:
{{PLAN}}

Đọc code thật trong các file đó và đề xuất đúng 6 mutation cho MỖI task (đảo cờ, lệch cận số/thời gian, bỏ ghi/phát sự kiện, trả mặc định/rỗng, đổi thứ tự ưu tiên, bỏ khôi phục/undo; chỉ chọn loại phù hợp). Mỗi mutation là một thay thế chuỗi chính xác: `find` phải xuất hiện đúng 1 lần trong file, giữ nguyên khoảng trắng; `replace` khiến code biên dịch được nhưng sai hành vi. Chỉ mutate file code (không phải test).
Cuối câu trả lời đưa MỘT khối ```json: `[ { "task": "X-01", "id": "m1", "file": "đường/dẫn", "find": "...", "replace": "...", "why": "..." } ]`
`RESULT: DONE`
