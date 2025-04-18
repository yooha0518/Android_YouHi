package com.yoohayoung.youhi.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.board.BoardListActivity
import com.yoohayoung.youhi.databinding.FragmentBoardBinding

class TalkFragment : Fragment() {

    private var _binding: FragmentBoardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBoardBinding.inflate(inflater,container,false)

        binding.IVCategory1.setOnClickListener {
            val intent = Intent(context, BoardListActivity::class.java)
            intent.putExtra("category", "board1")
            startActivity(intent)
        }

        binding.IVCategory2.setOnClickListener {
            val intent = Intent(context, BoardListActivity::class.java)
            intent.putExtra("category", "board2")
            startActivity(intent)
        }

        binding.IVCategory3.setOnClickListener {
            val intent = Intent(context, BoardListActivity::class.java)
            intent.putExtra("category", "board3")
            startActivity(intent)
        }

        binding.IVCategory4.setOnClickListener {
            val intent = Intent(context, BoardListActivity::class.java)
            intent.putExtra("category", "board4")
            startActivity(intent)
        }

        binding.IVMenubarFriend.setOnClickListener{
            Log.d("HomeFragment","click")
            it.findNavController().navigate(R.id.action_talkFragment_to_friendFragment)
        }
        binding.IVMenubarLike.setOnClickListener{
            Log.d("HomeFragment","click")
            it.findNavController().navigate(R.id.action_talkFragment_to_likeFragment)
        }
        binding.IVMenubarHome.setOnClickListener{
            Log.d("HomeFragment","click")
            it.findNavController().navigate(R.id.action_talkFragment_to_homeFragment)
        }
        binding.IVMenubarCalender.setOnClickListener{
            Log.d("HomeFragment","click")
            it.findNavController().navigate(R.id.action_talkFragment_to_calenderFragment)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}