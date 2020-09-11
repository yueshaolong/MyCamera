package com.example.android.camera2basic.jieping;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.android.camera2basic.R;
import com.example.android.camera2basic.camera1.CameraPreview;
import com.example.android.camera2basic.camera1.OverCameraView;
import com.example.android.camera2basic.camera1.SystemUtil;
import com.example.android.camera2basic.camera2.SingleMediaScanner;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

public class JiePingFragment extends Fragment
        implements View.OnClickListener, OnTouchListener{

    private static final String TAG = "Camera1BasicFragment";
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";
    private ImageView preview;
    private LinearLayout ll_take_photo;
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
     * 图片流暂存
     */
    private byte[] imageData;
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
    private Parameters mParameters;
    private CameraPreview cameraPreview;
    private RelativeLayout rl;

    public static JiePingFragment newInstance() {
        return new JiePingFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera1_basic, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        rl = view.findViewById(R.id.rl);
        mPreviewLayout = view.findViewById(R.id.camera_preview_layout);
        preview = view.findViewById(R.id.preview);
        ll_take_photo = view.findViewById(R.id.ll_take_photo);
        ll_save_delete = view.findViewById(R.id.ll_save_delete);
        switch_flash = view.findViewById(R.id.switch_flash);
        ImageView iv_back = view.findViewById(R.id.iv_back);
        ImageView take_photo = view.findViewById(R.id.take_photo);
        ImageView switch_camera = view.findViewById(R.id.switch_camera);
        ImageView delete = view.findViewById(R.id.delete);
        ImageView save = view.findViewById(R.id.save);
        view.findViewById(R.id.switch_flash).setOnClickListener(this);
        view.findViewById(R.id.iv_back).setOnClickListener(this);
        view.findViewById(R.id.take_photo).setOnClickListener(this);
        view.findViewById(R.id.switch_camera).setOnClickListener(this);
        view.findViewById(R.id.delete).setOnClickListener(this);
        view.findViewById(R.id.save).setOnClickListener(this);
        view.findViewById(R.id.preview).setOnTouchListener(this);
        initOrientate();
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
            mOrientationEventListener = new OrientationEventListener(getActivity()) {
                @Override
                public void onOrientationChanged(int orientation) {
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getScreenBrightness();
//        cameraPreview.setOnTouchListener(this);
    }

    /**
     * 加入调整亮度
     */
    private void getScreenBrightness() {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        //screenBrightness的值是0.0-1.0 从0到1.0 亮度逐渐增大 如果是-1，那就是跟随系统亮度
        lp.screenBrightness = Float.valueOf(200) * (1f / 255f);
        getActivity().getWindow().setAttributes(lp);
    }
    private void createFile() {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .getAbsoluteFile() + File.separator + "Camera2Basic";
        File mIVMSFolder = new File(path);
        if (!mIVMSFolder.exists()) {
            mIVMSFolder.mkdirs();
        }
        fileName = System.currentTimeMillis() + ".jpg";
        mFile = new File(mIVMSFolder.getAbsolutePath(), fileName);
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
        //调用相机拍照
        mCamera.takePicture(null, null, null, (data, camera1) -> {
            imageData = data;
            //停止预览
            mCamera.stopPreview();
            getPhoto();
        });
    }
    private void getPhoto() {
        createFile();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mFile);
            fos.write(imageData);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                    // 获得图片
                    Bitmap mBitmap = BitmapFactory.decodeFile(mFile.getPath());
                    //添加时间水印
                    newBitmap = AddTimeWatermark(mBitmap);
//                    Glide.with(getContext()).load(newBitmap).into(preview);
                    setPreview(newBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void switchFlash() {
        isFlashing = !isFlashing;
        switch_flash.setImageResource(isFlashing ? R.drawable.flash_open : R.drawable.flash_close);
        try {
            Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(isFlashing ? Parameters.FLASH_MODE_TORCH : Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            Toast.makeText(getActivity(), "该设备不支持闪光灯", Toast.LENGTH_SHORT);
        }
    }
    private void cancleSavePhoto() {
        ll_take_photo.setVisibility(View.VISIBLE);
        ll_save_delete.setVisibility(View.GONE);
        mPreviewLayout.setVisibility(View.VISIBLE);
        switch_flash.setVisibility(View.VISIBLE);
        if (mFile.exists()) mFile.delete();
        preview.setVisibility(View.GONE);
        //开始预览
        mCamera.startPreview();
        imageData = null;
        isTakePhoto = false;
    }
    private void setPreview(Bitmap bitmap) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ll_take_photo.setVisibility(View.GONE);
                ll_save_delete.setVisibility(View.VISIBLE);
                mPreviewLayout.setVisibility(View.GONE);
                switch_flash.setVisibility(View.GONE);
                preview.setVisibility(View.VISIBLE);
                preview.setImageBitmap(bitmap);
//                Glide.with(getContext()).load(bitmap).into(preview);
            }
        });
    }
    public Uri getUriFromFile(Context context, File file){
        if (Build.VERSION.SDK_INT >= 24) {
            return FileProvider.getUriForFile(context,getContext().getPackageName()+".provider", file);
        } else {
            return Uri.fromFile(file);
        }
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
    private Bitmap AddTimeWatermark(Bitmap mBitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(Float.valueOf(takePhotoOrientation));
        if(mCameraId == 1){
            if(takePhotoOrientation == 90){
                matrix.postRotate(180f);
            }
        }
        //获取原始图片与水印图片的宽与高
        Bitmap mNewBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
                mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
        //新增 如果是前置 需要镜面翻转处理
        if(mCameraId == 1){
            Matrix matrix1 = new Matrix();
            matrix1.postScale(-1f,1f);
            mNewBitmap = Bitmap.createBitmap(mNewBitmap, 0, 0,
                    mNewBitmap.getWidth(), mNewBitmap.getHeight(), matrix1, true);
        }

        Bitmap mNewBitmap2 = Bitmap.createBitmap(mNewBitmap.getWidth(), mNewBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(mNewBitmap2);
        //向位图中开始画入MBitmap原始图片
        mCanvas.drawBitmap(mNewBitmap,0,0,null);
        //添加文字
        Paint mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(dp2px(getActivity(),40));

        //水印的位置坐标
//        mPaint.setTextAlign(Paint.Align.LEFT);
        //根据路径得到Typeface
//        Typeface typeface=Typeface.createFromAsset(getActivity().getAssets(), "fonts/xs.ttf");
//        mPaint.setTypeface(typeface);
//        mCanvas.rotate(-45, (mNewBitmap2.getWidth() * 1) / 2, (mNewBitmap2.getHeight() * 1) / 2);
//        mCanvas.drawText(mFormat, (mNewBitmap.getWidth() * 1) / 2, (mNewBitmap.getHeight() * 1) / 2, mPaint);
        String mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss EEEE").format(new Date());
        mCanvas.drawText(mFormat, dp2px(getActivity(),20),
                mNewBitmap.getHeight()-dp2px(getActivity(),100), mPaint);
        //矩形背景
        Paint bgRect=new Paint();
        bgRect.setStyle(Paint.Style.FILL);
        bgRect.setColor(Color.YELLOW);
        RectF rectF=new RectF(200, 200, 800, 600);
        mCanvas.drawRect(rectF, bgRect);

        mFormat = "位置 "+" 纬度:"+123+"  经度:"+456;
        mPaint.setColor(Color.RED);
        mPaint.setTextSize(dp2px(getActivity(),30));
        mCanvas.drawText(mFormat, dp2px(getActivity(),60),
                mNewBitmap.getHeight()-dp2px(getActivity(),50), mPaint);

        Bitmap resource = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.ic_save);
        int width = resource.getWidth();
        int height = resource.getHeight();
        // 设置想要的大小
        int newWidth = dp2px(getActivity(),30);
        int newHeight = dp2px(getActivity(),30);
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix2 = new Matrix();
        matrix2.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        resource = Bitmap.createBitmap(resource, 0, 0, width, height, matrix2, true);
        mCanvas.drawBitmap(resource, dp2px(getActivity(),20),
                mNewBitmap.getHeight()-dp2px(getActivity(),80), null);


        mCanvas.save();
        mCanvas.restore();
        return mNewBitmap2;
    }

    public int sp2px(Context context, float spValue) {
//        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
//        return (int) (spValue * fontScale + 0.5f);
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }
    public static int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
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
                getActivity().finish();
                break;
            case R.id.switch_camera:
                switchCamera();
                break;
            case R.id.delete:
                cancleSavePhoto();
                break;
            case R.id.save:
                saveBitmapFile(newBitmap);
                new SingleMediaScanner(getContext(), mFile.getAbsolutePath(), new SingleMediaScanner.ScanListener() {
                    @Override public void onScanFinish() {
                        Log.i("SingleMediaScanner", "scan finish!");
                    }
                });
                getActivity().finish();
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
        cameraPreview = new CameraPreview(getActivity(), mCamera);
        cameraPreview.setmCameraId(mCameraId);
        if (mOverCameraView == null) {
            mOverCameraView = new OverCameraView(getActivity());
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mOrientationEventListener != null){
            mOrientationEventListener.disable();
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
