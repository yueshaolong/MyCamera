/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ysl.camera.camera1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.kingja.loadsir.core.LoadSir;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.ysl.camera.R;
import com.ysl.camera.weiget.EmptyCallback;
import com.ysl.camera.weiget.ErrorCallback;
import com.ysl.camera.weiget.LoadingCallback;
import com.ysl.camera.weiget.PlaceholderCallback;
import com.ysl.camera.weiget.TimeoutCallback;

import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;

public class CameraActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                //异常处理
            }
        });
        LoadSir.beginBuilder()
                .addCallback(new ErrorCallback())//添加各种状态页
                .addCallback(new EmptyCallback())
                .addCallback(new LoadingCallback())
                .addCallback(new TimeoutCallback())
                .addCallback(new PlaceholderCallback())
                .setDefaultCallback(LoadingCallback.class)//设置默认状态页
                .commit();
        new RxPermissions(this)
                .request(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION//,
//                        Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
//                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
                .subscribe(granted -> {
                    if (granted) {
                        if (null == savedInstanceState) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, Camera1BasicFragment.newInstance())
                                    .commit();
                        }
                    }
                });
    }

}
