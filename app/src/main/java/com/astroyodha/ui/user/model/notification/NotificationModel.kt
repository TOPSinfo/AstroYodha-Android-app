package com.astroyodha.ui.user.model.notification

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NotificationModel(
    var id: String = "",
    var bookingId: String = "",
    var description: String = "",
    var message: String = "",
    var title: String = "",
    var type: String = "",
    var userId: String = "",
    var userType: String = "",
    var createdAt: Timestamp? = null,
) : Parcelable