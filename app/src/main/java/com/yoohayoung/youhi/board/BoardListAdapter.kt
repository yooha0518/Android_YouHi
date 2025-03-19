package com.yoohayoung.youhi.board

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yoohayoung.youhi.Board
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.databinding.ItemBoardListBinding
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.GlideOptions
import de.hdodenhof.circleimageview.CircleImageView


class BoardListAdapter(private val boardList: List<Board>,
                       private val boardActionListener:BoardActionListener
) : RecyclerView.Adapter<BoardListAdapter.BoardViewHolder>() {

    interface BoardActionListener{
        fun onBoardListClick(board:Board)
    }

    class BoardViewHolder(binding: ItemBoardListBinding) : RecyclerView.ViewHolder(binding.root) { //뷰바인딩 방식
        val TV_title: TextView = binding.TVTitle
        val TV_time: TextView = binding.TVTime
        val TV_nickName: TextView = binding.TVNickName
        val IV_profile: CircleImageView = binding.IVProfile
    }

    // ViewHolder를 생성하는 메서드
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val view = ItemBoardListBinding.inflate(LayoutInflater.from(parent.context),parent,false) //뷰바인딩 방식
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
