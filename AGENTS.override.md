# YourRefri: hướng dẫn cho Codex

File này dành cho Codex và thay `AGENTS.md` (bản sinh bởi pullboard, không dùng cho dự án này). Đọc `HANDOFF.md` đầu tiên để biết đang làm tới đâu. Không chạy lệnh `pullboard`.

## Triết lý Sản phẩm: "Người trợ lý nhỏ trong căn bếp gia đình"
YourRefri KHÔNG PHẢI là phần mềm quản lý kho hàng thu nhỏ. YourRefri là người bạn đồng hành ấm áp của người nội trợ, giúp:
- **Nhìn thấy:** Mở app 5 giây biết tủ lạnh đang có gì.
- **Ghi nhớ:** Nhẹ nhàng ưu tiên món cần dùng sớm.
- **Gợi ý:** Trả lời *"Hôm nay ăn gì?"* bằng đúng 3 món thiết thực.
- **Tiết kiệm:** Giảm thức ăn bỏ phí, tiết kiệm tiền đi chợ cuối tháng.
- Không bắt người dùng dành thêm thời gian chăm sóc ứng dụng. Thao tác 1–2 chạm, có nút `+`/`-` số lượng, không bắt gõ chữ dài dòng.

## Ràng buộc cứng
- Dữ liệu chỉ trên máy (SQLDelight). Không tài khoản, backend, analytics hay crash SDK.
- Mọi tính năng chạy đầy đủ offline. Có phương án sao lưu JSON về máy.
- Hạn dùng luôn ghi "ước tính"; không khẳng định an toàn thực phẩm. Đồ quá hạn hiển thị: "kiểm tra lại xem còn dùng được không".
- Không tạo file ngoài danh sách cần thiết hoặc merge khi chưa hỏi người dùng.
- Không đọc hay in nội dung `.env*`, keystore, `local.properties`, file service account.

## Phong cách Thiết kế & Văn phong (Warm Minimalism & Impeccable)
- **Phong cách:** Warm Minimalism theo `DESIGN.md`. Giao diện như một cuốn sổ tay căn bếp gia đình mộc mạc, tĩnh lặng.
- **Bảng màu chuẩn:** Warm Ivory (`#F7F5EF`), Sage Green (`#789477`), Soft Green (`#DDE8D9`), Warm Beige (`#E8D8B9`), Terracotta (`#D98C70`), Deep Charcoal (`#343B34`).
- **Quy tắc Anti-Slop (Impeccable):** Cấm gradient tím xanh vũ trụ, cấm card lồng card 3 tầng, cấm icon hoạt hình 3D sặc sỡ. Vùng chạm tối thiểu 48dp, bo góc mượt mà 16dp.
- **Văn phong:** Ấm, ngắn, gợi ý không ra lệnh, luôn có đường lui. Cấm: "bạn quên", "lãng phí", "bỏ lỡ", "hết hạn", điểm số, so sánh, streak. Dùng: "ước tính", "khoảng", "nên dùng trước". Thông báo tối đa 1 lần/ngày lúc 17h30.

## Kiến trúc
- `:shared` (data + logic thuần, chỉ androidTarget) và `:composeApp` (UI, ViewModel).
- Package `com.loitu.yourrefri.feature.<tên>.{data,logic}` trong `:shared`; `...feature.<tên>.ui` trong `:composeApp`. `logic` là hàm thuần, không import SQLDelight.
- MVI: State / Action / Effect + ViewModel + StateFlow.
- Navigation Compose type-safe, tham số route chỉ kiểu nguyên thủy, mọi route trong `composeApp/.../nav/Routes.kt`.
- Luôn tiêm `Clock` (là `kotlin.time.Clock`) và `TimeZone`; không gọi giờ hệ thống trực tiếp.
- Công thức là JSON bundle đọc lúc chạy, không vào DB.
- Thông báo: `AlarmManager` không chính xác (`setAndAllowWhileIdle` / `setWindow`), đặt lại sau reboot và đổi múi giờ; mở app luôn thấy nội dung.

## Cách làm mỗi task (ba bước theo thứ tự, phân vai theo Team agent)
1. **Thiết kế test trước.** Nếu `docs/tests/<mã>.md` và test đã có thì dùng nguyên, đó là hợp đồng. Nếu chưa có, viết spec (tên test, đầu vào, kết quả cần đạt, mutation nó bắt) rồi viết test, chạy thấy đỏ vì thiếu code.
2. **Viết code cho test xanh.** Không sửa, xóa, `@Ignore` hay làm yếu test. Nếu cho rằng test sai, dừng và báo người dùng. Task nhỏ, đúng chữ ký trong spec, tối đa khoảng 300 dòng diff.
3. **Tự kiểm.** Chạy test; làm sai code có chủ đích ít nhất 6 kiểu (đảo dấu, đổi biên, bỏ ghi đè, ...) để xem test có đỏ không; khôi phục nguyên trạng và chạy lại xanh. Test nào không bắt được mutation nào thì báo là test yếu. Ghi biên bản ngắn: tiêu chí | đạt | bằng chứng (lệnh đã chạy, kết quả thật).
- Thêm thư viện thì ghi một dòng: vì sao cần, kéo theo gì.
- Comment chỉ giải thích lý do, không mô tả lại code; không để code bị comment.
- Báo thật: test đỏ thì nói đỏ, chưa chạy thì nói chưa chạy.

