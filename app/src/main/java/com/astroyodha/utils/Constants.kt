package com.astroyodha.utils

import androidx.lifecycle.MutableLiveData
import com.astroyodha.ui.astrologer.model.language.LanguageAndSpecialityModel
import com.astroyodha.ui.user.model.CallListModel

object Constants {

    const val BASE_URL = "http://app.topsdemo.co.in/webservices/dose_of_society/ws/"  //use this next time

    const val VALIDATION_ERROR = "Oops Something went wrong.Please try again later"
    const val MSG_NO_INTERNET_CONNECTION = "The internet connection appears to be offline"
    const val MSG_SOMETHING_WENT_WRONG = "Something went wrong"
    const val PASSWORD_PATTERN = "^.{8,15}$"

    const val VALIDATION_MOBILE_NUMBER = "Please add valid mobile number"
    const val VALIDATION_OTP = "Please enter pin number"
    const val VAL_OPTEXPIRED = "This code has expired. Please resend for a new code"
    const val VAL_OPTINVALID = "Invalid code. Please try again"

    const val MSG_UPDATE_SUCCESSFULL = "Profile updated successfully"
    const val MSG_BOOKING_UPDATE_SUCCESSFUL = "Booking updated successfully"
    const val MSG_BOOKING_DELETE_SUCCESSFUL = "Booking deleted successfully"
    const val MSG_TIMESLOT_UPDATE_SUCCESSFUL = "Timeslot updated successfully"
    const val MSG_CHARGES_UPDATE_SUCCESSFUL = "Charges updated successfully"
    const val MSG_MONEY_ADDED_SUCCESSFUL = "Money added successfully"
    const val MSG_RATING_SUCCESSFUL = "Rating added successfully"

    const val MSG_MOBILE_NUMBER_REGISTERD = "Mobile number is registered"
    const val MSG_MOBILE_NUMBER_NOT_REGISTERD = "Mobile number is not registered, Please Sign up."
    const val MSG_TIME_SLOT_DELETE_SUCCESSFULL = "Time slot deleted successfully"
    const val MSG_SOCIAL_MEDIA_ALREADY_REGISTER = "User is already registered using this social type."

    val notiCount: MutableLiveData<Int> = MutableLiveData()
//    const val razorpay_key = "rzp_test_TCxoTGO8D8aWmR"    //my account
    const val razorpay_key = "rzp_test_oOesxk9pgzGT9S"    // production account test key
//    const val razorpay_key = "rzp_live_694JtTuEBjC9LP"  //live

    const val PROFILE_IMAGE_PATH = "/images/users"

    /*******User type********/
    const val USER_ASTROLOGER = "astrologer"
    const val USER_NORMAL = "user"

    /******Social Login Type*******/
    const val SOCIAL_TYPE_GOOGLE="google"
    const val SOCIAL_TYPE_FACEBOOK="facebook"

    /******Policies type*******/
    const val TERM_AND_CONDITION_POLICY="terms"
    const val PRIVACY_POLICY="privacy policy"
    const val FAQ_POLICY="faq"

    /******Booking List type*******/
    const val BOOKING_UPCOMING="upcoming"
    const val BOOKING_ONGOING="ongoing"
    const val BOOKING_PAST="past"

    /**Transaction Type*******/
    const val TRANSACTION_TYPE_DEBIT="debit"
    const val TRANSACTION_TYPE_CREDIT="credit"

    /**Payment Type*******/
    const val PAYMENT_TYPE_RAZOR_PAY="paymentgateway"
    const val PAYMENT_TYPE_WALLET="wallet"
    const val PAYMENT_TYPE_REFUND="refund"
    /**Razor pay transaction status Type*******/
    const val RAZOR_PAY_STATUS_AUTHORIZED = "authorized"
    const val RAZOR_PAY_STATUS_CAPTURED = "captured"

    /**Notification Type*******/
    const val NOTIFICATION_REMINDER = "1"
    const val NOTIFICATION_REQUEST_ADDED_ACCEPTED = "2"

