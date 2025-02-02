package com.yoohayoung.youhi

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap

interface ApiService {
    @POST("/sendMsgYouHi") //메시지 전송 API 호출
    suspend fun sendMsg(
        @Body request: messageData,
    ): ApiResponse

    @POST("/saveUserToken") //유저의 토큰을 서버에 저장(알림 전송에 필요)
    suspend fun saveUserToken(
        @Body request: userData,
    ): ApiResponse

    @Multipart
    @POST("/postProfileImage") //프로필 이미지 등록
    suspend fun uploadProfileImage(
        @Part("nickname") nickname: RequestBody,
        @Part image: MultipartBody.Part,
    ): uploadImageResponseModel

    @Multipart
    @POST("/postBoardImage") //게시글 이미지 등록
    suspend fun uploadBoardImage(
        @Part("boardID") boardID: RequestBody,
        @Part image: MultipartBody.Part
    ): uploadImageResponseModel

}