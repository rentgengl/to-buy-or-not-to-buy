package com.world.jteam.bonb;

import android.os.Parcel;
import android.os.Parcelable;

public class ModelUser implements Parcelable {
    public String name;
    public String google_id;
    public String id;
    public String mail;
    public String city;


    public ModelUser(String name, String google_id, String id){
        this.name = name;
        this.google_id = google_id;
        this.id = id;
    }

    protected ModelUser(Parcel in) {
        name = in.readString();
        google_id = in.readString();
        id = in.readString();
        mail = in.readString();
        city = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(google_id);
        dest.writeString(id);
        dest.writeString(mail);
        dest.writeString(city);
    }

    @SuppressWarnings("unused")
    public static final Creator<ModelUser> CREATOR = new Creator<ModelUser>() {
        @Override
        public ModelUser createFromParcel(Parcel in) {
            return new ModelUser(in);
        }

        @Override
        public ModelUser[] newArray(int size) {
            return new ModelUser[size];
        }
    };


}
