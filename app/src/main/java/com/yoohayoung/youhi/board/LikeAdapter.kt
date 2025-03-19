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
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.yoohayoung.youhi.LikeData
import com.yoohayoung.youhi.databinding.ItemLikeListBinding
import com.yoohayoung.youhi.utils.GlideOptions.Companion.boardImageOptions
import com.yoohayoung.youhi.utils.GlideOptions.Companion.likeImageOptions


class LikeAdapter(private val boardList: List<LikeData>, private val boardActionListener: BoardActionListener) : RecyclerView.Adapter<LikeAdapter.LikeViewHolder>() {

    interface BoardActionListener {
        fun onBoardListClick(boardId: String, category: String)
    }

    class LikeViewHolder(binding: ItemLikeListBinding): RecyclerView.ViewHolder(binding.root){
        val LL_title: LinearLayout = binding.LLTitle
        val TV_title: TextView = binding.TVTitle
        val LL_like: LinearLayout = binding.LLLike
        val IV_boardImage: ImageView = binding.IVBoardImage
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeViewHolder {
        val binding = ItemLikeListBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return LikeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LikeViewHolder, position: Int) {
        val likeData = boardList[position]

        holder.TV_title.text = likeData.title

        Glide.with(holder.IV_boardImage.context)
            .load("http://youhi.tplinkdns.com:4000/${likeData.boardId}.jpg")
            .apply(likeImageOptions)
            .transform(CenterCrop(), RoundedCorners(10))
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    // 이미지 로드 실패 시 TV_title만 보이도록 설정
                    holder.IV_boardImage.visibility = View.GONE
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
                    // 이미지 로드 성공 시 IV_boardImage만 보이도록 설정
                    holder.IV_boardImage.visibility = View.VISIBLE
                    holder.LL_title.visibility = View.GONE
                    return false // Glide가 이미 성공적으로 로드한 리소스를 처리함
                }
            })
            .into(holder.IV_boardImage)

        holder.LL_like.setOnClickListener {
            boardActionListener.onBoardListClick(likeData.boardId, likeData.category)
        }
    }

    override fun getItemCount(): Int {
        return boardList.size
    }
}
