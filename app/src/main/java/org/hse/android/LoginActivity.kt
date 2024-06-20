package org.hse.android

import BaseActivity
import android.content.Intent
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
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

class LoginActivity : BaseActivity() {
    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private val REQ_ONE_TAP = 2
    private lateinit var signupRedirectText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Email/Password login views
        editTextUsername = findViewById(R.id.login_username)
        editTextPassword = findViewById(R.id.login_password)
        signupRedirectText = findViewById(R.id.signupRedirectText)

        btnLogin = findViewById(R.id.login_button)
        btnLogin.setOnClickListener {
            performLogin()
        }

        signupRedirectText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Google sign-in setup
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
            signInWithGoogle()
        }
    }

    private fun performLogin() {
        val email = editTextUsername.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Введите логин и пароль!", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val loginSuccess = login(email, password)
            withContext(Dispatchers.Main) {
                if (loginSuccess) {
                    startActivity(Intent(this@LoginActivity, ProfileActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Авторизация не удалась. Проверьте введенные данные.", Toast.LENGTH_LONG).show()
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

    private fun signInWithGoogle() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result ->
                try {
                    startIntentSenderForResult(result.pendingIntent.intentSender, REQ_ONE_TAP, null, 0, 0, 0)
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("LoginActivity", "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(this) { e ->
                if (e is ApiException) {
                    Log.e("LoginActivity", "Sign in failed with status code: ${e.statusCode} and message: ${e.localizedMessage}")
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
                    Log.i("LoginActivity", idToken)
                    // Execute network request in the background
                    CoroutineScope(Dispatchers.IO).launch {
                        val loginSuccess = postToken(idToken)
                        withContext(Dispatchers.Main) {
                            if (loginSuccess) {
                                startActivity(Intent(this@LoginActivity, ProfileActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this@LoginActivity, "Авторизация не удалась.", Toast.LENGTH_LONG).show()
                            }
                        }

                    }
                } else {
                    Log.e("LoginActivity", "No ID token received.")
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Sign in failed", e)
            }
        }
    }

    private fun postToken(idToken: String) : Boolean {
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

        if (!response.isSuccessful) {
            Log.e("LoginActivity", "Failed to post token. Status: ${response.code}. Message: ${response.message}. Body: ${response.body?.string()}")
            return false
        }

        val responseBody = response.body?.string()
        val gson = Gson()
        val authResponse = gson.fromJson(responseBody, AuthResponse::class.java)
        val tokenManager = TokenManager(this)
        tokenManager.saveTokens(authResponse.token!!, authResponse.refreshToken!!)
        Log.i("LoginActivity", "Auth response: $authResponse")
        return true
    }
}
