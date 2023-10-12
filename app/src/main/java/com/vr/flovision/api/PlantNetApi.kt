package com.vr.flovision.api

import com.google.gson.Gson
import com.vr.flovision.model.PlantNetModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException

fun sendPostRequest(
    url: String,
    imageFilePath: String,
    apiKey: String,
    imageFieldName: String = "images",
    imageMediaType: String = "image/jpeg"
): PlantNetModel? {
    val file = File(imageFilePath)

    val client = OkHttpClient()

    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            imageFieldName,
            file.name,
            RequestBody.create(imageMediaType.toMediaTypeOrNull(), file)
        )
        .build()

    val request = Request.Builder()
        .url(url)
        .addHeader("accept", "application/json")
        .addHeader("Content-Type", "multipart/form-data")
        .addHeader("api-key", apiKey)
        .post(requestBody)
        .build()

    val response = client.newCall(request).execute()

    if (response.isSuccessful) {
        val responseBody = response.body
        if (responseBody != null) {
            val responseString = responseBody.string()
            return Gson().fromJson(responseString, PlantNetModel::class.java)
        }
    }

    return null
}
