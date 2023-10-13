package com.vr.flovision.api

import com.vr.flovision.helper.ApiInstance
import com.vr.flovision.model.PlantNetModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

fun sendPostRequest(
    url: String,
    imageFilePath: String,
    apiKey: String,
    imageFieldName: String = "images",
    imageMediaType: String = "image/jpeg"
): Deferred<Response<PlantNetModel>> {
    val file = File(imageFilePath)

    val logging = HttpLoggingInterceptor()
    logging.level = HttpLoggingInterceptor.Level.BODY

    val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val service = retrofit.create(ApiInstance::class.java)

    val requestFile = RequestBody.create(imageMediaType.toMediaTypeOrNull(), file)
    val imagePart = MultipartBody.Part.createFormData(imageFieldName, file.name, requestFile)

    return GlobalScope.async(Dispatchers.IO) {
        try {
            val response = service.sendImage(false, false, "id", apiKey, imagePart).execute()
            response
        } catch (e: Exception) {
            null
        }!!
    }
}