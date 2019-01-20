package com.world.jteam.bonb.media;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;

import com.world.jteam.bonb.R;

import java.io.UnsupportedEncodingException;

public class CameraManager {
    private Context mContext;

    public CameraManager(Context context){
        mContext=context;
    }

    public interface CameraRequest{
        void previouslyDeniedTheCameraRequest();
    }

    public static void requestCameraPermission(Activity activity,
                                               int requestCode,
                                               CameraRequest cameraRequest) {

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        } else {
            cameraRequest.previouslyDeniedTheCameraRequest();
        }

    }

    public static Intent getImageCaptureIntent(Context context) throws UnsupportedEncodingException {
        Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (imageCaptureIntent.resolveActivity(context.getPackageManager()) == null) {
            throw new UnsupportedEncodingException(context.getString(R.string.camera_not_support));
        }

        return imageCaptureIntent;
    }

}
