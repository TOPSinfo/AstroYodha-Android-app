package com.astroyodha.ui.user.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.google.firebase.auth.FirebaseAuth
import com.simform.custombottomnavigation.Model
import com.astroyodha.R
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityHomeBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.user.authentication.model.user.UserModel
import com.astroyodha.ui.user.viewmodel.ProfileViewModel
import com.astroyodha.utils.Constants
import com.astroyodha.utils.MyLog
import com.astroyodha.utils.showSnackBarToast

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

        /*if (savedInstanceState == null) {
            changeFragment(fragment = DashboardFragment(), TAG = "home")
        }*/
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
        tabChange.observe(this, {
//            binding.bottomNavigation.onMenuItemClick(1)
            startActivity(
                Intent(this, UserHomeActivity::class.java)
                    .putExtra(Constants.INTENT_INDEX, it)
            )
            finishAffinity()
        })

        /*binding.bottomNavigation.setOnMenuItemClickListener { model, i ->
//            binding.bottomNavigation.setSelectedIndex(i)
//            findNavController().navigate(model.destinationId)
            if (i == 0) {
                changeFragment(fragment = DashboardFragment(), TAG = "home")
            } else if (i == 1) {
                changeFragment(fragment = BookingFragment(), TAG = "book")
            } else if (i == 2) {
                changeFragment(fragment = WalletFragment(), TAG = "wallet")
            } else if (i == 3) {
                changeFragment(fragment = ProfileFragment(), TAG = "profile")
            }
        }

        binding.bottomNavigation.setOnReselectListener {  }
*/

//        profileViewModel.getUserDetail(FirebaseAuth.getInstance().currentUser?.uid.toString())

    }


    /**
     * set observer
     */
    private fun setObserver() {
        profileViewModel.languageAndSpecialityListResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
//                    showProgress(requireContext())
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { resultList ->
                        Constants.listOfLanguages.clear()
                        Constants.listOfLanguages.addAll(resultList)
                        MyLog.e("List of Language","=====${Constants.listOfLanguages.size}")
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
//                    showProgress(requireContext())
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { resultList ->
                        Constants.listOfSpeciality.clear()
                        Constants.listOfSpeciality.addAll(resultList)
                        MyLog.e("List of Speciality","=====${Constants.listOfSpeciality.size}")
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })
    }



    fun changeTab() {
        tabChange.postValue(1)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


}