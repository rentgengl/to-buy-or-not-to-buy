package com.world.jteam.bonb.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.world.jteam.bonb.server.HTTPService;

import java.util.ArrayList;

public class ModelComment implements Parcelable {
    public ModelUser user;
    public String comment;
    public float raiting = 0;


    public ModelComment(ModelUser user, String comment, float raiting){
        this.user = user;
        this.comment = comment;
        this.raiting = raiting;
    }

    public ArrayList<ModelComment> getCommetListByProduct(ModelProduct product){

        HTTPService http = new HTTPService();
        return null;

    }

    public static ArrayList<ModelComment> getTestData(){

        ArrayList<ModelComment> res = new ArrayList<>();

        res.add(new ModelComment(new ModelUser("Иван",null,0),
                "Отличая штука всем советую!",4.3f));
        res.add(new ModelComment(new ModelUser("Григорий Леонидович",null,0),
                "Ерунда, не берите это дерьмо - полная хрень!",2.3f));

        res.add(new ModelComment(new ModelUser("Григорий Леонидович",null,0),
                "Ерунда, не берите это дерьмо - полная хрень!",2.3f));

        res.add(new ModelComment(new ModelUser("Григорий Леонидович",null,0),
                "Ерунда, не берите это дерьмо - полная хрень!",2.3f));
        return res;

    }


    protected ModelComment(Parcel in) {
        user = (ModelUser) in.readValue(ModelUser.class.getClassLoader());
        comment = in.readString();
        raiting = in.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(user);
        dest.writeString(comment);
        dest.writeFloat(raiting);
    }

    @SuppressWarnings("unused")
    public static final Creator<ModelComment> CREATOR = new Creator<ModelComment>() {
        @Override
        public ModelComment createFromParcel(Parcel in) {
            return new ModelComment(in);
        }

        @Override
        public ModelComment[] newArray(int size) {
            return new ModelComment[size];
        }
    };


}
