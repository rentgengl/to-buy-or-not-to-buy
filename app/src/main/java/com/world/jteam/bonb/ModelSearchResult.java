package com.world.jteam.bonb;

import java.util.List;

public class ModelSearchResult {
    private List<ModelGroup> groups;
    private List<ModelProduct> products;

    public List<ModelGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<ModelGroup> groups) {
        this.groups = groups;
    }

    public List<ModelProduct> getProducts() {
        return products;
    }

    public void setProducts(List<ModelProduct> products) {
        this.products = products;
    }
}
