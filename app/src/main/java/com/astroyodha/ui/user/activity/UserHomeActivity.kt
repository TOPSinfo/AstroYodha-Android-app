package com.astroyodha.ui.user.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.astroyodha.R
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityHomeBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.user.authentication.activity.SplashActivity
import com.astroyodha.ui.user.authentication.model.user.UserModel
import com.astroyodha.ui.user.viewmodel.ProfileViewModel
import com.astroyodha.utils.Constants
import com.astroyodha.utils.showSnackBarToast
import com.google.firebase.auth.FirebaseAuth
import com.simform.custombottomnavigation.Model

class UserHomeActivity : BaseActivity() {
    private val TAG = javaClass.simpleName
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding
    lateinit var navController: NavController

    var userModel: UserModel? = null

    private val profileViewModel: ProfileViewModel by viewModels()
    var tabChange = MutableLiveData<Int>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setObserver()
        profileViewModel.getSpeciality()
        init()

    }

    /**
     * initialize view
     */
    private fun init() {

        val fb = FirebaseAuth.getInstance().currentUser
        var userId = fb?.uid.toString()
        profileViewModel.updateUserToken(userId,pref.getValue(this,Constants.PREF_FCM_TOKEN,"").toString())
        profileViewModel.getLanguageList()


        navController = findNavController(R.id.nav_host_fragment)

        val menuItems = arrayOf(
            Model(
                icon = R.drawable.ic_home,                // Icon
                destinationId = R.id.nav_dashboard,     // destinationID
                id = 0,                // ID
                text = R.string.title_dashboard),
            Model(
                icon = R.drawable.ic_calender,
                destinationId = R.id.nav_booking,
                id = 1,
                text = R.string.title_bookings
            ),
            Model(
                icon = R.drawable.ic_wallet,
                destinationId = R.id.nav_wallet,
                id = 2,
                text = R.string.title_wallet
            ),
            Model(
                R.drawable.ic_profile,
                R.id.nav_profile,
                3,
                R.string.title_profile
            )
        )

        binding.bottomNavigation.apply {
            setMenuItems(menuItems, intent.getIntExtra(Constants.INTENT_INDEX, 0))
            setupWithNavController(navController)
        }
        tabChange.observe(this) {
            // dashboard launch mode is single task it will kill middle screens automatically
            startActivity(
                Intent(this, SplashActivity::class.java)
                    .putExtra(Constants.INTENT_SHOW_TIMER, false)
                    .putExtra(Constants.INTENT_USER_TYPE, Constants.USER_NORMAL)
            )
            finishAffinity()
        }

    }


    /**
     * set observer
     */
    private fun setObserver() {
        profileViewModel.languageAndSpecialityListResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    // loading state
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
        }


        profileViewModel.specialityResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    // loading state
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
        }
    }

    fun changeTab() {
        tabChange.postValue(1)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    /**
     * manage notification intent
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // dashboard launch mode is single task it will kill middle screens automatically
        startActivity(
            Intent(this, SplashActivity::class.java)
                .putExtra(Constants.INTENT_SHOW_TIMER, false)
                .putExtra(Constants.INTENT_USER_TYPE, Constants.USER_NORMAL)
        )
        finishAffinity()
    }

}