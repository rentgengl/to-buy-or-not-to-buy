package com.world.jteam.bonb;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DataApi {

    @GET("GetProductListByGroup.php")
    Call<ModelSearchResult> getProductListByGroup(@Query("id") int groupId,
                                                   @Query("page") long page,
                                                   @Query("pageSize") int pageSize);

    @GET("GetProductGroupListByName.php")
    Call<ModelSearchResult> getProductGroupListByName(@Query("name") String name,
                                                      @Query("page") long page,
                                                      @Query("pageSize") int pageSize);

    @GET("GetProductFullById.php")
    Call<ModelProductFull> getProductFullById(@Query("id") int id);

    @GET("GetProductFullByEAN.php")
    Call<ModelProductFull> getProductFullByEAN(@Query("EAN") String ean);

    @GET("GetGroupList.php")
    Call<List<ModelGroup>> getGroupList();
}
