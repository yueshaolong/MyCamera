package com.example.android.camera2basic.screencapture;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.example.android.camera2basic.camera1.SystemUtil;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * 启动悬浮窗界面
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class FloatWindowsService extends Service {
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private Intent mResultData;
    private ImageReader mImageReader;
    private WindowManager mWindowManager;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;
    private MediaProjectionManager mediaProjectionManager;
    private int barHeight;
    private String imagePath;

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        System.out.println("屏幕宽："+mScreenWidth+"   高："+mScreenHeight);
        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 1);
    }

    @SuppressLint("WrongConstant")
    @Override
    public IBinder onBind(Intent intent) {
        mResultData = intent.getParcelableExtra("Intent");
        barHeight = intent.getIntExtra("barHeight", 0);
        imagePath = intent.getStringExtra("imagePath");
        return new MyBinder();
    }
    public class MyBinder extends Binder {
        public void start(){
            startScreenShot();
        }
        public FloatWindowsService getService(){
            return FloatWindowsService.this;
        }
    }

    public void startScreenShot() {
        if (mResultData == null){
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            startActivity(intent);
            return;
        }
        startCapture();
//        Handler handler1 = new Handler();
//        handler1.postDelayed(new Runnable() {
//            public void run() {
//                startVirtual();
//            }
//        }, 5);
//
//        handler1.postDelayed(new Runnable() {
//            public void run() {
//                startCapture();
//            }
//        }, 30);
    }

//    public void startVirtual() {
//        if (mMediaProjection != null) {
//            virtualDisplay();
//        } else {
//            setUpMediaProjection();
//            virtualDisplay();
//        }
//    }
//    public void setUpMediaProjection() {
//        if (mResultData == null) {
//            Intent intent = new Intent(Intent.ACTION_MAIN);
//            intent.addCategory(Intent.CATEGORY_LAUNCHER);
//            startActivity(intent);
//        } else {
//            mMediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, mResultData);
//        }
//    }
    private void startCapture() {
        mMediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, mResultData);
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                mScreenWidth, mScreenHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);

        Image image = mImageReader.acquireLatestImage();
        if (image == null) {
            startScreenShot();
        } else {
            int width = image.getWidth();
            int height = image.getHeight();
            System.out.println("image 宽："+width+"   高："+height);
            final Image.Plane[] planes = image.getPlanes();
            System.out.println(planes.length);
            final ByteBuffer buffer = planes[0].getBuffer();
            //每个像素的间距
            int pixelStride = planes[0].getPixelStride();
            //总的间距
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height,
                    Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width,
                    height
                            - SystemUtil.dp2px(FloatWindowsService.this,114)
                            -barHeight);
            image.close();
            if(finish != null){
                finish.setImage(bitmap);
            }

//            File fileImage = null;
//            if (bitmap != null) {
//                try {
//                    fileImage = new File(imagePath);
//                    if (!fileImage.exists()) {
//                        fileImage.createNewFile();
//                    }
//                    FileOutputStream out = new FileOutputStream(fileImage);
//                    if (out != null) {
//                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
//                        out.flush();
//                        out.close();
//
//                        Uri contentUri = ImagePathUriUtil.path2Uri(getApplicationContext(), fileImage.getAbsolutePath());
//                        System.out.println("contentUri-====="+contentUri);
////                        Intent media = new Intent("com.example.android.camera2basic");
////                        media.setData(contentUri);
////                        FloatWindowsService.this.sendBroadcast(media);
//
//                    }
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                    fileImage = null;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    fileImage = null;
//                }
//            }
        }
    }

    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    private void stopVirtual() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopVirtual();
        tearDownMediaProjection();
    }

    public interface Finish extends Serializable {
        void setImage(Bitmap bitmap);
    }
    private Finish finish;

    public void setFinish(Finish finish) {
        this.finish = finish;
    }
}
