package org.hse.android

import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import models.Comment
import models.Discount
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import services.AuthInterceptor
import services.TokenManager
import java.io.IOException


class ItemCardActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var commentsAdapter: CommentsAdapter
    private lateinit var discount: Discount
    private lateinit var buttonAddComment: Button
    private lateinit var buttonSubmitComment: Button
    private lateinit var editTextComment: EditText
    private lateinit var layoutComments: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_card)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        layoutComments = findViewById(R.id.layout_comment)
        editTextComment = findViewById(R.id.editTextComment)
        layoutComments.visibility = View.GONE

        buttonAddComment = findViewById(R.id.btnAddComment)

        buttonAddComment.setOnClickListener {
            layoutComments.visibility = View.VISIBLE
        }

        buttonSubmitComment = findViewById(R.id.btnSubmitComment)

        buttonSubmitComment.setOnClickListener {
            addComment()
        }

        val intent = intent
        val discountJson = intent.getStringExtra("discountJson")
        // Десериализуем JSON-строку в объект Discount
        val gson = Gson()
        discount = gson.fromJson(discountJson, Discount::class.java)
        discount.title?.let { Log.e("TestDiscountIntent", it) }

        val imageView = findViewById<ImageView>(R.id.ivProductImage)
        val title = findViewById<TextView>(R.id.tvProductTitle)
        val description = findViewById<TextView>(R.id.tvProductDescription)
        val defaultPrice = findViewById<TextView>(R.id.tvDefaultPrice)
        val discountPrice = findViewById<TextView>(R.id.tvDiscountPrice)
        title.setText(discount.title)
        description.setText(discount.description)
        defaultPrice.setText(discount.defaultPrice.toString())
        discountPrice.setText(discount.discountPrice.toString())

        val imageUrl = discount.imageLink

        Glide.with(this)
            .load(imageUrl)
            .into(imageView)


        getComments()
    }

    private fun createSampleData(): List<Comment> {
        // Sample data including comments and nested replies
        return listOf(
            Comment(
                id = "1",
                content = "This is the first comment.",
                userId = "user1",
                userName = "User One",
                createdAt = "2024-04-12T22:03:05Z",
                replies = listOf(
                    Comment(
                        id = "2",
                        content = "This is a reply.",
                        userId = "user2",
                        userName = "User Two",
                        createdAt = "2024-04-13T10:15:30Z",
                        replies = null
                    )
                )
            ),
            Comment(
                id = "3",
                content = "This is another comment.",
                userId = "user3",
                userName = "User Three",
                createdAt = "2024-04-14T07:20:45Z",
                replies = listOf(
                    Comment(
                        id = "4",
                        content = "This is another reply.",
                        userId = "user4",
                        userName = "User Four",
                        createdAt = "2024-04-15T09:22:33Z",
                        replies = null
                    ),
                    Comment(
                        id = "5",
                        content = "This is reply 5.",
                        userId = "user4",
                        userName = "User Four",
                        createdAt = "2024-04-15T09:22:33Z",
                        replies = listOf(
                            Comment(
                                id = "6",
                                content = "This is reply 6..",
                                userId = "user6",
                                userName = "User 6",
                                createdAt = "2024-04-15T09:22:33Z",
                                replies = null
                            )
                        )
                    ),
                )
            )
        )
    }

    private fun addComment() {
        val commentText: String = editTextComment.text.toString()
        if (!commentText.isEmpty()) {
            discount.id?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    addNewComment(commentText, it)
                }}
        }
        // Сохраните комментарий в базе данных, файле или на сервере в зависимости от ваших требований.
        Log.e("ASD", commentText)

        // Очистить EditText для ввода следующего комментария.
        editTextComment.setText("")

        // Скрыть комментарий контейнер.
        layoutComments.visibility = View.GONE
    }

    private fun addNewComment(content: String, discountId: String) {
        val url = "http://109.68.213.18/api/Comment/add"

        val jsonObject = JSONObject()
        jsonObject.put("content", content)
        jsonObject.put("discountId", discountId)
        //jsonObject.put("parentCommentId", parentCommentId)

        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val tokenManager = TokenManager(this@ItemCardActivity)

        val client = OkHttpClient()
            .newBuilder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .build()
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                // Обработайте ошибку запроса
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.isSuccessful) {
                    Log.e("SuccessResponse", response.toString())
                    getComments()
                } else {
                    Log.e("ErrorResponse", response.toString())
                }
            }
        })
    }

    private fun submitComment() {

    }

    private fun getComments() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://109.68.213.18/api/Comment/" + discount.id)
            .addHeader("Content-Type", "application/json")
            .build()

        val call = client.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FAILED", "getComments", e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                parseResponse(response)
            }
        })
    }

    private fun parseResponse(response: Response) {

// Создаем объект Gson
        val gson = Gson()
        val body = response.body
        try {
            if (body == null) {
                return
            }
            val responseString = body.string()

            Log.d("TEST_PARSE_comment", responseString)
            Log.d("TEST_COMMENTS_SIZE", discount.comments?.size.toString())

            val listType = object : TypeToken<List<Comment?>?>() {}.type
            val comments = gson.fromJson<List<Comment>>(responseString, listType)

            runOnUiThread {
                commentsAdapter = CommentsAdapter(comments, discount.id!!, TokenManager(this@ItemCardActivity)) {}
                recyclerView.adapter = commentsAdapter
            }

// Делаем что-то с данными
            for (comment in discount.comments!!) {
                Log.d("MyApp", "Comment: " + comment.userName + ", " + comment.content)
            }
        } catch (e: Exception) {
            Log.e("PARSE_RESPONSE", "", e)
        }

    }
}