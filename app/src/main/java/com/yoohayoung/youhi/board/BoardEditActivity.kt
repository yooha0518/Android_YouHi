package com.yoohayoung.youhi.board

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.yoohayoung.youhi.Board
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.databinding.ActivityBoardEditBinding
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.FBRef
import com.yoohayoung.youhi.utils.GlideOptions.Companion.boardImageOptions
import com.yoohayoung.youhi.utils.GlideOptions.Companion.detailImageOptions
import com.yoohayoung.youhi.utils.RetrofitClient.apiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class BoardEditActivity : AppCompatActivity() {
    private lateinit var binding : ActivityBoardEditBinding
    private lateinit var writerUid : String
    private var boardId:String? = null
    private var category :String? = null
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
                    .into(binding.IVBoard)

                imageSelected = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        boardId = intent.getStringExtra("boardId")
        category = intent.getStringExtra("category")

        boardId?.let {
            getBoardData(boardId!!)
            getBoardImage(boardId!!)
        }



        binding.editBtn.setOnClickListener {
            boardId?.let {
                editBoardData(boardId!!)
            }
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

        val intent = Intent(this, BoardListActivity::class.java)
        intent.putExtra("category", category)
        startActivity(intent)

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
            .apply(detailImageOptions)
            .error(R.drawable.plusbtn_blue)
            .into(binding.IVBoard)

//        Log.d("게시글 이미지 로드","http://youhi.tplinkdns.com:4000/${boardId}.jpg 가 업로드 됨")
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