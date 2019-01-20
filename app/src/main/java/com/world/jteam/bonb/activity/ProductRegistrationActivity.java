package com.world.jteam.bonb.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.world.jteam.bonb.media.CameraManager;
import com.world.jteam.bonb.media.ImageManager;
import com.world.jteam.bonb.model.BarcodeEditView;
import com.world.jteam.bonb.Constants;
import com.world.jteam.bonb.DatabaseApp;
import com.world.jteam.bonb.Product;
import com.world.jteam.bonb.R;
import com.world.jteam.bonb.model.ModelGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ProductRegistrationActivity extends AppCompatActivity {
    private final AppCompatActivity mThis=this;
    private ImageManager mImage;
    private static final int IMAGE_REQUEST=1;
    private static final int BARCODE_REQUEST=2;
    private static final int RC_HANDLE_CAMERA_PERM=2;
    private boolean SHOW_KEYBOARD=false;
    private ImageView image_product_view;
    private BarcodeEditView barcode_view;
    private FrameLayout product_registration_frame;

    //Категории
    private ConstraintLayout product_category_groupselect;
    private Button product_category_view;
    private Product.CategoriesAdapter mCategoriesAdapter;
    private LinkedHashMap<ModelGroup,LinkedHashMap> mProductCategoriesCurrent; //В момент выбора
    private LinkedHashMap<ModelGroup,LinkedHashMap> mProductCategoriesSelected; //Выбранный

    //Инициализация
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_registration);

        product_registration_frame=(FrameLayout) findViewById(R.id.product_registration_frame);
        product_category_view=(Button) findViewById(R.id.product_category);
        image_product_view=(ImageView) findViewById(R.id.image_product);
        barcode_view =(BarcodeEditView) findViewById(R.id.barcode);

        //Восстановим значения
        SaveContainer saveContainer=(SaveContainer) getLastCustomNonConfigurationInstance();
        if (saveContainer!=null){
            mImage=saveContainer.imageManager;
            mImage.linkContext(mThis);
            setProductImage();
            mProductCategoriesSelected=saveContainer.productCategoriesSelected;
            product_category_view.setText(saveContainer.productCategory);
        }

        //Продукт
        image_product_view.setOnClickListener(new ImageProductOnClickListener());

        //Штрихкод
        barcode_view.setOnFocusChangeListener(new BarcodeOnFocusChangeListener());

        //Категория
        product_category_view.setOnClickListener(new ProductCategoryOnClick());

        //Инициализация работы с картинками
        if (mImage==null)
            mImage=new ImageManager(mThis);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        mImage.unLinkContext();
        return new SaveContainer(
                mImage,
                mProductCategoriesSelected,
                product_category_view.getText()
        );
    }

    static class SaveContainer{
        public ImageManager imageManager;
        public LinkedHashMap<ModelGroup,LinkedHashMap> productCategoriesSelected;
        public CharSequence productCategory;

        public SaveContainer(
                ImageManager imageManager,
                LinkedHashMap<ModelGroup,LinkedHashMap> productCategoriesSelected,
                CharSequence productCategory){
            this.imageManager=imageManager;
            this.productCategoriesSelected=productCategoriesSelected;
            this.productCategory=productCategory;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(SHOW_KEYBOARD){

            View currentFocus=this.getCurrentFocus();
            currentFocus.requestFocus();
            InputMethodManager imm = (InputMethodManager)getSystemService(mThis.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

            SHOW_KEYBOARD=false;
        }
    }

    //Заполнение и обработка меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product_registration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.register_product:
                registerProduct();
                break;
        }
        return true;
    }


    //Слушатели
    private class ImageProductOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            int rc = ActivityCompat.checkSelfPermission(mThis, Manifest.permission.CAMERA);
            if (rc == PackageManager.PERMISSION_GRANTED) {
                getCameraImageToFile();
            } else {
                CameraManager.requestCameraPermission(mThis,
                        RC_HANDLE_CAMERA_PERM,
                        new CameraManager.CameraRequest() {
                            @Override
                            public void previouslyDeniedTheCameraRequest() {
                                Toast.makeText(mThis,R.string.not_camera_permission,Toast.LENGTH_LONG).show();
                            }
                        });
            }
        }
    }

    private class BarcodeOnFocusChangeListener implements View.OnFocusChangeListener{
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus){
                Intent barcodeIntent=new Intent(mThis,BarcodeActivity.class);
                startActivityForResult(barcodeIntent,BARCODE_REQUEST);
            }
        }
    }

    //Категории
    private class ProductCategoryOnClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if (mProductCategoriesSelected==null){
                mProductCategoriesSelected = Product.getProductCategories();
            }
            if (mProductCategoriesSelected==null){
                Toast.makeText(mThis,R.string.product_categories_not_init,Toast.LENGTH_LONG).show();
                return;
            }

            if (product_category_groupselect==null){
                //Выведем лайаут со списком
                LayoutInflater ltInflater = getLayoutInflater();
                ltInflater.inflate(
                        R.layout.fragment_product_category_listview,
                        product_registration_frame,
                        //product_registration_constr,
                        true);

                //Настроим список
                ListView product_category_select=(ListView) findViewById(R.id.product_category_select);
                ArrayList productCategoriesList =
                        Product.getCurrentProductCategories(mProductCategoriesSelected,Product.CAT_NM_PRODUCT_ADD);

                mCategoriesAdapter=new Product.CategoriesAdapter(mThis,productCategoriesList);
                product_category_select.setAdapter(mCategoriesAdapter);
                product_category_select.setOnItemClickListener(new ProductCategoryOnItemClickListener());

                float productCategoryPosY=product_category_view.getY()+product_category_view.getHeight();
                product_category_groupselect=(ConstraintLayout) findViewById(R.id.product_category_groupselect);
                product_category_groupselect.setY(productCategoryPosY); //Спозиционируем на кнопку
                product_category_groupselect.setX(product_category_view.getX()); //Спозиционируем на кнопку
                resizeProductCategorySelectedView(); //Подгоним список

                mProductCategoriesCurrent=mProductCategoriesSelected;

            } else {
                removeProductCategorySelectedView();
            }
        }
    }

    private void removeProductCategorySelectedView(){
        product_registration_frame.removeView(product_category_groupselect);
        product_category_groupselect=null;
    }

    private void resizeProductCategorySelectedView(){
        boolean setDefaultParam=false;
        ViewGroup.LayoutParams layoutParams= product_category_groupselect.getLayoutParams();
        if (layoutParams.height!=ViewGroup.LayoutParams.WRAP_CONTENT){
            layoutParams.height=ViewGroup.LayoutParams.WRAP_CONTENT;
            setDefaultParam=true;
        }

        int maxCategoryWidth=product_category_view.getWidth();
        if (layoutParams.width!=maxCategoryWidth){
            layoutParams.width=maxCategoryWidth;
            setDefaultParam=true;
        }

        if (setDefaultParam)
            product_category_groupselect.setLayoutParams(layoutParams);

        product_category_groupselect.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        product_category_groupselect.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        //Если позиция Y + высота выбора категории больше чем сам лайаут на которм все отображается, то подгоним
                        int positionY=(int) product_category_groupselect.getY();
                        if (positionY+product_category_groupselect.getHeight()>product_registration_frame.getHeight()){
                            ViewGroup.LayoutParams layoutParams= product_category_groupselect.getLayoutParams();
                            layoutParams.height=product_registration_frame.getHeight()-positionY;
                            product_category_groupselect.setLayoutParams(layoutParams);
                        }
                    }
                }
        );
    }

    private class ProductCategoryOnItemClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ModelGroup productCategory=mCategoriesAdapter.getItem(position);
            LinkedHashMap<ModelGroup,LinkedHashMap> productCategoryCategories=
                    mProductCategoriesCurrent.get(productCategory);
            if (productCategoryCategories==null){
                product_category_view.setText(productCategory.toString());
                mProductCategoriesSelected=mProductCategoriesCurrent;
                removeProductCategorySelectedView();

            } else {
                mProductCategoriesCurrent =productCategoryCategories;
                mCategoriesAdapter.clear();
                mCategoriesAdapter.addAll(Product.getCurrentProductCategories(
                        mProductCategoriesCurrent,Product.CAT_NM_PRODUCT_ADD));
                resizeProductCategorySelectedView();
            }

        }
    }

    //Результаты
    private void getCameraImageToFile(){
        Intent imageIntent = null;
        try {
            imageIntent=mImage.getCameraImageIntentToFile();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        startActivityForResult(imageIntent,IMAGE_REQUEST);
    }

    private void setProductImage(){
        try {
            Bitmap productImage=mImage.getImageBitmap();
            if (productImage!=null)
                image_product_view.setImageBitmap(productImage);
        } catch (IOException e) {
            Toast.makeText(mThis,e.toString(),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode==RESULT_OK) {
            setProductImage();
            return;
        }
        if (requestCode == BARCODE_REQUEST){
            if (data!=null){
                String barcodeResult=data.getStringExtra(getApplicationContext().getPackageName()+".barcode");

                if (resultCode==RESULT_OK){
                    barcode_view.setText(barcodeResult);
                } else{
                    Toast.makeText(mThis,barcodeResult,Toast.LENGTH_LONG).show();
                }
            } else {//Отобразим клавиатуру
                SHOW_KEYBOARD=true;
            }

            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RC_HANDLE_CAMERA_PERM
                && grantResults.length != 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            getCameraImageToFile();
        }

    }

    private void registerProduct(){
        if (!barcode_view.checkBarcode()){
            Toast.makeText(mThis,R.string.barcode_not_check,Toast.LENGTH_LONG).show();
            return;
        }

        ArrayList <ImageManager.DecodeImageStructure> decodeImageParamArr=new ArrayList<>();

        decodeImageParamArr.add(new ImageManager.DecodeImageStructure(
                mImage.mImagePath,
                Constants.DECODE_IMAGE_SIZE_SMALL,Constants.DECODE_IMAGE_SIZE_SMALL));
        decodeImageParamArr.add(new ImageManager.DecodeImageStructure(
                mImage.mImagePath,
                Constants.DECODE_IMAGE_SIZE_MEDIUM,Constants.DECODE_IMAGE_SIZE_MEDIUM));
        decodeImageParamArr.add(new ImageManager.DecodeImageStructure(
                mImage.mImagePath,
                Constants.DECODE_IMAGE_SIZE_LARGE,Constants.DECODE_IMAGE_SIZE_LARGE));

        //ImageManager.startImageDecodeService(mThis,decodeImageParamArr);
    }



}