package com.astroyodha.ui.astrologer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.astroyodha.data.repository.UserRepository
import com.astroyodha.network.NetworkHelper
import com.astroyodha.network.Resource
import com.astroyodha.ui.astrologer.model.timeslot.TimeSlotList
import com.astroyodha.ui.astrologer.model.timeslot.TimeSlotModel
import com.astroyodha.utils.Constants
import com.astroyodha.utils.MyLog
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TimeSlotViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {

    private val _timeslotDataResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val timeslotDataResponse: LiveData<Resource<String>> get() = _timeslotDataResponse

    private val _timeSlotListResponse: MutableLiveData<Resource<ArrayList<TimeSlotModel>>> =
        MutableLiveData()
    val timeSlotListResponse: LiveData<Resource<ArrayList<TimeSlotModel>>> get() = _timeSlotListResponse

    private val _timeslotDeleteResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val timeslotDeleteResponse: LiveData<Resource<String>> get() = _timeslotDeleteResponse



    /**
     * uploading profile picture to firebase storage
     */
    fun addUpdateBookingData(user: TimeSlotModel, isForUpdate: Boolean) {

        _timeslotDataResponse.value = Resource.loading(null)
        if (networkHelper.isNetworkConnected()) {
            userRepository.getUserProfileRepository(user.userId)
            if (isForUpdate) {
                updateTimeSlotData(user)
            } else {
                addTimeSlotData(user)
            }
        } else {
            _timeslotDataResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }

    }

    /**
     * Adding timeslot info in firebase
     */
    fun addTimeSlotData(timeSlotModel: TimeSlotModel) {
        _timeslotDataResponse.value = Resource.loading(null)
        val ref = userRepository.getTimeSlotAddRepository(timeSlotModel)

        timeSlotModel.id = ref.id
        timeSlotModel.createdAt = Timestamp.now()

//        Constants.FIELD_DATE to timeSlotModel.date,
        val data = hashMapOf(
            Constants.FIELD_TIME_SLOT_ID to timeSlotModel.id,
            Constants.FIELD_START_DATE to timeSlotModel.startDate,
            Constants.FIELD_END_DATE to timeSlotModel.endDate,
            Constants.FIELD_START_TIME to timeSlotModel.fromTime,
            Constants.FIELD_END_TIME to timeSlotModel.toTime,
            Constants.FIELD_REPEAT_DAYS to timeSlotModel.days,
            Constants.FIELD_UID to timeSlotModel.userId,
            Constants.FIELD_TYPE to timeSlotModel.type,
            Constants.FIELD_GROUP_CREATED_AT to timeSlotModel.createdAt,
        )

        ref.set(data)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    _timeslotDataResponse.postValue(
                        Resource.success(
                            Constants.MSG_TIMESLOT_UPDATE_SUCCESSFUL,
                        )
                    )
                }
            }.addOnFailureListener {

                _timeslotDataResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }

    /**
     * Updating timeslot info in firebase
     */
    fun updateTimeSlotData(user: TimeSlotModel) {

        _timeslotDataResponse.value = Resource.loading(null)
        user.createdAt = Timestamp.now()

        var data1 = HashMap<String, Any>()
        user.date.let { data1.put(Constants.FIELD_DATE, it) }
        user.fromTime.let { data1.put(Constants.FIELD_START_TIME, it) }
        user.toTime.let { data1.put(Constants.FIELD_END_TIME, it) }

        userRepository.getTimeSlotUpdateRepository(user)
            .update(data1)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    _timeslotDataResponse.postValue(
                        Resource.success(
                            Constants.MSG_TIMESLOT_UPDATE_SUCCESSFUL,
                        )
                    )
                }
            }.addOnFailureListener {

                _timeslotDataResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }

    /**
     * Get time slot list
     */
    fun getTimeSlotList(userId: String) {

        if (networkHelper.isNetworkConnected()) {
            _timeSlotListResponse.value = Resource.loading(null)
            val ref = userRepository.getAllTimeSlotRepository(userId)

            ref.addSnapshotListener { value, error ->
                if (error != null) {
                    MyLog.e("Error getting timeslot", "===")
                    _timeSlotListResponse.value = Resource.error("", null)

                } else {
                    var timeSlotList = TimeSlotList.getTimeSlotArrayList(value!!, userId)
                    _timeSlotListResponse.postValue(Resource.success(timeSlotList))
                }
            }
        } else {
            _timeSlotListResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }

    }

    /**
     * Get time slot list
     */
    fun getCustomTimeSlotList(userId: String, eventDate: String) {

        if (networkHelper.isNetworkConnected()) {
            _timeSlotListResponse.value = Resource.loading(null)
            val ref = userRepository.getCustomTimeSlotRepository(userId, eventDate)

            ref.addSnapshotListener { value, error ->
                if (error != null) {
                    MyLog.e("Error getting timeslot", "===")
                    _timeSlotListResponse.value = Resource.error("", null)
                } else {
                    var timeSlotList = TimeSlotList.getTimeSlotArrayList(value!!, userId)
                    _timeSlotListResponse.postValue(Resource.success(timeSlotList))
                }
            }
        } else {
            _timeSlotListResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }

    }

    /**
     * Get time slot list
     */
    fun getWeeklyTimeSlotList(userId: String, eventDate: String) {

        if (networkHelper.isNetworkConnected()) {
            _timeSlotListResponse.value = Resource.loading(null)
            val ref = userRepository.getWeeklyTimeSlotRepository(userId, eventDate)

            ref.addSnapshotListener { value, error ->
                if (error != null) {
                    MyLog.e("Error getting timeslot", "===")
                    _timeSlotListResponse.value = Resource.error("", null)
                } else {
                    var timeSlotList = TimeSlotList.getTimeSlotArrayList(value!!, userId)
                    _timeSlotListResponse.postValue(Resource.success(timeSlotList))
                }
            }
        } else {
            _timeSlotListResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }

    }

    /**
     * Get time slot list
     */
    fun getRepeatTimeSlotList(userId: String, eventDate: String) {

        if (networkHelper.isNetworkConnected()) {
            _timeSlotListResponse.value = Resource.loading(null)
            val ref = userRepository.getRepeatTimeSlotRepository(userId, eventDate)

            ref.addSnapshotListener { value, error ->
                if (error != null) {
                    MyLog.e("Error getting timeslot", "===")
                    _timeSlotListResponse.value = Resource.error("", null)
                } else {
                    var timeSlotList = TimeSlotList.getTimeSlotArrayList(value!!, userId)
                    _timeSlotListResponse.postValue(Resource.success(timeSlotList))
                }
            }
        } else {
            _timeSlotListResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }

    }

    /**
     * delete time slot
     */
    fun deleteTimeSlot(userId: String, timeSlotId: String) {

        if (networkHelper.isNetworkConnected()) {
            _timeSlotListResponse.value = Resource.loading(null)
            val ref = userRepository.getAllTimeSlotRepository(userId)

            ref.document(timeSlotId).delete()
                .addOnCompleteListener {

                    if (it.isSuccessful) {
                        _timeslotDeleteResponse.value = Resource.success(Constants.MSG_TIME_SLOT_DELETE_SUCCESSFULL)
                    }
                }
                .addOnFailureListener {
                    _timeslotDeleteResponse.value = Resource.error(it.localizedMessage, null)
                }

        } else {
            _timeslotDeleteResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }

    }
}