package com.demo.zoujiang.customviewlibrary.viewgroup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.icu.util.MeasureUnit;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.TintTypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.demo.zoujiang.customviewlibrary.R;


/**
 * Created by zj on 2017/8/31.
 */

public class FoldableViewGroup extends ViewGroup {
    private float width;//控件的宽
    private float heigh;//控件的高
    private float center;//两边圆的半径
    private float x;//矩形右边x的坐标值
    private Paint mPaint;
    public final static int IS_ADD_WIDTH = 0x01;
    public final static int IS_REDUCE_WIDTH = 0x02;
    private boolean isAdd = false;//记录状态
    private float speed;//变化的速度
    private float y;//左边小圆与大圆的间距

    private float x_y;//放缩的比例

    private View child;
    private int cWidth, cHeigh;
    private float tX;//文本框的右下角的x坐标
    private float tX_x;//文本框的放缩比例

    private float iconWidth;//小图标的宽度
    private float degress;//图标旋转的度数
    private float deg_x;//旋转相关比例

    public FoldableViewGroup(Context context) {
        super(context);
        mPaint = new Paint();
    }

    public FoldableViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        TintTypedArray ta = TintTypedArray.obtainStyledAttributes(context, attrs, R.styleable.FoldableViewGroup);
        TextView textView = new TextView(context);
        textView.setText(ta.getString(R.styleable.FoldableViewGroup_text));
        textView.setTextSize(ta.getDimension(R.styleable.FoldableViewGroup_textSize, 20));
        textView.setTextColor(ta.getColor(R.styleable.FoldableViewGroup_textColor, Color.WHITE));
        textView.setMaxLines(1);
        speed = ta.getInteger(R.styleable.FoldableViewGroup_speed, 30);
        y = ta.getDimension(R.styleable.FoldableViewGroup_space, 30);
        ta.recycle();
        addView(textView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (width == 0) {
            width = MeasureSpec.getSize(widthMeasureSpec);
            heigh = MeasureSpec.getSize(heightMeasureSpec);
            center = heigh / 2;
            x = width - center;
            x_y = x / y;

            child = getChildAt(0);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            cWidth = child.getMeasuredWidth();
            cHeigh = child.getMeasuredHeight();

//            tX = cWidth + center - 10;
            tX = width - center - 10;
            tX_x = tX / x;

            degress = 90;
            deg_x = degress / x;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Log.i("zoujiang", x + "");
        mPaint.setColor(Color.BLUE);
        //画左边的圆
        canvas.drawCircle(center, center, center, mPaint);
        //画中间的矩形
        RectF rectF = new RectF(center, 0, x, heigh);
        canvas.drawRect(rectF, mPaint);
        //画右边的圆
        canvas.drawCircle(x, center, center, mPaint);
        //画左边的小圆
        mPaint.setColor(Color.GREEN);
        canvas.drawCircle(center, center, center - y, mPaint);

        //绘制小圆里面的图片
        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.dengpao);
        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.dengpao_open);
        iconWidth = center - 30;
        canvas.save();
        canvas.rotate(-90 + degress, center, center);
        if (degress == 0) {
            canvas.drawBitmap(bitmap2, center - iconWidth / 2, center - iconWidth / 2, mPaint);
        } else {
            canvas.drawBitmap(bitmap1, center - iconWidth / 2, center - iconWidth / 2, mPaint);
        }
        canvas.restore();
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        child.layout((int) (center * 2 + 5), (int) (center - cHeigh / 2), (int) tX, (int) (center + cHeigh / 2));
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
            switch (msg.what) {
                case IS_ADD_WIDTH:
                    x += speed;
                    y = x / x_y;
                    tX = x * tX_x;
                    degress = deg_x * x;
                    if (x < width - center) {
                        mHandler.sendEmptyMessageDelayed(IS_ADD_WIDTH, 20);
                    } else {
                        x = width - center;
                        y = 30;
                        tX = width - center - 10;
                        degress = 90;
                        setEnabled(true);
                    }
                    break;
                case IS_REDUCE_WIDTH:
                    x -= speed;
                    y = x / x_y;
                    tX = x * tX_x;
                    degress = deg_x * x;
                    if (x >= center + speed) {
                        mHandler.sendEmptyMessageDelayed(IS_REDUCE_WIDTH, 20);
                    } else {
                        x = center;
                        y = 0;
                        tX = center * 2 + 5;
                        degress = 0;
                        setEnabled(true);
                    }
                    break;
            }
            requestLayout();
            invalidate();
        }
    };

    public void startAdd() {
        setEnabled(false);
        isAdd = false;
        mHandler.sendEmptyMessageDelayed(IS_ADD_WIDTH, 20);
    }

    public void startReduce() {
        Log.i("zoujiang", "变小");
        setEnabled(false);
        isAdd = true;
        mHandler.sendEmptyMessageDelayed(IS_REDUCE_WIDTH, 20);
    }

    public void start() {
        Log.i("zoujiang", "开始了动起来");
        if (isAdd) {
            startAdd();
        } else {
            startReduce();
        }
    }

}
