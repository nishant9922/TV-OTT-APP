package com.example.tvtvapp.data

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    // Search movies by keyword
    @GET(".")
    fun searchMovies(
        @Query("apikey") apiKey: String,
        @Query("s") query: String
    ): Call<OMDbResponse>

    // Fetch full movie details by ID
    @GET(".")
    fun getMovieDetail(
        @Query("apikey") apiKey: String,
        @Query("i") imdbId: String
    ): Call<OMDbDetail>
}
