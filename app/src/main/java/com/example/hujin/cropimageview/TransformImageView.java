package com.example.hujin.cropimageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

/**
 * @author hujin
 * @package com.example.hujin.cropimageview
 * @description: 图片移动缩放
 * @email xiaoqingxu0502@gamil.com
 * @since 2018/7/22 下午6:16
 */
public class TransformImageView extends View {
    private Context mContext;

    protected boolean isInitDrawFinish = false;

    /**
     * 记录两指同时放在屏幕上时，中心点的坐标值
     */
    private float mCenterPointX;
    private float mCenterPointY;

    private float mTotalTranslateX;
    private float mTotalTranslateY;

    private float mCurrentBitmapwWidth;
    private float mCurrentBitmapHeight;

    private int mCurrentStatus = 0;
    private static final int STATUS_INIT = 1;
    private static final int STATUS_CHANGE = 2;

    private float mFocusX = 0.f;
    private float mFocusY = 0.f;

    private ScaleGestureDetector mScaleDetector;
    private float mInitScale;
    private float mMaxScale;
    private float mScaledRatio;

    /******************************** 子类需要的 *****************************/
    protected float mTotalScale;

    protected float mBitmapLeft;
    protected float mBitmapTop;
    protected float mBitmapRight;
    protected float mBitmapBottom;

    protected Bitmap mSourceBitmap;

    protected int mWidth;
    protected int mHeight;
    protected int mMoveLimitLeft;
    protected int mMoveLimitTop;
    protected int mMoveLimitRight;
    protected int mMoveLimitBottom;

    protected Matrix mMatrix = new Matrix();

