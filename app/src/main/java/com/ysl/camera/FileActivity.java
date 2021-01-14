package com.ysl.camera;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.ysl.camera.camera1.CameraActivity;
import com.ysl.camera.screencapture.CaptureActivity;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import me.rosuh.filepicker.config.FilePickerManager;

public class FileActivity extends AppCompatActivity {

    private String path;
    private TextView tv;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        new RxPermissions(this)
                .request(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                )
                .subscribe();
        tv = findViewById(R.id.tv);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
//                intent.setType("image/jpeg");
//                intent.setType("text/plain");
//                intent.setType("application/pdf");
//                intent.setType("application/msword");
//                intent.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                startActivityForResult(intent, 0);
            }
        });


        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilePickerManager.INSTANCE
                        .from(FileActivity.this)
//                        .filter(new AbstractFileFilter() {
//                            @Override
//                            public ArrayList<FileItemBeanImpl> doFilter(ArrayList<FileItemBeanImpl> arrayList) {
//                                for (FileItemBeanImpl fileItemBean : arrayList) {
//                                    System.out.println("fileItemBean--name--->"+fileItemBean.getFileName());
//                                    System.out.println("fileItemBean----->"+fileItemBean.getFilePath());
//                                    FileType fileType = fileItemBean.getFileType();
//                                    System.out.println("fileItemBean----->"+(fileType!=null?fileType.getFileType():"null"));
//                                }
//                                Iterator<FileItemBeanImpl> iterator = arrayList.iterator();
//                                while (iterator.hasNext()){
//                                    FileItemBeanImpl next = iterator.next();
//                                    if(next!= null){
//                                        FileType fileType = next.getFileType();
//                                        if(fileType == null){
//                                            continue;
//                                        }
//                                        String fileName = next.getFileName();
//                                        if(!TextUtils.isEmpty(fileName)
//                                                && !fileName.toLowerCase().endsWith(".doc")
//                                                && !fileName.toLowerCase().endsWith(".docx")
//                                                && !fileName.toLowerCase().endsWith(".ppt")
//                                                && !fileName.toLowerCase().endsWith(".pptx")
//                                                && !fileName.toLowerCase().endsWith(".pdf")
//                                        ){
//                                            iterator.remove();
//                                        }
//                                    }
//                                }
//                                return arrayList;
//                            }
//                        })
                        .enableSingleChoice()
                        .forResult(FilePickerManager.REQUEST_CODE);
            }
        });

        findViewById(R.id.btn_sy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(FileActivity.this, CameraActivity.class),100);
            }
        });
        findViewById(R.id.btn_jp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(FileActivity.this, CaptureActivity.class),100);
            }
        });
        imageView = findViewById(R.id.iv);
    }

    // 获取文件的真实路径
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            // 用户未选择任何文件，直接返回
            return;
        }
        switch (requestCode) {
            case FilePickerManager.REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    List<String> strings = FilePickerManager.INSTANCE.obtainData();
                    // do your work
                    System.out.println("strings-------->" + strings);
                    Iterator<String> iterator = strings.iterator();
                    while (iterator.hasNext()) {
                        String next = iterator.next();
                        if (!TextUtils.isEmpty(next)
                                && !next.toLowerCase().endsWith(".doc")
                                && !next.toLowerCase().endsWith(".docx")
                                && !next.toLowerCase().endsWith(".ppt")
                                && !next.toLowerCase().endsWith(".pptx")
                                && !next.toLowerCase().endsWith(".pdf")
                        ) {
                            iterator.remove();
                        }
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String string : strings) {
                        if (!TextUtils.isEmpty(string)) {
                            stringBuilder.append(string).append("\n");
                            File file = new File(string);
                            System.out.println(file.getName() + "=========" + byte2FitMemorySize(file.length()));
                        }
                    }
                    tv.setText(stringBuilder.toString());
                } else {
                    Toast.makeText(FileActivity.this,
                            "You didn't choose anything~", Toast.LENGTH_SHORT).show();
                }
                break;
            case 0:
                Uri uri = data.getData(); // 获取用户选择文件的URI
                //通过ContentProvider查询文件路径
                ContentResolver resolver = this.getContentResolver();
                Cursor cursor = resolver.query(uri, null, null,
                        null, null);
                if (cursor == null) {
                    // 未查询到，说明为普通文件，可直接通过URI获取文件路径
                    path = uri.getPath();
//                    return;
                }
                if (cursor.moveToFirst()) {
                    // 多媒体文件，从数据库中获取文件的真实路径
                    path = cursor.getString(cursor.getColumnIndex("_data"));
                }
                cursor.close();

                path = getFilePathFromUri(this, uri);
                tv.setText(path);
                break;
            case 100:
                imageView.setVisibility(View.VISIBLE);
                String imagePath = data.getStringExtra("imagePath");
                String imageUri = data.getStringExtra("imageUri");
                Glide.with(this).load(imageUri).centerInside().into(imageView);
                break;
        }
    }

    public static String byte2FitMemorySize(long byteNum) {
        if (byteNum <= 0) {
            return String.format("%sB", 0);
        } else if (byteNum < 1024) {
            return String.format("%.2fB", (double) byteNum);
        } else if (byteNum < 1048576) {
            return String.format("%.2fK", (double) byteNum / 1024);
        } else if (byteNum < 1073741824) {
            return String.format("%.2fM", (double) byteNum / 1048576);
        } else {
            return String.format("%.2fG", (double) byteNum / 1073741824);
        }
    }

    // 获取文件的真实路径
    public static String getFilePathFromUri(Context context, Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        System.out.println("scheme-------->" + scheme);
        String path = null;
        if (scheme == null)
            path = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            path = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{
                    MediaStore.MediaColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                    if (index > -1) {
                        path = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        System.out.println("path----------->" + path);
        return path;
    }
}