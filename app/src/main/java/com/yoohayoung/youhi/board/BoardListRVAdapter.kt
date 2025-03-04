package com.yoohayoung.youhi.board

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yoohayoung.youhi.Board
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.GlideOptions
import de.hdodenhof.circleimageview.CircleImageView


class BoardListRVAdapter(private val boardList: List<Board>,
                         private val boardActionListener:BoardActionListener
) : RecyclerView.Adapter<BoardListRVAdapter.BoardViewHolder>() {

    interface BoardActionListener{
        fun onBoardListClick(board:Board)
    }

    // ViewHolder 정의
    class BoardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val TV_title: TextView = itemView.findViewById(R.id.TV_title)
        val TV_time: TextView = itemView.findViewById(R.id.TV_time)
        val TV_nickName: TextView = itemView.findViewById(R.id.TV_nickName)
        val IV_profile: CircleImageView = itemView.findViewById(R.id.IV_profile)
    }

    // ViewHolder를 생성하는 메서드
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_board_list, parent, false)
        return BoardViewHolder(view)
    }

    // 각 아이템의 데이터를 ViewHolder에 바인딩
    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        val board = boardList[position]

        holder.TV_title.text = board.title
        holder.TV_time.text = board.time

        // 프로필 이미지를 Glide를 사용하여 로드
        Glide.with(holder.IV_profile.context)
            .load("http://youhi.tplinkdns.com:4000/${board.uid}.jpg")
            .apply(GlideOptions.profileOptions)
            .into(holder.IV_profile)

        // 닉네임을 비동기적으로 가져와서 설정
        FBAuth.getNickName(board.uid) { nickName ->
            holder.TV_nickName.text = nickName
        }

        // 아이템 클릭 리스너 설정
        holder.itemView.setOnClickListener {
            boardActionListener.onBoardListClick(board)
        }
    }


    override fun getItemCount(): Int {
        return boardList.size
    }
}
