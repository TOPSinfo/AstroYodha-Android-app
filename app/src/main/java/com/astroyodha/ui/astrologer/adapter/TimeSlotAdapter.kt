package com.astroyodha.ui.astrologer.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.astroyodha.R
import com.astroyodha.ui.astrologer.model.timeslot.TimeSlotModel
import com.astroyodha.utils.dateFormat

/**
 * Community list adapter : Show Community list.
 */
class TimeSlotAdapter(
    private val mContext: Context, var mList: ArrayList<TimeSlotModel>,
    private val viewHolderClicks: ViewHolderClicks,
) : RecyclerView.Adapter<TimeSlotAdapter.ViewHolder>() {
    var dateDBFormat: String = "dd - MMM - yyyy"
    var dateFormat: String = "dd MMM"
    var mInflater: LayoutInflater =
        mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v =
            mInflater.inflate(R.layout.raw_timeslot, parent, false)
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
            holder.tvTimeSlot.text = "${model.fromTime}\nTo\n${model.toTime}"
            holder.tvAppointment.text = mContext.getString(
                R.string.appointment_slot_user, (holder.adapterPosition + 1).toString())

            if (model.type == mContext.getString(R.string.repeat)) {
                holder.tvDate.text = "${model.startDate.dateFormat(dateDBFormat, dateFormat)} to ${
                    model.endDate.dateFormat(dateDBFormat, dateFormat)
                }"
            } else if (model.type == mContext.getString(R.string.weekly)) {
                if (model.days?.size == 1) {
                    holder.tvDate.text =
                        mContext.getString(
                            R.string.every_day,
                            model.days?.joinToString(separator = ",") { it ->
                                "${it.replaceFirstChar(Char::uppercase)}"
                            })
                } else {
                    holder.tvDate.text = model.days?.joinToString(separator = ",") { it ->
                        "${it.replaceFirstChar(Char::uppercase)}"
                    }
                }
            } else if (model.type == mContext.getString(R.string.custom)) {
                holder.tvDate.text = "${model.startDate.dateFormat(dateDBFormat, dateFormat)}"
            }
            holder.imgDeleteTimeSlot.setOnClickListener {
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
        var tvTimeSlot: TextView = itemView.findViewById(R.id.tvTimeSlot)
        var tvAppointment: TextView = itemView.findViewById(R.id.tvAppointment)
        var tvDate: TextView = itemView.findViewById(R.id.tvDate)
        var imgDeleteTimeSlot: ImageView = itemView.findViewById(R.id.imgDeleteTimeSlot)
    }

    interface ViewHolderClicks {
        fun onClickItem(model: TimeSlotModel, position: Int)
    }
}