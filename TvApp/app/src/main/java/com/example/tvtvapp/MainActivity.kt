package com.example.tvtvapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log.v
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.animate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.example.tvtvapp.model.MenuItemModel
import com.example.tvtvapp.player.PlayerManager
import com.example.tvtvapp.player.PlayerMode
import com.example.tvtvapp.ui.HomeFragment
import com.example.tvtvapp.ui.fragments.*
import com.example.tvtvapp.ui.menu.NavigationAdapter
import com.logituit.logixsdk.logixplayer.player.LogixPlayerView

@UnstableApi
class MainActivity : AppCompatActivity() {

    private lateinit var drawer: DrawerLayout
    private lateinit var navRecycler: RecyclerView
    private lateinit var miniContainer: View
    private lateinit var miniPlayerView: LogixPlayerView
    private lateinit var miniPlayPause: View


    private val MINI_WIDTH_DP = 420
    private val MINI_HEIGHT_DP = 236
    private val MINI_MARGIN_DP = 24

    private var isAnimating = false
    var blockNextCenterKeyUp = false

    private var lastAnchoredRail = RecyclerView.NO_POSITION

    private var currentMiniPosition: MiniPosition = MiniPosition.BOTTOM_RIGHT

    enum class MiniPosition {
        TOP_RIGHT,
        BOTTOM_RIGHT
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawer = findViewById(R.id.drawer)
        navRecycler = findViewById(R.id.navRecycler)
        miniContainer = findViewById(R.id.miniPlayerContainer)
        miniPlayerView = findViewById(R.id.miniPlayerView)
        miniPlayPause = findViewById(R.id.miniPlayPause)
        PlayerManager.init(this)
        miniContainer.isFocusable = true
        miniContainer.isFocusableInTouchMode = true


        miniContainer.apply {
            visibility = View.GONE
            isFocusable = true
            isFocusableInTouchMode = true
        }
        miniContainer.setOnFocusChangeListener { _, hasFocus ->
            updatePlayPauseIcon()
            miniPlayPause.visibility = if (hasFocus) View.VISIBLE else View.INVISIBLE


            // 🔍 Debug – confirms focus is actually moving
            android.util.Log.d("MiniPlayerFocus", "hasFocus = $hasFocus")

        }


        setupMiniPlayerControls()
        setupDrawer()
        setupBackHandler()
    }


    override fun onResume() {
        super.onResume()

        if (PlayerManager.isActive() && PlayerManager.mode == PlayerMode.MINI) {
            resetMiniSize()
            miniContainer.visibility = View.VISIBLE

            miniPlayerView.post {
                PlayerManager.attach(miniPlayerView)
                updatePlayPauseIcon()
                currentMiniPosition = MiniPosition.BOTTOM_RIGHT
                moveMiniPlayer(MiniPosition.BOTTOM_RIGHT)
            }
        } else {
            miniContainer.visibility = View.GONE
        }
    }

    private fun setupMiniPlayerControls() {

        // 🎬 MINI PLAYER CONTAINER
        miniContainer.setOnKeyListener { _, keyCode, event ->

            when (keyCode) {

                // ▶️ OPEN FULLSCREEN (ONLY ON KEY UP)
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER -> {
                    // 🚫 If play/pause is focused, DO NOT open fullscreen
                    if (miniPlayPause.hasFocus()) {
                        return@setOnKeyListener false
                    }
                    // 🚫 Ignore DOWN to avoid long-press conflict
                    if (event.action != KeyEvent.ACTION_UP) return@setOnKeyListener true

                    // 🔐 Block if coming from long-press focus transfer
                    if (blockNextCenterKeyUp) {
                        blockNextCenterKeyUp = false
                        return@setOnKeyListener true
                    }

                    launchFullscreenSmooth()
                    true
                }

                // ➡️ MOVE TO PLAY / PAUSE
                KeyEvent.KEYCODE_DPAD_LEFT,
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        updatePlayPauseIcon()
                        miniPlayPause.visibility = View.VISIBLE
                        miniPlayPause.requestFocus()
                    }
                    true
                }

                // ⬆️ BACK TO RAIL
                KeyEvent.KEYCODE_DPAD_UP -> {
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        PlayerManager.restoreRailFocus()
                    }
                    true
                }

