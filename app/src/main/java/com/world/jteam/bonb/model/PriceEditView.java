package com.world.jteam.bonb.model;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.AttributeSet;

import com.world.jteam.bonb.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PriceEditView extends android.support.v7.widget.AppCompatEditText {
    public PriceEditView(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.priceEditTextStyle);

        setFilters(new InputFilter[] {new PriceFormatInputFilter()});
    }

    private class PriceFormatInputFilter implements InputFilter {

        Pattern mPattern = Pattern.compile("^|((0|[1-9]{1,1}[0-9]{0,5})(\\.[0-9]{0,2})?)");

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
