package com.astroyodha.ui.user.model.notification

import com.astroyodha.utils.Constants
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

object NotificationList {

    /**
     * make array list of order from firestore snapshot
     */
    fun getNotificationArrayList(
        querySnapshot: QuerySnapshot,
        userId: String
    ): ArrayList<NotificationModel> {
        val bookingArrayList = ArrayList<NotificationModel>()
        for (doc in querySnapshot.documents) {
            val notificationModel = NotificationModel()

            doc.get(Constants.FIELD_BOOKING_ID)?.let {
                notificationModel.bookingId = it.toString()
            }
            doc.get(Constants.FIELD_DESCRIPTION)?.let {
                notificationModel.description = it.toString()
            }
            doc.get(Constants.FIELD_MESSAGE_NOTIFICATION)?.let {
                notificationModel.message = it.toString()
            }
            doc.get(Constants.FIELD_TITLE)?.let {
                notificationModel.title = it.toString()
            }
            doc.get(Constants.FIELD_TYPE)?.let {
                notificationModel.type = it.toString()
            }
            doc.get(Constants.FIELD_UID)?.let {
                notificationModel.userId = it.toString()
            }
            doc.get(Constants.FIELD_GROUP_CREATED_AT)?.let {
                notificationModel.createdAt = it as Timestamp
            }
            doc.get(Constants.FIELD_USER_TYPE)?.let {
                notificationModel.userType = it.toString()
            }

            bookingArrayList.add(notificationModel)

//            if (doc.get(Constants.FIELD_UID) == userId) {
//                bookingArrayList.add(bookingModel)
//            }

        }
        return bookingArrayList
    }

    /**
     * set single model item
     */
    fun getNotificationModel(
        querySnapshot: QueryDocumentSnapshot
    ): NotificationModel {

        val notificationModel = NotificationModel()

        querySnapshot.get(Constants.FIELD_BOOKING_ID)?.let {
            notificationModel.bookingId = it.toString()
        }
        querySnapshot.get(Constants.FIELD_DESCRIPTION)?.let {
            notificationModel.description = it.toString()
        }
        querySnapshot.get(Constants.FIELD_MESSAGE_NOTIFICATION)?.let {
            notificationModel.message = it.toString()
        }
        querySnapshot.get(Constants.FIELD_TITLE)?.let {
            notificationModel.title = it.toString()
        }
        querySnapshot.get(Constants.FIELD_TYPE)?.let {
            notificationModel.type = it.toString()
        }
        querySnapshot.get(Constants.FIELD_UID)?.let {
            notificationModel.userId = it.toString()
        }
        querySnapshot.get(Constants.FIELD_GROUP_CREATED_AT)?.let {
            notificationModel.createdAt = it as Timestamp
        }
        querySnapshot.get(Constants.FIELD_USER_TYPE)?.let {
            notificationModel.userType = it.toString()
        }
        return notificationModel
    }

    /**
     * set single model item
     */
    fun getNotificationDetail(
        querySnapshot: DocumentSnapshot
    ): NotificationModel {

        val notificationModel = NotificationModel()

        querySnapshot.get(Constants.FIELD_BOOKING_ID)?.let {
            notificationModel.bookingId = it.toString()
        }
        querySnapshot.get(Constants.FIELD_DESCRIPTION)?.let {
            notificationModel.description = it.toString()
        }
        querySnapshot.get(Constants.FIELD_MESSAGE_NOTIFICATION)?.let {
            notificationModel.message = it.toString()
        }
        querySnapshot.get(Constants.FIELD_TITLE)?.let {
            notificationModel.title = it.toString()
        }
        querySnapshot.get(Constants.FIELD_TYPE)?.let {
            notificationModel.type = it.toString()
        }
        querySnapshot.get(Constants.FIELD_UID)?.let {
            notificationModel.userId = it.toString()
        }
        querySnapshot.get(Constants.FIELD_GROUP_CREATED_AT)?.let {
            notificationModel.createdAt = it as Timestamp
        }
        querySnapshot.get(Constants.FIELD_USER_TYPE)?.let {
            notificationModel.userType = it.toString()
        }
        return notificationModel
    }
}