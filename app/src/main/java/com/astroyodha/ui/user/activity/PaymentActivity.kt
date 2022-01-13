package com.astroyodha.ui.user.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.astroyodha.R
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityPaymentBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.model.user.AstrologerUserModel
import com.astroyodha.ui.user.authentication.model.user.UserModel
import com.astroyodha.ui.user.model.booking.BookingModel
import com.astroyodha.ui.user.model.wallet.WalletModel
import com.astroyodha.ui.user.viewmodel.BookingViewModel
import com.astroyodha.ui.user.viewmodel.ProfileViewModel
import com.astroyodha.ui.user.viewmodel.WalletViewModel
import com.astroyodha.utils.*
import org.json.JSONObject
import java.util.*

class PaymentActivity : BaseActivity(), PaymentResultListener {
    val TAG = javaClass.simpleName
    private lateinit var binding: ActivityPaymentBinding
    private val profileViewModel: ProfileViewModel by viewModels()
    private val walletViewModel: WalletViewModel by viewModels()
    private val bookingViewModel: BookingViewModel by viewModels()

    var userModel: UserModel = UserModel()
    private var walletModel: WalletModel = WalletModel()
    var amountInSubUnit = 0
    var amount = ""
    var minute = 0
    var isDirectPayment = false
    var isExtendMinute = false
    var transactionID = ""
    var bookingModel: BookingModel? = BookingModel()
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                //  you will get result here in result.data
                if (isDirectPayment) {
                    setResult(
                        Activity.RESULT_OK,
                        Intent().putExtra(Constants.INTENT_IS_DIRECT_PAYMENT, isDirectPayment)
                            .putExtra(Constants.INTENT_MODEL, Gson().toJson(walletModel))
                    )
                } else {
                    setResult(Activity.RESULT_OK, result.data)
                }
                onBackPressed()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getIntentData()
        init()
        setObserver()
//        setClickListener()
    }

    private fun getIntentData() {
        isDirectPayment = intent.getBooleanExtra(Constants.INTENT_IS_DIRECT_PAYMENT, false)
        isExtendMinute = intent.hasExtra(Constants.INTENT_IS_EXTEND_CALL)
        bookingModel = intent.getParcelableExtra(Constants.INTENT_BOOKING_MODEL)
        amount = intent.getStringExtra(Constants.INTENT_AMOUNT)!!
        minute = intent.getIntExtra(Constants.INTENT_MINUTE, minute)
    }

    /**
     * initialize view
     */
    private fun init() {
        Checkout.preload(applicationContext)
        profileViewModel.getUserDetail(FirebaseAuth.getInstance().currentUser?.uid.toString())
    }

    /**
     * set observer
     */
    private fun setObserver() {
        profileViewModel.userDetailResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        userModel = it
                        startPayment(amount)
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })

        profileViewModel.userDataResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        redirectToPaymentSuccessScreen()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })

        walletViewModel.walletDataResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        walletModel.trancationId = it.substringBefore(" ")
                        if (!isDirectPayment) {
                            userModel.walletBalance =
                                (userModel.walletBalance!!.toInt() + amount.toInt())
                            profileViewModel.updateUserData(
                                userModel
                            )
                        } else {
                            if(!isExtendMinute) {
                                //comes from add booking payment mode online
                                redirectToPaymentSuccessScreen()
                            } else {
                                //comes when user want's to extend call
                                //update booking end time
                                val minuteMillis: Long = 60000 //millisecs
                                val curTimeInMs: Long = bookingModel!!.endTime!!.time
                                MyLog.e(TAG, "current time is ${bookingModel!!.endTime.toString()}")
                                bookingModel!!.endTime = Date(curTimeInMs + minute * minuteMillis)
                                bookingModel!!.status = Constants.APPROVE_STATUS
                                MyLog.e(TAG, "extended time is ${bookingModel!!.endTime.toString()}")
                                bookingViewModel.addUpdateBookingData(
                                    bookingModel!!,
                                    true
                                )
                            }
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })

        bookingViewModel.bookingAddUpdateResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                       //booking extended successfully
                        setResult(
                            Activity.RESULT_OK,
                            Intent().putExtra(
                                Constants.INTENT_BOOKING_MODEL,
                                Gson().toJson(bookingModel)
                            )
                        )
                        onBackPressed()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })
    }

    private fun redirectToPaymentSuccessScreen() {
        startForResult.launch(
            Intent(this, ThankYouActivity::class.java)
                .putExtra(Constants.INTENT_IS_DIRECT_PAYMENT, isDirectPayment)
        )
    }

    fun startPayment(amount: String) {
        /*
          You need to pass current activity in order to let Razorpay create CheckoutActivity
         */

        amountInSubUnit = amount.getAmount().toInt() * 100

        val activity: Activity = this
        val co = Checkout()
        co.setKeyID(Constants.razorpay_key)

        try {
            val options = JSONObject()
            options.put("name", getString(R.string.app_name))
            options.put("description", "")
            //You can omit the image option to fetch the image from dashboard
            // options.put("image", Constants.BASE_IMAGE_URL+"512.png")
            options.put("theme.color", ContextCompat.getColor(this, R.color.orange_theme))
            options.put("currency", "INR")
//            options.put("status", Constants.RAZOR_PAY_STATUS_CAPTURED)
            //options.put("order_id", "order_DBJOWsdsdzybf0sJbb")
            options.put(
                "amount",
                amountInSubUnit
            )//pass amount in currency subunits

            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 4)
            options.put("retry", retryObj)

            val preFill = JSONObject()
            preFill.put("email", userModel.email)
            preFill.put("contact", userModel.phone)
            options.put("prefill", preFill)
            co.open(activity, options)
        } catch (e: Exception) {
            binding.root.showSnackBarToast("Error in payment: ${e.message}")
            e.printStackTrace()
        }

    }

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        try {
            if (razorpayPaymentID != null) {
                walletModel = WalletModel()
                var astrologerModel: AstrologerUserModel =
                    intent.getParcelableExtra(Constants.INTENT_MODEL)!!
                walletModel.trancationId = razorpayPaymentID
                walletModel.paymentType = Constants.PAYMENT_TYPE_RAZOR_PAY
                walletModel.bookingId = if(bookingModel?.id.isNullOrBlank()) "" else bookingModel?.id.toString()
                walletModel.description = ""
                walletModel.astrologerId = astrologerModel.uid.toString()
                walletModel.astrologerName = astrologerModel.fullName.toString()
                walletModel.userId = userModel.uid.toString()
                walletModel.userName = userModel.fullName.toString()
                walletModel.amount = amount.toInt()
                if (!isDirectPayment) {
//                    walletModel.amount = amount
                    walletModel.trancationType = Constants.TRANSACTION_TYPE_CREDIT
                    //adding money in wallet not required to add in astrologer side
                    walletModel.isMoneyAddedInWallet = true
                    walletViewModel.addMoney(walletModel, false)
                } else {
//                    walletModel.amount = "-$amount"
                    walletModel.trancationType = Constants.TRANSACTION_TYPE_DEBIT
                    walletViewModel.addMoney(walletModel, false, true)
                }
                /*if (!isDirectPayment) {
                    walletViewModel.addMoney(walletModel, false)
                } else {
                    redirectToPaymentSuccessScreen()
                }*/
            }
        } catch (e: java.lang.Exception) {
            MyLog.e(TAG, "Exception in onPaymentSuccess $e")
        }
    }

    override fun onPaymentError(code: Int, response: String?) {

        try {
            if (code == Checkout.PAYMENT_CANCELED) {
                val jsonObject = JSONObject(response)
                if (jsonObject.has("error")) {
                    val description = jsonObject.get("error")
                    val json = JSONObject(description.toString())
                    if (json.has("description")) {
                        val description = json.get("description")
                        toast("$description")
                        onBackPressed()
                    }
                }
            }/* else if(code == Checkout.INVALID_OPTIONS) {
                val jsonObject = JSONObject(response)
                if (jsonObject.has("description")) {
                    val description = jsonObject.get("description")
                    toast("$description")
                    onBackPressed()
                }
            }*/
        } catch (e: java.lang.Exception) {
            MyLog.e(TAG, "Exception in onPaymentSuccess$e")
        }
    }
}