package com.yoohayoung.youhi.friend

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.yoohayoung.youhi.databinding.ActivityFriendSearchBinding

class FriendSearchActivity : AppCompatActivity(), UserAdapter.UserActionListener {

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

        // 모든 유저 정보 실시간으로 로드
        database.child("user").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val allUsers = mutableListOf<Friend>()  // 중복 방지를 위해 Map 대신 List 사용

                for (snapshot in dataSnapshot.children) {
                    val uid = snapshot.key ?: continue
                    val nickName = snapshot.child("nickName").value.toString()
                    val email = snapshot.child("email").value.toString()
                    val name = snapshot.child("name").value.toString()

                    // 초기 사용자 객체 생성
                    allUsers.add(Friend(uid, nickName, email, name, isFriend = false, isPendingRequest = false, isRequestSent = false))
                }

                if (allUsers.isEmpty()) return

                // 친구 상태와 친구 요청 상태 실시간으로 감지
                database.child("friendRequests").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(requestSnapshot: DataSnapshot) {
                        database.child("user").child(currentUserUid).child("friends")
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(friendSnapshot: DataSnapshot) {
                                    val updatedUsers = allUsers.map { user ->
                                        val uid = user.uid
                                        val isFriend = friendSnapshot.child(uid).exists()
                                        val isRequestSent = requestSnapshot.child(currentUserUid).child(uid).exists()
                                        val isPendingRequest = requestSnapshot.child(uid).child(currentUserUid).exists()

                                        // 사용자 상태 업데이트
                                        user.copy(
                                            isFriend = isFriend,
                                            isRequestSent = isRequestSent,
                                            isPendingRequest = isPendingRequest
                                        )
                                    }

                                    // userList 업데이트
                                    userList.clear()
                                    userList.addAll(updatedUsers)
                                    userAdapter.notifyDataSetChanged()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("FriendSearchActivity", "Failed to check friends", error.toException())
                                }
                            })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FriendSearchActivity", "Failed to check friend requests", error.toException())
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FriendSearchActivity", "Failed to load users", error.toException())
            }
        })
    }


    private fun searchUsers(keyword: String) {
        val database = FirebaseDatabase.getInstance().reference
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        database.child("user")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    userList.clear()
                    for (snapshot in dataSnapshot.children) {
                        val uid = snapshot.key ?: continue
                        val nickName = snapshot.child("nickName").value.toString()
                        val email = snapshot.child("email").value.toString()
                        val name = snapshot.child("name").value.toString()


                        // nickName에 keyword가 포함된 경우만 리스트에 추가
                        if (nickName.contains(keyword, ignoreCase = true)) {
                            val isFriend = snapshot.child("friends").child(uid).exists()
                            val isPendingRequest = snapshot.child("friendRequests").child(currentUserUid).child(uid).exists()
                            val isRequestSent = snapshot.child("friendRequests").child(uid).child(currentUserUid).exists()

                            userList.add(Friend(uid, nickName, email, name, isFriend, isPendingRequest, isRequestSent))
                        }
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
            // 이미 친구인 경우 아무것도 하지 않음
            Log.d("onUserAction","이미 친구입니다.")
        } else if (friend.isPendingRequest) {
            // 요청을 받은 경우 친구 수락
            Log.d("onUserAction","친구 요청을 수락합니다.")
            acceptFriendRequest(friend)
        } else if (friend.isRequestSent) {
            // 이미 요청을 보낸 경우 아무것도 하지 않음
            Log.d("onUserAction","이미 친구요청을 전송했습니다.")
        } else {
            // 친구요청을 전송함
            Log.d("onUserAction","친구 요청을 전송합니다.")
            sendFriendRequest(friend)
        }
    }

    private fun sendFriendRequest(friend: Friend) {
        val database = FirebaseDatabase.getInstance().reference
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        database.child("friendRequests").child(currentUserUid).child(friend.uid).setValue("pending")
            .addOnSuccessListener {
                loadAllUsers()
                Log.d("FriendSearchActivity", "Friend request sent")
            }
            .addOnFailureListener {
                Log.e("FriendSearchActivity", "Failed to send friend request", it)
            }
    }

    private fun acceptFriendRequest(friend: Friend) {
        val database = FirebaseDatabase.getInstance().reference
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Log.d("FriendFragment", "Friend clicked: ${friend.uid}")

        // 친구 요청 수락 처리
        database.child("user").child(currentUserId).child("friends").child(friend.uid).setValue(true)
        database.child("user").child(friend.uid).child("friends").child(currentUserId).setValue(true)

        // friendRequests에서 요청 제거
        database.child("friendRequests").child(friend.uid).child(currentUserId).removeValue()
    }
}
