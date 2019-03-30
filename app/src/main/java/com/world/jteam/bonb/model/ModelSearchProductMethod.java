package com.world.jteam.bonb.model;

public class ModelSearchProductMethod {

    public static final int SEARCH_BY_NAME = 1;
    public static final int SEARCH_BY_GROUP = 2;
    public static final int SEARCH_BY_NAME_AND_GROUP = 3;
    public int searchMethod;
    public String searchText;
    public int searchGroup;
    public int market_id;

    public ModelSearchProductMethod(String searchText){
        this.searchText = searchText;
        this.searchMethod = SEARCH_BY_NAME;
    }

    public ModelSearchProductMethod(int searchGroup){
        this.searchGroup = searchGroup;
        this.searchMethod = SEARCH_BY_GROUP;
        this.market_id = 0;
    }

    public ModelSearchProductMethod(String searchText,int searchGroup){
        this.searchGroup = searchGroup;
        this.searchText = searchText;
        this.searchMethod = SEARCH_BY_NAME_AND_GROUP;
        this.market_id = 0;
    }

    public ModelSearchProductMethod(String searchText,int searchGroup, int market_id){
        this.searchGroup = searchGroup;
        this.searchText = searchText;
        this.market_id = market_id;
        this.searchMethod = SEARCH_BY_NAME_AND_GROUP;

    }

}
