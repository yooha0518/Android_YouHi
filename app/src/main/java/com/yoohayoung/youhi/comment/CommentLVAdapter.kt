package com.yoohayoung.youhi.comment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.utils.FBAuth


class CommentLVAdapter(val commentList : MutableList<CommentModel>) : BaseAdapter() {
    override fun getCount(): Int {
        return commentList.size
    }

    override fun getItem(position: Int): Any {
        return commentList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView

        if (view == null) {
            view = LayoutInflater.from(parent?.context).inflate(R.layout.board_list_item, parent, false)
        }

        val title = view?.findViewById<TextView>(R.id.titleArea)
        val time = view?.findViewById<TextView>(R.id.timeArea)
        val TV_nickName = view?.findViewById<TextView>(R.id.TV_nickName)

        title!!.text = commentList[position].commentTitle
        time!!.text = commentList[position].commentCreatedTime

        // 닉네임을 비동기적으로 가져와서 설정
        FBAuth.getNickName(commentList[position].uid) { nickName ->
            TV_nickName!!.text = nickName
        }

        return view!!
    }

}