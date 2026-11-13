# Đợt A — Góp ý người dùng: Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Làm xong 6 mục không cần ảnh/đổi kiến trúc: chip "Nên dùng sớm" (#1), nấu ngẫu nhiên (#4), bớt chữ (#6), bữa trưa (#7), lịch sử nấu (#8), nút thực đơn tuần + thông báo Chủ Nhật (#10).

**Architecture:** Logic mới là hàm thuần trong `:shared` (`feature.<tên>.logic`), test commonTest; UI + ViewModel trong `:composeApp`, test unit trong `composeApp/src/test`. Giữ MVI hiện có. Một điểm duy nhất ghi `CookLog` khi nấu xong, dùng chung cho 3 luồng nấu hiện có.

**Tech Stack:** Kotlin 2.2.20, Compose Multiplatform 1.7.3, SQLDelight, kotlinx-datetime, `kotlin.time.Clock`.

**Spec:** `docs/superpowers/specs/2026-11-13-user-feedback-round-design.md`

## Global Constraints

- Offline hoàn toàn; dữ liệu chỉ trên máy; không SDK ngoài.
- Tối đa 1 thông báo/ngày; `AlarmManager.setAndAllowWhileIdle`; lên lịch lại sau reboot và đổi múi giờ.
- Thời gian luôn qua `kotlin.time.Clock` và `kotlinx.datetime.TimeZone` được tiêm; ngẫu nhiên qua `kotlin.random.Random` được tiêm.
- `logic` chỉ hàm thuần, không import SQLDelight.
- Cấm trong chữ hiển thị: "bạn quên", "lãng phí", "bỏ lỡ", "hết hạn", điểm số, so sánh, streak. Dùng "ước tính", "khoảng", "nên dùng trước". Đồ quá hạn: "Kiểm tra lại xem còn dùng được không".
- Không sửa, không `@Ignore`, không làm yếu test cũ. Nếu test cũ chặn thay đổi thì dừng và báo.
- Mỗi task ≤ ~300 dòng diff. Mỗi task có contract `docs/tests/A-NN.md` (Mục tiêu / Input và Expected Output / Execution record); viết test trước code; chạy mutation và ghi kết quả thật.
- **Không commit, không tạo nhánh/PR** (CLAUDE.md). Mỗi task kết thúc bằng bước báo kết quả.
- Lệnh: `.\gradlew.bat :shared:testDebugUnitTest`, `.\gradlew.bat :composeApp:testDebugUnitTest`, `.\gradlew.bat :composeApp:assembleDebug`.
- Điều hướng hiện là state cục bộ trong `MainScaffold` (chưa có `nav/Routes.kt`); giữ cách đó, không giới thiệu Navigation Compose trong đợt này.

## Review Focus

- Chip "Nên dùng sớm" khi tủ trống hoặc không có món cần dùng: vẫn hiện, kèm câu ấm, không crash.
- Nấu ngẫu nhiên khi danh sách công thức rỗng hoặc chỉ có 1 món (bấm lại không có món khác).
- Chủ Nhật: đã gửi thông báo thường lúc sáng thì không gửi lần hai; đổi múi giờ/DST không làm lệch giờ 8:00.
- `CookLog` cũ (trước khi có cột bữa) vẫn đọc được và hiện trong "Đã nấu"; backup cũ còn khôi phục được.
- Món có trong chip "Nên dùng sớm" nhưng đã quá hạn ước tính: hiện câu "Kiểm tra lại", không bị đẩy vào gợi ý nấu.

---

### Task A1: Logic lọc "Nên dùng sớm"

**Files:**
- Modify: `composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridScreen.kt:97` (thêm `SOON` vào `StorageTab`)
- Modify: `composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridPresentation.kt` (nhánh `when` theo tab)
- Modify: `composeApp/src/main/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridFilters.kt` (trạng thái rỗng)
- Test: `composeApp/src/test/kotlin/com/loitu/yourrefri/feature/pantry/ui/PantryGridPresentationTest.kt`, `PantryGridFiltersTest.kt` (chỉ thêm test mới, không sửa test cũ)
- Create: `docs/tests/A-01.md`

**Interfaces:**
- Produces: `StorageTab.SOON`; `pantryGridPresentation(...)` với `SOON` trả `visibleCells` = ô `have` có `urgency != Urgency.OK`, sắp theo `daysLeft` tăng dần rồi `ingredientId`; `PantryGridEmptyState.NO_SOON_ITEMS`; `soonCount(cells: List<PantryCell>): Int`.

