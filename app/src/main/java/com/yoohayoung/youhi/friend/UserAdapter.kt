package com.yoohayoung.youhi.friend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.databinding.ItemUserBinding
import com.yoohayoung.youhi.utils.GlideOptions.Companion.profileOptions
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(
    private val users: List<Friend>,
    private val currentUserUid: String,
    private val userActionListener: UserActionListener,
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    interface UserActionListener {
        fun onUserAction(friend: Friend)
    }

    class UserViewHolder(binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        val TV_name:TextView = binding.TVName
        val TV_nickName:TextView = binding.TVNickName
        val BTN_friendAction:TextView = binding.BTNFriendAction
        val IV_profile: CircleImageView = binding.IVProfile
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val friend = users[position]

        holder.TV_name.text = friend.name
        holder.TV_nickName.text = friend.nickName

        // 프로필 이미지를 Glide를 사용하여 로드
        Glide.with(holder.IV_profile.context)
            .load("http://youhi.tplinkdns.com:4000/${friend.uid}.jpg")
            .apply(profileOptions)
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


