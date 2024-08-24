package com.yoohayoung.youhi.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FBAuth {
    companion object {
        private lateinit var auth :FirebaseAuth

        fun getUid(): String{
            auth = FirebaseAuth.getInstance()

            return auth.currentUser?.uid.toString()
        }

        fun getTime() :String{
            val currentDateTime = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(currentDateTime)

            return dateFormat
        }

        suspend fun getNickName(): String? = suspendCancellableCoroutine { continuation ->
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                FBRef.userRef.child(uid).child("nickName")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val nickName = dataSnapshot.getValue(String::class.java)
                            continuation.resume(nickName)
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            continuation.resumeWithException(databaseError.toException())
                        }
                    })
            } else {
                continuation.resumeWithException(IllegalStateException("User UID is null"))
            }
        }
    }
}