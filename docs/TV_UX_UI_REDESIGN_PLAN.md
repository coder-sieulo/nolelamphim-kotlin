# Kế hoạch làm lại UX/UI Nolelamphim TV

Ngày lập: 20/11/2026. Trạng thái: đang triển khai sau khi người dùng đồng ý; hướng được chọn là **Ưu tiên thư viện**, với rail điều hướng cố định bên trái.

### Cập nhật triển khai

- Đã tách TV shell và các layout TV cho Home, Detail, Search, Khám phá, Catalog, Library, Settings và Player; giữ giao diện mobile.
- Home bỏ hero lớn, đưa Continue lên đầu; rail chỉ có icon, giữ nhãn tiếng Việt cho trình đọc màn hình và selected/focus riêng. Motion TV mặc định giảm; bỏ glow/bounce/shimmer TV.
- Đã bổ sung focus restoration, filter draft/apply, panel tập/nguồn, trạng thái lỗi, một owner input/HUD player và event Media3 thật.
- Progress theo tập lưu bền có migration từ lịch sử cũ; xem tiếp resolve danh tính nguồn/tập, chống callback/load cũ khi đổi tập.
- Đã thêm regression tests cho tiến trình, đổi tập/nguồn, query/phân trang và taxonomy lỗi. Kết quả build/test cuối cùng ghi trong `TV_IMPLEMENTATION_STATUS.md` sau tích hợp.
- **Chưa nghiệm thu TV thật, hiệu năng, accessibility và usability.** ADB chưa được cấp quyền trong phiên này. Các chỉ tiêu số ở mục 13 vẫn là mục tiêu, không phải kết quả đo.
- Cờ chuyển shell cũ/mới và beta rollout chưa triển khai; không coi bản build hiện tại là bản đã vượt mọi release gate.

## 1. Quyết định thiết kế

Hướng chính đã chọn: **Ưu tiên thư viện — giao diện tối, nội dung nổi bật, điều hướng bằng remote rõ ràng, chuyển động tối thiểu**. Giữ màu cam nhận diện Nolelamphim, dùng ảnh phim và khoảng trống. Mỗi màn có một tác vụ chính rõ ràng.

Thứ tự ưu tiên: **điều khiển được → dễ hiểu → dễ đọc → đẹp → chuyển động**. Đổi màu hoặc làm thêm hiệu ứng không giải quyết được các vấn đề hiện tại.

Kết quả cần đạt:

- Người dùng luôn biết đang focus ở đâu và bấm OK sẽ làm gì.
- Vào xem tiếp nhanh; quay lại vẫn ở đúng phim, hàng và vị trí cuộn.
- Đọc tên phim, tập phim, nút và thông báo từ khoảng cách 2–3 m.
- Không có chuyển động trang trí khi để yên màn hình.
- Player có một hệ điều khiển thống nhất, phản ánh đúng trạng thái phát.
- Tất cả nhãn, chất lượng, tiến trình và thông tin phim dựa trên dữ liệu thật.

## 2. Căn cứ và giới hạn của audit

Đã đọc cấu trúc KMP, navigation, các màn TV, theme, focus modifier, component phim, dữ liệu thư viện và Media3 player. Đã đối chiếu hướng dẫn chính thức của Android TV ở mục 16.

**Audit này dựa trên source code; chưa đo trên TV thật hoặc thực hiện usability test.** Hành vi gây xung đột input/focus là nguy cơ cần xác nhận trong giai đoạn baseline; các con số scale, timeout, hardcode và callback dưới đây đã có bằng chứng trong code. Chỉ tiêu bên dưới là mục tiêu nghiệm thu, chưa phải số đo hiện tại.

Các đường dẫn source trong tài liệu tính từ thư mục gốc repository. Số dòng là mốc tại thời điểm lập kế hoạch.

### Các vấn đề đã xác định

| Mức | Vấn đề hiện tại | Bằng chứng | Quyết định sửa |
|---|---|---|---|
| P0 | Focus TV mặc định phóng 7,5%, viền và shadow cam 18dp; card còn đổi chữ, hiện Play | `ui/theme/FocusModifiers.kt:60`, `:75`, `:86`; `ui/components/MovieCard.kt:70`, `:120`, `:210` | Viền rõ, footprint cố định; bỏ glow, đổi weight và Play bật lên |
| P0 | Chưa có cơ chế phục hồi focus thống nhất cho browsing | `ui/components/MovieGrid.kt:28`; `MovieRow.kt:115`; rail tại `navigation/AppNavHost.kt:567` | Lưu focus theo ID phim/hàng cùng scroll; khai báo hướng ở ranh giới |
| P0 | “Xem ngay” và “Chi tiết” dẫn vào cùng luồng | `HeroBanner.kt:247`; `HomeScreen.kt:326`; `AppNavHost.kt:143` | Tách lệnh mở chi tiết, phát mới và xem tiếp |
| P0 | TV có HUD Compose tự ẩn 5 giây và native PlayerView controls mặc định bật, timeout 3 giây | `WatchScreen.kt:1352`, `:1410`; `VideoPlayer.kt:28`; Android `VideoPlayer.android.kt:133` | Một owner nhận remote, focus và hiển thị controls |
| P0 | Resume có nguy cơ dùng offset của tập trong route sau khi đã đổi tập | `WatchScreen.kt:140`, `:167`, `:1343` | Resolve tiến trình bằng danh tính tập đang phát |
| P1 | Hero cao 380dp và category đứng trước Continue; quá nhiều kiểu hàng trên Home | `HeroBanner.kt:117`; `HomeScreen.kt:152`, `:174`, `:187` | Hero gọn; Continue trong màn hình đầu; giảm số biến thể card |
| P1 | Ranking nhún 14%, dùng spring bouncy và đổi đỏ | `MovieRow.kt:220` | Số tĩnh; chỉ dùng ranking nếu thực sự có dữ liệu xếp hạng |
| P1 | Bộ lọc tự ẩn khi focus sang kết quả, gây thay đổi bố cục | `FilterScreen.kt:228`, `:501` | Panel cố định, chỉ mở/đóng bằng thao tác chủ động |
| P1 | Thanh Continue hiển thị cố định 55% | `ContinueWatchingRow.kt:188`; dữ liệu thật có ở `data/local/LocalLibrary.kt:40` | Tính từ position/duration; thiếu duration thì ẩn thanh |
| P1 | Chữ điều hướng 10sp, một số metadata 9–11sp | `AppNavHost.kt:724`; `MovieCard.kt:161`, `:193`; `TvMovieDetailModal.kt:413` | Type scale TV riêng, bỏ các override quá nhỏ |
| P1 | Nội dung tạo cảm giác giả: “CINEFY ORIGINALS”, năm fallback, tên “Vien.”, synopsis chung, “VIP 1080p” | `HeroBanner.kt:196`, `:224`; `TvToplineHeader.kt:73`; `TvMovieDetailModal.kt:245`; `WatchScreen.kt:1501` | Nhãn trung thực, đồng nhất thương hiệu Nolelamphim, ẩn dữ liệu chưa có |
| P1 | Nút nguồn phát chỉ cycle server và giữ nguyên episode index | `WatchScreen.kt:1632`; `WatchViewModel.kt:51` | Cho chọn nguồn rõ ràng; đối chiếu tập bằng danh tính, không chỉ index |

