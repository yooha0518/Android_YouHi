package com.yoohayoung.youhi.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.auth.UserModel
import com.yoohayoung.youhi.contentList.ContentListActivity
import com.yoohayoung.youhi.databinding.FragmentStoreBinding
import com.yoohayoung.youhi.utils.FBRef

class StoreFragment : Fragment() {

    private lateinit var binding: FragmentStoreBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_store,container,false)


        binding.category1.setOnClickListener {
            val intent = Intent(context, ContentListActivity::class.java)
            intent.putExtra("category", "category1")
            startActivity(intent)
        }

        binding.category2.setOnClickListener {
            val intent = Intent(context, ContentListActivity::class.java)
            intent.putExtra("category", "category2")
            startActivity(intent)
        }


        binding.tipTap.setOnClickListener{
            Log.d("HomeFragment","click")
            it.findNavController().navigate(R.id.action_storeFragment_to_friendFragment)
        }
        binding.bookmarkTap.setOnClickListener{
            Log.d("HomeFragment","click")
            it.findNavController().navigate(R.id.action_storeFragment_to_bookmarkFragment)
        }
        binding.talkTap.setOnClickListener{
            Log.d("HomeFragment","click")
            it.findNavController().navigate(R.id.action_storeFragment_to_talkFragment)
        }
        binding.homeTap.setOnClickListener{
            Log.d("HomeFragment","click")
            it.findNavController().navigate(R.id.action_storeFragment_to_homeFragment)
        }



        return binding.root
    }

}