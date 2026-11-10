# Nô Lệ Làm Phim - Web Design Document (Cho Dev Mobile)

> Tài liệu này mô tả chi tiết thiết kế, cấu trúc, API và quy tắc UI/UX của web app để dev mobile tham khảo implement tương tự.

## 1. Tổng quan

- **Tên dự án:** Nô Lệ Làm Phim (nolelamphim)
- **Mô tả:** Website xem phim trực tuyến đa nguồn, tối ưu SEO, trải nghiệm mượt mà.
- **Domain:** `https://nolelamphim.vercel.app` (production)
- **Tech Stack:** Astro 5 (SSR/Server output) + React 19 (islands) + TypeScript + Tailwind CSS 4 + Vercel Adapter
- **Database:** PostgreSQL (Supabase/Neon) via Drizzle ORM
- **Cache/Realtime:** Upstash Redis
- **Auth:** Custom JWT session (`nllp_session`) + Supabase Auth (email/password/magic link)
- **Media:** HLS.js cho streaming, custom player

## 2. Thiết kế hệ thống (Design System)

### 2.1 Color Palette (Cinema Minimal)

Định nghĩa trong `web/src/styles/tokens.css`. Chỉ dùng **1 màu accent** chính.

```css
/* Background */
--color-cm-bg: #1b1b1f        /* Nền chính */
--color-cm-bg-2: #202024      /* Nền phụ */
--color-cm-surface: #26262b   /* Card/surface */
--color-cm-elevated: #2e2e34  /* Elevated elements */

/* Text */
--color-cm-text: #f5f5f5      /* Text chính */
--color-cm-text-2: #a1a1aa    /* Text phụ */
--color-cm-text-3: #71717a    /* Text mờ */

/* Accent (DUY NHẤT) */
--color-cm-accent: #f97316     /* Orange - CTA, active, progress */
--color-cm-accent-hover: #fb923c

/* Border */
--color-cm-border: rgba(255, 255, 255, 0.1)
--color-cm-border-strong: rgba(255, 255, 255, 0.16)

/* Decor (chỉ dùng cho phim-dna) */
--color-cm-pink: #FF6B9D

/* Danger/Error */
--color-cm-danger: #ef4444
```

### 2.2 Typography

Font chính: **Inter** (fallback: Be Vietnam Pro, system-ui)

```css
--font-sans: "Inter", ui-sans-serif, system-ui, -apple-system, "Segoe UI", "Be Vietnam Pro", Roboto, "Helvetica Neue", Arial, sans-serif;

/* Type Scale */
--text-cm-h1: 2.25rem (36px) / 1.2
--text-cm-h2: 1.625rem (26px) / 1.25
--text-cm-h3: 1.125rem (18px) / 1.35
--text-cm-body: 0.9375rem (15px) / 1.55
--text-cm-meta: 0.75rem (12px) / 1.4
```

Font weight: 400, 500, 600, 700

### 2.3 Spacing, Radius, Shadow, Motion

```css
/* Radius */
--radius-cm-card: 8px
--radius-cm-btn: 6px
--radius-cm-input: 6px
--radius-cm-modal: 12px

/* Shadow */
--shadow-cm: 0 1px 2px rgba(0, 0, 0, 0.35)
--shadow-cm-up: 0 6px 20px -8px rgba(0, 0, 0, 0.55)

/* Motion */
--ease-cm: cubic-bezier(0.2, 0, 0, 1)
--duration-cm-fast: 140ms
--duration-cm: 160ms
--duration-cm-slow: 320ms
```

**Nguyên tắc:** Tối giản, không lạm dụng border/glass/gradient. Chỉ dùng accent cam cho CTA/progress/active.

## 3. Cấu trúc dự án (Web)

```
web/
├── src/
│   ├── components/     # UI components (Astro + React islands)
│   ├── db/            # Drizzle schema & connection
│   ├── layouts/       # BaseLayout.astro
│   ├── lib/           # Core logic (api, auth, types, utils)
│   ├── pages/         # File-based routing
│   ├── scripts/       # Client-side TS (vanilla)
│   └── styles/        # CSS tokens, base, components
├── public/            # Static assets
├── tools/             # CLI tools (sync, backfill)
└── drizzle/           # Migrations
```