Các component ở bảng trên thuộc `shared/src/commonMain/kotlin/nolelamphim/vercel/app/`, trừ Android player thuộc `shared/src/androidMain/kotlin/nolelamphim/vercel/app/ui/components/`.

**Tránh chẩn đoán sai:** TV hero hiện không có timer tự chuyển. Timer 6 giây thuộc `MobileCoverFlowHeroBanner`, không phải nhánh TV. Focus chung dùng tween 120ms; spring bouncy được xác nhận ở ranking. Quy tắc bỏ auto-preview bên dưới là chính sách cho bản mới, không khẳng định TV đang có chức năng đó.

## 3. Phạm vi và nền tảng giữ lại

Làm lại toàn bộ trải nghiệm **Android TV flavor**: app shell, Home, Detail, Search, Khám phá/catalog + filter, Thư viện, Player, loading/empty/error và cài đặt trải nghiệm TV.

Tái dùng repository/API/DTO, ViewModel phù hợp, local library, stream resolver, Media3/HLS và player session hiện tại. Sửa logic dùng chung khi cần cho tính đúng đắn, có regression test.

UI TV cần tách thành composable và theme rõ ràng. Mobile/iOS tiếp tục dùng UI hiện có; giảm việc rải `if (isTv)` trong từng chi tiết nhỏ. Không đưa thư viện Android TV vào `commonMain`.

Chưa đưa vào đợt này: hệ tài khoản mới, đồng bộ cloud, recommendation engine, dashboard, trailer tự phát, thành tích, bình luận TV, tự viết bàn phím toàn màn hình. Voice search, ảnh preview khi tua và subtitle/audio/quality selector chỉ mở rộng khi thiết bị hoặc stream thực sự hỗ trợ.

## 4. Cấu trúc điều hướng mới

Menu chính có 5 đích, tên tiếng Việt thống nhất:

1. **Trang chủ:** xem tiếp và gợi ý duyệt phim.
2. **Tìm kiếm:** tìm tên phim, lịch sử tìm kiếm.
3. **Khám phá:** danh mục, thể loại, quốc gia, năm, sắp xếp nếu API hỗ trợ.
4. **Thư viện:** Tiếp tục xem / Đã lưu / Lịch sử.
5. **Cài đặt:** giảm chuyển động, tự phát tập tiếp theo, tùy chọn hiển thị và thông tin ứng dụng.

Chi tiết phim và Player là các màn con, không thêm mục vào menu. Danh sách tập, nguồn phát, bộ lọc là panel theo ngữ cảnh. Tối đa một panel tương tác mở tại một thời điểm.

**Lựa chọn đã chốt sau phản hồi:** rail bên trái cố định, rộng 112dp và chỉ hiển thị icon cho bố cục Ưu tiên thư viện. Nhãn vẫn có trong semantics cho trình đọc màn hình; wordmark nhỏ, một dòng. Trái từ nội dung về menu, phải vào vùng nội dung đã nhớ; lên/xuống di chuyển giữa các mục menu. Rail không mở rộng/thu hẹp làm nội dung dịch ngang khi di chuyển focus.

### Luồng chính

| Tác vụ | Luồng mới | Mục tiêu thao tác |
|---|---|---|
| Xem tiếp | Mở app → card Continue gần nhất → OK → Player | Một OK khi app đã focus card phù hợp |
| Xem phim mới | Card → Detail → Xem ngay | Hai lần OK từ card đã focus |
| Chọn tập | Detail → Tập phim → tập muốn xem | Mở panel bằng một OK; số D-pad tùy vị trí tập |
| Đổi nguồn | Player → Nguồn phát → chọn nguồn | Thấy tên nguồn, trạng thái và tập tương ứng |
| Tìm phim | Menu Tìm kiếm → nhập/nhận giọng nói nếu có → kết quả → Detail | Không thêm lớp modal xem nhanh |
| Quay lại | Player → Detail hoặc màn gọi → đúng card ban đầu | Giữ query, filter, tab, scroll và focus |

