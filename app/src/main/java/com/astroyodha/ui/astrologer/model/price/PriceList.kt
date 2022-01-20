package com.astroyodha.ui.astrologer.model.price

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.astroyodha.utils.Constants

object PriceList {

    /**
     * make array list of order from firestore snapshot
     */
    fun getPriceArrayList(
        querySnapshot: QuerySnapshot,
        userId: String
    ): ArrayList<PriceModel> {
        val bookingArrayList = ArrayList<PriceModel>()
        for (doc in querySnapshot.documents) {
            val priceModel = PriceModel()

            doc.get(Constants.FIELD_PRICE_ID)?.let {
                priceModel.id = it.toString()
            }
            doc.get(Constants.FIELD_FIFTEEN_MIN_CHARGE)?.let {
                priceModel.fifteenMin = it.toString()
            }
            doc.get(Constants.FIELD_THIRTY_MIN_CHARGE)?.let {
                priceModel.thirtyMin = it.toString()
            }
            doc.get(Constants.FIELD_FORTYFIVE_MIN_CHARGE)?.let {
                priceModel.fortyFiveMin = it.toString()
            }
            doc.get(Constants.FIELD_SIXTY_MIN_CHARGE)?.let {
                priceModel.sixtyMin = it.toString()
            }
            doc.get(Constants.FIELD_UID)?.let {
                priceModel.userId = it.toString()
            }
            if (doc.get(Constants.FIELD_UID) == userId) {
                bookingArrayList.add(priceModel)
            }

        }
        return bookingArrayList
    }

    /**
     * set single model item
     */
    fun getPriceModel(
        querySnapshot: QueryDocumentSnapshot
    ): PriceModel {

        val priceModel = PriceModel()

        querySnapshot.get(Constants.FIELD_PRICE_ID)?.let {
            priceModel.id = it.toString()
        }
        querySnapshot.get(Constants.FIELD_FIFTEEN_MIN_CHARGE)?.let {
            priceModel.fifteenMin = it.toString()
        }
        querySnapshot.get(Constants.FIELD_THIRTY_MIN_CHARGE)?.let {
            priceModel.thirtyMin = it.toString()
        }
        querySnapshot.get(Constants.FIELD_FORTYFIVE_MIN_CHARGE)?.let {
            priceModel.fortyFiveMin = it.toString()
        }
        querySnapshot.get(Constants.FIELD_SIXTY_MIN_CHARGE)?.let {
            priceModel.sixtyMin = it.toString()
        }
        querySnapshot.get(Constants.FIELD_UID)?.let {
            priceModel.userId = it.toString()
        }

        return priceModel
    }

    /**
     * set single model item
     */
    fun getPriceDetail(
        doc: DocumentSnapshot
    ): PriceModel {

        val priceModel = PriceModel()

        doc.get(Constants.FIELD_PRICE_ID)?.let {
            priceModel.id = it.toString()
        }
        doc.get(Constants.FIELD_FIFTEEN_MIN_CHARGE)?.let {
            priceModel.fifteenMin = it.toString()
        }
        doc.get(Constants.FIELD_THIRTY_MIN_CHARGE)?.let {
            priceModel.thirtyMin = it.toString()
        }
        doc.get(Constants.FIELD_FORTYFIVE_MIN_CHARGE)?.let {
            priceModel.fortyFiveMin = it.toString()
        }
        doc.get(Constants.FIELD_SIXTY_MIN_CHARGE)?.let {
            priceModel.sixtyMin = it.toString()
        }
        doc.get(Constants.FIELD_UID)?.let {
            priceModel.userId = it.toString()
        }

        return priceModel
    }
}