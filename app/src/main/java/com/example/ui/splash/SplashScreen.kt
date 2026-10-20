package com.example.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Exact Design Tokens from HTML SplashScreen
private val GarnetPrimary = Color(0xFF781C2E)
private val Garnet80 = Color(0xCC781C2E)
private val Garnet35 = Color(0x59781C2E)
private val Garnet25 = Color(0x40781C2E)
private val GoldPrimary = Color(0xFFD4AF37)
private val Gold50 = Color(0x80D4AF37)
private val Gold40 = Color(0x66D4AF37)
private val White90 = Color(0xE6FFFFFF)

// Sacerdotal Light Theme Gradient Stops (160deg)
private val BgStop0 = Color(0xFFFFF8F8)
private val BgStop22 = Color(0xFFFAF5F6)
private val BgStop45 = Color(0xFFFAF4E8)
private val BgStop65 = Color(0xFFF8EFF1)
private val BgStop82 = Color(0xFFF7EBED)
private val BgStop100 = Color(0xFFF1DFE3)

// cubic-bezier(0.16, 1, 0.3, 1)
private val EaseSacred = CubicBezierEasing(0.16f, 1.0f, 0.3f, 1.0f)

private const val LOGO_URL =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuD0u0JUwCZi59aq9R4BDIpfVVHfGSUjTcGpVm5HsBBYFPzagk6WhIfHB4hCGWQEVM9xOIqqShq6a1wQbKWgobvLOdSOp4CI3NzTrnbGLU0kI28uGQE8qVIi8_BQccJojDTR4Y6n1thyyoVYOFrPiA8nU45VQxMJZJH5iMXtjT5lUDyUpjnNpZVPxOwZ4yWWiNCWsDqvKnj7hmvxQnMHMIRU9vsaeBa1HxNMvb11bhPbu6BatPUQrRArUXygjdAMr5Pz5UM"

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier,
    splashDurationMs: Long = 2850L
) {
    var replayTrigger by remember { mutableIntStateOf(0) }

    val plusJakartaSans = remember {
        FontFamily(
            Font(R.font.plus_jakarta_sans, FontWeight.Normal),
            Font(R.font.plus_jakarta_sans, FontWeight.Medium),
            Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
            Font(R.font.plus_jakarta_sans, FontWeight.Bold),
            Font(R.font.plus_jakarta_sans, FontWeight.ExtraBold)
        )
    }
    val interFont = remember {
        FontFamily(
            Font(R.font.inter, FontWeight.Normal),
            Font(R.font.inter, FontWeight.Medium),
            Font(R.font.inter, FontWeight.SemiBold)
        )
    }

    // Auto-navigate after splash choreography completes (restarts if user clicks AVERENO)
    LaunchedEffect(replayTrigger) {
        delay(splashDurationMs)
        onSplashFinished()
    }

    // Continuous 14s Sacerdotal Background Shift (60 FPS, 0% Blur)
    val bgInfinite = rememberInfiniteTransition(label = "sacerdotal_bg")
    val bgShift by bgInfinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_shift"
    )

    key(replayTrigger) {
        SplashAnimatedContent(
            plusJakartaSans = plusJakartaSans,
            interFont = interFont,
            bgShift = bgShift,
            onReplayClick = { replayTrigger++ },
            modifier = modifier
        )
    }
}

