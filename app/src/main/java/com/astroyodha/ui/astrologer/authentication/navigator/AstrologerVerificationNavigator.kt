package com.astroyodha.ui.astrologer.authentication.navigator

import com.google.firebase.auth.PhoneAuthProvider
import com.astroyodha.ui.astrologer.model.user.AstrologerUserModel

interface AstrologerVerificationNavigator {
    fun onStarted()
    fun onSuccess()
    fun redirectToDashboard(user: AstrologerUserModel)
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