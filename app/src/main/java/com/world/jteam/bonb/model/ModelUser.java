package com.world.jteam.bonb.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ModelUser implements Parcelable {
    public String name;
    public String displayName;
    public String google_id;
    public int id;
    public String mail;

    public ModelUser(String name, String google_id, int id){
        this.name = name;
        this.google_id = google_id;
        this.id = id;
    }

    public ModelUser(String name, String google_id, String mail){
        this.name = name;
        this.google_id = google_id;
        this.mail = mail;
    }

    protected ModelUser(Parcel in) {
        name = in.readString();
        displayName = in.readString();
        google_id = in.readString();
        id = in.readInt();
        mail = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(displayName);
        dest.writeString(google_id);
        dest.writeInt(id);
        dest.writeString(mail);
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

    public boolean isAuthUser(){
        return id>=0;
    }

    /*private void generatedisplayName(){
        int nameLenght = name.length();
        if (nameLenght<=4){
            displayName = name;
        } else {
            displayName = name.substring(0,2)+"***"+name.substring(nameLenght-2,nameLenght);
        }
    }*/
}
