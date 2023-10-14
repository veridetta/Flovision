package com.vr.flovision.api

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import com.vr.flovision.helper.ApiInstance
import com.vr.flovision.model.PlantNetModel
import com.vr.flovision.model.SpeciesQuery
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
    context: Context,
    url: String,
    imageFilePath: String,
    apiKey: String,
    isFile:Boolean,
    imageFieldName: String = "images",
    imageMediaType: String = "image/jpeg"
): Deferred<Response<PlantNetModel>?> {
    lateinit var file : File
    if(isFile){
        val folder = File( context.getExternalFilesDir("images"), "media")
        val files = folder.listFiles()
        //cek file ada tidak
        if(files != null) {
            for (filex in files) {
                if (imageFilePath == filex.name) {
                    file = filex
                    Log.d("File", "Nama file : ${file.name} dengan lokasi ${file.absolutePath}}")
                }
            }
        }
    }else{
        file = File(imageFilePath)
    }

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
            Log.e("Kenapa null", "Error sending POST request: ${e.message}")
            return@async null
        }
    }
}

fun sendPostRequestDetail(
    context: Context,
    url: String,
    imageFilePath: String,
    apiKey: String,
    isFile: Boolean,
    imageFieldName: String = "images",
    imageMediaType: String = "image/jpeg"
): Deferred<Response<SpeciesQuery>?> {
    lateinit var file: File
    if (isFile) {
        val folder = File(context.getExternalFilesDir("images"), "media")
        val files = folder.listFiles()
        // Cek apakah file ada
        if (files != null) {
            for (filex in files) {
                if (imageFilePath == filex.name) {
                    file = filex
                    Log.d("File", "Nama file : ${file.name} dengan lokasi ${file.absolutePath}")
                }
            }
        }
    } else {
        file = File(imageFilePath)
    }

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
            val response = service.sendImageDetail(false, false, "id", apiKey, imagePart).execute()
            response
        } catch (e: Exception) {
            Log.e("Kenapa null", "Error sending POST request: ${e.message}")
            return@async null
        }
    }
}
