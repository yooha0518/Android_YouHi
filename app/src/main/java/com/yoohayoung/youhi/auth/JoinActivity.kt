package com.yoohayoung.youhi.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.yoohayoung.youhi.MainActivity
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.databinding.ActivityJoinBinding
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.FBRef

class JoinActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var binding : ActivityJoinBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_join)

        auth = Firebase.auth

        binding.joinBtn.setOnClickListener {
            var isGoToJoin = true

            val nickName = binding.nickNameArea.text.toString()
            val email = binding.emailArea.text.toString()
            val password1 = binding.passwordArea1.text.toString()
            val password2 = binding.passwordArea2.text.toString()
            val point = 0

            if(email.isEmpty()){
                Toast.makeText(this, "이메일을 입력해주세요",Toast.LENGTH_LONG).show()
                isGoToJoin = false
            }
            if(password1.isEmpty()){
                Toast.makeText(this, "패스워드를 입력해주세요", Toast.LENGTH_LONG).show()
                isGoToJoin = false
            }
            if(password2.isEmpty()){
                Toast.makeText(this, "패스워드 확인을 입력해주세요", Toast.LENGTH_LONG).show()
                isGoToJoin = false
            }
            if(!password1.equals(password2)){
                Toast.makeText(this, "패스워드가 일치하지 않습니다.", Toast.LENGTH_LONG).show()
                isGoToJoin = false
            }
            if(password1.length<6){
                Toast.makeText(this, "패스워드를 6자리 이상으로 입력해주세요", Toast.LENGTH_LONG).show()
                isGoToJoin = false
            }

            if(isGoToJoin){
                auth.createUserWithEmailAndPassword(email, password1)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this,"성공", Toast.LENGTH_LONG).show()

                            Log.d("joinActivity","uid: ${FBAuth.getUid()}")

                            FBRef.userRef.child(FBAuth.getUid()).setValue(UserModel(email,nickName,point))
                            //기존의 Acitivity를 날려서 뒤로가기했을때, 앱이 나가지도록 함
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)

                        } else {
                            Toast.makeText(this,"실패", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }
}