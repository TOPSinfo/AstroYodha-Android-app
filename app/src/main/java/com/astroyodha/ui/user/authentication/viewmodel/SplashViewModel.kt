package com.astroyodha.ui.user.authentication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astroyodha.data.repository.CMSRepository
import com.astroyodha.data.repository.UserRepository
import com.astroyodha.network.NetworkHelper
import com.astroyodha.network.Resource
import com.astroyodha.ui.user.authentication.model.cms.CMSList
import com.astroyodha.ui.user.authentication.model.cms.CMSModel
import com.astroyodha.ui.user.authentication.model.cms.FAQList
import com.astroyodha.ui.user.authentication.model.cms.FAQModel
import com.astroyodha.ui.user.authentication.model.user.UserModel
import com.astroyodha.ui.user.authentication.model.user.UsersList
import com.astroyodha.utils.Constants
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val cmsRepository: CMSRepository,
    private val userRepository: UserRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {


    private val _userDataResponse: MutableLiveData<Resource<UserModel>> = MutableLiveData()
    val userDataResponse: LiveData<Resource<UserModel>> get() = _userDataResponse

    private val _mobileValidationCheckWithUserType: MutableLiveData<Resource<String>> = MutableLiveData()
    val mobileValidationCheckWithUserType: LiveData<Resource<String>> get() = _mobileValidationCheckWithUserType

    private val _socialLoginCheckWithUserType: MutableLiveData<Resource<Boolean>> = MutableLiveData()
    val socialLoginCheckWithUserType: LiveData<Resource<Boolean>> get() = _socialLoginCheckWithUserType

    private val _termsAndPolicyResponse: MutableLiveData<Resource<ArrayList<CMSModel>>> = MutableLiveData()
    val termsAndPolicyResponse: LiveData<Resource<ArrayList<CMSModel>>> get() = _termsAndPolicyResponse

    private val _faqResponse: MutableLiveData<Resource<ArrayList<FAQModel>>> = MutableLiveData()
    val faqResponse: LiveData<Resource<ArrayList<FAQModel>>> get() = _faqResponse

    private val _socialLoginCheckWithoutUserType: MutableLiveData<Resource<Boolean>> = MutableLiveData()
    val socialLoginCheckWithoutUserType: LiveData<Resource<Boolean>> get() = _socialLoginCheckWithoutUserType


    private val _mobileValidationCheckWithoutUserType: MutableLiveData<Resource<String>> = MutableLiveData()
    val mobileValidationCheckWithoutUserType: LiveData<Resource<String>> get() = _mobileValidationCheckWithoutUserType

    /**
     * Checking user already registered or not
     */
    fun checkUserRegisterOrNot(userId: String) = viewModelScope.launch {
        if (networkHelper.isNetworkConnected()) {
            _userDataResponse.value = Resource.loading(null)
            val docIdRef: DocumentReference = userRepository.getUserProfileRepository(userId)

            docIdRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document: DocumentSnapshot? = task.result
                    if (document?.exists() == true) {

                        var userModel = UsersList.getUserDetail(document)

                        document?.get(Constants.FIELD_FULL_NAME)?.let {
                            Constants.USER_NAME = it.toString()
                        }

                        document?.get(Constants.FIELD_PROFILE_IMAGE)?.let {
                            Constants.USER_PROFILE_IMAGE = it.toString()
                        }

                        _userDataResponse.postValue(Resource.success(userModel))

                        /*_userDataResponse.postValue(
                            Resource.success(
                                true,
                            )
                        )*/

                    } else {

                        _userDataResponse.postValue(
                            Resource.success(
                                UserModel(),
                            )
                        )
                    }
                } else {

                    _userDataResponse.postValue(
                        Resource.error(
                            Constants.VALIDATION_ERROR,
                            null
                        )
                    )
                }
            }
        } else _userDataResponse.postValue(
            Resource.error(
                Constants.MSG_NO_INTERNET_CONNECTION,
                null
            )
        )
    }

    /**
     * Check mobile number is registered or not
     */
    fun checkMobieNuberRegisterdOrNotWithUserType(mobileNumber: String, userType:String) = viewModelScope.launch {
        if (networkHelper.isNetworkConnected()) {
            _userDataResponse.value = Resource.loading(null)
            userRepository.getUserCollection()
                .whereEqualTo(Constants.FIELD_PHONE, mobileNumber.replace(" ",""))
                .whereEqualTo(Constants.FIELD_USER_TYPE,userType)
                .get().addOnSuccessListener {
                    if(!it.isEmpty) {
                        _mobileValidationCheckWithUserType.postValue(
                            Resource.success(
                                Constants.MSG_MOBILE_NUMBER_REGISTERD
                            )
                        )
                    }
                    else{
                        _mobileValidationCheckWithUserType.postValue(
                            Resource.error(
                                Constants.MSG_MOBILE_NUMBER_NOT_REGISTERD,
                                null
                            ))
                    }
                }
                .addOnFailureListener {
                    _mobileValidationCheckWithUserType.postValue(
                        Resource.error(
                            Constants.MSG_MOBILE_NUMBER_NOT_REGISTERD,
                            null
                        ))
                }
        } else _mobileValidationCheckWithUserType.postValue(
            Resource.error(
                Constants.MSG_NO_INTERNET_CONNECTION,
                null
            )
        )
    }

    /**
     * Check mobile number is registered or not
     */
    fun checkMobieNuberRegisterdOrNotWithouUserType(mobileNumber: String) = viewModelScope.launch {
        if (networkHelper.isNetworkConnected()) {
            _userDataResponse.value = Resource.loading(null)
            userRepository.getUserCollection()
                .whereEqualTo(Constants.FIELD_PHONE, mobileNumber.replace(" ",""))
                .get().addOnSuccessListener {
                    if(!it.isEmpty) {
                        _mobileValidationCheckWithoutUserType.postValue(
                            Resource.success(
                                Constants.MSG_MOBILE_NUMBER_REGISTERD
                            )
                        )
                    }
                    else{
                        _mobileValidationCheckWithoutUserType.postValue(
                            Resource.error(
                                Constants.MSG_MOBILE_NUMBER_NOT_REGISTERD,
                                null
                            ))
                    }
                }
                .addOnFailureListener {
                    _mobileValidationCheckWithoutUserType.postValue(
                        Resource.error(
                            Constants.MSG_MOBILE_NUMBER_NOT_REGISTERD,
                            null
                        ))
                }
        } else _mobileValidationCheckWithUserType.postValue(
            Resource.error(
                Constants.MSG_NO_INTERNET_CONNECTION,
                null
            )
        )
    }

    /**
     * Check user is already login using social media and user type
     */
    fun checkUserWithSocialMediaWithUserType(socialId: String, userType:String) = viewModelScope.launch {
        if (networkHelper.isNetworkConnected()) {
            _userDataResponse.value = Resource.loading(null)
            userRepository.getUserCollection()
                .whereEqualTo(Constants.FIELD_SOCIAL_ID, socialId)
                .whereEqualTo(Constants.FIELD_USER_TYPE,userType)
                .get().addOnSuccessListener {
                    if(!it.isEmpty) {
                        _socialLoginCheckWithUserType.postValue(
                            Resource.success(true)
                        )
                    }
                    else{
                        _socialLoginCheckWithUserType.postValue(Resource.success(false))

                    }
                }
                .addOnFailureListener {
                    _socialLoginCheckWithUserType.postValue(
                        Resource.error(
                            Constants.MSG_MOBILE_NUMBER_NOT_REGISTERD,
                            null
                        ))
                }
        } else _socialLoginCheckWithUserType.postValue(
            Resource.error(
                Constants.MSG_NO_INTERNET_CONNECTION,
                null
            )
        )
    }

    /**
     * Check user is already login using social media and user type
     */
    fun checkUserWithSocialMediaWithoutUserType(socialId: String) = viewModelScope.launch {
        if (networkHelper.isNetworkConnected()) {
            _userDataResponse.value = Resource.loading(null)
            userRepository.getUserCollection()
                .whereEqualTo(Constants.FIELD_SOCIAL_ID, socialId)
                .get().addOnSuccessListener {
                    if(!it.isEmpty) {
                        _socialLoginCheckWithoutUserType.postValue(
                            Resource.success(true)
                        )
                    }
                    else{
                        _socialLoginCheckWithoutUserType.postValue(Resource.success(false))

                    }
                }
                .addOnFailureListener {
                    _socialLoginCheckWithoutUserType.postValue(
                        Resource.error(
                            Constants.MSG_MOBILE_NUMBER_NOT_REGISTERD,
                            null
                        ))
                }
        } else _socialLoginCheckWithoutUserType.postValue(
            Resource.error(
                Constants.MSG_NO_INTERNET_CONNECTION,
                null
            )
        )
    }

    /**
     * get policy, terms and condition
     */
    fun getPolicies(type: String) = viewModelScope.launch {
        if (networkHelper.isNetworkConnected()) {
            _termsAndPolicyResponse.value = Resource.loading(null)
            cmsRepository.getPolicy(type).get().addOnSuccessListener {
                val mList = CMSList.getCMSArrayList(it)
                    _termsAndPolicyResponse.postValue(
                        Resource.success(
                            mList
                        ))
                }
        } else _termsAndPolicyResponse.postValue(
            Resource.error(
                Constants.MSG_NO_INTERNET_CONNECTION,
                null
            )
        )
    }

    /**
     * get FAQ
     */
    fun getFAQ(type: String) {
        viewModelScope.launch {
            if (networkHelper.isNetworkConnected()) {
                _faqResponse.value = Resource.loading(null)
                cmsRepository.getPolicy(type).get().addOnSuccessListener {
                    val mList = FAQList.getCMSArrayList(it)
                    if(mList.isNotEmpty()) {
                        cmsRepository.getFAQ(it.documents[0].id).get().addOnSuccessListener {
                            val mList = FAQList.getCMSArrayList(it)
                            _faqResponse.postValue(
                                Resource.success(
                                    mList
                                )
                            )
                        }
                    } else {
                        Resource.error(
                            Constants.MSG_SOMETHING_WENT_WRONG,
                            null
                        )
                    }
                }

            } else _faqResponse.postValue(
                Resource.error(
                    Constants.MSG_NO_INTERNET_CONNECTION,
                    null
                )
            )
        }
    }
}