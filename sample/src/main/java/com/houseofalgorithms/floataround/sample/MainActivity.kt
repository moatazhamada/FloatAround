package com.houseofalgorithms.floataround.sample

import com.houseofalgorithms.floataround.FloatingObstacle
import com.houseofalgorithms.floataround.avoid
import com.houseofalgorithms.floataround.floatingObstacle
import com.houseofalgorithms.floataround.plusAvoidance
import com.houseofalgorithms.floataround.rememberAvoidanceTracker
import com.houseofalgorithms.floataround.track

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
                DemoApp()
            }
        }
    }
}

private data class Destination(val label: String, val icon: ImageVector)

private val destinations = listOf(
    Destination("Home", Icons.Filled.Home),
    Destination("Search", Icons.Filled.Search),
    Destination("Saved", Icons.Filled.Favorite),
    Destination("Profile", Icons.Filled.Person),
    Destination("Settings", Icons.Filled.Settings),
)

@Composable
private fun DemoApp() {
    val obstacle = remember { FloatingObstacle() }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        if (maxWidth >= 600.dp) {
            Box(Modifier.fillMaxSize()) {
                DemoFeed(obstacle)
                FloatingPill(
                    selected = selected,
                    onSelect = { selected = it },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp)
                        .floatingObstacle(obstacle),
                )
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f)) { DemoFeed(obstacle) }
                NavigationBar {
                    destinations.forEachIndexed { index, destination ->
                        NavigationBarItem(
                            selected = index == selected,
                            onClick = { selected = index },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                        )
                    }
                }
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
        )
    }
}

@Composable
private fun FloatingPill(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 6.dp,
    ) {
        Column(Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            destinations.forEachIndexed { index, destination ->
                NavigationRailItem(
                    selected = index == selected,
                    onClick = { onSelect(index) },
                    icon = { Icon(destination.icon, contentDescription = destination.label) },
                )
            }
        }
    }
}

private val paragraphs = listOf(
    "Floating navigation is back. Tablets and unfolded foldables give us room to lift the rail off the edge and let content run underneath it, the way a modern sidebar floats over the page.",
    "But content that runs underneath also gets covered. A title, a row of cards or the first words of a paragraph end up hidden behind the pill.",
    "Padding the whole page wastes the space we just gained. The pill is short; most of the screen never touches it.",
    "So every row asks one question while it scrolls: how close am I to the pill? Rows beside it step aside. Rows near it step aside a little. Rows far away keep the full width.",
    "The easing is a smootherstep curve over a small feather distance, so a row starts moving before it reaches the pill and settles back after it leaves. No animation clock, the movement is tied to the scroll position itself.",
)

@Composable
private fun DemoFeed(obstacle: FloatingObstacle) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Hero(obstacle) }
        repeat(3) { round ->
            item { SectionHeader("Featured ${round + 1}", obstacle) }
            item { Carousel(obstacle) }
            item { SectionHeader("The idea", obstacle) }
            items(paragraphs) { paragraph ->
                Text(
                    text = paragraph,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.avoid(obstacle).padding(horizontal = 16.dp),
                )
            }
            item { SectionHeader("List rows", obstacle) }
            items(4) { index ->
                Card(Modifier.avoid(obstacle).padding(horizontal = 16.dp).fillMaxWidth()) {
                    Text("Row ${index + 1} keeps its right edge and gives up only its start", Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
private fun Hero(obstacle: FloatingObstacle) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(Brush.linearGradient(listOf(Color(0xFF3949AB), Color(0xFF00897B)))),
    ) {
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .avoid(obstacle)
                .padding(24.dp),
        ) {
            Text("FloatAround", color = Color.White, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text("The hero stays full-bleed. Only its text steps aside.", color = Color.White)
        }
        FilterChip(
            selected = obstacle.enabled,
            onClick = { obstacle.enabled = !obstacle.enabled },
            label = { Text(if (obstacle.enabled) "Avoidance on" else "Avoidance off") },
            modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(16.dp),
        )
    }
}

@Composable
private fun SectionHeader(title: String, obstacle: FloatingObstacle) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HorizontalDivider(Modifier.weight(1f).avoid(obstacle), thickness = 2.dp)
        Text(title, style = MaterialTheme.typography.titleLarge)
        HorizontalDivider(Modifier.weight(1f), thickness = 2.dp)
    }
}

@Composable
private fun Carousel(obstacle: FloatingObstacle) {
    val tracker = rememberAvoidanceTracker(obstacle)
    val density = LocalDensity.current
    LazyRow(
        modifier = Modifier.fillMaxWidth().track(tracker),
        contentPadding = PaddingValues(horizontal = 16.dp).plusAvoidance(tracker, density),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(10) { index ->
            Card(Modifier.size(width = 160.dp, height = 120.dp)) {
                Box(Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.BottomStart) {
                    Text("Card ${index + 1}", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
