package com.astroyodha.ui.astrologer.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.astroyodha.R
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityAstrologerDashboardBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.model.user.AstrologerUserModel
import com.astroyodha.ui.astrologer.viewmodel.ProfileAstrologerViewModel
import com.astroyodha.utils.Constants
import com.astroyodha.utils.MyLog
import com.astroyodha.utils.showSnackBarToast
import com.google.firebase.auth.FirebaseAuth
import com.simform.custombottomnavigation.Model

class AstrologerDashboardActivity : BaseActivity() {
    private lateinit var binding: ActivityAstrologerDashboardBinding
    lateinit var navController: NavController

    var userModel: AstrologerUserModel? = null

    private val profileViewModel: ProfileAstrologerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAstrologerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

    }

    /**
     * initialize view
     */
    private fun init() {
        val fb = FirebaseAuth.getInstance().currentUser
        var userId = fb?.uid.toString()
        setObserver()
        profileViewModel.updateUserToken(userId,pref.getValue(this,Constants.PREF_FCM_TOKEN,"").toString())
        profileViewModel.getLanguageList()
        profileViewModel.getSpeciality()

        navController = findNavController(R.id.nav_host_fragment_astrologer)

        val menuItems = arrayOf(
            Model(
                icon = R.drawable.ic_calender,
                destinationId = R.id.nav_booking_astrologer,
                id = 0,
                text = R.string.title_bookings
            ),
            Model(
                R.drawable.ic_profile,
                R.id.nav_profile_astrologer,
                1,
                R.string.title_profile
            )
        )

        binding.bottomNavigation.apply {
            setMenuItems(menuItems, intent.getIntExtra(Constants.INTENT_INDEX, 0))
            setupWithNavController(navController)
        }

    }

    /**
     * set observer
     */
    private fun setObserver() {
        profileViewModel.languageAndSpecialityListResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { resultList ->
                        Constants.listOfLanguages.clear()
                        Constants.listOfLanguages.addAll(resultList)
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })

        profileViewModel.specialityResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { resultList ->
                        Constants.listOfSpeciality.clear()
                        Constants.listOfSpeciality.addAll(resultList)
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
     * manage new intent
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        startActivity(
            Intent(this, AstrologerDashboardActivity::class.java)
        )
        finishAffinity()
    }


}