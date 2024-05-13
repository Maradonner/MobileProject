package services

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

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
}
