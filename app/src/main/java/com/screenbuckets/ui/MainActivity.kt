package com.screenbuckets.ui

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.screenbuckets.service.capture.FloatingButtonService
import com.screenbuckets.service.capture.ScreenCaptureManager
import com.screenbuckets.ui.screens.HomeScreen
import com.screenbuckets.ui.screens.SearchScreen
import com.screenbuckets.ui.screens.SettingsScreen
import com.screenbuckets.ui.theme.ScreenBucketsTheme
import com.screenbuckets.utils.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val requestOverlayPermission = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (PermissionHelper.hasOverlayPermission(this)) {
            initFloatingButton()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            ScreenBucketsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Handle screen capture permission result
                    val screenCaptureLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                            // Store the result in ScreenCaptureManager
                            ScreenCaptureManager.resultCode = result.resultCode
                            ScreenCaptureManager.data = result.data
                            
                            // Start the floating button if we have the overlay permission
                            if (PermissionHelper.hasOverlayPermission(this)) {
                                initFloatingButton()
                            } else {
                                // Request overlay permission
                                requestOverlayPermission.launch(
                                    PermissionHelper.getOverlayPermissionIntent(this)
                                )
                            }
                        }
                    }
                    
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                navController = navController,
                                viewModel = hiltViewModel(),
                                onStartCapture = {
                                    // Request screen capture permission
                                    val mediaProjectionManager = getSystemService(
                                        MEDIA_PROJECTION_SERVICE
                                    ) as MediaProjectionManager
                                    
                                    val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
                                    screenCaptureLauncher.launch(captureIntent)
                                }
                            )
                        }
                        composable("search") {
                            SearchScreen(
                                navController = navController,
                                viewModel = hiltViewModel()
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                navController = navController,
                                viewModel = hiltViewModel()
                            )
                        }
                        composable("screenshot/{screenshotId}") { backStackEntry ->
                            val screenshotId = backStackEntry.arguments?.getString("screenshotId")?.toLongOrNull() ?: return@composable
                            ScreenshotDetailScreen(
                                navController = navController,
                                screenshotId = screenshotId,
                                viewModel = hiltViewModel()
                            )
                        }
                    }
                }
            }
        }
    }
    
    private fun initFloatingButton() {
        FloatingButtonService.start(this)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        FloatingButtonService.stop(this)
    }
}