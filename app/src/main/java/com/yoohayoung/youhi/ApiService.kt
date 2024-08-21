package com.yoohayoung.youhi

import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/sendMsgYouHi") //메시지 전송 API 호출
    suspend fun sendMsg(
        @Body request: messageData,
    ): ApiResponse

    @POST("/saveUserToken") //메시지 전송 API 호출
    suspend fun saveUserToken(
        @Body request: userData,
    ): ApiResponse
}