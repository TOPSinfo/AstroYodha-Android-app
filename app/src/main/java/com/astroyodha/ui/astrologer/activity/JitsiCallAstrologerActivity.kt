package com.astroyodha.ui.astrologer.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.astroyodha.R
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityJitsiCallBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.viewmodel.AstrologerBookingViewModel
import com.astroyodha.ui.astrologer.viewmodel.AstrologerJitsiViewModel
import com.astroyodha.ui.user.model.booking.BookingModel
import com.astroyodha.utils.*
import com.facebook.react.modules.core.PermissionListener
import org.jitsi.meet.sdk.JitsiMeetActivityInterface
import org.jitsi.meet.sdk.JitsiMeetView
import org.jitsi.meet.sdk.JitsiMeetViewListener
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime

class JitsiCallAstrologerActivity : BaseActivity(), JitsiMeetActivityInterface,
    JitsiMeetViewListener {

    private var TAG = javaClass.simpleName

    private lateinit var jitsiManager: JitsiManager
    private val jitsiViewModel: AstrologerJitsiViewModel by viewModels()
    private lateinit var binding: ActivityJitsiCallBinding
    var roomId = ""
    lateinit var view: JitsiMeetView
    var username = ""
    var isGroupCall = false

    //For Auto call drop
    var endTime = 0L

    private var timer: CountDownTimer? = null

    var bookingModel: BookingModel = BookingModel()

    private val bookingViewModel: AstrologerBookingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJitsiCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        roomId = intent.getStringExtra("RoomId")!!
        username = intent.getStringExtra("OpponentUserName")!!
        isGroupCall = intent.getBooleanExtra("isGroupCall", false)!!
        bookingModel = intent.getParcelableExtra(Constants.INTENT_BOOKING_MODEL)!!

        setObserver()
        //Get detail of booking
        bookingViewModel.getBookingDetail(bookingModel.id)
        startCall()
    }


    fun startCall() {
        jitsiManager = JitsiManager(this)
        view = jitsiManager.startCustomVideoCall(roomId) as JitsiMeetView
        view.listener = this
        binding.layoutJitsi.addView(view)
        binding.txtRemainingTime.bringToFront()
    }

    /**
     * set observer
     */
    private fun setObserver() {
        bookingViewModel.getBookingDetailResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    bookingModel = it.data!!
                    endTime = bookingModel.endTime!!.time
                    if (bookingModel.status != Constants.COMPLETED_STATUS) {
                        if (timer != null) {
                            timer!!.cancel()
                        }
                        setTime(endTime)
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
                        toast(getString(R.string.call_over))
                        view.leave()
                        startFromFresh()
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
     * start From dashboard
     */
    private fun startFromFresh() {
        startActivity(
            Intent(this, AstrologerDashboardActivity::class.java)
        )
        finishAffinity()
    }


    override fun requestPermissions(p0: Array<out String>?, p1: Int, p2: PermissionListener?) {
    }

    override fun onConferenceJoined(p0: MutableMap<String, Any>?) {
    }

    override fun onConferenceTerminated(p0: MutableMap<String, Any>?) {
        jitsiViewModel.changeStatus(roomId, false)
        if (bookingModel.status != Constants.COMPLETED_STATUS) {
            finish()
        }


    }

    override fun onConferenceWillJoin(p0: MutableMap<String, Any>?) {
        jitsiViewModel.changeStatus(roomId, true)
        endTime = bookingModel.endTime!!.time

        setTime(endTime)
        binding.txtRemainingTime.makeVisible()
    }

    /**
     * manage back press
     */
    override fun onBackPressed() {
        super.onBackPressed()
        if (view != null) {
            jitsiViewModel.changeStatus(roomId, false)
            view.leave()
        }
    }


    fun setTime(endTime: Long) {
        val currentTime = Date().time
        if (endTime > currentTime) {
            val time = endTime - currentTime //startTime - endTime
            startCountdownTimer(time)
        } else {
            jitsiViewModel.changeStatus(roomId, false)
            view.leave()
            toast(getString(R.string.meeting_time_over))
            return
        }
    }

    /**
     * start count down timer for auto drop
     */
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

                val totalTime =
                    TimeUnit.HOURS.toMillis(hour) + TimeUnit.MINUTES.toMillis(minutes) + TimeUnit.SECONDS.toMillis(
                        seconds
                    )
                val fixedTime = time - 600000L // 10 min
                val totalFixedTime = time - fixedTime

                var timeString = String.format(" Ends in : %02d:%02d:%02d", hour, minutes, seconds)

                binding.txtRemainingTime.text = timeString

                if (totalTime == totalFixedTime) {
                    displayTimeValidationDialog(getString(R.string.call_will_end_in_5_min))
                }
            }

            override fun onFinish() {
                bookingModel.status = Constants.COMPLETED_STATUS
                bookingViewModel.addUpdateBookingData(bookingModel, true)
            }
        }
        (timer as CountDownTimer).start()
    }

    /**
     * show remaining time dialog
     */
    private fun displayTimeValidationDialog(description: String) {

        try {
            val dialogEndTimeAlert = Dialog(this)
            dialogEndTimeAlert.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialogEndTimeAlert.setContentView(R.layout.dialog_time_remaining_astrologer)

            val lp = WindowManager.LayoutParams()
            lp.copyFrom(dialogEndTimeAlert.window!!.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            dialogEndTimeAlert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogEndTimeAlert.window!!.attributes = lp

            val llBtnOk = dialogEndTimeAlert.findViewById<View>(R.id.llBtnOk) as AppCompatButton
            val txtDescription =
                dialogEndTimeAlert.findViewById<View>(R.id.txtDescription) as AppCompatTextView

            txtDescription.text = description

            val remainingTime =
                pref.getValueLong(this, Constants.REMAINING_TIME, 0L, Constants.PREF_FILE)
            llBtnOk.setOnClickListener {
                dialogEndTimeAlert.dismiss()
            }
            dialogEndTimeAlert.show()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer!!.cancel()
    }
}