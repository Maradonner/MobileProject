package org.hse.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import okhttp3.Response
import services.AuthInterceptor
import services.TokenManager

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var currentPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmNewPasswordEditText: EditText
    private lateinit var changePasswordButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        currentPasswordEditText = findViewById(R.id.currentPassword)
        newPasswordEditText = findViewById(R.id.newPassword)
        confirmNewPasswordEditText = findViewById(R.id.confirmNewPassword)
        changePasswordButton = findViewById(R.id.changePasswordButton)

        changePasswordButton.setOnClickListener {
            val currentPassword = currentPasswordEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()
            val confirmNewPassword = confirmNewPasswordEditText.text.toString()

            if (newPassword == confirmNewPassword) {
                changePassword(currentPassword, newPassword)
            } else {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun changePassword(currentPassword: String, newPassword: String) {
        val tokenManager = TokenManager(this)
        val client = OkHttpClient()
            .newBuilder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .build()
        val mediaType = "text/plain".toMediaType()
        val body = "".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("http://109.68.213.18/api/Login/change/password?oldPassword=$currentPassword&newPassword=$newPassword")
            .post(body)
            .addHeader("accept", "text/plain")
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            val response: Response = client.newCall(request).execute()

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ChangePasswordActivity, "Password changed successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@ChangePasswordActivity, ProfileActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@ChangePasswordActivity, "Failed. ${response.body?.string()}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
}