package com.astroyodha.ui.astrologer.viewmodel

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
import com.astroyodha.utils.MyLog
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AstrologerBookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {

    private val _bookingAddUpdateResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val bookingAddUpdateResponse: LiveData<Resource<String>> get() = _bookingAddUpdateResponse

    var bookingList: MutableLiveData<ArrayList<BookingModel>> = MutableLiveData()

    private val _getBookingListDataResponse: MutableLiveData<Resource<ArrayList<BookingModel>>> =
        MutableLiveData()
    val getBookingListDataResponse: LiveData<Resource<ArrayList<BookingModel>>> get() = _getBookingListDataResponse


    private val _completedBookingResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val completedBookingResponse: LiveData<Resource<String>> get() = _completedBookingResponse


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
            _bookingAddUpdateResponse.value =
                Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
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
            Constants.FIELD_STATUS to user.status,
            Constants.FIELD_GROUP_CREATED_AT to user.createdAt,
        )

        ref.set(data)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    _bookingAddUpdateResponse.postValue(
                        Resource.success(
                            Constants.MSG_BOOKING_UPDATE_SUCCESSFUL,
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

        bookingRepository.getBookingUpdateRepository(user)
            .update(data1)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    _bookingAddUpdateResponse.postValue(
                        Resource.success(
                            Constants.MSG_BOOKING_UPDATE_SUCCESSFUL,
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
    fun getAllCompletedBooking(astrologerId: String) {

        _completedBookingResponse.value = Resource.loading(null)

        if (networkHelper.isNetworkConnected()) {
            bookingRepository.getAllCompletedBookingByAstrologer(
                astrologerId,
                Constants.COMPLETED_STATUS
            )
                .get()
                .addOnSuccessListener {
                    _completedBookingResponse.postValue(
                        Resource.success(it.documents.size.toString())
                    )
                }.addOnFailureListener {

                    _completedBookingResponse.postValue(
                        Resource.error(
                            Constants.VALIDATION_ERROR,
                            null
                        )
                    )
                }
        } else {
            _completedBookingResponse.postValue(
                Resource.error(
                    Constants.MSG_NO_INTERNET_CONNECTION,
                    null
                )
            )
        }
    }

    /**
     * Get all pending booking info  for normal user in firebase
     */
    fun getPendingBookingofAstrologer(userId: String) {
        if (networkHelper.isNetworkConnected()) {
            _getBookingListDataResponse.value = Resource.loading(null)

            bookingRepository.getAllPendingBookingListOfAstrologerRepository(
                userId, Constants.PENDING_STATUS
            ).addSnapshotListener { value, error ->
                if (error != null) {
                    _getBookingListDataResponse.postValue(
                        Resource.error(
                            error.message.toString(),
                            null
                        )
                    )
                } else {
                    if (value != null) {
                        val mList = BookingList.getAstrologerBookingArrayList(value, userId)
                        _getBookingListDataResponse.postValue(
                            Resource.success(
                                mList
                            )
                        )
                    }
                }

            }
        } else {
            _getBookingListDataResponse.postValue(
                Resource.error(
                    Constants.MSG_NO_INTERNET_CONNECTION,
                    null
                )
            )
        }
    }


    /**
     * Get all booking info  for normal user in firebase
     */
    fun getAllAstrologerBookingRequest(userId: String) {

        _getBookingListDataResponse.value = Resource.loading(null)

        bookingRepository.getAllBookingByAstrlogerRepository(userId).get()
            .addOnSuccessListener {
                val mList = BookingList.getAstrologerBookingArrayList(it, userId)
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
     * Get status update
     */
    fun getStatusUpdateListener(userId: String) {

        bookingRepository.getAllBookingByAstrlogerRepository(userId)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        return
                    }
                    val mList = BookingList.getAstrologerBookingArrayList(value!!, userId)
                    bookingList.value = mList
                }
            })
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