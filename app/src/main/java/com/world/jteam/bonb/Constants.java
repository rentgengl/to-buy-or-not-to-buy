package com.world.jteam.bonb;

import android.os.Environment;

public class Constants {

    public static final String APP_PREFS_NAME = Constants.class.getPackage().getName();
    public static final String APP_CACHE_PATH =
            Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/Android/data/" + APP_PREFS_NAME + "/cache/";


    //HTTP
    public static final String HTTP_SERVER = "http://tst.tradecode.ru/";

    public static final String SERVICE_GET_PRODUCT = HTTP_SERVER + "GetProductByEAN.php?";
    public static final String SERVICE_GET_PRODUCTFULL_ID = HTTP_SERVER + "GetProductFullById.php?";
    public static final String SERVICE_GET_PRODUCTFULL_EAN = HTTP_SERVER + "GetProductFullByEAN.php?";
    public static final String SERVICE_GET_PRODUCT_LIST_BY_GROUP = HTTP_SERVER + "GetProductListByGroup.php?";
    public static final String SERVICE_GET_PRODUCT_GROU_LIST_BY_NAME = HTTP_SERVER + "GetProductGroupListByName.php?";
    public static final String SERVICE_POST_NEW_PRODUCT = HTTP_SERVER + "NewProduct.php";

    public static final String SERVER_PATH_IMAGE = "img/";
    public static final String SERVICE_GET_IMAGE = HTTP_SERVER + SERVER_PATH_IMAGE;

    public static Integer DEFAULT_PER_PAGE = 15; //Количество элементов считываемых с сервера
    public static int MAX_PRODUCT_LIST_ITEMS = 1000; // Максимальное количество которое можно вывести в список
    public static int MAX_PRODUCT_LIST_FLING_Y = 5000; //максимальная скорость броска списка

    public static final String SEARCH_METHOD_BY_ID = "SEARCH_METHOD_BY_ID";
    public static final String SEARCH_METHOD_BY_EAN = "SEARCH_METHOD_BY_EAN";
    public static final String SEARCH_METHOD_BY_NAME = "SEARCH_METHOD_BY_NAME";
    public static final String SEARCH_METHOD_BY_GROUP = "SEARCH_METHOD_BY_GROUP";

    public static final int DECODE_IMAGE_SIZE_SMALL=30;
    public static final int DECODE_IMAGE_SIZE_MEDIUM=100;
    public static final int DECODE_IMAGE_SIZE_LARGE=500;

    public static final int DEFAULT_RADIUS_AREA=5; //км
    public static final int UPDATE_RATE_GEO_POSITION=1800000; //30мин в миллисекундах

}