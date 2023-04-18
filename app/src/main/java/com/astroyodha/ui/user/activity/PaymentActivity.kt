package com.astroyodha.ui.user.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.astroyodha.R
import com.astroyodha.core.BaseActivity
import com.astroyodha.databinding.ActivityPaymentBinding
import com.astroyodha.network.Status
import com.astroyodha.ui.astrologer.model.user.AstrologerUserModel
import com.astroyodha.ui.astrologer.viewmodel.ProfileAstrologerViewModel
import com.astroyodha.ui.user.authentication.model.user.UserModel
import com.astroyodha.ui.user.model.booking.BookingModel
import com.astroyodha.ui.user.model.wallet.WalletModel
import com.astroyodha.ui.user.viewmodel.BookingViewModel
import com.astroyodha.ui.user.viewmodel.ProfileViewModel
import com.astroyodha.ui.user.viewmodel.WalletViewModel
import com.astroyodha.utils.Constants
import com.astroyodha.utils.getAmount
import com.astroyodha.utils.showSnackBarToast
import com.astroyodha.utils.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import java.util.*

class PaymentActivity : BaseActivity(), PaymentResultListener {
    val TAG = javaClass.simpleName
    private lateinit var binding: ActivityPaymentBinding
    private val profileViewModel: ProfileViewModel by viewModels()
    private val walletViewModel: WalletViewModel by viewModels()
    private val bookingViewModel: BookingViewModel by viewModels()
    private val astrologerProfileViewModel: ProfileAstrologerViewModel by viewModels()

    var userModel: UserModel = UserModel()
    var astrologerModel: AstrologerUserModel = AstrologerUserModel()
    private var walletModel: WalletModel = WalletModel()
    var amountInSubUnit = 0
    var amount = ""
    var minute = 0
    var isDirectPayment = false
    var isExtendMinute = false
    var transactionID = ""
    var bookingModel: BookingModel? = BookingModel()

    var photoPath: Uri? = null
    var kundaliPath: Uri? = null


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
    }

    private fun getIntentData() {
        isDirectPayment = intent.getBooleanExtra(Constants.INTENT_IS_DIRECT_PAYMENT, false)
        isExtendMinute = intent.hasExtra(Constants.INTENT_IS_EXTEND_CALL)
        bookingModel = intent.getParcelableExtra(Constants.INTENT_BOOKING_MODEL)
        astrologerModel = intent.getParcelableExtra(Constants.INTENT_MODEL)!!
        amount = intent.getStringExtra(Constants.INTENT_AMOUNT)!!
        minute = intent.getIntExtra(Constants.INTENT_MINUTE, minute)

        if (intent.hasExtra(Constants.INTENT_PHOTO_URI)) {
            photoPath = Uri.parse(intent.getStringExtra(Constants.INTENT_PHOTO_URI))
        }

        if (intent.hasExtra(Constants.INTENT_KUNDALI_URI)) {
            kundaliPath = Uri.parse(intent.getStringExtra(Constants.INTENT_KUNDALI_URI))
        }

    }

    /**
     * initialize view
     */
    private fun init() {
        Checkout.preload(applicationContext)
        profileViewModel.getUserDetail(FirebaseAuth.getInstance().currentUser?.uid.toString())
        if(isDirectPayment) {
            astrologerProfileViewModel.getUserSnapshotDetail(astrologerModel.uid.toString())
        }
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

        //astrologer profile
        astrologerProfileViewModel.userDetailResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { result ->
                        if (result.uid == astrologerModel.uid) {
                            astrologerModel = result
                        }
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
                        if (!walletModel.bookingId.isNullOrBlank() && !isExtendMinute) {
                            //after booking added booking id will be not blank
                            astrologerModel.walletBalance =
                                (astrologerModel.walletBalance!!.toInt() + amount.toInt())
                            astrologerProfileViewModel.updateAstrologerWalletBalance(
                                astrologerModel.uid!!,
                                astrologerModel.walletBalance!!
                            )
                            return@let
                        }

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
                                bookingModel?.transactionId = walletModel.trancationId
                                if (walletModel.paymentType == Constants.PAYMENT_TYPE_WALLET) {
                                    bookingModel?.paymentStatus = ""
                                } else {
                                    bookingModel?.paymentStatus = Constants.RAZOR_PAY_STATUS_AUTHORIZED
                                }
                                bookingModel?.paymentType = walletModel.paymentType
                                bookingModel?.amount = walletModel.amount

                                if(kundaliPath!=null && photoPath!=null) {

                                    bookingViewModel.uploadPhotoAndKundaliForAddBooking(
                                        bookingModel!!,photoPath,kundaliPath,
                                        false
                                    )
                                }
                            } else {
                                //comes when user want's to extend call
                                //update booking end time
                                val minuteMillis: Long = 60000 //millisecs
                                val curTimeInMs: Long = bookingModel!!.endTime!!.time
                                bookingModel!!.endTime = Date(curTimeInMs + minute * minuteMillis)
                                bookingModel!!.status = Constants.APPROVE_STATUS
                                bookingModel!!.allowExtendTIme = Constants.EXTEND_STATUS_COMPLETE

                                bookingViewModel.extendBookingMinute(
                                    bookingModel!!
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


        //add event and upload photo or kundali
        bookingViewModel.bookingUploadPhotoResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
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
//                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                       //booking added add booking id in trancation table
                        walletModel.bookingId = it.substringBefore(" ")
                        walletViewModel.addMoney(walletModel, true, true)
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })

        bookingViewModel.bookingExtendResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                       //booking extended successfully
                        astrologerModel.walletBalance =
                            (astrologerModel.walletBalance!!.toInt() + amount.toInt())
                        astrologerProfileViewModel.updateAstrologerWalletBalance(
                            astrologerModel.uid!!,
                            astrologerModel.walletBalance!!
                        )
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        })

        //update astrologer balance
        astrologerProfileViewModel.userDataResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    if(isExtendMinute) {
                        setResult(
                            Activity.RESULT_OK,
                            Intent().putExtra(
                                Constants.INTENT_BOOKING_MODEL,
                                Gson().toJson(bookingModel)
                            )
                        )
                        onBackPressed()
                    } else {
                        redirectToPaymentSuccessScreen()
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

    /**
     * start payment
     */
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
            options.put("theme.color", ContextCompat.getColor(this, R.color.user_theme))
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

    /**
     * on Payment Success
     */
    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        try {
            if (razorpayPaymentID != null) {
                walletModel = WalletModel()
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
//                    walletModel.setCapturedGateway = true
                    walletViewModel.addMoney(walletModel, false)
                } else {
//                    walletModel.amount = "-$amount"
                    walletModel.trancationType = Constants.TRANSACTION_TYPE_DEBIT
                    walletViewModel.addMoney(walletModel, false, true)
                }
            }
        } catch (e: java.lang.Exception) {
            // exception
        }
    }

    /**
     * on Payment Error
     */
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
            }
        } catch (e: java.lang.Exception) {
            // exception
        }
    }
}