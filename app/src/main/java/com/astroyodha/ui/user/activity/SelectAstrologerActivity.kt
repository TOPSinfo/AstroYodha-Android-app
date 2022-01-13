package com.astroyodha.ui.user.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.astroyodha.R
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivitySelectAstrologerBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.model.language.LanguageAndSpecialityModel
import com.astroyodha.ui.astrologer.model.user.AstrologerUserModel
import com.astroyodha.ui.astrologer.viewmodel.ProfileAstrologerViewModel
import com.astroyodha.ui.user.adapter.AstrologerAdapter
import com.astroyodha.ui.user.authentication.model.speciality.SpecialityModel
import com.astroyodha.ui.user.dialog.FilterDialog
import com.astroyodha.utils.*

@SuppressLint("NotifyDataSetChanged")
class SelectAstrologerActivity : BaseActivity() {
    val TAG = javaClass.simpleName
    private lateinit var binding: ActivitySelectAstrologerBinding
    private val viewModel: ProfileAstrologerViewModel by viewModels()

    private var mAstrologerList: ArrayList<AstrologerUserModel> = arrayListOf()
    private var mSpeciality: ArrayList<SpecialityModel> = arrayListOf()

    var selectedSortBy = ""
    var mSelectedSpeciality: ArrayList<LanguageAndSpecialityModel> = arrayListOf()

    private val startBookNowForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                //  you will get result here in result.data
                setResult()
            }
        }
    private val startProfileForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                //  you will get result from astrologer profile
                setResult()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectAstrologerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setObserver()
        setClickListener()
    }

    /**
     * initialize view
     */
    private fun init() {
        selectedSortBy = getString(R.string.experience_high_to_low)
        with(binding.rvAstrologer) {
            val mLayoutManager = GridLayoutManager(context, 2)
            layoutManager = mLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = AstrologerAdapter(
                context, mAstrologerList,
                object : AstrologerAdapter.ViewHolderClicks {
                    override fun onBookNowClickItem(
                        model: AstrologerUserModel,
                        position: Int
                    ) {
                        //click of recyclerview item
                        startBookNowForResult.launch(
                            Intent(this@SelectAstrologerActivity, EventBookingActivity::class.java)
                                .putExtra(Constants.INTENT_ISEDIT, false)
                                .putExtra(Constants.INTENT_MODEL, model)
                        )
                    }

                    override fun onProfileClickItem(model: AstrologerUserModel, position: Int) {
                        startProfileForResult.launch(
                            Intent(
                                this@SelectAstrologerActivity,
                                AstrologerProfileViewActivity::class.java
                            )
                                .putExtra(Constants.INTENT_MODEL, model)
                        )
                    }
                }
            )
        }

        viewModel.getAllAstrologer()
    }

    /**
     * set observer
     */
    private fun setObserver() {
        viewModel.getAllAstrologerResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { result ->
                        mAstrologerList.clear()
                        mAstrologerList.addAll(result)
                        binding.rvAstrologer.adapter?.notifyDataSetChanged()

                        if (mAstrologerList.isEmpty()) {
                            binding.tvNoDataFound.makeVisible()
                        } else {
                            binding.tvNoDataFound.makeGone()
                        }
//                        viewModel.getSpeciality()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })

//        viewModel.specialityResponse.observe(this, {
//            when (it.status) {
//                Status.LOADING -> {
//                    showProgress(this)
//                }
//                Status.SUCCESS -> {
//                    hideProgress()
//                    it.data?.let { result ->
//                        mSpeciality.clear()
//                        mSpeciality.addAll(result)
//                    }
//                }
//                Status.ERROR -> {
//                    hideProgress()
//                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
//                }
//            }
//        })
    }

    private fun setResult() {
        setResult(Activity.RESULT_OK, Intent())
        onBackPressed()
    }

    /**
     * manage click listener of view
     */
    private fun setClickListener() {
        binding.imgClose.setOnClickListener {
            onBackPressed()
        }

        binding.imgFilter.setOnClickListener {
//            showFilterDialog()
            FilterDialog(
                this,
                Constants.listOfSpeciality,
                selectedSortBy,
                mSelectedSpeciality,
                object : FilterDialog.FilterListener {
                    override fun onFilterApplyed(
                        sortBy: String,
                        speciality: ArrayList<LanguageAndSpecialityModel>
                    ) {
                        MyLog.e(TAG, "data == $sortBy, Speciality == $speciality")
                        selectedSortBy = sortBy
                        mSelectedSpeciality = speciality
                        val mList = mSelectedSpeciality.map { it.id }
                        viewModel.getAllAstrologerFilterWise(selectedSortBy, mList)
                        if (selectedSortBy.isBlank()) {
                            selectedSortBy = getString(R.string.experience_high_to_low)
                        }
                    }
                })
        }
    }
}