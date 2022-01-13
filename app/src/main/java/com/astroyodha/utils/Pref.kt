package com.astroyodha.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Pref  @Inject constructor(@ApplicationContext private val mContext: Context){
    private val TAG = Pref::class.java.simpleName
//    val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    fun openPref_(contex: Context, mPrefName: String?): SharedPreferences {
        return contex.getSharedPreferences(mPrefName, Context.MODE_PRIVATE)
    }

    fun getValue(context: Context? = mContext, key: String, defaultValue: String, mFileName: String? = Constants.PREF_FILE): String? {
        val sharedPreferences = context!!.getSharedPreferences(mFileName, Context.MODE_PRIVATE)
        val result = sharedPreferences.getString(key, defaultValue)
//        MyLog.d(TAG, "getValue() called with: key = [$key], value = [$result]")
        return result
    }

    fun setValue(context: Context = mContext, key: String, value: String, mFileName: String? = Constants.PREF_FILE) {
//        MyLog.d(TAG, "setValue() called with: key = [$key], value = [$value]")
        val sharedPreferences =
            context.getSharedPreferences(mFileName, Context.MODE_PRIVATE)
        val prefsPrivateEditor = sharedPreferences?.edit()
        prefsPrivateEditor?.putString(key, value)
        prefsPrivateEditor?.apply()
//        prefsPrivateEditor.commit()
    }

    fun getValue(context: Context = mContext, key: String, defaultValue: Int, mFileName: String? = Constants.PREF_FILE): Int {
        val sharedPreferences = context.getSharedPreferences(
            mFileName,
            Context.MODE_PRIVATE
        )
        val result = sharedPreferences.getInt(key, defaultValue)
        //		Pref.sharedPreferences = null;
//        MyLog.d(TAG, "getValue() called with: key = [$key], value = [$result]")
        return result
    }

    fun setValue(context: Context = mContext, key: String, value: Int, mFileName: String? = Constants.PREF_FILE) {
//        MyLog.d(TAG, "getValue() called with: key = [$key], value = [$value]")
        val sharedPreferences = context.getSharedPreferences(
            mFileName,
            Context.MODE_PRIVATE
        )
        val prefsPrivateEditor = sharedPreferences.edit()
        prefsPrivateEditor.putInt(key, value)
        prefsPrivateEditor.apply()
    }

    //encrept shared preference
    /*@JvmStatic
    fun getEncryptedValue(context: Context? = mContext?, key: String, defaultValue: String?, mFileName: String? = Constants.PREF_FILE): String? {
        val sharedPreferences = EncryptedSharedPreferences
            .create(
                mFileName!!,
                masterKeyAlias,
                context!!,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

        val result = sharedPreferences.getString(key, defaultValue)
        MyLog.d(TAG, "getValue() called with: key = [$key], value = [$result]")
        return result
    }


    @JvmStatic
    fun setEncryptedValue(context: Context? = mContext?, key: String, value: String, mFileName: String? = Constants.PREF_FILE) {
        MyLog.d(TAG, "setValue() called with: key = [$key], value = [$value]")
        val sharedPreferences = EncryptedSharedPreferences
            .create(
                mFileName!!,
                masterKeyAlias,
                context!!,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

//        val sharedPreferences = context?.getSharedPreferences(mFileName, Context.MODE_PRIVATE)
        val prefsPrivateEditor = sharedPreferences?.edit()
        prefsPrivateEditor?.putString(key, value)
        prefsPrivateEditor?.apply()
    }*/

    fun getValue(context: Context? = mContext, key: String?, defaultValue: Boolean, mFileName: String? = Constants.PREF_FILE): Boolean {
        val sharedPreferences = context!!.getSharedPreferences(mFileName, Context.MODE_PRIVATE)
        //		Pref.sharedPreferences = null;
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun setValue(context: Context = mContext, key: String?, value: Boolean, mFileName: String? = Constants.PREF_FILE) {
        val sharedPreferences = context.getSharedPreferences(
            mFileName,
            Context.MODE_PRIVATE
        )
        val prefsPrivateEditor = sharedPreferences.edit()
        prefsPrivateEditor.putBoolean(key, value)
        prefsPrivateEditor.apply()
        //		prefsPrivateEditor = null;
//		Pref.sharedPreferences = null;
    }

    fun hasKey(context: Context = mContext, mKeyName: String?, PrefFileName: String?, mFileName: String?): Boolean {
        var sharedPreferences = context.getSharedPreferences(
            mFileName,
            Context.MODE_PRIVATE
        )
        val isContain = sharedPreferences!!.contains(mKeyName)
        sharedPreferences = null
        return isContain
    }

    fun setValueLong(context: Context = mContext, key: String?, value: Long, mFileName: String?) {
        val sharedPreferences = context.getSharedPreferences(
            mFileName,
            Context.MODE_PRIVATE
        )
        val prefsPrivateEditor = sharedPreferences.edit()
        prefsPrivateEditor.putLong(key, value)
        prefsPrivateEditor.apply()
        //		prefsPrivateEditor = null;
//		Pref.sharedPreferences = null;
    }

    fun getValueLong(context: Context = mContext, key: String?, defaultValue: Long, mFileName: String?): Long {
        val sharedPreferences = context.getSharedPreferences(
            mFileName,
            Context.MODE_PRIVATE
        )
        //		Pref.sharedPreferences = null;
        return sharedPreferences.getLong(key, 1L)
    }

    fun clearAllPref(context: Context = mContext, mFileName: String?) {
        val sharedPreferences = context.getSharedPreferences(mFileName, Context.MODE_PRIVATE)
        val prefsPrivateEditor = sharedPreferences.edit()
        prefsPrivateEditor.clear()
        prefsPrivateEditor.apply()
    }

    fun clearSpecificKeyValue(context: Context = mContext, mFileName: String?, strKeyValue: String?) {
        val sharedPreferences = context.getSharedPreferences(mFileName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(strKeyValue)
        editor.apply()
    }
}