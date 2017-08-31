package com.demo.zoujiang.customviewlibrary.activity;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.demo.zoujiang.customviewlibrary.R;
import com.demo.zoujiang.customviewlibrary.viewgroup.CustomToast;
import com.demo.zoujiang.customviewlibrary.viewgroup.CustomToolBar;
import com.demo.zoujiang.customviewlibrary.viewgroup.FoldableViewGroup;

public class MainActivity extends AppCompatActivity {
    private FoldableViewGroup foldableViewGroup;
    private CustomToolBar toolBar;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        foldableViewGroup = (FoldableViewGroup) findViewById(R.id.foldableView);
        foldableViewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                foldableViewGroup.start();
            }
        });
        toolBar = (CustomToolBar) findViewById(R.id.toolbar);
        toolBar.setOnLeftClickListener(new CustomToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                CustomToast.showToast(mContext, "左监听事件", 2000);
            }
        });
        toolBar.setOnRightClickListener(new CustomToolBar.OnRightClickListener() {
            @Override
            public void onRightClick(View view) {
                CustomToast.showToast(mContext, "右监听事件", 2000);
            }
        });
    }
}
