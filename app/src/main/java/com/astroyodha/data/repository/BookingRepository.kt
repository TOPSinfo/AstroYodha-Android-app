package com.astroyodha.data.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.astroyodha.ui.user.model.booking.BookingModel
import com.astroyodha.utils.Constants
import java.util.*
import javax.inject.Inject

class BookingRepository @Inject constructor() {

    private var firestoreDB = FirebaseFirestore.getInstance()

    //booking
    fun getBookingAll(): CollectionReference {
        return firestoreDB.collection(Constants.TABLE_BOOKING)
    }

    fun getBookingAddRepository(): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_BOOKING).document()
    }

    fun getBookingUpdateRepository(
        user: BookingModel
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_BOOKING).document(user.id)
    }

    fun getAllBookingByUserRepository(
        userId: String,
    ): Query {
        return firestoreDB.collection(Constants.TABLE_BOOKING)
            .whereEqualTo(Constants.FIELD_UID, userId)
            .orderBy(Constants.FIELD_START_TIME, Query.Direction.DESCENDING)
    }

    fun getAllUserBookingRequestWithDate(
        userId: String,
        eventDate: String,
    ): Query {
        return firestoreDB.collection(Constants.TABLE_BOOKING)
            .whereEqualTo(Constants.FIELD_ASTROLOGER_ID, userId)
            .whereEqualTo(Constants.FIELD_DATE, eventDate)
            .orderBy(Constants.FIELD_START_TIME, Query.Direction.DESCENDING)
    }

    fun getPastBookingByUserRepository(
        userId: String,
    ): Query {
        return firestoreDB.collection(Constants.TABLE_BOOKING)
            .whereEqualTo(Constants.FIELD_UID, userId)
            .whereLessThan(Constants.FIELD_END_TIME, Date())
            .orderBy(Constants.FIELD_START_TIME, Query.Direction.DESCENDING)
    }

    fun getOnGoingBookingByUserRepository(
        userId: String,
    ): Query {
        //error multiple filter not supported in firestore
        /*
            All where filters with an inequality (notEqualTo, notIn, lessThan, lessThanOrEqualTo,
            greaterThan, or greaterThanOrEqualTo) must be on the same field. But you have filters
            on 'starttime' and 'endtime'
        */
        val mTime = Date()
        return firestoreDB.collection(Constants.TABLE_BOOKING)
            .whereEqualTo(Constants.FIELD_UID, userId)
            .whereNotEqualTo(Constants.FIELD_START_TIME, mTime)
            .whereLessThan(Constants.FIELD_END_TIME, mTime)
            .orderBy(Constants.FIELD_START_TIME, Query.Direction.DESCENDING)
    }

    fun getUpComingBookingByUserRepository(
        userId: String,
    ): Query {
        return firestoreDB.collection(Constants.TABLE_BOOKING)
            .whereEqualTo(Constants.FIELD_UID, userId)
            .whereGreaterThan(Constants.FIELD_START_TIME, Date())
            .orderBy(Constants.FIELD_START_TIME, Query.Direction.DESCENDING)
    }

    fun getBookingByAstrologerRepository(
        userId: String
    ): Query {
        return firestoreDB.collection(Constants.TABLE_BOOKING)
            .whereEqualTo(Constants.FIELD_ASTROLOGER_ID, userId)
    }

    fun getPendingBookingListOfAstrologerRepository(
        userId: String,
        pendingStatus: String,limit:Long
    ): Query {
        return firestoreDB.collection(Constants.TABLE_BOOKING)
            .whereEqualTo(Constants.FIELD_ASTROLOGER_ID, userId)
            .whereEqualTo(Constants.FIELD_STATUS,pendingStatus)
            .limit(limit)
            .orderBy(Constants.FIELD_START_TIME, Query.Direction.DESCENDING)
    }


    fun getAllCompletedBookingByAstrologer(
        astrologerId:String,
        status:String
    ): Query {
        return firestoreDB.collection(Constants.TABLE_BOOKING)
            .whereEqualTo(Constants.FIELD_ASTROLOGER_ID, astrologerId)
            .whereEqualTo(Constants.FIELD_STATUS, status)
    }

    fun getAllPendingBookingListOfAstrologerRepository(
        userId: String,status:String
    ): Query {
        return firestoreDB.collection(Constants.TABLE_BOOKING)
            .whereEqualTo(Constants.FIELD_ASTROLOGER_ID, userId)
            .whereEqualTo(Constants.FIELD_STATUS,status)
            .orderBy(Constants.FIELD_START_TIME, Query.Direction.DESCENDING)
    }

    fun getAllBookingByAstrlogerRepository(
        userId: String,
    ): Query {
        return firestoreDB.collection(Constants.TABLE_BOOKING)
            .whereEqualTo(Constants.FIELD_ASTROLOGER_ID, userId)
            .orderBy(Constants.FIELD_START_TIME, Query.Direction.DESCENDING)
    }

    fun getAstrologerBookingEventResponse(userId: String): Query {
        return firestoreDB.collection(Constants.TABLE_BOOKING)
            .whereEqualTo(Constants.FIELD_ASTROLOGER_ID, userId)
    }

    fun getBookingEventResponse(userId: String): Query {
        return firestoreDB.collection(Constants.TABLE_BOOKING)
            .whereEqualTo(Constants.FIELD_UID, userId)
    }


    fun getBookingDetail(
        bookingId:String
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_BOOKING).document(bookingId)
    }



}