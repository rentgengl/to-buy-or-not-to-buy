package com.world.jteam.bonb.server;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.world.jteam.bonb.Constants;
import com.world.jteam.bonb.model.ModelProduct;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class HTTPService {

    final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

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

    //Получить из продукта JSON-объект
    JSONObject GetJObjectByProduct(ModelProduct product) {

        JSONObject res = new JSONObject();


        try {
            res.put("id", product.id);
            res.put("name", product.name);
            res.put("EAN", product.EAN);
            res.put("price", product.price);

            //res.put("imageSmall", product.imageSmall);

            res.put("rating", product.rating);
            res.put("userID", product.userID);
            res.put("newComment", product.newComment);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return res;
    }

}
