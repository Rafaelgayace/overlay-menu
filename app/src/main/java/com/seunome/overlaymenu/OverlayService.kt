package com.seunome.overlaymenu

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import java.util.concurrent.ConcurrentLinkedQueue

class OverlayService : Service() {
    companion object {
        val pendingActions = ConcurrentLinkedQueue<String>()
    }

    private lateinit var wm: WindowManager
    private lateinit var root: LinearLayout
    private lateinit var server: LocalServer

    override fun onBind(i: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#CC1E1E2E"))
            setPadding(16, 16, 16, 16)
        }

        // exemplos de botões — adicione quantos quiser
        listOf("oi", "gg", "kkk", "obrigado").forEach { msg ->
            val b = Button(this).apply {
                text = msg
                setOnClickListener { pendingActions.add(msg) }
            }
            root.addView(b)
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50; y = 200
        }

        // arrastar
        root.setOnTouchListener(object : View.OnTouchListener {
            var ix = 0; var iy = 0; var tx = 0f; var ty = 0f
            override fun onTouch(v: View, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> { ix = params.x; iy = params.y; tx = e.rawX; ty = e.rawY }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = ix + (e.rawX - tx).toInt()
                        params.y = iy + (e.rawY - ty).toInt()
                        wm.updateViewLayout(root, params)
                    }
                }
                return false
            }
        })

        wm.addView(root, params)
        server = LocalServer(8080).also { it.start() }
    }

    override fun onDestroy() {
        super.onDestroy()
        wm.removeView(root)
        server.stop()
    }
}
