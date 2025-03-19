package com.yoohayoung.youhi.utils

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FBRef {
    companion object{
        val database = Firebase.database

        val boardRef1 = database.getReference("board1")
        val boardRef2 = database.getReference("board2")
        val boardRef3 = database.getReference("board3")
        val boardRef4 = database.getReference("board4")

        val contentsRef1 = database.getReference("contents1")
        val contentsRef2 = database.getReference("contents2")
        val userRef = database.getReference("user")
        val commentRef = database.getReference("comment")
        val eventsRef = database.getReference("events")
        val likeRef = database.getReference("like_list")
        val newsRef = database.getReference("news")

    }
}