package com.enclaveapp.data

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface GiphyApiService {
    @GET("v1/gifs/search")
    suspend fun searchGifs(
        @Query("api_key") apiKey: String,
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("rating") rating: String = "g"
    ): GiphyResponse
}

@JsonClass(generateAdapter = true)
data class GiphyResponse(val data: List<GiphyGif>)

@JsonClass(generateAdapter = true)
data class GiphyGif(val id: String, val images: GiphyImages)

@JsonClass(generateAdapter = true)
data class GiphyImages(val fixed_height: GiphyImageDetail)

@JsonClass(generateAdapter = true)
data class GiphyImageDetail(val url: String)
