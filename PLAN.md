# YourRefri "Hôm nay nấu gì" — Kế hoạch 5 tuần

## Kế hoạch thực thi cập nhật 05/11/2026

- Nguồn phạm vi hiện hành: `HANDOFF.md` và `AGENTS.override.md`. PR ý tưởng ở fork mô tả MVP khác (nhập câu tự nhiên, ba trạng thái, giờ nhắc 16:30); không tự áp dụng các quyết định đó vào app offline hiện tại.
- Tuần 1: hoàn thiện W1-04 schema, W1-05 repository theo spec và test có sẵn; sau đó chạy toàn bộ test và ít nhất sáu mutation cho từng task W1-02/03/04/05/06, khôi phục và chạy xanh. W1-02/03/06 mới được rà tĩnh, chưa xác nhận xanh.
- W1-07 chia thành: (a) ViewModel, state và effect; (b) lưới và hộp sửa hạn; (c) điều hướng, nối dữ liệu, đóng gói JSON runtime và kiểm tra emulator. Thiết kế test/spec trước cho từng phần, kiểm ảnh emulator khi hoàn tất UI.
- Tuần 2: danh sách cần dùng sớm, nội dung và lịch nhắc, xử lý reboot/múi giờ, màn hình chính; build APK thử. Tuần 3: gợi ý món, công thức và nhật ký. Tuần 4: xác nhận tuần, bộ đếm và phản hồi thử nghiệm. Tuần 5: kiểm tổng thể, quyền riêng tư và hoàn thiện APK.
- Mỗi task có một owner làm test → code → tự kiểm; chỉ một agent chạy build hoặc mutation trên workspace chung. Chia file rõ ràng, các task độc lập có thể chạy song song trong số slot thực tế.
- Sau mỗi task đã kiểm, tạo commit và PR trên fork theo phân công; CodeRabbit và Qodo nhận xét trên PR, agent xác minh từng ý, sửa lỗi hợp lệ và chạy lại kiểm tra. Không merge tự động. PR ý tưởng `anhvb702/thuc-tap-sinh#1` chỉ review tài liệu; code app cần PR riêng trên nhánh phù hợp sau khi đối chiếu lịch sử repo, không ghi đè nhánh ý tưởng.
- Chờ duyệt bảng 25 món, các ràng buộc model và giờ/ngưỡng nhắc nêu trong `HANDOFF.md`; tiếp tục các phần không phụ thuộc trong lúc chờ.
- Lệnh kiểm tra Android: `:shared:testDebugUnitTest` và `:composeApp:assembleDebug`. Không tải dependency khi chưa được phép; báo riêng nếu cache thiếu. Mỗi task ghi kết quả thật, số mutation bị test bắt và những hạn chế.

App KMM (Compose Multiplatform, Android trước) cho phụ huynh: nhập đồ trong tủ bằng lưới chạm, nhắc đồ nên dùng sớm, gợi ý 3 món, công thức từng bước. Dữ liệu chỉ trên máy.

Trạng thái: bản nháp từ planner, chưa chạy task nào. Mục "Cần người dùng xác nhận" còn mở.

## Quy trình mỗi task
1. **Opus** thiết kế unit test trước: tên test, đầu vào, kết quả cần đạt.
2. **Haiku** viết code cho test xanh, không sửa hay làm yếu test.
3. **Sonnet** kiểm: đọc diff, chạy test, đối chiếu tiêu chí, ghi biên bản ngắn.

Mỗi task ≤ ~300 dòng diff (S ≤100, M ≤200, L ≤300; vượt thì tách).

