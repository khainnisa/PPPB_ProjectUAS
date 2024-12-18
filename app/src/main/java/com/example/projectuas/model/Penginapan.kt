package com.example.projectuas.model

import com.example.projectuas.PenginapanFavorite
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Penginapan(
    @SerializedName("_id") val id: String?, // Mapping field "_id" dari API
    @SerializedName("nama") val nama: String,
    @SerializedName("lokasi") val lokasi: String,
    @SerializedName("harga") val harga: String,
    @SerializedName("deskripsi") val deskripsi: String,
    @SerializedName("fasilitas") val fasilitas: String,
    @SerializedName("gambarUrl") val gambarUrl: String
) : Serializable

fun Penginapan.toEntity(): PenginapanFavorite {
    return PenginapanFavorite(id = this.id ?: "")
}





