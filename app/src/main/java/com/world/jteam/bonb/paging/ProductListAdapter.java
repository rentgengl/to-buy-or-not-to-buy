package com.world.jteam.bonb.paging;

import android.arch.paging.PagedListAdapter;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.world.jteam.bonb.model.ModelSearchProductMethod;
import com.world.jteam.bonb.swipe.SwipeLayout;
import com.world.jteam.bonb.swipe.SwipeItemMangerImpl;
import com.world.jteam.bonb.swipe.SwipeAdapterInterface;
import com.world.jteam.bonb.swipe.SwipeItemMangerInterface;
import com.world.jteam.bonb.swipe.SwipeAttributes;
import com.world.jteam.bonb.model.ModelProduct;


import java.util.List;

public class ProductListAdapter extends PagedListAdapter<ModelProduct, ProductViewHolder> implements SwipeItemMangerInterface, SwipeAdapterInterface {

    public SwipeItemMangerImpl mItemManger = new SwipeItemMangerImpl(this);
    private int swipeLayID;
    private ModelSearchProductMethod mSearchMethod;

    public ProductListAdapter(int swipeLayID,ModelSearchProductMethod searchMethod){
        super(ModelProduct.DIFF_CALLBACK);
        this.swipeLayID = swipeLayID;
        this.mSearchMethod=searchMethod;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ProductViewHolder.create(this,parent,mSearchMethod);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bindTo(getItem(position),position);
    }

    @Override
    public void notifyDatasetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public void openItem(int position) {
        mItemManger.openItem(position);
    }

    @Override
    public void closeItem(int position) {
        mItemManger.closeItem(position);
    }

    @Override
    public void closeAllExcept(SwipeLayout layout) {
        mItemManger.closeAllExcept(layout);
    }

    @Override
    public void closeAllItems() {
        mItemManger.closeAllItems();
    }

    @Override
    public List<Integer> getOpenItems() {
        return mItemManger.getOpenItems();
    }

    @Override
    public List<SwipeLayout> getOpenLayouts() {
        return mItemManger.getOpenLayouts();
    }

    @Override
    public void removeShownLayouts(SwipeLayout layout) {
        mItemManger.removeShownLayouts(layout);
    }

    @Override
    public boolean isOpen(int position) {
        return mItemManger.isOpen(position);
    }

    @Override
    public SwipeAttributes.Mode getMode() {
        return mItemManger.getMode();
    }

    @Override
    public void setMode(SwipeAttributes.Mode mode) {
        mItemManger.setMode(mode);
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return swipeLayID;
    }
}

