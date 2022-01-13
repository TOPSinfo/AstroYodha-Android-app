package com.astroyodha.ui.user.authentication.model.cms

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.astroyodha.utils.Constants

object CMSList {

    /**
     * make array list of order from firestore snapshot
     */

    fun getCMSArrayList(
        querySnapshot: QuerySnapshot
    ): ArrayList<CMSModel> {
        val cmsArrayList = ArrayList<CMSModel>()
        for (doc in querySnapshot.documents) {
            val cmsModel = CMSModel()

            doc.get(Constants.FIELD_ID)?.let {
                cmsModel.id= it.toString()
            }
            doc.get(Constants.FIELD_TYPE)?.let {
                cmsModel.type = it.toString()
            }
            doc.get(Constants.FIELD_TITLE)?.let {
                cmsModel.title = it.toString()
            }
            doc.get(Constants.FIELD_CONTENT)?.let {
                cmsModel.content = it.toString()
            }
            cmsArrayList.add(cmsModel)
        }
        return cmsArrayList
    }

    fun getCMSModel(
        querySnapshot: QueryDocumentSnapshot
    ): CMSModel {

        val cmsModel = CMSModel()

        querySnapshot.get(Constants.FIELD_ID)?.let {
            cmsModel.id= it.toString()
        }
        querySnapshot.get(Constants.FIELD_TYPE)?.let {
            cmsModel.type = it.toString()
        }
        querySnapshot.get(Constants.FIELD_TITLE)?.let {
            cmsModel.title = it.toString()
        }
        querySnapshot.get(Constants.FIELD_CONTENT)?.let {
            cmsModel.content = it.toString()
        }


        return cmsModel
    }


    fun getCMSDetail(
        querySnapshot: DocumentSnapshot
    ): CMSModel {

        val cmsModel = CMSModel()

        querySnapshot.get(Constants.FIELD_ID)?.let {
            cmsModel.id= it.toString()
        }
        querySnapshot.get(Constants.FIELD_TYPE)?.let {
            cmsModel.type = it.toString()
        }
        querySnapshot.get(Constants.FIELD_TITLE)?.let {
            cmsModel.title = it.toString()
        }
        querySnapshot.get(Constants.FIELD_CONTENT)?.let {
            cmsModel.content = it.toString()
        }

        return cmsModel
    }
}