package com.astroyodha.ui.user.authentication.model.speciality

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.astroyodha.utils.Constants

object SpecialityList {

    /**
     * make array list of order from firestore snapshot
     */

    fun getSpecialityArrayList(
        querySnapshot: QuerySnapshot,
        userId: String
    ): ArrayList<SpecialityModel> {
        val ratingArrayList = ArrayList<SpecialityModel>()
        for (doc in querySnapshot.documents) {
            val specialityModel = SpecialityModel()

            doc.get(Constants.FIELD_ID)?.let {
                specialityModel.id= it.toString()
            }
            doc.get(Constants.FIELD_SPECIALITY)?.let {
                specialityModel.speciality = it.toString()
            }
            doc.get(Constants.FIELD_GROUP_CREATED_AT)?.let {
                val tm = it as Timestamp
                specialityModel.createdAt = it
            }
            ratingArrayList.add(specialityModel)
        }
        return ratingArrayList
    }

    fun getSpecialityModel(
        querySnapshot: QueryDocumentSnapshot
    ): SpecialityModel {

        val specialityModel = SpecialityModel()

        querySnapshot.get(Constants.FIELD_ID)?.let {
            specialityModel.id= it.toString()
        }
        querySnapshot.get(Constants.FIELD_SPECIALITY)?.let {
            specialityModel.speciality = it.toString()
        }
        querySnapshot.get(Constants.FIELD_GROUP_CREATED_AT)?.let {
            val tm = it as Timestamp
            specialityModel.createdAt = it
        }

        return specialityModel
    }


    fun getSpecialityDetail(
        querySnapshot: DocumentSnapshot
    ): SpecialityModel {

        val specialityModel = SpecialityModel()

        querySnapshot.get(Constants.FIELD_ID)?.let {
            specialityModel.id= it.toString()
        }
        querySnapshot.get(Constants.FIELD_SPECIALITY)?.let {
            specialityModel.speciality = it.toString()
        }
        querySnapshot.get(Constants.FIELD_GROUP_CREATED_AT)?.let {
            val tm = it as Timestamp
            specialityModel.createdAt = it
        }

        return specialityModel
    }
}