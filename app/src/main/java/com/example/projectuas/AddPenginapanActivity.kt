package com.example.projectuas

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.projectuas.databinding.ActivityAddPenginapanBinding
import com.example.projectuas.model.Penginapan
import com.example.projectuas.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddPenginapanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPenginapanBinding
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPenginapanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tombol pilih gambar
        binding.btnSelectImage.setOnClickListener {
            openImagePicker()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                REQUEST_CODE_PERMISSION
            )
        }


        // Tombol simpan
        binding.btnSavePenginapan.setOnClickListener {
            savePenginapan()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Izin diberikan, Anda bisa memilih gambar.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Izin ditolak, tidak dapat memilih gambar.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fungsi untuk membuka galeri gambar
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    // Callback ketika gambar dipilih
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {

            selectedImageUri = data?.data
            Log.d("UPLOAD_IMAGE", "Selected Image URI: $selectedImageUri")
            Glide.with(this)
                .load(selectedImageUri)
                .into(binding.addImagePreview) // Menampilkan gambar di ImageView
        }
    }

    private fun uploadImageToFirebase(uri: Uri, onSuccess: (String) -> Unit) {
        val storageRef: StorageReference = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${System.currentTimeMillis()}_penginapan.jpg")

        imageRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onSuccess(downloadUri.toString()) // Kirim URL ke server atau simpan
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengupload gambar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Fungsi untuk menyimpan data penginapan
    private fun savePenginapan() {
        val nama = binding.addName.text.toString().trim()
        val lokasi = binding.addLocation.text.toString().trim()
        val harga = binding.addPrice.text.toString().trim()
        val deskripsi = binding.addDescription.text.toString().trim()
        val fasilitas = binding.addFacilities.text.toString().trim()

        // Validasi input
        if (nama.isEmpty() || lokasi.isEmpty() || harga.isEmpty() || deskripsi.isEmpty() || fasilitas.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri != null) {
            // Upload gambar ke Firebase jika ada gambar yang dipilih
            uploadImageToFirebase(selectedImageUri!!) { imageUrl ->
                // Setelah berhasil upload, panggil fungsi untuk menyimpan ke API
                saveToApi(nama, lokasi, harga, deskripsi, fasilitas, imageUrl)
            }
        } else {
            // Jika tidak ada gambar, gunakan URL placeholder
            saveToApi(nama, lokasi, harga, deskripsi, fasilitas, "https://placeholder.com/image.png")
        }
    }

    // Fungsi untuk menyimpan data ke API
    private fun saveToApi(
        nama: String,
        lokasi: String,
        harga: String,
        deskripsi: String,
        fasilitas: String,
        imageUrl: String
    ) {
        val newPenginapan = Penginapan(
            id = null,
            nama = nama,
            lokasi = lokasi,
            harga = harga,
            deskripsi = deskripsi,
            fasilitas = fasilitas,
            gambarUrl = imageUrl
        )

        ApiClient.apiService.addPenginapan(newPenginapan).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AddPenginapanActivity, "Data berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@AddPenginapanActivity, "Gagal menambahkan data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@AddPenginapanActivity, "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    companion object {
        private const val IMAGE_PICK_CODE = 1000
        private const val REQUEST_CODE_PERMISSION = 2000
    }
}
