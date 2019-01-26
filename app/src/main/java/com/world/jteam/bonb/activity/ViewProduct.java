package com.world.jteam.bonb.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.squareup.picasso.Picasso;
import com.world.jteam.bonb.Constants;
import com.world.jteam.bonb.DataApi;
import com.world.jteam.bonb.R;
import com.world.jteam.bonb.SingletonRetrofit;
import com.world.jteam.bonb.model.ModelComment;
import com.world.jteam.bonb.model.ModelPrice;
import com.world.jteam.bonb.model.ModelProductFull;
import com.world.jteam.bonb.model.ModelUser;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewProduct extends AppCompatActivity implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    public ModelProductFull thisProductFull;
    private SliderLayout mDemoSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_product);

        thisProductFull = (ModelProductFull) getIntent().getParcelableExtra("object");
        onGetData(thisProductFull);
        final Button button = findViewById(R.id.show_price_on_map);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showPriceOnMap();
            }
        });


    }

    public void showPriceOnMap() {

        String arrName[] = new String[thisProductFull.prices.size()];
        double arrLat[] = new double[thisProductFull.prices.size()];
        double arrLng[] = new double[thisProductFull.prices.size()];
        int arrPrice[] = new int[thisProductFull.prices.size()];

        int i = 0;
        for (ModelPrice mPrice : thisProductFull.prices) {
            arrName[i] = mPrice.market.name;
            arrLat[i] = mPrice.market.latitude;
            arrLng[i] = mPrice.market.longitude;
            arrPrice[i] = mPrice.price;
            i++;

        }

        Intent intent = new Intent(this, ViewProductPriceMap.class);
        intent.putExtra("name", arrName);
        intent.putExtra("lat", arrLat);
        intent.putExtra("lng", arrLng);
        intent.putExtra("price", arrPrice);
        startActivity(intent);

    }

    public void onGetData(ModelProductFull product) {

        thisProductFull = product;
        mDemoSlider = (SliderLayout) findViewById(R.id.slider);

        if (product.images_links != null )

            if(product.images_links.size() > 0) {

            for (String link : product.images_links) {
                TextSliderView textSliderView = new TextSliderView(this);
                // initialize a SliderLayout
                textSliderView
                        .image(Constants.SERVICE_GET_IMAGE + link)
                        .setOnSliderClickListener(this)
                        .setScaleType(BaseSliderView.ScaleType.CenterInside);

                //Данные для передачи при клике
                textSliderView.bundle(new Bundle());
                textSliderView.getBundle()
                        .putString("extra", link);

                mDemoSlider.addSlider(textSliderView);
            }


        } else {

            TextSliderView textSliderView = new TextSliderView(this);
            textSliderView
                    .image(R.drawable.ic_action_noimage)
                    .setScaleType(BaseSliderView.ScaleType.CenterInside);

            mDemoSlider.addSlider(textSliderView);

        }

        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Default);
        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        mDemoSlider.stopAutoCycle();
        mDemoSlider.addOnPageChangeListener(this);


        //Накину данные продукта на форму
        TextView view_midPrice = this.findViewById(R.id.midPrice);//Средняя цена
        TextView view_productName = this.findViewById(R.id.productName);//Наименование
        TextView view_lowPrice = this.findViewById(R.id.lowPrice);//Разброс цен
        TextView view_textRaiting = this.findViewById(R.id.textRaiting);//Рейтинг текстом
        RatingBar view_productRaiting = this.findViewById(R.id.productRaiting);//Рейтинг

        ListView view_price_list = this.findViewById(R.id.price_list);
        ListView view_comment_list = this.findViewById(R.id.comment_list);

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        view_productName.setText(product.name);
        view_productName.setTag(product.id);
        view_midPrice.setText(product.price + "\u20BD");

        view_lowPrice.setText("от " + product.price_min + " до " + product.price_max + "\u20BD");
        view_textRaiting.setText(product.raiting + " из 5");
        view_productRaiting.setRating(product.raiting);

        //Вывод списков
        if (product.prices != null) {
            PriceListAdapter priceAdapter = new PriceListAdapter(this, product.prices);
            view_price_list.setAdapter(priceAdapter);
        }
        if (product.comments != null) {
            CommentListAdapter commentAdapter = new CommentListAdapter(this, product.comments);
            view_comment_list.setAdapter(commentAdapter);
        }

    }

    // Обработчики слайдера картинок
    @Override
    protected void onStop() {
        // To prevent a memory leak on rotation, make sure to call stopAutoCycle() on the slider before activity or fragment is destroyed
        mDemoSlider.stopAutoCycle();
        super.onStop();
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {
        Toast.makeText(this, slider.getBundle().get("extra") + "", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        Log.d("Slider Demo", "Page Changed: " + position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    //Адаптеры списков
    private class CommentListAdapter extends BaseAdapter {
        Context ctx;
        LayoutInflater lInflater;
        ArrayList<ModelComment> objects;

        CommentListAdapter(Context context, ArrayList<ModelComment> comments) {
            ctx = context;
            objects = comments;
            lInflater = (LayoutInflater) ctx
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
                view = lInflater.inflate(R.layout.fragment_list_comment, parent, false);
            }

            ModelComment obj = getObj(position);

            TextView view_user_comment = view.findViewById(R.id.user_comment);
            TextView view_user_name = view.findViewById(R.id.user_name);
            TextView view_user_raiting_text = view.findViewById(R.id.user_raiting_text);
            RatingBar view_user_raiting = view.findViewById(R.id.user_raiting);

            view_user_comment.setText(obj.comment);
            if (obj.user != null)
                view_user_name.setText(obj.user.name);
            view_user_raiting_text.setText(obj.raiting + " из 5");
            view_user_raiting.setRating(obj.raiting);

            return view;
        }

        ModelComment getObj(int position) {
            return ((ModelComment) getItem(position));
        }
    }

    private class PriceListAdapter extends BaseAdapter {
        Context ctx;
        LayoutInflater lInflater;
        ArrayList<ModelPrice> objects;

        PriceListAdapter(Context context, ArrayList<ModelPrice> products) {
            ctx = context;
            objects = products;
            lInflater = (LayoutInflater) ctx
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
                view = lInflater.inflate(R.layout.fragment_list_price, parent, false);
            }

            ModelPrice obj = getObj(position);

            TextView view_magazinName = view.findViewById(R.id.magazinName);
            TextView view_magazinAdres = view.findViewById(R.id.magazinAdres);
            TextView view_magazinPrice = view.findViewById(R.id.magazinPrice);
            ImageView view_imageLogo = view.findViewById(R.id.imageLogo);
            Button view_addPrice = view.findViewById(R.id.addPrice);

            view_magazinName.setText(obj.market.name);
            view_magazinAdres.setText(obj.market.adress);
            view_magazinPrice.setText(obj.price + "\u20BD");
            view_addPrice.setTag(obj.market.id);
            view_addPrice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startInputPrice((int) v.getTag());
                }
            });

            if (obj.market.logo_link != null) {
                Picasso.with(ctx)
                        .load(Constants.SERVICE_GET_IMAGE + obj.market.logo_link)
                        .placeholder(R.drawable.ic_action_noimage)
                        .error(R.drawable.ic_action_noimage)
                        .into(view_imageLogo);
            }

            return view;
        }

        ModelPrice getObj(int position) {
            return ((ModelPrice) getItem(position));
        }

    }

    //Запускает диалог ввода новой цены
    public void startInputPrice(final int market_id){

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.activity_dialog_new_price, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.newPriceEdit);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
                                int newPR = Integer.valueOf(userInput.getText().toString());
                                addNewPrice(newPR ,market_id);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();


    }


    public void addNewPrice(int price,int market){

        ModelPrice mprice = new ModelPrice(thisProductFull.id,price,market,1 );
        DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();
        Call<Void> serviceCall = mDataApi.addNewPrice(mprice);
        serviceCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                //Что за дичь
            }
        });
    }
}
