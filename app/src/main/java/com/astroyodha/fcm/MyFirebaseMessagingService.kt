package com.astroyodha.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.astroyodha.R
import com.astroyodha.ui.astrologer.activity.AstrologerDashboardActivity
import com.astroyodha.ui.user.activity.UserHomeActivity
import com.astroyodha.ui.user.authentication.activity.SplashActivity
import com.astroyodha.utils.Constants
import com.astroyodha.utils.Coroutines
import com.astroyodha.utils.MyLog
import com.astroyodha.utils.Pref
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = javaClass.simpleName
    private var large_icon: Bitmap? = null

    @Inject
    lateinit var pref: Pref

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Coroutines.io {
            pref.setValue(this, Constants.PREF_FCM_TOKEN, s, Constants.PREF_FILE)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Check if message contains a data payload.
        large_icon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)

        /*//for testing
        remoteMessage.notification?.let {
            val intent = Intent(this, SplashActivity::class.java)
            createNotification(
                it.title,
                it.body, intent
            )
        }*/


        // Notification type
        /*define('BOOKING_EVENT_REMINDER', 1);
        define('BOOKING_ACCEPTED,BOOKING_CREATED', 2);
        define('DIRECT_MESSAGE', 5);
        define('BOOKING_CREATED', 6);*/
        if (remoteMessage.data.isNotEmpty()) {
            val jsonObject = JSONObject(remoteMessage.data as Map<*, *>)
            val badge = jsonObject.optString("badge")

            if (remoteMessage.data["type"] == Constants.NOTIFICATION_REMINDER) {
//                {userid=mn1ZgWDCRZThnNZAYAkk4LpXQNp1, usertype=user, type=1, title=Meeting Reminder, message=You have a meeting in 10 minutes., bookingid=EOfCO2lPHa7eU4h4onvT}
//                {userid=L4j7RaoMaXhNRKckD3CsehRPUfH3, usertype=astrologer, type=1, title=Meeting Reminder, message=You have a meeting in 10 minutes., bookingid=EOfCO2lPHa7eU4h4onvT}

                val intent = if (remoteMessage.data["usertype"] == Constants.USER_NORMAL) {
                    Intent(this, UserHomeActivity::class.java)
                        .putExtra(Constants.INTENT_INDEX, 1)    //booking screen 1 index
                } else {
                    Intent(this, AstrologerDashboardActivity::class.java)
                        .putExtra(Constants.INTENT_INDEX, 0)    //booking screen 1 index//0 after remove home
                }
                createNotification(
                    remoteMessage.data["title"],
                    remoteMessage.data["message"], intent
                )
            } else if (remoteMessage.data["type"] == Constants.NOTIFICATION_REQUEST_ADDED_ACCEPTED) {
//                {userid=mn1ZgWDCRZThnNZAYAkk4LpXQNp1, usertype=user, type=2, title=Meeting request accepted, message=Your meeting request with Rana Satyarajsinh has been accepted., bookingid=EOfCO2lPHa7eU4h4onvT}
//                {userid=L4j7RaoMaXhNRKckD3CsehRPUfH3, usertype=astrologer, type=2, title=New meeting, message=Mitesh Makwana has created a new meeting with you., bookingid=EOfCO2lPHa7eU4h4onvT}

                val intent = if (remoteMessage.data["usertype"] == Constants.USER_NORMAL) {
                    Intent(this, UserHomeActivity::class.java)
                        .putExtra(Constants.INTENT_INDEX, 1)    //booking screen 1 index
                } else {
                    Intent(this, AstrologerDashboardActivity::class.java)
                        .putExtra(Constants.INTENT_INDEX, 0)    //booking screen 1 index//0 after remove home
                }
                createNotification(
                    remoteMessage.data["title"],
                    remoteMessage.data["message"], intent
                )
            } else {
                val intent = Intent(this, SplashActivity::class.java)

                createNotification(
                    remoteMessage.data["title"],
                    remoteMessage.data["message"], intent
                )
            }
        }
    }

    /**
     * notification builder to show the notification view all functionality
     *
     * @param title
     * @param body
     * @param mIntent
     * @return
     */
    private fun createNotification(
        title: String?,
        body: String?,
        mIntent: Intent,
        id: Int = System.currentTimeMillis().toInt()
    ) {
        try {
            val channelId = resources.getString(R.string.app_name)
            val channelName = resources.getString(R.string.app_name)
            val mNotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mChannel = NotificationChannel(
                    channelId, channelName, NotificationManager.IMPORTANCE_HIGH
                )
                mChannel.setLightColor(ContextCompat.getColor(this, R.color.orange_theme))
                mChannel.enableLights(true)
                val audioAttributes: AudioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                mNotificationManager.createNotificationChannel(mChannel)
            }

            val contentIntent = PendingIntent.getActivity(
                this,
                System.currentTimeMillis().toInt(),
                mIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val mBuilder = NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_logo_transparent)
//                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(body)
                ) as NotificationCompat.Builder

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBuilder.color = resources.getColor(R.color.orange_theme)
            }

            mBuilder.setContentIntent(contentIntent)

            mBuilder.build().flags = mBuilder.build().flags or Notification.FLAG_AUTO_CANCEL
            mNotificationManager.notify(id, mBuilder.build())

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}