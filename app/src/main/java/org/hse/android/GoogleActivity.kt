package org.hse.android

import android.content.Intent
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import models.AuthResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import services.TokenManager

class GoogleActivity : AppCompatActivity() {
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private val REQ_ONE_TAP = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google)

        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                .setSupported(true)
                .build())
            .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId("734202120966-rd9lap6e0a9a90us2q2f0p5d35d3l5uv.apps.googleusercontent.com")
                .setFilterByAuthorizedAccounts(false)
                .build())
            .build()

        findViewById<View>(R.id.buttonSignInWithGoogle).setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result ->
                try {
                    startIntentSenderForResult(result.pendingIntent.intentSender, REQ_ONE_TAP, null, 0, 0, 0)
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("MainActivity", "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(this) { e ->
                if (e is ApiException) {
                    Log.e("MainActivity", "Sign in failed with status code: ${e.statusCode} and message: ${e.localizedMessage}")
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_ONE_TAP) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(data!!)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    Log.i("MainActivity", idToken)
                    // Execute network request in the background
                    CoroutineScope(Dispatchers.IO).launch {
                        postToken(idToken)
                    }
                } else {
                    Log.e("MainActivity", "No ID token received.")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Sign in failed", e)
            }
        }
    }

    private fun postToken(idToken: String) {
        val client = OkHttpClient()
        val baseUrl = "http://109.68.213.18/api/Login/login/google"
        val url = "$baseUrl?idToken=$idToken"

        val request = Request.Builder()
            .url(url)
            .post("".toRequestBody())
            .addHeader("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
            .addHeader("Accept", "text/plain")
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful){
            Log.e("MainActivity", "Failed to post token. Status: ${response.code}. Message: ${response.message}. Body: ${response.body?.string()}")
        }

        val responseBody = response.body?.string()
        val gson = Gson()
        val authResponse = gson.fromJson(responseBody, AuthResponse::class.java)
        val tokenManager = TokenManager(this)
        tokenManager.saveTokens(authResponse.token!!, authResponse.refreshToken!!)
        Log.i("MainActivity", "Auth response: $authResponse")
    }
}