---
name: task-verify-checklist
description: Sinh checklist verify (test đỏ, lệnh Gradle, bảng test, 6 mutation, mục tay, biên bản trống) cho một task YourRefri từ mã task hoặc mô tả. Dùng khi task cần nghiệm thu, khi viết hoặc kiểm `docs/tests/<ID>.md`, hoặc khi người dùng hỏi "checklist verify cho task này".
---

# Sinh checklist verify cho task YourRefri

## Mục tiêu
Từ một task, tạo checklist nghiệm thu cụ thể, bám `CLAUDE.md` (DoD, lệnh Gradle) và `docs/tests/_TEMPLATE.md`. Checklist phải đủ để người khác chạy lại và kiểm chứng, không chỉ là danh sách việc.

## Bước 1: Lấy nguồn
1. Nếu có mã task (ví dụ `W1-07a`, `UI-02`, `A-03`) thì tìm `docs/tests/<ID>.md`. Nếu có, đó là hợp đồng: dùng nguyên bảng Test, biên và 6 mutation, không tự đổi tiêu chí. Tên mục trong spec cũ có thể khác `_TEMPLATE.md` (ví dụ A-03 dùng "Mục tiêu", "Input và Expected Output", "Test", "Execution record"); ánh xạ theo nội dung, không theo tên mục.
2. Nếu chưa có spec, đọc mô tả task và file liên quan (`git diff`, file đang sửa). Ghi rõ trong đầu ra: "Chưa có spec, checklist này là đề xuất, cần Planner duyệt trước khi coi là hợp đồng."
3. Không đọc hay in nội dung `.env*`, keystore, `local.properties`, file service account.

## Bước 2: Phân loại task
Chọn mọi loại đúng, một task có thể thuộc nhiều loại:

| Loại | Dấu hiệu | Mục bắt buộc thêm |
| --- | --- | --- |
| Logic/data | Hàm thuần trong `feature/<tên>/logic`, repository, SQLDelight | Test `:shared:testDebugUnitTest` (commonTest hoặc androidUnitTest) |
| ViewModel/presentation | `*ViewModel`, `*Reducer`, `*Presentation.kt`, `*Copy.kt` | Test `:composeApp:testDebugUnitTest` |
| UI Compose | Màn hình, composable, thay đổi giao diện | Ảnh chụp emulator; `:composeApp:assembleDebug` |
| Thông báo/thời gian | `feature/notice/logic`, `composeApp/.../reminder/`, múi giờ, lên lịch lại | Checklist tay (xem bước 4) |
| Migration DB | Thêm file `migrations/<n>.sqm` | Test mở DB cũ (`JdbcSqliteDriver` in-memory) |
| Sao lưu | `BackupCodec.kt`, `PantryBackupRepository` | Test đọc bản JSON cũ (có `version`) và khôi phục trong một transaction |
| Danh mục/công thức | `shelf_table.json`, `recipes.json` | Sửa cả hai bản `shelf_table.json` (assets và `shared/src/commonMain/resources`) |
| Chữ hiển thị | Chuỗi người dùng thấy | Lint văn phong (bước 4) |

## Bước 3: Viết checklist
Dùng đúng khung sau, theo thứ tự. Mục nào không áp dụng thì ghi "Không áp dụng, vì ..." thay vì bỏ trống.

