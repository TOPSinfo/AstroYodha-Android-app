package com.astroyodha.ui.astrologer.activity

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.viewModels
import com.google.common.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.astroyodha.R
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityAstrologerEventBookingBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.model.timeslot.TimeSlotModel
import com.astroyodha.ui.astrologer.model.user.AstrologerUserModel
import com.astroyodha.ui.astrologer.viewmodel.AstrologerBookingViewModel
import com.astroyodha.ui.astrologer.viewmodel.AstrologerJitsiViewModel
import com.astroyodha.ui.astrologer.viewmodel.ProfileAstrologerViewModel
import com.astroyodha.ui.astrologer.viewmodel.TimeSlotViewModel
import com.astroyodha.ui.user.model.booking.BookingModel
import com.astroyodha.ui.user.viewmodel.ChatViewModel
import com.astroyodha.utils.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class AstrologerEventBookingActivity : BaseActivity() {
    val TAG = javaClass.simpleName
    private lateinit var binding: ActivityAstrologerEventBookingBinding
    private val chatViewModel: ChatViewModel by viewModels()
    private val bookingViewModel: AstrologerBookingViewModel by viewModels()
    private val profileViewModel: ProfileAstrologerViewModel by viewModels()
    private val timeSlotViewModel: TimeSlotViewModel by viewModels()
    private val jitsCallAstrologerViewModel: AstrologerJitsiViewModel by viewModels()

    private var userId: String? = null
    var bookingModel: BookingModel = BookingModel()

    var mAstrologerTimeSlotList: ArrayList<TimeSlotModel> = ArrayList()

    var cal = Calendar.getInstance()

    var dateFormat: String = "EEE, dd MMM, yyyy"
    var timeFormat: String = "hh:mm a"
    var startTime: String = ""
    var endTime: String = ""


    var isEdit: Boolean = false
    var userModel: AstrologerUserModel = AstrologerUserModel()

    var selectedStatus: String = ""


    private var isGroup: Boolean = false
    private var memberIdList: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAstrologerEventBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getIntentData()
        setClickListener()
    }

    private fun getIntentData() {
        bookingModel = intent.getParcelableExtra(Constants.INTENT_BOOKING_MODEL)!!

        init()
        setObserver()

    }

    /**
     * initialize view
     */
    private fun init() {
        val fb = FirebaseAuth.getInstance().currentUser
        userId = fb?.uid.toString()
//        timeSlotViewModel.getTimeSlotList(astrologerModel.uid.toString())
        jitsCallAstrologerViewModel.getLIstOfActiveCall(userId.toString(), "Active")
        bookingViewModel.getBookingDetail(bookingModel.id)
    }


    private fun disableAddEdit() {
        binding.tvTitle.text = getString(R.string.view_event)
        binding.edDetails.isEnabled = false
        binding.tvDate.isEnabled = false
        binding.tvStartTime.isEnabled = false
        binding.tvEndTime.isEnabled = false
        binding.tvStatus.isEnabled = false
        binding.tvSave.makeGone()
        binding.tvNotify.isEnabled = false
        setData()
        binding.groupStatus.makeVisible()

        val mCurrentTime = Date()
        if (bookingModel.status == Constants.APPROVE_STATUS &&
            bookingModel.startTime!!.before(mCurrentTime) &&
            bookingModel.endTime!!.after(mCurrentTime)
        ) {
            binding.groupCommunication.makeVisible()
        }
        if (bookingModel.status == Constants.COMPLETED_STATUS) {
            binding.imgChat.makeVisible()
        }


    }

    private fun setData() {
        binding.tvAstrologerName.text = getString(
            R.string.appointment_with,
            bookingModel.userName.substringBefore(" ")
        )
        binding.edDetails.setText(bookingModel.description)
        binding.tvDate.text = bookingModel.startTime?.dateToStringFormat(dateFormat)
        binding.tvStartTime.text = bookingModel.startTime?.dateToStringFormat(timeFormat)
        startTime = binding.tvStartTime.text.toString()
        binding.tvEndTime.text = bookingModel.endTime?.dateToStringFormat(timeFormat)
        endTime = binding.tvEndTime.text.toString()
        binding.tvNotify.text = bookingModel.notify

        if (bookingModel.paymentType == Constants.PAYMENT_TYPE_WALLET) {
            binding.tvPaymentMode.text = getString(R.string.pay_with_wallet)
        } else {
            binding.tvPaymentMode.text = getString(R.string.pay_with_other)
        }
        binding.tvAmount.text = bookingModel.amount.toString()

//        if (showStatus) {
        var mColor = getColor(R.color.pending_color)
        var mStatus = getString(R.string.waiting)
        var image = R.drawable.ic_waiting
        when (bookingModel.status) {
            Constants.COMPLETED_STATUS -> {
                mStatus = getString(R.string.completed)
                mColor = getColor(R.color.completed_color)
                image = R.drawable.ic_read
            }
            Constants.APPROVE_STATUS -> {
                mStatus = getString(R.string.approved)
                mColor = getColor(R.color.approved_color)
                image = R.drawable.ic_check_black
            }
            Constants.REJECT_STATUS -> {
                mStatus = getString(R.string.rejected)
                mColor = getColor(R.color.orange_theme)
                image = R.drawable.ic_close
            }
            Constants.PENDING_STATUS -> {
                mStatus = getString(R.string.waiting)
                mColor = getColor(R.color.pending_color)
                image = R.drawable.ic_waiting
            }
            Constants.CANCEL_STATUS -> {
                mStatus = getString(R.string.deleted)
                mColor = getColor(R.color.delete_color)
                image = R.drawable.ic_delete
            }
        }
        binding.tvStatus.text = mStatus
        binding.tvStatus.setTextColor(mColor)
        binding.imgStatus.setImageResource(image)
        binding.imgStatus.setColorFilter(mColor);

//        }
    }

    /**
     * set observer
     */
    private fun setObserver() {
        //chat
        chatViewModel.addGroupCallDataResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    it.data.let {
                        val intent = Intent(this, JitsiCallAstrologerActivity::class.java)
                        intent.putExtra("RoomId", it!!)
                        intent.putExtra("OpponentUserName", "")
                        intent.putExtra("isGroupCall", isGroup)
                        intent.putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel)
                        startActivity(intent)
                    }

                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })

        bookingViewModel.getBookingDetailResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    MyLog.e(TAG, "Updated Value observer===" + it.data!!.endTime)
