package com.world.jteam.bonb;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductViewHolder extends RecyclerView.ViewHolder {
    TextView view_midPrice;//Средняя цена
    TextView view_productName;//Наименование
    TextView view_lowPrice;//Разброс цен
    TextView view_textRaiting;//Количество отзывов
    RatingBar view_productRaiting;//Рейтинг
    ImageView picture;


    private ProductViewHolder(View itemView) {
        super(itemView);

        view_midPrice = itemView.findViewById(R.id.midPrice);//Средняя цена
        view_productName = itemView.findViewById(R.id.productName);//Наименование
        view_lowPrice = itemView.findViewById(R.id.lowPrice);//Разброс цен
        view_textRaiting = itemView.findViewById(R.id.textRaiting);//Количество отзывов
        view_productRaiting = itemView.findViewById(R.id.productRaiting);//Рейтинг
        picture = itemView.findViewById(R.id.imageView);


        view_productName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();
                Call<ModelProductFull> serviceCall = mDataApi.getProductFullById((int)v.getTag());
                serviceCall.enqueue(new Callback<ModelProductFull>() {
                    @Override
                    public void onResponse(Call<ModelProductFull> call, Response<ModelProductFull> response) {
                        Context mContext = v.getContext();
                        ModelProductFull ss = response.body();
                        //Context mContext = AppInstance.getAppContext();
                        Intent intent = new Intent(mContext, ViewProduct.class);
                        intent.putExtra("object", ss);
                        mContext.startActivity(intent);

                    }

                    @Override
                    public void onFailure(Call<ModelProductFull> call, Throwable t) {

                    }
                });





            }
        });
    }

    public static ProductViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_list_product, parent, false);
        return new ProductViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    public void bindTo(ModelProduct p) {
        // If placeholders are enabled, Paging will pass null first and then pass the actual data when it's available.
        if (p != null) {
            view_productName.setText(p.name);
            view_productName.setTag(p.id);
            view_midPrice.setText(p.price + "\u20BD");

            view_lowPrice.setText("от " + p.price_min + " до " + p.price_max + "\u20BD");
            view_textRaiting.setText(p.comment_count + " отзывов");
            view_productRaiting.setRating(p.raiting);

            if (p.imageSmall_link == null) {
                picture.setImageResource(R.drawable.ic_action_noimage);
            } else {

                Picasso.with(picture.getContext())
                        .load(Constants.SERVICE_GET_IMAGE + p.imageSmall_link)
                        .placeholder(R.drawable.ic_action_noimage)
                        .error(R.drawable.ic_action_noimage)
                        .into(picture);

            }
        } else {
            view_productName.setText("Loading...");
        }
    }

}
