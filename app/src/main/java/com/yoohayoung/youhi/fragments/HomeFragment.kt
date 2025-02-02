package com.yoohayoung.youhi.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.yoohayoung.youhi.MyPageActivity
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.board.News
import com.yoohayoung.youhi.contentList.ContentListActivity
import com.yoohayoung.youhi.databinding.FragmentHomeBinding
import com.yoohayoung.youhi.event.CreateEventActivity
import com.yoohayoung.youhi.friend.FriendSearchActivity
import com.yoohayoung.youhi.utils.FBAuth.Companion.getUid
import com.yoohayoung.youhi.utils.FBRef
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var binding:FragmentHomeBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home,container,false)

        // 광고뷰 초기화 및 로드
        val adView: AdView = binding.adView
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("homeFragment", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            Log.d("homeFragment", token)
        })

        getNewsList()

        binding.IVMypage.setOnClickListener {
            val intent = Intent(context, MyPageActivity::class.java)
            startActivity(intent)
        }

        binding.IVBlog.setOnClickListener {
            val intent = Intent(context, ContentListActivity::class.java)
            intent.putExtra("category", "category2")
            startActivity(intent)
        }

        binding.BTNFindFriend.setOnClickListener{
            val intent = Intent(context, FriendSearchActivity::class.java)
            startActivity(intent)
        }

        binding.BTNDeveloperProfile.setOnClickListener{
            val intent = Intent(context, ContentListActivity::class.java)
            intent.putExtra("category", "category1")
            startActivity(intent)
        }

        binding.BTNCalender.setOnClickListener{
            val intent = Intent(context, CreateEventActivity::class.java)
            val currentDate = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            intent.putExtra("selectedDate", currentDate.format(formatter))
            startActivity(intent)
        }

        binding.IVMenubarFriend.setOnClickListener{
            Log.d("HomeFragment","click")
            it.findNavController().navigate(R.id.action_homeFragment_to_friendFragment)
        }
        binding.IVMenubarLike.setOnClickListener{
            Log.d("HomeFragment","click")
            it.findNavController().navigate(R.id.action_homeFragment_to_likeFragment)
        }
        binding.IVMenubarBoard.setOnClickListener{
            Log.d("HomeFragment","click")
            it.findNavController().navigate(R.id.action_homeFragment_to_talkFragment)
        }
        binding.IVMenubarCalender.setOnClickListener{
            Log.d("HomeFragment","click")
            it.findNavController().navigate(R.id.action_homeFragment_to_calenderFragment)
        }


        return binding.root
    }

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun getNewsList() {
        val myUid = getUid() // 현재 로그인한 사용자의 UID
        val friendsRef = FBRef.userRef.child(myUid).child("friends") // 친구 목록 경로
        val newsRef = FBRef.newsRef.child(getCurrentDate()) // 오늘 날짜의 뉴스 목록 가져오기

        friendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(friendsSnapshot: DataSnapshot) {
                val friendUids = mutableSetOf<String>()

                for (friend in friendsSnapshot.children) {
                    friendUids.add(friend.key ?: continue) // 친구 UID 저장
                }

                // 뉴스 데이터 가져오기
                newsRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(newsSnapshot: DataSnapshot) {
                        val filteredNewsList = mutableListOf<News>()

                        for (newsItemSnapshot in newsSnapshot.children) {
                            val newsItem = newsItemSnapshot.getValue(News::class.java)

                            // news의 uid가 friends 목록에 포함되어 있는 경우만 추가
                            if (newsItem != null && friendUids.contains(newsItem.uid) || newsItem != null && newsItem.uid==myUid) {
                                filteredNewsList.add(newsItem)
                            }
                        }
                        //UI업데이트
                        if (filteredNewsList.isNotEmpty()) {
                            filteredNewsList.reverse() // 최신순 정렬
                            binding.TVNews1.text = filteredNewsList.getOrNull(0)?.content ?: ""
                            binding.TVNews2.text = filteredNewsList.getOrNull(1)?.content ?: ""
                            binding.TVNews3.text = filteredNewsList.getOrNull(2)?.content ?: ""

                            binding.CIVNews1

                            // 프로필 이미지를 Glide를 사용하여 로드
                            Glide.with(binding.CIVNews1)
                                .load("http://youhi.tplinkdns.com:4000/${filteredNewsList.getOrNull(0)?.uid}.jpg")
                                .error(R.drawable.default_profile) // 로드 실패 시 기본 이미지 로드
                                .diskCacheStrategy(DiskCacheStrategy.NONE) // 디스크 캐시 사용 안 함
                                .skipMemoryCache(true) // 메모리 캐시 사용 안 함
                                .into(binding.CIVNews1)

                            Glide.with(binding.CIVNews2)
                                .load("http://youhi.tplinkdns.com:4000/${filteredNewsList.getOrNull(1)?.uid}.jpg")
                                .error(R.drawable.default_profile) // 로드 실패 시 기본 이미지 로드
                                .diskCacheStrategy(DiskCacheStrategy.NONE) // 디스크 캐시 사용 안 함
                                .skipMemoryCache(true) // 메모리 캐시 사용 안 함
                                .into(binding.CIVNews2)

                            Glide.with(binding.CIVNews3)
                                .load("http://youhi.tplinkdns.com:4000/${filteredNewsList.getOrNull(2)?.uid}.jpg")
                                .error(R.drawable.default_profile) // 로드 실패 시 기본 이미지 로드
                                .diskCacheStrategy(DiskCacheStrategy.NONE) // 디스크 캐시 사용 안 함
                                .skipMemoryCache(true) // 메모리 캐시 사용 안 함
                                .into(binding.CIVNews3)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("getNewsList", "뉴스 데이터를 가져오는 데 실패했습니다.", error.toException())
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("getNewsList", "친구 목록을 가져오는 데 실패했습니다.", error.toException())
            }
        })
    }

}