package com.example.tvtvapp.player

import android.content.Context
import android.util.Log
import android.view.View
import androidx.media3.common.util.UnstableApi
import com.logituit.logixsdk.logixplayer.LogixPlayerSDK
import com.logituit.logixsdk.logixplayer.player.LogixMediaItem
import com.logituit.logixsdk.logixplayer.player.LogixPlayer
import com.logituit.logixsdk.logixplayer.player.LogixPlayerView
import java.lang.ref.WeakReference

@UnstableApi
object PlayerManager {

    private const val TAG = "PlayerManager"

    private var player: LogixPlayer? = null
    private var currentView: LogixPlayerView? = null
    private var media: LogixMediaItem? = null
    private var resumePos: Long = 0L

    var lastFocusedRailViewRef: WeakReference<View>? = null


    var mode: PlayerMode = PlayerMode.MINI
        private set

    // 🔥 EXTRA DATA FOR FULLSCREEN LAUNCH
    private var lastMediaId: String? = null


    fun init(context: Context) {
        if (player == null) {
            player = LogixPlayerSDK
                .newLogixPlayerBuilder(context.applicationContext)
                .build()
            Log.d(TAG, "Shared player created")
        }
    }

    fun getPlayer(): LogixPlayer =
        player ?: error("Player not initialized")

    fun setMode(newMode: PlayerMode) {
        mode = newMode
    }

    fun setMedia(item: LogixMediaItem, position: Long = 0L) {
        media = item
        lastMediaId = item.id
        resumePos = position
        player?.prepare(item)
    }

    fun attach(view: LogixPlayerView) {
        val p = player ?: return
        val m = media ?: return

        detachCurrentView()

        view.visibility = View.VISIBLE
        p.attachPlayerView(view)
        currentView = view

        if (resumePos > 0) {
            p.seekTo(resumePos)
            resumePos = 0L
        }

        p.play()
    }

    fun detachCurrentView() {
        currentView?.let {
            player?.detachPlayerView(it)
            currentView = null
        }
    }

    fun handoffToMini(pos: Long) {
        resumePos = pos
        mode = PlayerMode.MINI
    }

    // 🔥 DATA FOR FULLSCREEN
    fun getFullscreenIntentData(): Triple<String, String, Long>? {
        val m = media ?: return null
        val url = m.sourceUrl ?: return null

        return Triple(
            url,
            m.title,
            resumePos
        )
    }


    fun isActive(): Boolean = media != null

    fun togglePlayPause() {
        val p = player ?: return

        try {
            if (p.isPlaying()) {
                Log.d("PlayerManager", "Pause")
                p.pause()
            } else {
                Log.d("PlayerManager", "Play")
                p.play()
            }
        } catch (e: Exception) {
            Log.e("PlayerManager", "Toggle failed", e)
        }
    }


    fun isPlaying(): Boolean {
        return player?.isPlaying() == true
    }
    fun setLastFocusedRail(view: View) {
        lastFocusedRailViewRef = WeakReference(view)
    }

    fun restoreRailFocus() {
        lastFocusedRailViewRef?.get()?.requestFocus()
    }



    fun release() {
        player?.release()
        player = null
        currentView = null
        media = null
        resumePos = 0L
        lastMediaId = null
        mode = PlayerMode.MINI
    }
}
