package org.hse.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.AuthResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import services.TokenManager

class RegisterActivity : AppCompatActivity() {
    private lateinit var editTextId: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        editTextId = findViewById(R.id.editTextId)
        editTextPassword = findViewById(R.id.editTextTextPassword)
        btnLogin = findViewById(R.id.btnRegister)
        btnLogin.setOnClickListener {
            register()
        }
    }

    fun register() {
        val email = editTextId.text.toString()
        val password = editTextPassword.text.toString()

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Please enter both ID and password", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val registerSuccess = register(email, password)
            withContext(Dispatchers.Main) {
                if (registerSuccess) {
                    startActivity(Intent(this@RegisterActivity, ProfileActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@RegisterActivity, "Register failed. Please check your credentials.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun register(email: String, password: String): Boolean {
        val client = OkHttpClient()
        val mediaType = "application/json".toMediaType()
        val requestBody = """
            {
                "email": "$email",
                "password": "$password"
            }
        """.trimIndent().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("http://109.68.213.18/api/Login/register")
            .post(requestBody)
            .addHeader("Accept", "text/plain")
            .addHeader("Content-Type", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("RegisterActivity", "Register failed. Response: ${response.body?.string()}")
                    return false
                }


                val responseBody = response.body?.string() ?: return false
                val authResponse = Gson().fromJson(responseBody, AuthResponse::class.java)
                TokenManager(this@RegisterActivity).saveTokens(authResponse.token!!, authResponse.refreshToken!!)
                return true
            }
        } catch (e: Exception) {
            Log.e("RegisterActivity", "Error register", e)
            return false
        }
    }
}