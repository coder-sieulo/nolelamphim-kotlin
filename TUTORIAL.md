# TUTORIAL — Tự xây app xem phim Nolelamphim trên API có sẵn

> Tài liệu dành cho chính project này. **API phim đã có sẵn**, bạn không cần viết backend.
> Mục tiêu: tự hoàn thiện app xem phim Android + iOS bằng Kotlin Multiplatform & Compose Multiplatform.
>
> Không sửa gì trong thư mục `build/`. Đó là output của Gradle.

---

## 0. Bạn đang có gì?

| Thành phần              | Trạng thái                       |
| ----------------------- | -------------------------------- |
| Khung KMP Android + iOS | Đã có, build được                |
| UI Compose dùng chung   | Đã có `App.kt` mẫu               |
| API phim                | **Đã có sẵn** (VSMOV public API) |
| Tầng network/API client | Chưa có — bạn sẽ tự thêm         |
| Các màn hình phim       | Chưa có — bạn sẽ tự thêm         |

Nguồn API:

- Base URL: `https://vsmov.com/api/`
- Tài liệu gốc: <https://vsmov.com/api-document>
- Không cần token, không cần đăng ký cho các endpoint phim.
- Tất cả request dùng `GET`, trả JSON UTF-8.

---

## 1. Hợp đồng API (đọc kỹ trước khi code)

### 1.1. Bảng endpoint

| Mục đích           | Method + Endpoint                      | Query params                                   |
| ------------------ | -------------------------------------- | ---------------------------------------------- |
| Phim mới cập nhật  | `GET /api/danh-sach/phim-moi-cap-nhat` | `page`, `limit`                                |
| Danh sách + lọc    | `GET /api/danh-sach`                   | `category`, `country`, `year`, `page`, `limit` |
| Theo nhóm phim     | `GET /api/danh-sach/{slug}`            | `page`, `limit`                                |
| Tìm kiếm           | `GET /api/tim-kiem`                    | `keyword`, `page`, `limit`                     |
| Danh sách thể loại | `GET /api/the-loai`                    | —                                              |
| Danh sách quốc gia | `GET /api/quoc-gia`                    | —                                              |
| Phim theo thể loại | `GET /api/the-loai/{slug}`             | `page`, `limit`                                |
| Phim theo quốc gia | `GET /api/quoc-gia/{slug}`             | `page`, `limit`                                |
| Chi tiết phim      | `GET /api/phim/{slug}`                 | —                                              |

Các `slug` nhóm đã kiểm chứng:

```
phim-moi, phim-bo, phim-le, phim-chieu-rap, phim-moi-cap-nhat,
subteam, thuyet-minh, long-tieng, dang-chieu, 4k
```

### 1.2. Response danh sách

`GET https://vsmov.com/api/danh-sach/phim-moi-cap-nhat?page=1&limit=20`

```json
{
  "status": true,
  "items": [
    {
      "tmdb": {
        "type": "tv",
        "id": "279246",
        "vote_average": "0.0",
        "vote_count": 0
      },
      "imdb": { "id": "tt39325975" },
      "modified": { "time": "2026-11-04T00:32:45+07:00" },
      "_id": 54207,
      "name": "Hoàng Hậu Tái Hôn",
      "origin_name": "The Remarried Empress",
      "slug": "hoang-hau-tai-hon",
      "poster_url": "https://vsmov.com/storage/images/....jpg",
      "thumb_url": "https://vsmov.com/storage/images/....jpg",
      "year": 2026
    }
  ],
  "pagination": {
    "totalItems": 20928,
    "totalItemsPerPage": 24,
    "currentPage": 1,
    "totalPages": 872
  }
}
```

### 1.3. Response chi tiết

`GET https://vsmov.com/api/phim/hoang-hau-tai-hon`

```json
{
  "status": true,
  "msg": "",
  "movie": {
    "_id": 54207,
    "name": "Hoàng Hậu Tái Hôn",
    "origin_name": "The Remarried Empress",
    "slug": "hoang-hau-tai-hon",
    "content": "Mô tả phim...",
    "type": "series",
    "status": "ongoing",
    "poster_url": "https://vsmov.com/storage/images/....jpg",
    "thumb_url": "https://vsmov.com/storage/images/....jpg",
    "trailer_url": null,
    "time": "60 phút",
    "episode_current": "Tập 2",
    "episode_total": "10",
    "quality": "HD",
    "lang": "Vietsub",
    "year": 2026,
    "view": 12345,
    "chieurap": false,
    "actor": ["Shin Min-a", "Ju Ji-hoon"],
    "director": ["Jo Soo-won", "Sim Na-yeon"],
    "category": [
      { "id": 15, "name": "Chính Kịch", "slug": "chinh-kich" },
      { "id": 46, "name": "Viễn Tưởng", "slug": "vien-tuong" }
    ],
    "country": [
      { "id": 10, "name": "Hàn Quốc", "slug": "han-quoc" }
    ]
  },
  "episodes": [
    {
      "server_name": "Vietsub #1",
      "server_data": [
        {
          "name": "1",
          "slug": "tap-1",
          "filename": "1",
          "link_embed": "https://v1.streamvsmov.com/video/...."
        }
      ]
    }
  ]
}
```

### 1.4. Response thể loại / quốc gia

`GET https://vsmov.com/api/the-loai`

```json
{
  "status": "success",
  "message": "",
  "data": {
    "items": [
      { "_id": 15, "name": "Chính Kịch", "slug": "chinh-kich" }
    ]
  }
}
```

Quốc gia có cùng cấu trúc, endpoint `/api/quoc-gia`.

### 1.5. Những điểm "bẫy" của API

