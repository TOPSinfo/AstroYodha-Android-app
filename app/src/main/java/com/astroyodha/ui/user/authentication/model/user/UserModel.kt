package com.astroyodha.ui.user.authentication.model.user

import com.astroyodha.utils.Constants
import com.google.firebase.Timestamp
import java.io.Serializable

data class UserModel(
    var uid: String? = "",
    var email: String? = "",
    var fullName: String? = "",
    var phone: String? = "",
    var profileImage: String? = "",
    var socialId: String? = "",
    var birthPlace: String? = "",
    var birthDate: String? = "",
    var birthTime: String? = "",
    var socialType: String? = "",
    var fcmToken: String? = "",
    var walletBalance: Int? = 0,
    var createdAt: Timestamp? = null,
    var isSelectedForCall: Boolean = false,
    var isOnline: Boolean = false,
    var type: String = Constants.USER_NORMAL
):Serializable
