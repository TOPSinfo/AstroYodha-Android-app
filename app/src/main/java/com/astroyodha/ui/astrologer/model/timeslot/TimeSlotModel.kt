package com.astroyodha.ui.astrologer.model.timeslot


import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TimeSlotModel(
    var date: String = "",
    var fromTime: String = "",
    var startDate: String = "",
    var endDate: String = "",
    var days: List<String>? = listOf(),
    var id: String = "",
    var toTime: String = "",
    var userId: String = "",
    var type: String = "",
    var createdAt: Timestamp? = null,
) : Parcelable