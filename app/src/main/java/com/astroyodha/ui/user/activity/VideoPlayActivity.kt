package com.astroyodha.ui.user.activity

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityVideoPlayBinding

class VideoPlayActivity : BaseActivity() {

    private lateinit var binding: ActivityVideoPlayBinding

    var mediaController: MediaController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    /**
     * initialize view
     */
    private fun initUI() {

        binding.imgBack.setOnClickListener {
            finish()
        }

        mediaController = MediaController(this, true)

        binding.videoView.setOnPreparedListener {
            mediaController!!.show()
            binding.videoView.start()
            binding.progressBar.setVisibility(View.GONE)
        }

        binding.videoView.setVideoPath(intent.getStringExtra("VideoUrl"))

        mediaController!!.setAnchorView(binding.videoView)
        binding.videoView.setMediaController(mediaController)
        mediaController!!.setMediaPlayer(binding.videoView)
        binding.videoView.requestFocus()

        binding.videoView.setOnErrorListener { mp: MediaPlayer?, _: Int, _: Int ->
            true
        }

    }

}