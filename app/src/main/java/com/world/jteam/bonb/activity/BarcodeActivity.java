package com.world.jteam.bonb.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import com.world.jteam.bonb.AppInstance;
import com.world.jteam.bonb.R;
import com.world.jteam.bonb.media.BarcodeManager;
import com.world.jteam.bonb.media.CameraManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


public class BarcodeActivity extends AppCompatActivity {
    private static final int RC_HANDLE_CAMERA_PERM = 1;
    private BarcodeDetector mBarcodeDetector;
    private CameraSource mCameraSource;
    private SurfaceView mCameraView;
    private boolean mCloseAfterRead =true;
    private BarcodeManager.OnAfterReadListener mOnAfterReadListener;
    private String mLastBarcode;
    private Date mLastDataReadBarcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);

        //Обработаем вхождения
        Intent intent = getIntent();
        mCloseAfterRead=intent.getBooleanExtra("CloseAfterRead",mCloseAfterRead);
        mOnAfterReadListener=(BarcodeManager.OnAfterReadListener) intent.getSerializableExtra("afterReadOK");

        mCameraView = (SurfaceView) findViewById(R.id.camera_view);

        mBarcodeDetector = BarcodeManager.getBarcodeDetector(this,Barcode.EAN_13);
        if (!mBarcodeDetector.isOperational()) {
            finishWithResult(RESULT_CANCELED, getString(R.string.barcode_not_operational));
        }

        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createSource();
        } else {
            CameraManager.requestCameraPermission(this,
                    RC_HANDLE_CAMERA_PERM,
                    new CameraManager.CameraRequest() {
                        @Override
                        public void previouslyDeniedTheCameraRequest() {
                            finishWithResult(RESULT_CANCELED, getString(R.string.not_camera_permission));
                        }
                    });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mCameraView.getHolder().getSurface().isValid())
            startCameraSource();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mCameraSource!=null)
            mCameraSource.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mBarcodeDetector!=null)
            mBarcodeDetector.release();

        if(mCameraSource!=null)
            mCameraSource.release();
    }

    private class SurfaceHolderCallback implements SurfaceHolder.Callback{
        @Override
        @SuppressWarnings("MissingPermission")
        public void surfaceCreated(SurfaceHolder holder) {
            startCameraSource();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }

    private class BarcodeDetectorProcessor implements BarcodeDetector.Processor {
        private ArrayList<String> barcodes = new ArrayList<String>();

        @Override
        public void release() {

        }

        @Override
        public void receiveDetections(Detector.Detections detections) {
            SparseArray<Barcode> barcodesDetections=detections.getDetectedItems();
            final String barcode=barcodesDetections.valueAt(0).rawValue;
            barcodes.add(barcode);

            if (Collections.frequency(barcodes,barcode)>=7){
                barcodes.clear();
                Date dataRead=new Date();

                if (mLastBarcode==null || !mLastBarcode.equals(barcode) || (dataRead.getTime()-mLastDataReadBarcode.getTime())/1000>=5) { //Затычка от мультизацикливания одно и того же штрихкода
                    mLastBarcode=barcode;
                    mLastDataReadBarcode=dataRead;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (Character.toString(barcode.charAt(0)).equals("2"))
                                readProcessing(RESULT_CANCELED,
                                        getString(R.string.service_barcode) + ": " + barcode);
                            else
                                readProcessing(RESULT_OK, barcode);
                        }
                    });
                }
            }

        }
    }

    private void createSource(){
        mBarcodeDetector.setProcessor(new BarcodeDetectorProcessor());

        mCameraView.getHolder().addCallback(new SurfaceHolderCallback());

        Camera camera=Camera.open();
        Camera.Size cameraSize=camera.getParameters().getPreviewSize();

        mCameraSource = new CameraSource.Builder(getApplicationContext(), mBarcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(cameraSize.width, cameraSize.height)
                .setRequestedFps(15.0f)
                .setAutoFocusEnabled(true)
                .build();

        camera.release();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RC_HANDLE_CAMERA_PERM
                && grantResults.length != 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createSource();
                if (mCameraView.getHolder().getSurface().isValid())
                    startCameraSource();
            } else {
                finishWithResult(RESULT_CANCELED, getString(R.string.not_camera_permission));
            }
        }

    }

    @SuppressWarnings("MissingPermission")
    private void startCameraSource(){
        try {
            mCameraSource.start(mCameraView.getHolder());
        } catch (IOException e) {
            AppInstance.errorLog("Start camera", e.toString());
            finishWithResult(RESULT_CANCELED,getString(R.string.error_start_barcode_detector));
        }
    }

    public void setCloseAfterRead(boolean closeAfterRead){
        mCloseAfterRead=closeAfterRead;
    }

    public void setOnAfterReadListener(BarcodeManager.OnAfterReadListener onAfterReadListener){
        mOnAfterReadListener=onAfterReadListener;
    }

    private void readProcessing(int resultCode, String barcode){
        if (mCloseAfterRead){
            finishWithResult(resultCode,barcode);
        } else{
            if (resultCode==RESULT_OK && mOnAfterReadListener!=null){
                mOnAfterReadListener.onAfterReadOK(barcode);
            } else {
                Toast.makeText(this,barcode,Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void finishWithResult(int resultCode, String barcode){
        Intent resultIntent = new Intent();
        resultIntent.putExtra(getApplicationContext().getPackageName()+".barcode",barcode);

        setResult(resultCode, resultIntent);
        finish();
    }

}
