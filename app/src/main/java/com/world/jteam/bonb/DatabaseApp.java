package com.world.jteam.bonb;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.world.jteam.bonb.model.ModelGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DatabaseApp {
    private static DatabaseApp databaseApp;
    private AppRoomDatabase database;

    private DatabaseApp() {

    }

    public static void initDatabaseApp(Context context) {
        if (databaseApp != null)
            return;

        databaseApp = new DatabaseApp();
        databaseApp.database = Room.databaseBuilder(context, DatabaseApp.AppRoomDatabase.class, "database")
                .build();

        //Категории
        //- Получение версии категорий с сервера
        int categoryVersionServer = 1;
        //- Обновление данных по версии
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int categoryVersion = sharedPreferences.getInt("product_categories_version", 0);

        if (categoryVersion != categoryVersionServer) {
            //- Получение данных по категориям с сервера
            DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();
            Call<List<ModelGroup>> serviceCall = mDataApi.getGroupList();
            serviceCall.enqueue(new Callback<List<ModelGroup>>() {
                @Override
                public void onResponse(Call<List<ModelGroup>> call, Response<List<ModelGroup>> response) {
                    List<ModelGroup> ss = response.body();
                    initGroupList(ss);
                }

                @Override
                public void onFailure(Call<List<ModelGroup>> call, Throwable t) {
                }
            });


            //- Сохранение версии
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putInt("product_categories_version", categoryVersionServer);
            edit.commit();

        }
    }


    public static void initGroupList(List<ModelGroup> groupList) {
        ArrayList<ProductCategories> productCategories = new ArrayList<>();

        for(ModelGroup group:groupList){
            productCategories.add(new ProductCategories(group.id, group.name, group.parent_id, group.logo_link));
        }

        AppRoomDao appRoomDao = databaseApp.database.appRoomDao();
        appRoomDao.deleteAllProductCategories();
        appRoomDao.insertProductCategories(productCategories);

    }

    public static AppRoomDao getAppRoomDao() {
        return databaseApp.database.appRoomDao();
    }

    @Entity
    public static class ProductCategories {
        @PrimaryKey
        public int id;
        public String name;
        //public int parentid;
        @ColumnInfo(name = "parent_id")
        public int parent_id;
        @ColumnInfo(name = "logo_link")
        public String logo_link;

        @Ignore
        public int navigation_method;

        public ProductCategories(int id, String name, int parent_id, String logo_link) {
            this.id = id;
            this.name = name;
            this.parent_id = parent_id;
            this.logo_link = logo_link;
        }

        public ProductCategories(int id, String name, int parent_id, int navigation_method) {
            this.id = id;
            this.name = name;
            this.parent_id = parent_id;
            this.navigation_method = navigation_method;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Dao
    public interface AppRoomDao {

        @Query("SELECT * FROM ProductCategories ORDER BY name ASC")
        ProductCategories[] getAllProductCategories();

        @Query("DELETE FROM ProductCategories")
        void deleteAllProductCategories();

        @Insert
        void insertProductCategories(List<ProductCategories> productCategories);
    }

    @Database(entities = {ProductCategories.class}, version = 1)
    public static abstract class AppRoomDatabase extends RoomDatabase {
        public abstract AppRoomDao appRoomDao();
    }

}
