package com.yoohayoung.youhi.contentList

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.yoohayoung.youhi.ContentModel
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.databinding.ActivityContentListBinding
import com.yoohayoung.youhi.utils.FBRef

class ContentListActivity : AppCompatActivity(), ContentAdapter.ContentItemClick {
    lateinit var contentAdapter:ContentAdapter
    val contentList = mutableListOf<ContentModel>()
    var category:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityContentListBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        contentAdapter = ContentAdapter(contentList, this)

        category = intent.getStringExtra("category")

        val RV_content = binding.RVContent
        RV_content.adapter = contentAdapter
        RV_content.layoutManager = GridLayoutManager(this, 2)

        loadContentDataFromFirebase()
    }

    override fun onContentClick(content: ContentModel) {
        val intent = Intent(this, ContentShowActivity::class.java)
        intent.putExtra("url", content.webUrl) // url을 인텐트에 넣어서 엑티비티 실행
        startActivity(intent)
    }

    private fun loadContentDataFromFirebase() {

        Log.d("loadContentDataFromFirebase", "content: $category ")
        when(category){
            "contents1"-> {
                FBRef.contentsRef1.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        contentList.clear()
                        for (snapshot in dataSnapshot.children) {
                            val content = snapshot.getValue(ContentModel::class.java)
                            if (content != null) {
                                contentList.add(content)  // contentId를 포함한 ContentModel 추가
                            }
                        }
                        contentAdapter.notifyDataSetChanged()  // RecyclerView 갱신
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ContentListActivity", "Error loading data: ${error.message}")
                    }
                })
            }
            "contents2"->{
                FBRef.contentsRef2.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        contentList.clear()
                        for (snapshot in dataSnapshot.children) {
                            val content = snapshot.getValue(ContentModel::class.java)
                            if (content != null) {
                                contentList.add(content)  // contentId를 포함한 ContentModel 추가
                            }
                        }
                        contentAdapter.notifyDataSetChanged()  // RecyclerView 갱신
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ContentListActivity", "Error loading data: ${error.message}")
                    }
                })
            }
        }

    }


}