- [ ] **Step 1:** Viết `docs/tests/A-01.md` và test thất bại: `soonTab_listsOnlyHaveWithNonOkUrgency_sortedByDaysLeft`, `soonTab_putsOverdueFirst`, `soonTab_emptyWhenNothingUrgent_returnsNoSoonItemsState`, `soonCount_matchesExpiringListRule` (so với `expiringList()` trên cùng dữ liệu để chứng minh khớp thông báo).
- [ ] **Step 2:** Chạy `.\gradlew.bat :composeApp:testDebugUnitTest --tests "*PantryGrid*"`; mong đợi FAIL do chưa có `SOON`.
- [ ] **Step 3:** Thêm `SOON`, nhánh trong `pantryGridPresentation`, `soonCount`, trạng thái rỗng. Dùng `PantryCell.urgency` đã có sẵn (cùng `urgencyOf` với `expiringList`).
- [ ] **Step 4:** Chạy lại cùng lệnh; mong đợi PASS. Chạy mutation (đổi `!=` thành `==`, bỏ sắp xếp, bỏ `have`) và ghi kết quả vào contract.
- [ ] **Step 5:** Báo kết quả, không commit.

### Task A2: Chip "Nên dùng sớm" và nút "Nấu gì từ các món này?"

**Files:**
- Modify: `PantryGridScreen.kt` (`StorageSegmentedControl` ~dòng 820, thêm tham số đếm và icon; thêm nút cuối danh sách; thêm tham số `onCookFromSoon: () -> Unit`)
- Modify: `composeApp/src/main/kotlin/com/loitu/yourrefri/ui/MainScaffold.kt` (nối `onCookFromSoon` → `currentTab = NavTab.RECIPES`, `requestedRecipeMode = RecipeScreenMode.DAILY`, `requestedRecipeId = null`)
- Modify: `PantryScreenCopy.kt` (chữ mới)
- Test: `PantryScreenCopyTest.kt`
- Create: `docs/tests/A-02.md`

**Interfaces:**
- Consumes: `StorageTab.SOON`, `soonCount` (A1).
- Produces: `PantryScreenCopy.soonChipLabel(count: Int): String` = `"Nên dùng sớm · $count"`; `PantryScreenCopy.soonEmpty` = `"Tủ đang ổn, chưa có món nào cần dùng sớm"`; `PantryScreenCopy.cookFromSoonAction` = `"Nấu gì từ các món này?"`.

- [ ] **Step 1:** Test chữ thất bại: ba chuỗi trên đúng nguyên văn, và không chứa "hết hạn" (`assertFalse`).
- [ ] **Step 2:** Chạy `--tests "*PantryScreenCopy*"`; mong đợi FAIL.
- [ ] **Step 3:** Thêm chuỗi, chip thứ tư (icon `AppIcons` sẵn có phù hợp ví dụ đồng hồ/chuông; nếu chưa có thì dùng icon gần nhất và ghi vào contract), nút chỉ hiện khi tab `SOON` và danh sách không rỗng, câu rỗng khi `NO_SOON_ITEMS`. Hàng trạng thái dùng `pantryRowStatus` sẵn có.
- [ ] **Step 4:** PASS; `:composeApp:assembleDebug` xanh; chụp emulator (chip có số, danh sách, chip rỗng, cỡ chữ 1.3) vào contract.
- [ ] **Step 5:** Báo kết quả, không commit.

### Task A3: Logic nấu ngẫu nhiên

**Files:**
- Create: `shared/src/commonMain/kotlin/com/loitu/yourrefri/feature/recipe/logic/RandomRecipe.kt`
- Test: `shared/src/commonTest/kotlin/com/loitu/yourrefri/feature/recipe/logic/RandomRecipeTest.kt`
- Create: `docs/tests/A-03.md`

**Interfaces:**
- Produces:
  - `data class RandomPick(val recipe: Recipe, val canCookNow: Boolean)`
  - `fun pickRandomRecipe(recipes: List<Recipe>, state: PantryGridState, excludeId: String?, random: Random): RandomPick?`
