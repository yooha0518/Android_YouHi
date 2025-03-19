package com.yoohayoung.youhi.contentList

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yoohayoung.youhi.ContentModel
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.databinding.ItemContentListBinding

class ContentAdapter(private val contentList: List<ContentModel>, private val contentItemClick:ContentItemClick) : RecyclerView.Adapter<ContentAdapter.ContentViewHolder>() {

    interface ContentItemClick{ //클릭 이벤트 리스너 생성을 위한 인터페이스
        fun onContentClick(content: ContentModel)
    }

    class ContentViewHolder(binding:ItemContentListBinding) :RecyclerView.ViewHolder(binding.root){
        val TV_contentTitle:TextView = binding.TVContentTitle
        val IV_content: ImageView = binding.IVContent
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val binding = ItemContentListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        val content = contentList[position]
        holder.TV_contentTitle.text = content.title
        val activity = holder.itemView.context as? ContentListActivity

        if (activity != null && !activity.isDestroyed) {
            Glide.with(activity)
                .load(content.imageUrl)
                .into(holder.IV_content)
        }


        holder.itemView.setOnClickListener{
            contentItemClick.onContentClick(content)
        }
    }

    override fun getItemCount(): Int {
        return contentList.size
    }

}