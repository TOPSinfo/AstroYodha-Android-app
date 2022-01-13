package com.astroyodha.ui.user.activity

import android.os.Bundle
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityImageViewBinding
import com.astroyodha.utils.loadImageWithoutCenterCrop

class ImageViewActivity : BaseActivity() {
    private lateinit var binding: ActivityImageViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    private fun initUI() {
        binding.imgView.loadImageWithoutCenterCrop(intent.getStringExtra("ImageUrl"))
        binding.imgBack.setOnClickListener {
            finish()
        }
    }

}