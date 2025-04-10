package com.yoohayoung.youhi

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.yoohayoung.youhi.auth.IntroActivity
import com.yoohayoung.youhi.databinding.ActivityMyPageBinding
import com.yoohayoung.youhi.utils.FBAuth.Companion.getUid
import com.yoohayoung.youhi.utils.FBRef
import com.yoohayoung.youhi.utils.GlideOptions
import com.yoohayoung.youhi.utils.ResponseInterceptor
import com.yoohayoung.youhi.utils.RetrofitClient.apiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class MyPageActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMyPageBinding

    // 이미지 선택 결과를 처리하는 ActivityResultLauncher 선언
    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Glide로 선택된 이미지를 ImageView에 표시
            Glide.with(this)
                .load(it)
                .apply(GlideOptions.myPageProfileOptions)
                .into(binding.IVProfile)

            // Glide를 사용해 Bitmap으로 로드 후 서버 업로드
            Glide.with(this)
                .asBitmap() // Bitmap으로 로드
                .load(it)
                .apply(GlideOptions.myPageProfileOptions)
                .into(object : com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                        // Bitmap을 받으면 reqUploadProfileImage 호출
                        CoroutineScope(Dispatchers.Main).launch {
                            reqUploadProfileImage(resource)
                        }
                    }

                    override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                        // 리소스가 해제될 때 처리할 내용 (필요 없으면 비워둬도 됩니다)
                    }
                })
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        val uid = getUid()
        loadUserData(uid)

        binding.BTNProfile.setOnClickListener {
            selectImageLauncher.launch("image/*") // 이미지 타입만 선택하도록 필터링
        }

        binding.BTNLogout.setOnClickListener {
            auth.signOut()

            val intent = Intent(this, IntroActivity::class.java)

            //로그아웃한뒤에 뒤로가기 눌렀을때 앱이 종료되도록 설정
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

    }

    suspend fun reqUploadProfileImage(bitmap: Bitmap) {
        val uid = getUid()
        try {
            // 비트맵 리사이징
            val resizedBitmap = resizeBitmap(bitmap, 800, 800) // 최대 너비와 높이를 800으로 설정

            // Bitmap을 ByteArray로 변환
            val baos = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageData = baos.toByteArray()

            // 이미지 파일을 RequestBody로 변환
            val requestFile = imageData.toRequestBody("image/jpeg".toMediaType()) // toRequestBody 사용
            val imageBody = MultipartBody.Part.createFormData("image", "${uid}.jpg", requestFile)

            // 닉네임을 String RequestBody로 변환합니다.
            val nicknameBody = uid.toRequestBody("text/plain".toMediaTypeOrNull())

            Log.d("reqUploadProfileImage", "uid: $nicknameBody")

            // 서버로 이미지와 닉네임 업로드 요청
            val response = apiService.uploadProfileImage(nicknameBody,imageBody)

            Log.d("Upload", "Success: ${response.message}")
        } catch (e: Exception) {
            Log.e("Upload", "Error: ${e.message}")
        }
    }



    private fun loadUserData(uid: String) {
        val userRef = FBRef.userRef.child(uid)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(UserModel::class.java)
                    if (user != null) {
                        binding.textViewNickname.text = "닉네임: ${user.nickName}"
                        binding.textViewName.text = "이름: ${user.name}"
                        binding.textViewEmail.text = "이메일: ${user.email}"
                        binding.textViewPoints.text = "포인트: ${user.point}"

                        //프로필 이미지 로드
                        loadProfileImage(uid)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("MyPageActivity", "loadUserData:onCancelled", databaseError.toException())
            }
        })
    }

    private fun loadProfileImage(uid: String) {
        Glide.with(this)
            .load("http://youhi.tplinkdns.com:4000/${uid}.jpg")
            .apply(GlideOptions.myPageProfileOptions)
            .into(binding.IVProfile)
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

    // 비트맵 리사이징 함수
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val aspectRatio = width.toFloat() / height.toFloat()

        // 새로운 너비와 높이 계산
        var newWidth = maxWidth
        var newHeight = (newWidth / aspectRatio).toInt()

        if (newHeight > maxHeight) {
            newHeight = maxHeight
            newWidth = (newHeight * aspectRatio).toInt()
        }

        // 리사이즈된 비트맵 반환
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }



}