package com.astroyodha.ui.user.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.astroyodha.data.repository.BookingRepository
import com.astroyodha.network.NetworkHelper
import com.astroyodha.network.Resource
import com.astroyodha.ui.user.model.booking.BookingList
import com.astroyodha.ui.user.model.booking.BookingModel
import com.astroyodha.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val networkHelper: NetworkHelper
) : ViewModel()  {

    private val _monthWiseBookingEventResponse: MutableLiveData<Resource<ArrayList<BookingModel>>> = MutableLiveData()
    val monthWiseBookingEventResponse: LiveData<Resource<ArrayList<BookingModel>>> get() = _monthWiseBookingEventResponse


    fun getMonthWiseAstrologerBookingEventRequest(userId: String) {

        _monthWiseBookingEventResponse.value = Resource.loading(null)

        bookingRepository.getAstrologerBookingEventResponse(userId).get()
            .addOnSuccessListener {
                val mList = BookingList.getUserBookingArrayList(it, userId)
                _monthWiseBookingEventResponse.postValue(
                    Resource.success(
                        mList
                    )
                )
            }.addOnFailureListener {

                _monthWiseBookingEventResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }

    /**
     * set month wise booking event
     */
    fun getMonthWiseBookingEventRequest(userId: String, month: String) {

        _monthWiseBookingEventResponse.value = Resource.loading(null)

        bookingRepository.getBookingEventResponse(userId).get()
            .addOnSuccessListener {
                val mList = BookingList.getUserBookingArrayList(it, userId)
                _monthWiseBookingEventResponse.postValue(
                    Resource.success(
                        mList
                    )
                )
            }.addOnFailureListener {

                _monthWiseBookingEventResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }

    }


}