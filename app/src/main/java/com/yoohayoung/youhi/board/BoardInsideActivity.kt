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
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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

        binding.BTNEditBoard.setOnClickListener {
            showEditDialog()
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
        addIdToExistingComments(boardId)

        binding.commentBtn.setOnClickListener {
            insertComment(boardId)
        }

        getCommentData(boardId)
        commentAdapter = CommentRVAdapter(commentDataList)
        binding.RVComment.layoutManager = LinearLayoutManager(this)
        binding.RVComment.adapter = commentAdapter

        binding.IVBoard.setOnClickListener {
            showImageDialog(boardId)
        }

        // 댓글 아이템 클릭 시 수정 다이얼로그 띄우기
        // 클릭 리스너 설정
        commentAdapter.setOnItemClickListener(object : CommentRVAdapter.OnItemClickListener {
            override fun onEditClick(position: Int) {
                showEditCommentDialog(position)
            }

            override fun onDeleteClick(position: Int) {
                showDeleteCommentDialog(position)
            }
        })

    }

    private fun addIdToExistingComments(key: String) { //이전 버전 댓글들은 id가 없어서 추가하는 함수
        FBRef.commentRef.child(key).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (commentSnapshot in dataSnapshot.children) {
                    val commentId = commentSnapshot.key
                    val comment = commentSnapshot.getValue(CommentModel::class.java)

                    // ID가 없는 댓글만 처리
                    if (comment != null && comment.id.isEmpty()) {
                        val updatedComment = comment.copy(id = commentId ?: "")

                        // 댓글 ID 업데이트
                        FBRef.commentRef.child(key).child(commentId!!).setValue(updatedComment)
                            .addOnSuccessListener {
                                Log.d(TAG, "Comment ID updated successfully for commentId: $commentId")
                            }
                            .addOnFailureListener {
                                Log.e(TAG, "Failed to update comment ID for commentId: $commentId", it)
                            }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Failed to read comments", databaseError.toException())
            }
        })
    }


    private fun showEditCommentDialog(position: Int) {
        val commentId = commentDataList[position].id  // 이미 저장된 comment의 id (고유 키)
        val currentComment = commentDataList[position]

        val editText = EditText(this).apply {
            setText(currentComment.comment)
        }

        AlertDialog.Builder(this)
            .setTitle("댓글 수정")
            .setView(editText)
            .setPositiveButton("저장") { _, _ ->
                val updatedComment = editText.text.toString().trim()
                if (updatedComment.isNotEmpty()) {
                    FBRef.commentRef.child(boardId)
                        .child(commentId)       // commentId는 수정할 댓글의 고유 키
                        .child("comment")   // 수정할 필드
                        .setValue(updatedComment) // 수정할 값
                        .addOnSuccessListener {
                            // 데이터 리스트 및 어댑터 업데이트
                            commentDataList[position] = commentDataList[position].copy(comment = updatedComment)
                            commentAdapter.notifyItemChanged(position)
                            Toast.makeText(this, "댓글 수정 완료", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "댓글 수정 실패", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "수정할 댓글 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showDeleteCommentDialog(position: Int) {
        val currentComment = commentDataList[position]
        val commentId = currentComment.id  // 댓글의 ID를 가져옵니다.

        AlertDialog.Builder(this)
            .setTitle("댓글 삭제")
            .setMessage("이 댓글을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                // 댓글 삭제
                FBRef.commentRef.child(boardId)
                    .child(commentId)       // 댓글 ID
                    .removeValue()
                    .addOnSuccessListener {
                        // 데이터 리스트 및 어댑터 업데이트
                        if (position >= 0 && position < commentDataList.size) {
                            commentDataList.removeAt(position)
                            commentAdapter.notifyItemRemoved(position)
                        } else {
                            // 인덱스가 범위를 초과한 경우
                            Log.e("BoardInsideActivity", "Invalid position: $position")
                        }
                        Toast.makeText(this, "댓글 삭제 완료", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "댓글 삭제 실패", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("취소", null)
            .show()
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
            val newCommentRef = FBRef.commentRef.child(boardId).push() // 새로운 댓글 참조 생성
            val commentId = newCommentRef.key // 고유 키 가져오기
            if (commentId != null) {
                FBRef.commentRef
                    .child(boardId)
                    .child(commentId)
                    .setValue(
                        CommentModel(
                            getUid(),
                            commentText,
                            FBAuth.getTime(),
                            id = commentId ?: "" // 고유 키가 없을 경우 빈 문자열
                        )
                    )
            }

            Toast.makeText(this, "댓글 입력 완료", Toast.LENGTH_SHORT).show()
            binding.commentArea.setText("")

            // 닉네임을 가져와서 sendMsgApiRequest 호출
            FBRef.userRef.child(getUid()).child("nickName")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val nickName = dataSnapshot.getValue(String::class.java)
                        if (nickName != null) {
                            CoroutineScope(Dispatchers.Main).launch {
                                Log.d("sendMsgApiRequiest","nickName: $nickName, 댓글: $commentText")
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
        Glide.with(this)
            .load("http://hihihaha.tplinkdns.com:4000/${boardId}.jpg")
            .diskCacheStrategy(DiskCacheStrategy.NONE) // 디스크 캐시 사용 안 함
            .skipMemoryCache(true) // 메모리 캐시 사용 안 함
            .into(binding.IVBoard)
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
                    binding.TVTitle.text = board?.title
                    binding.timeArea.text = board?.time

                    val myUid = getUid()
                    val writerUid = board.uid

                    loadProfileImage(writerUid)

                    if (myUid == writerUid) {
                        binding.BTNEditBoard.isVisible = true
                    }
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

    private fun loadProfileImage(uid: String) {
        Glide.with(this)
            .load("http://hihihaha.tplinkdns.com:4000/${uid}.jpg")
            .into(binding.IVProfile)
    }

    private fun showEditDialog(){
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
            if(category.equals("category1")){
                FBRef.boardRef1.child(boardId).removeValue() //게시글 삭제
            }else if(category.equals("category2")){
                FBRef.boardRef2.child(boardId).removeValue() //게시글 삭제
            }else if(category.equals("category3")){
                FBRef.boardRef3.child(boardId).removeValue() //게시글 삭제
            }else if(category.equals("category4")){
                FBRef.boardRef4.child(boardId).removeValue() //게시글 삭제
            }else{
                Log.e("error", "category가 없습니다")
            }

            // TODO: 이미지 삭제

            Toast.makeText(this, "삭제 완료",Toast.LENGTH_SHORT).show()
            finish()
        }

    }

    private fun showImageDialog(boardId : String) {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.image_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
//            .setTitle("이미지")
        val alertDialog = mBuilder.show()
        val imageDownBtn = alertDialog.findViewById<ImageView>(R.id.imageDownBtn)
        val IV_board = alertDialog.findViewById<ImageView>(R.id.dialog_imageArea)

        if (IV_board != null) {
            Glide.with(this)
                .load("http://hihihaha.tplinkdns.com:4000/${boardId}.jpg")
                .diskCacheStrategy(DiskCacheStrategy.NONE) // 디스크 캐시 사용 안 함
                .skipMemoryCache(true) // 메모리 캐시 사용 안 함
                .into(IV_board)
        }

        if (imageDownBtn != null) {
            imageDownBtn.setOnClickListener {

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
        val request = messageData(name = getUid(), message = message, title = title)
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