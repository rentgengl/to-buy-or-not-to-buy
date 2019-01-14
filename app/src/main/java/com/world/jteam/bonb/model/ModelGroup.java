package com.world.jteam.bonb.model;

public class ModelGroup {

    public int id;
    public String name;
    public int parent_id;
    public String logo_link;

    public ModelGroup(int id,String name,int parent_id, String logo_link){
        this.id = id;
        this.name = name;
        this.parent_id = parent_id;
        this.logo_link = logo_link;
    }

}
