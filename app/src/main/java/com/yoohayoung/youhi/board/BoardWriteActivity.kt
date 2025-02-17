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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.yoohayoung.youhi.databinding.ActivityBoardWriteBinding
import com.yoohayoung.youhi.messageData
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.FBAuth.Companion.getTime
import com.yoohayoung.youhi.utils.FBAuth.Companion.getUid
import com.yoohayoung.youhi.utils.FBRef
import com.yoohayoung.youhi.utils.RetrofitClient.apiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class News(
    val uid: String ="",
    val content: String = ""
)
class BoardWriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBoardWriteBinding
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
        binding = ActivityBoardWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        category = intent.getStringExtra("category").toString()

        binding.BTNWriteBoard.setOnClickListener {
            val title = binding.titleArea.text.toString()
            val content = binding.contentArea.text.toString()
            val uid = getUid()
            val time = getTime()

            val key = when (category) {
                "board1" -> {
                    val newKey = FBRef.boardRef1.push().key.toString()
                    FBRef.boardRef1.child(newKey).setValue(Board(title, content, uid, time, newKey))
                    newKey
                }
                "board2" -> {
                    val newKey = FBRef.boardRef2.push().key.toString()
                    FBRef.boardRef2.child(newKey).setValue(Board(title, content, uid, time, newKey))
                    newKey
                }
                "board3" -> {
                    val newKey = FBRef.boardRef3.push().key.toString()
                    FBRef.boardRef3.child(newKey).setValue(Board(title, content, uid, time, newKey))
                    newKey
                }
                "board4" -> {
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
                        val nickName = dataSnapshot.getValue(String::class.java)?:"알 수 없음"
                        if (nickName != null) {
                            CoroutineScope(Dispatchers.Main).launch {
                                sendMsgApiRequest(nickName, title)
                            }

                            val newsRef = FBRef.newsRef.child(getCurrentDate()).push() // push를 사용하여 새로운 키 생성
                            val news = News(getUid(), "${nickName}님이 게시글을 작성했습니다.")
                            newsRef.setValue(news)

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

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    suspend fun sendMsgApiRequest(nickName: String, message: String) { //nickName과 친구인 유저들에게 알림 발송(서버에서 친구 필터링)
        val title = "$nickName 님이 게시글을 작성했습니다."
        val request = messageData(name = FBAuth.getUid(), message = message, title = title, type = "board", auther = nickName)
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
