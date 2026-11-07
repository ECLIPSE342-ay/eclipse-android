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
