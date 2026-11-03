# TUTORIAL — Tự code màn hình mới & gọi API (Kotlin Multiplatform + Compose)

> Tài liệu này viết cho chính project **Nolelamphim** của bạn.
> Mục tiêu: sau khi đọc xong bạn tự thêm được màn hình, điều hướng, gọi API, quản lý state và test — không cần nhờ ai viết code.
>
> **Không sửa gì trong thư mục `build/`.** Đó là output của Gradle.

---

## 0. Bản đồ source code

```
Nolelamphim-kotlin/
├── androidApp/                      # Vỏ Android: Activity + Manifest + resource Android
│   └── src/main/
│       ├── kotlin/nolelamphim/vercel/app/MainActivity.kt
│       ├── AndroidManifest.xml
│       └── res/values/strings.xml
│
├── shared/                          # ★ NƠI VIẾT GẦN NHƯ TOÀN BỘ CODE
│   └── src/
│       ├── commonMain/kotlin/nolelamphim/vercel/app/
│       │   ├── App.kt               # UI gốc
│       │   ├── Platform.kt          # expect (API dùng chung)
│       │   └── ...
│       ├── commonMain/composeResources/
│       │   └── drawable/            # ảnh/icon dùng chung
│       ├── androidMain/kotlin/...   # actual riêng Android
│       ├── iosMain/kotlin/...       # actual riêng iOS
│       ├── commonTest/...           # test chung
│       ├── androidHostTest/...      # test JVM
│       └── iosTest/...              # test iOS (chỉ chạy trên macOS)
│
├── iosApp/                          # Vỏ iOS: Xcode project + Info.plist
├── gradle/libs.versions.toml        # ★ Khai báo version + thư viện
├── gradle.properties
├── settings.gradle.kts
└── build.gradle.kts
```

### Thông tin project hiện tại

| Thành phần | Giá trị |
|---|---|
| Kotlin | 2.4.21 |
| Compose Multiplatform | 1.12.2 |
| Android Gradle Plugin | 9.1.1 |
| Gradle | 9.5.1 |
| compileSdk / minSdk / targetSdk | 37 / 24 / 37 |
| Package name (Android) | `nolelamphim.vercel.app` |
| Package code shared | `nolelamphim.vercel.app` |
| Package resource sinh ra | `nolelamphim.shared.generated.resources` |
| Module | `:androidApp`, `:shared` |

### Quy tắc chọn thư mục

| Bạn viết gì | Đặt ở đâu |
|---|---|
| UI, ViewModel, state, model, repository, gọi API | `shared/src/commonMain/...` |
| Code chỉ Android dùng (Context, Toast, BuildConfig...) | `shared/src/androidMain/...` |
| Code chỉ iOS dùng (UIKit...) | `shared/src/iosMain/...` |
| Ảnh, icon, font, string dùng chung | `shared/src/commonMain/composeResources/...` |
| Icon launcher, tên app, theme Android | `androidApp/src/main/res/...` |
| Quyền Android, activity, deep link | `androidApp/src/main/AndroidManifest.xml` |

---

## 1. Chuẩn bị: thêm thư viện cần thiết

Để gọi API bạn cần 4 nhóm thư viện:

1. **Ktor Client** — HTTP client đa nền tảng.
2. **kotlinx.serialization** — parse JSON.
3. **kotlinx.coroutines** — chạy bất đồng bộ.
4. **navigation-compose** (JetBrains) — điều hướng màn hình cho Compose Multiplatform.

Ngoài ra project **đã có** `lifecycle-viewmodel-compose` và `lifecycle-runtime-compose`, nên không cần thêm để dùng `ViewModel` và `collectAsStateWithLifecycle()`.

### 1.1. Sửa `gradle/libs.versions.toml`

Thêm vào mục `[versions]`:

```toml
ktor = "3.6.1"
kotlinx-serialization = "1.12.0"
kotlinx-coroutines = "1.11.0"
navigation = "2.10.0"
```

