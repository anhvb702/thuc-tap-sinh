---
name: qodo-review-workflow
description: Quy trình tiếp nhận, bóc tách và chuyển hóa prompt review từ Qodo PR thành task khắc phục
---

# Qodo Review Workflow

## 1. Tiếp nhận Prompt Review từ Qodo
Khi Qodo (trước đây là CodiumAI) để lại comment hoặc PR review prompt:
1. Trích xuất chính xác:
   - File và dòng code bị gắn cờ
   - Vấn đề bảo mật/logic/hiệu năng mà Qodo chỉ ra
   - Giải pháp Qodo đề xuất

## 2. Đối chiếu Hợp đồng Hệ thống
- Kiểm tra xem giải pháp của Qodo có xung đột với `AGENTS.override.md`, `SPEC.md` hay `HANDOFF.md` không.
  *Ví dụ:* Qodo nêu việc hoàn tác món chưa từng có để lại dòng `have=false`. Phải xem lại spec reducer: liệu `setHave(false)` trên món chưa có có được phép tạo dòng không?
- Nếu Qodo đúng: Bẻ thành sub-task sửa code và viết test bổ sung để bắt lỗi đó.
- Nếu Qodo chưa phù hợp với domain logic: Ghi nhận lý do kỹ thuật vào `DECISIONS.md`.

## 3. Quy trình Sửa và Tái thẩm định
- Executor viết unit test mô phỏng lại đúng lỗi Qodo phát hiện (test phải ĐỎ).
- Executor sửa code để test XANH.
- Reviewer kiểm tra lại mutation và xác nhận không có hồi quy (regression).
