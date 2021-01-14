package com.ysl.camera.weiget;


import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.ysl.camera.R;
import com.kingja.loadsir.callback.Callback;

public class ErrorCallback extends Callback {
    @Override
    protected int onCreateView() {
        return R.layout.layout_error;
    }
    public static View.OnClickListener listener;
    @Override
    protected void onViewCreate(Context context, View view) {
        super.onViewCreate(context, view);
        TextView id_tv_refresh = view.findViewById(R.id.id_tv_refresh);
        id_tv_refresh.setOnClickListener(listener);
    }

    @Override
    protected boolean onReloadEvent(Context context, View view) {
        return true;
    }
}
