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
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.astroyodha.core.BaseFragment
import com.astroyodha.databinding.FragmentPastBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.viewmodel.ProfileAstrologerViewModel
import com.astroyodha.ui.user.activity.EventBookingActivity
import com.astroyodha.ui.user.adapter.BookingAdapter
import com.astroyodha.ui.user.model.booking.BookingModel
import com.astroyodha.ui.user.viewmodel.BookingViewModel
import com.astroyodha.utils.Constants
import com.astroyodha.utils.makeGone
import com.astroyodha.utils.makeVisible
import com.astroyodha.utils.showSnackBarToast

private const val ARG_PARAM1 = "userId"
private const val ARG_PARAM2 = "list"

class PastFragment : BaseFragment() {
    val TAG = "PastFragment"

    private lateinit var binding: FragmentPastBinding
    private val bookingViewModel: BookingViewModel by viewModels()
    private val profileAstrologerViewModel: ProfileAstrologerViewModel by viewModels()

    private var mList: ArrayList<BookingModel> = arrayListOf()
    private var userId = ""
    private var bookingModel = BookingModel()
    var mPositionEdit = 0
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                //  you will get result here in result.data
                var mData =
                    result.data?.getParcelableExtra<BookingModel>(Constants.INTENT_BOOKING_MODEL)
                mList[mPositionEdit] = mData!!
                binding.rvBooking.adapter?.notifyItemChanged(mPositionEdit)

            }
        }
    companion object {
        @JvmStatic
        fun newInstance(param1: String,param2: ArrayList<BookingModel>) =
            PastFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putParcelableArrayList(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setObserver()
    }

    /**
     * initialize view
     */
    private fun init() {
        userId = arguments?.getString(ARG_PARAM1).toString()
        mList =
            arguments?.getParcelableArrayList<BookingModel>(ARG_PARAM2) as ArrayList<BookingModel>
        if (mList.isEmpty()) {
            binding.rvBooking.makeGone()
            binding.tvNoDataFound.makeVisible()
        } else {
            binding.rvBooking.makeVisible()
            binding.tvNoDataFound.makeGone()
        }

        with(binding.rvBooking) {
            val mLayoutManager = LinearLayoutManager(context)
            layoutManager = mLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = BookingAdapter(
                context, mList,
                object : BookingAdapter.ViewHolderClicks {
                    override fun onClickItem(
                        model: BookingModel,
                        position: Int
                    ) {
                        //click of recyclerview item
                        bookingModel = model
                        mPositionEdit = position
                        profileAstrologerViewModel.getUserDetail(model.astrologerID, true)
                    }
                }
            )
        }

        bookingViewModel.getStatusUpdateListener(userId)
    }

    /**
     * set observer
     */
    private fun setObserver() {
        bookingViewModel.bookingList.observe(viewLifecycleOwner) { updatedData ->
            updatedData.forEach {
                mList.mapIndexed { index, bookingModel ->
                    if (bookingModel.id == it.id /*&& bookingModel.status != it.status*/) {
//                        bookingModel.status = it.status
//                        binding.rvBooking.adapter?.notifyItemChanged(index, bookingModel)
                        mList[index] = it
                        binding.rvBooking.adapter?.notifyItemChanged(index)
                        return@forEach
                    }
                }
            }

        }

        bookingViewModel.getBookingListDataResponse.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(requireContext())
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { resultList ->
                        mList.addAll(resultList)
                        binding.rvBooking.adapter?.notifyDataSetChanged()

                        if (mList.isEmpty()) {
                            binding.rvBooking.makeGone()
                            binding.tvNoDataFound.makeVisible()
                        } else {
                            binding.rvBooking.makeVisible()
                            binding.tvNoDataFound.makeGone()
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        profileAstrologerViewModel.userDetailResponse.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(requireContext())
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { result ->
                        startForResult.launch(
                            Intent(context, EventBookingActivity::class.java)
                                .putExtra(Constants.INTENT_ISEDIT, false)
                                .putExtra(Constants.INTENT_MODEL, result)
                                .putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel)
                                .putExtra(Constants.INTENT_IS_FROM, Constants.BOOKING_PAST)
                        )
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
    }


}