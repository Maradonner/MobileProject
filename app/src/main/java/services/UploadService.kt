package services

import android.content.Context
import android.net.Uri
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException

fun uploadImage(context: Context, imageUri: Uri) {
    val client = OkHttpClient()

    // Get real path from URI
    val filePath = getRealPathFromURI(context, imageUri)
    val file = File(filePath)

    val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        .addFormDataPart("file", file.name, file.asRequestBody("image/jpeg".toMediaType()))
        .build()

    val request = Request.Builder()
        .url("http://109.68.213.18/api/UploadImages")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            // Handle the error
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                // Handle the successful response
            } else {
                // Handle the error
            }
        }
    })
}

fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
    val cursor = context.contentResolver.query(contentUri, null, null, null, null)
    return cursor?.use {
        if (it.moveToFirst()) {
            val index = it.getColumnIndexOrThrow("_data")
            it.getString(index)
        } else null
    }
}