Phân biệt số lần OK với số lần D-pad; không quảng cáo “2 bước” cho mọi vị trí trong thư viện.

## 5. Hợp đồng remote và focus

### Hành vi chung

| Ngữ cảnh | Hành vi |
|---|---|
| Trái/phải trong hàng phim | Đi giữa các card; không tự chạy sang hàng khác |
| Lên/xuống giữa các hàng | Sang hàng gần nhất; ưu tiên vị trí ngang tương đương, nếu đã từng vào hàng thì phục hồi card đã nhớ |
| Trái ở card đầu | Vào menu, focus mục của màn đang mở |
| Phải từ menu | Trở về focus bookmark của màn; menu chỉ đổi màn khi OK |
| OK trên card | Mở Detail; riêng card Continue phát lại đúng tập/vị trí |
| Đang tải thêm | Giữ focus trên item hiện tại; thêm dữ liệu không kéo về đầu |
| Panel mở | Focus vào lựa chọn hiện tại; chặn focus xuống nền; Back đóng và trả focus về nút mở |
| Item đã bị xóa/không còn trong filter | Chọn item hợp lệ gần nhất cùng hàng, rồi hàng gần nhất, cuối cùng CTA/menu |

Mỗi hành động chỉ có **một focus target và một activation handler**. Rà soát các trường hợp wrapper focusable bọc IconButton và modifier gắn cả click lẫn KeyUp. Nhấn/nhả OK một lần chỉ tạo một hành động.

### Focus lúc vào màn

- Home lần đầu: CTA nổi bật. Người quay lại có lịch sử: card Continue gần nhất; quay lại từ Detail: bookmark cũ có ưu tiên cao nhất.
- Catalog/Khám phá: card đầu, hoặc bookmark cũ. Không ép focus sang nút Lọc sau mỗi lần fetch.
- Search: ô tìm kiếm khi vào mới; quay lại từ Detail: đúng card kết quả.
- Detail: `Tiếp tục Tập N` hoặc `Xem ngay`; CTA Tập phim ở liền kề.
- Library: tab và card đã nhớ; mới vào dùng Tiếp tục xem nếu có.
- Player: input do một controller sở hữu; panel tập mở ở tập đang phát.
- Error: focus Thử lại hoặc hành động khôi phục phù hợp.

Bookmark gồm route + tham số/query/filter + rowId + movieSlug/ID + scroll. Không chỉ lưu index vì phân trang và refresh có thể đổi thứ tự. Khôi phục sau khi lazy item thực sự được tạo; tránh gọi requestFocus theo timer tùy ý.

### Thứ tự Back

1. Bàn phím hệ thống hoặc panel đang mở xử lý trước.
2. Player: đóng panel → ẩn controls → về màn gọi; lưu tiến trình. Khi có lỗi toàn màn và không có controls, Back quay lại trực tiếp.
3. Detail/catalog con: về màn gọi và phục hồi bookmark.
4. Màn chính: từ nội dung về menu của màn hiện tại; từ menu màn khác về Home theo back stack chuẩn; tại menu Home, Back tiếp theo về launcher.

Không vòng lặp menu mở/đóng và không hộp xác nhận thoát app. Hành động xóa dữ liệu có xác nhận/hoàn tác riêng.

## 6. Hệ thiết kế TV

Các giá trị sau là token khởi điểm cần kiểm tra trên TV, không phải quy định bắt buộc của Android.

| Nhóm | Đề xuất |
|---|---|
| Canvas | Thiết kế 960×540dp, tỉ lệ 16:9; ảnh đủ cho 1080p; không dùng pixel 4K làm dp |
| Safe area | Nội dung quan trọng cách mép trái/phải khoảng 48dp, trên/dưới khoảng 27dp; backdrop được full bleed |
| Nền | Than đen `#0B0D12`; surface `#171B23`; elevated `#232A35` |
| Chữ | Chính `#F5F6F8`; phụ `#B4BDC9`; kiểm tra contrast trên cả surface và backdrop thật |
| Accent | Cam `#FF812B`; CTA cam dùng chữ tối `#211108`; đỏ dành cho lỗi/xóa |
| Focus | Outline sáng 2–3dp với offset 2–3dp; màu cam dành CTA/đích đang chọn; focus không phụ thuộc chỉ vào màu |
| Type | Hero 32–36sp; page title 26–28sp; heading 20–22sp; tên card/nút 16–18sp; metadata 14–16sp |
| Spacing | Thang 4/8/12/16/24/32/48dp; gutter card 16–20dp; chừa chỗ cho outline/scale |
| Shape | Card radius 8–10dp, nút 8dp; tránh mỗi component một kiểu pill/glass |
| Action size | Nút/panel item khoảng 44–48dp trở lên theo chiều cao; kiểm chứng với D-pad và khả năng đọc |
| Ảnh | Poster 2:3, Continue 16:9; có placeholder đúng tỉ lệ và lỗi tải ảnh ổn định |

Dùng Roboto hoặc font sans hỗ trợ đầy đủ tiếng Việt; giới hạn 2–3 weight, không đổi weight lúc focus. Tên phim tối đa hai dòng, chiều cao title đồng nhất; thông tin dài đầy đủ ở Detail. Bỏ chạy chữ marquee tự động.

Hai kiểu card đủ cho hầu hết app:

- **Poster:** tên dưới ảnh, một dòng thông tin phụ, tối đa một badge cần thiết. Focus không hiện thêm nút nổi hay làm đổi chiều cao.
- **Continue:** ảnh ngang, tên + tập, tiến trình/thời gian thật. Không hiển thị rating, năm và nhiều badge cạnh tranh với việc xem tiếp.

