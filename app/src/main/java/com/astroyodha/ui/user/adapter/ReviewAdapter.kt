package com.astroyodha.ui.user.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.astroyodha.R
import com.astroyodha.ui.user.authentication.model.rating.RatingModel
import com.astroyodha.utils.dateToStringFormat

/**
 * Review list adapter : Show Review list.
 */
class ReviewAdapter(
    private val mContext: Context, var mList: ArrayList<RatingModel>,
    private val viewHolderClicks: ViewHolderClicks,
) : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {
    var mDateFormat: String = "dd MMM yyyy"
    var mInflater: LayoutInflater =
        mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v =
            mInflater.inflate(R.layout.raw_review, parent, false)
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
            holder.tvFirstCharacter.text = model.userName.first().toString()
            holder.tvName.text = model.userName.substringBefore(" ")
            holder.tvTime.text = model.createdAt?.toDate()?.dateToStringFormat(mDateFormat)
            holder.userRating.rating = model.rating.toFloat()
            holder.tvReview.text = model.feedBack

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
        var tvFirstCharacter: TextView = itemView.findViewById(R.id.tvFirstCharacter)
        var tvName: TextView = itemView.findViewById(R.id.tvName)
        var tvTime: TextView = itemView.findViewById(R.id.tvTime)
        var userRating: RatingBar = itemView.findViewById(R.id.userRating)
        var tvReview: TextView = itemView.findViewById(R.id.tvReview)
    }

    interface ViewHolderClicks {
        fun onClickItem(model: RatingModel, position: Int)
    }
}