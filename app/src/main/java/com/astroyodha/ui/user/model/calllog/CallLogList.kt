package com.astroyodha.ui.user.model.calllog

import com.astroyodha.utils.Constants
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

object CallLogList {

    /**
     * make array list of order from firestore snapshot
     */
    fun getCallLogArrayList(
        querySnapshot: QuerySnapshot,
        userId: String
    ): ArrayList<CallLogModel> {
        val callLogArrayList = ArrayList<CallLogModel>()
        for (doc in querySnapshot.documents) {
            val callLogModel = CallLogModel()

            doc.get(Constants.FIELD_ID)?.let {
                callLogModel.id = it.toString()
            }
            doc.get(Constants.FIELD_START_TIME)?.let {
                val tm = it as Timestamp
                callLogModel.startTime = tm.toDate()
            }
            doc.get(Constants.FIELD_END_TIME)?.let {
                val tm = it as Timestamp
                callLogModel.endTime = tm.toDate()
            }
            doc.get(Constants.FIELD_UID)?.let {
                callLogModel.userId = it.toString()
            }
            doc.get(Constants.FIELD_USER_NAME)?.let {
                callLogModel.userName = it.toString()
            }
            doc.get(Constants.FIELD_EXTEND_COUNT)?.let {
                callLogModel.extendCount = it.toString().toInt()
            }
            doc.get(Constants.FIELD_EXTEND_MIN)?.let {
                callLogModel.extendMin = it.toString().toInt()
            }
            doc.get(Constants.FIELD_GROUP_CREATED_AT)?.let {
                callLogModel.createdAt = it as Timestamp
            }
            callLogArrayList.add(callLogModel)

        }
        return callLogArrayList
    }

    /**
     * set single model item
     */
    fun getCallLogDetail(
        doc: DocumentSnapshot
    ): CallLogModel {

        val callLogModel = CallLogModel()

        doc.get(Constants.FIELD_ID)?.let {
            callLogModel.id = it.toString()
        }
        doc.get(Constants.FIELD_START_TIME)?.let {
            val tm = it as Timestamp
            callLogModel.startTime = tm.toDate()
        }
        doc.get(Constants.FIELD_END_TIME)?.let {
            val tm = it as Timestamp
            callLogModel.endTime = tm.toDate()
        }
        doc.get(Constants.FIELD_UID)?.let {
            callLogModel.userId = it.toString()
        }
        doc.get(Constants.FIELD_USER_NAME)?.let {
            callLogModel.userName = it.toString()
        }
        doc.get(Constants.FIELD_EXTEND_COUNT)?.let {
            callLogModel.extendCount = it.toString().toInt()
        }
        doc.get(Constants.FIELD_EXTEND_MIN)?.let {
            callLogModel.extendMin = it.toString().toInt()
        }
        doc.get(Constants.FIELD_GROUP_CREATED_AT)?.let {
            callLogModel.createdAt = it as Timestamp
        }
        return callLogModel
    }
}