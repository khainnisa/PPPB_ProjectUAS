package com.example.projectuas

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class SectionPageAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    // SectionPageAdapter adalah adapter untuk ViewPager2, yang akan menampilkan fragment sesuai dengan posisi.

    override fun getItemCount(): Int {
        return 2
        // Metode ini menentukan jumlah halaman (fragment) yang akan ditampilkan di ViewPager.
        // Dalam hal ini, ada 2 halaman: RegisterFragment dan LoginFragment.
    }

    override fun createFragment(position: Int): Fragment {
        // Metode ini bertanggung jawab untuk membuat fragment berdasarkan posisi.
        return when (position) {
            0 -> UserLoginFragment()
            // Jika posisi adalah 0, tampilkan RegisterFragment.

            1 -> AdminLoginFragment()
            // Jika posisi adalah 1, tampilkan LoginFragment.

            else -> throw IllegalArgumentException("Invalid position: $position")
            // Jika posisi tidak valid (bukan 0 atau 1), lempar pengecualian.
        }
    }
}
