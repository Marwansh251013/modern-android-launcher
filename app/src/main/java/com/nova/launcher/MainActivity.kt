package com.nova.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class LaunchableApp(
    val label: String,
    val packageName: String,
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                NovaHome(
                    loadApps = ::loadLaunchableApps,
                    openApp = ::openApp,
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun loadLaunchableApps(): List<LaunchableApp> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .mapNotNull { info ->
                val packageName = info.activityInfo?.packageName ?: return@mapNotNull null
                LaunchableApp(info.loadLabel(packageManager).toString(), packageName)
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    private fun openApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { startActivity(launchIntent) }
    }
}

@Composable
private fun NovaHome(
    loadApps: () -> List<LaunchableApp>,
    openApp: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var apps by remember { mutableStateOf(emptyList<LaunchableApp>()) }
    var showDrawer by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { apps = loadApps() }
    LaunchedEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) apps = loadApps()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        androidx.coroutines.suspendCancellableCoroutine<Unit> { continuation ->
            continuation.invokeOnCancellation { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
    }
    BackHandler(enabled = showDrawer) {
        showDrawer = false
        query = ""
    }

    val filteredApps = remember(apps, query) {
        apps.filter { it.label.contains(query.trim(), ignoreCase = true) }
    }
    val favorites = remember(apps) {
        val preferred = listOf("phone", "camera", "message", "chrome")
        preferred.mapNotNull { word -> apps.firstOrNull { it.label.contains(word, true) } }
            .distinctBy { it.packageName }
            .take(4)
            .ifEmpty { apps.take(4) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF101A39), Color(0xFF263B67), Color(0xFF6D6588)),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(horizontal = 24.dp, vertical = 18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()),
                        color = Color.White.copy(alpha = 0.78f),
                        fontSize = 15.sp,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = SimpleDateFormat("h:mm", Locale.getDefault()).format(Date()),
                        color = Color.White,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-2).sp,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✦", color = Color(0xFFFFD98A), fontSize = 25.sp)
                }
            }

            Spacer(Modifier.height(22.dp))
            SearchField(
                value = query,
                onValueChange = { query = it },
                onFocus = { showDrawer = true },
            )

            Spacer(Modifier.height(24.dp))
            Text("YOUR SPACE", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                items(favorites, key = { it.packageName }) { app ->
                    AppTile(app = app, onClick = { openApp(app.packageName) })
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.14f),
                shape = RoundedCornerShape(28.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    favorites.take(4).forEach { app ->
                        AppGlyph(app.label, Modifier.clickable { openApp(app.packageName) })
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { showDrawer = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("⌕", color = Color.White, fontSize = 29.sp)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Swipe into your apps • Tap the glass button",
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }

        AnimatedVisibility(
            visible = showDrawer,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            AppDrawer(
                apps = filteredApps,
                query = query,
                onQueryChange = { query = it },
                onClose = { showDrawer = false; query = "" },
                onOpenApp = openApp,
            )
        }
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit, onFocus: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.17f),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onFocus)
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("⌕", color = Color.White.copy(alpha = 0.85f), fontSize = 24.sp)
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    Box {
                        if (value.isEmpty()) Text("Search apps", color = Color.White.copy(alpha = 0.7f), fontSize = 15.sp)
                        inner()
                    }
                },
            )
        }
    }
}

@Composable
private fun AppDrawer(
    apps: List<LaunchableApp>,
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    onOpenApp: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF111A31), Color(0xFF293957))))
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(horizontal = 22.dp, vertical = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("All apps", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.SemiBold)
                Text("${apps.size} ready when you are", color = Color.White.copy(alpha = 0.65f), fontSize = 14.sp)
            }
            Text("Done", modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClose).padding(10.dp), color = Color(0xFFB7D7FF), fontSize = 15.sp)
        }
        Spacer(Modifier.height(22.dp))
        SearchField(value = query, onValueChange = onQueryChange, onFocus = {})
        Spacer(Modifier.height(18.dp))
        if (apps.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No matching apps", color = Color.White.copy(alpha = 0.75f))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                items(apps, key = { it.packageName }) { app ->
                    AppTile(app = app, onClick = { onOpenApp(app.packageName) })
                }
            }
        }
    }
}

@Composable
private fun AppTile(app: LaunchableApp, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppGlyph(app.label)
        Spacer(Modifier.height(7.dp))
        Text(
            text = app.label,
            color = Color.White,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AppGlyph(label: String, modifier: Modifier = Modifier) {
    val colors = listOf(Color(0xFF74C7F8), Color(0xFFB39DFA), Color(0xFFFFA98F), Color(0xFF8CDBB8), Color(0xFFFFD278))
    val color = colors[(label.hashCode() and Int.MAX_VALUE) % colors.size]
    Box(
        modifier = modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(color.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "•",
            color = Color(0xFF17223D),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
