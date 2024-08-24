package com.yoohayoung.youhi.board

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.databinding.ActivityBoardListBinding
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.FBRef

class BoardListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBoardListBinding
    private val boardDataList = mutableListOf<BoardModel>()
    private val boardKeyList = mutableListOf<String>()
    private lateinit var boardRVAdapter : BoardListLVAdapter
    private lateinit var category :String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board_list)

        // 데이터 바인딩 초기화
        binding = DataBindingUtil.setContentView(this, R.layout.activity_board_list)

        boardRVAdapter = BoardListLVAdapter(boardDataList)
        binding.boardListView.adapter = boardRVAdapter

        category = intent.getStringExtra("category").toString()
        Log.d("category", "선택된 카테고리: $category")

        binding.boardListView.setOnItemClickListener{parent, view, position, id->
            val intent = Intent(this, BoardInsideActivity::class.java)

            //아래의 방법은 게시글의 id로 데이터를 가져오는 방식
            intent.putExtra("key",boardKeyList[position])
            intent.putExtra("category", category)

            startActivity(intent)
        }

        binding.writeBtn.setOnClickListener{
            val intent = Intent(this, BoardWriteActivity::class.java)
            intent.putExtra("category", category)
            startActivity(intent)
        }

        getFBBoardData()

    }

    private fun getFBBoardData() {
        val currentUserUid = FBAuth.getUid()
        val database = FirebaseDatabase.getInstance().reference
        val friendsList = mutableListOf<String>()

        // 현재 유저의 친구 목록을 가져옵니다.
        database.child("user").child(currentUserUid).child("friends")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    friendsList.clear()
                    for (friendSnapshot in snapshot.children) {
                        val friendUid = friendSnapshot.key ?: continue
                        friendsList.add(friendUid)
                    }

                    // 이후 게시물 데이터를 가져옵니다.
                    fetchBoardData(friendsList, currentUserUid)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("getFBBoardData", "Failed to fetch friends: ${error.message}")
                }
            })
    }

    private fun fetchBoardData(friendsList: List<String>, currentUserUid: String) {
        val postListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                boardDataList.clear()
                for (dataModel in snapshot.children) {
                    val item = dataModel.getValue(BoardModel::class.java)
                    val writer = item?.uid

                    // writer가 친구 목록에 있거나, 현재 사용자 자신인 경우에만 데이터를 추가합니다.
                    if (writer != null && (friendsList.contains(writer) || writer == currentUserUid)) {
                        boardDataList.add(item)
                        boardKeyList.add(dataModel.key.toString()) // 각 게시물의 고유키값이 들어감
                    }
                }

                // 데이터 역순으로 정렬(새로운 글이 작성되면 위로 쌓이도록 하기 위함)
                boardDataList.reverse()
                boardKeyList.reverse()

                boardRVAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("fetchBoardData", "Failed to fetch board data: ${databaseError.message}")
            }
        }

        // 카테고리에 따라 적절한 게시판을 선택합니다.
        when (category) {
            "category1" -> FBRef.boardRef1.addValueEventListener(postListener)
            "category2" -> FBRef.boardRef2.addValueEventListener(postListener)
            "category3" -> FBRef.boardRef3.addValueEventListener(postListener)
            "category4" -> FBRef.boardRef4.addValueEventListener(postListener)
            "category5" -> FBRef.boardRef5.addValueEventListener(postListener)
            "category6" -> FBRef.boardRef6.addValueEventListener(postListener)
            "category7" -> FBRef.boardRef7.addValueEventListener(postListener)
            else -> Log.e("error", "!!!! category가 없습니다")
        }
    }
}