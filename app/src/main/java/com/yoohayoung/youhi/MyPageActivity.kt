package com.yoohayoung.youhi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.yoohayoung.youhi.auth.UserModel
import com.yoohayoung.youhi.databinding.ActivityMyPageBinding
import com.yoohayoung.youhi.utils.FBAuth.Companion.getUid
import com.yoohayoung.youhi.utils.FBRef

class MyPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_page)

        val uid = getUid()
        loadUserData(uid)
    }

    private fun loadUserData(uid: String) {
        val userRef = FBRef.userRef.child(uid)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(UserModel::class.java)
                    if (user != null) {
                        binding.textViewNickname.text = "닉네임: ${user.nickName}"
                        binding.textViewEmail.text = "이메일: ${user.email}"
                        binding.textViewPoints.text = "포인트: ${user.point}"

                        // Firebase Storage에서 프로필 이미지 로드
                        loadProfileImage(uid)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("MyPageActivity", "loadUserData:onCancelled", databaseError.toException())
            }
        })
    }

    private fun loadProfileImage(uid: String) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val profileImageRef = storageRef.child("$uid.png")

        profileImageRef.downloadUrl.addOnSuccessListener { uri ->
            // Glide를 사용하여 프로필 이미지 로드
            Glide.with(this@MyPageActivity)
                .load(uri)
                .placeholder(R.drawable.default_profile)
                .into(binding.imageViewProfile)
        }.addOnFailureListener { exception ->
            Log.e("MyPageActivity", "Failed to load profile image", exception)
        }
    }


}