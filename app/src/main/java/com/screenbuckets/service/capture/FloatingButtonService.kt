package com.screenbuckets.service.capture

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.screenbuckets.R
import com.screenbuckets.utils.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FloatingButtonService : Service() {
    
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f
    
    override fun onCreate() {
        super.onCreate()
        
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        // Inflate the floating view layout
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_button, null)
        
        // Set up the WindowManager layout parameters
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }
        
        // Set up touch listener for moving the view
        setupTouchListener()
        
        // Set up the screenshot button click
        setupButtonClickListener()
        
        // Add the view to the window
        windowManager.addView(floatingView, params)
    }
    
    private fun setupTouchListener() {
        floatingView.setOnTouchListener { view, event ->
            val params = view.layoutParams as WindowManager.LayoutParams
            
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(view, params)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }
    
    private fun setupButtonClickListener() {
        val screenshotButton = floatingView.findViewById<ImageView>(R.id.ivFloatingButton)
        screenshotButton.setOnClickListener {
            // Check if we're in a sensitive app
            if (isSensitiveApp()) {
                // Show some feedback that screenshots are not allowed
                return@setOnClickListener
            }
            
            // Start screen capture
            val captureIntent = Intent(this, ScreenCaptureService::class.java).apply {
                action = ScreenCaptureService.ACTION_START_CAPTURE
            }
            ContextCompat.startForegroundService(this, captureIntent)
        }
    }
    
    private fun isSensitiveApp(): Boolean {
        // Package names of sensitive apps that shouldn't be captured
        val sensitiveApps = listOf(
            "com.bankapp", 
            "com.paymentapp",
            "org.thoughtcrime.securesms", // Signal
            "com.whatsapp",
            "com.android.settings"
            // Add more sensitive apps as needed
        )
        
        // Get current foreground app package name
        val foregroundApp = PermissionHelper.getForegroundAppPackage(this)
        
        return sensitiveApps.contains(foregroundApp)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::windowManager.isInitialized && ::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, FloatingButtonService::class.java)
            context.startService(intent)
        }
        
        fun stop(context: Context) {
            val intent = Intent(context, FloatingButtonService::class.java)
            context.stopService(intent)
        }
    }
}