package com.world.jteam.bonb;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.AttributeSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BarcodeEditView extends android.support.v7.widget.AppCompatEditText {
    public BarcodeEditView(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.barcodeEditTextStyle);

        setFilters(new InputFilter[] {new BarcodeFormatInputFilter()});
    }

    public final boolean checkBarcode(){
        CharSequence barcode=getText();

        if (barcode.length()!=13)
            return false;

        int digit;
        int evens=0; //четные
        int odds=0; //не четные
        int checksum=0; //не четные

        for (int i=1; i<=barcode.length();i++){
            digit=Integer.parseInt(""+barcode.charAt(i-1));
            if (i % 2 == 0){ //Четные
                evens=evens+digit;
            } else { //Не четные, кроме последней
                odds=odds+digit;
            }

            if (i==barcode.length())
                checksum=digit;
        }

        return (10 - ((evens*3 + odds - checksum)%10)) == checksum;
    }

    private class BarcodeFormatInputFilter implements InputFilter {

        Pattern mPattern = Pattern.compile("^|([0-13-9]{1,1}[0-9]{0,12})");

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            String result =
                    dest.subSequence(0, dstart)
                            + source.toString()
                            + dest.subSequence(dend, dest.length());

            Matcher matcher = mPattern.matcher(result);

            if (!matcher.matches()) return dest.subSequence(dstart, dend);

            return null;
        }
    }
}
