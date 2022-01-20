package com.astroyodha.ui.user.authentication.model.rating

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.astroyodha.utils.Constants

object RatingList {

    /**
     * make array list of order from firestore snapshot
     */
    fun getRatingArrayList(
        querySnapshot: QuerySnapshot,
        userId: String
    ): ArrayList<RatingModel> {
        val ratingArrayList = ArrayList<RatingModel>()
        for (doc in querySnapshot.documents) {
            val ratingModel = RatingModel()

            doc.get(Constants.FIELD_UID)?.let {
                ratingModel.userId = it.toString()
            }
            doc.get(Constants.FIELD_USER_NAME)?.let {
                ratingModel.userName = it.toString()
            }
            doc.get(Constants.FIELD_ASTROLOGER_ID)?.let {
                ratingModel.astrologerId = it.toString()
            }
            doc.get(Constants.FIELD_RATING)?.let {
                ratingModel.rating = it.toString().toFloat()
            }
            doc.get(Constants.FIELD_FEEDBACK)?.let {
                ratingModel.feedBack = it.toString()
            }
            doc.get(Constants.FIELD_GROUP_CREATED_AT)?.let {
                val tm = it as Timestamp
                ratingModel.createdAt = it
            }
            ratingArrayList.add(ratingModel)
        }
        return ratingArrayList
    }

    /**
     * set single model item
     */
    fun getRatingModel(
        querySnapshot: QueryDocumentSnapshot
    ): RatingModel {

        val ratingModel = RatingModel()

        querySnapshot.get(Constants.FIELD_UID)?.let {
            ratingModel.userId = it.toString()
        }
        querySnapshot.get(Constants.FIELD_USER_NAME)?.let {
            ratingModel.userName = it.toString()
        }
        querySnapshot.get(Constants.FIELD_ASTROLOGER_ID)?.let {
            ratingModel.astrologerId = it.toString()
        }
        querySnapshot.get(Constants.FIELD_RATING)?.let {
            ratingModel.rating = it.toString().toFloat()
        }
        querySnapshot.get(Constants.FIELD_FEEDBACK)?.let {
            ratingModel.feedBack = it.toString()
        }
        querySnapshot.get(Constants.FIELD_GROUP_CREATED_AT)?.let {
            val tm = it as Timestamp
            ratingModel.createdAt = it
        }

        return ratingModel
    }

    /**
     * set single model item
     */
    fun getRatingDetail(
        querySnapshot: DocumentSnapshot
    ): RatingModel {

        val ratingModel = RatingModel()

        querySnapshot.get(Constants.FIELD_UID)?.let {
            ratingModel.userId = it.toString()
        }
        querySnapshot.get(Constants.FIELD_USER_NAME)?.let {
            ratingModel.userName = it.toString()
        }
        querySnapshot.get(Constants.FIELD_ASTROLOGER_ID)?.let {
            ratingModel.astrologerId = it.toString()
        }
        querySnapshot.get(Constants.FIELD_RATING)?.let {
            ratingModel.rating = it.toString().toFloat()
        }
        querySnapshot.get(Constants.FIELD_FEEDBACK)?.let {
            ratingModel.feedBack = it.toString()
        }
        querySnapshot.get(Constants.FIELD_GROUP_CREATED_AT)?.let {
            val tm = it as Timestamp
            ratingModel.createdAt = it
        }

        return ratingModel
    }
}