package com.example.android.camera2basic.camera1;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.allen.library.SuperTextView;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.android.camera2basic.R;
import com.example.android.camera2basic.camera1.BitmapManager.OnBitmapCompleteListener;
import com.example.android.camera2basic.camera2.SingleMediaScanner;
import com.example.android.camera2basic.weiget.EmptyCallback;
import com.example.android.camera2basic.weiget.LoadingCallback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class Camera1BasicFragment extends Fragment
        implements View.OnClickListener, OnTouchListener{
    private static final String TAG = "Camera1BasicFragment";
    private ImageView preview;
    private LinearLayout ll_take_photo;
    private LinearLayout ll_save_delete;
    private ImageView switch_flash;
    private File mFile;
    private String fileName;
    private FrameLayout mPreviewLayout;
    private OverCameraView mOverCameraView;
    private Camera mCamera;
    private boolean isFlashing;
    private boolean isTakePhoto;
    private boolean isFoucing;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private Bitmap newBitmap;
    private CameraPreview cameraPreview;
    private RelativeLayout rl;
    private LinearLayout ll_photo_message;
    private LinearLayout ll_rv;
    private SuperTextView stv_time;
    private SuperTextView stv_position;
    private TextView tv_position;
    private RecyclerView rv;
    private SmartRefreshLayout srl;
    private LocationManager locationManager;
    private BitmapManager bitmapManager;
    private String position;
    private String projectName = "士大夫那我非法八十端口饭卡手动阀你看的身份那是肯定减肥士大夫那我非法八十端口饭卡手动阀你看的身份那是肯定减肥";
    private ImageView iv_back;
    private ImageView take_photo;
    private ImageView switch_camera;
    private ImageView delete;
    private ImageView save;
    private SuperTextView stv_project_name;
    private ImageView iv_search_position;
    private int screenHeight;
    private int oldOrientation = 0;
    private int llRvHeight;
    private int photoMessageHeight;
    private int photoMessageWidth;
    public static Camera1BasicFragment newInstance() {
        return new Camera1BasicFragment();
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
        iv_back = view.findViewById(R.id.iv_back);
        take_photo = view.findViewById(R.id.take_photo);
        switch_camera = view.findViewById(R.id.switch_camera);
        delete = view.findViewById(R.id.delete);
        save = view.findViewById(R.id.save);
        ll_photo_message = view.findViewById(R.id.ll_photo_message);
        stv_time = view.findViewById(R.id.stv_time);
        stv_position = view.findViewById(R.id.stv_position);
        stv_project_name = view.findViewById(R.id.stv_project_name);
        srl = view.findViewById(R.id.srl);
        loadService = LoadSir.getDefault().register(srl);
        ll_rv = view.findViewById(R.id.ll_rv);
        tv_position = view.findViewById(R.id.tv_position);
        rv = view.findViewById(R.id.rv);
        iv_search_position = view.findViewById(R.id.iv_search_position);

        view.findViewById(R.id.iv_search_position).setOnClickListener(this);
        view.findViewById(R.id.switch_flash).setOnClickListener(this);
        view.findViewById(R.id.iv_back).setOnClickListener(this);
        view.findViewById(R.id.take_photo).setOnClickListener(this);
        view.findViewById(R.id.switch_camera).setOnClickListener(this);
        view.findViewById(R.id.delete).setOnClickListener(this);
        view.findViewById(R.id.save).setOnClickListener(this);
        view.findViewById(R.id.tv_position).setOnClickListener(this);
        view.findViewById(R.id.camera_preview_layout).setOnClickListener(this);
        view.findViewById(R.id.camera_preview_layout).setOnTouchListener(this);
        initOrientate();
    }
    //增加传感器
    private OrientationEventListener mOrientationEventListener;
    //拍照时的传感器方向
    private int takePhotoOrientation = 0;
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

                    // 只检测是否有四个角度的改变
                    if (orientation < 60 && orientation > 30) { // 动画0度与接口360度相反,增加下限抵消0度影响
                        orientation = 360;
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
                        System.out.println("cameraPreview--h---->"+cameraPreview.getHeight()+"--w---->"+cameraPreview.getWidth());
                        System.out.println("ll_photo_message--h---->"+photoMessageHeight+"--w---->"+photoMessageWidth);
                        if (orientation == 270) {
                            ObjectAnimator rotation = ObjectAnimator
                                    .ofFloat(ll_photo_message, "Rotation", oldOrientation,
                                            orientation).setDuration(0);
                            ll_photo_message.setPivotX((photoMessageWidth-photoMessageHeight/2)/2-photoMessageHeight/2);
                            ll_photo_message.setPivotY(-(photoMessageWidth-photoMessageHeight/2)/2+photoMessageHeight/2);
                            rotation.start();
                        }else if(orientation == 90){
                            ObjectAnimator rotation = ObjectAnimator
                                    .ofFloat(ll_photo_message, "Rotation", oldOrientation,
                                            orientation).setDuration(0);
//                            ll_photo_message.setPivotX(photoMessageWidth/2);
                            ll_photo_message.setPivotY(-(cameraPreview.getWidth()- (photoMessageWidth-photoMessageHeight/2)));
                            rotation.start();
                        }else if(orientation == 180){
                            ObjectAnimator rotation = ObjectAnimator
                                    .ofFloat(ll_photo_message, "Rotation", oldOrientation,
                                            orientation).setDuration(0);
                            ll_photo_message.setPivotY(-(cameraPreview.getHeight()-photoMessageHeight*2)/2);
                            rotation.start();
                        }else {
                            ObjectAnimator rotation = ObjectAnimator
                                    .ofFloat(ll_photo_message, "Rotation", oldOrientation,
                                            orientation).setDuration(0);
                            rotation.start();
                        }
                        oldOrientation = orientation;
                    }
                }
            };

        }
        mOrientationEventListener.enable();

    }
    public LoadService loadService;
    @Override
    public void onStart() {
        super.onStart();
        ll_rv.post(new Runnable() {
            @Override
            public void run() {
                llRvHeight = ll_rv.getMeasuredHeight();
                System.out.println("screenHeight---->"+screenHeight+"    llRvHeight---->"+llRvHeight);
                ll_rv.setVisibility(View.GONE);
            }
        });
        ll_photo_message.post(new Runnable() {
            @Override
            public void run() {
                photoMessageHeight = ll_photo_message.getMeasuredHeight();
                photoMessageWidth = ll_photo_message.getMeasuredWidth();
                initOrientate();
                System.out.println("photoMessageHeight---->"+photoMessageHeight+"    photoMessageWidth---->"+photoMessageWidth);
            }
        });
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getScreenBrightness();
        screenHeight = SystemUtil.getScreenHeight(getActivity());
        stv_project_name.setLeftString(projectName);

        mData = new ArrayList<>();
        adapter = new BaseQuickAdapter<PoiItem, BaseViewHolder>(R.layout.poi_item, mData){
            @Override
            protected void convert(BaseViewHolder helper, PoiItem item) {
                TextView poi_name = helper.getView(R.id.poi_name);
                TextView poi_position = helper.getView(R.id.poi_position);
                poi_name.setText(item.getTitle());
                poi_position.setText(item.getDistance()+"米·"+item.getSnippet());
                System.out.println(item.getAdName()+"\n" +
                                item.getDirection()+"\n" +
                                item.getBusinessArea()+"\n" +
                                item.getParkingType()+"\n" +
                                item.getSnippet()+"\n" +
                                item.getTypeDes()+"\n" +
                                item.getWebsite()+"\n"
                        );
                helper.itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        position = item.getTitle();
                        stv_position.setLeftString(position);
                        ll_rv.setVisibility(View.GONE);
                    }
                });
            }
        };
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.addItemDecoration(new RecyclerViewSpacesItemDecoration(SystemUtil.dp2px(getActivity(),0),
                SystemUtil.dp2px(getActivity(),0), SystemUtil.dp2px(getActivity(),0),
                SystemUtil.dp2px(getActivity(),1)));
        rv.setAdapter(adapter);
        srl.setOnRefreshListener(refreshLayout -> {
            refreash = true;
            currentPage = 0;
            locationManager.query(currentPage);
        });
        srl.setOnLoadMoreListener(refreshlayout -> {
            refreash = false;
            currentPage++;
            locationManager.query(currentPage);
        });
    }

    private BaseQuickAdapter<PoiItem, BaseViewHolder> adapter;
    private List<PoiItem> mData;


    private void getScreenBrightness() {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        //screenBrightness的值是0.0-1.0 从0到1.0 亮度逐渐增大 如果是-1，那就是跟随系统亮度
        lp.screenBrightness = Float.valueOf(200) * (1f / 255f);
        getActivity().getWindow().setAttributes(lp);
    }

    @Override
    public void onResume() {
        super.onResume();
        openCamera();

        locationManager = new LocationManager(getActivity());
        locationManager.setaMapLocationListener(locationListener);
        locationManager.setOnPoiSearchListener(poiSearchListener);
        locationManager.initLocation();
        locationManager.startLocation();

        bitmapManager = new BitmapManager(getActivity());
        bitmapManager.setOnBitmapCompleteListener(onBitmapCompleteListener);
    }
    OnBitmapCompleteListener onBitmapCompleteListener = new OnBitmapCompleteListener() {
        @Override
        public void OnBitmapComplete(Bitmap bitmap) {
            newBitmap = bitmap;
            setPreview(newBitmap);
        }
    };
    OnPoiSearchListener poiSearchListener = new OnPoiSearchListener() {
        @Override
        public void onPoiSearched(PoiResult poiResult, int i) {
            loadService.showSuccess();
            System.out.println("onPoiSearched i = "+i);
            System.out.println("onPoiSearched poiResult = "+poiResult.getPois());
            System.out.println("onPoiSearched poiResult = "+poiResult.getPageCount());
            ArrayList<PoiItem> pois = poiResult.getPois();
            if (refreash) {
                    if (pois == null || pois.size() == 0) {
                        System.out.println("暂未找到兴趣点");
                        loadService.showCallback(EmptyCallback.class);
                        return;
                    }
                srl.finishRefresh(true);
                srl.setNoMoreData(false);
            } else {
                srl.finishLoadMore();
            }
            if (refreash) mData.clear();
            mData.addAll(pois);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onPoiItemSearched(PoiItem poiItem, int i) {

        }
    };
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (null != location) {
                StringBuffer sb = new StringBuffer();
                //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
                if(location.getErrorCode() == 0){
                    sb.append("定位成功" + "\n");
                    sb.append("定位类型: " + location.getLocationType() + "\n");
                    sb.append("经    度    : " + location.getLongitude() + "\n");
                    sb.append("纬    度    : " + location.getLatitude() + "\n");
                    sb.append("精    度    : " + location.getAccuracy() + "米" + "\n");
                    sb.append("提供者    : " + location.getProvider() + "\n");

                    sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
                    sb.append("角    度    : " + location.getBearing() + "\n");
                    // 获取当前提供定位服务的卫星个数
                    sb.append("星    数    : " + location.getSatellites() + "\n");
                    sb.append("国    家    : " + location.getCountry() + "\n");
                    sb.append("省            : " + location.getProvince() + "\n");
                    sb.append("市            : " + location.getCity() + "\n");
                    sb.append("城市编码 : " + location.getCityCode() + "\n");
                    sb.append("区            : " + location.getDistrict() + "\n");
                    sb.append("区域 码   : " + location.getAdCode() + "\n");
                    sb.append("地    址    : " + location.getAddress() + "\n");
                    sb.append("地    址    : " + location.getDescription() + "\n");
                    sb.append("兴趣点    : " + location.getPoiName() + "\n");
                    //定位完成的时间
                    sb.append("定位时间: " + SystemUtil.formatTime(location.getTime()) + "\n");

                    //定位成功后就不再定位了
                    locationManager.destroyLocation();
                    position = location.getAddress();
                    //设置周边搜索的中心点以及半径
                    tv_position.setText(location.getProvince()+location.getCity()+location.getDistrict());
                    locationManager.setBound(new LatLonPoint(location.getLatitude(),location.getLongitude()), 500);
                    iv_search_position.setEnabled(true);
                } else {
                    //定位失败
                    sb.append("定位失败" + "\n");
                    sb.append("错误码:" + location.getErrorCode() + "\n");
                    sb.append("错误信息:" + location.getErrorInfo() + "\n");
                    sb.append("错误描述:" + location.getLocationDetail() + "\n");

                    position = "未识别该位置";
                    iv_search_position.setEnabled(false);
                }
                sb.append("***定位质量报告***").append("\n");
                sb.append("* WIFI开关：").append(location.getLocationQualityReport().isWifiAble() ? "开启":"关闭").append("\n");
                sb.append("* GPS状态：").append(locationManager.getGPSStatusString(location.getLocationQualityReport().getGPSStatus())).append("\n");
                sb.append("* GPS星数：").append(location.getLocationQualityReport().getGPSSatellites()).append("\n");
                sb.append("****************").append("\n");
                //定位之后的回调时间
                sb.append("回调时间: " + SystemUtil.formatTime(System.currentTimeMillis()));

                //解析定位结果，
                String result = sb.toString();
                System.out.println("定位结果："+result);
            } else {
                System.out.println("定位结果："+"定位失败，loc is null");
                position = "未识别该位置";
                iv_search_position.setEnabled(false);//定位失败时，不能查看周边
            }
            stv_time.setLeftString(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
            stv_position.setLeftString(position);
        }
    };
    private boolean refreash;
    private int currentPage;

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
            //停止预览
            mCamera.stopPreview();
            fileName = System.currentTimeMillis() + ".jpg";
            mFile = bitmapManager.createFile(fileName, "Camera2Basic");
            bitmapManager.getPhoto(mFile, data, mCameraId, takePhotoOrientation,position,projectName);
        });
    }
    private void switchFlash() {
        isFlashing = !isFlashing;
        switch_flash.setImageResource(isFlashing ? R.drawable.flash_open : R.drawable.flash_close);
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(isFlashing ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            Toast.makeText(getActivity(), "该设备不支持闪光灯", Toast.LENGTH_SHORT);
        }
    }
    private void cancleSavePhoto() {
        ll_save_delete.setVisibility(View.GONE);
        ll_take_photo.setVisibility(View.VISIBLE);
        ll_photo_message.setVisibility(View.VISIBLE);
        mPreviewLayout.setVisibility(View.VISIBLE);
        switch_flash.setVisibility(View.VISIBLE);
        if (mFile.exists()) mFile.delete();
        preview.setVisibility(View.GONE);
        //开始预览
        mCamera.startPreview();
        isTakePhoto = false;
    }
    private void setPreview(Bitmap bitmap) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ll_take_photo.setVisibility(View.GONE);
                mPreviewLayout.setVisibility(View.GONE);
                switch_flash.setVisibility(View.GONE);
                ll_photo_message.setVisibility(View.GONE);
                ll_save_delete.setVisibility(View.VISIBLE);
                preview.setVisibility(View.VISIBLE);
                preview.setImageBitmap(bitmap);
