package com.example.newheadlineapp

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

//###########################################################################################################
//                                            NewsAPIService                                                #
// Contains interface to call API                                                                           #
// Build HTTP URL with query requested: API & source                                                        #
//###########################################################################################################

//Service to call "news-API"
interface NewsApiService {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("sources") source: String,
        @Query("apiKey") apiKey: String = "1d8a727d621f4949aa4002d9a427145c"
    ): NewsResponse

}

//Build Retrofit client. Converts response from API to JSON using Gson.
object RetrofitClient {

    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val api: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://newsapi.org/v2/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApiService::class.java)
    }
}