package com.example.projectuas

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.projectuas.databinding.ActivityDetailPenginapanBinding
import com.example.projectuas.model.Penginapan

class DetailPenginapanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailPenginapanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPenginapanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil data dari intent
        val penginapan = intent.getSerializableExtra("PENGINAPAN_DATA") as? Penginapan

        // Tampilkan data ke layout
        penginapan?.let { data ->
            // Nama Penginapan
            binding.detailName.text = data.nama ?: "Nama tidak tersedia"

            // Lokasi Penginapan
            binding.detailLocation.text = data.lokasi ?: "Lokasi tidak tersedia"

            // Harga Penginapan
            binding.detailPrice.text = "Rp. ${data.harga ?: "0"}"

            // Deskripsi
            binding.detailDescription.text = data.deskripsi ?: "Deskripsi tidak tersedia"

            // Fasilitas
            binding.detailFacilities.text = data.fasilitas ?: "Fasilitas tidak tersedia"

            // Gambar menggunakan Glide
            Glide.with(this)
                .load(data.gambarUrl)
                .placeholder(R.drawable.placeholder_image) // Placeholder jika URL kosong
                .error(R.drawable.placeholder_image)      // Jika gagal memuat gambar
                .into(binding.detailImage)
        }

        // Tombol Kembali
        binding.btnBack.setOnClickListener {
            finish() // Menutup aktivitas ini dan kembali ke sebelumnya
        }
    }
}
