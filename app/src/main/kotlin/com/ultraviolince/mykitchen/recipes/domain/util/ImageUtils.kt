package com.ultraviolince.mykitchen.recipes.domain.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

object ImageUtils {
    
    /**
     * Copies an image from URI to internal storage and returns the internal file path
     */
    fun copyImageToInternalStorage(context: Context, imageUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            val filename = "recipe_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, filename)
            
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.close()
            
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Deletes an image file from internal storage
     */
    fun deleteImageFromInternalStorage(imagePath: String): Boolean {
        return try {
            val file = File(imagePath)
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}