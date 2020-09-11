package com.example.android.camera2basic.camera2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;


/**
 * 摄像头帮助类
 */
public class CameraHelper implements ICamera{

//    /**
//     * 设备旋转方向
//     */
//    private int mDeviceRotation;
//
    private int mPhotoRotation;
//
//    private float mLight;
//
//    private AtomicBoolean mIsCameraOpen;
//
    private CameraManager mCameraManager;
//
    private TakePhotoListener mTakePhotoListener;
//
////    private CameraReady mCameraReady;
//
    /**
     * 摄像头的id集合
     */
    private String[] mCameraIds;
//
//    /**
//     * 摄像头支持的最大size
//     */
//    private Size mLargest;
//
//    /**
//     * 可缩放区域
//     */
//    private Size mZoomSize;
//
//    private Size mVideoSize;
//
    private Context mContext;
//
//
//    private MediaRecorder mMediaRecorder;
//
//    private CaptureRequest.Builder mPreviewBuilder;
//
//    private CameraCaptureSession mPreviewSession;
//
//
//    private AtomicBoolean mIsRecordVideo = new AtomicBoolean();
//
//    private CameraType mNowCameraType;
//
//
//
//    /**
//     * 最大的放大倍数
//     */
//    private float mMaxZoom = 0;
//
//    /**
//     * 放大的矩阵，拍照使用
//     */
//    private Rect mZoomRect;
//
//    /**
//     * 摄像头支持的分辨率流集合
//     */
//    private StreamConfigurationMap mMap;
//
    private FlashState mNowFlashState = FlashState.CLOSE;
//
//    private boolean mIsCapture = false;
//
//    private CameraCharacteristics mCharacteristics;
//
//    private boolean mNoAFRun = false;
//
//    private boolean mIsAFRequest = false;
//
    private CameraMode CAMERA_STATE = CameraMode.TAKE_PHOTO;
//
//    private Surface mPreViewSurface;
//
//    private Surface mRecordSurface;
//
//    private CoordinateTransformer mCoordinateTransformer;
//
//    private Rect mPreviewRect;
//    private Rect mFocusRect;

