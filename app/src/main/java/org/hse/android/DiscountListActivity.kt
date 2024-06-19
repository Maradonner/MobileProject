package org.hse.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import models.Comment;
import models.Discount;
import models.DiscountListResponse;
import models.DiscountResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DiscountListActivity extends AppCompatActivity{
    RecyclerView recyclerView;
    private Button buttonPrevPage;
    private Button buttonNextPage;
    private TextView textViewPagination;

    private DiscountAdapter adapter;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private int currentPage = 1;
    private int totalPages;
    private int pageSize;
    private int totalCount;
    private boolean hasPrevious;
    private boolean hasNext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discount_list);

        buttonPrevPage = findViewById(R.id.buttonPrevPage);
        buttonNextPage = findViewById(R.id.buttonNextPage);
        textViewPagination = findViewById(R.id.textViewPagination);

        buttonNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               nextPage();
            }
        });
        buttonPrevPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousPage();
            }
        });


        recyclerView = findViewById(R.id.discountListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        getDiscounts();

    }

    private void nextPage() {
        if (currentPage < totalPages) {
            currentPage += 1;
            getDiscounts();
        }
        if (currentPage == totalPages) {
            buttonNextPage.setEnabled(false);
        }
        if (currentPage > 1) {
            buttonPrevPage.setEnabled(true);
        }
    }

    private void previousPage() {
        if (currentPage > 1) {
            currentPage -= 1;
            getDiscounts();
        }
        if (currentPage == 1) {
            buttonPrevPage.setEnabled(false);
        }
        if (currentPage < totalPages) {
            buttonNextPage.setEnabled(true);
        }
    }

    private void updateTextViewPagination() {
        textViewPagination.setText(String.valueOf(currentPage)+"/"+String.valueOf(totalPages));
    }


    public void getDiscounts() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\r\n \"currentPage\": "+String.valueOf(currentPage)+"\r\n}");
        Request request = new Request.Builder()
                .url("http://109.68.213.18/api/Discounts/search")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("FAILED", "getDiscounts", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                parseResponse(response);
            }
        });
        updateTextViewPagination();
    }

    public void parseResponse(Response response) {
// Создаем объект Gson
        Gson gson = new Gson();
        ResponseBody body = response.body();
        try {
            if (body == null) {
                return;
            }
            String responseString = body.string();
            Log.d("TEST_PARSE", responseString);

            // Преобразуем ответ в формате JSON в объект Java
            //DiscountResponse discountResponse = gson.fromJson(responseString, DiscountResponse.class);

            // Определяем тип списка объектов Discount
            //Type listType = new TypeToken<List<Discount>>() {}.getType();

// Распарсиваем JSON-массив в список объектов Discount
            DiscountListResponse discountListResponse = gson.fromJson(responseString, DiscountListResponse.class);

            pageSize = discountListResponse.getPageSize();
            totalPages = discountListResponse.getTotalPages();
            totalCount = discountListResponse.getTotalCount();
            hasNext = discountListResponse.getHasNext();
            hasPrevious = discountListResponse.getHasPrevious();
            currentPage = discountListResponse.getCurrentPage();


            List<Discount> discounts = discountListResponse.getItems();


            adapter = new DiscountAdapter(discounts);
            adapter.setOnDiscountClickListener(onDiscountClickListener);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    // Обновите пользовательский интерфейс здесь
                    // Например, обновите список discountListView
                    recyclerView.setAdapter(adapter);
                }
            });


// Делаем что-то с данными
            //for (Discount discount : discounts) {
              //  Log.d("MyApp", "Discount: " + discount.getTitle() + ", " + discount.getDiscountPrice());
            //}

        } catch (Exception e) {
            Log.e("PARSE_RESPONSE", "", e);
        }

    }

    private DiscountAdapter.OnDiscountClickListener onDiscountClickListener = new DiscountAdapter.OnDiscountClickListener() {
        @Override
        public void onDiscountClick(Discount discount) {
            // Запускаем новое activity и передаем туда объект Discount
            Intent intent = new Intent(DiscountListActivity.this, ItemCardActivity.class);
            Gson gson = new Gson();
            String discountJson = gson.toJson(discount);
            // Добавляем JSON-строку в Intent
            Log.d("CheckCOms", discountJson);
            intent.putExtra("discountJson", discountJson);
            startActivity(intent);
        }
    };


    public static final class DiscountAdapter extends RecyclerView.Adapter<DiscountAdapter.DiscountViewHolder> {

        private List<Discount> discountList;

        public DiscountAdapter(List<Discount> discountList) {
            this.discountList = discountList;
        }

        private OnDiscountClickListener onDiscountClickListener;

        @NonNull
        @Override
        public DiscountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_discount, parent, false);
            return new DiscountViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DiscountViewHolder holder, int position) {
            Discount discount = discountList.get(position);
            holder.title.setText(discount.getTitle());
            holder.defuaultPrice.setText(String.valueOf(discount.getDefaultPrice()));
            holder.discountPrice.setText(String.valueOf(discount.getDiscountPrice()));
            if (discount.getCountry() != null) {
                holder.country.setText(String.valueOf(discount.getCountry().getName()));
            }
            if (discount.getShop() != null) {
                holder.shop.setText(String.valueOf(discount.getShop().getName()));
            }
            holder.commentsCount.setText(String.valueOf(discount.getComments().size())); // Сделать получение кол-ва комментов
            Glide.with(holder.itemView.getContext())
                    .load(discount.getImageLink())
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return discountList.size();
        }

        public class DiscountViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView imageView;
            TextView title;
            TextView defuaultPrice;
            TextView discountPrice;
            TextView country;
            TextView commentsCount;
            TextView shop;

            public DiscountViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.image);
                title = itemView.findViewById(R.id.title);
                defuaultPrice = itemView.findViewById(R.id.oldPrice);
                discountPrice = itemView.findViewById(R.id.newPrice);
                country = itemView.findViewById(R.id.country);
                commentsCount = itemView.findViewById(R.id.commentCount);
                shop = itemView.findViewById(R.id.shop);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                // Вызываем метод onDiscountClick() интерфейса OnDiscountClickListener
                if (onDiscountClickListener != null) {
                    onDiscountClickListener.onDiscountClick(discountList.get(getAdapterPosition()));
                }
            }
        }

        public interface OnDiscountClickListener {
            void onDiscountClick(Discount discount);
        }

        public void setOnDiscountClickListener(OnDiscountClickListener onDiscountClickListener) {
            this.onDiscountClickListener = onDiscountClickListener;
        }
    }
}