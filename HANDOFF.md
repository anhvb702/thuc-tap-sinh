# HANDOFF: YourRefri

> Cập nhật: **2026-11-25**. Repo `anhvb702/thuc-tap-sinh`; `origin/main` tại **`ea9b74a`** (sau PR #88).
> Sứ mệnh: **Người trợ lý nhỏ trong căn bếp gia đình** — Warm Minimalism, dữ liệu chỉ lưu trên thiết bị (offline-only SQLDelight).
> Đọc file này đầu tiên, rồi các quy tắc bắt buộc trong `.agents/rules/` (`vibe-coding-10-stage-workflow.md`, `clean-architecture-mvi.md`, `test-retry-and-bug-isolation.md`), `DECISIONS.md` và `PLAN.md`. Luôn đối chiếu `git log origin/main -n 10` trước khi làm tiếp.

---

## 1. Tổng kết tiến độ trên `main` (`ea9b74a`)

### Modern UI & Motion Master Plan · P0 hoàn tất, PR #77–#88 đã merge

- **P0 · Design Freeze v2.3:** HTML Mockup gồm **17 màn hình (00–16)**. Figma v2 `H9jGzYBEaeGQ64ZFPp0XGu` đủ **17/17 frame**.
- **PR #77 (`67be092`):** Sửa 5 lỗi phân loại Tủ lạnh và QuickAdd, gồm tách nhóm Gia vị, đồng bộ hai bản `shelf_table.json` và sửa regex «du» cho Dưa leo.
- **PR #78 (`ae8d215`):** Floating Charcoal Pill Bottom Nav, `TabTransitionSpec` và `pressScale`.
- **PR #79 (`a30dff6`):** Warm Hero Card cho `ReminderPreviewPopup`.
- **PR #80 (`5a94a8e`, P1a):** Thông báo `RemoteViews` 3 tầng kiểu Duolingo, `RUBRIC_PASS 21/25`.
- **PR #81 (`80abb5b`, P1b):** Đổi giờ nhắc mặc định ngày thường từ 17:30 sang 16:00.
- **PR #82 (`28f9a09`, P2a–P2b):** Nâng cấp thẻ Tủ lạnh hai cột, mở bottom sheet từ nút ba chấm và thêm khoảng đệm đáy 148dp.
- **PR #83 (`e030a73`, P2c):** CTA một chạm «Xem & nấu ngay» trên Home Spotlight, `ModalBottomSheet` «Cần dùng ngay» và ẩn placeholder thời tiết.
- **PR #84 (`ac44952`, P2d):** Làm tròn theo cách đong trong bếp «khoảng X–Y quả» ở `:shared` và giữ ghi chú gia vị trung thực khi đổi khẩu phần.
- **PR #85 (`9601f83`, P2e–P2f):** Nhãn «CÒN LẠI TRONG TỦ», icon nhóm Cài đặt, công tắc Material 3 và khoảng đệm đáy 148dp.
- **PR #86 (`23df85b`, P2g):** Lối tắt «Thực đơn» hai chiều trên Kho món và công tắc Gacha «Chỉ quay từ đồ đang có» xuyên suốt từ `:shared` đến `HomeViewModel`.
- **PR #87 (`178ed2f`, P3a):** `AnimatedContent` trượt theo hướng cho bước Rảnh tay và ghi chú khẩu phần, hiệu ứng pop-in ở màn nấu xong và `AnimatedVisibility` cho tìm kiếm Tủ lạnh.
- **PR #88 (`ea9b74a`, P3b):** Voice RMS waveform 5 thanh xử lý trên thiết bị, `animateContentSize` và `maxLines = 2` cho Khẩu vị gia đình, `pressScale` cho CTA Home và nút Rảnh tay.

### Sản phẩm & dữ liệu
- **Danh mục đã mở rộng & đồng bộ ảnh 100%:** **42 nguyên liệu** (gồm cả gia vị `nuoc-mam`, `dau-an`, `nuoc-tuong`) và **30 món ăn Việt Nam** (4 bước/món), toàn bộ có ảnh studio đồng bộ (`AssetCoverageTest` kiểm tra 100%).
- **Database SQLDelight v9:** Đã có migration `1.sqm`–`8.sqm` (migration 8 thêm bảng `taste_profile` cho Khẩu vị gia đình); sao lưu/khôi phục JSON v2 tương thích ngược v1.
- **Tủ lạnh (`Pantry`):**
  - Nhãn phân khu bảo quản **Ngăn mát / Ngăn đông · Cần rã đông trước** kèm nhắc rã đông ở chi tiết món & bước 1 nấu rảnh tay (**PR #58**).
  - **Món thừa (`+ Món thừa`)**, **Combo đi chợ 1 chạm (`+ Combo`)** có snackbar xác nhận, **Dọn tủ 1 lần** (xác nhận → xoá hết đồ đang có + Hoàn tác 5 giây), sửa lỗi **Khác / Thêm món** lưu đúng vào tủ qua `AddCustomFood(isLeftover = false)` (**PR #59, #60**).
  - **Nhập giọng nói (Mic):** Đã sửa lỗi deadlock/disable mic bằng cách chuyển `SpeechRecognizer.destroy()` ra khỏi callback đồng bộ qua `Handler(Looper.getMainLooper()).post`, bọc `try/catch`, fallback 1.5s và timeout UI 10s (**PR #60**).
- **Món ăn, Thực đơn tuần & Quay ngẫu nhiên:**
  - **Khẩu vị gia đình:** Thẻ cài đặt gọn có thể thu gọn và nút **Lưu khẩu vị** rõ ràng (`1ac558c`).
  - **Thực đơn tuần:** Cho phép chọn **Bữa trưa / Bữa tối / Cả trưa & tối** (`WeeklyMealOption`) ngay trong bước thiết lập (**PR #61**).
  - **Quay ngẫu nhiên:** Chỉ quay **các món có thể nấu được** (`cookableRecipes`), xác suất đều, tốc độ quay giảm dần mượt 5.2s (cubic ease-out) + phóng sáng `1.08x` ô trúng; hộp quay chỉ giữ nút **Đóng**; sau khi dừng tự chuyển sang màn hình kết quả **"Nay hữu duyên nấu món này nè!"** kèm ảnh món và 2 nút **Quay lại / Xem cách nấu** (**PR #62**).
  - **Nấu rảnh tay & thông báo:** Đồng hồ đếm ngược tròn (`CookTimerCard.kt`), ảnh thành phẩm bước cuối, `LruCache<String, ImageBitmap>(64)` cho thumbnail, thông báo heads-up kèm ảnh món và `ReminderPopupBoxDialog` (**PR #46–#57**).

### Kiến trúc & Quy trình Agent
- **Clean Architecture + MVI + Manual DI (Giai đoạn 1 & 2 đã merge):**
  - **PR #63 (`83c9bc3`):** Thêm `.agents/rules/clean-architecture-mvi.md`, base `MviViewModel<S, I, E>` (`core/mvi/MviViewModel.kt`), và tách Manual DI sang `di/AppContainer.kt` (gắn vào Repository interface thay vì `*Impl`; giảm `MainActivity.kt` từ 379 → 287 dòng).
  - **PR #64 (`0f14d03`):** Refactor feature mẫu **`Home`** theo chuẩn 3 tầng: `:shared/.../home/domain/` (`HomeDashboardData`, `BuildHomeDashboardUseCase`, `PickHomeRandomRecipeUseCase`) + `:composeApp/.../home/presentation/` (`HomeContract`, `HomeViewModel`) + UI mỏng `HomeScreen.kt` (giảm **521 → 162 dòng**, tách thẻ sang `components/HomeSections.kt`).
- **Quy trình 10 giai đoạn Vibe Coding:**
  - **PR #65 (`3d578a7`):** Lưu tại `.agents/rules/vibe-coding-10-stage-workflow.md`, ánh xạ rõ vai trò đội ngũ agent hiện tại (Antigravity Tech Lead + Codex CLI `gpt-5.6-luna` + subagents + `.agents/roster.json`) cùng 5 nguyên tắc bắt buộc và mẫu báo cáo 7 mục.

---

## 2. Bằng chứng kiểm chứng gần nhất (2026-11-25)

- **Unit test:** **1.126/1.126 PASS**, gồm **550/550** ở `:shared` và **576/576** ở `:composeApp`, với **0 lỗi, 0 bỏ qua**.
- **P4 Full Regression** trên `emulator-5554`, Claude CLI ghi nhận **VISUAL_PASS**.
  - **Dark Mode:** VISUAL_PASS trên Home, Tủ lạnh và Cài đặt.
  - **Reduce Motion** (`animator_duration_scale=0`): 0 crash/error trong `logcat *:E`.
  - **Cô Hạnh, cỡ chữ 1.35x:** Tủ lạnh chuyển sang một cột gọn, Cài đặt hiển thị đầy đủ chữ.
  - **Ba điểm chỉnh nhẹ cho đợt sau:** Chip «Chưa chế biến» hơi dài ở 1.35x và hiển thị «Chưa chế ...», dòng phụ màu xám cần tăng độ tương phản trong Dark Mode, và hai mục trong Cài đặt cùng mang nhãn «Khẩu vị gia đình».

---

## 3. Môi trường & Lưu ý kỹ thuật quan trọng

- **Toolchain Windows:**
  - `JAVA_HOME = C:\Setups\JavaJDK\jdk-21.0.9`
  - `ANDROID_HOME = C:\Users\anhvb2\AppData\Local\Android\Sdk`
  - `ADB = C:\Users\anhvb2\AppData\Local\Android\Sdk\platform-tools\adb.exe`
  - Gradle 8.5, AGP 8.3.2, Kotlin 2.2.20, Compose Multiplatform 1.7.3, compile/target SDK 34, minSdk 24.
- **Quy tắc bắt buộc khi thao tác repo:**
  - **Không commit hoặc ghi đè `gradle.properties`** ở root checkout (đang có tinh chỉnh RAM cục bộ của máy).
  - **Chỉ chạy 1 tiến trình Gradle tại một thời điểm** (`--no-configuration-cache --max-workers=1`).
  - **Tối đa 3 lần thử** cho mỗi lỗi test/build; quá 3 lần phải dừng khoanh vùng root cause.
  - Luôn gọi `lint-copy.ps1` kèm `-Path $files` (tránh treo chờ stdin).
  - Luôn dùng `--body-file` (UTF-8 không BOM) khi tạo PR bằng `gh pr create` trên PowerShell.
  - Xoá worktree trên Windows khi gặp lỗi đường dẫn dài: `cmd /c "rmdir /s /q \\?\<path>"` rồi `git worktree prune`.

---

## 4. Điểm tồn tại & Việc tiếp theo (Ưu tiên)

1. **Cài & kiểm tra runtime trên emulator (`emulator-5554`):** Build lại `:composeApp:assembleDebug` từ `main` (`3d578a7`) và cài APK khi máy ảo bật để kiểm chứng thực tế 7 bản sửa lỗi UX (#60–#62) + màn `Home` MVI (#64).
2. **Giai đoạn 3 Clean Architecture + MVI (Rã God Screens):**
   - Tách `PantryGridScreen.kt` (**1.656 dòng**) và `RecipeScreen.kt` (**696 dòng**) theo đúng chuẩn mẫu `Home` (`domain/usecase` + `presentation/Contract & ViewModel` + `ui/components`).
   - Giảm prop drilling ở `MainScaffold.kt` (**476 dòng**) và chuyển `PantryGridViewModel` sang `ViewModelProvider.Factory` trong `AppContainer`.
3. **Dọn cấu hình Agent phụ:**
   - Gỡ trường `reasoning_effort` cũ trong `.codex/agents/*.toml` để hết cảnh báo `Ignoring malformed agent role definition` khi khởi động Codex CLI `v0.167.2`.
   - Đồng bộ lại tên model trong `.agents/roster.json` nếu muốn kích hoạt script `orchestrate.ps1`.
4. **Rủi ro phát hành còn mở (kế thừa từ bản trước):**
   - `targetSdk 34` (cần API 36 + keystore + AAB trước khi lên Google Play).
   - Bộ lọc chế độ ăn chỉ kiểm nguyên liệu chính (không trình bày như cam kết dị ứng).
   - Chưa kiểm thực tế trên máy thật Xiaomi/Oppo/Vivo/Samsung cho thông báo nền & TalkBack/cỡ chữ 2.0.
