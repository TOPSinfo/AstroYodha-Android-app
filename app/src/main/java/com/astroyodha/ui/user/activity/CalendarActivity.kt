package com.astroyodha.ui.user.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.util.Preconditions
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.astroyodha.R
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityCalendarBinding
import com.astroyodha.databinding.Example3CalendarDayBinding
import com.astroyodha.databinding.Example3CalendarHeaderBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.user.adapter.CalendarEventAdapter
import com.astroyodha.ui.user.model.booking.BookingModel
import com.astroyodha.ui.user.viewmodel.CalendarViewModel
import com.astroyodha.utils.dateFormat
import com.astroyodha.utils.makeInvisible
import com.astroyodha.utils.makeVisible
import com.astroyodha.utils.showSnackBarToast
import com.google.firebase.auth.FirebaseAuth
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*


class CalendarActivity : BaseActivity() {

    private var from: String = ""
    private var selectedMonth: String = ""
    private var isDaysLoaded: Boolean = false
    private var allEventList: ArrayList<BookingModel> = arrayListOf()
    private var selectedDateEventList: ArrayList<BookingModel> = arrayListOf()
    private lateinit var binding: ActivityCalendarBinding
    private val calendarViewModel: CalendarViewModel by viewModels()

    private var selectedDate: LocalDate? = null

    @RequiresApi(Build.VERSION_CODES.O)
    private val titleSameYearFormatter = DateTimeFormatter.ofPattern("MMMM")

    @RequiresApi(Build.VERSION_CODES.O)
    private val titleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    @RequiresApi(Build.VERSION_CODES.O)
    private val selectionFormatter = DateTimeFormatter.ofPattern("d MMMM")

