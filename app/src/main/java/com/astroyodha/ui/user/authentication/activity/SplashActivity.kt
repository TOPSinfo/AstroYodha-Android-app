package com.astroyodha.ui.user.authentication.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivitySplashBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.activity.AstrologerDashboardActivity
import com.astroyodha.ui.user.activity.UserHomeActivity
import com.astroyodha.ui.user.authentication.viewmodel.SplashViewModel
import com.astroyodha.utils.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity() {
    val TAG = javaClass.simpleName

    lateinit var binding: ActivitySplashBinding
    private var SPLASH_TIME_OUT = 3000L

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        throw RuntimeException("Test Crash") // Force a crash
        init()
        setObserver()
    }

    /**
     * initialize view
     */
    private fun init() {
        if (Utility.checkConnection(this)) {
            val db = FirebaseFirestore.getInstance()
            getFirebaseToken(pref)
        } else {
            toast(Constants.MSG_NO_INTERNET_CONNECTION)
        }

        if (!intent.hasExtra(Constants.INTENT_SHOW_TIMER)) {
            lifecycleScope.launch {
                delay(2000L)
                redirectDashBoardActivity()
            }
        } else {
            redirectDashBoardActivity()
        }
    }

    /**
     * Initialize observer
     */
    private fun setObserver() {

        viewModel.userDataResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { user ->
                        if (!user.uid.isNullOrBlank()) {
                            if (user.type == Constants.USER_NORMAL) {
                                startActivity(Intent(this, UserHomeActivity::class.java))
                                finish()
                            } else if (user.type == Constants.USER_ASTROLOGER) {
                                startActivity(Intent(this, AstrologerDashboardActivity::class.java))
                                finish()
                            }
                        } else {
                            startActivity(Intent(this, WelcomeActivity::class.java))
                            finish()
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })
    }

    /**
     * Checking user login or not and redirecting according to screen
     */
    private fun redirectDashBoardActivity() {

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        } else {
            /**
             * Checking that user is login but user profile info add or not
             */
            viewModel.checkUserRegisterOrNot(FirebaseAuth.getInstance().currentUser?.uid.toString())
        }
    }

}