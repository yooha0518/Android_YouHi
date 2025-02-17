package com.yoohayoung.youhi.board

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yoohayoung.youhi.databinding.ActivityBoardListBinding
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.FBRef

class BoardListActivity : AppCompatActivity(),BoardListRVAdapter.BoardActionListener {
    private lateinit var binding: ActivityBoardListBinding
    private val boardDataList = mutableListOf<Board>()
    private lateinit var boardRVAdapter : BoardListRVAdapter
    private lateinit var category :String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        boardRVAdapter = BoardListRVAdapter(boardDataList,this)
        binding.RVBoard.adapter = boardRVAdapter
        binding.RVBoard.layoutManager = LinearLayoutManager(this)

        category = intent.getStringExtra("category").toString()

        binding.writeBtn.setOnClickListener{
            val intent = Intent(this, BoardWriteActivity::class.java)
            intent.putExtra("category", category)
            startActivity(intent)
        }

        getFBBoardData()

    }

    override fun onBoardListClick(board: Board) {
        val intent = Intent(this, BoardInsideActivity::class.java)
        intent.putExtra("category", category)
        intent.putExtra("boardId", board.boardId) // boardId 전달
        startActivity(intent)
    }

    private fun getFBBoardData() {
        val currentUserUid = FBAuth.getUid()
        val database = FirebaseDatabase.getInstance().reference
        val friendsList = mutableListOf<String>()

        // 현재 유저의 친구 목록
        database.child("user").child(currentUserUid).child("friends")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    friendsList.clear()
                    for (friendSnapshot in snapshot.children) {
                        val friendUid = friendSnapshot.key ?: continue
                        friendsList.add(friendUid)
                    }

                    // 이후 게시물 데이터
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
                    val item = dataModel.getValue(Board::class.java)
                    val writer = item?.uid

                    // writer가 친구 목록에 있거나, 현재 사용자 자신인 경우에만 데이터를 추가
                    if (writer != null && (friendsList.contains(writer) || writer == currentUserUid)) {
                        boardDataList.add(item)
                    }

                }

                // 데이터 역순으로 정렬(새로운 글이 작성되면 위로 쌓이도록 하기 위함)
                boardDataList.reverse()

                boardRVAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("fetchBoardData", "Failed to fetch board data: ${databaseError.message}")
            }
        }

        // 카테고리에 따라 적절한 게시판을 선택
        when (category) {
            "board1" -> FBRef.boardRef1.addValueEventListener(postListener)
            "board2" -> FBRef.boardRef2.addValueEventListener(postListener)
            "board3" -> FBRef.boardRef3.addValueEventListener(postListener)
            "board4" -> FBRef.boardRef4.addValueEventListener(postListener)
            else -> Log.e("error", "!!!! category가 없습니다")
        }
    }


}