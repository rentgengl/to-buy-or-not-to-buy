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

import java.util.ArrayList;
import java.util.List;

public class DatabaseApp {
    private static DatabaseApp databaseApp;
    private AppRoomDatabase database;

    private DatabaseApp(){

    }

    public static void initDatabaseApp(Context context){
        if (databaseApp!=null)
            return;

        databaseApp=new DatabaseApp();
        databaseApp.database = Room.databaseBuilder(context, DatabaseApp.AppRoomDatabase.class, "database")
                .build();

        //Категории
        //- Получение версии категорий с сервера
        int categoryVersionServer=1;
        //- Обновление данных по версии
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int categoryVersion=sharedPreferences.getInt("product_categories_version",0);

        if (categoryVersion!=categoryVersionServer){
            //- Получение данных по категориям с сервера
            ArrayList<ProductCategories> productCategories=new ArrayList<>();

            productCategories.add(new ProductCategories(10,"Продукты",0));
                productCategories.add(new ProductCategories(11,"Крупы",10));
                productCategories.add(new ProductCategories(12,"Конфеты",10));
                productCategories.add(new ProductCategories(13,"Чипсы",10));
                productCategories.add(new ProductCategories(14,"Кетчуп",10));
            productCategories.add(new ProductCategories(20,"Химия",0));
                productCategories.add(new ProductCategories(21,"Порошки",20));
                productCategories.add(new ProductCategories(22,"Шампуни",20));
                productCategories.add(new ProductCategories(23,"Зуб паста",20));
            productCategories.add(new ProductCategories(30,"Хозтовары",0));
                productCategories.add(new ProductCategories(31,"Лампочки",30));
                productCategories.add(new ProductCategories(32,"Прищепки",30));
                productCategories.add(new ProductCategories(33,"Тарелки",30));
                productCategories.add(new ProductCategories(34,"Полотенца",30));
                productCategories.add(new ProductCategories(35,"Батарейки",30));
                productCategories.add(new ProductCategories(36,"Инструменты",30));
                productCategories.add(new ProductCategories(37,"Тапки",30));

            AppRoomDao appRoomDao=databaseApp.database.appRoomDao();
            appRoomDao.deleteAllProductCategories();
            appRoomDao.insertProductCategories(productCategories);

            //- Сохранение версии
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putInt("product_categories_version", categoryVersionServer);
            edit.commit();
        }
    }

    public static AppRoomDao getAppRoomDao(){
        return databaseApp.database.appRoomDao();
    }

    @Entity
    public static class ProductCategories{
        @PrimaryKey
        public int id;
        public String name;
        @ColumnInfo(name = "parent_id")
        public int parentid;

        @Ignore
        public int navigation_method;

        public ProductCategories(int id,String name,int parentid){
            this.id=id;
            this.name=name;
            this.parentid=parentid;
        }

        public ProductCategories(int id,String name,int parentid,int navigation_method){
            this.id=id;
            this.name=name;
            this.parentid=parentid;
            this.navigation_method=navigation_method;
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
