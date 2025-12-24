package com.example.tvtvapp.data

import com.google.gson.annotations.SerializedName

data class OMDbResponse(
    @SerializedName("Search") val Search: List<OMDbMovie>?,
    @SerializedName("totalResults") val totalResults: String?,
    @SerializedName("Response") val Response: String?
)

data class OMDbMovie(
    @SerializedName("Title") val Title: String?,
    @SerializedName("Year") val Year: String?,
    @SerializedName("imdbID") val imdbID: String?,
    @SerializedName("Type") val Type: String?,
    @SerializedName("Poster") val Poster: String?
)

data class OMDbDetail(
    @SerializedName("Title") val Title: String?,
    @SerializedName("Year") val Year: String?,
    @SerializedName("Plot") val Plot: String?,
    @SerializedName("Poster") val Poster: String?,
    @SerializedName("Genre") val Genre: String?,
    @SerializedName("imdbRating") val imdbRating: String?
)
