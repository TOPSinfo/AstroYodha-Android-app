package com.astroyodha.ui.astrologer.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.astroyodha.R
import com.astroyodha.core.BaseFragment
import com.astroyodha.databinding.FragmentPriceBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.model.price.PriceModel
import com.astroyodha.ui.astrologer.viewmodel.PriceViewModel
import com.astroyodha.utils.showSnackBarToast

class PriceFragment : BaseFragment() {
    private val TAG = "PriceFragment"

    private val profileViewModel: PriceViewModel by viewModels()
    private lateinit var binding: FragmentPriceBinding

    var userModel: PriceModel = PriceModel()
    var userID: String = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPriceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setObserver()
        clickListener()
    }

    /**
     * initialize view
     */
    private fun init() {
        userID = FirebaseAuth.getInstance().currentUser?.uid.toString()
        userModel.userId = userID
        profileViewModel.getUserDetail(userModel)
    }

    /**
     * manage click listener of view
     */
    private fun clickListener() {

        binding.btnSubmit.setOnClickListener {
            if (checkValidation()) {
                userModel.userId = userID
                userModel.fifteenMin = binding.edFifteenMin.text.toString()
                userModel.thirtyMin = binding.edThirtyMin.text.toString()
                userModel.fortyFiveMin = binding.edFortyFiveMin.text.toString()
                userModel.sixtyMin = binding.edSixtyMin.text.toString()
                profileViewModel.addUpdatePriceData(userModel)
            }
        }
        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    /**
     * set observer
     */
    private fun setObserver() {

        profileViewModel.priceDetailResponse.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(requireContext())
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        userModel = it
                        if (userModel.id.isNotBlank()) {
                            setUserData()
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })

        profileViewModel.priceDataResponse.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(requireContext())
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        binding.root.showSnackBarToast(it)
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
    private fun setUserData() {
        userModel.fifteenMin.let {
            binding.edFifteenMin.setText(it)
        }

        userModel.thirtyMin.let {
            binding.edThirtyMin.setText(it)
        }

        userModel.fortyFiveMin.let {
            binding.edFortyFiveMin.setText(it)
        }

        userModel.sixtyMin.let {
            binding.edSixtyMin.setText(it)
        }
    }

    /**
     * Checking validation
     */
    private fun checkValidation(): Boolean {

        if (TextUtils.isEmpty(binding.edFifteenMin.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_15_min_charges))
            return false
        } else if (TextUtils.isEmpty(binding.edThirtyMin.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_30_min_charges))
            return false
        } else if (TextUtils.isEmpty(binding.edFortyFiveMin.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_45_min_charges))
            return false
        } else if (TextUtils.isEmpty(binding.edSixtyMin.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_60_min_charges))
            return false
        }
        return true
    }
}