1. **`status` không nhất quán**: có endpoint trả `true` (boolean), có endpoint trả `"success"` (string). → **Đừng** khai báo `status: Boolean`; hãy bỏ qua field này trong DTO.
2. **`totalItemsPerPage` không nhất quán**: lúc là `24`, lúc là `"2"` khi truyền `limit`. → Chỉ khai báo `totalItems`, `currentPage`, `totalPages` (luôn là số).
3. **`tmdb.vote_average` là String** (`"8.2"`), không phải Double. → Dùng `String?` rồi `toDoubleOrNull()`.
4. **`poster_url` / `thumb_url` có thể null** → model để nullable, UI hiển thị placeholder.
5. **`episodes` có thể rỗng** (phim chưa có tập / chỉ có trailer) → UI phải xử lý empty state.
6. **`link_embed` là trang HTML**, không phải file `.mp4`/`.m3u8` → phát bằng WebView.
7. **Ảnh là URL tuyệt đối** (`https://vsmov.com/storage/...`) → dùng trực tiếp, không cần ghép base.
8. **Không có endpoint "trang chủ"** → tự ghép từ nhiều endpoint danh sách.
9. **Không có endpoint năm phát hành riêng** → dùng `year` trong `/api/danh-sach`.

### 1.6. Kiểm thử API bằng terminal

```bash
curl -s "https://vsmov.com/api/danh-sach/phim-moi-cap-nhat?page=1&limit=5" | head -c 600
curl -s "https://vsmov.com/api/danh-sach/phim-bo?page=1&limit=5" | head -c 600
curl -s "https://vsmov.com/api/tim-kiem?keyword=avengers&limit=5" | head -c 600
curl -s "https://vsmov.com/api/phim/hoang-hau-tai-hon" | head -c 600
curl -s "https://vsmov.com/api/the-loai" | head -c 600
```

---

## 2. Kiến trúc & cấu trúc thư mục

Luồng dữ liệu một chiều:

```
Composable (UI)
    ↓ gọi hàm / phát callback
ViewModel (StateFlow<UiState>)
    ↓
Repository (map DTO → model, gộp/cache)
    ↓
MovieApi (Ktor)
    ↓
https://vsmov.com/api
```

Quy tắc:

- Composable **không** gọi API, **không** giữ logic tải dữ liệu.
- ViewModel **không** biết Ktor, chỉ biết Repository.
- Repository **không** biết Compose.
- DTO khớp JSON; model khớp nhu cầu UI.

Cấu trúc nên tạo trong `shared/src/commonMain/kotlin/nolelamphim/vercel/app/`:

```
nolelamphim/vercel/app/
├── App.kt
├── navigation/
│   └── AppNavHost.kt
├── di/
│   └── AppContainer.kt
├── model/
│   ├── Movie.kt
│   ├── MovieDetail.kt
│   └── Episode.kt
├── data/
│   ├── MovieRepository.kt
│   └── remote/
│       ├── ApiConfig.kt
│       ├── HttpClientFactory.kt
│       ├── HttpClientFactory.android.kt
│       ├── HttpClientFactory.ios.kt
│       ├── MovieApi.kt
│       └── dto/
│           ├── MovieDto.kt
│           ├── TaxonomyDto.kt
│           └── MovieDetailDto.kt
└── ui/
    ├── components/
    │   ├── MovieCard.kt
    │   ├── MovieRow.kt
    │   └── ErrorView.kt
    └── screens/
        ├── home/
        ├── search/
        ├── detail/
        └── watch/
```

Code riêng nền tảng đặt ở:

- `shared/src/androidMain/kotlin/...`
- `shared/src/iosMain/kotlin/...`

---

## 3. Thêm thư viện

Mở `gradle/libs.versions.toml`.

### 3.1. `[versions]`

```toml
ktor = "3.6.1"
kotlinx-serialization = "1.12.0"
kotlinx-coroutines = "1.11.0"
navigation = "2.10.0"
coil = "3.6.3"
```

### 3.2. `[libraries]`

```toml
# JSON
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
# Coroutines
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinx-coroutines" }
# Ktor
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-contentNegotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinxJson = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
# Test
ktor-client-mock = { module = "io.ktor:ktor-client-mock", version.ref = "ktor" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinx-coroutines" }
# Navigation
androidx-navigation-compose = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "navigation" }
# Ảnh
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }
coil-network-ktor3 = { module = "io.coil-kt.coil3:coil-network-ktor3", version.ref = "coil" }
```

### 3.3. `[plugins]`

```toml
kotlinSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

### 3.4. Root `build.gradle.kts`

```kotlin
alias(libs.plugins.kotlinSerialization) apply false
```

### 3.5. `shared/build.gradle.kts`

Thêm plugin:

```kotlin
plugins {
    // ... các plugin đang có
    alias(libs.plugins.kotlinSerialization)
}
```

Thêm dependency vào đúng source set:

```kotlin
sourceSets {
    commonMain.dependencies {
        // ... giữ nguyên dependency hiện có
        implementation(libs.androidx.navigation.compose)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.ktor.client.core)
        implementation(libs.ktor.client.contentNegotiation)
        implementation(libs.ktor.serialization.kotlinxJson)
        implementation(libs.ktor.client.logging)
        implementation(libs.coil.compose)
        implementation(libs.coil.network.ktor3)
    }
    androidMain.dependencies {
        implementation(libs.ktor.client.okhttp)
    }
    iosMain.dependencies {
        implementation(libs.ktor.client.darwin)
    }
    commonTest.dependencies {
        implementation(libs.ktor.client.mock)
        implementation(libs.kotlinx.coroutines.test)
    }
}
```

Kiểm tra:

```bash
./gradlew :shared:compileKotlinMetadata
```

### 3.6. Quyền Internet (Android)

Mở `androidApp/src/main/AndroidManifest.xml`, thêm dưới thẻ `<manifest>`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

API dùng HTTPS nên **không** cần `usesCleartextTraffic`. Chỉ thêm nếu sau này gọi backend nội bộ bằng `http://`.

---

## 4. Tầng HTTP client

### 4.1. Hằng số cấu hình

Vì API là HTTPS và dùng chung cho mọi nền tảng, không cần `expect/actual` cho base URL:

```kotlin
// data/remote/ApiConfig.kt
const val API_BASE_URL = "https://vsmov.com/api/"
const val DEFAULT_PAGE_SIZE = 20
```

