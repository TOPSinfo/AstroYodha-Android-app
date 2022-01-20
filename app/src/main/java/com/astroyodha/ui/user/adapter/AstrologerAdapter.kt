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
import com.astroyodha.ui.astrologer.model.user.AstrologerUserModel
import com.astroyodha.utils.Constants
import com.astroyodha.utils.loadRoundedImage
import com.astroyodha.utils.roundOffDecimal
import java.util.*

/**
 * Astrologer list adapter : Show Astrologer list.
 */
class AstrologerAdapter(
    private val mContext: Context, var mList: ArrayList<AstrologerUserModel>,
    private val viewHolderClicks: ViewHolderClicks,
) : RecyclerView.Adapter<AstrologerAdapter.ViewHolder>() {
    var mInflater: LayoutInflater =
        mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v =
            mInflater.inflate(R.layout.raw_astrologer_card, parent, false)
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
            holder.imgAvatar.loadRoundedImage(model.profileImage, 14)
            holder.tvName.text = model.fullName?.substringBefore(" ")
            holder.tvRate.text = model.rating.toDouble().roundOffDecimal().toString()

            var displaySpecialityList = ArrayList<String>()
            for (speciality in Constants.listOfSpeciality) {
                for (selectedSpeciality in model.speciality!!) {

                    if (speciality.id.equals(selectedSpeciality)) {
                        displaySpecialityList.add(speciality.language)
                    }
                }
            }


            holder.tvDesc.text = displaySpecialityList.joinToString(", ")

            holder.itemView.setOnClickListener {
                viewHolderClicks.onProfileClickItem(model, holder.adapterPosition)
            }
            holder.btnBookNow.setOnClickListener {
                viewHolderClicks.onBookNowClickItem(model, holder.adapterPosition)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)
        var tvName: TextView = itemView.findViewById(R.id.tvName)
        var imgVerified: ImageView = itemView.findViewById(R.id.imgVerified)
        var tvRate: TextView = itemView.findViewById(R.id.tvRate)
        var tvDesc: TextView = itemView.findViewById(R.id.tvDesc)
        var btnBookNow: TextView = itemView.findViewById(R.id.btnBookNow)
    }

    interface ViewHolderClicks {
        fun onBookNowClickItem(model: AstrologerUserModel, position: Int)
        fun onProfileClickItem(model: AstrologerUserModel, position: Int)
    }
}