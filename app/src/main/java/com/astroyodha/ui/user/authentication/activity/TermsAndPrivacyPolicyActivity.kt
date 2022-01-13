package com.astroyodha.ui.user.authentication.activity

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.activity.viewModels
import com.astroyodha.R
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityTermsAndPrivacyPolicyBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.user.authentication.viewmodel.SplashViewModel
import com.astroyodha.utils.Constants
import com.astroyodha.utils.showSnackBarToast

class TermsAndPrivacyPolicyActivity : BaseActivity() {
    val TAG = javaClass.simpleName
    private lateinit var binding: ActivityTermsAndPrivacyPolicyBinding
    var userType:String=""

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsAndPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)
/*
        binding.tvDescription.append(binding.tvDescription.text)
        binding.tvDescription.append(binding.tvDescription.text)
        binding.tvDescription.append(binding.tvDescription.text)
        binding.tvDescription.setHtmlText(binding.tvDescription.text.toString())*/

        init()
        setClickListener()
        setObserver()
    }

    /**
     * initialize view
     */
    private fun init() {
        val mFrom = intent.getStringExtra(Constants.INTENT_IS_FROM)

        intent.getStringExtra(Constants.INTENT_USER_TYPE).also {
            userType = it!!
        }

        if(userType==Constants.USER_ASTROLOGER)
        {
            binding.imgClose.setImageResource(R.drawable.astrologer_back)
        }
        else
        {
            binding.imgClose.setImageResource(R.drawable.ic_back)
        }


        /*if(mFrom == Constants.TERM_AND_CONDITION_POLICY) {

        } else if(mFrom == Constants.PRIVACY_POLICY) {

        } else if(mFrom == Constants.FAQ_POLICY) {

        }*/
        viewModel.getPolicies(mFrom.toString())

    }
    /**
     * Initialize observer
     */
    private fun setObserver() {

        viewModel.termsAndPolicyResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { resultList ->
                        if(resultList.isNotEmpty()) {
                            binding.tvTitle.text = resultList[0].title

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                binding.tvDescription.setText(Html.fromHtml(resultList[0].content, Html.FROM_HTML_MODE_COMPACT));
                            } else
                                binding.tvDescription.setText(Html.fromHtml(resultList[0].content));

                            binding.tvDescription.movementMethod = LinkMovementMethod.getInstance();

//                            binding.tvDescription.setHtmlText(resultList[0].content)
                        }
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
     * manage click listener of view
     */
    private fun setClickListener() {
        binding.imgClose.setOnClickListener {
            onBackPressed()
        }
    }
}