- Quy tắc: bỏ `excludeId`; ưu tiên món nấu được từ đồ đang có (`canCook` sẵn có), chọn ngẫu nhiên đều trong nhóm đó, `canCookNow = true`; nhóm rỗng thì rút từ phần còn lại, `canCookNow = false`; danh sách rỗng, hoặc chỉ còn món bị loại, thì trả `null`. Không lọc theo `DietRestriction` (người gọi truyền công thức chưa lọc).

- [ ] **Step 1:** Test thất bại với `Random(seed)` cố định: `prefersCookableRecipes`, `fallsBackToAllWhenNoneCookable_marksNotCookable`, `neverReturnsExcludedId`, `returnsNullWhenOnlyExcludedLeft`, `returnsNullForEmptyList`, `ignoresDietByConstruction` (nhận công thức nào cũng được).
- [ ] **Step 2:** Chạy `.\gradlew.bat :shared:testDebugUnitTest --tests "*RandomRecipe*"`; mong đợi FAIL.
- [ ] **Step 3:** Cài đặt `pickRandomRecipe` dùng `canCook` hiện có; không import SQLDelight.
- [ ] **Step 4:** PASS; mutation (bỏ lọc `excludeId`, đảo ưu tiên cookable, luôn trả phần tử đầu) và ghi kết quả.
- [ ] **Step 5:** Báo kết quả, không commit.

### Task A4: Nút nấu ngẫu nhiên trên UI

**Files:**
- Modify: `RecipeScreen.kt` (khu DAILY ~dòng 276; thêm nút và thẻ kết quả), `MainScaffold.kt` (truyền `allRecipes = recipes` chưa lọc chế độ ăn cho nút này)
- Create: `composeApp/src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/RandomRecipeCopy.kt`
- Test: `composeApp/src/test/kotlin/com/loitu/yourrefri/feature/recipe/ui/RandomRecipeCopyTest.kt`
- Create: `docs/tests/A-04.md`

**Interfaces:**
- Consumes: `pickRandomRecipe`, `RandomPick` (A3).
- Produces: `RandomRecipeCopy.button` = `"Nấu ngẫu nhiên"`; `RandomRecipeCopy.needMore` = `"Món này cần thêm vài thứ nữa nè"`; `RandomRecipeCopy.outsideDiet` = `"Món này ngoài chế độ ăn của bạn"`; `RandomRecipeCopy.tryAnother` = `"Thử món khác"`; `fun isOutsideDiet(recipe: Recipe, restriction: DietRestriction, ingredients: List<Ingredient>): Boolean` (dùng `filterRecipesByDiet` sẵn có).

- [ ] **Step 1:** Test thất bại: `isOutsideDiet_trueWhenFilteredOut`, `isOutsideDiet_falseWhenNoRestriction`, chuỗi đúng nguyên văn, không chứa chữ cấm.
- [ ] **Step 2:** Chạy `--tests "*RandomRecipeCopy*"`; mong đợi FAIL.
- [ ] **Step 3:** Cài đặt copy + `isOutsideDiet`. Nút "Nấu ngẫu nhiên" nằm cạnh bộ lọc thời gian ở DAILY; bấm → `pickRandomRecipe(..., excludeId = lastRandomId, Random.Default)` (`Random` tiêm từ `remember`), mở `onOpenRecipe`; thẻ kết quả hiện `needMore` khi `!canCookNow`, `outsideDiet` khi trùng, có "Thử món khác" và "Quay lại". Pick `null` thì hiện câu ấm "Chưa có món khác để thử" (chuỗi thêm vào copy và test).
- [ ] **Step 4:** PASS; assemble xanh; ảnh emulator cho 3 trạng thái.
- [ ] **Step 5:** Báo kết quả, không commit.

### Task A5: Logic thông báo Chủ Nhật

**Files:**
- Modify: `shared/.../notice/logic/NextTriggerTime.kt`, `DailyNotice.kt`
- Test: `shared/src/commonTest/.../notice/logic/NextTriggerTimeTest.kt`, `DailyNoticeTest.kt` (chỉ thêm test mới)
- Create: `docs/tests/A-05.md`

