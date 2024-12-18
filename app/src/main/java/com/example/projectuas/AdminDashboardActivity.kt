package com.example.projectuas

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectuas.databinding.ActivityAdminDashboardBinding
import com.example.projectuas.model.Penginapan
import com.example.projectuas.PenginapanAdapter
import com.example.projectuas.network.ApiClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminDashboardActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var penginapanAdapter: PenginapanAdapter
    private val penginapanList = mutableListOf<Penginapan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set toolbar sebagai action bar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Dashboard Admin"
        binding.toolbar.setTitleTextColor(resources.getColor(android.R.color.white, null))

        // Setup RecyclerView di Home
        setupRecyclerView()

        // Setup Bottom Navigation
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(this)

        // Menampilkan data pertama kali
        showHome()
        binding.bottomNavigationView.selectedItemId = R.id.menu_home
    }

    private fun deletePenginapan(penginapan: Penginapan) {
        if (penginapan.id == null) {
            Log.e("DELETE_ACTION", "ID null, tidak dapat menghapus")
            Toast.makeText(this, "Gagal menghapus: ID tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("DELETE_ACTION", "Menghapus item dengan ID: ${penginapan.id}")

        val call = ApiClient.apiService.deletePenginapan(penginapan.id)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("DELETE_ACTION", "Data berhasil dihapus dari server")

                    // Panggil ulang loadPenginapanData() untuk refresh data dari server
                    loadPenginapanData()

                    Toast.makeText(
                        this@AdminDashboardActivity,
                        "Data berhasil dihapus",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.e("DELETE_ACTION", "Gagal menghapus item: ${response.code()} - ${response.message()}")
                    Toast.makeText(
                        this@AdminDashboardActivity,
                        "Gagal menghapus data: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("DELETE_ACTION", "Error: ${t.localizedMessage}")
                Toast.makeText(
                    this@AdminDashboardActivity,
                    "Error: ${t.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setupRecyclerView() {
        penginapanAdapter = PenginapanAdapter(
            penginapanList,
            onDetailClick = { penginapan ->
                val intent = Intent(this, DetailPenginapanActivity::class.java)
                intent.putExtra("PENGINAPAN_DATA", penginapan)
                startActivity(intent)
            },
            onEditClick = { penginapan ->
                Log.d("EDIT_DATA", "Gambar URL: ${penginapan.gambarUrl}")
                val intent = Intent(this, EditPenginapanActivity::class.java)
                intent.putExtra("PENGINAPAN_DATA", penginapan)
                startActivityForResult(intent, EDIT_PENGINAPAN_REQUEST_CODE)
            },
            onDeleteClick = { penginapan ->
                deletePenginapan(penginapan)
                Log.d("DELETE_ACTION", "Data dihapus, memuat ulang data...")
            },
            isAdmin = true // Koma di sini sudah benar
        )

        binding.penginapanRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AdminDashboardActivity)
            adapter = penginapanAdapter
        }
    }

    private fun showHome() {
        // Tampilkan RecyclerView dan sembunyikan komponen lainnya jika ada
        binding.penginapanRecyclerView.visible()
        loadPenginapanData()
    }

    private fun loadPenginapanData() {
        // Bersihkan daftar terlebih dahulu
        penginapanList.clear()

        // Panggil API menggunakan Retrofit
        val call = ApiClient.apiService.getAllPenginapan()
        call.enqueue(object : Callback<List<Penginapan>> {
            override fun onResponse(call: Call<List<Penginapan>>, response: Response<List<Penginapan>>) {
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    Log.d("API_RESPONSE", "Data Loaded: $data")
                    penginapanAdapter.updateData(data) // Update adapter
                } else {
                    Log.e("API_ERROR", "Failed to load data: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<Penginapan>>, t: Throwable) {
                showError("Error: ${t.localizedMessage}")
                Log.e("API_FAILURE", "Throwable: ${t.localizedMessage}")
            }

        })
    }


    private fun showError(message: String) {
        Toast.makeText(this@AdminDashboardActivity, message, Toast.LENGTH_LONG).show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_home -> {
                showHome()
                true
            }
            R.id.menu_add -> {
                val intent = Intent(this, AddPenginapanActivity::class.java)
                startActivity(intent)
                true
            }
            else -> false
        }
    }

    // Ekstensi fungsi untuk membuat komponen terlihat
    private fun androidx.recyclerview.widget.RecyclerView.visible() {
        this.visibility = android.view.View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_PENGINAPAN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Memuat ulang data setelah menambahkan penginapan baru
            loadPenginapanData()
            Toast.makeText(this, "Data baru berhasil ditambahkan", Toast.LENGTH_SHORT).show()
        } else if (requestCode == EDIT_PENGINAPAN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Memperbarui data setelah edit berhasil
            loadPenginapanData()
            val updatedPenginapan = data?.getSerializableExtra("UPDATED_PENGINAPAN") as? Penginapan
            updatedPenginapan?.let {
                val index = penginapanList.indexOfFirst { it.id == updatedPenginapan.id }
                if (index != -1) {
                    penginapanList[index] = updatedPenginapan
                    penginapanAdapter.notifyItemChanged(index)
                }
            }
        }
    }

    companion object {
        private const val EDIT_PENGINAPAN_REQUEST_CODE = 2000
        private const val ADD_PENGINAPAN_REQUEST_CODE = 1000 // Tambahkan ini
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_admin_dashboard, menu) // Menghubungkan menu XML ke toolbar
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                showLogoutConfirmation() // Fungsi untuk logout
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutConfirmation() {
        // Tampilkan dialog konfirmasi
        AlertDialog.Builder(ContextThemeWrapper(this, R.style.CustomAlertDialogTheme))
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin logout?")
            .setPositiveButton("Ya") { _, _ ->
                performLogout() // Fungsi logout
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performLogout() {
        // Hapus session login
        val sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear() // Hapus data session
            apply()
        }

        // Arahkan ke halaman login
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }



}
