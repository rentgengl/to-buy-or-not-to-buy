package com.world.jteam.bonb.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.world.jteam.bonb.server.HTTPService;

import java.util.ArrayList;
import java.util.Date;

public class ModelComment implements Parcelable {
    public int product_id;
    public ModelUser user;
    public String comment;
    public float rating = 0;
    public Date date;

    public ModelComment(int product_id, ModelUser user, String comment, float rating){
        this.product_id = product_id;
        this.user = user;
        this.comment = comment;
        this.rating = rating;
    }

    public ArrayList<ModelComment> getCommetListByProduct(ModelProduct product){

        HTTPService http = new HTTPService();
        return null;

    }

    /*public static ArrayList<ModelComment> getTestData(){

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

    }*/


    protected ModelComment(Parcel in) {
        product_id = in.readInt();
        user = (ModelUser) in.readParcelable(ModelUser.class.getClassLoader());
        comment = in.readString();
        rating = in.readFloat();
        long tmpDate = in.readLong();
        date = tmpDate != -1 ? new Date(tmpDate) : null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(product_id);
        dest.writeParcelable(user,0);
        dest.writeString(comment);
        dest.writeFloat(rating);
        dest.writeLong(date != null ? date.getTime() : -1L);
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
