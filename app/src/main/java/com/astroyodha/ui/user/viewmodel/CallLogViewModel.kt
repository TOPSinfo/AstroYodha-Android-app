package com.astroyodha.ui.user.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.astroyodha.data.repository.BookingRepository
import com.astroyodha.network.NetworkHelper
import com.astroyodha.network.Resource
import com.astroyodha.ui.user.model.calllog.CallLogModel
import com.astroyodha.utils.Constants
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CallLogViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {

    private val _bookingLogsResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val bookingLogsResponse: LiveData<Resource<String>> get() = _bookingLogsResponse

    /**
     * Adding call log info in firebase
     */
    fun addCallLogData(bookingId: String, user: CallLogModel) {
        _bookingLogsResponse.value = Resource.loading(null)
        val ref = bookingRepository.getCallLogAddRepository(bookingId)
        user.id = ref.id
        user.createdAt = Timestamp.now()

        val data = hashMapOf(
            Constants.FIELD_ID to user.id,
            Constants.FIELD_START_TIME to user.startTime,
            Constants.FIELD_END_TIME to user.endTime,
            Constants.FIELD_UID to user.userId,
            Constants.FIELD_USER_TYPE to user.userType,
            Constants.FIELD_USER_NAME to user.userName,
            Constants.FIELD_EXTEND_COUNT to user.extendCount,
            Constants.FIELD_EXTEND_MIN to user.extendMin,
            Constants.FIELD_GROUP_CREATED_AT to user.createdAt,
        )

        ref.set(data)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    _bookingLogsResponse.postValue(
                        Resource.success(
                            Constants.MSG_BOOKING_UPDATE_SUCCESSFUL,
                        )
                    )
                }

            }.addOnFailureListener {
                _bookingLogsResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }

    /**
     * Updating call log info in firebase
     */
    fun updateCallLogData(bookingId: String, callLogModel: CallLogModel) {

        _bookingLogsResponse.value = Resource.loading(null)
        callLogModel.createdAt = Timestamp.now()

        var data1 = HashMap<String, Any>()
        callLogModel.startTime.let { data1.put(Constants.FIELD_START_TIME, it!!) }
        if (callLogModel.endTime != null) {
            callLogModel.endTime.let { data1.put(Constants.FIELD_END_TIME, it!!) }
        }
        callLogModel.userName.let { data1.put(Constants.FIELD_USER_NAME, it) }
        callLogModel.extendMin.let { data1.put(Constants.FIELD_EXTEND_MIN, it) }
        callLogModel.extendCount.let { data1.put(Constants.FIELD_EXTEND_COUNT, it) }
        callLogModel.userId.let { data1.put(Constants.FIELD_UID, it) }


        bookingRepository.getCallLogUpdateRepository(bookingId, callLogModel.id)
            .update(data1)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    _bookingLogsResponse.postValue(
                        Resource.success(
                            Constants.MSG_BOOKING_UPDATE_SUCCESSFUL,
                        )
                    )
                }
            }.addOnFailureListener {
                _bookingLogsResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }
}