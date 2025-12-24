package com.example.tvtvapp.data

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

/**
 * Intercepts API calls and returns mock OMDb JSON data when the network
 * is unavailable or for offline testing.
 */
class MockApiInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        return try {
            // Try actual network
            chain.proceed(request)
        } catch (e: IOException) {
            // Network failed → fallback to mock JSON
            val mockJson = when {
                url.contains("i=") -> MOCK_DETAIL_JSON
                else -> MOCK_SEARCH_JSON
            }

            Response.Builder()
                .code(200)
                .message("Mock Response")
                .request(request)
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .body(mockJson.toResponseBody("application/json".toMediaType()))
                .addHeader("content-type", "application/json")
                .build()
        }
    }

    companion object {
        // ✅ Returns parsed mock movie list for use in HomeFragment fallback
        fun getMockMovies(): List<OMDbMovie> {
            return listOf(
                OMDbMovie(
                    Title = "Interstellar",
                    Year = "2014",
                    imdbID = "tt0816692",
                    Type = "movie",
                    Poster = "https://placehold.co/400x600?text=Interstellar"
                ),
                OMDbMovie(
                    Title = "Inception",
                    Year = "2010",
                    imdbID = "tt1375666",
                    Type = "movie",
                    Poster = "https://placehold.co/400x600?text=Inception"
                ),
                OMDbMovie(
                    Title = "The Dark Knight",
                    Year = "2008",
                    imdbID = "tt0468569",
                    Type = "movie",
                    Poster = "https://placehold.co/400x600?text=Dark+Knight"
                ),
                OMDbMovie(
                    Title = "Dune",
                    Year = "2021",
                    imdbID = "tt1160419",
                    Type = "movie",
                    Poster = "https://placehold.co/400x600?text=Dune"
                ),
                OMDbMovie(
                    Title = "Oppenheimer",
                    Year = "2023",
                    imdbID = "tt15398776",
                    Type = "movie",
                    Poster = "https://placehold.co/400x600?text=Oppenheimer"
                )
            )
        }

        // Mock Search response (for list endpoints)
        private const val MOCK_SEARCH_JSON = """
        {
          "Search": [
            {
              "Title": "Inception",
              "Year": "2010",
              "imdbID": "tt1375666",
              "Type": "movie",
              "Poster": "https://placehold.co/400x600?text=Inception"
            },
            {
              "Title": "Interstellar",
              "Year": "2014",
              "imdbID": "tt0816692",
              "Type": "movie",
              "Poster": "https://placehold.co/400x600?text=Interstellar"
            },
            {
              "Title": "The Dark Knight",
              "Year": "2008",
              "imdbID": "tt0468569",
              "Type": "movie",
              "Poster": "https://placehold.co/400x600?text=Dark+Knight"
            }
          ],
          "totalResults": "3",
          "Response": "True"
        }
        """

        // Mock Detail response (for single movie endpoints)
        private const val MOCK_DETAIL_JSON = """
        {
          "Title": "Inception",
          "Year": "2010",
          "Plot": "A thief who steals corporate secrets through dream-sharing technology must plant an idea in someone's mind.",
          "Poster": "https://placehold.co/1200x600?text=Inception",
          "Genre": "Action, Sci-Fi, Thriller",
          "imdbRating": "8.8"
        }
        """
    }
}
