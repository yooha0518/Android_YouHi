package com.yoohayoung.youhi.board

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.yoohayoung.youhi.Board
import com.yoohayoung.youhi.News
import com.yoohayoung.youhi.databinding.ActivityBoardWriteBinding
import com.yoohayoung.youhi.messageData
import com.yoohayoung.youhi.utils.FBAuth.Companion.getTime
import com.yoohayoung.youhi.utils.FBAuth.Companion.getUid
import com.yoohayoung.youhi.utils.FBRef
import com.yoohayoung.youhi.utils.GlideOptions.Companion.boardImageOptions
import com.yoohayoung.youhi.utils.RetrofitClient.apiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BoardWriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBoardWriteBinding
    private lateinit var category: String
    private var imageSelected: Boolean = false
    private var selectedImageUri: Uri? = null

    // ActivityResultLauncher 정의
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            selectedImageUri = data?.data

            if (selectedImageUri != null) {
                Glide.with(this)
                    .load(selectedImageUri)
                    .apply(boardImageOptions)
                    .into(binding.imageArea)

                imageSelected = true
            }
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
        val request = messageData(name = getUid(), message = message, title = title, type = "board", auther = nickName)
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
                // URI가 없을 경우 예외 처리
                if (selectedImageUri == null) {
                    Log.e("Upload", "Error: No image selected")
                    return@withContext
                }

                // 임시 파일 생성
                val file = createTempFileFromUri(selectedImageUri!!)

                // 파일을 MultipartBody.Part로 변환
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imageBody = MultipartBody.Part.createFormData("image", "${boardId}.jpg", requestFile)

                val boardIdBody = boardId.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = apiService.uploadBoardImage(boardIdBody, imageBody)

                Log.d("Upload", "Success: ${response.message}")
            } catch (e: Exception) {
                Log.e("Upload", "Error: ${e.message}")
            }
        }
    }

    // URI로부터 임시 파일 생성하는 함수
    private fun createTempFileFromUri(uri: Uri): File {
        val fileName = "${System.currentTimeMillis()}.jpg"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val tempFile = File(storageDir, fileName)

        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }

}
