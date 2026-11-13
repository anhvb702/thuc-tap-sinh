# Bước: chốt phạm vi, contract và chia task
Brief và đánh giá:
{{BRIEF}}
{{EVAL}}

Việc cần làm:
1. Đọc `HANDOFF.md`, `docs/tests/_TEMPLATE.md` và 1-2 contract gần nhất để theo đúng kiểu.
2. Với mỗi task, viết contract vào `docs/tests/<ID>.md` theo `_TEMPLATE.md`: Task ID, mục tiêu, Input (state/action/fake/clock), Expected Output quan sát được, biên, và 6 mutation phù hợp. Chỉ ghi vào `docs/tests/`; để trống phần Biên bản.
3. Mỗi task ≤ 300 dòng diff, có file sở hữu riêng; hai task độc lập không sở hữu cùng file. Task phụ thuộc phải khai `deps`.
4. Cuối câu trả lời, đưa đúng MỘT khối ```json sau (đường dẫn dùng dấu `/`, tính từ gốc repo):
```json
{ "feature": "tên ngắn",
  "tasks": [ { "id": "X-01", "title": "...", "kind": "logic|ui", "deps": [],
               "owns": ["shared/src/commonMain/kotlin/.../File.kt"],
               "tests": ["shared/src/commonTest/kotlin/.../FileTest.kt"],
               "gradle": [":shared:testDebugUnitTest"],
               "contract": "docs/tests/X-01.md", "est_diff": 120 } ] }
```
`owns` là file code (không phải test). `tests` là file test sẽ được viết trước.
`RESULT: DONE`
