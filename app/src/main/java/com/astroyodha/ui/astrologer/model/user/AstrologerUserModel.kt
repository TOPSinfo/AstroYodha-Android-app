package com.astroyodha.ui.astrologer.model.user

import android.os.Parcelable
import com.astroyodha.utils.Constants
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AstrologerUserModel(
    var uid: String? = "",
    var email: String? = "",
    var fullName: String? = "",
    var speciality: List<String>? = listOf("VYv29cpEIx5g9CKJ3XVh"),//static id of speciality from firebase database
    var languages: List<String>? = listOf("dOOKLo25RujzelsOqWi7"),//static id of language from firebase database
    var phone: String? = "",
    var profileImage: String? = "",
    var socialId: String? = "",
    var birthDate: String? = "",
    var socialType: String? = "",
    var walletBalance: Int? = 0,
    var createdAt: Timestamp? = null,
    var isSelectedForCall: Boolean = false,
    var isOnline: Boolean = false,
    var price: Int = 0,
    var rating: Float = 0f,
    var type: String = Constants.USER_NORMAL,
    var walletbalance: String = "0",
    var experience: Int = 0,
    var about: String = "",
    var fcmToken: String = ""
) : Parcelable