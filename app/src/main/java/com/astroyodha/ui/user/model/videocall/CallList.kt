package com.astroyodha.ui.user.model.videocall

import com.astroyodha.ui.user.model.CallListModel
import com.astroyodha.utils.Constants
import com.google.firebase.firestore.DocumentSnapshot

object CallList {

    /**
     * set single model item
     */
    fun getCallList(
        querySnapshot: DocumentSnapshot
    ): CallListModel {

        val callModel = CallListModel()

        querySnapshot.get(Constants.FIELD_CALL_STATUS)?.let {
            callModel.callStatus = it.toString()
        }
        querySnapshot.get(Constants.FIELD_HOST_ID)?.let {
            callModel.HostId = it.toString()
        }
        querySnapshot.get(Constants.FIELD_USERIDS)?.let {
            callModel.userIds = it as ArrayList<String>
        }
        querySnapshot.get(Constants.FIELD_HOST_NAME)?.let {
            callModel.hostName = it.toString()
        }
        callModel.docId = querySnapshot.id

        return callModel
    }
}