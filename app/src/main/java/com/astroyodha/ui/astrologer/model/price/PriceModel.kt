package com.astroyodha.ui.astrologer.model.price


import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PriceModel(
    var fifteenMin: String = "",
    var fortyFiveMin: String = "",
    var id: String = "",
    var sixtyMin: String = "",
    var thirtyMin: String = "",
    var userId: String = "",
    var createdAt: Timestamp? = null,
) : Parcelable