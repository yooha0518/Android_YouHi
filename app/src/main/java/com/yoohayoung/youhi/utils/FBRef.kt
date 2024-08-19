package com.yoohayoung.youhi.utils

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FBRef {
    companion object{
        val database = Firebase.database

        val bookmarkRef = database.getReference("bookmark_list")

        val category1 = database.getReference("contents1")
        val category2 = database.getReference("contents2")

//        val boardRef = database.getReference("board")
        val boardRef1 = database.getReference("board1")
        val boardRef2 = database.getReference("board2")
        val boardRef3 = database.getReference("board3")
        val boardRef4 = database.getReference("board4")
        val boardRef5 = database.getReference("board5")
        val boardRef6 = database.getReference("board6")
        val boardRef7 = database.getReference("board7")

        val userRef = database.getReference("user")

        val commentRef = database.getReference("comment")
    }
}