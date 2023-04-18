package com.astroyodha.ui.astrologer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.astroyodha.data.repository.UserRepository
import com.astroyodha.network.NetworkHelper
import com.astroyodha.network.Resource
import com.astroyodha.ui.astrologer.model.price.PriceList
import com.astroyodha.ui.astrologer.model.price.PriceModel
import com.astroyodha.utils.Constants
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PriceViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {


    private val _priceDataResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val priceDataResponse: LiveData<Resource<String>> get() = _priceDataResponse

    private val _priceDetailResponse: MutableLiveData<Resource<PriceModel>> = MutableLiveData()
    val priceDetailResponse: LiveData<Resource<PriceModel>> get() = _priceDetailResponse


    /**
     * uploading profile picture to firebase storage
     */
    fun addUpdatePriceData(user: PriceModel) {

        _priceDataResponse.value = Resource.loading(null)
        if (networkHelper.isNetworkConnected()) {
            if (user.id.isNotBlank()) {
                updateUserData(user)
            } else {
                addUserData(user)
            }
        } else {
            _priceDataResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }
    }

    /**
     * Adding user info in firebase
     */
    fun addUserData(priceModel: PriceModel) {
        _priceDataResponse.value = Resource.loading(null)
        val ref = userRepository.getPriceAddRepository(priceModel.userId)

        priceModel.id = ref.id
        priceModel.createdAt = Timestamp.now()
        val data = hashMapOf(
            Constants.FIELD_PRICE_ID to priceModel.id,
            Constants.FIELD_UID to priceModel.userId,
            Constants.FIELD_FIFTEEN_MIN_CHARGE to priceModel.fifteenMin,
            Constants.FIELD_THIRTY_MIN_CHARGE to priceModel.thirtyMin,
            Constants.FIELD_FORTYFIVE_MIN_CHARGE to priceModel.fortyFiveMin,
            Constants.FIELD_SIXTY_MIN_CHARGE to priceModel.sixtyMin,
            Constants.FIELD_GROUP_CREATED_AT to priceModel.createdAt,
        )

        ref.set(data)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    _priceDataResponse.postValue(
                        Resource.success(
                            Constants.MSG_CHARGES_UPDATE_SUCCESSFUL,
                        )
                    )
                }
            }.addOnFailureListener {
                _priceDataResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }

    /**
     * Get user presence of selected user
     */
    fun getUserDetail(model: PriceModel) {

        if (networkHelper.isNetworkConnected()) {
            _priceDetailResponse.value = Resource.loading(null)
            userRepository.getPriceRepository(model)
                .get().addOnSuccessListener {
                    var userModel = if(it.documents.isNotEmpty()) PriceList.getPriceDetail(it.documents[0]) else PriceModel()
                    _priceDetailResponse.postValue(Resource.success(userModel))
                }
        } else {
            _priceDetailResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }

    }

    /**
     * Updating user info in firebase
     */
    fun updateUserData(user: PriceModel) {
        _priceDataResponse.value = Resource.loading(null)
        user.createdAt = Timestamp.now()

        var data1 = HashMap<String, Any>()
        user.fifteenMin.let { data1.put(Constants.FIELD_FIFTEEN_MIN_CHARGE, it) }
        user.thirtyMin.let { data1.put(Constants.FIELD_THIRTY_MIN_CHARGE, it) }
        user.fortyFiveMin.let { data1.put(Constants.FIELD_FORTYFIVE_MIN_CHARGE, it) }
        user.sixtyMin.let { data1.put(Constants.FIELD_SIXTY_MIN_CHARGE, it) }

        userRepository.getPriceUpdateRepository(user).update(data1)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    _priceDataResponse.postValue(
                        Resource.success(
                            Constants.MSG_CHARGES_UPDATE_SUCCESSFUL,
                        )
                    )
                }
            }.addOnFailureListener {
                _priceDataResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }
}