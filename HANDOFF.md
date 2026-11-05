# HANDOFF: YourRefri (tạm dừng giữa tuần 1)

Cập nhật: 2026-11-05. Xem tiến độ mới nhất ngay bên dưới; thông tin phía sau là handoff ban đầu và có thể đã cũ.

## Tiến độ cập nhật trong phiên Codex

- Fork `anhvb702/thuc-tap-sinh`, nhánh `codex/yourrefri-baseline`, PR review #2. Commit `dcc0482` chứa W1-04/05; không merge tự động.
- W1-04/05: 187 test shared xanh; DB 34 test, repository 30 test; mỗi task 6 mutation bị bắt và đã khôi phục. Xem biên bản trong spec tương ứng.
- W1-02/03/06: mỗi task đã kiểm 6 mutation bị test bắt, khôi phục và test shared xanh từ cache; W1-02 mutation mới tập trung Recipe/Slot, chưa riêng CookLog. Chi tiết trong các spec.
- W1-07a có spec, test và code ViewModel bản đầu. Test compile lỗi suy luận kiểu ở hai phép `assertEquals`; đang hỏi người dùng cho phép sửa kiểu test mà giữ nguyên điều kiện. Chưa xác nhận ViewModel xanh hoặc mutation.
- Qodo nêu vấn đề hoàn tác món chưa từng có để lại dòng `have=false`; cần đối chiếu hợp đồng reducer/repository và viết test tích hợp trước khi đổi hành vi. CodeRabbit bị giới hạn lượt review ở lần kiểm trước.
- Người dùng cho phép tải dependency Gradle, commit/push theo feature và dùng emulator. Workspace đang mở trực tiếp trong Android Studio; báo khi có bản phù hợp để sync và chạy.

## Dự án
App KMM "Hôm nay nấu gì" cho phụ huynh (Android trước, 5 tuần). Nhập đồ bằng lưới chạm (có/hết), hạn dùng ước tính, một thông báo mỗi ngày, gợi ý 3 món từ đồ sắp hỏng, công thức từng bước. Dữ liệu chỉ trên máy. Quy tắc đầy đủ ở `CLAUDE.md` (cùng nội dung cho Codex ở `AGENTS.override.md`), kế hoạch ở `PLAN.md`, spec test ở `docs/tests/W1-0x.md`.

Quy trình mỗi task: Opus thiết kế test trước, Haiku code cho test xanh (không sửa test), Sonnet kiểm (có mutation). Chuyển sang Codex thì một agent làm cả ba vai theo thứ tự đó, xem `AGENTS.override.md`.

## Đã xong và đã được kiểm
- **W1-00** (toolchain, module `:shared`, `:composeApp`): Sonnet chấp nhận. `assembleDebug` xanh.
- **W1-01** (`daysLeft`, `urgencyOf`): Sonnet chấp nhận. 28 test + 1 smoke xanh; 11 mutation đều làm test đỏ.
- **W1-08** (viết lại `CLAUDE.md`): Haiku xong, chưa ai kiểm. Haiku đã bỏ dòng "iOS cần Mac"; dòng "Chỉ build Android cục bộ" vẫn còn.

## Đã viết nhưng CHƯA kiểm độc lập
Haiku bị dừng ở bước cuối, nên chưa có báo cáo cuối và chưa có biên bản Sonnet. Coi như chưa xanh cho đến khi chạy lại.
- **W1-02** (model Recipe, Slot, CookLog): `feature/recipe/data/Models.kt`, `feature/cooklog/data/Models.kt`.
- **W1-03** (`ShelfTable.parse` + `shelf_table.json` 25 món): `feature/pantry/data/ShelfTable.kt`, `shared/src/commonMain/resources/shelf_table.json`, `ShelfTableResource.android.kt` (actual cho test). Đã thêm kotlinx-serialization-json vào catalog và `shared/build.gradle.kts`, cần kiểm lại.
- **W1-06** (`PantryGridReducer`): `feature/pantry/logic/PantryGridReducer.kt`.

## Đã có test, CHƯA có code
- **W1-04** (schema SQLDelight): 34 test trong `shared/src/androidUnitTest/.../shared/db/PantryDbTest.kt`, spec `docs/tests/W1-04.md`.
- **W1-05** (`PantryRepository`): 30 test trong `.../feature/pantry/data/PantryRepositoryTest.kt`, spec `docs/tests/W1-05.md`.

