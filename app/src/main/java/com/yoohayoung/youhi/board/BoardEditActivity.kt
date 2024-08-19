package com.yoohayoung.youhi.board

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.databinding.ActivityBoardEditBinding
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.FBRef
import java.io.ByteArrayOutputStream

class BoardEditActivity : AppCompatActivity() {

    private val TAG = BoardEditActivity::class.java.simpleName

    private lateinit var binding : ActivityBoardEditBinding

    private lateinit var key:String
    private lateinit var writerUid : String
    private lateinit var category :String

    private var imageSelected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_board_edit)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_board_edit)

        key = intent.getStringExtra("key").toString()
        category = intent.getStringExtra("category").toString()

        getBoardData(key)
        getImageData(key)

        binding.editBtn.setOnClickListener {
            editBoardData(key)
        }

        binding.imageArea.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 200)
        }

    }

    private fun editBoardData(key : String){

        if(category.equals("category1")){
            FBRef.boardRef1
                .child(key)
                .setValue(
                    BoardModel(binding.titleArea.text.toString(),
                        binding.contentArea.text.toString(),
                        writerUid,
                        FBAuth.getTime())
                )
        }else if(category.equals("category2")){
            FBRef.boardRef2
                .child(key)
                .setValue(
                    BoardModel(binding.titleArea.text.toString(),
                        binding.contentArea.text.toString(),
                        writerUid,
                        FBAuth.getTime())
                )
        }else if(category.equals("category3")){
            FBRef.boardRef3
                .child(key)
                .setValue(
                    BoardModel(binding.titleArea.text.toString(),
                        binding.contentArea.text.toString(),
                        writerUid,
                        FBAuth.getTime())
                )
        }else if(category.equals("category4")){
            FBRef.boardRef4
                .child(key)
                .setValue(
                    BoardModel(binding.titleArea.text.toString(),
                        binding.contentArea.text.toString(),
                        writerUid,
                        FBAuth.getTime())
                )
        }else if(category.equals("category5")){
            FBRef.boardRef5
                .child(key)
                .setValue(
                    BoardModel(binding.titleArea.text.toString(),
                        binding.contentArea.text.toString(),
                        writerUid,
                        FBAuth.getTime())
                )
        }else if(category.equals("category6")){
            FBRef.boardRef6
                .child(key)
                .setValue(
                    BoardModel(binding.titleArea.text.toString(),
                        binding.contentArea.text.toString(),
                        writerUid,
                        FBAuth.getTime())
                )
        }else if(category.equals("category7")){
            FBRef.boardRef7
                .child(key)
                .setValue(
                    BoardModel(binding.titleArea.text.toString(),
                        binding.contentArea.text.toString(),
                        writerUid,
                        FBAuth.getTime())
                )
        }else{
            Log.e("error", "!!!! category가 없습니다")
        }


        // 이미지를 선택한 경우에만 이미지 업로드
        if (imageSelected) {
            imageUpload(key)
        }

        Toast.makeText(this, "수정완료", Toast.LENGTH_LONG).show()

        finish()

    }

    private fun getBoardData(key:String){
        val postListener = object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val dataModel = snapshot.getValue(BoardModel::class.java)
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



        if(category.equals("category1")){
            FBRef.boardRef1.child(key).addValueEventListener(postListener)
        }else if(category.equals("category2")){
            FBRef.boardRef2.child(key).addValueEventListener(postListener)
        }else if(category.equals("category3")){
            FBRef.boardRef3.child(key).addValueEventListener(postListener)
        }else if(category.equals("category4")){
            FBRef.boardRef4.child(key).addValueEventListener(postListener)
        }else if(category.equals("category5")){
            FBRef.boardRef5.child(key).addValueEventListener(postListener)
        }else if(category.equals("category6")){
            FBRef.boardRef6.child(key).addValueEventListener(postListener)
        }else if(category.equals("category7")){
            FBRef.boardRef7.child(key).addValueEventListener(postListener)
        }else{
            Log.e("getBoardData", "!!!! category가 없습니다")
        }
    }

    private fun getImageData(key : String){

        // Reference to an image file in Cloud Storage
        val storageReference = Firebase.storage.reference.child(key + ".png")

        // ImageView in your Activity
        val imageViewFromFB = binding.imageArea

        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if(task.isSuccessful) {

                Glide.with(this)
                    .load(task.result)
                    .into(imageViewFromFB)

            } else {

            }
        })

    }

    private  fun imageUpload(key : String){
        val storage = Firebase.storage
        val storageRef = storage.reference
        val mountainsRef = storageRef.child(key+".png")

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



    //이미지 선택했을때 실행되는 메서드
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK && requestCode ==200){
            binding.imageArea.setImageURI(data?.data)
            imageSelected = true // 이미지를 선택한 경우 플래그를 설정
        }
    }
}