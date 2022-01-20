package com.astroyodha.ui.user.model.wallet

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WalletModel(
    var id: String = "",
    var trancationId: String = "",
    var amount: Int = 0,
    var userId: String = "",
    var userName: String = "",
    var description: String = "",
    var astrologerId: String = "",
    var astrologerName: String = "",
    var paymentType: String = "",
    var trancationType: String = "",
    var bookingId: String = "",
    var isRefund: Boolean = false,
    var setCapturedGateway: Boolean = true,
    var createdAt: Timestamp? = null,
) : Parcelable