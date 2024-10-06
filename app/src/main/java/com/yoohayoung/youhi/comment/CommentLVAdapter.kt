package com.yoohayoung.youhi.comment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.utils.FBAuth

class CommentRVAdapter(private val commentList: MutableList<CommentModel>) : RecyclerView.Adapter<CommentRVAdapter.CommentViewHolder>() {

    // ViewHolder 클래스 정의
    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.titleArea)
        val time: TextView = itemView.findViewById(R.id.timeArea)
        val TV_nickName: TextView = itemView.findViewById(R.id.TV_nickName)
    }

    // 아이템의 개수 반환
    override fun getItemCount(): Int {
        return commentList.size
    }

    // ViewHolder를 생성하여 반환
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_board_list, parent, false)
        return CommentViewHolder(view)
    }

    // ViewHolder에 데이터를 바인딩
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        holder.title.text = comment.commentTitle
        holder.time.text = comment.commentCreatedTime

        // 닉네임을 비동기적으로 가져와서 설정
        FBAuth.getNickName(comment.uid) { nickName ->
            holder.TV_nickName.text = nickName
        }
    }
}
