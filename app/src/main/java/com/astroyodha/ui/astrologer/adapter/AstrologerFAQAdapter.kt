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
import com.astroyodha.ui.user.authentication.model.cms.FAQModel
import com.astroyodha.utils.makeGone
import com.astroyodha.utils.makeVisible

/**
 * FAQ list adapter : Show FAQ list.
 */
class AstrologerFAQAdapter(
    private val mContext: Context, var mList: ArrayList<FAQModel>,
    private val viewHolderClicks: ViewHolderClicks,
) : RecyclerView.Adapter<AstrologerFAQAdapter.ViewHolder>() {
    var mCheckedPosition = -1
    var mInflater: LayoutInflater =
        mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v =
            mInflater.inflate(R.layout.raw_faq, parent, false)
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
            holder.imgDown.setColorFilter(mContext.getColor(R.color.astrologer_theme))
            if(holder.adapterPosition == mCheckedPosition) {
                holder.tvAnswer.makeVisible()
                holder.tvQuestion.setTextColor(mContext.getColor(R.color.astrologer_theme))
                holder.imgDown.setImageResource(R.drawable.ic_up_arrow)
            } else {
                holder.tvAnswer.makeGone()
                holder.tvQuestion.setTextColor(mContext.getColor(R.color.text_gray))
                holder.imgDown.setImageResource(R.drawable.ic_down_arrow)
            }

            holder.tvQuestion.text = model.title
            holder.tvAnswer.text = model.answer

            holder.itemView.setOnClickListener {
                mCheckedPosition = if(mCheckedPosition == position) -1 else position
                notifyDataSetChanged()
//                viewHolderClicks.onClickItem(model, holder.adapterPosition)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvQuestion: TextView = itemView.findViewById(R.id.tvQuestion)
        var tvAnswer: TextView = itemView.findViewById(R.id.tvAnswer)
        var imgDown: ImageView = itemView.findViewById(R.id.imgDown)
    }

    interface ViewHolderClicks {
        fun onClickItem(model: FAQModel, position: Int)
    }
}