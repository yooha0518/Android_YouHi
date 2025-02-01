package com.yoohayoung.youhi.board

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.yoohayoung.youhi.ApiService
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.databinding.ActivityBoardWriteBinding
import com.yoohayoung.youhi.messageData
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.FBRef
import com.yoohayoung.youhi.utils.ResponseInterceptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.security.KeyManagementException
import java.util.concurrent.TimeUnit


class BoardWriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBoardWriteBinding
    private lateinit var apiService: ApiService
    private var imageSelected: Boolean = false
    private lateinit var category: String

    // ActivityResultLauncher 정의
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            binding.imageArea.setImageURI(data?.data)
            imageSelected = true // 이미지를 선택한 경우 플래그를 설정
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_board_write)
        category = intent.getStringExtra("category").toString()

        try {
            // Retrofit 객체 초기화
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("http://youhi.tplinkdns.com:4000")
                .client(createOkHttpClient()) // Interceptor를 사용하는 클라이언트 지정
                .addConverterFactory(GsonConverterFactory.create()) // JSON 변환기 추가
                .build()

            // ApiService 인터페이스 구현체 생성
            apiService = retrofit.create(ApiService::class.java)
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        }

        binding.BTNWriteBoard.setOnClickListener {
            val title = binding.titleArea.text.toString()
            val content = binding.contentArea.text.toString()
            val uid = FBAuth.getUid()
            val time = FBAuth.getTime()

            val key = when (category) {
                "category1" -> {
                    val newKey = FBRef.boardRef1.push().key.toString()
                    FBRef.boardRef1.child(newKey).setValue(Board(title, content, uid, time, newKey))
                    newKey
                }
                "category2" -> {
                    val newKey = FBRef.boardRef2.push().key.toString()
                    FBRef.boardRef2.child(newKey).setValue(Board(title, content, uid, time, newKey))
                    newKey
                }
                "category3" -> {
                    val newKey = FBRef.boardRef3.push().key.toString()
                    FBRef.boardRef3.child(newKey).setValue(Board(title, content, uid, time, newKey))
                    newKey
                }
                "category4" -> {
                    val newKey = FBRef.boardRef4.push().key.toString()
                    FBRef.boardRef4.child(newKey).setValue(Board(title, content, uid, time, newKey))
                    newKey
                }
                else -> {
                    Log.e("error", "!!!! category가 없습니다")
                    "null"
                }
            }

            Toast.makeText(this, "게시글 입력 완료", Toast.LENGTH_SHORT).show()

            // 이미지를 선택한 경우에만 이미지 업로드
            if (imageSelected) {
                CoroutineScope(Dispatchers.Main).launch {
                    reqUploadBoardImage(key)
                }
            }

            // 닉네임을 가져와서 sendMsgApiRequest 호출
            FBRef.userRef.child(uid).child("nickName")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val nickName = dataSnapshot.getValue(String::class.java)
                        if (nickName != null) {
                            CoroutineScope(Dispatchers.Main).launch {
                                sendMsgApiRequest(nickName, title)
                            }
                        } else {
                            Log.e("sendMsgApiRequest", "닉네임을 찾을 수 없습니다.")
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("sendMsgApiRequest", "닉네임을 가져오는 데 실패했습니다.", databaseError.toException())
                    }
                })

            finish()
        }

        // 이미지 선택을 위한 클릭 리스너 설정
        binding.imageArea.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            pickImageLauncher.launch(gallery)
        }
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

    suspend fun sendMsgApiRequest(nickName: String, message: String) {
        val title = "$nickName 님이 게시물을 작성했습니다."
        val request = messageData(name = FBAuth.getUid(), message = message, title = title)
        try {
            Log.d("sendMsgApiRequest", "nickName: $nickName, message: $message")
            val apiResponse = apiService.sendMsg(request)
            Log.d("apiResponse", apiResponse.toString())
        } catch (e: Exception) {
            Log.e("apiResponse", "Error", e)
        }
    }

    suspend fun reqUploadBoardImage(boardId: String) {
        withContext(Dispatchers.IO) {
            try {
                val imageView = binding.imageArea
                imageView.isDrawingCacheEnabled = true
                val bitmap = (imageView.drawable as BitmapDrawable).bitmap

                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val imageData = baos.toByteArray()

                val requestFile = imageData.toRequestBody("image/jpeg".toMediaType())
                val imageBody = MultipartBody.Part.createFormData("image", "${boardId}.jpg", requestFile)

                val boardIdBody = boardId.toRequestBody("text/plain".toMediaTypeOrNull())

                Log.d("reqUploadProfileImage", "uid: $boardIdBody")

                val response = apiService.uploadBoardImage(boardIdBody, imageBody)

                Log.d("Upload", "Success: ${response.message}")
            } catch (e: Exception) {
                Log.e("Upload", "Error: ${e.message}")
            }
        }
    }
}
