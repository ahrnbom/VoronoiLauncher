package com.tboneiss.voronoilauncher

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import com.tboneiss.voronoilauncher.ui.theme.VoronoiLauncherTheme
import kotlin.collections.minByOrNull
import kotlin.collections.indexOfFirst

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoronoiLauncherTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VoronoiLauncherTheme {
        Greeting("Android")
    }
}

data class PinnedApp(
    val id: String = java.util.UUID.randomUUID().toString(),
    val packageName: String,
    val label: String,
    val icon: Drawable,
    val position: Offset,
    val themeColor: Color = Color.Gray
)

fun findNearestApp(tapOffset: Offset, apps: List<PinnedApp>): PinnedApp? {
    return apps.minByOrNull { app: PinnedApp ->
        (tapOffset - app.position).getDistance()
    }
}

class LauncherViewModel : ViewModel() {
    // 1. Explicitly type the list
    val pinnedApps: SnapshotStateList<PinnedApp> = mutableStateListOf<PinnedApp>()

    // 2. Use the Unique ID for searching (safer than package name)
    fun moveApp(id: String, newPosition: Offset) {
        val idx = pinnedApps.indexOfFirst { it.id == id }
        if (idx != -1) {
            val updatedApp = pinnedApps[idx].copy(position = newPosition)
            pinnedApps[idx] = updatedApp
        }
    }
}

@Composable
fun VoronoiHomeScreen(apps: List<PinnedApp>) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset: Offset ->
                    val clickedApp = findNearestApp(offset, apps)
                    launchApp(clickedApp?.packageName)
                }
            }
        ) {
            val polygons = calculateVoronoi(apps, width, height)
            polygons.forEach { polygon ->
                drawPath(
                    path = polygon.toComposePath(),
                    color = polygon.appColor,
                    style = androidx.compose.ui.graphics.drawscope.Fill
                )
                drawIcon(polygon.center, polygon.icon)
            }
        }
    }
}