package com.world.jteam.bonb.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class ModelGroup {
    @PrimaryKey
    public int id;
    public String name;
    public int parent_id;
    public String logo_link;
    @Ignore
    public int navigation_method;

    public ModelGroup(int id,String name,int parent_id, String logo_link){
        this.id = id;
        this.name = name;
        this.parent_id = parent_id;
        this.logo_link = logo_link;
    }

    public ModelGroup(int id, String name, int parent_id, int navigation_method) {
        this.id = id;
        this.name = name;
        this.parent_id = parent_id;
        this.navigation_method = navigation_method;
    }

    @Override
    public String toString() {
        return name;
    }

}
