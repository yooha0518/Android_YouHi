package com.yoohayoung.youhi.board

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.yoohayoung.youhi.ApiService
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.comment.CommentLVAdapter
import com.yoohayoung.youhi.comment.CommentModel
import com.yoohayoung.youhi.databinding.ActivityBoardInsideBinding
import com.yoohayoung.youhi.messageData
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.FBAuth.Companion.getUid
import com.yoohayoung.youhi.utils.FBRef
import com.yoohayoung.youhi.utils.ResponseInterceptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.security.KeyManagementException
import java.util.concurrent.TimeUnit

class BoardInsideActivity : AppCompatActivity() {

    private val TAG = BoardInsideActivity::class.java.simpleName

    private lateinit var binding :ActivityBoardInsideBinding

    private lateinit var apiService: ApiService

    private lateinit var key:String
    private lateinit var category :String

    private val commentDataList = mutableListOf<CommentModel>()

    private lateinit var commentAdapter : CommentLVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_board_inside)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_board_inside)

        binding.boardSettingIcon.setOnClickListener {
            showDialog()
        }

        try {
            // Retrofit 객체 초기화
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("http://hihihaha.tplinkdns.com:5000")
                .client(createOkHttpClient()) //<- Interceptor 를 사용하는 클라이언트 지정
                .addConverterFactory(GsonConverterFactory.create())// json 변환기 추가
                .build()

            // ApiService 인터페이스 구현체 생성
            apiService = retrofit.create(ApiService::class.java)

        } catch (e: KeyManagementException) {
            e.printStackTrace()
        }

        //아래의 방법은 게시글의 id로 데이터를 가져오는 방식
        key = intent.getStringExtra("key").toString()
        category = intent.getStringExtra("category").toString()

        Log.d("게시물의 key", "$key")
        Log.d("게시물의 category", "$category")

        getBoardData(key)
        getImageData(key)

        binding.commentBtn.setOnClickListener {
            insertComment(key)
        }

        getCommentData(key)
        commentAdapter = CommentLVAdapter(commentDataList)
        binding.commentLV.adapter = commentAdapter

        binding.getImageArea.setOnClickListener {
            showImageDialog()
        }



    }

    fun getCommentData(key : String){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                commentDataList.clear()

                for (dataModel in dataSnapshot.children) {
                    val item = dataModel.getValue(CommentModel::class.java)
                    commentDataList.add(item!!)
                }

                commentAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.commentRef.child(key).addValueEventListener(postListener)


    }

    fun insertComment(key: String) {
        val commentText = binding.commentArea.text.toString().trim()

        if (commentText.isNotEmpty()) {
            FBRef.commentRef
                .child(key)
                .push()
                .setValue(
                    CommentModel(
                        commentText,
                        FBAuth.getTime()
                    )
                )

            Toast.makeText(this, "댓글 입력 완료", Toast.LENGTH_SHORT).show()
            binding.commentArea.setText("")

            // 닉네임을 가져와서 sendMsgApiRequest 호출
            FBRef.userRef.child(getUid()).child("nickName")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val nickName = dataSnapshot.getValue(String::class.java)
                        if (nickName != null) {
                            CoroutineScope(Dispatchers.Main).launch {
                                sendMsgApiRequest(nickName, commentText)
                            }
                        } else {
                            Log.e("sendMsgApiRequest", "닉네임을 찾을 수 없습니다.")
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("sendMsgApiRequest", "닉네임을 가져오는 데 실패했습니다.", databaseError.toException())
                    }
                })
        } else {
            Toast.makeText(this, "댓글 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getImageData(key: String){
        // Reference to an image file in Cloud Storage
        val storageReference = Firebase.storage.reference.child(key+".png")

        // ImageView in your Activity
        val imageViewFromFB = binding.getImageArea

        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener{ task ->
            if (task.isSuccessful){
                Glide.with(this)
                    .load(task.result)
                    .into(imageViewFromFB)
            }else{
                Log.d(TAG, "이미지 업로드 실패")

                binding.getImageArea.isVisible = false
            }
        })
    }

    private fun getBoardData(key: String) {
        val postListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val dataModel = snapshot.getValue(BoardModel::class.java)
                val userRef = FBRef.userRef.child(dataModel!!.uid).child("nickName")

                try {
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            val nickName = userSnapshot.getValue(String::class.java)
                            Log.d("uid", dataModel.uid)
                            Log.d("nickName", nickName ?: "null")
                            Log.d("title", dataModel.title)
                            Log.d("content", dataModel.content)

                            binding.usernameArea.text = nickName ?: "Unknown"
                            binding.titleArea.text = dataModel.title
                            binding.contentArea.text = dataModel.content
                            binding.timeArea.text = dataModel.time

                            val myUid = getUid()
                            val writerUid = dataModel.uid

                            if (myUid == writerUid) {
                                binding.boardSettingIcon.isVisible = true
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.e("getBoardData", "Failed to read nickName", databaseError.toException())
                        }
                    })

                } catch (e: Exception) {
                    Log.d(TAG, "이미 삭제됨")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("getBoardData", "Failed to read post", databaseError.toException())
            }
        }

        if (category == "category1") {
            FBRef.boardRef1.child(key).addValueEventListener(postListener)
        } else if (category == "category2") {
            FBRef.boardRef2.child(key).addValueEventListener(postListener)
        } else if (category == "category3") {
            FBRef.boardRef3.child(key).addValueEventListener(postListener)
        } else if (category == "category4") {
            FBRef.boardRef4.child(key).addValueEventListener(postListener)
        } else if (category == "category5") {
            FBRef.boardRef5.child(key).addValueEventListener(postListener)
        } else if (category == "category6") {
            FBRef.boardRef6.child(key).addValueEventListener(postListener)
        } else if (category == "category7") {
            FBRef.boardRef7.child(key).addValueEventListener(postListener)
        } else {
            Log.e("getBoardData", "!!!! category가 없습니다")
        }
    }


    private fun showDialog(){
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("게시글 수정/삭제")

        val alertDialog = mBuilder.show()
        alertDialog.findViewById<Button>(R.id.editBtn)?.setOnClickListener{
            val intent = Intent(this, BoardEditActivity::class.java)

            intent.putExtra("key",key)
            intent.putExtra("category", category)

            alertDialog.dismiss()
            startActivity(intent)
        }
        alertDialog.findViewById<Button>(R.id.removeBtn)?.setOnClickListener{
//            FBRef.boardRef.child(key).removeValue() //게시글 삭제
            if(category.equals("category1")){
                FBRef.boardRef1.child(key).removeValue() //게시글 삭제
            }else if(category.equals("category2")){
                FBRef.boardRef2.child(key).removeValue() //게시글 삭제
            }else if(category.equals("category3")){
                FBRef.boardRef3.child(key).removeValue() //게시글 삭제
            }else if(category.equals("category4")){
                FBRef.boardRef4.child(key).removeValue() //게시글 삭제
            }else if(category.equals("category5")){
                FBRef.boardRef5.child(key).removeValue() //게시글 삭제
            }else if(category.equals("category6")){
                FBRef.boardRef6.child(key).removeValue() //게시글 삭제
            }else if(category.equals("category7")){
                FBRef.boardRef7.child(key).removeValue() //게시글 삭제
            }else{
                Log.e("error", "!!!! category가 없습니다")
            }

            // 이미지 삭제
            val storageReference = Firebase.storage.reference.child("$key.png")
            storageReference.metadata.addOnSuccessListener { metadata ->
                // 메타데이터가 있으면 이미지가 존재하는 것
                storageReference.delete().addOnSuccessListener {
                    Log.d(TAG, "이미지 삭제 성공")
                }.addOnFailureListener {
                    Log.d(TAG, "이미지 삭제 실패")
                }
            }.addOnFailureListener {
                // 메타데이터가 없으면 이미지가 존재하지 않음
                Log.d(TAG, "이미지가 존재하지 않음")
            }

            Toast.makeText(this, "삭제 완료",Toast.LENGTH_SHORT).show()
            finish()
        }

    }

    private fun showImageDialog() {
        val storageReference = Firebase.storage.reference.child("$key.png")

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.image_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
//            .setTitle("이미지")
        val alertDialog = mBuilder.show()
        val imageDownBtn = alertDialog.findViewById<ImageView>(R.id.imageDownBtn)
        val imageViewFromFB = alertDialog.findViewById<ImageView>(R.id.dialog_imageArea)

        storageReference.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (imageViewFromFB != null) {
                    Glide.with(this)
                        .load(task.result)
                        .into(imageViewFromFB)
                }
            } else {
                Log.d(TAG, "이미지 업로드 실패: ${task.exception?.message}")
            }
        }

        if (imageDownBtn != null) {
            imageDownBtn.setOnClickListener {

                if (imageViewFromFB != null) {
                    // 로컬로 이미지 다운받기
                    val localFile = File.createTempFile("images", "png")

                    storageReference.getFile(localFile).addOnSuccessListener {
                        Log.d(TAG, "이미지 다운로드 성공")

                        // Save the downloaded file to the gallery
                        val savedUri = saveImageToGallery(this, localFile, "$key.png")
                        if (savedUri != null) {
                            Toast.makeText(this, "이미지가 갤러리에 저장되었습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "갤러리에 이미지 저장 실패", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { e ->
                        Log.d(TAG, "이미지 다운로드 실패: ${e.message}")
                    }
                } else {
                    Log.d(TAG, "ImageView is null")
                }
            }
        } else {
            Log.d(TAG, "imageDownBtn is null")
        }
    }

    fun saveImageToGallery(context: Context, sourceFile: File, fileName: String): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val resolver = context.contentResolver
        var uri: Uri? = null

        try {
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                val outputStream = resolver.openOutputStream(uri)
                if (outputStream != null) {
                    copyFile(sourceFile, outputStream)
                    outputStream.close()
                } else {
                    resolver.delete(uri, null, null)
                    uri = null
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            if (uri != null) {
                resolver.delete(uri, null, null)
                uri = null
            }
        }

        return uri
    }

    private fun copyFile(sourceFile: File, outputStream: OutputStream) {
        val inputStream = sourceFile.inputStream()
        try {
            inputStream.copyTo(outputStream)
        } finally {
            inputStream.close()
        }
    }

    suspend fun sendMsgApiRequest(nickName: String, message: String) {
        val title = "$nickName 님이 댓글을 작성했습니다."
        val request = messageData(name = nickName, message = message, title = title)
        try {
            Log.d("sendMsgApiRequest", "nickName: $nickName, message: $message")
            val apiResponse = apiService.sendMsg(request)
            Log.d("apiResponse", apiResponse.toString())
        } catch (e: Exception) {
            Log.e("apiResponse", "Error", e)
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

}