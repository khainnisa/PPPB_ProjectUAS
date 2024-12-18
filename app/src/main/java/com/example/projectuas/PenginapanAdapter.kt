package com.example.projectuas

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectuas.databinding.ItemPenginapanBinding
import com.example.projectuas.model.Penginapan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PenginapanAdapter(
    private var listPenginapan: MutableList<Penginapan>,
    private val penginapanDao: PenginapanDao? = null, // Jadikan opsional
    private val onDetailClick: (Penginapan) -> Unit,
    private val onEditClick: ((Penginapan) -> Unit)? = null,
    private val onDeleteClick: ((Penginapan) -> Unit)? = null,
    private val onFavoriteToggled: (() -> Unit)? = null, // Callback untuk refresh data
    private val isFavoritePage: Boolean = false, // Tambahan untuk membedakan halaman
    private val isAdmin: Boolean = false
) : RecyclerView.Adapter<PenginapanAdapter.PenginapanViewHolder>() {

    inner class PenginapanViewHolder(private val binding: ItemPenginapanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val scope = CoroutineScope(Dispatchers.IO)

        fun bind(penginapan: Penginapan) {
            // Set data ke komponen UI
            binding.itemName.text = penginapan.nama ?: "Nama tidak tersedia"
            binding.itemLocation.text = penginapan.lokasi ?: "Lokasi tidak tersedia"
            binding.itemPrice.text = "Rp. ${penginapan.harga ?: "0"} / malam"

            Glide.with(binding.root.context)
                .load(penginapan.gambarUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(binding.itemImage)

            // Reset semua tombol sebelum diatur ulang
            binding.btnFavorite.visibility = View.GONE
            binding.btnEdit.visibility = View.GONE
            binding.btnDelete.visibility = View.GONE
            binding.btnDetail.visibility = View.GONE

            // Jika Admin: Tampilkan Edit, Delete, dan tombol Detail
            if (isAdmin) {
                binding.btnEdit.visibility = View.VISIBLE
                binding.btnDelete.visibility = View.VISIBLE
                binding.btnDetail.visibility = View.VISIBLE

                binding.btnEdit.setOnClickListener { onEditClick?.invoke(penginapan) }
                binding.btnDelete.setOnClickListener { onDeleteClick?.invoke(penginapan) }
                binding.btnDetail.setOnClickListener { onDetailClick(penginapan) }
            } else {
                // Jika User: Tampilkan tombol Favorite
                binding.btnFavorite.visibility = View.VISIBLE

                // Cek status favorit dari Room
                // Cek status favorit hanya jika DAO tersedia
                if (penginapanDao != null) {
                    scope.launch {
                        val isFavorite = penginapanDao.getPenginapanById(penginapan.id ?: "") != null
                        withContext(Dispatchers.Main) {
                            binding.btnFavorite.setImageResource(
                                if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_outline
                            )
                        }
                    }

                    // Toggle favorit
                    binding.btnFavorite.setOnClickListener {
                        scope.launch {
                            try {
                                val favorite = penginapanDao?.getPenginapanById(penginapan.id ?: "")
                                if (favorite == null) {
                                    // Tambahkan ke favorit (Room)
                                    penginapanDao?.insertPenginapan(PenginapanFavorite(penginapan.id ?: ""))
                                    withContext(Dispatchers.Main) {
                                        binding.btnFavorite.setImageResource(R.drawable.ic_favorite)
                                        Toast.makeText(binding.root.context, "Ditambahkan ke favorit", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    // Hapus dari favorit
                                    penginapanDao?.deleteFavoriteById(penginapan.id ?: "")
                                    withContext(Dispatchers.Main) {
                                        binding.btnFavorite.setImageResource(R.drawable.ic_favorite_outline)
                                        Toast.makeText(binding.root.context, "Dihapus dari favorit", Toast.LENGTH_SHORT).show()
                                    }

                                    // Jika berada di halaman favorit, hapus dari list
                                    if (isFavoritePage) {
                                        (listPenginapan as? MutableList)?.remove(penginapan)
                                        withContext(Dispatchers.Main) { notifyDataSetChanged() }
                                    } else {
                                        // Jika di halaman Home, hanya ubah ikon
                                        withContext(Dispatchers.Main) { notifyItemChanged(adapterPosition) }
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("FAVORITE_ERROR", "Kesalahan: ${e.message}")
                            }
                        }
                    }
                }

                // Klik judul untuk melihat detail
                binding.itemName.setOnClickListener { onDetailClick(penginapan) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PenginapanViewHolder {
        val binding = ItemPenginapanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PenginapanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PenginapanViewHolder, position: Int) {
        holder.bind(listPenginapan[position])
    }

    override fun getItemCount(): Int = listPenginapan.size

    fun updateData(newData: List<Penginapan>) {
        listPenginapan.clear() // Kosongkan data lama
        listPenginapan.addAll(newData) // Tambahkan data baru
        notifyDataSetChanged() // Refresh RecyclerView
    }

    fun resetData() {
        listPenginapan = mutableListOf()
        notifyDataSetChanged()
    }
}
