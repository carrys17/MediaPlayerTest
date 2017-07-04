package com.example.shang.mediaplayertest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by shang on 2017/7/4.
 */
// 这个是音乐的示波器的View界面。提供3种波形：块状波形、柱状波形、曲线波形
public class MyVisualizerView extends View {

    private byte[] bytes;// 保存波形抽样点的值
    private Paint paint = new Paint();
    private Rect rect = new Rect();
    private int type = 0;
    private float[] points;

    public MyVisualizerView(Context context) {
        super(context);
        bytes = null;
        paint.setStrokeWidth(1f); //画笔宽度
        paint.setColor(Color.GREEN);
        paint.setAntiAlias(true); // 抗锯齿
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction()!=MotionEvent.ACTION_DOWN){
            return false;
        }
        type++; // 切换显示波形
        if (type>=3){
            type=0;
        }
        return true;
    }

    // 参数参入的是波形数据
    void updateVisualizer(byte[] bt){
        bytes = bt;
        invalidate(); // 通知组件重绘制自己
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bytes == null){
            return;
        }
        //  将背景绘制成白色
        canvas.drawColor(Color.WHITE);
        rect.set(0,0,getWidth(),getHeight());

        switch (type) {
            //  绘制块状的波形图
            case 0:
                for (int i = 0;i<bytes.length-1;i++){
                    float left = rect.width()*i / (bytes.length-1);
                    float top = rect.height() - (byte)(bytes[i+1]+128)*rect.height()/128;
                    float right = left + 1;
                    float bottom = rect.height();
                    canvas.drawRect(left,top,right,bottom,paint);
                }
                break;

            //  绘制柱状的波形图(每隔18个抽样点绘制一个矩形)
            case 1:
                for (int i = 0;i<bytes.length-1;i +=18){
                    float left = rect.width()*i / (bytes.length-1);
                    float top = rect.height() - (byte)(bytes[i+1]+128)*rect.height()/128;
                    float right = left + 6; // 宽度为6个抽样点
                    float bottom = rect.height();
                    canvas.drawRect(left,top,right,bottom,paint);
                }
                break;

            //  绘制曲线波形图
            case 2:
                if (points == null || points.length < bytes.length*4){
                    points = new float[bytes.length*4];
                }
                for (int i = 0;i < bytes.length-1; i++){
                    points[i*4] = rect.width()*i / (bytes.length-1);
                    points[i*4+1] = (rect.height()/2) + ((byte)(bytes[i]+128))*128 / (rect.height()/2);
                    points[i*4+2] = rect.width()*(i+1) / (bytes.length-1);
                    points[i*4+3] = (rect.height()/2) + ((byte)(bytes[i+1]+128))*128 / (rect.height()/2);
                }
                canvas.drawLines(points,paint);
                break;

        }
    }
}
