package org.hse.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import models.Comment

class ItemCardActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var commentsAdapter: CommentsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_card)

        val imageView = findViewById<ImageView>(R.id.ivProductImage)
        val imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/Cat_August_2010-4.jpg/1200px-Cat_August_2010-4.jpg"

        Glide.with(this)
            .load(imageUrl)
            .into(imageView)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val commentsData = createSampleData()
        commentsAdapter = CommentsAdapter(commentsData) { comment ->
            Toast.makeText(this, "Reply to: ${comment.userName}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = commentsAdapter
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
}