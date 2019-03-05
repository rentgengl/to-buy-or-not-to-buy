package com.world.jteam.bonb.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.world.jteam.bonb.AppInstance;
import com.world.jteam.bonb.AuthManager;
import com.world.jteam.bonb.Constants;
import com.world.jteam.bonb.model.ModelUser;
import com.world.jteam.bonb.server.DataApi;
import com.world.jteam.bonb.R;
import com.world.jteam.bonb.server.SingletonRetrofit;
import com.world.jteam.bonb.model.ModelComment;
import com.world.jteam.bonb.model.ModelPrice;
import com.world.jteam.bonb.model.ModelProductFull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductActivity extends AppCompatActivity implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {
    private final AppCompatActivity mThis = this;

    public ModelProductFull thisProductFull;
    private SliderLayout mDemoSlider;

    private boolean mFullCommentMode = false; //Определяет будет ли обображатся полный список комментариев или упрощенный режим
    private boolean mFullPriceMode = false; //Определяет будет ли обображатся полный список цен или упрощенный режим

    RatingBar mProductRaitingView;

    private static final int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_product);

        mProductRaitingView = findViewById(R.id.productRaiting);

        thisProductFull = (ModelProductFull) getIntent().getParcelableExtra("object");
        onGetData(thisProductFull);
        final Button button = findViewById(R.id.show_price_on_map);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showPriceOnMap();
            }
        });

        //Определим действия для показа цен
        final NonScrollListView priceListView = findViewById(R.id.price_list);
        final NonScrollListView priceListViewLite = findViewById(R.id.price_list_lite);
        final Button showPriceView = findViewById(R.id.show_price);
        showPriceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFullPriceMode = !mFullPriceMode;
                if (mFullPriceMode) {
                    priceListViewLite.setVisibility(View.GONE);
                    priceListView.setVisibility(View.VISIBLE);
                    showPriceView.setText(R.string.minimize_price);
                } else {
                    priceListView.setVisibility(View.GONE);
                    priceListViewLite.setVisibility(View.VISIBLE);
                    showPriceView.setText(R.string.more_price);
                }
            }
        });

        //Определим действия для показа комментариев
        final NonScrollListView commentListView = findViewById(R.id.comment_list);
        final NonScrollListView commentListViewLite = findViewById(R.id.comment_list_lite);
        final Button showCommentView = findViewById(R.id.show_comment);
        showCommentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFullCommentMode = !mFullCommentMode;
                if (mFullCommentMode) {
                    commentListViewLite.setVisibility(View.GONE);
                    commentListView.setVisibility(View.VISIBLE);
                    showCommentView.setText(R.string.minimize_comment);
                } else {
                    commentListView.setVisibility(View.GONE);
                    commentListViewLite.setVisibility(View.VISIBLE);
                    showCommentView.setText(R.string.more_comment);
                }
            }
        });

        //Обработчик рейтинга
        mProductRaitingView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!mProductRaitingView.isIndicator()) {
                    switch (event.getAction()) {
                        case 0: //Нажали
                            mProductRaitingView.setRating(0);
                            mProductRaitingView.setStepSize((float) 1);
                            break;
                        case 1: //Отжали
                            break;
                    }
                }

                return false;
            }
        });
        mProductRaitingView.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser) {
                    mProductRaitingView.setStepSize((float) 0.1);
                    if (AppInstance.getUser().isAuthUser()) {
                        startInputComment(rating);
                    }
                    else {
                        mProductRaitingView.setIsIndicator(true);
                        AuthManager.signIn(mThis, RC_SIGN_IN);
                    }
                }
            }

        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            AuthManager.signInOnResult(resultCode, data, new AuthManager.OnLoginListener() {
                @Override
                public void onLogin() {
                    final float rating = mProductRaitingView.getRating();
                    refreshData(new RefreshDataListener() {
                        @Override
                        public void onAfterResponse() {
                            if (thisProductFull.user_leave_comment==0)
                                startInputComment(rating);
                        }
                    });
                }

                @Override
                public void onFailureLogin() {
                    AuthManager.informLoginError();
                    if (!AppInstance.getUser().isAuthUser())
                        mProductRaitingView.setIsIndicator(false);
                }
            });
        }
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

        Intent intent = new Intent(this, ProductPriceMapActivity.class);
        intent.putExtra("name", arrName);
        intent.putExtra("lat", arrLat);
        intent.putExtra("lng", arrLng);
        intent.putExtra("price", arrPrice);
        startActivity(intent);

    }

    public void onGetData(ModelProductFull product) {

        thisProductFull = product;
        mDemoSlider = (SliderLayout) findViewById(R.id.slider);

        if (product.images_links != null)

            if (product.images_links.size() > 0) {

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
        //TextView view_productName = this.findViewById(R.id.productName);//Наименование
        TextView view_lowPrice = this.findViewById(R.id.lowPrice);//Разброс цен
        TextView view_textRaiting = this.findViewById(R.id.textRaiting);//Рейтинг текстом

        ListView view_price_list = this.findViewById(R.id.price_list);
        ListView view_price_list_lite = this.findViewById(R.id.price_list_lite);
        ListView view_comment_list = this.findViewById(R.id.comment_list);
        ListView view_comment_list_lite = this.findViewById(R.id.comment_list_lite);

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
//        view_productName.setText(product.name);
//        view_productName.setTag(product.id);
        setTitle(product.name);
        view_midPrice.setText(product.price + "\u20BD");

        view_lowPrice.setText("от " + product.price_min + " до " + product.price_max + "\u20BD");
        view_textRaiting.setText(product.raiting + " из 5");
        mProductRaitingView.setRating(product.raiting);
        if (product.user_leave_comment==1)
            mProductRaitingView.setIsIndicator(true);

        //Вывод списков
        if (product.prices != null && product.prices.size()>0) {
            findViewById(R.id.show_price).setVisibility(View.VISIBLE);

            view_price_list.setAdapter(new PriceListAdapter(this, product.prices,true));

            //Для лайт списка возьмем несколько значений
            int maxSizeLite = product.prices.size()>=3 ? 3 : product.prices.size();
            ArrayList<ModelPrice> productPriceLite = new ArrayList<>();
            for (int i=0;i<=maxSizeLite-1;i++)
                productPriceLite.add(product.prices.get(i));

            view_price_list_lite.setAdapter(new PriceListAdapter(this, productPriceLite,false));
        } else{
            findViewById(R.id.show_price).setVisibility(View.GONE);
        }
        if (product.comments != null && product.comments.size()>0) {
            findViewById(R.id.show_comment).setVisibility(View.VISIBLE);

            view_comment_list.setAdapter(new CommentListAdapter(this, product.comments,true));

            //Для лайт списка возьмем несколько значений
            int maxSizeLite = product.comments.size()>=3 ? 3 : product.comments.size();
            ArrayList<ModelComment> productCommentsLite = new ArrayList<>();
            for (int i=0;i<=maxSizeLite-1;i++)
                productCommentsLite.add(product.comments.get(i));

            view_comment_list_lite.setAdapter(new CommentListAdapter(this, productCommentsLite,false));

        } else {
            findViewById(R.id.show_comment).setVisibility(View.GONE);
        }

    }

    private interface RefreshDataListener{
        void onAfterResponse();
    }

    private void refreshData(final RefreshDataListener refreshDataListener){
        DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();
        Call<ModelProductFull> serviceCall = mDataApi.getProductFull(
                thisProductFull.id,
                "",
                AppInstance.getUser().id,
                AppInstance.getRadiusArea(),
                AppInstance.getGeoPosition().latitude,
                AppInstance.getGeoPosition().longitude);
        serviceCall.enqueue(new Callback<ModelProductFull>() {
            @Override
            public void onResponse(Call<ModelProductFull> call, Response<ModelProductFull> response) {
                onGetData(response.body());
                refreshDataListener.onAfterResponse();
            }

            @Override
            public void onFailure(Call<ModelProductFull> call, Throwable t) {

            }
        });
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
        boolean fullMode;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");

        CommentListAdapter(Context context, ArrayList<ModelComment> comments, boolean fullMode) {
            this.ctx = context;
            this.objects = comments;
            this.fullMode = fullMode;
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
                if (fullMode)
                    view = lInflater.inflate(R.layout.fragment_list_comment, parent, false);
                else
                    view = lInflater.inflate(R.layout.fragment_list_comment_lite, parent, false);
            }

            ModelComment obj = getObj(position);

            TextView view_user_comment = view.findViewById(R.id.user_comment);
            RatingBar view_user_raiting = view.findViewById(R.id.user_raiting);

            view_user_comment.setText(obj.comment);
            view_user_raiting.setRating(obj.raiting);

            if (fullMode) {
                TextView view_user_name = view.findViewById(R.id.user_name);
                TextView view_user_raiting_text = view.findViewById(R.id.user_raiting_text);
                TextView view_user_raiting_date = view.findViewById(R.id.user_raiting_date);
                if (obj.user != null)
                    view_user_name.setText(obj.user.name);
                view_user_raiting_text.setText(obj.raiting + " из 5");
                view_user_raiting_date.setText(dateFormat.format(obj.date));
            }

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
        boolean fullMode;

        PriceListAdapter(Context context, ArrayList<ModelPrice> products,  boolean fullMode) {
            this.ctx = context;
            this.objects = products;
            this.fullMode =  fullMode;
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
                if (fullMode)
                    view = lInflater.inflate(R.layout.fragment_list_price, parent, false);
                else
                    view = lInflater.inflate(R.layout.fragment_list_price_lite, parent, false);
            }

            ModelPrice obj = getObj(position);

            TextView view_magazinName = view.findViewById(R.id.magazinName);
            TextView view_magazinPrice = view.findViewById(R.id.magazinPrice);
            ImageView view_imageLogo = view.findViewById(R.id.imageLogo);
            //Button view_addPrice = view.findViewById(R.id.addPrice);


            view_magazinName.setText(obj.market.name);
            view_magazinPrice.setText(obj.price + "\u20BD");
            /*view_addPrice.setTag(obj.market.id);
            view_addPrice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startInputPrice((int) v.getTag());
                }
            });*/

            if (obj.market.logo_link != null) {
                Picasso.with(ctx)
                        .load(Constants.SERVICE_GET_IMAGE + obj.market.logo_link)
                        .placeholder(R.drawable.ic_action_noimage)
                        .error(R.drawable.ic_action_noimage)
                        .into(view_imageLogo);
            }

            if (fullMode){
                TextView view_magazinAdres = view.findViewById(R.id.magazinAdres);
                view_magazinAdres.setText(obj.market.adress);

                //Дата цены
                TextView view_magazinDate = view.findViewById(R.id.magazinDate);
                SimpleDateFormat simpleDate =  new SimpleDateFormat("dd.MM.yy");
                view_magazinDate.setText(simpleDate.format(obj.date));
            }

            return view;
        }

        ModelPrice getObj(int position) {
            return ((ModelPrice) getItem(position));
        }

    }

    //Запускает диалог ввода новой цены
    public void startInputPrice(final int market_id) {

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
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                int newPR = Integer.valueOf(userInput.getText().toString());
                                addNewPrice(newPR, market_id);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();


    }

    public void addNewPrice(int price, int market) {

        ModelPrice mprice = new ModelPrice(thisProductFull.id, price, market, AppInstance.getUser().id);
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

    //Запускает диалог ввода отзыва и рейтнга
    public void startInputComment(float rating) {

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View commentView = li.inflate(R.layout.activity_dialog_new_comment, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(commentView);

        final RatingBar productRaitingNewView = commentView.findViewById(R.id.productRaiting);
        final EditText productCommentNewView = commentView.findViewById(R.id.productComment);

        productRaitingNewView.setRating(rating);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                addNewComment(productRaitingNewView.getRating(), productCommentNewView.getText().toString());
                                mProductRaitingView.setIsIndicator(true);
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mProductRaitingView.setRating(thisProductFull.raiting);
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        productCommentNewView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        // show it
        alertDialog.show();
    }

    public void addNewComment(float rating, String comment){
        ModelComment mcomment = new ModelComment(thisProductFull.id, AppInstance.getUser(), comment, rating);
        DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();
        Call<Void> serviceCall = mDataApi.addNewComment(mcomment);
        serviceCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                refreshData(new RefreshDataListener() {
                    @Override
                    public void onAfterResponse() {

                    }
                });
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }
}
