package com.yoohayoung.youhi.board

import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.comment.CommentRVAdapter
import com.yoohayoung.youhi.databinding.ActivityBoardInsideBinding
import com.yoohayoung.youhi.messageData
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.FBAuth.Companion.getUid
import com.yoohayoung.youhi.utils.FBRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.OutputStream
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.request.target.Target
import com.yoohayoung.youhi.Board
import com.yoohayoung.youhi.CommentModel
import com.yoohayoung.youhi.utils.GlideOptions
import com.yoohayoung.youhi.utils.GlideOptions.Companion.boardImageOptions
import com.yoohayoung.youhi.utils.GlideOptions.Companion.detailImageOptions
import com.yoohayoung.youhi.utils.GlideOptions.Companion.profileOptions
import com.yoohayoung.youhi.utils.RetrofitClient.apiService
import java.io.FileOutputStream

class BoardInsideActivity : AppCompatActivity(), CommentRVAdapter.CommentActionListener {
    private val TAG = BoardInsideActivity::class.java.simpleName
    private lateinit var binding : ActivityBoardInsideBinding
    private lateinit var boardId:String
    private lateinit var category :String
    private var islike: Boolean = false

    private val commentDataList = mutableListOf<CommentModel>()

    private lateinit var commentAdapter : CommentRVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardInsideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.BTNEditBoard.setOnClickListener {
            showEditDialog()
        }

        boardId = intent.getStringExtra("boardId").toString()
        category = intent.getStringExtra("category").toString()

        Log.d("boardId", "$boardId")
        Log.d("category", "$category")

        getBoardData(boardId)
        getImageData(boardId)
        getLikeListData(boardId)
//        addIdToExistingComments(boardId) //ID가 없는 댓글에 ID 추가 (이전 버전 호환 고려)

        binding.commentBtn.setOnClickListener {
            insertComment(boardId)
        }

        getCommentData(boardId)
        commentAdapter = CommentRVAdapter(commentDataList,this)
        binding.RVComment.layoutManager = LinearLayoutManager(this)
        binding.RVComment.adapter = commentAdapter


        binding.IVBoard.setOnClickListener {
            showImageDialog(boardId)
        }

        binding.IVLike.setOnClickListener {
            if(islike == true){
                //좋아요 해제
                binding.IVLike.setImageResource(R.drawable.icon_unlike)
                val ref = FBRef.likeRef.child(getUid()).child(boardId)
                ref.removeValue()
                    .addOnSuccessListener {
                        Log.d("좋아요 해제","성공")
                        islike = false
                    }
                    .addOnFailureListener {
                        Log.d("좋아요 해제", "실패")
                    }

            }else{
                //좋아요 설정
                binding.IVLike.setImageResource(R.drawable.icon_like)
                val ref = FBRef.likeRef.child(getUid()).child(boardId)
                ref.setValue(category)
                    .addOnSuccessListener { 
                        Log.d("좋아요 설정","성공")
                        islike = true
                    }
                    .addOnFailureListener { 
                        Log.d("좋아요 설정", "실패")
                    }

            }
        }

