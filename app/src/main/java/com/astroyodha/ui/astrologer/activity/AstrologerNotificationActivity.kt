package com.astroyodha.ui.astrologer.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityAstrologerNotificationBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.adapter.AstrologerNotificationAdapter
import com.astroyodha.ui.user.adapter.NotificationAdapter
import com.astroyodha.ui.user.model.notification.NotificationModel
import com.astroyodha.ui.user.viewmodel.NotificationViewModel
import com.astroyodha.utils.makeGone
import com.astroyodha.utils.makeVisible
import com.astroyodha.utils.showSnackBarToast
import com.google.firebase.auth.FirebaseAuth

class AstrologerNotificationActivity : BaseActivity() {
    val TAG = javaClass.simpleName
    private lateinit var binding: ActivityAstrologerNotificationBinding
    private val viewModel: NotificationViewModel by viewModels()
    private var mList: ArrayList<NotificationModel> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAstrologerNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setObserver()
        setClickListener()
    }

    /**
     * initialize view
     */
    private fun init() {
        with(binding.rvNotification) {
            val mLayoutManager = LinearLayoutManager(context)
            layoutManager = mLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = AstrologerNotificationAdapter(
                context, mList,
                object : AstrologerNotificationAdapter.ViewHolderClicks {
                    override fun onClickItem(
                        model: NotificationModel,
                        position: Int
                    ) {
                        //click of recyclerview item

                    }
                }
            )
        }

        viewModel.getAllNotification(FirebaseAuth.getInstance().currentUser?.uid.toString())
    }

    /**
     * set observer
     */
    private fun setObserver() {
        viewModel.getNotificationListDataResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { resultList ->
                        mList.addAll(resultList)
                        binding.rvNotification.adapter?.notifyDataSetChanged()

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