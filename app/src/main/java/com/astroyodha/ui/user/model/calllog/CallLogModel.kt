package com.astroyodha.ui.user.model.calllog

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class CallLogModel(
    var startTime: Date? = null,
    var id: String = "",
    var endTime: Date? = null,
    var userId: String = "",
    var userName: String = "",
    var userType: String = "",
    var extendCount: Int = 0,
    var extendMin: Int = 0,
    var type: String = "",
    var createdAt: Timestamp? = null,
) : Parcelable