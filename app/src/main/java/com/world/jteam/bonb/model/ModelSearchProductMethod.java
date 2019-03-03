package com.world.jteam.bonb.model;

public class ModelSearchProductMethod {

    public static final int SEARCH_BY_NAME = 1;
    public static final int SEARCH_BY_GROUP = 2;
    public static final int SEARCH_BY_NAME_AND_GROUP = 3;
    public int searchMethod;
    public String searchText;
    public int searchGroup;

    public ModelSearchProductMethod(String searchText){
        this.searchText = searchText;
        this.searchMethod = SEARCH_BY_NAME;
    }

    public ModelSearchProductMethod(int searchGroup){
        this.searchGroup = searchGroup;
        this.searchMethod = SEARCH_BY_GROUP;
    }

    public ModelSearchProductMethod(String searchText,int searchGroup){
        this.searchGroup = searchGroup;
        this.searchText = searchText;
        this.searchMethod = SEARCH_BY_NAME_AND_GROUP;

    }

}
