package com.yoohayoung.youhi.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.board.BoardModel
import com.yoohayoung.youhi.contentList.BookmarkRVAdapter
import com.yoohayoung.youhi.contentList.ContentModel
import com.yoohayoung.youhi.databinding.FragmentHomeBinding
import com.yoohayoung.youhi.utils.FBRef

class HomeFragment : Fragment() {

    private lateinit var binding:FragmentHomeBinding

    private lateinit var auth: FirebaseAuth

    val bookmarkIdList = mutableListOf<String>()
    val items = ArrayList<ContentModel>()
    val itemKeyList = ArrayList<String>()

    private  val boardDataList = mutableListOf<BoardModel>()

    lateinit var rvAdapter : BookmarkRVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home,container,false)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("homeFragment", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            Log.d("homeFragment", token)
        })


        binding.tipTap.setOnClickListener{
            Log.d("HomeFragment","click")
            it.findNavController().navigate(R.id.action_homeFragment_to_friendFragment)
        }
        binding.bookmarkTap.setOnClickListener{
            Log.d("HomeFragment","click")
            it.findNavController().navigate(R.id.action_homeFragment_to_bookmarkFragment)
        }
        binding.talkTap.setOnClickListener{
            Log.d("HomeFragment","click")
            it.findNavController().navigate(R.id.action_homeFragment_to_talkFragment)
        }
        binding.storeTap.setOnClickListener{
            Log.d("HomeFragment","click")
            it.findNavController().navigate(R.id.action_homeFragment_to_storeFragment)
        }

        rvAdapter = BookmarkRVAdapter(requireContext(), items, itemKeyList, bookmarkIdList)

        val rv : RecyclerView = binding.mainRV
        rv.adapter = rvAdapter

        rv.layoutManager = GridLayoutManager(requireContext(), 2)


        getCategoryData()
        getFBBoardData()



        return binding.root
    }

    private fun getFBBoardData(){
        val postListener = object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                boardDataList.clear()
                for(dataModel in snapshot.children){
                    val item = dataModel.getValue(BoardModel::class.java)
                    boardDataList.add(item!!)
                }

                // 데이터 역순으로 정렬(새로운 글이 작성되면 위로 쌓이도록 하기 위함)
                boardDataList.reverse()

                // 최근 3개의 데이터를 각각의 TextView에 설정
                val latestBoards = boardDataList.take(3)
                if (latestBoards.size > 0) {
                    binding.board1Area.text = latestBoards[0].title // item.toString()을 적절히 변경
                }
                if (latestBoards.size > 1) {
                    binding.board2Area.text = latestBoards[1].title
                }
                if (latestBoards.size > 2) {
                    binding.board3Area.text = latestBoards[2].title
                }
            }

            override fun onCancelled(databaseError: DatabaseError){

            }

        }

        FBRef.boardRef1.addValueEventListener(postListener)
    }




    private fun getCategoryData(){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children) {

                    val item = dataModel.getValue(ContentModel::class.java)

                    items.add(item!!)
                    itemKeyList.add(dataModel.key.toString())


                }
                rvAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("ContentListActivity", "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.category1.addValueEventListener(postListener)
        FBRef.category2.addValueEventListener(postListener)

    }

}