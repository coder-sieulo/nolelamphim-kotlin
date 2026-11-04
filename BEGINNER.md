# BEGINNER — Lần đầu chạm vào Kotlin

Tài liệu này dành cho người **chưa từng dùng Kotlin/Android**. Cứ làm đúng thứ tự, mỗi bước đều có cách kiểm tra. Khi bí, quay lại đây hoặc nhắn mình kèm lỗi.

Mục tiêu sau buổi đầu:

- [ ] Mở được project trong Android Studio.
- [ ] Chạy được app trên emulator.
- [ ] Hiểu app lấy dữ liệu từ đâu.
- [ ] Tự sửa một dòng chữ và thấy kết quả.
- [ ] Biết xem log khi app lỗi.

---

## 1. Cài công cụ

### Cách A — Android Studio (khuyến nghị cho người mới)

Trên Ubuntu:

```bash
sudo snap install android-studio --classic
```

Hoặc tải bản mới nhất tại <https://developer.android.com/studio> rồi giải nén và chạy `android-studio/bin/studio.sh`.

Sau khi cài:

1. Mở **Android Studio**.
2. Chọn **Open**.
3. Chọn thư mục `/home/viennn/Desktop/Nolelamphim-kotlin`.
4. Đợi **Gradle Sync** chạy xong (lần đầu có thể 3–10 phút vì tải thư viện).

Nếu Android Studio báo thiếu SDK:

- Vào **Settings → Languages & Frameworks → Android SDK**.
- Chọn đường dẫn: `/home/viennn/Android/Sdk`.

File `local.properties` trên máy này đã được trỏ sẵn:

```properties
sdk.dir=/home/viennn/Android/Sdk
```

> File `local.properties` chỉ dành cho máy cá nhân, đã nằm trong `.gitignore` — **không commit**.

### Cách B — Chỉ dùng terminal (không cần Android Studio)

Máy đã có JDK 17, Android SDK và emulator. Chạy:

```bash
cd /home/viennn/Desktop/Nolelamphim-kotlin
./gradlew :androidApp:assembleDebug
```

Nếu lệnh trên báo `BUILD SUCCESSFUL` là môi trường đã ổn.

---

## 2. Chạy app lần đầu

### Bật emulator

```bash
# Xem emulator có sẵn
emulator -list-avds

# Bật Pixel_5
emulator -avd Pixel_5 &

# Chờ emulator boot xong
adb wait-for-device
adb shell 'while [ "$(getprop sys.boot_completed)" != "1" ]; do sleep 1; done'
```

### Chạy app

**Nếu dùng Android Studio:**

- Trên thanh trên cùng, chọn cấu hình **androidApp**.
- Chọn emulator **Pixel_5**.
- Bấm nút ▶ **Run**.

**Nếu dùng terminal:**

```bash
./gradlew :androidApp:installDebug
adb shell am start -n nolelamphim.vercel.app/.MainActivity
```

### Kiểm tra

App mở ra sẽ thấy:

- Nền tối.
- Banner phim **Hoàng Hậu Tái Hôn**.
- Các hàng **Mới cập nhật**, **Phim bộ**, **Phim lẻ**.
- Thanh dưới cùng: **Trang chủ** và **Tìm kiếm**.

Nếu thấy các nội dung trên là bạn đã chạy thành công.

---

## 3. Bản đồ code — file nào làm gì?

| File | Nhiệm vụ |
|---|---|
| `shared/src/commonMain/.../App.kt` | Điểm vào của UI, gắn theme tối và điều hướng |
| `navigation/AppNavHost.kt` | Quy định màn nào mở màn nào, bottom bar |
| `navigation/Routes.kt` | Tên các route |
| `ui/screens/home/HomeScreen.kt` | Giao diện trang chủ |
| `ui/screens/home/HomeViewModel.kt` | Tải dữ liệu, giữ trạng thái loading/error |
| `ui/screens/search/SearchScreen.kt` | Giao diện tìm kiếm |
| `ui/screens/filter/FilterScreen.kt` | Giao diện lọc phim |
| `ui/screens/detail/DetailScreen.kt` | Giao diện chi tiết phim + danh sách tập |
| `ui/screens/watch/WatchScreen.kt` | Màn xem phim |
| `ui/components/` | Các khối UI dùng lại: card, banner, grid, loading... |
| `data/MovieRepository.kt` | Gọi API và đổi dữ liệu API thành dữ liệu cho UI |
| `data/remote/MovieApi.kt` | Khai báo các endpoint VSMOV |
| `model/` | Các class dữ liệu đơn giản cho UI |
| `androidMain/.../VideoPlayer.android.kt` | WebView trên Android |
| `iosMain/.../VideoPlayer.ios.kt` | WKWebView trên iOS |

