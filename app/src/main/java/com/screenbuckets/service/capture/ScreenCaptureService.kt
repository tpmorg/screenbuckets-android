package com.screenbuckets.service.capture

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import androidx.core.app.NotificationCompat
import com.screenbuckets.R
import com.screenbuckets.data.repository.ScreenshotRepository
import com.screenbuckets.utils.PermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

class ScreenCaptureService : Service() {
    
    private val repository by lazy { ScreenshotRepository.getInstance() }
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private val handler = Handler(Looper.getMainLooper())
    
    private lateinit var metrics: DisplayMetrics
    private var width = 0
    private var height = 0
    private var density = 0
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground()
        
        metrics = resources.displayMetrics
        width = metrics.widthPixels
        height = metrics.heightPixels
        density = metrics.densityDpi
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_CAPTURE -> {
                val resultCode = ScreenCaptureManager.resultCode
                val data = ScreenCaptureManager.data
                
                if (resultCode != 0 && data != null) {
                    startCapture(resultCode, data)
                } else {
                    stopSelf()
                }
            }
            ACTION_STOP_CAPTURE -> {
                stopCapture()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }
    
    private fun startCapture(resultCode: Int, data: Intent) {
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)
        
        imageReader = ImageReader.newInstance(
            width, height, PixelFormat.RGBA_8888, 2
        ).apply {
            setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage()
                if (image != null) {
                    processImage(image)
                }
            }, handler)
        }
        
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, null
        )
    }
    
    private fun processImage(image: Image) {
        try {
            val buffer: ByteBuffer = image.planes[0].buffer
            val pixelStride = image.planes[0].pixelStride
            val rowStride = image.planes[0].rowStride
            val rowPadding = rowStride - pixelStride * width
            
            // Create bitmap
            val bitmap = Bitmap.createBitmap(
                width + rowPadding / pixelStride,
                height, Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)
            
            // Crop bitmap to screen dimensions
            val croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)
            bitmap.recycle()
            
            // Get current foreground app info
            val packageName = PermissionHelper.getForegroundAppPackage(this) ?: "unknown"
            val appName = PermissionHelper.getAppNameFromPackage(this, packageName) ?: "Unknown App"
            
            // Save screenshot
            serviceScope.launch {
                repository.saveScreenshot(
                    croppedBitmap,
                    appName,
                    packageName
                )
                
                // Stop capturing after taking a screenshot
                stopCapture()
                stopSelf()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image: ${e.message}")
        } finally {
            image.close()
        }
    }
    
    private fun stopCapture() {
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        
        virtualDisplay = null
        imageReader = null
        mediaProjection = null
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screen Capture Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used when capturing the screen"
                enableVibration(false)
                enableLights(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun startForeground() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Capturing Screen")
            .setContentText("ScreenBuckets is capturing your screen")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        stopCapture()
        super.onDestroy()
    }
    
    companion object {
        private const val TAG = "ScreenCaptureService"
        private const val CHANNEL_ID = "screen_capture_channel"
        private const val NOTIFICATION_ID = 1
        
        const val ACTION_START_CAPTURE = "action_start_capture"
        const val ACTION_STOP_CAPTURE = "action_stop_capture"
    }
}