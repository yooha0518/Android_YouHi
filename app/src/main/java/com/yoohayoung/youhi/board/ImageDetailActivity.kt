package com.yoohayoung.youhi.board

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.yoohayoung.youhi.R

class ImageDetailActivity : AppCompatActivity() {

    private lateinit var imageKey:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)


        //아래의 방법은 게시글의 id로 데이터를 가져오는 방식
        imageKey = intent.getStringExtra("key").toString()


    }
}