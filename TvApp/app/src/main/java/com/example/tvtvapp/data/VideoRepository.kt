package com.example.tvtvapp.data

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VideoRepository {

    fun fetchVideos(
        onSuccess: (List<Video>) -> Unit,
        onError: () -> Unit
    ) {
        VideoApiClient.api.getAllVideos()
            .enqueue(object : Callback<List<Video>> {
                override fun onResponse(
                    call: Call<List<Video>>,
                    response: Response<List<Video>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        onSuccess(response.body()!!)
                    } else {
                        onError()
                    }
                }

                override fun onFailure(call: Call<List<Video>>, t: Throwable) {
                    onError()
                }
            })
    }
}
