package com.astroyodha.ui.astrologer.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.astroyodha.R
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityAstrologerEventDetailBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.viewmodel.AstrologerBookingViewModel
import com.astroyodha.ui.astrologer.viewmodel.ProfileAstrologerViewModel
import com.astroyodha.ui.user.authentication.model.user.UserModel
import com.astroyodha.ui.user.model.booking.BookingModel
import com.astroyodha.utils.*

class AstrologerEventDetailActivity : BaseActivity() {

    private lateinit var bookingModel: BookingModel
    lateinit var binding: ActivityAstrologerEventDetailBinding
    private val profileViewModel: ProfileAstrologerViewModel by viewModels()
    private val bookingViewModel: AstrologerBookingViewModel by viewModels()

    var userId: String = ""
    var bookingId: String = ""

    var dateFormat: String = "EEE, dd MMM, yyyy"
    var timeFormat: String = "hh:mm a"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAstrologerEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        clickListeners()
    }


    fun init() {

        intent.getStringExtra(Constants.INTENT_USER_ID).let {
            userId = it.toString()
        }

        intent.getStringExtra(Constants.INTENT_BOOKING_ID).let {
            bookingId = it.toString()
        }
        bookingModel = intent.getParcelableExtra(Constants.INTENT_BOOKING_MODEL)!!

        profileViewModel.getUserDetailById(userId)

        setObserver()
    }

    fun setObserver() {

        profileViewModel.normalUserDetailResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    it.data.let {
//                        bookingViewModel.getBookingDetail(bookingId)
                        setData(it)
                        setEventData(bookingModel)
                    }


                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })

        bookingViewModel.bookingAddUpdateResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { result ->
                        binding.root.showSnackBarToast(result)
                        binding.btnAccept.makeGone()
                        binding.btnReject.makeGone()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })

//        bookingViewModel.getBookingDetailResponse.observe(this, {
//            when (it.status) {
//                Status.LOADING -> {
////                    showProgress(requireContext())
//                }
//                Status.SUCCESS -> {
//                    hideProgress()
//                    it.data.let {
//                        setEventData(it)
//                    }
//
//
//                }
//                Status.ERROR -> {
//                    hideProgress()
//                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
//                }
//            }
//        })
    }

    private fun clickListeners() {
        binding.imgBack.setOnClickListener { onBackPressed() }

        binding.btnAccept.setOnClickListener {

            if (bookingModel.paymentStatus == Constants.RAZOR_PAY_STATUS_AUTHORIZED) {
                binding.root.showSnackBarToast("You can Accept or Reject booking only after 5 min")
            } else {
                bookingModel.status = Constants.APPROVE_STATUS
                bookingViewModel.addUpdateBookingData(bookingModel, true)
            }
        }

        binding.btnReject.setOnClickListener {
            bookingModel.status = Constants.REJECT_STATUS
            bookingViewModel.addUpdateBookingData(bookingModel, true)
        }
    }


    private fun setEventData(it: BookingModel?) {

        var startTime = ""
        var endTime = ""
        it?.startTime.let {
            binding.tvEventDate.text = it?.dateToStringFormat(dateFormat)
            startTime = it?.dateToStringFormat(timeFormat).toString()

        }

        it?.endTime.let {
//            binding.tvEndTime.text = it?.dateToStringFormat(timeFormat)
            endTime = it?.dateToStringFormat(timeFormat).toString()

        }

        binding.tvStartTime.text = "${startTime} To ${endTime}"

        it?.description.let {
            binding.tvEventDescription.addReadMoreText(
                it.toString(),
                ContextCompat.getColor(this, R.color.astrologer_blue_theme)
            )
//            binding.tvEventDescription.text = it.toString()
        }
    }

    private fun setData(it: UserModel?) {


        it?.profileImage.let {
            binding.imgAvatar.loadRoundedImage(it, 24)
        }


        it?.fullName.let {
            binding.tvName.text = it.toString()
            binding.tvAbout.text = getString(R.string.about_, it?.substringBefore(" "))
            binding.tvFullName.text = it.toString()
        }

        it?.phone.let {
            binding.tvMobileNumber.text = it.toString()
        }

        it?.birthDate.let {
            binding.tvDateOfBirth.text = it.toString()
        }

        it?.birthTime.let {
            binding.tvBirthTime.text = it.toString()
        }

        it?.birthPlace.let {
            binding.tvBirthPlace.text = it.toString()
        }
    }
}
