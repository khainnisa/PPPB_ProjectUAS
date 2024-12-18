package com.example.projectuas.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // Konstanta base URL API
    private const val BASE_URL = "https://ppapb-a-api.vercel.app/"

    // OkHttpClient dengan interceptor untuk log
    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    // Retrofit instance
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    // Properti ApiService agar dapat dipanggil di mana saja
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}