Phân biệt **selected** (đang ở mục menu/tab nào), **focused** (remote đang ở đâu), **pressed**, **disabled**, **loading**, **error**. Không dùng cùng một nền cam cho mọi trạng thái.

## 7. Thiết kế từng màn

### 7.1 Trang chủ

Thứ tự mới: rail icon cố định bên trái → tiêu đề → Tiếp tục xem → Mới cập nhật → Phim bộ → Phim lẻ. Phim chiếu rạp thêm khi cần; dữ liệu rỗng thì bỏ hàng và nối focus an toàn.

- Home không có hero để ưu tiên phim trong thư viện; component hero gọn chỉ giữ cho trường hợp tái sử dụng riêng.
- Card Continue phát thẳng đúng tập/vị trí; card phim thường mở Detail với hành động phát rõ ràng.
- Người có lịch sử nhìn thấy tiêu đề và card Continue ngay màn đầu; kiểm tra trên 960×540 thật sau khi cộng safe area.
- Người chưa có lịch sử thấy hàng Mới cập nhật thay thế, không khối Continue trống.
- Bỏ lời chào cá nhân giả, WiFi/thông báo trang trí, spotlight lặp, chip danh mục dài và Top 10 thiếu dữ liệu ranking.
- Mỗi hàng có heading ngắn và `Xem tất cả` cùng vị trí. Không chen một kiểu carousel khác giữa luồng đang duyệt.
- Không tự đổi backdrop toàn trang khi di chuyển focus hoặc giữ remote.

### 7.2 Chi tiết phim

Màn đầu cần trả lời được: phim gì, thông tin gì đáng biết, bắt đầu xem ở đâu, chọn tập ở đâu.

- Backdrop tối vừa đủ, tên phim, metadata thật, tóm tắt ngắn.
- Hàng hành động: `Tiếp tục Tập N · mm:ss` hoặc `Xem ngay`, `Tập phim`, `Lưu`.
- Thứ tự phần dưới: tập phim → nội dung đầy đủ → diễn viên/thể loại → liên quan.
- Bỏ quick-detail modal hiện tại để giảm một tầng điều hướng trùng. Một trang Detail chịu trách nhiệm nội dung và lựa chọn phát.
- Phim lẻ không có panel hàng trăm tập; nút chính phát tập/nguồn hợp lệ.
- Lưu/đã lưu thay trạng thái tại chỗ; không mở modal chúc mừng.
- CTA resolve theo progress và nguồn hợp lệ; sửa nhánh TV đang hardcode server 0/tập 0.

### 7.3 Danh sách tập

- Panel rộng vừa đủ, có tên phim, nguồn hiện tại và tập đang xem.
- Series dài chia nhóm 1–20, 21–40… hoặc mùa nếu API có mùa thật. Có `Đến tập đang xem`.
- Tập có trạng thái đang xem/tiến trình; tên không lặp nguyên tên phim. Bản đầu chỉ đánh dấu từ dữ liệu đã lưu thật. Muốn có trạng thái đã xem và progress cho mọi tập phải bổ sung episode progress map: LocalLibrary hiện chỉ giữ một WatchEntry gần nhất cho mỗi phim, không có lịch sử từng tập.
- Một lần chọn tập đóng panel và phát; nếu lỗi giữ ngữ cảnh, có Thử lại/Đổi nguồn.
- Focus mở panel ở tập hiện tại, Back trả về nút `Tập phim`.

### 7.4 Tìm kiếm

- Màn riêng: ô nhập lớn, system IME, lịch sử khi chưa nhập, grid kết quả ổn định khi có query.
- Tái dùng debounce nhưng xử lý request cũ để kết quả đến chậm không ghi đè query mới.
- Không thay kích thước grid khi chuyển loading/kết quả. Hiện kết quả trước đó với trạng thái đang cập nhật khi phù hợp.
- Phím xác nhận bàn phím chuyển rõ vào kết quả; Back lần đầu đóng bàn phím.
- Empty: `Không tìm thấy “…”` + `Sửa từ khóa` + `Xóa bộ lọc` nếu đang lọc.
- Micro chỉ xuất hiện khi có integration thật, có đường nhập thường khi remote không hỗ trợ.
- Không hứa tìm diễn viên/đạo diễn nếu API chỉ tìm tên phim.

### 7.5 Khám phá / catalog / lọc

- Header cố định: tên danh mục, tóm tắt filter, nút `Bộ lọc`; phía dưới grid 5–6 cột tùy kích thước card đã kiểm tra.
- Panel filter mở bằng OK, có các nhóm Thể loại / Quốc gia / Năm; Apply và Đặt lại luôn dễ đến bằng remote.
- Giá trị đang chỉnh là draft; chỉ Apply mới cập nhật kết quả. Back hủy draft; không tự đóng theo focus.
- Apply thành công: đóng panel, focus card đầu của kết quả mới; giữ nút Lọc làm fallback khi empty.
- Trạng thái filter đang áp dụng nhìn rõ ở header; sửa filter không mất query không liên quan.
- Phân trang/infinite load không đổi focus; lỗi tải thêm có nút Thử lại trong vùng đó.
- TV refresh bằng hành động rõ ràng, không phụ thuộc pull-to-refresh.

### 7.6 Thư viện

