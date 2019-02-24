package com.world.jteam.bonb.model;

import android.app.Activity;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.world.jteam.bonb.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Entity
public class ModelGroup {
    @Ignore
    public static final int GROUP_NM_PRODUCT_ADD = 1; //Метод навигации по категориям - добавление продукта
    @Ignore
    public static final int GROUP_NM_VIEW = 2; //Метод навигации по категориям - просмотр

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

    public static class ProductGroupsAdapter extends ArrayAdapter<ModelGroup> {
        private Activity mActivity;

        public ProductGroupsAdapter(Activity activity, List<ModelGroup> objects) {
            super(activity, R.layout.fragment_product_groups_listview_item, objects);
            mActivity = activity;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = mActivity.getLayoutInflater();
            View row = inflater.inflate(R.layout.fragment_product_groups_listview_item, parent, false);

            TextView text = (TextView) row.findViewById(R.id.product_group_list_text);
            text.setText(getItem(position).toString());

            ImageView icon = (ImageView) row.findViewById(R.id.product_group_list_icon);
            icon.setImageResource(R.mipmap.ic_launcher);

            return row;
        }
    }

    //Получает массив категорий текущего списка с учетом метода навигации
    public static ArrayList<ModelGroup> getCurrentProductGroups(
            LinkedHashMap<ModelGroup, LinkedHashMap> currentProductGroups,
            int navigation_method) {

        ArrayList<ModelGroup> productGroups = new ArrayList<>();

        for (ModelGroup key : currentProductGroups.keySet()) {
            if (key.navigation_method != 0 && key.navigation_method != navigation_method)
                continue;

            productGroups.add(key);
        }

        return productGroups;
    }
}
