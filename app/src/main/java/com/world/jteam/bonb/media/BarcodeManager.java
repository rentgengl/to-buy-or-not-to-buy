package com.world.jteam.bonb.media;

import android.content.Context;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.Serializable;

public class BarcodeManager {
    //Возвращает класс детектора
    public static BarcodeDetector getBarcodeDetector(Context context, int barcode_format){
        return new BarcodeDetector.Builder(context)
                .setBarcodeFormats(barcode_format)
                .build();
    }

    //Производит первую инициализацию и загрузку компонентов для декодера
    public static void firstInitBarcodeDetector(Context context){
        BarcodeDetector barcodeDetector = BarcodeManager.getBarcodeDetector(context,Barcode.EAN_13);
        barcodeDetector.isOperational();
        barcodeDetector.release();
    }

    //Интерфейс для обработки считывания
    public static interface OnAfterReadListener extends Serializable {
        void onAfterReadOK(String barcode);
    }
}
