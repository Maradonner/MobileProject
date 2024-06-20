package org.hse.android

import BaseActivity
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import services.FileUtils
import java.io.File
import android.Manifest
import android.os.Build
import services.AuthInterceptor
import services.TokenManager


class UploadImageActivity : BaseActivity() {
    private lateinit var imageViewSelected: ImageView
    private lateinit var buttonSelectImage: Button
    private lateinit var buttonUpload: Button
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_image)

        imageViewSelected = findViewById(R.id.imageViewSelected)
        buttonSelectImage = findViewById(R.id.buttonSelectImage)
        buttonUpload = findViewById(R.id.buttonUpload)

        buttonSelectImage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Use new permission for Android 13 and above
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), REQUEST_PERMISSION)
                } else {
                    selectImage()
                }
            } else {
                // Fallback on the general read external storage permission for earlier Android versions
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION)
                } else {
                    selectImage()
                }
            }
        }

        buttonUpload.setOnClickListener {
            imageUri?.let { uri ->
                uploadImage(uri)
            } ?: Toast.makeText(this, "Сначала выберите изображение", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                selectImage()
            } else {
                // Permission denied
                Toast.makeText(this, "Нет доступа к вашему Внутреннему Хранилищу", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            imageViewSelected.setImageURI(imageUri)
        }
    }

    private fun uploadImage(uri: Uri) {
        val filePath = FileUtils.getPath(this, imageUri!!)
        val file = File(filePath)

        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("image/jpeg".toMediaType()))
            .build()

        val tokenManager = TokenManager(this@UploadImageActivity)
        val client = OkHttpClient()
            .newBuilder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .build()

        val request = Request.Builder()
            .url("http://109.68.213.18/api/UploadImages")
            .post(requestBody)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.newCall(request).execute().use { response ->
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@UploadImageActivity, "Изображение успешно загружено!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@UploadImageActivity, "Не удалось загрузить изображение", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UploadImageActivity, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1
        private const val REQUEST_PERMISSION = 2
    }
}