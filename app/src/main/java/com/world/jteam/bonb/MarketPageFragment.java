package com.world.jteam.bonb;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Random;

public class MarketPageFragment extends Fragment {
    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    int pageNumber;
    int backColor;
    public View page1_List = null;
    public View page2_Info = null;

    public static MarketPageFragment newInstance(int page,View page1, View page2) {
        MarketPageFragment pageFragment = new MarketPageFragment();

        pageFragment.page1_List = page1;
        pageFragment.page2_Info = page2;
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        switch (pageNumber) {
            case (1):
                if (page2_Info == null) {
                    page2_Info = inflater.inflate(R.layout.fragment_market_contacts, null);
                }
                return page2_Info;
            case (2):

                if (page1_List == null) {
                    page1_List = inflater.inflate(R.layout.fragment_market_product_list, null);
                }
                return page1_List;
            default:
                if (page1_List == null) {
                    page1_List = inflater.inflate(R.layout.fragment_market_product_list, null);
                }
                return page1_List;
        }

    }
}