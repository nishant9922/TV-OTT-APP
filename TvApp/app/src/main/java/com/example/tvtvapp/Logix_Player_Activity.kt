package com.example.tvtvapp

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import com.example.tvtvapp.player.PlayerManager
import com.example.tvtvapp.player.PlayerMode
import com.logituit.logixsdk.logixplayer.player.LogixPlayer
import com.logituit.logixsdk.logixplayer.player.LogixPlayerView

private const val TAG = "LogixPlayerActivity"

@UnstableApi
class LogixPlayerActivity : AppCompatActivity() {

    private lateinit var player: LogixPlayer
    private lateinit var playerView: LogixPlayerView
    private lateinit var controller: PlayerControllerLogix

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logix_player)

        playerView = findViewById(R.id.logixPlayerView)
        val overlayContainer = findViewById<ViewGroup>(R.id.playerOverlayContainer)

        if (!PlayerManager.isActive()) {
            Toast.makeText(this, "No active media", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 🔥 shared player
        PlayerManager.init(this)
        player = PlayerManager.getPlayer()

        // 🔥 switch to FULL mode
        PlayerManager.setMode(PlayerMode.FULL)

        // 🔥 detach mini surface
        PlayerManager.detachCurrentView()

        // 🔥 attach fullscreen surface
        player.attachPlayerView(playerView)

        playerView.bringToFront()
        playerView.requestFocus()
        overlayContainer.bringToFront()

        // 🔥 resume playback
        val resumePos = intent.getLongExtra("RESUME_POSITION", 0L)
        if (resumePos > 0) {
            player.seekTo(resumePos)
        }

        player.play()

        controller = PlayerControllerLogix(
            context = this,
            overlayContainer = overlayContainer,
            playerView = playerView,
            logixPlayer = player
        )

        Log.d(TAG, "Fullscreen attached successfully")
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    /**
     * 🔥 FULL → MINI HANDOFF (CORRECT)
     */
    override fun onBackPressed() {
        val pos = player.getCurrentPosition()
        Log.d("Player", "FULL → MINI at $pos")

        // 🔥 HANDOFF CORRECTLY
        PlayerManager.handoffToMini(pos)

        // 🔥 DETACH ONLY VIEW (NOT PLAYER)
        player.detachPlayerView(playerView)

        finish()
    }

    override fun onDestroy() {
        try {
            controller.release()
        } catch (_: Exception) {}
        super.onDestroy()
    }
}