package com.world.jteam.bonb;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.world.denacid.ldrawer.ActionBarDrawerToggle;
import com.world.denacid.ldrawer.DrawerArrowDrawable;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class MainActivity extends AppCompatActivity {
    //Леонов
    //Шакун2
    private final AppCompatActivity mThis=this;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    //Категории
    private Product.CategoriesAdapter mCategoriesAdapter;
    private LinkedHashMap<DatabaseApp.ProductCategories,LinkedHashMap> mProductCategoriesCurrent; //В момент выбора
    private LinkedHashMap<DatabaseApp.ProductCategories,LinkedHashMap> mProductCategoriesSelected; //Выбранный

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Подготовка навигации
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        //Кнопка вызова списка категорий
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navdrawer);

        DrawerArrowDrawable drawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                drawerArrow, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        Intent intent = new Intent(MainActivity.this, AuthenticationActivity.class);
        intent.putExtra("userID",1);
        startActivity(intent);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    //Категории
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                mDrawerLayout.closeDrawer(mDrawerList);
            } else {

                if (mProductCategoriesSelected==null){
                    mProductCategoriesSelected = Product.getProductCategories();
                }
                if (mProductCategoriesSelected!=null){
                    ArrayList productCategoriesList =
                            Product.getCurrentProductCategories(mProductCategoriesSelected,Product.CAT_NM_VIEW);

                    mCategoriesAdapter=new Product.CategoriesAdapter(mThis,productCategoriesList);
                    mDrawerList.setAdapter(mCategoriesAdapter);
                    mDrawerList.setOnItemClickListener(new ProductCategoryOnItemClickListener());
                    mProductCategoriesCurrent=mProductCategoriesSelected;

                    mDrawerLayout.openDrawer(mDrawerList);
                } else {
                    Toast.makeText(mThis,R.string.product_categories_not_init,Toast.LENGTH_LONG).show();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private class ProductCategoryOnItemClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            DatabaseApp.ProductCategories productCategory=mCategoriesAdapter.getItem(position);
            LinkedHashMap<DatabaseApp.ProductCategories,LinkedHashMap> productCategoryCategories=
                    mProductCategoriesCurrent.get(productCategory);
            if (productCategoryCategories==null){
                mProductCategoriesSelected=mProductCategoriesCurrent;
                mDrawerLayout.closeDrawer(mDrawerList);

            } else {
                mProductCategoriesCurrent =productCategoryCategories;
                mCategoriesAdapter.clear();
                mCategoriesAdapter.addAll(Product.getCurrentProductCategories(
                        mProductCategoriesCurrent,Product.CAT_NM_VIEW));
            }

        }
    }
}
