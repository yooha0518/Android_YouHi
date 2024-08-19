package com.yoohayoung.youhi.friend

data class Friend(
    val uid: String = "",
    val nickName: String = "",
    val email: String = "",
    val isFriend: Boolean = false,
    val isPendingRequest: Boolean = false,
    val isRequestSent: Boolean = false
)