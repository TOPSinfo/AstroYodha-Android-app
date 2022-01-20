package com.astroyodha.ui.astrologer.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.astroyodha.R
import com.astroyodha.core.BaseFragment
import com.astroyodha.databinding.FragmentAstrologerDashboardBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.activity.AstrologerEventDetailActivity
import com.astroyodha.ui.astrologer.activity.PendingBookingListActivity
import com.astroyodha.ui.astrologer.adapter.BookingAstrologerAdapter
import com.astroyodha.ui.astrologer.viewmodel.AstrologerBookingViewModel
import com.astroyodha.ui.astrologer.viewmodel.DashboardAstrologerViewModel
import com.astroyodha.ui.user.model.booking.BookingModel
import com.astroyodha.utils.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DashboardAstrologerFragment : BaseFragment() {


    private val dashboardAstrologerViewModel: DashboardAstrologerViewModel by viewModels()
    private val astrologerBookingViewModel: AstrologerBookingViewModel by viewModels()
    private lateinit var binding: FragmentAstrologerDashboardBinding
    private var userId: String? = null

    private var mList: ArrayList<BookingModel> = ArrayList()
    private var mPosition: Int=0

    lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext=context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAstrologerDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setObserver()
        clickListeners()
    }

    /**
     * initialize view
     */
    private fun init() {
        val fb = FirebaseAuth.getInstance().currentUser
        userId = fb?.uid.toString()

        val myFormat = "dd MMMM yyyy" //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.txtDate.setText(sdf.format(System.currentTimeMillis()))

        binding.txtUserName.setText(
            String.format(
                resources.getString(R.string.namaste),
                Constants.USER_NAME.substringBefore(" ")
            )
        )

        dashboardAstrologerViewModel.getPendingBookingofAstrologer(userId!!)
    }

    /**
     * manage click listener of view
     */
    fun clickListeners() {
        binding.txtViewAll.setOnClickListener {
            startActivity(Intent(mContext, PendingBookingListActivity::class.java))
        }
    }


    /**
     * set observer
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun setObserver() {
        dashboardAstrologerViewModel.getBookingListDataResponse.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.LOADING -> {
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { resultList ->
                        if (resultList.isNotEmpty()) {
                            binding.layoutNoData.makeGone()
                            binding.recyclerBookingList.makeVisible()
                            mList.clear()
                            mList.addAll(resultList)
                            setBookingListAdapter()
                        } else {
                            binding.layoutNoData.makeVisible()
                            binding.recyclerBookingList.makeGone()
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })

        astrologerBookingViewModel.bookingAddUpdateResponse.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(requireContext())
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { result ->
                        binding.root.showSnackBarToast(result)
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
     * This is used to set adapter of recyclerview ti display list of booking.
     */
    private fun setBookingListAdapter() {
        with(binding.recyclerBookingList) {

            val  mGridLayoutManager= GridLayoutManager(context, 2)
            val  mLinearLayoutManager= LinearLayoutManager(context)

            layoutManager = mGridLayoutManager

            itemAnimator = DefaultItemAnimator()
            adapter = BookingAstrologerAdapter(
                context, mList,
                object : BookingAstrologerAdapter.ViewHolderClicks {
                    override fun onClickItem(
                        model: BookingModel,
                        isAccept: Boolean,
                        position: Int,
                        isForDetail: Boolean
                    ) {

                        if (isForDetail) {
                            mContext.startActivity(
                                Intent(mContext, AstrologerEventDetailActivity::class.java)
                                    .putExtra(Constants.INTENT_USER_ID, model.userId)
                                    .putExtra(Constants.INTENT_BOOKING_ID, model.id)
                                    .putExtra(Constants.INTENT_BOOKING_MODEL, model)
                            )
                        } else {
                            if (model.status == Constants.PENDING_STATUS) {

                                mPosition = position
                                if (isAccept) {
                                    model.status = Constants.APPROVE_STATUS
                                } else {
                                    model.status = Constants.REJECT_STATUS
                                }
                                astrologerBookingViewModel.addUpdateBookingData(model, true)
                            } else {
                                binding.root.showSnackBarToast("Status already changed")
                            }
                        }


                    }

                }
            )
        }
    }


}