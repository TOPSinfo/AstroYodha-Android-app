package com.astroyodha.data.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.astroyodha.utils.Constants
import javax.inject.Inject

class ChatRepository @Inject constructor() {

    var firestoreDB = FirebaseFirestore.getInstance()

    fun getChatMessageRepository(docId: String): Query {
        return firestoreDB.collection(Constants.TABLE_MESSAGES)
            .document(docId)
            .collection(Constants.TABLE_MESSAGE_COLLECTION)
            .orderBy(Constants.FIELD_TIMESTAMP, Query.Direction.ASCENDING)
    }


    fun getUserList(): CollectionReference {
        return firestoreDB.collection(Constants.TABLE_USER)
    }

    fun getSendMessagePath(docId: String): CollectionReference {
        return firestoreDB.collection(Constants.TABLE_MESSAGES).document(docId)
            .collection(Constants.TABLE_MESSAGE_COLLECTION)
    }

    fun getMessageDetailData(messageDocId: String, messageId: String): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_MESSAGES).document(messageDocId)
            .collection(Constants.TABLE_MESSAGE_COLLECTION).document(messageId)
    }

    fun getMessageDocumentId(messageDocId: String): String {
        return firestoreDB.collection(Constants.TABLE_MESSAGES).document(messageDocId)
            .collection(Constants.TABLE_MESSAGE_COLLECTION).document().id
    }

}