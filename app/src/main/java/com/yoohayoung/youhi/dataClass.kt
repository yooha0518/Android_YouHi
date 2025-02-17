package com.yoohayoung.youhi

data class userData(
    val name:String,
    val token:String
)
data class messageData(
    val name:String,
    val message:String,
    val title:String,
    val type: String,
    val auther: String,
)

data class uploadImageResponseModel(
    val status: String, // 업로드 성공 또는 실패 상태
    val message: String, // 응답 메시지
    val file: FileInfo? = null // 업로드된 파일 정보 (성공 시)
)

data class FileInfo(
    val fieldname: String,       // 필드 이름 (예: "image")
    val originalname: String,    // 원본 파일 이름
    val encoding: String,        // 인코딩 방식 (예: "7bit")
    val mimetype: String,        // 파일의 MIME 타입 (예: "image/jpeg")
    val destination: String,     // 파일이 저장된 디렉토리 경로 (예: "public/")
    val filename: String,        // 저장된 파일 이름 (예: 닉네임으로 지정된 이름)
    val path: String,            // 파일 경로
    val size: Long               // 파일 크기 (바이트 단위)
)

