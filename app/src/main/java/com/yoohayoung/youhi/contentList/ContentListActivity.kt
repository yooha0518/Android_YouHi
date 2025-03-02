package com.yoohayoung.youhi.contentList

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.yoohayoung.youhi.ContentModel
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.FBRef

class ContentListActivity : AppCompatActivity() {
    val bookmarkIdList = mutableListOf<String>()
    lateinit var rvAdapter:ContentRVAdapter
    val items = ArrayList<ContentModel>()
    val itemKeyList = ArrayList<String>()
    var category:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_list)

        rvAdapter = ContentRVAdapter(baseContext, items, itemKeyList, bookmarkIdList)

        category = intent.getStringExtra("category")

        getCategoryData()
        getBookmarkData()

        val rv: RecyclerView = findViewById(R.id.rv)

        rv.adapter = rvAdapter
        rv.layoutManager = GridLayoutManager(this, 2)

        rvAdapter.itemClick = object :ContentRVAdapter.ItemClick{
            override fun onClick(view: View, position:Int){

                var intent = Intent(this@ContentListActivity, ContentShowActivity::class.java)
                intent.putExtra("url",items[position].webUrl) //url을 인텐트에 넣어서 엑티비티 실행
                startActivity(intent)
            }
        }
    }

    private fun getCategoryData(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(dataModel in dataSnapshot.children){
                    val item = dataModel.getValue(ContentModel::class.java)
                    items.add(item!!)
                    itemKeyList.add(dataModel.key.toString())
                }
                rvAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("ContentListActivity", "loadPost:onCancelled")
            }
        }
        when(category){
            "board1" -> FBRef.boardRef1.addValueEventListener(postListener)
            "board2" -> FBRef.boardRef2.addValueEventListener(postListener)
            "board3" -> FBRef.boardRef3.addValueEventListener(postListener)
            "board4" -> FBRef.boardRef4.addValueEventListener(postListener)
            else -> Log.e("ContentListActivity","카테고리가 없습니다.")
        }

    }

    private fun getBookmarkData(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                bookmarkIdList.clear()
                for(dataModel in dataSnapshot.children){
                    bookmarkIdList.add(dataModel.key.toString())
                }
                rvAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("ContentListActivity", "getBookmarkData:onCancelled")
            }
        }
        FBRef.likeRef.child(FBAuth.getUid()).addValueEventListener(postListener)
    }
}