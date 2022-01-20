package com.astroyodha.ui.user.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.astroyodha.data.repository.WalletRepository
import com.astroyodha.network.NetworkHelper
import com.astroyodha.network.Resource
import com.astroyodha.ui.user.model.wallet.WalletList
import com.astroyodha.ui.user.model.wallet.WalletModel
import com.astroyodha.utils.Constants
import com.astroyodha.utils.MyLog
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val userRepository: WalletRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {


    private val _walletDataResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val walletDataResponse: LiveData<Resource<String>> get() = _walletDataResponse

    private val _getTransactionListDataResponse: MutableLiveData<Resource<ArrayList<WalletModel>>> =
        MutableLiveData()
    val getTransactionListDataResponse: LiveData<Resource<ArrayList<WalletModel>>> get() = _getTransactionListDataResponse

    /**
     * uploading profile picture to firebase storage
     */
    fun addMoney(user: WalletModel, isForUpdate: Boolean, isForAstrologer: Boolean = false) {

        _walletDataResponse.value = Resource.loading(null)
        if (networkHelper.isNetworkConnected()) {

            if (isForUpdate) {
                updateWalletData(user, isForAstrologer)
            } else {
                addWalletData(user, isForAstrologer)
            }
        } else {
            _walletDataResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }

    }

    /**
     * Adding user info in firebase
     */
    fun addWalletData(wallet: WalletModel, isForAstrologer: Boolean) {

        _walletDataResponse.value = Resource.loading(null)

        var addUserDocument = userRepository.getWalletAddRepository(wallet.userId)
        wallet.id = addUserDocument.id
        wallet.createdAt = Timestamp.now()

        val data = hashMapOf(
            Constants.FIELD_ID to wallet.id,
            Constants.FIELD_UID to wallet.userId,
            Constants.FIELD_USER_NAME to wallet.userName,
            Constants.FIELD_TRANSACTION_ID to wallet.trancationId,
            Constants.FIELD_AMOUNT to wallet.amount,
            Constants.FIELD_PAYMENT_TYPE to wallet.paymentType,
            Constants.FIELD_TRANSACTION_TYPE to wallet.trancationType,
            Constants.FIELD_DESCRIPTION to wallet.description,
            Constants.FIELD_BOOKING_ID to wallet.bookingId,
            Constants.FIELD_ASTROLOGER_ID to wallet.astrologerId,
            Constants.FIELD_ASTROLOGER_NAME to wallet.astrologerName,
            Constants.FIELD_SET_CAPTURED_GATEWAY to true/*wallet.setCapturedGateway*/,  // required to convert from authorized to captured in razor pay
            Constants.FIELD_GROUP_CREATED_AT to wallet.createdAt
        )
        addUserDocument.set(data)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    _walletDataResponse.postValue(
                        Resource.success(
                            "${wallet.id} ${Constants.MSG_MONEY_ADDED_SUCCESSFUL}",
                        )
                    )
                }
            }.addOnFailureListener {

                _walletDataResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }

        if(isForAstrologer) {
            var addUserDocument =
                userRepository.getAstrologerWalletAddRepository(wallet.astrologerId, wallet.id)
            if (wallet.trancationType == Constants.TRANSACTION_TYPE_DEBIT) {
                wallet.trancationType = Constants.TRANSACTION_TYPE_CREDIT
            } else {
                wallet.trancationType = Constants.TRANSACTION_TYPE_DEBIT
            }
            data.put(Constants.FIELD_TRANSACTION_TYPE, wallet.trancationType);

            addUserDocument.set(data)
                .addOnCompleteListener {
                    if (it.isSuccessful) {}
                }.addOnFailureListener {
                }
        }
    }

    /**
     * Updating user info in firebase
     */
    fun updateWalletData(user: WalletModel, isForAstrologer: Boolean) {

        _walletDataResponse.value = Resource.loading(null)
        user.createdAt = Timestamp.now()

        var data1 = HashMap<String, Any>()
        user.bookingId?.let { data1.put(Constants.FIELD_BOOKING_ID, it) }

        userRepository.getWalletUpdateRepository(user).update(data1)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                        _walletDataResponse.postValue(
                            Resource.success(
                                "${true} ${Constants.MSG_MONEY_ADDED_SUCCESSFUL}",
                            )
                        )
                }
            }.addOnFailureListener {

                _walletDataResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }

        if(isForAstrologer) {
            userRepository.getAstrologerWalletUpdateRepository(user).update(data1)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                    }
                }.addOnFailureListener {
                }
        }
    }

    /**
     * Get all transaction data of user
     */
    fun getAllTransaction(userId: String) {

        _getTransactionListDataResponse.value = Resource.loading(null)

        userRepository.getAllWalletByUserRepository(userId).get()
            .addOnSuccessListener {
                val mList = WalletList.getWalletArrayList(it, userId)
                _getTransactionListDataResponse.postValue(
                    Resource.success(
                        mList
                    )
                )
            }.addOnFailureListener {

                _getTransactionListDataResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }
}