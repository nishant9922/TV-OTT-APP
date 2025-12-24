package com.example.tvtvapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.example.tvtvapp.data.VideoRepository
import com.example.tvtvapp.player.PlayerManager
import com.example.tvtvapp.player.PlayerMode
import com.logituit.logixsdk.logixplayer.player.LogixMediaItem

@UnstableApi
class DetailActivity : AppCompatActivity() {

    private lateinit var image: ImageView
    private lateinit var titleTxt: TextView
    private lateinit var yearTxt: TextView
    private lateinit var genreTxt: TextView
    private lateinit var ratingTxt: TextView
    private lateinit var plotTxt: TextView
    private lateinit var btnWatchNow: Button
    private lateinit var progress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        image = findViewById(R.id.detailImage)
        titleTxt = findViewById(R.id.detailTitle)
        yearTxt = findViewById(R.id.detailYear)
        genreTxt = findViewById(R.id.detailGenre)
        ratingTxt = findViewById(R.id.detailRating)
        plotTxt = findViewById(R.id.detailPlot)
        btnWatchNow = findViewById(R.id.btnWatchNow)
        progress = findViewById(R.id.detailProgress)

        val videoId = intent.getStringExtra("VIDEO_ID") ?: return

        val repo = VideoRepository()
        repo.fetchVideos(
            onSuccess = { list ->
                val video = list.firstOrNull { it.id == videoId } ?: return@fetchVideos

                titleTxt.text = video.title
                yearTxt.text = "Uploaded: ${video.uploadTime}"
                genreTxt.text = "Author: ${video.author}"
                ratingTxt.text = "Views: ${video.views}"
                plotTxt.text = video.description

                Glide.with(this)
                    .load(video.thumbnailUrl)
                    .into(image)

                btnWatchNow.setOnClickListener {

                    val media = LogixMediaItem(
                        id = video.id,
                        title = video.title,
                        sourceUrl = video.videoUrl,
                        drmScheme = LogixMediaItem.ContentType.CLEAR,
                        streamType = LogixMediaItem.StreamType.VOD
                    )

                    // 🔥 1. INIT PLAYER
                    PlayerManager.init(this)

                    // 🔥 2. SET MEDIA (THIS FIXES "NO ACTIVE MEDIA")
                    PlayerManager.setMedia(media)

                    // 🔥 3. SWITCH MODE
                    PlayerManager.setMode(PlayerMode.FULL)

                    // 🔥 4. OPEN PLAYER
                    startActivity(
                        Intent(this, LogixPlayerActivity::class.java)
                    )
                    finish()
                }


            },
            onError = {
                Toast.makeText(this, "Failed to load video", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
