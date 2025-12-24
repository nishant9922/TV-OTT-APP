package com.example.tvtvapp.data

import com.google.gson.annotations.SerializedName

data class MovieDetailResponse(
    @SerializedName("Title") val Title: String?,
    @SerializedName("Year") val Year: String?,
    @SerializedName("Plot") val Plot: String?,
    @SerializedName("Poster") val Poster: String?,
    @SerializedName("Genre") val Genre: String?,
    @SerializedName("imdbRating") val imdbRating: String?
)
