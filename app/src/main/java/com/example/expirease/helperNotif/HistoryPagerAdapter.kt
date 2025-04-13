package com.example.expirease.helperNotif

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.expirease.pager.AllItemFragment
import com.example.expirease.pager.ConsumedItemFragment
import com.example.expirease.pager.ExpiredItemFragment

class HistoryPagerAdapter(fragment : Fragment) : FragmentStateAdapter(fragment){
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
       return when (position){
           0 -> AllItemFragment()
           1 -> ExpiredItemFragment()
           2 -> ConsumedItemFragment()
           else -> AllItemFragment()
       }
    }
}