package com.world.jteam.bonb;

import android.app.Activity;
import android.arch.paging.PagedList;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.world.denacid.media.BarcodeActivity;

import java.util.List;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewProductList extends Activity implements View.OnClickListener {

    //Идентификатор результата сканирования ШК
    private static final int BARCODE_REQUEST = 1;
    //Режимы поиска по наименованию или группе
    public ModelSearchProductMethod searchMethod;

    //Переменные пагинации
    private MainThreadExecutor executor;
    private ProductListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        //Обработчик клика по кнопке сканирования
        ImageButton searchButton = this.findViewById(R.id.search_panel_button);
        searchButton.setOnClickListener(this);

        //Обработчик ввода текста в строку поиска
        EditText searchText = this.findViewById(R.id.search_panel_text);
        searchText.setOnKeyListener(new OnKeyPress());
        executor = new MainThreadExecutor();

        //По умолчанию отображаю товары первой группы
        this.searchMethod = new ModelSearchProductMethod(1);

        //Формирование списка товаров
        pagingStart();

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

    private class OnKeyPress implements View.OnKeyListener {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
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
        recyclerView.setLayoutManager(new LinearLayoutManager(ViewProductList.this));
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


