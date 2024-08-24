package com.yoohayoung.youhi.board

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.utils.FBAuth

class BoardListLVAdapter(val boardList :MutableList<BoardModel>): BaseAdapter() {
    override fun getCount(): Int {
        return boardList.size
    }

    override fun getItem(position: Int): Any {
        return boardList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View? = LayoutInflater.from(parent?.context).inflate(R.layout.board_list_item,parent, false)

        val itemLinearLayoutView = view?.findViewById<LinearLayout>(R.id.itemView)
        val title = view?.findViewById<TextView>(R.id.titleArea)
        val time = view?.findViewById<TextView>(R.id.timeArea)

        if(boardList[position].uid == FBAuth.getUid()) {
            itemLinearLayoutView?.setBackgroundColor(Color.parseColor("#F0E4F3"))
        }

        title!!.text = boardList[position].title
        time!!.text = boardList[position].time

        return view!!
    }
}