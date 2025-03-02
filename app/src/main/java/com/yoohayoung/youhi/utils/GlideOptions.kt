package com.yoohayoung.youhi.utils

import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.yoohayoung.youhi.R

class GlideOptions {
    companion object {
        val profileOptions: RequestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE) // 디스크 캐시 사용 안 함
            .skipMemoryCache(true) // 메모리 캐시 사용 안 함
            .override(100, 100) // 이미지 크기 조정
            .error(R.drawable.default_profile) // 에러 시 표시할 이미지
            .fitCenter() // 이미지 비율 맞추기


        val myPageProfileOptions: RequestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE) // 디스크 캐시 사용 안 함
            .skipMemoryCache(true) // 메모리 캐시 사용 안 함
            .override(500, 500) // 이미지 크기 조정
            .error(R.drawable.default_profile) // 에러 시 표시할 이미지
            .fitCenter() // 이미지 비율 맞추기

        val boardImageOptions: RequestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE) // 디스크 캐시 사용 안 함
            .skipMemoryCache(true) // 메모리 캐시 사용 안 함
            .override(500, 500) // 이미지 크기 조정
            .fitCenter() // 이미지 비율 맞추기

        val detailImageOptions: RequestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE) // 디스크 캐시 사용 안 함
            .skipMemoryCache(true) // 메모리 캐시 사용 안 함
            .fitCenter() // 이미지 비율 맞추기
    }
}