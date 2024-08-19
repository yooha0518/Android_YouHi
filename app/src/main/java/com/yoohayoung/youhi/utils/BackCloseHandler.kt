package com.yoohayoung.youhi.utils

import android.app.Activity
import android.widget.Toast


class BackPressHandler(private val activity: Activity) {
    private var backKeyPressedTime: Long = 0
    private val toast: Toast? = null

    fun onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis()
            showGuide()
            return
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            activity.finish()
            toast!!.cancel()
        }
    }

    private fun showGuide() {
        // do something before app is exit ...
        Toast.makeText(activity.application, "앱을 종료하려면 뒤로가기버튼을 한번 더 누르세요.", Toast.LENGTH_SHORT)
            .show()
    }
}