## 4. Routing & Pages

### 4.1 Public Routes

| Route | Mô tả |
|-------|-------|
| `/` | Trang chủ - Hero, Tiếp tục xem, Dành cho bạn, Phim hot, Mới cập nhật |
| `/phim/[slug]` | Chi tiết phim - Thông tin, tabs (Tổng quan/Trailer/Diễn viên/Tập phim/Đánh giá), bình luận, phim tương tự |
| `/xem/[slug]/[episode]` | Trang xem phim - Player HLS/iframe, danh sách tập, điều hướng, phím tắt |
| `/danh-sach/[path]` | Danh sách theo đường dẫn (phim-le, phim-bo, phim-moi-cap-nhat, ...) |
| `/the-loai/[slug]` | Phim theo thể loại |
| `/quoc-gia/[slug]` | Phim theo quốc gia |
| `/nam/[year]` | Phim theo năm |
| `/tim-kiem` hoặc `/search` | Tìm kiếm realtime |
| `/be-lo-chon-phim` | Bé Lỏ chọn phim (mở hòm random theo thể loại hoặc bất kỳ) |
| `/dien-vien/[slug]` | Danh sách phim theo diễn viên |
| `/dao-dien/[slug]` | Danh sách phim theo đạo diễn |
| `/bo-suu-tap` | Danh sách bộ sưu tập biên tập |
| `/bo-suu-tap/[slug]` | Chi tiết bộ sưu tập biên tập |
| `/collection/[slug]` | Bộ sưu tập cá nhân (public) |
| `/xem-chung` | Trang xem chung (tạo/tham gia phòng) |
| `/xem-chung/[code]` | Phòng xem chung |
| `/phim-dna` | Phim theo DNA (recommendation) |
| `/dang-nhap` | Đăng nhập/Đăng ký (tab) |
| `/favorites` | Phim yêu thích |
| `/recent` | Đã xem gần đây |
| `/profile` | Hồ sơ người dùng |
| `/download.astro` | Trang download app |

### 4.2 API Routes (`/api/*`)

| Endpoint | Method | Mô tả |
|----------|--------|-------|
| `/api/auth/me` | GET | Lấy thông tin user hiện tại |
| `/api/auth/logout` | POST | Đăng xuất |
| `/api/auth/supabase-session` | POST | Tạo session từ Supabase auth |
| `/api/auth/supabase-callback` | GET | Callback Supabase |
| `/api/user/library` | GET/POST/DELETE | Favorites/ratings/history |
| `/api/user/history/sync` | POST | Đồng bộ lịch sử local lên tài khoản |
| `/api/collections/*` | CRUD | Bộ sưu tập cá nhân |
| `/api/movie/[slug]/comments` | GET/POST/PUT/DELETE | Bình luận phim (trả lời 1 cấp, like) |
| `/api/be-lo-pick` | GET | Random phim (có thể filter theo thể loại) |
| `/api/random` | GET | Redirect random phim |
| `/api/presence` | GET | Đếm người đang xem |
| `/api/img` | GET | Proxy ảnh |
| `/api/og/[slug].png` | GET | OG image |
| `/api/xem-chung/*` | CRUD | API xem chung |
| `/api/search` | GET | Tìm kiếm |

### 4.3 Sitemap

`/sitemap.xml` (index) + sub-sitemaps: phim theo page, thể loại, quốc gia, năm, bộ sưu tập, diễn viên/đạo diễn.

## 5. Core Features

### 5.1 Navigation

- **Desktop Header:** Logo, Trang chủ, Phim, Series, Thể loại (dropdown), **"Bé Lỏ chọn phim" pill**, Search, Xem chung, Auth
- **Mobile Bottom Nav:** Trang chủ, Danh sách, Đã xem, Hồ sơ
- **Search Overlay:** Realtime search, không rời trang

