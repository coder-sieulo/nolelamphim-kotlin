# Nolelamphim

Ứng dụng **xem phim đa nền tảng** (Android + iOS) viết bằng **Kotlin Multiplatform** và **Compose Multiplatform**, sử dụng nguồn dữ liệu phim công khai từ **VSMOV**.

> Trạng thái hiện tại: khung dự án KMP đã build/chạy được trên Android và iOS. Các màn hình phim được xây dần theo [`TUTORIAL.md`](./TUTORIAL.md).

---

## Tính năng mục tiêu

- **Trang chủ**: phim mới cập nhật, hero banner, các hàng Phim bộ / Phim lẻ / Phim chiếu rạp / Subteam.
- **Tìm kiếm**: tìm theo tên phim, có debounce và phân trang.
- **Lọc phim**: theo thể loại, quốc gia, năm phát hành; kết hợp nhiều bộ lọc.
- **Chi tiết phim**: poster, mô tả, diễn viên, đạo diễn, thể loại, quốc gia, trạng thái, số tập.
- **Xem phim**: chọn server/tập và phát qua `link_embed`.
- **Yêu thích & lịch sử xem**: lưu cục bộ trên thiết bị.
- **Dark theme** tối ưu cho trải nghiệm xem phim.

---

## Nguồn dữ liệu (API đã có sẵn)

- **Base URL**: `https://vsmov.com/api/`
- **Định dạng**: JSON, UTF-8
- **Method**: `GET`
- **Auth**: không cần token cho các endpoint phim
- **Tài liệu gốc**: <https://vsmov.com/api-document>

### Endpoint chính

| Mục đích | Endpoint | Tham số |
|---|---|---|
| Phim mới cập nhật | `/api/danh-sach/phim-moi-cap-nhat` | `page`, `limit` |
| Danh sách / lọc | `/api/danh-sach` | `category`, `country`, `year`, `page`, `limit`, `sort` |
| Theo nhóm | `/api/danh-sach/{slug}` | `page`, `limit` |
| Tìm kiếm | `/api/tim-kiem` | `keyword`, `page`, `limit` |
| Thể loại | `/api/the-loai` | — |
| Quốc gia | `/api/quoc-gia` | — |
| Phim theo thể loại | `/api/the-loai/{slug}` | `page`, `limit` |
| Phim theo quốc gia | `/api/quoc-gia/{slug}` | `page`, `limit` |
| Chi tiết phim | `/api/phim/{slug}` | — |

Các `slug` nhóm đã kiểm chứng: `phim-moi`, `phim-bo`, `phim-le`, `phim-chieu-rap`, `phim-moi-cap-nhat`, `subteam`, `thuyet-minh`, `long-tieng`, `dang-chieu`, `4k`.

### Ví dụ nhanh

```bash
curl -s "https://vsmov.com/api/danh-sach/phim-moi-cap-nhat?page=1&limit=5" | head -c 500
curl -s "https://vsmov.com/api/tim-kiem?keyword=avengers&limit=5" | head -c 500
curl -s "https://vsmov.com/api/phim/hoang-hau-tai-hon" | head -c 500
```

### Cấu trúc dữ liệu rút gọn

**Danh sách phim** (`/api/danh-sach/...`):

```json
{
  "status": true,
  "items": [
    {
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
    "currentPage": 1,
    "totalPages": 872
  }
}
```

**Chi tiết phim** (`/api/phim/{slug}`):

