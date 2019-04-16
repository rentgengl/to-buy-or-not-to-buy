package com.world.jteam.bonb;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.world.jteam.bonb.model.ModelGroup;
import com.world.jteam.bonb.server.DataApi;
import com.world.jteam.bonb.server.SingletonRetrofit;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

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
        int groupsVersionServer = AppInstance.getProductGroupsVersion();

        //- Обновление данных по версии
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int groupsVersion = sharedPreferences.getInt("product_groups_version", 0);

        if (groupsVersion != groupsVersionServer && groupsVersionServer>0) {
            //- Получение данных по категориям с сервера
            DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();
            Call<List<ModelGroup>> serviceCall = mDataApi.getGroupList();

            List<ModelGroup> ss = null;
            try {
                ss = serviceCall.execute().body();

                initGroupList(ss);

                //- Сохранение версии
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putInt("product_groups_version", groupsVersionServer);
                edit.commit();

            } catch (Exception e) {
                AppInstance.errorLog("HTTP getGroupList", e.toString());
            }

        }
    }

    public static void initGroupList(List<ModelGroup> groupList) {
        ArrayList<ModelGroup> productGroups = new ArrayList<>();

        for(ModelGroup group:groupList){
            productGroups.add(new ModelGroup(group.id, group.name, group.parent_id, group.logo_link));
        }

        AppRoomDao appRoomDao = databaseApp.database.appRoomDao();
        appRoomDao.deleteAllProductGroups();
        appRoomDao.insertProductGroups(productGroups);

    }

    public static AppRoomDao getAppRoomDao() {
        return databaseApp.database.appRoomDao();
    }

    @Dao
    public interface AppRoomDao {

        @Query("SELECT * FROM ModelGroup ORDER BY name ASC")
        ModelGroup[] getAllProductGroups();

        @Query("DELETE FROM ModelGroup")
        void deleteAllProductGroups();

        @Insert
        void insertProductGroups(List<ModelGroup> ModelGroup);
    }

    @Database(entities = {ModelGroup.class}, version = 1)
    public static abstract class AppRoomDatabase extends RoomDatabase {
        public abstract AppRoomDao appRoomDao();
    }

}
