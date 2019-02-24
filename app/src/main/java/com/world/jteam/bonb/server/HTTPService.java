package com.world.jteam.bonb.server;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.world.jteam.bonb.Constants;
import com.world.jteam.bonb.model.ModelComment;
import com.world.jteam.bonb.model.ModelMarket;
import com.world.jteam.bonb.model.ModelPrice;
import com.world.jteam.bonb.model.ModelProduct;
import com.world.jteam.bonb.model.ModelProductFull;
import com.world.jteam.bonb.model.ModelUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class HTTPService {

    final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

    //Получение данных по id группы товара
    public ArrayList<ModelProduct> GetProductListByGroup(String groupID) throws IOException {
        OkHttpClient clientOk = new OkHttpClient();

        ArrayList<ModelProduct> resultData;
        resultData = new ArrayList<ModelProduct>();
        //Подготовка HTTP запроса
        String url = Constants.SERVICE_GET_PRODUCT_LIST_BY_GROUP + "&id=" + groupID;
        Request requestHTTP = new Request.Builder()
                .url(url)
                .build();
        //Выполнение HTTP запроса
        Response responseHTTP = clientOk.newCall(requestHTTP).execute();

        //Если ответ не 200 верну пустую карту
        if (!responseHTTP.isSuccessful()) {
            return resultData;
        }
        ;

        //Читаю данные JSON
        String jsonData = responseHTTP.body().string();
        try {
            JSONArray Jarray = new JSONArray(jsonData);

            for (int i = 0; i < Jarray.length(); i++) {
                JSONObject object = Jarray.getJSONObject(i);

                resultData.add(GetProductByJObject(object));

            }
            return resultData;
        } catch (JSONException e) {
            return resultData;
        }

    }

    private ModelProductFull getTovarFullByURL(String url) throws IOException {

        OkHttpClient clientOk = new OkHttpClient();
        //Подготовка HTTP запроса
        Request requestHTTP = new Request.Builder()
                .url(url)
                .build();
        //Выполнение HTTP запроса
        Response responseHTTP = clientOk.newCall(requestHTTP).execute();

        //Если ответ не 200 верну пустую карту
        if (!responseHTTP.isSuccessful()) {
            return null;
        }

        //Читаю данные JSON
        String jsonData = responseHTTP.body().string();
        try {
            JSONObject Jobject = new JSONObject(jsonData);
            return GetProductFullByJObject(Jobject);
        } catch (JSONException e) {
            return null;
        }

    }


    public boolean createNewProduct(ModelProduct newProduct) throws IOException {
        OkHttpClient clientOk = new OkHttpClient();

        // Создам структуру JSON значений полей товара
        JSONObject productJSON = GetJObjectByProduct(newProduct);
        MultipartBuilder reqData = new MultipartBuilder();
        reqData.type(MultipartBuilder.FORM);

        reqData.addFormDataPart("dataJSON", productJSON.toString());


        // Подготовка картинок для отправки
        if (newProduct.imageSmall_link != null) {
            File sourceFile = new File(newProduct.imageSmall_link);
            reqData.addFormDataPart(
                    "imageSmall",
                    "imageSmall.png",
                    RequestBody.create(MEDIA_TYPE_PNG, sourceFile));
        }

        RequestBody requestBody = reqData.build();

        // Собираю запрос
        Request requestHTTP = new Request.Builder()
                .url(Constants.SERVICE_POST_NEW_PRODUCT)
                .post(requestBody)
                .build();

        //Выполнение HTTP запроса
        Response responseHTTP = clientOk.newCall(requestHTTP).execute();

        //Если ответ не 200 верну пустую карту
        if (!responseHTTP.isSuccessful()) {
            return false;
        } else {
            return true;
        }

    }

    //Получить из JSON-объекта продукт
    ModelProduct GetProductByJObject(JSONObject jData) {

        ModelProduct res;
        try {
            res = new ModelProduct(
                    jData.getString("id"),
                    jData.getString("name"));

            if (jData.has("price"))
                res.price = jData.getInt("price");

            if (jData.has("price_min"))
                res.price_min = jData.getInt("price_min");

            if (jData.has("price_max"))
                res.price_max = jData.getInt("price_max");

            if (jData.has("comment_count"))
                res.comment_count = jData.getInt("comment_count");

            if (jData.has("raiting"))
                res.raiting = (float) jData.getDouble("raiting");

            res.setImageLink(Constants.SERVICE_GET_IMAGE + jData.getString("imageSmall_link"));

        } catch (JSONException e) {
            res = new ModelProduct("0", "Error");
        }
        return res;

    }

    ModelProductFull GetProductFullByJObject(JSONObject jData) {

        ModelProductFull res;


        res = new ModelProductFull(GetProductByJObject(jData));

        //setTestData(res);

        if (jData.has("prices"))
            try {
                res.prices = GetPriceListByJObject(jData.getJSONArray("prices"));
            } catch (JSONException e) {
            }
        if (jData.has("comments"))
            try {
                res.comments = GetCommentListByJObject(jData.getJSONArray("comments"));
            } catch (JSONException e) {
            }
        if (jData.has("images_links")) {

            try {
                JSONArray Jarray = jData.getJSONArray("images_links");
                res.images_links = new ArrayList<String>();
                for (int i = 0; i < Jarray.length(); i++) {

                    res.images_links.add(Constants.SERVICE_GET_IMAGE + Jarray.getString(i));

                }
            } catch (JSONException e) {
            }
        }

        return res;

    }


    ArrayList<ModelPrice> GetPriceListByJObject(JSONArray Jarray) throws JSONException {

        ArrayList<ModelPrice> res = new ArrayList<ModelPrice>();

        for (int i = 0; i < Jarray.length(); i++) {
            JSONObject object = Jarray.getJSONObject(i);

            res.add(GetPriceByJObject(object));

        }

        return res;

    }


    ModelPrice GetPriceByJObject(JSONObject jData) {

        ModelPrice res;
        try {
            res = new ModelPrice();
            if (jData.has("price"))
                res.price = jData.getInt("price");
            if (jData.has("date")) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                res.date = dateFormat.parse(jData.getString("date"));
            }

            if (jData.has("market"))
                res.market = GetMarketByJObject(jData.getJSONObject("market"));
            else res.market = ModelMarket.getDefaultMarket();

        } catch (JSONException e) {
            return null;
        } catch (ParseException e) {
            return null;
        }
        return res;

    }

    ModelMarket GetMarketByJObject(JSONObject jData) {

        ModelMarket res;
        String name = "";
        String adress = "";
        String logo_link = "";


        if (jData.has("name"))
            try {
                name = jData.getString("name");
            } catch (JSONException e) {
                name = "Неопределено";
            }


        if (jData.has("adress"))
            try {
                adress = jData.getString("adress");
            } catch (JSONException e) {
                adress = "Адрес неопределен";
            }

        if (jData.has("logo_link"))
            try {
                logo_link = Constants.SERVICE_GET_IMAGE + jData.getString("logo_link");
            } catch (JSONException e) {
                logo_link = null;
            }

        res = new ModelMarket(name, adress, logo_link);

        if (jData.has("latitude") & jData.has("longitude")) {
            try {
                res.setLatLng(jData.getDouble("latitude"), jData.getDouble("longitude"));
            } catch (JSONException e) {
            }
        }

        return res;

    }

    ArrayList<ModelComment> GetCommentListByJObject(JSONArray Jarray) throws JSONException {

        ArrayList<ModelComment> res = new ArrayList<ModelComment>();

        for (int i = 0; i < Jarray.length(); i++) {
            JSONObject object = Jarray.getJSONObject(i);

            res.add(GetCommentByJObject(object));

        }

        return res;

    }

    ModelComment GetCommentByJObject(JSONObject jData) {

        ModelComment res;
        try {
            res = new ModelComment(0, null, null, 0f);
            if (jData.has("comment"))
                res.comment = jData.getString("comment");

            if (jData.has("raiting"))
                res.raiting = (float) jData.getDouble("raiting");

            if (jData.has("user_name"))
                res.user = new ModelUser(jData.getString("user_name"), null, 0);

            if (jData.has("product_id"))
                res.product_id = jData.getInt("product_id");

            if (jData.has("date")) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");
                res.date = dateFormat.parse(jData.getString("date"));
            }

        } catch (JSONException e) {
            return null;
        } catch (ParseException e) {
            return null;
        }
        return res;

    }


    /*protected void setTestData(ModelProductFull pr) {

        ArrayList<ModelPrice> mp = new ArrayList<ModelPrice>();
        ModelMarket mark = ModelMarket.getMarketById(22);

        ModelPrice prc = new ModelPrice();
        prc.market = mark;
        prc.price = 345;
        prc.sale = true;
        mp.add(prc);
        pr.prices = mp;
        pr.comments = ModelComment.getTestData();

    }*/

    //Получить из продукта JSON-объект
    JSONObject GetJObjectByProduct(ModelProduct product) {

        JSONObject res = new JSONObject();


        try {
            res.put("id", product.id);
            res.put("name", product.name);
            res.put("EAN", product.EAN);
            res.put("price", product.price);

            //res.put("imageSmall", product.imageSmall);

            res.put("raiting", product.raiting);
            res.put("userID", product.userID);
            res.put("newComment", product.newComment);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return res;
    }


}