> Base URL **phải có `/` ở cuối**. Khi gọi dùng path **không có `/` ở đầu**, ví dụ `client.get("phim/$slug")`.

### 4.2. Cấu hình chung

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
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
}

fun HttpClientConfig<*>.applyCommonConfig() {
    expectSuccess = true

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
        url(API_BASE_URL)
        contentType(ContentType.Application.Json)
        headers.append(HttpHeaders.Accept, ContentType.Application.Json.toString())
    }
}
```

`expectSuccess = true` giúp Ktor ném exception khi HTTP 4xx/5xx thay vì trả body lỗi im lặng.

### 4.3. Engine theo nền tảng

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

---

## 5. DTO — khớp đúng JSON

### 5.1. DTO danh sách phim

```kotlin
// data/remote/dto/MovieDto.kt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbDto(
    val type: String? = null,
    @SerialName("vote_average") val voteAverage: String? = null,
)

@Serializable
data class MovieSummaryDto(
    @SerialName("_id") val id: Int,
    val name: String,
    @SerialName("origin_name") val originName: String = "",
    val slug: String,
    @SerialName("poster_url") val posterUrl: String? = null,
    @SerialName("thumb_url") val thumbUrl: String? = null,
    val year: Int? = null,
    val tmdb: TmdbDto? = null,
)

@Serializable
data class PaginationDto(
    val totalItems: Int = 0,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
)

@Serializable
data class MovieListResponse(
    val items: List<MovieSummaryDto> = emptyList(),
    val pagination: PaginationDto = PaginationDto(),
)
```

### 5.2. DTO thể loại / quốc gia

```kotlin
// data/remote/dto/TaxonomyDto.kt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaxonomyDto(
    @SerialName("_id") val id: Int = 0,
    val name: String,
    val slug: String,
)

@Serializable
data class TaxonomyDataDto(
    val items: List<TaxonomyDto> = emptyList(),
)

@Serializable
data class TaxonomyResponse(
    val data: TaxonomyDataDto = TaxonomyDataDto(),
)
```

### 5.3. DTO chi tiết phim + tập phim

```kotlin
// data/remote/dto/MovieDetailDto.kt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieDetailDto(
    @SerialName("_id") val id: Int,
    val name: String,
    @SerialName("origin_name") val originName: String = "",
    val slug: String,
    val content: String = "",
    val type: String = "",
    val status: String = "",
    @SerialName("poster_url") val posterUrl: String? = null,
    @SerialName("thumb_url") val thumbUrl: String? = null,
    @SerialName("trailer_url") val trailerUrl: String? = null,
    val time: String? = null,
    @SerialName("episode_current") val episodeCurrent: String? = null,
    @SerialName("episode_total") val episodeTotal: String? = null,
    val quality: String? = null,
    val lang: String? = null,
    val year: Int? = null,
    val view: Long? = null,
    val chieurap: Boolean? = null,
    val actor: List<String> = emptyList(),
    val director: List<String> = emptyList(),
    val category: List<TaxonomyDto> = emptyList(),
    val country: List<TaxonomyDto> = emptyList(),
    val tmdb: TmdbDto? = null,
)

@Serializable
data class EpisodeDto(
    val name: String,
    val slug: String = "",
    val filename: String = "",
    @SerialName("link_embed") val linkEmbed: String = "",
)

@Serializable
data class ServerDto(
    @SerialName("server_name") val serverName: String,
    @SerialName("server_data") val serverData: List<EpisodeDto> = emptyList(),
)

@Serializable
data class MovieDetailResponse(
    val movie: MovieDetailDto,
    val episodes: List<ServerDto> = emptyList(),
)
```

---

## 6. API service

```kotlin
// data/remote/MovieApi.kt
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class MovieApi(private val client: HttpClient) {

    suspend fun latest(
        page: Int = 1,
        limit: Int = DEFAULT_PAGE_SIZE,
    ): MovieListResponse =
        client.get("danh-sach/phim-moi-cap-nhat") {
            parameter("page", page)
            parameter("limit", limit)
        }.body()

    suspend fun byCategory(
        categorySlug: String,
        page: Int = 1,
        limit: Int = DEFAULT_PAGE_SIZE,
    ): MovieListResponse =
        client.get("danh-sach/$categorySlug") {
            parameter("page", page)
            parameter("limit", limit)
        }.body()

    suspend fun filtered(
        category: String? = null,
        country: String? = null,
        year: Int? = null,
        page: Int = 1,
        limit: Int = DEFAULT_PAGE_SIZE,
    ): MovieListResponse =
        client.get("danh-sach") {
            category?.let { parameter("category", it) }
            country?.let { parameter("country", it) }
            year?.let { parameter("year", it) }
            parameter("page", page)
            parameter("limit", limit)
        }.body()

    suspend fun search(
        keyword: String,
        page: Int = 1,
        limit: Int = DEFAULT_PAGE_SIZE,
    ): MovieListResponse =
        client.get("tim-kiem") {
            parameter("keyword", keyword)
            parameter("page", page)
            parameter("limit", limit)
        }.body()

    suspend fun detail(slug: String): MovieDetailResponse =
        client.get("phim/$slug").body()

    suspend fun genres(): List<TaxonomyDto> =
        client.get("the-loai").body<TaxonomyResponse>().data.items

    suspend fun countries(): List<TaxonomyDto> =
        client.get("quoc-gia").body<TaxonomyResponse>().data.items
}
```

Ghi nhớ:

- Hàm network phải `suspend`.
- `.body<T>()` cần import `io.ktor.client.call.body`.
- Không tự ghép URL bằng string có dấu `/` ở đầu path.
- Ktor tự URL-encode `keyword` tiếng Việt khi dùng `parameter`.

---

## 7. Model & Repository

### 7.1. Model cho UI

```kotlin
// model/Movie.kt
data class Movie(
    val id: Int,
    val name: String,
    val originName: String,
    val slug: String,
    val posterUrl: String?,
    val thumbUrl: String?,
    val year: Int?,
    val rating: Double,
)

