package com.astroyodha.ui.user.model.booking

import android.os.Parcelable
import com.astroyodha.utils.Constants
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class BookingModel(
    var astrologerID: String = "",//"Q2L2xS2usRT7oJCmqKLZZPnQ2eG2",  //astrologer id taken from firebase
    var astrologerName: String = "",
    var astrologerPerMinCharge: Int = 0,
    var date: String = "",
    var description: String = "",
    var startTime: Date? = null,
    var id: String = "",
    var month: String = "",
    var year: String = "",
    var endTime: Date? = null,
    var userId: String = "",
    var userName: String = "",
    var userBirthday: String = "",
    var userProfileImage: String = "",
    var status: String = Constants.APPROVE_STATUS,//PENDING_STATUS
    var notify: String = "",
    var notificationMin: Int = 0,
    var paymentStatus: String = Constants.RAZOR_PAY_STATUS_AUTHORIZED,
    var paymentType: String = Constants.PAYMENT_TYPE_WALLET,
    var transactionId: String = "",
    var amount: Int = 0,
    var createdAt: Timestamp? = null,
    var extendedTimeInMinute: Int = 0,
    var allowExtendTIme: String = Constants.EXTEND_STATUS_NO,
    var fullname: String = "",
    var birthPlace: String = "",
    var birthDate: String = "",
    var birthTime: String = "",
    var photo: String = "",
    var kundali: String = "",


) : Parcelable