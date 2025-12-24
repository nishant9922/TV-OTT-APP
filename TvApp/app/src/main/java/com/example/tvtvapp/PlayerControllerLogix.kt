package com.example.tvtvapp

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.media3.common.util.UnstableApi
import com.logituit.logixsdk.logixplayer.player.LogixPlayer
import com.logituit.logixsdk.logixplayer.player.LogixPlayerView
import java.util.Locale
import java.util.concurrent.TimeUnit

@UnstableApi
class PlayerControllerLogix(
    private val context: Context,
    private val overlayContainer: ViewGroup,
    private val playerView: LogixPlayerView,
    private val logixPlayer: LogixPlayer
) {

    private val handler = Handler(Looper.getMainLooper())
    private val updateMs = 500L

    // Views
    private val controlsRoot: View
    private val bottomContainer: View
    private val container: ViewGroup = overlayContainer // alias

    private val btnPlayPause: ImageButton
    private val btnRewind: ImageButton
    private val btnForward: ImageButton
    private val seekBar: SeekBar
    private val tvElapsed: TextView
    private val tvTotal: TextView

    // State
    private var isControlsVisible = true
    private val autoHideDelay = 3000L

    init {
        LayoutInflater.from(context).inflate(R.layout.view_video_controls, overlayContainer, true)

        controlsRoot = overlayContainer.findViewById(R.id.controlsRoot)
        bottomContainer = overlayContainer.findViewById(R.id.bottomSeekContainer)

        btnPlayPause = overlayContainer.findViewById(R.id.btnPlayPause)
        btnRewind = overlayContainer.findViewById(R.id.btnRewind)
        btnForward = overlayContainer.findViewById(R.id.btnForward)

        seekBar = overlayContainer.findViewById(R.id.seekBar)
        tvElapsed = overlayContainer.findViewById(R.id.tvElapsed)
        tvTotal = overlayContainer.findViewById(R.id.tvTotal)

        // Ensure the overlay container can capture keys when controls are hidden
        container.isFocusable = true
        container.isFocusableInTouchMode = true
        container.isClickable = true
        container.requestFocus()

        setupFocusButtons()
        setupSeekBar()
        setupGlobalKeyHandler()
        startUpdatingProgress()
        syncPlayPauseIcon()


        // Initial focus on play/pause
        btnPlayPause.post { btnPlayPause.requestFocus() }

        // show controls initially then schedule auto-hide
        showControls()
    }

    // -----------------------------
    // Buttons & Focus handling
    // -----------------------------
    private fun setupFocusButtons() {
        val focusAnim = View.OnFocusChangeListener { v, hasFocus ->
            val scale = if (hasFocus) 1.15f else 1f
            ObjectAnimator.ofFloat(v, "scaleX", scale).setDuration(120).start()
            ObjectAnimator.ofFloat(v, "scaleY", scale).setDuration(120).start()

            if (hasFocus) scheduleAutoHide() // keep controls visible while user interacts
        }

        btnPlayPause.onFocusChangeListener = focusAnim
        btnRewind.onFocusChangeListener = focusAnim
        btnForward.onFocusChangeListener = focusAnim

        btnPlayPause.setOnClickListener {
            togglePlayPause()
            scheduleAutoHide()
        }

        btnRewind.setOnClickListener {
            val pos = logixPlayer.getCurrentPosition()
            logixPlayer.seekTo((pos - 10_000).coerceAtLeast(0))
            scheduleAutoHide()
        }

        btnForward.setOnClickListener {
            val pos = logixPlayer.getCurrentPosition()
            val dur = logixPlayer.getDuration()
            logixPlayer.seekTo((pos + 10_000).coerceAtMost(dur))
            scheduleAutoHide()
        }

        // DPAD button navigation handler (left/right/center)
        val keyHandler = View.OnKeyListener { v, keyCode, event ->
            if (event.action != KeyEvent.ACTION_DOWN) return@OnKeyListener false

            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                    v.performClick()
                    true
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    when (v.id) {
                        R.id.btnPlayPause -> btnRewind.requestFocus()
                        R.id.btnForward -> btnPlayPause.requestFocus()
                        else -> false
                    }
                    true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    when (v.id) {
                        R.id.btnPlayPause -> btnForward.requestFocus()
                        R.id.btnRewind -> btnPlayPause.requestFocus()
                        else -> false
                    }
                    true
                }
                else -> false
            }
        }

        btnPlayPause.setOnKeyListener(keyHandler)
        btnRewind.setOnKeyListener(keyHandler)
        btnForward.setOnKeyListener(keyHandler)
    }

    private fun syncPlayPauseIcon() {
        btnPlayPause.setImageResource(
            if (logixPlayer.isPlaying())
                R.drawable.ic_pause
            else
                R.drawable.ic_play
        )
    }


    // -----------------------------
    // Make overlayContainer capture keys and show controls when hidden
    // -----------------------------
    private fun setupGlobalKeyHandler() {
        container.setOnKeyListener { _, keyCode, event ->
            if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false

            // If controls hidden: always show them and focus Play/Pause
            if (!isControlsVisible) {
                showControls()
                btnPlayPause.post { btnPlayPause.requestFocus() }
                return@setOnKeyListener true
            }

            // When visible: handle global media keys (play/pause) and refresh auto-hide
            when (keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                KeyEvent.KEYCODE_MEDIA_PLAY,
                KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                    togglePlayPause()
                    scheduleAutoHide()
                    return@setOnKeyListener true
                }
            }

            false
        }
    }

    // -----------------------------
    // Seek bar
    // -----------------------------
    private fun setupSeekBar() {
        seekBar.max = 1000
        seekBar.isFocusable = false
        seekBar.isFocusableInTouchMode = false

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(sb: SeekBar?) {
                // pause auto-hide while scrubbing
                handler.removeCallbacks(hideRunnable)
            }

            override fun onStopTrackingTouch(sb: SeekBar?) {
                val dur = logixPlayer.getDuration()
                val pr = sb?.progress ?: 0
                val pos = (pr / 1000f) * dur
                logixPlayer.seekTo(pos.toLong())
                scheduleAutoHide()
            }

            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val dur = logixPlayer.getDuration()
                    if (dur > 0) {
                        val pos = (progress / 1000f) * dur
                        tvElapsed.text = formatTime(pos.toLong())
                    }
                }
            }
        })
    }

    // -----------------------------
    // Play/pause
    // -----------------------------
    private fun togglePlayPause() {
        if (logixPlayer.isPlaying()) {
            logixPlayer.pause()
        } else {
            logixPlayer.play()
        }
        // icon will be updated by sync / listener
    }


    // -----------------------------
    // Auto-hide
    // -----------------------------
    private val hideRunnable = Runnable { hideControls() }

    private fun scheduleAutoHide() {
        handler.removeCallbacks(hideRunnable)
        handler.postDelayed(hideRunnable, autoHideDelay)
    }

    private fun showControls() {
        // make visible
        controlsRoot.visibility = View.VISIBLE
        bottomContainer.visibility = View.VISIBLE
        controlsRoot.animate().alpha(1f).setDuration(180).start()
        bottomContainer.animate().alpha(1f).setDuration(180).start()

        // When visible, ensure container does not steal focus so buttons can receive focus
        container.isFocusable = true
        container.isFocusableInTouchMode = true
        container.isClickable = true

        // ensure buttons are focusable (they are) and set initial focus
        btnPlayPause.post { btnPlayPause.requestFocus() }

        isControlsVisible = true
        scheduleAutoHide()
    }

    private fun hideControls() {
        // fade out
        controlsRoot.animate().alpha(0f).setDuration(180).withEndAction {
            controlsRoot.visibility = View.GONE
        }.start()
        bottomContainer.animate().alpha(0f).setDuration(180).withEndAction {
            bottomContainer.visibility = View.GONE
        }.start()

        // keep container focusable and request focus so it captures the next remote key
        container.isFocusable = true
        container.isFocusableInTouchMode = true
        container.isClickable = true
        container.requestFocus()

        // clear focus from children so container truly holds it
        try { controlsRoot.clearFocus() } catch (_: Exception) {}

        isControlsVisible = false
    }

    // -----------------------------
    // Progress updater
    // -----------------------------
    private fun startUpdatingProgress() {
        handler.post(object : Runnable {
            override fun run() {
                val dur = logixPlayer.getDuration()
                val pos = logixPlayer.getCurrentPosition()
                if (dur > 0) {
                    val p = ((pos.toFloat() / dur) * 1000).toInt().coerceIn(0, 1000)
                    if (!seekBar.isPressed) seekBar.progress = p
                }
                tvElapsed.text = formatTime(pos)
                tvTotal.text = formatTime(dur)
                syncPlayPauseIcon()
                handler.postDelayed(this, updateMs)
            }
        })
    }



    private fun formatTime(ms: Long): String {
        if (ms <= 0) return "00:00"
        val sec = TimeUnit.MILLISECONDS.toSeconds(ms)
        return String.format(Locale.getDefault(), "%02d:%02d", sec / 60, sec % 60)
    }

    fun release() {
        handler.removeCallbacksAndMessages(null)
    }
}