    /*******Table********/
    const val TABLE_CMS = "cms"
    const val TABLE_QUESTION = "questions"
    const val TABLE_USER = "user"
    const val TABLE_BOOKING = "bookinghistory"
    const val TABLE_TRANSACTION = "transactionhistory"
    const val TABLE_TIMESLOT = "timeslot"
    const val TABLE_PRICE = "price"
    const val TABLE_GROUPCALL = "groupcall"
    const val TABLE_MESSAGES = "messages"
    const val TABLE_MESSAGE_COLLECTION = "message"
    const val TABLE_RATING = "rating"
    const val TABLE_SPECIALITY = "speciality"
    const val TABLE_LANGUAGE = "language"
    const val TABLE_NOTIFICATION = "notification"

    const val FIELD_TYPE = "type"
    const val FIELD_CONTENT = "content"
    const val FIELD_ANSWER = "answer"

    const val FIELD_UID = "uid"
    const val FIELD_EMAIL = "email"
    const val FIELD_FULL_NAME = "fullname"
    const val FIELD_PHONE = "phone"
    const val FIELD_PRICE = "price"
    const val FIELD_PROFILE_IMAGE = "profileimage"
    const val FIELD_IS_ONLINE = "isOnline"
    const val FIELD_USER_TYPE = "usertype"
    const val FIELD_SPECIALITY = "speciality"
    const val FIELD_RATING = "rating"
    const val FIELD_SOCIAL_ID = "socialid"
    const val FIELD_BIRTH_DATE = "birthdate"
    const val FIELD_BIRTH_TIME = "birthtime"
    const val FIELD_BIRTH_PLACE = "birthplace"
    const val FIELD_SOCIAL_TYPE = "socialtype"
    const val FIELD_WALLET_BALANCE = "walletbalance"
    const val FIELD_LANGUAGES = "languages"
    const val FIELD_EXPERIENCE = "experience"
    const val FIELD_ABOUT = "aboutyou"
    const val FIELD_TOKEN = "token"


    const val FIELD_MESSAGE = "messagetext"
    const val FIELD_RECEIVER_ID = "receiverid"
    const val FIELD_SENDER_ID = "senderid"
    const val FIELD_TIMESTAMP = "timestamp"
    const val FIELD_MESSAGE_TYPE = "messagetype"
    const val FIELD_URL = "url"
    const val FIELD_VIDEO_URL = "video_url"
    const val FIELD_MESSAGE_STATUS = "status"

    const val FIELD_GROUP_CREATED_AT = "createdat"


    const val FIELD_BOOKING_ID = "bookingid"
    const val FIELD_ASTROLOGER_ID = "astrologerid"
    const val FIELD_ASTROLOGER_NAME = "astrologername"
    const val FIELD_ASTROLOGER_CHARGE = "astrologercharge"
    const val FIELD_TITLE = "title"
    const val FIELD_MONTH = "month"
    const val FIELD_YEAR = "year"
    const val FIELD_DESCRIPTION= "description"
    const val FIELD_DATE = "date"
    const val FIELD_START_TIME = "starttime"
    const val FIELD_END_TIME = "endtime"
    const val FIELD_STATUS = "status"
    const val FIELD_NOTIFY = "notify"
    const val FIELD_NOTIFICATION_MIN = "notificationmin"
    const val FIELD_USER_NAME = "username"
    const val FIELD_USER_BIRTH_DATE = "userbirthdate"
    const val FIELD_USER_PROFILE_IMAGE = "userprofileimage"
    //status
    const val PENDING_STATUS = "pending"
    const val APPROVE_STATUS = "approve"
    const val REJECT_STATUS = "reject"
    const val COMPLETED_STATUS = "completed"
    const val CANCEL_STATUS = "cancel"


    const val FIELD_TIME_SLOT_ID = "timeslotid"
    const val FIELD_START_DATE = "startdate"
    const val FIELD_END_DATE = "enddate"
    const val FIELD_REPEAT_DAYS = "repeatdays"

