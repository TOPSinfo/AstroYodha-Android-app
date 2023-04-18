package com.astroyodha.ui.astrologer.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.RecyclerView
import com.astroyodha.R
import com.astroyodha.ui.astrologer.model.language.LanguageAndSpecialityModel

/**
 * Language list adapter : Show list of languages.
 */
class AstrologerLanguageAndAstologyTypeAdapter(
    private val mContext: Context, var mList: ArrayList<LanguageAndSpecialityModel>,
    var selectedList: ArrayList<String>,
) : RecyclerView.Adapter<AstrologerLanguageAndAstologyTypeAdapter.ViewHolder>() {
    var mInflater: LayoutInflater =
        mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    var selectedLanaguagesAndAstroType:ArrayList<String> = ArrayList()
    init{
        selectedLanaguagesAndAstroType.addAll(selectedList)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v =
            mInflater.inflate(R.layout.row_language, parent, false)
        return ViewHolder(
            v
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val languageAndAstroType = mList[holder.adapterPosition]
        try {
            holder.cbLanguage.setText(languageAndAstroType.language)

            holder.cbLanguage.isChecked = selectedList.contains(languageAndAstroType.id)

            holder.cbLanguage.setOnCheckedChangeListener { _, b ->
                if (b) {
                    selectedLanaguagesAndAstroType.add(languageAndAstroType.id)
                } else {
                    selectedLanaguagesAndAstroType.remove(languageAndAstroType.id)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun getSelectedLanguagesAndAstroType(): ArrayList<String> {
        return selectedLanaguagesAndAstroType
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cbLanguage: AppCompatCheckBox = itemView.findViewById(R.id.cbLanguage)
    }

}