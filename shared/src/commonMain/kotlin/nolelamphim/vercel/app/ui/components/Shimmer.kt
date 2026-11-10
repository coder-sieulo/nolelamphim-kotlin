package nolelamphim.vercel.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import nolelamphim.vercel.app.ui.theme.CinemaColors
import nolelamphim.vercel.app.ui.theme.LocalIsTv

@Composable
fun Modifier.shimmerEffect(
    shape: Shape = RoundedCornerShape(8.dp),
    baseColor: Color = Color(0xFF181822),
    highlightColor: Color = Color(0xFF2B2B3A),
): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer-transition")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer-translate",
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            baseColor,
            highlightColor,
            baseColor,
        ),
        start = Offset.Zero,
        end = Offset(x = translateAnimation.value, y = translateAnimation.value),
    )

    return this
        .clip(shape)
        .background(shimmerBrush)
}

@Composable
fun HomeSkeletonScreen(modifier: Modifier = Modifier) {
    val isTv = LocalIsTv.current
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaColors.Background),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Top Bar Skeleton
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.size(36.dp).shimmerEffect(CircleShape))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.width(130.dp).height(16.dp).shimmerEffect(RoundedCornerShape(4.dp)))
                        Box(modifier = Modifier.width(80.dp).height(10.dp).shimmerEffect(RoundedCornerShape(4.dp)))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(38.dp).shimmerEffect(CircleShape))
                    Box(modifier = Modifier.size(38.dp).shimmerEffect(CircleShape))
                }
            }
        }

        // Hero Skeleton
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isTv) 440.dp else 390.dp)
                    .then(if (isTv) Modifier.padding(horizontal = 24.dp) else Modifier)
                    .shimmerEffect(if (isTv) RoundedCornerShape(20.dp) else RoundedCornerShape(0.dp)),
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(modifier = Modifier.width(100.dp).height(20.dp).shimmerEffect(RoundedCornerShape(6.dp)))
                    Box(modifier = Modifier.width(260.dp).height(30.dp).shimmerEffect(RoundedCornerShape(6.dp)))
                    Box(modifier = Modifier.width(180.dp).height(14.dp).shimmerEffect(RoundedCornerShape(4.dp)))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.width(120.dp).height(42.dp).shimmerEffect(RoundedCornerShape(10.dp)))
                        Box(modifier = Modifier.width(100.dp).height(42.dp).shimmerEffect(RoundedCornerShape(10.dp)))
                    }
                }
            }
        }

        // Row 1 Skeleton
        item {
            MovieRowSkeleton()
        }

        // Row 2 Skeleton
        item {
            MovieRowSkeleton()
        }
    }
}

@Composable
fun MovieRowSkeleton(modifier: Modifier = Modifier) {
    val isTv = LocalIsTv.current
    val cardWidth = if (isTv) 175.dp else 136.dp
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.width(140.dp).height(20.dp).shimmerEffect(RoundedCornerShape(4.dp)))
            Box(modifier = Modifier.width(60.dp).height(14.dp).shimmerEffect(RoundedCornerShape(4.dp)))
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(5) {
                Column(modifier = Modifier.width(cardWidth)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f)
                            .shimmerEffect(RoundedCornerShape(12.dp)),
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth(0.9f).height(14.dp).shimmerEffect(RoundedCornerShape(4.dp)))
                    Spacer(Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth(0.5f).height(11.dp).shimmerEffect(RoundedCornerShape(4.dp)))
                }
            }
        }
    }
}

@Composable
fun DetailSkeletonScreen(modifier: Modifier = Modifier) {
    val isTv = LocalIsTv.current
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaColors.Background),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Hero Header Skeleton
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isTv) 420.dp else 360.dp)
                    .shimmerEffect(RoundedCornerShape(0.dp)),
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(modifier = Modifier.width(280.dp).height(32.dp).shimmerEffect(RoundedCornerShape(6.dp)))
                    Box(modifier = Modifier.width(160.dp).height(16.dp).shimmerEffect(RoundedCornerShape(4.dp)))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.width(60.dp).height(24.dp).shimmerEffect(RoundedCornerShape(6.dp)))
                        Box(modifier = Modifier.width(60.dp).height(24.dp).shimmerEffect(RoundedCornerShape(6.dp)))
                        Box(modifier = Modifier.width(60.dp).height(24.dp).shimmerEffect(RoundedCornerShape(6.dp)))
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(48.dp).shimmerEffect(RoundedCornerShape(12.dp)))
                }
            }
        }

        // Synopsis Skeleton
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(modifier = Modifier.width(120.dp).height(18.dp).shimmerEffect(RoundedCornerShape(4.dp)))
                Box(modifier = Modifier.fillMaxWidth().height(14.dp).shimmerEffect(RoundedCornerShape(4.dp)))
                Box(modifier = Modifier.fillMaxWidth(0.9f).height(14.dp).shimmerEffect(RoundedCornerShape(4.dp)))
                Box(modifier = Modifier.fillMaxWidth(0.6f).height(14.dp).shimmerEffect(RoundedCornerShape(4.dp)))
            }
        }

        // Episode list skeleton
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(modifier = Modifier.width(140.dp).height(18.dp).shimmerEffect(RoundedCornerShape(4.dp)))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(6) {
                        Box(modifier = Modifier.size(width = 68.dp, height = 40.dp).shimmerEffect(RoundedCornerShape(8.dp)))
                    }
                }
            }
        }
    }
}

