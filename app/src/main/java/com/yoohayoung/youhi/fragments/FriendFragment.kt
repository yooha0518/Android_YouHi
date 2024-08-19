package com.yoohayoung.youhi.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.auth.LoginActivity
import com.yoohayoung.youhi.databinding.FragmentFriendBinding
import com.yoohayoung.youhi.friend.Friend
import com.yoohayoung.youhi.friend.FriendAdapter
import com.yoohayoung.youhi.friend.FriendSearchActivity
import com.yoohayoung.youhi.friend.UserAdapter
import com.yoohayoung.youhi.utils.FBAuth

class FriendFragment : Fragment() {

    private lateinit var binding: FragmentFriendBinding
    private lateinit var friendAdapter : FriendAdapter
    private lateinit var pendingFriendAdapter : FriendAdapter
    private var isShowingPendingRequests: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_friend,container,false)

        friendAdapter = FriendAdapter(mutableListOf())
        pendingFriendAdapter = FriendAdapter(mutableListOf())

        binding.RVFriendList.adapter = friendAdapter
        binding.RVFriendRequestsList.adapter = pendingFriendAdapter

        // LayoutManager 설정
        binding.RVFriendList.layoutManager = LinearLayoutManager(context)
        binding.RVFriendRequestsList.layoutManager = LinearLayoutManager(context)

        loadPendingFriendRequests()
        loadFriends()



        // Toggle button to switch between friends list and pending requests
        binding.BTNFriendRes.setOnClickListener {
            toggleView()
        }

        binding.BTNFriendSearch.setOnClickListener {
            val intent = Intent(context, FriendSearchActivity::class.java)
            startActivity(intent)
        }

        binding.homeTap.setOnClickListener{
            it.findNavController().navigate(R.id.action_friendFragment_to_homeFragment)
        }
        binding.bookmarkTap.setOnClickListener{
            it.findNavController().navigate(R.id.action_friendFragment_to_bookmarkFragment)
        }
        binding.talkTap.setOnClickListener{
            it.findNavController().navigate(R.id.action_friendFragment_to_talkFragment)
        }
        binding.storeTap.setOnClickListener{
            it.findNavController().navigate(R.id.action_friendFragment_to_storeFragment)
        }

        return binding.root
    }

    private fun loadPendingFriendRequests() {
        val database = FirebaseDatabase.getInstance().reference
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Log.d("FriendFragment2", "loadPendingFriendRequests")

        // 현재 사용자가 요청을 받은 사람으로서, 요청을 보낸 사람들의 UID를 조회
        database.child("friendRequests").orderByChild(currentUserId).equalTo("pending")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val pendingList = mutableListOf<Friend>()

                    // 데이터가 없으면 로그 출력 후 종료
                    if (!dataSnapshot.exists()) {
                        Log.d("FriendFragment2", "No request friends found")
                        return
                    }

                    // 데이터가 있을 경우 루프 실행
                    for (snapshot in dataSnapshot.children) {
                        val senderUid = snapshot.key ?: continue  // 요청을 보낸 사용자의 UID

                        // 요청을 보낸 유저의 정보를 가져오기 위해 'user' 경로에서 데이터 읽기
                        database.child("user").child(senderUid)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(senderSnapshot: DataSnapshot) {
                                    val nickName = senderSnapshot.child("nickName").value.toString()
                                    val email = senderSnapshot.child("email").value.toString()
                                    Log.d("FriendFragment2", "Sender: $senderUid, NickName: $nickName")

                                    // 요청한 친구 목록에 추가
                                    pendingList.add(Friend(senderUid, nickName,email,false,true,false))

                                    // 어댑터에 변경 사항 알리기

                                    pendingFriendAdapter = FriendAdapter(pendingList).apply {
                                        setOnItemClickListener { friend ->
                                            Log.d("FriendFragment", "Friend clicked: ${friend.uid}")

                                            // 친구 요청 수락 처리
                                            database.child("user").child(currentUserId).child("friends").child(friend.uid).setValue(true)
                                            database.child("user").child(friend.uid).child("friends").child(currentUserId).setValue(true)

                                            // friendRequests에서 요청 제거
                                            database.child("friendRequests").child(friend.uid).child(currentUserId).removeValue()
                                        }
                                    }
                                    binding.RVFriendRequestsList.adapter = pendingFriendAdapter
                                    pendingFriendAdapter.notifyDataSetChanged()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("FriendFragment2", "Failed to load user info", error.toException())
                                }
                            })
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("FriendFragment2", "Failed to load friend requests", databaseError.toException())
                }
            })
    }

    private fun loadFriends() {
        val database = FirebaseDatabase.getInstance().reference
        val currentUserId = FBAuth.getUid()

        database.child("user").child(currentUserId).child("friends")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val friendsList = mutableListOf<Friend>()

                    if (!dataSnapshot.exists()) {
                        Log.d("FriendFragment1", "No friends found")
                        return
                    }

                    for (snapshot in dataSnapshot.children) {
                        val friendUid = snapshot.key ?: continue

                        database.child("user").child(friendUid)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(friendSnapshot: DataSnapshot) {
                                    val nickName = friendSnapshot.child("nickName").value.toString()
                                    val email = friendSnapshot.child("email").value.toString()
                                    val isFriend = friendSnapshot.child("friends").child(currentUserId).exists()

                                    friendsList.add(Friend(friendUid, nickName, email, isFriend, false, false))

                                    // 친구 목록이 모두 로드된 후 어댑터에 변경 사항 적용
                                    if (friendsList.size == dataSnapshot.childrenCount.toInt()) {
                                        friendAdapter = FriendAdapter(friendsList).apply {
                                            setOnItemClickListener { friend ->
                                                Log.d("FriendFragment", "click/")
                                                // 서로 친구 삭제
                                                database.child("user").child(currentUserId).child("friends").child(friend.uid).removeValue()
                                                database.child("user").child(friend.uid).child("friends").child(currentUserId).removeValue()
                                            }
                                        }
                                        binding.RVFriendList.adapter = friendAdapter
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("FriendFragment1", "Failed to load friend info", error.toException())
                                }
                            })
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("FriendFragment1", "Failed to load friends", databaseError.toException())
                }
            })
    }

    private fun toggleView() {
        if (isShowingPendingRequests) {
            // Show friends list
            binding.RVFriendList.visibility = View.VISIBLE
            binding.RVFriendRequestsList.visibility = View.GONE
            binding.BTNFriendRes.text = "받은 친구 요청 보기"
        } else {
            // Show pending friend requests
            binding.RVFriendList.visibility = View.GONE
            binding.RVFriendRequestsList.visibility = View.VISIBLE
            binding.BTNFriendRes.text = "친구 목록 보기"
        }
        isShowingPendingRequests = !isShowingPendingRequests
    }



}