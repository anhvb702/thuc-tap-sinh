# PLAN — MakeYourDay: app trợ lý ghi nhận thành quả (KMM, 5 tuần)

Trợ lý mặc định tên **Luna** (người dùng đặt lại tên được).

Ký hiệu: 👤 = bạn làm · 🤖 = Claude làm · 🤝 = cả hai.
Quy tắc: mỗi bước có "Xong khi". Bạn nhắn "xong bước X" thì mới làm tiếp.

## 1. Ý tưởng
Mỗi sáng, một trợ lý có tính cách mở app bằng lời kể lại những việc bạn đã hoàn thành hôm trước, để tạo động lực làm việc.
- Android trước (đang dùng Windows). iOS chỉ làm nếu có Mac.
- Dữ liệu nằm trên máy, không tài khoản, không backend ở v1.
- Đối tượng người dùng: **dân văn phòng / freelancer** (đã chốt). Giọng trợ lý ấm áp, hơi dí dỏm, chuyên nghiệp. Tín hiệu kiệt sức chỉ dùng để gợi ý nghỉ nhẹ, không chẩn đoán, không đóng vai chuyên gia tâm lý.

## 2. Phạm vi MVP
Có (theo ADR-006): thêm nhanh thành quả · danh sách "định làm hôm nay" dùng xong bỏ (không chuyển sang hôm sau, tick xong thành thành quả) · recap buổi sáng (màn hình và thông báo) · trợ lý một giọng, chọn xưng hô, bộ mẫu có slot và nhãn · tổng kết tuần · cài đặt giờ nhắc.
Không làm ở v1: **streak (đã cắt)**, tài khoản, đồng bộ đám mây, widget, tích hợp lịch/Todoist, chia sẻ xã hội.
Quyết định kỹ thuật chi tiết nằm ở `DECISIONS.md` (ADR-006 đến 014).

## 3. Stack
Compose Multiplatform · Navigation Compose type-safe · SQLDelight · Koin · thông báo cục bộ (`expect/actual`, WorkManager) · module `:shared` và `:composeApp`.

## 4. Vai trò của Jev
Jev không sinh văn bản. Nó chọn và chấm điểm, còn lời nhắn lấy từ bộ mẫu.

**Trong app (tuần 4, tùy chọn, có công tắc đồng ý):**
| Quyết định | Kiểu |
|---|---|
| Thành quả nổi bật nhất hôm qua (độ khó 1–5) | Score |
| Gắn nhãn danh mục việc | Choice |
| Chọn tông lời nhắn sáng nay (ăn mừng / động viên / nghỉ ngơi) | Choice |
| Tín hiệu kiệt sức trong ghi chú (chỉ đổi giọng, không chẩn đoán) | yes/no |

**Trong quy trình làm việc (script `tools/jev-gate`, chỉ tư vấn, không định tuyến hay chặn, làm sau nếu còn thời gian, ADR-012):**
| Quyết định | Kiểu |
|---|---|
| Thẻ Trello đã sẵn sàng chưa | yes/no |
| Thẻ có quá to không (≥4 thì tách) | Score |
| Giao cho agent nào | Choice |
| Có cần bạn duyệt trước không (secrets, quyền riêng tư, xóa dữ liệu) | yes/no |

Nguyên tắc: Jev chỉ gợi ý. Độ tin cậy dưới ngưỡng thì chuyển lên Opus/bạn. Jev không bao giờ merge hay kéo thẻ sang Done. Mọi tính năng trong app phải chạy được khi không có Jev (fallback bằng luật). API key không nằm trong app: gọi qua proxy Cloudflare Workers. Ghim phiên bản `jev-1.13.0`. Chưa kiểm chứng với tiếng Việt, phải đo thử trước.

