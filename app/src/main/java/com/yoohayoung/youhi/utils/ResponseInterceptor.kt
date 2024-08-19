package com.yoohayoung.youhi.utils
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class ResponseInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        //네트워크 요청을 가로챘지만, Response 를 파싱해야 하기 때문에 그대로 요청을 진행함
        val response = chain.proceed(chain.request())
        Log.d("intercept", response.toString())


        return response;
    }


}