package com.astroyodha.ui.user.authentication.model.cms

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.astroyodha.utils.Constants

object FAQList {

    /**
     * make array list of order from firestore snapshot
     */

    fun getCMSArrayList(
        querySnapshot: QuerySnapshot
    ): ArrayList<FAQModel> {
        val cmsArrayList = ArrayList<FAQModel>()
        for (doc in querySnapshot.documents) {
            val cmsModel = FAQModel()

            doc.get(Constants.FIELD_ID)?.let {
                cmsModel.id= it.toString()
            }
            doc.get(Constants.FIELD_ANSWER)?.let {
                cmsModel.answer = it.toString()
            }
            doc.get(Constants.FIELD_TITLE)?.let {
                cmsModel.title = it.toString()
            }
            cmsArrayList.add(cmsModel)
        }
        return cmsArrayList
    }

    fun getCMSModel(
        querySnapshot: QueryDocumentSnapshot
    ): FAQModel {

        val cmsModel = FAQModel()

        querySnapshot.get(Constants.FIELD_ID)?.let {
            cmsModel.id= it.toString()
        }
        querySnapshot.get(Constants.FIELD_ANSWER)?.let {
            cmsModel.answer = it.toString()
        }
        querySnapshot.get(Constants.FIELD_TITLE)?.let {
            cmsModel.title = it.toString()
        }

        return cmsModel
    }


    fun getCMSDetail(
        querySnapshot: DocumentSnapshot
    ): FAQModel {

        val cmsModel = FAQModel()

        querySnapshot.get(Constants.FIELD_ID)?.let {
            cmsModel.id= it.toString()
        }
        querySnapshot.get(Constants.FIELD_ANSWER)?.let {
            cmsModel.answer = it.toString()
        }
        querySnapshot.get(Constants.FIELD_TITLE)?.let {
            cmsModel.title = it.toString()
        }

        return cmsModel
    }
}