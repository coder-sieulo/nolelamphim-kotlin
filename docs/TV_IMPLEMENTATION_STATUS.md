# TV redesign implementation status

Ngày kiểm tra: 20/11/2026

## Đã triển khai

- TV shell riêng với rail bên trái chỉ hiển thị icon (112dp); nhãn Trang chủ, Tìm kiếm, Khám phá, Thư viện, Cài đặt được giữ cho trình đọc màn hình. Wordmark nhỏ, một dòng.
- Home TV theo hướng Ưu tiên thư viện: Continue → Mới cập nhật → Phim bộ → Phim lẻ → Chiếu rạp.
- Focus restoration theo hàng/card, stable slug key, điều hướng D-pad và Back theo lớp.
- TV cards giữ footprint ổn định; progress Continue lấy từ position/duration thật.
- TV Detail và episode dialog; chọn nguồn/tập theo identity, không giả định cùng index.
- Search IME, pagination chống stale result; filter draft/Apply/Reset/Cancel; Library 3 tab và confirm xóa.
- Player TV một owner input/HUD Compose; Media3 play/pause/seek/buffering/error/end; pause/save khi background.
- Resume theo episode/source identity, migration progress cũ, guard load/callback cũ, replay tập đã hoàn thành.
- Motion mặc định giảm: không glow, bounce, shimmer TV hoặc auto layout shift; có tùy chọn tự phát tập tiếp theo.
- HUD player dùng icon cho tua, phát/tạm dừng, tập phim, nguồn, cài đặt và tập tiếp theo; tên hành động giữ trong accessibility.

## Validation đã chạy

Lệnh:

```text
./gradlew :shared:testAndroidHostTest :androidApp:assembleTvDebug :androidApp:assembleMobileDebug :androidApp:lintTvDebug
```

Kết quả:

- `BUILD SUCCESSFUL`
- 31 host tests passed.
- `androidApp:assembleTvDebug` passed.
- `androidApp:assembleMobileDebug` passed.
- TV lint: 0 errors, 14 existing/project warnings (dependency updates, obsolete resources, unused resources and launcher/icon assets).
- `git diff --check` passed.

## Chưa xác minh

- Remote thật, Android TV emulator/box, hold-key behavior, overscan và frame timing: ADB không được cấp quyền trong phiên này.
- iOS build trên macOS.
- Usability test, accessibility/TalkBack, performance p95/jank và network playback trên thiết bị yếu.
- Cờ rollout/rollback giữa shell cũ và shell mới chưa được thêm; bản hiện tại dispatch TV trực tiếp vào shell mới.

## APK debug

- TV: `androidApp/build/outputs/apk/tv/debug/androidApp-tv-debug.apk`
- Mobile: `androidApp/build/outputs/apk/mobile/debug/androidApp-mobile-debug.apk`
