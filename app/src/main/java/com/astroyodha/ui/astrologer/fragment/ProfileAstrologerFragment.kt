package com.astroyodha.ui.astrologer.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.viewModels
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.astroyodha.R
import com.astroyodha.core.BaseFragment
import com.astroyodha.databinding.FragmentAstrologerProfileBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.activity.AstrologerEditProfileActivity
import com.astroyodha.ui.astrologer.activity.AstrologerFAQActivity
import com.astroyodha.ui.astrologer.activity.AstrologerTransactionActivity
import com.astroyodha.ui.astrologer.model.user.AstrologerUserModel
import com.astroyodha.ui.astrologer.viewmodel.AstrologerBookingViewModel
import com.astroyodha.ui.astrologer.viewmodel.ProfileAstrologerViewModel
import com.astroyodha.ui.user.authentication.activity.WelcomeActivity
import com.astroyodha.utils.*

class ProfileAstrologerFragment : BaseFragment() {
    private val TAG = "ProfileAstrologerFragment"
    val RC_UPDATE_PROFILE: Int = 100
    private val profileViewModel: ProfileAstrologerViewModel by viewModels()
    private val bookingViewModel: AstrologerBookingViewModel by viewModels()
    private lateinit var binding: FragmentAstrologerProfileBinding

    var userModel: AstrologerUserModel? = null
    var profileImagePath: Uri? = null

    lateinit var mContext: Context

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                //  you will get result here in result.data
                profileViewModel.getUserDetail(FirebaseAuth.getInstance().currentUser?.uid.toString())
            }
        }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAstrologerProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setObserver()
        clickListener()
    }

    /**
     * initialize view
     */
    private fun init() {
        profileViewModel.getUserDetail(FirebaseAuth.getInstance().currentUser?.uid.toString())

    }

    /**
     * manage click listener of view
     */
    private fun clickListener() {

        binding.imgEdit.setOnClickListener {

            startForResult.launch(
                Intent(mContext, AstrologerEditProfileActivity::class.java)
            )
        }

        binding.txtLogout.setOnClickListener {
            Firebase.auth.signOut()
            if (userModel != null) {
                if (userModel!!.socialType.equals(Constants.SOCIAL_TYPE_FACEBOOK)) {
                    LoginManager.getInstance().logOut();
                } else if (userModel!!.socialType.equals(Constants.SOCIAL_TYPE_GOOGLE)) {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.web_client_id))
                        .requestEmail()
                        .build()

                    val googleSignInClient = GoogleSignIn.getClient(context!!, gso)
                    googleSignInClient.signOut()
                }
            }
            context?.toast(getString(R.string.logout_successfully))
            //            pref.clearAllPref(context!!, Constants.PREF_FILE) // do not clear scm token will clear and it will set "" in after login
            NotificationManagerCompat.from(context!!).cancelAll() // clear all notification on logout
            startActivity(Intent(context, WelcomeActivity::class.java))
            activity!!.finish()
        }

        binding.txtRateApp.setOnClickListener {
            mContext.openSocialMedia(
                mContext.getString(
                    R.string.rating_package_name,
                    mContext.packageName
                )
            )
        }

        binding.txtShareApp.setOnClickListener {
            mContext.shareApp(
                mContext.getString(
                    R.string.share_app_message,
                    getString(R.string.app_name),
                    mContext.packageName
                )
            )
        }

        binding.txtTransactionHistory.setOnClickListener {
            startActivity(Intent(mContext, AstrologerTransactionActivity::class.java))
        }

        binding.txtHelp.setOnClickListener {
            startActivity(Intent(mContext, AstrologerFAQActivity::class.java))
        }

    }

    /**
     * set observer
     */
    private fun setObserver() {

        profileViewModel.userDetailResponse.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(requireContext())
                }
                Status.SUCCESS -> {
                    it.data?.let {
                        userModel = it
                        if (userModel != null) {
                            bookingViewModel.getAllCompletedBooking(userModel!!.uid!!)
                            setUserData()
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })


        bookingViewModel.completedBookingResponse.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.LOADING -> {
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        binding.txtConsults.setText(it)
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
     * set data to view
     */
    private fun setUserData() {
        userModel!!.fullName.let {
            binding.txtUserName.setText(it)
        }

        userModel!!.rating.let {
            binding.txtRating.setText(it.toString())
        }

        userModel!!.birthDate.let {
            binding.txtBirthDate.setText(it)
        }


        userModel!!.profileImage.let {
            binding.imgUser.loadProfileImage(it.toString())
        }
    }

    /**
     * Checking image picker and cropper result after image selection
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_UPDATE_PROFILE) {
            if (resultCode == Activity.RESULT_OK) {
                profileViewModel.getUserDetail(FirebaseAuth.getInstance().currentUser?.uid.toString())
            }
        }

    }


}