### 5.2 Trang chủ
- Hero banner
- Tiếp tục xem (localStorage + sync khi đăng nhập)
- Dành cho bạn
- Phim đang hot
- Mới cập nhật
- Section theo thể loại

### 5.3 Chi tiết phim (`/phim/[slug]`)
- Backdrop, poster, info (năm/chất lượng/quốc gia/thể loại/thời lượng/trạng thái)
- **Diễn viên/Đạo diễn** link tới `/dien-vien/[slug]`, `/dao-dien/[slug]`
- Tabs: Tổng quan, Trailer, Diễn viên, Tập phim, Đánh giá
- Nút: Xem ngay, Yêu thích, Chia sẻ, Xem chung, Báo cáo
- Phim tương tự
- Bình luận
- Rating (local + community)

### 5.4 Trang xem phim (`/xem/[slug]/[episode]`)
- Player hỗ trợ HLS (hls.js) + iframe fallback
- Danh sách tập (drawer)
- Điều hướng tập trước/sau
- **Phím tắt:** Space (play/pause), ←/→ (tua ±10s), ↑/↓ (âm lượng), M (mute), F (fullscreen), N/P (tập sau/trước), I (PiP), 0-9 (nhảy %), Shift+←/→ (chuyển tập)
- Tự động chuyển tập sau 10s
- Lưu tiến trình xem (localStorage + sync)
- Phim tương tự
- Chia sẻ

### 5.5 Bé Lỏ chọn phim
- UI mở hòm (case opening animation)
- Chọn "Bé Lỏ chọn bất kỳ" hoặc chọn theo thể loại
- API `/api/be-lo-pick?cat=...` random phim
- Hiển thị poster + thông tin, link đến trang phim

### 5.6 Xem chung (Watch Together)
- Tạo phòng, mời bạn bè
- Host điều khiển phát/dừng/tua/đổi tập
- Sync thời gian real-time
- Chat trong phòng
- Hỗ trợ m3u8 sync
- Chặn người vào muộn
- Admin panel

### 5.7 Auth
- Đăng nhập/Đăng ký qua trang `/dang-nhap` (tab)
- Supabase Auth (email/password)
- Session cookie `nllp_session` (JWT HS256)
- Tự động ghi user vào bảng `users` khi đăng nhập
- Đăng xuất xóa session + Supabase session
- Menu user với avatar, hồ sơ, yêu thích, đã xem, Bé Lỏ, Phim DNA, đăng xuất

### 5.8 Bộ sưu tập
- **Cá nhân:** Tạo/sửa/xóa bộ sưu tập, thêm/xóa phim, công khai/riêng tư, chia sẻ link `/collection/[slug]`
- **Biên tập:** Admin tạo bộ sưu tập tại `/bo-suu-tap/[slug]`

### 5.9 Bình luận
- Chỉ người đăng nhập mới bình luận
- Trả lời **1 cấp**
- Thích/bỏ thích
- Sắp xếp: Mới nhất / Nhiều lượt thích
- Xóa (chủ sở hữu hoặc admin)
- Chống spam 15s
- Giới hạn 500 bình luận gốc/phim

## 6. Data Models

### 6.1 Movie (Core)
```typescript
interface Movie {
  _id: number
  name: string
  slug: string
  origin_name?: string
  content?: string
  thumb_url?: string
  poster_url?: string
  trailer_url?: string
  time?: string
  episode_current?: string
  episode_total?: string
  quality?: string
  lang?: string
  year?: number
  view?: number
  type?: 'single' | 'series'
  status?: string
  category?: Category[]
  country?: Country[]
  actor?: string[]
  director?: string[]
  tmdb?: { type: string; id: string; vote_average?: number; vote_count?: number }
}
```

### 6.2 User Session
```typescript
interface SessionPayload {
  sub: string
  username: string
  global_name?: string | null
  avatar?: string | null
  iat: number
  exp: number
}
```

