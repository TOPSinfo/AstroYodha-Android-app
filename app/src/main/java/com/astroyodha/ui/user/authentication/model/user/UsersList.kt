package com.astroyodha.ui.user.authentication.model.user

import com.astroyodha.ui.user.authentication.model.chat.MessagesModel
import com.astroyodha.utils.Constants
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot

object UsersList {

    /**
     * set single model item
     */
    fun getMessagesModel(
        querySnapshot: QueryDocumentSnapshot
    ): MessagesModel {

        val messagesModel = MessagesModel()

        messagesModel.messageId = querySnapshot.id

        querySnapshot.get(Constants.FIELD_MESSAGE)?.let {
            messagesModel.message = it.toString()
        }
        querySnapshot.get(Constants.FIELD_SENDER_ID)?.let {
            messagesModel.senderId = it.toString()
        }
        querySnapshot.get(Constants.FIELD_RECEIVER_ID)?.let {
            messagesModel.receiverId = it.toString()
        }
        querySnapshot.get(Constants.FIELD_TIMESTAMP)?.let {
            messagesModel.timeStamp = it as Timestamp
        }

        querySnapshot.get(Constants.FIELD_MESSAGE_TYPE)?.let {
            messagesModel.messageType = it.toString()
        }

        querySnapshot.get(Constants.FIELD_URL)?.let {
            messagesModel.url = it.toString()
        }

        querySnapshot.get(Constants.FIELD_VIDEO_URL)?.let {
            messagesModel.videoUrl = it.toString()
        }
        querySnapshot.get(Constants.FIELD_MESSAGE_STATUS)?.let {
            messagesModel.status = it.toString()
        }

        return messagesModel
    }

    /**
     * set single model item
     */
    fun getUserDetail(
        querySnapshot: DocumentSnapshot
    ): UserModel {

        val messagesModel = UserModel()

        querySnapshot.get(Constants.FIELD_UID)?.let {
            messagesModel.uid = it.toString()
        }
        querySnapshot.get(Constants.FIELD_EMAIL)?.let {
            messagesModel.email = it.toString()
        }
        querySnapshot.get(Constants.FIELD_FULL_NAME)?.let {
            messagesModel.fullName = it.toString()
        }
        querySnapshot.get(Constants.FIELD_PHONE)?.let {
            messagesModel.phone = it.toString()
        }

        querySnapshot.get(Constants.FIELD_PROFILE_IMAGE)?.let {
            messagesModel.profileImage = it.toString()
        }
        querySnapshot.get(Constants.FIELD_IS_ONLINE)?.let {
            messagesModel.isOnline = it as Boolean
        }
        querySnapshot.get(Constants.FIELD_GROUP_CREATED_AT)?.let {
            messagesModel.createdAt = it as Timestamp
        }
        querySnapshot.get(Constants.FIELD_USER_TYPE)?.let {
            messagesModel.type = it.toString()
        }
        querySnapshot.get(Constants.FIELD_SOCIAL_ID)?.let {
            messagesModel.socialId = it.toString()
        }
        querySnapshot.get(Constants.FIELD_BIRTH_DATE)?.let {
            messagesModel.birthDate = it.toString()
        }
        querySnapshot.get(Constants.FIELD_BIRTH_TIME)?.let {
            messagesModel.birthTime = it.toString()
        }
        querySnapshot.get(Constants.FIELD_BIRTH_PLACE)?.let {
            messagesModel.birthPlace = it.toString()
        }
        querySnapshot.get(Constants.FIELD_SOCIAL_TYPE)?.let {
            messagesModel.socialType = it.toString()
        }
        querySnapshot.get(Constants.FIELD_WALLET_BALANCE)?.let {
            messagesModel.walletBalance = it.toString().toInt()
        }
        querySnapshot.get(Constants.FIELD_TOKEN)?.let {
            messagesModel.fcmToken = it.toString()
        }

        return messagesModel
    }
}