        // 키보드가 올라왔을 때 자동 스크롤
        binding.SVBoardInside.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            binding.SVBoardInside.getWindowVisibleDisplayFrame(rect)
            val screenHeight =  binding.SVBoardInside.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.15) { // 키보드가 올라온 경우 (키보드가 화면의 15%이상 차지하면 실행)
                binding.SVBoardInside.postDelayed({
                binding.SVBoardInside.fullScroll(View.FOCUS_DOWN)

                // EditText에 포커스를 다시 맞추기
                binding.ETEditComment.requestFocus()
                }, 200) //키보드가 올라오는 속도 맞추기
            }
        }
    }

    override fun onCommentLongClick(position: Int) {
        showEditCommentDialog(position)
    }

    //스크롤뷰에서 리사이클러뷰 아이템을 스크롤하기 위한 height동적설정
    private fun setRecyclerViewHeight(recyclerView: RecyclerView) {
        val adapter = recyclerView.adapter ?: return

        var totalHeight = 0

        for (position in 0 until adapter.itemCount) {
            val viewHolder = adapter.createViewHolder(recyclerView, adapter.getItemViewType(position))
            adapter.onBindViewHolder(viewHolder, position)

            // 아이템의 높이 계산
            viewHolder.itemView.measure(
                View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.UNSPECIFIED
            )

            val itemHeight = viewHolder.itemView.measuredHeight

            // 마진 계산 (아이템의 상/하 마진을 포함)
            val layoutParams = viewHolder.itemView.layoutParams as ViewGroup.MarginLayoutParams
            val marginTop = layoutParams.topMargin
            val marginBottom = layoutParams.bottomMargin

            totalHeight += itemHeight + marginTop + marginBottom
        }

        val params = recyclerView.layoutParams
        params.height = totalHeight + recyclerView.paddingTop + recyclerView.paddingBottom
        recyclerView.layoutParams = params
        recyclerView.requestLayout()
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
        val commentId = commentDataList[position].id  // 수정할 댓글 ID
        val currentComment = commentDataList[position]

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_comment, null)
        val editText = dialogView.findViewById<EditText>(R.id.et_edit_comment)
        val btnSave = dialogView.findViewById<Button>(R.id.btn_save)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btn_delete = dialogView.findViewById<Button>(R.id.btn_delete)

        editText.setText(currentComment.comment)

        // 다이얼로그 생성
        val dialog = Dialog(this)
        dialog.setContentView(dialogView)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//        dialog.setCancelable(false)  // 다이얼로그 바깥 터치로 닫히지 않도록 설정

        // 저장 버튼 클릭 리스너
        btnSave.setOnClickListener {
            val updatedComment = editText.text.toString().trim()
            if (updatedComment.isNotEmpty()) {
                FBRef.commentRef.child(boardId)
                    .child(commentId)
                    .child("comment")
                    .setValue(updatedComment)
                    .addOnSuccessListener {
                        // 데이터 리스트 및 어댑터 업데이트
                        commentDataList[position] = commentDataList[position].copy(comment = updatedComment)
                        commentAdapter.notifyItemChanged(position)
                        Toast.makeText(this, "댓글 수정 완료", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "댓글 수정 실패", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "수정할 댓글 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }


        // 삭제 버튼 클릭 리스너
        btn_delete.setOnClickListener {
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

            dialog.dismiss()
        }
        // 취소 버튼 클릭 리스너
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        // 다이얼로그 표시
        dialog.show()
    }

    private fun getCommentData(boardId : String){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                commentDataList.clear()
                for (dataModel in dataSnapshot.children) {
                    val item = dataModel.getValue(CommentModel::class.java)
                    commentDataList.add(item!!)
                }
                commentAdapter.notifyDataSetChanged()

                setRecyclerViewHeight(binding.RVComment)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.commentRef.child(boardId).addValueEventListener(postListener)
    }

    private fun insertComment(boardId: String) {
        val commentText = binding.ETEditComment.text.toString().trim()

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

//                getCommentData(boardId)
            }


            Toast.makeText(this, "댓글 입력 완료", Toast.LENGTH_SHORT).show()
            binding.ETEditComment.setText("")

            // 닉네임을 가져와서 sendMsgApiRequest 호출
            FBRef.userRef.child(getUid()).child("nickName")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val nickName = dataSnapshot.getValue(String::class.java)

                        // 게시글의 작성자 UID 가져오기
                        val postRef = FBRef.database.getReference("$category/$boardId/uid")

                        Log.d("postRef", "$postRef")

                        postRef.get().addOnSuccessListener { postSnapshot ->
                            val postUid = postSnapshot.getValue(String::class.java)

                            if (nickName != null && postUid != null) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    Log.d("sendMsgApiRequest", "nickName: $nickName, 댓글: $commentText, postUid: $postUid")

                                    sendMsgApiRequest(nickName, commentText, postUid)
                                }
                            } else {
                                Log.e("sendMsgApiRequest", "닉네임 또는 게시글 작성자 UID를 찾을 수 없습니다.")
                            }
                        }.addOnFailureListener { exception ->
                            Log.e("sendMsgApiRequest", "게시글 작성자 UID를 가져오는 데 실패했습니다.", exception)
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

    private fun getImageData(boardId: String) {
        // 액티비티가 종료되지 않았는지 확인
        if (!isDestroyed) {
            Log.d("getImageData", "액티비티 종료 안됨")
            Glide.with(this)
                .load("http://youhi.tplinkdns.com:4000/${boardId}.jpg")
                .apply(boardImageOptions)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        // 이미지 로드 실패 시 IVBoard 높이를 0dp로 설정
                        binding.IVBoard.layoutParams.height = 0
                        binding.IVBoard.requestLayout()
                        return false
                    }

                    override fun onResourceReady( // 이미지 로드 성공 시
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .into(binding.IVBoard)
        }
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
            "board1" -> FBRef.boardRef1.child(boardId).addValueEventListener(postListener)
            "board2" -> FBRef.boardRef2.child(boardId).addValueEventListener(postListener)
            "board3" -> FBRef.boardRef3.child(boardId).addValueEventListener(postListener)
            "board4" -> FBRef.boardRef4.child(boardId).addValueEventListener(postListener)
            else -> Log.e("BoardInsideActivity", "카테고리가 없습니다.")
        }
    }

    private fun getLikeListData(boardId: String) {
        val uid = getUid()
        val ref = FBRef.database.getReference("like_list/$uid/$boardId")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // boardId가 key이므로 존재 여부만 확인
                    binding.IVLike.setImageResource(R.drawable.icon_like)
                    islike = true
                    Log.d("getLikeListData", "Like exists: $boardId")
                } else {
                    binding.IVLike.setImageResource(R.drawable.icon_unlike)
                    islike = false
                    Log.d("getLikeListData", "No like found for boardId: $boardId")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database error: ${error.message}")
            }
        })
    }

    private fun loadProfileImage(uid: String) {
        Glide.with(this)
            .load("http://youhi.tplinkdns.com:4000/${uid}.jpg")
            .apply(profileOptions)
            .into(binding.IVProfile)
    }

    private fun showEditDialog(){
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.edit_board_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)


        val alertDialog = mBuilder.show()
        alertDialog.findViewById<Button>(R.id.editBtn)?.setOnClickListener{
            val intent = Intent(this, BoardEditActivity::class.java)

            intent.putExtra("boardId",boardId)
            intent.putExtra("category", category)

            alertDialog.dismiss()
            startActivity(intent)
        }
        alertDialog.findViewById<Button>(R.id.removeBtn)?.setOnClickListener{
            if(category.equals("board1")){
                FBRef.boardRef1.child(boardId).removeValue() //게시글 삭제
            }else if(category.equals("board2")){
                FBRef.boardRef2.child(boardId).removeValue() //게시글 삭제
            }else if(category.equals("board3")){
                FBRef.boardRef3.child(boardId).removeValue() //게시글 삭제
            }else if(category.equals("board4")){
                FBRef.boardRef4.child(boardId).removeValue() //게시글 삭제
            }else{
                Log.e("error", "category가 없습니다")
            }

            //like_list 삭제
            val ref = FBRef.likeRef.child(getUid()).child(boardId)
            ref.removeValue()
                .addOnSuccessListener {
                    Log.d("좋아요 해제","성공")
                    islike = false
                }
                .addOnFailureListener {
                    Log.d("좋아요 해제", "실패")
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
        val alertDialog = mBuilder.show()
        val imageDownBtn = alertDialog.findViewById<ImageView>(R.id.imageDownBtn)
        val IV_board = alertDialog.findViewById<ImageView>(R.id.dialog_imageArea)

        val imageUrl = "http://youhi.tplinkdns.com:4000/${boardId}.jpg"

        if (IV_board != null) {
            Glide.with(this)
                .load(imageUrl)
                .apply(detailImageOptions)
                .into(IV_board)
        }

        imageDownBtn?.setOnClickListener {
            downloadAndSaveImage(this, imageUrl, "${boardId}.png")
        }
    }

    // Glide를 이용해 이미지 다운로드 후 저장
    private fun downloadAndSaveImage(context: Context, imageUrl: String, fileName: String) {
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .apply(detailImageOptions)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val file = File(context.cacheDir, fileName)
                    try {
                        val outputStream = FileOutputStream(file)
                        resource.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        outputStream.flush()
                        outputStream.close()

                        // 저장 실행
                        val savedUri = saveImageToGallery(context, file, fileName)
                        if (savedUri != null) {
                            Toast.makeText(context, "이미지 저장 완료", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "이미지 저장 실패", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(context, "파일 저장 오류", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // 필요 시 처리 가능
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    Toast.makeText(context, "이미지 다운로드 실패", Toast.LENGTH_SHORT).show()
                }
            })
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

    suspend fun sendMsgApiRequest(nickName: String, message: String, postUid: String) {
        val title = "$nickName 님이 댓글을 작성했습니다."
        val request = messageData(name = getUid(), message = message, title = title, type = "comment", auther = postUid )
        try {
            Log.d("sendMsgApiRequest", "nickName: $nickName, message: $message, auther: $postUid")
            val apiResponse = apiService.sendMsg(request)
            Log.d("apiResponse", apiResponse.toString())
        } catch (e: Exception) {
            Log.e("apiResponse", "Error", e)
        }
    }
}