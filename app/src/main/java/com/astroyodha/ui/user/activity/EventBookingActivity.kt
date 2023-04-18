package com.astroyodha.ui.user.activity

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.RecyclerView
import com.astroyodha.R
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityEventBookingBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.model.timeslot.TimeSlotModel
import com.astroyodha.ui.astrologer.model.user.AstrologerUserModel
import com.astroyodha.ui.astrologer.viewmodel.ProfileAstrologerViewModel
import com.astroyodha.ui.astrologer.viewmodel.TimeSlotViewModel
import com.astroyodha.ui.user.adapter.CustomPopupAdapter
import com.astroyodha.ui.user.authentication.activity.SplashActivity
import com.astroyodha.ui.user.authentication.model.user.UserModel
import com.astroyodha.ui.user.model.booking.BookingModel
import com.astroyodha.ui.user.model.wallet.WalletModel
import com.astroyodha.ui.user.viewmodel.*
import com.astroyodha.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.skydoves.balloon.balloon
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.utils.ContentUriUtils
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class EventBookingActivity : BaseActivity() {

    private val RC_UPLOAD_IMAGE = 121
    private val RC_UPLOAD_KUNDALI = 122

    private var totalamount: Int = 0
    val TAG = javaClass.simpleName
    private lateinit var binding: ActivityEventBookingBinding
    private val chatViewModel: ChatViewModel by viewModels()
    private val bookingViewModel: BookingViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val astrologerProfileViewModel: ProfileAstrologerViewModel by viewModels()
    private val walletViewModel: WalletViewModel by viewModels()
    private val timeSlotViewModel: TimeSlotViewModel by viewModels()
    private val jitsiCallViewModel: JitsiViewModel by viewModels()

    private var userId: String? = null
    var bookingModel: BookingModel = BookingModel()
    var userModel: UserModel = UserModel()
    private var walletModel: WalletModel = WalletModel()
    var chargableMin: Int = 0
    private var isDirectPayment: Boolean = false
    private var isFromWallet: Boolean = false
    var mAstrologerTimeSlotList: ArrayList<TimeSlotModel> = ArrayList()

    var cal = Calendar.getInstance()
    var calBirthdate = Calendar.getInstance()
    lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    var interval = 15
    private var mTimeList: ArrayList<String> = arrayListOf()
    private var request: String = ""

    var dateFormat: String = "EEE, dd MMM, yyyy"
    var dayFormat: String = "EEEE"
    var dateDBFormat: String = "dd - MMM - yyyy"
    var birthdateFormat: String = "dd MMM yyyy"
    var timeFormat: String = "hh:mm a"
    var startTime: String = ""
    var endTime: String = ""
    var isFromTimeClick = false

    // ballon popup
    private val customListBalloon by balloon<CustomListBalloonFactory>()

    // ballon popup
    private val customListEventDurationBalloon by balloon<CustomListBalloonFactory>()
    private var mEventDurationList: ArrayList<String> = arrayListOf("15", "30", "45", "60")

    var onSave: Boolean = false
    var isEdit: Boolean = false
    var astrologerModel: AstrologerUserModel = AstrologerUserModel()
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // booking added in payment no need to add from here
                setResult()
            }
        }

    private var isGroup: Boolean = false
    private var memberIdList: String = ""
    private var opponentUserName: String = ""

    var photoPath: Uri? = null
    var kundaliPath: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startFromFresh()
    }

    private fun startFromFresh() {
        getIntentData()
        init()
        setObserver()
        setClickListener()
    }

    private fun getIntentData() {
        isEdit = intent.getBooleanExtra(Constants.INTENT_ISEDIT, false)
        astrologerModel = intent.getParcelableExtra(Constants.INTENT_MODEL)!!
        if (isEdit) {
            binding.tvTitle.text = getString(R.string.edit_event)
            bookingModel = intent.getParcelableExtra(Constants.INTENT_BOOKING_MODEL)!!
            binding.tvStartTime.isEnabled = false
            binding.tvEndTime.isEnabled = false
            binding.tvPaymentMode.isEnabled = false
            setData(false)
            // showing delete icon on waiting request
            if (bookingModel.status == Constants.PENDING_STATUS) {
                binding.imgDelete.makeVisible()
            }
        } else {
            if (intent.hasExtra(Constants.INTENT_IS_FROM)) {
                bookingModel = intent.getParcelableExtra(Constants.INTENT_BOOKING_MODEL)!!
                bookingViewModel.getBookingDetail(bookingModel.id)
            } else {
                // comes on add event
                binding.groupAmount.makeGone()
            }
        }
    }

    /**
     * set disable data
     */
    private fun disableAddEdit() {
        binding.tvTitle.text = getString(R.string.view_event)
        binding.tvSave.makeGone()
        binding.edDetails.isEnabled = false
        binding.tvDate.isEnabled = false
        binding.tvStartTime.isEnabled = false
        binding.tvEndTime.isEnabled = false
        binding.tvStatus.isEnabled = false
        binding.tvNotify.isEnabled = false
        binding.tvPaymentMode.isEnabled = false
        binding.edtUserName.isEnabled = false
        binding.edtPlaceOfBirth.isEnabled = false
//        binding.tvUploadPhoto.isEnabled = false
//        binding.tvUploadKundali.isEnabled = false
        binding.tvDateOfBirth.isEnabled = false
        binding.tvTimeOfBirth.isEnabled = false

        setData(true)
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
    private fun setData(showStatus: Boolean) {
        binding.tvAstrologerName.text = bookingModel.astrologerName
        binding.edDetails.setText(bookingModel.description)
        binding.tvDate.text = bookingModel.startTime?.dateToStringFormat(dateFormat)
        binding.tvStartTime.text = bookingModel.startTime?.dateToStringFormat(timeFormat)
        startTime = binding.tvStartTime.text.toString()
//        binding.tvEndTime.text = bookingModel.endTime?.dateToStringFormat(timeFormat)
        endTime = bookingModel.endTime?.dateToStringFormat(timeFormat).toString()
        binding.tvEndTime.text = "${getMin()} " + getString(R.string.minute)

        binding.tvNotify.text = bookingModel.notify
        if (bookingModel.paymentType == Constants.PAYMENT_TYPE_WALLET) {
            binding.tvPaymentMode.text = getString(R.string.pay_with_wallet)
        } else {
            binding.tvPaymentMode.text = getString(R.string.pay_with_other)
        }
        binding.tvAmount.text = bookingModel.amount.toString()

        binding.edtUserName.setText(bookingModel.fullname)
        binding.tvDateOfBirth.setText(bookingModel.birthDate)
        binding.tvTimeOfBirth.setText(bookingModel.birthTime)
        binding.edtPlaceOfBirth.setText(bookingModel.birthPlace)

        binding.imgUploadPhoto.loadRoundedImage(bookingModel.photo)
        binding.imgUploadKundali.loadRoundedImage(bookingModel.kundali)

        if (showStatus) {
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
            binding.imgStatus.setColorFilter(mColor)
        }
    }

    /**
     * initialize view
     */
    private fun init() {
        val fb = FirebaseAuth.getInstance().currentUser
        userId = fb?.uid.toString()
        binding.tvAstrologerName.text = astrologerModel.fullName?.substringBefore(" ")
        // only set date when event add time only
        if (!isEdit && bookingModel.status == Constants.PENDING_STATUS) {
            updateDateInView()
        }

        val currentTime = Date()
        val currentTimeString = currentTime.dateToStringFormat(timeFormat)
        var currentTimePosition = -1
        var index = 0
        // adding 12 am to 12pm time in list
        for (h in 0..23) {
            var m = 0
            while (m < 60) {
                val innerTimeFormat = if (h < 12) {
                    String.format("%02d:%02d %s", h, m, "AM")
                } else {
                    String.format("%02d:%02d %s", h % 12, m, "PM")  // convert 24 to 12 hour
                }
                mTimeList.add(innerTimeFormat)

                // store position of nearest time according to current time
                if (currentTimePosition < 0) {
                    val simpleDateFormat = SimpleDateFormat(timeFormat)
                    val date1 = simpleDateFormat.parse(currentTimeString)
                    val date2 = simpleDateFormat.parse(innerTimeFormat)
                    val difference: Long = date2.time - date1.time
                    val min = TimeUnit.MILLISECONDS.toMinutes(difference).toInt()
                    index++
                    if (abs(min) <= 15) {
                        currentTimePosition = index
                    }
                }
                m += interval
            }
        }

        // gets customListBalloon's recyclerView.
        val listRecycler: RecyclerView =
            customListBalloon.getContentView().findViewById(R.id.list_recyclerView)
        listRecycler.adapter =
            CustomPopupAdapter(
                this, mTimeList,
                object : CustomPopupAdapter.Delegate {
                    override fun onCustomItemClick(customItem: String, position: Int) {
                        customListBalloon.dismiss()
//                    if (isFromTimeClick) {
                        startTime = customItem
                        binding.tvStartTime.text = startTime
                        if (binding.tvEndTime.text.isNotBlank() && endTime.isNotBlank()) {
                            if (binding.tvDate.text.toString() != getString(R.string.select_date)) {
                                getEndTime()
                            } else {
                                binding.root.showSnackBarToast(getString(R.string.please_select_date))
                            }
                        }
                    }
                }
            )
        listRecycler.scrollToPosition(currentTimePosition)

        val listEventDurationRecycler: RecyclerView =
            customListEventDurationBalloon.getContentView().findViewById(R.id.list_recyclerView)
        listEventDurationRecycler.adapter =
            CustomPopupAdapter(this, mEventDurationList, object : CustomPopupAdapter.Delegate {
                override fun onCustomItemClick(customItem: String, position: Int) {
                    customListEventDurationBalloon.dismiss()

                    endTime = customItem
                    binding.tvEndTime.text = "${endTime} " + getString(R.string.minute)
                    if (startTime.isNotBlank() && endTime.isNotBlank()) {
                        binding.groupAmount.makeVisible()
                        totalamount = 0
                        if (endTime == "15") {
                            totalamount = astrologerModel.price
                        } else if (endTime == "30") {
                            totalamount = astrologerModel.price * 2
                        } else if (endTime == "45") {
                            totalamount = astrologerModel.price * 3
                        } else if (endTime == "60") {
                            totalamount = astrologerModel.price * 4
                        }

                        if (binding.tvDate.text.toString() != getString(R.string.select_date)) {
                            binding.tvAmount.text = totalamount.toString()
                            getEndTime()
                            checkWalletBalance(false)
                        } else {
                            endTime = ""
                            binding.root.showSnackBarToast(getString(R.string.please_select_date))
                        }
                    }
                }
            })

        profileViewModel.getUserSnapshotDetail(userId.toString())
        astrologerProfileViewModel.getUserSnapshotDetail(astrologerModel.uid.toString())
//        timeSlotViewModel.getTimeSlotList(astrologerModel.uid.toString())
        jitsiCallViewModel.getLIstOfActiveCall(userId.toString(), "Active")
    }

    /**
     * set end time calculation
     */
    private fun getEndTime() {
        val mFormat = SimpleDateFormat("$dateFormat $timeFormat")
        var startDate: Date = mFormat.parse("${binding.tvDate.text} $startTime")
        val minuteMillis: Long = 60000 // millisecs
        val curTimeInMs: Long = startDate.time
        endTime = mFormat.format(Date(curTimeInMs + binding.tvEndTime.text.toString().getAmount().toInt() * minuteMillis))
        var endDate: Date = mFormat.parse("$endTime")
        Log.e("End Date=", "==${endDate}")

        val mTimeFormat = SimpleDateFormat("$timeFormat")
        endTime = mTimeFormat.format(endDate)

        MyLog.e("End Time=", "==${endTime}")
    }

    /**
     * show Notification dialog
     */
    private fun showNotificationDialog() {
        val mDialog = Dialog(this)
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog.setContentView(R.layout.dialog_notification)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(mDialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog.window!!.attributes = lp
        mDialog.show()

        val rgNotification = mDialog.findViewById(R.id.rgNotification) as RadioGroup

        rgNotification.setOnCheckedChangeListener { radioGroup, checkedId ->
            val radioButton = radioGroup.findViewById(checkedId) as RadioButton
            binding.tvNotify.text = radioButton.text.toString()
            mDialog.hide()
        }
    }

    /**
     * show Notification dialog
     */
    private fun showPaymentModeDialog() {
        val mDialog = Dialog(this)
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog.setContentView(R.layout.dialog_payment_mode)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(mDialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog.window!!.attributes = lp
        mDialog.show()

        val rgPaymentMode = mDialog.findViewById(R.id.rgPaymentMode) as RadioGroup

        rgPaymentMode.setOnCheckedChangeListener { radioGroup, checkedId ->
            val radioButton = radioGroup.findViewById(checkedId) as RadioButton
            binding.tvPaymentMode.text = radioButton.text.toString()
            mDialog.dismiss()
        }
    }

    /**
     * set observer
     */
    private fun setObserver() {

        // get booking detail when comes from upcoming(edit false),past or ongoing
        bookingViewModel.getBookingDetailResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { result ->
                        bookingModel = result
                        disableAddEdit()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        // get login user profile details
        profileViewModel.userDetailResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { result ->
                        if (result.uid == userId) {
                            val isFirstTime = userModel.uid.isNullOrBlank()
                            userModel = result
                            if (onSave) {
                                onSave = false
                            } else if (!intent.hasExtra(Constants.INTENT_IS_FROM) && isFirstTime) {
                                checkWalletBalance(true)
                            }
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        // astrologer profile
        astrologerProfileViewModel.userDetailResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { result ->
                        if (result.uid == astrologerModel.uid) {
                            astrologerModel = result
                            if (intent.hasExtra(Constants.INTENT_IS_FROM) &&
                                intent.getStringExtra(Constants.INTENT_IS_FROM) == Constants.VIDEO_CALL_NOTIFICATION
                            ) {
                                // clear call notification
                                NotificationManagerCompat.from(this)
                                    .cancel(intent.getIntExtra(Constants.INTENT_NOTIFICATION_ID, 0))
                                MyLog.e(TAG,"===== ${intent.getBooleanExtra(Constants.INTENT_CALL_REJECT, false)}")
                                if (!intent.getBooleanExtra(Constants.INTENT_CALL_REJECT, false)) {
                                    redirectToVideoCallActivity()
                                }
                            }
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }


        // add event and upload photo or kundali
        bookingViewModel.bookingUploadPhotoResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }


        // add event
        bookingViewModel.bookingAddUpdateResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
//                    showProgress(this)
                }
                Status.SUCCESS -> {
                    it.data?.let {
                        // add data in wallet history
                        if (isDirectPayment) {
                            // comes here after payment
                            toast(it.substringAfter(" "))
                            walletModel.bookingId = it.substringBefore(" ")
                            walletViewModel.addMoney(walletModel, true, true)
                        } else if (isFromWallet) {
                            // comes here if user have sufficient wallet balance
                            var walletModel = WalletModel()
                            walletModel.amount = totalamount/*astrologerModel.price * chargableMin*/
                            walletModel.userId = userModel.uid.toString()
                            walletModel.userName = userModel.fullName.toString()
                            walletModel.paymentType = Constants.PAYMENT_TYPE_WALLET
                            walletModel.trancationType = Constants.TRANSACTION_TYPE_DEBIT
                            walletModel.bookingId = it.substringBefore(" ")
                            walletModel.astrologerId = bookingModel.astrologerID
                            walletModel.astrologerName = bookingModel.astrologerName
                            walletModel.setCapturedGateway = false
                            walletViewModel.addMoney(walletModel, false, true)
                        } else {
                            // comes hear on edit
                            hideProgress()
                            toast(it.substringAfter(" "))
                            setResult()
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        // delete event
        bookingViewModel.bookingDeleteResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        toast(it)
                        // clear old booking model object
                        bookingModel = BookingModel()
                        setResult()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        // add deduction entry in wallet history
        walletViewModel.walletDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        if (!it.contains(true.toString())) {
                            userModel.walletBalance =
                                (userModel.walletBalance!!.toInt() - totalamount/*astrologerModel.price * chargableMin*/)
                            profileViewModel.updateUserData(
                                userModel
                            )
                        } else {
                            astrologerModel.walletBalance =
                                (astrologerModel.walletBalance!!.toInt() + totalamount/*astrologerModel.price * chargableMin*/)
                            astrologerProfileViewModel.updateAstrologerWalletBalance(
                                astrologerModel.uid!!,
                                astrologerModel.walletBalance!!
                            )
                            setResult()
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        // update user balance
        profileViewModel.userDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    astrologerModel.walletBalance =
                        (astrologerModel.walletBalance!!.toInt() + totalamount/*astrologerModel.price * chargableMin*/)
                    astrologerProfileViewModel.updateAstrologerWalletBalance(
                        astrologerModel.uid!!,
                        astrologerModel.walletBalance!!
                    )
                    setResult()
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        // update astrologer balance
        astrologerProfileViewModel.userDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    setResult()
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        // chat
        chatViewModel.addGroupCallDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    it.data.let {
                        val intent = Intent(this, JitsiCallActivity::class.java)
                        intent.putExtra("RoomId", bookingModel.id)
//                        intent.putExtra("OpponentUserName", "")
//                        intent.putExtra("isGroupCall", isGroup)
                        intent.putExtra(Constants.INTENT_MODEL, astrologerModel)
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

        // get astrologer timeslots
        timeSlotViewModel.timeSlotListResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
//                    hideProgress()
                    var performTask = false
                    it.data?.let {
                        mAstrologerTimeSlotList.clear()
                        mAstrologerTimeSlotList.addAll(it)
                        run loop@{
                            mAstrologerTimeSlotList.forEachIndexed { index, inner ->
                                val simpleDateFormat = SimpleDateFormat(timeFormat)
                                val selectStartTime = simpleDateFormat.parse(startTime)
                                val selectEndTime = simpleDateFormat.parse(endTime)
                                val innerStartTime = simpleDateFormat.parse(inner.fromTime)
                                val innerEndTime = simpleDateFormat.parse(inner.toTime)
                                if (inner.type == getString(R.string.custom)) {
                                    if (selectStartTime.before(innerStartTime) ||
                                        selectStartTime.after(innerEndTime) ||
                                        selectEndTime.before(innerStartTime) ||
                                        selectEndTime.after(innerEndTime)
                                    ) {
                                        if (index == mAstrologerTimeSlotList.size - 1) {
                                            getWeeklyTimeSlot()
                                            return@let
//                                            binding.root.showSnackBarToast(getString(R.string.astrologer_not_available))
                                        }
                                        performTask = false
                                    } else {
                                        performTask = true
                                        return@loop
                                    }
                                } else if (inner.type == getString(R.string.weekly)) {
                                    // get repeat
                                    val selectedDateDay = binding.tvDate.text.toString()
                                        .dateFormat(dateFormat, dayFormat)
                                    inner.days?.forEach { day ->
                                        if (day.lowercase() == selectedDateDay.lowercase()) {
                                            if (selectStartTime.before(innerStartTime) ||
                                                selectStartTime.after(innerEndTime) ||
                                                selectEndTime.before(innerStartTime) ||
                                                selectEndTime.after(innerEndTime)
                                            ) {
                                                performTask = false
                                            } else {
                                                performTask = true
                                                return@loop
                                            }
                                        }
                                    }
                                    if (index == mAstrologerTimeSlotList.size - 1) {
                                        getRepeatTimeSlot()
                                        return@let
                                    }
                                } else if (inner.type == getString(R.string.repeat)) {
                                    // get repeat
                                    val simpleRepeatDateFormat = SimpleDateFormat(dateDBFormat)
                                    val selectedStringDate = binding.tvDate.text.toString()
                                        .dateFormat(dateFormat, dateDBFormat)
                                    val selectedDate =
                                        simpleRepeatDateFormat.parse(selectedStringDate)
                                    val innerStartDate =
                                        simpleRepeatDateFormat.parse(inner.startDate)
                                    val innerEndDate = simpleRepeatDateFormat.parse(inner.endDate)
                                    if ((selectedDate.after(innerStartDate) && selectedDate.before(
                                            innerEndDate
                                        )) ||
                                        selectedStringDate == inner.startDate ||
                                        selectedStringDate == inner.endDate
                                    ) {
                                        if (selectStartTime.before(innerStartTime) ||
                                            selectStartTime.after(innerEndTime) ||
                                            selectEndTime.before(innerStartTime) ||
                                            selectEndTime.after(innerEndTime)
                                        ) {
                                            performTask = false
                                        } else {
                                            performTask = true
                                            return@loop
                                        }
                                    }
                                }
                            }
                            if (mAstrologerTimeSlotList.isEmpty() && request == getString(R.string.custom)) {
                                getWeeklyTimeSlot()
                                return@let
                            } else if (mAstrologerTimeSlotList.isEmpty() && request == getString(R.string.weekly)) {
                                getRepeatTimeSlot()
                                return@let
                            }
                        }
                        hideProgress()
                        if (performTask) {
                            // astrologer available check if astrologer have any booking in same time
                            bookingViewModel.getAllAstrologerrBookingRequestWithDate(
                                astrologerModel.uid.toString(),
                                binding.tvDate.text.toString()
                                    .dateFormat(dateFormat, dateDBFormat)
                            )
//                            performSave()
                        } else {
                            binding.root.showSnackBarToast(getString(R.string.astrologer_not_available))
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        // get astrologer bookings
        bookingViewModel.getBookingListDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    var performTask = false
                    it.data.let { result ->
                        var astrologerBookingStartTime = ""
                        var astrologerBookingEndTime = ""
                        run loop@{
                            result?.forEachIndexed { index, inner ->
                                val simpleRepeatDateFormat = SimpleDateFormat(dateDBFormat)
                                val selectedStringDate = binding.tvDate.text.toString()
                                    .dateFormat(dateFormat, dateDBFormat)
                                val selectedDate = simpleRepeatDateFormat.parse(selectedStringDate)
                                val mFormat = SimpleDateFormat("$dateFormat $timeFormat")
                                var selectetStartTime: Date =
                                    mFormat.parse("${binding.tvDate.text} $startTime")
                                var selectetEndTime: Date =
                                    mFormat.parse("${binding.tvDate.text} $endTime")
                                val selectetStringStartTime =
                                    selectetStartTime.dateToStringFormat(timeFormat)
                                val selectetStringEndTime =
                                    selectetEndTime?.dateToStringFormat(timeFormat)
                                val innerStringStartTime =
                                    inner.startTime?.dateToStringFormat(timeFormat)
                                val innerStringEndTime =
                                    inner.endTime?.dateToStringFormat(timeFormat)
                                if (selectedStringDate == inner.date) {
                                    if ((selectetStartTime.after(inner.startTime) &&
                                                selectetEndTime.before(inner.endTime)) ||
                                        (selectetStartTime.before(inner.startTime) &&
                                                selectetEndTime.after(inner.startTime)) ||
                                        (selectetStartTime.before(inner.endTime) &&
                                                selectetEndTime.after(inner.endTime)) ||
                                        selectetStringStartTime == innerStringStartTime ||
                                        selectetStringEndTime == innerStringEndTime
                                    ) {
                                        performTask = false
                                        astrologerBookingStartTime = innerStringStartTime.toString()
                                        astrologerBookingEndTime = innerStringEndTime.toString()
                                        return@loop
                                    } else {
                                        performTask = true
                                    }
                                }
                            }
                        }

                        if (result!!.isEmpty()) performTask = true

                        if (performTask) {
                            performSave()
                        } else {
                            binding.root.showSnackBarToast("Astrologer have already meeting $astrologerBookingStartTime to $astrologerBookingEndTime")
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
     * get weekly timeslot
     */
    private fun getWeeklyTimeSlot() {
        // custom time not available call weekly
        request = getString(R.string.weekly)
        timeSlotViewModel.getWeeklyTimeSlotList(
            astrologerModel.uid.toString(),
            binding.tvDate.text.toString()
                .dateFormat(dateFormat, dateDBFormat)
        )
    }

    /**
     * get repeat timeslot
     */
    private fun getRepeatTimeSlot() {
        // weekly not available call repeat
        request = getString(R.string.repeat)
        timeSlotViewModel.getRepeatTimeSlotList(
            astrologerModel.uid.toString(),
            binding.tvDate.text.toString()
                .dateFormat(dateFormat, dateDBFormat)
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

    /**
     * check wallet balance
     */
    private fun checkWalletBalance(isFirstTime: Boolean) {
        if (isFirstTime) {
            if (userModel.walletBalance!! > 0) {
                binding.tvPaymentMode.isEnabled = true
            }
        } else {
            if (binding.tvAmount.text.toString().toInt() < userModel.walletBalance!!) {
                binding.tvPaymentMode.isEnabled = true
            } else {
                binding.tvPaymentMode.isEnabled = false
                binding.tvPaymentMode.text = getString(R.string.pay_with_other)
            }
        }
    }

    /**
     * redirect to payment
     */
    private fun redirectToPayment() {
        setBookingModel()
        startForResult.launch(
            Intent(this, PaymentActivity::class.java)
                .putExtra(Constants.INTENT_AMOUNT, totalamount.toString())
                .putExtra(Constants.INTENT_MODEL, astrologerModel)
                .putExtra(Constants.INTENT_IS_DIRECT_PAYMENT, true)
                .putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel)
                .putExtra(Constants.INTENT_PHOTO_URI, photoPath.toString())
                .putExtra(Constants.INTENT_KUNDALI_URI, kundaliPath.toString())
        )
    }

    /**
     * set result
     */
    private fun setResult() {
        setResult(
            Activity.RESULT_OK,
            Intent().putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel)
        )
        onBackPressed()
    }

    /**
     * manage click listener of view
     */
    private fun setClickListener() {
        binding.imgClose.setOnClickListener {
            onBackPressed()
        }

        // this will allow inside scroll on multiline edit text if you have parent nested scrollview
        binding.edDetails.setOnTouchListener { v, event ->
            if (v.id == R.id.edDetails) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }

        dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }

        binding.tvDate.setOnClickListener {
            hideKeyboard()
            showDatePicker()
        }

        binding.tvStartTime.setOnClickListener {
            hideKeyboard()
            isFromTimeClick = true
            customListBalloon.showAlignBottom(it, 0, 10)
        }

        binding.tvEndTime.setOnClickListener {
            hideKeyboard()
            customListEventDurationBalloon.showAlignBottom(it, 0, 10)
        }

        binding.tvNotify.setOnClickListener {
            showNotificationDialog()
        }
        binding.tvPaymentMode.setOnClickListener {
            showPaymentModeDialog()
        }
        binding.tvSave.setOnClickListener {

            hideKeyboard()
            if (checkValidation()) {
                request = getString(R.string.custom)

                timeSlotViewModel.getCustomTimeSlotList(
                    astrologerModel.uid!!,
                    binding.tvDate.text.toString().dateFormat(dateFormat, dateDBFormat)
                )
            }
        }

        binding.btnCall.setOnClickListener {
            redirectToVideoCallActivity()
        }
        binding.imgChat.setOnClickListener {
            redirectChatActivity(userModel)
        }
        binding.imgDelete.setOnClickListener {
            bookingModel.status = Constants.CANCEL_STATUS
            addUpdateEvent()
        }

        binding.btnChat.setOnClickListener {
            if (bookingModel.endTime!!.time - Date().time <= 0L) {
                toast(getString(R.string.meeting_time_over))
                binding.btnCall.makeGone()
                binding.btnChat.makeGone()
            } else {
                redirectChatActivity(userModel)
            }
        }

        binding.tvDateOfBirth.setOnClickListener {
            var datePickerDialog = DatePickerDialog(
                this@EventBookingActivity, date, calBirthdate[Calendar.YEAR],
                calBirthdate[Calendar.MONTH],
                calBirthdate[Calendar.DAY_OF_MONTH]
            )
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis();
            datePickerDialog.setTitle(resources.getString(R.string.date_of_birth))
            datePickerDialog.show()
        }

        binding.tvTimeOfBirth.setOnClickListener {
            val mcurrentTime = Calendar.getInstance()
            val hour = mcurrentTime[Calendar.HOUR_OF_DAY]
            val minute = mcurrentTime[Calendar.MINUTE]
            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(
                this@EventBookingActivity,
                { _, selectedHour, selectedMinute ->

                    var timeCalnder = Calendar.getInstance()
                    timeCalnder.set(Calendar.HOUR_OF_DAY, selectedHour)
                    timeCalnder.set(Calendar.MINUTE, selectedMinute)

                    val myFormat = "hh:mm a" // In which you need put here
                    val sdf = SimpleDateFormat(myFormat, Locale.US)

                    binding.tvTimeOfBirth.setText(sdf.format(timeCalnder.time))
                },
                hour,
                minute,
                false
            ) // Yes 24 hour time

            mTimePicker.setTitle(resources.getString(R.string.time_of_birth))
            mTimePicker.show()
        }

        binding.tvUploadPhoto.setOnClickListener {
            if (!intent.hasExtra(Constants.INTENT_IS_FROM)) {
                // comes from select astrologer
                pickImage(RC_UPLOAD_IMAGE)
            } else {
                // comes from upcoming/ongoing/past
                redirectToImageView(bookingModel.photo)
            }
        }

        binding.tvUploadKundali.setOnClickListener {
            if (!intent.hasExtra(Constants.INTENT_IS_FROM)) {
                // comes from select astrologer
                pickImage(RC_UPLOAD_KUNDALI)
            } else {
                // comes from upcoming/ongoing/past
                redirectToImageView(bookingModel.kundali)
            }
        }
    }

    private fun redirectToVideoCallActivity() {
        if (!isComeFromNotification)
            return
        isComeFromNotification = false
        Constants.USER_NAME = bookingModel.userName // if user comes from notification this field is blank so assigning again
        TedPermission.with(this)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {

                    if (bookingModel.endTime!!.time - Date().time <= 0L) {
                        toast(getString(R.string.meeting_time_over))
                        binding.btnCall.makeGone()
                        binding.btnChat.makeGone()
                    } else {
                        val intent = Intent(this@EventBookingActivity, JitsiCallActivity::class.java)
                        intent.putExtra("RoomId", bookingModel.id)
//                            intent.putExtra("OpponentUserName", "")
//                            intent.putExtra("isGroupCall", isGroup)
                        intent.putExtra(Constants.INTENT_MODEL, astrologerModel)
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
                            userIds.add(bookingModel.astrologerID)
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
                                        this@EventBookingActivity,
                                        JitsiCallActivity::class.java
                                    )
                                intent.putExtra("RoomId", i.docId)
                                intent.putExtra("OpponentUserName", bookingModel.astrologerName)
                                intent.putExtra("isGroupCall", isGroup)
                                intent.putExtra(Constants.INTENT_MODEL, astrologerModel)
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
                ImageViewActivity::class.java
            ).putExtra(Constants.INTENT_IMAGE_URL, path)
        )
    }

    var date =
        DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calBirthdate.set(Calendar.YEAR, year)
            calBirthdate.set(Calendar.MONTH, monthOfYear)
            calBirthdate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateBirthDateLabel()
        }
    private fun updateBirthDateLabel() {
        val myFormat = birthdateFormat // In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.tvDateOfBirth.setText(sdf.format(calBirthdate.time))
    }
    /**
     * perform save click
     */
    private fun performSave() {
        if (isEdit) {
            addUpdateEvent()
        } else {
            onSave = true
            chargableMin = getMin()
            if (binding.tvPaymentMode.text == getString(R.string.pay_with_other)) {
                // redirect to razor pay for direct payment
                isFromWallet = false
                redirectToPayment()
            } else if (userModel.walletBalance!!.toInt() > totalamount) {
                // deduct from wallet
                isFromWallet = true
                walletModel.amount = totalamount
                walletModel.paymentType = Constants.PAYMENT_TYPE_WALLET
                addUpdateEvent()
            } else {
                // redirect to razor pay for direct payment
                isFromWallet = false
                redirectToPayment()
            }
        }
    }

    /**
     * adding booking event
     */
    private fun addUpdateEvent() {
        bookingModel.astrologerName = astrologerModel.fullName.toString()
        bookingModel.astrologerPerMinCharge = astrologerModel.price
        bookingModel.description = binding.edDetails.text.toString()
        bookingModel.date = binding.tvDate.text.toString().dateFormat(
            dateFormat,
            dateDBFormat
        )

        val mFormat = SimpleDateFormat("$dateFormat $timeFormat")
        var startDate: Date = mFormat.parse("${binding.tvDate.text} $startTime")
        var endDate: Date = mFormat.parse("${binding.tvDate.text} $endTime")

        bookingModel.startTime = startDate

        bookingModel.endTime = endDate
        bookingModel.month = "${binding.tvDate.text}".dateFormat(dateFormat, "MM")
        bookingModel.year = "${binding.tvDate.text}".dateFormat(dateFormat, "yyyy")
        bookingModel.userId = userId.toString()
        bookingModel.userName = userModel.fullName.toString()
        bookingModel.userBirthday = userModel.birthDate.toString()
        bookingModel.userProfileImage = userModel.profileImage.toString()

        bookingModel.fullname = binding.edtUserName.text.toString().trim()
        bookingModel.birthDate = binding.tvDateOfBirth.text.toString().trim()
        bookingModel.birthTime = binding.tvTimeOfBirth.text.toString().trim()
        bookingModel.birthPlace = binding.edtPlaceOfBirth.text.toString().trim()

        bookingModel.astrologerID = astrologerModel.uid.toString()
        bookingModel.notify = binding.tvNotify.text.toString()
        bookingModel.notificationMin =
            when {
                binding.tvNotify.text.toString() == getString(R.string.no_notification) -> {
                    0
                }
                binding.tvNotify.text.toString() == getString(R.string._5_minutes_before) -> {
                    5
                }
                binding.tvNotify.text.toString() == getString(R.string._10_minutes_before) -> {
                    10
                }
                binding.tvNotify.text.toString() == getString(R.string._15_minutes_before) -> {
                    15
                }
                binding.tvNotify.text.toString() == getString(R.string._30_minutes_before) -> {
                    30
                }
                binding.tvNotify.text.toString() == getString(R.string._1_hour_before) -> {
                    60
                }
                binding.tvNotify.text.toString() == getString(R.string._1_day_before) -> {
                    1440
                }
                else -> {
                    0
                }
            }
        bookingModel.transactionId = walletModel.trancationId
        if (walletModel.paymentType == Constants.PAYMENT_TYPE_WALLET) {
            bookingModel.paymentStatus = ""
        } else {
            bookingModel.paymentStatus = Constants.RAZOR_PAY_STATUS_AUTHORIZED
        }
        bookingModel.paymentType = walletModel.paymentType
        bookingModel.amount = walletModel.amount

//        bookingViewModel.addUpdateBookingData(
//            bookingModel,
//            isEdit
//        )

        bookingViewModel.uploadPhotoAndKundaliForAddBooking(bookingModel,photoPath,kundaliPath,isEdit)
    }

    /**
     * setting data to booking model
     */
    private fun setBookingModel() {
        bookingModel.astrologerName = astrologerModel.fullName.toString()
        bookingModel.astrologerPerMinCharge = astrologerModel.price
        bookingModel.description = binding.edDetails.text.toString()
        bookingModel.date = binding.tvDate.text.toString().dateFormat(
            dateFormat,
            dateDBFormat
        )

        val mFormat = SimpleDateFormat("$dateFormat $timeFormat")
        var startDate: Date = mFormat.parse("${binding.tvDate.text} $startTime")
        var endDate: Date = mFormat.parse("${binding.tvDate.text} $endTime")

        bookingModel.startTime = startDate
        bookingModel.endTime = endDate
        bookingModel.month = "${binding.tvDate.text}".dateFormat(dateFormat, "MM")
        bookingModel.year = "${binding.tvDate.text}".dateFormat(dateFormat, "yyyy")
        bookingModel.userId = userId.toString()
        bookingModel.userName = userModel.fullName.toString()
        bookingModel.userBirthday = userModel.birthDate.toString()
        bookingModel.userProfileImage = userModel.profileImage.toString()

        bookingModel.fullname = binding.edtUserName.text.toString().trim()
        bookingModel.birthDate = binding.tvDateOfBirth.text.toString().trim()
        bookingModel.birthTime = binding.tvTimeOfBirth.text.toString().trim()
        bookingModel.birthPlace = binding.edtPlaceOfBirth.text.toString().trim()

        bookingModel.astrologerID = astrologerModel.uid.toString()
        bookingModel.notify = binding.tvNotify.text.toString()
        bookingModel.notificationMin =
            when {
                binding.tvNotify.text.toString() == getString(R.string.no_notification) -> {
                    0
                }
                binding.tvNotify.text.toString() == getString(R.string._5_minutes_before) -> {
                    5
                }
                binding.tvNotify.text.toString() == getString(R.string._10_minutes_before) -> {
                    10
                }
                binding.tvNotify.text.toString() == getString(R.string._15_minutes_before) -> {
                    15
                }
                binding.tvNotify.text.toString() == getString(R.string._30_minutes_before) -> {
                    30
                }
                binding.tvNotify.text.toString() == getString(R.string._1_hour_before) -> {
                    60
                }
                binding.tvNotify.text.toString() == getString(R.string._1_day_before) -> {
                    1440
                }
                else -> {
                    0
                }
            }
        bookingModel.transactionId = walletModel.trancationId
        if (walletModel.paymentType == Constants.PAYMENT_TYPE_WALLET) {
            bookingModel.paymentStatus = ""
        } else {
            bookingModel.paymentStatus = Constants.RAZOR_PAY_STATUS_AUTHORIZED
        }
        bookingModel.paymentType = walletModel.paymentType
        bookingModel.amount = walletModel.amount
    }

    /**
     * update date in date
     */
    private fun updateDateInView() {
        val myFormat = dateFormat // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.tvDate.text = sdf.format(cal.time)
    }

    /**
     * show date picker
     */
    private fun showDatePicker() {
        DatePickerDialog(
            this,
            dateSetListener,
            // set DatePickerDialog to point to today's date when it loads up
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).run {
            datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.maxDate =
                (System.currentTimeMillis() - 1000) + (1000 * 60 * 60 * 24 * 10) //After current(1)+10 Days from Now
            show()
        }
    }

    /**
     * Redirect to chat activity after click on user
     */
    fun redirectChatActivity(userModel: UserModel) {

        val userlist = ArrayList<String>()
        userlist.add(0, userId.toString())
        userlist.add(1, bookingModel.astrologerID)

        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("isGroup", false)
        intent.putExtra("userList", Gson().toJson(ArrayList<UserModel>()))
        intent.putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel)

        intent.putExtra("user_id", bookingModel.astrologerID)
        intent.putExtra("user_name", bookingModel.astrologerName)
        intent.putExtra(Constants.INTENT_MODEL, astrologerModel)
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
     * Checking validation
     */
    private fun checkValidation(): Boolean {
        val mBirthFormat = SimpleDateFormat("$birthdateFormat $timeFormat")
        var birthDate: Date =
            if (binding.tvDateOfBirth.text.toString().trim() != getString(R.string.date_of_birth) &&
                binding.tvTimeOfBirth.text.toString()
                    .trim() != getString(R.string.time_of_birth)
            ) {
                mBirthFormat.parse("${binding.tvDateOfBirth.text} ${binding.tvTimeOfBirth.text}")
            } else Date()

        if (TextUtils.isEmpty(binding.edDetails.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_details))
            return false
        } else if (binding.tvDate.text.toString().trim() == getString(R.string.select_date)) {
            binding.root.showSnackBarToast(getString(R.string.please_select_date))
            return false
        } else if (binding.tvStartTime.text.toString().trim() == getString(R.string.start_time)) {
            binding.root.showSnackBarToast(getString(R.string.please_select_start_time))
            return false
        } else if (binding.tvEndTime.text.toString().trim() == getString(R.string.event_duration)) {
            binding.root.showSnackBarToast(getString(R.string.please_select_duration))
//            binding.root.showSnackBarToast(getString(R.string.please_select_end_time))
            return false
        } else if (photoPath == null) {
            binding.root.showSnackBarToast(getString(R.string.please_upload_your_photo))
            return false
        } else if (binding.edtUserName.text.toString().trim().isNullOrBlank()) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_first_name))
            return false
        } else if (binding.tvDateOfBirth.text.toString()
                .trim() == getString(R.string.date_of_birth)
        ) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_birth_date))
            return false
        } else if (binding.tvTimeOfBirth.text.toString()
                .trim() == getString(R.string.time_of_birth)
        ) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_birth_time))
            return false
        } else if (birthDate.after(Date())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_valid_birth_time))
            return false
        } else if (TextUtils.isEmpty(binding.edtPlaceOfBirth.text.toString())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_birth_place))
            return false
        } else if (kundaliPath == null) {
            binding.root.showSnackBarToast(getString(R.string.please_upload_your_kundali))
            return false
        } else if (binding.tvPaymentMode.text.toString()
                .trim() == getString(R.string.select_payment_mode)
        ) {
            binding.root.showSnackBarToast(getString(R.string.please_select_payment_mode))
            return false
        } else if (astrologerModel.price <= 0) {
            binding.root.showSnackBarToast(getString(R.string.astrologer_price_validation))
            return false
        } else if (binding.tvDate.text.toString().trim().isNotBlank() &&
            binding.tvStartTime.text.toString().trim().isNotBlank() &&
            binding.tvEndTime.text.toString().trim().isNotBlank()
        ) {
            val myFormat = dateFormat // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            val selectedDate = sdf.format(cal.time)
            val currentDate = sdf.format(Date())

//            lslsd
            val mFormat = SimpleDateFormat("$dateFormat $timeFormat")
            var startDate: Date = mFormat.parse("${binding.tvDate.text} $startTime")
            var endDate: Date = mFormat.parse("${binding.tvDate.text} $endTime")
            var currentTime = Date()

            if (selectedDate == currentDate) {
                if (startDate.before(currentTime)) {
                    binding.root.showSnackBarToast(getString(R.string.please_select_valid_start_time))
                    return false
                } else if (endDate.before(startDate)) {
                    binding.root.showSnackBarToast(getString(R.string.please_select_valid_end_time))
                    return false
                } else if (startDate == endDate) {
                    binding.root.showSnackBarToast(getString(R.string.please_select_valid_time))
                    return false
                } /*else if (mAstrologerTimeSlotList.isEmpty()) {
                    binding.root.showSnackBarToast(getString(R.string.astrologer_profile_not_completed))
                    return false
                } else if (mAstrologerTimeSlotList.isNotEmpty()) {
                    return checkAstrologerAvailable()
                }*/
                return true
            } else if (startDate.after(endDate) || endDate.before(startDate) || startDate == endDate) {
                binding.root.showSnackBarToast(getString(R.string.please_select_valid_time))
                return false
            } /*else if (mAstrologerTimeSlotList.isEmpty()) {
                binding.root.showSnackBarToast(getString(R.string.astrologer_profile_not_completed))
                return false
            } else if (mAstrologerTimeSlotList.isNotEmpty()) {
                return checkAstrologerAvailable()
            }*/
            return true
        }
        return true
    }

    private fun checkAstrologerAvailable(): Boolean {
        val simpleDateFormat = SimpleDateFormat(timeFormat)

        val myStartTime = simpleDateFormat.parse(startTime)
        val myEndTime = simpleDateFormat.parse(endTime)
        mAstrologerTimeSlotList.forEachIndexed { index, model ->
            val astrologerStartTime = simpleDateFormat.parse(model.fromTime)
            val astrologerEndTime = simpleDateFormat.parse(model.toTime)
            if ((myStartTime.after(astrologerStartTime) && myEndTime.before(astrologerEndTime)) ||
                (myStartTime.after(astrologerStartTime) && myEndTime == astrologerEndTime) ||
                (myStartTime == astrologerStartTime && myEndTime.before(astrologerEndTime)) ||
                (myStartTime == astrologerStartTime && myEndTime == astrologerEndTime)
            ) {
                return true
            }
        }
        binding.root.showSnackBarToast(getString(R.string.astrologer_not_available))
        return false
    }
    /**
     * Check camera and read write permission and open camera and image picker
     */
    private fun pickImage(requestCode: Int) {
        TedPermission.with(this@EventBookingActivity)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {

                    FilePickerBuilder.instance
                        .setMaxCount(1)
                        .setActivityTheme(R.style.FilePickerTheme)
                        .setActivityTitle("Please select image")
                        .enableVideoPicker(false)
                        .enableCameraSupport(true)
                        .showGifs(false)
                        .showFolderView(true)
                        .enableSelectAll(false)
                        .enableImagePicker(true)
                        .setCameraPlaceholder(R.drawable.ic_camera_pic)
                        .withOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .pickPhoto(this@EventBookingActivity, requestCode)
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    // onPermissionDenied
                }
            }).setDeniedMessage(getString(R.string.permission_denied))
            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            .check()
    }
    /**
     * Checking image picker and cropper result after image selection
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                RC_UPLOAD_IMAGE -> {
                    val dataList =
                        data.getParcelableArrayListExtra<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)
                    if (dataList != null && dataList.size > 0) {
                        binding.imgUploadPhoto.loadRoundedImage(
                            ContentUriUtils.getFilePath(
                                this,
                                dataList[0]
                            )
                        )
                        photoPath = dataList[0]
//                    profileImagePath = resultUri
                    }
                }

                RC_UPLOAD_KUNDALI -> {
                    val dataList =
                        data.getParcelableArrayListExtra<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)
                    if (dataList != null && dataList.size > 0) {
                        binding.imgUploadKundali.loadProfileImage(
                            ContentUriUtils.getFilePath(
                                this,
                                dataList[0]
                            )
                        )
                        kundaliPath = dataList[0]
//                    profileImagePath = resultUri
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (checkIsLastActivity()) {
            //last activity go to Dashboard
            startActivity(
                Intent(this, SplashActivity::class.java)
                    .putExtra(Constants.INTENT_SHOW_TIMER, false)
                    .putExtra(Constants.INTENT_USER_TYPE, Constants.USER_NORMAL)
            )
            finishAffinity()
        } else {
            super.onBackPressed()
        }
    }

    /**
     * manage notification intent
     */
    var isComeFromNotification = false // somehow video screen opening twice if new intent run this will pervert to open video call screen twice
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        MyLog.e(TAG, "onNewIntent")
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
