# Nolelamphim

Ứng dụng **xem phim đa nền tảng** (Android + iOS) viết bằng **Kotlin Multiplatform** và **Compose Multiplatform**, sử dụng nguồn dữ liệu phim công khai từ **VSMOV**.

> Trạng thái hiện tại: bộ khung KMP đã có đủ tầng API, Repository, các màn Trang chủ / Tìm kiếm / Lọc / Chi tiết / Xem phim và điều hướng. Android build + test chạy được; iOS cần build trên macOS.
>
> **Mới bắt đầu với Kotlin?** Đọc [`BEGINNER.md`](./BEGINNER.md) để được hướng dẫn từng bước.

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

### Chạy nhanh Android (TL;DR)

```bash
# 1. Xem danh sách emulator có sẵn
emulator -list-avds

# 2. Bật emulator (ví dụ Pixel_5)
emulator -avd Pixel_5 &

# 3. Chờ emulator boot xong
adb wait-for-device
adb shell 'while [ "$(getprop sys.boot_completed)" != "1" ]; do sleep 1; done'

# 4. Build + cài + mở app
./gradlew :androidApp:installDebug
adb shell am start -n nolelamphim.vercel.app/.MainActivity
```

### Build Android

```bash
# Build APK debug
./gradlew :androidApp:assembleDebug

# Build APK release (cần cấu hình ký nếu muốn phát hành)
./gradlew :androidApp:assembleRelease

# Chỉ kiểm tra compile phần shared
./gradlew :shared:compileAndroidMain

# Dọn build cũ
./gradlew clean

# Dừng Gradle daemon khi bị treo
./gradlew --stop

# Xem toàn bộ task
./gradlew tasks
```

APK debug: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`

### Cài & chạy trên thiết bị/emulator

```bash
# Kiểm tra thiết bị đang kết nối
adb devices

# Cách 1: build + cài qua Gradle
./gradlew :androidApp:installDebug

# Cách 2: cài APK đã build
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk

# Mở app
adb shell am start -n nolelamphim.vercel.app/.MainActivity

# Tắt app
adb shell am force-stop nolelamphim.vercel.app

# Xoá dữ liệu app (reset trạng thái)
adb shell pm clear nolelamphim.vercel.app

# Gỡ app
adb uninstall nolelamphim.vercel.app
```

### Emulator

```bash
# Liệt kê AVD
emulator -list-avds

# Bật bình thường
emulator -avd Pixel_5

# Bật không snapshot, không âm thanh (nhẹ hơn)
emulator -avd Pixel_5 -no-snapshot -no-audio

# Bật dạng không cửa sổ (headless)
emulator -avd Pixel_5 -no-window -no-audio

# Tắt emulator
adb emu kill
```

> Thêm `&` vào cuối lệnh nếu muốn terminal tiếp tục dùng được ngay.

### Test & kiểm tra

```bash
# Test repository trên JVM/Android host
./gradlew :shared:testAndroidHostTest

# Chỉ chạy một test
./gradlew :shared:testAndroidHostTest --tests "*MovieRepositoryTest*"

# Chạy toàn bộ test của shared (trên macOS sẽ gồm cả iOS)
./gradlew :shared:allTests

# Lint Android
./gradlew :androidApp:lintDebug

# Build + test một lượt trước khi commit
./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug
```

### Log & debug

```bash
# Xem log toàn bộ thiết bị
adb logcat

# Chỉ xem log crash
adb logcat -s AndroidRuntime

# Xem log của riêng app
adb logcat --pid=$(adb shell pidof -s nolelamphim.vercel.app)

# Xoá log cũ
adb logcat -c

# Chụp màn hình
adb exec-out screencap -p > screen.png
```

### iOS (chỉ trên macOS)

```bash
# Mở project trong Xcode
open iosApp/iosApp.xcodeproj

# Build framework cho iOS Simulator (Apple Silicon)
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

Chọn simulator/device trong Xcode và bấm Run. iOS simulator chỉ chạy trên macOS.

---

## Lộ trình

- [x] Dựng tầng API: Ktor client, DTO, Repository.
- [x] Màn hình Trang chủ (hero + các hàng phim).
- [x] Màn hình Tìm kiếm + Lọc.
- [x] Màn hình Chi tiết + danh sách tập/server.
- [x] Màn hình Xem phim (WebView/WKWebView).
- [ ] Yêu thích & lịch sử xem (lưu cục bộ).
- [x] Dark theme cơ bản, xử lý lỗi/empty state.
- [x] Test repository với MockEngine.
- [ ] Pull-to-refresh, cache ảnh/API và tối ưu hiệu năng.

Hướng dẫn từng bước: [`TUTORIAL.md`](./TUTORIAL.md).

---

## Lưu ý

- Dữ liệu và nội dung phim thuộc về VSMOV và các đơn vị bản quyền tương ứng. Dự án chỉ là client tiêu thụ API công khai, **không host lại video**.
- Tôn trọng điều khoản sử dụng của nguồn API, không spam request; nên cache và debounce hợp lý.
- Không commit `local.properties`, keystore, token hay thông tin máy cá nhân.
- Đây là dự án học tập/demo; hãy tuân thủ pháp luật về bản quyền khi triển khai thực tế.
