package com.crowdrobo.robohead;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.crowdrobo.robohead.face.NoConnectionScreen;

/**
 * Created by rory on 30/01/16.
 */
public class PixelMatrix extends View {
    private static final int CELL_COUNT = 16;
    private static final int BACKGROUND_COLOUR = 0xFCBC00;

    private float mCellPadding;
    private Paint mPixelPaint;
    private float mCellHeight;
    private float mCellWidth;
    private PixelInterface mPixelInterface;

    public PixelMatrix(Context context) {
        super(context);
        init(context);
    }

    public PixelMatrix(Context context,AttributeSet attrs) {
        super(context,attrs);
        init(context);
    }

    public PixelMatrix(Context context,AttributeSet attrs,int defStyleAttr) {
        super(context,attrs,defStyleAttr);
        init(context);
    }

    public PixelMatrix(Context context,AttributeSet attrs,int defStyleAttr,int defStyleRes) {
        super(context,attrs,defStyleAttr,defStyleRes);
        init(context);
    }

    private void init(Context context) {

        mPixelInterface = new NoConnectionScreen();
        mPixelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPixelPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        int width = getMeasuredWidth();
        int heigth = getMeasuredHeight();

        int dimen = (width > heigth) ? heigth : width;

        setMeasuredDimension(dimen,dimen);
    }

    @Override
    protected void onSizeChanged(int w,int h,int oldw,int oldh) {
        super.onSizeChanged(w,h,oldw,oldh);

//            mVertCellCount = h / CELL_COUNT;
//            mHorizCellCount = w / CELL_COUNT;

//            mVertCellCount = mHorizCellCount = CELL_COUNT;


        mCellHeight = h / CELL_COUNT;
        mCellWidth = w / CELL_COUNT;

        mCellPadding = mCellHeight * 0.01f;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int cellColour;
        for (int i = 0; i < CELL_COUNT; i++) {
            for (int j = 0; j < CELL_COUNT; j++) {
                cellColour = mPixelInterface.shouldDrawPixel(i, j) ? Color.BLACK : BACKGROUND_COLOUR;
                mPixelPaint.setColor(cellColour);
                canvas.drawRect(i * mCellWidth + mCellPadding,j * mCellHeight + mCellPadding,(i + 1) * mCellWidth - mCellPadding,(j + 1) * mCellHeight - mCellPadding,mPixelPaint);
            }
        }

    }

    public void setFace(PixelInterface pixelInterface) {
        this.mPixelInterface = pixelInterface;
        invalidate();
    }
}