## Kiến trúc đã thống nhất
- Hai module: `:shared` (data + logic thuần) và `:composeApp` (UI, ViewModel).
- `shared/.../feature/{pantry,recipe,suggest,cooklog,reminder}/{data,logic}`; `logic` chỉ hàm thuần, không import SQLDelight.
- `composeApp/.../feature/*/ui` và `nav/Routes.kt` (một file, route type-safe, tham số nguyên thủy).
- MVI: State / Action / Effect + ViewModel + StateFlow. Luôn tiêm `Clock`/`TimeZone` (kotlinx-datetime).
- SQLDelight cho dữ liệu; công thức là JSON bundle đọc lúc chạy, không vào DB.
- Không backend, analytics, tài khoản.
- Dữ liệu: `Ingredient(id,nameVi,group,defaultShelfDays,userShelfDays?)`, `PantryItem(ingredientId,have,markedAt)`, `CookLog(id,recipeId,cookedOn,photoUri?,savedIngredientIds)`, `Recipe(id,title,steps,required:List<Slot>,optional)`, `Slot=anyOf(ingredientIds)`. Không lưu số lượng.
- `daysLeft = markedAt + (userShelfDays ?: defaultShelfDays) - today`; `Urgency`: EXPIRED (<0), SOON (0..2), OK.
- `suggest(recipes,pantry,ingredients,log,today,n=3)` kiểu B: lọc món nấu được, ưu tiên món dùng đồ SOON, xếp theo `min(daysLeft)`, hòa thì theo id; món nấu trong 3 ngày gần nhất bị đẩy xuống; fallback theo số đồ khớp nếu <3 món. Test chỉ kiểm thuộc tính, không khóa điểm số.

## Văn phong
Ấm, ngắn, gợi ý không ra lệnh, luôn có đường lui. Cấm: "bạn quên", "lãng phí", "bỏ lỡ", "hết hạn", điểm số, so sánh, streak. Nói "khoảng / nên dùng trước". Đồ quá hạn: "kiểm tra lại xem còn dùng được không".

## Tuần 1 — Khung, DB, bảng hạn, lưới chạm
| Mã | Mục tiêu | Chữ ký / file chính | Phụ thuộc | Cỡ | Test Opus thiết kế |
|---|---|---|---|---|---|
| W1-00 | Kiểm tra toolchain, nâng `libs.versions.toml` (SQLDelight, kotlinx-datetime, navigation-compose, lifecycle, targetSdk); dựng 2 module | gradle/, build.gradle.kts | — | M | Không unit test; `./gradlew build` xanh, test mẫu chạy được ở shared |
| W1-01 | `daysLeft`, `urgencyOf` | `feature/pantry/logic/Shelf.kt` | — | S | userShelfDays ghi đè default; biên -1/0/2/3; markedAt = hôm nay; qua tháng và năm nhuận |
| W1-02 | Model dữ liệu | `feature/*/data/Models.kt` | W1-00 | S | Giá trị mặc định; `Slot.anyOf` không rỗng |
| W1-03 | Bảng hạn ~25 món (JSON) và loader | `ShelfTable.parse(json)` | W1-02 | M | Đủ ~25 món; id trùng lỗi; thiếu defaultShelfDays lỗi; `nameVi` không rỗng |
| W1-04 | Schema SQLDelight, `DriverFactory` | `*.sq` | W1-00 | M | Driver in-memory: chèn, đọc, ghi đè; userShelfDays được nhớ |
| W1-05 | `PantryRepository` | `observe()`, `setHave(id,have,now)`, `setShelfDays(id,days)` | W1-02, W1-04 | M | setHave(true) ghi markedAt từ Clock tiêm; sửa hạn được nhớ |
| W1-06 | Reducer lưới chạm (MVI) | `PantryGridReducer` | W1-05 | M | 1 chạm đổi có/hết; 10 món ≤3 chạm/món; sửa hạn ≤3 chạm |
| W1-07 | UI lưới + nhãn "ước tính" + `nav/Routes.kt` | composeApp pantry/ui | W1-06 | L | Không unit test; ảnh chụp emulator |
| W1-08 | Viết lại CLAUDE.md cho YourRefri | CLAUDE.md | W1-00 | S | — |

