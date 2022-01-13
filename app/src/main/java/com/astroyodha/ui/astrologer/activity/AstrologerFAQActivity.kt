package com.astroyodha.ui.astrologer.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityAstrologerFaqBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.adapter.AstrologerFAQAdapter
import com.astroyodha.ui.user.authentication.model.cms.FAQModel
import com.astroyodha.ui.user.authentication.viewmodel.SplashViewModel
import com.astroyodha.utils.Constants
import com.astroyodha.utils.makeGone
import com.astroyodha.utils.makeVisible
import com.astroyodha.utils.showSnackBarToast

class AstrologerFAQActivity : BaseActivity() {
    val TAG = javaClass.simpleName
    private lateinit var binding: ActivityAstrologerFaqBinding

    private val viewModel: SplashViewModel by viewModels()
    private var mList: ArrayList<FAQModel> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAstrologerFaqBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setObserver()
        setClickListener()
    }

    /**
     * initialize view
     */
    private fun init() {
        with(binding.rvFAQ) {
            val mLayoutManager = LinearLayoutManager(context)
            layoutManager = mLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = AstrologerFAQAdapter(
                context, mList,
                object : AstrologerFAQAdapter.ViewHolderClicks {
                    override fun onClickItem(
                        model: FAQModel,
                        position: Int
                    ) {
                        //click of recyclerview item

                    }
                }
            )
        }

        viewModel.getFAQ(Constants.FAQ_POLICY)
    }

    /**
     * set observer
     */
    private fun setObserver() {
        viewModel.faqResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { result ->
                        mList.addAll(result)
                        binding.rvFAQ.adapter?.notifyDataSetChanged()

                        if (mList.isEmpty()) {
                            binding.tvNoDataFound.makeVisible()
                        } else {
                            binding.tvNoDataFound.makeGone()
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