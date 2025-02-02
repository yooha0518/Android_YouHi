package com.yoohayoung.youhi.board

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yoohayoung.youhi.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

data class LikeData(
    val boardId: String,
    val title: String,
    val category: String
)
class LikeRVAdapter(private val boardList: MutableList<LikeData>, private val boardActionListener: BoardActionListener) : RecyclerView.Adapter<LikeRVAdapter.LikeViewholder>() {

    interface BoardActionListener {
        fun onBoardListClick(boardId: String, category: String)
    }

    class LikeViewholder(view: View) : RecyclerView.ViewHolder(view) {
        val LL_title: LinearLayout = itemView.findViewById(R.id.LL_title)
        val TV_title: TextView = itemView.findViewById(R.id.TV_title)
        val LL_like: LinearLayout = itemView.findViewById(R.id.LL_like)
        val IV_like: ImageView = itemView.findViewById(R.id.IV_boardImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeViewholder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_like_list, parent, false)
        return LikeViewholder(view)
    }

    override fun onBindViewHolder(holder: LikeViewholder, position: Int) {
        val likeData = boardList[position]

        holder.TV_title.text = likeData.title

        Glide.with(holder.IV_like.context)
            .load("http://youhi.tplinkdns.com:4000/${likeData.boardId}.jpg")
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .transform(CenterCrop(), RoundedCorners(10))
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    // 이미지 로드 실패 시 TV_title만 보이도록 설정
                    holder.IV_like.visibility = View.GONE
                    holder.LL_title.visibility = View.VISIBLE
                    return false // 계속해서 Glide 실패 처리를 진행하도록
                }


                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    // 이미지 로드 성공 시 IV_like만 보이도록 설정
                    holder.IV_like.visibility = View.VISIBLE
                    holder.LL_title.visibility = View.GONE
                    return false // Glide가 이미 성공적으로 로드한 리소스를 처리함
                }
            })
            .into(holder.IV_like)

        holder.LL_like.setOnClickListener {
            boardActionListener.onBoardListClick(likeData.boardId, likeData.category)
        }
    }

    override fun getItemCount(): Int {
        return boardList.size
    }
}
