package com.astroyodha.utils

import android.content.Context
import android.view.View
import com.astroyodha.R
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetUserInfo
import org.jitsi.meet.sdk.JitsiMeetView
import java.net.MalformedURLException
import java.net.URL

class JitsiManager(var context: Context) {

    private var defaultOptions: JitsiMeetConferenceOptions.Builder?

    init {
        val serverURL: URL = try {
            URL(context.getString(R.string.server_url))
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            throw RuntimeException(context.getString(R.string.invalid_srver_url))
        }

        var userinfo = JitsiMeetUserInfo()
        userinfo.displayName = Constants.USER_NAME
        if(!Constants.USER_PROFILE_IMAGE.equals("")) {
            var url = URL(Constants.USER_PROFILE_IMAGE)
            userinfo.avatar = url
        }

        defaultOptions = JitsiMeetConferenceOptions.Builder()
            .setServerURL(serverURL)
            .setSubject("")
            .setWelcomePageEnabled(false)
            .setUserInfo(userinfo)
            .setFeatureFlag("meeting-password.enabled", false)
            .setFeatureFlag("add-people.enabled", false)
            .setFeatureFlag("invite.enabled", true)
            .setFeatureFlag("chat.enabled", false)
            .setFeatureFlag("kick-out.enabled", false)
            .setFeatureFlag("live-streaming.enabled", false)
            .setFeatureFlag("meeting-password.enabled", false)
            .setFeatureFlag("video-share.enabled", true)
            .setFeatureFlag("calendar.enabled", false)
            .setFeatureFlag("lobby-mode.enabled", false)
            .setFeatureFlag("help-view.enabled", false)
            .setFeatureFlag("close-captions.enabled", false)
            .setFeatureFlag("call-integration.enabled", false)
            .setFeatureFlag("recording.enabled", true)
            .setFeatureFlag("close-captions.enabled", false)
            .setFeatureFlag("toolbox.alwaysVisible", false)
            .setFeatureFlag("help.enabled", false)
            .setFeatureFlag("raise-hand.enabled", false)
            .setFeatureFlag("overflow-menu.enabled", true)
            .setFeatureFlag("meeting-name.enabled", false)
            .setFeatureFlag("android.screensharing.enabled",false)
            .setFeatureFlag("conference-timer.enabled",false)

    }

    /**
     * Start video call
     */
    fun startVideoCall(roomId: String):JitsiMeetConferenceOptions {

        val options = defaultOptions!!
            .setRoom(roomId)
            .build()
        return options
    }
    
    /**
     * start custom call using jitsi meet view
     */
    fun startCustomVideoCall(roomId: String):View{

        val view=JitsiMeetView(context)
        val options = defaultOptions!!
            .setRoom(roomId)
            .build()
        view.join(options)
        return view
    }
}