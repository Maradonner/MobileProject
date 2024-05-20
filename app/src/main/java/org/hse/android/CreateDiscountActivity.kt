package org.hse.android

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.Discount
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import services.AuthInterceptor
import services.TokenManager


class CreateDiscountActivity : AppCompatActivity() {
    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var editTextDiscountLink: EditText
    //private lateinit var editTextImageLink: EditText
    private lateinit var editTextDefaultPrice: EditText
    private lateinit var editTextDiscountPrice: EditText
    private lateinit var buttonSubmit: Button
    private lateinit var buttonAddImage: Button

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

        buttonSubmit.setOnClickListener {
            performCreationDiscount()
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
        discount.imageLink = "https://storage.yandexcloud.net/pictures/4db1bbfc-ef54-416f-9ec9-cf6bb4c46cc7"

        val gson = Gson()
        val json = gson.toJson(discount)


        val mediaType = "application/json".toMediaType()
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        Log.e("BODY_FIRST", json)
        CoroutineScope(Dispatchers.IO).launch {
            submitDiscount(body)
        }
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
}