package services

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.auth0.android.jwt.JWT

fun provideEncryptedSharedPreferences(context: Context): SharedPreferences {
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    return EncryptedSharedPreferences.create(
        context,
        "secure_app_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

class TokenManager(context: Context) {

    private val sharedPreferences = provideEncryptedSharedPreferences(context)

    fun saveTokens(token: String, refreshToken: String) {
        sharedPreferences.edit().apply {
            putString("token", token)
            putString("refreshToken", refreshToken)
            apply()
        }
    }

    fun getAccessToken(): String? = sharedPreferences.getString("token", null)

    fun getRefreshToken(): String? = sharedPreferences.getString("refreshToken", null)

    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }

    fun logout() {
        sharedPreferences.edit().clear().apply()
    }

    fun getUserIdFromToken(): String? {
        val token = getAccessToken() ?: return null
        return try {
            val jwt = JWT(token)
            jwt.getClaim("Id").asString()
        } catch (e: Exception) {
            null
        }
    }

    fun getEmailFromToken(): String? {
        val token = getAccessToken() ?: return null
        return try {
            val jwt = JWT(token)
            jwt.getClaim("Email").asString()
        } catch (e: Exception) {
            null
        }
    }
}