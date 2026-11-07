package com.eclipse.shorts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK
        setContent { EclipseApp() }
    }
}

private data class Clip(
    val creator: String,
    val caption: String,
    val audio: String,
    val url: String
)

private val demoClips = listOf(
    Clip("@eclipse_demo", "Welcome to ECLIPSE 🌑 First test clip.", "Original audio", "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"),
    Clip("@motion.lab", "Motion, light and speed. 🏎️", "Demo soundtrack", "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"),
    Clip("@night.drive", "Late night atmosphere.", "Original audio", "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EclipseApp() {
    var tab by remember { mutableStateOf(0) }
    var liked by remember { mutableStateOf(setOf<Int>()) }
    var saved by remember { mutableStateOf(setOf<Int>()) }
    var commentsFor by remember { mutableStateOf<Int?>(null) }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Color.Black,
        surface = Color(0xFF111111),
        primary = Color(0xFFB7A2FF),
        onBackground = Color.White,
        onSurface = Color.White
    )) {
        Scaffold(
            containerColor = Color.Black,
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF090909), contentColor = Color.White) {
                    NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Лента") })
                    NavigationBarItem(selected = tab == 1, onClick = { tab = 1 }, icon = { Icon(Icons.Default.Add, null) }, label = { Text("Создать") })
                    NavigationBarItem(selected = tab == 2, onClick = { tab = 2 }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Профиль") })
                }
            }
        ) { padding ->
            when (tab) {
                0 -> FeedScreen(
                    modifier = Modifier.padding(padding),
                    liked = liked,
                    saved = saved,
                    onLike = { i -> liked = if (i in liked) liked - i else liked + i },
                    onSave = { i -> saved = if (i in saved) saved - i else saved + i },
                    onComments = { commentsFor = it }
                )
                1 -> CreateScreen(Modifier.padding(padding))
                else -> ProfileScreen(Modifier.padding(padding), liked.size, saved.size)
            }
        }

        commentsFor?.let { index ->
            ModalBottomSheet(
                onDismissRequest = { commentsFor = null },
                containerColor = Color(0xFF151515),
                contentColor = Color.White
            ) {
                Column(Modifier.fillMaxWidth().padding(24.dp)) {
                    Text("Комментарии", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Text("Пока здесь пусто. Это демо-версия ECLIPSE.", color = Color.LightGray)
                    Spacer(Modifier.height(28.dp))
                }
            }
        }
    }
}

@Composable
private fun FeedScreen(
    modifier: Modifier,
    liked: Set<Int>,
    saved: Set<Int>,
    onLike: (Int) -> Unit,
    onSave: (Int) -> Unit,
    onComments: (Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { demoClips.size })
    Box(modifier.fillMaxSize().background(Color.Black)) {
        VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            ClipPage(
                clip = demoClips[page],
                page = page,
                isActive = pagerState.currentPage == page,
                isLiked = page in liked,
                isSaved = page in saved,
                onLike = { onLike(page) },
                onSave = { onSave(page) },
                onComments = { onComments(page) }
            )
        }
        Text(
            "ECLIPSE",
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 18.dp),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp
        )
    }
}

@Composable
private fun ClipPage(
    clip: Clip,
    page: Int,
    isActive: Boolean,
    isLiked: Boolean,
    isSaved: Boolean,
    onLike: () -> Unit,
    onSave: () -> Unit,
    onComments: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val player = remember(clip.url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(clip.url))
            repeatMode = Player.REPEAT_MODE_ONE
            volume = if (isActive) 1f else 0f
            prepare()
        }
    }
    LaunchedEffect(isActive) {
        player.volume = if (isActive) 1f else 0f
        if (isActive) player.play() else player.pause()
    }
    DisposableEffect(player) { onDispose { player.release() } }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    this.player = player
                }
            },
            update = { it.player = player },
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
            0f to Color(0x55000000), 0.45f to Color.Transparent, 1f to Color(0xCC000000)
        )))

        Column(
            Modifier.align(Alignment.BottomStart).fillMaxWidth(0.78f).padding(start = 16.dp, end = 8.dp, bottom = 24.dp)
        ) {
            Text(clip.creator, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Text(clip.caption, color = Color.White, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(17.dp))
                Text(clip.audio, color = Color.White, fontSize = 12.sp)
            }
        }

        Column(
            Modifier.align(Alignment.BottomEnd).padding(end = 14.dp, bottom = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            ActionIcon(if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, if (isLiked) Color.Red else Color.White, if (isLiked) "Liked" else "Like", onLike)
            ActionIcon(Icons.Default.ChatBubbleOutline, Color.White, "Comments", onComments)
            ActionIcon(Icons.Default.BookmarkBorder, if (isSaved) Color(0xFFB7A2FF) else Color.White, if (isSaved) "Saved" else "Save", onSave)
            ActionIcon(Icons.Default.Send, Color.White, "Share") { }
        }
    }
}

@Composable
private fun ActionIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(31.dp))
        Spacer(Modifier.height(3.dp))
        Text(label, color = Color.White, fontSize = 10.sp)
    }
}

@Composable
private fun CreateScreen(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().background(Color.Black).padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Add, null, tint = Color(0xFFB7A2FF), modifier = Modifier.size(54.dp))
        Spacer(Modifier.height(12.dp))
        Text("Создание видео", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Загрузка роликов появится в следующем обновлении.", color = Color.LightGray)
    }
}

@Composable
private fun ProfileScreen(modifier: Modifier = Modifier, likes: Int, saved: Int) {
    Column(modifier.fillMaxSize().background(Color.Black).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(42.dp))
        Box(Modifier.size(94.dp).background(Color(0xFF24202F), CircleShape), contentAlignment = Alignment.Center) {
            Text("E", color = Color(0xFFB7A2FF), fontSize = 42.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(16.dp))
        Text("@eclipse", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Мой профиль", color = Color.LightGray)
        Spacer(Modifier.height(28.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ProfileStat("Лайки", likes)
            ProfileStat("Избранное", saved)
            ProfileStat("Видео", 0)
        }
        Spacer(Modifier.height(32.dp))
        Text("ECLIPSE • версия 0.1.0", color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
private fun ProfileStat(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.LightGray, fontSize = 12.sp)
    }
}
