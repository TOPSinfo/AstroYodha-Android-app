package com.astroyodha.ui.user.authentication.navigator

import com.astroyodha.ui.user.authentication.model.user.UserModel
import com.google.firebase.auth.PhoneAuthProvider

interface VerificationNavigator {
    fun onStarted()
    fun onSuccess()
    fun redirectToDashboard(user: UserModel)
    fun startPhoneNumberVerification()
    fun hideDialog()
    fun showDialog()
    fun enableResendOTPView()
    fun startCountdownTimer()
    fun showMessage(message: String)
    fun onFailure(message: String)
    fun showLinkErrormsg()
    fun signInWithPhoneAuth()
    fun resendVerificationCode(resentToken: PhoneAuthProvider.ForceResendingToken)
}