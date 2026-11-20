# Quy Tắc Kiến Trúc Clean Architecture + MVI (YourRefri KMM)

## 1. Phân Tầng Clean Architecture (KMM)
Mọi tính năng mới hoặc tính năng được refactor phải tách rõ 3 tầng:

1. **Tầng `domain` (`shared/src/commonMain/.../feature/<name>/domain/`)**
   - `model/`: Entity thuần Kotlin, bất biến (`data class`, `enum`), **không** phụ thuộc Android, Compose, SQLDelight hay JSON serializer.
   - `repository/`: Interface Repository định nghĩa hợp đồng dữ liệu.
   - `usecase/`: Nghiệp vụ đơn trách nhiệm (pure Kotlin, có `operator fun invoke(...)`), dễ kiểm thử đơn vị độc lập.
2. **Tầng `data` (`shared/src/commonMain/.../feature/<name>/data/`)**
   - `datasource/`, `mapper/`, `repository/`: Cài đặt Repository từ SQLDelight, asset JSON hoặc preference; ánh xạ DTO/Row sang Domain Model.
3. **Tầng `presentation` (`composeApp/src/main/.../feature/<name>/presentation/`)**
   - `<Name>Contract.kt`: Gom 3 hợp đồng MVI — `UiState` (immutable `data class`), `Intent` (`sealed interface`), `Effect` (`sealed interface` cho sự kiện một lần như điều hướng, snackbar).
   - `<Name>ViewModel.kt`: Kế thừa `MviViewModel<UiState, Intent, Effect>`, nhận `Intent`, gọi UseCase/Repository và cập nhật `StateFlow` / phát `Effect`.
   - `<Name>Screen.kt`: Composable mỏng, chỉ render `UiState` và gửi `Intent` (mục tiêu ≤ 300 dòng; tách thành phần nhỏ vào `components/`). Không tính toán nghiệp vụ trực tiếp trong Composable.

## 2. Manual DI (`AppContainer`)
- Toàn bộ khởi tạo Repository, UseCase và `ViewModelProvider.Factory` đặt tập trung trong `composeApp/.../di/AppContainer.kt`.
- Các tầng tiêu thụ chỉ phụ thuộc vào **Repository interface**, không gọi trực tiếp lớp `*Impl`.
- `MainActivity.kt` chỉ giữ vòng đời Activity, quyền hệ thống và gắn Compose root.

## 3. Kiểm Thử Bắt Buộc
- Mỗi `UseCase` mới phải có unit test thuần trong `shared/src/commonTest`.
- Mỗi `MviViewModel` mới phải có unit test kiểm tra `UiState` và `Effect` trong `composeApp/src/test`.
- Áp dụng **chuyển đổi cuốn chiếu (incremental)**: code mới bắt buộc theo chuẩn này; màn hình cũ refactor dần theo từng feature mà không làm vỡ test hiện có.
