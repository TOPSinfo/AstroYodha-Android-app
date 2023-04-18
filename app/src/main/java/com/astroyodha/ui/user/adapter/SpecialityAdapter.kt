package com.astroyodha.ui.user.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.astroyodha.R
import com.astroyodha.ui.astrologer.model.language.LanguageAndSpecialityModel

/**
 * Review list adapter : Show Review list.
 */
class SpecialityAdapter(
    private val mContext: Context, var mList: ArrayList<LanguageAndSpecialityModel>, var mselectedList: ArrayList<LanguageAndSpecialityModel>,
    private val viewHolderClicks: ViewHolderClicks,
) : RecyclerView.Adapter<SpecialityAdapter.ViewHolder>() {
    var mInflater: LayoutInflater =
        mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v =
            mInflater.inflate(R.layout.raw_speciality, parent, false)
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
            holder.cbSpeciality.text = model.language
            mselectedList.forEach {
                if (model.language == it.language) {
                    holder.cbSpeciality.isChecked = true
                    return@forEach
                }
            }
            holder.cbSpeciality.setOnClickListener {
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
        var cbSpeciality: CheckBox = itemView.findViewById(R.id.cbSpeciality)
    }

    interface ViewHolderClicks {
        fun onClickItem(model: LanguageAndSpecialityModel, position: Int)
    }
}