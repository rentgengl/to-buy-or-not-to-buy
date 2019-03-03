package com.world.jteam.bonb.server;

import com.world.jteam.bonb.model.ModelComment;
import com.world.jteam.bonb.model.ModelGroup;
import com.world.jteam.bonb.model.ModelPrice;
import com.world.jteam.bonb.model.ModelProductFull;
import com.world.jteam.bonb.model.ModelSearchResult;
import com.world.jteam.bonb.model.ModelUser;
import com.world.jteam.bonb.model.Versions;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DataApi {

    @GET("getGroupListByName.php")
    Call<List<ModelGroup>> getGroupListByName(@Query("name") String name);

    @GET("GetProductGroupListByName.php")
    Call<ModelSearchResult> getProductGroupListByName(@Query("name") String name,
                                                      @Query("page") long page,
                                                      @Query("pageSize") int pageSize);

    @GET("getProductList.php")
    Call<ModelSearchResult> getProductList(@Query("name") String name,
                                           @Query("id") int groupId,
                                           @Query("page") long page,
                                           @Query("pageSize") int pageSize,
                                           @Query("radius") int radius,
                                           @Query("lat") double lat,
                                           @Query("lng") double lng);

    @GET("GetProductFullById.php")
    Call<ModelProductFull> getProductFullById(@Query("id") int id,
                                              @Query("user_id") int user_id,
                                              @Query("radius") int radius,
                                              @Query("lat") double lat,
                                              @Query("lng") double lng);

    @GET("GetProductFullByEAN.php")
    Call<ModelProductFull> getProductFullByEAN(@Query("EAN") String ean,
                                               @Query("radius") int radius,
                                               @Query("lat") double lat,
                                               @Query("lng") double lng);

    @GET("GetGroupList.php")
    Call<List<ModelGroup>> getGroupList();

    @POST("Registration.php")
    Call<Void> registerUser(@Body ModelUser user);

    @GET("LoginUser.php")
    Call<ModelUser> loginUser(@Query("google_id") String google_id);

    @GET("getVersions.php")
    Call<Versions> getVersions();

    @POST("addNewPrice.php")
    Call<Void> addNewPrice(@Body ModelPrice price);

    @POST("addNewComment.php")
    Call<Void> addNewComment(@Body ModelComment comment);

}
