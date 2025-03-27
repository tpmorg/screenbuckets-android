package com.screenbuckets.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel
) {
    val settings by viewModel.settings.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SettingsSection(title = "General")
            
            SettingsSwitch(
                title = "Start on boot",
                description = "Start ScreenBuckets when device boots",
                checked = settings.startOnBoot,
                onCheckedChange = { viewModel.updateStartOnBoot(it) }
            )
            
            SettingsSwitch(
                title = "Floating button",
                description = "Show floating screenshot button",
                checked = settings.showFloatingButton,
                onCheckedChange = { viewModel.updateShowFloatingButton(it) }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            SettingsSection(title = "Privacy")
            
            SettingsSwitch(
                title = "Skip sensitive apps",
                description = "Don't allow screenshots in banking, messaging apps",
                checked = settings.skipSensitiveApps,
                onCheckedChange = { viewModel.updateSkipSensitiveApps(it) }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            SettingsSection(title = "Analysis")
            
            SettingsSwitch(
                title = "Auto analyze screenshots",
                description = "Automatically analyze screenshots with AI",
                checked = settings.autoAnalyzeScreenshots,
                onCheckedChange = { viewModel.updateAutoAnalyzeScreenshots(it) }
            )
            
            SettingsSwitch(
                title = "Only analyze on Wi-Fi",
                description = "Save mobile data by only analyzing on Wi-Fi",
                checked = settings.analyzeOnlyOnWifi,
                onCheckedChange = { viewModel.updateAnalyzeOnlyOnWifi(it) }
            )
            
            SettingItem(
                title = "API Key",
                description = "Set your AI service API key",
                onClick = {
                    // Navigate to API key setting screen
                }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            SettingsSection(title = "Storage")
            
            SettingItem(
                title = "Clear all screenshots",
                description = "Delete all captured screenshots",
                onClick = {
                    // Show confirmation dialog and clear
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "ScreenBuckets v1.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}