- Ba tab: Tiếp tục xem / Đã lưu / Lịch sử; mỗi tab nhớ focus và scroll riêng.
- Continue dùng cùng card của Home, phát thẳng đúng tập/vị trí.
- Đã lưu mở Detail. Lịch sử thể hiện tập, thời gian đã xem; không dùng poster quá nhỏ và chữ 11sp.
- Xóa mục qua menu ngữ cảnh có thể tới bằng remote; không bắt buộc long-press.
- Xóa toàn bộ cần xác nhận với Hủy làm lựa chọn an toàn; xóa đơn có thể dùng Hoàn tác.
- Empty có hành động `Khám phá phim` và không để focus vào vùng trống.

### 7.7 Player

**Công việc đầu tiên là sửa hợp đồng điều khiển và dữ liệu player, sau đó mới đổi HUD.**

Một controller duy nhất cung cấp trạng thái Preparing / Playing / Paused / Buffering / Seeking / Ended / Error, position, duration, active episode/source, các track có thật và khả năng seek. UI phát lệnh play/pause/seek/select; không tự suy luận isPlaying từ việc controls đang hiện.

- Video full screen, tên phim/tập gọn ở trên khi mở controls.
- Timeline + vị trí/độ dài ở dưới; transport tập trung quanh Play/Pause.
- Nút phụ: Tập phim, Nguồn phát, Cài đặt phát, Tập tiếp theo khi có.
- Không badge “VIP 1080p”, rating hay gradient dày che video. Chất lượng/phụ đề/âm thanh chỉ hiện khi player xác nhận có hỗ trợ.
- Không để PlayerView controller và Compose HUD cùng sở hữu remote. Spike quyết định dùng native controller tùy biến hoặc Compose controls với native controls tắt; không phát hành trạng thái hai lớp.
- Controls chỉ tự ẩn khi đang Playing, người dùng idle và không có panel/lỗi/seek; mốc ban đầu 4 giây. Mọi thao tác điều khiển reset timer. Paused và panel luôn nhìn được.
- Giữ trạng thái Playing/Paused khi tua. Nguồn mới tương đương giữ thời gian khi có thể, thông báo rõ nếu không giữ được.
- Tải playback URL và khởi tạo video trước, phim liên quan tải độc lập.
- Web fallback phải có giao diện và đường thoát usable bằng remote; nếu không đạt thì báo nguồn không hỗ trợ và cho đổi nguồn, không đẩy người dùng vào trang web khó điều khiển.

Remote khi không có panel:

| Trạng thái | Phím | Hành vi |
|---|---|---|
| Controls ẩn | OK | Play/Pause và hiện controls, phù hợp khuyến nghị Android TV |
| Controls ẩn | Lên/xuống | Hiện controls, không pause |
| Controls ẩn | Trái/phải | Tua ±10 giây, hiện vị trí đích; giữ phím tua liên tục có gia tốc giới hạn |
| Controls hiện, focus timeline | Trái/phải | Chỉnh vị trí đích; commit theo một quy tắc thống nhất khi nhả/OK, không seek network lại mỗi key repeat |
| Controls hiện, focus nút | Trái/phải/OK | Di chuyển hoặc kích hoạt đúng nút; root không chặn OK của con |
| Bất kỳ, không panel | Media Play/Pause | Điều khiển trực tiếp, không phụ thuộc HUD |

Resume lưu theo movie + source/tập thực tế, ưu tiên ID/slug ổn định; nếu nguồn không có ID ổn định thì dùng mapping tập và fallback có kiểm soát. Update định kỳ, khi pause/rời màn/background; tránh ghi storage trên mỗi frame. Dữ liệu cũ không mất khi đổi schema. Không dùng offset của route ban đầu cho tất cả tập.

Tự động phát tập tiếp theo: chỉ triển khai sau khi có event kết thúc và tùy chọn lưu bền. Nút chuyển tập tiếp theo thủ công vẫn dùng được khi có tập hợp lệ. Gợi ý gần cuối cần Hủy và không tự cướp focus. Nếu metadata không đủ để biết đoạn credits, không tự đoán mốc “bỏ qua intro”.

Khi app ra background: pause và lưu tiến trình; quay lại không tự phát bất ngờ, trừ khi một hành vi playback nền đã được thiết kế riêng. Mọi event/progress callback gắn episode ID + session/generation; hủy hoặc bỏ kết quả load cũ khi đổi tập nhanh. Không để callback từ tập trước ghi vị trí vào tập hiện tại.

### 7.8 Cài đặt TV

- `Giảm chuyển động`: mặc định bật cho đợt thiết kế mới; tôn trọng thiết lập hệ thống khi khả dụng. Cả hai chế độ đều không dùng spring/glow/trailer tự phát.
- `Tự phát tập tiếp theo`: công tắc có mô tả ngắn, chỉ bật tính năng khi player đã hỗ trợ.
- Có thể thêm cỡ chữ nếu toàn bộ layout đã kiểm chứng với mức lớn; không thêm công tắc không hoạt động.
- Thông tin ứng dụng và thao tác thư viện chỉ hiện khi có tác dụng rõ ràng.

## 8. Chính sách animation mới

Đích là phản hồi rõ, ít chuyển động. Không mặc định tắt mọi phản hồi focus.

| Tình huống | Chế độ Nhẹ | Chế độ Giảm chuyển động — mặc định |
|---|---|---|
| Focus card/nút | Outline ngay, thay nền 80–100ms; card scale tối đa 1.02 trong 100ms nếu cần | Outline và nền ngay, scale 1.00 |
| Nhấn OK | Thay độ sáng tối đa 80ms | Đổi trạng thái ngay |
| Đổi màn | Fade 120–160ms | Thay ngay |
| Panel | Một fade 120–140ms, không resize nền | Xuất hiện ngay |
| Cuộn | Dịch tối thiểu để item nằm trong vùng nhìn; không recenter mỗi card | Vẫn giữ liên tục không gian, duration ngắn, không thêm easing nhún |
| Loading | Skeleton tĩnh đúng bố cục; spinner nhỏ khi thực sự đang chờ | Tương tự |
| Hero/backdrop/ranking | Tĩnh khi idle | Tĩnh khi idle |

