package com.ysl.camera.screencapture;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.allen.library.SuperTextView;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.ysl.camera.R;
import com.ysl.camera.camera1.CameraPreview;
import com.ysl.camera.camera1.SystemUtil;
import com.ysl.camera.camera2.SingleMediaScanner;
import com.ysl.camera.screencapture.FloatWindowsService.Finish;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CaptureActivity extends AppCompatActivity
        implements OnClickListener {
    private static final String TAG = "CaptureActivity";
    private static final int REQUEST_MEDIA_PROJECTION = 0;
    private ImageView preview;
    private LinearLayout ll_take_photo;
    private LinearLayout ll_photo_message;
    private LinearLayout ll_save_delete;
    private ImageView switch_flash;
    private File mFile;
    private String fileName;
    private FrameLayout mPreviewLayout;
    private Camera mCamera;
    private boolean isFlashing;
    private Bitmap newBitmap;
    private CameraPreview cameraPreview;
    private FloatWindowsService.MyBinder myBinder;
    private int screenWidth;
    private int screenHeight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_camera3_basic);

        new RxPermissions(this)
                .request(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION//,
                )
                .subscribe(granted -> {

                });

        getScreenBrightness();
        screenHeight = SystemUtil.getScreenHeight(this);
        screenWidth = SystemUtil.getScreenWidth(this);

        RelativeLayout rl = findViewById(R.id.rl);
        mPreviewLayout = findViewById(R.id.camera_preview_layout);
        preview = findViewById(R.id.preview);
        ll_take_photo = findViewById(R.id.ll_take_photo);
        ll_photo_message = findViewById(R.id.ll_photo_message);
        ll_save_delete = findViewById(R.id.ll_save_delete);
        switch_flash = findViewById(R.id.switch_flash);
        TextView iv_back = findViewById(R.id.iv_back);
        ImageView take_photo = findViewById(R.id.take_photo);
        ImageView switch_camera = findViewById(R.id.switch_camera);
        ImageView delete = findViewById(R.id.delete);
        ImageView save = findViewById(R.id.save);
        findViewById(R.id.switch_flash).setOnClickListener(this);
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.take_photo).setOnClickListener(this);
        findViewById(R.id.switch_camera).setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
        findViewById(R.id.save).setOnClickListener(this);
        initOrientate();
        requestCapturePermission();
        ((SuperTextView)findViewById(R.id.stv_time)).setLeftString(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
    }
    public void requestCapturePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //5.0 之后才允许使用屏幕截图
            return;
        }

        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION);
    }
    //增加传感器
    private OrientationEventListener mOrientationEventListener;
    //拍照时的传感器方向
    private int takePhotoOrientation = 0;
    //初始化传感器方向
    private int oldOrientation = 0;
    private void  initOrientate(){
        if(mOrientationEventListener == null){
            mOrientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_UI) {
                @Override
                public void onOrientationChanged(int orientation) {
                    // i的范围是0-359
                    // 屏幕左边在顶部的时候 i = 90;
                    // 屏幕顶部在底部的时候 i = 180;
                    // 屏幕右边在底部的时候 i = 270;
                    // 正常的情况默认i = 0;
//                    System.out.println("orientation------>"+orientation);
                    if(45 <= orientation && orientation < 135){
                        takePhotoOrientation = 180;
                    } else if(135 <= orientation && orientation < 225){
                        takePhotoOrientation = 270;
                    } else if(225 <= orientation && orientation < 315){
                        takePhotoOrientation = 0;
                    } else {
                        takePhotoOrientation = 90;
                    }
//                    System.out.println("takePhotoOrientation------>"+takePhotoOrientation);


                    if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                        return; // 手机平放时，检测不到有效的角度
                    }
                    // 只检测是否有四个角度的改变
                    if (orientation < 60 && orientation > 30) { // 动画0度与接口360度相反,增加下限抵消0度影响
                        orientation = 0;
                    } else if (orientation > 70 && orientation < 110) { // 动画90度与接口270度相反
                        orientation = 270;
                    } else if (orientation > 160 && orientation < 200) { // 180度
                        orientation = 180;
                    } else if (orientation > 240 && orientation < 300) {
                        orientation = 90;
                    } else if (orientation > 320 && orientation < 340) {// 减少上限减少360度的影响
                        orientation = 0;
                    } else {
                        return;
                    }
                    if (oldOrientation != orientation) {
                        ObjectAnimator rotation = ObjectAnimator
                                .ofFloat(ll_photo_message, "Rotation", oldOrientation,
                                        orientation).setDuration(0);
                        int photoMessageHeight = ll_photo_message.getHeight();
//                        System.out.println("ll_photo_message--h---->"+photoMessageHeight+"--w---->"+screenWidth);
//                        System.out.println("orientation------>"+orientation);
                        if (orientation == 270) {
                            ll_photo_message.setPivotX(screenWidth/2);
                            ll_photo_message.setPivotY(-(screenWidth/2-photoMessageHeight));
                        }else if(orientation == 90){
                            ll_photo_message.setPivotX(screenWidth/2);
                            ll_photo_message.setPivotY(-(screenWidth/2-photoMessageHeight));
                        }else if(orientation == 180){
                            ll_photo_message.setPivotX(screenWidth/2);
                            ll_photo_message.setPivotY(-(screenWidth/2-photoMessageHeight));
                        }else {
                            ll_photo_message.setPivotX(0);
                            ll_photo_message.setPivotY(0);
                        }
                        rotation.start();
                        if (orientation == 270 || orientation == 0){
                            ObjectAnimator translationY = ObjectAnimator.ofFloat(ll_photo_message, "translationY",
                                    -(cameraPreview.getHeight() - screenWidth), 0);
                            translationY.setDuration(0);
                            translationY.start();
                        } else if(orientation == 90 || orientation == 180){
                            ObjectAnimator translationY = ObjectAnimator.ofFloat(ll_photo_message, "translationY",
                                    0,-(cameraPreview.getHeight() - screenWidth));
                            translationY.setDuration(0);
                            translationY.start();
                        }
                        ll_photo_message.clearAnimation();
                        oldOrientation = orientation;
                    }
                }
            };

        }
        mOrientationEventListener.enable();
    }

    /**
     * 加入调整亮度
     */
    private void getScreenBrightness() {
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        //screenBrightness的值是0.0-1.0 从0到1.0 亮度逐渐增大 如果是-1，那就是跟随系统亮度
        lp.screenBrightness = Float.valueOf(200) * (1f / 255f);
        this.getWindow().setAttributes(lp);
    }
    private String createFile() {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .getAbsoluteFile() + File.separator + "Camera2Basic";
        File mIVMSFolder = new File(path);
        if (!mIVMSFolder.exists()) {
            mIVMSFolder.mkdirs();
        }
        fileName = System.currentTimeMillis() + ".jpg";
        mFile = new File(mIVMSFolder.getAbsolutePath(), fileName);
        return mFile.getAbsolutePath();
    }
    private void takePhoto() {
        ll_take_photo.setVisibility(View.GONE);
        ll_save_delete.setVisibility(View.VISIBLE);
        Toast.makeText(this,"拍照",Toast.LENGTH_SHORT).show();
        System.out.println("-------->执行拍照");
        //TODO 执行截屏
        myBinder.start();
    }
    private int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    public boolean isNavigationBarShow(Activity mActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = mActivity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            return realSize.y != size.y;
        } else {
            boolean menu = ViewConfiguration.get(mActivity).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            if (menu || back) {
                return false;
            } else {
                return true;
            }
        }
    }
    private int getNavigationBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_MEDIA_PROJECTION:
                if (resultCode == RESULT_OK && data != null) {
                    Intent intent = new Intent(getApplicationContext(), FloatWindowsService.class);
                    intent.putExtra("Intent", data);
                    intent.putExtra("navigationBarHeight",isNavigationBarShow(this)? getNavigationBarHeight(this):0);
                    intent.putExtra("statusBarHeight",getStatusBarHeight(this));
                    intent.putExtra("imagePath",createFile());
                    bindService(intent, new ServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName name, IBinder service) {
                            Log.d(TAG, "onServiceConnected is invoke");
                            myBinder = (FloatWindowsService.MyBinder) service;
                            myBinder.getService().setFinish(new Finish() {
                                @Override
                                public void setImage(Bitmap bitmap) {
                                    System.out.println("-------<设置图片");
                                    newBitmap = bitmap;
                                    ll_take_photo.setVisibility(View.GONE);
                                    ll_photo_message.setVisibility(View.GONE);
                                    ll_save_delete.setVisibility(View.VISIBLE);
                                    mPreviewLayout.setVisibility(View.GONE);
                                    switch_flash.setVisibility(View.GONE);
                                    preview.setVisibility(View.VISIBLE);
                                    preview.setImageBitmap(bitmap);

                                    cameraPreview.setVisibility(View.GONE);
                                }
                            });
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName name) {

                        }
                    },BIND_AUTO_CREATE);

                    openCamera();
                }
                break;
        }
    }
    private void switchFlash() {
        isFlashing = !isFlashing;
        switch_flash.setImageResource(isFlashing ? R.drawable.flash_open : R.drawable.flash_close);
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(isFlashing ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            Toast.makeText(this, "该设备不支持闪光灯", Toast.LENGTH_SHORT);
        }
    }
    private void cancleSavePhoto() {
        ll_take_photo.setVisibility(View.VISIBLE);
        ll_save_delete.setVisibility(View.GONE);
        ll_photo_message.setVisibility(View.VISIBLE);
        mPreviewLayout.setVisibility(View.VISIBLE);
        switch_flash.setVisibility(View.VISIBLE);
        if (mFile != null && mFile.exists()) mFile.delete();
        preview.setVisibility(View.GONE);
        //开始预览
        cameraPreview.setVisibility(View.VISIBLE);
//        mCamera.startPreview();
    }
    public void saveBitmapFile(Bitmap bitmap){
        if (null == bitmap) return;
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(mFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.take_photo:
                takePhoto();
                break;
            case R.id.switch_flash:
                switchFlash();
                break;
            case R.id.iv_back:
                this.finish();
                break;
            case R.id.switch_camera:
                switchCamera();
                break;
            case R.id.delete:
                cancleSavePhoto();
                break;
            case R.id.save:
                System.out.println("--------->开始保存");
                saveBitmapFile(newBitmap);
                System.out.println("--------->刷新");
                new SingleMediaScanner(this, mFile.getAbsolutePath(), new SingleMediaScanner.ScanListener() {
                    @Override public void onScanFinish() {
                        Log.i("SingleMediaScanner", "scan finish!");
                    }
                });
                System.out.println("---------<结束保存");
                Intent intent = new Intent();
                intent.putExtra("imagePath", mFile.getAbsolutePath());
                Uri uri = ImagePathUriUtil.path2Uri(this, mFile.getAbsolutePath());//拍完照插入到数据库
                intent.putExtra("imageUri", uri.toString());
                setResult(Activity.RESULT_OK, intent);
                finish();
                break;
        }
    }
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private void switchCamera() {
        if (mCamera != null) {
            //停止预览
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
        //在Android P之前 Android设备仍然最多只有前后两个摄像头，在Android p后支持多个摄像头 用户想打开哪个就打开哪个
        mCameraId = (mCameraId + 1) % Camera.getNumberOfCameras();
        //打开摄像头
        //是否支持前后摄像头
        boolean isSupportCamera = isSupport(mCameraId);
        //如果支持
        if (isSupportCamera) {
            try {
                openCamera();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    private void openCamera() {
        mCamera = Camera.open(mCameraId);
        cameraPreview = new CameraPreview(this, mCamera);
        cameraPreview.setmCameraId(mCameraId);
        mPreviewLayout.removeAllViews();
        mPreviewLayout.addView(cameraPreview);
    }
    private boolean isSupport(int faceOrBack) {//判断是否支持某个相机
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            //返回相机信息
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == faceOrBack) {
                return true;
            }
        }
        return false;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mOrientationEventListener != null){
            mOrientationEventListener.disable();
        }
        if (mCamera != null) {
            mCamera.release();
        }
    }
}
