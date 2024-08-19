package com.yoohayoung.youhi.friend

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yoohayoung.youhi.R

class FriendAdapter(private val friendsList: List<Friend>) :
    RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    private var onItemClickListener: ((Friend) -> Unit)? = null

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nickNameTextView: TextView = itemView.findViewById(R.id.TV_nickName)
        val BTN_friend_res: Button = itemView.findViewById(R.id.BTN_friend_res)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friendsList[position]

        holder.nickNameTextView.text = friend.nickName

        Log.d("reqfriendBind","$friend.nickName")

        when {
            friend.isFriend -> {
                holder.BTN_friend_res.text = "친구 끊기"
            }
            else -> {
                holder.BTN_friend_res.text = "친구 추가"
            }
        }

        // 버튼 클릭 리스너 등록
        holder.BTN_friend_res.setOnClickListener {
            onItemClickListener?.invoke(friend)
        }
    }

    override fun getItemCount(): Int {
        return friendsList.size
    }

    fun setOnItemClickListener(listener: (Friend) -> Unit) {
        onItemClickListener = listener
    }
}

