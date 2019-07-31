package com.world.jteam.bonb.activity;

import android.Manifest;
import android.arch.paging.PagedList;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.world.jteam.bonb.AppInstance;
import com.world.jteam.bonb.AuthManager;
import com.world.jteam.bonb.geo.GeoManager;
import com.world.jteam.bonb.ldrawer.ActionBarDrawerToggle;
import com.world.jteam.bonb.ldrawer.DrawerArrowDrawable;
import com.world.jteam.bonb.Constants;
import com.world.jteam.bonb.model.ModelContact;
import com.world.jteam.bonb.model.ModelMarket;
import com.world.jteam.bonb.model.ModelSearchResult;
import com.world.jteam.bonb.paging.ProductListAdapterStatic;
import com.world.jteam.bonb.server.DataApi;
import com.world.jteam.bonb.R;
import com.world.jteam.bonb.server.SingletonRetrofit;
import com.world.jteam.bonb.model.ModelGroup;
import com.world.jteam.bonb.model.ModelProduct;
import com.world.jteam.bonb.model.ModelProductFull;
import com.world.jteam.bonb.model.ModelSearchProductMethod;
import com.world.jteam.bonb.paging.ProductDataSource;
import com.world.jteam.bonb.paging.ProductListAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //region Переменные
    private final AppCompatActivity mThis = this;
    private Menu mMenu;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    //Страницы
    static final int PAGE_COUNT = 2;
    private ViewPager pager;
    private PagerAdapter pagerAdapter;
    public int market_id;
    public String market_name;
    public String market_logo;
    public View page_products;
    public View page_contacts;


    //Категории
    private int[] mMarketsProductsGroup;
    private ModelGroup.ProductGroupsAdapter mProductGroupsAdapter;
    private LinkedHashMap<ModelGroup, LinkedHashMap> mProductGroupsCurrent; //В момент выбора
    private LinkedHashMap<ModelGroup, LinkedHashMap> mProductGroupsSelected; //Выбранный
    ModelGroup mProductGroupShopping;

    //Идентификатор результата сканирования ШК
    private static final int BARCODE_REQUEST = 1;
    //Режимы поиска по наименованию или группе
    public ModelSearchProductMethod mSearchMethod;

    //Переменные пагинации
    private MainActivity.MainThreadExecutor executor;
    private ProductListAdapter mAdapter;
    private ProductListAdapterStatic mAdapterStatic;

    //Геолокация
    private int mCurrentRadiusArea=0;
    private double mCurrentLat;
    private double mCurrentLng;
    private static final int GEO_REQUEST = 2;

    //Авторизация
    private static final int SIGN_IN_SHOPPING_REQUEST = 3;

    //Нажатие назад
    private static long back_pressed;

    //Контакты
    private ContactsListAdapter mContactsListAdapter;

    //endregion

    //region События активити
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent mIntent = getIntent();
        market_id = mIntent.getIntExtra("market_id", 0);
        int market_group_id = mIntent.getIntExtra("market_group_id", 0);
        market_name = mIntent.getStringExtra("market_name");
        market_logo = mIntent.getStringExtra("market_logo");

        //По умолчанию отображаю товары первой группы
        this.mSearchMethod = new ModelSearchProductMethod("", Constants.SALE_GROUP_ID, market_id);

        //Инициализирую станицы для адаптера страниц
        page_contacts = this.getLayoutInflater().inflate(R.layout.fragment_market_contacts, null);
        page_products = this.getLayoutInflater().inflate(R.layout.fragment_market_product_list, null);

        //Обработчик ввода текста в строку поиска
        EditText searchText = page_products.findViewById(R.id.search_panel_text);
        searchText.setOnKeyListener(new MainActivity.OnKeyPress());

        //Обработчик клика по кнопке сканирования
        ImageButton searchButton = page_products.findViewById(R.id.search_panel_button);
        searchButton.setOnClickListener(this);

        //Инициализация списка категорий
        initGroupMenu();

        //Инициализация страниц
        initPager();

        //Это карточка магазина
        if (market_id != 0) {
            setTitle(market_name);

            //Отобразим группы только те, в которых есть товары выбранного магазина
            ModelGroup.getMarketsProductsGroup(market_group_id, new ModelGroup.MarketsProductsGroupListener() {
                @Override
                public void onAfterResponse(int[] marketsProductsGroup) {
                    mMarketsProductsGroup=marketsProductsGroup;
                }
            });

            //Получим и отобразим список контактактных данных магазина
            getContactsListByMarketId(market_id);
            ImageView view_imageLogo = page_contacts.findViewById(R.id.imageLogo);


            if (market_logo != null) {
                Picasso.with(this)
                        .load(Constants.SERVICE_GET_IMAGE + market_logo)
                        .placeholder(R.drawable.ic_action_noimage)
                        .error(R.drawable.ic_action_noimage)
                        .into(view_imageLogo);
            }

        }

        //Подготовка навигации
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        executor = new MainActivity.MainThreadExecutor();

        //Формирование списка товаров
        pagingStart();

        //Геолокация
        int rc = ActivityCompat.checkSelfPermission(mThis, Manifest.permission.ACCESS_FINE_LOCATION);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            GeoManager.starGeoPositionTrace();
        } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(mThis, Manifest.permission.ACCESS_FINE_LOCATION)) {
                final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                ActivityCompat.requestPermissions(mThis, permissions, GEO_REQUEST);
            } else {
                AppInstance.setAutoGeoPosition(false);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mCurrentRadiusArea=AppInstance.getRadiusArea();
        mCurrentLat=AppInstance.getGeoPosition().latitude;
        mCurrentLng=AppInstance.getGeoPosition().longitude;
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Обновление при изменении геолокации
        if (        mCurrentRadiusArea!=0
                &&
                    (mCurrentRadiusArea!=AppInstance.getRadiusArea()
                    || mCurrentLat!=AppInstance.getGeoPosition().latitude
                    || mCurrentLng!=AppInstance.getGeoPosition().longitude
                    )
                ){
            pagingStart();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case BARCODE_REQUEST:
                //Обрабочик скана ШК
                if (data != null) {
                    String barcodeResult = data.getStringExtra(getApplicationContext().getPackageName() + ".barcode");
                    if (resultCode == RESULT_OK) {
                        //Попробую открыть карточку товара по ШК
                        showProductDetailByEAN(barcodeResult);
                    } else {
                        //Ошибка сканирования ШК
                    }
                }
                break;
            case SIGN_IN_SHOPPING_REQUEST:
                //Обработчик авторизации
                AuthManager.signInOnResult(resultCode, data, new AuthManager.OnLoginListener() {
                    @Override
                    public void onLogin() {
                        showProductGroup(mProductGroupShopping);
                    }

                    @Override
                    public void onFailureLogin() {
                    }
                });

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case GEO_REQUEST:
                //Обработка права доступа на геолокацию
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    GeoManager.starGeoPositionTrace();
                } else {
                    AppInstance.setAutoGeoPosition(false);
                }
                break;

        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    //endregion

    //region Заполнение и обработка меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if(market_id!=0)
            menu.findItem(R.id.choose_markets).setIcon(R.drawable.ic_clear);
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
                if(market_id!=0){
                    clearMarketId();
                } else {
                    Intent marketsIntent = new Intent(this, SearchMarketActivity.class);
                    startActivity(marketsIntent);
                }

                break;
        }

        return super.onOptionsItemSelected(item);
    }
    //endregion

    //region Группы товаров
    private class ProductGroupsOnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ModelGroup productGroup = mProductGroupsAdapter.getItem(position);

            //Список покупок и не пользователь не авторизован
            if (productGroup.id==Constants.SHOPPINGLIST_GROUP_ID && !AppInstance.getUser().isAuthUser()){
                mProductGroupShopping=productGroup;
                AuthManager.signIn(mThis, SIGN_IN_SHOPPING_REQUEST);
            } else {
                showProductGroup(productGroup);
            }
        }
    }

    private void showProductGroup(ModelGroup productGroup){
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
                    mProductGroupsCurrent, ModelGroup.GROUP_NM_VIEW,mMarketsProductsGroup));
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
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
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
                    ArrayList productGroupsList = ModelGroup.getCurrentProductGroups(
                            mProductGroupsSelected, ModelGroup.GROUP_NM_VIEW,mMarketsProductsGroup);

                    mProductGroupsAdapter = new ModelGroup.ProductGroupsAdapter(mThis, productGroupsList);
                    mDrawerList.setAdapter(mProductGroupsAdapter);
                    mDrawerList.setOnItemClickListener(new ProductGroupsOnItemClickListener());
                    mProductGroupsCurrent = mProductGroupsSelected;

                    //Получим количество списка товаров
                    final ModelGroup slg = AppInstance.getShoppingListGroup();
                    if (slg.count==null && AppInstance.getUser().isAuthUser()) {
                        DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();
                        Call<Integer> serviceCall = mDataApi.getShoppingListCount(AppInstance.getUser().id);
                        SingletonRetrofit.enqueue(serviceCall,new Callback<Integer>() {
                            @Override
                            public void onResponse(Call<Integer> call, Response<Integer> response) {
                                Integer resultData = response.body();
                                slg.count=resultData.intValue();
                                mProductGroupsAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onFailure(Call<Integer> call, Throwable t) {
                                AppInstance.errorLog("HTTP getShoppingListCount", t.toString());
                            }
                        });
                    }

                } else {
                    Toast.makeText(mThis, R.string.product_groups_not_init, Toast.LENGTH_LONG).show();
                }

            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }
    //endregion

    //region Пагинация
    //Поиск товаров по имени
    private void searchByName(String name) {

        if (mSearchMethod == null) {
            mSearchMethod = new ModelSearchProductMethod(name, 0, market_id);
        } else {
            mSearchMethod.searchText = name;
        }
        pagingStart();

        //Подгрузка списка групп
        if (!name.equals("")) {
            DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();
            Call<List<ModelGroup>> serviceCall = mDataApi.getGroupListByName(name, market_id);
            SingletonRetrofit.enqueue(serviceCall, new Callback<List<ModelGroup>>() {
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
        } else {
            LinearLayout resultGroup = findViewById(R.id.search_result_group);
            resultGroup.removeAllViews();
        }


    }

    //Поиск товаров по группе
    private void searchByGroup(int groupId, boolean removeSearchText) {
        if (mSearchMethod == null) {
            mSearchMethod = new ModelSearchProductMethod(groupId);
            mSearchMethod.market_id = market_id;
        } else {
            mSearchMethod.searchGroup = groupId;
        }

        if (removeSearchText) {
            mSearchMethod.searchText = "";
            EditText editText = this.findViewById(R.id.search_panel_text);
            editText.setText("");
        }
        //Почищу группы результата поиска
        LinearLayout resultGroup = findViewById(R.id.search_result_group);
        resultGroup.removeAllViews();

        pagingStart();
    }

    //Основной обработчик заполнения списка товаров
    private void pagingStart() {
        setupRecyclerView();
        setupDataSource();
    }

    //Инициализация объекта
    private void setupRecyclerView() {
        final RecyclerView recyclerView = page_products.findViewById(R.id.productRW);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setHasFixedSize(true);

        if (mSearchMethod.searchGroup==Constants.SHOPPINGLIST_GROUP_ID){
            mAdapterStatic=new ProductListAdapterStatic(R.id.swipeLayout, mSearchMethod);
            mAdapter=null;
            recyclerView.setAdapter(mAdapterStatic);
        } else {
            mAdapter = new ProductListAdapter(R.id.swipeLayout, mSearchMethod);
            mAdapterStatic=null;
            recyclerView.setAdapter(mAdapter);
        }

        recyclerView.setOnClickListener(this);
        recyclerView.setOnFlingListener(new RecyclerView.OnFlingListener() { //порог броска прогрутки
            @Override
            public boolean onFling(int velocityX, int velocityY) {
                if (velocityY > Constants.MAX_PRODUCT_LIST_FLING_Y) {
                    recyclerView.fling(velocityX, Constants.MAX_PRODUCT_LIST_FLING_Y);
                    return true;
                }

                return false;
            }
        });

    }

    //Инициализация источника данных
    private void setupDataSource() {
        if (mSearchMethod.searchGroup==Constants.SHOPPINGLIST_GROUP_ID){
            DataApi dataApi = SingletonRetrofit.getInstance().getDataApi();
            Call<ModelSearchResult> retrofitCall = dataApi.getProductList(
                    mSearchMethod.searchText,
                    mSearchMethod.searchGroup,
                    AppInstance.getRadiusArea(),
                    AppInstance.getGeoPosition().latitude,
                    AppInstance.getGeoPosition().longitude,
                    5000,
                    "",
                    -1,
                    mSearchMethod.market_id,
                    AppInstance.getUser().id
            );
            SingletonRetrofit.enqueue(retrofitCall,new Callback<ModelSearchResult>() {
                @Override
                public void onResponse(Call<ModelSearchResult> call, Response<ModelSearchResult> response) {
                    ModelSearchResult resultData = response.body();
                    List<ModelProduct> products = resultData.getProducts();
                    mAdapterStatic.setProductsList(products);

                    //Заполним количество у списка покупок
                    ModelGroup slg = AppInstance.getShoppingListGroup();
                    slg.count = 0;

                    for (ModelProduct product : products) {
                        if (product.purchased == 0) slg.count++;
                    }
                }

                @Override
                public void onFailure(Call<ModelSearchResult> call, Throwable t) {
                    AppInstance.errorLog("HTTP getProductList", t.toString());
                }
            });
        } else {
            // Подготовка источника данных
            ProductDataSource dataSource = new ProductDataSource();
            dataSource.searchMethod = mSearchMethod;

            PagedList.Config config = new PagedList.Config.Builder()
                    .setPageSize(Constants.DEFAULT_PER_PAGE)// Количество записей для порции данных
                    .setInitialLoadSizeHint(Constants.DEFAULT_PER_PAGE * 2)// Количество записей для первой порции
                    .setEnablePlaceholders(true) // Показ пустых блоков пока данные не подрузятся
                    .build();

            PagedList<ModelProduct> list =
                    new PagedList.Builder<>(dataSource, config)
                            .setFetchExecutor(executor)
                            .setNotifyExecutor(executor)
                            .build();

            mAdapter.submitList(list);
        }
    }

    class MainThreadExecutor implements Executor {
        private final Handler mHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            mHandler.post(command);
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
        SingletonRetrofit.enqueue(serviceCall, new Callback<ModelProductFull>() {
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

    //Очистка отбора по маркету
    private void clearMarketId(){
        mSearchMethod.market_id = 0;
        market_id = 0;
        initPager();
        mMarketsProductsGroup=null;
        setTitle(R.string.app_name);
        mMenu.findItem(R.id.choose_markets).setIcon(R.drawable.ic_market);
        searchByName("");
    }
    //endregion

    //region Страницы
    public void initPager() {

        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new MainActivity.MarketFragmentPagerAdapter(getSupportFragmentManager());
        ((MainActivity.MarketFragmentPagerAdapter) pagerAdapter).page1 = page_products;
        ((MainActivity.MarketFragmentPagerAdapter) pagerAdapter).page2 = page_contacts;

        pager.setAdapter(pagerAdapter);

    }

    public static class MarketPageFragment extends Fragment {
        static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
        int pageNumber;
        public View page1_List = null;
        public View page2_Info = null;

        static MarketPageFragment newInstance(int page,View page1, View page2) {
            MarketPageFragment pageFragment = new MarketPageFragment();

            pageFragment.page1_List = page1;
            pageFragment.page2_Info = page2;
            Bundle arguments = new Bundle();
            arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
            pageFragment.setArguments(arguments);
            return pageFragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            switch (pageNumber) {
                case (1):
                    if (page2_Info == null) {
                        page2_Info = inflater.inflate(R.layout.fragment_market_contacts, null);
                    }
                    return page2_Info;
                case (2):

                    if (page1_List == null) {
                        page1_List = inflater.inflate(R.layout.fragment_market_product_list, null);
                    }
                    return page1_List;
                default:
                    if (page1_List == null) {
                        page1_List = inflater.inflate(R.layout.fragment_market_product_list, null);
                    }
                    return page1_List;
            }

        }
    }

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
                    return "Контакты";
                default:
                    return "Другое";
            }

        }

        @Override
        public Fragment getItem(int position) {
            if(market_id == 0){
                return MarketPageFragment.newInstance(0, page1, page2);
            }
            return MarketPageFragment.newInstance(position, page1, page2);
        }

        @Override
        public int getCount() {

            if(market_id == 0) {
                return 1;
            }else {

                return PAGE_COUNT;
            }
        }
    }
    //endregion

    //region Общие события нажатий
    @Override
    public void onBackPressed() {

        if(market_id!=0){
            clearMarketId();
        }else {

            if (back_pressed + 2000 > System.currentTimeMillis())
                super.onBackPressed();
            else
                Toast.makeText(getBaseContext(), "Нажмите еще раз для выхода",
                        Toast.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();
        }
    }

    private class OnKeyPress implements View.OnKeyListener {
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            //Возврат если это не нажатие
            if (event.getAction() != KeyEvent.ACTION_UP || (keyCode != KeyEvent.KEYCODE_BACK & keyCode != KeyEvent.KEYCODE_ENTER))
                return false;

            EditText editText = v.findViewById(R.id.search_panel_text);
            String strName = editText.getText().toString();
            switch (keyCode) {
                case (KeyEvent.KEYCODE_BACK):


                    if (strName.isEmpty()) {

                        if(market_id!=0){
                            clearMarketId();
                            return true;
                        }
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

                    if (strName.length() > 2 || strName.length()==0) {
                        searchByName(strName);
                        InputMethodManager imm = (InputMethodManager)getSystemService(mThis.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                    return true;
            }

            return false;
        }
    }

    public void onClick(View v) {

        if (v.getId() == R.id.search_panel_button) {
            //Получить ШК
            Intent barcodeIntent = new Intent(this, BarcodeActivity.class);
            startActivityForResult(barcodeIntent, BARCODE_REQUEST);

        } else {
            //Клик по группе
            //Подгрузка списка товаров
            int productGroupId = (int) v.getTag();

            //Спозиционируем на выбранной группе
            TreeMap<Integer, LinkedHashMap> productGroupsParent = AppInstance.getProductGroupsParent();
            mProductGroupsCurrent = productGroupsParent.get(Integer.valueOf(productGroupId));
            mProductGroupsSelected = mProductGroupsCurrent;

            //Получим товар только по выбранной группе
            searchByGroup(productGroupId, false);

        }

    }
    //endregion


    //region Контактные данные магазина

    //Получает список контактов магазина с сервера и выводит на форму
    public void getContactsListByMarketId(int id){


        DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();
        Call<List<ModelContact>> serviceCall = mDataApi.getMarketContacts(id);
        SingletonRetrofit.enqueue(serviceCall,new Callback<List<ModelContact>>() {
            @Override
            public void onResponse(Call<List<ModelContact>> call, Response<List<ModelContact>> response) {
                showMarketContacts(response.body());
            }

            @Override
            public void onFailure(Call<List<ModelContact>> call, Throwable t) {

            }
        });


    }

    //Выводит список контактов на форму
    public void showMarketContacts(List<ModelContact> contacts) {
        ListView view_contacts_list = this.findViewById(R.id.contacts_list);
        mContactsListAdapter = new MainActivity.ContactsListAdapter(this, contacts);
        view_contacts_list.setAdapter(mContactsListAdapter);

    }

    private class ContactsListAdapter extends BaseAdapter {
        Context ctx;
        LayoutInflater lInflater;
        List<ModelContact> objects;

        ContactsListAdapter(Context context, List<ModelContact> listData) {
            this.ctx = context;
            this.objects = listData;
            this.lInflater = (LayoutInflater) ctx
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        // кол-во элементов
        @Override
        public int getCount() {
            return objects.size();
        }

        // элемент по позиции
        @Override
        public Object getItem(int position) {
            return objects.get(position);
        }

        // id по позиции
        @Override
        public long getItemId(int position) {
            return position;
        }

        // пункт списка
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // используем созданные, но не используемые view
            View view = convertView;
            if (view == null) {
                view = lInflater.inflate(R.layout.fragment_list_contact, parent, false);
            }

            ModelContact obj = getObj(position);

            TextView view_contactName = view.findViewById(R.id.contactName);
            //TextView view_contactValue = view.findViewById(R.id.contactValue);
            EditText view_editValue = view.findViewById(R.id.editValue);
            ImageView view_imageLogo = view.findViewById(R.id.imageLogo);

            //view_contactValue.setText(obj.value);
            view_contactName.setText(obj.name);

            view_editValue.setText(obj.value);

            if (obj.logo_link != null) {
                Picasso.with(ctx)
                        .load(Constants.SERVICE_GET_IMAGE + obj.logo_link)
                        .placeholder(R.drawable.ic_action_noimage)
                        .error(R.drawable.ic_action_noimage)
                        .into(view_imageLogo);
            }

            return view;
        }

        ModelContact getObj(int position) {
            return ((ModelContact) getItem(position));
        }

    }

    //endregion
}
