package com.yoohayoung.youhi

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.yoohayoung.youhi.databinding.DialogExitBinding
import com.yoohayoung.youhi.utils.FBAuth.Companion.getUid
import com.yoohayoung.youhi.utils.RetrofitClient.apiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this)

        auth = Firebase.auth

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

        // 뒤로 가기 버튼 콜백 설정
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitDialog()
            }
        })

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

    private fun askNotificationPermission() {
        // This is only necessary for API Level > 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showExitDialog() {
        val binding = DialogExitBinding.inflate(LayoutInflater.from(this))

        val dialog = AlertDialog.Builder(this)
            .setView(binding.root)
            .create()

        val BTN_exit = binding.BTNExit
        val BTN_cancel = binding.BTNCancel

        BTN_exit.setOnClickListener {
            dialog.dismiss()
            finish()  // 앱 종료
        }

        BTN_cancel.setOnClickListener {
            dialog.dismiss()  // 다이얼로그 닫기
        }

        dialog.show()

    }

}
