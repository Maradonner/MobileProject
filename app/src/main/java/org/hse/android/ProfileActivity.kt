package org.hse.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.AuthResponse
import models.Profile
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import services.TokenManager

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val tokenManager = TokenManager(this)
        val userId = tokenManager.getUserIdFromToken()
        val email = tokenManager.getEmailFromToken()

        val emailTextView: TextView = findViewById(R.id.profileEmail)
        emailTextView.text = email

        if (userId == null || email == null){
            Toast.makeText(this, "Failed to extract user ID or Email from token.", Toast.LENGTH_SHORT).show()
        } else {
            fetchProfileData(userId)
        }

    }

    private fun fetchProfileData(userId: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://109.68.213.18/api/User/$userId/profile")
            .addHeader("accept", "text/plain")
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            val response: Response = client.newCall(request).execute()
            val responseData = response.body?.string()

            if (response.isSuccessful && !responseData.isNullOrEmpty()) {
                val profile = Gson().fromJson(responseData, Profile::class.java)
                withContext(Dispatchers.Main) {
                    displayProfileData(profile)
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Failed to fetch profile data.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayProfileData(profile: Profile) {
        val postsNumberTextView: TextView = findViewById(R.id.postsNumber)
        postsNumberTextView.text = profile.totalDiscounts.toString()
    }

}