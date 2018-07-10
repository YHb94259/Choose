package com.example.choose;

import android.view.LayoutInflater;
import android.view.View;

import com.guoqi.highlightview.Component;

/**
 * Created by Administrator on 2018/5/24.
 */

public class MyComponent implements Component {
    @Override
    public View getView(LayoutInflater inflater) {
        View view = View.inflate(inflater.getContext(), R.layout.shade_main, null);
        return view;
    }

    @Override
    public int getAnchor() {
        return Component.ANCHOR_TOP;
    }

    @Override
    public int getFitPosition() {
        return Component.FIT_START;
    }

    @Override
    public int getXOffset() {
        return 120;
    }

    @Override
    public int getYOffset() {
        return -50;
    }
}