```json
{
  "status": true,
  "movie": {
    "_id": 54207,
    "name": "Hoàng Hậu Tái Hôn",
    "slug": "hoang-hau-tai-hon",
    "content": "...",
    "type": "series",
    "status": "ongoing",
    "quality": "HD",
    "lang": "Vietsub",
    "year": 2026,
    "episode_current": "Tập 2",
    "episode_total": "10",
    "category": [{ "id": 15, "name": "Chính Kịch", "slug": "chinh-kich" }],
    "country": [{ "id": 10, "name": "Hàn Quốc", "slug": "han-quoc" }],
    "actor": ["Shin Min-a", "Ju Ji-hoon"],
    "director": ["Jo Soo-won"]
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

**Thể loại / quốc gia** (`/api/the-loai`, `/api/quoc-gia`):

```json
{
  "status": "success",
  "data": {
    "items": [
      { "_id": 15, "name": "Chính Kịch", "slug": "chinh-kich" }
    ]
  }
}
```

> Lưu ý: `status` lúc là `true`, lúc là `"success"`; `totalItemsPerPage` lúc là số, lúc là chuỗi. Khi viết DTO chỉ khai báo field thực sự dùng để tránh lỗi parse.

---

## Công nghệ

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Kotlin 2.4.21 |
| UI | Compose Multiplatform 1.12.2, Material 3 |
| Kiến trúc | MVVM + Repository |
| HTTP | Ktor Client 3.6.1 (OkHttp / Darwin) |
| JSON | kotlinx.serialization |
| Async | kotlinx.coroutines, StateFlow |
| Điều hướng | Navigation Compose (JetBrains) |
| Ảnh | Coil 3 (KMP) |
| Video | WebView (Android) / WKWebView (iOS) qua `expect/actual` |
| Build | Gradle 9.5.1, AGP 9.1.1 |

---

## Cấu trúc dự án

```
Nolelamphim-kotlin/
├── androidApp/                  # Vỏ Android: MainActivity, Manifest, resource
├── shared/                      # Toàn bộ UI + logic dùng chung
│   └── src/
│       ├── commonMain/          # UI, ViewModel, Repository, API, model
│       ├── androidMain/         # actual: OkHttp, WebView, storage...
│       ├── iosMain/             # actual: Darwin, WKWebView...
│       └── commonTest/          # test chung
├── iosApp/                      # Xcode project, Info.plist
├── gradle/libs.versions.toml    # Quản lý version thư viện
├── TUTORIAL.md                  # Hướng dẫn tự code từng bước
└── README.md
```

Chi tiết cấu trúc thư mục và luồng dữ liệu xem trong [`TUTORIAL.md`](./TUTORIAL.md).

---

## Chạy dự án

### Yêu cầu

- JDK 17+
- Android Studio (bản hỗ trợ Kotlin Multiplatform) + Android SDK
- Với iOS: macOS + Xcode

### Android

```bash
# Build APK debug
./gradlew :androidApp:assembleDebug

# Cài lên emulator/điện thoại đang kết nối
./gradlew :androidApp:installDebug

# Kiểm tra thiết bị
adb devices

# Bật emulator có sẵn (ví dụ)
emulator -avd Pixel_5 &
```

APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`

### iOS

```bash
open iosApp/iosApp.xcodeproj
```

Chọn simulator/device trong Xcode và Run. iOS simulator chỉ chạy trên macOS.

### Test

```bash
./gradlew :shared:testAndroidHostTest
./gradlew :shared:allTests
```

---

## Lộ trình

- [ ] Dựng tầng API: Ktor client, DTO, Repository.
- [ ] Màn hình Trang chủ (hero + các hàng phim).
- [ ] Màn hình Tìm kiếm + Lọc.
- [ ] Màn hình Chi tiết + danh sách tập/server.
- [ ] Màn hình Xem phim (WebView/WKWebView).
- [ ] Yêu thích & lịch sử xem (lưu cục bộ).
- [ ] Dark theme, pull-to-refresh, xử lý lỗi/empty state.
- [ ] Test repository/ViewModel, tối ưu hiệu năng.

Hướng dẫn từng bước: [`TUTORIAL.md`](./TUTORIAL.md).

---

## Lưu ý

- Dữ liệu và nội dung phim thuộc về VSMOV và các đơn vị bản quyền tương ứng. Dự án chỉ là client tiêu thụ API công khai, **không host lại video**.
- Tôn trọng điều khoản sử dụng của nguồn API, không spam request; nên cache và debounce hợp lý.
- Không commit `local.properties`, keystore, token hay thông tin máy cá nhân.
- Đây là dự án học tập/demo; hãy tuân thủ pháp luật về bản quyền khi triển khai thực tế.
