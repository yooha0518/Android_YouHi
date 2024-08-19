package com.yoohayoung.youhi.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.yoohayoung.youhi.MainActivity
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.databinding.ActivityIntroBinding

class IntroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIntroBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_intro)


        binding = DataBindingUtil.setContentView(this,R.layout.activity_intro)

        auth = Firebase.auth

        binding.loginBtn.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }

        binding.joinBtn.setOnClickListener{
            val intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }

        binding.noAccountBtn.setOnClickListener {
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
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