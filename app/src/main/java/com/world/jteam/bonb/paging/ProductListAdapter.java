package com.world.jteam.bonb.paging;

import android.arch.paging.PagedListAdapter;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.world.jteam.bonb.model.ModelProduct;

public class ProductListAdapter extends PagedListAdapter<ModelProduct, ProductViewHolder> {

    public ProductListAdapter(){
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

