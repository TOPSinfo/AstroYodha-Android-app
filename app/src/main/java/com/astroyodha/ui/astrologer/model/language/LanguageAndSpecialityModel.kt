package com.astroyodha.ui.astrologer.model.language

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LanguageAndSpecialityModel(
    var id: String = "",
    var language: String = "",
):Parcelable
