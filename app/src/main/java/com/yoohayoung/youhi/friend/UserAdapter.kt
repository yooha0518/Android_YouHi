package com.yoohayoung.youhi.friend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.yoohayoung.youhi.R
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(
    private val users: List<Friend>,
    private val currentUserUid: String,
    private val userActionListener: UserActionListener,
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    interface UserActionListener {
        fun onUserAction(friend: Friend)
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val TV_name:TextView = itemView.findViewById(R.id.TV_name)
        val TV_nickName:TextView = itemView.findViewById(R.id.TV_nickName)
        val BTN_friendAction:TextView = itemView.findViewById(R.id.BTN_friendAction)
        val IV_profile: CircleImageView = itemView.findViewById(R.id.IV_profile)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)

        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val friend = users[position]

        holder.TV_name.text = friend.name
        holder.TV_nickName.text = friend.nickName

        // 프로필 이미지를 Glide를 사용하여 로드
        Glide.with(holder.IV_profile.context)
            .load("http://youhi.tplinkdns.com:4000/${friend.uid}.jpg")
            .error(R.drawable.default_profile) // 로드 실패 시 기본 이미지 로드
            .diskCacheStrategy(DiskCacheStrategy.NONE) // 디스크 캐시 사용 안 함
            .skipMemoryCache(true) // 메모리 캐시 사용 안 함
            .into(holder.IV_profile)

        when {
            friend.uid == currentUserUid -> {
                holder.BTN_friendAction.text = "나"
                holder.BTN_friendAction.isEnabled = false
            }
            friend.isFriend -> {
                holder.BTN_friendAction.text = "친구"
                holder.BTN_friendAction.isEnabled = false
            }
            friend.isPendingRequest -> {
                holder.BTN_friendAction.text = "요청 수락"
            }
            friend.isRequestSent -> {
                holder.BTN_friendAction.text = "요청 됨"
            }
            else -> {
                holder.BTN_friendAction.text = "친구 요청"
            }
        }

        // 버튼 클릭 리스너 등록
        holder.BTN_friendAction.setOnClickListener {
            userActionListener.onUserAction(friend)
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

}


