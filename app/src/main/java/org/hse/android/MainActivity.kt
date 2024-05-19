package org.hse.android

import android.content.Intent
import android.os.Bundle
import android.view.View
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


        findViewById<Button>(R.id.btnToItemCard).setOnClickListener {
            startItemCardActivity()
        }

        findViewById<Button>(R.id.btnToCreateDiscount).setOnClickListener {
            startCreateDiscountActivity()
        }

        findViewById<Button>(R.id.btnToUploadImage).setOnClickListener {
            startUploadImageActivity()
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
}