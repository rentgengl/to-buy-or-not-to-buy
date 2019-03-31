package com.world.jteam.bonb.server;

import com.world.jteam.bonb.model.ModelComment;
import com.world.jteam.bonb.model.ModelGroup;
import com.world.jteam.bonb.model.ModelMarket;
import com.world.jteam.bonb.model.ModelPrice;
import com.world.jteam.bonb.model.ModelProductFull;
import com.world.jteam.bonb.model.ModelSearchResult;
import com.world.jteam.bonb.model.ModelUser;
import com.world.jteam.bonb.model.ModelVersion;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DataApi {


    @GET("getMarketList.php")
    Call<List<ModelMarket>> getMarketList(@Query("name") String name,
                                    @Query("radius") int radius,
                                    @Query("lat") double lat,
                                    @Query("lng") double lng);

    @GET("getGroupListByName.php")
    Call<List<ModelGroup>> getGroupListByName(@Query("name") String name,
                                              @Query("marketID") int marketID);

    @GET("getProductList_v2.php")
    Call<ModelSearchResult> getProductList( @Query("name") String name,
                                            @Query("id") int groupId,
                                            @Query("radius") int radius,
                                            @Query("lat") double lat,
                                            @Query("lng") double lng,
                                            @Query("pageSize") int pageSize,
                                            @Query("lastName") String lastName,
                                            @Query("lastID") int lastID,
                                            @Query("marketID") int marketID
                                            );

    @GET("GetProductFull.php")
    Call<ModelProductFull> getProductFull(    @Query("id") int id,
                                              @Query("EAN") String ean,
                                              @Query("user_id") int user_id,
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
    Call<ModelVersion> getVersions();

    @POST("addNewPrice.php")
    Call<Void> addNewPrice(@Body ModelPrice price);

    @POST("addNewComment.php")
    Call<Void> addNewComment(@Body ModelComment comment);

}
