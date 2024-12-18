package com.yoohayoung.youhi.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.databinding.FragmentHomeBinding

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


        binding.IVMenubarFriend.setOnClickListener{
            Log.d("HomeFragment","click")
            it.findNavController().navigate(R.id.action_homeFragment_to_friendFragment)
        }
        binding.IVMenubarLike.setOnClickListener{
            Log.d("HomeFragment","click")
            it.findNavController().navigate(R.id.action_homeFragment_to_bookmarkFragment)
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
}