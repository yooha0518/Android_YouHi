package com.yoohayoung.youhi

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @POST("/sendMsgYouHi") //메시지 전송 API 호출
    suspend fun sendMsg(
        @Body request: messageData,
    ): ApiResponse

    @POST("/saveUserToken") //메시지 전송 API 호출
    suspend fun saveUserToken(
        @Body request: userData,
    ): ApiResponse

    @Multipart
    @POST("/postProfileImage")
    suspend fun uploadProfileImage(
        @Part image: MultipartBody.Part
    ): UploadResponse
}