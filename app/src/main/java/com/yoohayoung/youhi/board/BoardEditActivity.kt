package com.yoohayoung.youhi.board

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.databinding.ActivityBoardEditBinding
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.FBRef
import com.yoohayoung.youhi.utils.ResponseInterceptor
import com.yoohayoung.youhi.utils.RetrofitClient.apiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class BoardEditActivity : AppCompatActivity() {
    private lateinit var binding : ActivityBoardEditBinding
    private lateinit var boardId:String
    private lateinit var writerUid : String
    private lateinit var category :String

    private var imageSelected: Boolean = false

    // ActivityResultLauncher 정의
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val imageUri = data?.data

            if (imageUri != null) {
                Glide.with(this)
                    .load(imageUri)
                    .into(binding.IVBoard)

                imageSelected = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_board_edit)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_board_edit)

        boardId = intent.getStringExtra("boardId").toString()
        category = intent.getStringExtra("category").toString()

        getBoardData(boardId)
        getBoardImage(boardId)

        binding.editBtn.setOnClickListener {
            editBoardData(boardId)
        }

        binding.IVBoard.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            pickImageLauncher.launch(gallery)
        }

    }

    private fun editBoardData(boardId : String){

        if(category.equals("board1")){
            FBRef.boardRef1
                .child(boardId)
                .setValue(
                    Board(binding.titleArea.text.toString(),
                        binding.contentArea.text.toString(),
                        writerUid,
                        FBAuth.getTime(),
                        boardId)
                )
        }else if(category.equals("board2")){
            FBRef.boardRef2
                .child(boardId)
                .setValue(
                    Board(binding.titleArea.text.toString(),
                        binding.contentArea.text.toString(),
                        writerUid,
                        FBAuth.getTime(),
                        boardId)
                )
        }else if(category.equals("board3")){
            FBRef.boardRef3
                .child(boardId)
                .setValue(
                    Board(binding.titleArea.text.toString(),
                        binding.contentArea.text.toString(),
                        writerUid,
                        FBAuth.getTime(),
                        boardId)
                )
        }else if(category.equals("board4")){
            FBRef.boardRef4
                .child(boardId)
                .setValue(
                    Board(binding.titleArea.text.toString(),
                        binding.contentArea.text.toString(),
                        writerUid,
                        FBAuth.getTime(),
                        boardId)
                )
        }else{
            Log.e("error", "!!!! category가 없습니다")
        }


        // 이미지를 선택한 경우에만 이미지 업로드
        if (imageSelected) {
            CoroutineScope(Dispatchers.Main).launch {
                reqUploadBoardImage(boardId)
            }
        }

        Toast.makeText(this, "수정완료", Toast.LENGTH_LONG).show()

        finish()

    }

    private fun getBoardData(key:String){
        val postListener = object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val dataModel = snapshot.getValue(Board::class.java)
                dataModel?.title?.let { Log.d("Edit title", it) }
                dataModel?.content?.let { Log.d("Edit content", it) }
                dataModel?.uid?.let { Log.d("Edit uid", it) }

                binding.titleArea.setText(dataModel?.title)
                binding.contentArea.setText(dataModel?.content)
                dataModel?.uid?.let { writerUid = dataModel!!.uid }

            }

            override fun onCancelled(databaseError: DatabaseError){
            }
        }

        if(category.equals("board1")){
            FBRef.boardRef1.child(key).addValueEventListener(postListener)
        }else if(category.equals("board2")){
            FBRef.boardRef2.child(key).addValueEventListener(postListener)
        }else if(category.equals("board3")){
            FBRef.boardRef3.child(key).addValueEventListener(postListener)
        }else if(category.equals("board4")){
            FBRef.boardRef4.child(key).addValueEventListener(postListener)
        }else{
            Log.e("getBoardData", "!!!! category가 없습니다")
        }
    }

    private fun getBoardImage(boardId : String){
        Glide.with(this)
            .load("http://youhi.tplinkdns.com:4000/${boardId}.jpg")
            .diskCacheStrategy(DiskCacheStrategy.NONE) // 디스크 캐시 사용 안 함
            .skipMemoryCache(true) // 메모리 캐시 사용 안 함
            .error(R.drawable.plusbtn_blue)
            .into(binding.IVBoard)
        
//        Log.d("게시글 이미지 로드","http://youhi.tplinkdns.com:4000/${boardId}.jpg 가 업로드 됨")
    }

    suspend fun reqUploadBoardImage(boardId: String) {
        withContext(Dispatchers.IO) {
            try {
                val imageView = binding.IVBoard
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