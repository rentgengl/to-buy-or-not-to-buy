package com.world.jteam.bonb;

import android.arch.paging.PagedListAdapter;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

public class ProductListAdapter extends PagedListAdapter<ModelProduct, ProductViewHolder> {


    ProductListAdapter(){
        super(ModelProduct.DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ProductViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bindTo(getItem(position));
    }

}

