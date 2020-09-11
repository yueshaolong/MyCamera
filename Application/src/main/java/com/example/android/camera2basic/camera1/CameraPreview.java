package com.example.android.camera2basic.camera1;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private boolean isPreview;
    //摄像头Id 默认后置 0,前置的值是1
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private Camera.Parameters parameters;

    public void setmCameraId(int mCameraId) {
        this.mCameraId = mCameraId;
    }
    private int mDisplayOrientation;
    public CameraPreview(Context context, Camera mCamera) {
        super(context);
        this.mCamera = mCamera;
        this.mHolder = getHolder();
        this.mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mDisplayOrientation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            initParameters();
            //把这个预览效果展示在SurfaceView上面
            mCamera.setPreviewDisplay(holder);
            //调整预览角度
            setCameraDisplayOrientation(mCameraId,mCamera);
            //开启预览效果
            mCamera.startPreview();
            isPreview = true;
        } catch (Exception e) {
            Log.e("CameraPreview", "相机预览错误: " + e.getMessage());
        }
    }
    private void initParameters() {
        //设置设备高宽比
//            mAspectRatio = getDeviceAspectRatio((Activity) context);
//            //设置预览方向
//            mCamera.setDisplayOrientation(getDisplayOrientation(mCameraId));
        parameters = mCamera.getParameters();
        //获取所有支持的预览尺寸
//            mPreviewSizes.clear();
//            for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
//                int width = Math.min(size.width, size.height);
//                int heigth = Math.max(size.width, size.height);
//                mPreviewSizes.add(new Size(width, heigth));
//            }
        //获取所有支持的图片尺寸
//            mPictureSizes.clear();
//            for (Camera.Size size : parameters.getSupportedPictureSizes()) {
//                int width = Math.min(size.width, size.height);
//                int heigth = Math.max(size.width, size.height);
//                mPictureSizes.add(new Size(width, heigth));
//            }
//            Size previewSize = chooseOptimalSize(mPreviewSizes.sizes(mAspectRatio));
//            Size pictureSize = mPictureSizes.sizes(mAspectRatio).last();
        //设置相机参数
//            parameters.setPreviewSize(Math.max(previewSize.getWidth(), previewSize.getHeight()), Math.min(previewSize.getWidth(), previewSize.getHeight()));
//            parameters.setPictureSize(Math.max(pictureSize.getWidth(), pictureSize.getHeight()), Math.min(pictureSize.getWidth(), pictureSize.getHeight()));
        parameters.setPictureFormat(ImageFormat.JPEG);
//        parameters.setRotation(getDisplayOrientation(mCameraId));
        if (isSupportFocus(parameters, Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (isSupportFocus(parameters,Camera.Parameters.FOCUS_MODE_AUTO)) {
            //自动对焦(单次)
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        setPreviewSize(getMeasuredWidth(),getMeasuredHeight());
        setPictureSize();
        mCamera.setParameters(parameters);
    }
    private void setCameraDisplayOrientation(int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        //rotation是预览Window的旋转方向，对于手机而言，当在清单文件设置Activity的screenOrientation="portait"时，
        //rotation=0，这时候没有旋转，当screenOrientation="landScape"时，rotation=1。
        int degrees = 0;
        switch (mDisplayOrientation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        //计算图像所要旋转的角度
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        mDisplayOrientation = result;
        //调整预览图像旋转角度
        camera.setDisplayOrientation(result);
    }
    private void setPictureSize() {
        List<Camera.Size> localSizes = parameters.getSupportedPictureSizes();
        Camera.Size biggestSize = null;
        Camera.Size fitSize = null;// 优先选预览界面的尺寸
        Camera.Size previewSize = parameters.getPreviewSize();//获取预览界面尺寸
        float previewSizeScale = 0;
        if (previewSize != null) {
            previewSizeScale = previewSize.width / (float) previewSize.height;
        }

        if (localSizes != null) {
            int cameraSizeLength = localSizes.size();
            for (int n = 0; n < cameraSizeLength; n++) {
                Camera.Size size = localSizes.get(n);
                if (biggestSize == null) {
                    biggestSize = size;
                } else if (size.width >= biggestSize.width && size.height >= biggestSize.height) {
                    biggestSize = size;
                }

                // 选出与预览界面等比的最高分辨率
                if (previewSizeScale > 0
                        && size.width >= previewSize.width && size.height >= previewSize.height) {
                    float sizeScale = size.width / (float) size.height;
                    if (sizeScale == previewSizeScale) {
                        if (fitSize == null) {
                            fitSize = size;
                        } else if (size.width >= fitSize.width && size.height >= fitSize.height) {
                            fitSize = size;
                        }
                    }
                }
            }

            // 如果没有选出fitSize, 那么最大的Size就是FitSize
            if (fitSize == null) {
                fitSize = biggestSize;
            }
            parameters.setPictureSize(fitSize.width, fitSize.height);
        }

    }
    public void setPreviewSize(int width,int height) {
        //获取系统支持预览大小
        List<Camera.Size> localSizes = parameters.getSupportedPreviewSizes();
        Camera.Size biggestSize = null;//最大分辨率
        Camera.Size fitSize = null;// 优先选屏幕分辨率
        Camera.Size targetSize = null;// 没有屏幕分辨率就取跟屏幕分辨率相近(大)的size
        Camera.Size targetSiz2 = null;// 没有屏幕分辨率就取跟屏幕分辨率相近(小)的size
        if (localSizes != null) {
            int cameraSizeLength = localSizes.size();

            if(Float.valueOf(width) / height == 3.0f / 4){
                for (int n = 0; n < cameraSizeLength; n++) {
                    Camera.Size size = localSizes.get(n);
                    //  Log.d("sssd-系统支持的尺寸size.width:",size.width + "*" +size.height);
                    //  Log.d("sssd-系统",1440f / 1080+"");
                    //  Log.d("sssd-系统支持的尺寸比:",Double.valueOf(size.width) / size.height+"");
                    if(Float.valueOf(size.width) / size.height == 4.0f / 3){
                        Log.d("sssd-系统支持的尺寸:","进入");
                        parameters.setPreviewSize(size.width,size.height);
                        break;
                    }


                }
            } else {
                for (int n = 0; n < cameraSizeLength; n++) {
                    Camera.Size size = localSizes.get(n);
                    Log.d("sssd-系统支持的尺寸:",size.width + "*" +size.height);
                    if (biggestSize == null ||
                            (size.width >= biggestSize.width && size.height >= biggestSize.height)) {
                        biggestSize = size;
                    }

                    //如果支持的比例都等于所获取到的宽高
                    if (size.width == height
                            && size.height == width) {
                        fitSize = size;
                        //如果任一宽或者高等于所支持的尺寸
                    } else if (size.width == height
                            || size.height == width) {
                        if (targetSize == null) {
                            targetSize = size;
                            //如果上面条件都不成立 如果任一宽高小于所支持的尺寸
                        } else if (size.width < height
                                || size.height < width) {
                            targetSiz2 = size;
                        }
                    }
                }

                if (fitSize == null) {
                    fitSize = targetSize;
                }

                if (fitSize == null) {
                    fitSize = targetSiz2;
                }

                if (fitSize == null) {
                    fitSize = biggestSize;
                }
                Log.d("sssd-最佳预览尺寸:",fitSize.width + "*" + fitSize.height);

                //mParameters.setPreviewSize(640,480);
                parameters.setPreviewSize(fitSize.width, fitSize.height);
            }





        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (holder.getSurface() == null) {
            return;
        }
        //停止预览效果
        mCamera.stopPreview();
        //重新设置预览效果
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            if (isPreview) {
                //正在预览
                mCamera.stopPreview();
                mCamera.release();
            }
        }
    }

    private boolean isSupportFocus(Camera.Parameters parameters, String focusMode) {
        boolean isSupport = false;
        //获取所支持对焦模式
        List<String> listFocus = parameters.getSupportedFocusModes();
        for (String s : listFocus) {
            //如果存在 返回true
            if (s.equals(focusMode)) {
                isSupport = true;
            }
        }
        return isSupport;
    }


    public void setZoom(int zoom){
        if(mCamera == null){
            return;
        }
        //获取Paramters对象
        Camera.Parameters parameters;
        parameters = mCamera.getParameters();
        //如果不支持变焦
        if(!parameters.isZoomSupported()){
            return;
        }
        //
        parameters.setZoom(zoom);
        //Camera对象重新设置Paramters对象参数
        mCamera.setParameters(parameters);
        mZoom = zoom;
    }
    private int mZoom;
    public int getZoom(){
        return mZoom;
    }
    public int getMaxZoom(){
        if(mCamera == null){
            return -1;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if(!parameters.isZoomSupported()){
            return -1;
        }
        return parameters.getMaxZoom() > 50 ? 50 : parameters.getMaxZoom();
    }
    public void autoFoucus(){
        if(mCamera == null){
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {

                }
            });
        }
    }
}
