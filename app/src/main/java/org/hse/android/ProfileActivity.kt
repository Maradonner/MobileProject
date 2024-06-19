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
        //fetchProfileData()
    }

//    private fun fetchProfileData() {
//        val client = OkHttpClient()
//        val request = Request.Builder()
//            .url("http://109.68.213.18/api/User/f6b1ebed-6b34-4be0-8b79-8dcf51b531c2/profile")
//            .addHeader("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
//            .addHeader("Connection", "keep-alive")
//            .addHeader("Referer", "http://109.68.213.18/swagger/index.html")
//            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
//            .addHeader("accept", "text/plain")
//            .build()
//
//        CoroutineScope(Dispatchers.IO).launch {
//            val response: Response = client.newCall(request).execute()
//            val responseData = response.body?.string()
//
//            if (response.isSuccessful && !responseData.isNullOrEmpty()) {
//                val profile = Gson().fromJson(responseData, Profile::class.java)
//                withContext(Dispatchers.Main) {
//                    displayProfileData(profile)
//                }
//            }
//        }
//    }
//
//    private fun displayProfileData(profile: Profile) {
//        val totalDiscountsTextView: TextView = findViewById(R.id.totalDiscountsTextView)
//        val rankingTextView: TextView = findViewById(R.id.rankingTextView)
//        val highestRatingTextView: TextView = findViewById(R.id.highestRatingTextView)
//        val lowestRatingTextView: TextView = findViewById(R.id.lowestRatingTextView)
//        val averageRatingTextView: TextView = findViewById(R.id.averageRatingTextView)
//        val totalReactionsTextView: TextView = findViewById(R.id.totalReactionsTextView)
//
//        totalDiscountsTextView.text = profile.totalDiscounts.toString()
//        rankingTextView.text = profile.ranking.toString()
//        highestRatingTextView.text = profile.highestRating?.toString() ?: "N/A"
//        lowestRatingTextView.text = profile.lowestRating?.toString() ?: "N/A"
//        averageRatingTextView.text = profile.averageRating?.toString() ?: "N/A"
//        totalReactionsTextView.text = profile.totalReactions.toString()
//    }

}