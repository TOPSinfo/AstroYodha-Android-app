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
import com.astroyodha.ui.user.model.notification.NotificationModel
import com.astroyodha.utils.Constants
import com.astroyodha.utils.dateToStringFormat
import java.util.*

/**
 * Notification list adapter : Show Notification list.
 */
class AstrologerNotificationAdapter(
    private val mContext: Context, var mList: ArrayList<NotificationModel>,
    private val viewHolderClicks: ViewHolderClicks,
) : RecyclerView.Adapter<AstrologerNotificationAdapter.ViewHolder>() {
    var mDateFormat: String = "d MMM"
    var mInflater: LayoutInflater =
        mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v =
            mInflater.inflate(R.layout.raw_astrologer_notification, parent, false)
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
            if (model.type == Constants.NOTIFICATION_REQUEST_ADDED_ACCEPTED) {
                //booking accepted or created
                holder.imgNotification.setImageResource(R.drawable.ic_notification_calendar)
            } else {
                //reminder
                holder.imgNotification.setImageResource(R.drawable.ic_notification_reminder)
            }
            holder.tvNoticationTitle.text = model.title
            holder.tvDesc.text = model.message
            holder.tvTime.text = model.createdAt?.toDate()?.dateToStringFormat(mDateFormat)
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
        var imgNotification: ImageView = itemView.findViewById(R.id.imgNotification)
        var tvNoticationTitle: TextView = itemView.findViewById(R.id.tvNoticationTitle)
        var tvDesc: TextView = itemView.findViewById(R.id.tvDesc)
        var tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }

    interface ViewHolderClicks {
        fun onClickItem(model: NotificationModel, position: Int)
    }
}