                else -> false
            }
        }

        // ⏯ PLAY / PAUSE BUTTON
        miniPlayPause.setOnKeyListener { _, keyCode, event ->
            if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false

            when (keyCode) {

                // ⏯ TOGGLE PLAY / PAUSE
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER -> {
                    PlayerManager.togglePlayPause()
                    updatePlayPauseIcon()
                    true
                }

                // ⬅️ BACK TO MINI PLAYER
                KeyEvent.KEYCODE_DPAD_LEFT,
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    miniContainer.requestFocus()
                    true
                }

                // ⬆️ BACK TO RAIL
                KeyEvent.KEYCODE_DPAD_UP -> {
                    PlayerManager.restoreRailFocus()
                    true
                }

                else -> false
            }

        }
        miniPlayPause.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                updatePlayPauseIcon()
                miniPlayPause.visibility = View.VISIBLE
            }
        }


        // 👁 VISUAL ONLY
        miniContainer.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                //  When mini container is focused → HIDE icon
                miniPlayPause.visibility = View.GONE
            }
        }

    }

    fun updatePlayPauseIcon() {
        val icon = if (PlayerManager.isPlaying())
            R.drawable.ic_pause
        else
            R.drawable.ic_play

        (miniPlayPause as ImageView).setImageResource(icon)
    }


    // ---------------- MINI → FULL ----------------
    private fun launchFullscreenSmooth() {
        if (isAnimating) return
        isAnimating = true

        val data = PlayerManager.getFullscreenIntentData()
            ?: run {
                isAnimating = false
                return
            }

        val (url, title, pos) = data

        val startW = miniContainer.width
        val startH = miniContainer.height
        val endW = window.decorView.width
        val endH = window.decorView.height

        val widthAnim = ValueAnimator.ofInt(startW, endW)
        val heightAnim = ValueAnimator.ofInt(startH, endH)

        widthAnim.addUpdateListener {
            miniContainer.layoutParams.width = it.animatedValue as Int
            miniContainer.requestLayout()
        }

        heightAnim.addUpdateListener {
            miniContainer.layoutParams.height = it.animatedValue as Int
            miniContainer.requestLayout()
        }

        AnimatorSet().apply {
            duration = 300
            interpolator = FastOutSlowInInterpolator()
            playTogether(widthAnim, heightAnim)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    startActivity(
                        Intent(this@MainActivity, LogixPlayerActivity::class.java).apply {
                            putExtra("VIDEO_URL", url)
                            putExtra("VIDEO_TITLE", title)
                            putExtra("RESUME_POSITION", pos)
                        }
                    )
                    overridePendingTransition(0, 0)
                    miniContainer.visibility = View.GONE
                    isAnimating = false
                }
            })
            start()
        }
    }


    private fun animateResize(
        startW: Int,
        startH: Int,
        endW: Int,
        endH: Int,
        onEnd: () -> Unit
    ) {
        val w = ValueAnimator.ofInt(startW, endW)
        val h = ValueAnimator.ofInt(startH, endH)

        w.addUpdateListener {
            miniContainer.layoutParams.width = it.animatedValue as Int
            miniContainer.requestLayout()
        }
        h.addUpdateListener {
            miniContainer.layoutParams.height = it.animatedValue as Int
            miniContainer.requestLayout()
        }

        AnimatorSet().apply {
            duration = 280
            interpolator = FastOutSlowInInterpolator()
            playTogether(w, h)
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onEnd()
                }
            })
            start()
        }
    }

    private fun setupBackHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START)
                    return
                }

                if (miniContainer.visibility == View.VISIBLE &&
                    PlayerManager.mode == PlayerMode.MINI
                ) {
                    miniContainer.visibility = View.GONE
                    PlayerManager.release()
                    return
                }

                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        })
    }

    private fun setupDrawer() {
        navRecycler.adapter = NavigationAdapter(
            listOf(
                MenuItemModel("Home", "HomeFragment"),
                MenuItemModel("My List", "MyListFragment"),
                MenuItemModel("Movies", "MoviesFragment"),
                MenuItemModel("TV Shows", "TvShowsFragment"),
                MenuItemModel("Settings", "SettingsFragment")
            )
        ) {
            openFragment(it.fragmentTag)
            drawer.closeDrawer(GravityCompat.START)
        }

        if (supportFragmentManager.fragments.isEmpty()) {
            openFragment("HomeFragment")
        }
    }

    private fun openFragment(tag: String) {
        val fragment: Fragment = when (tag) {
            "MyListFragment" -> MyListFragment()
            "MoviesFragment" -> MoviesFragment()
            "TvShowsFragment" -> TvShowsFragment()
            "SettingsFragment" -> SettingsFragment()
            else -> HomeFragment()
        }

        supportFragmentManager.commit {
            replace(R.id.container, fragment)
        }
    }

    fun updateMiniPlayerForRailIndex(railIndex: Int) {
        if (!isMiniPlayerVisible()) return

        miniContainer.post {

            val miniRect = Rect()
            val railRect = Rect()

            miniContainer.getGlobalVisibleRect(miniRect)

            // Find rail view safely
            val railView = findRailViewByIndex(railIndex) ?: return@post
            railView.getGlobalVisibleRect(railRect)

            val isOverlapping =
                Rect.intersects(miniRect, railRect)

            // 🎯 Only move mini IF overlap happens
            if (isOverlapping) {
                moveMiniAwayFromRail(railRect)
            }
        }
    }

    private fun findRailViewByIndex(index: Int): View? {
        val fragment =
            supportFragmentManager.findFragmentById(R.id.container)
                    as? HomeFragment ?: return null

        return fragment.getRailViewAt(index)
    }

    private fun moveMiniAwayFromRail(railRect: Rect) {
        val params = miniContainer.layoutParams as FrameLayout.LayoutParams

        val screenMidY = resources.displayMetrics.heightPixels / 2

        // If rail is in top half → move mini to bottom
        if (railRect.centerY() < screenMidY) {
            params.gravity = Gravity.BOTTOM or Gravity.END
        } else {
            params.gravity = Gravity.TOP or Gravity.END
        }

        miniContainer.layoutParams = params
    }


    override fun dispatchKeyEvent(event: KeyEvent): Boolean {

        // 🔐 Block KEY_UP caused by long-press focus shift
        if (
            blockNextCenterKeyUp &&
            event.action == KeyEvent.ACTION_UP &&
            (event.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                    event.keyCode == KeyEvent.KEYCODE_ENTER)
        ) {
            blockNextCenterKeyUp = false
            return true
        }

        // 🎬 MINI PLAYER CLICK → FULLSCREEN
        if (
            event.action == KeyEvent.ACTION_UP &&
            (event.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                    event.keyCode == KeyEvent.KEYCODE_ENTER) &&
            miniContainer.isFocused()
        ) {
            launchFullscreenSmooth()
            return true
        }

        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {

                KeyEvent.KEYCODE_MENU -> {
                    drawer.openDrawer(GravityCompat.START)
                    navRecycler.requestFocus()
                    return true
                }

                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    if (drawer.isDrawerOpen(GravityCompat.START)) {
                        drawer.closeDrawer(GravityCompat.START)
                        return true
                    }
                }
            }
        }

        return super.dispatchKeyEvent(event)
    }



    fun openDrawerFromRails() {
        drawer.openDrawer(GravityCompat.START)
        navRecycler.requestFocus()
    }

    fun focusMiniPlayer() {
        blockNextCenterKeyUp = true
        miniContainer.requestFocus()
    }




    fun isMiniPlayerVisible(): Boolean =
        miniContainer.visibility == View.VISIBLE

    private fun resetMiniSize() {
        miniContainer.layoutParams.apply {
            width = dpToPx(MINI_WIDTH_DP)
            height = dpToPx(MINI_HEIGHT_DP)
        }
    }



    private fun moveMiniPlayer(position: MiniPosition) {
        miniContainer.animate().cancel()

        val margin = dpToPx(24)

        val targetY = when (position) {
            MiniPosition.BOTTOM_RIGHT -> {
                (window.decorView.height - miniContainer.height - margin).toFloat()
            }
            MiniPosition.TOP_RIGHT -> {
                margin.toFloat()
            }
        }

        miniContainer.animate()
            .y(targetY)
            .setDuration(220)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()
    }



    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()


}
