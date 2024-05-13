package org.hse.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import services.AuthInterceptor
import services.TokenManager

class CreateDiscountActivity : AppCompatActivity() {
    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var editTextDiscountLink: EditText
    private lateinit var editTextImageLink: EditText
    private lateinit var editTextDefaultPrice: EditText
    private lateinit var editTextDiscountPrice: EditText
    private lateinit var buttonSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_discount)

        editTextTitle = findViewById(R.id.editTextTitle)
        editTextDescription = findViewById(R.id.editTextDescription)
        editTextDiscountLink = findViewById(R.id.editTextDiscountLink)
//        editTextImageLink = findViewById(R.id.editTextImageLink)
//        editTextDefaultPrice = findViewById(R.id.editTextDefaultPrice)
//        editTextDiscountPrice = findViewById(R.id.editTextDiscountPrice)
//        buttonSubmit = findViewById(R.id.buttonSubmit)

        buttonSubmit.setOnClickListener {
            performCreationDiscount()
        }
    }

    private fun performCreationDiscount() {
        val title = editTextTitle.text.toString().trim()
        val description = editTextDescription.text.toString().trim()
        val discountLink = editTextDiscountLink.text.toString().trim()
        val imageLink = editTextImageLink.text.toString().trim()
        val defaultPrice = editTextDefaultPrice.text.toString().trim()
        val discountPrice = editTextDiscountPrice.text.toString().trim()

        if (title.isEmpty() || description.isEmpty() || defaultPrice.isEmpty() || discountPrice.isEmpty()) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        val mediaType = "application/json".toMediaType()
        val body = """
            {
              "title": "$title",
              "description": "$description",
              "discountLink": "$discountLink",
              "imageLink": "$imageLink",
              "defaultPrice": $defaultPrice,
              "discountPrice": $discountPrice
            }
        """.trimIndent().toRequestBody(mediaType)

        CoroutineScope(Dispatchers.IO).launch {
            submitDiscount(body)
        }
    }

    private suspend fun submitDiscount(body: okhttp3.RequestBody) {
        val tokenManager = TokenManager(this@CreateDiscountActivity)
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