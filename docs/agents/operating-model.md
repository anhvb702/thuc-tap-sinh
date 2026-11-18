# Mô hình vận hành team agent

Tài liệu này mô tả cách chạy pipeline thực tế trong repo, không phải kế hoạch. Nguồn chân lý: `scripts/orchestrate.ps1`, `.agents/flow.json`, `.agents/roster.json`. Phân vai theo `AGENTS.override.md`.

## Một cổng người duyệt

Pipeline có đúng một cổng: người dùng đọc `.agents/runs/<RunId>/FINAL_REPORT.md` rồi tự quyết merge. Script không push, không mở PR, không merge (`flow.json`, `humanGate`).

Các cổng khác đề xuất nhưng chưa có trong script:
- **Chốt việc trước khi code** (đọc contract và quyết định sản phẩm). Hiện không có điểm dừng này; pipeline chạy liền từ đầu đến cuối.
- **Kiểm tra trên thiết bị** (thông báo, giờ, TalkBack, ảnh emulator). Chỉ ghi vào checklist, script không tự kiểm tra được.

## Các stage

| Stage | Vai | Model | Ghi chú |
|---|---|---|---|
| s01 idea | brainstormer | Gemini | cần Gemini CLI |
| s02 evaluate | advisor | Gemini | cần Gemini CLI; có quyết định GO |
| s03 plan | planner | Claude Opus (`claude-opus-5-5`) | vai duy nhất của Claude; chỉ ghi `docs/tests/` |
| s04–s06 design | designer, design_reviewer | Gemini, Codex `gpt-6.2-sol` | cần Gemini CLI |
| s07 tests first | test_writer | Codex `gpt-6.1-terra` | test phải đỏ trước code |
| s08 code | executor | Codex `gpt-6.1-terra` | song song tối đa 3, theo file sở hữu |
| s09 verify | mutation_designer | Codex `gpt-6.2-sol` | sáu mutation |
| s10 bugcheck | bug_hunter | Codex `gpt-6.2-sol` | |
| s11 review | reviewer | Codex `gpt-6.2-sol` | chỉ đọc |
| s12 personas | customer ×3 | Gemini | cần Gemini CLI; không phải nghiên cứu thật |
| s13 final | triage | Codex `gpt-6.1-terra` | ghi kết luận READY / BLOCKED / ERROR |

## Quy tắc không thương lượng

- **Claude chỉ làm planner.** `roster.json` không còn fallback Claude cho vai Gemini. Nếu thiếu Gemini CLI, script dừng với `ERROR`, không chạy tiếp bằng Claude.
- **Gradle một tiến trình tại một thời điểm.** Hàm `With-Lock` dùng file khóa độc quyền trong `.agents/locks/` (đã gitignore), tự gỡ khóa cũ khi tiến trình giữ khóa đã chết hoặc khóa quá 120 phút.
- **Giới hạn:** tối đa 2 vòng sửa mỗi nhóm, 60 lượt gọi agent, 3 agent song song, mỗi task tối đa 300 dòng diff (`flow.json`, `limits`).
- **Test không được làm yếu.** Pattern `testPattern` trong `flow.json` khoanh vùng file test.

## Chạy

```powershell
# Thử luồng, không gọi model, git hay Gradle
.\scripts\orchestrate.ps1 -Idea "mô tả ý tưởng" -DryRun

# Chạy thật (cần Gemini CLI cho s01, s02, s04, s12)
.\scripts\orchestrate.ps1 -Idea "mô tả ý tưởng"

# Chạy tiếp run dở
.\scripts\orchestrate.ps1 -RunId <RunId>
```

Kết quả: `.agents/runs/<RunId>/FINAL_REPORT.md` (gitignored). Kết quả DryRun không phải bằng chứng thật; báo cáo ghi rõ "DRY-RUN".

## Điều kiện trước khi chạy thật

1. Cài Gemini CLI và đăng nhập (việc của người dùng, cần API key). Hiện máy chưa có; Antigravity là IDE, không phải CLI mà script gọi.
2. Xác nhận các ID model trong `roster.json` (`gpt-6.2-sol`, `gpt-6.1-terra`, `gpt-6-luna`, `gemini-3.8-flash`, `gemini-3.1-pro-preview`) bằng cách chạy thử từng model một lần. `roster.json` ghi rõ cần xác nhận trước lần chạy thật.
3. `JAVA_HOME` (JDK 21) và `ANDROID_HOME` đã đặt. Đã kiểm tra trên máy này: đúng.

## Việc còn mở

- Chưa có lần chạy thật nào trên một feature. Thời gian mỗi task và số vòng sửa chưa đo được.
- `known_mismatches` trong `roster.json` còn 4 điểm lệch; đọc để biết.
- Jev không nằm trong pipeline. Nếu cần, dùng để phân loại việc ở ngoài luồng.
