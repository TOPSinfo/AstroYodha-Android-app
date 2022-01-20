package com.astroyodha.ui.user.authentication.activity

import android.content.Intent
import android.os.Bundle
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityWelcomeBinding
import com.astroyodha.ui.astrologer.authentication.activity.AstrologerLoginActivity
import com.astroyodha.utils.Constants

class WelcomeActivity : BaseActivity() {

    lateinit var binding:ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setClickListener()


    }

    /**
     * manage click listener of view
     */
    private fun setClickListener() {
        binding.btnSignupAstrologer.setOnClickListener {
            startActivity(
                Intent(this, AstrologerLoginActivity::class.java)
                    .putExtra(Constants.INTENT_USER_TYPE, Constants.USER_ASTROLOGER)
            )
        }
        binding.btnSignupUser.setOnClickListener {
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .putExtra(Constants.INTENT_USER_TYPE, Constants.USER_NORMAL)
            )
        }
    }
}