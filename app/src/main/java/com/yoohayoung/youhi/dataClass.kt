package com.yoohayoung.youhi

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

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
    val fieldname: String,
    val originalname: String,
    val encoding: String,
    val mimetype: String, // 파일의 MIME 타입 (예: "image/jpeg")
    val destination: String,
    val filename: String,
    val path: String,
    val size: Long
)

@Root(name = "response", strict = false)
data class ApiResponse(
    @field:Element(name = "protocol", required = false)
    var protocol: String = "",

    @field:Element(name = "code", required = false)
    var code: Int = 0,

    @field:Element(name = "message", required = false)
    var message: String = "",

    @field:Element(name = "url", required = false)
    var url: String = ""
)

data class ContentModel (
    val contentId: String="",
    var title:String ="",
    var imageUrl : String="",
    var webUrl :String = ""
)

data class CommentModel (
    val uid : String = "",
    val comment : String = "",
    val commentCreatedTime : String = "",
    val id:String = ""
)

data class CalendarDay(val day: String, val events: List<EventModel>) // 이벤트 배열로 변경

data class EventModel (
    val eventId: String="",
    val date:String ="",
    val title:String="",
    val private:Boolean=false,
    val uid:String="",
)

data class Board(
    val title:String ="",
    val content : String="",
    val uid:String="",
    val time:String="",
    val boardId : String ="",
    val category: String ="",
)

data class News(
    val uid: String ="",
    val content: String = ""
)

data class LikeData(
    val boardId: String,
    val title: String,
    val category: String
)

data class UserModel(
    val email:String = "",
    val nickName:String = "",
    val point: Int = 0,
    val name: String = ""
)