**Interfaces:**
- Produces:
  - `fun reminderTimeFor(date: LocalDate, sundayTime: LocalTime): LocalTime` — Chủ Nhật trả `sundayTime`, ngày khác `LocalTime(17, 30)`.
  - `fun nextReminderTrigger(enabled: Boolean, now: Instant, tz: TimeZone, sundayTime: LocalTime = LocalTime(8, 0)): Instant?` — mốc kế tiếp lớn hơn `now` trong 8 ngày tới theo `reminderTimeFor`.
  - `planDailyNotice(...)` giữ chữ ký; thêm tham số cuối `sundayShopping: Boolean = true`. Khi `today` là Chủ Nhật và `lastSent < today`, trả `DailyNotice(title = "Cuối tuần rồi nè", body = "Nay là cuối tuần rồi nè, đã sẵn sàng đi chợ cho 1 tuần mới chưa nào!", ingredientIds = emptyList())` dù tủ có đồ cần dùng hay không.

- [ ] **Step 1:** Trước tiên kiểm tra test cũ trong `DailyNoticeTest.kt`/`NextTriggerTimeTest.kt` có dùng ngày Chủ Nhật không. Nếu có và sẽ đổi kết quả thì **dừng, báo**, không sửa test cũ.
- [ ] **Step 2:** Viết test thất bại: `sunday_usesSundayTime`, `weekday_uses1730`, `sunday_afterEight_nextIsMonday1730`, `saturday1800_nextIsSunday0800`, `dstChange_keepsLocalWallClock` (múi giờ có DST), `sundayNotice_sentEvenWithoutExpiring`, `sundayNotice_notSentTwice` (`lastSent == today` → `null`), `sundayNotice_bodyExact`.
- [ ] **Step 3:** Chạy `.\gradlew.bat :shared:testDebugUnitTest --tests "*NextTriggerTime*" --tests "*DailyNotice*"`; mong đợi FAIL.
- [ ] **Step 4:** Cài đặt; giữ nguyên `nextTriggerTime` cũ nếu test cũ đang dùng.
- [ ] **Step 5:** PASS toàn bộ `:shared:testDebugUnitTest`; mutation (đổi thứ, bỏ kiểm `lastSent`, hoán giờ) và ghi kết quả. Báo kết quả, không commit.

### Task A6: Giờ Chủ Nhật trong Cài đặt và nối vào lịch báo

**Files:**
- Modify: `composeApp/.../reminder/ReminderScheduler.kt`, `ReminderReceiver.kt`
- Modify: `composeApp/.../feature/settings/ui/SettingsScreen.kt`, `ReminderCopy.kt`, `SettingsPresentation.kt`; `MainScaffold.kt`, `MainActivity.kt` (tham số mới)
- Test: `composeApp/src/test/.../settings/ui/` (file test copy/presentation tương ứng; thêm mới nếu chưa có)
- Create: `docs/tests/A-06.md`

**Interfaces:**
- Consumes: `nextReminderTrigger(..., sundayTime)`, `planDailyNotice(...)` (A5).
- Produces: `ReminderScheduler.sundayTime(context): LocalTime` (đọc `daily_reminder` prefs khóa `sunday_minutes`, mặc định 480); `ReminderScheduler.setSundayTime(context, LocalTime)` (ghi rồi `schedule`); `ReminderCopy.sundayTimeLabel(time: LocalTime): String` = `"Chủ Nhật nhắc đi chợ lúc 08:00"` (định dạng `HH:mm`).

- [ ] **Step 1:** Test thất bại cho `sundayTimeLabel` (08:00, 07:05) và việc chuỗi không chứa chữ cấm.
- [ ] **Step 2:** Chạy `--tests "*Reminder*"`; mong đợi FAIL.
- [ ] **Step 3:** Cài đặt copy; thêm hàng chọn giờ trong Cài đặt (hộp thoại chọn giờ của Material); `ReminderScheduler.schedule` truyền `sundayTime`; `ReminderReceiver` truyền cùng giờ vào `planDailyNotice`.
- [ ] **Step 4:** PASS; assemble xanh. Ghi **checklist tay** vào contract (không suy từ unit test): đặt giờ Chủ Nhật 2 phút sau trên emulator và thấy đúng 1 thông báo; reboot rồi lên lịch lại; đổi múi giờ. Việc đổi múi giờ qua adb từng bị hệ thống từ chối (xem `HANDOFF.md`) → ghi rõ nếu chưa kiểm chứng.
- [ ] **Step 5:** Báo kết quả, không commit.

### Task A7: Nút "Lên thực đơn cả tuần"

