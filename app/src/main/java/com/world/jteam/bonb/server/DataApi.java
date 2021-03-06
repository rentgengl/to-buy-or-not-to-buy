package com.world.jteam.bonb.server;

import com.world.jteam.bonb.model.ModelComment;
import com.world.jteam.bonb.model.ModelContact;
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
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DataApi {

    //Не используется
    @POST("addNewPrice.php")
    Call<Void> addNewPrice(@Body ModelPrice price);


    @GET("Contact_Get_List_By_MarketId.php")
    Call<List<ModelContact>> getMarketContacts(@Query("market_id") int market_id);


    @GET("Market_Get_List_By_Name_GEO.php")
    Call<List<ModelMarket>> getMarketList(@Query("name") String name,
                                    @Query("radius") int radius,
                                    @Query("lat") double lat,
                                    @Query("lng") double lng);

    @GET("Group_Get_List_By_MarketGroupId.php")
    Call<int[]> getMarketProductsGroup(@Query("marketGroupID") int marketGroupID);

    @GET("Group_Get_List_By_Name_MarketGroupId.php")
    Call<List<ModelGroup>> getGroupListByName(@Query("name") String name,
                                              @Query("marketID") int marketID);
    @GET("Group_Get_List.php")
    Call<List<ModelGroup>> getGroupList();

    @GET("SearchResult_Get_Item.php")
    Call<ModelSearchResult> getProductList( @Query("name") String name,
                                            @Query("id") int groupId,
                                            @Query("radius") int radius,
                                            @Query("lat") double lat,
                                            @Query("lng") double lng,
                                            @Query("pageSize") int pageSize,
                                            @Query("lastName") String lastName,
                                            @Query("lastID") int lastID,
                                            @Query("marketID") int marketID,
                                            @Query("user_id") int user_id
                                            );

    @GET("ProductFull_Get_Item_By_Id_EAN_GEO.php")
    Call<ModelProductFull> getProductFull(    @Query("id") int id,
                                              @Query("EAN") String ean,
                                              @Query("user_id") int user_id,
                                              @Query("radius") int radius,
                                              @Query("lat") double lat,
                                              @Query("lng") double lng);

    @POST("User_Create_Item.php")
    Call<Void> registerUser(@Body ModelUser user);

    @GET("User_Get_Item_By_GoogleId.php")
    Call<ModelUser> loginUser(@Query("google_id") String google_id);

    //Version_Get_Item.php
    @GET("Version_Get_Item.php")
    Call<ModelVersion> getVersions();

    @POST("Comment_Create_Item.php")
    Call<Void> addNewComment(@Body ModelComment comment);

    @GET("LogMobile_Create_Item.php")
    Call<Void> addLogMobile(@Query("err_group") String err_group,
                                              @Query("error") String error);

    @GET("ShoppingListCount_Get_Item_By_UserId.php")
    Call<Integer> getShoppingListCount(@Query("user_id") int user_id);

    @FormUrlEncoded
    @POST("ShoppingListCount_Update_Item.php")
    Call<Integer> setShoppingListCount(@Field("user_id") int user_id,
                                    @Field("product_id") int product_id,
                                    @Field("product_name") String product_name,
                                    @Field("product_count") int product_count
                                    );

    @FormUrlEncoded
    @POST("ShoppingList_Delete_Item.php")
    Call<Integer> delShoppingListProduct(@Field("user_id") int user_id,
                                       @Field("product_id") int product_id,
                                       @Field("product_name") String product_name
                                       );

    @FormUrlEncoded
    @POST("ShoppingList_Create_Item.php")
    Call<Integer> addShoppingListProduct(@Field("user_id") int user_id,
                                         @Field("product_id") int product_id,
                                         @Field("product_name") String product_name
    );

    @FormUrlEncoded
    @POST("ShoppingList_Create_Item_By_EAN.php")
    Call<String> addShoppingListProductEAN(@Field("user_id") int user_id,
                                           @Field("EAN") String EAN
    );

    @FormUrlEncoded
    @POST("ShoppingList_Update_Item.php")
    Call<Integer> markShoppingListProduct(@Field("user_id") int user_id,
                                         @Field("product_id") int product_id,
                                         @Field("product_name") String product_name
    );


}
