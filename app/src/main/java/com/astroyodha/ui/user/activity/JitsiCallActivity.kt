package com.astroyodha.ui.user.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import com.astroyodha.R
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityJitsiCallBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.model.user.AstrologerUserModel
import com.astroyodha.ui.user.model.booking.BookingModel
import com.astroyodha.ui.user.model.calllog.CallLogModel
import com.astroyodha.ui.user.viewmodel.BookingViewModel
import com.astroyodha.ui.user.viewmodel.CallLogViewModel
import com.astroyodha.ui.user.viewmodel.JitsiViewModel
import com.astroyodha.utils.*
import com.facebook.react.modules.core.PermissionListener
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import org.jitsi.meet.sdk.JitsiMeetActivityInterface
import org.jitsi.meet.sdk.JitsiMeetView
import org.jitsi.meet.sdk.JitsiMeetViewListener
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime

@AndroidEntryPoint
class JitsiCallActivity : BaseActivity()
    , JitsiMeetActivityInterface, JitsiMeetViewListener
{

    private val TAG = javaClass.simpleName
    private lateinit var jitsiManager: JitsiManager
    private val jitsiViewModel: JitsiViewModel by viewModels()
    private val bookingViewModel: BookingViewModel by viewModels()
    private val callLogViewModel: CallLogViewModel by viewModels()
    private lateinit var binding: ActivityJitsiCallBinding
    var roomId = ""
    lateinit var view:JitsiMeetView
//    var username=""
//    var isGroupCall=false


    //For Auto call drop
    var endTime = 0L

    private var timer: CountDownTimer? = null

    var callLogModel = CallLogModel()
    var bookingModel: BookingModel = BookingModel()
    var astrologerModel: AstrologerUserModel = AstrologerUserModel()
    private var showExtendDialogOnStart = true
    private var extendTime = 0
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                bookingModel = Gson().fromJson(
                    result.data?.getStringExtra(Constants.INTENT_BOOKING_MODEL),
                    BookingModel::class.java
                )
                //update timer
                endTime = bookingModel.endTime!!.time
                bookingModel.extendedTimeInMinute = 0
                bookingModel.allowExtendTIme = Constants.EXTEND_STATUS_NO
                bookingViewModel.extendBookingMinute(
                    bookingModel
                )
                timer?.cancel()
                setTime(endTime)
                addExtendTimeLog()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJitsiCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Constants.IS_VIDEO_SCREEN_ACTIVE = Constants.USER_VIDEO_SCREEN

        // pervert screen to dim or lock
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        roomId = intent.getStringExtra("RoomId")!!
//        username=intent.getStringExtra("OpponentUserName")!!
//        isGroupCall=intent.getBooleanExtra("isGroupCall",false)!!

        bookingModel = intent.getParcelableExtra(Constants.INTENT_BOOKING_MODEL)!!
        astrologerModel = intent.getParcelableExtra(Constants.INTENT_MODEL)!!

        bookingViewModel.getBookingDetail(bookingModel.id)

        setObserver()
        startCall()
        setCallLogData()
    }

    fun startCall() {
        jitsiManager = JitsiManager(this)
        view = jitsiManager.startCustomVideoCall(roomId) as JitsiMeetView
        view.listener = this
        binding.layoutJitsi.addView(view)
        binding.txtRemainingTime.bringToFront()
    }

    /**
     * set call log
     */
    private fun setCallLogData() {
        callLogModel.startTime = Date()
//        callLogModel.endTime = ""
        callLogModel.userId = bookingModel.userId
        callLogModel.userName = bookingModel.userName
        callLogModel.userType = Constants.USER_NORMAL
        callLogViewModel.addCallLogData(bookingModel.id, callLogModel)
    }

    private fun endCallLog() {
        callLogModel.endTime = Date()
        callLogViewModel.updateCallLogData(bookingModel.id, callLogModel)
    }

    private fun addExtendTimeLog() {
        callLogModel.extendCount += 1
        callLogModel.extendMin += extendTime
        callLogViewModel.updateCallLogData(bookingModel.id, callLogModel)
    }

    override fun requestPermissions(p0: Array<out String>?, p1: Int, p2: PermissionListener?) {
        // requestPermissions
    }

    override fun onConferenceJoined(p0: MutableMap<String, Any>?) {
        // onConferenceJoined
    }

    override fun onConferenceTerminated(p0: MutableMap<String, Any>?) {
//        jitsiViewModel.changeStatus(roomId,false)
        endCallLog()
        if (bookingModel.status != Constants.COMPLETED_STATUS) {
            finish()
        } else {
            // comes when call finish
            startFromFresh()
        }

    }

    override fun onConferenceWillJoin(p0: MutableMap<String, Any>?) {
//        jitsiViewModel.changeStatus(roomId,true)

        endTime = bookingModel.endTime!!.time

        setTime(endTime)
        binding.txtRemainingTime.makeVisible()


    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(view!=null) {
//            jitsiViewModel.changeStatus(roomId,false)
            view.leave()
        }
    }


    fun setTime(endTime: Long) {
        val calendars = Calendar.getInstance()
        val currentTime = Date().time

        if (endTime > currentTime) {
            val time = endTime - currentTime //startTime - endTime
            startCountdownTimer(time)
        } else {
//            jitsiViewModel.changeStatus(roomId,false)
            view.leave()
            toast(getString(R.string.meeting_time_over))
            return
        }
    }


    @OptIn(ExperimentalTime::class)
    fun startCountdownTimer(time: Long) {

        timer?.cancel()
        timer = object : CountDownTimer(time, 1000) {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTick(millisUntilFinished: Long) {

                val totalSecs = millisUntilFinished / 1000
                val hour = totalSecs / 3600
                val minutes = (totalSecs % 3600) / 60
                val seconds = totalSecs % 60

                val totalTime = TimeUnit.HOURS.toMillis(hour) + TimeUnit.MINUTES.toMillis(minutes) + TimeUnit.SECONDS.toMillis(
                    seconds)
                val tenMin = 600000L // 10 min
                val fixedTime = time - tenMin
                val totalFixedTime = time - fixedTime

                val difference: Long = endTime - Date().time
                val min = TimeUnit.MILLISECONDS.toMinutes(difference).toInt()

                var timeString = String.format(" Ends in : %02d:%02d:%02d", hour, minutes, seconds)

                binding.txtRemainingTime.text = timeString

                if (totalTime > tenMin) {
                    showExtendDialogOnStart = false
                }
                if (totalTime == totalFixedTime || (showExtendDialogOnStart && totalTime < tenMin)) {
                    displayTimeValidationDialog(
                        getString(
                            R.string.your_call_is_completed_in_next_5_min,
                            (minutes + 1).toString()
                        )
                    )
                    showExtendDialogOnStart = false
                } else if (isDialogVisible) {
                    updateTime(
                        getString(
                            R.string.your_call_is_completed_in_next_5_min,
                            (minutes + 1).toString()
                        )
                    )
                }
            }

            override fun onFinish() {
                // update booking status to complete
                bookingModel.status = Constants.COMPLETED_STATUS
                bookingViewModel.addUpdateBookingData(bookingModel, true)
//                jitsiViewModel.changeStatus(roomId,false)
            }
        }
        (timer as CountDownTimer).start()
    }

    lateinit var dialogEndTimeAlert: Dialog
    var isDialogVisible = false
    private fun displayTimeValidationDialog(description: String) {
        try {
            isDialogVisible = true
            dialogEndTimeAlert = Dialog(this)
            dialogEndTimeAlert.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialogEndTimeAlert.setContentView(R.layout.dialog_time_remaining_user)

            val lp = WindowManager.LayoutParams()
            lp.copyFrom(dialogEndTimeAlert.window!!.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            dialogEndTimeAlert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogEndTimeAlert.window!!.attributes = lp
            dialogEndTimeAlert.show()
            dialogEndTimeAlert.setCanceledOnTouchOutside(false)

            val llBtnOk = dialogEndTimeAlert.findViewById<View>(R.id.llBtnOk) as MaterialButton
            val llEdit = dialogEndTimeAlert.findViewById<View>(R.id.llEdit) as MaterialButton
            val llCancel = dialogEndTimeAlert.findViewById<View>(R.id.llCancel) as MaterialButton
            val txtDescription = dialogEndTimeAlert.findViewById<View>(R.id.txtDescription) as AppCompatTextView
            val txtTimeValue = dialogEndTimeAlert.findViewById<View>(R.id.txtTimeValue) as TextView

            txtDescription.text = description
            txtTimeValue.setOnClickListener {
                val popupMenu = PopupMenu(this, it)
                popupMenu.menuInflater.inflate(R.menu.menu_extend_time, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { item ->
                    txtTimeValue.text = item.title.toString()
                    true
                }
                popupMenu.show()
            }
            llEdit.setOnClickListener {
                txtTimeValue.visibility = View.VISIBLE
                llCancel.visibility = View.GONE
                llEdit.visibility = View.GONE
                llBtnOk.visibility = View.VISIBLE
                txtDescription.visibility = View.GONE
            }

            llCancel.setOnClickListener {
                isDialogVisible = false
                dialogEndTimeAlert.dismiss()
            }
            llBtnOk.setOnClickListener {
                bookingModel!!.status = Constants.APPROVE_STATUS
                bookingModel!!.extendedTimeInMinute = txtTimeValue.text.toString().toInt()
                bookingViewModel.extendBookingMinute(
                    bookingModel!!
                )
                toast("Wait for astrologer to accept your extend request.")

//                if (txtTimeValue.text.isBlank()) {
//                    binding.root.showSnackBarToast("Please select minute to extend time")
//                    return@setOnClickListener
//                }
//
//               var  totalamount = 0
//                if (txtTimeValue.text.toString() == "15") {
//                    totalamount = astrologerModel.price
//                } else if (txtTimeValue.text.toString() == "30") {
//                    totalamount = astrologerModel.price * 2
//                } else if (txtTimeValue.text.toString() == "45") {
//                    totalamount = astrologerModel.price * 3
//                } else if (txtTimeValue.text.toString() == "60") {
//                    totalamount = astrologerModel.price * 4
//                }
//
//
//                startForResult.launch(
//                    Intent(this, PaymentActivity::class.java)
//                        .putExtra(Constants.INTENT_AMOUNT, totalamount.toString())
//                        .putExtra(Constants.INTENT_MODEL, astrologerModel)
//                        .putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel)
//                        .putExtra(Constants.INTENT_MINUTE, txtTimeValue.text.toString().toInt())
//                        .putExtra(Constants.INTENT_IS_DIRECT_PAYMENT, true) //direct payment
//                        .putExtra(Constants.INTENT_IS_EXTEND_CALL, true)
//                )
                isDialogVisible = false
                dialogEndTimeAlert.dismiss()
            }
        } catch (e: Exception) {
            // exception
        }
    }

    private fun updateTime(description: String) {

        try {
            val txtDescription =
                dialogEndTimeAlert.findViewById<View>(R.id.txtDescription) as AppCompatTextView

            txtDescription.text = description

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun displayTimeExtendConfirmationDialog(description:String) {
        try {
            val mDialog = Dialog(this)
            mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            mDialog.setContentView(R.layout.dialog_time_remaining_user)

            val lp = WindowManager.LayoutParams()
            lp.copyFrom(mDialog.window!!.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            mDialog.window!!.attributes = lp
            mDialog.show()
            mDialog.setCanceledOnTouchOutside(false)

            val llBtnOk = mDialog.findViewById<View>(R.id.llBtnOk) as MaterialButton
            val llEdit = mDialog.findViewById<View>(R.id.llEdit) as MaterialButton
            val llCancel = mDialog.findViewById<View>(R.id.llCancel) as MaterialButton
            val txtDescription = mDialog.findViewById<View>(R.id.txtDescription) as AppCompatTextView
            val txtTimeValue = mDialog.findViewById<View>(R.id.txtTimeValue) as TextView


            txtTimeValue.visibility = View.GONE
            llCancel.visibility = View.GONE
            llEdit.visibility = View.GONE
            llBtnOk.visibility = View.VISIBLE
            txtDescription.visibility = View.VISIBLE

            txtDescription.text=description
            llBtnOk.text=getString(R.string.make_payment)


            llBtnOk.setOnClickListener {

               var  totalamount = 0
                if (bookingModel.extendedTimeInMinute.toString() == "15") {
                    totalamount = astrologerModel.price
                } else if (bookingModel.extendedTimeInMinute.toString() == "30") {
                    totalamount = astrologerModel.price * 2
                } else if (bookingModel.extendedTimeInMinute.toString() == "45") {
                    totalamount = astrologerModel.price * 3
                } else if (bookingModel.extendedTimeInMinute.toString() == "60") {
                    totalamount = astrologerModel.price * 4
                }

                extendTime = bookingModel.extendedTimeInMinute.toString().toInt()

                startForResult.launch(
                    Intent(this, PaymentActivity::class.java)
                        .putExtra(Constants.INTENT_AMOUNT, totalamount.toString())
                        .putExtra(Constants.INTENT_MODEL, astrologerModel)
                        .putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel)
                        .putExtra(Constants.INTENT_MINUTE, bookingModel!!.extendedTimeInMinute)
                        .putExtra(Constants.INTENT_IS_DIRECT_PAYMENT, true) //direct payment
                        .putExtra(Constants.INTENT_IS_EXTEND_CALL, true)
                )
                mDialog.dismiss()
            }
        } catch (e: Exception) {
            // exception
        }
    }






    /**
     * Set up observer
     */
    private fun setObserver() {
        bookingViewModel.bookingAddUpdateResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    // loading state
                }
                Status.SUCCESS -> {
                    it.data?.let {
                        toast(getString(R.string.call_over))
                        view.leave()
                    }
                }
                Status.ERROR -> {
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }


        bookingViewModel.bookingExtendResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        //booking extended successfully
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
                    bookingModel = it.data!!


                    if (bookingModel.extendedTimeInMinute > 0 && bookingModel.allowExtendTIme == Constants.EXTEND_STATUS_YES) {
                        displayTimeExtendConfirmationDialog(
                            String.format(
                                getString(R.string.time_extend_approval_payment_dialog_message),
                                bookingModel!!.astrologerName
                            )
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

    private fun startFromFresh() {
        // dashboard launch mode is single task it will kill middle screens automatically
        startActivity(
            Intent(this, UserHomeActivity::class.java)
        )
//        finishAffinity()
    }

    override fun onDestroy() {
        super.onDestroy()
        endCallLog()
        Constants.IS_VIDEO_SCREEN_ACTIVE = ""
        timer?.cancel()
    }

}