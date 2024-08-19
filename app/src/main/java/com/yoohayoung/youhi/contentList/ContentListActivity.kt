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
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.FBRef

class ContentListActivity : AppCompatActivity() {

    lateinit var myRef : DatabaseReference

    val bookmarkIdList = mutableListOf<String>()

    lateinit var rvAdapter:ContentRVAdapter
    val items = ArrayList<ContentModel>()
    val itemKeyList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_list)



        rvAdapter = ContentRVAdapter(baseContext, items, itemKeyList, bookmarkIdList)

        // Write a message to the database
        val database = Firebase.database
        myRef = database.getReference("content")

        val category = intent.getStringExtra("category")


        if(category == "category1") {
            myRef = database.getReference("contents1")

        } else if(category == "category2") {
            myRef = database.getReference("contents2")
        }

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
        myRef.addValueEventListener(postListener)

    }

    private fun getBookmarkData(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                bookmarkIdList.clear()
                for(dataModel in dataSnapshot.children){
//                    Log.d("CA:getBookmarkData", dataModel.toString())
                    bookmarkIdList.add(dataModel.key.toString())
                }

                rvAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("ContentListActivity", "getBookmarkData:onCancelled")
            }

        }
        FBRef.bookmarkRef.child(FBAuth.getUid()).addValueEventListener(postListener)
    }


}


//        myRef.push().setValue(
//            ContentModel("title1", "https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FblYPPY%2Fbtq66v0S4wu%2FRmuhpkXUO4FOcrlOmVG4G1%2Fimg.png", "https://philosopher-chan.tistory.com/1235?category=941578")
//        )
//        myRef.push().setValue(
//            ContentModel("title2", "https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FznKK4%2Fbtq665AUWem%2FRUawPn5Wwb4cQ8BetEwN40%2Fimg.png", "https://philosopher-chan.tistory.com/1236?category=941578")
//        )
//        myRef.push().setValue(
//            ContentModel("title3", "https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fbtig9C%2Fbtq65UGxyWI%2FPRBIGUKJ4rjMkI7KTGrxtK%2Fimg.png", "https://philosopher-chan.tistory.com/1237?category=941578")
//        )
//
//        myRef.push().setValue(
//            ContentModel("title4", "https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FcOYyBM%2Fbtq67Or43WW%2F17lZ3tKajnNwGPSCLtfnE1%2Fimg.png", "https://philosopher-chan.tistory.com/1238?category=941578")
//        )

//
//        val myRef2 = database.getReference("contents2")
//        myRef2.push().setValue(
//            ContentModel("title5", "https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FblYPPY%2Fbtq66v0S4wu%2FRmuhpkXUO4FOcrlOmVG4G1%2Fimg.png", "https://philosopher-chan.tistory.com/1235?category=941578")
//        )
//        myRef2.push().setValue(
//            ContentModel("title6", "https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FznKK4%2Fbtq665AUWem%2FRUawPn5Wwb4cQ8BetEwN40%2Fimg.png", "https://philosopher-chan.tistory.com/1236?category=941578")
//        )
//        myRef2.push().setValue(
//            ContentModel("title7", "https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fbtig9C%2Fbtq65UGxyWI%2FPRBIGUKJ4rjMkI7KTGrxtK%2Fimg.png", "https://philosopher-chan.tistory.com/1237?category=941578")
//        )
//
//        myRef2.push().setValue(
//            ContentModel("title8", "https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FcOYyBM%2Fbtq67Or43WW%2F17lZ3tKajnNwGPSCLtfnE1%2Fimg.png", "https://philosopher-chan.tistory.com/1238?category=941578")
//        )