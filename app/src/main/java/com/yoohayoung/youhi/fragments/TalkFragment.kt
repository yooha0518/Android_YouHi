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
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.board.BoardListActivity
import com.yoohayoung.youhi.databinding.FragmentTalkBinding

class TalkFragment : Fragment() {

    private lateinit var binding: FragmentTalkBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_talk,container,false)


        binding.category1.setOnClickListener {
            val intent = Intent(context, BoardListActivity::class.java)
            intent.putExtra("category", "category1")
            startActivity(intent)
        }

        binding.category2.setOnClickListener {
            val intent = Intent(context, BoardListActivity::class.java)
            intent.putExtra("category", "category2")
            startActivity(intent)
        }

        binding.category3.setOnClickListener {
            val intent = Intent(context, BoardListActivity::class.java)
            intent.putExtra("category", "category3")
            startActivity(intent)
        }

        binding.category4.setOnClickListener {
            val intent = Intent(context, BoardListActivity::class.java)
            intent.putExtra("category", "category4")
            startActivity(intent)
        }

        binding.category5.setOnClickListener {
            val intent = Intent(context, BoardListActivity::class.java)
            intent.putExtra("category", "category5")
            startActivity(intent)
        }

        binding.category6.setOnClickListener {
            val intent = Intent(context, BoardListActivity::class.java)
            intent.putExtra("category", "category6")
            startActivity(intent)
        }
        binding.category7.setOnClickListener {
            val intent = Intent(context, BoardListActivity::class.java)
            intent.putExtra("category", "category7")
            startActivity(intent)
        }

        binding.tipTap.setOnClickListener{
            Log.d("HomeFragment","click")
            it.findNavController().navigate(R.id.action_talkFragment_to_friendFragment)
        }
        binding.bookmarkTap.setOnClickListener{
            Log.d("HomeFragment","click")
            it.findNavController().navigate(R.id.action_talkFragment_to_bookmarkFragment)
        }
        binding.homeTap.setOnClickListener{
            Log.d("HomeFragment","click")
            it.findNavController().navigate(R.id.action_talkFragment_to_homeFragment)
        }
        binding.storeTap.setOnClickListener{
            Log.d("HomeFragment","click")
            it.findNavController().navigate(R.id.action_talkFragment_to_storeFragment)
        }



        return binding.root
    }


}