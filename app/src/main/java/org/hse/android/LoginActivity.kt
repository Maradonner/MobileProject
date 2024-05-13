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

class LoginActivity : AppCompatActivity() {
    private lateinit var editTextId: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextId = findViewById(R.id.editTextId)
        editTextPassword = findViewById(R.id.editTextTextPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnLogin.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin() {
        val email = editTextId.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val loginSuccess = login(email, password)
            withContext(Dispatchers.Main) {
                if (loginSuccess) {
                    startActivity(Intent(this@LoginActivity, ProfileActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Login failed. Please check your credentials.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun login(email: String, password: String): Boolean {
        val client = OkHttpClient()
        val mediaType = "application/json".toMediaType()
        val body = """
            {
              "email": "$email",
              "password": "$password"
            }
        """.trimIndent().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("http://109.68.213.18/api/Login/login")
            .post(body)
            .addHeader("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
            .addHeader("Content-Type", "application/json")
            .addHeader("Origin", "http://109.68.213.18")
            .addHeader("Accept", "text/plain")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("LoginActivity", "Login failed. Response: ${response.body?.string()}")
                    return false
                }

                val responseBody = response.body?.string() ?: return false
                val authResponse = Gson().fromJson(responseBody, AuthResponse::class.java)
                TokenManager(this@LoginActivity).saveTokens(authResponse.token!!, authResponse.refreshToken!!)
                return true
            }
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error logging in", e)
            return false
        }

    }
}

