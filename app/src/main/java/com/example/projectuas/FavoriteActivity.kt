package com.example.projectuas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectuas.databinding.ActivityFavoriteBinding
import com.example.projectuas.model.Penginapan
import com.example.projectuas.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

class FavoriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoriteBinding
    private lateinit var adapter: PenginapanAdapter
    private lateinit var penginapanDao: PenginapanDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Favorite Penginapan"

        // Inisialisasi database
        val db = PenginapanDatabase.getDatabase(this)
        penginapanDao = db?.penginapanDao() ?: throw IllegalStateException("Database error!")

        setupRecyclerView()
        loadFavoriteData()
    }

    // Setup RecyclerView
    private fun setupRecyclerView() {
        adapter = PenginapanAdapter(
            listPenginapan = mutableListOf(),
            penginapanDao = penginapanDao,
            onDetailClick = { penginapan ->
                val intent = Intent(this, DetailPenginapanActivity::class.java)
                intent.putExtra("PENGINAPAN_DATA", penginapan)
                startActivity(intent)
            },
            onFavoriteToggled = {
                // Refresh data setelah favorit ditambahkan atau dihapus
//                loadFavoriteData()
            },
            isAdmin = false
        )

        binding.recyclerViewFavorite.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewFavorite.adapter = adapter

        loadFavoriteData() // Muat data saat RecyclerView di-setup
    }

    // Mengambil data favorit dari Room dan API
    private fun loadFavoriteData() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. Ambil ID favorit dari Room
                val favoriteIds = penginapanDao.getAllFavoriteIds()
                Log.d("FAVORITE_IDS", "ID favorit: $favoriteIds")

                // 2. Ambil data lengkap dari API berdasarkan ID favorit
                val favoriteData = favoriteIds.mapNotNull { id ->
                    val response = ApiClient.apiService.getPenginapanById(id)
                    if (response.isSuccessful) response.body() else null
                }

                // 3. Tampilkan data di RecyclerView
                runOnUiThread {
                    adapter.updateData(favoriteData)
                    Log.d("FAVORITE_UPDATE", "Data favorit ditampilkan: ${favoriteData.size} item")
                }
            } catch (e: Exception) {
                Log.e("FAVORITE_ERROR", "Kesalahan: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@FavoriteActivity, "Gagal memuat data favorit", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadFavoriteData() // Refresh data favorit saat activity dimulai kembali
    }
}