Thêm vào mục `[libraries]`:

```toml
# JSON
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
# Coroutines
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinx-coroutines" }
# Ktor client core + plugin
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-contentNegotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinxJson = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }
# Ktor engine theo nền tảng
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
# Test
ktor-client-mock = { module = "io.ktor:ktor-client-mock", version.ref = "ktor" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinx-coroutines" }
# Navigation
androidx-navigation-compose = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "navigation" }
```

Thêm vào mục `[plugins]`:

```toml
kotlinSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

### 1.2. Khai báo plugin ở root

Mở `build.gradle.kts` (root), thêm dòng:

```kotlin
alias(libs.plugins.kotlinSerialization) apply false
```

### 1.3. Bật plugin serialization cho module shared

Mở `shared/build.gradle.kts`, thêm vào block `plugins { ... }`:

```kotlin
alias(libs.plugins.kotlinSerialization)
```

### 1.4. Thêm dependency vào source set

Trong `shared/build.gradle.kts`, bên trong `kotlin { sourceSets { ... } }`:

```kotlin
commonMain.dependencies {
    // ... các dependency đang có, giữ nguyên ...
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.serialization.kotlinxJson)
    implementation(libs.ktor.client.logging)
}

androidMain.dependencies {
    // ... giữ nguyên ...
    implementation(libs.ktor.client.okhttp)
}

iosMain.dependencies {
    // ... giữ nguyên ...
    implementation(libs.ktor.client.darwin)
}

commonTest.dependencies {
    // ... giữ nguyên ...
    implementation(libs.ktor.client.mock)
    implementation(libs.kotlinx.coroutines.test)
}
```

Sau khi sửa xong chạy:

```bash
./gradlew :shared:compileKotlinMetadata --offline
```

Nếu pass nghĩa là dependency/plugin đã đúng. (Lần đầu nếu chưa có cache thì bỏ `--offline`.)

---

## 2. Cấu trúc thư mục nên tạo

Đặt trong `shared/src/commonMain/kotlin/nolelamphim/vercel/app/`:

```
nolelamphim/vercel/app/
├── App.kt
├── navigation/
│   └── AppNavHost.kt          # toàn bộ điều hướng
├── di/
│   └── AppContainer.kt        # nơi tạo repository/client dùng chung
├── model/
│   └── Movie.kt               # model cho UI
├── data/
│   ├── MovieRepository.kt
│   └── remote/
│       ├── HttpClientFactory.kt
│       ├── MovieApi.kt
│       └── dto/MovieDto.kt
└── ui/
    └── screens/
        ├── home/
        │   ├── HomeScreen.kt
        │   └── HomeViewModel.kt
        └── detail/
            └── DetailScreen.kt
```

Luồng dữ liệu nên đi theo một chiều:

```
UI (Composable) → ViewModel → Repository → API (Ktor) → Server
        ↑___________ StateFlow (state) __________________|
```

Không gọi API trực tiếp trong Composable.

---

## 3. Bước 1 — Cấu hình HTTP client

### 3.1. Base URL theo nền tảng

Mỗi nền tảng thấy "localhost" khác nhau:

| Môi trường | Base URL |
|---|---|
| Android emulator | `http://10.0.2.2:8080/` |
| Android máy thật cùng Wi-Fi | `http://<IP-LAN-của-máy-tính>:8080/` |
| Android máy thật + `adb reverse` | `http://localhost:8080/` |
| iOS simulator | `http://localhost:8080/` |
| iOS máy thật | `http://<IP-LAN-của-máy-tính>:8080/` |
| API thật | `https://api.example.com/` |

Tạo `expect` trong `commonMain`:

```kotlin
// data/remote/ApiConfig.kt
expect val apiBaseUrl: String
```

`androidMain`:

```kotlin
// data/remote/ApiConfig.android.kt
actual val apiBaseUrl: String = "http://10.0.2.2:8080/"
```

