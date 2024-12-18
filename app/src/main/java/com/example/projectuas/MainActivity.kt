package com.example.projectuas

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.projectuas.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Gunakan View Binding untuk ActivityMain
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)

        // Periksa status login
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val role = sharedPreferences.getString("role", null)

        if (isLoggedIn) {
            // Jika sudah login, arahkan berdasarkan role
            navigateToHome(role)
        } else {
            // Jika belum login, atur TabLayout dan ViewPager2
            setupTabLayoutAndViewPager()
        }
    }

    private fun setupTabLayoutAndViewPager() {
        // Atur adapter untuk ViewPager2
        val adapter = SectionPageAdapter(this)
        binding.viewPager.adapter = adapter

        // Hubungkan TabLayout dengan ViewPager2 menggunakan TabLayoutMediator
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "User Login"
                1 -> "Admin Login"
                else -> null
            }
        }.attach()
    }

    private fun navigateToHome(role: String?) {
        when (role) {
            "user" -> {
                // Jika role adalah user, navigasi ke UserHomeActivity
                val intent = Intent(this, UserHomeActivity::class.java)
                startActivity(intent)
                finish()
            }
            "admin" -> {
                // Jika role adalah admin, navigasi ke AdminDashboardActivity
                val intent = Intent(this, AdminDashboardActivity::class.java)
                startActivity(intent)
                finish()
            }
            else -> {
                // Jika role tidak dikenali, reset login
                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()

                // Tampilkan TabLayout untuk login kembali
                setupTabLayoutAndViewPager()
            }
        }
    }
}