## Điều phối team agent

**Jev (Orchestrator, TypeSafe System One):** nhận yêu cầu đầu vào, gọi `scripts/jev.ps1` (model `jev-latest`) để chọn vai bằng `choice`, chấm mức sẵn sàng bằng `score`, kiểm tra khẳng định bằng `noul`. Jev chỉ phân loại và chấm điểm, không sửa code. Nếu Jev lỗi hoặc `confidence` thấp, Não quyết định theo bảng dưới. Không gửi secret hay dữ liệu cá nhân cho Jev. Định nghĩa vai: `.claude/agents/jev.md`.

Phân vai theo provider (chốt bởi người dùng). Mỗi provider một mảng việc:

| Vai | Provider | Model (chọn theo tài liệu provider, xác nhận ID trước khi dùng) | Phạm vi |
| --- | --- | --- | --- |
| **Jev — Điều hướng** | TypeSafe | `jev-latest` | Phân loại yêu cầu, chọn vai (`choice`), chấm sẵn sàng (`score`), kiểm khẳng định (`noul`). Không sửa file. |
| **Planner** | Claude | `claude-opus-5-5` (cao nhất khả dụng) | Phân tích, chia task (≤300 dòng diff), thiết kế contract và 6 mutation, quyết định nghiệm thu. Đây là vai duy nhất của Claude. |
| **Nghiên cứu thị trường / Phát triển ý tưởng** | Gemini | model Pro mới nhất, dùng grounding/search | Tìm đối thủ, nhu cầu thị trường, phát triển và so sánh ý tưởng; đầu ra là brief, không code. |
| **Coding (executor)** | Codex | `gpt-6-luna`, reasoning `low` | Sửa đúng file được giao theo contract; không sửa hoặc làm yếu test. |
| **Reviewer / Verifier** | Codex | `gpt-6.1-terra`, reasoning `high` | Review độc lập contract, diff, bằng chứng và mutation; khác model với executor để không tự chấm bài mình. Qodo vẫn review PR; không làm trùng. |
| **Advisor (trade-off)** | Gemini | model Pro mới nhất | Ý kiến độc lập về rủi ro và phương án; chỉ phân tích, không sửa file. |
| **Brainstormer (chia task UX/logic)** | Gemini | model Flash mới nhất | Phương án và chia task với acceptance, owner, dependency; không code. |
| **DocBot** | Codex | `gpt-6-luna`, reasoning `low` | Chỉ sửa `HANDOFF.md`, `PLAN.md`, `SPEC.md`, `DECISIONS.md`, `docs/tests/*.md`, bằng chứng thật. |
| **Persona khách hàng** | Gemini | model Flash mới nhất | Đánh giá UX/ngôn từ theo Chị Mai, Anh Nam, Cô Hạnh và `PRODUCT.md`; không code, không review kỹ thuật. Không coi walkthrough giả lập là nghiên cứu thật. |

Quy tắc:
- Claude không nhận coding, review, nghiên cứu hay chia việc ngoài vai planner.
- Jev chỉ điều hướng; quyết định cuối khi Jev lỗi hoặc `confidence` thấp thuộc về planner (Claude).
- Chỉ một agent chạy Gradle tại một thời điểm. Reviewer đọc kết quả và báo lại planner.

### Luồng làm việc và chạy song song

- Não luôn soạn contract trước: **Task ID, mục tiêu, Input (state/action/fake/clock), Expected Output** (StateFlow, Effect, DB/repository), biên/rủi ro và sáu mutation phù hợp. `Expected Output` phải là tiêu chí có thể quan sát và dùng để verify.
- Khi câu hỏi còn mơ hồ hoặc có trade-off sản phẩm/contract đáng kể, Não bàn luận nội bộ với một Advisor và một Brainstormer; không giao việc đó cho nhiều agent hơn mức cần thiết. Não tổng hợp kết luận, rồi gửi quyết định cùng bằng chứng cho Planner/Criteria để lập danh sách task, thứ tự phụ thuộc và quyền sở hữu file. Contract test hiện hữu và chỉ thị rõ của người dùng có quyền cao hơn đề xuất persona hoặc brainstorm; không âm thầm sửa contract để làm vừa feedback.
- Task lớn được chia thành phần độc lập, mỗi agent có file sở hữu rõ ràng. Chạy song song ngay khi không phụ thuộc đầu ra nhau và không sửa trùng file; xếp task phụ thuộc thành các đợt nối tiếp. Không đặt giới hạn nhân tạo cho số task/agent; dùng hết slot đồng thời nền tảng cho phép rồi tiếp tục đợt kế.
- Tự động chạy unit test bằng Gradle CLI sau khi code/test hoàn tất; đây là cùng Gradle task mà Android Studio gọi. Dùng PowerShell và môi trường trong mục bên dưới. Với thay đổi `:shared`, chạy `:shared:testDebugUnitTest`; với thay đổi `:composeApp`, chạy `:composeApp:testDebugUnitTest`; nếu ảnh hưởng cả hai, chạy cả hai task tuần tự. Không cài thêm package KMM/Android test chỉ để chạy test.
- Chỉ một agent chạy Gradle test/build/mutation tại một thời điểm trên workspace dùng chung. Ghi task chính xác, kết quả thật và lỗi nếu có; Reviewer đọc kết quả và báo về Não. Không tự kết luận xanh nếu lệnh chưa chạy hoặc thất bại.
- Không bắt buộc gọi đủ mọi agent cho task nhỏ. Dùng Docs khi cần cập nhật tài liệu, Customer khi thay đổi trải nghiệm/ngôn từ, Reviewer cho thay đổi logic/contract hoặc khi cần xử lý Qodo.
- Không tự tạo mục tiêu/nhánh/PR hay merge ngoài phạm vi giao. Không chạy `pullboard`.

