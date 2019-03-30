package com.world.jteam.bonb.activity;

import android.Manifest;
import android.arch.paging.PagedList;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import com.world.jteam.bonb.Constants;
import com.world.jteam.bonb.R;
import com.world.jteam.bonb.geo.GeoManager;
import com.world.jteam.bonb.ldrawer.ActionBarDrawerToggle;
import com.world.jteam.bonb.ldrawer.DrawerArrowDrawable;
import com.world.jteam.bonb.model.ModelGroup;
import com.world.jteam.bonb.model.ModelMarket;
import com.world.jteam.bonb.model.ModelPrice;
import com.world.jteam.bonb.model.ModelProduct;
import com.world.jteam.bonb.model.ModelProductFull;
import com.world.jteam.bonb.model.ModelSearchProductMethod;
import com.world.jteam.bonb.paging.ProductDataSource;
import com.world.jteam.bonb.paging.ProductListAdapter;
import com.world.jteam.bonb.server.DataApi;
import com.world.jteam.bonb.server.SingletonRetrofit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchMarketActivity extends AppCompatActivity implements View.OnClickListener {

    private final AppCompatActivity mThis = this;

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

    public void onClick(View v) {

        switch (v.getId()) {

            case (R.id.search_market_panel_button):
                //Открыть карту с магазинами

        }

    }

    //Обработчик ввода текста в поле поиска
    private class OnKeyPress implements View.OnKeyListener {
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            //Возврат если это не нажатие
            if (event.getAction() != KeyEvent.ACTION_DOWN || (keyCode != KeyEvent.KEYCODE_BACK & keyCode != KeyEvent.KEYCODE_ENTER))
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

                    if (strName.length() > 2) {
                        searchByName(strName);
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

        if (markets != null && markets.size() > 0) {
            ListView view_market_list = this.findViewById(R.id.market_list);
            view_market_list.setAdapter(new SearchMarketActivity.MarketListAdapter(this, markets));
        }
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
            view_magazinName.setTag(obj.id);
            double distance = GeoManager.getDistance(obj.latitude, obj.longitude, AppInstance.getGeoPosition().latitude, AppInstance.getGeoPosition().longitude);
            view_magazinDistance.setText(String.format("%.1f", distance) + " км");

            if (obj.logo_link != null) {
                Picasso.with(ctx)
                        .load(Constants.SERVICE_GET_IMAGE + obj.logo_link)
                        .placeholder(R.drawable.ic_action_noimage)
                        .error(R.drawable.ic_action_noimage)
                        .into(view_imageLogo);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    TextView view_magazinName = v.findViewById(R.id.magazinName);
                    TextView view_magazinAdres = v.findViewById(R.id.magazinAdres);
                    openMarket((int) view_magazinName.getTag());

                }
            });


            return view;
        }

        ModelMarket getObj(int position) {
            return ((ModelMarket) getItem(position));
        }

    }

    public void openMarket(int id){

        Intent intent = new Intent(this, MarketActivity.class);
                    intent.putExtra("market_id", id);
//                    intent.putExtra("market_name", (String) view_magazinName.getText());
//                    intent.putExtra("market_adress", (String) view_magazinAdres.getText());
        startActivity(intent);

    }




}
