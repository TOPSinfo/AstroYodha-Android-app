package com.astroyodha.ui.user.dialog

import android.content.Context
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.astroyodha.R
import com.astroyodha.ui.astrologer.model.language.LanguageAndSpecialityModel
import com.astroyodha.ui.user.adapter.SpecialityAdapter
import com.astroyodha.utils.makeGone
import com.astroyodha.utils.makeVisible


class FilterDialog(
    val mContext: Context,
    private val mSpeciality: ArrayList<LanguageAndSpecialityModel>,
    private var selectedSortBy: String,
    private var outerSelectedSpeciality: ArrayList<LanguageAndSpecialityModel>,
    private val listener: FilterListener
) : BottomSheetDialog(mContext) {
    interface FilterListener {
        fun onFilterApplyed(sortBy: String, speciality: ArrayList<LanguageAndSpecialityModel>)
    }

    // used extra variable because directly made changes on outerSelectedSpeciality updating parent activity  list
    private var innerSelectedSpeciality: ArrayList<LanguageAndSpecialityModel> = arrayListOf()

    init {
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        create()
    }

    override fun create() {

        val bottomSheetView = layoutInflater.inflate(R.layout.dialog_filter, null)
        val dialog = BottomSheetDialog(mContext, R.style.BottomSheetDialog) // Style here
        dialog.setContentView(bottomSheetView)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.show()

        innerSelectedSpeciality.addAll(outerSelectedSpeciality)
        val tvSortBy: TextView = bottomSheetView.findViewById(R.id.tvSortBy)
        val rgSortBy: RadioGroup = bottomSheetView.findViewById(R.id.rgSortBy)
        val tvSpeciality: TextView = bottomSheetView.findViewById(R.id.tvSpeciality)
        val rvSpeciality: RecyclerView = bottomSheetView.findViewById(R.id.rvSpeciality)
        val btnReset: MaterialButton = bottomSheetView.findViewById(R.id.btnReset)
        val btnApply: MaterialButton = bottomSheetView.findViewById(R.id.btnApply)

        tvSortBy.isSelected = true
        rvSpeciality.makeGone()

        rgSortBy.apply {
            for (i in 0..childCount) {
                val radioButton = getChildAt(i) as RadioButton
                if (radioButton.text == selectedSortBy) {
                    check(getChildAt(i).id)
                    return@apply
                }
            }
        }

        tvSortBy.setOnClickListener {
            if (!tvSortBy.isSelected) {
                tvSortBy.isSelected = true
                tvSpeciality.isSelected = false
                rvSpeciality.makeGone()
                rgSortBy.makeVisible()
            }
        }
        tvSpeciality.setOnClickListener {
            if (!tvSpeciality.isSelected) {
                tvSpeciality.isSelected = true
                tvSortBy.isSelected = false
                rvSpeciality.makeVisible()
                rgSortBy.makeGone()
            }
        }

        rgSortBy.setOnCheckedChangeListener { radioGroup, checkedId ->
            val radioButton = radioGroup.findViewById(checkedId) as RadioButton
            selectedSortBy = radioButton.text.toString()
        }

        with(rvSpeciality) {
            layoutManager = LinearLayoutManager(mContext)
            itemAnimator = DefaultItemAnimator()
            adapter = SpecialityAdapter(
                context, mSpeciality, innerSelectedSpeciality,
                object : SpecialityAdapter.ViewHolderClicks {
                    override fun onClickItem(
                        model: LanguageAndSpecialityModel,
                        position: Int
                    ) {
                        //click of recyclerview item
                        if (innerSelectedSpeciality.isEmpty()) {
                            innerSelectedSpeciality.add(model)
                        } else {
                            innerSelectedSpeciality.toMutableList()
                                .forEachIndexed { index, specialityModel ->
                                    if (specialityModel.id == model.id) {
                                        innerSelectedSpeciality.removeAt(index)
                                        return
                                    } else {
                                        innerSelectedSpeciality.add(model)
                                        return
                                    }
                                }
                        }
                    }
                }
            )
        }

        btnReset.setOnClickListener {
            listener.onFilterApplyed("", ArrayList())
            dialog.dismiss()
        }

        btnApply.setOnClickListener {
            listener.onFilterApplyed(selectedSortBy, innerSelectedSpeciality)
            dialog.dismiss()
        }

    }
}