### Luồng dữ liệu

```text
HomeScreen (Compose)
      ↓  hiển thị state
HomeViewModel (StateFlow)
      ↓  gọi hàm suspend
MovieRepository
      ↓
MovieApi (Ktor)  →  https://vsmov.com/api/
```

Nguyên tắc quan trọng: **UI không gọi API trực tiếp**. UI chỉ biết ViewModel; ViewModel gọi Repository; Repository gọi API.

---

## 4. Kotlin tối thiểu cần biết

Chỉ cần hiểu 6 thứ này trước đã.

### 4.1. `val` và `var`

```kotlin
val name = "Nolelamphim"   // không đổi được
var count = 0              // đổi được
count = 1
```

Trong dự án, hầu hết dùng `val` vì an toàn hơn.

### 4.2. Null safety

```kotlin
val poster: String? = null      // có thể null
val length = poster?.length     // nếu null thì trả null
val safe = poster ?: "Không có" // nếu null thì lấy giá trị mặc định
```

Gặp `String?` nghĩa là **có thể null**. Đừng dùng `!!` trừ khi thật chắc chắn.

### 4.3. Data class

```kotlin
data class Movie(
    val name: String,
    val year: Int?,
)
```

Chỉ để chứa dữ liệu. Xem `model/Movie.kt` và `model/MovieDetail.kt`.

### 4.4. Hàm và lambda

```kotlin
fun tinhTong(a: Int, b: Int): Int = a + b

val movies = listOf("A", "B")
movies.forEach { movie ->
    println(movie)
}
```

Trong Compose bạn sẽ thấy rất nhiều lambda, ví dụ:

```kotlin
Button(onClick = { navController.popBackStack() }) {
    Text("Quay lại")
}
```

### 4.5. `when`

```kotlin
when (state) {
    is DetailUiState.Loading -> LoadingView()
    is DetailUiState.Success -> DetailContent(state.movie)
    is DetailUiState.Error -> ErrorView(state.message)
}
```

Đây là cách các màn hình chọn giao diện theo trạng thái. Xem `DetailScreen.kt`.

### 4.6. Coroutine và `StateFlow`

```kotlin
viewModelScope.launch {
    val movies = repository.latest()
    _uiState.value = HomeUiState(latest = movies)
}
```

- `launch` mở một tác vụ chạy nền.
- `suspend` là hàm có thể chờ (gọi API, đọc dữ liệu).
- `StateFlow` giữ trạng thái mới nhất để UI tự cập nhật.

Xem `HomeViewModel.kt` — đây là ví dụ ngắn và dễ nhất.

---

## 5. Compose tối thiểu cần biết

### 5.1. `@Composable`

Một hàm có `@Composable` là một mảnh giao diện:

```kotlin
@Composable
fun Hello() {
    Text("Xin chào")
}
```

### 5.2. `Modifier`

`Modifier` dùng để chỉnh kích thước, padding, màu nền, click...

```kotlin
Text(
    text = "Xin chào",
    modifier = Modifier.padding(16.dp),
)
```

### 5.3. Các layout hay dùng

| Thành phần | Công dụng |
|---|---|
| `Column` | xếp dọc |
| `Row` | xếp ngang |
| `Box` | chồng lớp |
| `LazyColumn` | danh sách dọc, chỉ vẽ phần đang thấy |
| `LazyRow` | danh sách ngang |
| `LazyVerticalGrid` | lưới |

### 5.4. State và recomposition

```kotlin
var text by remember { mutableStateOf("Hello") }

TextField(
    value = text,
    onValueChange = { text = it },
)
```

