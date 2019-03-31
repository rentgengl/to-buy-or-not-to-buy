package com.world.jteam.bonb.activity;

import android.Manifest;
import android.arch.paging.PagedList;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.TextView;
import android.widget.Toast;

import com.world.jteam.bonb.AppInstance;
import com.world.jteam.bonb.Constants;
import com.world.jteam.bonb.MarketPageFragment;
import com.world.jteam.bonb.R;
import com.world.jteam.bonb.geo.GeoManager;
import com.world.jteam.bonb.ldrawer.ActionBarDrawerToggle;
import com.world.jteam.bonb.ldrawer.DrawerArrowDrawable;
import com.world.jteam.bonb.model.ModelGroup;
import com.world.jteam.bonb.model.ModelMarket;
import com.world.jteam.bonb.model.ModelProduct;
import com.world.jteam.bonb.model.ModelProductFull;
import com.world.jteam.bonb.model.ModelSearchProductMethod;
import com.world.jteam.bonb.paging.ProductDataSource;
import com.world.jteam.bonb.paging.ProductListAdapter;
import com.world.jteam.bonb.server.DataApi;
import com.world.jteam.bonb.server.SingletonRetrofit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MarketActivity extends AppCompatActivity implements View.OnClickListener {

    static final int PAGE_COUNT = 1;

    private final AppCompatActivity mThis = this;


    ViewPager pager;
    PagerAdapter pagerAdapter;

    //Категории
    private ModelGroup.ProductGroupsAdapter mProductGroupsAdapter;
    private LinkedHashMap<ModelGroup, LinkedHashMap> mProductGroupsCurrent; //В момент выбора
    private LinkedHashMap<ModelGroup, LinkedHashMap> mProductGroupsSelected; //Выбранный
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;


    public int market_id;
    public String market_name;

    public View page_products;
    public View page_contacts;


    //Режимы поиска по наименованию или группе
    public ModelSearchProductMethod searchMethod;

    //Переменные пагинации
    private MainThreadExecutor executor;
    private ProductListAdapter adapter;

    //Нажатие назад
    private static long back_pressed;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);

        executor = new MarketActivity.MainThreadExecutor();
        //Инициализация списка групп товаров
        initGroupMenu();

        //Инициализация страниц
        initPager();


        Intent mIntent = getIntent();
        market_id = mIntent.getIntExtra("market_id", 0);
        market_name = mIntent.getStringExtra("market_name");
        setTitle(market_name);

        //По умолчанию отображаю товары первой группы
        this.searchMethod = new ModelSearchProductMethod("", 0, market_id);

        //Формирование списка товаров
        pagingStart();

        //Подготовка навигации
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

    }


        @Override
        protected void onPostCreate(Bundle savedInstanceState) {
            super.onPostCreate(savedInstanceState);
            mDrawerToggle.syncState();
        }

        @Override
        public void onBackPressed() {
            if (back_pressed + 2000 > System.currentTimeMillis())
                super.onBackPressed();
            else
                Toast.makeText(getBaseContext(), "Нажмите еще раз для выхода",
                        Toast.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();
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
            switch (item.getItemId()) {
                //Категории
                case android.R.id.home:
                    if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                        mDrawerLayout.closeDrawer(mDrawerList);
                    } else {
                        mDrawerLayout.openDrawer(mDrawerList);
                    }
                    break;
                //Геопозиция
                case R.id.choose_geo:
                    Intent geoIntent = new Intent(this, CoverageAreaActivity.class);
                    startActivity(geoIntent);
                    break;

                case R.id.choose_markets:
                    Intent marketsIntent = new Intent(this, SearchMarketActivity.class);
                    startActivity(marketsIntent);
                    break;
            }

            return super.onOptionsItemSelected(item);
        }




        //Обработчик ввода текста в поле поиска
        private class OnKeyPress implements View.OnKeyListener {
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                //Возврат если это не нажатие
                if (event.getAction() != KeyEvent.ACTION_DOWN || (keyCode != KeyEvent.KEYCODE_BACK & keyCode != KeyEvent.KEYCODE_ENTER))
                    return false;

                EditText editText = v.findViewById(R.id.search_panel_text);
                String strName = editText.getText().toString();
                switch (keyCode) {
                    case (KeyEvent.KEYCODE_BACK):

                        if (strName.isEmpty()) {
                            //Если поле поиска пустое, то зафиксирую начало выхода
                            //onBackPressed();
                        } else {
                            //Если нажали бэк и есть текст, то очищу поле поиска
                            editText.setText("");
                            searchByName("");
                            return true;
                        }


                        break;
                    case (KeyEvent.KEYCODE_ENTER):

                        if (strName.length() > 2) {
                            searchByName(strName);
                        }
                        return true;
                }

                return false;
            }
        }

        //Показать карточку товара по ШК
        private void showProductDetailByEAN(String EAN) {

            //Запрос на сервер
            DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();
            Call<ModelProductFull> serviceCall = mDataApi.getProductFull(
                    -1,
                    EAN,
                    AppInstance.getUser().id,
                    AppInstance.getRadiusArea(),
                    AppInstance.getGeoPosition().latitude,
                    AppInstance.getGeoPosition().longitude);
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
        private void showProductDetail(ModelProductFull prod) {

            Intent intent = new Intent(this, ProductActivity.class);
            intent.putExtra("object", prod);
            startActivity(intent);

        }

        public void showErrorSearch() {
            Toast mt = Toast.makeText(this, "Ничего не найдено", Toast.LENGTH_LONG);
            mt.show();
        }

        public void showGroupList(List<ModelGroup> groupList) {
            LinearLayout resultGroup = findViewById(R.id.search_result_group);
            //Подчищу старые теги групп
            resultGroup.removeAllViews();
            for (ModelGroup strGr : groupList) {
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()));

                layoutParams.leftMargin = 4;
                layoutParams.rightMargin = 4;
                Button nButton = new Button(this, null, R.style.Widget_AppCompat_Button_Borderless);
                nButton.setHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()));
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

            if (searchMethod == null) {
                searchMethod = new ModelSearchProductMethod(name, 0, market_id);
            } else {
                searchMethod.searchText = name;
            }
            pagingStart();

            //Подгрузка списка групп
            if (!name.equals("")) {
                DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();
                Call<List<ModelGroup>> serviceCall = mDataApi.getGroupListByName(name,market_id);
                serviceCall.enqueue(new Callback<List<ModelGroup>>() {
                    @Override
                    public void onResponse(Call<List<ModelGroup>> call, Response<List<ModelGroup>> response) {
                        List<ModelGroup> ss = response.body();
                        showGroupList(ss);

                    }

                    @Override
                    public void onFailure(Call<List<ModelGroup>> call, Throwable t) {
                        showErrorSearch();
                    }
                });
            }


        }

    //Поиск товаров по группе
    private void searchByGroup(int groupId, boolean removeSearchText) {
        if (searchMethod == null) {
            searchMethod = new ModelSearchProductMethod("", groupId, market_id);
        } else {
            searchMethod.searchGroup = groupId;
        }

        if (removeSearchText) {
            searchMethod.searchText = "";
            EditText editText = this.findViewById(R.id.search_panel_text);
            editText.setText("");
        }
        //Почищу группы результата поиска
        LinearLayout resultGroup = findViewById(R.id.search_result_group);
        resultGroup.removeAllViews();

        pagingStart();
    }


    public void onClick(View v) {

        searchByGroup((int) v.getTag(), false);

    }


    //Основной обработчик заполнения списка товаров
    private void pagingStart() {
        setupRecyclerView();
        setupDataSource(searchMethod);
    }

    //Инициализация объекта
    private void setupRecyclerView() {

        adapter = new ProductListAdapter();

        RecyclerView recyclerView = page_products.findViewById(R.id.productRW);
        recyclerView.setLayoutManager(new LinearLayoutManager(MarketActivity.this));
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


    //Адаптер страниц
    private class MarketFragmentPagerAdapter extends FragmentPagerAdapter {

        public View page1;
        public View page2;
        public MarketFragmentPagerAdapter(FragmentManager fm) {

            super(fm);
        }

        //Заголовки страниц
        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return "Товары";
                case 1:
                    return "Контактные данные";
                default:
                    return "Другое";
            }

        }

        @Override
        public Fragment getItem(int position) {
            return MarketPageFragment.newInstance(position,page1,page2);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }
    }


    private void initGroupMenu() {

        //Кнопка вызова списка категорий
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navdrawer);

        DrawerArrowDrawable drawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };
        mDrawerToggle = new ActionBarDrawerToggle((AppCompatActivity) mThis, mDrawerLayout,
                drawerArrow, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();

                mProductGroupsSelected = mProductGroupsCurrent;
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();

                if (mProductGroupsSelected == null) {
                    mProductGroupsSelected = AppInstance.getProductGroups();
                }
                if (mProductGroupsSelected != null) {
                    ArrayList productGroupsList =
                            ModelGroup.getCurrentProductGroups(mProductGroupsSelected, ModelGroup.GROUP_NM_VIEW);

                    mProductGroupsAdapter = new ModelGroup.ProductGroupsAdapter(mThis, productGroupsList);
                    mDrawerList.setAdapter(mProductGroupsAdapter);
                    mDrawerList.setOnItemClickListener(new ProductGroupsOnItemClickListener());
                    mProductGroupsCurrent = mProductGroupsSelected;

                } else {
                    Toast.makeText(mThis, R.string.product_groups_not_init, Toast.LENGTH_LONG).show();
                }

            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();


    }

    //Группы
    public class ProductGroupsOnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ModelGroup productGroup = mProductGroupsAdapter.getItem(position);
            LinkedHashMap<ModelGroup, LinkedHashMap> productGroupGroups =
                    mProductGroupsCurrent.get(productGroup);

            searchByGroup(productGroup.id, true);
            //Раскрытие группы
            if (productGroupGroups == null) {
                mProductGroupsSelected = mProductGroupsCurrent;
                mDrawerLayout.closeDrawer(mDrawerList);
                //Возврат к родителю группы
            } else {

                mProductGroupsCurrent = productGroupGroups;
                mProductGroupsAdapter.clear();
                mProductGroupsAdapter.addAll(ModelGroup.getCurrentProductGroups(
                        mProductGroupsCurrent, ModelGroup.GROUP_NM_VIEW));
            }

        }
    }

    public void initPager() {

        //Инициализирую станицы и передам их в адаптер страниц


        page_contacts = this.getLayoutInflater().inflate(R.layout.fragment_market_contacts, null);
        page_products = this.getLayoutInflater().inflate(R.layout.fragment_market_product_list, null);

        TextView view_magazinName = page_contacts.findViewById(R.id.magazinName);
        TextView view_magazinAdres = page_contacts.findViewById(R.id.magazinAdres);
        //view_magazinName.setText(mIntent.getStringExtra("market_name"));
        //view_magazinAdres.setText(mIntent.getStringExtra("market_adress"));

        //Обработчик ввода текста в строку поиска
        EditText searchText = page_products.findViewById(R.id.search_panel_text);
        searchText.setOnKeyListener(new MarketActivity.OnKeyPress());

        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new MarketFragmentPagerAdapter(getSupportFragmentManager());
        ((MarketFragmentPagerAdapter) pagerAdapter).page1 = page_products;
        ((MarketFragmentPagerAdapter) pagerAdapter).page2 = page_contacts;

        pager.setAdapter(pagerAdapter);


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
