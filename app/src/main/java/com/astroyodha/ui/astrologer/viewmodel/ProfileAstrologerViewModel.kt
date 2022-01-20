package com.astroyodha.ui.astrologer.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.astroyodha.R
import com.astroyodha.data.repository.UserRepository
import com.astroyodha.network.NetworkHelper
import com.astroyodha.network.Resource
import com.astroyodha.ui.astrologer.model.language.LanguageAndSpecialityList
import com.astroyodha.ui.astrologer.model.language.LanguageAndSpecialityModel
import com.astroyodha.ui.astrologer.model.user.AstrologerUserModel
import com.astroyodha.ui.astrologer.model.user.AstrologerUsersList
import com.astroyodha.ui.user.authentication.model.rating.RatingList
import com.astroyodha.ui.user.authentication.model.rating.RatingModel
import com.astroyodha.ui.user.authentication.model.user.UserModel
import com.astroyodha.utils.Constants
import com.astroyodha.utils.MyLog
import com.astroyodha.utils.Utility
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class ProfileAstrologerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {
    private val TAG: String = javaClass.simpleName

    private val _userDataResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val userDataResponse: LiveData<Resource<String>> get() = _userDataResponse

    var isUserOnline: MutableLiveData<Boolean> = MutableLiveData()

    private val _userDetailResponse: MutableLiveData<Resource<AstrologerUserModel>> =
        MutableLiveData()
    val userDetailResponse: LiveData<Resource<AstrologerUserModel>> get() = _userDetailResponse

    private val _getAllAstrologerResponse: MutableLiveData<Resource<List<AstrologerUserModel>>> =
        MutableLiveData()
    val getAllAstrologerResponse: LiveData<Resource<List<AstrologerUserModel>>> get() = _getAllAstrologerResponse

    private val _getAstrologerListResponse: MutableLiveData<Resource<List<AstrologerUserModel>>> =
        MutableLiveData()
    val getAstrologerListResponse: LiveData<Resource<List<AstrologerUserModel>>> get() = _getAstrologerListResponse

    private val _ratingDataResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val ratingDataResponse: LiveData<Resource<String>> get() = _ratingDataResponse

    private val _ratingResponse: MutableLiveData<Resource<ArrayList<RatingModel>>> = MutableLiveData()
    val ratingResponse: LiveData<Resource<ArrayList<RatingModel>>> get() = _ratingResponse

    private val _specialityResponse: MutableLiveData<Resource<ArrayList<LanguageAndSpecialityModel>>> = MutableLiveData()
    val specialityResponse: LiveData<Resource<ArrayList<LanguageAndSpecialityModel>>> get() = _specialityResponse


    private val _languageAndSpecialityListResponse: MutableLiveData<Resource<ArrayList<LanguageAndSpecialityModel>>> = MutableLiveData()
    val languageAndSpecialityListResponse: LiveData<Resource<ArrayList<LanguageAndSpecialityModel>>> get() = _languageAndSpecialityListResponse


    private val _normalUserDetailResponse: MutableLiveData<Resource<UserModel>> =
        MutableLiveData()
    val normalUserDetailResponse: LiveData<Resource<UserModel>> get() = _normalUserDetailResponse


    /**
     * uploading profile picture to firebase storage
     */
    fun updateProfilePicture(
        user: AstrologerUserModel,
        profileImagePath: Uri,
        isForUpdate: Boolean
    ) {

        _userDataResponse.value = Resource.loading(null)
        if (networkHelper.isNetworkConnected()) {

            if (isForUpdate) {
                if (profileImagePath != null && !user.profileImage.equals("")) {//Update Data If image is Update


                    var pictureRef =
                        Utility.storageRef.storage.getReferenceFromUrl(user.profileImage!!)

                    // Delete the file
                    pictureRef.delete().addOnCompleteListener {
                        if(it.isSuccessful) {
                        }
                    }.addOnFailureListener {
                    }
                }
            }

            val frontCardPath =
                "${Constants.PROFILE_IMAGE_PATH}/${System.currentTimeMillis()}.jpg"
            val filepath = Utility.storageRef.child(frontCardPath)
            filepath.putFile(profileImagePath).continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                filepath.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downUri: Uri? = task.result
                    downUri?.let {
                        user.profileImage = it.toString()
                        if (isForUpdate) {
                            updateUserData(user)
                        } else {
                            addUserData(user)
                        }

                    }
                }
            }
        } else {
            _userDataResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }

    }


    /**
     * Adding user info in firebase
     */
    fun addUserData(user: AstrologerUserModel) {

        _userDataResponse.value = Resource.loading(null)
        user.createdAt = Timestamp.now()
        val data = hashMapOf(
            Constants.FIELD_UID to user.uid,
            Constants.FIELD_FULL_NAME to user.fullName,
            Constants.FIELD_PHONE to user.phone,
            Constants.FIELD_EMAIL to user.email,
            Constants.FIELD_BIRTH_DATE to user.birthDate,
            Constants.FIELD_PROFILE_IMAGE to user.profileImage,
            Constants.FIELD_IS_ONLINE to user.isOnline,
            Constants.FIELD_GROUP_CREATED_AT to user.createdAt,
            Constants.FIELD_LANGUAGES to user.languages,
            Constants.FIELD_SPECIALITY to user.speciality,
            Constants.FIELD_PRICE to user.price,
            Constants.FIELD_WALLET_BALANCE to user.walletBalance,
            Constants.FIELD_EXPERIENCE to user.experience,
            Constants.FIELD_ABOUT to user.about,
            Constants.FIELD_TOKEN to user.fcmToken
        )
        userRepository.getUserProfileRepository(user.uid.toString()).set(data)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    _userDataResponse.postValue(
                        Resource.success(
                            Constants.VALIDATION_ERROR,
                        )
                    )
                }
            }.addOnFailureListener {

                _userDataResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }

    /**
     * Update user presence online/offline
     */
    fun updateUserPresence(isOnline: Boolean, userId: String) {

        userRepository.getUserProfileRepository(userId)
            .update(
                mapOf(
                    Constants.FIELD_IS_ONLINE to isOnline,
                )
            )
    }

    /**
     * Get user presence of selected user
     */
    fun getUserPresenceUpdateListener(otherUserId: String) {

        userRepository.getUserProfileRepository(otherUserId)
            .addSnapshotListener(object : EventListener<DocumentSnapshot?> {

                override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        return
                    }

                    value?.get(Constants.FIELD_IS_ONLINE)?.let {
                        isUserOnline.value = it as Boolean
                    }
                }
            })
    }


    /**
     * Get user presence of selected user
     */
    fun getUserDetail(userId: String, isCallForEditBooking: Boolean = false) {

        if (networkHelper.isNetworkConnected()) {
            _userDetailResponse.value = Resource.loading(null)
            userRepository.getUserProfileRepository(userId)
                .get().addOnSuccessListener {

                    var userModel = AstrologerUsersList.getUserDetail(it)

                    if (!isCallForEditBooking) {
                        it?.get(Constants.FIELD_FULL_NAME)?.let {
                            Constants.USER_NAME = it.toString()
                        }
                        it?.get(Constants.FIELD_PROFILE_IMAGE)?.let {
                            Constants.USER_PROFILE_IMAGE = it.toString()
                        }
                    }

                    _userDetailResponse.postValue(Resource.success(userModel))

                }
        } else {
            _userDetailResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }

    }

    /**
     * Get user presence of selected user
     */
    fun getUserSnapshotDetail(userId: String) {

        if (networkHelper.isNetworkConnected()) {
            _userDetailResponse.value = Resource.loading(null)
            userRepository.getUserProfileRepository(userId)
                .addSnapshotListener { snapshot, e ->

                    if (e != null) {
                        _userDetailResponse.value= Resource.error(e.message.toString(),null)
                        return@addSnapshotListener
                    }
                    var userModel = AstrologerUsersList.getUserDetail(snapshot!!)

                    _userDetailResponse.postValue(Resource.success(userModel))
                }
        } else {
            _userDetailResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }

    }

    /**
     * Updating user info in firebase
     */
    fun updateUserData(user: AstrologerUserModel) {

        _userDataResponse.value = Resource.loading(null)
        user.createdAt = Timestamp.now()

        var data1 = HashMap<String, Any>()
        user.fullName?.let { data1.put(Constants.FIELD_FULL_NAME, it) }
        user.profileImage?.let { data1.put(Constants.FIELD_PROFILE_IMAGE, it) }
        user.email?.let { data1.put(Constants.FIELD_EMAIL, it) }
        user.birthDate?.let { data1.put(Constants.FIELD_BIRTH_DATE, it) }
        user.languages?.let { data1.put(Constants.FIELD_LANGUAGES, it) }
        user.speciality?.let { data1.put(Constants.FIELD_SPECIALITY, it) }
        user.price?.let { data1.put(Constants.FIELD_PRICE, it) }
        user.experience?.let { data1.put(Constants.FIELD_EXPERIENCE, it) }
        user.about?.let { data1.put(Constants.FIELD_ABOUT, it) }
        user.fcmToken?.let { data1.put(Constants.FIELD_TOKEN, it) }

        userRepository.getUserProfileRepository(user.uid.toString()).update(data1)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    _userDataResponse.postValue(
                        Resource.success(
                            Constants.MSG_UPDATE_SUCCESSFULL,
                        )
                    )
                }
            }.addOnFailureListener {

                _userDataResponse.postValue(
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
    fun getAllAstrologer() {

        if (networkHelper.isNetworkConnected()) {
            _getAllAstrologerResponse.value = Resource.loading(null)
            userRepository.getAllAstrologerUser()
                .get().addOnSuccessListener {

                    var userModel = AstrologerUsersList.getUserArrayList(it, "")


                    _getAllAstrologerResponse.postValue(Resource.success(userModel))

                }
        } else {
            _getAllAstrologerResponse.value =
                Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }

    }

    /**
     * get astrologer as per filter
     */
    fun getAllAstrologerFilterWise(sortBy: String, specialityList: List<String>) {

        if (networkHelper.isNetworkConnected()) {
            _getAllAstrologerResponse.value = Resource.loading(null)
            var ref = userRepository.getAllAstrologerUser()
            if (sortBy == context.getString(R.string.experience_high_to_low)) {
                ref = userRepository.getAllAstrologerExperienceWise(sortBy, specialityList, false)
            } else if (sortBy == context.getString(R.string.experience_low_to_high)) {
                ref = userRepository.getAllAstrologerExperienceWise(sortBy, specialityList, true)
            } else if (sortBy == context.getString(R.string.price_high_to_low)) {
                ref = userRepository.getAllAstrologerPriceWise(sortBy, specialityList, false)
            } else if (sortBy == context.getString(R.string.price_low_to_high)) {
                ref = userRepository.getAllAstrologerPriceWise(sortBy, specialityList, true)
            }

            ref.get().addOnSuccessListener {

                var userModel = AstrologerUsersList.getUserArrayList(it, "")

                _getAllAstrologerResponse.postValue(Resource.success(userModel))

            }
        } else {
            _getAllAstrologerResponse.value =
                Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }

    }

    /**
     * Get user presence of selected user
     */
    fun getAstrologerList(limit: Long) {

        if (networkHelper.isNetworkConnected()) {
            _getAstrologerListResponse.value = Resource.loading(null)
            userRepository.getAllAstrologerUser()
                .limit(limit)
                .get().addOnSuccessListener {
                    var userModel = AstrologerUsersList.getUserArrayList(it, "")
                    _getAstrologerListResponse.postValue(Resource.success(userModel))
                }
        } else {
            _getAstrologerListResponse.value =
                Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }

    }

    /**
     * Adding astrologer rating
     */
    fun addRatingToAstrologer(user: RatingModel) {

        _ratingDataResponse.value = Resource.loading(null)

        var addUserDocument= userRepository.addRating(user.astrologerId)
        user.createdAt= Timestamp.now()
        user.id = addUserDocument.id

        val data = hashMapOf(
            Constants.FIELD_RATING_ID to user.id,
            Constants.FIELD_UID to user.userId,
            Constants.FIELD_USER_NAME to user.userName,
            Constants.FIELD_ASTROLOGER_ID to user.astrologerId,
            Constants.FIELD_RATING to user.rating,
            Constants.FIELD_FEEDBACK to user.feedBack,
            Constants.FIELD_GROUP_CREATED_AT to user.createdAt
        )
        addUserDocument.set(data)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    _ratingDataResponse.postValue(
                        Resource.success(
                            Constants.MSG_RATING_SUCCESSFUL,
                        )
                    )
                }
            }.addOnFailureListener {

                _ratingDataResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }

    /**
     * Get astrologer rating
     */
    fun getAstrologerRating(userId: String) {

        _ratingResponse.value = Resource.loading(null)

        userRepository.getRating(userId).get()
            .addOnSuccessListener {
                var ratingList = RatingList.getRatingArrayList(it, userId)
                _ratingResponse.postValue(
                    Resource.success(
                        ratingList,
                    )
                )
            }
            .addOnFailureListener {

                _ratingResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }

    /**
     * Get astrologer rating
     */
    fun getSpeciality(userId: String = "") {

        _specialityResponse.value = Resource.loading(null)

        userRepository.getSpeciality(userId).get()
            .addOnSuccessListener {
                var specialityList = LanguageAndSpecialityList.getLanguageArrayList(it)
                _specialityResponse.postValue(
                    Resource.success(
                        specialityList,
                    )
                )
            }
            .addOnFailureListener {

                _specialityResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }

    /**
     * Updating user FCM token in firebase
     */
    fun updateUserToken(userId: String,token:String) {

        if(networkHelper.isNetworkConnected()) {
            var data1 = HashMap<String, Any>()
            token.let { data1.put(Constants.FIELD_TOKEN, it) }

            userRepository.getUserProfileRepository(userId).update(data1)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        _userDataResponse.postValue(
                            Resource.success(
                                Constants.MSG_UPDATE_SUCCESSFULL,
                            )
                        )
                    }
                }.addOnFailureListener {

                    _userDataResponse.postValue(
                        Resource.error(
                            Constants.VALIDATION_ERROR,
                            null
                        )
                    )
                }
        }
        else
        {
            _userDataResponse.postValue(
                Resource.error(
                    Constants.MSG_NO_INTERNET_CONNECTION,
                    null
                )
            )
        }
    }

    /**
     * Updating user Walletbalance of astrologer in firebase
     */
    fun updateAstrologerWalletBalance(userId: String,balance:Int) {

        if(networkHelper.isNetworkConnected()) {
            var data1 = HashMap<String, Any>()
            balance.let { data1.put(Constants.FIELD_WALLET_BALANCE, it) }

            userRepository.getUserProfileRepository(userId).update(data1)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        _userDataResponse.postValue(
                            Resource.success(
                                Constants.MSG_UPDATE_SUCCESSFULL,
                            )
                        )
                    }
                }.addOnFailureListener {

                    _userDataResponse.postValue(
                        Resource.error(
                            Constants.VALIDATION_ERROR,
                            null
                        )
                    )
                }
        }
        else
        {
            _userDataResponse.postValue(
                Resource.error(
                    Constants.MSG_NO_INTERNET_CONNECTION,
                    null
                )
            )
        }
    }


    /**
     * getting list of language
     */
    fun getLanguageList() {

        if (networkHelper.isNetworkConnected()) {
            userRepository.getLanguageRepository().get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        var languageList =
                            LanguageAndSpecialityList.getLanguageArrayList(it.result!!)

                        _languageAndSpecialityListResponse.postValue(
                            Resource.success(languageList)
                        )
                    }
                }.addOnFailureListener {

                    _languageAndSpecialityListResponse.postValue(
                        Resource.error(
                            Constants.VALIDATION_ERROR,
                            null
                        )
                    )
                }
        } else {
            _languageAndSpecialityListResponse.postValue(
                Resource.error(
                    Constants.MSG_NO_INTERNET_CONNECTION,
                    null
                )
            )
        }
    }



    /**
     * Get user presence of selected user
     */
    fun getUserDetailById(userId: String) {

        if (networkHelper.isNetworkConnected()) {
            _userDetailResponse.value = Resource.loading(null)
            userRepository.getUserProfileRepository(userId)
                .get().addOnSuccessListener {

                    var userModel = AstrologerUsersList.getNormalUserDetail(it)

                    _normalUserDetailResponse.postValue(Resource.success(userModel))
                }
        } else {
            _normalUserDetailResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }

    }


}