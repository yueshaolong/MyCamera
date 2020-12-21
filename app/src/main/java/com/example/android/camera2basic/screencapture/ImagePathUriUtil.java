package com.example.android.camera2basic.screencapture;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;

public class ImagePathUriUtil {
    private static final String TAG = "ImagePathUriUtil";
    /**
     * 通过图片路径得到uri,
     * @param context  上下文
     * @param path  图片的绝对路径
     * @return
     */
    public static Uri path2Uri(Context context, String path) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { path },
                null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
            Log.d(TAG, "path2Uri: "+uri.toString());
            return uri;
        } else {
            // 如果图片不在手机的共享图片数据库，就先把它插入。
            if (new File(path).exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, path);
                Uri uri2 = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Log.d(TAG, "path2Uri uri2: "+uri2.toString());
                return uri2;
            } else {
                return null;
            }
        }
    }
}
