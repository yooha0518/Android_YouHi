package com.yoohayoung.youhi.contentList

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.webkit.WebView
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.databinding.ActivityContentShowBinding

class ContentShowActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityContentShowBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        val getUrl = intent.getStringExtra("url")

        val webView : WebView = binding.webView
        webView.loadUrl(getUrl.toString())
    }
}