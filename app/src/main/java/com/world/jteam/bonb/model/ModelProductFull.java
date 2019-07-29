package com.world.jteam.bonb.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ModelProductFull extends ModelProduct implements Parcelable {
    public ArrayList<ModelPrice> prices;
    public ArrayList<ModelComment> comments;
    public ArrayList<ModelProperty> properties;
    public ArrayList<String> images_links;

    public ModelProductFull(ModelProduct product) {
        this.id = product.id;
        this.name = product.name;
        this.price = product.price;
        this.price_min = product.price_min;
        this.price_max = product.price_max;
        this.sale = product.sale;
        this.rating = product.rating;
        this.user_leave_comment = product.user_leave_comment;
    }

    //Парселэйбл
    protected ModelProductFull(Parcel in) {
        id = in.readInt();
        price = in.readInt();
        price_min = in.readInt();
        price_max = in.readInt();
        sale = in.readByte();
        comment_count = in.readInt();
        userID = in.readInt();
        name = in.readString();
        EAN = in.readString();
        imageSmall_link = in.readString();
        newComment = in.readString();
        rating = in.readFloat();
        user_leave_comment = in.readByte();
        if (in.readByte() == 0x01) {
            prices = new ArrayList<ModelPrice>();
            in.readList(prices, ModelPrice.class.getClassLoader());
        } else {
            prices = null;
        }
        if (in.readByte() == 0x01) {
            comments = new ArrayList<ModelComment>();
            in.readList(comments, ModelComment.class.getClassLoader());
        } else {
            comments = null;
        }
        if (in.readByte() == 0x01) {
            images_links = new ArrayList<String>();
            in.readList(images_links, String.class.getClassLoader());
        } else {
            images_links = null;
        }
        if (in.readByte() == 0x01) {
            properties = new ArrayList<ModelProperty>();
            in.readList(properties, ModelProperty.class.getClassLoader());
        } else {
            properties = null;
        }


    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(price);
        dest.writeInt(price_min);
        dest.writeInt(price_max);
        dest.writeByte(sale);
        dest.writeInt(comment_count);
        dest.writeInt(userID);
        dest.writeString(name);
        dest.writeString(EAN);
        dest.writeString(imageSmall_link);
        dest.writeString(newComment);
        dest.writeFloat(rating);
        dest.writeByte(user_leave_comment);
        if (prices == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(prices);
        }
        if (comments == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(comments);
        }
        if (images_links == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(images_links);
        }

        if (properties== null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(properties);
        }
    }

    @SuppressWarnings("unused")
    public static final Creator<ModelProductFull> CREATOR = new Creator<ModelProductFull>() {
        @Override
        public ModelProductFull createFromParcel(Parcel in) {
            return new ModelProductFull(in);
        }

        @Override
        public ModelProductFull[] newArray(int size) {
            return new ModelProductFull[size];
        }
    };



}
