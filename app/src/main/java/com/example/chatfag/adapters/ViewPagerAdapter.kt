package com.example.chatfag.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.chatfag.fragments.ContatosFragments
import com.example.chatfag.fragments.ConversasFragment

class ViewPagerAdapter(
    val abas : List<String>,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
): FragmentStateAdapter(fragmentManager, lifecycle) {


    override fun getItemCount(): Int {
        return abas.size

    }

    override fun createFragment(position: Int): Fragment {
        when (position){
            1 -> return ContatosFragments()
        }
        return ConversasFragment()
    }
}