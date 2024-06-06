package org.hse.android

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import models.Comment

class CommentsAdapter(private val comments: List<Comment>, private val onReplyClicked: (Comment) -> Unit) :
    RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        val tvContent: TextView = view.findViewById(R.id.tvContent)
        val tvCreatedAt: TextView = view.findViewById(R.id.tvCreatedAt)
        val btnReply: Button = view.findViewById(R.id.btnReply)
        val rvReplies: RecyclerView = view.findViewById(R.id.rvReplies)
        val layoutComment: LinearLayout = view.findViewById(R.id.layout_comment)
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

        holder.layoutComment.visibility = View.GONE

        holder.btnReply.setOnClickListener {
            onReplyClicked(comment)
            holder.layoutComment.visibility = View.VISIBLE
        }

        if (comment.replies.isNullOrEmpty()) {
            holder.rvReplies.visibility = View.GONE
        } else {
            holder.rvReplies.visibility = View.VISIBLE
            val adapter = CommentsAdapter(comment.replies, onReplyClicked)
            holder.rvReplies.layoutManager = LinearLayoutManager(holder.itemView.context)
            holder.rvReplies.adapter = adapter
        }
    }

    override fun getItemCount() = comments.size

    private fun addReply() {

    }
}
