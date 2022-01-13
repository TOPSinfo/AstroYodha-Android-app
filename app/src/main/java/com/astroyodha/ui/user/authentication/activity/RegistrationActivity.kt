package com.astroyodha.ui.user.authentication.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.astroyodha.R
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityRegistrationBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.user.authentication.model.user.UserModel
import com.astroyodha.ui.user.authentication.viewmodel.SplashViewModel
import com.astroyodha.utils.Constants
import com.astroyodha.utils.Utility
import com.astroyodha.utils.showSnackBarToast


class RegistrationActivity : BaseActivity() {

    private lateinit var binding: ActivityRegistrationBinding

    private val viewModel: SplashViewModel by viewModels()

    private var name: String = ""
    private var email: String = ""
    private var socialId: String = ""
    private var loginType: String = ""
    private var userType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    /**
     * Initialize user interface
     */
    private fun initUI() {

        intent.getStringExtra(Constants.INTENT_NAME).also {
            name = it!!
        }

        intent.getStringExtra(Constants.INTENT_EMAIL).also {
            email = it!!
        }
        intent.getStringExtra(Constants.INTENT_SOCIAL_ID).also {
            socialId = it!!
        }

        intent.getStringExtra(Constants.INTENT_SOCIAL_TYPE).also {
            loginType = it!!
        }
        intent.getStringExtra(Constants.INTENT_USER_TYPE).also {
            userType = it!!
        }

        binding.edFName.setText(name)
        binding.edEmail.setText(email)
        manageFocus()
        setClickListener()
        setObserver()
    }

    /*
   * change layout borders color based on view focus
   * */

