package com.yoohayoung.youhi.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.yoohayoung.youhi.LikeData
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.board.BoardInsideActivity
import com.yoohayoung.youhi.board.LikeAdapter
import com.yoohayoung.youhi.databinding.FragmentLikeBinding
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.FBRef

class LikeFragment : Fragment(), LikeAdapter.BoardActionListener {

    private var _binding: FragmentLikeBinding? = null
    private val binding get() = _binding!!
    private var likeBoardDataList = mutableListOf<LikeData>()
    lateinit var rvAdapter: LikeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLikeBinding.inflate(inflater,container,false)


        binding.IVMenubarFriend.setOnClickListener{
            it.findNavController().navigate(R.id.action_likeFragment_to_friendFragment)
        }
        binding.IVMenubarHome.setOnClickListener{
            it.findNavController().navigate(R.id.action_likeFragment_to_homeFragment)
        }
        binding.IVMenubarBoard.setOnClickListener{
            it.findNavController().navigate(R.id.action_likeFragment_to_talkFragment)
        }
        binding.IVMenubarCalender.setOnClickListener{
            it.findNavController().navigate(R.id.action_likeFragment_to_calenderFragment)
        }

        getLikeListData()

        rvAdapter = LikeAdapter(likeBoardDataList, this)

        val RV_like: RecyclerView = binding.RVLike
        RV_like.adapter = rvAdapter
        RV_like.layoutManager = GridLayoutManager(requireContext(), 3) //recycler View를 사용하기 위해서 manager를 설정해야함

        return binding.root
    }

    override fun onBoardListClick(boardId: String, category: String) {
        val intent = Intent(context, BoardInsideActivity::class.java)
        intent.putExtra("category", category)
        intent.putExtra("boardId", boardId) // boardId 전달
        startActivity(intent)
    }

    private fun getLikeListData() {
        val uid = FBAuth.getUid()
        val ref = FBRef.database.getReference("like_list/$uid")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    likeBoardDataList.clear() // 기존 리스트 초기화

                    //여러 개의 Firebase 요청을 실행하기 때문에 tasks 리스트를 사용해 모든 요청이 완료될 때 notifyDataSetChanged() 호출
                    val tasks = mutableListOf<Task<DataSnapshot>>() // Firebase 요청을 추적할 리스트

                    for (item in snapshot.children) {
                        val boardId = item.key ?: continue
                        val category = item.getValue(String::class.java) ?: continue

                        // boardId를 이용해 해당 게시글의 title 가져오기
                        val boardRef = FBRef.database.getReference("$category/$boardId/title")
                        val task = boardRef.get() // 비동기 요청 실행
                        tasks.add(task)

                        task.addOnSuccessListener { boardSnapshot ->
                            val title = boardSnapshot.getValue(String::class.java) ?: "제목 없음"
                            val likeItem = LikeData(boardId, title, category)
                            likeBoardDataList.add(likeItem)

                            // 모든 데이터 로드 완료 후 RecyclerView 업데이트
                            if (tasks.all { it.isComplete }) {
                                rvAdapter.notifyDataSetChanged()
//                                Log.d("getLikeListData", "Like List: $likeBoardDataList")
                            }
                        }.addOnFailureListener {
                            Log.e("Firebase", "Failed to load title for $boardId")
                        }
                    }
                } else {
                    Log.d("getLikeListData", "No data found at like_list/$uid")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database error: ${error.message}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}