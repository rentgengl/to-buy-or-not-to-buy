package com.world.jteam.bonb.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class ModelPrice implements Parcelable {

    public int price;
    public int discount;
    public int product_id;
    public ModelMarket market;
    public Date date;
    public boolean sale;
    public int user_id;

    public ModelPrice(){}

    public ModelPrice(int product_id,int price,int market_id, int user_id){
        this.product_id = product_id;
        this.price = price;
        this.market = new ModelMarket(market_id);
        this.user_id = user_id;
    }

    protected ModelPrice(Parcel in) {
        price = in.readInt();
        discount = in.readInt();
        market = (ModelMarket) in.readValue(ModelMarket.class.getClassLoader());
        long tmpDate = in.readLong();
        date = tmpDate != -1 ? new Date(tmpDate) : null;
        sale = in.readByte() != 0x00;

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(price);
        dest.writeInt(discount);
        dest.writeValue(market);
        dest.writeLong(date != null ? date.getTime() : -1L);
        dest.writeByte((byte) (sale ? 0x01 : 0x00));

    }

    @SuppressWarnings("unused")
    public static final Creator<ModelPrice> CREATOR = new Creator<ModelPrice>() {
        @Override
        public ModelPrice createFromParcel(Parcel in) {
            return new ModelPrice(in);
        }

        @Override
        public ModelPrice[] newArray(int size) {
            return new ModelPrice[size];
        }
    };


}
