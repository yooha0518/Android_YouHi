package com.yoohayoung.youhi

import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.yoohayoung.youhi.auth.UserModel
import com.yoohayoung.youhi.databinding.ActivityMyPageBinding
import com.yoohayoung.youhi.utils.FBAuth.Companion.getUid
import com.yoohayoung.youhi.utils.FBRef
import com.yoohayoung.youhi.utils.ResponseInterceptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.security.KeyManagementException
import java.util.concurrent.TimeUnit

data class uploadImageResponseModel(
    val status: String, // 업로드 성공 또는 실패 상태
    val message: String, // 응답 메시지
    val file: FileInfo? = null // 업로드된 파일 정보 (성공 시)
)

data class FileInfo(
    val fieldname: String,       // 필드 이름 (예: "image")
    val originalname: String,    // 원본 파일 이름
    val encoding: String,        // 인코딩 방식 (예: "7bit")
    val mimetype: String,        // 파일의 MIME 타입 (예: "image/jpeg")
    val destination: String,     // 파일이 저장된 디렉토리 경로 (예: "public/")
    val filename: String,        // 저장된 파일 이름 (예: 닉네임으로 지정된 이름)
    val path: String,            // 파일 경로
    val size: Long               // 파일 크기 (바이트 단위)
)


class MyPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyPageBinding
    private lateinit var apiService: ApiService

    // 이미지 선택 결과를 처리하는 ActivityResultLauncher 선언
    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Glide로 선택된 이미지를 ImageView에 표시
            Glide.with(this)
                .load(it)
                .into(binding.IVProfile)

            // Glide를 사용해 Bitmap으로 로드 후 서버 업로드
            Glide.with(this)
                .asBitmap() // Bitmap으로 로드
                .load(it)
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
        setContentView(R.layout.activity_my_page)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_page)

        val uid = getUid()
        loadUserData(uid)

        try {
            // Retrofit 객체 초기화
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("http://hihihaha.tplinkdns.com:4000")
                .client(createOkHttpClient()) //<- Interceptor 를 사용하는 클라이언트 지정
                .addConverterFactory(GsonConverterFactory.create())// json 변환기 추가
                .build()

            // ApiService 인터페이스 구현체 생성
            apiService = retrofit.create(ApiService::class.java)

        } catch (e: KeyManagementException) {
            e.printStackTrace()
        }

        binding.BTNProfile.setOnClickListener {
            selectImageLauncher.launch("image/*") // 이미지 타입만 선택하도록 필터링
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
            .load("http://hihihaha.tplinkdns.com:4000/${uid}.jpg")
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