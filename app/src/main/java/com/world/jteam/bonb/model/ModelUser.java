package com.world.jteam.bonb.model;

import android.os.Parcel;
import android.os.Parcelable;

public class   ModelUser implements Parcelable {
    public String name;
    public String google_id;
    public int id;
    public String mail;
    public int city_id;


    public ModelUser(String name, String google_id, int id){
        this.name = name;
        this.google_id = google_id;
        this.id = id;
    }

    public ModelUser(String name, String google_id, String mail, int city){
        this.name = name;
        this.google_id = google_id;
        this.mail = mail;
        this.city_id = city;
    }

    protected ModelUser(Parcel in) {
        name = in.readString();
        google_id = in.readString();
        id = in.readInt();
        mail = in.readString();
        city_id = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(google_id);
        dest.writeInt(id);
        dest.writeString(mail);
        dest.writeInt(city_id);
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
