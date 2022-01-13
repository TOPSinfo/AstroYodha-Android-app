package com.astroyodha.ui.user.authentication.model.chat

import com.google.firebase.Timestamp

data class MessagesModel(
    var senderId: String? = null,
    var receiverId: String? = null,
    var timeStamp: Timestamp? = null,
    var message: String? = null,
    var messageType: String? = null,
    var url: String? = null,
    var videoUrl: String? = null,
    var status: String? = null,
    var messageId: String? = null,
    var senderName: String? = null,
)
