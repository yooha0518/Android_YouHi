package com.yoohayoung.youhi.comment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yoohayoung.youhi.CommentModel
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.board.BoardInsideActivity
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.GlideOptions.Companion.profileOptions

class CommentRVAdapter(private val commentList: MutableList<CommentModel>,private val commentActionListener: CommentActionListener) : RecyclerView.Adapter<CommentRVAdapter.CommentViewHolder>() {

    interface CommentActionListener {
        fun onCommentLongClick(position: Int)
    }

    // ViewHolder 클래스 정의
    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val IV_profile: ImageView = itemView.findViewById(R.id.IV_profile)
        val TV_comment: TextView = itemView.findViewById(R.id.TV_comment)
        val TV_time: TextView = itemView.findViewById(R.id.TV_time)
        val nickname: TextView = itemView.findViewById(R.id.TV_nickName)
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

    // 프로필 이미지 URL 반환
    private fun loadProfileImage(uid: String): String {
        return "http://youhi.tplinkdns.com:4000/${uid}.jpg"
    }

    // ViewHolder에 데이터를 바인딩
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        holder.TV_comment.text = comment.comment
        holder.TV_time.text = comment.commentCreatedTime

        // 닉네임을 비동기적으로 가져와서 설정
        FBAuth.getNickName(comment.uid) { nickName ->
            holder.nickname.text = nickName
        }

        // 프로필 이미지 로드
        val profileImageUrl = loadProfileImage(comment.uid)
        val activity = holder.itemView.context as? BoardInsideActivity
        if (activity != null && !activity.isDestroyed) {
            Glide.with(activity)
                .load(profileImageUrl)
                .apply(profileOptions)
                .into(holder.IV_profile)
        }

        val myUid = FBAuth.getUid()
        val writerUid = comment.uid

        holder.itemView.setOnLongClickListener {
            if (myUid == writerUid) {
                commentActionListener.onCommentLongClick(position)
             }
            true // 이벤트 소비
        }
    }
}
