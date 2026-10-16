package com.sahasra.app.data.remote

import com.sahasra.app.data.remote.dto.AniListGraphQLResponse
import com.sahasra.app.data.remote.dto.AniListMediaContainer
import com.sahasra.app.data.remote.dto.AniListPageContainer
import com.sahasra.app.data.remote.dto.GraphQLRequestBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface AniListApi {

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("/")
    suspend fun queryPage(
        @Body body: GraphQLRequestBody
    ): AniListGraphQLResponse<AniListPageContainer>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("/")
    suspend fun queryMedia(
        @Body body: GraphQLRequestBody
    ): AniListGraphQLResponse<AniListMediaContainer>

    companion object {
        private const val BASE_URL = "https://graphql.anilist.co"

        fun create(baseUrl: String = BASE_URL): AniListApi {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AniListApi::class.java)
        }
    }
}
