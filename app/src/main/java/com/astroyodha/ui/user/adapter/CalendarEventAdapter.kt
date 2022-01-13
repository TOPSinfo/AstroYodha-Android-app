package com.astroyodha.ui.user.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.astroyodha.R
import com.astroyodha.ui.user.model.booking.BookingModel
import com.astroyodha.utils.Constants
import com.astroyodha.utils.dateToStringFormat
import java.util.*

/**
 * Event list adapter : Show Event list.
 */
class CalendarEventAdapter(
    private val mContext: Context, var mList: ArrayList<BookingModel>,
    private val viewHolderClicks: ViewHolderClicks,
) : RecyclerView.Adapter<CalendarEventAdapter.ViewHolder>() {
    var mTimeFormat: String = "hh:mm a"
    var mDateFormat: String = "d\nMMM"
    var mInflater: LayoutInflater =
        mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v =
            mInflater.inflate(R.layout.raw_calendar_event, parent, false)
        return ViewHolder(
            v
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val model = mList[holder.adapterPosition]
        try {
            holder.tvBookingWith.text = mContext.getString(
                R.string.your_appointment_with_user,
                model.astrologerName.substringBefore(" ")
            )
            holder.tvTime.text = "${model.startTime!!.dateToStringFormat(mTimeFormat)} ${model.endTime!!.dateToStringFormat(mTimeFormat)}"

            var mColor = mContext.getColor(R.color.pending_color)
            var image = R.drawable.ic_waiting

            when (model.status) {
                Constants.COMPLETED_STATUS -> {
                    image = R.drawable.ic_read
                    mColor = mContext.getColor(R.color.completed_color)
                }
                Constants.APPROVE_STATUS -> {
                    image = R.drawable.ic_check_black
                    mColor = mContext.getColor(R.color.approved_color)
                }
                Constants.REJECT_STATUS -> {
                    image = R.drawable.ic_close
                    mColor = mContext.getColor(R.color.orange_theme)
                }
                Constants.PENDING_STATUS -> {
                    image = R.drawable.ic_waiting
                    mColor = mContext.getColor(R.color.pending_color)
                }
                Constants.CANCEL_STATUS -> {
                    image = R.drawable.ic_delete
                    mColor = mContext.getColor(R.color.delete_color)
                }
            }
            holder.imgColor.setBackgroundColor(mColor)
            holder.imgStatus.setImageResource(image)

            holder.itemView.setOnClickListener {
                viewHolderClicks.onClickItem(model, holder.adapterPosition)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgColor: ImageView = itemView.findViewById(R.id.imgColor)
        var tvBookingWith: TextView = itemView.findViewById(R.id.tvBookingWith)
        var imgStatus: ImageView = itemView.findViewById(R.id.imgEventType)
        var tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }

    interface ViewHolderClicks {
        fun onClickItem(model: BookingModel, position: Int)
    }
}