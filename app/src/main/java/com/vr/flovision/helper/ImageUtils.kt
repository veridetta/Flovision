package com.vr.flovision.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageUtils {

    fun compressBitmap(bitmap: Bitmap): Bitmap {
        val maxSize = 1024 // Max size in kilobytes
        var quality = 100 // Initial quality value

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        while (outputStream.toByteArray().size / 1024 > maxSize && quality > 0) {
            outputStream.reset() // Clear the output stream
            quality -= 10 // Reduce the quality
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        }
        val compressedBitmap = BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.toByteArray().size)
        outputStream.close()
        return compressedBitmap
    }

    fun createTempImageFile(context: Context): Uri {
        val storageDir = context.cacheDir
        val imageFile = File.createTempFile("temp_image", ".jpg", storageDir)
        return imageFile.toUri()
    }

    // You can add more image-related utility functions here
}
