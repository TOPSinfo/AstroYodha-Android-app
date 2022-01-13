package com.astroyodha.data.repository

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.astroyodha.ui.user.model.wallet.WalletModel
import com.astroyodha.utils.Constants
import javax.inject.Inject

class WalletRepository @Inject constructor() {

    private var firestoreDB = FirebaseFirestore.getInstance()

    //booking
    fun getWalletAddRepository(
        userId: String
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(userId)
            .collection(Constants.TABLE_TRANSACTION).document()
    }

    fun getWalletUpdateRepository(
        user: WalletModel
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(user.userId).collection(Constants.TABLE_TRANSACTION).document(user.trancationId)
    }

    fun getAstrologerWalletAddRepository(
        userId: String,
        docId:String
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(userId)
            .collection(Constants.TABLE_TRANSACTION).document(docId)
    }

    fun getAstrologerWalletUpdateRepository(
        user: WalletModel
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(user.astrologerId).collection(Constants.TABLE_TRANSACTION).document(user.trancationId)
    }

    fun getAllWalletByUserRepository(
        userId: String,
    ): Query {
        return return firestoreDB.collection(Constants.TABLE_USER).document(userId)
            .collection(Constants.TABLE_TRANSACTION)
            .orderBy(Constants.FIELD_GROUP_CREATED_AT, Query.Direction.DESCENDING)
    }

}