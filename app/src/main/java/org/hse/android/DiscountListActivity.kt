package org.hse.android

import BaseActivity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import models.Discount
import models.DiscountListResponse
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class DiscountListActivity : BaseActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonPrevPage: Button
    private lateinit var buttonNextPage: Button
    private lateinit var textViewPagination: TextView
    private lateinit var editTextSearch: EditText
    private var adapter: DiscountAdapter? = null

    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 1
    private var totalPages = 0
    private var pageSize = 0
    private var totalCount = 0
    private var hasPrevious = false
    private var hasNext = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discount_list)

        buttonPrevPage = findViewById(R.id.buttonPrevPage)
        buttonNextPage = findViewById(R.id.buttonNextPage)
        textViewPagination = findViewById(R.id.textViewPagination)

        buttonNextPage.setOnClickListener { nextPage() }
        buttonPrevPage.setOnClickListener { previousPage() }

        recyclerView = findViewById(R.id.discountListView)
        val layoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        editTextSearch = findViewById(R.id.editTextSearch)
        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter?.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        getDiscounts()

    }

    private fun nextPage() {
        if (currentPage < totalPages) {
            currentPage += 1
            getDiscounts()
        }
        if (currentPage == totalPages) {
            buttonNextPage.isEnabled = false
        }
        if (currentPage > 1) {
            buttonPrevPage.isEnabled = true
        }
    }

    private fun previousPage() {
        if (currentPage > 1) {
            currentPage -= 1
            getDiscounts()
        }
        if (currentPage == 1) {
            buttonPrevPage.isEnabled = false
        }
        if (currentPage < totalPages) {
            buttonNextPage.isEnabled = true
        }
    }

    private fun updateTextViewPagination() {
        textViewPagination.text = "$currentPage/$totalPages"
    }

    fun getDiscounts() {
        val client = OkHttpClient().newBuilder().build()
        val mediaType = "application/json".toMediaType()
        val body = "{\r\n \"currentPage\": $currentPage,\r\n\"pageSize\": 12\r\n}".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("http://109.68.213.18/api/Discounts/search")
            .method("POST", body)
            .addHeader("Content-Type", "application/json")
            .build()

        val call = client.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FAILED", "getDiscounts", e)
            }

            override fun onResponse(call: Call, response: Response) {
                parseResponse(response)
            }
        })

        updateTextViewPagination()
    }

    fun parseResponse(response: Response) {
        val gson = Gson()
        val body = response.body
        try {
            if (body == null) {
                return
            }
            val responseString = body.string()
            Log.d("TEST_PARSE", responseString)

            val discountListResponse = gson.fromJson(responseString, DiscountListResponse::class.java)

            pageSize = discountListResponse.pageSize!!
            totalPages = discountListResponse.totalPages!!
            totalCount = discountListResponse.totalCount!!
            hasNext = discountListResponse.hasNext!!
            hasPrevious = discountListResponse.hasPrevious!!
            currentPage = discountListResponse.currentPage!!

            runOnUiThread {
                updateTextViewPagination()
            }

            val discounts = discountListResponse.items

            adapter = DiscountAdapter(discounts!!)
            adapter?.onDiscountClickListener = onDiscountClickListener

            handler.post {
                recyclerView.adapter = adapter
            }
        } catch (e: Exception) {
            Log.e("PARSE_RESPONSE", "", e)
        }
    }

    private val onDiscountClickListener = DiscountAdapter.OnDiscountClickListener { discount: Discount ->
        val intent = Intent(this@DiscountListActivity, ItemCardActivity::class.java)
        val gson = Gson()
        val discountJson = gson.toJson(discount)
        Log.d("CheckCOms", discountJson)
        intent.putExtra("discountJson", discountJson)
        startActivity(intent)
    }

    class DiscountAdapter(private val discountList: List<Discount>) :
        RecyclerView.Adapter<DiscountAdapter.DiscountViewHolder>() {
        private var filteredDiscountList: List<Discount> = discountList

        var onDiscountClickListener: OnDiscountClickListener? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscountViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_discount, parent, false)
            return DiscountViewHolder(view)
        }

        override fun onBindViewHolder(holder: DiscountViewHolder, position: Int) {
            val discount = discountList[position]
            holder.title.text = discount.title
            holder.defuaultPrice.text = String.format("%.3f", discount.defaultPrice) + " ₽"
            holder.discountPrice.text = String.format("%.3f", discount.discountPrice)+ " ₽"
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
        fun filter(query: String) {
            filteredDiscountList = if (query.isEmpty()) {
                discountList
            } else {
                discountList.filter { discount ->
                    discount.title.contains(query, ignoreCase = true)
                }
            }
            notifyDataSetChanged()
        }
        override fun getItemCount(): Int {
            return discountList.size
        }

        inner class DiscountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
            var imageView: ImageView
            var title: TextView
            var defuaultPrice: TextView
            var discountPrice: TextView
            var country: TextView
            var commentsCount: TextView
            var shop: TextView

            init {
                imageView = itemView.findViewById(R.id.image)
                title = itemView.findViewById(R.id.title)
                defuaultPrice = itemView.findViewById(R.id.oldPrice)
                discountPrice = itemView.findViewById(R.id.newPrice)
                country = itemView.findViewById(R.id.country)
                commentsCount = itemView.findViewById(R.id.commentCount)
                shop = itemView.findViewById(R.id.shop)
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