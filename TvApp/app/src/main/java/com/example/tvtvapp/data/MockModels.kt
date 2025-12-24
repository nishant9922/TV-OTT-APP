package com.example.tvtvapp.data

// 🎬 Banner item shown in top carousel
data class Banner(
    val imdbId: String,   // instead of Int id
    val title: String,
    val tagline: String,
    val imageUrl: String
)

data class Card(
    val imdbId: String,
    val title: String,
    val imageUrl: String,
    val category: String
)


// 📺 A rail or row section (e.g., “Trending Now”)
data class Row(
    val id: Int,
    val title: String,
    val cards: List<Card>
)


// 🏠 Entire home response (banners + all rows)
data class HomeResponse(
    val banners: List<Banner>,
    val rows: List<Row>
)


