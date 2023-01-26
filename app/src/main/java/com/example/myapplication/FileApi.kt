package com.example.myapplication

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET

interface FileApi {
    @GET("/wikipedia/commons/3/3f/Fronalpstock_big.jpg")
    suspend fun downloadImage(): Response<ResponseBody>

    companion object {
        val instance by lazy {
            Retrofit.Builder().baseUrl("https://upload.wikimedia.org").build().create(FileApi::class.java)
        }
    }
}