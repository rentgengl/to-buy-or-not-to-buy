package com.world.jteam.bonb.media;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import com.world.jteam.bonb.AppInstance;
import com.world.jteam.bonb.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ImageManager {
    public Uri mImageUri;
    public String mImagePath;
    private Context mContext;

    public ImageManager(Context context){
        mContext=context;
    }

    //Получение
    public Intent getCameraImageIntentToFile() throws IOException,UnsupportedOperationException {
        Intent cameraIntent = CameraManager.getImageCaptureIntent(mContext);

        File imageFile = null;
        try {
            imageFile = createImageFile(mContext,Bitmap.CompressFormat.PNG);
        } catch (IOException e) {
            AppInstance.errorLog("Create image", e.toString());
            throw new IOException(mContext.getString(R.string.temp_file_ex));
        }

        mImageUri = FileProvider.getUriForFile(mContext,"com.world.jteam.bonb.provider",imageFile);

        mImagePath=imageFile.getPath();

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);

        return cameraIntent;
    }

    public static Bitmap getImageBitmapPreview(Intent data){
        if(data==null)
            return null;

        return (Bitmap) data.getExtras().get("data");
    }

    public Bitmap getImageBitmap() throws IOException {
        Bitmap imageBitmap = null;
        if (mImageUri!=null){
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), mImageUri);
            } catch (IOException e) {
                AppInstance.errorLog("Get bitmap", e.toString());
                throw new IOException(mContext.getString(R.string.temp_file_ex));
            }
        }

        return imageBitmap;
    }

   //Сжатие
    public static int calculateInSampleSize(
           BitmapFactory.Options options, int reqWidth, int reqHeight) {
       // Raw height and width of image
       final int height = options.outHeight;
       final int width = options.outWidth;
       int inSampleSize = 1;

       if (height > reqHeight || width > reqWidth) {

           // Calculate ratios of height and width to requested height and width
           final int heightRatio = Math.round((float) height / (float) reqHeight);
           final int widthRatio = Math.round((float) width / (float) reqWidth);

           // Choose the smallest ratio as inSampleSize value, this will guarantee
           // a final image with both dimensions larger than or equal to the
           // requested height and width.
           inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
       }

       return inSampleSize;
   }

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();

        // First decode with inJustDecodeBounds=true to check dimensions
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);

    }

//    public static void startImageDecodeService(Context context, ArrayList<DecodeImageStructure> decodeImageParamArr){
//        Intent intentService = new Intent(context, ImageDecodeService.class);
//        intentService.putExtra("decodeImageParamArr",decodeImageParamArr);
//        context.startService(intentService);
//    }

    public static class DecodeImageStructure implements Parcelable {
        public String imagePath;
        public int imageWidth;
        public int imageHeight;

        public DecodeImageStructure(String path, int width, int height){
            imagePath=path;
            imageWidth=width;
            imageHeight=height;
        }

        public static final Creator<DecodeImageStructure> CREATOR = new Creator<DecodeImageStructure>() {
            @Override
            public DecodeImageStructure createFromParcel(Parcel source) {
                String path = source.readString();
                int width = source.readInt();
                int height = source.readInt();
                return new DecodeImageStructure(path, width, height);
            }

            @Override
            public DecodeImageStructure[] newArray(int size) {
                return new DecodeImageStructure[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel source, int flags) {
            source.writeString(imagePath);
            source.writeInt(imageWidth);
            source.writeInt(imageHeight);
        }

    }

    //Сохранение
    public static String saveBitmapToImageFile(Context context, Bitmap imageBitmap,Bitmap.CompressFormat format) throws Exception{
        File imageFile = null;

        try {
            imageFile = createImageFile(context,format);
            FileOutputStream fos=new FileOutputStream(imageFile);
            imageBitmap.compress(format, 100, fos);
            fos.close();
        } catch (Exception e) {
            AppInstance.errorLog("Save image", e.toString());
            throw new Exception(context.getString(R.string.save_image_ex));
        }

        return imageFile.getPath();
    }

    //Прочее
    private static File createImageFile(Context context, Bitmap.CompressFormat format) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = format.name()+"_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                "."+format.name(),         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    public void unLinkContext(){
        mContext=null;
    }

    public void linkContext(Context context){
        mContext=context;
    }


}
