package com.yoohayoung.youhi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.MobileAds
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonSyntaxException
import com.yoohayoung.youhi.auth.IntroActivity
import com.yoohayoung.youhi.contentList.ContentListActivity
import com.yoohayoung.youhi.utils.FBAuth.Companion.getUid
import com.yoohayoung.youhi.utils.ResponseInterceptor
import com.yoohayoung.youhi.utils.RetrofitClient.apiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.security.KeyManagementException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "알림 권한이 허용되었습니다.", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(
                this,
                "알림 권한이 거부되었습니다.",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

//    private lateinit var backPressHandler: BackPressHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this)

        auth = Firebase.auth

        // BackPressCloseHandler 인스턴스 생성
//        backPressHandler = BackPressHandler(this);
//        findViewById<Button>(R.id.logoutBtn).setOnClickListener {
//            auth.signOut()
            //기존의 Acitivity를 날려서 뒤로가기했을때, 앱이 나가지도록 함
//            val intent = Intent(this, IntroActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
//        }


        askNotificationPermission()

        // Firebase 인스턴스에서 현재 장치의 토큰 가져오기
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // FCM 토큰 가져오기
            val token = task.result

            CoroutineScope(Dispatchers.Main).launch {
                saveUserDataApiRequest(getUid(), token)

            }

        }

    }

    suspend fun saveUserDataApiRequest(uid:String, token:String){
        val request = userData(name= uid, token = token)

        Log.d("token", token)
        Log.d("getUid()", getUid())
        Log.d("request", request.toString())

        try{
            val apiResponse = apiService.saveUserToken(request)
            Log.d("apiResponse", apiResponse.toString())
        }catch (e: Exception){
            Log.e("apiResponse", "Error", e)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // BackPressCloseHandler의 onBackPressed 호출
//        backPressHandler.onBackPressed()
    }

    private fun askNotificationPermission() {
        // This is only necessary for API Level > 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

}
