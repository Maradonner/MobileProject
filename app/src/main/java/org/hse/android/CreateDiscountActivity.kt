package org.hse.android

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.Discount
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import services.AuthInterceptor
import services.FileUtils
import services.TokenManager
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter


class CreateDiscountActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var editTextDiscountLink: EditText
    //private lateinit var editTextImageLink: EditText
    private lateinit var editTextDefaultPrice: EditText
    private lateinit var editTextDiscountPrice: EditText
    private lateinit var buttonSubmit: Button
    private lateinit var buttonAddImage: Button
    private lateinit var imageLink: String
    private var imageUri: Uri? = null
    private lateinit var imageViewSelected: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_discount)

        editTextTitle = findViewById(R.id.editTextTitle)
        editTextDescription = findViewById(R.id.editTextDescription)
        editTextDiscountLink = findViewById(R.id.editTextDiscountLink)
        //editTextImageLink = findViewById(R.id.editTextImageLink)
        editTextDefaultPrice = findViewById(R.id.editTextDefaultPrice)
        editTextDiscountPrice = findViewById(R.id.editTextDiscountPrice)
        buttonSubmit = findViewById(R.id.buttonSubmit)
        buttonAddImage = findViewById(R.id.buttonAddImage)
        imageViewSelected = findViewById(R.id.imageView)

        buttonSubmit.setOnClickListener {
            performCreationDiscount()
        }

        buttonAddImage.setOnClickListener {
            getImageFromDevice()
        }

        buttonAddImage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Use new permission for Android 13 and above
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                        Companion.REQUEST_PERMISSION
                    )
                } else {
                    selectImage()
                }
            } else {
                // Fallback on the general read external storage permission for earlier Android versions
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        Companion.REQUEST_PERMISSION
                    )
                } else {
                    selectImage()
                }
            }
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, Companion.REQUEST_IMAGE_PICK)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Companion.REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                selectImage()
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Companion.REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            imageViewSelected.setImageURI(imageUri)
            imageUri?.let { uploadImage(it) }
        }
    }


    private fun uploadImage(uri: Uri) {
        val filePath = FileUtils.getPath(this, imageUri!!)
        val file = File(filePath)


        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("image/jpeg".toMediaType()))
            .build()

        val tokenManager = TokenManager(this@CreateDiscountActivity)
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
                        //Log.e("ImageLinkin", response.body?.string() + response.message)
                        if (response.isSuccessful) {
                            Toast.makeText(this@CreateDiscountActivity, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                            imageLink = response.body!!.string()
                            //Log.e("TEST", response.body.toString())
                        } else {
                            Toast.makeText(this@CreateDiscountActivity, "Failed to upload image", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ImageLinkinError", e.toString())
                    Toast.makeText(this@CreateDiscountActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    private fun performCreationDiscount() {
        val title = editTextTitle.text.toString().trim()
        val description = editTextDescription.text.toString().trim()
        val discountLink = editTextDiscountLink.text.toString().trim()
        //val imageLink = editTextImageLink.text.toString().trim()
        val defaultPrice = editTextDefaultPrice.text.toString().trim()
        val discountPrice = editTextDiscountPrice.text.toString().trim()

        if (title.isEmpty() || description.isEmpty() || defaultPrice.isEmpty() || discountPrice.isEmpty()) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
            return
        }
        val discount: Discount = Discount()
        discount.title = title
        discount.description = description
        discount.discountLink = discountLink
        //val imageLink = editTextImageLink.text.toString().trim()
        discount.defaultPrice = defaultPrice.toDouble()
        discount.discountPrice = discountPrice.toDouble()
        //discount.imageLink = "https://storage.yandexcloud.net/pictures/4db1bbfc-ef54-416f-9ec9-cf6bb4c46cc7"
        discount.imageLink = imageLink

        val gson = Gson()
        val json = gson.toJson(discount)


        val mediaType = "application/json".toMediaType()
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        Log.e("BODY_FIRST", json)
        CoroutineScope(Dispatchers.IO).launch {
            submitDiscount(body)
        }
    }

    private fun getImageFromDevice() {

    }

    private suspend fun submitDiscount(body: RequestBody) {
        val tokenManager = TokenManager(this@CreateDiscountActivity)
        Log.e("BODY", body.toString())
        val client = OkHttpClient()
            .newBuilder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .build()

        val request = Request.Builder()
            .url("http://109.68.213.18/api/Discounts/create")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CreateDiscountActivity, "Discount created successfully: $responseBody", Toast.LENGTH_LONG).show()
                    } else {
                        Log.e("ErrorResponse", responseBody.toString())
                        Toast.makeText(this@CreateDiscountActivity, "Failed to create discount: $responseBody", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@CreateDiscountActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
            Log.e("CreateDiscountActivity", "Error:", e)
        }
    }

    companion object {
        private const val REQUEST_PERMISSION = 2
        private const val REQUEST_IMAGE_PICK = 1
    }
}