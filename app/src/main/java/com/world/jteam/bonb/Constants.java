package com.world.jteam.bonb;

import android.os.Environment;

public class Constants {

    public static final String APP_PREFS_NAME = Constants.class.getPackage().getName();
    public static final String APP_CACHE_PATH =
            Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/Android/data/" + APP_PREFS_NAME + "/cache/";


    //HTTP
    public static final String HTTP_SERVER = "https://goodno.ru/services/";

    public static final String SERVICE_POST_NEW_PRODUCT = HTTP_SERVER + "NewProduct.php";

    //Picasso работает только с http, а мне и не жалко
    public static final String SERVER_PATH_IMAGE = "img/";
    public static final String SERVICE_GET_IMAGE = "http://goodno.ru/" + SERVER_PATH_IMAGE;

    public static final String SERVER_PATH_GROUPS_LOGO = "groups_logo/";
    public static final String SERVICE_GET_GROUPS_LOGO = "http://goodno.ru/" + SERVER_PATH_GROUPS_LOGO;
    public static final String SALE_GROUP_LOGO_LINK = "sale.png";
    public static final int SALE_GROUP_ID = -100;
    public static final String SHOPPINGLIST_GROUP_LOGO_LINK = "shopping_list.png";
    public static final int SHOPPINGLIST_GROUP_ID = -99;
    public static final int SHOPPINGLIST_MANUAL_ID = -100;

    public static Integer DEFAULT_PER_PAGE = 15; //Количество элементов считываемых с сервера
    public static int MAX_PRODUCT_LIST_ITEMS = 1000; // Максимальное количество которое можно вывести в список
    public static int MAX_PRODUCT_LIST_FLING_Y = 5000; //максимальная скорость броска списка

    public static final int DECODE_IMAGE_SIZE_SMALL=30;
    public static final int DECODE_IMAGE_SIZE_MEDIUM=100;
    public static final int DECODE_IMAGE_SIZE_LARGE=500;

    public static final int DEFAULT_RADIUS_AREA=5; //км
    public static final int UPDATE_RATE_GEO_POSITION=1800000; //30мин в миллисекундах

    public static final int UPDATE_RATE_VERSION = 18000000; //5ч в миллисекундах

}