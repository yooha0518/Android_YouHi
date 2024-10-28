package com.yoohayoung.youhi.board

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.utils.FBAuth


class BoardListLVAdapter(private val boardList: MutableList<Board>,
    private val boardActionListener:BoardActionListener
) : RecyclerView.Adapter<BoardListLVAdapter.BoardViewHolder>() {

    interface BoardActionListener{
        fun onBoardListClick(board:Board)
    }

    private lateinit var itemClickListener: (Int) -> Unit

    // ViewHolder 정의
    class BoardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemLinearLayoutView: LinearLayout = itemView.findViewById(R.id.itemView)
        val title: TextView = itemView.findViewById(R.id.titleArea)
        val time: TextView = itemView.findViewById(R.id.timeArea)
        val TV_nickName: TextView = itemView.findViewById(R.id.TV_nickName)
    }

    // ViewHolder를 생성하는 메서드
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_board_list, parent, false)
        return BoardViewHolder(view)
    }

    // 각 아이템의 데이터를 ViewHolder에 바인딩
    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        val board = boardList[position]

        if (board.uid == FBAuth.getUid()) {
            holder.itemLinearLayoutView.setBackgroundColor(Color.parseColor("#F0E4F3"))
        }

        holder.title.text = board.title
        holder.time.text = board.time

        // 닉네임을 비동기적으로 가져와서 설정
        FBAuth.getNickName(board.uid) { nickName ->
            holder.TV_nickName.text = nickName
        }

        // 아이템 클릭 리스너 설정
        holder.itemView.setOnClickListener {
            boardActionListener.onBoardListClick(board)
        }
    }

    // 아이템 개수 반환
    override fun getItemCount(): Int {
        return boardList.size
    }

    // 클릭 리스너 설정 메서드
    fun setOnItemClickListener(listener: (Int) -> Unit) {
        itemClickListener = listener
    }
}
