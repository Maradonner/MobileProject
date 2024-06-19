package org.hse.android

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import models.Comment
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import services.AuthInterceptor
import services.TokenManager
import java.io.IOException

class CommentsAdapter(private val context: Context, private var comments: List<Comment>, private val parentDiscountId: String, private val tokenManager: TokenManager, private val activity: ItemCardActivity, private val onReplyClicked: (Comment) -> Unit) :
    RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

       private lateinit var editTextComment: EditText
        private lateinit var layoutComment: LinearLayout


    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        val tvContent: TextView = view.findViewById(R.id.tvContent)
        val tvCreatedAt: TextView = view.findViewById(R.id.tvCreatedAt)
        val btnReply: Button = view.findViewById(R.id.btnReply)
        val rvReplies: RecyclerView = view.findViewById(R.id.rvReplies)
        val bufLayoutComment: LinearLayout = view.findViewById(R.id.layout_comment)
        val btnSubmitReply: Button = view.findViewById(R.id.btnSubmitComment)
        var bufEditTextComment: EditText = view.findViewById(R.id.editTextComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.tvUserName.text = comment.userName
        holder.tvContent.text = comment.content
        holder.tvCreatedAt.text = comment.createdAt
        layoutComment = holder.bufLayoutComment

        layoutComment.visibility = View.GONE

        editTextComment = holder.bufEditTextComment

        holder.btnReply.setOnClickListener {
            onReplyClicked(comment)
            layoutComment = holder.bufLayoutComment
            editTextComment = holder.bufEditTextComment
            Log.e("COMTEST",comment.id.toString())
            Log.e("COMTEST",comment.replies.toString())
            if (layoutComment.visibility == View.VISIBLE) {
                layoutComment.visibility = View.GONE
            } else {
                layoutComment.visibility = View.VISIBLE
            }
        }

        holder.btnSubmitReply.setOnClickListener {
            Log.e("COMTEST",comment.id.toString())
            Log.e("COMTEST",comment.replies.toString())
            addReply(comment)
        }

        if (comment.replies.isNullOrEmpty()) {
            holder.rvReplies.visibility = View.GONE
        } else {
            holder.rvReplies.visibility = View.VISIBLE
            (context as Activity).runOnUiThread {
                val adapter =
                    CommentsAdapter(context, comment.replies, parentDiscountId, tokenManager, activity, onReplyClicked)
                holder.rvReplies.layoutManager = LinearLayoutManager(holder.itemView.context)
                holder.rvReplies.adapter = adapter
            }
        }
    }

    override fun getItemCount() = comments.size

    private fun addReply(comment: Comment) {
        val commentText: String = editTextComment.text.toString()
        if (!commentText.isEmpty()) {
            comment.id?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    addNewReply(commentText, it)
                }}
        }
        // Сохраните комментарий в базе данных, файле или на сервере в зависимости от ваших требований.
        Log.e("ASD", commentText)

        // Очистить EditText для ввода следующего комментария.
        editTextComment.setText("")

        // Скрыть комментарий контейнер.
        layoutComment.visibility = View.GONE
    }
    private fun addNewReply(content: String, parentCommentId: String) {
        val url = "http://109.68.213.18/api/Comment/add"

        val jsonObject = JSONObject()
        jsonObject.put("content", content)
        jsonObject.put("discountId", parentDiscountId)
        jsonObject.put("parentCommentId", parentCommentId)

        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())


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
                    activity.onCommentsUpdated()
                } else {
                    Log.e("ErrorResponse", response.toString() + requestBody)
                }
            }
        })
    }

    fun updateComments(newComments: List<Comment>) {
        comments = newComments
        notifyDataSetChanged()
    }

    interface OnCommentsUpdatedListener {
        fun onCommentsUpdated()
    }
}
