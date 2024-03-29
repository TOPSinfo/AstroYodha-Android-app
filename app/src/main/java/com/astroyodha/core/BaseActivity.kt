package com.astroyodha.core

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.astroyodha.ui.user.authentication.activity.LoginActivity
import com.astroyodha.utils.Pref
import com.astroyodha.view.CustomProgressDialog
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {
    private var customProgressDialog: CustomProgressDialog? = null

    @Inject
    lateinit var pref: Pref

    /**
     * show progress dialog
     * @param context context of activity
     */
    protected fun showProgress(context: Context) {
        try {
            if (customProgressDialog != null && customProgressDialog!!.isShowing) {
                return
            }
            customProgressDialog = CustomProgressDialog(context)
            customProgressDialog?.show()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * hide progress dialog
     */
    protected fun hideProgress() {
        try {
            if (customProgressDialog != null && customProgressDialog!!.isShowing) {
                customProgressDialog!!.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * redirect to login
     */
    protected fun redirectToLogin(context: Context) {
        try {
            FirebaseAuth.getInstance().signOut()
//            pref.clearAllPref(context, Constants.PREF_FILE)// do not clear scm token will clear and it will set "" in after login
            NotificationManagerCompat.from(context).cancelAll() // clear all notification on logout
            startActivity(Intent(context, LoginActivity::class.java))
            finishAffinity()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}