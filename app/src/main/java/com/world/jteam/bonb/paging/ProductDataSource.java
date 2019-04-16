package com.world.jteam.bonb.paging;

import android.arch.paging.ItemKeyedDataSource;
import android.support.annotation.NonNull;

import com.world.jteam.bonb.AppInstance;
import com.world.jteam.bonb.Constants;
import com.world.jteam.bonb.server.DataApi;
import com.world.jteam.bonb.server.SingletonRetrofit;
import com.world.jteam.bonb.model.ModelProduct;
import com.world.jteam.bonb.model.ModelSearchProductMethod;
import com.world.jteam.bonb.model.ModelSearchResult;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class ProductDataSource extends ItemKeyedDataSource<ModelProduct, ModelProduct> {
    public ModelSearchProductMethod searchMethod;
    private int mProductsCount = 0;
    //Начальная инициализация списка
    @Override
    public void loadInitial(@NonNull final LoadInitialParams<ModelProduct> params, @NonNull final LoadInitialCallback<ModelProduct> callback) {

        DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();

        Call<ModelSearchResult> retrofitCall = mDataApi.getProductList(
                searchMethod.searchText,
                searchMethod.searchGroup,
                AppInstance.getRadiusArea(),
                AppInstance.getGeoPosition().latitude,
                AppInstance.getGeoPosition().longitude,
                params.requestedLoadSize,
                "",
                -1,
                searchMethod.market_id);
        Callback<ModelSearchResult> requestCallback = new Callback<ModelSearchResult>() {
            @Override
            public void onResponse(@NonNull Call<ModelSearchResult> call, @NonNull Response<ModelSearchResult> response) {
                ModelSearchResult resultData = response.body();

                if (resultData == null) {
                    onFailure(call, new HttpException(response));
                    return;
                }

                mProductsCount = Math.min(resultData.getCount(),Constants.MAX_PRODUCT_LIST_ITEMS);

                // Result can be passed asynchronously
                callback.onResult(
                        resultData.getProducts(), // List of data items
                        0, // Position of first item
                        mProductsCount // Total number of items that can be fetched from api
                );
            }

            @Override
            public void onFailure(@NonNull Call<ModelSearchResult> call, @NonNull Throwable t) {
                AppInstance.errorLog("HTTP getProductList", t.toString());
            }
        };

        SingletonRetrofit.enqueue(retrofitCall,requestCallback);
    }


    @Override
    public void loadBefore(LoadParams<ModelProduct> params, LoadCallback< ModelProduct> callback) {

    }

    //Основная загрузка данных
    @Override
    public void loadAfter(@NonNull final LoadParams<ModelProduct> params, @NonNull final LoadCallback<ModelProduct> callback) {

        DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();

        Call<ModelSearchResult> retrofitCall = mDataApi.getProductList(
                searchMethod.searchText,
                searchMethod.searchGroup,
                AppInstance.getRadiusArea(),
                AppInstance.getGeoPosition().latitude,
                AppInstance.getGeoPosition().longitude,
                params.requestedLoadSize,
                params.key.name,
                params.key.id,
                searchMethod.market_id);
        Callback<ModelSearchResult> requestCallback = new Callback<ModelSearchResult>() {
            @Override
            public void onResponse(@NonNull Call<ModelSearchResult> call, @NonNull Response<ModelSearchResult> response) {
                //List<ModelProduct> searchModel = response.body().getProducts();
                ModelSearchResult resultData = response.body();

                if (resultData == null) {
                    onFailure(call, new HttpException(response));
                    return;
                }

                // Result can be passed asynchronously
                callback.onResult(resultData.getProducts());
            }

            @Override
            public void onFailure(@NonNull Call<ModelSearchResult> call, @NonNull Throwable t) {
                AppInstance.errorLog("HTTP getProductList", t.toString());
            }
        };

        SingletonRetrofit.enqueue(retrofitCall,requestCallback);
    }

    @NonNull
    @Override
    public ModelProduct getKey(@NonNull ModelProduct item) {
        return item;
    }
}