**Files:**
- Modify: `HomeScreen.kt` (`WeeklyPlanCard` ~dòng 207/440 chuyển lên ngay dưới phần chào, thành nút to), `RecipeScreen.kt`/`RecipeDailyCards.kt` (bỏ `WeekStripSection` khỏi DAILY; tìm nơi gọi bằng Grep `WeekStripSection`)
- Test: `composeApp/src/test/.../recipe/ui/RecipeTabsPresentationTest.kt` hoặc file test mới `WeeklyEntryCopyTest.kt`
- Create: `docs/tests/A-07.md`

**Interfaces:**
- Produces: `WEEKLY_BUTTON_LABEL` = `"Lên thực đơn cả tuần"` (hằng `internal` trong `WeeklyMealPlanPresentation.kt`); mở bằng `onNavigateToWeeklyRecipes` sẵn có.

- [ ] **Step 1:** Test thất bại: hằng đúng nguyên văn; `recipeTabSpecs()` vẫn có chế độ tuần (không phá test cũ `RecipeTabsPresentationTest`).
- [ ] **Step 2:** Chạy `--tests "*RecipeTabs*" --tests "*WeeklyEntry*"`; mong đợi FAIL ở test mới.
- [ ] **Step 3:** Cài đặt; giữ màn thực đơn tuần ở chế độ `RecipeScreenMode.WEEKLY`. Không đổi logic thực đơn.
- [ ] **Step 4:** Toàn bộ `:composeApp:testDebugUnitTest` PASS; assemble xanh; ảnh emulator Home và Recipe DAILY.
- [ ] **Step 5:** Báo kết quả, không commit. Ghi trong contract chỗ chồng với SW-05 và SW-09 trong `PLAN.md` (Smart Weekend Loop) để người duyệt đối chiếu.

### Task A8: Lịch sử nấu — logic nhóm theo tuần

**Files:**
- Create: `shared/src/commonMain/kotlin/com/loitu/yourrefri/feature/cooklog/logic/CookHistory.kt`
- Test: `shared/src/commonTest/kotlin/com/loitu/yourrefri/feature/cooklog/logic/CookHistoryTest.kt`
- Create: `docs/tests/A-08.md`

**Interfaces:**
- Produces:
  - `data class CookHistoryWeek(val label: CookHistoryWeekLabel, val entries: List<CookLog>)`
  - `enum class CookHistoryWeekLabel { THIS_WEEK, LAST_WEEK, EARLIER }`
  - `fun groupCookHistory(logs: List<CookLog>, today: LocalDate): List<CookHistoryWeek>` — tuần bắt đầu Thứ Hai; giữ thứ tự `logs` (đã mới nhất trước); bỏ nhóm rỗng.

- [ ] **Step 1:** Test thất bại: `groupsByMondayWeek`, `sundayBelongsToPreviousMonday`, `omitsEmptyGroups`, `keepsInputOrder`, `emptyInputReturnsEmpty`, `futureDatedEntryCountsAsThisWeek`.
- [ ] **Step 2:** Chạy `--tests "*CookHistory*"`; mong đợi FAIL.
- [ ] **Step 3:** Cài đặt hàm thuần.
- [ ] **Step 4:** PASS; mutation (lệch ngày đầu tuần, đảo nhãn) và ghi kết quả.
- [ ] **Step 5:** Báo kết quả, không commit.

### Task A9: Ghi `CookLog` một chỗ khi nấu xong

**Files:**
- Modify: `composeApp/.../MainActivity.kt` (tạo `CookLogRepositoryImpl(db, Dispatchers.IO)` như các repository khác), `MainScaffold.kt` (một hàm `completeCook(recipe, consumedIds)` thay cho ba luồng: `finishHandsFree`, `RecipeDetailScreen.onCook`, `RecipeScreen.onCookRecipe`)
- Create: `composeApp/src/main/kotlin/com/loitu/yourrefri/feature/cooklog/ui/CookHistoryViewModel.kt` (MVI: `State(weeks)`, `Action.Delete(id)`, nhận `Flow<List<CookLog>>` và `today`)
- Test: `composeApp/src/test/.../cooklog/ui/CookHistoryViewModelTest.kt`
- Create: `docs/tests/A-09.md`

