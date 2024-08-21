package com.yoohayoung.youhi.board

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
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
import okhttp3.*
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_board_write)

        category = intent.getStringExtra("category").toString()

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

        binding.writeBtn.setOnClickListener {
            val title = binding.titleArea.text.toString()
            val content = binding.contentArea.text.toString()
            val uid = FBAuth.getUid()
            val time = FBAuth.getTime()

            var key = ""

            if (category == "category1") {
                key = FBRef.boardRef1.push().key.toString() // 데이터가 생성되기 전에 키값을 먼저 받을 수 있다.

                FBRef.boardRef1
                    .child(key)
                    .setValue(BoardModel(title, content, uid, time))

            } else if (category == "category2") {
                key = FBRef.boardRef2.push().key.toString() // 데이터가 생성되기 전에 키값을 먼저 받을 수 있다.
                FBRef.boardRef2
                    .child(key)
                    .setValue(BoardModel(title, content, uid, time))
            } else if (category == "category3") {
                key = FBRef.boardRef3.push().key.toString() // 데이터가 생성되기 전에 키값을 먼저 받을 수 있다.
                FBRef.boardRef3
                    .child(key)
                    .setValue(BoardModel(title, content, uid, time))
            } else if (category == "category4") {
                key = FBRef.boardRef4.push().key.toString() // 데이터가 생성되기 전에 키값을 먼저 받을 수 있다.
                FBRef.boardRef4
                    .child(key)
                    .setValue(BoardModel(title, content, uid, time))
            } else if (category == "category5") {
                key = FBRef.boardRef5.push().key.toString() // 데이터가 생성되기 전에 키값을 먼저 받을 수 있다.
                FBRef.boardRef5
                    .child(key)
                    .setValue(BoardModel(title, content, uid, time))
            } else if (category == "category6") {
                key = FBRef.boardRef6.push().key.toString() // 데이터가 생성되기 전에 키값을 먼저 받을 수 있다.
                FBRef.boardRef6
                    .child(key)
                    .setValue(BoardModel(title, content, uid, time))
            }  else if (category == "category7") {
                key = FBRef.boardRef7.push().key.toString() // 데이터가 생성되기 전에 키값을 먼저 받을 수 있다.
                FBRef.boardRef7
                    .child(key)
                    .setValue(BoardModel(title, content, uid, time))
            } else {
                key = "null"
                Log.e("error", "!!!! category가 없습니다")
            }

            Toast.makeText(this, "게시글 입력 완료", Toast.LENGTH_SHORT).show()

            // 이미지를 선택한 경우에만 이미지 업로드
            if (imageSelected) {
                imageUpload(key)
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

        binding.imageArea.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 200)
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

    private fun imageUpload(key: String) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val mountainsRef = storageRef.child(key + ".png")

        val imageView = binding.imageArea

        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = mountainsRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == 200) {
            binding.imageArea.setImageURI(data?.data)
            imageSelected = true // 이미지를 선택한 경우 플래그를 설정
        }
    }
}