## Định tuyến skill

Skill liên quan đã có trong `.agents/skills/`; nạp theo nhu cầu, không đọc toàn bộ cho mỗi task:

| Khi cần | Skill | Vai trò |
| --- | --- | --- |
| Thiết kế test contract hoặc mutation | `test-mutation-spec` | Não thiết kế; Reviewer kiểm chứng |
| Tham khảo tác vụ Gradle KMM/Android Studio | `kmm-gradle-workflow` | Executor/Reviewer; ưu tiên Android Studio theo quy trình người dùng |
| Đọc và xử lý comment Qodo | `qodo-review-workflow` | Reviewer trích finding; Não quyết định task sửa |
| Thẩm định trải nghiệm gia đình | `customer-ux-evaluation` | Chỉ Customer Persona |
| Tạo/chỉnh UI Compose | `impeccable` | Executor dùng thêm design vocabulary và anti-slop |
| Rà rủi ro lưu trữ/luồng dữ liệu cục bộ | `security-threat-model` | Não thiết kế threat model; Reviewer xác minh phạm vi và mức độ |
| Rà manifest, permission, receiver/service hoặc Intent/PendingIntent | `android-permissions-security`, `android-intent-security` | Reviewer kiểm tra khi task chạm đúng bề mặt Android; không thêm dependency mặc định |

Đã rà danh mục marketplace Codex và GitHub: marketplace không có skill Kotlin/KMM phù hợp; Android Skills có skill `testing-setup` nhưng nhắm tới dựng thêm testing infrastructure/dependencies nên không cài vì dự án đã có test và người dùng chạy Android Studio. Không cần tải package KMM. Trước khi cài skill mới, kiểm tra nguồn chính thức/chuyên ngành và chỉ cài nếu phù hợp, không trùng skill hiện có, không áp đặt dependency hoặc quy trình xung đột. Skill ngoài không ghi đè ràng buộc repo.

Security skills đã cài ở Codex user skills từ nguồn chính thức: `security-threat-model` (OpenAI Skills), `android-permissions-security` và `android-intent-security` (Android Skills). `security-best-practices` trong marketplace hiện chỉ hỗ trợ Python/JS/TS/Go nên không chọn cho Kotlin; `testing-setup` cũng không phù hợp vì có thể cài thêm framework/dependency. `impeccable` và `customer-ux-evaluation` đã có sẵn trong repo.

## Kết nối giữa phiên

Dùng `HANDOFF.md` làm điểm đồng bộ trạng thái; luôn đối chiếu working tree và bằng chứng hiện tại trước khi khẳng định tiến độ. Nội dung handoff cũ không thay thế kết quả test hiện tại.
## Môi trường (Windows, PowerShell)
```powershell
$env:JAVA_HOME = "E:\Dev\Java\jdk-21.0.13+9"
$env:ANDROID_HOME = "E:\Dev\Android\Sdk"
.\gradlew.bat :shared:testDebugUnitTest      # test (không có allTests)
.\gradlew.bat :composeApp:assembleDebug      # build APK debug
```
- Chỉ build Android cục bộ. Cache Gradle chung ở `C:\Users\loitu\.gradle`, đừng xóa.
- Test của `:shared` nằm ở `commonTest` (logic thuần) và `androidUnitTest` (SQLDelight in-memory bằng `JdbcSqliteDriver`).
- Sandbox của Codex cần quyền ghi vào thư mục repo và `C:\Users\loitu\.gradle` để Gradle chạy được.
- File tải về đặt trong `downloads\` (mỗi lần một thư mục con) và phải hỏi trước.

## Mốc hiện tại
Xem phần tiến độ cập nhật ở đầu `HANDOFF.md`, rồi đối chiếu working tree, test và PR trước khi kết luận trạng thái. Không dùng các mốc cũ hơn trong handoff làm bằng chứng hiện tại.
