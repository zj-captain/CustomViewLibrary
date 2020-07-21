package com.hjtech.test20720;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Scroller;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

public class BigImageView extends View implements GestureDetector.OnGestureListener, View.OnTouchListener,
        GestureDetector.OnDoubleTapListener {
    private final Rect mRect;
    private final BitmapFactory.Options mOptions;
    private final GestureDetector mGestureDetector;
    private final Scroller mScroller;
    private final ScaleGestureDetector mScaleGestureDetector;
    private int mImgWidth;
    private int mImgHeight;
    private BitmapRegionDecoder mDecoder;
    private int mViewWidth;
    private int mViewHeight;
    private float mScale;
    public Bitmap mBitmap = null;
    private float originalScale;

    public BigImageView(Context context) {
        this(context, null);
    }

    public BigImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BigImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //第一步：设置成员变量
        //设置加载的矩形区域
        mRect = new Rect();
        //内存复用
        mOptions = new BitmapFactory.Options();
        //手势识别
        mGestureDetector = new GestureDetector(context, this);
        //滚动类
        mScroller = new Scroller(context);
        //缩放手势识别
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGesture());
        //设置触摸类
        setOnTouchListener(this);
    }

    @Override
    public void computeScroll() {
        //第9步：处理结果
        if (mScroller.isFinished()) {
            return;
        }
        if (mScroller.computeScrollOffset()) {
            mRect.top = mScroller.getCurrY();
            mRect.bottom = mRect.top + (int) (mViewHeight / mScale);
            invalidate();
        }
        super.computeScroll();
    }

    //第二步：设置图片
    public void setImage(InputStream is) {
        //获取图片信息，不能将整张图片加载到内存
        mOptions.inJustDecodeBounds = true;//成对使用
        BitmapFactory.decodeStream(is, null, mOptions);
        mImgWidth = mOptions.outWidth;
        mImgHeight = mOptions.outHeight;
        //设置图片可复用
        mOptions.inMutable = true;
        //设置图片格式
        mOptions.inPreferredConfig = Bitmap.Config.RGB_565;

        mOptions.inJustDecodeBounds = false;

        //创建一个区域解码器
        try {
            mDecoder = BitmapRegionDecoder.newInstance(is, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        requestLayout();
    }

    //第三步：测量
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mViewWidth = getMeasuredWidth();
        mViewHeight = getMeasuredHeight();
        /*//确定加载图片的区域
        mRect.top = 0;
        mRect.left = 0;
        mRect.right = mImgWidth;
        //根据宽度，可以确定缩放因子
        mScale = mViewWidth / (float) mImgWidth;
        mRect.bottom = (int) (mViewHeight / mScale);*/

        //添加手势以后的逻辑
        mRect.top = 0;
        mRect.left = 0;
        mRect.right = Math.min(mImgWidth, mViewWidth);
        mRect.bottom = Math.min(mViewHeight, mImgHeight);

        //定义缩放因子
        originalScale = mViewWidth / (float) mImgWidth;     //初始的缩放因子
        mScale = originalScale;
    }

    //第四步：绘制矩形区域
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDecoder == null) {
            return;
        }
        mBitmap = null;
        mOptions.inBitmap = mBitmap;
        mBitmap = mDecoder.decodeRegion(mRect, mOptions);
        Matrix matrix = new Matrix();
//        matrix.setScale(mScale, mScale);

        // mRect.width() 矩形区域随着手势缩放一直在变，需动态计算缩放因子
        matrix.setScale(mViewWidth / (float) mRect.width(), mViewWidth / (float) mRect.width());
        canvas.drawBitmap(mBitmap, matrix, null);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        //第6步：手按下去，处理事件
        if (mScroller != null && !mScroller.isFinished()) {
            mScroller.forceFinished(true);
        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        //第7步：处理滑动事件

        mRect.offset((int) distanceX, (int) distanceY);
        //移动的时候，mRect需要改变显示区域
        //移动到顶部和底部的处理
        if (mRect.top < 0) {
            mRect.top = 0;
            mRect.bottom = (int) (mViewHeight / mScale);
        }
        if (mRect.bottom > mImgHeight) {
            mRect.bottom = mImgHeight;
            mRect.top = mImgHeight - (int) (mViewHeight / mScale);
        }
        //移动到左边和右边的处理
        if (mRect.left < 0) {
            mRect.left = 0;
            mRect.right = (int) (mViewWidth / mScale);
        }
        if (mRect.right > mImgWidth) {
            mRect.right = mImgWidth;
            mRect.left = mImgWidth - (int) (mViewWidth / mScale);
        }
        invalidate();
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        //第8步：处理滑动惯性问题
        mScroller.fling(mRect.left, mRect.top, (int) -velocityX, (int) -velocityY, 0, mImgWidth - (int)(mViewWidth/mScale), 0, mImgHeight - (int) (mViewHeight / mScale));
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //第五步：处理点击事件,将事件传递给手势事件处理
        mGestureDetector.onTouchEvent(event);//单点 手势触摸处理
        mScaleGestureDetector.onTouchEvent(event);//双点 手势触摸处理
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        //双击事件
        if (mScale < originalScale * 2) {
            mScale = originalScale * 3;
        }else {
            mScale = originalScale;
        }
        //移动的时候，mRect需要改变显示区域
        //移动到顶部和底部的处理
        if (mRect.top < 0) {
            mRect.top = 0;
            mRect.bottom = (int) (mViewHeight / mScale);
        }
        if (mRect.bottom > mImgHeight) {
            mRect.bottom = mImgHeight;
            mRect.top = mImgHeight - (int) (mViewHeight / mScale);
        }
        //移动到左边和右边的处理
        if (mRect.left < 0) {
            mRect.left = 0;
            mRect.right = (int) (mViewWidth / mScale);
        }
        if (mRect.right > mImgWidth) {
            mRect.right = mImgWidth;
            mRect.left = mImgWidth - (int) (mViewWidth / mScale);
        }
        mRect.right = mRect.left + (int) (mViewWidth / mScale);
        mRect.bottom = mRect.top + (int) (mViewHeight / mScale);

        invalidate();
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }


    //todo 处理缩放的回调事件
    private class ScaleGesture extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = mScale;
            //detector.getScaleFactor() 上次的缩放因子，eg:放大0.5倍  detector.getScaleFactor() = 1.5，故需要-1（减1）来获取到当前的缩放因子
            scale += detector.getScaleFactor() - 1;
            if (scale <= originalScale) {
                scale = originalScale;
            } else if (scale > originalScale * 3) {
                scale = originalScale * 3;
            }

            mRect.right = mRect.left + (int) (mViewWidth / scale);
            mRect.bottom = mRect.top + (int) (mViewHeight / scale);

            mScale = scale;
            invalidate();
            return true;
        }
    }
}
