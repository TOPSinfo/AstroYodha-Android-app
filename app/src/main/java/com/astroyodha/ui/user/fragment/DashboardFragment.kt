package com.astroyodha.ui.user.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.astroyodha.R
import com.astroyodha.core.BaseFragment
import com.astroyodha.databinding.FragmentDashboardBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.model.user.AstrologerUserModel
import com.astroyodha.ui.astrologer.viewmodel.ProfileAstrologerViewModel
import com.astroyodha.ui.user.activity.*
import com.astroyodha.ui.user.adapter.AstrologerAdapter
import com.astroyodha.ui.user.viewmodel.DashboardViewModel
import com.astroyodha.utils.Constants
import com.astroyodha.utils.showSnackBarToast
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : BaseFragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    lateinit var binding: FragmentDashboardBinding
    lateinit var mContext: Context

    private val astrologerViewModel: ProfileAstrologerViewModel by viewModels()
    private var mAstrologerList: ArrayList<AstrologerUserModel> = arrayListOf()

    private val startBookNowForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                //  you will get result here in result.data
                (activity as UserHomeActivity).changeTab()

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
        dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setObserver()
        setClickListener()
    }

    /**
     * initialize view
     */
    fun init() {

        val myFormat = "dd MMMM yyyy" //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.txtDate.setText(sdf.format(System.currentTimeMillis()))

        binding.txtUserName.setText(
            String.format(
                resources.getString(R.string.namaste),
                Constants.USER_NAME.substringBefore(" ")
            )
        )

        with(binding.recyclerAstrologerList) {
            val mLayoutManager = GridLayoutManager(context, 2)
            layoutManager = mLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = AstrologerAdapter(
                context, mAstrologerList,
                object : AstrologerAdapter.ViewHolderClicks {
                    override fun onBookNowClickItem(
                        model: AstrologerUserModel,
                        position: Int
                    ) {
                        //click of recyclerview item
                        startBookNowForResult.launch(
                            Intent(mContext, EventBookingActivity::class.java)
                                .putExtra(Constants.INTENT_ISEDIT, false)
                                .putExtra(Constants.INTENT_MODEL, model)
                        )
                    }

                    override fun onProfileClickItem(model: AstrologerUserModel, position: Int) {
                        startBookNowForResult.launch(
                            Intent(mContext, AstrologerProfileViewActivity::class.java)
                                .putExtra(Constants.INTENT_MODEL, model)
                        )
                    }
                }
            )
        }

        astrologerViewModel.getAstrologerList(4)
    }

    /**
     * set observer
     */
    private fun setObserver() {
        astrologerViewModel.getAstrologerListResponse.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    // loading state
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { result ->
                        mAstrologerList.addAll(result)
                        binding.recyclerAstrologerList.adapter?.notifyDataSetChanged()
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
     * manage click listener of view
     */
    private fun setClickListener() {
        binding.lnNotification.setOnClickListener {
            startActivity(Intent(mContext, NotificationActivity::class.java))
        }
        binding.txtViewAll.setOnClickListener {
            startBookNowForResult.launch(Intent(mContext, SelectAstrologerActivity::class.java))
        }
        binding.txtBookAppointment.setOnClickListener {
            startBookNowForResult.launch(Intent(mContext, SelectAstrologerActivity::class.java))
        }

        binding.layoutDailyHoroscope.setOnClickListener {
            startActivity(Intent(mContext, ComingSoonActivity::class.java))
        }
        binding.layoutFreeKundali.setOnClickListener {
            startActivity(Intent(mContext, ComingSoonActivity::class.java))
        }
        binding.layoutHoroscopeMatching.setOnClickListener {
            startActivity(Intent(mContext, ComingSoonActivity::class.java))
        }
    }

}