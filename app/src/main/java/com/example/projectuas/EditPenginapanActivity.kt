package com.example.projectuas

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.projectuas.databinding.ActivityEditPenginapanBinding
import com.example.projectuas.model.Penginapan
import com.example.projectuas.model.toEntity
import com.example.projectuas.network.ApiClient
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditPenginapanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditPenginapanBinding
    private lateinit var penginapanDao: PenginapanDao
    private var penginapan: Penginapan? = null
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPenginapanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = PenginapanDatabase.getDatabase(this)
        penginapanDao = db.penginapanDao()

        penginapan = intent.getSerializableExtra("PENGINAPAN_DATA") as? Penginapan
        setupViews()

        penginapan?.let { data ->
            binding.editName.setText(data.nama)
            binding.editLocation.setText(data.lokasi)
            binding.editPrice.setText(data.harga)
            binding.editDescription.setText(data.deskripsi)
            binding.editFacilities.setText(data.fasilitas)
            Glide.with(this)
                .load(data.gambarUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(binding.editImage)
        }

        binding.btnChangeImage.setOnClickListener {
            openImagePicker()
        }

        binding.btnSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            binding.editImage.setImageURI(selectedImageUri)
        }
    }

    private fun saveChanges() {
        penginapan?.id?.let { id ->
            if (selectedImageUri != null) {
                uploadImageAndSaveData(id, selectedImageUri!!)
            } else {
                saveUpdatedData(id, penginapan?.gambarUrl.orEmpty())
            }
        }
    }

    private fun uploadImageAndSaveData(id: String, newImageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val newImageRef = storageRef.child("images/${System.currentTimeMillis()}_penginapan.jpg")

        newImageRef.putFile(newImageUri)
            .addOnSuccessListener {
                newImageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveUpdatedData(id, downloadUri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengupload gambar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUpdatedData(id: String, imageUrl: String) {
        val updatedPenginapan = Penginapan(
            id = id,
            nama = binding.editName.text.toString(),
            lokasi = binding.editLocation.text.toString(),
            harga = binding.editPrice.text.toString(),
            deskripsi = binding.editDescription.text.toString(),
            fasilitas = binding.editFacilities.text.toString(),
            gambarUrl = imageUrl
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Simpan ke Room (hanya ID untuk favorit)
                penginapanDao.insertPenginapan(updatedPenginapan.toEntity())

                // Update ke server
                ApiClient.apiService.updatePenginapan(id.toString(), updatedPenginapan).execute()

                runOnUiThread {
                    Toast.makeText(this@EditPenginapanActivity, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@EditPenginapanActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupViews() {
        binding.editName.setText(penginapan?.nama)
        binding.editLocation.setText(penginapan?.lokasi)
        binding.editPrice.setText(penginapan?.harga)
        binding.editDescription.setText(penginapan?.deskripsi)
        binding.editFacilities.setText(penginapan?.fasilitas)
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}