## 5. Đội agent và kiểm soát
Opus (orchestrator) · Brainstormer (phản biện, chạy Opus, chỉ dùng tuần 1) · Dev · Reviewer (agent riêng).
Kiểm soát theo lớp: Jev lọc → Opus chịu trách nhiệm kế hoạch → Reviewer review từng PR → CI là cổng cứng → bạn merge và kéo thẻ sang Done.
Mỗi thẻ = một nhánh = một PR. Hai agent không sửa cùng nhóm file.

## 6. Trello (connector chính thức)
Cột: Backlog → Ready → In Progress → In Review → QA → Done.
Nhãn: Dev-UI, Dev-Shared, QA, Cần Duyệt, Rủi ro cao.
Mẫu thẻ: Mục tiêu · Tiêu chí hoàn thành · Module/file · Phụ thuộc · Rủi ro.
Luật: chỉ thẻ ở Ready mới được nhận · WIP tối đa 3 · chỉ bạn kéo sang Done.

## 7. Skill
Sẵn có: `init`, `code-review`, `simplify`, `security-review`, `run`.
Từ GitHub: đề xuất `chrisbanes/skills` (Kotlin/Compose/Android/KMP). Tôi đọc kỹ `SKILL.md` và script, ghim commit, chỉ cài sau khi bạn đồng ý. Tối đa 1–2 skill ngoài.
Tự viết bằng `skill-creator`: một skill quy ước dự án.

## 8. File giữ trong dự án (không tạo thêm)
`CLAUDE.md` · `DECISIONS.md` · `.claude/agents/brainstormer.md` · `tools/jev-gate` · `.gitignore`. Bảng việc ở Trello nên không cần `TASKS.md`.

