package com.world.jteam.bonb;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;

import com.google.android.gms.maps.model.LatLng;
import com.world.jteam.bonb.geo.GeoManager;
import com.world.jteam.bonb.media.BarcodeManager;
import com.world.jteam.bonb.model.ModelGroup;
import com.world.jteam.bonb.model.ModelUser;
import com.world.jteam.bonb.model.ModelVersion;
import com.world.jteam.bonb.server.DataApi;
import com.world.jteam.bonb.server.SingletonRetrofit;

import java.util.LinkedHashMap;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppInstance extends Application {
    private static Context sContext;
    private static boolean sFirstStart;
    private static ModelUser sUser;
    private static ModelVersion sServerVersion = new ModelVersion();

    private static LinkedHashMap<ModelGroup, LinkedHashMap> sProductGroups; //Дерево групп
    private static TreeMap<Integer, LinkedHashMap> sProductGroupsParent; //Дерево родителей групп
    private static ModelGroup sShoppingListGroup; //Группа списка товаров

    private static boolean sAutoGeoPosition = true;
    private static int sRadiusArea = Constants.DEFAULT_RADIUS_AREA;
    private static LatLng sGeoPosition = new LatLng(55.755814, 37.617635); //Москва по дефолту

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();

        //Геолокация
        sRadiusArea = GeoManager.getRadiusAreaFromSettings(sRadiusArea);
        sGeoPosition = GeoManager.getGeoPositionFromSettings(sGeoPosition);
        sUser = AuthManager.getDefaultUser();

        Thread thread = new Thread(new AppInitialisation());
        thread.start();
    }

    //Основная инициализация
    private class AppInitialisation implements Runnable {
        @Override
        public void run() {
            //Геолокация
            sAutoGeoPosition = GeoManager.getAutoGeoPositionFromSettings(sAutoGeoPosition);

            //Версии
            synchronized (sServerVersion){
                updateVersions(null);
            }

            Thread versionThread=new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        Thread.currentThread().sleep(Constants.UPDATE_RATE_VERSION);
                    } catch (InterruptedException e) {
                        AppInstance.errorLog("Sleep error", e.toString());
                    }

                    DataApi dataApi = SingletonRetrofit.getInstance().getDataApi();

                    while (true) {
                        updateVersions(dataApi);

                        try {
                            Thread.currentThread().sleep(Constants.UPDATE_RATE_VERSION);
                        } catch (InterruptedException e) {
                            AppInstance.errorLog("Sleep error", e.toString());
                            break;
                        }
                    }
                }
            });
            versionThread.start();

            //Первый запуск
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(sContext);
            sFirstStart = sharedPreferences.getBoolean("first_start", true);
            if (sFirstStart) {
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putBoolean("first_start", false);
                edit.commit();
            }

            //Пользователь
            AuthManager.setStartUser();

            //Штрихкодер
            if (sFirstStart) {
                BarcodeManager.firstInitBarcodeDetector(sContext);
            }

            //БД
            DatabaseApp.initDatabaseApp(sContext);

            //Категории
            productGroupsInitialisation();

        }
    }

    //Получение контекста
    public static Context getAppContext() {
        return sContext;
    }

    //Мультидекс - необходимый фикс для запуска приложения
    public static ModelUser getUser(){
        return AppInstance.sUser;
    }

    public static void setUser(ModelUser user){
        AppInstance.sUser = user;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    //Версии
    public static ModelVersion getServerVersion() {
        return sServerVersion;
    }

    public static int getMinServerAppVersion() {
        return sServerVersion.appVersion;
    }

    public static int getProductGroupsVersion() {
        return sServerVersion.groupVersion;
    }

    public static void updateVersions(DataApi dataApi) {
        if (dataApi==null)
            dataApi=SingletonRetrofit.getInstance().getDataApi();

        Call<ModelVersion> versionsCall = dataApi.getVersions();
        try {
            ModelVersion servModelVersion = versionsCall.execute().body();
            sServerVersion.appVersion = servModelVersion.appVersion;
            sServerVersion.groupVersion = servModelVersion.groupVersion;
        } catch (Exception e) {
            AppInstance.errorLog("HTTP getVersions", e.toString());
        }
    }

    //Группы товаров
    public static void setProductGroups(LinkedHashMap productGroups) {
        sProductGroups = productGroups;
    }

    public static LinkedHashMap getProductGroups() {
        return sProductGroups;
    }

    public static void setProductGroupsChildIdToParent(TreeMap productGroups) {
        sProductGroupsParent = productGroups;
    }

    public static TreeMap getProductGroupsParent() {
        return sProductGroupsParent;
    }

    public static void productGroupsInitialisation() {
        ModelGroup[] productGroups = DatabaseApp.getAppRoomDao().getAllProductGroups();
        ModelGroup group;

        // - получим родителей с инициализированными списками
        //      находим все parentid и создаем для них пустой список
        LinkedHashMap<Integer, LinkedHashMap> prodGroupsParent = new LinkedHashMap<>();

        for (int i = 0; i < productGroups.length; i++) {
            group = productGroups[i];
            if (!prodGroupsParent.containsKey(group.parent_id)) {
                prodGroupsParent.put(group.parent_id,
                        new LinkedHashMap<ModelGroup, LinkedHashMap>());
            }
        }

        // - определим родителей элементов
        //      для каждого элемента ищем родительский список, в котором он будет находиться
        TreeMap<Integer, LinkedHashMap> prodGroupsChildIdToParent = new TreeMap<>();
        TreeMap<Integer, Integer> prodGroupsParentIdToId = new TreeMap<>(); //для поиска самого объекта категории родителя
        for (int i = 0; i < productGroups.length; i++) {
            group = productGroups[i];

            LinkedHashMap<ModelGroup, LinkedHashMap> parentTree =
                    prodGroupsParent.get(group.parent_id);

            prodGroupsChildIdToParent.put(group.id, parentTree);

            if (prodGroupsParent.containsKey(group.id))
                prodGroupsParentIdToId.put(group.id, i);
        }

        // - распределим элементы по родителям
        //      берем родительский элемент и в него добавляем текущий со своим списком
        for (int i = 0; i < productGroups.length; i++) {
            group = productGroups[i];

            LinkedHashMap<ModelGroup, LinkedHashMap> parentTree =
                    prodGroupsParent.get(group.parent_id);
            LinkedHashMap<ModelGroup, LinkedHashMap> childTree =
                    prodGroupsParent.get(group.id);

            // добавим элементы дополнительной навигации
            if (parentTree.isEmpty()) {
                if (group.parent_id != 0) {
                    //возврат на предыдущий уровень
                    parentTree.put(new ModelGroup(
                                    0, "...", group.parent_id,null, ModelGroup.GROUP_NM_PRODUCT_ADD),
                            prodGroupsChildIdToParent.get(group.parent_id));

                    //возврат по иерархии вверх
                    fillProductGroupsNaigationBack(
                            productGroups,
                            group.parent_id,
                            prodGroupsParentIdToId,
                            prodGroupsChildIdToParent,
                            parentTree
                    );

                } else {
                    //Список покупок
                    sShoppingListGroup=new ModelGroup(
                            Constants.SHOPPINGLIST_GROUP_ID,
                            getAppContext().getString(R.string.product_shoplist_group_name),
                            group.parent_id,
                            Constants.SHOPPINGLIST_GROUP_LOGO_LINK,
                            ModelGroup.GROUP_NM_VIEW
                    );
                    parentTree.put(sShoppingListGroup,null);

                    //Акции
                    parentTree.put(new ModelGroup(
                                    Constants.SALE_GROUP_ID,
                                    getAppContext().getString(R.string.product_sale_group_name),
                                    group.parent_id,
                                    Constants.SALE_GROUP_LOGO_LINK,
                                    ModelGroup.GROUP_NM_VIEW),
                            null);

                }
                //текущая группа категорий
                parentTree.put(new ModelGroup(
                                group.parent_id,
                                getAppContext().getString(R.string.default_product_group_name),
                                group.parent_id,
                                group.logo_link,
                                ModelGroup.GROUP_NM_PRODUCT_ADD),
                        null);


            }
            /*if (childTree!=null){
                group.name=group.name+">";
            }*/

            parentTree.put(group, childTree);
        }

        // - запишем в статик
        setProductGroups(prodGroupsParent.get(0));
        setProductGroupsChildIdToParent(prodGroupsChildIdToParent);
    }

    private static void fillProductGroupsNaigationBack(
            ModelGroup[] productGroups,
            int parentid,
            TreeMap<Integer, Integer> prodGroupsParentIdToId,
            TreeMap<Integer, LinkedHashMap> prodGroupsChildIdToParent,
            LinkedHashMap<ModelGroup, LinkedHashMap> parentTree) {

        //Добавляет дополнительную навигацию категорий по иерархии вверх

        if (parentid == 0) //добрались до конца
            return;

        ModelGroup productGroupParent = productGroups[prodGroupsParentIdToId.get(parentid)];

        fillProductGroupsNaigationBack(
                productGroups,
                productGroupParent.parent_id,
                prodGroupsParentIdToId,
                prodGroupsChildIdToParent,
                parentTree
        );

        parentTree.put(new ModelGroup(
                        parentid,
                        "<" + productGroupParent.name,
                        productGroupParent.parent_id,
                        productGroupParent.logo_link,
                        ModelGroup.GROUP_NM_VIEW),
                prodGroupsChildIdToParent.get(parentid));
    }

    public static ModelGroup getShoppingListGroup(){
        return sShoppingListGroup;
    }

    public static void addShoppingListGroupCount(int add){
        if (sShoppingListGroup.count!=null)
            sShoppingListGroup.count=sShoppingListGroup.count+add;
    }

    //Гео
    public static boolean isAutoGeoPosition() {
        return sAutoGeoPosition;
    }

    public static void setAutoGeoPosition(boolean flag) {
        if (flag == AppInstance.sAutoGeoPosition)
            return;

        GeoManager.setAutoGeoPositionInSettings(flag);
        AppInstance.sAutoGeoPosition = flag;
    }

    public static int getRadiusArea() {
        return sRadiusArea;
    }

    public static void setRadiusArea(int radius) {
        if (radius == AppInstance.sRadiusArea)
            return;

        GeoManager.setRadiusAreaInSettings(radius);
        AppInstance.sRadiusArea = radius;
    }

    public static LatLng getGeoPosition() {
        return sGeoPosition;
    }

    public static void setGeoPosition(LatLng position) {
        if (position.latitude == AppInstance.sGeoPosition.latitude
                && position.longitude == AppInstance.sGeoPosition.longitude)
            return;

        GeoManager.setGeoPositionInSettings(position);
        AppInstance.sGeoPosition = position;
    }

    public static void errorLog(String err_group, String error){

        DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();
        Call<Void> serviceCall = mDataApi.addLogMobile(err_group, error);
        SingletonRetrofit.enqueue(serviceCall,new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });

    }
}
