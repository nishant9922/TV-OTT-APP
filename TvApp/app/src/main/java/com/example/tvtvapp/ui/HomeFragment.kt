package com.example.tvtvapp.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.tvtvapp.DetailActivity
import com.example.tvtvapp.MainActivity
import com.example.tvtvapp.R
import com.example.tvtvapp.data.*
import com.example.tvtvapp.ui.adapters.BannerAdapter
import com.example.tvtvapp.ui.adapters.RowAdapter
import kotlin.math.abs

class HomeFragment : Fragment() {

    private lateinit var bannerPager: ViewPager2
    private lateinit var exploreButton: Button
    private lateinit var railsRecycler: RecyclerView
    private lateinit var indicatorContainer: LinearLayout

    private val handler = Handler(Looper.getMainLooper())
    private val allRails = mutableListOf<Row>()

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            val adapter = bannerPager.adapter ?: return
            if (adapter.itemCount > 1) {
                bannerPager.currentItem =
                    (bannerPager.currentItem + 1) % adapter.itemCount
                handler.postDelayed(this, 5000)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        bannerPager = root.findViewById(R.id.bannerPager)
        exploreButton = root.findViewById(R.id.btnExplore)
        railsRecycler = root.findViewById(R.id.railsRecycler)
        indicatorContainer = root.findViewById(R.id.indicatorContainer)

        railsRecycler.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        loadVideoRails()
        setupExploreClick()
        handleBackPress()
        handleFocusKeys(root)

        bannerPager.requestFocus()
        return root
    }

    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = activity as? MainActivity ?: return

        railsRecycler.viewTreeObserver.addOnGlobalFocusChangeListener { _, newFocus ->

            // 1️⃣ Ignore null focus
            if (newFocus == null) return@addOnGlobalFocusChangeListener

            // 2️⃣ Only react when focus is INSIDE railsRecycler
            if (!isViewInside(railsRecycler, newFocus)) return@addOnGlobalFocusChangeListener

            // 3️⃣ Find rail item safely
            val railItemView = railsRecycler.findContainingItemView(newFocus)
                ?: return@addOnGlobalFocusChangeListener

            // 4️⃣ Get adapter index
            val railIndex = railsRecycler.getChildAdapterPosition(railItemView)
            if (railIndex == RecyclerView.NO_POSITION) return@addOnGlobalFocusChangeListener

            // 🎯 NETFLIX BEHAVIOR
            // 0th rail  → mini bottom-right
            // >=1 rail → mini top-right
            mainActivity.updateMiniPlayerForRailIndex(railIndex)
        }
    }





    // -------------------------------
    // EXPLORE
    // -------------------------------
    private fun setupExploreClick() {
        exploreButton.setOnClickListener {
            bannerPager.visibility = View.GONE
            exploreButton.visibility = View.GONE
            indicatorContainer.visibility = View.GONE
            railsRecycler.visibility = View.VISIBLE
            railsRecycler.requestFocus()
        }
    }

    // -------------------------------
    // DATA
    // -------------------------------
    @OptIn(UnstableApi::class)
    private fun loadVideoRails() {
        val repo = VideoRepository()

        repo.fetchVideos(
            onSuccess = { videoList ->
                if (videoList.isEmpty()) return@fetchVideos

                val cards = videoList.map {
                    Card(
                        imdbId = it.id,
                        title = it.title,
                        imageUrl = it.thumbnailUrl,
                        category = "Featured"
                    )
                }

                setupBanner(cards.take(5))

                allRails.clear()
                val titles = listOf(
                    "Trending Now",
                    "Popular on TVApp",
                    "Recommended For You",
                    "New Releases",
                    "Watch Again"
                )

                titles.forEachIndexed { index, title ->
                    allRails.add(
                        Row(
                            index + 1,
                            title,
                            cards.shuffled()
                        )
                    )
                }

                railsRecycler.adapter = RowAdapter(allRails) { card ->
                    startActivity(
                        Intent(requireContext(), DetailActivity::class.java)
                            .putExtra("VIDEO_ID", card.imdbId)
                    )
                }
            },
            onError = {
                Log.e("VIDEO_API", "Failed to load videos")
            }
        )
    }

    // -------------------------------
    // BANNER
    // -------------------------------
    private fun setupBanner(cards: List<Card>) {
        val banners = cards.map {
            Banner(it.imdbId, it.title, "", it.imageUrl)
        }

        bannerPager.adapter = BannerAdapter(banners)

        bannerPager.setPageTransformer { page, position ->
            page.alpha = 1 - abs(position * 0.3f)
            page.translationX = -position * 40
        }

        setupIndicators(banners)
        handler.postDelayed(autoScrollRunnable, 5000)
    }

    private fun setupIndicators(banners: List<Banner>) {
        indicatorContainer.removeAllViews()

        repeat(banners.size) { i ->
            indicatorContainer.addView(
                ImageView(requireContext()).apply {
                    val size = (10 * resources.displayMetrics.density).toInt()
                    layoutParams = LinearLayout.LayoutParams(size, size).apply {
                        setMargins(6, 0, 6, 0)
                    }
                    setImageResource(
                        if (i == 0)
                            android.R.drawable.presence_online
                        else
                            android.R.drawable.presence_invisible
                    )
                }
            )
        }

        bannerPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    for (i in 0 until indicatorContainer.childCount) {
                        (indicatorContainer.getChildAt(i) as ImageView)
                            .setImageResource(
                                if (i == position)
                                    android.R.drawable.presence_online
                                else
                                    android.R.drawable.presence_invisible
                            )
                    }
                }
            }
        )
    }

    // -------------------------------
    // BACK
    // -------------------------------
    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (railsRecycler.visibility == View.VISIBLE) {
                        railsRecycler.visibility = View.GONE
                        bannerPager.visibility = View.VISIBLE
                        exploreButton.visibility = View.VISIBLE
                        indicatorContainer.visibility = View.VISIBLE
                        bannerPager.requestFocus()
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            }
        )
    }

    // -------------------------------
    // DPAD
    // -------------------------------
    private fun handleFocusKeys(root: View) {
        root.isFocusableInTouchMode = true
        root.requestFocus()

        root.setOnKeyListener { _, keyCode, event ->
            if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false

            when {
                bannerPager.hasFocus() -> when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        bannerPager.currentItem =
                            (bannerPager.currentItem + 1) %
                                    (bannerPager.adapter?.itemCount ?: 1)
                        true
                    }

                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        val count = bannerPager.adapter?.itemCount ?: 1
                        bannerPager.currentItem =
                            if (bannerPager.currentItem - 1 < 0)
                                count - 1
                            else
                                bannerPager.currentItem - 1
                        true
                    }

                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        exploreButton.requestFocus()
                        true
                    }

                    else -> false
                }

                exploreButton.hasFocus() && keyCode == KeyEvent.KEYCODE_DPAD_DOWN -> {
                    railsRecycler.requestFocus()
                    true
                }

                else -> false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(autoScrollRunnable)
    }

    private fun isViewInside(parent: View, child: View): Boolean {
        var current: View? = child
        while (current != null) {
            if (current == parent) return true
            val p = current.parent
            current = if (p is View) p else null
        }
        return false
    }
    fun getRailViewAt(index: Int): View? {
        if (!::railsRecycler.isInitialized) return null
        return railsRecycler.layoutManager?.findViewByPosition(index)
    }


}