    /**
     * 根据摄像头管理器获取一个帮助类
     *
     * @param context
     * @return
     */
    public CameraHelper(Context context, AutoFitTextureView mTextureView) {
        this.mContext = context;
        this.mTextureView = mTextureView;
//        mIsCameraOpen = new AtomicBoolean(false);
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraIds = mCameraManager.getCameraIdList();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
//        mFocusRect = new Rect();
    }


//    @Override
//    public void cameraZoom(float scale) {
//        if (scale < 1.0f)
//            scale = 1.0f;
//        if (scale <= mMaxZoom) {
//
//            int cropW = (int) ((mZoomSize.getWidth() / (mMaxZoom * 2.6)) * scale);
//            int cropH = (int) ((mZoomSize.getHeight() / (mMaxZoom * 2.6)) * scale);
//
//            Rect zoom = new Rect(cropW, cropH,
//                    mZoomSize.getWidth() - cropW,
//                    mZoomSize.getHeight() - cropH);
//            mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
//            mZoomRect = zoom;
//            updatePreview();   //重复更新预览请求
//        }
//    }
//
//    /**
//     * 获取最大zoom
//     *
//     * @return
//     */
//    public float getMaxZoom() {
//        return mMaxZoom;
//    }
//
//    /**
//     * 初始化拍照的图片读取类
//     */
//    private void initImageReader() {
//        //取最大的分辨率
//        Size largest = Collections.max(Arrays.asList(mMap.getOutputSizes(ImageFormat.JPEG)),
//                new CompareSizesByArea());
//        mZoomSize = largest;
//        //实例化拍照用的图片读取类
//        if (mImageReader != null)
//            mImageReader.close();
//        mImageReader = ImageReader.newInstance(largest.getWidth(),
//                largest.getHeight(), ImageFormat.JPEG, 2);
//    }
//
//    /**
//     * 初始化一个适合的预览尺寸
//     */
//    private void initSize() {
//        Size largest = Collections.max(
//                Arrays.asList(mMap.getOutputSizes(ImageFormat.JPEG)),
//                new CompareSizesByArea());
//
//        Point displaySize = new Point();
//        ((Activity) mContext).getWindowManager().getDefaultDisplay().getSize(displaySize);
//
//        mLargest = chooseOptimalSize(mMap.getOutputSizes(SurfaceTexture.class),
//                this.mTextureView.getWidth(),
//                this.mTextureView.getHeight(),
//                displaySize.x,
//                displaySize.y,
//                largest
//        );
//    }
//
//    @SuppressLint("MissingPermission")
//    @Override
//    public boolean openCamera(CameraType cameraType) {
//
//        if (mIsCameraOpen.get())
//            return true;
//        mIsCameraOpen.set(true);
//        mZoomRect = null;
//        this.mNowCameraType = cameraType;
//        int cameraTypeId;
//        switch (cameraType) {
//            default:
//            case BACK:
//                cameraTypeId = CameraCharacteristics.LENS_FACING_BACK;
//                break;
//            case FRONT:
//                cameraTypeId = CameraCharacteristics.LENS_FACING_FRONT;
//                break;
//            case USB:
//                cameraTypeId = CameraCharacteristics.LENS_FACING_EXTERNAL;
//                break;
//        }
//
//        try {
//            for (String cameraId : mCameraIds) {
//                mCharacteristics = mCameraManager.getCameraCharacteristics(cameraId);
//                Integer facing = mCharacteristics.get(CameraCharacteristics.LENS_FACING);
//                if (facing != null && facing != cameraTypeId) {
//                    continue;
//                }
//
//                Float maxZoom = mCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
//                if (maxZoom != null) {
//                    mMaxZoom = maxZoom.floatValue();
//                }
//
//                //获取摄像头支持的流配置信息
//                mMap = mCharacteristics
//                        .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//                if (mMap == null)
//                    return false;
//                //初始化拍照的图片读取类
//                initImageReader();
//                //初始化尺寸
//                initSize();
//
//                //获取摄像头角度
//                mSensorOrientation = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
//
//                mVideoSize = chooseVideoSize(mMap.getOutputSizes(MediaRecorder.class));
//                if (mTextureView != null) {
//                    ((AutoFitTextureView) mTextureView).setAspectRatio(mLargest.getHeight(), mLargest.getWidth());
//                }
//
//                //检查是否这个摄像头是否支持闪光灯，拍照模式的时候使用
//                Boolean available = mCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
//                mFlashSupported = available == null ? false : available;
//
//                this.mCameraId = cameraId;
//                mPreviewRect = new Rect(0, 0, mTextureView.getWidth(), mTextureView.getHeight());
//                mCoordinateTransformer = new CoordinateTransformer(mCharacteristics, new RectF(mPreviewRect));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return openCamera(mCameraId);
//    }
//
//    @SuppressLint("MissingPermission")
//    private boolean openCamera(String cameraId) {
//        try {
//            mCameraManager.openCamera(cameraId, mStateCallback, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//    }
//
////    @Override
////    public void closeCamera() {
////        Log.e("camera", "关闭摄像头");
////        mIsCameraOpen.set(false);
////
////        closePreviewSession();
////        if (mCameraDevice != null) {
////            mCameraDevice.close();
////            mCameraDevice = null;
////        }
////        if (mImageReader != null) {
////            mImageReader.close();
////            mImageReader = null;
////        }
////    }
//
    int cameraTypeId = CameraCharacteristics.LENS_FACING_BACK;
    @SuppressLint("MissingPermission")
    @Override
    public boolean switchCamera(CameraType cameraType) {
        closeCamera();
        switch (cameraType) {
            case BACK:
                cameraTypeId = CameraCharacteristics.LENS_FACING_BACK;
                break;
            case FRONT:
                cameraTypeId = CameraCharacteristics.LENS_FACING_FRONT;
                break;
            default:
        }
        System.out.println("----->"+cameraTypeId);
        openCamera(mTextureView.getWidth(),mTextureView.getHeight());
//        try {
//            mCameraManager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
        return true;
    }
//
//    @Override
//    public boolean startPreview() {
//        if (mBackgroundHandler == null)
//            return false;
//        try {
//
//            //初始化预览的尺寸
//            initSize();
//
//            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
//            surfaceTexture.setDefaultBufferSize(mLargest.getWidth(), mLargest.getHeight());
//            mPreViewSurface = new Surface(surfaceTexture);
//
//            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);  //创建一个预览请求
//            mPreviewBuilder.addTarget(mPreViewSurface); //添加预览输出目标画面
//
//            if (mZoomRect != null)
//                mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, mZoomRect);   //放大的矩阵
//            mCameraDevice.createCaptureSession(Arrays.asList(mPreViewSurface,
//                    mImageReader.getSurface()),   //当前线程创建一个预览请求
//                    new CameraCaptureSession.StateCallback() {
//                        @Override
//                        public void onConfigured(CameraCaptureSession session) {
//                            mPreviewSession = session;
//                            setup3AControlsLocked(mPreviewBuilder);
//                            updatePreview();   //重复更新预览请求
//                            if (mCameraReady != null)
//                                mCameraReady.onCameraReady();
//                        }
//
//                        @Override
//                        public void onConfigureFailed(CameraCaptureSession session) {
//
//                        }
//                    }, mBackgroundHandler);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//    }
//
    /**
     * 更新预览界面
     */
    private void updatePreview() {
        if (mCameraDevice == null)
            return;
        try {
            //  mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            mCaptureSession.setRepeatingRequest(
                    mPreviewRequestBuilder.build(),
                    null,
                    mBackgroundHandler
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//
//
//    @Override
//    public void resumePreview() {
//        try {
//            if (!mNoAFRun) {
//                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
//                        CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
//                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
//                        CameraMetadata.CONTROL_AF_TRIGGER_IDLE);
//            }
//            if (!isLegacyLocked()) {
//                // Tell the camera to lock focus.
//                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
//                        CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_CANCEL);
//                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
//                        CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE);
//            }
//            mIsAFRequest = false;
//            mCameraState = 0;
//            mPreviewSession.capture(mPreviewBuilder.build(), null,
//                    mBackgroundHandler);
//            updatePreview();
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public boolean startVideoRecord(String path, int mediaType) {
//        if (mIsRecordVideo.get())
//            new Throwable("video record is recording");
//        if (path == null)
//            new Throwable("path can not null");
//        if (mediaType != MediaRecorder.OutputFormat.MPEG_4)
//            new Throwable("this mediaType can not support");
//        if (!setVideoRecordParam(path))
//            return false;
//        startRecordVideo();
//        return true;
//    }
//
//    /**
//     * 设置录像的参数
//     *
//     * @param path
//     * @return
//     */
//    private boolean setVideoRecordParam(String path) {
//        mMediaRecorder = new MediaRecorder();
//
//        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
//        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mMediaRecorder.setOutputFile(path);
//
//        int bitRate = mVideoSize.getWidth() * mVideoSize.getHeight();
//        bitRate = mVideoSize.getWidth() < 1080 ? bitRate * 2 : bitRate;
//
//        mMediaRecorder.setVideoEncodingBitRate(bitRate);
//        mMediaRecorder.setVideoFrameRate(15);
//        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
//        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//
//        mMediaRecorder.setAudioEncodingBitRate(8000);
//        mMediaRecorder.setAudioChannels(1);
//        mMediaRecorder.setAudioSamplingRate(8000);
//        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//
//        if (mNowCameraType == CameraType.BACK)   //后置摄像头图像要旋转90度
//            mMediaRecorder.setOrientationHint(90);
//        else
//            mMediaRecorder.setOrientationHint(270);   //前置摄像头图像要旋转270度
//        try {
//            mMediaRecorder.prepare();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    public void stopVideoRecord() {
//        if (mIsRecordVideo.get())
//            mIsRecordVideo.set(false);
//        else
//            return;
//        mMediaRecorder.stop();
//        mMediaRecorder.reset();
//        mMediaRecorder.release();
//
//        // startPreview();
//    }
//
//    @Override
//    public boolean takePhone(File file, MediaType mediaType) {
//        System.out.println("------------>"+file.getAbsolutePath());
//        this.mFile = file;
//        setTakePhotoFlashMode(mPreviewBuilder);
//        updatePreview();
//        // lockFocus();
//
//        if (!mNoAFRun) {
//
//            if (mIsAFRequest) {
//                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
//                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, AFRegions);
//                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, AERegions);
//            }
//            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
//                    CameraMetadata.CONTROL_AF_TRIGGER_START);
//        }
//        if (!isLegacyLocked()) {
//            // Tell the camera to lock focus.
//            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
//                    CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
//        }
//        mCameraState = WAITING_LOCK;
//        if(!mFlashSupported) {
//            capturePhoto();
//        }else
//        {
//            switch (mNowFlashState)
//            {
//                case CLOSE:
//                    capturePhoto();
//                    break;
//                case OPEN:
//                case AUTO:
//                    mBackgroundHandler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                mPreviewSession.capture(mPreviewBuilder.build(), mCaptureCallback,
//                                        mBackgroundHandler);
//                            } catch (CameraAccessException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }, 800);
//                    break;
//            }
//        }
//        return true;
//    }
//
//
//    private boolean isLegacyLocked() {
//        return mCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) ==
//                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY;
//    }
//
//    private void setup3AControlsLocked(CaptureRequest.Builder builder) {
//        // Enable auto-magical 3A run by camera device
//        builder.set(CaptureRequest.CONTROL_MODE,
//                CaptureRequest.CONTROL_MODE_AUTO);
//
//        Float minFocusDist =
//                mCharacteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
//
//        // If MINIMUM_FOCUS_DISTANCE is 0, lens is fixed-focus and we need to skip the AF run.
//        mNoAFRun = (minFocusDist == null || minFocusDist == 0);
//
//        if (!mNoAFRun) {
//            // If there is a "continuous picture" mode available, use it, otherwise default to AUTO.
//            if (contains(mCharacteristics.get(
//                    CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES),
//                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)) {
//                builder.set(CaptureRequest.CONTROL_AF_MODE,
//                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//            } else {
//                builder.set(CaptureRequest.CONTROL_AF_MODE,
//                        CaptureRequest.CONTROL_AF_MODE_AUTO);
//            }
//        }
//
//        // If there is an auto-magical flash control mode available, use it, otherwise default to
//        // the "on" mode, which is guaranteed to always be available.
//        if (mNowFlashState != FlashState.CLOSE) {
//            if (contains(mCharacteristics.get(
//                    CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES),
//                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)) {
//                builder.set(CaptureRequest.CONTROL_AE_MODE,
//                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
//            } else {
//                builder.set(CaptureRequest.CONTROL_AE_MODE,
//                        CaptureRequest.CONTROL_AE_MODE_ON);
//            }
//        }
//
//        // If there is an auto-magical white balance control mode available, use it.
//        if (contains(mCharacteristics.get(
//                CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES),
//                CaptureRequest.CONTROL_AWB_MODE_AUTO)) {
//            // Allow AWB to run auto-magically if this device supports this
//            builder.set(CaptureRequest.CONTROL_AWB_MODE,
//                    CaptureRequest.CONTROL_AWB_MODE_AUTO);
//        }
//    }
//
//    /**
//     * 真正拍照
//     */
//    private void capturePhoto() {
//        mIsCapture = true;
//        final CaptureRequest.Builder captureBuilder;
//        try {
//            //设置拍照后的回调监听
//            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
//            captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
//            captureBuilder.addTarget(mImageReader.getSurface());
//            //设置自动对焦
//            /*captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
//                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);*/
//            // Use the same AE and AF modes as the preview.
//      /*      if(mNowFlashState != FlashState.CLOSE) {
//                if(mFlashSupported)
//                    setup3AControlsLocked(captureBuilder);
//            }*/
//
//            mPhotoRotation = getOrientation(mDeviceRotation);
//            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, mPhotoRotation);
//
//            if (mZoomRect != null)
//                captureBuilder.set(CaptureRequest.SCALER_CROP_REGION, mZoomRect);   //放大的矩阵
//            setTakePhotoFlashMode(captureBuilder);
//            captureBuilder.setTag(1);
//            mPreviewSession.stopRepeating();
//            mPreviewSession.abortCaptures();
//            mBackgroundHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        mPreviewSession.capture(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
//                            @Override
//                            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
//                            }
//
//                            @Override
//                            public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
//
//                            }
//
//
//                        }, mBackgroundHandler);
//                    } catch (CameraAccessException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }, 200);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public Size getPreViewSize() {
//        return mLargest;
//    }
//
//    @Override
//    public void setSurface(Surface surface) {
//        //this.mSurface = surface;
//    }

//    /**
//     * 如果设置了textureView则不用设置Surface
//     *
//     * @param textureView
//     */
//    @Override
//    public void setTextureView(TextureView textureView) {
//        this.mTextureView = textureView;
//    }

//    @Override
    public void setTakePhotoListener(TakePhotoListener mTakePhotoListener) {
        this.mTakePhotoListener = mTakePhotoListener;
    }
//
//    @Override
//    public void setCameraReady(CameraReady cameraReady) {
//        this.mCameraReady = cameraReady;
//    }
//
    @Override
    public void flashSwitchState(FlashState mFlashState) {
        mNowFlashState = mFlashState;
        if (CAMERA_STATE == CameraMode.TAKE_PHOTO) {
            setTakePhotoFlashMode(mPreviewRequestBuilder);
            updatePreview();
        }
    }
//
//    @Override
//    public void setCameraState(CameraMode cameraMode) {
//        CAMERA_STATE = cameraMode;
//        if (CAMERA_STATE == CameraMode.TAKE_PHOTO) {
//            setTakePhotoFlashMode(mPreviewBuilder);
//            updatePreview();
//        }
//    }
//
//    private void toFocusRect(RectF rectF) {
//        mFocusRect.left = Math.round(rectF.left);
//        mFocusRect.top = Math.round(rectF.top);
//        mFocusRect.right = Math.round(rectF.right);
//        mFocusRect.bottom = Math.round(rectF.bottom);
//    }
//
//    private MeteringRectangle calcTapAreaForCamera2(int areaSize, int weight, float x, float y) {
//        int left = clamp((int) x - areaSize / 2,
//                mPreviewRect.left, mPreviewRect.right - areaSize);
//        int top = clamp((int) y - areaSize / 2,
//                mPreviewRect.top, mPreviewRect.bottom - areaSize);
//        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
//        toFocusRect(mCoordinateTransformer.toCameraSpace(rectF));
//        return new MeteringRectangle(mFocusRect, weight);
//    }
//
//    private MeteringRectangle[] AFRegions;
//    private MeteringRectangle[] AERegions;
//
//    @Override
//    public void requestFocus(float x, float y) {
//        mIsAFRequest = true;
//        MeteringRectangle rect = calcTapAreaForCamera2(
//                mTextureView.getWidth() / 5,
//                1000, x, y);
//
//        AFRegions = new MeteringRectangle[]{rect};
//        AERegions = new MeteringRectangle[]{rect};
//
//        Log.e("AFRegions", "AFRegions:" + AFRegions[0].toString());
//
//        try {
//            final CaptureRequest.Builder mFocusBuilder =
//                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//
//            mFocusBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
//            mFocusBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, AFRegions);
//            mFocusBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, AERegions);
//            if(mZoomRect != null)
//                mFocusBuilder.set(CaptureRequest.SCALER_CROP_REGION,mZoomRect);
//
//            mFocusBuilder.addTarget(mPreViewSurface);
//
//            if(CAMERA_STATE == CameraMode.RECORD_VIDEO) {
//                if(mRecordSurface != null) {
//                    mFocusBuilder.addTarget(mRecordSurface);
//                    setRecordVideoFlashMode(mFocusBuilder);
//                }
//            }
//
//            mPreviewSession.setRepeatingRequest(mFocusBuilder.build(),
//                    null, mBackgroundHandler);
//
//            //      mFocusBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
//            mFocusBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
//
//            mPreviewSession.capture(mFocusBuilder.build(),
//                    null, mBackgroundHandler);
//
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }

//    /**
//     * 开启后台线程
//     */
//    public void startBackgroundThread() {
//        mBackgroundThread = new HandlerThread(CameraHelper.class.getSimpleName());
//        mBackgroundThread.start();
//        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
//    }

//    /**
//     * 停止后台进程
//     */
//    public void stopBackgroundThread() {
//        if (mBackgroundThread != null)
//            mBackgroundThread.quitSafely();
//        try {
//            if (mBackgroundThread != null)
//                mBackgroundThread.join();
//            mBackgroundThread = null;
//            mBackgroundHandler = null;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

    private void setTakePhotoFlashMode(CaptureRequest.Builder builder) {
        System.out.println("mFlashSupported----->"+mFlashSupported);
        if (!mFlashSupported) return;
        switch (mNowFlashState) {
            case CLOSE:
                builder.set(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON);
                builder.set(CaptureRequest.FLASH_MODE,
                        CaptureRequest.FLASH_MODE_OFF);
                break;
            case OPEN:
                builder.set(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                break;
            case AUTO:
                builder.set(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                Log.e("mode", "自动闪光灯");
                break;
        }
    }
//
//    private void setRecordVideoFlashMode(CaptureRequest.Builder builder) {
//        if (!mFlashSupported)
//            return;
//        switch (mNowFlashState) {
//            case CLOSE:
//                builder.set(CaptureRequest.FLASH_MODE,
//                        CaptureRequest.FLASH_MODE_OFF);
//                break;
//            case OPEN:
//                builder.set(CaptureRequest.FLASH_MODE,
//                        CaptureRequest.FLASH_MODE_TORCH);
//                break;
//            case AUTO:
//                if (mLight < 10.0f) {
//                    builder.set(CaptureRequest.FLASH_MODE,
//                            CaptureRequest.FLASH_MODE_TORCH);
//                }
//                break;
//        }
//    }
//
//    /**
//     * 设置光线强度
//     */
//    public void setLight(float light) {
//        this.mLight = light;
//    }
//
//    /**
//     * 开始录像
//     */
//    private void startRecordVideo() {
//        try {
//            closePreviewSession();
//
//            //录像的时候 取最大的分辨率
//            mLargest = Collections.max(Arrays.asList(mMap.getOutputSizes(SurfaceTexture.class)),
//                    new CompareSizesByArea());
//
//            if (mCameraDevice == null) return;
//
//            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
//
//            setRecordVideoFlashMode(mPreviewBuilder);
//
//            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
//            surfaceTexture.setDefaultBufferSize(mLargest.getWidth(), mLargest.getHeight());
//           /* if(mSurface != null)
//                mSurface.release();*/
//            mPreViewSurface = new Surface(surfaceTexture);
//
//            mPreviewBuilder.addTarget(mPreViewSurface);
//            mRecordSurface = mMediaRecorder.getSurface();
//            mPreviewBuilder.addTarget(mRecordSurface);
//            List<Surface> surfaceList = new ArrayList<>();
//            surfaceList.add(mPreViewSurface);
//            surfaceList.add(mRecordSurface);
//            if (mZoomRect != null)
//                mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, mZoomRect);   //放大的矩阵
//            mCameraDevice.createCaptureSession(surfaceList, new CameraCaptureSession.StateCallback() {
//                @Override
//                public void onConfigured(CameraCaptureSession session) {
//                    mPreviewSession = session;
//                    updatePreview();
//                    mIsRecordVideo.set(true);
//                    mMediaRecorder.start();
//                }
//
//                @Override
//                public void onConfigureFailed(CameraCaptureSession session) {
//
//                }
//            }, mBackgroundHandler);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void closePreviewSession() {
//        if (mPreviewSession != null) {
//            mPreviewSession.close();
//            mPreviewSession = null;
//        }
//    }

//    /**
//     * 释放资源
//     */
//    public void destroy() {
//        //mSurface.release();
//    }

//    private int getOrientation(int rotation) {
//        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
//    }

//    /**
//     * 设置当前相机位置
//     *
//     * @param rotation
//     */
//    public void setDeviceRotation(int rotation) {
//        this.mDeviceRotation = rotation;
//    }

//    /**
//     * 异步保存照片
//     */
//    private class PhotoSaver implements Runnable {
//
//        /**
//         * 图片文件
//         */
//        private File mFile;
//
//        /**
//         * 拍照的图片
//         */
//        private Image mImage;
//
//        public PhotoSaver(Image image, File file) {
//            this.mImage = image;
//            this.mFile = file;
//        }
//
//        @Override
//        public void run() {
//            ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
//            byte[] buffer = new byte[byteBuffer.remaining()];
//            byteBuffer.get(buffer);
//            FileOutputStream fileOutputStream = null;
//            try {
//                System.out.println("======>"+mFile.getAbsolutePath());
//                fileOutputStream = new FileOutputStream(mFile);
//                fileOutputStream.write(buffer);
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                mImage.close();
//                byteBuffer.clear();
//                if (fileOutputStream != null) {
//                    try {
//                        fileOutputStream.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                if (mTakePhotoListener != null)
//                    mTakePhotoListener.onTakePhotoFinish(mFile, mPhotoRotation,0, 0);
//                resumePreview();
//            }
//        }
//    }


//    private static final int WAITING_LOCK = 1;
//    private int mCameraState = 0;
//    /**
//     * 拍照的有效回调
//     */
//    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
//        @Override
//        public void onImageAvailable(ImageReader reader) {
//            if (mIsCapture) {
//                Image image = reader.acquireNextImage();
//                new Thread(new PhotoSaver(image, mFile)).start();
//                mIsCapture = false;
//            }
//        }
//    };

//    /**
//     * 打开摄像头状态回调
//     */
//    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
//        @Override
//        public void onOpened(CameraDevice camera) {
//            mCameraDevice = camera;
//            startPreview();
//        }
//
//        @Override
//        public void onDisconnected(CameraDevice camera) {
//            camera.close();
//            mCameraDevice = null;
//        }
//
//        @Override
//        public void onError(CameraDevice camera, int error) {
//            camera.close();
//            mCameraDevice = null;
//        }
//    };

//    private static Size chooseVideoSize(Size[] choices) {
//        for (Size size : choices) {
//            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
//                return size;
//            }
//        }
//        return choices[choices.length - 1];
//    }


//    /**
//     * 选择一个适合的预览尺寸，不然有一些机型不支持
//     *
//     * @param choices
//     * @param textureViewWidth
//     * @param textureViewHeight
//     * @param maxWidth
//     * @param maxHeight
//     * @param aspectRatio
//     * @return
//     */
//    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
//                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {
//
//        // Collect the supported resolutions that are at least as big as the preview Surface
//        List<Size> bigEnough = new ArrayList<>();
//        // Collect the supported resolutions that are smaller than the preview Surface
//        List<Size> notBigEnough = new ArrayList<>();
//        int w = aspectRatio.getWidth();
//        int h = aspectRatio.getHeight();
//        for (Size option : choices) {
//            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
//                    option.getHeight() == option.getWidth() * h / w) {
//                if (option.getWidth() >= textureViewWidth &&
//                        option.getHeight() >= textureViewHeight) {
//                    bigEnough.add(option);
//                } else {
//                    notBigEnough.add(option);
//                }
//            }
//        }
//
//        // Pick the smallest of those big enough. If there is no one big enough, pick the
//        // largest of those not big enough.
//        if (bigEnough.size() > 0) {
//            return Collections.min(bigEnough, new CompareSizesByArea());
//        } else if (notBigEnough.size() > 0) {
//            return Collections.max(notBigEnough, new CompareSizesByArea());
//        } else {
//            return choices[0];
//        }
//    }

//    /**
//     * Compares two {@code Size}s based on their areas.
//     */
//    static class CompareSizesByArea implements Comparator<Size> {
//
//        @Override
//        public int compare(Size lhs, Size rhs) {
//            // We cast here to ensure the multiplications won't overflow
//            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
//                    (long) rhs.getWidth() * rhs.getHeight());
//        }
//
//    }

//    /**
//     * Return true if the given array contains the given integer.
//     *
//     * @param modes array to check.
//     * @param mode  integer to get for.
//     * @return true if the array contains the given integer, otherwise false.
//     */
//    private static boolean contains(int[] modes, int mode) {
//        if (modes == null) {
//            return false;
//        }
//        for (int i : modes) {
//            if (i == mode) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private static int clamp(int x, int min, int max) {
//        if (x > max) {
//            return max;
//        }
//        if (x < min) {
//            return min;
//        }
//        return x;
//    }

//-------------------------------------------------------------------------------------------------------------------------------

    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();


    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * Camera state: Showing camera preview.
     */
    private static final int STATE_PREVIEW = 0;

    /**
     * Camera state: Waiting for the focus to be locked.
     */
    private static final int STATE_WAITING_LOCK = 1;

    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    private static final int STATE_WAITING_PRECAPTURE = 2;

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;
    /**
     * Camera state: Picture was taken.
     */
    private static final int STATE_PICTURE_TAKEN = 4;

    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    public final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    /**
     * ID of the current {@link CameraDevice}.
     */
    private String mCameraId;

    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView mTextureView;

    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    private CameraCaptureSession mCaptureSession;

    /**
     * A reference to the opened {@link CameraDevice}.
     */
    private CameraDevice mCameraDevice;

    /**
     * The {@link android.util.Size} of camera preview.
     */
    private Size mPreviewSize;

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = (Activity) mContext;
            if (null != activity) {
                activity.finish();
            }
        }

    };

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    /**
     * An {@link ImageReader} that handles still image capture.
     */
    private ImageReader mImageReader;

    /**
     * This is the output file for our picture.
     */
    private File mFile;
    public void setmFile(File mFile) {
        this.mFile = mFile;
    }

    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
        }

    };

