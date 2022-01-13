package com.astroyodha.ui.user.activity

import android.os.Bundle
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityComingSoonBinding

class ComingSoonActivity : BaseActivity() {
    private val TAG = javaClass.simpleName
    private lateinit var binding: ActivityComingSoonBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComingSoonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setClickListener()
    }

    /**
     * manage click listener of view
     */
    private fun setClickListener() {
        binding.imgClose.setOnClickListener {
            onBackPressed()
        }
    }
}