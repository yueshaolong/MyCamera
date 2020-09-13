package com.example.android.camera2basic.camera1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.Layout;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;

import com.example.android.camera2basic.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.core.content.FileProvider;

public class BitmapManager {
    private Context context;

    public BitmapManager(Context context) {
        this.context = context;
    }

    public  File createFile(String fileName, String dirName) {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .getAbsoluteFile() + File.separator + dirName;
        File mIVMSFolder = new File(path);
        if (!mIVMSFolder.exists()) {
            mIVMSFolder.mkdirs();
        }
        return new File(mIVMSFolder.getAbsolutePath(), fileName);
    }
    public Uri getUriFromFile(Context context, File file){
        if (Build.VERSION.SDK_INT >= 24) {
            return FileProvider.getUriForFile(context,context.getPackageName()+".provider", file);
        } else {
            return Uri.fromFile(file);
        }
    }
    public void getPhoto(File mFile, byte[] imageData, int mCameraId, int takePhotoOrientation,String position, String project) {
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
                    Bitmap newBitmap = AddTimeWatermark(mBitmap,mCameraId, takePhotoOrientation, position, project);
                    if (onBitmapCompleteListener != null)
                        onBitmapCompleteListener.OnBitmapComplete(newBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public  Bitmap AddTimeWatermark(Bitmap mBitmap, int mCameraId, int takePhotoOrientation,String position, String project) {
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
        mPaint.setTextSize(SystemUtil.dp2px(context,40));

        //矩形背景
//        Paint bgRect=new Paint();
//        bgRect.setStyle(Paint.Style.FILL);
//        bgRect.setColor(Color.YELLOW);
//        RectF rectF=new RectF(SystemUtil.dp2px(context,60), mNewBitmap.getHeight()-SystemUtil.dp2px(context,260),
//                mNewBitmap.getWidth()-SystemUtil.dp2px(context,100), mNewBitmap.getHeight());
//        mCanvas.drawRect(rectF, bgRect);

        //根据路径得到Typeface
//        Typeface typeface=Typeface.createFromAsset(context.getAssets(), "fonts/xs.ttf");
//        mPaint.setTypeface(typeface);
//        mCanvas.rotate(-45, (mNewBitmap2.getWidth() * 1) / 2, (mNewBitmap2.getHeight() * 1) / 2);

        //画时间
        drawPosition(mNewBitmap, mCanvas,
                new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()),
                R.drawable.ic_launcher,
                SystemUtil.dp2px(context,20),
                mNewBitmap.getHeight()-SystemUtil.dp2px(context,260),
                true);
        //画位置
        drawPosition(mNewBitmap, mCanvas,
                position,
                R.drawable.ic_save,
                SystemUtil.dp2px(context,20),
                mNewBitmap.getHeight()-SystemUtil.dp2px(context,200),
                false);

        //画项目名称
        drawPosition(mNewBitmap, mCanvas,
                project,
                R.drawable.ic_save,
                SystemUtil.dp2px(context,20),
                mNewBitmap.getHeight()-SystemUtil.dp2px(context,100),
                false);


        mCanvas.save();
        mCanvas.restore();
        return mNewBitmap2;
    }

    public  void drawPosition(Bitmap mNewBitmap, Canvas mCanvas, String mFormat, int drawable, int x, int y, boolean isTime) {
        if (TextUtils.isEmpty(mFormat)) return;
        Bitmap resource = BitmapFactory.decodeResource(context.getResources(), drawable);
        int width = resource.getWidth();
        int height = resource.getHeight();
        // 设置想要的大小
        int newWidth = isTime?SystemUtil.dp2px(context,34):SystemUtil.dp2px(context,24);
        int newHeight = isTime?SystemUtil.dp2px(context,34):SystemUtil.dp2px(context,24);
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix2 = new Matrix();
        matrix2.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        resource = Bitmap.createBitmap(resource, 0, 0, width, height, matrix2, true);
        mCanvas.drawBitmap(resource, x, y+SystemUtil.dp2px(context,10), null);

        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(isTime?SystemUtil.dp2px(context,34):SystemUtil.dp2px(context,24));
        StaticLayout staticLayout = new StaticLayout(mFormat, textPaint,
                mNewBitmap.getWidth()-SystemUtil.dp2px(context,100),
                    Layout.Alignment.ALIGN_NORMAL, 1, 0, true);
        mCanvas.save();
        mCanvas.translate(3*x, y);
        staticLayout.draw(mCanvas);
        mCanvas.restore();

//        for (int i = 0; i < Layout.getLineCount(); i++) {
//            Rect rect = new Rect();
//            Paint bgRect=new Paint();
//            bgRect.setStyle(Paint.Style.FILL);
//            bgRect.setColor(Color.YELLOW);
//            Layout.getLineBounds(i, rect);
//            mCanvas.drawRect(rect, bgRect);
//        }

//        Rect rect = new Rect();
//        float v = textPaint.measureText(mFormat, 0, mFormat.length() - 1);
////        Paint bgRect=new Paint();
//        textPaint.setStyle(Paint.Style.FILL);
//        textPaint.setColor(Color.parseColor("#66FFFF00"));
//        RectF rectF=new RectF(3*x, y, x+v, y+(isTime?SystemUtil.dp2px(context,40):SystemUtil.dp2px(context,30))+SystemUtil.dp2px(context,10));
//        mCanvas.drawRect(rectF, textPaint);
    }
    public void saveBitmapFile(File mFile, Bitmap bitmap){
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

    public interface OnBitmapCompleteListener{
        void OnBitmapComplete(Bitmap bitmap);
    }
    public OnBitmapCompleteListener onBitmapCompleteListener;
    public void setOnBitmapCompleteListener(OnBitmapCompleteListener onBitmapCompleteListener) {
        this.onBitmapCompleteListener = onBitmapCompleteListener;
    }
}
