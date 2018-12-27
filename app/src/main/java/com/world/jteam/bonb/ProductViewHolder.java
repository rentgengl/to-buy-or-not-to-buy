package com.world.jteam.bonb;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class ProductViewHolder extends RecyclerView.ViewHolder {
    TextView view_midPrice;//Средняя цена
    TextView view_productName;//Наименование
    TextView view_lowPrice;//Разброс цен
    TextView view_textRaiting;//Количество отзывов
    RatingBar view_productRaiting;//Рейтинг
    ImageView picture;
    public View.OnClickListener mListener;


    private ProductViewHolder(View itemView) {
        super(itemView);

        view_midPrice = itemView.findViewById(R.id.midPrice);//Средняя цена
        view_productName = itemView.findViewById(R.id.productName);//Наименование
        view_lowPrice = itemView.findViewById(R.id.lowPrice);//Разброс цен
        view_textRaiting = itemView.findViewById(R.id.textRaiting);//Количество отзывов
        view_productRaiting = itemView.findViewById(R.id.productRaiting);//Рейтинг
        picture = itemView.findViewById(R.id.imageView);
        //itemView.setOnClickListener(mListener);
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
