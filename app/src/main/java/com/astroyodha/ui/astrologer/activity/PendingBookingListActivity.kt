package com.astroyodha.ui.astrologer.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityPendingBookingListBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.adapter.BookingAstrologerAdapter
import com.astroyodha.ui.astrologer.viewmodel.AstrologerBookingViewModel
import com.astroyodha.ui.user.model.booking.BookingModel
import com.astroyodha.utils.*
import com.google.firebase.auth.FirebaseAuth

class PendingBookingListActivity : BaseActivity() {

    private var mPosition: Int=0
    lateinit var binding:ActivityPendingBookingListBinding
    private val astrologerBookingViewModel: AstrologerBookingViewModel by viewModels()

    var mList:ArrayList<BookingModel> = ArrayList()
    var userId:String=""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityPendingBookingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        clickListeners()
        setObserver()
    }

    /**
     * initialize view
     */
    fun init() {
        val fb = FirebaseAuth.getInstance().currentUser
        userId = fb?.uid.toString()

        astrologerBookingViewModel.getPendingBookingofAstrologer(userId)
    }

    /**
     * manage click listener of view
     */
    fun clickListeners() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    /**
     * set observer
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun setObserver() {
        astrologerBookingViewModel.getBookingListDataResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
//                    showProgress(requireContext())
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

        astrologerBookingViewModel.bookingAddUpdateResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
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

                        //click of recyclerview item
                        if(isForDetail)
                        {
                            startActivity(
                                Intent(this@PendingBookingListActivity,AstrologerEventDetailActivity::class.java)
                                    .putExtra(Constants.INTENT_USER_ID,model.userId)
                                    .putExtra(Constants.INTENT_BOOKING_ID,model.id)
                                    .putExtra(Constants.INTENT_BOOKING_MODEL,model)
                            )
                        }
                        else {
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