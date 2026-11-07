
package com.eclipse.shorts

import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

private val Black = Color(0xFF08080D)
private val Purple = Color(0xFF9B5CFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(8, 8, 13)
        window.navigationBarColor = android.graphics.Color.rgb(8, 8, 13)
        setContent { EclipseApp() }
    }
}

data class Clip(
    val creator: String,
    val caption: String,
    val url: String
)

@Composable
fun EclipseApp() {
    var tab by remember { mutableIntStateOf(0) }
    var nickname by remember { mutableStateOf("eclipse") }
    var avatar by remember { mutableStateOf<Uri?>(null) }
    var selectedVideo by remember {
        mutableStateOf<Uri?>(
            Uri.parse("https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4")
        )
    }
    var liked by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    val avatarPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { avatar = it }

    val videoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { selectedVideo = it }

    MaterialTheme(colorScheme = darkColorScheme(primary = Purple)) {
        Scaffold(
            containerColor = Black,
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF17151F)) {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Лента") }
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        icon = { Icon(Icons.Default.AddCircle, null) },
                        label = { Text("Создать") }
                    )
                    NavigationBarItem(
                        selected = tab == 2,
                        onClick = { tab = 2 },
                        icon = { Icon(Icons.Default.Person, null) },
                        label = { Text("Профиль") }
                    )
                }
            }
        ) { padding ->
            when (tab) {
                0 -> FeedScreen(
                    video = selectedVideo,
                    liked = liked,
                    saved = saved,
                    onLike = { liked = !liked },
                    onSave = { saved = !saved },
                    modifier = Modifier.padding(padding)
                )
                1 -> CreateScreen(
                    onPick = { videoPicker.launch("video/*") },
                    modifier = Modifier.padding(padding)
                )
                2 -> ProfileScreen(
                    nickname = nickname,
                    avatar = avatar,
                    onNickname = { nickname = it },
                    onAvatar = { avatarPicker.launch("image/*") },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
fun FeedScreen(
    video: Uri?,
    liked: Boolean,
    saved: Boolean,
    onLike: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxSize().background(Black)
    ) {
        Text(
            "ECLIPSE",
            color = Purple,
            fontSize = 27.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(18.dp)
        )

        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (video != null) {
                EclipsePlayer(video)
            } else {
                Text(
                    "Выбери видео",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Column(
                modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = onLike) {
                    Icon(
                        Icons.Default.Favorite,
                        null,
                        tint = if (liked) Color.Red else Color.White
                    )
                }
                Text(if (liked) "1" else "0", color = Color.White)

                IconButton(onClick = onSave) {
                    Icon(
                        Icons.Default.Bookmark,
                        null,
                        tint = if (saved) Purple else Color.White
                    )
                }
                Text("Сохранить", color = Color.White, fontSize = 10.sp)
            }

            Text(
                "ECLIPSE • Видео",
                color = Color.White,
                modifier = Modifier.align(Alignment.BottomStart).padding(18.dp)
            )
        }
    }
}

@Composable
fun EclipsePlayer(uri: Uri) {
    val context = LocalContext.current
    var error by remember(uri) { mutableStateOf<String?>(null) }

    val player = remember(uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = Player.REPEAT_MODE_ONE
            addListener(object : Player.Listener {
                override fun onPlayerError(exception: PlaybackException) {
                    error = exception.message ?: "Ошибка воспроизведения"
                }
            })
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = true
                    setShutterBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update = { it.player = player },
            modifier = Modifier.fillMaxSize()
        )

        if (error != null) {
            Text(
                "Не удалось загрузить видео:\n$error",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center).padding(24.dp)
            )
        }
    }
}

@Composable
fun CreateScreen(
    onPick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxSize().background(Black).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.VideoLibrary,
            null,
            tint = Purple,
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(20.dp))
        Text("Создать видео", color = Color.White, fontSize = 25.sp)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onPick) {
            Text("Выбрать из галереи")
        }
    }
}

@Composable
fun ProfileScreen(
    nickname: String,
    avatar: Uri?,
    onNickname: (String) -> Unit,
    onAvatar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxSize().background(Black).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(30.dp))

        Box(
            Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Purple)
                .clickable { onAvatar() },
            contentAlignment = Alignment.Center
        ) {
            if (avatar != null) {
                val bitmap = remember(avatar) {
                    try {
                        contextBitmap(avatar)
                    } catch (e: Exception) {
                        null
                    }
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Аватар",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("E", color = Color.White, fontSize = 42.sp)
                }
            } else {
                Text(
                    nickname.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("@$nickname", color = Color.White, fontSize = 25.sp)

        Spacer(Modifier.height(20.dp))
        Button(onClick = onAvatar) {
            Text("Изменить аватар")
        }

        var editing by remember { mutableStateOf(false) }
        var newName by remember(nickname) { mutableStateOf(nickname) }

        if (editing) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Новый ник") }
            )
            Button(onClick = {
                if (newName.isNotBlank()) onNickname(newName.trim())
                editing = false
            }) {
                Text("Сохранить")
            }
        } else {
            Button(onClick = { editing = true }) {
                Text("Изменить ник")
            }
        }
    }
}

@Composable
fun contextBitmap(uri: Uri): android.graphics.Bitmap? {
    val context = LocalContext.current
    return remember(uri) {
        try {
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            null
        }
    }
}