//                    if (isEdit) {
                    binding.tvTitle.text = getString(R.string.edit_event)
                    bookingModel = it.data
                    disableAddEdit()
//                    }

                }
                Status.ERROR -> {
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
        binding.imgClose.setOnClickListener {
            onBackPressed()
        }

        //this will allow inside scroll on multiline edit text if you have parent nested scrollview
        binding.edDetails.setOnTouchListener { v, event ->
            if (v.id == R.id.edDetails) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }

//        binding.tvNotify.setOnClickListener {
//            showNotificationDialog()
//        }
        binding.tvSave.setOnClickListener {
            hideKeyboard()
            if (isEdit) {
                addUpdateEvent(selectedStatus)
            }
        }

//        binding.tvStatus.setOnClickListener {
////            Toast.makeText(this, "Change Status", Toast.LENGTH_SHORT).show()
////            this@AstrologerEventBookingActivity.toast("Change Status")
//            MyLog.e("====Status=====","====Clicked=======")
//            requestStatusChangeDialog()
//        }


        binding.btnCall.setOnClickListener {

            TedPermission.with(this)
                .setPermissionListener(object : PermissionListener {
                    override fun onPermissionGranted() {

                        if (bookingModel.endTime!!.time - Date().time <= 0L) {
                            toast(getString(R.string.meeting_time_over))
                            binding.btnCall.makeGone()
                            binding.btnChat.makeGone()
                        } else {

                            var currentUserId =
                                FirebaseAuth.getInstance().currentUser?.uid.toString()
                            var userIds = ArrayList<String>()
                            if (isGroup) {
                                var selectedMemberIdList = ArrayList<String>()
                                if (!memberIdList.equals("")) {
                                    val type: Type = object : TypeToken<ArrayList<String>>() {}.type
                                    selectedMemberIdList = Gson().fromJson(memberIdList, type)
                                }
                                userIds.addAll(selectedMemberIdList)
                            } else {
                                userIds.add(currentUserId)
                                userIds.add(bookingModel.userId)
                            }

                            var isCallActive = false
                            for (i in Constants.listOfActiveCall) {
                                var isAllAvailable = true
                                for (j in userIds) {
                                    if ((i.userIds.contains(j + "___Active") || i.userIds.contains(j + "___InActive"))) {
                                    } else {
                                        isAllAvailable = false
                                        break
                                    }
                                }

                                if (isAllAvailable && userIds.size == i.userIds.size) {
                                    isCallActive = true
                                    val intent =
                                        Intent(
                                            this@AstrologerEventBookingActivity,
                                            JitsiCallAstrologerActivity::class.java
                                        )
                                    intent.putExtra("RoomId", i.docId)
                                    intent.putExtra("OpponentUserName", bookingModel.userName)
                                    intent.putExtra("isGroupCall", isGroup)
                                    intent.putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel)
                                    startActivity(intent)
                                    break
                                }
                            }

                            if (!isCallActive) {
                                setupVideoCall(userIds, currentUserId)
                            }

                        }

                    }

                    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    }

                }).setDeniedMessage(getString(R.string.permission_denied))
                .setPermissions(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
                .check()
        }
        binding.imgChat.setOnClickListener {
            redirectChatActivity()
        }

        binding.btnChat.setOnClickListener {
            if (bookingModel.endTime!!.time - Date().time <= 0L) {
                toast(getString(R.string.meeting_time_over))
                binding.btnCall.makeGone()
                binding.btnChat.makeGone()
            } else {
                redirectChatActivity()
            }
        }
    }


    /*
   * Dialog for select status of request
   * */

