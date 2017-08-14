package com.tabletview;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wujun on 2017/8/14.
 *
 * @author madreain
 * @desc
 */

public class TuyaView extends FrameLayout {
    private Context mContext;
    private ImageView img;
    private TabletView tabletview;
    private FrameLayout layout;

    public TuyaView(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    public TuyaView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public TuyaView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View view = LayoutInflater.from(mContext).inflate(R.layout.tuya, null);
        layout = (FrameLayout) view.findViewById(R.id.layout);
        img = (ImageView) view.findViewById(R.id.img);
        tabletview = (TabletView) view.findViewById(R.id.tabletview);
        addView(view);
    }

    /**
     * 去拿到TabletView去执行相关的操作
     * @return
     */
    public TabletView getTabletview() {
        return tabletview;
    }

    /**
     * 设置照片，根据实际项目可支持照片选择、相册、拍照
     * @param bitmap
     */
    public void setImgBitmap(Bitmap bitmap) {
        img.setImageBitmap(bitmap);
    }

    /**
     * 保存照片
     */
    public void saveToSDCard() {
        new Thread() {
            @Override
            public void run() {
                SystemClock.sleep(1000);
                //view层的截图
                layout.setDrawingCacheEnabled(true);
                Bitmap newBitmap = Bitmap.createBitmap(layout.getDrawingCache());
                layout.setDrawingCacheEnabled(false);

                //获得系统当前时间，并以该时间作为文件名
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                String str = formatter.format(curDate) + "paint.png";
                File file = new File("sdcard/" + str);
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                newBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

            }
        }.start();
    }

}
