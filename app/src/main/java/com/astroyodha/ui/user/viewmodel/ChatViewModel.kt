package com.astroyodha.ui.user.viewmodel

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astroyodha.data.repository.ChatRepository
import com.astroyodha.network.NetworkHelper
import com.astroyodha.network.Resource
import com.astroyodha.ui.user.authentication.model.chat.MessagesModel
import com.astroyodha.ui.user.authentication.model.user.UserModel
import com.astroyodha.ui.user.authentication.model.user.UsersList
import com.astroyodha.utils.Constants
import com.astroyodha.utils.Utility
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {


    private val _messagesResponse: MutableLiveData<Resource<ArrayList<MessagesModel>>> =
        MutableLiveData()
    val messageDataResponse: LiveData<Resource<ArrayList<MessagesModel>>> get() = _messagesResponse

    private val _updatedMessagesResponse: MutableLiveData<Resource<ArrayList<MessagesModel>>> =
        MutableLiveData()
    val updatedMessagesResponse: LiveData<Resource<ArrayList<MessagesModel>>> get() = _updatedMessagesResponse

    private val _sendMessagesResponse: MutableLiveData<Resource<Boolean>> =
        MutableLiveData()
    val sendMessagesResponse: LiveData<Resource<Boolean>> get() = _sendMessagesResponse

    private val _addGroupCallDataResponse: MutableLiveData<Resource<String>> =
        MutableLiveData()

    val addGroupCallDataResponse: LiveData<Resource<String>> get() = _addGroupCallDataResponse


    fun getFirebaseDB() = chatRepository.firestoreDB


    /**
     * Get chat messages list and single message also on runtime
     */
    fun getMessagesList(otherUserId: String, isGroup: Boolean,userList:ArrayList<UserModel>) = viewModelScope.launch {

        if (networkHelper.isNetworkConnected()) {
            var docId = ""
            if (isGroup) {
                docId = otherUserId
            } else {
                docId = getCurrentDocumentId(otherUserId)
            }

            Log.e("","====++Document ID=$docId")

            chatRepository.getChatMessageRepository(docId)
                .addSnapshotListener(object : EventListener<QuerySnapshot?> {
                    override fun onEvent(
                        value: QuerySnapshot?,
                        error: FirebaseFirestoreException?
                    ) {
                        if (error != null) {
                            return
                        }
                        val chatList = ArrayList<MessagesModel>()
                        val modifiedChatList = ArrayList<MessagesModel>()

                        for (dc in value?.documentChanges!!) {
                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    val messageModel = UsersList.getMessagesModel(dc.document)

                                    if(isGroup) {
                                        var user: UserModel? =
                                            userList.find { it.uid == messageModel.senderId }
                                        messageModel.senderName = user?.fullName
                                    }
                                    chatList.add(messageModel)
                                }
                                DocumentChange.Type.MODIFIED -> {
                                    val messageModel = UsersList.getMessagesModel(dc.document)
                                    if(isGroup) {
                                        var user: UserModel? =
                                            userList.find { it.uid == messageModel.senderId }
                                        messageModel.senderName = user?.fullName
                                    }
                                    modifiedChatList.add(messageModel)
                                }
                                DocumentChange.Type.REMOVED -> println("ABC::Removed::" + dc.document.data)
                            }
                        }

                        if (chatList.size > 0) {
                            _messagesResponse.postValue(Resource.success(chatList))
                        }

                        if (modifiedChatList.size > 0) {
                            _updatedMessagesResponse.postValue(Resource.success(modifiedChatList))
                        }
                    }
                })
        } else _messagesResponse.postValue(
            Resource.error(
                Constants.MSG_NO_INTERNET_CONNECTION,
                null
            )
        )
    }


    /**
     * Send message to other user
     */
    fun sendMessage(messagesModel: MessagesModel, isAlreadyMessageId: Boolean,isGroup:Boolean) =
        viewModelScope.launch {
            if (networkHelper.isNetworkConnected()) {
                val doc:CollectionReference
                if (isGroup) {
                    doc =
                        chatRepository.getSendMessagePath(messagesModel.receiverId.toString())
                } else {
                    doc =
                        chatRepository.getSendMessagePath(getCurrentDocumentId(messagesModel.receiverId.toString()))
                }
                var timestamp = Timestamp(Date())
                if (isAlreadyMessageId) {
                    timestamp = messagesModel.timeStamp!!
                }

                val data = hashMapOf(
                    Constants.FIELD_MESSAGE to messagesModel.message,
                    Constants.FIELD_RECEIVER_ID to messagesModel.receiverId.toString(),
                    Constants.FIELD_SENDER_ID to messagesModel.senderId.toString(),
                    Constants.FIELD_TIMESTAMP to timestamp,
                    Constants.FIELD_MESSAGE_TYPE to messagesModel.messageType,
                    Constants.FIELD_URL to messagesModel.url,
                    Constants.FIELD_VIDEO_URL to messagesModel.videoUrl,
                    Constants.FIELD_MESSAGE_STATUS to messagesModel.status,
                )

                if (isAlreadyMessageId) {

                    doc.document(messagesModel.messageId.toString()).set(data)
                        .addOnCompleteListener {
                            if(it.isSuccessful) {
                                _sendMessagesResponse.postValue(Resource.success(true))
                            }
                        }.addOnFailureListener {
                            _sendMessagesResponse.postValue(Resource.success(false))
                        }

                } else {

                    doc.document().set(data).addOnCompleteListener {
                        if(it.isSuccessful) {
                            _sendMessagesResponse.postValue(Resource.success(true))
                        }
                    }.addOnFailureListener {
                        _sendMessagesResponse.postValue(Resource.success(false))
                    }
                }

            } else _sendMessagesResponse.postValue(
                Resource.error(
                    Constants.MSG_NO_INTERNET_CONNECTION,
                    null
                )
            )
        }


    /**
     * Get document id of both user of chat
     */
    private fun getCurrentDocumentId(otherUserId: String): String {
        val myUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        return if (myUserId < otherUserId) {
            myUserId + "" + otherUserId
        } else {
            otherUserId + myUserId
        }
    }

    /**
     * Getting video file thumb for upload to firebase
     */
    private fun getVideoThumb(
        messagesModel: MessagesModel,
        localVideoPath: String,
        progressBar: CircularProgressIndicator?,
        isGroup: Boolean
    ) {
        var imageSavedPath: File? = null
        val file = File(localVideoPath)
        val thumbnail = ThumbnailUtils.createVideoThumbnail(
            file.absolutePath,
            MediaStore.Video.Thumbnails.FULL_SCREEN_KIND
        )

        val fOut: FileOutputStream
        try {

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(
                Date()
            )

            imageSavedPath = Utility.getAndroidDefaultMediaPath("$timeStamp.png")
            fOut = FileOutputStream(imageSavedPath)
            thumbnail!!.compress(Bitmap.CompressFormat.PNG, 85, fOut)
            fOut.flush()
            fOut.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        uploadChatImageWithProgress(Uri.fromFile(imageSavedPath), messagesModel, progressBar!!,isGroup)

    }

    /**
     * set Messages Read Status
     */
    fun setMessagesReadStatus(messagesList: ArrayList<MessagesModel>) {

        for (i in messagesList.indices) {
            val messagesModel = messagesList[i]
            val doc =
                chatRepository.getMessageDetailData(
                    getCurrentDocumentId(messagesModel.senderId.toString()),
                    messagesModel.messageId.toString()
                )
            doc.update(
                mapOf(
                    Constants.FIELD_MESSAGE_STATUS to Constants.TYPE_READ,
                )
            )
        }
    }


    /**
     * uploading  picture to firebase storage
     */
    fun uploadChatImageWithProgress(
        profileImagePath: Uri,
        messagesModel: MessagesModel,
        progressBar: CircularProgressIndicator,
        isGroup: Boolean
    ) {
        if (networkHelper.isNetworkConnected()) {
            progressBar.progress = 10
            val frontCardPath =
                "${Constants.CHAT_IMAGE_PATH}/${System.currentTimeMillis()}.jpg"
            val filepath = Utility.storageRef.child(frontCardPath)

            val uploadTask = filepath.putFile(profileImagePath)

            try {
                uploadTask.addOnProgressListener { taskSnapshot ->
                    val progress =
                        (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                    progressBar.progress = progress.toInt()
                }

                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        _sendMessagesResponse.postValue(
                            Resource.error(
                                Constants.VALIDATION_ERROR,
                                null
                            )
                        )
                        throw task.exception!!
                    }
                    filepath.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downUri: Uri? = task.result
                        downUri?.let {
                            messagesModel.url = it.toString()
                            messagesModel.status = Constants.TYPE_SEND
                            sendMessage(messagesModel, true,isGroup)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


        } else _sendMessagesResponse.postValue(
            Resource.error(
                Constants.MSG_NO_INTERNET_CONNECTION,
                null
            )
        )
    }

    fun getChatDocumentId(otherUserId: String, isGroup: Boolean): String {
        if (isGroup) {
            return chatRepository.getMessageDocumentId(otherUserId)
        } else {
            return chatRepository.getMessageDocumentId(getCurrentDocumentId(otherUserId))
        }
    }


    /**
     * uploading video to firebase storage
     */
    fun uploadChatVideoWithProgress(
        profileImagePath: Uri,
        messagesModel: MessagesModel,
        videoExt: String,
        videoPath: String, progressBar: CircularProgressIndicator,
        isGroup: Boolean
    ) {
        if (networkHelper.isNetworkConnected()) {

            val frontCardPath =
                "${Constants.CHAT_VIDEO_PATH}/${System.currentTimeMillis()}.$videoExt"

            val filepath = Utility.storageRef.child(frontCardPath)

            val uploadTask = filepath.putFile(profileImagePath)

            try {
                uploadTask.addOnProgressListener { taskSnapshot ->
                    val progress =
                        (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                    progressBar.progress = progress.toInt()
                }

                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        _sendMessagesResponse.postValue(
                            Resource.error(
                                Constants.VALIDATION_ERROR,
                                null
                            )
                        )
                        throw task.exception!!
                    }
                    filepath.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downUri: Uri? = task.result
                        downUri?.let {
                            messagesModel.videoUrl = it.toString()
                            messagesModel.status = Constants.TYPE_SEND
                            progressBar.progress = 80
                            getVideoThumb(messagesModel, videoPath, progressBar,isGroup)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else _sendMessagesResponse.postValue(
            Resource.error(
                Constants.MSG_NO_INTERNET_CONNECTION,
                null
            )
        )
    }


    /**
     * Add video Call Data in database
     */
    fun setupVideoCallData(
        userIds: ArrayList<String>,
        callStatus: String,
        hostUserId: String,
        hostname: String,
        bookingID: String
    ) =
        viewModelScope.launch {
            if (networkHelper.isNetworkConnected()) {
                val doc = getFirebaseDB().collection(Constants.TABLE_GROUPCALL).document(bookingID)
                val map = hashMapOf(
                    Constants.FIELD_USERIDS to userIds,
                    Constants.FIELD_CALL_STATUS to callStatus,
                    Constants.FIELD_HOST_ID to hostUserId,
                    Constants.FIELD_HOST_NAME to hostname,
                    Constants.FIELD_GROUP_CREATED_AT to Timestamp.now()
                )

                doc.set(map).addOnCompleteListener {
                    if(it.isSuccessful) {
                        _addGroupCallDataResponse.postValue(Resource.success(doc.id))
                    }
                }.addOnFailureListener {
                    _addGroupCallDataResponse.postValue(Resource.success("Failed to add Data"))
                }
            } else _addGroupCallDataResponse.postValue(
                Resource.error(
                    Constants.MSG_NO_INTERNET_CONNECTION,
                    null
                )
            )
        }

}