fun MovieSummaryDto.toMovie(): Movie = Movie(
    id = id,
    name = name,
    originName = originName,
    slug = slug,
    posterUrl = posterUrl,
    thumbUrl = thumbUrl,
    year = year,
    rating = tmdb?.voteAverage?.toDoubleOrNull() ?: 0.0,
)
```

```kotlin
// model/MovieDetail.kt
data class MovieDetail(
    val id: Int,
    val name: String,
    val originName: String,
    val slug: String,
    val description: String,
    val posterUrl: String?,
    val year: Int?,
    val quality: String?,
    val lang: String?,
    val status: String?,
    val episodeCurrent: String?,
    val episodeTotal: String?,
    val duration: String?,
    val rating: Double,
    val categories: List<String>,
    val countries: List<String>,
    val actors: List<String>,
    val directors: List<String>,
    val servers: List<EpisodeServer>,
)

fun MovieDetailDto.toDetail(servers: List<ServerDto>): MovieDetail = MovieDetail(
    id = id,
    name = name,
    originName = originName,
    slug = slug,
    description = content,
    posterUrl = posterUrl,
    year = year,
    quality = quality,
    lang = lang,
    status = status,
    episodeCurrent = episodeCurrent,
    episodeTotal = episodeTotal,
    duration = time,
    rating = tmdb?.voteAverage?.toDoubleOrNull() ?: 0.0,
    categories = category.map { it.name },
    countries = country.map { it.name },
    actors = actor,
    directors = director,
    servers = servers.map { it.toServer() },
)
```

```kotlin
// model/Episode.kt
data class Episode(
    val name: String,
    val slug: String,
    val embedUrl: String,
)

data class EpisodeServer(
    val name: String,
    val episodes: List<Episode>,
)

fun ServerDto.toServer(): EpisodeServer = EpisodeServer(
    name = serverName.trim(),
    episodes = serverData.map {
        Episode(
            name = it.name,
            slug = it.slug,
            embedUrl = it.linkEmbed,
        )
    },
)
```

### 7.2. Repository

```kotlin
// data/MovieRepository.kt
class MovieRepository(private val api: MovieApi) {

    suspend fun latest(page: Int = 1): List<Movie> =
        api.latest(page).items.map { it.toMovie() }

    suspend fun byCategory(categorySlug: String, page: Int = 1): List<Movie> =
        api.byCategory(categorySlug, page).items.map { it.toMovie() }

    suspend fun filtered(
        category: String? = null,
        country: String? = null,
        year: Int? = null,
        page: Int = 1,
    ): List<Movie> =
        api.filtered(category, country, year, page).items.map { it.toMovie() }

    suspend fun search(keyword: String, page: Int = 1): List<Movie> =
        api.search(keyword, page).items.map { it.toMovie() }

    suspend fun detail(slug: String): MovieDetail {
        val response = api.detail(slug)
        return response.movie.toDetail(response.episodes)
    }

    suspend fun genres(): List<Taxonomy> =
        api.genres().map { Taxonomy(it.name, it.slug) }

    suspend fun countries(): List<Taxonomy> =
        api.countries().map { Taxonomy(it.name, it.slug) }
}

data class Taxonomy(val name: String, val slug: String)
```

### 7.3. DI thủ công

```kotlin
// di/AppContainer.kt
object AppContainer {
    val httpClient by lazy { createPlatformHttpClient() }
    val movieApi by lazy { MovieApi(httpClient) }
    val movieRepository by lazy { MovieRepository(movieApi) }
}
```

Khi app lớn hơn có thể thay bằng Koin, nhưng ban đầu object này là đủ và dễ test.

---

## 8. ViewModel & UiState

### 8.1. Home

```kotlin
// ui/screens/home/HomeUiState.kt
import nolelamphim.vercel.app.model.Movie

data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val hero: Movie? = null,
    val latest: List<Movie> = emptyList(),
    val series: List<Movie> = emptyList(),
    val singles: List<Movie> = emptyList(),
    val cinema: List<Movie> = emptyList(),
)
```

```kotlin
// ui/screens/home/HomeViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nolelamphim.vercel.app.data.MovieRepository

class HomeViewModel(
    private val repository: MovieRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)
            try {
                coroutineScope {
                    val latest = async { safe { repository.latest() } }
                    val series = async { safe { repository.byCategory("phim-bo") } }
                    val singles = async { safe { repository.byCategory("phim-le") } }
                    val cinema = async { safe { repository.byCategory("phim-chieu-rap") } }

                    val latestMovies = latest.await().getOrDefault(emptyList())
                    _uiState.value = HomeUiState(
                        isLoading = false,
                        hero = latestMovies.firstOrNull(),
                        latest = latestMovies,
                        series = series.await().getOrDefault(emptyList()),
                        singles = singles.await().getOrDefault(emptyList()),
                        cinema = cinema.await().getOrDefault(emptyList()),
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = HomeUiState(
                    isLoading = false,
                    error = e.message ?: "Không tải được trang chủ",
                )
            }
        }
    }
}

