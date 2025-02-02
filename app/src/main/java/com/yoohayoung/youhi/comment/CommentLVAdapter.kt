package com.yoohayoung.youhi.comment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.utils.FBAuth

class CommentRVAdapter(private val commentList: MutableList<CommentModel>) : RecyclerView.Adapter<CommentRVAdapter.CommentViewHolder>() {

    interface OnItemClickListener {
        fun onEditClick(position: Int)
    }

    private var listener: OnItemClickListener? = null

    // 리스너 설정 메서드
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
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
        Glide.with(holder.itemView.context)
            .load(profileImageUrl)
            .placeholder(R.drawable.default_profile) // 기본 이미지 설정 가능
            .error(R.drawable.default_profile) // 에러 시 표시할 이미지
            .diskCacheStrategy(DiskCacheStrategy.NONE) // 디스크 캐시 사용 안 함
            .skipMemoryCache(true) // 메모리 캐시 사용 안 함
            .into(holder.IV_profile)

        val myUid = FBAuth.getUid()
        val writerUid = comment.uid

//        Log.d("myUID:",myUid)
//        Log.d("writerUid:",writerUid)

        if (myUid == writerUid) {
            // 아이템 롱 클릭 리스너 추가
            holder.itemView.setOnLongClickListener {
                listener?.onEditClick(position)
                true // 이벤트 소비
            }
        }
    }
}
