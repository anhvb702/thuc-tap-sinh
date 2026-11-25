---
trigger: always_on
---

# Quy Tắc Ship Tính Năng: Commit → Push → PR → Review → Merge

Áp dụng khi người dùng yêu cầu đưa một việc vào `main`. Không áp dụng cho việc chỉ đọc, phân tích hay thử nghiệm không commit. Commit, push, tạo PR và merge chỉ chạy khi người dùng đã yêu cầu rõ trong việc đó.

## 1. Điều kiện "ok"

- Một việc được coi là ok khi **mọi mục ở mục 2 đạt hoặc ghi "không áp dụng" kèm lý do**, và mỗi mục có bằng chứng thật: lệnh đã chạy và kết quả đã thấy.
- Mục chưa làm ghi rõ "chưa xác minh", không ghi "đạt".
- Không suy đoán kết quả. Lệnh chưa chạy thì nói chưa chạy.

## 2. Kiểm tra trước khi commit

- [ ] Diff ≤ 300 dòng. Đo sau khi stage đúng file của task (gồm file mới chưa track): `git add <file của task>` rồi `git diff --cached --stat`. Vượt thì tách việc (CLAUDE.md).
- [ ] Task thay đổi code thì luôn chạy `:shared:testDebugUnitTest` (CLAUDE.md DoD), kể cả khi chỉ đụng `:composeApp`.
- [ ] Task thay đổi code thì phải có contract `docs/tests/<mã>.md` (viết trước code, theo `_TEMPLATE.md`) và test khớp contract; không sửa, xóa, `@Ignore` hay làm yếu test cũ. `<mã>` là mã của task trong `PLAN.md`. Chỉ task thuần tài liệu mới được ghi "không áp dụng" cho mục này.
- [ ] Môi trường trước khi build: `JAVA_HOME` (JDK 21) và `ANDROID_HOME` đã đặt đúng cho máy. Đường dẫn khác nhau theo máy, kiểm trước khi chạy.
- [ ] `.\gradlew.bat :shared:testDebugUnitTest` xanh (luôn chạy với task code).
- [ ] `.\gradlew.bat :composeApp:testDebugUnitTest` xanh (khi đụng `:composeApp`).
- [ ] `.\gradlew.bat :composeApp:assembleDebug` xanh (khi đụng code Android).
- [ ] Khi cần chạy lại thật, thêm `--rerun-tasks --no-configuration-cache --max-workers=1`. Kết quả đọc từ `<module>/build/test-results/testDebugUnitTest/*.xml`.
- [ ] Sáu mutation (ít nhất sáu) đã chạy; biên bản theo `docs/tests/_TEMPLATE.md` (tiêu chí | đạt | bằng chứng) có kết quả thật. Mỗi test mới phải đỏ trên code cố ý hỏng trước khi tin (PB4); mutation không làm test nào đỏ thì ghi "sống sót" và sửa test. Chỉ một tiến trình Gradle tại một thời điểm.
- [ ] `scripts/lint-copy.ps1 -Path <file>` không báo từ cấm (khi có chuỗi hiển thị).
- [ ] UI có ảnh chụp emulator; thông báo và thời gian có checklist tay (CLAUDE.md DoD). Nếu chưa làm: ghi "chưa xác minh" và để người dùng quyết (mục 6).
- [ ] Tài liệu thay đổi: đọc lại; không còn link hỏng (kiểm bằng tìm kiếm); không còn đường dẫn máy cá nhân (`Select-String -Path <file> -Pattern 'C:\\Users'` không ra kết quả).
- [ ] Nếu test đỏ, build đỏ (`assembleDebug`) hoặc kiểm tra emulator lỗi: sửa theo `.agents/rules/test-retry-and-bug-isolation.md`, tối đa 3 lần thử cho cùng một lệnh, rồi dừng và báo người dùng.

## 3. Git và PR

