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
import com.astroyodha.ui.user.model.booking.BookingModel
import com.astroyodha.utils.Constants
import com.astroyodha.utils.loadRoundedImage
import com.astroyodha.utils.makeGone
import java.util.*

/**
 * Booking list adapter : Show booking request.
 */
class BookingAstrologerAdapter(
    private val mContext: Context, var mList: ArrayList<BookingModel>,
    private val viewHolderClicks: ViewHolderClicks,
) : RecyclerView.Adapter<BookingAstrologerAdapter.ViewHolder>() {
    var mInflater: LayoutInflater =
        mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v =
            mInflater.inflate(R.layout.raw_booking_astrologer, parent, false)
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

            holder.tvName.text = model.userName.substringBefore(" ")
            holder.tvBirthDate.text = mContext.getString(R.string.birth_Date_value, model.userBirthday)

            holder.imgAvatar.loadRoundedImage(model.userProfileImage, 14)

            if (model.status == Constants.APPROVE_STATUS) {
                holder.btnAccept.text = mContext.getString(R.string.accepted)
                holder.btnReject.makeGone()
            } else if (model.status == Constants.REJECT_STATUS) {
                holder.btnReject.text = mContext.getString(R.string.rejected)
                holder.btnAccept.makeGone()
            }

            holder.btnAccept.setOnClickListener {
                viewHolderClicks.onClickItem(model, true, holder.adapterPosition,false)
            }
            holder.btnReject.setOnClickListener {
                viewHolderClicks.onClickItem(model, false, holder.adapterPosition,false)
            }
            holder.itemView.setOnClickListener {
                viewHolderClicks.onClickItem(model, false, holder.adapterPosition,true)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvName: TextView = itemView.findViewById(R.id.tvName)
        var tvBirthDate: TextView = itemView.findViewById(R.id.tvBirthDate)
        var btnAccept: TextView = itemView.findViewById(R.id.btnAccept)
        var btnReject: TextView = itemView.findViewById(R.id.btnReject)
        var imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)
    }

    interface ViewHolderClicks {
        fun onClickItem(model: BookingModel, isAccept: Boolean, position: Int,isForDetail:Boolean)
    }
}