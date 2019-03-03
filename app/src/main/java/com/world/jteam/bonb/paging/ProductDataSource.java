package com.world.jteam.bonb.paging;

import android.arch.paging.PageKeyedDataSource;
import android.support.annotation.NonNull;

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

public class ProductDataSource extends PageKeyedDataSource<Integer, ModelProduct> {
    public ModelSearchProductMethod searchMethod;
    //Начальная инициализация списка
    @Override
    public void loadInitial(@NonNull final LoadInitialParams<Integer> params, @NonNull final LoadInitialCallback<Integer, ModelProduct> callback) {

        // Initial page

        final int page = 1;

        DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();

        Call<ModelSearchResult> retrofitCall = mDataApi.getProductList(searchMethod.searchText,searchMethod.searchGroup, page, params.requestedLoadSize);
        Callback<ModelSearchResult> requestCallback = new Callback<ModelSearchResult>() {
            @Override
            public void onResponse(@NonNull Call<ModelSearchResult> call, @NonNull Response<ModelSearchResult> response) {
                ModelSearchResult resultData = response.body();

                if (resultData == null) {
                    onFailure(call, new HttpException(response));
                    return;
                }

                Integer nextPage = null;
                if (resultData.getCount() > Constants.DEFAULT_PER_PAGE)
                    nextPage = page + 1;

                // Result can be passed asynchronously
                callback.onResult(
                        resultData.getProducts(), // List of data items
                        0, // Position of first item
                        resultData.getCount(), // Total number of items that can be fetched from api
                        null, // Previous page. `null` if there's no previous page
                        nextPage // Next Page (Used at the next request). Return `null` if this is the last page.
                );
            }

            @Override
            public void onFailure(@NonNull Call<ModelSearchResult> call, @NonNull Throwable t) {

            }
        };

        retrofitCall.enqueue(requestCallback);
    }


    @Override
    public void loadBefore(LoadParams<Integer> params,
                           LoadCallback<Integer, ModelProduct> callback) {

    }


    //Основная загрузка данных
    @Override
    public void loadAfter(@NonNull final LoadParams<Integer> params, @NonNull final LoadCallback<Integer, ModelProduct> callback) {

        // Next page.
        final int page = params.key;

        DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();

        Call<ModelSearchResult> retrofitCall = mDataApi.getProductList(searchMethod.searchText, searchMethod.searchGroup, page, params.requestedLoadSize);
        Callback<ModelSearchResult> requestCallback = new Callback<ModelSearchResult>() {
            @Override
            public void onResponse(@NonNull Call<ModelSearchResult> call, @NonNull Response<ModelSearchResult> response) {
                //List<ModelProduct> searchModel = response.body().getProducts();
                ModelSearchResult resultData = response.body();

                if (resultData == null) {
                    onFailure(call, new HttpException(response));
                    return;
                }

                Integer nextPage = null;
                if (resultData.getCount() > Constants.DEFAULT_PER_PAGE * page)
                    nextPage = page + 1;

                // Result can be passed asynchronously
                callback.onResult(
                        resultData.getProducts(), // List of data items
                        // Next Page key (Used at the next request). Return `null` if this is the last page.
                        nextPage
                );
            }

            @Override
            public void onFailure(@NonNull Call<ModelSearchResult> call, @NonNull Throwable t) {

            }
        };

        retrofitCall.enqueue(requestCallback);
    }
}



