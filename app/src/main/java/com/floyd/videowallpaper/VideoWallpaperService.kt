package com.floyd.videowallpaper

import android.app.KeyguardManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.media.MediaPlayer
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.os.Handler
import android.os.Looper
import android.util.Log

class VideoWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = VideoEngine()

    private inner class VideoEngine : Engine(), SurfaceHolder.Callback {
        private val handler = Handler(Looper.getMainLooper())
        private val keyguard by lazy { getSystemService(KEYGUARD_SERVICE) as KeyguardManager }
        private var player: MediaPlayer? = null
        private var visibleNow = false
        private var surfaceReady = false
        private var staticBitmap: Bitmap? = null

        private val stateCheck = object : Runnable {
            override fun run() {
                if (!visibleNow) return
                updatePlaybackState()
                handler.postDelayed(this, 250L)
            }
        }

        init {
            surfaceHolder.addCallback(this)
            staticBitmap = BitmapFactory.decodeResource(resources, R.drawable.static_wallpaper)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            visibleNow = visible
            handler.removeCallbacks(stateCheck)
            if (visible) {
                updatePlaybackState()
                handler.post(stateCheck)
            } else {
                stopVideo()
            }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            surfaceReady = true
            updatePlaybackState()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            surfaceReady = true
            updatePlaybackState()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            surfaceReady = false
            stopVideo()
        }

        private fun updatePlaybackState() {
            if (!visibleNow || !surfaceReady || !surfaceHolder.surface.isValid) return
            val locked = try { keyguard.isKeyguardLocked } catch (_: Exception) { true }
            if (locked) {
                stopVideo()
                drawStillFrame()
            } else if (player == null) {
                startVideo()
            }
        }

        private fun drawStillFrame() {
            val bitmap = staticBitmap ?: return
            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    val dst = Rect(0, 0, canvas.width, canvas.height)
                    canvas.drawBitmap(bitmap, null, dst, null)
                }
            } catch (e: Exception) {
                Log.w("FloydWallpaper", "Could not draw still frame", e)
            } finally {
                if (canvas != null) try { surfaceHolder.unlockCanvasAndPost(canvas) } catch (_: Exception) {}
            }
        }

        private fun startVideo() {
            if (!surfaceReady || !surfaceHolder.surface.isValid || player != null) return
            try {
                val mp = MediaPlayer.create(this@VideoWallpaperService, R.raw.wallpaper)
                if (mp == null) {
                    drawStillFrame()
                    return
                }
                mp.setSurface(surfaceHolder.surface)
                mp.setVolume(0f, 0f)
                mp.isLooping = true
                mp.setOnErrorListener { _, _, _ -> stopVideo(); drawStillFrame(); true }
                player = mp
                mp.start()
            } catch (e: Exception) {
                Log.e("FloydWallpaper", "Could not start wallpaper video", e)
                stopVideo()
                drawStillFrame()
            }
        }

        private fun stopVideo() {
            val old = player
            player = null
            if (old != null) {
                try { old.stop() } catch (_: Exception) {}
                try { old.reset() } catch (_: Exception) {}
                try { old.release() } catch (_: Exception) {}
            }
        }

        override fun onDestroy() {
            handler.removeCallbacks(stateCheck)
            stopVideo()
            staticBitmap?.recycle()
            staticBitmap = null
            super.onDestroy()
        }
    }
}
