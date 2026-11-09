# HANDOFF: YourRefri

> Cập nhật: **2026-11-09**. Repo `anhvb702/thuc-tap-sinh`, nhánh `codex/yourrefri-baseline`.
> Sứ mệnh: **Người trợ lý nhỏ trong căn bếp gia đình** — giao diện Warm Minimalism, bốn tab, thao tác gọn, dữ liệu chỉ lưu trên thiết bị.

## Snapshot hiện tại

- **W6-01 & W6-02 (Thực đơn tuần 7 ngày & Đi chợ 1 chạm — Zero-Input Pantry Loop):** triển khai `WeeklyMealPlanner.kt` (`:shared`) và `WeeklyMealPlanPresentation.kt` + chế độ `RecipeScreenMode.WEEKLY` (`:composeApp`) theo góp ý PR #5. Hỗ trợ gợi ý thực đơn 7 ngày (`Thứ 2` → `Chủ nhật`) theo `DietGoal` và `FamilyPreferenceTag` không lặp món ngày liền kề, đổi món từng ngày (`Đổi món ↻`), tự động tổng hợp danh sách đi chợ tuần có đếm số bữa và nút `"Đã mua xong (n món) · Cất vào tủ lạnh 🧊"` tự động thêm nguyên liệu vào tủ lạnh (`PantryGridAction.AddIngredient`). Suite unit test hiện đạt **338 `:shared` tests** và **106 `:composeApp` tests** (0 failures/errors); xem `docs/tests/W6-01.md` và `docs/tests/W6-02.md`.
- **UX-02 (Home pantry preview):** tối đa ba tên món đang có theo thứ tự catalog. Sáu mutation đều làm test đỏ; chi tiết và kết quả ở `docs/tests/UX-02.md`.
- **UX-03 (Recommendation copy):** Home và Recipes dùng chung nhãn mời gọi. Sáu mutation đều làm test đỏ; chi tiết ở `docs/tests/UX-03.md`.
- **UX-05 (Quick-add multi-select):** sáu mutation helper đều bị bắt; bộ lọc group/query, chọn nhiều món, nhãn số lượng và lưu món đã có trong source. `markedAt` sort không đảm bảo thời điểm hết thực tế vì reducer giữ timestamp cũ. Chưa có Compose interaction/runtime test cho multi-select; xem `docs/tests/UX-05.md`.
- **UX-06 (Recipe bundle):** bundle có 10 công thức, sáu mutation coverage đều bị bắt. Wording optional được chỉnh sau probes; không rerun mutation vì không chạm targets. Xem `docs/tests/UX-06.md`.
- **Feedback P0/P1 ngày 2026-11-08:** source đã cập nhật quick-add, xác nhận nấu, truyền ID recipe từ Home và bố cục thẻ/lưới Pantry. UX-04 bắt **6/6 mutation ở helper/presentation**; test này chưa instrument Compose call-site, thao tác chạm hay runtime layout. Source review xác nhận UI hiện gọi các helper tương ứng. Xem giới hạn và bằng chứng tại `docs/tests/UX-04.md`.
- **UX-04/05 runtime prep ngày 2026-11-09:** checklist nghiệm thu thủ công đã được ghi trong `docs/tests/UX-04.md` và `docs/tests/UX-05.md`. Các thao tác chưa chạy trên thiết bị/emulator; đây không phải kết quả instrumentation hay runtime verification.
- **Thử runtime ngày 2026-11-09:** `:composeApp:assembleDebug` thành công. AVD `YourRefri_API36_Fresh` không khởi động Android Package Manager; `adb install -r` trả `Can't find service: package`, nên app chưa cài/chạy và không bước checklist UI nào được xác minh. Chi tiết trong UX-04/05.
- **Test sau code:** `:composeApp:testDebugUnitTest --offline` — exit 0, BUILD SUCCESSFUL; XML **102 tests, 0 failures, 0 errors**. Đây là suite cuối sau chỉnh chip reset và wording optional. UX-05/06 mutation chạy trước hai chỉnh sửa không chạm target; source được khôi phục sau từng probe.
- **W5-02B:** UI lọc thời gian dùng “Tối đa 15 phút” và “Tối đa 30 phút”, inclusive theo lựa chọn ngày 2026-11-08; hợp đồng và biên bản ở `docs/tests/W5-02B.md`.
- **W4-03:** kiểm thử contrast/font có trong Compose suite; runtime ở 360dp, font scale lớn, TalkBack và tên dài chưa xác minh. Không coi test source hoặc ảnh cỡ mặc định là xác nhận runtime các tiêu chí này.

## Runtime và giới hạn bằng chứng

Ảnh runtime cũ trong `build/verification_reports/` là bằng chứng từ phiên trước, không phải xác minh lại trong phiên này. Snapshot P11 từng cho thấy Pantry mặc định với catalog, trạng thái và thao tác số lượng. Thử nghiệm 360dp/fontScale 1.3 và 2.0 trước đây không ổn định; TalkBack chưa được bật. Notification thực tế và restore qua picker end-to-end cũng chưa được xác minh. Persona report là walkthrough giả lập từ source, không phải nghiên cứu người dùng thật.

## Trạng thái dự án

- Bốn khu vực Home, Pantry, Recipes, Settings đã có source; lưu trữ cục bộ SQLDelight và backup thủ công qua tệp.
- W1–W4 có implementation data/logic; giới hạn runtime W4-03 nêu trên vẫn mở.
- W5-01 là source walkthrough persona. Release APK từng được build unsigned; cần signing key do chủ dự án quản lý để ký.
- Thay đổi hiện tại còn ở working tree; chưa push hoặc merge. Qodo đã phụ trách review PR; các finding đã xử lý tại local cần được cập nhật PR sau khi có quyết định push.

## Việc còn lại

1. Nếu cần nghiệm thu UI end-to-end cho UX-04/05, bổ sung test Compose instrumentation cho call-site, interaction và runtime layout; helper mutation hiện tại không bao phủ các phần đó.
2. Xác minh runtime W4-03, notification và restore khi môi trường phù hợp.
3. Ký release bằng signing key do chủ dự án quản lý nếu cần phát hành.

Giữ snapshot này làm điểm đồng bộ. Không dùng số test hay trạng thái runtime cũ để mô tả kết quả hiện tại.
