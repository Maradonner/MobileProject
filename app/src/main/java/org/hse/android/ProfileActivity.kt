package org.hse.android

import BaseActivity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.Discount
import models.Profile
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import services.TokenManager

class ProfileActivity : BaseActivity() {

    private lateinit var emailTextView: TextView
    private lateinit var postsNumberTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var editButton: Button

    private var adapter: DiscountAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val tokenManager = TokenManager(this)
        val userId = tokenManager.getUserIdFromToken()
        val email = tokenManager.getEmailFromToken()

        emailTextView = findViewById(R.id.profileEmail)
        postsNumberTextView = findViewById(R.id.postsNumber)
        recyclerView = findViewById(R.id.userDiscountListView)

        editButton = findViewById(R.id.editButton)
        editButton.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        emailTextView.text = email

        if (userId == null || email == null) {
            Toast.makeText(this, "Failed to extract user ID or Email from token.", Toast.LENGTH_SHORT).show()
        } else {
            setupRecyclerView()
            fetchProfileData(userId)
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    }

    private fun fetchProfileData(userId: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://109.68.213.18/api/User/$userId/profile")
            .addHeader("accept", "text/plain")
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            val response: Response = client.newCall(request).execute()
            val responseData = response.body?.string()

            if (response.isSuccessful && !responseData.isNullOrEmpty()) {
                val profile = Gson().fromJson(responseData, Profile::class.java)
                withContext(Dispatchers.Main) {
                    displayProfileData(profile)
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Failed to fetch profile data.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayProfileData(profile: Profile) {
        postsNumberTextView.text = profile.totalDiscounts.toString()

        val discounts = profile.discounts // Assuming Profile has a discounts property
        if (discounts != null) {
            adapter = DiscountAdapter(discounts)
            recyclerView.adapter = adapter
        }
    }

    class DiscountAdapter(private val discountList: List<Discount>) :
        RecyclerView.Adapter<DiscountAdapter.DiscountViewHolder>() {

        var onDiscountClickListener: OnDiscountClickListener? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscountViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_discount, parent, false)
            return DiscountViewHolder(view)
        }

        override fun onBindViewHolder(holder: DiscountViewHolder, position: Int) {
            val discount = discountList[position]
            holder.title.text = discount.title
            holder.defuaultPrice.text = String.format("%.3f", discount.defaultPrice) + " ₽"
            holder.discountPrice.text = String.format("%.3f", discount.discountPrice) + " ₽"
            if (discount.country != null) {
                holder.country.text = discount.country.name
            }
            if (discount.shop != null) {
                holder.shop.text = discount.shop.name
            }
            Glide.with(holder.itemView.context)
                .load(discount.imageLink)
                .placeholder(R.drawable.placeholder_image)
                .into(holder.imageView)
            holder.commentsCount.text = discount.commentsCount.toString()
        }

        override fun getItemCount(): Int {
            return discountList.size
        }

        inner class DiscountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
            var imageView: ImageView = itemView.findViewById(R.id.image)
            var title: TextView = itemView.findViewById(R.id.title)
            var defuaultPrice: TextView = itemView.findViewById(R.id.oldPrice)
            var discountPrice: TextView = itemView.findViewById(R.id.newPrice)
            var country: TextView = itemView.findViewById(R.id.country)
            var commentsCount: TextView = itemView.findViewById(R.id.commentCount)
            var shop: TextView = itemView.findViewById(R.id.shop)

            init {
                itemView.setOnClickListener(this)
            }

            override fun onClick(v: View) {
                onDiscountClickListener?.onDiscountClick(discountList[adapterPosition])
            }
        }

        fun interface OnDiscountClickListener {
            fun onDiscountClick(discount: Discount)
        }
    }
}
