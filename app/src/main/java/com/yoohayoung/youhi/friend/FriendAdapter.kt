package com.yoohayoung.youhi.friend

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.databinding.ItemFriendBinding
import com.yoohayoung.youhi.utils.GlideOptions.Companion.profileOptions
import de.hdodenhof.circleimageview.CircleImageView

class FriendAdapter(private val friendsList: List<Friend>,
    private val friendActionListener:FriendActionListener
) :
    RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    interface FriendActionListener{
        fun onFriendAction(friend: Friend)
    }

    class FriendViewHolder(binding: ItemFriendBinding) : RecyclerView.ViewHolder(binding.root) {
        val TV_nickName: TextView = binding.TVNickName
        val BTN_friend_res: Button = binding.BTNFriendRes
        val TV_name: TextView = binding.TVName
        val IV_profile: CircleImageView = binding.IVProfile
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friendsList[position]

        holder.TV_nickName.text = friend.nickName
        holder.TV_name.text = friend.name

        // 프로필 이미지를 Glide를 사용하여 로드
        Glide.with(holder.IV_profile.context)
            .load("http://youhi.tplinkdns.com:4000/${friend.uid}.jpg")
            .apply(profileOptions)
            .into(holder.IV_profile)

        Log.d("reqfriendBind","${friend.nickName}")

        when {
            friend.isFriend -> {
                holder.BTN_friend_res.text = "친구 끊기"
                holder.BTN_friend_res.setTextColor(Color.parseColor("#B8110A")) // 글자 색을 빨간색으로 변경
            }
            else -> {
                holder.BTN_friend_res.text = "요청 수락"
                holder.BTN_friend_res.setTextColor(Color.parseColor("#3139FF")) // 글자 색을 빨간색으로 변경
            }
        }

        // 버튼 클릭 리스너 등록
        holder.BTN_friend_res.setOnClickListener {
            friendActionListener.onFriendAction(friend)
        }
    }

    override fun getItemCount(): Int {
        return friendsList.size
    }

}