private suspend fun <T> safe(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
```

Điểm hay: `safe` cho phép một hàng phim lỗi nhưng các hàng khác vẫn hiển thị.

### 8.2. Search

```kotlin
// ui/screens/search/SearchUiState.kt
data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<Movie> = emptyList(),
    val error: String? = null,
    val page: Int = 0,
    val canLoadMore: Boolean = false,
)
```

```kotlin
// ui/screens/search/SearchViewModel.kt
class SearchViewModel(
    private val repository: MovieRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.value = SearchUiState(query = query)
            return
        }

        searchJob = viewModelScope.launch {
            delay(400) // debounce
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val movies = repository.search(query, page = 1)
                _uiState.value = SearchUiState(
                    query = query,
                    results = movies,
                    page = 1,
                    canLoadMore = movies.isNotEmpty(),
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Tìm kiếm thất bại",
                )
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || !state.canLoadMore || state.query.isBlank()) return

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            try {
                val nextPage = state.page + 1
                val movies = repository.search(state.query, nextPage)
                _uiState.value = state.copy(
                    isLoading = false,
                    results = state.results + movies,
                    page = nextPage,
                    canLoadMore = movies.isNotEmpty(),
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = e.message ?: "Không tải thêm được",
                )
            }
        }
    }
}
```

Cần import: `kotlinx.coroutines.Job`, `kotlinx.coroutines.delay`, `kotlinx.coroutines.launch`, `kotlinx.coroutines.flow.*`, `androidx.lifecycle.*`.

### 8.3. Detail

```kotlin
sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val movie: MovieDetail) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class DetailViewModel(
    private val repository: MovieRepository,
    private val slug: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                _uiState.value = DetailUiState.Success(repository.detail(slug))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(
                    e.message ?: "Không tải được chi tiết phim",
                )
            }
        }
    }
}
```

---

## 9. UI Trang chủ

### 9.1. MovieCard + MovieRow

```kotlin
// ui/components/MovieCard.kt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import nolelamphim.vercel.app.model.Movie

@Composable
fun MovieCard(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(130.dp)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = movie.name,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = buildString {
                append(movie.year ?: "")
                if (movie.rating > 0) append("  ⭐ ${movie.rating}")
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
```

```kotlin
// ui/components/MovieRow.kt
@Composable
fun MovieRow(
    title: String,
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
) {
    if (movies.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(movies, key = { it.id }) { movie ->
                MovieCard(
                    movie = movie,
                    onClick = { onMovieClick(movie) },
                )
            }
        }
    }
}
```

### 9.2. HomeScreen

```kotlin
// ui/screens/home/HomeScreen.kt
@Composable
fun HomeScreen(
    onMovieClick: (Movie) -> Unit,
    viewModel: HomeViewModel = viewModel {
        HomeViewModel(AppContainer.movieRepository)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        state.isLoading -> LoadingView()

        state.error != null -> ErrorView(
            message = state.error,
            onRetry = viewModel::load,
        )

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                state.hero?.let { hero ->
                    item { HeroBanner(movie = hero, onClick = { onMovieClick(hero) }) }
                }
                item {
                    MovieRow("Mới cập nhật", state.latest, onMovieClick)
                }
                item {
                    MovieRow("Phim bộ", state.series, onMovieClick)
                }
                item {
                    MovieRow("Phim lẻ", state.singles, onMovieClick)
                }
                item {
                    MovieRow("Phim chiếu rạp", state.cinema, onMovieClick)
                }
            }
        }
    }
}
```

`HeroBanner` tự viết: `Box` cao ~420dp, `AsyncImage` dùng `thumbUrl ?: posterUrl`, gradient tối phía dưới, tên phim + nút "Xem ngay". Dùng `Brush.verticalGradient`.

`LoadingView` / `ErrorView` là composable nhỏ tự tạo trong `ui/components`.

---

## 10. UI Tìm kiếm + Lọc

### 10.1. SearchScreen

```kotlin
@Composable
fun SearchScreen(
    onMovieClick: (Movie) -> Unit,
    viewModel: SearchViewModel = viewModel {
        SearchViewModel(AppContainer.movieRepository)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Tìm phim...") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        )

        when {
            state.isLoading && state.results.isEmpty() -> LoadingView()
            state.error != null && state.results.isEmpty() -> ErrorView(
                message = state.error,
                onRetry = { viewModel.onQueryChange(state.query) },
            )
            state.query.isBlank() -> EmptyView("Nhập tên phim để tìm")
            state.results.isEmpty() -> EmptyView("Không tìm thấy phim")
            else -> MovieGrid(
                movies = state.results,
                onMovieClick = onMovieClick,
                onLoadMore = viewModel::loadMore,
            )
        }
    }
}
```

`MovieGrid` dùng `LazyVerticalGrid(columns = GridCells.Fixed(3))`, và khi item cuối xuất hiện thì gọi `onLoadMore()`:

```kotlin
if (index == movies.lastIndex) {
    LaunchedEffect(index) { onLoadMore() }
}
```

### 10.2. Bộ lọc

Tạo `FilterViewModel`:

```kotlin
data class FilterUiState(
    val genres: List<Taxonomy> = emptyList(),
    val countries: List<Taxonomy> = emptyList(),
    val selectedGenre: String? = null,
    val selectedCountry: String? = null,
    val selectedYear: Int? = null,
    val results: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class FilterViewModel(
    private val repository: MovieRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FilterUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val genres = runCatching { repository.genres() }.getOrDefault(emptyList())
            val countries = runCatching { repository.countries() }.getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                genres = genres,
                countries = countries,
            )
        }
    }

    fun selectGenre(slug: String?) {
        _uiState.value = _uiState.value.copy(
            selectedGenre = if (_uiState.value.selectedGenre == slug) null else slug,
        )
    }

    fun selectCountry(slug: String?) {
        _uiState.value = _uiState.value.copy(
            selectedCountry = if (_uiState.value.selectedCountry == slug) null else slug,
        )
    }

    fun selectYear(year: Int?) {
        _uiState.value = _uiState.value.copy(selectedYear = year)
    }

    fun apply() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            try {
                val movies = repository.filtered(
                    category = state.selectedGenre,
                    country = state.selectedCountry,
                    year = state.selectedYear,
                )
                _uiState.value = state.copy(isLoading = false, results = movies)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = e.message ?: "Lọc phim thất bại",
                )
            }
        }
    }
}
```

UI:

- `LazyRow` chip cho thể loại.
- `LazyRow` chip cho quốc gia.
- Dropdown/`LazyRow` cho năm (tự sinh `2026 downTo 1990`).
- Nút "Áp dụng" gọi `apply()`.
- Kết quả hiển thị bằng `MovieGrid`.

Nếu muốn gộp vào màn Search: thêm nút "Bộ lọc" mở `ModalBottomSheet` chứa các chip trên.

---

## 11. UI Chi tiết phim

```kotlin
@Composable
fun DetailScreen(
    slug: String,
    onBack: () -> Unit,
    onPlay: (slug: String, serverIndex: Int, episodeIndex: Int) -> Unit,
    viewModel: DetailViewModel = viewModel {
        DetailViewModel(AppContainer.movieRepository, slug)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (val s = state) {
        DetailUiState.Loading -> LoadingView()
        is DetailUiState.Error -> ErrorView(s.message, onRetry = viewModel::load)
        is DetailUiState.Success -> DetailContent(
            movie = s.movie,
            onBack = onBack,
            onPlay = onPlay,
        )
    }
}
```

`DetailContent` gồm:

1. Poster lớn + gradient + nút Back.
2. Tên phim, tên gốc, năm, chất lượng, ngôn ngữ, rating.
3. Chips thể loại/quốc gia.
4. `episodeCurrent / episodeTotal`, thời lượng, trạng thái.
5. Mô tả (`description`).
6. Diễn viên, đạo diễn.
7. Danh sách server → mỗi server là một hàng chip tập. Bấm tập nào gọi:
   ```kotlin
   onPlay(movie.slug, serverIndex, episodeIndex)
   ```

Nếu `servers.isEmpty()` hiển thị: "Phim chưa có tập phim".

---

## 12. Xem phim bằng WebView / WKWebView

`link_embed` là trang HTML có player, nên cách đơn giản và ổn định nhất là nhúng WebView.

### 12.1. Khai báo expect

```kotlin
// ui/components/VideoPlayer.kt (commonMain)
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VideoPlayer(url: String, modifier: Modifier = Modifier)
```

### 12.2. Android actual

```kotlin
// ui/components/VideoPlayer.android.kt (androidMain)
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun VideoPlayer(url: String, modifier: Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.url != url) webView.loadUrl(url)
        },
    )
}
```

Thêm `android:hardwareAccelerated="true"` cho `<application>` nếu video bị đen (mặc định thường đã bật).

### 12.3. iOS actual

```kotlin
// ui/components/VideoPlayer.ios.kt (iosMain)
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