    const val FIELD_PRICE_ID = "priceid"
    const val FIELD_FIFTEEN_MIN_CHARGE = "fifteenmincharge"
    const val FIELD_THIRTY_MIN_CHARGE = "thirtymincharge"
    const val FIELD_FORTYFIVE_MIN_CHARGE = "fortyfivemincharge"
    const val FIELD_SIXTY_MIN_CHARGE = "sixtymincharge"



    const val FIELD_CALL_STATUS = "CallStatus"
    const val FIELD_HOST_ID = "HostId"
    const val FIELD_USERIDS = "userIds"
    const val FIELD_HOST_NAME = "HostName"

    const val FIELD_ID = "id"
    const val FIELD_TRANSACTION_ID = "transactionid"
    const val FIELD_AMOUNT = "amount"
    const val FIELD_TRANSACTION_TYPE = "transactiontype"
    const val FIELD_PAYMENT_TYPE = "paymenttype"
    const val FIELD_PAYMENT_STATUS = "paymentstatus"
    const val FIELD_REFUND = "refund"
    const val FIELD_SET_CAPTURED_GATEWAY = "setcapturedgateway"

    const val FIELD_MESSAGE_NOTIFICATION = "message"

    const val FIELD_RATING_ID = "ratingid"
    const val FIELD_FEEDBACK = "feedback"

    /*******Language list*/

    const val FIELD_LANGUAGE_ID = "id"
    const val FIELD_LANGUAGE_NAME = "name"

    /*******Pref Key********/
    const val PREF_FILE = "pref_astrology"
    const val PREF_FCM_TOKEN="fcmtoken"
    const val REMAINING_TIME = "remainingtime"


    const val ERROR_CODE_401 = 401
    var isTokenExpired = MutableLiveData<Boolean>()
    var strMessage = MutableLiveData<String>()


    /*******Intents Key********/
    const val INTENT_ISEDIT = "isEdit"
    const val INTENT_USER_ID = "user_id"
    const val INTENT_COUNTRY_CODE = "country_code"
    const val INTENT_MOBILE = "mobile"
    const val INTENT_ISLOGINWITH = "isLoginWith"
    const val INTENT_MODEL = "updatedmodel"
    const val INTENT_BOOKING_MODEL = "bookingmodel"
    const val INTENT_IS_FROM = "isFrom"
    const val INTENT_FNAME = "fname"
    const val INTENT_LNAME = "lname"
    const val INTENT_AMOUNT = "amount"
    const val INTENT_MINUTE = "minute"
    const val INTENT_IS_DIRECT_PAYMENT = "isdirectpayment"
    const val INTENT_IS_EXTEND_CALL = "isextendcall"
    const val INTENT_TRANSACTION = "transaction"
    const val INTENT_SHOW_TIMER = "showtimer"
    const val INTENT_ID = "id"
    const val INTENT_COUNTRY = "country"
    const val INTENT_TITLE = "title"
    const val INTENT_USER_DATA = "userdata"
    const val INTENT_NAME = "name"
    const val INTENT_EMAIL = "email"
    const val INTENT_PHONE_NUMBER = "phonenumber"
    const val INTENT_SOCIAL_ID = "socialid"
    const val INTENT_SOCIAL_TYPE = "socialtype"
    const val INTENT_TIME_SLOT_LIST="timeslotlist"
    const val INTENT_INDEX="index"
    const val INTENT_USER_TYPE="usertype"
    const val INTENT_BOOKING_ID = "bookingid"


    const val TYPE_MESSAGE = "TEXT"
    const val TYPE_IMAGE = "IMAGE"
    const val TYPE_VIDEO = "VIDEO"

    const val CHAT_IMAGE_PATH = "/images/chat"
    const val CHAT_VIDEO_PATH = "/videos/chat"

    const val TYPE_SEND = "SEND"
    const val TYPE_READ = "READ"
    const val TYPE_UPLOADING = "UPLOADING"
    const val TYPE_START_UPLOAD = "START_UPLOAD"

    var listOfActiveCall = ArrayList<CallListModel>()
    var listOfLanguages = ArrayList<LanguageAndSpecialityModel>()
    var listOfSpeciality = ArrayList<LanguageAndSpecialityModel>()

    var USER_NAME = ""
    var USER_PROFILE_IMAGE = ""

}