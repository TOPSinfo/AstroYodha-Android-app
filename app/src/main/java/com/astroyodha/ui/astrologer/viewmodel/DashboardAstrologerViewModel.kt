package com.astroyodha.ui.astrologer.viewmodel

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
class DashboardAstrologerViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {

    private val _getBookingListDataResponse: MutableLiveData<Resource<ArrayList<BookingModel>>> =
        MutableLiveData()
    val getBookingListDataResponse: LiveData<Resource<ArrayList<BookingModel>>> get() = _getBookingListDataResponse


    /**
     * Get all booking info  for normal user in firebase
     */
    fun getPendingBookingofAstrologer(userId: String) {
        if (networkHelper.isNetworkConnected()) {
            _getBookingListDataResponse.value = Resource.loading(null)

            bookingRepository.getPendingBookingListOfAstrologerRepository(
                userId,
                Constants.PENDING_STATUS, 6
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
}