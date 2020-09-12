package com.example.android.camera2basic.camera1;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.allen.library.SuperTextView;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationClientOption.AMapLocationProtocol;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationQualityReport;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.poisearch.PoiSearch.SearchBound;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.android.camera2basic.CameraActivity;
import com.example.android.camera2basic.R;
import com.example.android.camera2basic.camera2.AutoFitTextureView;
import com.example.android.camera2basic.camera2.CameraHelper;
import com.example.android.camera2basic.camera2.ICamera;
import com.example.android.camera2basic.camera2.ICamera.FlashState;
import com.example.android.camera2basic.camera2.ICamera.TakePhotoListener;
import com.example.android.camera2basic.camera2.SingleMediaScanner;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class Camera1BasicFragment extends Fragment
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
    private LinearLayout ll_photo_message;
    private LinearLayout ll_rv;
    private TextView stv_time;
    private TextView stv_position;
    private TextView tv_position;
    private RecyclerView rv;
    private SmartRefreshLayout srl;


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
        ImageView iv_back = view.findViewById(R.id.iv_back);
        ImageView take_photo = view.findViewById(R.id.take_photo);
        ImageView switch_camera = view.findViewById(R.id.switch_camera);
        ImageView delete = view.findViewById(R.id.delete);
        ImageView save = view.findViewById(R.id.save);
        ll_photo_message = view.findViewById(R.id.ll_photo_message);
        stv_time = view.findViewById(R.id.stv_time);
        stv_position = view.findViewById(R.id.stv_position);
        TextView stv_project_name = view.findViewById(R.id.stv_project_name);
        srl = view.findViewById(R.id.srl);
        ll_rv = view.findViewById(R.id.ll_rv);
        tv_position = view.findViewById(R.id.tv_position);
        rv = view.findViewById(R.id.rv);
        stv_project_name.setText("XXX项目");
        ImageView iv_search_position = view.findViewById(R.id.iv_search_position);

        view.findViewById(R.id.iv_search_position).setOnClickListener(this);
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
                        stv_position.setText(item.getTitle());
                        ll_rv.setVisibility(View.GONE);
                    }
                });
            }
        };
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
//        rv.addItemDecoration(new RecyclerViewSpacesItemDecoration(CommonUtil.dp2px(mBaseActivity,0),
//                CommonUtil.dp2px(mBaseActivity,1), CommonUtil.dp2px(mBaseActivity,0),
//                CommonUtil.dp2px(mBaseActivity,1)));
        rv.setAdapter(adapter);
        srl.setOnRefreshListener(refreshLayout -> {
            refreash = true;
            currentPage = 0;
            query.setPageNum(currentPage);//设置查询页码
            poiSearch.searchPOIAsyn();
        });
        srl.setOnLoadMoreListener(refreshlayout -> {
            refreash = false;
            currentPage++;
            query.setPageNum(currentPage);//设置查询页码
            poiSearch.searchPOIAsyn();
        });
    }
    private BaseQuickAdapter<PoiItem, BaseViewHolder> adapter;
    private List<PoiItem> mData;
