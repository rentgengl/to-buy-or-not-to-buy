package com.world.jteam.bonb.paging;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.world.jteam.bonb.model.ModelProduct;
import com.world.jteam.bonb.model.ModelSearchProductMethod;
import com.world.jteam.bonb.swipe.SwipeAdapterInterface;
import com.world.jteam.bonb.swipe.SwipeAttributes;
import com.world.jteam.bonb.swipe.SwipeItemMangerImpl;
import com.world.jteam.bonb.swipe.SwipeItemMangerInterface;
import com.world.jteam.bonb.swipe.SwipeLayout;

import java.util.ArrayList;
import java.util.List;

public class ProductListAdapterStatic extends RecyclerView.Adapter<ProductViewHolder> implements SwipeItemMangerInterface, SwipeAdapterInterface {
    public SwipeItemMangerImpl mItemManger = new SwipeItemMangerImpl(this);
    private ModelSearchProductMethod mSearchMethod;
    private int swipeLayID;
    private ArrayList<ModelProduct> mProductsList=new ArrayList<>();

    public ProductListAdapterStatic(int swipeLayID,ModelSearchProductMethod searchMethod){
        super();
        this.swipeLayID = swipeLayID;
        this.mSearchMethod=searchMethod;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return ProductViewHolder.create(this,parent,mSearchMethod);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bindTo(getItem(position),position);
    }

    @Override
    public int getItemCount() {
        return mProductsList.size();
    }

    public ModelProduct getItem(int position){
        return mProductsList.get(position);
    }

    public int getPosition(ModelProduct product){
        return mProductsList.indexOf(product);
    }

    public void setProductsList(List<ModelProduct> productList){
        mProductsList.addAll(productList);
        notifyDataSetChanged();
    }

    public void removeItem(ModelProduct currentProduct){
        int currentPos=getPosition(currentProduct);
        mProductsList.remove(currentPos);
        notifyItemRemoved(currentPos);
    }

    public void moveToPurchased(ModelProduct currentProduct){
        int currentPos=getPosition(currentProduct);
        int toPos=mProductsList.size(); //По умолчанию в конец

        for (int i=0;i<mProductsList.size();i++){
            ModelProduct product=mProductsList.get(i);
            if (product.purchased==1){
                toPos=i;
                break;
            }
        }

        if (toPos != currentPos) { //Это не таже самая позиция
            mProductsList.add(toPos,mProductsList.get(currentPos));
            mProductsList.remove(currentPos);

            notifyItemMoved(currentPos,toPos-1);
        }
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
