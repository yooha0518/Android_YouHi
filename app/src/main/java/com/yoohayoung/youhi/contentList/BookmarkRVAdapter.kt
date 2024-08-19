package com.yoohayoung.youhi.contentList

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.FBRef

class BookmarkRVAdapter(val context: Context, val items: ArrayList<ContentModel>, val keyList:ArrayList<String>, val bookmarkIdList:MutableList<String>) : RecyclerView.Adapter<BookmarkRVAdapter.Viewholder>() {

    interface ItemClick{ //클릭 이벤트 리스너 생성을 위한 인터페이스
        fun onClick(view: View, position:Int)
    }

    var itemClick :ItemClick ?= null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkRVAdapter.Viewholder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.content_rv_item,parent, false)
        return Viewholder(v)
    }

    override fun onBindViewHolder(holder: BookmarkRVAdapter.Viewholder, position: Int) {
        holder.bindItems(items[position], keyList[position])

        if(itemClick != null){
            holder.itemView.setOnClickListener{v->
                itemClick?.onClick(v, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView){

        fun bindItems(item:ContentModel, key:String){
            val contentTitle = itemView.findViewById<TextView>(R.id.textArea)
            val imageViewArea = itemView.findViewById<ImageView>(R.id.imageArea)
            val bookmarkArea = itemView.findViewById<ImageView>(R.id.bookmarkArea)

//            Log.d("BA:bindItems","item: $item")

            if(bookmarkIdList.contains(key)){
                bookmarkArea.setImageResource(R.drawable.bookmark_color)
            }else{
                bookmarkArea.setImageResource(R.drawable.bookmark_white)
            }

            bookmarkArea.setOnClickListener{
                //북마크 클릭 이벤트

                if(bookmarkIdList.contains(key)){
                    //북마크가 있을 때
                    FBRef.bookmarkRef
                        .child(FBAuth.getUid())
                        .child(key)
                        .removeValue()

                }else{
                    //북마크가 없을 때

                    FBRef.bookmarkRef
                        .child(FBAuth.getUid())
                        .child(key)
                        .setValue(BookmarkModel(true))
                }


            }

            contentTitle.text = item.title
            Glide.with(context)
                .load(item.imageUrl)
                .into(imageViewArea)


        }
    }

}