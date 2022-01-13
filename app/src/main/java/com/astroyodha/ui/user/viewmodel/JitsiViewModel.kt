package com.astroyodha.ui.user.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.astroyodha.data.repository.ChatRepository
import com.astroyodha.network.NetworkHelper
import com.astroyodha.network.Resource
import com.astroyodha.ui.user.model.CallListModel
import com.astroyodha.ui.user.model.videocall.CallList
import com.astroyodha.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JitsiViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {

    fun getFirebaseDB() = chatRepository.firestoreDB

    private val _updateUserStatusResponse: MutableLiveData<Resource<String>> =
        MutableLiveData()

    val updateUserStatusResponse: LiveData<Resource<String>> get() = _updateUserStatusResponse

    private val _receiveGroupCallInvitationResponse: MutableLiveData<Resource<String>> =
        MutableLiveData()

    val receiveGroupCallInvitationResponse: LiveData<Resource<String>> get() = _receiveGroupCallInvitationResponse


    fun changeStatus(docId:String,isActive:Boolean){
        if (networkHelper.isNetworkConnected()) {
            val docRef= getFirebaseDB().collection(Constants.TABLE_GROUPCALL).document(docId)

            docRef.get().addOnSuccessListener {
                var model = CallListModel(
                    it[Constants.FIELD_CALL_STATUS].toString(),
                    it[Constants.FIELD_HOST_ID].toString(),
                    it[Constants.FIELD_USERIDS] as ArrayList<String>,
                    it.id,
                    it[Constants.FIELD_HOST_NAME].toString()
                )

                var map=HashMap<String,Any>()

//                if(!isActive && model.HostId.equals(FirebaseAuth.getInstance().currentUser?.uid.toString()))
//                {
//                    map.put("CallStatus","InActive")
//                }

                if(isActive)
                {
                    var pos=model.userIds.indexOf(FirebaseAuth.getInstance().currentUser?.uid.toString()+"___InActive")
                    if(pos<model.userIds.size && pos!=-1) {
                        model.userIds[pos] =
                            FirebaseAuth.getInstance().currentUser?.uid.toString() + "___Active"
                    }
                }
                else
                {
                    var pos=model.userIds.indexOf(FirebaseAuth.getInstance().currentUser?.uid.toString()+"___Active")
                    if(pos<model.userIds.size && pos!=-1) {
                        model.userIds[pos] =
                            FirebaseAuth.getInstance().currentUser?.uid.toString() + "___InActive"
                    }
                }

                map.put(Constants.FIELD_CALL_STATUS,"InActive")
                for(i in model.userIds)
                {
                    if(i.endsWith("___Active"))
                    {
                        map.put(Constants.FIELD_CALL_STATUS,"Active")
                    }
                }
                map.put(Constants.FIELD_USERIDS,model.userIds)
                docRef.update(map)

            }

        } else {
//            toast(Constant.MSG_NO_INTERNET_CONNECTION)
        }
    }

    /**
     * get List of active call
     */
    fun getLIstOfActiveCall(currentUserId: String, callStatus: String) = viewModelScope.launch {
        if (networkHelper.isNetworkConnected()) {

            var userIdStatusCheckArray = ArrayList<String>()
            userIdStatusCheckArray.add(currentUserId + "___InActive")
            userIdStatusCheckArray.add(currentUserId + "___Active")

            var query = getFirebaseDB().collection(Constants.TABLE_GROUPCALL)
                .whereArrayContainsAny(Constants.FIELD_USERIDS, userIdStatusCheckArray)
                .whereEqualTo(Constants.FIELD_CALL_STATUS, callStatus)

            query.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _receiveGroupCallInvitationResponse.postValue(
                        Resource.error(
                            "No Data",
                            "Failed to connect"
                        )
                    )
                    return@addSnapshotListener
                }

                if (snapshot !== null) {
//                    Constant.listOfActiveCall.clear()
                    if(snapshot.isEmpty)
                    {
                        Constants.listOfActiveCall.clear()
                    }
                    else {

                        for (dc in snapshot.documents) {
                            val callModel = CallList.getCallList(dc)
                            if (callModel.callStatus.equals("Active")) {
                                Constants.listOfActiveCall.clear()
                                Constants.listOfActiveCall.add(callModel)

                            }
                        }
                    }
                    Log.e("List Of New Active Call ADD===", "===" + Constants.listOfActiveCall)
                }

            }


        } else {
            _receiveGroupCallInvitationResponse.postValue(
                Resource.error(
                    "",
                    Constants.MSG_NO_INTERNET_CONNECTION
                )
            )
        }
    }

}