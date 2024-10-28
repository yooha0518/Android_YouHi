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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.yoohayoung.youhi.ApiService
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.comment.CommentModel
import com.yoohayoung.youhi.comment.CommentRVAdapter
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

    private lateinit var boardId:String
    private lateinit var category :String

    private val commentDataList = mutableListOf<CommentModel>()

    private lateinit var commentAdapter : CommentRVAdapter

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
                .baseUrl("http://hihihaha.tplinkdns.com:4000")
                .client(createOkHttpClient()) //<- Interceptor 를 사용하는 클라이언트 지정
                .addConverterFactory(GsonConverterFactory.create())// json 변환기 추가
                .build()

            // ApiService 인터페이스 구현체 생성
            apiService = retrofit.create(ApiService::class.java)

        } catch (e: KeyManagementException) {
            e.printStackTrace()
        }

        //아래의 방법은 게시글의 id로 데이터를 가져오는 방식
        boardId = intent.getStringExtra("boardId").toString()
        category = intent.getStringExtra("category").toString()

        Log.d("게시물의 boardId", "$boardId")
        Log.d("게시물의 category", "$category")

        getBoardData(boardId)
        getImageData(boardId)

        binding.commentBtn.setOnClickListener {
            insertComment(boardId)
        }

        getCommentData(boardId)
        commentAdapter = CommentRVAdapter(commentDataList)
        binding.RVComment.layoutManager = LinearLayoutManager(this)
        binding.RVComment.adapter = commentAdapter

        binding.getImageArea.setOnClickListener {
            showImageDialog()
        }



    }

    private fun getCommentData(boardId : String){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                commentDataList.clear()

                for (dataModel in dataSnapshot.children) {
                    val item = dataModel.getValue(CommentModel::class.java)
                    commentDataList.add(item!!)
                    Log.d("comment",item.toString())
                }

                commentAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.commentRef.child(boardId).addValueEventListener(postListener)


    }

    private fun insertComment(boardId: String) {
        val commentText = binding.commentArea.text.toString().trim()

        if (commentText.isNotEmpty()) {
            FBRef.commentRef
                .child(boardId)
                .push()
                .setValue(
                    CommentModel(
                        getUid(),
                        commentText,
                        FBAuth.getTime()
                    )
                )

            Toast.makeText(this, "댓글 입력 완료", Toast.LENGTH_SHORT).show()
            binding.commentArea.setText("")

            // 닉네임을 가져와서 sendMsgApiRequest 호출 //TODO 댓글 알림 안가는 문제 해결하기
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

    private fun getImageData(boardId: String){
        // Reference to an image file in Cloud Storage
        val storageReference = Firebase.storage.reference.child(boardId+".png")

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
    private fun getBoardData(boardId: String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val board = snapshot.getValue(Board::class.java)
                if (board != null) {

                    // 닉네임을 비동기적으로 가져와서 설정
                    FBAuth.getNickName(board.uid) { nickName ->
                        binding.usernameArea.text = nickName
                    }
                    binding.contentArea.text = board?.content
                    binding.titleArea.text = board?.title
                    binding.timeArea.text = board?.time
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("BoardInsideActivity", "Failed to load board data: ${databaseError.message}")
            }
        }

        // 카테고리에 따라 적절한 레퍼런스를 선택합니다.
        when (category) {
            "category1" -> FBRef.boardRef1.child(boardId).addListenerForSingleValueEvent(postListener)
            "category2" -> FBRef.boardRef2.child(boardId).addListenerForSingleValueEvent(postListener)
            "category3" -> FBRef.boardRef3.child(boardId).addListenerForSingleValueEvent(postListener)
            "category4" -> FBRef.boardRef4.child(boardId).addListenerForSingleValueEvent(postListener)
            else -> Log.e("BoardInsideActivity", "Invalid category!")
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

            intent.putExtra("boardId",boardId)
            intent.putExtra("category", category)

            alertDialog.dismiss()
            startActivity(intent)
        }
        alertDialog.findViewById<Button>(R.id.removeBtn)?.setOnClickListener{
//            FBRef.boardRef.child(boardId).removeValue() //게시글 삭제
            if(category.equals("category1")){
                FBRef.boardRef1.child(boardId).removeValue() //게시글 삭제
            }else if(category.equals("category2")){
                FBRef.boardRef2.child(boardId).removeValue() //게시글 삭제
            }else if(category.equals("category3")){
                FBRef.boardRef3.child(boardId).removeValue() //게시글 삭제
            }else if(category.equals("category4")){
                FBRef.boardRef4.child(boardId).removeValue() //게시글 삭제
            }else{
                Log.e("error", "!!!! category가 없습니다")
            }

            // 이미지 삭제
            val storageReference = Firebase.storage.reference.child("$boardId.png")
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
        val storageReference = Firebase.storage.reference.child("$boardId.png")

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
                        val savedUri = saveImageToGallery(this, localFile, "$boardId.png")
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