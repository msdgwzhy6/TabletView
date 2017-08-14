package com.tabletview;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TabletView tabletView;
    private Button btn_undo;
    private Button btn_redo;
    private Button btn_save;
    private Button btn_recover;
    private SeekBar sb_size;
    private Button btn_paintcolor;
    private Button btn_paintsize;
    private Button btn_paintstyle;
    private TuyaView tuyaview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

    }

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
}
