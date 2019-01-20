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

import java.io.IOException;
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

    public static void initDatabaseApp(Context context) throws IOException {
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

            List<ModelGroup> ss = serviceCall.execute().body();
            initGroupList(ss);

            //- Сохранение версии
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putInt("product_categories_version", categoryVersionServer);
            edit.commit();

        }
    }


    public static void initGroupList(List<ModelGroup> groupList) {
        ArrayList<ModelGroup> productCategories = new ArrayList<>();

        for(ModelGroup group:groupList){
            productCategories.add(new ModelGroup(group.id, group.name, group.parent_id, group.logo_link));
        }

        AppRoomDao appRoomDao = databaseApp.database.appRoomDao();
        appRoomDao.deleteAllProductCategories();
        appRoomDao.insertProductCategories(productCategories);

    }

    public static AppRoomDao getAppRoomDao() {
        return databaseApp.database.appRoomDao();
    }

    @Dao
    public interface AppRoomDao {

        @Query("SELECT * FROM ModelGroup ORDER BY name ASC")
        ModelGroup[] getAllProductCategories();

        @Query("DELETE FROM ModelGroup")
        void deleteAllProductCategories();

        @Insert
        void insertProductCategories(List<ModelGroup> ModelGroup);
    }

    @Database(entities = {ModelGroup.class}, version = 1)
    public static abstract class AppRoomDatabase extends RoomDatabase {
        public abstract AppRoomDao appRoomDao();
    }

}
