package com.astroyodha.ui.user.model.wallet

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.astroyodha.utils.Constants

object WalletList {

    /**
     * make array list of order from firestore snapshot
     */
    fun getWalletArrayList(
        querySnapshot: QuerySnapshot,
        userId: String
    ): ArrayList<WalletModel> {
        val bookingArrayList = ArrayList<WalletModel>()
        for (doc in querySnapshot.documents) {
            val bookingModel = WalletModel()

            doc.get(Constants.FIELD_TRANSACTION_ID)?.let {
                bookingModel.trancationId = it.toString()
            }
            doc.get(Constants.FIELD_UID)?.let {
                bookingModel.userId = it.toString()
            }
            doc.get(Constants.FIELD_USER_NAME)?.let {
                bookingModel.userName = it.toString()
            }
            doc.get(Constants.FIELD_AMOUNT)?.let {
                bookingModel.amount = it.toString().toInt()
            }
            doc.get(Constants.FIELD_DESCRIPTION)?.let {
                bookingModel.description = it.toString()
            }
            doc.get(Constants.FIELD_PAYMENT_TYPE)?.let {
                bookingModel.paymentType = it.toString()
            }
            doc.get(Constants.FIELD_TRANSACTION_TYPE)?.let {
                bookingModel.trancationType = it.toString()
            }
            doc.get(Constants.FIELD_BOOKING_ID)?.let {
                bookingModel.bookingId = it.toString()
            }
            doc.get(Constants.FIELD_ASTROLOGER_ID)?.let {
                bookingModel.astrologerId = it.toString()
            }
            doc.get(Constants.FIELD_ASTROLOGER_NAME)?.let {
                bookingModel.astrologerName = it.toString()
            }
            doc.get(Constants.FIELD_REFUND)?.let {
                bookingModel.isRefund = it.toString().toBoolean()
            }
            doc.get(Constants.FIELD_SET_CAPTURED_GATEWAY)?.let {
                bookingModel.setCapturedGateway = it.toString().toBoolean()
            }
            doc.get(Constants.FIELD_GROUP_CREATED_AT)?.let {
                val tm = it as Timestamp
                bookingModel.createdAt = tm
            }

            bookingArrayList.add(bookingModel)

//            if (doc.get(Constants.FIELD_UID) == userId) {
//                bookingArrayList.add(bookingModel)
//            }

        }
        return bookingArrayList
    }

    /**
     * set single model item
     */
    fun getBookingModel(
        querySnapshot: QueryDocumentSnapshot
    ): WalletModel {

        val bookintModel = WalletModel()

        querySnapshot.get(Constants.FIELD_TRANSACTION_ID)?.let {
            bookintModel.trancationId = it.toString()
        }
        querySnapshot.get(Constants.FIELD_UID)?.let {
            bookintModel.userId = it.toString()
        }
        querySnapshot.get(Constants.FIELD_USER_NAME)?.let {
            bookintModel.userName = it.toString()
        }
        querySnapshot.get(Constants.FIELD_AMOUNT)?.let {
            bookintModel.amount = it.toString().toInt()
        }
        querySnapshot.get(Constants.FIELD_DESCRIPTION)?.let {
            bookintModel.description = it.toString()
        }
        querySnapshot.get(Constants.FIELD_PAYMENT_TYPE)?.let {
            bookintModel.paymentType = it.toString()
        }
        querySnapshot.get(Constants.FIELD_TRANSACTION_TYPE)?.let {
            bookintModel.trancationType = it.toString()
        }
        querySnapshot.get(Constants.FIELD_BOOKING_ID)?.let {
            bookintModel.bookingId = it.toString()
        }
        querySnapshot.get(Constants.FIELD_ASTROLOGER_ID)?.let {
            bookintModel.astrologerId = it.toString()
        }
        querySnapshot.get(Constants.FIELD_ASTROLOGER_NAME)?.let {
            bookintModel.astrologerName = it.toString()
        }
        querySnapshot.get(Constants.FIELD_REFUND)?.let {
            bookintModel.isRefund = it.toString().toBoolean()
        }
        querySnapshot.get(Constants.FIELD_SET_CAPTURED_GATEWAY)?.let {
            bookintModel.setCapturedGateway = it.toString().toBoolean()
        }
        querySnapshot.get(Constants.FIELD_GROUP_CREATED_AT)?.let {
            val tm = it as Timestamp
            bookintModel.createdAt = tm
        }
        return bookintModel
    }

    /**
     * set single model item
     */
    fun getBookingDetail(
        querySnapshot: DocumentSnapshot
    ): WalletModel {

        val bookingModel = WalletModel()

        querySnapshot.get(Constants.FIELD_TRANSACTION_ID)?.let {
            bookingModel.trancationId = it.toString()
        }
        querySnapshot.get(Constants.FIELD_USER_NAME)?.let {
            bookingModel.userName = it.toString()
        }
        querySnapshot.get(Constants.FIELD_UID)?.let {
            bookingModel.userId = it.toString()
        }
        querySnapshot.get(Constants.FIELD_AMOUNT)?.let {
            bookingModel.amount = it.toString().toInt()
        }
        querySnapshot.get(Constants.FIELD_DESCRIPTION)?.let {
            bookingModel.description = it.toString()
        }
        querySnapshot.get(Constants.FIELD_PAYMENT_TYPE)?.let {
            bookingModel.paymentType = it.toString()
        }
        querySnapshot.get(Constants.FIELD_TRANSACTION_TYPE)?.let {
            bookingModel.trancationType = it.toString()
        }
        querySnapshot.get(Constants.FIELD_BOOKING_ID)?.let {
            bookingModel.bookingId = it.toString()
        }
        querySnapshot.get(Constants.FIELD_ASTROLOGER_ID)?.let {
            bookingModel.astrologerId = it.toString()
        }
        querySnapshot.get(Constants.FIELD_ASTROLOGER_NAME)?.let {
            bookingModel.astrologerName = it.toString()
        }
        querySnapshot.get(Constants.FIELD_REFUND)?.let {
            bookingModel.isRefund = it.toString().toBoolean()
        }
        querySnapshot.get(Constants.FIELD_SET_CAPTURED_GATEWAY)?.let {
            bookingModel.setCapturedGateway = it.toString().toBoolean()
        }
        querySnapshot.get(Constants.FIELD_GROUP_CREATED_AT)?.let {
            val tm = it as Timestamp
            bookingModel.createdAt = tm
        }
        return bookingModel
    }
}