@Composable
private fun SplashAnimatedContent(
    plusJakartaSans: FontFamily,
    interFont: FontFamily,
    bgShift: Float,
    onReplayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // One-shot staggered entrance Animatables matching CSS keyframes & delays
    val medallionScaleEntrance = remember { Animatable(0.6f) }
    val medallionAlpha = remember { Animatable(0f) }

    val titleMainProgress = remember { Animatable(0f) }
    val titleSubProgress = remember { Animatable(0f) }
    val dividerProgress = remember { Animatable(0f) }
    val captionProgress = remember { Animatable(0f) }
    val verseProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 1. Medallion entrance: 0% scale(0.6) opacity(0) -> 65% scale(1.05) opacity(1) -> 100% scale(1)
        launch {
            medallionAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 715, easing = EaseSacred)
            )
        }
        launch {
            medallionScaleEntrance.animateTo(
                targetValue = 1f,
                animationSpec = keyframes {
                    durationMillis = 1100
                    0.6f at 0 using EaseSacred
                    1.05f at 715 using EaseSacred
                    1.0f at 1100
                }
            )
        }

        // 2. anim-title-main (delay 350ms, duration 1100ms)
        launch {
            delay(350)
            titleMainProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1100, easing = EaseSacred)
            )
        }

        // 3. anim-title-sub (delay 500ms, duration 1100ms)
        launch {
            delay(500)
            titleSubProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1100, easing = EaseSacred)
            )
        }

        // 4. anim-divider (delay 650ms, duration 1000ms)
        launch {
            delay(650)
            dividerProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000, easing = EaseSacred)
            )
        }

        // 5. anim-caption (delay 800ms, duration 1000ms)
        launch {
            delay(800)
            captionProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000, easing = EaseSacred)
            )
        }

        // 6. anim-verse (delay 1050ms, duration 1200ms)
        launch {
            delay(1050)
            verseProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1200, easing = EaseSacred)
            )
        }
    }

    // Continuous 60 FPS Infinite Animations
    val infiniteTransition = rememberInfiniteTransition(label = "splash_infinite")

    // rot-ring-1: slowRotate1 54s linear infinite (0deg -> 360deg)
    val rotRing1Degrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 54000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot_ring_1"
    )

    // rot-ring-2: slowRotate2 70s linear infinite (360deg -> 0deg)
    val rotRing2Degrees by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 70000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot_ring_2"
    )

    // medallionBreathe: 4.5s ease-in-out infinite after 1.1s (scale 1.0 <-> 1.025)
    val medallionBreatheScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(1100, StartOffsetType.Delay)
        ),
        label = "medallion_breathe"
    )

    // ripple-1: rippleWave 3.4s cubic-bezier(0.16, 1, 0.3, 1) infinite 0.6s
    val ripple1Progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3400, easing = EaseSacred),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(600, StartOffsetType.Delay)
        ),
        label = "ripple_1"
    )

    // ripple-2: rippleWave 3.4s cubic-bezier(0.16, 1, 0.3, 1) infinite 1.7s
    val ripple2Progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3400, easing = EaseSacred),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(1700, StartOffsetType.Delay)
        ),
        label = "ripple_2"
    )

    // 3 Pulsating Dots (1.6s ease-in-out infinite, delays 0.9s, 1.15s, 1.4s)
    val dot1Wave by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(900, StartOffsetType.Delay)
        ),
        label = "dot_1"
    )
    val dot2Wave by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(1150, StartOffsetType.Delay)
        ),
        label = "dot_2"
    )
    val dot3Wave by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(1400, StartOffsetType.Delay)
        ),
        label = "dot_3"
    )

    val backgroundBrush = Brush.linearGradient(
        0.0f to BgStop0,
        0.22f to BgStop22,
        0.45f to BgStop45,
        0.65f to BgStop65,
        0.82f to BgStop82,
        1.0f to BgStop100,
        start = Offset(x = 180f * bgShift, y = 120f * bgShift),
        end = Offset(x = 900f + 240f * bgShift, y = 1900f - 180f * bgShift)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(brush = backgroundBrush)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("splash_screen_root"),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ==========================================
        // iOS 18 TOP STATUS BAR & REPLAY TRIGGER
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 448.dp)
                .padding(top = 12.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Time in Dark Garnet / Neutral
            Text(
                text = "09:41",
                fontFamily = plusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                letterSpacing = (-0.2).sp,
                color = GarnetPrimary
            )

            // Center-Right: Discreet Replay Trigger (↺ AVERENO)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(White90)
                    .border(1.dp, Garnet25, RoundedCornerShape(999.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onReplayClick
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .testTag("splash_replay_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Rejouer l'animation",
                    tint = GarnetPrimary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AVERENO",
                    fontFamily = plusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = GarnetPrimary
                )
            }

            // Right: Cellular, Wi-Fi, Battery
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cellular Signal SVG (viewBox 0 0 17 12)
                Canvas(modifier = Modifier.size(width = 16.dp, height = 12.dp)) {
                    val sx = size.width / 17f
                    val sy = size.height / 12f
                    val barColor = GarnetPrimary.copy(alpha = 0.9f)
                    val r = CornerRadius(0.5f * sx, 0.5f * sy)

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(0f * sx, 8f * sy),
                        size = Size(2.5f * sx, 4f * sy),
                        cornerRadius = r
                    )
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(4.5f * sx, 5.5f * sy),
                        size = Size(2.5f * sx, 6.5f * sy),
                        cornerRadius = r
                    )
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(9f * sx, 3f * sy),
                        size = Size(2.5f * sx, 9f * sy),
                        cornerRadius = r
                    )
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(13.5f * sx, 0.5f * sy),
                        size = Size(2.5f * sx, 11.5f * sy),
                        cornerRadius = r
                    )
                }

                // Wi-Fi SVG (viewBox 0 0 16 12)
                Canvas(modifier = Modifier.size(width = 16.dp, height = 14.dp)) {
                    val iconColor = GarnetPrimary.copy(alpha = 0.9f)
                    val cx = size.width / 2f
                    val bottomY = size.height * 0.86f

                    // Bottom dot
                    drawCircle(
                        color = iconColor,
                        radius = size.width * 0.095f,
                        center = Offset(cx, bottomY)
                    )
                    // Middle arc
                    drawArc(
                        color = iconColor,
                        startAngle = 225f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset(cx - size.width * 0.34f, bottomY - size.width * 0.34f),
                        size = Size(size.width * 0.68f, size.width * 0.68f),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    // Outer arc
                    drawArc(
                        color = iconColor,
                        startAngle = 225f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset(cx - size.width * 0.56f, bottomY - size.width * 0.56f),
                        size = Size(size.width * 1.12f, size.width * 1.12f),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }

                // iOS Battery Capsule
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(width = 24.dp, height = 12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.2.dp, Garnet80, RoundedCornerShape(4.dp))
                            .padding(1.5.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.82f)
                                .clip(RoundedCornerShape(2.dp))
                                .background(GarnetPrimary)
                        )
                    }
                    Spacer(modifier = Modifier.width(0.5.dp))
                    Box(
                        modifier = Modifier
                            .size(width = 1.5.dp, height = 6.dp)
                            .clip(RoundedCornerShape(topEnd = 1.dp, bottomEnd = 1.dp))
                            .background(Garnet80)
                    )
                }
            }
        }

        // ==========================================
        // CENTRAL CONTEMPLATIVE COMPOSITION
        // ==========================================
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Medallion & Concentric Rings Stack (Centered at medallion center: top-[92px] of w-48/192.dp)
            Box(
                modifier = Modifier.size(310.dp),
                contentAlignment = Alignment.Center
            ) {
                // 1. Expanding delicate concentric ripple ring 1 in Gold (#D4AF37/50)
                val ripple1Scale = 0.85f + (1.45f - 0.85f) * ripple1Progress
                val ripple1Alpha = 0.7f * (1f - ripple1Progress)
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .graphicsLayer {
                            scaleX = ripple1Scale
                            scaleY = ripple1Scale
                            alpha = ripple1Alpha
                        }
                        .border(1.dp, Gold50, CircleShape)
                )

                // 2. Expanding delicate concentric ripple ring 2 in Garnet (#781C2E/35)
                val ripple2Scale = 0.85f + (1.45f - 0.85f) * ripple2Progress
                val ripple2Alpha = 0.7f * (1f - ripple2Progress)
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .graphicsLayer {
                            scaleX = ripple2Scale
                            scaleY = ripple2Scale
                            alpha = ripple2Alpha
                        }
                        .border(1.dp, Garnet35, CircleShape)
                )

                // 3. Outer precise dashed circle (rot-ring-2: 310.dp, orbital counter-rotation)
                Canvas(
                    modifier = Modifier
                        .size(310.dp)
                        .graphicsLayer {
                            rotationZ = rotRing2Degrees
                        }
                ) {
                    val strokePx = 1.dp.toPx()
                    val dashPx = 5.dp.toPx()
                    drawCircle(
                        color = Garnet25,
                        radius = (size.minDimension - strokePx) / 2f,
                        style = Stroke(
                            width = strokePx,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashPx, dashPx), 0f)
                        )
                    )
                }

                // 4. Inner clean geometric circle (rot-ring-1: 260.dp, orbital rotation)
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .graphicsLayer {
                            rotationZ = rotRing1Degrees
                        }
                        .border(1.dp, Gold40, CircleShape)
                )

                // 5. SOLID OPAQUE LOGO MEDALLION (w-48 h-48 = 192.dp)
                val combinedMedallionScale = medallionScaleEntrance.value * medallionBreatheScale
                Box(
                    modifier = Modifier
                        .size(192.dp)
                        .graphicsLayer {
                            scaleX = combinedMedallionScale
                            scaleY = combinedMedallionScale
                            alpha = medallionAlpha.value
                        }
                        .clip(CircleShape)
                        .background(GoldPrimary)
                        .padding(2.5.dp)
                        .testTag("splash_medallion")
                ) {
                    // Inner White Ring with 2px Garnet Border
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, GarnetPrimary, CircleShape)
                            .padding(3.dp)
                    ) {
                        // 100% Pure Opaque White Medallion Interior
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Offline vector fallback + Official Church Logo URL
                            Image(
                                painter = painterResource(id = R.drawable.ic_logo_jesosy_mamonjy),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                            AsyncImage(
                                model = LOGO_URL,
                                contentDescription = "Logo Jesosy Mamonjy",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // ========================================
            // 3. SACRED TYPOGRAPHY CHOREOGRAPHY
            // ========================================
            // Note: Since the 310.dp ring box extends 59.dp below the 192.dp medallion,
            // we offset/space so the typography sits at mt-9 (36.dp) below the 192.dp medallion.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = with(density) { (-23).dp.toPx() }
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Noble Primary Title (Deep Garnet #781C2E)
                val titleMainTranslatePx = with(density) { (22f * (1f - titleMainProgress.value)).dp.toPx() }
                val titleMainTrackingEm = 0.18f + (0.25f - 0.18f) * titleMainProgress.value
                Text(
                    text = "JESOSY MAMONJY",
                    fontFamily = plusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    lineHeight = 28.sp,
                    letterSpacing = (20f * titleMainTrackingEm).sp,
                    color = GarnetPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        alpha = titleMainProgress.value
                        translationY = titleMainTranslatePx
                    }
                )

                // Subtitle "M. A. DAOUD"
                val titleSubTranslatePx = with(density) { (18f * (1f - titleSubProgress.value)).dp.toPx() }
                val titleSubTrackingEm = 0.22f + (0.32f - 0.22f) * titleSubProgress.value
                Text(
                    text = "M. A. DAOUD",
                    fontFamily = plusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    letterSpacing = (15f * titleSubTrackingEm).sp,
                    color = GarnetPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .graphicsLayer {
                            alpha = titleSubProgress.value
                            translationY = titleSubTranslatePx
                        }
                )

                // Refined Gold & Garnet Geometric Divider (my-3.5 = 14.dp)
                val dividerTranslatePx = with(density) { (10f * (1f - dividerProgress.value)).dp.toPx() }
                Row(
                    modifier = Modifier
                        .padding(vertical = 14.dp)
                        .graphicsLayer {
                            alpha = dividerProgress.value
                            translationY = dividerTranslatePx
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(1.5.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, GoldPrimary, GarnetPrimary)
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(GarnetPrimary)
                    )
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(1.5.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(GarnetPrimary, GoldPrimary, Color.Transparent)
                                )
                            )
                    )
                }

                // Refined Subtitle in Garnet 80%
                val captionTranslatePx = with(density) { (10f * (1f - captionProgress.value)).dp.toPx() }
                Text(
                    text = "Fihirana Jesosy Mamonjy M. A DAOUD",
                    fontFamily = interFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.96.sp,
                    color = Garnet80,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        alpha = captionProgress.value
                        translationY = captionTranslatePx
                    }
                )
            }
        }

        // ==========================================
        // 4. FOOTER & STATUS INDICATORS
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            // 3 Pulsating Dots in Garnet #781C2E
            Row(
                modifier = Modifier.padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(dot1Wave, dot2Wave, dot3Wave).forEach { wave ->
                    val dotScale = 1f + 0.25f * wave
                    val dotAlpha = 0.35f + 0.65f * wave
                    val dotTranslateY = with(density) { (-4f * wave).dp.toPx() }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .graphicsLayer {
                                scaleX = dotScale
                                scaleY = dotScale
                                alpha = dotAlpha
                                translationY = dotTranslateY
                            }
                            .clip(CircleShape)
                            .background(GarnetPrimary)
                    )
                }
            }

            // Solemn Sacred Dedication
            val verseTranslatePx = with(density) { (10f * (1f - verseProgress.value)).dp.toPx() }
            Text(
                text = "« Ho voninahitr'Andriamanitra irery ihany »",
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                letterSpacing = 0.4.sp,
                color = GarnetPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .graphicsLayer {
                        alpha = verseProgress.value
                        translationY = verseTranslatePx
                    }
            )

            // iOS 18 Home Indicator Bar
            Box(
                modifier = Modifier
                    .width(128.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Garnet35)
            )
        }
    }
}
