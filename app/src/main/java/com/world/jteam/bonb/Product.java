package com.world.jteam.bonb;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.world.jteam.bonb.model.ModelGroup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

public class Product {
    //Категории
    private static LinkedHashMap<ModelGroup, LinkedHashMap> sProductCategories; //Дерево категорий
    public static final int CAT_NM_PRODUCT_ADD = 1; //Метод навигации по категориям - добавление продукта
    public static final int CAT_NM_VIEW = 2; //Метод навигации по категориям - просмотр

    public static void setProductCategories(LinkedHashMap productCategories) {
        sProductCategories = productCategories;
    }

    public static LinkedHashMap getProductCategories() {
        return sProductCategories;
    }

    public static void categoryInitialisation() {
        ModelGroup[] productCategories = DatabaseApp.getAppRoomDao().getAllProductCategories();
        ModelGroup productCategory;

        // - получим родителей с инициализированными списками
        //      находим все parentid и создаем для них пустой список
        LinkedHashMap<Integer, LinkedHashMap> prodCatParent = new LinkedHashMap<>();

        for (int i = 0; i < productCategories.length; i++) {
            productCategory = productCategories[i];
            if (!prodCatParent.containsKey(productCategory.parent_id)) {
                prodCatParent.put(productCategory.parent_id,
                        new LinkedHashMap<ModelGroup, LinkedHashMap>());
            }
        }

        // - определим родителей элементов
        //      для каждого элемента ищем родительский список, в котором он будет находиться
        TreeMap<Integer, LinkedHashMap> prodCatChildIdToParent = new TreeMap<>();
        TreeMap<Integer, Integer> prodCatParentIdToId = new TreeMap<>(); //для поиска самого объекта категории родителя
        for (int i = 0; i < productCategories.length; i++) {
            productCategory = productCategories[i];

            LinkedHashMap<ModelGroup, LinkedHashMap> parentTree =
                    prodCatParent.get(productCategory.parent_id);

            prodCatChildIdToParent.put(productCategory.id, parentTree);

            if (prodCatParent.containsKey(productCategory.id))
                prodCatParentIdToId.put(productCategory.id, i);
        }

        // - распределим элементы по родителям
        //      берем родительский элемент и в него добавляем текущий со своим списком
        for (int i = 0; i < productCategories.length; i++) {
            productCategory = productCategories[i];

            LinkedHashMap<ModelGroup, LinkedHashMap> parentTree =
                    prodCatParent.get(productCategory.parent_id);
            LinkedHashMap<ModelGroup, LinkedHashMap> childTree =
                    prodCatParent.get(productCategory.id);

            // добавим элементы дополнительной навигации
            if (parentTree.isEmpty()) {
                if (productCategory.parent_id != 0) {
                    //возврат на предыдущий уровень
                    parentTree.put(new ModelGroup(
                                    0, "...", productCategory.parent_id, CAT_NM_PRODUCT_ADD),
                            prodCatChildIdToParent.get(productCategory.parent_id));

                    //возврат по иерархии вверх
                    fillProductCategoriesNaigationBack(
                            productCategories,
                            productCategory.parent_id,
                            prodCatParentIdToId,
                            prodCatChildIdToParent,
                            parentTree
                    );

                }
                //текущая группа категорий
                parentTree.put(new ModelGroup(
                                productCategory.parent_id,
                                AppInstance.getAppContext().getString(R.string.default_category_name),
                                productCategory.parent_id,
                                CAT_NM_PRODUCT_ADD),
                        null);


            }
            /*if (childTree!=null){
                productCategory.name=productCategory.name+">";
            }*/

            parentTree.put(productCategory, childTree);
        }

        // - запишем в статик
        setProductCategories(prodCatParent.get(0));
    }

    //Добавляет дополнительную навигацию категорий по иерархии вверх
    private static void fillProductCategoriesNaigationBack(
            ModelGroup[] productCategories,
            int parentid,
            TreeMap<Integer, Integer> prodCatParentIdToId,
            TreeMap<Integer, LinkedHashMap> prodCatChildIdToParent,
            LinkedHashMap<ModelGroup, LinkedHashMap> parentTree) {

        if (parentid == 0) //добрались до конца
            return;

        ModelGroup productCategoryParent = productCategories[prodCatParentIdToId.get(parentid)];

        fillProductCategoriesNaigationBack(
                productCategories,
                productCategoryParent.parent_id,
                prodCatParentIdToId,
                prodCatChildIdToParent,
                parentTree
        );

        parentTree.put(new ModelGroup(
                        parentid,
                        "<" + productCategoryParent.name,
                        productCategoryParent.parent_id,
                        CAT_NM_VIEW),
                prodCatChildIdToParent.get(parentid));
    }

    //Получает массив категорий текущего списка с учетом метода навигации
    public static ArrayList<ModelGroup> getCurrentProductCategories(
            LinkedHashMap<ModelGroup, LinkedHashMap> currentProductCategories,
            int navigation_method) {

        ArrayList<ModelGroup> productCategories = new ArrayList<>();

        for (ModelGroup key : currentProductCategories.keySet()) {
            if (key.navigation_method != 0 && key.navigation_method != navigation_method)
                continue;

            productCategories.add(key);
        }

        return productCategories;
    }

    public static class CategoriesAdapter extends ArrayAdapter<ModelGroup> {
        private Activity mActivity;

        public CategoriesAdapter(Activity activity, List<ModelGroup> objects) {
            super(activity, R.layout.fragment_product_category_listview_item, objects);
            mActivity = activity;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = mActivity.getLayoutInflater();
            View row = inflater.inflate(R.layout.fragment_product_category_listview_item, parent, false);

            TextView text = (TextView) row.findViewById(R.id.product_category_list_text);
            text.setText(getItem(position).toString());

            ImageView icon = (ImageView) row.findViewById(R.id.product_category_list_icon);
            icon.setImageResource(R.mipmap.ic_launcher);

            return row;
        }
    }
}