`iosMain`:

```kotlin
// data/remote/ApiConfig.ios.kt
actual val apiBaseUrl: String = "http://localhost:8080/"
```

> Base URL nên **có dấu `/` ở cuối** và khi gọi API dùng đường dẫn **không có `/` ở đầu**, ví dụ `client.get("movies")`. Nếu không sẽ rất dễ bị mất path.

### 3.2. Tạo client theo nền tảng

`commonMain`:

```kotlin
// data/remote/HttpClientFactory.kt
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect fun createPlatformHttpClient(): HttpClient

val appJson: Json = Json {
    ignoreUnknownKeys = true   // server trả field lạ thì bỏ qua, không crash
    isLenient = true
    explicitNulls = false
}

fun HttpClientConfig<*>.applyCommonConfig() {
    install(ContentNegotiation) {
        json(appJson)
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
        connectTimeoutMillis = 15_000
        socketTimeoutMillis = 15_000
    }
    install(Logging) {
        level = LogLevel.INFO
    }
    defaultRequest {
        url(apiBaseUrl)
        contentType(ContentType.Application.Json)
        headers.append(HttpHeaders.Accept, ContentType.Application.Json.toString())
    }
}
```

`androidMain`:

```kotlin
// data/remote/HttpClientFactory.android.kt
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

actual fun createPlatformHttpClient(): HttpClient =
    HttpClient(OkHttp) { applyCommonConfig() }
```

`iosMain`:

```kotlin
// data/remote/HttpClientFactory.ios.kt
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun createPlatformHttpClient(): HttpClient =
    HttpClient(Darwin) { applyCommonConfig() }
```

> Nếu chỉ cần nhanh và không muốn `expect/actual`, có thể thử `HttpClient { applyCommonConfig() }` trong `commonMain` (Ktor tự tìm engine có trên classpath). Cách `expect/actual` ở trên chắc chắn hơn và dễ kiểm soát.

### 3.3. Quyền Internet + HTTP cleartext

**Android:** mở `androidApp/src/main/AndroidManifest.xml`, thêm ngay dưới thẻ `<manifest ...>`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Nếu gọi `http://` (không phải https) khi dev, Android 9+ sẽ chặn. Cách nhanh cho debug: thêm `android:usesCleartextTraffic="true"` vào thẻ `<application>` — **chỉ dùng khi dev**, đừng đưa lên production.

Cách sạch hơn: tạo `androidApp/src/debug/res/xml/network_security_config.xml` cho phép cleartext tới `10.0.2.2`/localhost, và khai báo `android:networkSecurityConfig="@xml/network_security_config"` trong `<application>`. Production nên dùng HTTPS.

**iOS:** nếu gọi `http://` khi dev, mở `iosApp/iosApp/Info.plist`, thêm:

```xml
<key>NSAppTransportSecurity</key>
<dict>
    <key>NSAllowsLocalNetworking</key>
    <true/>
</dict>
```

Production dùng HTTPS thì không cần.

---

## 4. Bước 2 — DTO, API, Repository

### 4.1. DTO (đúng shape JSON của server)

```kotlin
// data/remote/dto/MovieDto.kt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieDto(
    val id: Int,
    val title: String,
    val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
)

@Serializable
data class PopularMoviesResponse(
    val page: Int = 1,
    val results: List<MovieDto> = emptyList(),
    @SerialName("total_pages") val totalPages: Int = 1,
)
```

Quy tắc:
- Field JSON khác tên Kotlin → dùng `@SerialName("tên_json")`.
- Field có thể thiếu → cho default value và/hoặc nullable.
- Class phải có `@Serializable`.
- Không đặt logic UI trong DTO.

### 4.2. API

```kotlin
// data/remote/MovieApi.kt
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class MovieApi(private val client: HttpClient) {

    suspend fun getPopular(page: Int = 1): PopularMoviesResponse =
        client.get("movie/popular") {
            parameter("page", page)
        }.body()

    suspend fun getMovieDetail(id: Int): MovieDto =
        client.get("movie/$id").body()
}
```

