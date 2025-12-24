package com.example.tvtvapp.data

fun Video.toCard(): Card {
    return Card(
        imdbId = this.id,
        title = this.title,
        imageUrl = this.thumbnailUrl,
        category = "Featured"
    )
}

fun Video.toBanner(): Banner {
    return Banner(
        imdbId = this.id,
        title = this.title,
        tagline = this.duration,      // OR set empty ""
        imageUrl = this.thumbnailUrl
    )
}