@Composable
actual fun VideoPlayer(url: String, modifier: Modifier) {
    UIKitView(
        modifier = modifier,
        factory = {
            WKWebView(
                frame = CGRectZero,
                configuration = WKWebViewConfiguration(),
            ).apply {
                loadRequest(NSURLRequest(NSURL(string = url)))
            }
        },
        update = { webView ->
            if (webView.URL?.absoluteString != url) {
                webView.loadRequest(NSURLRequest(NSURL(string = url)))
            }
        },
    )
}
```

Có thể cần `@OptIn(ExperimentalForeignApi::class)` / `@OptIn(ExperimentalComposeUiApi::class)` tùy phiên bản Compose. Nếu IDE báo thiếu, thêm opt-in.

### 12.4. WatchScreen + WatchViewModel

Route `watch/{slug}/{server}/{episode}`. `WatchViewModel` nhận 3 tham số, gọi lại `repository.detail(slug)`, chọn:

```kotlin
val embedUrl = detail.servers
    .getOrNull(serverIndex)
    ?.episodes
    ?.getOrNull(episodeIndex)
    ?.embedUrl
```

Nếu `embedUrl` null/blank → hiển thị lỗi "Tập phim không khả dụng".
Nếu có → `VideoPlayer(url = embedUrl, modifier = Modifier.fillMaxSize().aspectRatio(16f / 9f))`.

Code đầy đủ cho `WatchViewModel`:

```kotlin
sealed interface WatchUiState {
    data object Loading : WatchUiState
    data class Success(
        val movieName: String,
        val episodeName: String,
        val embedUrl: String,
    ) : WatchUiState
    data class Error(val message: String) : WatchUiState
}

