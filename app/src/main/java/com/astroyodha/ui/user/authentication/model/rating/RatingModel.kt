package com.astroyodha.ui.user.authentication.model.rating

import com.google.firebase.Timestamp
import java.io.Serializable

data class RatingModel(
    var id: String = "",
    var userId: String = "",
    var astrologerId: String = "",
    var userName: String = "",
    var rating: Float = 1f,
    var feedBack: String = "",
    var createdAt: Timestamp? = null,
):Serializable