### 6.3 Comment
```typescript
interface MovieComment {
  id: string
  userId: string
  username: string
  avatar?: string | null
  text: string
  ts: number
  parentId?: string | null
  likeCount?: number
  likedByMe?: boolean
  replies?: MovieComment[]
}
```

## 7. API Patterns

- **Base URL:** Sử dụng internal API routes hoặc `lib/api.ts` proxy
- **Response format:** JSON
- **Auth:** Cookie `nllp_session` (httpOnly, sameSite=lax)
- **Caching:** SSR với `cache="public" sMaxAge=... staleWhileRevalidate=...` hoặc Redis
- **Images:** Proxy qua `/api/img?url=...&w=...` với allowlist

## 8. UI Components quan trọng

| Component | Mô tả |
|-----------|-------|
| `Navbar.astro` | Header với nav, search, auth, "Bé Lỏ chọn phim" pill |
| `MovieCard.astro` | Card phim (poster, tên, tập hiện tại, chất lượng) |
| `MovieGrid.astro` | Grid responsive |
| `HeroSingle.astro` | Hero banner trang chủ |
| `ContinueWatching.astro` | Tiếp tục xem (island) |
| `MovieComments.astro` | Khối bình luận (client island) |
| `EpisodeList.astro` | Danh sách tập phim |
| `RatingBox.astro` | Đánh giá sao |
| `ShareButton.astro` | Chia sẻ (native share + copy link) |
| `FavoriteButton.astro` | Yêu thích |
| `Footer.astro` | Footer |

## 9. Styling Guidelines cho Mobile

Khi implement mobile app, tuân theo các quy tắc sau:

### Layout
- **Mobile-first**: Ưu tiên thiết kế mobile
- **Bottom nav**: Trang chủ, Danh sách, Đã xem, Hồ sơ (ẩn trên desktop)
- **Safe area**: Cân nhắc notch/bottom bar
- **Grid**: 2 cột mobile, 3-4 tablet, 5-7 desktop

### Interactions
- **Card tap**: Vào trang chi tiết phim
- **Long press / menu**: Có thể thêm menu ngữ cảnh
- **Pull to refresh**: Trên danh sách
- **Infinite scroll**: Thay "Xem thêm"

### Player
- **Fullscreen**: Landscape hỗ trợ
- **PiP**: Android/iOS
- **Controls auto-hide**: 3-5s
- **Next episode overlay**: Hiện trước khi kết thúc
- **Quality selector**: Nếu có nhiều chất lượng

### Colors
- Sử dụng đúng color palette trên
- Chỉ accent cam `#f97316` cho CTA
- Background `#1b1b1f`

### Typography
- Tương thích tiếng Việt
- Line height thoải mái

## 10. Environment Variables (Web)

```env
# Database
DATABASE_URL=postgresql://...

# Auth
AUTH_SECRET=...
ADMIN_IDS=...

# Supabase
PUBLIC_SUPABASE_URL=...
PUBLIC_SUPABASE_ANON_KEY=...
SUPABASE_SERVICE_ROLE_KEY=...

# Redis
UPSTASH_REDIS_REST_URL=...
UPSTASH_REDIS_REST_TOKEN=...

# Site
SITE_URL=https://nolelamphim.vercel.app
PUBLIC_SITE_URL=https://nolelamphim.vercel.app
```

## 11. Notes quan trọng

- **Không dùng Discord login UI** nữa, chỉ dùng Supabase auth
- **URL SEO giữ nguyên** - không thay đổi slug structure
- **"Bé Lỏ chọn phim"** thay thế "Phim bất kỳ"
- **Không có trang /admin** công khai
- **Chỉ 1 cấp trả lời bình luận**
- **Accent chỉ cam**, đỏ chỉ dùng cho danger/error, hồng chỉ cho phim-dna
- **Cache-aware**: Một số trang cache công khai cho SEO

---

*Tài liệu cập nhật từ codebase web hiện tại. Chỉ áp dụng cho web app, không liên quan Flutter.*
