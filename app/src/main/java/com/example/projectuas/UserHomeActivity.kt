package com.example.projectuas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectuas.databinding.ActivityUserHomeBinding
import com.example.projectuas.model.Penginapan
import com.example.projectuas.model.toEntity
import com.example.projectuas.network.ApiClient
import com.example.projectuas.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserHomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserHomeBinding
    private lateinit var adapter: PenginapanAdapter
    private lateinit var penginapanDao: PenginapanDao
    private val penginapanList = mutableListOf<Penginapan>()
    private lateinit var editLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "Home User"
        binding.toolbar.setTitleTextColor(resources.getColor(android.R.color.white, null))

        // Setup RecyclerView
        val db = PenginapanDatabase.getDatabase(this)
        penginapanDao = db.penginapanDao()
        setupRecyclerView()
        loadDataFromApi()

        // Setup Bottom Navigation
        setupBottomNavigation()

        // Daftar ActivityResultLauncher untuk menangani refresh data
        editLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Jika hasil dari EditPenginapanActivity adalah OK, refresh data
                loadDataFromApi()
            }
        }
    }

    private fun openEditPenginapanActivity(penginapan: Penginapan) {
        val intent = Intent(this, EditPenginapanActivity::class.java)
        intent.putExtra("PENGINAPAN_DATA", penginapan)
        editLauncher.launch(intent) // Buka EditPenginapanActivity dengan launcher
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_admin_dashboard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                showLogoutConfirmation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(ContextThemeWrapper(this, R.style.CustomAlertDialogTheme))
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin logout?")
            .setPositiveButton("Ya") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performLogout() {
        val sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    loadDataFromApi() // Langsung memuat data terbaru
                    true
                }
                R.id.nav_favorite -> {
                    loadFavoriteData()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = PenginapanAdapter(
            listPenginapan = mutableListOf(),
            penginapanDao = penginapanDao, // Kirim DAO jika diperlukan
            onDetailClick = { penginapan ->
                val intent = Intent(this, DetailPenginapanActivity::class.java)
                intent.putExtra("PENGINAPAN_DATA", penginapan)
                startActivity(intent)
            },
            onFavoriteToggled = {
                // Refresh data setelah favorit ditambah atau dihapus
                loadDataFromApi()
            },
            isAdmin = false // User mode
        )

        binding.recyclerViewPenginapan.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPenginapan.adapter = adapter
    }

    private fun loadDataFromApi() {
        Log.d("LOAD_DATA", "Memulai memuat data dari API...")
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.apiService.getAllPenginapan().execute()
                if (response.isSuccessful && response.body() != null) {
                    val newData = response.body()!!

                    Log.d("LOAD_DATA", "Data diterima dari API: ${newData.size} item")
                    runOnUiThread {
                        penginapanList.clear()
                        penginapanList.addAll(newData.sortedBy { it.id ?: "" }) // Urutkan data

                        // Pastikan adapter diperbarui secara konsisten
                        adapter.updateData(penginapanList)
                    }
                } else {
                    Log.e("LOAD_DATA", "Gagal memuat data: ${response.message()}")
                    showToast("Gagal memuat data: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("LOAD_DATA", "Kesalahan jaringan: ${e.message}")
                showToast("Kesalahan jaringan: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@UserHomeActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetRecyclerView() {
        penginapanList.clear() // Kosongkan list data
        adapter.resetData() // Reset adapter
    }

    private fun loadFavoriteData() {
        adapter = PenginapanAdapter(
            listPenginapan = mutableListOf(),
            penginapanDao = penginapanDao,
            onDetailClick = { penginapan ->
                val intent = Intent(this, DetailPenginapanActivity::class.java)
                intent.putExtra("PENGINAPAN_DATA", penginapan)
                startActivity(intent)
            },
            isFavoritePage = true // Tambahkan ini
        )
        binding.recyclerViewPenginapan.adapter = adapter

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val favoriteIds = penginapanDao.getAllFavoriteIds()
                val updatedList = mutableListOf<Penginapan>()
                favoriteIds.forEach { id ->
                    val response = ApiClient.apiService.getPenginapanById(id)
                    if (response.isSuccessful) {
                        response.body()?.let { penginapan ->
                            updatedList.add(penginapan)
                        }
                    }
                }
                runOnUiThread {
                    adapter.updateData(updatedList)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@UserHomeActivity, "Gagal memuat data favorit: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (penginapanList.isEmpty()) { // Cegah pemuatan ulang jika sudah ada data
            resetRecyclerView()
            loadDataFromApi()
        }
    }

}
