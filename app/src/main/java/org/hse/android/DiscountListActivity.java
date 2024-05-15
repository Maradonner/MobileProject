package org.hse.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import models.Discount;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DiscountListActivity extends AppCompatActivity {
    RecyclerView recyclerView;

    private DiscountAdapter adapter;

    private final Handler handler = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discount_list);

        recyclerView = findViewById(R.id.discountListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));



        getDiscounts();
    }

    public void getDiscounts() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\r\n}");
        Request request = new Request.Builder()
                .url("http://109.68.213.18/api/Discounts/search")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("FAILED", "getTime", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                parseResponse(response);
            }
        });
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
            Type listType = new TypeToken<List<Discount>>() {}.getType();

// Распарсиваем JSON-массив в список объектов Discount
            List<Discount> discounts = gson.fromJson(responseString, listType);

            adapter = new DiscountAdapter(discounts);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    // Обновите пользовательский интерфейс здесь
                    // Например, обновите список discountListView
                    recyclerView.setAdapter(adapter);
                }
            });


// Делаем что-то с данными
            for (Discount discount : discounts) {
                Log.d("MyApp", "Discount: " + discount.getTitle() + ", " + discount.getDiscountPrice());
            }

        } catch (Exception e) {
            Log.e("PARSE_RESPONSE", "", e);
        }

    }

    public static final class DiscountAdapter extends RecyclerView.Adapter<DiscountAdapter.DiscountViewHolder> {

        private List<Discount> discountList;

        public DiscountAdapter(List<Discount> discountList) {
            this.discountList = discountList;
        }

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
            Glide.with(holder.itemView.getContext())
                    .load(discount.getImageLink())
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return discountList.size();
        }

        public static class DiscountViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView title;
            TextView defuaultPrice;
            TextView discountPrice;

            public DiscountViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.image);
                title = itemView.findViewById(R.id.title);
                defuaultPrice = itemView.findViewById(R.id.oldPrice);
                discountPrice = itemView.findViewById(R.id.newPrice);
            }
        }
    }
}