Loại bỏ: spring overshoot, bounce ranking, neon shadow, parallax, reveal từng card theo dây chuyền, animateContentSize làm xô nội dung, shimmer chạy khắp trang, đổi font weight theo focus, tự mở/đóng filter, auto-preview.

Chỉ dùng một kiểu chuyển động chính cho mỗi thay đổi trạng thái. Hold remote không xếp hàng animation; ưu tiên trạng thái mới nhất. Response outline không chờ ảnh/network hoặc animation hoàn tất.

## 9. Loading, lỗi, dữ liệu thiếu và khả năng tiếp cận

| Tình huống | Thiết kế cần có |
|---|---|
| Tải lần đầu | Skeleton tĩnh đúng hình học; focus ở nút/menu hợp lệ, không để toàn màn không điều khiển được |
| Refresh / tải thêm | Giữ nội dung có sẵn và focus; trạng thái trong vùng đang cập nhật |
| Mạng lỗi | Thông báo ngắn + Thử lại; không mất query, filter hoặc scroll |
| Tập/nguồn không phát được | Thử lại / Đổi nguồn / Quay lại; giữ tên phim và vị trí |
| Poster hỏng | Placeholder cố định, vẫn đọc được tên và chọn phim |
| Duration không rõ | Không phần trăm giả; timeline chỉ tương tác khi seek được |
| Rating/năm/chất lượng thiếu | Ẩn field; không fallback thành một khẳng định sai |
| Danh sách trống | Giải thích theo ngữ cảnh + một hành động tiếp theo có focus |

Contrast mục tiêu tối thiểu 4.5:1 cho chữ thường và 3:1 cho chữ lớn/biên nhận biết tương tác. Kiểm tra bằng công cụ và trên ảnh nền thật. Focus luôn có hình dạng/outline, không dùng màu là tín hiệu duy nhất.

Semantics có tên phim, loại hành động, tập, trạng thái Đã lưu/Đang xem; không đọc mọi badge trang trí. Kiểm tra TalkBack, font lớn, tiếng Việt có dấu, tên phim dài. Subtitle trong Player không bị controls hoặc safe area che; tùy chọn track phụ thuộc khả năng stream.

## 10. Kế hoạch kỹ thuật

### Tách presentation TV, tái dùng logic

```text
shared/commonMain
  data/, model/, ViewModel / playback contracts / library
  ui/mobile/...                     # UI hiện tại theo lộ trình refactor
  ui/tv/...                         # component TV trung lập nền tảng khi phù hợp
  ui/tv/theme, focus, motion, screens

shared/androidMain
  ui/tv/...                         # host, TV Material, remote/native integration
  ui/components/...                 # Media3 adapter và session

androidApp
  mobile / tv flavor                # chọn app shell tại entry
```

Cây trên là hướng tổ chức, không yêu cầu di chuyển mọi file trong một PR. Bắt đầu bằng `TvAppShell`, `TvTheme`, `TvMotionPolicy`, `TvFocusStyle`, `TvPosterCard`, `TvContinueCard`, `TvContentRow`, `TvFilterPanel`, `TvEpisodePanel`, `TvPlayerControls`.

- AppNavHost chọn TV shell rõ ràng; route và callback có kiểu tác vụ riêng: openDetail / play / resume.
- Tạo focus bookmark store theo destination, lifecycle và item ID; dùng focus groups/restoration của Compose phù hợp version hiện tại.
- Spike `androidx.tv:tv-material` trong `androidMain`; kiểm tra tương thích Compose Multiplatform, dependency và theme. Nếu đạt, dùng TV Material cho control TV; không trộn ngẫu nhiên với Material3 theme. Không nâng toàn bộ toolchain chỉ để đổi UI.
- Một playback adapter gắn listener Media3, xuất StateFlow; một owner input. Giữ phần xử lý HLS và PNG-wrapped TS hiện có.
- Tách playback state khỏi trạng thái controls/panel; UI state có thể là Hidden / Controls / Episodes / Settings / Error.
- Đổi representation tập/nguồn từ index thuần sang ID/slug + fallback. Migrate dữ liệu local có kiểm thử đọc schema cũ; tránh recordWatch ghi đè progress đã lưu. Nếu mở rộng tiến trình cho mọi tập, lưu episode progress map riêng và giữ watch history ở mức phim để không nhân bản card Continue.
- Bổ sung Compose UI/device test dependencies; hiện repo chủ yếu có test logic, chưa có bộ focus regression TV.
- Bỏ MiniPlayer TV trong bản đầu nếu đang active; TV chưa có use case và focus contract hoàn chỉnh cho cửa sổ nổi.
- Đo ảnh/lazy scroll/compose recomposition trước khi tối ưu; dùng decode size đúng card, cache sẵn có, tránh blur thời gian thực và shadow lớn.

## 11. Lộ trình thực hiện

Ước lượng **24–32 ngày làm việc, khoảng 5–7 tuần** cho một lập trình viên quen Kotlin/Compose và có phản hồi thiết kế thường xuyên. Đây là ước lượng lập kế hoạch; cập nhật sau baseline và spike player/TV Material. Hai người có thể làm song song sau khi thống nhất contract; thời gian QA TV thật vẫn cần giữ.