**Interfaces:**
- Consumes: `CookLogRepository.add(recipeId, cookedOn, savedIngredientIds)`, `groupCookHistory` (A8).
- Produces: `CookHistoryViewModel(repository: CookLogRepository, clock: Clock, tz: TimeZone)` với `state: StateFlow<CookHistoryState>`; `completeCook` ghi đúng một `CookLog` mỗi lần nấu xong và ghi cả khi không có nguyên liệu nào được trừ.

- [ ] **Step 1:** Test thất bại: `state_emitsGroupedWeeks_fromRepositoryFlow`, `delete_callsRepository`, `todayComesFromInjectedClock`, dùng repository giả trong bộ nhớ.
- [ ] **Step 2:** Chạy `--tests "*CookHistoryViewModel*"`; mong đợi FAIL.
- [ ] **Step 3:** Cài đặt ViewModel và `completeCook`; Hoàn tác trong snackbar hiện có cũng gọi `delete(id)` của log vừa ghi (giữ id trả về từ `add`).
- [ ] **Step 4:** PASS; assemble xanh; kiểm tay trên emulator: nấu bằng cả ba luồng, mỗi luồng ra đúng 1 bản ghi; Hoàn tác xóa bản ghi.
- [ ] **Step 5:** Báo kết quả, không commit.

### Task A10: Màn "Đã nấu"

**Files:**
- Create: `composeApp/.../feature/cooklog/ui/CookHistoryScreen.kt`, `CookHistoryCopy.kt`
- Modify: `MainScaffold.kt` (state `showHistory`, `BackHandler`, lối vào từ "Hôm nay nấu gì" bằng một thẻ nhỏ "Đã nấu" — vị trí cuối cùng do người xây chọn, ghi vào contract)
- Test: `composeApp/src/test/.../cooklog/ui/CookHistoryCopyTest.kt`
- Create: `docs/tests/A-10.md`

**Interfaces:**
- Consumes: `CookHistoryViewModel`, `CookHistoryWeekLabel` (A8, A9).
- Produces: `CookHistoryCopy.title` = `"Đã nấu"`; `weekLabel(CookHistoryWeekLabel): String` = `"Tuần này"` / `"Tuần trước"` / `"Trước đó"`; `empty` = `"Chưa có món nào ở đây. Nấu xong, món sẽ nằm gọn trong này nhé"`; `cookAgain` = `"Nấu lại"`.

- [ ] **Step 1:** Test chữ thất bại (nguyên văn, không chữ cấm).
- [ ] **Step 2:** Chạy `--tests "*CookHistoryCopy*"`; mong đợi FAIL.
- [ ] **Step 3:** Màn danh sách theo tuần: tên món (tra `Recipe` theo `recipeId`; món không còn trong bundle hiện "Món đã lưu"), ngày, icon nguyên liệu từ `savedIngredientIds` (dùng `FoodThumb`/`FoodIcons` sẵn có); bấm dòng mở `openedRecipeId`; "Nấu lại" cũng mở công thức. Không thống kê, không tab "Đã dùng".
- [ ] **Step 4:** PASS; assemble xanh; ảnh emulator (có dữ liệu, rỗng, cỡ chữ 1.3).
- [ ] **Step 5:** Báo kết quả, không commit.

### Task A11: Cột bữa trong `CookLog` (migration)

**Files:**
- Create: `shared/src/commonMain/sqldelight/com/loitu/yourrefri/shared/db/migrations/6.sqm` (thêm cột `mealSlot TEXT` cho `CookLogEntity`; số phiên bản kế tiếp phải lấy bằng cách đếm file `.sqm` thực có trong thư mục migrations lúc làm task, không lấy theo con số ghi ở đây. Kế hoạch này viết khi working tree đã có `3.sqm`–`5.sqm` của SW-01, các file đó nằm ở nhánh `feat/batch-a-ui-v2-weekly-plan`; trên nhánh chỉ có `1.sqm`–`2.sqm` thì file kế tiếp là `3.sqm`)
- Modify: `CookLogEntity.sq`, `cooklog/data/Models.kt`, `CookLogRepository.kt`, `backup/data/BackupCodec.kt`, `PantryBackupModels.kt`
- Test: `shared/src/androidUnitTest/.../cooklog/data/CookLogRepositoryTest.kt` (thêm test mới), test migration theo mẫu của SW-01a, `PantryBackupRepositoryTest.kt` (thêm test mới)
- Create: `docs/tests/A-11.md`

