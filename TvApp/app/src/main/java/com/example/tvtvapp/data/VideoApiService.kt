package com.example.tvtvapp.data

import retrofit2.Call
import retrofit2.http.GET

interface VideoApiService {

    @GET("videos.json")
    fun getAllVideos(): Call<List<Video>>
}
