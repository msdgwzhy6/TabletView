# TabletView

自定义view————涂鸦画板

今天给大家带来一个特别有意思的自定义view---涂鸦，先看看效果

![效果图](https://user-gold-cdn.xitu.io/2017/8/14/95178a118b7f303bf4a2200c7a09937b)

效果看了，挺好玩吧，其实就是利用万能的Path来画路径，颜色，画笔大小，就是一些设置，废话不多说，直接上真家伙

### 触摸事件处理

这里的触摸事件主要有按下(MotionEvent.ACTION_DOWN)、移动(MotionEvent.ACTION_MOVE)、抬起(MotionEvent.ACTION_UP),需要对其分别做相应的处理

触摸事件处理方法
```
@Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 每次down下去重新new一个Path
                mPath = new Path();
                //每一次记录的路径对象是不一样的
                dp = new DrawPath();
                dp.path = mPath;
                dp.paint = mPaint;
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }
```
按下(MotionEvent.ACTION_DOWN)后执行的touch_start(x, y);
```
    private void touch_start(float x, float y) {
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }
```

移动(MotionEvent.ACTION_MOVE)后执行的touch_move(x, y);
```
     private void touch_move(float x, float y) {
         float dx = Math.abs(x - mX);
         float dy = Math.abs(mY - y);
         if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
             // 从x1,y1到x2,y2画一条贝塞尔曲线，更平滑(直接用mPath.lineTo也可以)
             mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
             //mPath.lineTo(mX,mY);
             mX = x;
             mY = y;
         }
     }
```
抬起(MotionEvent.ACTION_UP)后执行的touch_up();
```
    private void touch_up() {
        mPath.lineTo(mX, mY);
        mCanvas.drawPath(mPath, mPaint);
        //将一条完整的路径保存下来(相当于入栈操作)
        savePath.add(dp);
        mPath = null;// 重新置空
    }
```
然后看一下onDraw
 
``` 
     @Override
     public void onDraw(Canvas canvas) {
         //canvas.drawColor(0xFFAAAAAA);
         // 将前面已经画过得显示出来
         canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
         if (mPath != null) {
             // 实时的显示
             canvas.drawPath(mPath, mPaint);
         }
     }
```
整个过程走完了，接下来就来考虑一下其他过程

### 撤销、恢复、重做三个操作

撤销操作
```
    /**
     * 撤销
     * 撤销的核心思想就是将画布清空，
     * 将保存下来的Path路径最后一个移除掉，
     * 重新将路径画在画布上面。
     */
    public void undo() {
        if (savePath != null && savePath.size() > 0) {
            DrawPath drawPath = savePath.get(savePath.size() - 1);
            deletePath.add(drawPath);
            savePath.remove(savePath.size() - 1);
            redrawOnBitmap();
        }
    }
```

恢复操作
```
    /**
     * 恢复，恢复的核心就是将删除的那条路径重新添加到savapath中重新绘画即可
     */
    public void recover() {
        if (deletePath.size() > 0) {
            //将删除的路径列表中的最后一个，也就是最顶端路径取出（栈）,并加入路径保存列表中
            DrawPath dp = deletePath.get(deletePath.size() - 1);
            savePath.add(dp);
            //将取出的路径重绘在画布上
            mCanvas.drawPath(dp.path, dp.paint);
            //将该路径从删除的路径列表中去除
            deletePath.remove(deletePath.size() - 1);
            invalidate();
        }
    }
```


重做操作
```
    /**
     * 重做
     */
    public void redo() {
        if (savePath != null && savePath.size() > 0) {
            savePath.clear();
            redrawOnBitmap();
        }
    }
```

```
    private void redrawOnBitmap() {
        /*mBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
                Bitmap.Config.RGB_565);
        mCanvas.setBitmap(mBitmap);// 重新设置画布，相当于清空画布*/
        initCanvas();
        Iterator<DrawPath> iter = savePath.iterator();
        while (iter.hasNext()) {
            DrawPath drawPath = iter.next();
            mCanvas.drawPath(drawPath.path, drawPath.paint);
        }
        invalidate();// 刷新
    }
```

初始化相关
```
    public void initCanvas() {
        setPaintStyle();
        if (screenWidth > 0 && screenHeight > 0) {
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            //画布大小
            mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
            mBitmap.eraseColor(Color.argb(0, 0, 0, 0));
            mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
            mCanvas.drawColor(Color.TRANSPARENT);
        }
    }

    //初始化画笔样式
    private void setPaintStyle() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
        mPaint.setStrokeCap(Paint.Cap.ROUND);// 形状
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        if (currentStyle == 1) {
            mPaint.setStrokeWidth(currentSize);
            mPaint.setColor(currentColor);
        } else {//橡皮擦
            mPaint.setAlpha(0);
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            mPaint.setColor(Color.TRANSPARENT);
            mPaint.setStrokeWidth(50);
        }
    }
```

### 样式相关修改

```
    //以下为样式修改内容
    //设置画笔样式
    public void selectPaintStyle(int which) {
        if (which == 0) {
            currentStyle = 1;
            setPaintStyle();
        }
        //当选择的是橡皮擦时，设置颜色为白色
        if (which == 1) {
            currentStyle = 2;
            setPaintStyle();
        }
    }

    //选择画笔大小
    public void selectPaintSize(int which) {
        currentSize = which;
        setPaintStyle();
    }

    //设置画笔颜色
    public void selectPaintColor(int which) {
        currentColor = paintColor[which];
        setPaintStyle();
    }
```

到此为止，涂鸦的效果有了，实际项目中都是对一个照片进行涂鸦操作，因此添加照片进行再次封装，达到实际项目中的那种效果

### 添加照片进行涂鸦

利用组合自定义view，结合上面的TabletView和ImageView来到实际项目效果

xml布局
```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <ImageView
        android:id="@+id/img"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <com.tabletview.TabletView
        android:id="@+id/tabletview"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</FrameLayout>
```

继承FrameLayout，在onFinishInflate()中添加view
```
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View view = LayoutInflater.from(mContext).inflate(R.layout.tuya, null);
        layout = (FrameLayout) view.findViewById(R.id.layout);
        img = (ImageView) view.findViewById(R.id.img);
        tabletview = (TabletView) view.findViewById(R.id.tabletview);
        addView(view);
    }
```

获得TabletView去执行相关操作
```
    /**
     * 去拿到TabletView去执行相关的操作
     * @return
     */
    public TabletView getTabletview() {
        return tabletview;
    }
```

设置照片的方法，实际项目中可利用照片、相册选择照片返回设置

```
    /**
     * 设置照片，根据实际项目可支持照片选择、相册、拍照
     * @param bitmap
     */
    public void setImgBitmap(Bitmap bitmap) {
        img.setImageBitmap(bitmap);
    }
```

保存照片，这里的权限问题根据6.0、7.0、8.0得设置，可以参考其他的权限讲解

```
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
```

### 代码中应用

xml布局

```
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.tabletview.MainActivity">

    <com.tabletview.TuyaView
        android:id="@+id/tuyaview"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">


        <Button
            android:id="@+id/btn_undo"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="撤销" />

        <Button
            android:id="@+id/btn_redo"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="重做" />

        <Button
            android:id="@+id/btn_save"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="保存" />

        <Button
            android:id="@+id/btn_recover"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="恢复" />


        <Button
            android:id="@+id/btn_paintcolor"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="颜色" />

        <Button
            android:id="@+id/btn_paintsize"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="画笔大小" />

        <Button
            android:id="@+id/btn_paintstyle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="画笔类型" />

        <SeekBar
            android:id="@+id/sb_size"
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />

    </LinearLayout>




</RelativeLayout>
```

java代码
```
    private void initView() {
        btn_undo = (Button) findViewById(R.id.btn_undo);
        btn_undo.setOnClickListener(this);
        btn_redo = (Button) findViewById(R.id.btn_redo);
        btn_redo.setOnClickListener(this);
        btn_save = (Button) findViewById(R.id.btn_save);
        btn_save.setOnClickListener(this);
        btn_recover = (Button) findViewById(R.id.btn_recover);
        btn_recover.setOnClickListener(this);
        sb_size = (SeekBar) findViewById(R.id.sb_size);
        sb_size.setOnClickListener(this);
        btn_paintcolor = (Button) findViewById(R.id.btn_paintcolor);
        btn_paintcolor.setOnClickListener(this);
        btn_paintsize = (Button) findViewById(R.id.btn_paintsize);
        btn_paintsize.setOnClickListener(this);
        btn_paintstyle = (Button) findViewById(R.id.btn_paintstyle);
        btn_paintstyle.setOnClickListener(this);

        tuyaview = (TuyaView) findViewById(R.id.tuyaview);
        //设置照片
        Bitmap bitmap= BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
        tuyaview.setImgBitmap(bitmap);
//
        tabletView = tuyaview.getTabletview();
        tabletView.requestFocus();
        tabletView.selectPaintSize(sb_size.getProgress());
        sb_size.setOnSeekBarChangeListener(new MySeekChangeListener());
    }


    class MySeekChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            tabletView.selectPaintSize(seekBar.getProgress());
            //Toast.makeText(MainActivity.this,"当前画笔尺寸为"+seekBar.getProgress(),Toast.LENGTH_SHORT ).show();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            tabletView.selectPaintSize(seekBar.getProgress());
            //Toast.makeText(MainActivity.this,"当前画笔尺寸为"+seekBar.getProgress(),Toast.LENGTH_SHORT ).show();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_undo:
                tabletView.undo();
                break;
            case R.id.btn_redo:
                tabletView.redo();
                break;
            case R.id.btn_save:
//                tabletView.saveToSDCard();
                tuyaview.saveToSDCard();
                break;
            case R.id.btn_recover:
                tabletView.recover();
                break;
            case R.id.btn_paintcolor:
                sb_size.setVisibility(View.GONE);
                showPaintColorDialog(v);
                break;
            case R.id.btn_paintsize:
                sb_size.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_paintstyle:
                sb_size.setVisibility(View.GONE);
                showMoreDialog(v);
                break;
        }
    }

    private int select_paint_color_index = 0;
    private int select_paint_style_index = 0;

    public void showPaintColorDialog(View parent) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle("选择画笔颜色：");
        alertDialogBuilder.setSingleChoiceItems(R.array.paintcolor, select_paint_color_index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                select_paint_color_index = which;
                tabletView.selectPaintColor(which);
                dialogInterface.dismiss();
            }
        });
        alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialogBuilder.create().show();
    }

    public void showMoreDialog(View parent) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("选择画笔或橡皮擦：");
        alertDialogBuilder.setSingleChoiceItems(R.array.paintstyle, select_paint_style_index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                select_paint_style_index = which;
                tabletView.selectPaintStyle(which);
                dialog.dismiss();
            }
        });
        alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialogBuilder.create().show();
    }

```


[个人博客](https://madreain.github.io/)

