package com.world.jteam.bonb.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ModelProperty implements Parcelable{
    public String name;
    public String value;
    public String logo_link;

    public ModelProperty(String name, String value, String logo_link){

        this.name = name;
        this.value = value;
        this.logo_link = logo_link;

    }


    protected ModelProperty(Parcel in) {
        name = in.readString();
        value = in.readString();
        logo_link = in.readString();

    }

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(name);
        dest.writeString(value);
        dest.writeString(logo_link);

    }
    public static final Creator<ModelProperty> CREATOR = new Creator<ModelProperty>() {
        @Override
        public ModelProperty createFromParcel(Parcel in) {
            return new ModelProperty(in);
        }

        @Override
        public ModelProperty[] newArray(int size) {
            return new ModelProperty[size];
        }
    };
}