## Việc kế tiếp (theo thứ tự)
1. Chạy `.\gradlew.bat :shared:compileDebugKotlinAndroid` rồi kiểm W1-02/03/06 trên bản sao, vì test W1-04/05 chưa có code làm `:shared:testDebugUnitTest` không biên dịch được trên repo thật. Cách dễ nhất: làm W1-04/05 trước rồi chạy cả bộ.
2. Làm W1-04 rồi W1-05 theo spec (thêm SQLDelight plugin, `sqlite-driver` và `kotlinx-coroutines-test` vào test; lý do ghi trong `docs/tests/W1-04.md` và `W1-05.md`).
3. Chạy mutation cho W1-02/03/04/05/06 (cố ý làm sai code, test phải đỏ, rồi khôi phục). Ghi biên bản ngắn.
4. W1-07: UI lưới chạm + nhãn "ước tính" + `composeApp/.../nav/Routes.kt` (chưa tạo). Cần ảnh chụp emulator.
5. Sang tuần 2 theo `PLAN.md`.

## Quyết định đã chốt (mặc định, người dùng chưa duyệt từng mục)
- `applicationId` `com.loitu.yourrefri`; bỏ pullboard khỏi quy trình dự án này.
- Hai module `:shared` (chỉ androidTarget) và `:composeApp`; không iOS.
- `PantryRepository.setHave(ingredientId, have, markedAt: LocalDate? = null)`: `null` lấy ngày từ `Clock` + `TimeZone` được tiêm. Dùng để khôi phục ngày cũ khi chạm nhầm hai lần (effect `SaveHave` của reducer).
- Hạn tay lưu ở bảng riêng `UserShelfEntity`. `setShelfDays` với số âm thì ném lỗi; `0` hợp lệ.
- `setHave(false)` trên id chưa có không tạo dòng.
- `Clock` là `kotlin.time.Clock` (kotlinx-datetime 0.7.1 đã bỏ lớp cũ).
- Nhóm đồ: thit, ca, rau-la, rau-cu, trai-cay, sua-trung, khac. Hạn hợp lệ 1 đến 365 ngày.
- Lựa chọn sửa hạn: 1, 2, 3, 4, 5, 7, 10, 14, 21, 30 ngày.

## Cần người dùng duyệt
- **25 món và hạn ước tính** (ngày): heo 2, heo xay 1, bò 2, gà 2, cá basa 2, cá hồi 2, tôm 1, rau muống 2, cải ngọt 3, xà lách 3, rau ngót 2, hành lá 5, cà rốt 14, khoai tây 21, cà chua 5, bí xanh 7, hành tây 21, chuối 4, táo 14, cam 10, trứng gà 21, sữa tươi đã mở 3, sữa chua 10, đậu phụ 2, nấm tươi 3. Đây là hạn ước tính, không phải lời khuyên an toàn thực phẩm. Mỗi đợt công thức sau này cũng cần duyệt.
- 3 ràng buộc Opus tự thêm ở W1-02: phần tử `anyOf` và từng bước không được trống; `required` không được rỗng.
- Giờ thông báo mặc định (đề xuất 17:30) và ngưỡng "sắp hỏng" 0 đến 2 ngày.

## Lưu ý kỹ thuật
- Test chạy bằng `:shared:testDebugUnitTest` (không có `allTests` vì chỉ có androidTarget). JDK 21 `E:\Dev\Java\jdk-21.0.13+9`, SDK `E:\Dev\Android\Sdk`.
- Gradle wrapper đang là 8.14.3 và không có `distributionSha256Sum`; thay đổi này có sẵn từ trước phiên. Nên thêm lại checksum.
- `PLAN.md` đã bị viết lại hoàn toàn so với bản cũ trong git; khi commit nên tách khỏi thay đổi code. Các file đã xóa khỏi bản cũ: `androidApp/`, `shared/` cũ, `iosApp/`, `SPEC.md`, `DECISIONS.md`, `README.md`, `HANDOFF.md` cũ (vẫn nằm trong git).
- `AGENTS.md` là bản sinh bởi pullboard (cho dự án cũ), `pullboard` MCP không kết nối. Không dùng quy trình pullboard cho dự án này.
- Chưa có icon launcher, chưa có `local.properties` trong git (đúng, đã `.gitignore`).
- Không đọc hay in `local.properties`, `.env*`, keystore.

## Việc chưa hỏi người dùng
Commit, tách PR, đổi tên thư mục `E:\KMM App` thành `YourRefri` (nên làm khi đóng Android Studio), tải skill codegraph (người dùng gợi ý; cần hỏi trước khi tải).