class WatchViewModel(
    private val repository: MovieRepository,
    private val slug: String,
    private val serverIndex: Int,
    private val episodeIndex: Int,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WatchUiState>(WatchUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = WatchUiState.Loading
            try {
                val detail = repository.detail(slug)
                val episode = detail.servers
                    .getOrNull(serverIndex)
                    ?.episodes
                    ?.getOrNull(episodeIndex)

                _uiState.value = if (episode == null || episode.embedUrl.isBlank()) {
                    WatchUiState.Error("Tập phim không khả dụng")
                } else {
                    WatchUiState.Success(
                        movieName = detail.name,
                        episodeName = episode.name,
                        embedUrl = episode.embedUrl,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = WatchUiState.Error(
                    e.message ?: "Không mở được tập phim",
                )
            }
        }
    }
}
```

`WatchScreen`:

```kotlin
@Composable
fun WatchScreen(
    slug: String,
    serverIndex: Int,
    episodeIndex: Int,
    onBack: () -> Unit,
    viewModel: WatchViewModel = viewModel {
        WatchViewModel(
            repository = AppContainer.movieRepository,
            slug = slug,
            serverIndex = serverIndex,
            episodeIndex = episodeIndex,
        )
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    Column(Modifier.fillMaxSize()) {
        when (val s = state) {
            WatchUiState.Loading -> LoadingView()

            is WatchUiState.Error -> ErrorView(s.message, onRetry = viewModel::load)

            is WatchUiState.Success -> {
                VideoPlayer(
                    url = s.embedUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                )
                Text(
                    text = "${s.movieName} — ${s.episodeName}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                )
                TextButton(onClick = { uriHandler.openUri(s.embedUrl) }) {
                    Text("Mở bằng trình duyệt")
                }
            }
        }
    }
}
```

Nếu muốn video toàn màn hình/landscape, xử lý riêng ở `androidMain`/`iosMain`; bản cơ bản chỉ cần khung 16:9 như trên.

Mẹo: thêm nút "Mở bằng trình duyệt" dùng `LocalUriHandler.current.openUri(embedUrl)` nếu WebView bị chặn.

---

## 13. Điều hướng + Bottom bar

### 13.1. Khai báo route

```kotlin
// navigation/Routes.kt
object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val DETAIL = "detail/{slug}"
    const val WATCH = "watch/{slug}/{server}/{episode}"

    fun detail(slug: String) = "detail/$slug"
    fun watch(slug: String, server: Int, episode: Int) =
        "watch/$slug/$server/$episode"
}
```

### 13.2. NavHost + bottom bar

```kotlin
// navigation/AppNavHost.kt
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute == Routes.HOME || currentRoute == Routes.SEARCH

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Routes.HOME,
                        onClick = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Trang chủ") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.SEARCH,
                        onClick = {
                            navController.navigate(Routes.SEARCH) {
                                popUpTo(Routes.HOME)
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.Search, null) },
                        label = { Text("Tìm kiếm") },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onMovieClick = { movie ->
                        navController.navigate(Routes.detail(movie.slug))
                    },
                )
            }

            composable(Routes.SEARCH) {
                SearchScreen(
                    onMovieClick = { movie ->
                        navController.navigate(Routes.detail(movie.slug))
                    },
                )
            }

            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("slug") { type = NavType.StringType }),
            ) { entry ->
                val slug = entry.arguments?.getString("slug") ?: return@composable
                DetailScreen(
                    slug = slug,
                    onBack = { navController.popBackStack() },
                    onPlay = { movieSlug, server, episode ->
                        navController.navigate(Routes.watch(movieSlug, server, episode))
                    },
                )
            }

            composable(
                route = Routes.WATCH,
                arguments = listOf(
                    navArgument("slug") { type = NavType.StringType },
                    navArgument("server") { type = NavType.IntType },
                    navArgument("episode") { type = NavType.IntType },
                ),
            ) { entry ->
                val slug = entry.arguments?.getString("slug") ?: return@composable
                val server = entry.arguments?.getInt("server") ?: 0
                val episode = entry.arguments?.getInt("episode") ?: 0
                WatchScreen(
                    slug = slug,
                    serverIndex = server,
                    episodeIndex = episode,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
```

### 13.3. Gắn vào App.kt

Trong `App.kt`, thay nội dung demo bằng:

```kotlin
@Composable
fun App() {
    MaterialTheme(
        colorScheme = darkColorScheme(),
    ) {
        AppNavHost()
    }
}
```

Có thể thêm `@Preview` cho từng màn hình.

---

## 14. Tải ảnh bằng Coil

Đã thêm `coil-compose` + `coil-network-ktor3`. Dùng:

```kotlin
AsyncImage(
    model = movie.posterUrl,
    contentDescription = movie.name,
    contentScale = ContentScale.Crop,
    modifier = Modifier.fillMaxSize(),
)
```

Lưu ý:

- `model = null` hoặc URL hỏng → ảnh trống; đặt `Box` nền màu tối phía sau để UI không bị trắng.
- Coil tự cache ảnh; không cần tự viết cache.
- Nếu ảnh không tải trên iOS, kiểm tra `coil-network-ktor3` đã nằm trong `commonMain` và engine Darwin đã có trong `iosMain`.
- Không dùng `remember { URL(...) }`; truyền thẳng String.

---

## 15. Yêu thích & lịch sử xem (làm sau)

Gợi ý thiết kế:

- Lưu **slug** phim yêu thích (không lưu cả object).
- Lưu lịch sử: `slug`, `serverIndex`, `episodeIndex`, `episodeName`, `updatedAt`.
- Serialize danh sách bằng `kotlinx.serialization` thành JSON string.

Cách lưu cục bộ:

| Nền tảng | Công cụ |
|---|---|
| Android | `SharedPreferences` / `DataStore` |
| iOS | `NSUserDefaults` |
| Cả hai | thư viện `multiplatform-settings` |

Cách gọn nhất về kiến trúc: tạo interface trong `commonMain`:

```kotlin
interface LocalStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
}
```

- Android actual: `SharedPreferences` (truyền `Context` từ `Application`/`Activity` vào `AppContainer`).
- iOS actual: `NSUserDefaults.standardUserDefaults`.

Sau đó:

```kotlin
object FavoritesStore {
    private const val KEY = "favorite_slugs"

    fun load(store: LocalStore): Set<String> =
        store.getString(KEY)
            ?.let { runCatching { Json.decodeFromString<Set<String>>(it) }.getOrNull() }
            ?: emptySet()

    fun save(store: LocalStore, slugs: Set<String>) {
        store.putString(KEY, Json.encodeToString(slugs))
    }
}
```

Có thể làm milestone này sau khi Home/Search/Detail/Watch đã chạy ổn.

---

## 16. Test

### 16.1. Test repository với MockEngine

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
import kotlin.test.assertTrue

class MovieRepositoryTest {

    @Test
    fun search_mapsItems_andSendsKeyword() = runTest {
        var requestedUrl = ""

        val engine = MockEngine { request ->
            requestedUrl = request.url.toString()
            respond(
                content = """
                    {
                      "status": true,
                      "items": [
                        {
                          "_id": 1,
                          "name": "Avengers",
                          "origin_name": "Avengers",
                          "slug": "avengers",
                          "poster_url": null,
                          "thumb_url": null,
                          "year": 2019,
                          "tmdb": { "vote_average": "8.2" }
                        }
                      ],
                      "pagination": {
                        "totalItems": 1,
                        "currentPage": 1,
                        "totalPages": 1
                      }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val client = HttpClient(engine) {
            install(ContentNegotiation) { json() }
        }
        val repository = MovieRepository(MovieApi(client))

        val result = repository.search("avengers")

        assertEquals(1, result.size)
        assertEquals("Avengers", result.first().name)
        assertEquals(8.2, result.first().rating)
        assertTrue(requestedUrl.contains("keyword=avengers"))
    }
}
```

### 16.2. Chạy test

```bash
./gradlew :shared:testAndroidHostTest
./gradlew :shared:allTests
```

---

## 17. Chạy app & debug

```bash
# Build APK
./gradlew :androidApp:assembleDebug

# Bật emulator
emulator -avd Pixel_5 &

# Đợi boot
adb wait-for-device
adb shell getprop sys.boot_completed

# Cài + chạy
./gradlew :androidApp:installDebug
adb shell am start -n nolelamphim.vercel.app/.MainActivity

# Log app + network
adb logcat | grep -iE "nolelamphim|ktor|AndroidRuntime"

# Test nhanh
./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug
```

APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`

iOS:

```bash
open iosApp/iosApp.xcodeproj
```

Chọn simulator/device và Run.

---

## 18. Lộ trình làm theo milestone

### Milestone 1 — Network chạy được

- [ ] Thêm dependency + plugin serialization.
- [ ] Thêm `INTERNET` permission.
- [ ] Tạo `ApiConfig`, `HttpClientFactory` (expect/actual), `MovieApi`.
- [ ] Viết test gọi thử `latest()` bằng MockEngine.
- [ ] Chạy `./gradlew :shared:testAndroidHostTest`.

### Milestone 2 — Trang chủ

- [ ] Tạo DTO + model + repository.
- [ ] Tạo `HomeUiState`, `HomeViewModel`.
- [ ] Tạo `MovieCard`, `MovieRow`, `HeroBanner`, `LoadingView`, `ErrorView`.
- [ ] Tạo `HomeScreen`.
- [ ] Gắn `AppNavHost` + `App.kt`.
- [ ] Chạy app, kiểm tra ảnh và các hàng phim.

### Milestone 3 — Tìm kiếm & lọc

- [ ] `SearchViewModel` có debounce.
- [ ] `SearchScreen` với `OutlinedTextField`.
- [ ] `MovieGrid` + load more.
- [ ] `FilterViewModel` + chip thể loại/quốc gia/năm.
- [ ] Kiểm tra tìm tiếng Việt có dấu.

### Milestone 4 — Chi tiết & xem phim

- [ ] `DetailViewModel` + `DetailScreen`.
- [ ] Hiển thị server/tập.
- [ ] `VideoPlayer` expect/actual (WebView/WKWebView).
- [ ] `WatchViewModel` + `WatchScreen`.
- [ ] Xử lý trường hợp chưa có tập / link lỗi.

### Milestone 5 — Hoàn thiện

- [ ] Yêu thích + lịch sử xem.
- [ ] Dark theme, pull-to-refresh.
- [ ] Empty state, error state, loading skeleton.
- [ ] Test thêm cho search/filter/detail.
- [ ] Kiểm tra trên cả Android emulator và iOS simulator (nếu có macOS).

---

## 19. Lỗi thường gặp

| Lỗi | Nguyên nhân | Cách xử lý |
|---|---|---|
| `Serializer for class 'X' is not found` | Chưa bật plugin serialization hoặc thiếu `@Serializable` | Thêm plugin vào `shared`, annotate DTO |
| `Unexpected JSON token` | DTO khai báo sai kiểu (`status`, `totalItemsPerPage`) | Bỏ field không cần, dùng `ignoreUnknownKeys` |
| `NoTransformationFoundException` | Gọi `.body<T>()` nhưng thiếu `ContentNegotiation` | Cài plugin + `json()` |
| 404 khi gọi API | Base URL/path sai | Base URL có `/` cuối; path không có `/` đầu |
| Ảnh không hiện | Thiếu Coil network hoặc URL null | Kiểm tra dependency, dùng placeholder |
| `Cleartext HTTP traffic not permitted` | Gọi `http://` | API này dùng HTTPS; nếu gọi local thì thêm cấu hình dev |
| `No connected devices!` | Chưa có emulator/điện thoại | `adb devices`, bật emulator/USB debugging |
| WebView trắng/đen | Thiếu JS/DOM storage hoặc link chặn | Bật `javaScriptEnabled`, `domStorageEnabled`; thử mở trình duyệt |
| Video không phát trên iOS | WKWebView chưa load đúng URL | Kiểm tra `NSURL(string = url)`, dùng HTTPS |
| Tìm kiếm không trả kết quả | Chưa debounce hoặc query rỗng | Debounce 300–500ms, bỏ qua query blank |
| API bị gọi quá nhiều | Load more/scroll liên tục | Chặn khi `isLoading`, debounce, cache |
| `ViewModel` mất state | Dùng `remember` thay vì `viewModel()` | Dùng `androidx.lifecycle.viewmodel.compose.viewModel` |
| iOS test fail trên Linux | Simulator cần macOS | Dùng test JVM |
| Warning `iosSimulatorArm64Test disabled` | Host Linux | Đã xử lý bằng `kotlin.native.ignoreDisabledTargets=true` |

---

## 20. Ghi nhớ nhanh

```
API: https://vsmov.com/api/  (HTTPS, không token)

Trang chủ  = phim-moi-cap-nhat + phim-bo + phim-le + phim-chieu-rap
Tìm kiếm   = tim-kiem?keyword=...
Lọc        = danh-sach?category=&country=&year=&page=&limit=
Chi tiết   = phim/{slug}
Thể loại   = the-loai
Quốc gia   = quoc-gia
Xem phim   = episodes[].server_data[].link_embed → WebView

UI chỉ hiển thị state + callback.
ViewModel giữ state + gọi Repository.
Repository gọi API + map DTO → model.
DTO khớp JSON; model khớp UI.
Code dùng chung → commonMain.
Code riêng nền tảng → androidMain / iosMain.
Dependency khai báo trong gradle/libs.versions.toml.
Không sửa build/, không hardcode token/URL nội bộ.
```

---

## 21. Lưu ý pháp lý

- Nội dung phim, hình ảnh và link phát thuộc VSMOV và các đơn vị bản quyền.
- App chỉ là client gọi API công khai, **không host lại video**.
- Tôn trọng điều khoản của nguồn API, không spam request, nên cache/debounce.
- Khi triển khai thật, cần tuân thủ pháp luật về bản quyền và nội dung tại nơi phân phối.
