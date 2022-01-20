package com.astroyodha.ui.user.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.astroyodha.R
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityAstrologerProfileViewBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.model.user.AstrologerUserModel
import com.astroyodha.ui.astrologer.viewmodel.AstrologerBookingViewModel
import com.astroyodha.ui.astrologer.viewmodel.ProfileAstrologerViewModel
import com.astroyodha.ui.user.adapter.ReviewAdapter
import com.astroyodha.ui.user.authentication.model.rating.RatingModel
import com.astroyodha.utils.*
import kotlin.math.roundToInt

class AstrologerProfileViewActivity : BaseActivity() {
    val TAG = javaClass.simpleName
    private lateinit var binding: ActivityAstrologerProfileViewBinding
    private val viewModel: ProfileAstrologerViewModel by viewModels()
    private val bookingViewModel: AstrologerBookingViewModel by viewModels()


    private var astrologerUserModel = AstrologerUserModel()
    private var mList: ArrayList<RatingModel> = arrayListOf()
    private val startBookNowForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                //  you will get result here in result.data
                setResult(Activity.RESULT_OK, Intent())
                onBackPressed()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAstrologerProfileViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getIntentData()
        init()
        setObserver()
        setClickListener()
    }

    private fun getIntentData() {
        val model: AstrologerUserModel = intent.getParcelableExtra(Constants.INTENT_MODEL)!!
        viewModel.getUserDetail(model.uid.toString())
    }

    /**
     * initialize view
     */
    private fun init() {
        with(binding.rvReview) {
            val mLayoutManager = LinearLayoutManager(context)
            layoutManager = mLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = ReviewAdapter(
                context, mList,
                object : ReviewAdapter.ViewHolderClicks {
                    override fun onClickItem(
                        model: RatingModel,
                        position: Int
                    ) {
                        //click of recyclerview item

                    }
                }
            )
        }

    }

    /**
     * set observer
     */
    private fun setObserver() {
        viewModel.userDetailResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    it.data?.let { result ->
                        astrologerUserModel = result
                        bookingViewModel.getAllCompletedBooking(astrologerUserModel.uid.toString())
                        setData(result)
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })

        bookingViewModel.completedBookingResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                }
                Status.SUCCESS -> {
                    it.data?.let {
                        binding.tvConsults.text = it
                        viewModel.getAstrologerRating(astrologerUserModel.uid.toString())
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })

        viewModel.ratingResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { result ->
                        mList.addAll(result)
                        setRatingData()
                        binding.rvReview.adapter?.notifyDataSetChanged()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })
    }

    /**
     * set data to view
     */
    private fun setData(model: AstrologerUserModel) {
        binding.imgAvatar.loadRoundedImage(model.profileImage)
        binding.tvAstrologerName.text = model.fullName?.substringBefore(" ")
        binding.tvPrice.text = getString(R.string.per_min_price, model.price.toString())

        var displayLanguageList = java.util.ArrayList<String>()


        for (language in Constants.listOfLanguages) {
            for (selectedLanguage in model.languages!!) {
                if (language.id.equals(selectedLanguage)) {
                    displayLanguageList.add(language.language)
                }
            }
        }

        binding.tvLanguage.text = displayLanguageList.joinToString(", ")


        var displaySpecialityList = ArrayList<String>()
        for (speciality in Constants.listOfSpeciality) {
            for (selectedSpeciality in model.speciality!!) {

                if (speciality.id.equals(selectedSpeciality)) {
                    displaySpecialityList.add(speciality.language)
                }
            }
        }


        binding.tvSpeciality.text = displaySpecialityList.joinToString(", ")
        binding.tvExperience.text = "${model.experience} yr"
        binding.tvRating.text = model.rating.toDouble().roundOffDecimal().toString()
        binding.tvRate.text = model.rating.toDouble().roundOffDecimal().toString()
        binding.tvAboutDesc.addReadMoreText(
            model.about,
            ContextCompat.getColor(this, R.color.orange_theme)
        )
        binding.groupAbout.isVisible = model.about.isNotBlank()

    }

    /**
     * set data to rating
     */
    private fun setRatingData() {
        val m5Star = mList.filter { it.rating == 5f }
        val m4Star = mList.filter { it.rating == 4f }
        val m3Star = mList.filter { it.rating == 3f }
        val m2Star = mList.filter { it.rating == 2f }
        val m1Star = mList.filter { it.rating == 1f }
        val total = mList.sumOf { it.rating.toInt() }
//        val avarage = (total / mList.size).toDouble().roundOffDecimal()

        var mRecomendedAvarage = 0.toDouble()
        var m5StarAvarage = 0.toDouble()
        var m4StarAvarage = 0.toDouble()
        var m3StarAvarage = 0.toDouble()
        var m2StarAvarage = 0.toDouble()
        var m1StarAvarage = 0.toDouble()
        if (mList.isNotEmpty()) {
            m5StarAvarage = (m5Star.size.toDouble() / mList.size) * 100
            m4StarAvarage = (m4Star.size.toDouble() / mList.size) * 100
            m3StarAvarage = (m3Star.size.toDouble() / mList.size) * 100
            m2StarAvarage = (m2Star.size.toDouble() / mList.size) * 100
            m1StarAvarage = (m1Star.size.toDouble() / mList.size) * 100
        }
        val avarage = if (mList.isNotEmpty()) {
            mList.map { it.rating }.average().roundOffDecimal()
        } else {
            0.toDouble().roundOffDecimal()
        }
        if (avarage != null) {
            mRecomendedAvarage = (avarage / 5) * 100
        }

        binding.tvRecommended.text =
            getString(
                R.string.per_of_customers_recommend_astrologer_based_on_review_count_reviews,
                mRecomendedAvarage.roundToInt().toString(),
                astrologerUserModel.fullName?.substringBefore(" "),
                mList.size.toString()
            )
        binding.progressBar5Star.progress = m5StarAvarage.roundToInt()
        binding.tv5StarPercentage.text = "${m5StarAvarage.roundToInt()}%"
        binding.progressBar4Star.progress = m4StarAvarage.roundToInt()
        binding.tv4StarPercentage.text = "${m4StarAvarage.roundToInt()}%"
        binding.progressBar3Star.progress = m3StarAvarage.roundToInt()
        binding.tv3StarPercentage.text = "${m3StarAvarage.roundToInt()}%"
        binding.progressBar2Star.progress = m2StarAvarage.roundToInt()
        binding.tv2StarPercentage.text = "${m2StarAvarage.roundToInt()}%"
        binding.progressBar1Star.progress = m1StarAvarage.roundToInt()
        binding.tv1StarPercentage.text = "${m1StarAvarage.roundToInt()}%"


    }

    /**
     * manage click listener of view
     */
    private fun setClickListener() {
        binding.imgClose.setOnClickListener {
            onBackPressed()
        }

        binding.imgAdd.setOnClickListener {
            redirectToBookEvent()
        }

        binding.btnBookNow.setOnClickListener {
            redirectToBookEvent()
        }
    }

    /**
     * redirect to event booking
     */
    private fun redirectToBookEvent() {
        startBookNowForResult.launch(
            Intent(this, EventBookingActivity::class.java)
                .putExtra(Constants.INTENT_ISEDIT, false)
                .putExtra(Constants.INTENT_MODEL, astrologerUserModel)
        )
    }
}