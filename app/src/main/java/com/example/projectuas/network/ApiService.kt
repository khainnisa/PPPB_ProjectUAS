package com.example.projectuas.network

import com.example.projectuas.model.Penginapan
import retrofit2.http.*
import retrofit2.Call
import retrofit2.Response

interface ApiService {
    @GET("geuqT/penginapan")
    fun getAllPenginapan(): Call<List<Penginapan>>

    @POST("geuqT/penginapan")
    fun addPenginapan(@Body penginapan: Penginapan): Call<Void>

    @POST("geuqT/penginapan/{id}")
    fun updatePenginapan(
        @Path("id") id: String,
        @Body penginapan: Penginapan
    ): Call<Void>

    @GET("geuqT/penginapan/{id}")
    fun getDetailPenginapan(@Path("id") id: String): Call<Penginapan>

    @GET("geuqT/penginapan/{id}")
    suspend fun getPenginapanById(@Path("id") id: String): Response<Penginapan>

    @DELETE("geuqT/penginapan/{id}")
    fun deletePenginapan(@Path("id") id: String): Call<Void>
}


