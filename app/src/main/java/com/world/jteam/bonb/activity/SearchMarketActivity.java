package com.world.jteam.bonb.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.world.jteam.bonb.AppInstance;
import com.world.jteam.bonb.Constants;
import com.world.jteam.bonb.R;
import com.world.jteam.bonb.geo.GeoManager;
import com.world.jteam.bonb.model.ModelMarket;
import com.world.jteam.bonb.server.DataApi;
import com.world.jteam.bonb.server.SingletonRetrofit;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchMarketActivity extends AppCompatActivity implements View.OnClickListener {

    private final AppCompatActivity mThis = this;
    private MarketListAdapter mMarketListAdapter;

    //Геолокация
    private int mCurrentRadiusArea=0;
    private double mCurrentLat;
    private double mCurrentLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_market);

        //Обработчик клика по кнопке выбоа на карте
        ImageButton searchButton = this.findViewById(R.id.search_market_panel_button);
        searchButton.setOnClickListener(this);

        //Обработчик ввода текста в строку поиска
        EditText searchText = this.findViewById(R.id.search_market_panel_text);
        searchText.setOnKeyListener(new SearchMarketActivity.OnKeyPress());

        searchByName("");

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
            searchByName("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_markets, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Настройки
            case R.id.choose_markets_setting:
                Intent geoIntent = new Intent(this, CoverageAreaActivity.class);
                startActivity(geoIntent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v) {

        switch (v.getId()) {

            case (R.id.search_market_panel_button):

                ArrayList<ModelMarket> markets = new ArrayList<>();

                for (ModelMarket market : mMarketListAdapter.objects) {
                    markets.add(market);
                }

                Intent intent = new Intent(this, MarketMapActivity.class);
                intent.putParcelableArrayListExtra("markets", markets);
                startActivity(intent);

        }

    }

    //Обработчик ввода текста в поле поиска
    private class OnKeyPress implements View.OnKeyListener {
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            //Возврат если это не нажатие
            if (event.getAction() != KeyEvent.ACTION_UP || (keyCode != KeyEvent.KEYCODE_BACK & keyCode != KeyEvent.KEYCODE_ENTER))
                return false;

            EditText editText = v.findViewById(R.id.search_market_panel_text);
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

    //Поиск товаров по имени
    private void searchByName(String name) {

        //Подгрузка списка групп

        DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();
        Call<List<ModelMarket>> serviceCall = mDataApi.getMarketList(name,
                AppInstance.getRadiusArea(),
                AppInstance.getGeoPosition().latitude,
                AppInstance.getGeoPosition().longitude);
        SingletonRetrofit.enqueue(serviceCall,new Callback<List<ModelMarket>>() {
            @Override
            public void onResponse(Call<List<ModelMarket>> call, Response<List<ModelMarket>> response) {
                showMarketList(response.body());
            }

            @Override
            public void onFailure(Call<List<ModelMarket>> call, Throwable t) {

            }
        });


    }

    public void showMarketList(List<ModelMarket> markets) {
        ListView view_market_list = this.findViewById(R.id.market_list);
        mMarketListAdapter = new MarketListAdapter(this, markets);
        view_market_list.setAdapter(mMarketListAdapter);
        view_market_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openMarket((ModelMarket) mMarketListAdapter.getItem(position));
            }
        });
    }

    private class MarketListAdapter extends BaseAdapter {
        Context ctx;
        LayoutInflater lInflater;
        List<ModelMarket> objects;

        MarketListAdapter(Context context, List<ModelMarket> listData) {
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
                view = lInflater.inflate(R.layout.fragment_list_market, parent, false);
            }

            ModelMarket obj = getObj(position);

            TextView view_magazinName = view.findViewById(R.id.magazinName);
            TextView view_magazinDistance = view.findViewById(R.id.magazinDistance);
            TextView view_magazinAdres = view.findViewById(R.id.magazinAdres);

            ImageView view_imageLogo = view.findViewById(R.id.imageLogo);

            view_magazinAdres.setText(obj.adress);
            view_magazinName.setText(obj.name);
            double distance = GeoManager.getDistance(obj.latitude, obj.longitude, AppInstance.getGeoPosition().latitude, AppInstance.getGeoPosition().longitude);
            view_magazinDistance.setText(String.format("%.1f", distance) + " км");

            if (obj.logo_link != null) {
                Picasso.with(ctx)
                        .load(Constants.SERVICE_GET_IMAGE + obj.logo_link)
                        .placeholder(R.drawable.ic_action_noimage)
                        .error(R.drawable.ic_action_noimage)
                        .into(view_imageLogo);
            }

            return view;
        }

        ModelMarket getObj(int position) {
            return ((ModelMarket) getItem(position));
        }

    }

    private void openMarket(ModelMarket market){

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("market_id", market.id);
        intent.putExtra("market_group_id", market.market_group_id);
        intent.putExtra("market_name", market.name);
        intent.putExtra("market_logo", market.logo_link);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);

    }




}
