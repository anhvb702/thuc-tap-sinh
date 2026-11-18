---
trigger: always_on
---

# Quy Tắc Ship Tính Năng: Commit → Push → PR → Merge

Áp dụng khi người dùng yêu cầu đưa một việc vào `main`. Không áp dụng cho việc chỉ đọc, phân tích hay thử nghiệm không commit.

## 1. Điều kiện "ok"

- Một việc chỉ được coi là ok khi **mọi mục checklist ở mục 2 đạt**, và mỗi mục có bằng chứng thật: lệnh đã chạy và kết quả đã thấy.
- Mục nào chưa làm thì ghi rõ "chưa xác minh", không ghi "đạt".
- Không suy đoán kết quả. Lệnh chưa chạy thì nói chưa chạy.

## 2. Checklist kiểm thử bắt buộc

Đánh dấu từng mục khi đã chạy. Mục không áp dụng cho thay đổi thì ghi "không áp dụng" kèm lý do.

- [ ] `.\gradlew.bat :shared:testDebugUnitTest` xanh (khi đụng `:shared`)
- [ ] `.\gradlew.bat :composeApp:testDebugUnitTest` xanh (khi đụng `:composeApp`)
- [ ] `.\gradlew.bat :composeApp:assembleDebug` xanh (khi đụng code Android)
- [ ] Test khớp tiêu chí trong `docs/tests/<mã>.md` (nếu có contract); không sửa, xóa, `@Ignore` hay làm yếu test cũ
- [ ] Sáu mutation đã chạy và biên bản đã điền kết quả thật (nếu có contract)
- [ ] `scripts/lint-copy.ps1 -Path <file>` không báo từ cấm (khi có chuỗi hiển thị)
- [ ] Checklist tay (thông báo, giờ, ảnh chụp emulator, TalkBack, cỡ chữ lớn) đã làm, hoặc ghi rõ "chưa xác minh"
- [ ] Tài liệu thay đổi: đọc lại, không còn link hỏng, không còn đường dẫn máy cá nhân

Lệnh Gradle chỉ chạy lại khi có thay đổi mã. Chỉ một tiến trình Gradle tại một thời điểm.

## 3. Review

1. Chạy `superpowers:requesting-code-review` trên diff của nhánh.
2. Ghi lại từng phát hiện: file, dòng, mức độ, lý do.
3. Không merge khi còn phát hiện mở.

## 4. Tiếp nhận ý kiến review

1. Chạy `superpowers:receiving-code-review` trước khi sửa.
2. Kiểm chứng từng ý kiến: đúng thì sửa, sai thì ghi lý do kèm bằng chứng.
3. Sửa xong thì chạy lại toàn bộ checklist mục 2, không chỉ mục liên quan.
4. Lặp lại mục 3 cho đến khi không còn phát hiện mở.

## 5. Git

- Nhánh mới tách từ `main`, đặt tên `feat/...`, `fix/...` hoặc `docs/...`. Không commit thẳng `main`.
- Chỉ commit file thuộc task. Không gộp file đang sửa dở của việc khác.
- Commit message theo PB9: `type(scope): subject`, tối đa 72 ký tự, phần thân chỉ khi có lý do không hiển nhiên.
- Không dùng `--no-verify`. Không force-push. Không sửa lịch sử của `main`.
- Push lên `origin`, tạo PR vào `main` của `anhvb702/thuc-tap-sinh`. Mô tả PR gồm: mục đích, file thay đổi, checklist mục 2 với bằng chứng, kết quả review.

## 6. Merge

- Chỉ merge khi **đủ cả ba**: checklist mục 2 đạt (hoặc các mục chưa làm đã được người dùng chấp nhận rõ ràng), review không còn mục mở, và người dùng nói "ok" trong tin nhắn.
- Nếu còn thiếu mục nào, dừng và gợi ý cho người dùng chọn một trong các hướng:
  - sửa tiếp phần thiếu;
  - bỏ qua mục đó với lý do ghi trong PR;
  - hoãn merge và giữ PR ở trạng thái draft.
- Không tự merge khi chưa có "ok" từ người dùng, kể cả khi mọi mục đã đạt.

## 7. Phạm vi

- Không đụng file đang thay đổi dở của người khác hoặc không thuộc task (ví dụ `gradle.properties` có thay đổi chưa commit).
- Không đổi `CLAUDE.md`, `AGENTS.override.md` hay `DECISIONS.md` trong việc ship, trừ khi việc đó chính là nội dung được yêu cầu.
- Không tạo file ngoài danh sách cần thiết nếu chưa hỏi người dùng.