//    private fun requestStatusChangeDialog() {
//
//        var beaconDialog = Dialog(this)
//        beaconDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        beaconDialog.setContentView(R.layout.dialog_request_status)
//
//        val lp = WindowManager.LayoutParams()
//        lp.copyFrom(beaconDialog.window!!.attributes)
//        lp.width = WindowManager.LayoutParams.MATCH_PARENT
//        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
//        beaconDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        beaconDialog.window!!.attributes = lp
//        beaconDialog.show()
//
//        var radioGroupRequestStatus=beaconDialog.findViewById<RadioGroup>(R.id.rgrequestStatus)
//
//        radioGroupRequestStatus.setOnCheckedChangeListener { radioGroup, i ->
//
//            var mColor = getColor(R.color.pending_color)
//            var mStatus = getString(R.string.waiting)
//            var image = R.drawable.ic_waiting
//            if(i==R.id.rbAccept)
//            {
//                mStatus = getString(R.string.approved)
//                mColor = getColor(R.color.approved_color)
//                image = R.drawable.ic_check_black
//                selectedStatus=Constants.APPROVE_STATUS
//            }
//            else if(i== R.id.rbReject)
//            {
//                mStatus = getString(R.string.rejected)
//                mColor = getColor(R.color.orange_theme)
//                image = R.drawable.ic_close
//                selectedStatus=Constants.REJECT_STATUS
//            }
//
//            binding.tvStatus.text = mStatus
//            binding.tvStatus.setTextColor(mColor)
//            binding.imgStatus.setImageResource(image)
//            binding.imgStatus.setColorFilter(mColor);
//
//            beaconDialog.dismiss()
//
//        }
//    }

    private fun addUpdateEvent(status: String) {

        bookingModel.status = status

        bookingViewModel.addUpdateBookingData(
            bookingModel,
            isEdit
        )
    }

//    private fun updateDateInView() {
//        val myFormat = dateFormat // mention the format you need
//        val sdf = SimpleDateFormat(myFormat, Locale.US)
//        binding.tvDate.text = sdf.format(cal.time)
//    }


    /**
     * Redirect to chat activity after click on user
     */
    fun redirectChatActivity() {

        val userlist = ArrayList<String>()
        userlist.add(0, userId.toString())
        userlist.add(1, bookingModel.userId)

        val intent = Intent(this, AstrologerChatActivity::class.java)
        intent.putExtra("isGroup", false)
        intent.putExtra("userList", Gson().toJson(ArrayList<AstrologerUserModel>()))
        intent.putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel)
        intent.putExtra("user_id", bookingModel.userId)
        intent.putExtra("user_name", bookingModel.astrologerName)
        intent.putExtra("group_image", "")
        startActivity(intent)
    }

    /**
     * setupVideoCall
     */
    private fun setupVideoCall(userIds: ArrayList<String>, currentUserId: String) {
        var userIdsWithStatus = ArrayList<String>()
        for (i in userIds) {
            if (i.equals(currentUserId)) {
                userIdsWithStatus.add(i + "___Active")
            } else {
                userIdsWithStatus.add(i + "___InActive")
            }
        }

//            userIds.add(0, currentUserId + "___Active")
        chatViewModel.setupVideoCallData(
            userIdsWithStatus,
            "Active",
            currentUserId,
            Constants.USER_NAME
        )

    }
}