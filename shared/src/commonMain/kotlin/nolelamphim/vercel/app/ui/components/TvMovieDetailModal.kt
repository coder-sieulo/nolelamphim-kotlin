package nolelamphim.vercel.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.ui.theme.CinemaColors
import nolelamphim.vercel.app.ui.theme.cinemaFocusable

/**
 * Cinefy TV Movie Detail Quick Overlay Modal matching cinefy-tv.html mockup:
 * - Frosted backdrop with cinema gradient
 * - Featured eyebrow badge
 * - Large headline, meta row (Year • Category • Rating)
 * - Cleaned synopsis
 * - "Xem phim ngay" (Primary orange gradient) & "Chi tiết đầy đủ" buttons
 * - Dismissable via Remote BACK / ESC key or close button
 * - TV remote navigation key hint at bottom right
 */
@Composable
fun TvMovieDetailModal(
    movie: Movie,
    onDismiss: () -> Unit,
    onPlayClick: (Movie) -> Unit,
    modifier: Modifier = Modifier,
    onDetailClick: ((Movie) -> Unit)? = null,
) {
    val playFocusRequester = remember { FocusRequester() }

    LaunchedEffect(movie.id) {
        try {
            playFocusRequester.requestFocus()
        } catch (_: Exception) {
        }
    }

    // Full-screen frosted scrim container intercepting remote BACK / ESC
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xD9050609))
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp && (event.key == Key.Back || event.key == Key.Escape)) {
                    onDismiss()
                    true
                } else {
                    false
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Modal Card
        Box(
            modifier = Modifier
                .widthIn(max = 940.dp)
                .fillMaxWidth(0.88f)
                .height(490.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFF101014))
                .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(26.dp))
                .shadow(40.dp, RoundedCornerShape(26.dp), spotColor = Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* consume click to prevent dismissing */ },
                ),
        ) {
            // Backdrop Image in right portion of card
            AsyncImage(
                model = movie.thumbUrl ?: movie.posterUrl,
                contentDescription = movie.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.CenterEnd),
            )

            // Left-to-right cinema gradient scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            0f to Color(0xF7101014),
                            0.45f to Color(0xEE101014),
                            0.78f to Color(0xAA101014),
                            1f to Color(0x44101014),
                        ),
                    ),
            )

            // Top-bottom subtle vignette
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color(0x55101014),
                            0.5f to Color.Transparent,
                            1f to Color(0x99101014),
                        ),
                    ),
            )

            // Close button in top-right corner
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    .cinemaFocusable(
                        onClick = onDismiss,
                        shape = CircleShape,
                        focusedBorderColor = CinemaColors.TvOrangeHighlight,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Đóng",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Left copy content
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.62f)
                    .padding(horizontal = 46.dp, vertical = 36.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                // Eyebrow badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(CinemaColors.TvEyebrowBg)
                        .border(1.dp, CinemaColors.TvEyebrowBorder, RoundedCornerShape(30.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = CinemaColors.TvOrange,
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        text = "CINEFY ĐỀ XUẤT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CinemaColors.TvEyebrowText,
                        letterSpacing = 1.sp,
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Title
                Text(
                    text = movie.name,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        lineHeight = 38.sp,
                    ),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(10.dp))

                // Metadata Row: Năm · Thể loại / Quốc gia · Đánh giá
                val metaItems = buildList {
                    if (movie.year != null && movie.year > 0) add("${movie.year}")
                    if (movie.originName.isNotBlank() && movie.originName != movie.name) add(movie.originName)
                    if (movie.rating > 0) add("★ ${((movie.rating * 10).toInt() / 10.0)}")
                }
                Text(
                    text = metaItems.joinToString("  ·  "),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = Color(0xFFFFBD87),
                    fontWeight = FontWeight.Medium,
                )

                Spacer(Modifier.height(14.dp))

                // Synopsis
                val cleanedSynopsis = if (movie.originName.isNotBlank() && movie.originName != movie.name) {
                    "${movie.originName} • Trải nghiệm tác phẩm điện ảnh sắc nét với độ phân giải cao và âm thanh sống động trên màn hình lớn."
                } else {
                    "Trải nghiệm phim điện ảnh đỉnh cao với chất lượng hình ảnh sắc nét và âm thanh sống động trên TV."
                }

                Text(
                    text = cleanedSynopsis,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                    ),
                    color = Color(0xFFD1CED0),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(26.dp))

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Primary Play Now
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .focusRequester(playFocusRequester)
                            .cinemaFocusable(
                                onClick = {
                                    onPlayClick(movie)
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(26.dp),
                                focusedBorderColor = Color(0xFFFFD0A9),
                            )
                            .clip(RoundedCornerShape(26.dp))
                            .background(CinemaColors.TvOrangeGradient)
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF211108),
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = "Xem phim ngay",
                                color = Color(0xFF211108),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                            )
                        }
                    }

                    // Secondary Details button
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .cinemaFocusable(
                                onClick = {
                                    onDetailClick?.invoke(movie) ?: onPlayClick(movie)
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(26.dp),
                                focusedBorderColor = Color(0xFFFFD0A9),
                            )
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(26.dp))
                            .padding(horizontal = 22.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "Chi tiết đầy đủ",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            }
        }

        // TV Remote Navigation Key hint at bottom right matching mockup
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 40.dp, bottom = 26.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                RemoteKeyBadge("←")
                RemoteKeyBadge("↑")
                RemoteKeyBadge("↓")
                RemoteKeyBadge("→")
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Di chuyển",
                    fontSize = 11.sp,
                    color = Color(0xFF888888),
                    letterSpacing = 0.3.sp,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                RemoteKeyBadge("OK")
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Chọn",
                    fontSize = 11.sp,
                    color = Color(0xFF888888),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                RemoteKeyBadge("BACK")
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Đóng",
                    fontSize = 11.sp,
                    color = Color(0xFF888888),
                )
            }
        }
    }
}

@Composable
private fun RemoteKeyBadge(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(5.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFDDDDDD),
        )
    }
}
