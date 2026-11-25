---
trigger: always_on
---

# Quy Tắc 4 Cổng Nghiệm Thu & Giao Tính Năng (Feature Delivery Gates)

Mọi đề xuất và tính năng mới trên YourRefri bắt buộc đi qua đủ 4 cổng sau theo đúng thứ tự:

1. **Cổng 1 — Expert Check trước khi chốt đề xuất & viết code:**
   - Không vội chốt hay code ngay khi nhận ý tưởng.
   - Đưa đề xuất qua thẩm định đa góc nhìn: **UX Persona gia đình** (skill `customer-ux-evaluation`), **JEV** (`scripts/jev.ps1`) chấm độ rõ ràng & văn phong tiếng Việt, và **chuyên gia kỹ thuật (Claude / Codex)** phản biện kiến trúc, edge-case & trade-off.
   - Trình bày kết quả phản biện và chỉ lập trình sau khi người dùng xác nhận (`OK` / `APPROVED`).

2. **Cổng 2 — Thiết kế Checklist nghiệm thu bắt buộc (`task-verify-checklist`):**
   - Áp dụng skill `task-verify-checklist` để thiết kế đầy đủ checklist khung **A → G** (Test đỏ, lệnh Gradle, Bảng test Input/Expected Output, 6 Mutation chỉ rõ `file:hàm`, Biên & rủi ro, Mục kiểm tay UI/copy, Biên bản kết quả thật).
   - Mọi mục trong checklist là hợp đồng bắt buộc phải đạt **100%** trước khi chuyển sang bước commit/PR/merge.

3. **Cổng 3 — Review chéo Code & Kiểm chứng Ảnh Emulator bởi Model khác (Cross-Model Verify):**
   - **Code:** Review chéo độc lập (Claude review `:composeApp` của Codex; Codex `high` review `:shared` của Claude); sửa hết finding P1/P2 trước khi nghiệm thu.
   - **Ảnh giao diện (bắt buộc với mọi task UI):** Chụp đủ mọi trạng thái UI thật trên emulator và **đưa trực tiếp file ảnh PNG cho một model khác độc lập kiểm tra** (ví dụ Claude CLI đọc ảnh bằng `Read` hoặc Codex CLI đọc ảnh bằng `--image`) về bố cục, căn lề, cắt chữ ở cỡ chữ lớn, độ tương phản và độ khớp checklist. Không tự nghiệm thu bằng một model duy nhất.

4. **Cổng 4 — Hoàn tất gọn từng tính năng (Commit → Push → PR → Merge):**
   - Chỉ khi đạt 100% checklist (Test pass, 6/6 Mutation KILLED, lint-copy PASS, ảnh đã được model khác xác nhận) mới thực hiện **commit → push → tạo PR kèm biên bản → merge vào `main` → đồng bộ `main` cục bộ** rồi mới mở nhánh cho tính năng tiếp theo.
   - Tuyệt đối không đưa file cấu hình máy cục bộ (`gradle.properties`) hoặc file nháp ở thư mục gốc vào commit/PR.