Khi `text` đổi, Compose tự vẽ lại phần giao diện liên quan. Đây là lý do app không cần cập nhật UI bằng tay.

### 5.5. Đọc state từ ViewModel

```kotlin
val state by viewModel.uiState.collectAsStateWithLifecycle()
```

Dòng này biến `StateFlow` trong ViewModel thành state để Compose hiển thị.

---

## 6. Bài tập đầu tiên — sửa một dòng chữ

Đây là bài quan trọng nhất: làm quen vòng lặp **sửa → chạy → xem kết quả**.

1. Mở file:
   `shared/src/commonMain/kotlin/nolelamphim/vercel/app/ui/screens/home/HomeScreen.kt`
2. Tìm dòng:

   ```kotlin
   text = "Nolelamphim",
   ```

3. Đổi thành:

   ```kotlin
   text = "Phim Của Tôi",
   ```

4. Lưu file (`Ctrl + S`).
5. Bấm **Run** lại (hoặc chạy `./gradlew :androidApp:installDebug`).
6. Mở app → tiêu đề trang chủ đã đổi.

Nếu làm được bước này, bạn đã biết cách sửa và chạy dự án.

### Bài tập 2 — đổi màu chủ đạo

Mở `App.kt`:

```kotlin
MaterialTheme(
    colorScheme = darkColorScheme(),
) {
    AppNavHost()
}
```

Thử đổi thành:

```kotlin
MaterialTheme(
    colorScheme = darkColorScheme(
        primary = Color(0xFFE50914),
    ),
) {
    AppNavHost()
}
```

Nhớ thêm import:

```kotlin
import androidx.compose.ui.graphics.Color
```

Chạy lại và xem màu nút/chip thay đổi.

---

## 7. Xem log khi app lỗi

```bash
# Log crash
adb logcat -s AndroidRuntime

# Log riêng app
adb logcat --pid=$(adb shell pidof -s nolelamphim.vercel.app)

# Xoá log cũ rồi mở lại app
adb logcat -c
```

Trong Android Studio: mở tab **Logcat** ở dưới cùng, chọn app `nolelamphim.vercel.app`.

Lỗi thường gặp:

| Lỗi | Cách xử lý |
|---|---|
| `No connected devices` | Bật emulator, chạy `adb devices` |
| `sdk.dir` trống | Sửa `local.properties` như mục 1 |
| Gradle sync mãi | Đợi lần đầu tải thư viện; kiểm tra mạng |
| `Unresolved reference` | Chưa sync Gradle, hoặc thiếu import (`Alt + Enter`) |
| App trắng / không có phim | Kiểm tra mạng, xem Logcat |
| iOS không build được trên Linux | Bình thường — cần macOS + Xcode |
| Sửa code không thấy đổi | Chạy lại app, không chỉ build |

---

## 8. Thứ tự học tiếp

1. **Tuần 1:** chạy app, sửa text/màu, tập đọc `HomeScreen` + `HomeViewModel`.
2. **Tuần 2:** học Kotlin cơ bản: `val/var`, null safety, data class, `when`, collection.
3. **Tuần 3:** học Compose: `Column`, `Row`, `LazyColumn`, state, modifier.
4. **Tuần 4:** đọc luồng UI → ViewModel → Repository → API.
5. Sau đó mới thêm tính năng mới (yêu thích, lịch sử xem, tìm kiếm nâng cao).

Đừng cố hiểu hết code ngay. Người mới chỉ cần nắm:

```text
Bấm nút → gọi hàm → đổi state → Compose vẽ lại
```

---

## 9. Checklist buổi đầu

- [ ] Cài hoặc mở được Android Studio.
- [ ] Gradle sync thành công.
- [ ] Chạy app trên emulator.
- [ ] Đổi chữ "Nolelamphim" thành công.
- [ ] Mở được Logcat.
- [ ] Biết file nào là UI, ViewModel, Repository, API.

Làm xong checklist này rồi hãy sang tính năng mới. Nếu kẹt ở bước nào, gửi mình:

1. Bạn đang làm bước nào.
2. Lệnh vừa chạy.
3. Nội dung lỗi (copy nguyên văn).