| Chặng | Thời lượng | Đầu việc | Điều kiện hoàn thành |
|---|---|---|---|
| 0. Baseline | 1–2 ngày | Ghi hình 5 tác vụ trên TV, map focus/Back, đo startup/scroll/player; chốt thiết bị chuẩn | Có số đo và danh sách lỗi tái hiện được |
| 1. Sửa điểm khó chịu sớm | 2 ngày | Bỏ glow/ranking bounce, tắt shimmer TV, sửa progress/nhãn giả, thống nhất CTA; phân loại xung đột input | Idle tĩnh, focus rõ, dữ liệu UI trung thực |
| 2. Foundation | 3–4 ngày | TV shell/theme/motion/focus, bookmark, component và spike TV Material; build cả flavor | Một luồng mẫu Home → Detail → Back giữ đúng focus |
| 3. Player contract | 4–5 ngày | Chọn controller owner, Media3 state/events, input map, seek/resume/source identity, loading/error | Không hai HUD; pause/seek/đổi tập/Back có test |
| 4. Home + Detail | 5–6 ngày | Bố cục mới, card chuẩn, Continue, Detail thống nhất, panel tập | Ba luồng xem mới/xem tiếp/chọn tập usable chỉ bằng remote |
| 5. Search + Khám phá + Library + Settings | 5–7 ngày | Search/IME, filter draft/apply, pagination focus, tab restore, motion preference | Không layout nhảy, không mất query/tab/focus |
| 6. QA và phát hành | 4–6 ngày | TV thật + emulator, perf/accessibility, regression mobile, sửa lỗi, rollout theo cờ | Đạt tiêu chí mục 13, có phương án rollback |

Tổng phạm vi dao động theo lỗi phát hiện; các khoảng từng chặng không cộng thành cam kết cứng. Nếu player integration vượt 5 ngày, báo lại estimate và cắt P2, không cắt kiểm thử remote.

### Các gói thay đổi để review

1. `TV-01`: baseline, acceptance checklist và screenshot chuẩn.
2. `TV-02`: motion/focus style, chữ và nhãn dữ liệu — thay đổi nhìn thấy sớm.
3. `TV-03`: TV app shell, focus bookmarks, Back và một activation handler.
4. `TV-04`: playback state/input owner/resume identity; test trước khi thay HUD.
5. `TV-05`: Home/Detail và hai card chuẩn; bỏ quick modal trùng.
6. `TV-06`: Player controls + panels + recovery.
7. `TV-07`: Search/Khám phá/filter và pagination focus.
8. `TV-08`: Library/Settings và migration nếu cần.
9. `TV-09`: performance/accessibility/regression, rollout.

TV-04 có thể phát triển song song TV-05 sau TV-03; TV-06 phụ thuộc TV-04. Mỗi gói phải review được độc lập, có ảnh trước/sau và mô tả hành vi remote. Không gộp refactor data layer không liên quan.

## 12. Backlog ưu tiên

| Mức | Bắt buộc |
|---|---|
| P0 — trước khi đánh giá bản mới | Focus/Back/restoration; một OK một hành động; player một owner; resume đúng tập; CTA đúng tác vụ; motion tối thiểu |
| P1 — bản redesign hoàn chỉnh | Shell/design system; Home/Detail/Search/Khám phá/Library mới; filter ổn định; panel tập/nguồn; loading/error/empty; dữ liệu UI thật; TV thật và perf |
| P2 — sau khi P0/P1 đạt | Voice integration, preview thumbnail khi seek, tinh chỉnh autoplay, thêm option hiển thị đã được kiểm chứng |

Không dùng P2 để trì hoãn sửa các lỗi người dùng đang gặp. Tắt animation không thay cho việc sửa focus và điều khiển player.

## 13. Tiêu chí nghiệm thu và đo lường

### Tác vụ và điều hướng

- 100% hành động chính dùng được với D-pad + OK + Back; không cần chuột/touch/long-press.
- Không mất focus, focus lọt sau panel, trap không có đường thoát hoặc hai item cùng trông như focused.
- Home/Catalog/Search/Library → Detail → Back khôi phục đúng item và scroll, kể cả đã phân trang.
- Đi qua 20 card liên tục và giữ remote 3 giây: không nhún/glow chớp, không focus vào item chưa tồn tại, không OK hai lần.
- Continue được focus trên Home người quay lại, một OK vào đúng tập/vị trí; thời gian chờ phát đo riêng với số thao tác.
- Detail mở panel tập bằng một OK từ nút Tập phim; series dài có đường về tập đang xem.
- Filter không tự đổi bố cục khi focus qua lại; apply/cancel và Back nhất quán.
- Hủy/đóng bàn phím không thoát nhầm màn Search.
- Xóa phim đang focus có fallback đúng, không rơi focus ra nền.

### Thị giác và motion

- Idle 30 giây không có chuyển động trang trí hoặc đổi nội dung tự động. Video đang phát và chỉ báo buffering cần thiết được loại khỏi phép kiểm tra này.
- Metadata/hành động quan trọng đọc được ở 2–3 m trên TV kiểm thử; không chữ quan trọng 9–11sp.
- Không layout shift khi focus card, mở controls hoặc tải ảnh.
- Outline focus không bị clip ở mép hàng/grid; rõ trên poster sáng và tối.
- Kiểm tra Reduce motion cả hai trạng thái, lưu qua lần mở app sau.

### Player và dữ liệu

- Chỉ một HUD và một owner input trong native playback và fallback.
- OK/PlayPause/seek theo contract; seek giữ Playing/Paused; panel không bị root cướp OK.
- Controls không tự ẩn khi pause/panel/lỗi hoặc trong lúc người dùng thao tác.
- Đổi tập không dùng offset của tập trước; quay lại app phục hồi vị trí sai số mục tiêu ≤5 giây trong luồng HLS seek được.
- Đổi nguồn đối chiếu tập thật; thiếu tập có thông báo và hành động phục hồi.
- Progress Continue khớp position/duration đã lưu, không còn 55% cố định.
- Mạng chậm, mất mạng, URL lỗi, ảnh hỏng, duration chưa biết đều có trạng thái usable.