```markdown
## Checklist verify: <ID> — <tên task>

### A. Trước khi code
- [ ] Expert check đề xuất (UX Persona `customer-ux-evaluation` + JEV văn phong + phản biện kỹ thuật Claude/Codex) đã được người dùng chốt.
- [ ] Test đỏ: chạy `<lệnh>`, kết quả mong đợi: <số test đỏ, tên test>. Ghi output thật vào Biên bản.
- [ ] Spec có tiêu chí cho mọi hành vi trong mục Task (nếu thiếu, dừng và báo Planner).

### B. Sau khi code (lệnh, chạy đúng thứ tự, một tiến trình Gradle tại một lúc)
- [ ] **File Ownership Check:** chạy `git status --short` và `git diff --stat` xác nhận 0 chạm file ngoài phạm vi được giao và 0 stage `gradle.properties` / file nháp.
- [ ] `$env:JAVA_HOME` và `$env:ANDROID_HOME` đã đặt (xem `AGENTS.override.md`, mục Môi trường).
- [ ] `.\gradlew.bat :shared:testDebugUnitTest`  (nếu đổi `:shared`)
- [ ] `.\gradlew.bat :composeApp:testDebugUnitTest`  (nếu đổi `:composeApp`)
- [ ] `.\gradlew.bat :composeApp:assembleDebug`
- [ ] Chạy lại thật (không lấy cache): thêm `--rerun-tasks --no-configuration-cache --max-workers=1`, khi cần kết quả không cache.
- [ ] Chạy một lớp: thêm `--tests '<package>.<LớpTest>'`.
- [ ] Đọc kết quả trong `<module>/build/test-results/testDebugUnitTest/*.xml`: số test, lỗi, bỏ qua (phải 0 lỗi, 0 bỏ qua).
- [ ] **Codex `high` soi chéo diff** (với `:shared` hoặc logic đa file): tìm 5 edge-case dễ gây bug nhất và viết test tự động kiểm chứng.
- [ ] **Automated Proof Report:** chạy `.\scripts\jev.ps1 -Verify` xuất `docs/PROOF_REPORT.md` (≤ 25 dòng) đạt `READY_TO_MERGE`.

### C. Bảng test theo tiêu chí
| Test | Input | Expected Output | Mutation bắt |
| --- | --- | --- | --- |
| `tenTest_dieuKien` | ... | StateFlow/Effect/DB quan sát được | ... |

Lấy từ spec nếu có. Nếu chưa có, đề xuất ít nhất: một ca bình thường, một ca biên, một ca rỗng/âm.

### D. Sáu mutation (làm sai code có chủ đích, mỗi kiểu phải làm test ĐỎ, rồi khôi phục và test XANH)
1. Đảo cờ logic: <file:hàm cụ thể của task>
2. Lệch cận số/thời gian: <cận cụ thể>
3. Bỏ ghi hoặc bỏ phát sự kiện: <dòng gọi repository/emit>
4. Trả mặc định/rỗng: <hàm trả emptyList()/null>
5. Đổi thứ tự ưu tiên/sort: <comparator cụ thể>
6. Bỏ khôi phục/undo: <nhánh undo cụ thể, nếu task không có undo ghi "Không áp dụng, vì ...">

Ghi rõ file và hàm, không ghi chung chung "đổi logic".

### E. Biên và rủi ro
- Rỗng, âm, trùng, dữ liệu lớn, mất quyền, múi giờ/DST (nếu task có thời gian), chuyển bản DB/backup (nếu có).

### F. Mục tay (Quy tắc 3 Phút Nghiệm Thu Cuối)
- [ ] (UI) Ảnh chụp emulator: màn hình, trạng thái, kích thước điện thoại; lưu file ảnh vào `docs/` hoặc đường dẫn người dùng chỉ định.
- [ ] (UI — Cross-Model Visual Verify) Gửi trực tiếp các file ảnh PNG chụp từ emulator cho một model khác độc lập (Claude CLI `Read` hoặc Codex CLI `--image`) kiểm tra bố cục, cắt chữ, căn lề và độ khớp checklist.
- [ ] (Integration Seam) Kiểm tra khớp nối giữa 2 màn: dữ liệu sửa ở màn A chảy đúng định dạng sang màn B.
- [ ] (UX Feel & 1.35×) Kiểm tra cỡ chữ lớn Cô Hạnh `1.35×` và đảm bảo không bị che nút bởi Floating Pill Nav / FAB / bàn phím ảo.
- [ ] (Kịch bản phá đám) Bấm đúp liên tục nút chính, nhập chuỗi rỗng/ký tự đặc biệt, hoặc tắt mạng giữa chừng.
- [ ] (Thông báo/giờ) Đổi múi giờ, khởi động lại máy, tắt thông báo hệ thống: nội dung vẫn thấy khi mở app.
- [ ] (Chữ hiển thị) `scripts/lint-copy.ps1 -Path <file>` không báo từ cấm.

### G. Biên bản (chỉ điền bằng kết quả thật)
| Kiểm tra | Kết quả | Bằng chứng (lệnh, kết quả) |
| --- | --- | --- |
| Expert check đề xuất | Chưa chạy | |
| File Ownership Check (`git status` + `diff --stat`) | Chưa chạy | |
| Test đỏ trước khi có code | Chưa chạy | |
| `:shared:testDebugUnitTest` | Chưa chạy | |
| `:composeApp:testDebugUnitTest` | Chưa chạy | |
| `:composeApp:assembleDebug` | Chưa chạy | |
| Mutation 1-6 | Chưa chạy | |
| Review chéo code + Visual verify ảnh (model khác) | Chưa chạy | |
| `docs/PROOF_REPORT.md` (≤ 25 dòng, `jev.ps1 -Verify`) | Chưa chạy | |
| Ảnh emulator (task UI) | Chưa chạy | |
| Known Risks (≤ 2 điểm cần lưu ý) | Chưa ghi | |
```

## Bước 4: Quy tắc khi điền
- Chỉ ghi "Đạt", "Bị bắt" hoặc "Sống sót (test yếu)" khi đã chạy lệnh và có output thật. Chưa chạy thì ghi "Chưa chạy". Chưa xác minh được thì ghi "Chưa xác minh".
- Không sửa, xóa, `@Ignore` hay làm yếu test cũ. Nếu nghi test sai, dừng và báo Planner.
- Test hoặc build lỗi: thử tối đa 3 lần, rồi khoanh vùng nguyên nhân (log, stack trace, file:dòng; phân biệt code nguồn, test cũ, môi trường) trước khi sửa tiếp. Theo `.agents/rules/test-retry-and-bug-isolation.md`.
- Chỉ build Android cục bộ. Không cài thêm package KMM hay Android test.
- Văn phong: không dùng từ cấm trong `CLAUDE.md` (mục Văn phong) khi viết ghi chú tiếng Việt cho người dùng. Dùng "ước tính", "khoảng", "nên dùng trước".
- Không tạo file ngoài đường dẫn người dùng yêu cầu. Checklist mặc định trả trực tiếp trong câu trả lời; chỉ ghi vào `docs/tests/<ID>.md` khi được yêu cầu.

## Bước 5: Đầu ra
- Một khối Markdown theo khung bước 3, đặt trong câu trả lời để dán được vào spec hoặc PR.
- Dòng đầu ghi nguồn: "Nguồn: docs/tests/<ID>.md" hoặc "Nguồn: mô tả task (chưa có spec)".
- Kết thúc bằng một dòng: số mục đã điền và số mục còn "Chưa chạy".
