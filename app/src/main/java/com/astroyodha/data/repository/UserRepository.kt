package com.astroyodha.data.repository

import android.content.Context
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.astroyodha.R
import com.astroyodha.ui.astrologer.model.price.PriceModel
import com.astroyodha.ui.astrologer.model.timeslot.TimeSlotModel
import com.astroyodha.ui.user.model.booking.BookingModel
import com.astroyodha.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class UserRepository @Inject constructor(
    @ApplicationContext private val mContext: Context
) {

    private var firestoreDB = FirebaseFirestore.getInstance()

    //user
    fun getUserProfileRepository(userId: String): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(userId)
    }

    fun getAllAstrologerUser(
    ): Query {
        return firestoreDB.collection(Constants.TABLE_USER)
            .whereEqualTo(Constants.FIELD_USER_TYPE, Constants.USER_ASTROLOGER)
    }

    fun getAllAstrologerExperienceWise(
        sortBy: String,
        speciality: List<String>,
        isAscending: Boolean
    ): Query {
        return if (isAscending) {
            if (speciality.isNotEmpty()) {
                firestoreDB.collection(Constants.TABLE_USER)
                    .whereEqualTo(Constants.FIELD_USER_TYPE, Constants.USER_ASTROLOGER)
                    .whereArrayContainsAny(Constants.FIELD_SPECIALITY, speciality)
                    .orderBy(Constants.FIELD_EXPERIENCE, Query.Direction.ASCENDING)
            } else {
                firestoreDB.collection(Constants.TABLE_USER)
                    .whereEqualTo(Constants.FIELD_USER_TYPE, Constants.USER_ASTROLOGER)
                    .orderBy(Constants.FIELD_EXPERIENCE, Query.Direction.ASCENDING)
            }
        } else {
            if (speciality.isNotEmpty()) {
                firestoreDB.collection(Constants.TABLE_USER)
                    .whereEqualTo(Constants.FIELD_USER_TYPE, Constants.USER_ASTROLOGER)
                    .whereArrayContainsAny(Constants.FIELD_SPECIALITY, speciality)
                    .orderBy(Constants.FIELD_EXPERIENCE, Query.Direction.DESCENDING)
            } else {
                firestoreDB.collection(Constants.TABLE_USER)
                    .whereEqualTo(Constants.FIELD_USER_TYPE, Constants.USER_ASTROLOGER)
                    .orderBy(Constants.FIELD_EXPERIENCE, Query.Direction.DESCENDING)
            }
        }
    }

    fun getAllAstrologerPriceWise(
        sortBy: String,
        speciality: List<String>,
        isAscending: Boolean
    ): Query {
        return if (isAscending) {
            if (speciality.isNotEmpty()) {
                firestoreDB.collection(Constants.TABLE_USER)
                    .whereEqualTo(Constants.FIELD_USER_TYPE, Constants.USER_ASTROLOGER)
                    .whereArrayContainsAny(Constants.FIELD_SPECIALITY, speciality)
                    .orderBy(Constants.FIELD_PRICE, Query.Direction.ASCENDING)
            } else {
                firestoreDB.collection(Constants.TABLE_USER)
                    .whereEqualTo(Constants.FIELD_USER_TYPE, Constants.USER_ASTROLOGER)
                    .orderBy(Constants.FIELD_PRICE, Query.Direction.ASCENDING)
            }
        } else {
            if (speciality.isNotEmpty()) {
                firestoreDB.collection(Constants.TABLE_USER)
                    .whereEqualTo(Constants.FIELD_USER_TYPE, Constants.USER_ASTROLOGER)
                    .orderBy(Constants.FIELD_PRICE, Query.Direction.DESCENDING)
                    .whereArrayContainsAny(Constants.FIELD_SPECIALITY, speciality)
            } else {
                firestoreDB.collection(Constants.TABLE_USER)
                    .whereEqualTo(Constants.FIELD_USER_TYPE, Constants.USER_ASTROLOGER)
                    .orderBy(Constants.FIELD_PRICE, Query.Direction.DESCENDING)
            }

        }
    }

    fun addRating(userId: String): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_USER)
            .document(userId)
            .collection(Constants.TABLE_RATING)
            .document()
    }

    fun getRating(userId: String): Query {
        return firestoreDB.collection(Constants.TABLE_USER)
            .document(userId)
            .collection(Constants.TABLE_RATING)
            .orderBy(Constants.FIELD_GROUP_CREATED_AT, Query.Direction.DESCENDING)
    }

    //booking
    fun getBookingAddRepository(
        userId: String
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(userId)
            .collection(Constants.TABLE_BOOKING)
            .document()
    }

    fun getBookingUpdateRepository(
        user: BookingModel
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(user.userId)
            .collection(Constants.TABLE_BOOKING)
            .document(user.id)
    }

    //timeslot
    fun getTimeSlotAddRepository(
        timeSloteModel: TimeSlotModel
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(timeSloteModel.userId)
            .collection(Constants.TABLE_TIMESLOT)
            .document()
    }

    fun getTimeSlotUpdateRepository(
        timeSloteModel: TimeSlotModel
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(timeSloteModel.userId)
            .collection(Constants.TABLE_TIMESLOT)
            .document(timeSloteModel.id)

    }

    fun getAllTimeSlotRepository(
        userId: String
    ): CollectionReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(userId)
            .collection(Constants.TABLE_TIMESLOT)
//            .whereGreaterThan(Constants.FIELD_START_TIME, Date())

    }

    fun getCustomTimeSlotRepository(
        userId: String,
        eventDate: String
    ): Query {
        return firestoreDB.collection(Constants.TABLE_USER).document(userId)
            .collection(Constants.TABLE_TIMESLOT)
            .whereEqualTo(Constants.FIELD_START_DATE, eventDate)
            .whereEqualTo(Constants.FIELD_TYPE, mContext.getString(R.string.custom))
    }

    fun getWeeklyTimeSlotRepository(
        userId: String,
        eventDate: String
    ): Query {
        return firestoreDB.collection(Constants.TABLE_USER).document(userId)
            .collection(Constants.TABLE_TIMESLOT)
            .whereEqualTo(Constants.FIELD_TYPE, mContext.getString(R.string.weekly))
    }

    fun getRepeatTimeSlotRepository(
        userId: String,
        eventDate: String
    ): Query {
        return firestoreDB.collection(Constants.TABLE_USER).document(userId)
            .collection(Constants.TABLE_TIMESLOT)
            .whereEqualTo(Constants.FIELD_TYPE, mContext.getString(R.string.repeat))
    }

    //price
    fun getPriceAddRepository(
        userId: String
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(userId)
            .collection(Constants.TABLE_PRICE)
            .document()
    }

    fun getPriceUpdateRepository(
        user: PriceModel
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(user.userId)
            .collection(Constants.TABLE_PRICE)
            .document(user.id)
    }

    fun getPriceRepository(
        user: PriceModel
    ): CollectionReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(user.userId)
            .collection(Constants.TABLE_PRICE)
    }

    fun getUserCollection(): CollectionReference {
        return firestoreDB.collection(Constants.TABLE_USER)
    }

    fun getSpeciality(userId: String): CollectionReference {
        return firestoreDB.collection(Constants.TABLE_SPECIALITY)
    }

    fun getLanguageRepository(): CollectionReference {
        return firestoreDB.collection(Constants.TABLE_LANGUAGE)
    }
}