    private fun manageFocus() {

        binding.edFName.setOnFocusChangeListener { view, b ->
            if (b) {
                binding.icUser.setColorFilter(
                    ContextCompat.getColor(this, R.color.border_blue),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.layoutFullName.setBackgroundResource(R.drawable.background_edit_text_blue_line_background)
                binding.edFName.setTextColor(ContextCompat.getColor(this, R.color.border_blue))
            } else {
                binding.icUser.setColorFilter(
                    ContextCompat.getColor(this, R.color.text_gray),
                    android.graphics.PorterDuff.Mode.SRC_IN
                );
                binding.layoutFullName.setBackgroundResource(R.drawable.background_edit_text_grey_line_background)
                binding.edFName.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }

        binding.edPhoneNumber.setOnFocusChangeListener { view, b ->
            if (b) {
                binding.icMobile.setColorFilter(
                    ContextCompat.getColor(this, R.color.border_blue),
                    android.graphics.PorterDuff.Mode.SRC_IN
                );
                binding.layoutMobileNumber.setBackgroundResource(R.drawable.background_edit_text_blue_line_background)
                binding.edPhoneNumber.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.border_blue
                    )
                )
                binding.countryPicker.textColor = ContextCompat.getColor(this, R.color.border_blue)
            } else {
                binding.icMobile.setColorFilter(
                    ContextCompat.getColor(this, R.color.text_gray),
                    android.graphics.PorterDuff.Mode.SRC_IN
                );
                binding.layoutMobileNumber.setBackgroundResource(R.drawable.background_edit_text_grey_line_background)
                binding.edPhoneNumber.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.countryPicker.textColor = ContextCompat.getColor(this, R.color.black)
            }
        }

        binding.edEmail.setOnFocusChangeListener { view, b ->
            if (b) {
                binding.icEmail.setColorFilter(
                    ContextCompat.getColor(this, R.color.border_blue),
                    android.graphics.PorterDuff.Mode.SRC_IN
                );
                binding.layoutEmail.setBackgroundResource(R.drawable.background_edit_text_blue_line_background)
                binding.edEmail.setTextColor(ContextCompat.getColor(this, R.color.border_blue))
            } else {
                binding.icEmail.setColorFilter(
                    ContextCompat.getColor(this, R.color.text_gray),
                    android.graphics.PorterDuff.Mode.SRC_IN
                );
                binding.layoutEmail.setBackgroundResource(R.drawable.background_edit_text_grey_line_background)
                binding.edEmail.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }

    }


    /**
     * set observer
     */
    private fun setObserver() {

        viewModel.mobileValidationCheckWithUserType.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
                Status.ERROR -> {
                    if (it.message.equals(Constants.MSG_MOBILE_NUMBER_NOT_REGISTERD)) {

                        viewModel.checkMobieNuberRegisterdOrNotWithouUserType(
                            "+" + binding.countryPicker.selectedCountryCode + binding.edPhoneNumber.text.toString()
                                .trim())

                    }
                }
            }
        })



        viewModel.mobileValidationCheckWithoutUserType.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
                Status.ERROR -> {
                    hideProgress()
                    if (it.message.equals(Constants.MSG_MOBILE_NUMBER_NOT_REGISTERD)) {

                        val userModel = UserModel()
                        userModel.email = binding.edEmail.text.toString().trim()
                        userModel.fullName = binding.edFName.text.toString().trim()
                        userModel.phone =
                            "+" + binding.countryPicker.selectedCountryCode + binding.edPhoneNumber.text.toString()
                                .trim()
                        userModel.socialId = socialId
                        userModel.socialType = loginType
                        userModel.isOnline = false
                        redirectTOVerification(userModel)
                    }
                }
            }
        })


    }

    /**
     * Set click listener
     */
    private fun setClickListener() {

        binding.btnRegister.setOnClickListener {

            if (checkValidation()) {

                viewModel.checkMobieNuberRegisterdOrNotWithUserType(
                    "+" + binding.countryPicker.selectedCountryCode + binding.edPhoneNumber.text.toString()
                        .trim()
                    ,userType)
            }
        }
        binding.tvTermsCondition.setOnClickListener {
            startActivity(
                Intent(this, TermsAndPrivacyPolicyActivity::class.java)
                    .putExtra(Constants.INTENT_IS_FROM, Constants.TERM_AND_CONDITION_POLICY)
                    .putExtra(Constants.INTENT_USER_TYPE, userType)
            )
        }
        binding.tvPrivacyPolicy.setOnClickListener {
            startActivity(
                Intent(this, TermsAndPrivacyPolicyActivity::class.java)
                    .putExtra(Constants.INTENT_IS_FROM, Constants.PRIVACY_POLICY)
                    .putExtra(Constants.INTENT_USER_TYPE, userType)
            )
        }
        binding.tvLogin.setOnClickListener {
            if(!loginType.equals("")) {
                logoutSocialMedia()
            }
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .putExtra(Constants.INTENT_USER_TYPE, Constants.USER_NORMAL)
            )
            finish()
        }

        binding.imgBack.setOnClickListener {
            if(!loginType.equals("")) {
                logoutSocialMedia()
            }
            onBackPressed()
        }
    }


    /*
    * This function logout social media account
    * */
    fun logoutSocialMedia()
    {
        Firebase.auth.signOut()
        if(loginType.equals(Constants.SOCIAL_TYPE_FACEBOOK))
        {
            LoginManager.getInstance().logOut();
        }
        else if(loginType.equals(Constants.SOCIAL_TYPE_GOOGLE))
        {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInClient.signOut()
        }
    }


    /**
     * Checking validation
     */
    private fun checkValidation(): Boolean {

        if (TextUtils.isEmpty(binding.edFName.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_first_name))
            return false
        } else if (TextUtils.isEmpty(binding.edEmail.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_email_address))
            return false
        } else if (!Utility.emailValidator(binding.edEmail.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_valid_email_address))
            return false
        } else if (!binding.cbTerms.isChecked) {
            binding.root.showSnackBarToast(getString(R.string.please_accept_terms_and_condition))
            return false
        }
        return true
    }

    /**
     * Redirecting to dashboard
     */
    private fun redirectTOVerification(userModel: UserModel) {

        var intent = Intent(this, VerificationActivity::class.java)
        intent.putExtra(
            Constants.INTENT_MOBILE,
            "+" + binding.countryPicker.selectedCountryCode + binding.edPhoneNumber.text.toString()
                .trim()
        )
        intent.putExtra(Constants.INTENT_USER_DATA, userModel)
        startActivity(intent)
    }
}