1. Nhánh mới tách từ `main`, đặt tên `feat/...`, `fix/...` hoặc `docs/...`. Không commit thẳng `main`.
2. Chỉ stage file thuộc task. Không gộp file đang sửa dở của việc khác.
3. Commit theo PB9: `type(scope): subject`, tối đa 72 ký tự; thêm `[id]` nếu có. Phần thân (nếu có) tối đa ba câu, chỉ khi có lý do không hiển nhiên. Dòng trailer (`Co-Authored-By`) đặt cuối.
4. Không dùng `--no-verify`. Không force-push. Không sửa lịch sử của `main`.
5. `git push -u origin <nhánh>`, rồi `gh pr create --repo anhvb702/thuc-tap-sinh --base main --head <nhánh>`.
6. Nội dung PR: mục đích; file thay đổi; checklist mục 2 với bằng chứng; mục nào "chưa xác minh"; kết quả review. Kết thúc bằng dòng `🤖 Generated with [Claude Code](https://claude.com/claude-code)`.

## 4. Review

1. Sau khi có PR, reviewer là **Codex** (`gpt-6.2-sol`, reasoning `high`, theo AGENTS.override.md), chạy ở chế độ chỉ đọc, trên diff của PR. Claude không tự review: Claude chỉ là planner. Skill `superpowers:requesting-code-review` không dùng cho bước này.
2. Phân loại từng phát hiện: **Critical** (sai chức năng, lộ dữ liệu, vi phạm ràng buộc cứng), **Important** (thiếu test, sai quy ước), **Minor** (văn phong, đặt tên).
3. Critical và Important phải được xử lý trước khi merge. Minor có thể bỏ qua nếu ghi lý do trong PR.
4. Comment từ Qodo trên PR xử lý theo `.agents/skills/qodo-review-workflow/SKILL.md`.

## 5. Tiếp nhận ý kiến review

1. Người viết code (executor) đọc ý kiến review trước khi sửa. Kiểm chứng từng ý kiến; đúng thì sửa, sai thì trả lời trong comment PR kèm bằng chứng.
2. Claude (planner) kiểm lại từng sửa đổi trước khi cho PR đi tiếp.
3. Sửa xong thì push thêm commit và chạy lại toàn bộ mục 2, không chỉ mục liên quan.
4. Lặp mục 4–5 tối đa 3 vòng. Nếu vẫn còn Critical hoặc Important thì dừng và hỏi người dùng.

## 6. Merge

- Chỉ merge khi **đủ cả ba**: mục 2 đạt (trừ các mục kiểm tay mà người dùng đã chấp nhận chưa làm, theo bullet dưới); không còn Critical hay Important; và người dùng nói "ok" trong tin nhắn.
- Chỉ các mục kiểm tay (ảnh emulator, thông báo, giờ, TalkBack, cỡ chữ) mới có thể được người dùng chấp nhận chưa làm, kèm lý do ghi trong PR. Test tự động (Gradle, mutation, contract, lint) không được miễn (PB6).
- Khi còn thiếu, dừng và gợi ý cho người dùng chọn: sửa tiếp phần thiếu; người dùng chọn chấp nhận mục chưa làm (người dùng quyết, lý do ghi vào PR); hoặc giữ PR ở trạng thái draft.
- Lệnh merge chỉ chạy sau khi có "ok", và phải ghim đúng commit đã review và đã được duyệt: `gh pr merge <số> --squash --delete-branch --match-head-commit <sha>`, với `<sha>` là đầu PR lúc người dùng nói "ok". Nếu đầu PR đã đổi, dừng và báo người dùng để xem lại.
- Trước khi hỏi "ok", đưa người dùng link PR và tóm tắt bằng chứng.
- Không tự merge khi chưa có "ok", kể cả khi mọi mục đã đạt.

## 7. Phạm vi

- Không đụng file đang thay đổi dở của người khác hoặc không thuộc task (ví dụ `gradle.properties` có thay đổi chưa commit).
- Không đổi `CLAUDE.md`, `AGENTS.override.md` hay `DECISIONS.md` trong việc ship, trừ khi chính việc đó là nội dung được yêu cầu.
- Không tạo file ngoài danh sách cần thiết nếu chưa hỏi người dùng.
