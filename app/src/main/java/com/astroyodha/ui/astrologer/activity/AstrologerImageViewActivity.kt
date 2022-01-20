package com.astroyodha.ui.astrologer.activity

import android.os.Bundle
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityAstrologerImageViewBinding
import com.astroyodha.utils.loadImageWithoutCenterCrop

class AstrologerImageViewActivity : BaseActivity() {
    private lateinit var binding: ActivityAstrologerImageViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAstrologerImageViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    /**
     * initialize view
     */
    private fun initUI() {
        binding.imgView.loadImageWithoutCenterCrop(intent.getStringExtra("ImageUrl"))
        binding.imgBack.setOnClickListener {
            finish()
        }
    }

}