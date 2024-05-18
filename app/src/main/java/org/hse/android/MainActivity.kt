package org.hse.android

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnToLogin).setOnClickListener {
            startLoginActivity()
        }

        findViewById<Button>(R.id.btnToRegister).setOnClickListener {
            startRegisterActivity()
        }

        findViewById<Button>(R.id.btnToGoogle).setOnClickListener {
            startGoogleActivity()
        }

        findViewById<Button>(R.id.btnToItemCard).setOnClickListener {
            startItemCardActivity()
        }

        findViewById<Button>(R.id.btnToCreateDiscount).setOnClickListener {
            startCreateDiscountActivity()
        }

        findViewById<Button>(R.id.btnToUploadImage).setOnClickListener {
            startUploadImageActivity()
        }

        findViewById<Button>(R.id.btnToDiscountList).setOnClickListener {
            startDiscountListActivity()
        }

    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun startRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun startGoogleActivity() {
        val intent = Intent(this, GoogleActivity::class.java)
        startActivity(intent)
    }

    private fun startItemCardActivity() {
        val intent = Intent(this, ItemCardActivity::class.java)
        startActivity(intent)
    }

    private fun startCreateDiscountActivity() {
        val intent = Intent(this, CreateDiscountActivity::class.java)
        startActivity(intent)
    }

    private fun startUploadImageActivity() {
        val intent = Intent(this, UploadImageActivity::class.java)
        startActivity(intent)
    }

    private fun startDiscountListActivity() {
        val intent = Intent(this, DiscountListActivity::class.java)
        startActivity(intent)
    }
}