Lưu ý:
- Hàm gọi mạng phải là `suspend`.
- `.body<T>()` cần import `io.ktor.client.call.body`.
- `client.get(...)` là suspend, không được gọi trên main thread nếu không có coroutine.

### 4.3. Model cho UI

Tách DTO khỏi model UI để đổi API không ảnh hưởng màn hình:

```kotlin
// model/Movie.kt
data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val rating: Double,
)

fun MovieDto.toMovie(): Movie = Movie(
    id = id,
    title = title,
    overview = overview,
    posterUrl = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" },
    rating = voteAverage,
)
```

### 4.4. Repository

```kotlin
// data/MovieRepository.kt
class MovieRepository(private val api: MovieApi) {

    suspend fun popularMovies(page: Int = 1): List<Movie> =
        api.getPopular(page).results.map { it.toMovie() }

    suspend fun movieDetail(id: Int): Movie =
        api.getMovieDetail(id).toMovie()
}
```

Repository là nơi:
- gọi 1 hoặc nhiều API rồi gộp,
- cache (nếu cần),
- map DTO → model,
- ném lỗi có ý nghĩa.

### 4.5. Nơi tạo dependency (DI thủ công)

```kotlin
// di/AppContainer.kt
object AppContainer {
    val httpClient by lazy { createPlatformHttpClient() }
    val movieApi by lazy { MovieApi(httpClient) }
    val movieRepository by lazy { MovieRepository(movieApi) }
}
```

Khi app lớn hơn có thể thay bằng Koin/koin-compose. Ban đầu dùng object như trên là đủ.

---

## 5. Bước 3 — ViewModel + state

### 5.1. Định nghĩa UiState

```kotlin
// ui/screens/home/HomeUiState.kt
import nolelamphim.vercel.app.model.Movie

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val movies: List<Movie>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
```

Dùng `sealed interface` để Compose `when` bắt buộc xử lý đủ trạng thái.

### 5.2. ViewModel

```kotlin
// ui/screens/home/HomeViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nolelamphim.vercel.app.data.MovieRepository

class HomeViewModel(
    private val repository: MovieRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val movies = repository.popularMovies()
                _uiState.value = HomeUiState.Success(movies)
            } catch (e: CancellationException) {
                throw e                       // KHÔNG nuốt CancellationException
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(
                    e.message ?: "Không tải được dữ liệu"
                )
            }
        }
    }
}
```

Điểm quan trọng:
- `viewModelScope` tự huỷ khi ViewModel bị clear.
- Không dùng `GlobalScope`.
- `catch (e: CancellationException) { throw e }` trước `catch (e: Exception)`.
- UI chỉ đọc `uiState`, không sửa trực tiếp.

### 5.3. Dùng `Dispatchers`

- Công việc CPU nặng: `withContext(Dispatchers.Default) { ... }`.
- Ktor tự lo IO mạng, không cần `Dispatchers.IO`.
- Nếu cần `Dispatchers.IO` hãy kiểm tra target có hỗ trợ không; an toàn nhất trong `commonMain` là `Dispatchers.Default`.

---

## 6. Bước 4 — Viết màn hình

```kotlin
// ui/screens/home/HomeScreen.kt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import nolelamphim.vercel.app.di.AppContainer

@Composable
fun HomeScreen(
    onMovieClick: (Int) -> Unit,
    viewModel: HomeViewModel = viewModel {
        HomeViewModel(AppContainer.movieRepository)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (val s = state) {
        is HomeUiState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
        }

        is HomeUiState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(s.message, color = MaterialTheme.colorScheme.error)
                Button(onClick = viewModel::load) { Text("Thử lại") }
            }
        }

        is HomeUiState.Success -> {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(s.movies, key = { it.id }) { movie ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMovieClick(movie.id) }
                            .padding(16.dp),
                    ) {
                        Text(movie.title, style = MaterialTheme.typography.titleMedium)
                        Text("⭐ ${movie.rating}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
```