//    private PageParam pageParam;


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

        initLocation();
        startLocation();
    }
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private GeocodeSearch geocoderSearch;
    private PoiSearch.Query query;
    private PoiSearch poiSearch;
    private boolean refreash;
    private int currentPage;
    private void initLocation(){
        //初始化client
        locationClient = new AMapLocationClient(getActivity().getApplicationContext());
        locationOption = getDefaultOption();
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        // 设置定位监听
        locationClient.setLocationListener(locationListener);

        //逆坐标查询
        geocoderSearch = new GeocodeSearch(getActivity().getApplicationContext());
        geocoderSearch.setOnGeocodeSearchListener(new OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                System.out.println("你坐标："+regeocodeResult.getRegeocodeAddress().getCity()+"\n"
                        +regeocodeResult.getRegeocodeAddress().getDistrict()+"\n"
                        +regeocodeResult.getRegeocodeAddress().getFormatAddress()+"\n"
                        +regeocodeResult.getRegeocodeAddress().getBusinessAreas()+"\n"
                        +regeocodeResult.getRegeocodeAddress().getPois()+"\n"
                );
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

            }
        });

        //poi查询
        //keyWord表示搜索字符串，
        //第二个参数表示POI搜索类型，二者选填其一，选用POI搜索类型时建议填写类型代码，码表可以参考下方（而非文字）
        //cityCode表示POI搜索区域，可以是城市编码也可以是城市名称，也可以传空字符串，空字符串代表全国在全国范围内进行搜索
        query = new PoiSearch.Query("汽车服务|汽车销售|\n" +
                "        汽车维修|摩托车服务|餐饮服务|购物服务|生活服务|体育休闲服务|医疗保健服务|\n" +
                "        住宿服务|风景名胜|商务住宅|政府机构及社会团体|科教文化服务|交通设施服务|\n" +
                "        金融保险服务|公司企业|道路附属设施|地名地址信息|公共设施", "", "");
        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);//设置查询页码
        poiSearch = new PoiSearch(getActivity(), query);
        poiSearch.setOnPoiSearchListener(new OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int i) {
                System.out.println("onPoiSearched i = "+i);
                System.out.println("onPoiSearched poiResult = "+poiResult.getPois());
                System.out.println("onPoiSearched poiResult = "+poiResult.getPageCount());
                if (refreash) {
//                    if (isEmptyData(dangerModel)) {
//                        setEmptyCallBack("暂无该类风险",true);
//                        return;
//                    }
                    srl.finishRefresh(true);
                    srl.setNoMoreData(false);
                } else {
                    srl.finishLoadMore();
                }
                if (refreash) mData.clear();
                mData.addAll(poiResult.getPois());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int i) {

            }
        });
    }
    private AMapLocationClientOption getDefaultOption(){
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        return mOption;
    }
    private void destroyLocation(){
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }
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
                    sb.append("定位时间: " + Utils.formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss") + "\n");


//                    LatLonPoint latLonPoint = new LatLonPoint(location.getLatitude(),location.getLongitude());
//                    // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
//                    RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,GeocodeSearch.GPS);
//                    geocoderSearch.getFromLocationAsyn(query);
                    destroyLocation();
                    stv_time.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
                    stv_position.setText(location.getAddress());
                    tv_position.setText(location.getProvince()+location.getCity()+location.getDistrict());
                    poiSearch.setBound(new SearchBound(new LatLonPoint(location.getLatitude(),location.getLongitude()), 500));//设置周边搜索的中心点以及半径
                } else {
                    //定位失败
                    sb.append("定位失败" + "\n");
                    sb.append("错误码:" + location.getErrorCode() + "\n");
                    sb.append("错误信息:" + location.getErrorInfo() + "\n");
                    sb.append("错误描述:" + location.getLocationDetail() + "\n");
                }
                sb.append("***定位质量报告***").append("\n");
                sb.append("* WIFI开关：").append(location.getLocationQualityReport().isWifiAble() ? "开启":"关闭").append("\n");
                sb.append("* GPS状态：").append(getGPSStatusString(location.getLocationQualityReport().getGPSStatus())).append("\n");
                sb.append("* GPS星数：").append(location.getLocationQualityReport().getGPSSatellites()).append("\n");
                sb.append("****************").append("\n");
                //定位之后的回调时间
                sb.append("回调时间: " + Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss") + "\n");

                //解析定位结果，
                String result = sb.toString();
                System.out.println("定位结果："+result);
            } else {
                System.out.println("定位结果："+"定位失败，loc is null");
            }
        }
    };
    private String getGPSStatusString(int statusCode){
        String str = "";
        switch (statusCode){
            case AMapLocationQualityReport.GPS_STATUS_OK:
                str = "GPS状态正常";
                break;
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPROVIDER:
                str = "手机中没有GPS Provider，无法进行GPS定位";
                break;
            case AMapLocationQualityReport.GPS_STATUS_OFF:
                str = "GPS关闭，建议开启GPS，提高定位质量";
                break;
            case AMapLocationQualityReport.GPS_STATUS_MODE_SAVING:
                str = "选择的定位模式中不包含GPS定位，建议选择包含GPS定位的模式，提高定位质量";
                break;
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPERMISSION:
                str = "没有GPS定位权限，建议开启gps定位权限";
                break;
        }
        return str;
    }
    private void startLocation(){
        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
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
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(isFlashing ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
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

        //矩形背景
//        Paint bgRect=new Paint();
//        bgRect.setStyle(Paint.Style.FILL);
//        bgRect.setColor(Color.YELLOW);
//        RectF rectF=new RectF(dp2px(getActivity(),60), mNewBitmap.getHeight()-dp2px(getActivity(),260),
//                mNewBitmap.getWidth()-dp2px(getActivity(),100), mNewBitmap.getHeight());
//        mCanvas.drawRect(rectF, bgRect);

        //水印的位置坐标
        //根据路径得到Typeface
//        Typeface typeface=Typeface.createFromAsset(getActivity().getAssets(), "fonts/xs.ttf");
//        mPaint.setTypeface(typeface);
//        mCanvas.rotate(-45, (mNewBitmap2.getWidth() * 1) / 2, (mNewBitmap2.getHeight() * 1) / 2);

        //画时间
        drawPosition(mNewBitmap, mCanvas,
                new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()),
                R.drawable.ic_launcher,
                dp2px(getActivity(),20),
                mNewBitmap.getHeight()-dp2px(getActivity(),260),
                true);
        //画位置
        drawPosition(mNewBitmap, mCanvas,
                "位置 "+" 纬度:"+123+"  经度:" + "但是覅哦粉红色的开发和上岛咖啡纳斯达克快点发货速度发货速度放",
                R.drawable.ic_save,
                dp2px(getActivity(),20),
                mNewBitmap.getHeight()-dp2px(getActivity(),200),
                false);

        //画项目名称
        drawPosition(mNewBitmap, mCanvas,
                "XXX项目用户开启定位后，获取用户当前位置和时间，时间到分即可，显示如图示",
                R.drawable.ic_save,
                dp2px(getActivity(),20),
                mNewBitmap.getHeight()-dp2px(getActivity(),100),
                false);


        mCanvas.save();
        mCanvas.restore();
        return mNewBitmap2;
    }

    private void drawPosition(Bitmap mNewBitmap, Canvas mCanvas, String mFormat, int drawable, int x, int y, boolean isTime) {
        Bitmap resource = BitmapFactory.decodeResource(getActivity().getResources(), drawable);
        int width = resource.getWidth();
        int height = resource.getHeight();
        // 设置想要的大小
        int newWidth = isTime?dp2px(getActivity(),34):dp2px(getActivity(),24);
        int newHeight = isTime?dp2px(getActivity(),34):dp2px(getActivity(),24);
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix2 = new Matrix();
        matrix2.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        resource = Bitmap.createBitmap(resource, 0, 0, width, height, matrix2, true);
        mCanvas.drawBitmap(resource, x, y+dp2px(getActivity(),10), null);

        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(isTime?dp2px(getActivity(),40):dp2px(getActivity(),30));
        StaticLayout staticLayout = new StaticLayout(mFormat, textPaint,
                mNewBitmap.getWidth()-dp2px(getActivity(),100),
                Layout.Alignment.ALIGN_NORMAL, 1, 0, true);
        mCanvas.save();
        mCanvas.translate(3*x, y);
        staticLayout.draw(mCanvas);
        mCanvas.restore();

//        for (int i = 0; i < staticLayout.getLineCount(); i++) {
//            Rect rect = new Rect();
//            Paint bgRect=new Paint();
//            bgRect.setStyle(Paint.Style.FILL);
//            bgRect.setColor(Color.YELLOW);
//            staticLayout.getLineBounds(i, rect);
//            mCanvas.drawRect(rect, bgRect);
//        }

//        Rect rect = new Rect();
//        float v = textPaint.measureText(mFormat, 0, mFormat.length() - 1);
////        Paint bgRect=new Paint();
//        textPaint.setStyle(Paint.Style.FILL);
//        textPaint.setColor(Color.parseColor("#66FFFF00"));
//        RectF rectF=new RectF(3*x, y, x+v, y+(isTime?dp2px(getActivity(),40):dp2px(getActivity(),30))+dp2px(getActivity(),10));
//        mCanvas.drawRect(rectF, textPaint);
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
            case R.id.iv_search_position:
                ll_rv.setVisibility(View.VISIBLE);
                poiSearch.searchPOIAsyn();
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
        destroyLocation();
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
