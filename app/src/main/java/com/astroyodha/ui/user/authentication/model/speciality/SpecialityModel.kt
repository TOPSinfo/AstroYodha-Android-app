package com.astroyodha.ui.user.authentication.model.speciality

import com.google.firebase.Timestamp
import java.io.Serializable

data class SpecialityModel(
    var id: String = "",
    var speciality: String = "",
    var createdAt: Timestamp? = null,
):Serializable