    public TransformImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCurrentStatus = STATUS_INIT;
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        mContext = context;
        mScaleDetector = new ScaleGestureDetector(mContext, new ScaleListener());

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mWidth = getWidth();
            mHeight = getHeight();
            mMoveLimitRight = mWidth;
            mMoveLimitBottom = mHeight;
        }
    }

    /**
     * 将待展示的图片设置进来。
     *
     * @param bitmap 待展示的Bitmap对象
     */
    public void setBitmap(Bitmap bitmap) {
        mSourceBitmap = bitmap;
        if (mSourceBitmap == null) {
            throw new RuntimeException("bitmap is null");
        }
        invalidate();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (mCurrentStatus == STATUS_CHANGE) {
                // 每次缩放倍数
                mScaledRatio = detector.getScaleFactor();
                mTotalScale *= mScaledRatio;
                // 控制图片的缩放范围
                mTotalScale = Math.max(mInitScale, Math.min(mTotalScale, mMaxScale));
                //mScaledRatio是用来在图片缩放时，计算位移的，如果图片没有缩放，mScaledRatio始终为1，避免错误计算。
                if (mTotalScale == mMaxScale) {
                    mScaledRatio = 1;
                }
            }
            return true;
        }
    }

    /**
     * 用于图片移动
     */
    protected int mLastPointerCount;
    protected float mMoveLastX;
    protected float mMoveLastY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isInitDrawFinish) {
            return true;
        }

        mScaleDetector.onTouchEvent(event);

        float xTranslate = 0, yTranslate = 0;
        // 拿到触摸点的个数
        final int pointerCount = event.getPointerCount();
        // 得到多个触摸点的x与y均值
        for (int i = 0; i < pointerCount; i++) {
            xTranslate += event.getX(i);
            yTranslate += event.getY(i);
        }
        xTranslate = xTranslate / pointerCount;
        yTranslate = yTranslate / pointerCount;
        /**
         * 每当触摸点发生变化时，重置mLasX , mMoveLastY
         */
        if (pointerCount != mLastPointerCount) {
            mMoveLastX = xTranslate;
            mMoveLastY = yTranslate;
        }
        mLastPointerCount = pointerCount;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                mCurrentStatus = STATUS_CHANGE;
                if (event.getPointerCount() == 2) {
                    centerPointBetweenFingers(event);
                }
                float dX = xTranslate - mMoveLastX;
                float dY = yTranslate - mMoveLastY;
                mFocusX = dX;
                mFocusY = dY;
                mMoveLastX = xTranslate;
                mMoveLastY = yTranslate;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLastPointerCount = 0;
                break;
            default:
                break;

        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        switch (mCurrentStatus) {
            case STATUS_INIT:
                initBitmap(canvas);
                break;
            case STATUS_CHANGE:
                change(canvas);
                break;
            default:
                break;

        }
        canvas.restore();
    }

    /**
     * 避免float精度损失引起误差
     */
    float tatalScale = .0f;

    /**
     * @Description:对图片进行缩放和移动。
     */
    protected void change(Canvas canvas) {
        mMatrix.reset();
        // 将图片按总缩放比例进行缩放
        mMatrix.postScale(mTotalScale, mTotalScale);
        //图片变化后的宽度
        float scaledWidth = mSourceBitmap.getWidth() * mTotalScale;
        //图片变化后的高度
        float scaledHeight = mSourceBitmap.getHeight() * mTotalScale;
        //当前图片的宽度
        mCurrentBitmapwWidth = scaledWidth;
        //当前图片的高度
        mCurrentBitmapHeight = scaledHeight;
        // 缩放后对图片进行偏移，以保证缩放后中心点位置不变
        float translateX;
        float translateY;
        //缩放后的图片宽度小于的控件的宽度时，x基于控件中心缩放
        int limitWidth = mMoveLimitRight - mMoveLimitLeft;
        if (scaledWidth < limitWidth) {
            translateX = (mWidth - scaledWidth) / 2f;
        } else {
            //推到过程：假设被放大的图片是一个矩形，左上角坐标为x0,y0,基点为x1,y1,图形被放大的倍数为q，求放大后的左上角坐标为x2,y2,现在我们要求这个x2,y2。根据图形可以得出公式：
            // [(x0 - x2) + (x1 - x0)] / (x1 -x0) = q，然后就可以求出坐标x2的值，同理可以求出y2。x2和y2即图片需要移动的距离。
            translateX = mTotalTranslateX * mScaledRatio + mCenterPointX * (1 - mScaledRatio) + mFocusX;
            //避免float的精度损失引起误差
            if (tatalScale == mTotalScale) {
                translateX = mTotalTranslateX + mFocusX;
            }
            // 进行边界检查，不允许将图片拖出边界
            if (translateX > mMoveLimitLeft) {
                //x方向上,左边界检查
                translateX = mMoveLimitLeft;
            } else if (translateX < mMoveLimitRight - scaledWidth) {
                //x方向上,右边界检查
                translateX = mMoveLimitRight - scaledWidth;
            }

        }
        //缩放后的图片高度小于限制的高度时，y基于控件中心缩放
        int limitHeight = mMoveLimitBottom - mMoveLimitTop;
        if (scaledHeight < limitHeight) {
            translateY = (mHeight - scaledHeight) / 2f;
        } else {
            translateY = mTotalTranslateY * mScaledRatio + mCenterPointY * (1 - mScaledRatio) + mFocusY;
            if (tatalScale == mTotalScale) {
                translateY = mTotalTranslateY + mFocusY;
            }
            if (translateY > mMoveLimitTop) {
                //y方向上，上边界检查
                translateY = mMoveLimitTop;
            } else if (translateY < mMoveLimitBottom - scaledHeight) {
                //y方向上，下边界检查
                translateY = mMoveLimitBottom - scaledHeight;
            }

        }
        mMatrix.postTranslate(translateX, translateY);
        mTotalTranslateX = translateX;
        mTotalTranslateY = translateY;
        //避免float精度损失引起误差
        tatalScale = mTotalScale;
        //绘制
        canvas.drawBitmap(mSourceBitmap, mMatrix, null);
        computeBoundry(mMatrix);
    }

    /**
     * @Description:初始化预览图，居中显示。
     */
    private void initBitmap(Canvas canvas) {
        if (mSourceBitmap != null) {
            //重置当前Matrix(将当前Matrix重置为单位矩阵)
            mMatrix.reset();
            //获取图片实际宽高
            int bitmapWidth = mSourceBitmap.getWidth();
            int bitmapHeight = mSourceBitmap.getHeight();
            // mWidth为控件宽，产品要求：将图片宽度充满，高度等比缩放。
            float ratio = mWidth / (bitmapWidth * 1.0f);
            mMatrix.postScale(ratio, ratio);
            // mHeight为控件高,在纵坐标方向上进行偏移，以保证图片居中显示
            float translateY = (mHeight - (bitmapHeight * ratio)) / 2f;
            mMatrix.postTranslate(0, translateY);
            //缩放倍数
            mScaledRatio = 1;
            //记录图片在矩阵上的横向偏移值
            mTotalTranslateX = 0;
            //记录图片在矩阵上的纵向偏移值
            mTotalTranslateY = translateY;
            //记录图片在矩阵上的总缩放比例
            mTotalScale = mInitScale = ratio;
            //图片最大尺寸
            mMaxScale = mInitScale * 4;
            //当前图片的宽
            mCurrentBitmapwWidth = bitmapWidth * mInitScale;
            //当前图片的高
            mCurrentBitmapHeight = bitmapHeight * mInitScale;
            //绘制图片
            canvas.drawBitmap(mSourceBitmap, mMatrix, null);
            //计算图片的四个顶点坐标,以便涂鸦时候的边界判断.
            computeBoundry(mMatrix);
            isInitDrawFinish = true;
        }
    }

    /**
     * 计算两个手指之间中心点的坐标。
     *
     * @param event
     */
    private void centerPointBetweenFingers(MotionEvent event) {
        float xPoint0 = event.getX(0);
        float yPoint0 = event.getY(0);
        float xPoint1 = event.getX(1);
        float yPoint1 = event.getY(1);
        mCenterPointX = (xPoint0 + xPoint1) / 2;
        mCenterPointY = (yPoint0 + yPoint1) / 2;

    }

    /**
     * @param matrix 矩阵
     * @Description:计算顶点坐标
     */
    protected void computeBoundry(Matrix matrix) {
        RectF rectF = new RectF(0, 0, mSourceBitmap.getWidth(), mSourceBitmap.getHeight());
        matrix.mapRect(rectF);
        mBitmapLeft = rectF.left;
        mBitmapTop = rectF.top;
        mBitmapRight = rectF.right;
        mBitmapBottom = rectF.bottom;
    }

    protected void setMoveLimitLeft(int limitLeft) {
        mMoveLimitLeft = limitLeft;
    }

    protected void setMoveLimitTop(int limitTop) {
        mMoveLimitTop = limitTop;
    }

    protected void setMoveLimitRight(int limitRight) {
        mMoveLimitRight = limitRight;
    }

    protected void setMoveLimitBottom(int limitBottom) {
        mMoveLimitBottom = limitBottom;
    }


}
