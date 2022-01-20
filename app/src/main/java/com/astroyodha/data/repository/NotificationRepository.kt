package com.astroyodha.data.repository

import com.astroyodha.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject

class NotificationRepository @Inject constructor() {

    private var firestoreDB = FirebaseFirestore.getInstance()

    //booking
    fun getAllWalletByUserRepository(
        userId: String,
    ): Query {
        return return firestoreDB.collection(Constants.TABLE_USER).document(userId)
            .collection(Constants.TABLE_NOTIFICATION)
            .orderBy(Constants.FIELD_GROUP_CREATED_AT, Query.Direction.DESCENDING)
    }

}