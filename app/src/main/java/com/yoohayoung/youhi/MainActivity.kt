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

    private lateinit var apiService: ApiService

    // CoroutineScope를 선언합니다. 적절한 범위에서 CoroutineScope를 생성하세요.
    val scope = CoroutineScope(Dispatchers.IO)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "알림기능이 허용되었습니다.", Toast.LENGTH_SHORT)
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


        findViewById<ImageView>(R.id.main_option).setOnClickListener {
            showDialog()
        }

        try {
            // Retrofit 객체 초기화
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("http://hihihaha.tplinkdns.com:4000")
                .client(createOkHttpClient()) //<- Interceptor 를 사용하는 클라이언트 지정
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            // ApiService 인터페이스 구현체 생성
            apiService = retrofit.create(ApiService::class.java)

        }catch (e: KeyManagementException) {
            e.printStackTrace()
        }



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

    private fun createOkHttpClient(): OkHttpClient {
        val interceptor = ResponseInterceptor()

        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
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

    // 액티비티의 생명주기에 맞게 CoroutineScope를 취소해야 합니다.
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel() // CoroutineScope를 취소합니다.
    }

    private fun showDialog(){
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.main_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("메뉴")

        val alertDialog = mBuilder.show()
        alertDialog.findViewById<Button>(R.id.mypage_btn)?.setOnClickListener{
            val intent = Intent(this, MyPageActivity::class.java)
            alertDialog.dismiss()
            startActivity(intent)
        }

        alertDialog.findViewById<Button>(R.id.BTN_profile)?.setOnClickListener{
            val intent = Intent(this, ContentListActivity::class.java)
            intent.putExtra("category", "category1")
            alertDialog.dismiss()
            startActivity(intent)
        }
        alertDialog.findViewById<Button>(R.id.BTN_blog)?.setOnClickListener{
            val intent = Intent(this, ContentListActivity::class.java)
            intent.putExtra("category", "category2")
            alertDialog.dismiss()
            startActivity(intent)
        }

        alertDialog.findViewById<Button>(R.id.logout_btn)?.setOnClickListener{
            auth.signOut()

            val intent = Intent(this, IntroActivity::class.java)

            //로그아웃한뒤에 뒤로가기 눌렀을때 앱이 종료되도록 설정
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            alertDialog.dismiss()
            startActivity(intent)
        }
    }



}
