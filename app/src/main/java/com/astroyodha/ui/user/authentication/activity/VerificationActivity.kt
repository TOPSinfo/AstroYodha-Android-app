package com.astroyodha.ui.user.authentication.activity

import `in`.aabhasjindal.otptextview.OTPListener
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import com.astroyodha.R
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityVerificationBinding
import com.astroyodha.network.NetworkHelper
import com.astroyodha.network.Status
import com.astroyodha.ui.user.activity.UserHomeActivity
import com.astroyodha.ui.user.authentication.model.user.UserModel
import com.astroyodha.ui.user.authentication.navigator.VerificationNavigator
import com.astroyodha.ui.user.authentication.viewmodel.VerificationViewModel
import com.astroyodha.utils.Constants
import com.astroyodha.utils.Utility
import com.astroyodha.utils.hideKeyboard
import com.astroyodha.utils.showSnackBarToast
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class VerificationActivity : BaseActivity(), VerificationNavigator {
    val TAG = javaClass.simpleName

    lateinit var binding: ActivityVerificationBinding
    private val viewModel: VerificationViewModel by viewModels()
    private var timer: CountDownTimer? = null
    private var phoneNumber: String? = null
    private var userModel: UserModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setObserver()
        setClickListener()
        startPhoneNumberVerification()
    }

    /**
     * initialize view
     */
    private fun init() {
        viewModel.verificationNavigator = this
        phoneNumber = intent.getStringExtra(Constants.INTENT_MOBILE)

        if (intent.hasExtra(Constants.INTENT_USER_DATA)) {
            userModel = intent.getSerializableExtra(Constants.INTENT_USER_DATA) as UserModel?
            if (userModel != null && !userModel!!.socialId.equals("")) {
                viewModel.isSocialLogin = true
            }
        }

        var phoneNumberWithMasking=phoneNumber!!.replace(phoneNumber!!.substring(5,phoneNumber!!.length-2),"******")

        binding.txtOtpSentTo.setText(String.format(getString(R.string.enter_otp_code_sent_to),phoneNumberWithMasking))

    }

    /**
     * Initialize click listener
     */
    private fun setClickListener() {

        binding.btnVerify.setOnClickListener {
            signInWithPhoneAuth()
        }

        binding.btnResendOTP.setOnClickListener {
            viewModel.resendOTP()
        }

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.otpView.otpListener = object : OTPListener {
            override fun onInteractionListener() {
                // fired when user types something in the Otpbox
            }

            override fun onOTPComplete(otp: String) {
                // fired when user has entered the OTP fully.
                hideKeyboard()
                viewModel.OTPCode.postValue(binding.otpView.otp.toString().trim())
            }
        }
    }

    /**
     * set up observer
     */
    private fun setObserver() {
        viewModel.isOTPFilled.observe(this@VerificationActivity) {
            if (it) {
                binding.otpView.setOTP(viewModel.OTPCode.value.toString())
            }
        }

        viewModel.isStopTimer.observe(this@VerificationActivity) {
            if (it) {
                timer?.cancel()
                enableResendOTPView()
            }
        }

        viewModel.userDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        redirectToDashboard(userModel!!)
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
    }

    /**
     * Shwoing progress dialog
     */
    override fun onStarted() {
        showProgress(this@VerificationActivity)
    }


    /**
     * After verify OTP redirecting to profile activity
     */
    override fun onSuccess() {
        hideDialog()
        if(userModel==null) {
            showMessage(getString(R.string.user_not_registered))
            val intent = Intent(this@VerificationActivity, WelcomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            timer?.cancel()
            finish()
        }
        else{
            userModel!!.uid= Firebase.auth.currentUser?.uid
            viewModel.addUserData(userModel!!)
        }
    }

    /**
     * After verify OTP redirecting to dashboard activity if user already added info
     */
    override fun redirectToDashboard(user: UserModel) {
        viewModel.isStopTimer.value = true
        Log.e("===============","${user.type}")
        if (user.type == Constants.USER_NORMAL) {
            startActivity(Intent(this, UserHomeActivity::class.java))
            finishAffinity()
        }
        timer?.cancel()
    }

    /**
     * Sending OTP To entered mobile number
     */
    override fun startPhoneNumberVerification() {
        if (NetworkHelper(this).isNetworkConnected()) {

            val options = phoneNumber?.let {
                PhoneAuthOptions.newBuilder()
                    .setPhoneNumber(it)       // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(this)                 // Activity (for callback binding)
                    .setCallbacks(viewModel.callbacks)          // OnVerificationStateChangedCallbacks
                    .build()
            }
            if (options != null) {
                PhoneAuthProvider.verifyPhoneNumber(options)
                showProgress(this@VerificationActivity)
            }

        } else
            binding.root.showSnackBarToast(getString(R.string.please_check_internet))
    }


    /**
     * Hiding progress dialog
     */
    override fun hideDialog() {
        hideProgress()
    }

    /**
     * Showing progress dialog
     */
    override fun showDialog() {
        showProgress(this@VerificationActivity)
    }


    /**
     * Enabling resend button
     */
    override fun enableResendOTPView() {
        binding.txtResend.text = getString(R.string.resend_otp_available)
        binding.btnResendOTP.visibility=View.VISIBLE
    }

    /**
     * Disabling resend button
     */
    private fun disableResendOTPView() {
        binding.btnResendOTP.visibility=View.GONE
    }

    /**
     * Starting timer for resend button enable and disable
     */
    override fun startCountdownTimer() {
        disableResendOTPView()
        timer?.cancel()
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                viewModel.isExpire = false

                val totalSecs = millisUntilFinished / 1000
                val minutes = (totalSecs % 3600) / 60
                val seconds = totalSecs % 60;

                var resendOTPString=String.format(getString(R.string.resend_code),Utility.twoDigitString(seconds.toInt()))
                binding.txtResend.text = resendOTPString
            }

            override fun onFinish() {
                viewModel.isExpire = true
                enableResendOTPView()
            }
        }
        (timer as CountDownTimer).start()
    }

    /**
     * Showing toast message
     */
    override fun showMessage(message: String) {
        binding.root.showSnackBarToast(message)
    }

    /**
     * Showing toast failure message
     */
    override fun onFailure(message: String) {
        hideProgress()
        binding.root.showSnackBarToast(message)
    }

    /**
     * Showing firebase link message
     */
    override fun showLinkErrormsg() {
        binding.root.showSnackBarToast(Constants.VALIDATION_ERROR)
    }

    /**
     * Verifying user entered otp
     */
    override fun signInWithPhoneAuth() {
        if (Utility.checkConnection(this@VerificationActivity)) {
            if (viewModel.checkValidation()) {
                viewModel.verifyPhoneNumberWithCode()
            }
        } else
            showMessage(getString(R.string.please_check_internet))
    }

    /**
     * Resending otp to entered mobile number
     */
    override fun resendVerificationCode(resentToken: PhoneAuthProvider.ForceResendingToken) {
        if (Utility.checkConnection(this@VerificationActivity)) {
            val options = phoneNumber?.let {
                PhoneAuthOptions.newBuilder()
                    .setPhoneNumber(it)       // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(this)                 // Activity (for callback binding)
                    .setCallbacks(viewModel.callbacks)          // OnVerificationStateChangedCallbacks
                    .setForceResendingToken(resentToken)
                    .build()
            }
            if (options != null) {
                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        } else
            showMessage(getString(R.string.please_check_internet))
    }

    /**
     * On back button cancelling timer
     */
    override fun onBackPressed() {
        timer?.cancel()
        super.onBackPressed()
    }
}