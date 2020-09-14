package com.example.android.camera2basic.weiget;

import android.content.Context;
import android.view.View;

import com.example.android.camera2basic.R;
import com.kingja.loadsir.callback.Callback;

public class PlaceholderCallback extends Callback {

    @Override
    protected int onCreateView() {
        return R.layout.layout_placeholder;
    }

    @Override
    protected boolean onReloadEvent(Context context, View view) {
        return true;
    }
}
