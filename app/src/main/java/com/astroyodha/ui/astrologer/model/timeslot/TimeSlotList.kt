package com.astroyodha.ui.astrologer.model.timeslot

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.astroyodha.utils.Constants

object TimeSlotList {

    /**
     * make array list of order from firestore snapshot
     */

    fun getTimeSlotArrayList(
        querySnapshot: QuerySnapshot,
        userId: String
    ): ArrayList<TimeSlotModel> {
        val bookingArrayList = ArrayList<TimeSlotModel>()
        for (doc in querySnapshot.documents) {
            val timeSlotModel = TimeSlotModel()

            doc.get(Constants.FIELD_TIME_SLOT_ID)?.let {
                timeSlotModel.id = it.toString()
            }
            doc.get(Constants.FIELD_START_DATE)?.let {
//                val tm = it as Timestamp
                timeSlotModel.startDate = it.toString()//.toDate()
            }
            doc.get(Constants.FIELD_END_DATE)?.let {
//                val tm = it as Timestamp
                timeSlotModel.endDate = it.toString()//.toDate()
            }
            doc.get(Constants.FIELD_REPEAT_DAYS)?.let {
                timeSlotModel.days = it as List<String>
            }
            doc.get(Constants.FIELD_START_TIME)?.let {
                timeSlotModel.fromTime = it.toString()
            }
            doc.get(Constants.FIELD_END_TIME)?.let {
                timeSlotModel.toTime = it.toString()
            }
            doc.get(Constants.FIELD_UID)?.let {
                timeSlotModel.userId = it.toString()
            }
            doc.get(Constants.FIELD_TYPE)?.let {
                timeSlotModel.type = it.toString()
            }
            if (doc.get(Constants.FIELD_UID) == userId) {
                bookingArrayList.add(timeSlotModel)
            }

        }
        return bookingArrayList
    }

    fun getTimeSlotModel(
        querySnapshot: QueryDocumentSnapshot
    ): TimeSlotModel {

        val timeSlotModel = TimeSlotModel()

        querySnapshot.get(Constants.FIELD_TIME_SLOT_ID)?.let {
            timeSlotModel.id = it.toString()
        }
        querySnapshot.get(Constants.FIELD_DATE)?.let {
            timeSlotModel.date = it.toString()
        }

        querySnapshot.get(Constants.FIELD_START_TIME)?.let {
            timeSlotModel.fromTime = it.toString()
        }

        querySnapshot.get(Constants.FIELD_END_TIME)?.let {
            timeSlotModel.toTime = it.toString()
        }

        querySnapshot.get(Constants.FIELD_UID)?.let {
            timeSlotModel.userId = it.toString()
        }

        return timeSlotModel
    }


    fun getTimeSlotDetail(
        querySnapshot: DocumentSnapshot
    ): TimeSlotModel {

        val timeSlotModel = TimeSlotModel()

        querySnapshot.get(Constants.FIELD_BOOKING_ID)?.let {
            timeSlotModel.id = it.toString()
        }
        querySnapshot.get(Constants.FIELD_DATE)?.let {
            timeSlotModel.date = it.toString()
        }

        querySnapshot.get(Constants.FIELD_START_TIME)?.let {
            timeSlotModel.fromTime = it.toString()
        }

        querySnapshot.get(Constants.FIELD_END_TIME)?.let {
            timeSlotModel.toTime = it.toString()
        }

        querySnapshot.get(Constants.FIELD_UID)?.let {
            timeSlotModel.userId = it.toString()
        }

        return timeSlotModel
    }
}