package com.world.jteam.bonb.model;

import android.support.v7.util.DiffUtil;

import com.world.jteam.bonb.server.HTTPService;

import java.io.IOException;

public class ModelProduct {
    public int id;
    public int price = 0;
    public int price_min = 0;
    public int price_max = 0;
    public byte sale; //Признак распродажи
    public int comment_count = 0;
    public int userID = 0;
    public String name;
    public String EAN;
    public String imageSmall_link;//Ссылка на маленькую основную привьюху
    public String newComment;
    public float rating = 0;
    public byte user_leave_comment = 0; //Признак что пользователь оставил отзыв
    public int count; //Количество продукта
    public int shoppingListCount; //Количество продукта в списке покупок
    public byte purchased; //Признак купленного товара
    public byte inShoppingList; //Признак налия в списке товаров

    public ModelProduct() {
    }

    //Проверка на изменение позиции
    public static final DiffUtil.ItemCallback<ModelProduct> DIFF_CALLBACK = new DiffUtil.ItemCallback<ModelProduct>() {

        // Check if items represent the same thing.
        @Override
        public boolean areItemsTheSame(ModelProduct oldItem, ModelProduct newItem) {
            return oldItem.id == newItem.id;
        }

        // Checks if the item contents have changed.
        @Override
        public boolean areContentsTheSame(ModelProduct oldItem, ModelProduct newItem) {
            return true; // Assume Repository details don't change
        }
    };

}
