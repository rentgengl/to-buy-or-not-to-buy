package com.world.jteam.bonb.activity;

import android.arch.paging.PagedList;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.world.jteam.bonb.ldrawer.ActionBarDrawerToggle;
import com.world.jteam.bonb.ldrawer.DrawerArrowDrawable;
import com.world.jteam.bonb.activity.BarcodeActivity;
import com.world.jteam.bonb.Constants;
import com.world.jteam.bonb.DataApi;
import com.world.jteam.bonb.DatabaseApp;
import com.world.jteam.bonb.FlowLayout;
import com.world.jteam.bonb.Product;
import com.world.jteam.bonb.R;
import com.world.jteam.bonb.SingletonRetrofit;
import com.world.jteam.bonb.model.ModelGroup;
import com.world.jteam.bonb.model.ModelProduct;
import com.world.jteam.bonb.model.ModelProductFull;
import com.world.jteam.bonb.model.ModelSearchProductMethod;
import com.world.jteam.bonb.model.ModelSearchResult;
import com.world.jteam.bonb.paging.ProductDataSource;
import com.world.jteam.bonb.paging.ProductListAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //Леонов
    //Шакун3
    private final AppCompatActivity mThis=this;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    //Категории
    private Product.CategoriesAdapter mCategoriesAdapter;
    private LinkedHashMap<ModelGroup,LinkedHashMap> mProductCategoriesCurrent; //В момент выбора
    private LinkedHashMap<ModelGroup,LinkedHashMap> mProductCategoriesSelected; //Выбранный


    //Идентификатор результата сканирования ШК
    private static final int BARCODE_REQUEST = 1;
    //Режимы поиска по наименованию или группе
    public ModelSearchProductMethod searchMethod;

    //Переменные пагинации
    private MainActivity.MainThreadExecutor executor;
    private ProductListAdapter adapter;


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

        //Обработчик клика по кнопке сканирования
        ImageButton searchButton = this.findViewById(R.id.search_panel_button);
        searchButton.setOnClickListener(this);

        //Обработчик ввода текста в строку поиска
        EditText searchText = this.findViewById(R.id.search_panel_text);
        searchText.setOnKeyListener(new MainActivity.OnKeyPress());
        executor = new MainActivity.MainThreadExecutor();

        //По умолчанию отображаю товары первой группы
        this.searchMethod = new ModelSearchProductMethod(1);

        //Формирование списка товаров
        pagingStart();

        /*
        Intent intent = new Intent(MainActivity.this, AuthenticationActivity.class);
        intent.putExtra("userID",1);
        startActivity(intent);
        */
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

    //Заполнение и обработка меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
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
                break;
            case R.id.choose_geo:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //Категории

    private class ProductCategoryOnItemClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ModelGroup productCategory=mCategoriesAdapter.getItem(position);
            LinkedHashMap<ModelGroup,LinkedHashMap> productCategoryCategories=
                    mProductCategoriesCurrent.get(productCategory);

            searchByGroup(productCategory.id);
            //Раскрытие группы
            if (productCategoryCategories==null){
                mProductCategoriesSelected=mProductCategoriesCurrent;
                mDrawerLayout.closeDrawer(mDrawerList);
            //Возврат к родителю группы
            } else {

                mProductCategoriesCurrent =productCategoryCategories;
                mCategoriesAdapter.clear();
                mCategoriesAdapter.addAll(Product.getCurrentProductCategories(
                        mProductCategoriesCurrent,Product.CAT_NM_VIEW));
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BARCODE_REQUEST){
            //Обрабочик скана ШК
            if (data!=null){
                String barcodeResult=data.getStringExtra(getApplicationContext().getPackageName()+".barcode");
                if (resultCode==RESULT_OK){
                    //Попробую открыть карточку товара по ШК
                    showProductDetailByEAN(barcodeResult);
                } else{
                    //Ошибка сканирования ШК
                }
            }

            return;
        }
    }

    public void onClick(View v) {

        if (v.getId() == R.id.search_panel_button) {
            //Получить ШК
            Intent barcodeIntent=new Intent(this,BarcodeActivity.class);
            startActivityForResult(barcodeIntent,BARCODE_REQUEST);

        } else {
            //Клик по группе
            //Подгрузка списка товаров
            searchByGroup((int) v.getTag());

        }

    }

    //Обработчик ввода текста в поле поиска
    private class OnKeyPress implements View.OnKeyListener {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN & keyCode!=KeyEvent.KEYCODE_DEL){
                //(keyCode == KeyEvent.KEYCODE_ENTER)) {
                // сохраняем текст, введенный до нажатия Enter в переменную
                EditText editText = v.findViewById(R.id.search_panel_text);
                String strCatName = editText.getText().toString();
                if (!strCatName.equals(""))
                    searchByName(strCatName);

                return true;
            }
            return false;
        }
    }

    //Показать карточку товара по ШК
    private void showProductDetailByEAN(String EAN){

        //Запрос на сервер
        DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();
        Call<ModelProductFull> serviceCall = mDataApi.getProductFullByEAN(EAN);
        //Обработчик ответа сервера
        serviceCall.enqueue(new Callback<ModelProductFull>() {
            @Override
            public void onResponse(Call<ModelProductFull> call, Response<ModelProductFull> response) {
                ModelProductFull ss = response.body();
                showProductDetail(ss);
            }

            @Override
            public void onFailure(Call<ModelProductFull> call, Throwable t) {
                //Товар не найден, предложим ввести новый
                showErrorSearch();
                //Леонов
            }
        });

    }

    //Показать карточку товара по объекту
    private void showProductDetail(ModelProductFull prod){

        Intent intent = new Intent(this, ViewProduct.class);
        intent.putExtra("object", prod);
        startActivity(intent);

    }

    public void showErrorSearch() {
        Toast mt = Toast.makeText(this,"Ничего не найдено", Toast.LENGTH_LONG);
        mt.show();
    }

    public void showGroupList(List<ModelGroup> groupList){
        FlowLayout resultGroup = findViewById(R.id.search_result_group);
        //Подчищу старые теги групп
        resultGroup.removeAllViews();
        for (ModelGroup strGr : groupList) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics()));

            Button nButton = new Button(this, null, R.style.Widget_AppCompat_Button_Borderless);
            nButton.setHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, getResources().getDisplayMetrics()));
            nButton.setLayoutParams(layoutParams);
            nButton.setTextColor(getResources().getColor(R.color.colorTextGroupResultSearch));
            nButton.setGravity(Gravity.CENTER);
            nButton.setAllCaps(false);
            nButton.setBackgroundResource(R.drawable.search_result_group);
            nButton.setText(strGr.name);
            nButton.setOnClickListener(this);
            nButton.setTag(strGr.id);
            resultGroup.addView(nButton);
        }
    }

    //Пагинация
    //Поиск товаров по имени
    private void searchByName(String name) {

        searchMethod = new ModelSearchProductMethod(name);
        pagingStart();

        //Подгрузка списка групп
        DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();
        Call<ModelSearchResult> serviceCall = mDataApi.getProductGroupListByName(name,1,100);
        serviceCall.enqueue(new Callback<ModelSearchResult>() {
            @Override
            public void onResponse(Call<ModelSearchResult> call, Response<ModelSearchResult> response) {
                ModelSearchResult ss = response.body();
                showGroupList(ss.getGroups());

            }

            @Override
            public void onFailure(Call<ModelSearchResult> call, Throwable t) {
                showErrorSearch();
            }
        });


    }

    //Поиск товаров по группе
    private void searchByGroup(int groupId){
        this.searchMethod = new ModelSearchProductMethod(groupId);
        pagingStart();
    }

    //Основной обработчик заполнения списка товаров
    private void pagingStart() {
        setupRecyclerView();
        setupDataSource(searchMethod);
    }

    //Инициализация объекта
    private void setupRecyclerView() {

        adapter = new ProductListAdapter();

        RecyclerView recyclerView = findViewById(R.id.productRW);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        recyclerView.setOnClickListener(this);
    }

    //Инициализация источника данных
    private void setupDataSource(ModelSearchProductMethod mSearchMethod) {

        // Initialize Data Source
        ProductDataSource dataSource = new ProductDataSource();//Добавить строку поиска по группе
        dataSource.searchMethod = mSearchMethod;
        // Configure paging
        PagedList.Config config = new PagedList.Config.Builder()
                // Number of items to fetch at once. [Required]
                .setPageSize(Constants.DEFAULT_PER_PAGE)
                // Number of items to fetch on initial load. Should be greater than Page size. [Optional]
                .setInitialLoadSizeHint(Constants.DEFAULT_PER_PAGE * 2)
                .setEnablePlaceholders(true) // Show empty views until data is available
                .build();

        // Build PagedList
        PagedList<ModelProduct> list =
                new PagedList.Builder<>(dataSource, config) // Can pass `pageSize` directly instead of `config`
                        // Do fetch operations on the main thread. We'll instead be using Retrofit's
                        // built-in enqueue() method for background api calls.
                        .setFetchExecutor(executor)
                        // Send updates on the main thread
                        .setNotifyExecutor(executor)
                        .build();

        // Ideally, the above code should be placed in a ViewModel class so that the list can be
        // retained across configuration changes.

        // Required only once. Paging will handle fetching and updating the list.
        adapter.submitList(list);

    }

    // Классы пагинации
    class MainThreadExecutor implements Executor {
        private final Handler mHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            mHandler.post(command);
        }
    }


}