    /**
     * {@link CaptureRequest.Builder} for the camera preview
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;

    /**
     * {@link CaptureRequest} generated by {@link #mPreviewRequestBuilder}
     */
    private CaptureRequest mPreviewRequest;

    /**
     * The current state of camera state for taking pictures.
     *
     * @see #mCaptureCallback
     */
    private int mState = STATE_PREVIEW;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * Whether the current camera device supports Flash or not.
     */
    private boolean mFlashSupported;

    /**
     * Orientation of the camera sensor
     */
    private int mSensorOrientation;

    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
     */
    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
                    // We have nothing to do when the camera preview is working normally.
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }

    };
    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended output
     *                          class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param aspectRatio       The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e("cameraHelper", "Couldn't find any suitable preview size");
            return choices[0];
        }
    }
    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private void setUpCameraOutputs(int width, int height) {
        Activity activity = (Activity) mContext;
//        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : mCameraIds) {
                CameraCharacteristics characteristics
                        = mCameraManager.getCameraCharacteristics(cameraId);

//                // We don't use a front facing camera in this sample.
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    System.out.println(cameraId+"-----"+facing+"============"+cameraTypeId);
                if (facing != null && !facing.equals(cameraTypeId)) {
                    continue;
                }

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                // For still image captures, we use the largest available size.
                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, /*maxImages*/2);
                mImageReader.setOnImageAvailableListener(
                        mOnImageAvailableListener, mBackgroundHandler);

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                //noinspection ConstantConditions
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.e("cameraHelper", "Display rotation is invalid: " + displayRotation);
                }

                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                int orientation = mContext.getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }

                // Check if the flash is supported.
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
//            ErrorDialog.newInstance(mContext.getString(R.string.camera_error))
//                    .show(((FragmentActivity)mContext).getChildFragmentManager(), FRAGMENT_DIALOG);
        }
    }

    /**
     * Opens the camera specified by {@link CameraHelper#mCameraId}.
     */
    public void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
