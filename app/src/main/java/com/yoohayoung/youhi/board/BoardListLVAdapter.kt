package com.yoohayoung.youhi.board

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.utils.FBAuth
import de.hdodenhof.circleimageview.CircleImageView


class BoardListLVAdapter(private val boardList: MutableList<Board>,
    private val boardActionListener:BoardActionListener
) : RecyclerView.Adapter<BoardListLVAdapter.BoardViewHolder>() {

    interface BoardActionListener{
        fun onBoardListClick(board:Board)
    }

    private lateinit var itemClickListener: (Int) -> Unit

    // ViewHolder 정의
    class BoardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemLinearLayoutView: LinearLayout = itemView.findViewById(R.id.itemView)
        val title: TextView = itemView.findViewById(R.id.titleArea)
        val time: TextView = itemView.findViewById(R.id.timeArea)
        val TV_nickName: TextView = itemView.findViewById(R.id.TV_nickName)
        val IV_profile: CircleImageView = itemView.findViewById(R.id.IV_profile)

    }

    // ViewHolder를 생성하는 메서드
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_board_list, parent, false)
        return BoardViewHolder(view)
    }

    // 각 아이템의 데이터를 ViewHolder에 바인딩
    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        val board = boardList[position]

//        // 배경색 설정 (현재 사용자와 게시물 작성자 비교)
//        if (board.uid == FBAuth.getUid()) {
//            holder.itemLinearLayoutView.setBackgroundColor(Color.parseColor("#F0E4F3"))
//        } else {
//            holder.itemLinearLayoutView.setBackgroundColor(Color.TRANSPARENT) // 기본 배경색 설정
//        }

        holder.title.text = board.title
        holder.time.text = board.time

        // 프로필 이미지를 Glide를 사용하여 로드
        Glide.with(holder.IV_profile.context)
            .load("http://youhi.tplinkdns.com:4000/${board.uid}.jpg")
            .error(R.drawable.default_profile) // 로드 실패 시 기본 이미지 로드
            .diskCacheStrategy(DiskCacheStrategy.NONE) // 디스크 캐시 사용 안 함
            .skipMemoryCache(true) // 메모리 캐시 사용 안 함
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


    // 아이템 개수 반환
    override fun getItemCount(): Int {
        return boardList.size
    }
}
