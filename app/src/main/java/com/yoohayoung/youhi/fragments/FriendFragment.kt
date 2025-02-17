package com.yoohayoung.youhi.fragments

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.databinding.FragmentFriendBinding
import com.yoohayoung.youhi.friend.Friend
import com.yoohayoung.youhi.friend.FriendAdapter
import com.yoohayoung.youhi.friend.FriendSearchActivity
import com.yoohayoung.youhi.utils.FBAuth

class FriendFragment : Fragment(),FriendAdapter.FriendActionListener {

    private  var _binding: FragmentFriendBinding? = null
    private val binding get() = _binding!!
    private lateinit var friendAdapter : FriendAdapter
    private lateinit var pendingFriendAdapter : FriendAdapter
    private var isShowingPendingRequests: Boolean = false
    private val friendsList = mutableListOf<Friend>()
    private val pendingList = mutableListOf<Friend>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFriendBinding.inflate(inflater, container,false)

        friendAdapter = FriendAdapter(friendsList,this)
        pendingFriendAdapter = FriendAdapter(pendingList,this)


        binding.RVFriendList.adapter = friendAdapter
        binding.RVFriendRequestsList.adapter = pendingFriendAdapter

        // LayoutManager 설정
        binding.RVFriendList.layoutManager = LinearLayoutManager(context)
        binding.RVFriendRequestsList.layoutManager = LinearLayoutManager(context)

        loadPendingFriendRequests()
        loadFriends()

        binding.BTNFriendRes.setOnClickListener {
            toggleView()
        }

        binding.BTNFriendSearch.setOnClickListener {
            val intent = Intent(context, FriendSearchActivity::class.java)
            // 애니메이션 없이 액티비티 시작
            val options = ActivityOptions.makeCustomAnimation(context, 0, 0)
            startActivity(intent, options.toBundle())
        }

        binding.IVMenubarHome.setOnClickListener{
            it.findNavController().navigate(R.id.action_friendFragment_to_homeFragment)
        }
        binding.IVMenubarLike.setOnClickListener{
            it.findNavController().navigate(R.id.action_friendFragment_to_likeFragment)
        }
        binding.IVMenubarBoard.setOnClickListener{
            it.findNavController().navigate(R.id.action_friendFragment_to_talkFragment)
        }
        binding.IVMenubarCalender.setOnClickListener{
            it.findNavController().navigate(R.id.action_friendFragment_to_calenderFragment)
        }

        return binding.root
    }

    override fun onFriendAction(friend: Friend){
        if (friend.isFriend) {
            Log.d("onFriendAction","friend: $friend")
            // 이미 친구인 경우 친구 삭제
            Log.d("onFriendAction","친구를 삭제합니다.")
            remoteFriend(friend)
        } else {
            //친구가 아니면 친구 요청 수락
            Log.d("onFriendAction","친구 요청을 수락합니다.")
            acceptFriendRequest(friend)
        }
    }

    private fun acceptFriendRequest(friend: Friend){
        val database = FirebaseDatabase.getInstance().reference
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Log.d("FriendFragment", "Friend clicked: ${friend.uid}")

        // 친구 요청 수락 처리
        database.child("user").child(currentUserId).child("friends").child(friend.uid).setValue(true)
        database.child("user").child(friend.uid).child("friends").child(currentUserId).setValue(true)

        // friendRequests에서 요청 제거
        database.child("friendRequests").child(friend.uid).child(currentUserId).removeValue()
    }

    private fun remoteFriend(friend:Friend){
        val database = FirebaseDatabase.getInstance().reference
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        // 서로 친구 삭제
        database.child("user").child(currentUserId).child("friends").child(friend.uid).removeValue()
        database.child("user").child(friend.uid).child("friends").child(currentUserId).removeValue()
    }

    private fun loadPendingFriendRequests() {
        val database = FirebaseDatabase.getInstance().reference
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Log.d("FriendFragment2", "loadPendingFriendRequests")

        // 현재 사용자가 요청을 받은 사람으로서, 요청을 보낸 사람들의 UID를 조회
        database.child("friendRequests").orderByChild(currentUserId).equalTo("pending")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    pendingList.clear()
                    friendsList.clear()
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
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(senderSnapshot: DataSnapshot) {
                                    val nickName = senderSnapshot.child("nickName").value.toString()
                                    val email = senderSnapshot.child("email").value.toString()
                                    val name = senderSnapshot.child("name").value.toString()

                                    Log.d("FriendFragment2", "Sender: $senderUid, NickName: $nickName")

                                    // 요청한 친구 목록에 추가
                                    pendingList.add(Friend(senderUid, nickName,email, name,isFriend = false, isPendingRequest = true, isRequestSent = false))

                                    // 어댑터에 변경 사항 알리기
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


        // 새로운 이벤트 리스너 등록
        friendsList.clear()
        pendingList.clear()
        val friendsEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                friendsList.clear() // 중복 방지 위해 초기화
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
                                val name = friendSnapshot.child("name").value.toString()
                                val isFriend = friendSnapshot.child("friends").child(currentUserId).exists()

                                friendsList.add(Friend(friendUid, nickName, email, name, isFriend, isPendingRequest = false, isRequestSent = false))

                                friendAdapter.notifyDataSetChanged()  // UI 업데이트
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
        }

        // 친구 목록에 이벤트 리스너 추가
        database.child("user").child(currentUserId).child("friends").addValueEventListener(friendsEventListener)
    }


    private fun toggleView() {
        if (isShowingPendingRequests) {
            // Show friends list
            binding.RVFriendList.visibility = View.VISIBLE
            binding.RVFriendRequestsList.visibility = View.GONE
            binding.BTNFriendRes.text = "받은 요청"
            binding.TVTitle.text="친구 목록"
        } else {
            // Show pending friend requests
            binding.RVFriendList.visibility = View.GONE
            binding.RVFriendRequestsList.visibility = View.VISIBLE
            binding.BTNFriendRes.text = "친구 목록"
            binding.TVTitle.text="받은 요청 목록"
        }
        isShowingPendingRequests = !isShowingPendingRequests
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}