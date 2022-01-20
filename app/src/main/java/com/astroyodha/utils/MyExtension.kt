package com.astroyodha.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.Html
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import com.astroyodha.R
import com.astroyodha.view.ReadMoreOption
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * My Extensions
 */


/**
 * load fragment
 */
//replase fragment
fun Context.changeFragment(
    containerId: Int = R.id.nav_host_fragment,
    fragment: Fragment,
    TAG: String,
    isBackStack: Boolean = false
) {
    val ft = (this as AppCompatActivity).supportFragmentManager.beginTransaction()
    ft.replace(containerId, fragment, TAG)
    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
    if (isBackStack) {
        ft.addToBackStack(TAG)
    }
    ft.commit()
}

/**
 * load image from drawable
 */
//glide
fun ImageView.loadResourceImage(url: Int, radius: Int = 8) {
    Glide.with(context!!).load(url)
        .transform(CenterCrop(), RoundedCorners(radius))
        .placeholder(ContextCompat.getDrawable(this.context, R.drawable.ic_placeholder_image))
        .error(R.drawable.ic_placeholder_image)
        .into(this)
}

/**
 * load image like profile picture full circle
 */
fun ImageView.loadProfileImage(url: String?) {
    Glide.with(context!!).load(if (url.isNullOrBlank()) "error" else url)
        .apply(RequestOptions.circleCropTransform())
        .placeholder(ContextCompat.getDrawable(this.context, R.drawable.ic_placeholder_user))
        .error(R.drawable.ic_placeholder_user)
        .into(this)
}

/**
 * load image with custom rounded value
 */
fun ImageView.loadRoundedImage(url: String?, radius: Int = 8) {
    Glide.with(context!!).load(if (url.isNullOrBlank()) "error" else url)
        .transform(CenterCrop(), RoundedCorners(radius))
        .placeholder(ContextCompat.getDrawable(this.context, R.drawable.ic_placeholder_image))
        .error(R.drawable.ic_placeholder_image)
        .into(this)
}

/**
 * load image without rounded
 */
fun ImageView.loadImage(url: String?) {
    Glide.with(context!!).load(if (url.isNullOrBlank()) "error" else url)
        .transform(CenterCrop())
        .placeholder(ContextCompat.getDrawable(this.context, R.drawable.ic_placeholder_image))
        .error(R.drawable.ic_placeholder_image)
        .into(this)
}

/**
 * load image without rounded
 */
fun ImageView.loadImageWithoutCenterCrop(url: String?) {
    Glide.with(context!!).load(if (url.isNullOrBlank()) "error" else url)
        .placeholder(ContextCompat.getDrawable(this.context, R.drawable.ic_placeholder_image))
        .error(R.drawable.ic_placeholder_image)
        .into(this)
}

/**
 * toast
 */
fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

/**
 * toast with duration
 */
fun Context.toast(message: String, duration: Int) {
    Toast.makeText(this, message, duration).show()
}

/**
 * snack bar
 */
fun View.showSnackBarToast(strMessage: String) {
    try {
        Snackbar.make(this, strMessage, Snackbar.LENGTH_LONG).show()
    } catch (e: Exception) {

    }
}

/**
 * make view visible
 */
fun View.makeVisible() {
    visibility = View.VISIBLE
}

/**
 * make view gone
 */
fun View.makeGone() {
    visibility = View.GONE
}

/**
 * make view invisible
 */
fun View.makeInvisible() {
    visibility = View.INVISIBLE
}

/**
 * make view click disable
 */
fun View.disable() {
    alpha = .3f
    isClickable = false
}

/**
 * make view click enable
 */
fun View.enable() {
    alpha = 1f
    isClickable = true
}

/**
 * make view disable
 */
// with no alpha
fun View.isDisable() {
    isClickable = false
    isEnabled = false
}

/**
 * make view enable
 */
fun View.isEnable() {
    isClickable = true
    isEnabled = true
}

/**
 * hide keyboard
 */
fun Activity.hideKeyboard() {
    val inputManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val focusedView = this.currentFocus
    if (focusedView != null) {
        inputManager.hideSoftInputFromWindow(
            focusedView.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }
}

/**
 * change date format
 */
fun TextView.setHtmlText(value: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        this.text = (Html.fromHtml(value, Html.FROM_HTML_MODE_COMPACT));
    } else {
        this.text = (Html.fromHtml(value));
    }
}

/**
 * change date format
 */
fun String.dateFormat(currentfromate: String, newfromate: String): String {
    val sdf = SimpleDateFormat(currentfromate, Locale.ENGLISH)

    try {
        return SimpleDateFormat(newfromate).format(sdf.parse(this))

    } catch (e: Exception) {
        e.printStackTrace()
    }

    return ""

}

/**
 * change date format
 */
fun Date.dateToStringFormat(format: String): String {
    val sdf = SimpleDateFormat(format, Locale.ENGLISH)

    try {
        return sdf.format(this)

    } catch (e: Exception) {
        e.printStackTrace()
    }

    return ""

}

fun String.getAmount(): String {
    return substring(indexOfFirst { it.isDigit() }, indexOfLast { it.isDigit() } + 1)
        .filter { it.isDigit() || it == '.' }
}

fun Double.roundOffDecimal(): Double? {
    val df = DecimalFormat("#.#")
    df.roundingMode = RoundingMode.CEILING
    return df.format(this).toDouble()
}

/**
 * setup read more read less
 */
fun TextView.addReadMoreText(text: String?, textColor: Int) {
    try {
        val readMoreOption = ReadMoreOption.Builder(context)
            .textLength(4, ReadMoreOption.TYPE_LINE) // OR
            //.textLength(300, ReadMoreOption.TYPE_CHARACTER)
            .moreLabel("Read More")
            .lessLabel("  Read Less")
            .moreLabelColor(textColor)
            .lessLabelColor(textColor)
            .labelUnderLine(false)
            .expandAnimation(false)
            .build()
        readMoreOption.addReadMoreTo(this, text)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    /*this.text=text
    ReadMoreMediumTextView().makeTextViewResizable(this,2,"Read More",true)*/
}

/**
 * share app intent
 */
fun Context.shareApp(msg: String) {
    val shareBody = msg
    val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
    sharingIntent.type = "text/plain"
    sharingIntent.putExtra(
        android.content.Intent.EXTRA_SUBJECT,
        getString(R.string.app_name)
    )
    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
    startActivity(
        Intent.createChooser(sharingIntent, resources.getString(R.string.share_using))
    )
}

/**
 * redirect to browser
 */
fun Context.openSocialMedia(url: String) {
    var uri = Uri.parse(url)
    try {
        val i = Intent(Intent.ACTION_VIEW, uri)
        startActivity(i)
    } catch (e: java.lang.Exception) {
    }
}

/**
 * get firebase token for notification
 */
fun Context.getFirebaseToken(pref: Pref) {
    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
        if (!task.isSuccessful) {
            return@OnCompleteListener
        }

        // Get new FCM registration token
        val token = task.result

        // Log and toast
        token?.let {
            pref.setValue(
                this,
                Constants.PREF_FCM_TOKEN,
                it,
                Constants.PREF_FILE
            )
        }
    })
    //   MyLog.e(TAG, "&&&&& &&&&& FirebaseToken: " + strToken);
}