**Interfaces:**
- Produces:
  - `enum class CookMeal { LUNCH, DINNER }` (đặt trong `cooklog/data/Models.kt`)
  - `CookLog.meal: CookMeal?` (mặc định `null` = bản ghi cũ)
  - `CookLogRepository.add(recipeId, cookedOn, savedIngredientIds, meal: CookMeal? = null): Long`
  - Backup JSON tăng phiên bản; decoder cũ vẫn đọc được, `meal` thiếu → `null`.

- [ ] **Step 1:** Test thất bại: `add_storesMeal_roundTrip`, `oldRowsReadAsNullMeal` (migration từ schema cũ trên file DB, close/reopen), `backupV_old_restoresWithNullMeal`, `backupNew_roundTripsMeal`.
- [ ] **Step 2:** Chạy `.\gradlew.bat :shared:testDebugUnitTest --tests "*CookLog*" --tests "*Backup*"`; mong đợi FAIL.
- [ ] **Step 3:** Cài đặt migration, query, model, repository, backup. Test cũ phải vẫn xanh nguyên trạng.
- [ ] **Step 4:** PASS toàn bộ `:shared:testDebugUnitTest`; mutation (quên cột trong `insertWithId`, quên map `meal`, decoder bỏ mặc định) và ghi kết quả.
- [ ] **Step 5:** Báo kết quả, không commit.

### Task A12: Chọn Trưa/Tối và hỏi "Dùng hết hay còn lại?"

**Files:**
- Create: `shared/.../recipe/logic/MealOfDay.kt`, `composeApp/.../recipe/ui/CookLeftoverPresentation.kt`
- Modify: `RecipeScreen.kt` (chọn Trưa/Tối ở DAILY), `MainScaffold.kt` (`completeCook` nhận `meal` và danh sách giữ lại), `RecipeCookPresentation.kt` (chỉ thêm hàm mới)
- Test: `shared/src/commonTest/.../recipe/logic/MealOfDayTest.kt`, `composeApp/src/test/.../recipe/ui/CookLeftoverPresentationTest.kt`
- Create: `docs/tests/A-12.md`

**Interfaces:**
- Consumes: `CookMeal`, `CookLogRepository.add(..., meal)` (A11); `completeCook` (A9).
- Produces:
  - `fun defaultMealFor(hourOfDay: Int): CookMeal` — trước 14 giờ là `LUNCH`, từ 14 giờ là `DINNER` (giờ lấy từ `PantryGridState.hourOfDay`, vốn đã đến từ `Clock`).
  - `fun ingredientsToConsume(candidateIds: List<String>, keptIds: Set<String>): List<String>` — loại các id giữ lại, bỏ trùng, giữ thứ tự.
  - `CookLeftoverCopy.title` = `"Dùng hết hay còn lại?"`, `useAll` = `"Dùng hết"`, `keepSome` = `"Còn lại"`.

- [ ] **Step 1:** Test thất bại: `defaultMeal_13h59_isLunch`, `defaultMeal_14h00_isDinner`, `defaultMeal_midnight_isDinner`, `ingredientsToConsume_removesKept_keepsOrder`, `ingredientsToConsume_emptyKept_returnsDistinct`, `copy_exact`.
- [ ] **Step 2:** Chạy `--tests "*MealOfDay*" --tests "*CookLeftover*"` ở cả hai module; mong đợi FAIL.
- [ ] **Step 3:** Cài đặt. Hộp thoại hiện từng nguyên liệu sẽ bị trừ với công tắc "Còn lại" (mặc định "Dùng hết"); phần giữ lại không bị `ConsumeIngredient`. Chọn Trưa/Tối đổi `meal` ghi vào `CookLog`; gợi ý vẫn dùng `recommendRecipes` (không đổi thuật toán).
- [ ] **Step 4:** PASS; assemble xanh; kiểm tay: đặt giờ máy giả trước/sau 14:00, nấu, kiểm `meal` đúng; chọn "Còn lại" cho một nguyên liệu và thấy nó ở lại trong tủ.
- [ ] **Step 5:** Báo kết quả, không commit.

### Task A13: Bớt chữ — Hôm nay nấu gì, thẻ món, chi tiết công thức

**Files:**
- Modify: `RecommendationCopy.kt`, `HomeScreen.kt` (`HomeRecipeCard`), `RecipeHeroPager.kt`, `RecipeDetailScreen.kt`, `HandsFreeSteps.kt` nếu có câu dài
- Test: `RecommendationCopyTest.kt`, `EmptyRecommendationCopyTest.kt` (chỉ thêm test mới; test cũ khóa chữ cũ thì dừng và báo)
- Create: `docs/tests/A-13.md`

