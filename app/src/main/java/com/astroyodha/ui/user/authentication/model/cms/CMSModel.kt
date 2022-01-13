package com.astroyodha.ui.user.authentication.model.cms

import java.io.Serializable

data class CMSModel(
    var id: String = "",
    var title: String = "",
    var content: String = "",
    var type: String = "",
):Serializable
