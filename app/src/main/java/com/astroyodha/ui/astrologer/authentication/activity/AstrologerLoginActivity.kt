package com.astroyodha.ui.astrologer.authentication.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.astroyodha.R
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityAstrologerLoginBinding
import com.astroyodha.network.NetworkHelper
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.activity.AstrologerDashboardActivity
import com.astroyodha.ui.user.authentication.viewmodel.SplashViewModel
import com.astroyodha.utils.Constants
import com.astroyodha.utils.MyLog
import com.astroyodha.utils.showSnackBarToast
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AstrologerLoginActivity : BaseActivity() {
    val TAG = javaClass.simpleName

    private lateinit var binding: ActivityAstrologerLoginBinding
    private val viewModel: SplashViewModel by viewModels()

    lateinit var callbackManager: CallbackManager
    private lateinit var auth: FirebaseAuth

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var gso: GoogleSignInOptions
    private val RC_GOOGLE_SIGN_IN: Int = 100

    var name: String = ""
    var email: String = ""
    var socialId: String = ""
    var logintype: String = ""
    var userType:String=""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(this)
        binding = ActivityAstrologerLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchCountryCode()
        init()
        setClickListener()
        setObserver()
    }

    /**
     * initialize view
     */
    private fun init() {
        intent.getStringExtra(Constants.INTENT_USER_TYPE).let {
            userType=it.toString()
        }

        auth = Firebase.auth
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        initFacebookLogin()
        binding.countryPicker.registerPhoneNumberTextView(binding.edPhoneNumber)
        binding.countryPicker.setOnCountryChangeListener {
            binding.edPhoneNumber.setText("")
        }

    }

    /**
     * initialize facebook login
     */
    fun initFacebookLogin() {
        callbackManager = CallbackManager.Factory.create()
        binding.facebookLoginButton.setReadPermissions("email", "public_profile")
        binding.facebookLoginButton.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
            }

            override fun onError(error: FacebookException) {
            }
        })
    }

    /**
     * initialize google login
     */
    private fun googleSignIn() {
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
    }


    /**
     * Fetching user country code and set to country picker
     */
    private fun fetchCountryCode() {
        val tm =
            this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val countryCodeValue = tm.networkCountryIso

        if (countryCodeValue != null)
            binding.countryPicker.setCountryForNameCode(countryCodeValue)
    }

    /**
     * manage click listener of view
     */
    private fun setClickListener() {
        binding.btnLogin.setOnClickListener {
            if (NetworkHelper(this).isNetworkConnected()) {
                if (checkValidation()) {
                    viewModel.checkMobieNuberRegisterdOrNotWithUserType(
                        "+" + binding.countryPicker.selectedCountryCode + binding.edPhoneNumber.text.toString()
                            .trim()
                    ,userType)
                }
            } else {
                binding.root.showSnackBarToast(getString(R.string.please_check_internet))
            }
        }

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.edPhoneNumber.setOnFocusChangeListener { view, b ->
            changeLayoutOnFocusChange(b)

        }
        binding.countryPicker.setOnFocusChangeListener { view, b ->
            changeLayoutOnFocusChange(b)
        }

        binding.tvSignup.setOnClickListener {
            startActivity(
                Intent(this, AstrologerRegistrationActivity::class.java)
                    .putExtra(Constants.INTENT_NAME, name)
                    .putExtra(Constants.INTENT_EMAIL, email)
                    .putExtra(Constants.INTENT_SOCIAL_ID, socialId)
                    .putExtra(Constants.INTENT_SOCIAL_TYPE, logintype)
                    .putExtra(Constants.INTENT_USER_TYPE, userType)
            )
        }

        binding.layouLoginWithFacebook.setOnClickListener {
            binding.facebookLoginButton.performClick()

        }

        binding.layouLoginWithGoogle.setOnClickListener {
            googleSignIn()
        }
    }

    /**
     * manage edittext focus
     */
    fun changeLayoutOnFocusChange(b: Boolean) {
        if (b) {
            binding.imgMobile.setColorFilter(
                ContextCompat.getColor(this, R.color.orange_theme),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            binding.lnMobile.setBackgroundResource(R.drawable.background_edit_text_orange_line_background)
            binding.edPhoneNumber.setTextColor(ContextCompat.getColor(this, R.color.orange_theme))
            binding.countryPicker.textColor = ContextCompat.getColor(this, R.color.orange_theme)
        } else {
            binding.imgMobile.setColorFilter(
                ContextCompat.getColor(this, R.color.text_gray),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            binding.lnMobile.setBackgroundResource(R.drawable.background_edit_text_grey_line_background)
            binding.edPhoneNumber.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.countryPicker.textColor = ContextCompat.getColor(this, R.color.black)
        }
    }

    /**
     * Checking mobile number validation
     */
    private fun checkValidation(): Boolean {
        if (!binding.countryPicker.isValid) {
            binding.root.showSnackBarToast(Constants.VALIDATION_MOBILE_NUMBER)
            return false
        }
        return true
    }

    /**
     * Initialize observer
     */
    private fun setObserver() {

        viewModel.mobileValidationCheckWithUserType.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        if (it.equals(Constants.MSG_MOBILE_NUMBER_REGISTERD)) {
                            redirectVerificationActivity()
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })


        viewModel.socialLoginCheckWithUserType.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        if (it) {
                            redirectDashboard()
                        } else {
                            viewModel.checkUserWithSocialMediaWithoutUserType(socialId)
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })



        viewModel.socialLoginCheckWithoutUserType.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        if (it) {
                            if(!logintype.equals("")) {
                                logoutSocialMedia()
                            }
                            binding.root.showSnackBarToast(Constants.MSG_SOCIAL_MEDIA_ALREADY_REGISTER)
                        } else {
                            redirectCreateAccountActivity(name, email, socialId)
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
     * This function logout social media account
     */
    fun logoutSocialMedia()
    {
        Firebase.auth.signOut()
        if(logintype.equals(Constants.SOCIAL_TYPE_FACEBOOK))
        {
            LoginManager.getInstance().logOut();
        }
        else if(logintype.equals(Constants.SOCIAL_TYPE_GOOGLE))
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
     * Redirecting to phone number verification activity
     */
    private fun redirectVerificationActivity() {
        startActivity(
            Intent(this, AstrologerVerificationActivity::class.java).putExtra(
                Constants.INTENT_MOBILE,
                "+" + binding.countryPicker.selectedCountryCode + binding.edPhoneNumber.text.toString()
                    .trim()
            )
        )
    }

    /**
     * manage facebook token
     */
    private fun handleFacebookAccessToken(token: AccessToken) {

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    name = user!!.displayName!!
                    email = if (user.email.isNullOrBlank()) "" else user.email.toString()
                    socialId = user.uid
                    logintype = Constants.SOCIAL_TYPE_FACEBOOK
                    viewModel.checkUserWithSocialMediaWithUserType(socialId,userType)
                } else {
                    hideProgress()
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    /**
     * manage google login
     */
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser

                    name = user!!.displayName!!
                    email = user.email!!
                    socialId = user.uid
                    logintype = Constants.SOCIAL_TYPE_GOOGLE
                    viewModel.checkUserWithSocialMediaWithUserType(socialId,userType)
                } else {
                    hideProgress()
                }
            }
    }

    private fun redirectCreateAccountActivity(name: String, email: String, socialId: String) {
        hideProgress()
        startActivity(
            Intent(this, AstrologerRegistrationActivity::class.java)
                .putExtra(Constants.INTENT_NAME, name)
                .putExtra(Constants.INTENT_EMAIL, email)
                .putExtra(Constants.INTENT_SOCIAL_ID, socialId)
                .putExtra(Constants.INTENT_SOCIAL_TYPE, logintype)
                .putExtra(Constants.INTENT_USER_TYPE, userType)
        )

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        hideProgress()
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                showProgress(this)
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                hideProgress()
                // ...
            }
        } else {
            // Pass the activity result back to the Facebook SDK
            showProgress(this)
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * redirect to astrologer dashboard
     */
    fun redirectDashboard() {
        startActivity(Intent(this, AstrologerDashboardActivity::class.java))
        finishAffinity()
    }
}