package com.vr.flovision.helper

import com.vr.flovision.model.PlantModel
import com.vr.flovision.model.PlantNetModel
import com.vr.flovision.model.SpeciesQuery
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import okhttp3.RequestBody
import retrofit2.http.Query

interface ApiInstance {
    @POST("/v2/identify/all")
    @Multipart
    fun sendImage(
        @Query("include-related-images") includeRelatedImages: Boolean,
        @Query("no-reject") noReject: Boolean,
        @Query("lang") lang: String,
        @Query("api-key") apiKey: String,
        @Part image: MultipartBody.Part
    ): Call<PlantNetModel> // Gantilah ResponseModel dengan model yang sesuai
    @POST("/v2/identify/all")
    @Multipart
    fun sendImageDetail(
        @Query("include-related-images") includeRelatedImages: Boolean,
        @Query("no-reject") noReject: Boolean,
        @Query("lang") lang: String,
        @Query("api-key") apiKey: String,
        @Part image: MultipartBody.Part
    ): Call<SpeciesQuery> // Sesuaikan dengan model yang sesuai dengan respons JSON

}