## Tuần 2 — Bản thử người thật
| Mã | Mục tiêu | Phụ thuộc | Cỡ | Test Opus thiết kế |
|---|---|---|---|---|
| W2-01 | `expiringList(pantry,ingredients,today)` | W1-01 | S | Chỉ have=true; EXPIRED lên đầu; hòa theo id; rỗng thì rỗng |
| W2-02 | `statusText(daysLeft)` | W1-01 | S | EXPIRED có câu "kiểm tra lại…"; không có từ cấm; dùng "khoảng / nên dùng trước" |
| W2-03 | `planDailyNotice(pantry,ingredients,today,lastSent)` | W2-01 | M | Không có đồ cần nhắc thì null; đã gửi hôm nay thì null; gộp một thông báo; mô phỏng 30 ngày không ngày nào >1 |
| W2-04 | `nextTriggerTime(now,tz,hour)` | — | S | Trước giờ thì hôm nay, sau giờ thì ngày mai; đổi múi giờ; biên 23:59 |
| W2-05 | Android: AlarmManager không chính xác, BootReceiver, TIMEZONE_CHANGED, POST_NOTIFICATIONS | W2-03, W2-04 | M | Không unit test; checklist tay |
| W2-06 | Màn hình chính: danh sách sắp hỏng, mở app luôn có nội dung | W2-01, W1-07 | M | Reducer: rỗng vẫn có thông điệp ấm và đường lui |
| W2-07 | Ra APK thử, gửi 5 hộ qua Zalo, thu phản hồi | W2-05, W2-06 | S | — |

## Tuần 3 — Gợi ý và nhật ký nấu
| Mã | Mục tiêu | Phụ thuộc | Cỡ | Test Opus thiết kế |
|---|---|---|---|---|
| W3-01 | `RecipeBundle.parse(json)` | W1-02 | M | Bước ≤8; không có trường định lượng; slot rỗng lỗi; id trùng lỗi |
| W3-02 | `canCook(recipe,pantry)` | W3-01 | S | Mọi required slot khớp; anyOf khớp một là đủ; optional không bắt buộc |
| W3-03 | `suggest(...)` | W3-02, W1-01 | L | (1) mọi món trả về nấu được; (2) có món dùng đồ SOON thì đứng trước; (3) tất định, đảo thứ tự đầu vào thì kết quả giống; (4) món nấu trong 3 ngày gần nhất không vào top khi còn món khác; (5) fallback khi <3 món; (6) tối đa n; (7) rỗng thì rỗng |
| W3-04 | Công thức đợt 1 (25 món) | W3-01 | M | Validator chạy trên toàn bundle |
| W3-05 | `CookLogRepository` và "dùng hết chưa?" | W1-04 | M | Ghi log; hết/còn/bỏ cập nhật pantry đúng; 1 chạm |
| W3-06 | UI gợi ý 3 món, chi tiết món, hộp "dùng hết chưa?" | W3-03, W3-05 | L | Reducer có test; UI ảnh chụp |

## Tuần 4 — Xác nhận tuần, bộ đếm, 40 công thức
| Mã | Mục tiêu | Phụ thuộc | Cỡ | Test Opus thiết kế |
|---|---|---|---|---|
| W4-01 | `weeklyReview`, mặc định "đúng hết" | W1-05 | M | Mặc định giữ nguyên; chỉ đổi mục được chạm; số chạm ≤ ngưỡng 30s |
| W4-02 | `usedBeforeDueCount(log,...)` | W3-05 | S | Đếm khi chọn "hết" và daysLeft ≥0; cùng đồ không đếm 2 lần trong một lần nấu; câu chữ ghi nhận, không có từ cấm |
| W4-03 | UI xác nhận tuần và bộ đếm | W4-01, W4-02 | M | Ảnh chụp |
| W4-04 | Công thức 26 đến 40 | W3-04 | M | Validator |
| W4-05 | Sửa lỗi từ phản hồi tuần 2-3 | W2-07 | M | Test hồi quy cho mỗi lỗi |