### Hiệu năng và kiểm thử người dùng

- Mục tiêu phản hồi focus thấy được trong ≤100ms ở p95 trên thiết bị chuẩn; đo từ input tới frame, không chỉ duration animation.
- Với thiết bị 60Hz, đặt mục tiêu jank <5% trong kịch bản cuộn chuẩn; ghi cả loại frame và profile build. Chốt ngưỡng khả thi sau baseline, không coi đây là số đo hiện tại.
- Thời gian tới frame video đầu không tăng so với baseline trên cùng nội dung/mạng; related content không chặn playback.
- Test emulator TV và ít nhất một TV/box hiệu năng thấp; 720p/1080p, kiểm tra scale/overscan ở 4K khi có thiết bị.
- Usability test 5 người, có người ít rành công nghệ: mở phim mới, xem tiếp, tìm phim, đổi tập, thoát player. Mục tiêu ≥4/5 người hoàn thành từng tác vụ không cần hướng dẫn; ghi điểm nhầm focus/OK và mức khó chịu chuyển động.

### Nhóm test cần bổ sung

1. Unit: playback state transitions, episode/source mapping, resume/migration, next episode boundaries, event/load cũ sau khi đổi tập và request cũ trong Search.
2. Compose UI/device: initial focus, D-pad transitions, panel isolation, Back/restore, one activation, pagination và item bị xóa.
3. Manual real-device: hold key, system IME, Media3 View/Compose handoff, network recovery, background/foreground và controls timeout.
4. Regression: build/lint TV, build mobile và test shared; iOS compile trên macOS nếu thay contract chung.

Không viết test chỉ để khẳng định lại màu hoặc duration token. Ưu tiên test lỗi điều hướng và phát phim có tác động thực tế.

## 14. Phát hành và kiểm soát rủi ro

- Giữ cờ chọn TV shell cũ/mới trong quá trình triển khai, tránh đẩy UI mới từng mảnh không đồng bộ.
- Data library giữ tương thích; nếu phải đổi key/schema, có test migration và rollback không xóa lịch sử.
- Build kiểm tra `tvDebug` và `mobileDebug`; xác nhận tên Gradle task thực tế trước khi chạy automation.
- Beta nội bộ với 5 tác vụ chuẩn; chỉ mở rộng khi không còn lỗi focus/player P0.
- Cờ shell không che được migration sai hoặc lỗi playback chung; rollback phải bao gồm contract/data tương thích.
- Không thêm analytics từ xa nếu chưa có lựa chọn sản phẩm; baseline có thể ghi local/log và quan sát thủ công.

Rủi ro chính: native View–Compose focus, thư viện TV Material với KMP, dữ liệu nguồn thiếu ID/track, hiệu năng ảnh trên box yếu. Giải quyết bằng spike sớm và test trên thiết bị, không bằng thêm animation che tải.

## 15. Deliverables

- Audit/baseline có bằng chứng trên thiết bị và video remote.
- Sitemap, luồng tác vụ, bảng remote/Back/focus của từng màn.
- Design tokens + component states + motion policy.
- Mockup Home, Detail, Player; sau đó Search, Khám phá, Library, các panel và trạng thái lỗi/empty/loading.
- Các gói triển khai TV-01 đến TV-09, test phù hợp và ảnh review.
- Báo cáo usability/performance, checklist nghiệm thu và rollback.

Minh họa đi kèm trong cuộc trò chuyện dùng dữ liệu mẫu để so bố cục; không phải screenshot ứng dụng đang chạy hay dữ liệu phim từ API. Người dùng đã chọn **Ưu tiên thư viện**, giảm hero và đưa nội dung lên màn đầu. Các mô tả hero/rail trong phương án gốc là tài liệu tham khảo, không phải cấu trúc Home được triển khai.

## 16. Nguồn tham chiếu

Đã kiểm tra ngày 20/11/2026. Các thời lượng animation và token cụ thể trong kế hoạch là đề xuất cho app này; không gán chúng thành yêu cầu của Google.

- [Android TV — Focus system](https://developer.android.com/design/ui/tv/guides/styles/focus-system): trạng thái focus, focus group và tín hiệu focus.
- [Android TV — Layouts](https://developer.android.com/design/ui/tv/guides/styles/layouts): canvas 960×540dp, bố cục theo trục, safe area và giảm quá tải thông tin.
- [Android TV — Typography](https://developer.android.com/design/ui/tv/guides/styles/typography): chữ lớn, dễ đọc từ xa.
- [Android TV — Navigation drawer](https://developer.android.com/design/ui/tv/guides/components/navigation-drawer): số lượng đích chính, phân biệt đích đang chọn và focus, tiết chế badge.
- [Android TV — Navigation](https://developer.android.com/training/tv/get-started/navigation): remote, Back dự đoán được, không exit gating/vòng lặp.
- [Android TV — Playback controls](https://developer.android.com/training/tv/playback/controls): Center play/pause, trái/phải seek, lên/xuống xem controls.
- [Use Jetpack Compose on Android TV](https://developer.android.com/training/tv/playback/compose): component TV và giới hạn khi trộn Material themes.
- [Focus in Compose](https://developer.android.com/develop/ui/compose/touch-input/focus): nền tảng triển khai focus; đối chiếu API với version thực tế trước khi dùng.