    private val userId: String by lazy { FirebaseAuth.getInstance().currentUser!!.uid }
    var dateDBFormat: String = "dd - MMM - yyyy"

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                //  you will get result here in result.data
                setResult(Activity.RESULT_OK, Intent())
                onBackPressed()
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            calendarViewModel.getMonthWiseBookingEventRequest(
                userId, LocalDate.now().month.toString().dateFormat("MMMM", "MM")
            )
        }

        setObserver()
        setClickListener()

        with(binding.rvEventList) {
            val mLayoutManager = LinearLayoutManager(context)
            layoutManager = mLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = CalendarEventAdapter(
                context, selectedDateEventList,
                object : CalendarEventAdapter.ViewHolderClicks {
                    override fun onClickItem(
                        model: BookingModel,
                        position: Int
                    ) {
                        //click of recyclerview item
                    }
                }
            )
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpCalendarDate() {

        val today = LocalDate.now()
        val daysOfWeek = daysOfWeekFromLocale()
        val currentMonth = YearMonth.now()
        if (selectedMonth.isEmpty())
            selectedMonth = YearMonth.now().monthValue.toString()
        binding.exThreeCalendar.apply {
            setup(currentMonth.minusMonths(10), currentMonth.plusMonths(10), daysOfWeek.first())
            scrollToMonth(currentMonth)
        }

        binding.exThreeCalendar.post {
            // Show today's events initially.
            selectDate(today, "")
        }

        binding.exThreeCalendar.monthScrollListener = {

            // Select the first day of the month when
            // we scroll to a new month.
            var mmonth = it.month
            if (from.equals("Left")) {
                if (mmonth == 1) {
                    mmonth = 12
                } else {
                    mmonth = (mmonth - 1)
                }
            } else if (from.equals("Right")) {

                if (mmonth == 12) {
                    mmonth = 1
                } else {
                    mmonth = mmonth + 1
                }
            }
            selectedMonth = mmonth.toString()

            selectDate(it.yearMonth.atDay(1), "Init")
        }


        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = Example3CalendarHeaderBinding.bind(view).legendLayout.llWeekDays
            val legendHeaderImgLeftArrow =
                Example3CalendarHeaderBinding.bind(view).legendLayout.imgLeftArrow
            val legendHeaderImgRightArrow =
                Example3CalendarHeaderBinding.bind(view).legendLayout.imgRightArrow
            val legendHeaderTxtMonth =
                Example3CalendarHeaderBinding.bind(view).legendLayout.txtMonth
        }


        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val binding = Example3CalendarDayBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        selectDate(day.date, "")
                    }
                }
            }
        }


        binding.exThreeCalendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            @RequiresApi(Build.VERSION_CODES.O)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.binding.exThreeDayText
                val dotView = container.binding.exThreeDotView

                textView.text = day.date.dayOfMonth.toString()

                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.makeVisible()
                    when (day.date) {
                        today -> {
                            textView.setTextColor(
                                ContextCompat.getColor(
                                    this@CalendarActivity,
                                    R.color.user_theme
                                )
                            )
                            textView.setBackgroundResource(R.drawable.calendar_today_bg)
                            if (getEventListOfThisDate(day.date) > 0) {
                                dotView.isVisible = true
                            } else {
                                dotView.isVisible = false
                            }
                        }
                        selectedDate -> {
                            textView.setTextColor(
                                ContextCompat.getColor(
                                    this@CalendarActivity,
                                    R.color.white
                                )
                            )
                            textView.setBackgroundResource(R.drawable.calendar_selected_bg)
                            dotView.makeInvisible()
                        }
                        else -> {
                            textView.setTextColor(
                                ContextCompat.getColor(
                                    this@CalendarActivity,
                                    R.color.black
                                )
                            )
                            textView.background = null

                            if (getEventListOfThisDate(day.date) > 0) {
                                dotView.isVisible = true
                            } else {
                                dotView.isVisible = false
                            }

                        }
                    }
                } else {
                    textView.makeInvisible()
                    dotView.makeInvisible()
                }
            }
        }

        binding.exThreeCalendar.monthHeaderBinder = object :
            MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                // Setup each header day text if we have not done that already.
                if (container.legendLayout.tag == null) {
                    container.legendLayout.tag = month.yearMonth
                    container.legendLayout.children.map { it as TextView }
                        .forEachIndexed { index, tv ->
                            val dayOfWeekDisplayName: String =
                                daysOfWeek[index].getDisplayName(
                                    TextStyle.SHORT, Locale.US
                                )

                            tv.text = dayOfWeekDisplayName.toString()
                            tv.setTextColor(
                                ContextCompat.getColor(
                                    this@CalendarActivity,
                                    R.color.black
                                )
                            )
                        }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    container.legendHeaderTxtMonth.text = titleFormatter.format(month.yearMonth)
                }
                container.legendHeaderImgLeftArrow.setOnClickListener {
                    val newMonth = month.yearMonth.minusMonths(1)
                    from = "Left"
                    selectedMonth = newMonth.monthValue.toString()
                    if (from.isEmpty()) {
                        if (selectedMonth.toInt() == 1) {
                            selectedMonth = "12"
                        } else {
                            selectedMonth = (selectedMonth.toInt() - 1).toString()
                        }
                    }

                    binding.exThreeCalendar.smoothScrollToMonth(newMonth)

                }

                container.legendHeaderImgRightArrow.setOnClickListener {
                    val newMonth = month.yearMonth.plusMonths(1)
                    selectedMonth = newMonth.monthValue.toString()
                    if (from.isEmpty()) {
                        if (selectedMonth.toInt() == 12) {
                            selectedMonth = "1"
                        } else {
                            selectedMonth = (selectedMonth.toInt() + 1).toString()
                        }
                    }

                    from = "Right"


                    binding.exThreeCalendar.smoothScrollToMonth(newMonth)

                }

            }
        }


    }

    private fun getEventListOfThisDate(date: LocalDate): Int {
        var count = 0
        for (i in 0..allEventList.size - 1) {
            if (allEventList.get(i).date.equals(
                    date.toString().dateFormat(
                        "yyyy-MM-dd",
                        dateDBFormat
                    )
                )
            )
                count = count + 1
        }
        return count
    }

    @SuppressLint("RestrictedApi")
    fun getDayOfMonthSuffix(n: Int): String? {
        Preconditions.checkArgument(n >= 1 && n <= 31, "illegal day of month: $n")
        return if (n >= 11 && n <= 13) {
            "th"
        } else when (n % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }

    /**
     * set observer
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setObserver() {
        calendarViewModel.monthWiseBookingEventResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { resultList ->
                        allEventList.clear()
                        allEventList.addAll(resultList)
                        setUpCalendarDate()
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
        binding.imgAdd.setOnClickListener {
            startForResult.launch(Intent(this, SelectAstrologerActivity::class.java))
        }

        binding.imgClose.setOnClickListener {
            onBackPressed()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun selectDate(date: LocalDate, from: String) {
        if (selectedDate != date) {

            if (!from.equals("Init")) {

                val oldDate = selectedDate
                selectedDate = date
                binding.rvEventList.post(Runnable {
                    oldDate?.let { binding.exThreeCalendar.notifyDateChanged(it) }
                    binding.exThreeCalendar.notifyDateChanged(date)
                })

                selectedDateEventList.clear()
                for (i in 0..allEventList.size - 1) {
                    if (allEventList.get(i).date.equals(
                            date.toString().dateFormat(
                                "yyyy-MM-dd",
                                dateDBFormat
                            )
                        )
                    )
                        selectedDateEventList.add(allEventList.get(i))
                }

                binding.rvEventList.adapter?.notifyDataSetChanged()


                val selectedDate = selectionFormatter.format(date)
                binding.exThreeSelectedDateText.text = selectedDate.substring(
                    0,
                    selectedDate.indexOf(' ')
                ) + getDayOfMonthSuffix(
                    selectedDate.substring(0, selectedDate.indexOf(' ')).toInt()
                ) + selectedDate.substring(
                    selectedDate.indexOf(' '),
                    selectedDate.length
                ) + " Events"
            }

        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun daysOfWeekFromLocale(): Array<DayOfWeek> {
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        var daysOfWeek = DayOfWeek.values()
        // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
        // Only necessary if firstDayOfWeek != DayOfWeek.MONDAY which has ordinal 0.
        if (firstDayOfWeek != DayOfWeek.SUNDAY) {
            val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
            val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
            daysOfWeek = rhs + lhs
        }
        return daysOfWeek
    }

}