## Tuần 5 — Hoàn thiện
- W5-01: đo tiêu chí (10 món ≤60s, ≤3 chạm; xác nhận tuần ≤30s; mô phỏng 30 ngày).
- W5-02: quét từ cấm trên mọi chuỗi giao diện bằng test.
- W5-03: icon, ký APK, ghi chú quyền riêng tư (dữ liệu chỉ trên máy), cài sạch chạy được.
- W5-04 (nếu kịp, mỗi mục một task): công thức lên 60, ảnh nhật ký, nhập bằng giọng nói qua OS, danh sách đi chợ, món tự tạo.

## Thứ tự và song song
- Chuỗi chính: W1-00 → W1-02 → W1-04/W1-05 → W1-06 → W1-07 → W2-06 → W2-07 → W3-06 → W4-03 → W5.
- Song song được: W1-01 với W1-00; W1-08 với W1-02; W1-03 với W1-04; W2-01/02/04; W3-01/04 với W3-05; W4-01/02/04.
- Haiku không chạm cùng file cùng lúc.

## Cần người dùng xác nhận
1. `applicationId` mặc định đề xuất: `com.loitu.yourrefri`.
2. Viết lại CLAUDE.md (hiện còn mô tả MakeYourDay).
3. Cho phép nâng `libs.versions.toml`, `targetSdk`, lifecycle (W1-00).
4. Quy trình pullboard trong AGENTS.md: mặc định không dùng cho dự án mới.
5. Duyệt bảng hạn 25 món (W1-03) và từng đợt công thức (25, 40, 60).
6. Giờ gửi thông báo hằng ngày mặc định (đề xuất 17:30).
7. 5 hộ thử nghiệm và cách thu phản hồi.
8. Ngưỡng SOON = 0..2 ngày và cửa sổ "3 ngày gần nhất".

## Rủi ro và điều kiện cắt
- Toolchain KMM/SQLDelight lệch phiên bản: dành trọn W1-00; quá 1 ngày thì bỏ iOS, giữ androidMain.
- Hạn ước tính sai làm mất tin: luôn nhãn "ước tính", sửa dễ; nếu phản hồi tuần 2 nói sai nhiều thì sửa bảng hạn trước mọi thứ.
- Thông báo bị OS chặn: dùng báo thức không chính xác; mở app luôn thấy nội dung.
- Công thức là nút cổ chai (chờ duyệt): chốt lô 25 sớm trong tuần 2.
- Cắt theo thứ tự khi trễ: W5-04 → công thức 60 → công thức 40 (giữ 25-30) → bộ đếm → xác nhận tuần bản tối giản. Không cắt: lưới chạm, thông báo, gợi ý 3 món.
- Chưa có phản hồi người thật đến tuần 3: vẫn đi tiếp nhưng ghi rõ chưa kiểm chứng.

## Definition of Done chung
1. Test do Opus viết trước, không bị sửa hoặc làm yếu (Sonnet đối chiếu git diff của test).
2. `./gradlew :shared:allTests` và build composeApp xanh.
3. Logic chỉ hàm thuần, Clock/TimeZone được tiêm, `feature/*/logic` không import SQLDelight.
4. Diff ≤ ~300 dòng, đúng file và chữ ký trong task.
5. Không có từ cấm, văn phong ngắn, ấm, có đường lui.
6. Không backend, analytics, tài khoản.
7. UI có ảnh chụp emulator; AlarmManager/BootReceiver có checklist tay đã tick.
8. Sonnet ghi biên bản ngắn: tiêu chí nào đạt, bằng chứng.

## Task thử pipeline đầu tiên
W1-01 (`daysLeft`, `urgencyOf`): nhỏ, thuần logic. Ví dụ Opus có thể thiết kế: markedAt=2026-11-01, defaultShelfDays=5; today=2026-11-03 → 3 ngày, OK; today=2026-11-06 → 0, SOON; today=2026-11-07 → -1, EXPIRED; userShelfDays=2 ghi đè default 5; `urgencyOf(-1)=EXPIRED`, `(0)=SOON`, `(2)=SOON`, `(3)=OK`; 2028-02-28 + 2 ngày qua năm nhuận.
