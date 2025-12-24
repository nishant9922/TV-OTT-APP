package com.example.tvtvapp.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object VideoApiClient {

    private const val BASE_URL =
        "https://gist.githubusercontent.com/poudyalanil/ca84582cbeb4fc123a13290a586da925/raw/14a27bd0bcd0cd323b35ad79cf3b493dddf6216b/"

    val api: VideoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VideoApiService::class.java)
    }
}
