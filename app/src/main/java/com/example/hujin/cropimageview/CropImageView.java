package com.example.hujin.cropimageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;

/**
 * @author hujin
 * @package com.example.hujin.cropimageview
 * @description: 裁截图片
 * @email xiaoqingxu0502@gamil.com
 * @since 2018/7/22 下午6:19
 */
public class CropImageView extends TransformImageView{
    private RectF mFocusRect = new RectF();

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        cropImage(canvas);
    }


    private void cropImage(Canvas canvas) {
        Paint borderPaint = new Paint();
        Path focusPath = new Path();
        //根据需要设置不同宽度
        int lineWidth = 4;
        int left = lineWidth;
        int top = (mHeight - mWidth) / 2 + lineWidth;
        int right = mWidth - lineWidth;
        int bottom = (mHeight + mWidth) / 2 - lineWidth;
        canvas.save();
        mFocusRect.set(left, top, right, bottom);
        focusPath.addRect(mFocusRect, Path.Direction.CCW);
        borderPaint.setColor(Color.parseColor("#ffffff"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4);
        borderPaint.setAntiAlias(true);
        canvas.clipPath(focusPath, Region.Op.DIFFERENCE);
        canvas.drawColor(Color.parseColor("#8C000000"));
        canvas.drawPath(focusPath, borderPaint);
        canvas.restore();
        setMoveLimitLeft(left);
        setMoveLimitTop(top);
        setMoveLimitRight(right);
        setMoveLimitBottom(bottom);
    }

    /**
     * @return 裁剪后的Bitmap
     */
    public Bitmap getCropBitmap() {
        return makeCropBitmap(mSourceBitmap, mFocusRect);
    }

    /**
     * @param bitmap          需要裁剪的图片
     * @param focusRect       中间需要裁剪的矩形区域
     * @return 裁剪后的图片的Bitmap
     */
    private Bitmap makeCropBitmap(Bitmap bitmap, RectF focusRect) {
        if (bitmap == null) {
            return null;
        }
        int left = (int) ((focusRect.left - mBitmapLeft) / mTotalScale);
        int top = (int) ((focusRect.top - mBitmapTop) / mTotalScale);
        int width = (int) (focusRect.width() / mTotalScale);
        int height = (int) (focusRect.height() / mTotalScale);

        if (left < 0) {
            left = 0;
        }
        if (top < 0) {
            top = 0;
        }
        if (left + width > bitmap.getWidth()) {
            width = bitmap.getWidth() - left;
        }
        if (top + height > bitmap.getHeight()) {
            height = bitmap.getHeight() - top;
        }
        try {
            bitmap = Bitmap.createBitmap(bitmap, left, top, width, height);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return bitmap;
    }
}
