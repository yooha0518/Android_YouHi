package com.yoohayoung.youhi.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.contentList.BookmarkRVAdapter
import com.yoohayoung.youhi.contentList.ContentModel
import com.yoohayoung.youhi.databinding.FragmentBookmarkBinding
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.FBRef

class BookmarkFragment : Fragment() {

    private lateinit var binding: FragmentBookmarkBinding

    lateinit var rvAdapter: BookmarkRVAdapter

    val bookmarkIdList = mutableListOf<String>()
    val items = ArrayList<ContentModel>()
    val itemKeyList = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bookmark,container,false)

        binding.tipTap.setOnClickListener{
            it.findNavController().navigate(R.id.action_bookmarkFragment_to_friendFragment)
        }
        binding.homeTap.setOnClickListener{
            it.findNavController().navigate(R.id.action_bookmarkFragment_to_homeFragment)
        }
        binding.talkTap.setOnClickListener{
            it.findNavController().navigate(R.id.action_bookmarkFragment_to_talkFragment)
        }
        binding.storeTap.setOnClickListener{
            it.findNavController().navigate(R.id.action_bookmarkFragment_to_storeFragment)
        }


        getBookmarkData()
        //1. 북마크 여부를 다 가져옴

        rvAdapter = BookmarkRVAdapter(requireContext(), items, itemKeyList, bookmarkIdList)

        val rv: RecyclerView = binding.bookmarkRV
        rv.adapter = rvAdapter

        rv.layoutManager = GridLayoutManager(requireContext(), 2) //recycler View를 사용할기 위해서 manager를 설정해야함



        return binding.root
    }

    private fun getCategoryData(){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(dataModel in dataSnapshot.children){
                    val item = dataModel.getValue(ContentModel::class.java)

                    // 3. 전체 컨텐츠 중에서, 사용자가 북마크한 정보만 보여줌!
                    if (bookmarkIdList.contains(dataModel.key.toString())){
                        if(!itemKeyList.contains(dataModel.key.toString())){ //이미 포함되어 있을경우엔 배열에 넣지 않는다.
                            items.add(item!!)
                            itemKeyList.add(dataModel.key.toString())
                        }

                    }
                }
                rvAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("ContentListActivity", "loadPost:onCancelled")
            }

        }
        FBRef.category1.addValueEventListener(postListener)
        FBRef.category2.addValueEventListener(postListener)

    }

    private fun getBookmarkData(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                bookmarkIdList.clear()
                for(dataModel in dataSnapshot.children){
                    bookmarkIdList.add(dataModel.key.toString())
                    Log.d("1111 list에 데이터 추가","$dataModel")

                }


                getCategoryData()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("ContentListActivity", "getBookmarkData:onCancelled")
            }

        }
        FBRef.bookmarkRef.child(FBAuth.getUid()).addValueEventListener(postListener)
    }
}