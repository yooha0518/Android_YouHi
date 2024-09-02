package com.yoohayoung.youhi.friend

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.yoohayoung.youhi.databinding.ActivityFriendSearchBinding

class FriendSearchActivity : AppCompatActivity(), UserActionListener {

    private lateinit var binding: ActivityFriendSearchBinding
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<Friend>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 어댑터 초기화 시 'this'를 UserActionListener로 전달
        userAdapter = UserAdapter(userList, FirebaseAuth.getInstance().currentUser?.uid ?: "", this)
        binding.RVAllUserList.layoutManager = LinearLayoutManager(this)
        binding.RVAllUserList.adapter = userAdapter

        loadAllUsers()

        binding.BTNFriendSearch.setOnClickListener {
            val keyword = binding.ETFirendName.text.toString()
            searchUsers(keyword)
        }
    }

    private fun loadAllUsers() {
        val database = FirebaseDatabase.getInstance().reference
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        database.child("user").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList.clear()
                for (snapshot in dataSnapshot.children) {
                    val uid = snapshot.key ?: continue
                    val nickName = snapshot.child("nickName").value.toString()
                    val email = snapshot.child("email").value.toString()

                    val isFriend = snapshot.child("friends").child(currentUserUid).exists()
                    val isPendingRequest = snapshot.child("friendRequests").child(currentUserUid).child(uid).exists()
                    val isRequestSent = snapshot.child("friendRequests").child(uid).child(currentUserUid).exists()

                    Log.d("loadAllUsers","isFriend: $isFriend")
                    Log.d("loadAllUsers","isPendingRequest: $isPendingRequest")
                    Log.d("loadAllUsers","isRequestSent: $isRequestSent")

                    // Adjust status based on the conditions
                    userList.add(Friend(uid, nickName, email, isFriend, isPendingRequest, isRequestSent))
                }
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FriendSearchActivity", "Failed to load users", error.toException())
            }
        })
    }

    private fun searchUsers(keyword: String) {
        val database = FirebaseDatabase.getInstance().reference
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        database.child("user").orderByChild("nickName").startAt(keyword).endAt(keyword + "\uf8ff")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    userList.clear()
                    for (snapshot in dataSnapshot.children) {
                        val uid = snapshot.key ?: continue
                        val nickName = snapshot.child("nickName").value.toString()
                        val email = snapshot.child("email").value.toString()


                        val isFriend = snapshot.child("friends").child(uid).exists()
                        val isPendingRequest = snapshot.child("friendRequests").child(currentUserUid).child(uid).exists()
                        val isRequestSent = snapshot.child("friendRequests").child(uid).child(currentUserUid).exists()

                        userList.add(Friend(uid, nickName, email, isFriend, isPendingRequest, isRequestSent))
                    }
                    userAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FriendSearchActivity", "Failed to search users", error.toException())
                }
            })
    }

    override fun onUserAction(friend: Friend) {
        if (friend.isFriend) {
            // Already friends - no action
        } else if (friend.isPendingRequest) {
            acceptFriendRequest(friend.uid)
        } else if (friend.isRequestSent) {
            // Do nothing or show a message saying "Request already sent"
        } else {
            sendFriendRequest(friend.uid)
        }
    }

    private fun sendFriendRequest(uid: String) {
        val database = FirebaseDatabase.getInstance().reference
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        database.child("friendRequests").child(currentUserUid).child(uid).setValue("pending")
            .addOnSuccessListener {
                Log.d("FriendSearchActivity", "Friend request sent")
                loadAllUsers()
            }
            .addOnFailureListener {
                Log.e("FriendSearchActivity", "Failed to send friend request", it)
            }
    }

    private fun acceptFriendRequest(uid: String) {
        val database = FirebaseDatabase.getInstance().reference
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        database.child("friendRequests").child(currentUserUid).child(uid).removeValue()
            .addOnSuccessListener {
                database.child("friends").child(currentUserUid).child(uid).setValue(true)
                database.child("friends").child(uid).child(currentUserUid).setValue(true)
                    .addOnSuccessListener {
                        Log.d("FriendSearchActivity", "Friend request accepted")
                        loadAllUsers()
                    }
                    .addOnFailureListener {
                        Log.e("FriendSearchActivity", "Failed to accept friend request", it)
                    }
            }
            .addOnFailureListener {
                Log.e("FriendSearchActivity", "Failed to remove friend request", it)
            }
    }
}
