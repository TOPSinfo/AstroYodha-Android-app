package com.astroyodha.ui.astrologer.activity

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.viewModels
import com.astroyodha.R
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityAddTimeSlotBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.model.timeslot.TimeSlotModel
import com.astroyodha.ui.astrologer.viewmodel.TimeSlotViewModel
import com.astroyodha.utils.*
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class AddTimeSlotActivity : BaseActivity() {
    val TAG = javaClass.simpleName
    private lateinit var binding: ActivityAddTimeSlotBinding
    private val viewModel: TimeSlotViewModel by viewModels()
    private var userId: String? = null
    private var id: String = ""
    var model: TimeSlotModel = TimeSlotModel()

    var cal = Calendar.getInstance()
    lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    var interval = 15

    var startDate: Date? = null
    var endDate: Date? = null
    var isFromDateClick = false

    var fromHour: Int = 0
    var fromMinute: Int = 0
    var mTimeSlotList: ArrayList<TimeSlotModel> = ArrayList()
    var mWeeklyDaysList: ArrayList<String> = ArrayList()

    val dateFormat = "dd - MMM - yyyy"
    val timeFormat = "hh:mm a"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTimeSlotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setObserver()
        setClickListener()
    }

    /**
     * initialize view
     */
    private fun init() {
        val fb = FirebaseAuth.getInstance().currentUser
        userId = fb?.uid.toString()
        if (intent.hasExtra(Constants.INTENT_TIME_SLOT_LIST)) {
            intent.getParcelableArrayListExtra<TimeSlotModel>(Constants.INTENT_TIME_SLOT_LIST).let {
                mTimeSlotList.addAll(it!!)
            }
        }

        updateViewOnModeChange()
    }

    /**
     * update view on mode change
     */
    private fun updateViewOnModeChange() {
        binding.tvStartDate.text = ""
        startDate = null
        binding.tvEndDate.text = ""
        endDate = null
        binding.tvFromTime.text = ""
        binding.tvToTime.text = ""

        if (binding.tvMode.text.toString() == getString(R.string.repeat)) {
            binding.groupDays.makeGone()
            binding.groupStartDate.makeVisible()
            binding.groupEndDate.makeVisible()
            binding.viewDate.makeVisible()
        } else if (binding.tvMode.text.toString() == getString(R.string.weekly)) {
            binding.groupDays.makeVisible()
            binding.groupStartDate.makeGone()
            binding.groupEndDate.makeGone()
            binding.viewDate.makeGone()
        } else if (binding.tvMode.text.toString() == getString(R.string.custom)) {
            binding.groupDays.makeGone()
            binding.groupStartDate.makeVisible()
            binding.groupEndDate.makeGone()
            binding.viewDate.makeVisible()
        }
    }

    /**
     * set observer
     */
    private fun setObserver() {
        viewModel.timeslotDataResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        toast(it)
                        onBackPressed()
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
     * manage click listener of view
     */
    private fun setClickListener() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.tvMode.setOnClickListener {
            showTimeModeDialog()
        }
        binding.imgDown.setOnClickListener {
            showTimeModeDialog()
        }

        dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                id = "$dayOfMonth-${monthOfYear + 1}-$year"
                updateDateInView()
            }

        binding.tvStartDate.setOnClickListener {
            isFromDateClick = true
            showDatePicker()
        }
        binding.tvEndDate.setOnClickListener {
            isFromDateClick = false
            showDatePicker()
        }

        binding.tvFromTime.setOnClickListener {
            hideKeyboard()
            //checking start date end date added or not
            if(checkStartEndDate()) {
                val mcurrentTime = Calendar.getInstance()
                val hour = mcurrentTime[Calendar.HOUR_OF_DAY]
                val minute = mcurrentTime[Calendar.MINUTE]
                val mTimePicker: TimePickerDialog
                mTimePicker = TimePickerDialog(
                    this@AddTimeSlotActivity, R.style.DatePickerTheme,
                    { _, selectedHour, selectedMinute ->

                        var timeCalnder = Calendar.getInstance()
                        timeCalnder.set(Calendar.HOUR_OF_DAY, selectedHour)
                        timeCalnder.set(Calendar.MINUTE, selectedMinute)

                        val myFormat = timeFormat //In which you need put here
                        val sdf = SimpleDateFormat(myFormat, Locale.US)

                        var isValidTime = true
                        if (mTimeSlotList.size > 0) {
                            isValidTime =
                                checkSelectedTimeIsBetweenExistingTimeSlot(sdf.format(timeCalnder.time))
                        }

                        if (isValidTime) {
                            fromHour = selectedHour
                            fromMinute = selectedMinute
                            binding.tvFromTime.setText(sdf.format(timeCalnder.time))
                        } else {
                            binding.tvToTime.setText("")
                            binding.root.showSnackBarToast(getString(R.string.selected_time_alerady_available))
                        }
                    },
                    hour,
                    minute,
                    false
                ) //Yes 24 hour time

//            mTimePicker.setTimeInterval(1, 15);
                mTimePicker.setTitle(resources.getString(R.string.select_time))
                mTimePicker.show()
            }
        }

        binding.tvToTime.setOnClickListener {
            hideKeyboard()

            if(checkStartEndDate()) {
                if (fromHour == 0 && fromMinute == 0) {
                    binding.root.showSnackBarToast(getString(R.string.please_select_from_time))
                } else {
                    val hour = fromHour
                    val minute = fromMinute
                    val mTimePicker: TimePickerDialog
                    mTimePicker = TimePickerDialog(
                        this@AddTimeSlotActivity, R.style.DatePickerTheme,
                        { _, selectedHour, selectedMinute ->

                            var timeCalnder = Calendar.getInstance()
                            timeCalnder.set(Calendar.HOUR_OF_DAY, selectedHour)
                            timeCalnder.set(Calendar.MINUTE, selectedMinute)

                            val myFormat = timeFormat //In which you need put here
                            val sdf = SimpleDateFormat(myFormat, Locale.US)
                            var fromTime = sdf.parse(binding.tvFromTime.text.toString().trim())
                            var toTime = sdf.parse(sdf.format(timeCalnder.time))

                            if (toTime.after(fromTime)) {

                                var isValidTime = true
                                if (mTimeSlotList.size > 0) {
                                    isValidTime = checkSelectedTimeIsBetweenExistingTimeSlot(sdf.format(timeCalnder.time))
                                }

                                if (isValidTime &&
                                    checkExistingTimeSlotIsBetweenSelectedTime(
                                        binding.tvFromTime.text.toString().trim(),
                                        sdf.format(timeCalnder.time)
                                    )
                                ) {
                                    binding.tvToTime.setText(sdf.format(timeCalnder.time))
                                } else {
                                    binding.tvToTime.setText("")
                                    binding.root.showSnackBarToast(getString(R.string.selected_time_alerady_available))
                                }


                            } else {
                                binding.root.showSnackBarToast(getString(R.string.please_select_to_time_greater_than_from_time))
                            }

                        },
                        hour,
                        minute,
                        false
                    ) //Yes 24 hour time


                    mTimePicker.setTitle(resources.getString(R.string.select_time))
                    mTimePicker.show()
                }
            }
        }

        binding.txtSave.setOnClickListener {
            hideKeyboard()
            if (checkValidation()) {
                model.id = id

                if (binding.tvMode.text.toString() == getString(R.string.repeat)) {
                    model.startDate = startDate?.dateToStringFormat(dateFormat).toString()
                    model.endDate = endDate?.dateToStringFormat(dateFormat).toString()
                    model.fromTime = binding.tvFromTime.text.toString()
                    model.toTime = binding.tvToTime.text.toString()
                } else if (binding.tvMode.text.toString() == getString(R.string.weekly)) {
                    model.startDate = startDate?.dateToStringFormat(dateFormat).toString()
                    model.fromTime = binding.tvFromTime.text.toString()
                    model.toTime = binding.tvToTime.text.toString()
                    model.days = mWeeklyDaysList
                } else if (binding.tvMode.text.toString() == getString(R.string.custom)) {
                    model.startDate = startDate?.dateToStringFormat(dateFormat).toString()
                    model.fromTime = binding.tvFromTime.text.toString()
                    model.toTime = binding.tvToTime.text.toString()
                }
                model.type = binding.tvMode.text.toString()
                model.userId = userId.toString()

                viewModel.addUpdateBookingData(
                    model,
                    false
                )
            }
        }

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
     * update date in start date and end date
     */
    private fun updateDateInView() {
        val sdf = SimpleDateFormat(dateFormat, Locale.US)
        if (isFromDateClick) {
            startDate = cal.time
            binding.tvStartDate.text = sdf.format(cal.time)
        } else {
            endDate = cal.time
            binding.tvEndDate.text = sdf.format(cal.time)
        }
    }

    /**
     * Do not remove call made from xml
     * it will manage days selection
     */
    fun manageDaySelection(view: View) {
        val textView = findViewById<TextView>(view.id)
        if (!textView.isSelected) {
            textView.isSelected = true
            textView.setTextColor(getColor(R.color.white))
            textView.setBackgroundResource(R.drawable.calendar_selected_bg)
            mWeeklyDaysList.add(textView.contentDescription.toString())
        } else {
            textView.isSelected = false
            textView.setTextColor(getColor(R.color.text_gray))
            textView.setBackgroundResource(0)
            mWeeklyDaysList.remove(textView.contentDescription.toString())
        }
    }

    /**
     * show Notification dialog
     */
    private fun showTimeModeDialog() {
        val mDialog = Dialog(this)
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog.setContentView(R.layout.dialog_add_time_slot_mode)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(mDialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog.window!!.attributes = lp
        mDialog.show()

        val rgTimeMode = mDialog.findViewById(R.id.rgTimeMode) as RadioGroup

        rgTimeMode.setOnCheckedChangeListener { radioGroup, checkedId ->
            val radioButton = radioGroup.findViewById(checkedId) as RadioButton
            binding.tvMode.text = radioButton.text.toString()
            updateViewOnModeChange()
            mDialog.hide()
        }

    }

    /**
     * Checking validation
     */
    private fun checkValidation(): Boolean {

        if (binding.tvMode.text.toString() == getString(R.string.repeat)) {
            if (TextUtils.isEmpty(binding.tvStartDate.text.toString().trim())) {
                binding.root.showSnackBarToast(getString(R.string.please_select_date))
                return false
            } else if (TextUtils.isEmpty(binding.tvEndDate.text.toString().trim())) {
                binding.root.showSnackBarToast(getString(R.string.please_select_date))
                return false
            } else if (TextUtils.isEmpty(binding.tvFromTime.text.toString().trim())) {
                binding.root.showSnackBarToast(getString(R.string.please_select_from_time))
                return false
            } else if (TextUtils.isEmpty(binding.tvToTime.text.toString().trim())) {
                binding.root.showSnackBarToast(getString(R.string.please_select_to_time))
                return false
            } else if (startDate?.after(endDate)!! || binding.tvStartDate.text == binding.tvEndDate.text) {
                binding.root.showSnackBarToast(getString(R.string.please_select_end_date_greater_than_start_date))
                return false
            } else {
                return true
            }
        } else if (binding.tvMode.text.toString() == getString(R.string.weekly)) {
            if (TextUtils.isEmpty(binding.tvFromTime.text.toString().trim())) {
                binding.root.showSnackBarToast(getString(R.string.please_select_from_time))
                return false
            } else if (TextUtils.isEmpty(binding.tvToTime.text.toString().trim())) {
                binding.root.showSnackBarToast(getString(R.string.please_select_to_time))
                return false
            } else if (mWeeklyDaysList.isEmpty()) {
                binding.root.showSnackBarToast(getString(R.string.please_select_days))
                return false
            } else {
                return true
            }
        } else if (binding.tvMode.text.toString() == getString(R.string.custom)) {
            if (TextUtils.isEmpty(binding.tvStartDate.text.toString().trim())) {
                binding.root.showSnackBarToast(getString(R.string.please_select_date))
                return false
            } else if (TextUtils.isEmpty(binding.tvFromTime.text.toString().trim())) {
                binding.root.showSnackBarToast(getString(R.string.please_select_from_time))
                return false
            } else if (TextUtils.isEmpty(binding.tvToTime.text.toString().trim())) {
                binding.root.showSnackBarToast(getString(R.string.please_select_to_time))
                return false
            } else {
                return true
            }
        }


        return true
    }

    private fun checkStartEndDate(): Boolean {
        if (binding.tvMode.text.toString() == getString(R.string.repeat)) {
            if (startDate == null || endDate == null) {
                binding.root.showSnackBarToast(getString(R.string.please_select_date))
                return false
            } else {
                return true
            }
        } else if (binding.tvMode.text.toString() == getString(R.string.weekly)) {
            // no start date or end date
            return true
        } else if (binding.tvMode.text.toString() == getString(R.string.custom)) {
            if (startDate == null) {
                binding.root.showSnackBarToast(getString(R.string.please_select_date))
                return false
            } else {
                return true
            }
        }
        return false
    }


    fun checkSelectedTimeIsBetweenExistingTimeSlot(time: String): Boolean {
        var isTimeBetweenFormAndToTime = true
        //mode wise comparison continue mitesh
        /*if (binding.tvMode.text.toString() == getString(R.string.repeat)) {
            mTimeSlotList.forEachIndexed { index, timeSlotModel ->
                val myFormat = timeFormat //In which you need put here
                val dateFormay = "dd mm yyyy" //In which you need put here
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                var fromTime = sdf.parse(timeSlotModel.fromTime)
                var toTime = sdf.parse(timeSlotModel.toTime)
                var selectedTime = sdf.parse(time)
                var selectedStartDate = startDate?.dateToStringFormat(dateFormay)
                var selectedEndDate = endDate?.dateToStringFormat(dateFormay)
                var timeSlotStartDate = endDate?.dateToStringFormat(dateFormay)
                var timeSlotEndDate = endDate?.dateToStringFormat(dateFormay)
                if(timeSlotModel.type == getString(R.string.repeat)) {
                    if (startDate?.after(timeSlotModel.startDate)!! ||
                        endDate?.before(timeSlotModel.endDate)!!) {
                        if (selectedTime.after(fromTime) && selectedTime.before(toTime)) {
                            isTimeBetweenFormAndToTime = false
                            return isTimeBetweenFormAndToTime
                        }
                    } else if (selectedStartDate == timeSlotStartDate ||
                        selectedStartDate == timeSlotEndDate ||
                        selectedEndDate == (timeSlotStartDate) ||
                        selectedEndDate == (timeSlotEndDate)
                    ) {
                        if (selectedTime.after(fromTime) && selectedTime.before(toTime)) {
                            isTimeBetweenFormAndToTime = false
                            return isTimeBetweenFormAndToTime
                        }
                    }
                }
            }
        } else if (binding.tvMode.text.toString() == getString(R.string.weekly)) {

        } else if (binding.tvMode.text.toString() == getString(R.string.custom)) {

        }*/
        //any time wise comparison continue satyaraj
        /*for (timeSlot in mTimeSlotList) {
            val myFormat = timeFormat //In which you need put here
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            var fromTime = sdf.parse(timeSlot.fromTime)
            var toTime = sdf.parse(timeSlot.toTime)
            var selectedTime = sdf.parse(time)

            if (selectedTime.after(fromTime) && selectedTime.before(toTime)) {
                isTimeBetweenFormAndToTime = false
                break
            }
        }*/
        return isTimeBetweenFormAndToTime
    }

    fun checkExistingTimeSlotIsBetweenSelectedTime(timeFrom: String,timeTo:String): Boolean {
        var isTimeSlotBetweenFormAndToTime = true
        for (timeSlot in mTimeSlotList) {
            val myFormat = timeFormat //In which you need put here
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            var fromTime = sdf.parse(timeSlot.fromTime)
            var toTime = sdf.parse(timeSlot.toTime)
            var selectedTimeFrom = sdf.parse(timeFrom)
            var selectedTimeTo = sdf.parse(timeTo)

            if (selectedTimeFrom.before(fromTime) && selectedTimeTo.after(toTime)) {
                isTimeSlotBetweenFormAndToTime = false
                break
            }
        }
        return isTimeSlotBetweenFormAndToTime
    }

}