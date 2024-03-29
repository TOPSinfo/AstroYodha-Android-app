package com.astroyodha.ui.astrologer.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.core.app.NotificationManagerCompat
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
import com.astroyodha.ui.user.authentication.activity.SplashActivity
import com.astroyodha.ui.user.model.booking.BookingModel
import com.astroyodha.ui.user.viewmodel.ChatViewModel
import com.astroyodha.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

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

        startFromFresh()
    }

    private fun startFromFresh() {
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

    /**
     * disabling all fields on view
     */
    private fun disableAddEdit() {
        binding.tvTitle.text = getString(R.string.view_event)
        binding.edDetails.isEnabled = false
        binding.tvDate.isEnabled = false
        binding.tvStartTime.isEnabled = false
        binding.tvEndTime.isEnabled = false
        binding.tvStatus.isEnabled = false
        binding.tvSave.makeGone()
        binding.tvNotify.isEnabled = false

        binding.edtUserName.isEnabled = false
        binding.edtPlaceOfBirth.isEnabled = false
//        binding.tvUploadPhoto.isEnabled = false
//        binding.tvUploadKundali.isEnabled = false
        binding.tvDateOfBirth.isEnabled = false
        binding.tvTimeOfBirth.isEnabled = false


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

    /**
     * set data to view
     */
    private fun setData() {
        binding.tvAstrologerName.text = getString(
            R.string.appointment_with,
            bookingModel.userName.substringBefore(" ")
        )
        binding.edDetails.setText(bookingModel.description)
        binding.tvDate.text = bookingModel.startTime?.dateToStringFormat(dateFormat)
        binding.tvStartTime.text = bookingModel.startTime?.dateToStringFormat(timeFormat)
        startTime = bookingModel.startTime?.dateToStringFormat(timeFormat).toString()
        endTime = bookingModel.endTime?.dateToStringFormat(timeFormat).toString()

        binding.tvEndTime.text="${getMin()} "+getString(R.string.minute)

        binding.tvNotify.text = bookingModel.notify

        binding.edtUserName.setText(bookingModel.fullname)
        binding.tvDateOfBirth.setText(bookingModel.birthDate)
        binding.tvTimeOfBirth.setText(bookingModel.birthTime)
        binding.edtPlaceOfBirth.setText(bookingModel.birthPlace)

        binding.imgUploadPhoto.loadRoundedImage(bookingModel.photo)
        binding.imgUploadKundali.loadRoundedImage(bookingModel.kundali)




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
                mColor = getColor(R.color.user_theme)
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
        chatViewModel.addGroupCallDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    it.data.let {
                        val intent = Intent(this, JitsiCallAstrologerActivity::class.java)
                        intent.putExtra("RoomId", bookingModel.id)
//                        intent.putExtra("OpponentUserName", "")
//                        intent.putExtra("isGroupCall", isGroup)
                        intent.putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel)
                        startActivity(intent)
                    }

                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        bookingViewModel.getBookingDetailResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
//                    if (isEdit) {
                    binding.tvTitle.text = getString(R.string.edit_event)
                    bookingModel = it.data!!
                    disableAddEdit()
//                    }

                    if (intent.hasExtra(Constants.INTENT_IS_FROM) &&
                        intent.getStringExtra(Constants.INTENT_IS_FROM) == Constants.VIDEO_CALL_NOTIFICATION
                    ) {
                        NotificationManagerCompat.from(this)
                            .cancel(intent.getIntExtra(Constants.INTENT_NOTIFICATION_ID, 0))
                        if (!intent.getBooleanExtra(Constants.INTENT_CALL_REJECT, false)) {
                            redirectToVideoCallActivity()
                        }
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
        binding.imgClose.setOnClickListener {
            onBackPressed()
        }

        binding.tvAstrologerName.setOnClickListener {
            startActivity(
                Intent(this, AstrologerEventDetailActivity::class.java)
                    .putExtra(Constants.INTENT_USER_ID, bookingModel.userId)
                    .putExtra(Constants.INTENT_BOOKING_ID, bookingModel.id)
                    .putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel)
                    .putExtra(Constants.INTENT_ISEDIT, false)
            )
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

        binding.tvSave.setOnClickListener {
            hideKeyboard()
            if (isEdit) {
                addUpdateEvent(selectedStatus)
            }
        }

        binding.tvUploadPhoto.setOnClickListener {
            redirectToImageView(bookingModel.photo)
        }

        binding.tvUploadKundali.setOnClickListener {
            redirectToImageView(bookingModel.kundali)
        }

        binding.btnCall.setOnClickListener {
            redirectToVideoCallActivity()
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

    private fun redirectToVideoCallActivity() {
        if (!isComeFromNotification)
            return
        isComeFromNotification = false
        Constants.USER_NAME = bookingModel.astrologerName   // if user comes from notification this field is blank so assigning again
        TedPermission.with(this)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {

                    if (bookingModel.endTime!!.time - Date().time <= 0L) {
                        toast(getString(R.string.meeting_time_over))
                        binding.btnCall.makeGone()
                        binding.btnChat.makeGone()
                    } else {
                        val intent = Intent(this@AstrologerEventBookingActivity, JitsiCallAstrologerActivity::class.java)
                        intent.putExtra("RoomId", bookingModel.id)
//                            intent.putExtra("OpponentUserName", "")
//                            intent.putExtra("isGroupCall", isGroup)
                        intent.putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel)
                        startActivity(intent)
                        /*var currentUserId =
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
                        }*/

                    }

                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    // onPermissionDenied
                }

            }).setDeniedMessage(getString(R.string.permission_denied))
            .setPermissions(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
            .check()
    }

    /**
     * Redirect to view image
     */
    private fun redirectToImageView(path: String) {
        startActivity(
            Intent(
                this,
                AstrologerImageViewActivity::class.java
            ).putExtra(Constants.INTENT_IMAGE_URL, path)
        )
    }

    private fun addUpdateEvent(status: String) {

        bookingModel.status = status

        bookingViewModel.addUpdateBookingData(
            bookingModel,
            isEdit
        )
    }

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

        chatViewModel.setupVideoCallData(
            userIdsWithStatus,
            "Active",
            currentUserId,
            Constants.USER_NAME,
            bookingModel.id
        )

    }

    /**
     * get min from start time and end time
     */
    private fun getMin(): Int {
        val simpleDateFormat = SimpleDateFormat(timeFormat)

        val date1 = simpleDateFormat.parse(startTime)
        val date2 = simpleDateFormat.parse(endTime)
        val difference: Long = date2.time - date1.time
        val min = TimeUnit.MILLISECONDS.toMinutes(difference).toInt()
        return min

    }

    override fun onBackPressed() {
        if (checkIsLastActivity()) {
            //last activity go to Dashboard
            startActivity(
                Intent(this, SplashActivity::class.java)
                    .putExtra(Constants.INTENT_SHOW_TIMER, false)
                    .putExtra(Constants.INTENT_USER_TYPE, Constants.USER_ASTROLOGER)
            )
            finishAffinity()
        } else {
            super.onBackPressed()
        }
    }
    /**
     * manage notification intent
     */
    var isComeFromNotification = false    // somehow video screen opening twice if new intent run this will pervert to open video call screen twice
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // event launch mode is single task it will kill middle screens automatically
        setIntent(intent)
        startFromFresh()
        isComeFromNotification = true
    }

    /**
     * manage onResume
     */
    override fun onResume() {
        super.onResume()
        isComeFromNotification = true
    }
}