**Interfaces:**
- Produces: nhãn lý do ngắn trên thẻ — `USE_SOON` → `"Dùng đồ sắp dùng"`, `READY` → `"Nấu được ngay"`, `QUICK` → `"Nhanh"`, `OTHER` → `""` (không hiện nhãn).

- [ ] **Step 1:** Trước tiên đọc `RecommendationCopy.kt` và test cũ; liệt kê chữ đang bị test khóa vào contract. Viết test thất bại cho nhãn ngắn mới qua hàm mới (`shortReasonLabel(reason)`) mà không đổi hàm cũ.
- [ ] **Step 2:** Chạy `--tests "*RecommendationCopy*"`; mong đợi FAIL ở test mới.
- [ ] **Step 3:** Thẻ món chỉ giữ tên, thời gian, icon nguyên liệu và nhãn ngắn; bước nấu ở chi tiết giữ tối đa một câu hiển thị lớn (không cắt dữ liệu gốc). Không cắt câu đường lui và nhãn "ước tính".
- [ ] **Step 4:** Toàn bộ `:composeApp:testDebugUnitTest` PASS; assemble xanh; ảnh emulator trước/sau, cỡ chữ 1.3.
- [ ] **Step 5:** Báo kết quả, không commit.

### Task A14: Bớt chữ — Tủ lạnh và Cài đặt

**Files:**
- Modify: `PantryScreenCopy.kt`, `PantryCardPresentation.kt`, `PantryGridScreen.kt` (chữ dài, chú thích), `SettingsScreen.kt`
- Test: `PantryScreenCopyTest.kt`, `PantryCardPresentationTest.kt` (chỉ thêm)
- Create: `docs/tests/A-14.md`

**Interfaces:**
- Consumes: không có.
- Produces: không có hàm công khai mới; chỉ rút chữ có test khóa mới.

- [ ] **Step 1:** Liệt kê trong contract mỗi đoạn chữ dài đang có và bản rút gọn đề xuất; test thất bại cho bản mới (độ dài tối đa và không chứa chữ cấm).
- [ ] **Step 2:** Chạy `--tests "*PantryScreenCopy*" --tests "*PantryCardPresentation*"`; mong đợi FAIL.
- [ ] **Step 3:** Rút chữ, giữ các câu "ước tính", đường lui và nhãn trợ năng (`contentDescription`) đầy đủ cho TalkBack.
- [ ] **Step 4:** PASS; assemble xanh; ảnh emulator, cỡ chữ 1.3.
- [ ] **Step 5:** Báo kết quả, không commit.

---

## Thứ tự và phụ thuộc

A1 → A2. A3 → A4. A5 → A6. A7 độc lập. A8 → A9 → A10. A11 → A12 (A12 cũng cần A9). A13 và A14 làm cuối (đụng nhiều file chữ). A1–A2, A3–A4, A5–A6 có thể chạy song song vì không chung file ngoài `MainScaffold.kt` và `PantryGridScreen.kt`; nếu chạy song song, tách thay đổi `MainScaffold.kt` ra từng task nhỏ để tránh xung đột.

## Self-review

- Phủ spec: #1 → A1–A2; #4 → A3–A4; #6 → A13–A14; #7 → A11–A12; #8 → A8–A10; #10 → A5–A7. Thông báo "món ngon" (#5) thuộc đợt B nên không có ở đây.
- Đợt A không đụng ảnh; thẻ món chỉ chuẩn bị chỗ cho ảnh bìa ở đợt B.
- Chữ ký nhất quán: `CookMeal`/`meal` định nghĩa ở A11 và dùng ở A12; `completeCook` định nghĩa ở A9 và dùng ở A12; `StorageTab.SOON`/`soonCount` định nghĩa ở A1 và dùng ở A2.
- Rủi ro cao nhất: A11 (migration + backup) và A5/A6 (lịch báo, chỉ kiểm chứng đầy đủ bằng checklist tay). A1 giữ quy tắc "sắp dùng" bằng `PantryCell.urgency`, cùng nguồn `urgencyOf` với `expiringList`, nhưng A1 có test khớp trực tiếp để chặn lệch.
