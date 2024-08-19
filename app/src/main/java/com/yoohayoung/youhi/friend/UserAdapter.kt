package com.yoohayoung.youhi.friend

import com.yoohayoung.youhi.databinding.ItemUserBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


class UserAdapter(
    private val users: List<Friend>,
    private val currentUserUid: String,
    private val userActionListener: UserActionListener,
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(friend: Friend) {
            binding.friend = friend
            binding.executePendingBindings()

            when {
                friend.uid == currentUserUid -> {
                    binding.btnUserAction.text = "나"
                    binding.btnUserAction.isEnabled = false
                }
                friend.isFriend -> {
                    binding.btnUserAction.text = "친구"
                    binding.btnUserAction.isEnabled = false
                }
                friend.isPendingRequest -> {
                    binding.btnUserAction.text = "요청됨"
                }
                else -> {
                    binding.btnUserAction.text = "친구 요청"
                }
            }

            // 버튼 클릭 리스너 등록
            binding.btnUserAction.setOnClickListener {
                userActionListener.onUserAction(friend)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val friend = users[position]
        holder.bind(friend)
    }

    override fun getItemCount(): Int {
        return users.size
    }

}