Quy tắc UI:
- Screen nhận **callback** (`onMovieClick`), không tự điều hướng.
- Screen không gọi repository/API.
- Luôn xử lý đủ `Loading`, `Error`, `Success`.
- `collectAsStateWithLifecycle()` tốt hơn `collectAsState()` trên Android.
- `items(..., key = { it.id })` giúp list không bị nhảy khi dữ liệu đổi.
- Tách composable nhỏ nếu file dài.

### Preview

Có thể thêm `@Preview` cho từng trạng thái bằng cách tách phần hiển thị thành composable nhận state thuần:

```kotlin
@Preview
@Composable
private fun HomeSuccessPreview() {
    // Gọi composable con với dữ liệu giả
}
```

---

## 7. Bước 5 — Điều hướng (Navigation Compose)

### 7.1. Tạo NavHost

```kotlin
// navigation/AppNavHost.kt
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import nolelamphim.vercel.app.ui.screens.detail.DetailScreen
import nolelamphim.vercel.app.ui.screens.home.HomeScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
    ) {
        composable("home") {
            HomeScreen(
                onMovieClick = { id -> navController.navigate("detail/$id") },
            )
        }

        composable(
            route = "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType }),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: return@composable
            DetailScreen(
                movieId = id,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
```

### 7.2. Gắn vào App.kt

Mở `App.kt`, bên trong `MaterialTheme { ... }` thay nội dung demo bằng:

```kotlin
AppNavHost()
```

### 7.3. Quy tắc route

- Route đơn giản: `"home"`.
- Route có tham số: `"detail/{id}"`, navigate bằng `"detail/$id"`.
- Không truyền object phức tạp qua route; chỉ truyền id rồi màn đích tự load.
- Muốn xoá backstack khi login/logout: dùng `popUpTo(...)` + `launchSingleTop`.
- Tránh navigate trong lúc composition; nên trong callback/lambda.

### 7.4. (Tuỳ chọn) Type-safe route

Navigation 2.10 hỗ trợ route là `@Serializable` object/data class:

```kotlin
@Serializable data object HomeRoute
@Serializable data class DetailRoute(val id: Int)
```

Dùng `composable<HomeRoute> { ... }`, `navController.navigate(DetailRoute(id))`, trong detail dùng `backStackEntry.toRoute<DetailRoute>()`. Cần import `androidx.navigation.toRoute`. Chỉ dùng khi bạn đã bật plugin serialization. Nếu mới bắt đầu, route string dễ hiểu hơn.

---

## 8. Bước 6 — Viết màn hình Detail (ví dụ)

```kotlin
// ui/screens/detail/DetailScreen.kt
@Composable
fun DetailScreen(
    movieId: Int,
    onBack: () -> Unit,
    viewModel: DetailViewModel = viewModel {
        DetailViewModel(AppContainer.movieRepository, movieId)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // when(state) { ... } giống HomeScreen
}
```

DetailViewModel tương tự HomeViewModel nhưng nhận `movieId` và gọi `repository.movieDetail(movieId)`.

Lưu ý: truyền `movieId` vào constructor `viewModel { }` ổn khi id không đổi trong lúc màn hình tồn tại. Nếu id có thể đổi, dùng `LaunchedEffect(movieId) { viewModel.load(movieId) }`.

Nút back: gọi `onBack()` từ UI, không gọi thẳng `navController` trong screen.

---

## 9. Resource: ảnh, icon, string dùng chung

Đặt file vào:

```
shared/src/commonMain/composeResources/
├── drawable/poster_placeholder.png
└── values/strings.xml
```

Dùng:

```kotlin
import nolelamphim.shared.generated.resources.Res
import nolelamphim.shared.generated.resources.poster_placeholder
import org.jetbrains.compose.resources.painterResource

Image(painter = painterResource(Res.drawable.poster_placeholder), contentDescription = null)
```