//            requestCameraPermission();
            return;
        }
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
//        Activity activity = (Activity) mContext;
//        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            System.out.println("-----》"+mCameraId);
            mCameraManager.openCamera(mCameraId+"", mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    public void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    public void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    public void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    public void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // Flash is automatically enabled when necessary.
                                setAutoFlash(mPreviewRequestBuilder);

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
//                            showToast("Failed");
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = (Activity) mContext;
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }
    /**
     * Lock the focus as the first step for a still image capture.
     */
    public void lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in {@link #mCaptureCallback} from {@link #lockFocus()}.
     */
    public void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * {@link #mCaptureCallback} from both {@link #lockFocus()}.
     */
    public void captureStillPicture() {
        try {
            final Activity activity = (Activity) mContext;
            if (null == activity || null == mCameraDevice) {
                return;
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            setAutoFlash(captureBuilder);

            // Orientation
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            mPhotoRotation = getOrientation(rotation);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

            CameraCaptureSession.CaptureCallback CaptureCallback
                    = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
//                    showToast("Saved: " + mFile);
                    Log.d("cameraHelper", mFile.toString());
                    unlockFocus();
                }
            };

            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    public int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            setAutoFlash(mPreviewRequestBuilder);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        if (mFlashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        }
    }

    /**
     * Saves a JPEG {@link Image} into the specified {@link File}.
     */
    private class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;

        ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // 获得图片
                Bitmap mBitmap = BitmapFactory.decodeFile(mFile.getPath());
                //添加时间水印
                Bitmap newBitmap = AddTimeWatermark(mBitmap);
                saveBitmapFile(newBitmap);

                if (mTakePhotoListener != null)
                    mTakePhotoListener.onTakePhotoFinish(mFile, mPhotoRotation,0, 0);
            }
        }
    }
    public void saveBitmapFile(Bitmap bitmap){
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
        //获取原始图片与水印图片的宽与高
        int mBitmapWidth = mBitmap.getWidth();
        int mBitmapHeight = mBitmap.getHeight();
        Bitmap mNewBitmap = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(mNewBitmap);
        //向位图中开始画入MBitmap原始图片
        mCanvas.drawBitmap(mBitmap,0,0,null);
        //添加文字
        Paint mPaint = new Paint();
        String mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss EEEE").format(new Date());
        //String mFormat = TingUtils.getTime()+"\n"+" 纬度:"+GpsService.latitude+"  经度:"+GpsService.longitude;
        mPaint.setColor(Color.RED);
        mPaint.setTextSize(sp2px(mContext,50));
        //水印的位置坐标
        mPaint.setTextAlign(Paint.Align.CENTER);
        //根据路径得到Typeface
        Typeface typeface=Typeface.createFromAsset(mContext.getAssets(), "fonts/xs.ttf");
        mPaint.setTypeface(typeface);
        mCanvas.rotate(-45, (mBitmapWidth * 1) / 2, (mBitmapHeight * 1) / 2);
        mCanvas.drawText(mFormat, (mBitmapWidth * 1) / 2, (mBitmapHeight * 1) / 2, mPaint);
        mCanvas.save();
        mCanvas.restore();

        return mNewBitmap;
    }
    public int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }


}
