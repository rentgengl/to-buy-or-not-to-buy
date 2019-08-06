package com.world.jteam.bonb.paging;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.world.jteam.bonb.AppInstance;
import com.world.jteam.bonb.Constants;
import com.world.jteam.bonb.model.ModelGroup;
import com.world.jteam.bonb.model.ModelSearchProductMethod;
import com.world.jteam.bonb.server.DataApi;
import com.world.jteam.bonb.R;
import com.world.jteam.bonb.server.SingletonRetrofit;
import com.world.jteam.bonb.activity.ProductActivity;
import com.world.jteam.bonb.model.ModelProduct;
import com.world.jteam.bonb.model.ModelProductFull;
import com.world.jteam.bonb.swipe.SimpleSwipeListener;
import com.world.jteam.bonb.swipe.SwipeLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{
    private Context mContext;

    private ProductListAdapterStatic mAdapterStatic;
    private ModelSearchProductMethod mSearchMethod;
    private ModelProduct mProduct;

    private TextView view_midPrice;//Средняя цена
    private TextView view_productName;//Наименование
    private int mProductNameDefaultPaintFlags;
    private TextView view_lowPrice;//Разброс цен
    private TextView view_textRating;//Количество отзывов
    private RatingBar view_productRating;//Рейтинг
    private ImageView view_picture; // Картинка продукта
    private ImageView view_saleImage; //Картинка sale
    private TextView view_productCount; //Количество продукта
    private SwipeLayout view_swipeLayout; //Лайаут свайпа
    private ImageButton view_loupeSLButton; //Кнопка отметки товара в списке покупок
    private ImageButton view_addSLButton; //Кнопка добавления товара в список покупок
    private ImageButton view_delSLButton; //Кнопка удаления товара из списка покупок
    private ImageButton view_plusSLButton; //Кнопка добавления 1 количества в списке покупок
    private ImageButton view_minusSLButton; //Кнопка удаления 1 количества в списке покупок

    private ProductViewHolder(ProductListAdapterStatic adapter, View itemView,ModelSearchProductMethod searchMethod) {
        super(itemView);

        mContext=itemView.getContext();

        mAdapterStatic=adapter;
        mSearchMethod=searchMethod;

        view_midPrice = itemView.findViewById(R.id.midPrice);//Средняя цена
        view_productName = itemView.findViewById(R.id.productName);//Наименование
        mProductNameDefaultPaintFlags=view_productName.getPaintFlags();
        view_lowPrice = itemView.findViewById(R.id.lowPrice);//Разброс цен
        view_textRating = itemView.findViewById(R.id.textRating);//Количество отзывов
        view_productRating = itemView.findViewById(R.id.productRating);//Рейтинг
        view_picture = itemView.findViewById(R.id.productListImage);
        view_saleImage = itemView.findViewById(R.id.saleListImage);
        view_productCount=itemView.findViewById(R.id.productCount);

        view_swipeLayout=itemView.findViewById(R.id.swipeLayout);
        view_swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        view_swipeLayout.addDrag(SwipeLayout.DragEdge.Left, view_swipeLayout.findViewById(R.id.swipeLeft));
        view_swipeLayout.addDrag(SwipeLayout.DragEdge.Right,view_swipeLayout.findViewById(R.id.swipeRight));
        view_swipeLayout.setLeftSwipeEnabled(mSearchMethod.searchGroup == Constants.SHOPPINGLIST_GROUP_ID);
        view_swipeLayout.setWillOpenPercentAfterClose(0.5f);

        view_loupeSLButton=itemView.findViewById(R.id.loupeSLButton);
        view_addSLButton=itemView.findViewById(R.id.addSLButton);
        view_delSLButton=itemView.findViewById(R.id.delSLButton);
        view_plusSLButton=itemView.findViewById(R.id.plusSLButton);
        view_minusSLButton=itemView.findViewById(R.id.minusSLButton);

        //OnClickListener
        view_productName.setOnClickListener(this);
        view_picture.setOnClickListener(this);
        view_loupeSLButton.setOnClickListener(this);
        view_addSLButton.setOnClickListener(this);
        view_delSLButton.setOnClickListener(this);
        view_plusSLButton.setOnClickListener(this);
        view_minusSLButton.setOnClickListener(this);
        view_swipeLayout.addSwipeListener(new SwipeProductListener());

    }

    public static ProductViewHolder create(ProductListAdapter adapter,ViewGroup parent, ModelSearchProductMethod searchMethod) {
        View view = inflateView(parent);
        return new ProductViewHolder(null,view,searchMethod);
    }

    public static ProductViewHolder create(ProductListAdapterStatic adapter,ViewGroup parent, ModelSearchProductMethod searchMethod) {
        View view = inflateView(parent);
        return new ProductViewHolder(adapter,view,searchMethod);
    }

    private static View inflateView(ViewGroup parent){
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_list_product, parent, false);
    }

    @SuppressLint("SetTextI18n")
    public void bindTo(ModelProduct p, int pos) {
        // If placeholders are enabled, Paging will pass null first and then pass the actual data when it's available.
        if (p != null) {
            mProduct=p;

            bindView();

            if (p.imageSmall_link == null) {
                view_picture.setImageResource(R.drawable.ic_action_noimage);
            } else {

                Picasso.with(view_picture.getContext())
                        .load(Constants.SERVICE_GET_IMAGE + p.imageSmall_link)
                        .placeholder(R.drawable.ic_action_noimage)
                        .error(R.drawable.ic_action_noimage)
                        .into(view_picture);

            }
        } else {
            view_productName.setText("Loading...");
        }
    }

    private void bindView(){
        view_loupeSLButton.setVisibility(View.GONE);
        view_plusSLButton.setVisibility(View.GONE);
        view_minusSLButton.setVisibility(View.GONE);
        view_addSLButton.setVisibility(View.VISIBLE);
        view_delSLButton.setClickable(mProduct.inShoppingList==1 ? true : false);

        if (mSearchMethod.searchGroup==Constants.SHOPPINGLIST_GROUP_ID) {
            view_delSLButton.setImageResource(R.drawable.ic_trash_b);
            view_addSLButton.setVisibility(View.GONE);
            view_loupeSLButton.setVisibility(View.VISIBLE);

            if (mProduct.purchased==0) {
                view_plusSLButton.setVisibility(View.VISIBLE);
                view_minusSLButton.setVisibility(View.VISIBLE);

                view_productName.setPaintFlags(mProductNameDefaultPaintFlags);
            } else {
                view_productName.setPaintFlags(mProductNameDefaultPaintFlags | Paint.STRIKE_THRU_TEXT_FLAG); //Перечеркивание
            }

            view_productCount.setText(""+mProduct.shoppingListCount);
        } else {
            view_delSLButton.setImageResource(mProduct.inShoppingList==1 ? R.drawable.ic_del_sl : R.drawable.ic_del_sl_inactive);
            view_productCount.setText("");
        }

        view_productName.setText(mProduct.name);
        view_productName.setTag(mProduct.id);
        view_midPrice.setText(mProduct.price + "\u20BD");
        //view_lowPrice.setText("от " + p.price_min + " до " + p.price_max + "\u20BD");
        view_lowPrice.setText("" + mProduct.price_min + " - " + mProduct.price_max);
        view_saleImage.setVisibility(mProduct.sale==1 ? View.VISIBLE : View.INVISIBLE);
        view_textRating.setText(""+mProduct.comment_count);
        view_productRating.setRating(mProduct.rating);

    }

    @Override
    public void onClick(final View v) {
        int vID=v.getId();

        if (vID==R.id.productName || vID==R.id.productListImage) {
            if (mSearchMethod.searchGroup == Constants.SHOPPINGLIST_GROUP_ID) { //Клик по позиции
                if (mProduct.purchased == 0)
                    markShoppingList();
                else
                    addShoppingList();
            } else
                openProduct();

        } else if (vID==R.id.loupeSLButton) {
            openProduct();

        } else if (vID==R.id.plusSLButton) { //Кнопка Плюс
            plusShoppingList();

        } else if (vID==R.id.minusSLButton) { //Кнопка Минус
            minusShoppingList();

        } else if (vID==R.id.addSLButton) { //Кнопка добавить
            addShoppingList();

        } else if (vID==R.id.delSLButton) { //Кнопка удалить
            delShoppingList();
        }
    }

    private void openProduct(){
        if (mProduct.id>=0) {
            DataApi dataApi = SingletonRetrofit.getInstance().getDataApi();
            Call<ModelProductFull> serviceCallProduct = dataApi.getProductFull(
                    mProduct.id,
                    "",
                    AppInstance.getUser().id,
                    AppInstance.getRadiusArea(),
                    AppInstance.getGeoPosition().latitude,
                    AppInstance.getGeoPosition().longitude);
            SingletonRetrofit.enqueue(serviceCallProduct, new Callback<ModelProductFull>() {
                @Override
                public void onResponse(Call<ModelProductFull> call, Response<ModelProductFull> response) {
                    ModelProductFull ss = response.body();
                    //Context mContext = AppInstance.getAppContext();
                    Intent intent = new Intent(mContext, ProductActivity.class);
                    intent.putExtra("object", ss);
                    mContext.startActivity(intent);

                }

                @Override
                public void onFailure(Call<ModelProductFull> call, Throwable t) {
                    AppInstance.errorLog("HTTP getProductFull", t.toString());
                }
            });
        }
    }

    private void plusShoppingList(){
        DataApi dataApi = SingletonRetrofit.getInstance().getDataApi();
        Call<Integer> serviceCallInt = dataApi.setShoppingListCount(
                AppInstance.getUser().id,
                mProduct.id,
                mProduct.name,
                mProduct.shoppingListCount+1
        );
        SingletonRetrofit.enqueue(serviceCallInt,new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                mProduct.shoppingListCount=mProduct.shoppingListCount+1;
                bindView();

                Toast.makeText(mContext, "+1 : "+mProduct.name,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Toast.makeText(mContext, R.string.shopping_count_plus_error,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void minusShoppingList(){
        if (mProduct.shoppingListCount>1) {
            DataApi dataApi = SingletonRetrofit.getInstance().getDataApi();
            Call<Integer> serviceCallInt = dataApi.setShoppingListCount(
                    AppInstance.getUser().id,
                    mProduct.id,
                    mProduct.name,
                    mProduct.shoppingListCount - 1
            );
            SingletonRetrofit.enqueue(serviceCallInt, new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    mProduct.shoppingListCount = mProduct.shoppingListCount - 1;
                    bindView();

                    Toast.makeText(mContext, "-1 : " + mProduct.name, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    Toast.makeText(mContext, R.string.shopping_count_minus_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void addShoppingList(){
        DataApi dataApi = SingletonRetrofit.getInstance().getDataApi();
        Call<Integer> serviceCallInt = dataApi.addShoppingListProduct(
                AppInstance.getUser().id,
                mProduct.id,
                mProduct.name
        );
        SingletonRetrofit.enqueue(serviceCallInt, new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (mSearchMethod.searchGroup==Constants.SHOPPINGLIST_GROUP_ID) {
                    mAdapterStatic.moveToPurchasedBorder(mProduct);
                }

                mProduct.inShoppingList=1;
                if (mProduct.purchased==0) {
                    mProduct.shoppingListCount = mProduct.shoppingListCount + 1;
                }
                mProduct.purchased=0;
                ModelGroup slg = AppInstance.getShoppingListGroup();
                if (slg.count!=null)
                    slg.count = slg.count + 1;


                bindView();

                view_swipeLayout.close();

                Toast.makeText(mContext, "+1 : "+mProduct.name,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Toast.makeText(mContext, R.string.shopping_add_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void delShoppingList(){
        DataApi dataApi = SingletonRetrofit.getInstance().getDataApi();
        if (mProduct.inShoppingList==1){
            Call<Integer> serviceCallInt = dataApi.delShoppingListProduct(
                    AppInstance.getUser().id,
                    mProduct.id,
                    mProduct.name
            );
            SingletonRetrofit.enqueue(serviceCallInt, new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    mProduct.inShoppingList=0;
                    mProduct.shoppingListCount=0;
                    ModelGroup slg = AppInstance.getShoppingListGroup();
                    if (slg.count!=null && mProduct.purchased==0)
                        slg.count = slg.count - 1;
                    if (mSearchMethod.searchGroup==Constants.SHOPPINGLIST_GROUP_ID) {
                        mAdapterStatic.removeItem(mProduct);
                    } else {
                        bindView();
                        view_swipeLayout.close();
                    }

                    Toast.makeText(mContext, mContext.getText(R.string.shopping_del) + " : " + mProduct.name, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    Toast.makeText(mContext, R.string.shopping_del_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void markShoppingList(){
        DataApi dataApi = SingletonRetrofit.getInstance().getDataApi();
        Call<Integer> serviceCallInt = dataApi.markShoppingListProduct(
                AppInstance.getUser().id,
                mProduct.id,
                mProduct.name
        );
        SingletonRetrofit.enqueue(serviceCallInt, new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                mAdapterStatic.moveToPurchasedBorder(mProduct);
                mProduct.purchased=1;
                ModelGroup slg = AppInstance.getShoppingListGroup();
                slg.count=slg.count-1;
                bindView();

                view_swipeLayout.close();

                Toast.makeText(mContext, mProduct.name, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Toast.makeText(mContext, R.string.shopping_mark_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class SwipeProductListener extends SimpleSwipeListener {
        @Override
        public void onOpen(SwipeLayout layout) {
            super.onOpen(layout);
            if (layout.getDragEdge()==SwipeLayout.DragEdge.Left)
                delShoppingList();
        }
    }
}
