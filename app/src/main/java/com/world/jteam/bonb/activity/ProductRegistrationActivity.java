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

import com.world.jteam.bonb.AppInstance;
import com.world.jteam.bonb.media.CameraManager;
import com.world.jteam.bonb.media.ImageManager;
import com.world.jteam.bonb.Constants;
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
    private ConstraintLayout product_groups_groupselect;
    private Button product_group_view;
    private ModelGroup.ProductGroupsAdapter mProductGroupsAdapter;
    private LinkedHashMap<ModelGroup,LinkedHashMap> mProductGroupsCurrent; //В момент выбора
    private LinkedHashMap<ModelGroup,LinkedHashMap> mProductGroupsSelected; //Выбранный

    //Инициализация
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_registration);

        product_registration_frame=(FrameLayout) findViewById(R.id.product_registration_frame);
        product_group_view =(Button) findViewById(R.id.product_group);
        image_product_view=(ImageView) findViewById(R.id.image_product);
        barcode_view =(BarcodeEditView) findViewById(R.id.barcode);

        //Восстановим значения
        SaveContainer saveContainer=(SaveContainer) getLastCustomNonConfigurationInstance();
        if (saveContainer!=null){
            mImage=saveContainer.imageManager;
            mImage.linkContext(mThis);
            setProductImage();
            mProductGroupsSelected =saveContainer.productGroupsSelected;
            product_group_view.setText(saveContainer.productGroup);
        }

        //Продукт
        image_product_view.setOnClickListener(new ImageProductOnClickListener());

        //Штрихкод
        barcode_view.setOnFocusChangeListener(new BarcodeOnFocusChangeListener());

        //Категория
        product_group_view.setOnClickListener(new ProductGroupOnClick());

        //Инициализация работы с картинками
        if (mImage==null)
            mImage=new ImageManager(mThis);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        mImage.unLinkContext();
        return new SaveContainer(
                mImage,
                mProductGroupsSelected,
                product_group_view.getText()
        );
    }

    static class SaveContainer{
        public ImageManager imageManager;
        public LinkedHashMap<ModelGroup,LinkedHashMap> productGroupsSelected;
        public CharSequence productGroup;

        public SaveContainer(
                ImageManager imageManager,
                LinkedHashMap<ModelGroup,LinkedHashMap> productGroupsSelected,
                CharSequence productGroup){
            this.imageManager=imageManager;
            this.productGroupsSelected = productGroupsSelected;
            this.productGroup = productGroup;

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
    private class ProductGroupOnClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if (mProductGroupsSelected ==null){
                mProductGroupsSelected = AppInstance.getProductGroups();
            }
            if (mProductGroupsSelected ==null){
                Toast.makeText(mThis,R.string.product_groups_not_init,Toast.LENGTH_LONG).show();
                return;
            }

            if (product_groups_groupselect ==null){
                //Выведем лайаут со списком
                LayoutInflater ltInflater = getLayoutInflater();
                ltInflater.inflate(
                        R.layout.fragment_product_groups_listview,
                        product_registration_frame,
                        //product_registration_constr,
                        true);

                //Настроим список
                ListView product_groups_select =(ListView) findViewById(R.id.product_groups_select);
                ArrayList productGroupsList =
                        ModelGroup.getCurrentProductGroups(mProductGroupsSelected, ModelGroup.GROUP_NM_PRODUCT_ADD,new int[0]);

                mProductGroupsAdapter =new ModelGroup.ProductGroupsAdapter(mThis, productGroupsList);
                product_groups_select.setAdapter(mProductGroupsAdapter);
                product_groups_select.setOnItemClickListener(new ProductGroupOnItemClickListener());

                float productGroupPosY = product_group_view.getY()+ product_group_view.getHeight();
                product_groups_groupselect =(ConstraintLayout) findViewById(R.id.product_groups_groupselect);
                product_groups_groupselect.setY(productGroupPosY); //Спозиционируем на кнопку
                product_groups_groupselect.setX(product_group_view.getX()); //Спозиционируем на кнопку
                resizeProductGroupSelectedView(); //Подгоним список

                mProductGroupsCurrent = mProductGroupsSelected;

            } else {
                removeProductGroupSelectedView();
            }
        }
    }

    private void removeProductGroupSelectedView(){
        product_registration_frame.removeView(product_groups_groupselect);
        product_groups_groupselect =null;
    }

    private void resizeProductGroupSelectedView(){
        boolean setDefaultParam=false;
        ViewGroup.LayoutParams layoutParams= product_groups_groupselect.getLayoutParams();
        if (layoutParams.height!=ViewGroup.LayoutParams.WRAP_CONTENT){
            layoutParams.height=ViewGroup.LayoutParams.WRAP_CONTENT;
            setDefaultParam=true;
        }

        int maxGroupWidth = product_group_view.getWidth();
        if (layoutParams.width!= maxGroupWidth){
            layoutParams.width= maxGroupWidth;
            setDefaultParam=true;
        }

        if (setDefaultParam)
            product_groups_groupselect.setLayoutParams(layoutParams);

        product_groups_groupselect.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        product_groups_groupselect.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        //Если позиция Y + высота выбора категории больше чем сам лайаут на которм все отображается, то подгоним
                        int positionY=(int) product_groups_groupselect.getY();
                        if (positionY+ product_groups_groupselect.getHeight()>product_registration_frame.getHeight()){
                            ViewGroup.LayoutParams layoutParams= product_groups_groupselect.getLayoutParams();
                            layoutParams.height=product_registration_frame.getHeight()-positionY;
                            product_groups_groupselect.setLayoutParams(layoutParams);
                        }
                    }
                }
        );
    }

    private class ProductGroupOnItemClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ModelGroup productGroup = mProductGroupsAdapter.getItem(position);
            LinkedHashMap<ModelGroup,LinkedHashMap> productGroupGroups =
                    mProductGroupsCurrent.get(productGroup);
            if (productGroupGroups ==null){
                product_group_view.setText(productGroup.toString());
                mProductGroupsSelected = mProductGroupsCurrent;
                removeProductGroupSelectedView();

            } else {
                mProductGroupsCurrent = productGroupGroups;
                mProductGroupsAdapter.clear();
                mProductGroupsAdapter.addAll(ModelGroup.getCurrentProductGroups(
                        mProductGroupsCurrent, ModelGroup.GROUP_NM_PRODUCT_ADD,new int[0]));
                resizeProductGroupSelectedView();
            }

        }
    }

    //Результаты
    private void getCameraImageToFile(){
        Intent imageIntent = null;
        try {
            imageIntent=mImage.getCameraImageIntentToFile();
        } catch (IOException e) {
            AppInstance.errorLog("Camera image", e.toString());
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
            AppInstance.errorLog("Product image", e.toString());
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