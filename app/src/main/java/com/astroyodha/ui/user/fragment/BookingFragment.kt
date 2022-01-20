package com.astroyodha.ui.user.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.astroyodha.R
import com.astroyodha.core.BaseFragment
import com.astroyodha.databinding.FragmentBookingBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.user.activity.CalendarActivity
import com.astroyodha.ui.user.activity.SelectAstrologerActivity
import com.astroyodha.ui.user.adapter.ViewPagerFragmentAdapter
import com.astroyodha.ui.user.model.booking.BookingModel
import com.astroyodha.ui.user.viewmodel.BookingViewModel
import com.astroyodha.utils.Constants
import com.astroyodha.utils.makeGone
import com.astroyodha.utils.makeVisible
import com.astroyodha.utils.showSnackBarToast
import java.util.*

class BookingFragment : BaseFragment() {
    val TAG = "BookingFragment"

    private val bookingViewModel: BookingViewModel by viewModels()
    private lateinit var binding: FragmentBookingBinding
    private val userId: String by lazy { FirebaseAuth.getInstance().currentUser!!.uid }

    private var onGoingList: ArrayList<BookingModel> = arrayListOf()
    private var pastList: ArrayList<BookingModel> = arrayListOf()
    private var upComingList: ArrayList<BookingModel> = arrayListOf()
    private val mTitle by lazy {
        listOf(
            getString(R.string.booking_upcoming),
            getString(R.string.booking_ongoing),
            getString(R.string.booking_past)
        )
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                //  you will get result here in result.data
                init()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setClickListener()
        setObserver()
    }

    /**
     * initialize view
     */
    private fun init() {
        bookingViewModel.getAllUserBookingRequest(userId)
    }

    /**
     * Setup Viewpager
     */
    private fun setUpViewPager() {
        val mFragmentList = listOf(
            UpComingFragment.newInstance(userId, upComingList),
            OnGoingFragment.newInstance(userId, onGoingList),
            PastFragment.newInstance(userId, pastList)
        )
        binding.viewPager.adapter = ViewPagerFragmentAdapter(requireActivity(), mFragmentList)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = mTitle[position]
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                //tab selected
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                //unselected
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                //reselected
            }

        })
    }

    /**
     * set observer
     */
    private fun setObserver() {
        bookingViewModel.getBookingListDataResponse.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.LOADING -> {
                    binding.tabLayout.makeGone()
                }
                Status.SUCCESS -> {
                    binding.tabLayout.makeVisible()
                    hideProgress()
                    it.data?.let { resultList ->
                        val mCurrentTime = Date()
                        upComingList.clear()
                        upComingList.addAll(resultList.filter { resultData ->
                            resultData.startTime?.after(mCurrentTime)!! && resultData.endTime?.after(mCurrentTime)!!
                        })
                        pastList.clear()
                        pastList.addAll(resultList.filter { resultData ->
                            (resultData.startTime?.before(mCurrentTime)!! && resultData.endTime?.before(mCurrentTime)!!) ||
                              resultData.startTime!!.before(mCurrentTime) && resultData.endTime!!.after(mCurrentTime) && resultData.status != Constants.APPROVE_STATUS
                        })
                        onGoingList.clear()
                        onGoingList.addAll(resultList.filter { resultData ->
                            resultData.startTime!!.before(mCurrentTime) && resultData.endTime!!.after(mCurrentTime) && resultData.status == Constants.APPROVE_STATUS
                        })
                        setUpViewPager()
                    }
                }
                Status.ERROR -> {
                    binding.tabLayout.makeVisible()
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })
    }

    /**
     * manage click listener of view
     */
    private fun setClickListener() {
        binding.imgAdd.setOnClickListener {
            startForResult.launch(Intent(context, SelectAstrologerActivity::class.java))
        }

        binding.imgCalendar.setOnClickListener {
            startForResult.launch(Intent(context, CalendarActivity::class.java))
        }
    }
}