String:

```xml
<!-- composeResources/values/strings.xml -->
<resources>
    <string name="home_title">Phim phổ biến</string>
</resources>
```

```kotlin
import nolelamphim.shared.generated.resources.home_title
import org.jetbrains.compose.resources.stringResource

Text(stringResource(Res.string.home_title))
```

Lưu ý:
- Tên file chỉ dùng chữ thường, số, dấu `_`.
- Sau khi thêm resource, nếu IDE báo không thấy `Res`, chạy lại Gradle sync/build.
- Resource của **app Android** (icon launcher, app name) vẫn nằm ở `androidApp/src/main/res/`.

---

## 10. Test

### 10.1. Test repository với MockEngine

```kotlin
// commonTest/.../MovieRepositoryTest.kt
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MovieRepositoryTest {

    @Test
    fun popularMovies_mapsDtoToModel() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = """
                    {"page":1,"results":[{"id":1,"title":"A","vote_average":8.5}]}
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json() }
        }
        val repository = MovieRepository(MovieApi(client))

        val movies = repository.popularMovies()

        assertEquals(1, movies.size)
        assertEquals("A", movies.first().title)
    }
}
```

### 10.2. Chạy test

```bash
# Tất cả test chung (trên Linux sẽ bỏ qua iOS)
./gradlew :shared:allTests

# Chỉ test JVM, nhanh
./gradlew :shared:testAndroidHostTest

# Test iOS chỉ chạy được trên macOS
./gradlew :shared:iosSimulatorArm64Test
```

---

## 11. Vòng lặp phát triển & lệnh hay dùng

```bash
# Build APK, không cần thiết bị
./gradlew :androidApp:assembleDebug

# Cài lên emulator/điện thoại đang kết nối
./gradlew :androidApp:installDebug

# Kiểm tra thiết bị
adb devices

# Bật emulator có sẵn
emulator -avd Pixel_5 &

# Đợi emulator boot
adb wait-for-device
adb shell getprop sys.boot_completed

# Mở app
adb shell am start -n nolelamphim.vercel.app/.MainActivity

# Xem log
adb logcat | grep -iE "nolelamphim|ktor|AndroidRuntime"

# Cho app trên máy thật dùng localhost của máy tính
adb reverse tcp:8080 tcp:8080

# Build + test nhanh
./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug
```

APK nằm ở:

