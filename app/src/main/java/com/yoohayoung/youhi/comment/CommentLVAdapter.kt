package com.yoohayoung.youhi.comment

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.utils.FBAuth

class CommentRVAdapter(private val commentList: MutableList<CommentModel>) : RecyclerView.Adapter<CommentRVAdapter.CommentViewHolder>() {

    interface OnItemClickListener {
        fun onEditClick(position: Int)
        fun onDeleteClick(position: Int)
    }

    private var listener: OnItemClickListener? = null

    // 리스너 설정 메서드
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }


    // ViewHolder 클래스 정의
    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val comment: TextView = itemView.findViewById(R.id.TV_comment)
        val time: TextView = itemView.findViewById(R.id.TV_time)
        val nickname: TextView = itemView.findViewById(R.id.TV_nickName)
        val BTN_edit_comment: TextView = itemView.findViewById(R.id.BTN_edit_comment)
        val BTN_delete_comment: TextView = itemView.findViewById(R.id.BTN_delete_comment)
    }

    // 아이템의 개수 반환
    override fun getItemCount(): Int {
        return commentList.size
    }

    // ViewHolder를 생성하여 반환
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment_list, parent, false)
        return CommentViewHolder(view)
    }

    // ViewHolder에 데이터를 바인딩
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        holder.comment.text = comment.comment
        holder.time.text = comment.commentCreatedTime

        // 닉네임을 비동기적으로 가져와서 설정
        FBAuth.getNickName(comment.uid) { nickName ->
            holder.nickname.text = nickName
        }

        val myUid = FBAuth.getUid()
        val writerUid = comment.uid

        Log.d("myUID:",myUid)
        Log.d("writerUid:",writerUid)

        if (myUid == writerUid) {
            holder.BTN_edit_comment.isVisible = true
            holder.BTN_delete_comment.isVisible = true
        }


        // BTN_comment_edit 버튼 클릭 리스너 설정
        holder.BTN_edit_comment.setOnClickListener {
            // 클릭된 위치의 인덱스를 리스너에 전달
            listener?.onEditClick(position)
        }

        // BTN_comment_delete 버튼 클릭 리스너 설정
        holder.BTN_delete_comment.setOnClickListener {
            listener?.onDeleteClick(position)
        }
    }
}
