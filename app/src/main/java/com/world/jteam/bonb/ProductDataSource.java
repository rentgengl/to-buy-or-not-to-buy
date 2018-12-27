package com.world.jteam.bonb;

import android.arch.paging.PageKeyedDataSource;
import android.support.annotation.NonNull;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class ProductDataSource extends PageKeyedDataSource<Integer, ModelProduct> {

    //Начальная инициализация списка
    @Override
    public void loadInitial(@NonNull final LoadInitialParams<Integer> params, @NonNull final LoadInitialCallback<Integer, ModelProduct> callback) {

        // Initial page
        final int page = 1;

        DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();
        Call<List<ModelProduct>> retrofitCall = mDataApi.getProductListByGroup(1, page, params.requestedLoadSize);

        Callback<List<ModelProduct>> requestCallback = new Callback<List<ModelProduct>>() {
            @Override
            public void onResponse(@NonNull Call<List<ModelProduct>> call, @NonNull Response<List<ModelProduct>> response) {
                List<ModelProduct> resultData = response.body();

                if (resultData == null) {
                    onFailure(call, new HttpException(response));
                    return;
                }

                // Result can be passed asynchronously
                callback.onResult(
                        resultData, // List of data items
                        0, // Position of first item
                        16, // Total number of items that can be fetched from api
                        null, // Previous page. `null` if there's no previous page
                        page + 1 // Next Page (Used at the next request). Return `null` if this is the last page.
                );
            }

            @Override
            public void onFailure(@NonNull Call<List<ModelProduct>> call, @NonNull Throwable t) {

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

        // `params.requestedLoadSize` is the `pageSize` value provided while setting up PagedList.Builder
        Call<List<ModelProduct>> call = mDataApi.getProductListByGroup(1, page, params.requestedLoadSize);

        Callback<List<ModelProduct>> requestCallback = new Callback<List<ModelProduct>>() {
            @Override
            public void onResponse(@NonNull Call<List<ModelProduct>> call, @NonNull Response<List<ModelProduct>> response) {
                List<ModelProduct> searchModel = response.body();

                if (searchModel == null) {
                    onFailure(call, new HttpException(response));
                    return;
                }

                // Result can be passed asynchronously
                callback.onResult(
                        searchModel, // List of data items
                        // Next Page key (Used at the next request). Return `null` if this is the last page.
                        page + 1
                );
            }

            @Override
            public void onFailure(@NonNull Call<List<ModelProduct>> call, @NonNull Throwable t) {

            }
        };

        call.enqueue(requestCallback);
    }
}



