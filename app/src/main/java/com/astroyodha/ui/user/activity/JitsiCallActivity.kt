package com.astroyodha.ui.user.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import com.astroyodha.R
import com.astroyodha.core.BaseActivity
import com.facebook.react.modules.core.PermissionListener
import com.astroyodha.databinding.ActivityJitsiCallBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.model.user.AstrologerUserModel
import com.astroyodha.ui.user.model.booking.BookingModel
import com.astroyodha.ui.user.viewmodel.BookingViewModel
import com.astroyodha.ui.user.viewmodel.JitsiViewModel
import com.astroyodha.utils.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import org.jitsi.meet.sdk.JitsiMeetActivityInterface
import org.jitsi.meet.sdk.JitsiMeetView
import org.jitsi.meet.sdk.JitsiMeetViewListener
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@AndroidEntryPoint
class JitsiCallActivity : BaseActivity()
    , JitsiMeetActivityInterface, JitsiMeetViewListener
{

//    private val broadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            onBroadcastReceived(intent)
//        }
//    }

    private val TAG = javaClass.simpleName
    private lateinit var jitsiManager: JitsiManager
    private val jitsiViewModel: JitsiViewModel by viewModels()
    private val bookingViewModel: BookingViewModel by viewModels()
    private lateinit var binding: ActivityJitsiCallBinding
    var roomId = ""
    lateinit var view:JitsiMeetView
    var username=""
    var isGroupCall=false


    //For Auto call drop
    var endTime = 0L

    private var timer: CountDownTimer? = null

    var bookingModel: BookingModel = BookingModel()
    var astrologerModel: AstrologerUserModel = AstrologerUserModel()

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                bookingModel = Gson().fromJson(
                    result.data?.getStringExtra(Constants.INTENT_BOOKING_MODEL),
                    BookingModel::class.java
                )
                //update timer
                endTime = bookingModel.endTime!!.time
                MyLog.e(TAG, "extended time is ${bookingModel.endTime.toString()} \n===end$endTime")
                timer?.cancel()
                setTime(endTime)
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJitsiCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        roomId = intent.getStringExtra("RoomId")!!
        username=intent.getStringExtra("OpponentUserName")!!
        isGroupCall=intent.getBooleanExtra("isGroupCall",false)!!

        bookingModel = intent.getParcelableExtra(Constants.INTENT_BOOKING_MODEL)!!
        astrologerModel = intent.getParcelableExtra(Constants.INTENT_MODEL)!!

//        registerForBroadcastMessages()

        jitsiManager = JitsiManager(this)
        view = jitsiManager.startCustomVideoCall(roomId) as JitsiMeetView
        view.listener = this
//        setContentView(view)

        binding.layoutJitsi.addView(view)
//        binding.layoutJitsi.addView(binding.txtRemainingTime)
        binding.txtRemainingTime.bringToFront()
        MyLog.e("Call List in JITSI Screen","====="+ Constants.listOfActiveCall.size)

//        displayTimeValidationDialog()
        setObserver()
    }

    override fun requestPermissions(p0: Array<out String>?, p1: Int, p2: PermissionListener?) {
    }

    override fun onConferenceJoined(p0: MutableMap<String, Any>?) {
    }

    override fun onConferenceTerminated(p0: MutableMap<String, Any>?) {
        Log.e("===============", "onConferenceTerminated============")
        jitsiViewModel.changeStatus(roomId,false)
        if (bookingModel.status != Constants.COMPLETED_STATUS) {
            finish()
        }

    }

    override fun onConferenceWillJoin(p0: MutableMap<String, Any>?) {
        Log.e("===============", "onConferenceTerminated============")
        jitsiViewModel.changeStatus(roomId,true)

        endTime = bookingModel.endTime!!.time

        setTime(endTime)
        binding.txtRemainingTime.makeVisible()


    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(view!=null) {
            jitsiViewModel.changeStatus(roomId,false)
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
            jitsiViewModel.changeStatus(roomId,false)
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
                val fixedTime = time - 300000L // 5 min
                val totalFixedTime = time - fixedTime

                val difference: Long = endTime - Date().time
                val min = TimeUnit.MILLISECONDS.toMinutes(difference).toInt()

//                MyLog.e("Diff in Minute","===${min}")

//                MyLog.e("Timmerr", "$totalTime--$totalFixedTime--$totalSecs")


                var timeString = String.format(" Ends in : %02d:%02d:%02d", hour, minutes, seconds)

                binding.txtRemainingTime.text = timeString

                if (totalTime == totalFixedTime) {
                    displayTimeValidationDialog()
                }
                /*if (totalSecs == 0L) {
                    MyLog.e(TAG, "========================= $totalSecs")
                    timer?.cancel()
//                    pref.setValueLong(
//                        this@JitsiCallAstrologerActivity,
//                        Constants.REMAINING_TIME,
//                        endTime, Constants.PREF_FILE)
                    // update booking status to complete
                    bookingModel.status = Constants.COMPLETED_STATUS
                    bookingViewModel.addUpdateBookingData(bookingModel, true)
                    jitsiViewModel.changeStatus(roomId,false)
//                    view.leave()
//                    finish()
                }*/
            }

            override fun onFinish() {
                MyLog.e(TAG,"======================== onFinish")
                // update booking status to complete
                bookingModel.status = Constants.COMPLETED_STATUS
                bookingViewModel.addUpdateBookingData(bookingModel, true)
                jitsiViewModel.changeStatus(roomId,false)
            }
        }
        (timer as CountDownTimer).start()
    }

    private fun displayTimeValidationDialog() {
        try {
            val dialogBuilder = MaterialAlertDialogBuilder(this@JitsiCallActivity, R.style.CutShapeTheme)
            val customView = LayoutInflater.from(this@JitsiCallActivity).inflate(R.layout.dialog_time_remaining_user, null)
            var dialogEndTimeAlert = dialogBuilder.setView(customView).show()
            val llBtnOk = dialogEndTimeAlert.findViewById<View>(R.id.llBtnOk) as MaterialButton
            val llEdit = dialogEndTimeAlert.findViewById<View>(R.id.llEdit) as MaterialButton
            val llCancel = dialogEndTimeAlert.findViewById<View>(R.id.llCancel) as MaterialButton
            val txtDescription = dialogEndTimeAlert.findViewById<View>(R.id.txtDescription) as AppCompatTextView
            val txtTimeValue = dialogEndTimeAlert.findViewById<View>(R.id.txtTimeValue) as TextView


            txtTimeValue.setOnClickListener {
                val popupMenu = PopupMenu(this, it)
                popupMenu.menuInflater.inflate(R.menu.menu_extend_time, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { item ->
//                toast(item.title.toString())
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
                dialogEndTimeAlert.dismiss()
            }
            llBtnOk.setOnClickListener {
                if (txtTimeValue.text.isBlank()) {
                    binding.root.showSnackBarToast("Please select minute to extend time")
                    return@setOnClickListener
                }
                startForResult.launch(
                    Intent(this, PaymentActivity::class.java)
                        .putExtra(Constants.INTENT_AMOUNT, (astrologerModel.price * txtTimeValue.text.toString().toInt()).toString())
                        .putExtra(Constants.INTENT_MODEL, astrologerModel)
                        .putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel)
                        .putExtra(Constants.INTENT_MINUTE, txtTimeValue.text.toString().toInt())
                        .putExtra(Constants.INTENT_IS_DIRECT_PAYMENT, true) //direct payment
                        .putExtra(Constants.INTENT_IS_EXTEND_CALL, true)
                )
//            if (txtTimeValue.text!!.isNotEmpty()) {
//                timer?.cancel()
//
//                val tt = txtTimeValue.text.toString().trim()
//                val extraTimeAdd = remainingTime + TimeUnit.MINUTES.toMillis(tt.toLong())
//                // Call function
//                setTime(extraTimeAdd)
//                pref.setValueLong(
//                    this@JitsiCallAstrologerActivity,
//                    Constants.REMAINING_TIME,
//                    extraTimeAdd, Constants.PREF_FILE)
//                MyLog.e("remainingTime", remainingTime.toString())
//            }
                dialogEndTimeAlert.dismiss()
            }
            dialogEndTimeAlert.show()
        } catch (e: Exception) {
            MyLog.e(TAG,e.message.toString())
        }
    }

    /**
     * Set up observer
     */
    private fun setObserver() {
        bookingViewModel.bookingAddUpdateResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
//                    showProgress(this)
                }
                Status.SUCCESS -> {
//                    hideProgress()
                    it.data?.let { result ->
//                        toast(result)
                        toast(getString(R.string.call_over))
                        view.leave()
                        startFromFresh()
                    }
                }
                Status.ERROR -> {
//                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })
    }

    private fun startFromFresh() {
        //chat ended start from fresh
        startActivity(
            Intent(this, UserHomeActivity::class.java)
//                .putExtra(Constants.INTENT_SHOW_TIMER, false)
        )
        finishAffinity()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }

}