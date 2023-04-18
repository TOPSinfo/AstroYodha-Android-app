package com.astroyodha.data.repository

import com.astroyodha.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject

class CMSRepository @Inject constructor() {

    private var firestoreDB = FirebaseFirestore.getInstance()

    fun getPolicy(
        type: String
    ): Query {
        return firestoreDB.collection(Constants.TABLE_CMS)
            .whereEqualTo(Constants.FIELD_TYPE, type)
    }

    fun getFAQ(
        id: String
    ): Query {
        return firestoreDB.collection(Constants.TABLE_CMS)
            .document(id)
            .collection(Constants.TABLE_QUESTION)
    }

}