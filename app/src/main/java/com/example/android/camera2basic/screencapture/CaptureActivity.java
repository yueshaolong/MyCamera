package com.example.android.camera2basic.screencapture;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
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
import com.bumptech.glide.Glide;
import com.example.android.camera2basic.R;
import com.example.android.camera2basic.camera1.CameraPreview;
import com.example.android.camera2basic.camera1.OverCameraView;
import com.example.android.camera2basic.camera1.SystemUtil;
import com.example.android.camera2basic.camera2.SingleMediaScanner;
import com.example.android.camera2basic.screencapture.FloatWindowsService.Finish;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CaptureActivity extends AppCompatActivity
        implements OnClickListener, OnTouchListener {
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
    /**
     * 聚焦视图
     */
    private OverCameraView mOverCameraView;
    /**
     * 相机类
     */
    private Camera mCamera;
    /**
     * 是否开启闪光灯
     */
    private boolean isFlashing;
    /**
     * 拍照标记
     */
    private boolean isTakePhoto;
    /**
     * 是否正在聚焦
     */
    private boolean isFoucing;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private Bitmap newBitmap;
    private CameraPreview cameraPreview;
    private RelativeLayout rl;
    private FloatWindowsService.MyBinder myBinder;

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
        rl = findViewById(R.id.rl);
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
        findViewById(R.id.preview).setOnTouchListener(this);
        initOrientate();
        requestCapturePermission();
        registerReceiver();
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
    /**
     * 初始化传感器方向
     */
    private void  initOrientate(){
        if(mOrientationEventListener == null){
            mOrientationEventListener = new OrientationEventListener(this) {
                @Override
                public void onOrientationChanged(int orientation) {
                    System.out.println("------->"+orientation);
                    // i的范围是0-359
                    // 屏幕左边在顶部的时候 i = 90;
                    // 屏幕顶部在底部的时候 i = 180;
                    // 屏幕右边在底部的时候 i = 270;
                    // 正常的情况默认i = 0;
                    if(45 <= orientation && orientation < 135){
                        takePhotoOrientation = 180;
                    } else if(135 <= orientation && orientation < 225){
                        takePhotoOrientation = 270;
                    } else if(225 <= orientation && orientation < 315){
                        takePhotoOrientation = 0;
                    } else {
                        takePhotoOrientation = 90;
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
    @Override
    public void onResume() {
        super.onResume();
        openCamera();
    }

    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            isFoucing = false;
            mOverCameraView.setFoucuing(false);
            mOverCameraView.disDrawTouchFocusRect();
            //停止聚焦超时回调
            mHandler.removeCallbacks(mRunnable);
        }
    };
    private void takePhoto() {
        isTakePhoto = true;
        ll_take_photo.setVisibility(View.GONE);
        ll_save_delete.setVisibility(View.VISIBLE);
        Toast.makeText(this,"拍照",Toast.LENGTH_SHORT).show();
        //TODO 执行截屏
        myBinder.start();
    }
    //截屏成功后再onactivityResult回调,截取屏幕显示
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_MEDIA_PROJECTION:
                if (resultCode == RESULT_OK && data != null) {
                    Intent intent = new Intent(getApplicationContext(), FloatWindowsService.class);
                    intent.putExtra("Intent", data);
                    intent.putExtra("barHeight",isNavigationBarShow(this)? getNavigationBarHeight(this):0);
                    intent.putExtra("imagePath",createFile());
                    bindService(intent, new ServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName name, IBinder service) {
                            Log.d(TAG, "onServiceConnected is invoke");
                            myBinder = (FloatWindowsService.MyBinder) service;
                            myBinder.getService().setFinish(new Finish() {
                                @Override
                                public void setImage(Bitmap bitmap) {
                                    newBitmap = bitmap;
                                    ll_take_photo.setVisibility(View.GONE);
                                    ll_photo_message.setVisibility(View.GONE);
                                    ll_save_delete.setVisibility(View.VISIBLE);
                                    mPreviewLayout.setVisibility(View.GONE);
                                    switch_flash.setVisibility(View.GONE);
                                    preview.setVisibility(View.VISIBLE);
                                    preview.setImageBitmap(bitmap);
                                }
                            });
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName name) {

                        }
                    },BIND_AUTO_CREATE);
                }
                break;
        }
    }
    //NavigationBar状态是否是显示
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
    //获取NavigationBar高度
    public static int getNavigationBarHeight(Context context) {
        return getSizeByReflection(context, "navigation_bar_height");
    }
    public static int getSizeByReflection(Context context, String field) {
        int size = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField(field).get(object).toString());
            size = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
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
        mCamera.startPreview();
        isTakePhoto = false;
    }
    public void saveBitmapFile(Bitmap bitmap){
        if (null == bitmap) return;
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(mFile));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
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
                saveBitmapFile(newBitmap);
                new SingleMediaScanner(this, mFile.getAbsolutePath(), new SingleMediaScanner.ScanListener() {
                    @Override public void onScanFinish() {
                        Log.i("SingleMediaScanner", "scan finish!");
                    }
                });
                this.finish();
                break;
        }
    }
    public BroadcastReceiver readReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("onReceive=====>"+intent);
            try {
                if ("com.example.android.camera2basic".equals(intent.getAction())) {
                    Uri uri = intent.getData();
                    ll_take_photo.setVisibility(View.GONE);
                    ll_save_delete.setVisibility(View.VISIBLE);
                    mPreviewLayout.setVisibility(View.GONE);
                    switch_flash.setVisibility(View.GONE);
                    preview.setVisibility(View.VISIBLE);
                    System.out.println("uri=======>"+uri);
                    Glide.with(getApplicationContext()).load(uri).into(preview);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private void switchCamera() {
        if (mCamera != null) {
            //停止预览
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
            mHandler.removeCallbacks(mRunnable);
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
        if (mOverCameraView == null) {
            mOverCameraView = new OverCameraView(this);
        }
        mPreviewLayout.removeAllViews();
        mPreviewLayout.addView(cameraPreview);
        mPreviewLayout.addView(mOverCameraView);
    }
    /**
     * 判断是否支持某个相机
     * @param faceOrBack 前置还是后置
     */
    private boolean isSupport(int faceOrBack) {
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
    private void registerReceiver() {
        IntentFilter readFilter = new IntentFilter();
        readFilter.addAction("com.example.android.camera2basic");
        registerReceiver(readReceiver, readFilter);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mOrientationEventListener != null){
            mOrientationEventListener.disable();
        }
        if (readReceiver != null) {
            unregisterReceiver(readReceiver);
        }
    }

    private static final int MODE_INIT = 0;
    //两个触摸点触摸屏幕状态
    private static final int MODE_ZOOM = 1;
    //标识模式
    private int mode = MODE_INIT;
    private boolean isMove = false;
    //两点的初始距离
    private float startDis;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //无论多少跟手指加进来，都是MotionEvent.ACTION_DWON MotionEvent.ACTION_POINTER_DOWN
        //MotionEvent.ACTION_MOVE:
        switch (v.getId()) {
            case R.id.preview:
                if (!isFoucing) {
                    float x = event.getX();
                    float y = event.getY();
                    isFoucing = true;
                    if (mCamera != null && !isTakePhoto) {
                        mOverCameraView.setTouchFoucusRect(mCamera, autoFocusCallback, x, y);
                    }
                    mRunnable = () -> {
                        isFoucing = false;
                        mOverCameraView.setFoucuing(false);
                        mOverCameraView.disDrawTouchFocusRect();
                    };
                    //设置聚焦超时
                    mHandler.postDelayed(mRunnable, 3000);
                }
                break;
            default:
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    //手指按下屏幕
                    case MotionEvent.ACTION_DOWN:
                        mode = MODE_INIT;
                        break;
                    //当屏幕上已经有触摸点按下的状态的时候，再有新的触摸点被按下时会触发
                    case MotionEvent.ACTION_POINTER_DOWN:
                        mode = MODE_ZOOM;
                        //计算两个手指的距离 两点的距离
                        startDis = SystemUtil.twoPointDistance(event);
                        break;
                    //移动的时候回调
                    case MotionEvent.ACTION_MOVE:
                        isMove = true;
                        //这里主要判断有两个触摸点的时候才触发
                        if (mode == MODE_ZOOM) {
                            //只有两个点同时触屏才执行
                            if (event.getPointerCount() < 2) {
                                return true;
                            }
                            //获取结束的距离
                            float endDis = SystemUtil.twoPointDistance(event);
                            //每变化10f zoom变1
                            int scale = (int) ((endDis - startDis) / 10f);
                            if (scale >= 1 || scale <= -1) {
                                int zoom = cameraPreview.getZoom() + scale;
                                //判断zoom是否超出变焦距离
                                if (zoom > cameraPreview.getMaxZoom()) {
                                    zoom = cameraPreview.getMaxZoom();
                                }
                                //如果系数小于0
                                if (zoom < 0) {
                                    zoom = 0;
                                }
                                //设置焦距
                                cameraPreview.setZoom(zoom);
                                //将最后一次的距离设为当前距离
                                startDis = endDis;
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        //判断是否点击屏幕 如果是自动聚焦
                        if (isMove == false) {
                            //自动聚焦
                            cameraPreview.autoFoucus();
                        }
                        isMove = false;
                        break;
                }
        }
        return true;
    }
}