//                Glide.with(getContext()).load(bitmap).into(preview);
            }
        });
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.camera_preview_layout:
                if (ll_rv.getVisibility() == View.VISIBLE)
                    ll_rv.setVisibility(View.GONE);
                break;
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
                bitmapManager.saveBitmapFile(mFile, newBitmap);
                new SingleMediaScanner(getContext(), mFile.getAbsolutePath(), new SingleMediaScanner.ScanListener() {
                    @Override public void onScanFinish() {
                        Log.i("SingleMediaScanner", "scan finish!");
                    }
                });
                getActivity().finish();
                break;
            case R.id.tv_position:
                ll_rv.setVisibility(View.GONE);
                break;
            case R.id.iv_search_position:
                ll_rv.setVisibility(View.VISIBLE);
                loadService.showCallback(LoadingCallback.class);
                locationManager.query(currentPage);
                break;
            default:
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
        boolean isSupportCamera = cameraPreview.isSupport(mCameraId);
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
        mPreviewLayout.removeAllViews();
        mCamera = Camera.open(mCameraId);
        cameraPreview = new CameraPreview(getActivity(), mCamera);
        cameraPreview.setmCameraId(mCameraId);
        if (mOverCameraView == null) {
            mOverCameraView = new OverCameraView(getActivity());
        }
        mPreviewLayout.addView(cameraPreview);
        mPreviewLayout.addView(mOverCameraView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mOrientationEventListener != null){
            mOrientationEventListener.disable();
        }
        locationManager.destroyLocation();
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
            case R.id.camera_preview_layout:
                setOverCameraView(event);
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
        return false;
    }

    private void setOverCameraView(MotionEvent event) {
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
    }
}