```
androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

Quy trình mỗi lần sửa code:

1. Sửa file trong `shared/src/commonMain/...`.
2. Chạy `./gradlew :androidApp:installDebug`.
3. Xem app + `adb logcat`.
4. Nếu UI sai, kiểm tra state trong ViewModel trước khi sửa Composable.
5. Chạy test nếu có logic mới.

---

## 12. Checklist thêm một màn hình mới

Ví dụ thêm màn hình `Favorites`:

- [ ] Tạo `ui/screens/favorites/FavoritesUiState.kt`
- [ ] Tạo `FavoritesViewModel.kt`
- [ ] Tạo `FavoritesScreen.kt` (nhận state + callback)
- [ ] Thêm route vào `navigation/AppNavHost.kt`
- [ ] Thêm nút/điều hướng từ màn khác tới route mới
- [ ] Nếu cần API: thêm hàm vào `MovieApi`, `MovieRepository`
- [ ] Nếu cần DTO: thêm `@Serializable` class
- [ ] Nếu cần ảnh/string: thêm vào `composeResources`
- [ ] Thêm test cho logic mới trong `commonTest`
- [ ] Chạy `./gradlew :shared:testAndroidHostTest :androidApp:installDebug`

---

## 13. Lỗi thường gặp & cách xử lý

| Lỗi | Nguyên nhân | Cách xử lý |
|---|---|---|
| `No connected devices!` | Không có emulator/điện thoại | Bật emulator, `adb devices`, bật USB debugging |
| `Cleartext HTTP traffic not permitted` | Gọi `http://` trên Android 9+ | Thêm `usesCleartextTraffic` (dev) hoặc network security config, production dùng HTTPS |
| `Serializer for class 'X' is not found` | Chưa bật plugin serialization hoặc thiếu `@Serializable` | Thêm plugin vào `shared`, annotate DTO |
| `JsonDecodingException: Unexpected JSON token` | JSON không khớp DTO | Kiểm tra field, dùng `@SerialName`, `ignoreUnknownKeys = true` |
| `Unresolved reference: Res` | Resource chưa được generate | Sync/rebuild, kiểm tra đúng thư mục `composeResources` |
| `Unresolved reference: ktor` | Thiếu dependency hoặc sai source set | Kiểm tra `commonMain`/`androidMain`/`iosMain` trong `shared/build.gradle.kts` |
| Android emulator gọi `localhost:8080` không được | `localhost` của emulator là chính nó | Dùng `10.0.2.2`, hoặc `adb reverse tcp:8080 tcp:8080` + `localhost` |
| iOS bị chặn HTTP | App Transport Security | Thêm `NSAllowsLocalNetworking` (dev) hoặc dùng HTTPS |
| `NetworkOnMainThreadException` | Gọi mạng ngoài coroutine | Gọi trong `viewModelScope.launch` / hàm `suspend` |
| API trả 404 | Base URL/đường dẫn sai | Base URL có `/` cuối, path không có `/` đầu |
| `ViewModel` mất state khi xoay màn hình | Dùng `remember` thay vì `viewModel()` | Dùng `androidx.lifecycle.viewmodel.compose.viewModel` |
| App crash khi rời màn hình | Nuốt `CancellationException` | Rethrow `CancellationException` trước catch `Exception` |
| iOS test fail trên Linux | Simulator cần macOS | Bỏ qua, dùng test JVM |
| Warning `iosSimulatorArm64Test disabled` | Host Linux | Đã xử lý bằng `kotlin.native.ignoreDisabledTargets=true` |

---

## 14. Bảo mật & cấu hình

- Không hardcode API key trong source public.
- Android: có thể đọc key từ `local.properties` rồi đưa vào `BuildConfig` (cần bật `buildFeatures.buildConfig = true` trong `androidApp`).
- iOS: dùng `xcconfig` hoặc `Info.plist`.
- Tốt nhất: key nằm ở backend, app gọi backend của bạn.
- Không commit `local.properties`, file `.jks`, hay token.
- Bật R8/ProGuard cho release khi đã ổn định; giữ rule cho serialization nếu cần.

---

## 15. Thứ tự làm đề xuất (follow đúng thứ tự)

1. Thêm dependency + plugin theo **Mục 1**, chạy `./gradlew :shared:compileKotlinMetadata`.
2. Tạo `ApiConfig` + `HttpClientFactory` theo **Mục 3**, chạy build để chắc Ktor hoạt động.
3. Tạo DTO → API → Repository theo **Mục 4**.
4. Viết `HomeViewModel` + `HomeScreen` theo **Mục 5–6**.
5. Gắn `AppNavHost` vào `App.kt` theo **Mục 7**.
6. Thêm màn Detail theo **Mục 8**.
7. Thêm test theo **Mục 10**.
8. Chạy app trên emulator, sửa UI, thêm màn tiếp theo theo checklist **Mục 12**.

---

## 16. Ghi nhớ nhanh

```
UI chỉ hiển thị state + phát callback.
ViewModel giữ state + gọi Repository.
Repository gọi API + map dữ liệu.
API chỉ biết Ktor.
DTO khớp JSON, Model khớp UI.
Mọi thứ dùng chung đặt ở commonMain.
Code chỉ một nền tảng đặt ở androidMain/iosMain.
Dependency khai báo trong gradle/libs.versions.toml.
Không sửa build/, không hardcode URL/key.
```
