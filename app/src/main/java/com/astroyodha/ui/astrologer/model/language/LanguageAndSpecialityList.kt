package com.astroyodha.ui.astrologer.model.language

import com.google.firebase.firestore.QuerySnapshot
import com.astroyodha.utils.Constants

object LanguageAndSpecialityList {

    /**
     * make array list of language from firestore snapshot
     */
    fun getLanguageArrayList(
        querySnapshot: QuerySnapshot
    ): ArrayList<LanguageAndSpecialityModel> {
        val languageArrayList = ArrayList<LanguageAndSpecialityModel>()

        for (doc in querySnapshot.documents) {
            val languageModel = LanguageAndSpecialityModel()

            doc.get(Constants.FIELD_LANGUAGE_ID)?.let {
                languageModel.id = it.toString()
            }
            doc.get(Constants.FIELD_LANGUAGE_NAME)?.let {
                languageModel.language = it.toString()
            }

            languageArrayList.add(languageModel)

        }
        return languageArrayList
    }


}