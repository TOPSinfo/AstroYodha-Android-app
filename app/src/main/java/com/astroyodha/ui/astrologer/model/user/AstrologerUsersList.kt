package com.astroyodha.ui.astrologer.model.user

import com.astroyodha.ui.user.authentication.model.chat.MessagesModel
import com.astroyodha.ui.user.authentication.model.user.UserModel
import com.astroyodha.utils.Constants
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

object AstrologerUsersList {

    /**
     * make array list of order from firestore snapshot
     */
    fun getUserArrayList(
        querySnapshot: QuerySnapshot,
        userId: String
    ): ArrayList<AstrologerUserModel> {
        val userArrayList = ArrayList<AstrologerUserModel>()
        for (doc in querySnapshot.documents) {
            val astrologerUserModel = AstrologerUserModel()

            doc.get(Constants.FIELD_UID)?.let {
                astrologerUserModel.uid = it.toString()
            }
            doc.get(Constants.FIELD_EMAIL)?.let {
                astrologerUserModel.email = it.toString()
            }
            doc.get(Constants.FIELD_SPECIALITY)?.let {
                astrologerUserModel.speciality = it as List<String>
            }
            doc.get(Constants.FIELD_RATING)?.let {
                astrologerUserModel.rating = it.toString().toFloat()
            }
            doc.get(Constants.FIELD_FULL_NAME)?.let {
                astrologerUserModel.fullName = it.toString()
            }
            doc.get(Constants.FIELD_PHONE)?.let {
                astrologerUserModel.phone = it.toString()
            }
            doc.get(Constants.FIELD_PRICE)?.let {
                astrologerUserModel.price = it.toString().toInt()
            }
            doc.get(Constants.FIELD_PROFILE_IMAGE)?.let {
                astrologerUserModel.profileImage = it.toString()
            }
            doc.get(Constants.FIELD_USER_TYPE)?.let {
                astrologerUserModel.type = it.toString()
            }
            doc.get(Constants.FIELD_PRICE)?.let {
                astrologerUserModel.price = it.toString().toInt()
            }
            doc.get(Constants.FIELD_WALLET_BALANCE)?.let {
                astrologerUserModel.walletBalance = it.toString().toInt()
            }
            doc.get(Constants.FIELD_LANGUAGES)?.let {
                astrologerUserModel.languages = it as List<String>
            }
            doc.get(Constants.FIELD_EXPERIENCE)?.let {
                astrologerUserModel.experience = it.toString().toInt()
            }
            doc.get(Constants.FIELD_ABOUT)?.let {
                astrologerUserModel.about = it.toString()
            }

            doc.get(Constants.FIELD_BIRTH_DATE)?.let {
                astrologerUserModel.birthDate = it.toString()
            }

            doc.get(Constants.FIELD_TOKEN)?.let {
                astrologerUserModel.fcmToken = it.toString()
            }
            if (doc.get(Constants.FIELD_UID) != userId) {
                userArrayList.add(astrologerUserModel)
            }

        }
        return userArrayList
    }

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
    fun getAstrologerUserModel(
        querySnapshot: QueryDocumentSnapshot
    ): AstrologerUserModel {

        val messagesModel = AstrologerUserModel()

        querySnapshot.get(Constants.FIELD_UID)?.let {
            messagesModel.uid = it.toString()
        }
        querySnapshot.get(Constants.FIELD_EMAIL)?.let {
            messagesModel.email = it.toString()
        }
        querySnapshot.get(Constants.FIELD_FULL_NAME)?.let {
            messagesModel.fullName = it.toString()
        }
        querySnapshot.get(Constants.FIELD_SPECIALITY)?.let {
            messagesModel.speciality = it as List<String>
        }
        querySnapshot.get(Constants.FIELD_RATING)?.let {
            messagesModel.rating = it.toString().toFloat()
        }
        querySnapshot.get(Constants.FIELD_PHONE)?.let {
            messagesModel.phone = it.toString()
        }
        querySnapshot.get(Constants.FIELD_PRICE)?.let {
            messagesModel.price = it.toString().toInt()
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

        return messagesModel
    }


    fun getUserDetail(
        querySnapshot: DocumentSnapshot
    ): AstrologerUserModel {

        val messagesModel = AstrologerUserModel()

        querySnapshot.get(Constants.FIELD_UID)?.let {
            messagesModel.uid = it.toString()
        }
        querySnapshot.get(Constants.FIELD_EMAIL)?.let {
            messagesModel.email = it.toString()
        }
        querySnapshot.get(Constants.FIELD_FULL_NAME)?.let {
            messagesModel.fullName = it.toString()
        }
        querySnapshot.get(Constants.FIELD_SPECIALITY)?.let {
            messagesModel.speciality = it as List<String>
        }
        querySnapshot.get(Constants.FIELD_RATING)?.let {
            messagesModel.rating = it.toString().toFloat()
        }
        querySnapshot.get(Constants.FIELD_PHONE)?.let {
            messagesModel.phone = it.toString()
        }
        querySnapshot.get(Constants.FIELD_PRICE)?.let {
            messagesModel.price = it.toString().toInt()
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
        querySnapshot.get(Constants.FIELD_LANGUAGES)?.let {
            messagesModel.languages = it as List<String>
        }
        querySnapshot.get(Constants.FIELD_EXPERIENCE)?.let {
            messagesModel.experience = it.toString().toInt()
        }
        querySnapshot.get(Constants.FIELD_WALLET_BALANCE)?.let {
            messagesModel.walletBalance = it.toString().toInt()
        }
        querySnapshot.get(Constants.FIELD_ABOUT)?.let {
            messagesModel.about = it.toString()
        }
        querySnapshot.get(Constants.FIELD_BIRTH_DATE)?.let {
            messagesModel.birthDate = it.toString()
        }

        querySnapshot.get(Constants.FIELD_TOKEN)?.let {
            messagesModel.fcmToken = it.toString()
        }

        return messagesModel
    }

    fun getNormalUserDetail(
        querySnapshot: DocumentSnapshot
    ): UserModel {

        val user = UserModel()

        querySnapshot.get(Constants.FIELD_UID)?.let {
            user.uid = it.toString()
        }
        querySnapshot.get(Constants.FIELD_EMAIL)?.let {
            user.email = it.toString()
        }
        querySnapshot.get(Constants.FIELD_FULL_NAME)?.let {
            user.fullName = it.toString()
        }
        querySnapshot.get(Constants.FIELD_PHONE)?.let {
            user.phone = it.toString()
        }

        querySnapshot.get(Constants.FIELD_PROFILE_IMAGE)?.let {
            user.profileImage = it.toString()
        }
        querySnapshot.get(Constants.FIELD_IS_ONLINE)?.let {
            user.isOnline = it as Boolean
        }
        querySnapshot.get(Constants.FIELD_GROUP_CREATED_AT)?.let {
            user.createdAt = it as Timestamp
        }
        querySnapshot.get(Constants.FIELD_USER_TYPE)?.let {
            user.type = it.toString()
        }
        querySnapshot.get(Constants.FIELD_SOCIAL_ID)?.let {
            user.socialId = it.toString()
        }
        querySnapshot.get(Constants.FIELD_BIRTH_DATE)?.let {
            user.birthDate = it.toString()
        }
        querySnapshot.get(Constants.FIELD_BIRTH_TIME)?.let {
            user.birthTime = it.toString()
        }
        querySnapshot.get(Constants.FIELD_BIRTH_PLACE)?.let {
            user.birthPlace = it.toString()
        }
        querySnapshot.get(Constants.FIELD_SOCIAL_TYPE)?.let {
            user.socialType = it.toString()
        }
        querySnapshot.get(Constants.FIELD_WALLET_BALANCE)?.let {
            user.walletBalance = it.toString().toInt()
        }
        querySnapshot.get(Constants.FIELD_TOKEN)?.let {
            user.fcmToken = it.toString()
        }

        return user
    }
}