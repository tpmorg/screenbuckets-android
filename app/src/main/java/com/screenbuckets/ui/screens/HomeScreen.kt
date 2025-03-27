package com.screenbuckets.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.screenbuckets.data.model.Screenshot
import java.io.File
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    onStartCapture: () -> Unit
) {
    val screenshotsState by viewModel.screenshots.collectAsState(initial = emptyList())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ScreenBuckets") }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Already on home */ },
                    icon = { Icon(Icons.Default.Image, contentDescription = "Screenshots") },
                    label = { Text("Screenshots") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("search") },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text("Search") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("settings") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onStartCapture) {
                Icon(Icons.Default.Add, contentDescription = "Take Screenshot")
            }
        }
    ) { paddingValues ->
        
        if (screenshotsState.isEmpty()) {
            EmptyState(paddingValues)
        } else {
            ScreenshotGrid(
                screenshots = screenshotsState,
                paddingValues = paddingValues,
                onScreenshotClick = { screenshot ->
                    navController.navigate("screenshot/${screenshot.id}")
                }
            )
        }
    }
}

@Composable
private fun EmptyState(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Image,
            contentDescription = "No Screenshots",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No screenshots yet",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the + button to take a screenshot",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ScreenshotGrid(
    screenshots: List<Screenshot>,
    paddingValues: PaddingValues,
    onScreenshotClick: (Screenshot) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = paddingValues,
        modifier = Modifier.fillMaxSize()
    ) {
        items(screenshots) { screenshot ->
            ScreenshotItem(
                screenshot = screenshot,
                onClick = { onScreenshotClick(screenshot) }
            )
        }
    }
}

@Composable
private fun ScreenshotItem(
    screenshot: Screenshot,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = File(screenshot.filePath),
                    contentDescription = screenshot.description ?: "Screenshot",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    onLoading = {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.Center)
                        )
                    }
                )
            }
            
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = screenshot.appName,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = screenshot.timestamp.format(
                        DateTimeFormatter.ofPattern("MMM dd, HH:mm")
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (screenshot.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        screenshot.tags.take(2).forEach { tag ->
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}