package com.astroyodha.ui.user.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CallListModel(
    var callStatus: String = "",
    var HostId: String = "",
    var userIds: ArrayList<String> = ArrayList(),
    var docId: String = "",
    var hostName: String = ""
) : Parcelable

