package com.astroyodha.ui.astrologer.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityAstrologerTransactionBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.adapter.AstrologerTransactionAdapter
import com.astroyodha.ui.user.model.wallet.WalletModel
import com.astroyodha.ui.user.viewmodel.WalletViewModel
import com.astroyodha.utils.Constants
import com.astroyodha.utils.makeGone
import com.astroyodha.utils.makeVisible
import com.astroyodha.utils.showSnackBarToast

class AstrologerTransactionActivity : BaseActivity() {
    val TAG = javaClass.simpleName
    private lateinit var binding: ActivityAstrologerTransactionBinding
    private val viewModel: WalletViewModel by viewModels()
    private var mList: ArrayList<WalletModel> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAstrologerTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setObserver()
        setClickListener()
    }

    /**
     * initialize view
     */
    private fun init() {
        with(binding.rvTransaction) {
            val mLayoutManager = LinearLayoutManager(context)
            layoutManager = mLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = AstrologerTransactionAdapter(
                context, mList,
                object : AstrologerTransactionAdapter.ViewHolderClicks {
                    override fun onClickItem(
                        model: WalletModel,
                        position: Int
                    ) {
                        //click of recyclerview item

                    }
                }
            )
        }


        viewModel.getAllTransaction(FirebaseAuth.getInstance().currentUser?.uid.toString())

    }

    /**
     * set observer
     */
    private fun setObserver() {
        viewModel.getTransactionListDataResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { resultList ->
                        mList.addAll(resultList)
                        binding.rvTransaction.adapter?.notifyDataSetChanged()

                        var totalAmount=0
                        if (mList.isEmpty()) {
                            binding.tvNoDataFound.makeVisible()
                            binding.lnBottomBar.makeGone()
                        } else {
                            binding.tvNoDataFound.makeGone()
                            binding.lnBottomBar.makeVisible()
                            for (transactionData in mList)
                            {
                                if(transactionData.trancationType==Constants.TRANSACTION_TYPE_CREDIT)
                                {
                                    totalAmount += transactionData.amount
                                }
                                else if(transactionData.trancationType==Constants.TRANSACTION_TYPE_DEBIT)
                                {
                                    totalAmount -= transactionData.amount
                                }
                            }
                        }

                        binding.tvTotalEarning.text=totalAmount.toString()
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