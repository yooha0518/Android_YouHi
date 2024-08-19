package com.yoohayoung.youhi.utils

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MessagingService : FirebaseMessagingService() {

    // 새로운 토큰이 생성될 때 마다 해당 콜백이 호출된다.
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("onNewToken", "onNewToken: $token")
    }

    // Foreground에서 Push Service를 받기 위해 Notification 설정
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
    }

}