## 9. Môi trường của bạn (đã xác minh)
| Thành phần | Vị trí | Trạng thái |
|---|---|---|
| JDK 21 | `E:\Dev\Java\jdk-21.0.13+9` (`JAVA_HOME` đã đặt) | Xong |
| Android Studio | `E:\Dev\Android\AndroidStudio` | Xong |
| Android SDK, adb | `E:\Dev\Android\Sdk` (`ANDROID_HOME` đã đặt) | Xong |
| Emulator (AVD) | `E:\Dev\Android\avd` | Xong |
| Git 2.56, Node 26 | `C:\Program Files\...` | Xong |
| Thư mục dự án | `E:\KMM App` (đã chuyển khỏi OneDrive) | Đang dùng |
| File tải về | `downloads\` trong dự án (đã `.gitignore`) | Mỗi lần tải một thư mục con riêng, hỏi bạn trước |

Lưu ý: `java` chưa nằm trong PATH, Gradle dùng `JAVA_HOME` nên không sao. Dự án đã nằm ngoài OneDrive nên Gradle không bị OneDrive khóa file. Có thể đặt `GRADLE_USER_HOME=E:\Dev\gradle` để cache Gradle không vào ổ C.

---

## 10. Các bước

### GĐ 0 — Chuẩn bị (👤)
- **0.1 JDK, 0.2 Android Studio:** xong (xem mục 9).
- **0.3 Git, Node:** xong. Còn lại: `git config --global user.name "Tên"` và `git config --global user.email "yamihoshi15@gmail.com"`; tạo repo **private** tại https://github.com/new (không tick README); cài https://cli.github.com rồi `gh auth login`. Xong khi: `gh auth status` báo đã đăng nhập và có URL repo.
- **0.4 Model:** chọn Opus cho session chính.
- **0.5 Trello:** bấm Connect Trello trong Claude, tạo board trống tên "MakeYourDay". Xong khi: tôi đọc được board.
- **0.6 Jev:** đăng ký API tại https://typesafe.ai, lưu key vào `.env.local` (không dán vào chat).

### GĐ 1 — Quy ước (tuần 1, 🤖 + duyệt 👤)
- **1.1 🤖** Tạo `brainstormer.md` và `DECISIONS.md`; dựng cột, nhãn, thẻ tuần 1 trên Trello.
- **1.2 🤖** Opus ↔ Brainstormer tranh luận tối đa 3 vòng mỗi chủ đề: đối tượng và tính cách trợ lý, phạm vi MVP, kiến trúc và điều hướng, quy ước đặt tên/nhánh/commit, chiến lược test và Definition of Done, quyền hạn agent, quyền riêng tư.
- **1.3 👤** Duyệt `DECISIONS.md`, chỉ quyết các mục gắn [CẦN BẠN QUYẾT]. Xong khi: bạn nhắn "duyệt". Tôi sinh `CLAUDE.md`.

### GĐ 2 — Dựng khung (tuần 1)
- **2.1 👤** Sinh project tại https://kmp.jetbrains.com: đặt tên app mới, tick Android và iOS (Share UI), **bỏ Server**. Giải nén vào thư mục dự án, mở bằng Android Studio, chạy trên emulator.
- **2.2 👤** `git init`, commit, `git remote add origin <URL>`, `git push -u origin main`.
- **2.3 🤖** Thêm Koin, SQLDelight, Navigation type-safe, CI (build, test, detekt, ktlint).
- **2.4 👤** GitHub → Settings → Branches: bảo vệ `main` (bắt buộc PR và status checks).
- **2.5 🤖** `tools/jev-gate`, đo thử trên 20 thẻ mẫu. Cài skill sau khi bạn đồng ý.
Xong khi: app mẫu chạy, CI xanh, PR đầu tiên được merge.

### Lịch 5 tuần
| Tuần | Mục tiêu | Xong khi |
|---|---|---|
| 1 | Môi trường, quy ước, khung dự án, DB. **Spike ngày 1–2: chạm thông báo mở đúng màn recap (app chạy và bị kill)** | Thêm thành quả, dữ liệu còn sau khi tắt app |
| 2 | Thành quả, lịch sử theo ngày, điều hướng hoàn chỉnh | Xem lại được các ngày trước |
| 3 | Recap buổi sáng, thông báo, trợ lý, bộ mẫu lời nhắn. **Đưa bản test lên Play (closed testing)** | Sáng hôm sau nhận thông báo kể lại hôm qua |
| 4 | Tổng kết tuần, cài đặt, onboarding (không có streak). Thử Jev với tiếng Việt, proxy Cloudflare, công tắc đồng ý (cắt nếu kém) | Dùng liền 3 ngày không lỗi |
| 5 | Beta, sửa lỗi, icon, ảnh store, phát hành | Bản closed testing trên Google Play |

Lưu ý Google Play: tài khoản cá nhân mới thường phải closed testing khoảng 12 người trong 14 ngày liên tục trước khi phát hành chính thức (kiểm tra lại chính sách hiện hành). Phải tuyển người thử từ tuần 3.

### Việc của bạn theo tuần
- Tuần 3: tạo tài khoản Google Play Console (25 USD), tìm 12 người thử, thử thông báo trên điện thoại thật (bật Developer options và USB debugging, `adb devices`).
- Tuần 4: tạo tài khoản Cloudflare (miễn phí), gõ 50–100 câu "việc đã làm" thật làm bộ kiểm thử cho Jev.
- Tuần 5: chọn ảnh/mô tả store, duyệt chính sách quyền riêng tư.

## 11. Ai làm gì
| 🤖 Claude | 👤 Bạn |
|---|---|
| Code, test, CI, Trello, `jev-gate`, review, bản release | Cài phần mềm, tài khoản (GitHub, Trello, Play, Cloudflare, typesafe.ai) |
| Bộ mẫu lời nhắn, logic streak, thông báo | Quyết định sản phẩm, tính cách trợ lý, đối tượng người dùng |
| Thẩm định skill từ GitHub | Dán key vào `.env.local`, thử trên máy thật, tìm người thử |

## 12. Quy tắc chung
- Không dán mật khẩu, API key, token vào chat.
- Gặp lỗi thì dán nguyên văn thông báo lỗi.
- Không tạo thêm file ngoài danh sách ở mục 8 nếu chưa hỏi bạn.
