package com.astroyodha.ui.user.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.astroyodha.data.repository.BookingRepository
import com.astroyodha.network.NetworkHelper
import com.astroyodha.network.Resource
import com.astroyodha.ui.user.model.booking.BookingList
import com.astroyodha.ui.user.model.booking.BookingModel
import com.astroyodha.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {

    private val _bookingAddUpdateResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val bookingAddUpdateResponse: LiveData<Resource<String>> get() = _bookingAddUpdateResponse

    private val _bookingDeleteResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val bookingDeleteResponse: LiveData<Resource<String>> get() = _bookingDeleteResponse

    var bookingList: MutableLiveData<ArrayList<BookingModel>> = MutableLiveData()

    private val _getBookingListDataResponse: MutableLiveData<Resource<ArrayList<BookingModel>>> =
        MutableLiveData()
    val getBookingListDataResponse: LiveData<Resource<ArrayList<BookingModel>>> get() = _getBookingListDataResponse

    private val _getBookingDetailResponse: MutableLiveData<Resource<BookingModel>> =
        MutableLiveData()
    val getBookingDetailResponse: LiveData<Resource<BookingModel>> get() = _getBookingDetailResponse

    /**
     * uploading profile picture to firebase storage
     */
    fun addUpdateBookingData(user: BookingModel, isForUpdate: Boolean) {

        _bookingAddUpdateResponse.value = Resource.loading(null)
        if (networkHelper.isNetworkConnected()) {
            if (isForUpdate) {
                updateBookingData(user)
            } else {
                addBookingData(user)
            }
        } else {
            _bookingAddUpdateResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }

    }

    /**
     * Adding booking info in firebase
     */
    fun addBookingData(user: BookingModel) {
        _bookingAddUpdateResponse.value = Resource.loading(null)
        val ref = bookingRepository.getBookingAddRepository()
        user.id = ref.id
        user.createdAt = Timestamp.now()

        val data = hashMapOf(
            Constants.FIELD_BOOKING_ID to user.id,
            Constants.FIELD_ASTROLOGER_ID to user.astrologerID,
            Constants.FIELD_ASTROLOGER_NAME to user.astrologerName,
            Constants.FIELD_ASTROLOGER_CHARGE to user.astrologerPerMinCharge,
            Constants.FIELD_DESCRIPTION to user.description,
            Constants.FIELD_DATE to user.date,
            Constants.FIELD_MONTH to user.month,
            Constants.FIELD_YEAR to user.year,
            Constants.FIELD_START_TIME to user.startTime,
            Constants.FIELD_END_TIME to user.endTime,
            Constants.FIELD_UID to user.userId,
            Constants.FIELD_USER_NAME to user.userName,
            Constants.FIELD_USER_BIRTH_DATE to user.userBirthday,
            Constants.FIELD_USER_PROFILE_IMAGE to user.userProfileImage,
            Constants.FIELD_STATUS to user.status,
            Constants.FIELD_NOTIFY to user.notify,
            Constants.FIELD_NOTIFICATION_MIN to user.notificationMin,
            Constants.FIELD_TRANSACTION_ID to user.transactionId,
            Constants.FIELD_PAYMENT_STATUS to user.paymentStatus,
            Constants.FIELD_PAYMENT_TYPE to user.paymentType,
            Constants.FIELD_AMOUNT to user.amount,
            Constants.FIELD_GROUP_CREATED_AT to user.createdAt,
        )

        ref.set(data)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    _bookingAddUpdateResponse.postValue(
                        Resource.success(
                            "${user.id} ${Constants.MSG_BOOKING_UPDATE_SUCCESSFUL}",
                        )
                    )
                }

            }.addOnFailureListener {
                _bookingAddUpdateResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }

    /**
     * Updating booking info in firebase
     */
    fun updateBookingData(user: BookingModel) {

        _bookingAddUpdateResponse.value = Resource.loading(null)
        user.createdAt = Timestamp.now()

        var data1 = HashMap<String, Any>()
        user.description.let { data1.put(Constants.FIELD_DESCRIPTION, it) }
        user.date.let { data1.put(Constants.FIELD_DATE, it) }
        user.month.let { data1.put(Constants.FIELD_MONTH, it) }
        user.year.let { data1.put(Constants.FIELD_YEAR, it) }
        user.startTime.let { data1.put(Constants.FIELD_START_TIME, it!!) }
        user.endTime.let { data1.put(Constants.FIELD_END_TIME, it!!) }
        user.status.let { data1.put(Constants.FIELD_STATUS, it) }
        user.notify.let { data1.put(Constants.FIELD_NOTIFY, it) }
        user.notificationMin.let { data1.put(Constants.FIELD_NOTIFICATION_MIN, it) }

        bookingRepository.getBookingUpdateRepository(user)
            .update(data1)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    _bookingAddUpdateResponse.postValue(
                        Resource.success(
                            "update ${Constants.MSG_BOOKING_UPDATE_SUCCESSFUL}",
                        )
                    )
                }
            }.addOnFailureListener {
                _bookingAddUpdateResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }

    /**
     * delete time slot
     */
    fun deleteBooking(bookingId: String) {

        if (networkHelper.isNetworkConnected()) {
            _bookingDeleteResponse.value = Resource.loading(null)
            val ref = bookingRepository.getBookingAll()

            ref.document(bookingId).delete()
                .addOnCompleteListener {

                    if (it.isSuccessful) {
                        _bookingDeleteResponse.value = Resource.success(Constants.MSG_BOOKING_DELETE_SUCCESSFUL)
                    }
                }
                .addOnFailureListener {
                    _bookingDeleteResponse.value = Resource.error(it.localizedMessage, null)
                }

        } else {
            _bookingDeleteResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }

    }
    /**
     * Get status update
     */
    fun getStatusUpdateListener(userId: String) {

        bookingRepository.getAllBookingByUserRepository(userId)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        return
                    }
                    val mList = BookingList.getUserBookingArrayList(value!!, userId)

                    bookingList.value = mList
                }
            })
    }

    /**
     * Get all booking info  for normal user in firebase
     */
    fun getAllUserBookingRequest(userId: String) {

        _getBookingListDataResponse.value = Resource.loading(null)

        bookingRepository.getAllBookingByUserRepository(userId).get()
            .addOnSuccessListener {
                val mList = BookingList.getUserBookingArrayList(it, userId)
                _getBookingListDataResponse.postValue(
                    Resource.success(
                        mList
                    )
                )
            }.addOnFailureListener {

                _getBookingListDataResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }

    /**
     * Get all booking info  for normal user in firebase
     */
    fun getAllAstrologerrBookingRequestWithDate(userId: String, eventDate: String) {

        _getBookingListDataResponse.value = Resource.loading(null)

        bookingRepository.getAllUserBookingRequestWithDate(userId, eventDate).get()
            .addOnSuccessListener {
                val mList = BookingList.getUserBookingForAstrologerArrayList(it, userId)
                _getBookingListDataResponse.postValue(
                    Resource.success(
                        mList
                    )
                )
            }.addOnFailureListener {

                _getBookingListDataResponse.postValue(
                    Resource.error(
                        it.message.toString(),
                        null
                    )
                )
            }
        //snapshot
        /*bookingRepository.getAllUserBookingRequestWithDate(userId, eventDate)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    _getBookingListDataResponse.postValue(
                        Resource.error(
                            Constants.VALIDATION_ERROR,
                            null
                        )
                    )
                }

                val mList = BookingList.getUserBookingArrayList(value!!, userId)
                _getBookingListDataResponse.postValue(
                    Resource.success(
                        mList
                    )
                )
            }*/
    }

    /**
     * Get past booking info  for normal user in firebase
     */
    fun getPastUserBookingRequest(userId: String) {

        _getBookingListDataResponse.value = Resource.loading(null)

        bookingRepository.getPastBookingByUserRepository(userId).get()
            .addOnSuccessListener {
                val mList = BookingList.getUserBookingArrayList(it, userId)
                _getBookingListDataResponse.postValue(
                    Resource.success(
                        mList
                    )
                )
            }.addOnFailureListener {

                _getBookingListDataResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }

    /**
     * Get ongoing booking info  for normal user in firebase
     */
    fun getOnGoingUserBookingRequest(userId: String) {

        _getBookingListDataResponse.value = Resource.loading(null)

        bookingRepository.getOnGoingBookingByUserRepository(userId).get()
            .addOnSuccessListener {
                val mList = BookingList.getUserBookingArrayList(it, userId)
                _getBookingListDataResponse.postValue(
                    Resource.success(
                        mList
                    )
                )
            }.addOnFailureListener {

                _getBookingListDataResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }

    /**
     * Get upcoming booking info  for normal user in firebase
     */
    fun getUpComingUserBookingRequest(userId: String) {

        _getBookingListDataResponse.value = Resource.loading(null)

        bookingRepository.getUpComingBookingByUserRepository(userId).get()
            .addOnSuccessListener {
                val mList = BookingList.getUserBookingArrayList(it, userId)
                _getBookingListDataResponse.postValue(
                    Resource.success(
                        mList
                    )
                )
            }.addOnFailureListener {

                _getBookingListDataResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }

    /**
     * Get booking info  for astrologer in firebase
     */
    fun getAstrologerBookingRequest(userID: String) {

        _getBookingListDataResponse.value = Resource.loading(null)

        bookingRepository.getBookingByAstrologerRepository(userID).get()
            .addOnSuccessListener {
                val mList = BookingList.getAstrologerBookingArrayList(it, userID)
                _getBookingListDataResponse.postValue(
                    Resource.success(
                        mList
                    )
                )
            }.addOnFailureListener {

                _getBookingListDataResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }

    /**
     * Get all pending booking info
     */


    fun getBookingDetail(bookingId: String) {
        if (networkHelper.isNetworkConnected()) {
            _getBookingDetailResponse.value = Resource.loading(null)

            bookingRepository.getBookingDetail(bookingId)
                .addSnapshotListener { value, error ->
                    if (error!=null) {
                        _getBookingDetailResponse.postValue(
                            Resource.error(
                                error.message.toString(),
                                null
                            )
                        )

                    }
                    var bookingDetail = BookingList.getBookingDetail(value!!)
                    _getBookingDetailResponse.postValue(Resource.success(bookingDetail))
                }

        } else {
            _getBookingDetailResponse.postValue(
                Resource.error(
                    Constants.MSG_NO_INTERNET_CONNECTION,
                    null
                )
            )
        }
    }
}