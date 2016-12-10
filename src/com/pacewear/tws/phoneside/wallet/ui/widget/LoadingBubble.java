
package com.pacewear.tws.phoneside.wallet.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.plugindemo.R;

import qrom.component.log.QRomLog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class LoadingBubble extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "LoadingBubble";

    private DrawThread mDrawThread = null;

    private Context mContext = null;
    /**
     * 帧率
     */
    private static final int FPS = 30;

    /**
     * 每帧周期
     */
    private static final long FRAME_PERIOID = 1000 / FPS;

    private boolean mRunning = false;

    private int mBackgroundColor = 0;

    private int mWidth = 0;

    private float mCircleCenterX = 0.f;

    private float mCircleCenterY = 0.f;

    private float mCircleRadius = 0.f;

    private float mCircleStrokeWidth = 0.f;

    private float mCircleMarginTop = 0.f;

    private String mLoadingNotice = null;

    private float mBubbleRadius = 0.f;

    private Random mRandom = new Random(System.currentTimeMillis());

    private Bitmap mLoadingRoundBg = null;

    public LoadingBubble(Context context) {
        this(context, null);
    }

    public LoadingBubble(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        getHolder().addCallback(this);
        // 支持surface透明设置
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        QRomLog.d(TAG, "surfaceChanged format:" + format + " width:" + width + " height:" + height);

        mWidth = width;
        mCircleCenterX = mWidth / 2 + 0.5f;

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        QRomLog.d(TAG, "surfaceCreated");
        init();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        QRomLog.d(TAG, "surfaceDestroyed");
        mRunning = false;
        // mValueAnimator.cancel();
        if (mLoadingRoundBg != null) {
            mLoadingRoundBg.recycle();
            mLoadingRoundBg = null;
        }
    }

    private static final String PERCENTAGE_SYMBOL = "%";

    private LinearGradient mLinearGradient = null;

    private Paint mCirclePaint = null;
    private Paint mPercentPaint = null;
    private Paint mSymbolPaint = null;
    private Paint mNoticePaint = null;
    private Paint mBubblePaint = null;

    // TODO
    private static float PERCENTAGE_TEXT_SIZE = 250.f;

    // TODO
    private static float SYMBOL_TEXT_SIZE = 80.f;

    private float mPercentagePerWidth = 0.f;

    private float mSymbolWidth = 0.f;

    private float mPercentageStartX = 0.f;

    private static final String PERCENTAGE_SAMPLE = "13";

    private int mPercentage = 0;

    private String mPerCentageString = "0";

    private float mDegrees = 0;

    private long mCurrentTimeMillis = 0;

    private ArrayList<Bubble> mBubbles = new ArrayList<Bubble>();

    private static final int BUBBLE_NUM = 20;

    private float mPercentageOffset = 0.f;

    private float mNoticeTextOffset = 0.f;

    private float mSymbolOffset = 0.f;

    private float mPercentageCenterOffset = 0.f;

    // private ValueAnimator mValueAnimator = null;

    public void setPercentTage(int percent) {

        if (!mRunning) {
            return;
        }
        if (percent >= 100) {
            percent = 99;
        }
        mPercentage = percent;
    }

    public void updatePercentage(int percentage) {

        if (percentage > 100) {
            percentage = 100;
        } else if (percentage < 0) {
            percentage = 0;
        }

        float totalWidth = 0.f;

        mPerCentageString = String.valueOf(percentage);
        totalWidth = mPercentagePerWidth * mPerCentageString.length() + mSymbolWidth * 1.5f;

        mPercentageStartX = totalWidth / 2.f - mSymbolWidth * 1.5f + 0.5f;
    }

    public void setCustomLoadingNotice(String notice) {
        mLoadingNotice = notice;
    }

    private void init() {
        if (mLoadingNotice == null) {
            mLoadingNotice = mContext.getString(R.string.wallet_loading_circle_notice);
        }
        mBackgroundColor = getResources().getColor(R.color.wallet_overall_background);
        mLoadingRoundBg = BitmapFactory.decodeResource(getResources(),
                R.drawable.wallet_loading_roud);
        mCircleRadius = getResources().getDimension(R.dimen.wallet_loading_circle_radius);
        mCircleMarginTop = getResources().getDimension(R.dimen.wallet_loading_circle_margin_top);
        mCircleCenterY = mCircleMarginTop + mCircleRadius;
        mCircleStrokeWidth = getResources()
                .getDimension(R.dimen.wallet_loading_circle_srroke_width);
        mBubbleRadius = getResources().getDimension(R.dimen.wallet_loading_bubble_radius);
        mLinearGradient = new LinearGradient(-mCircleRadius, 0, mCircleRadius, 0,
                Color.parseColor("#4CD9C1"),
                Color.parseColor("#46DC5F"),
                Shader.TileMode.REPEAT);

        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(mCircleStrokeWidth);

        mPercentPaint = new Paint();
        mPercentPaint.setAntiAlias(true);
        mPercentPaint.setColor(Color.GREEN);
        mPercentPaint.setStyle(Paint.Style.FILL);
        PERCENTAGE_TEXT_SIZE = mContext.getResources().getDimension(
                R.dimen.wallet_loading_cricle_progress_text_size);
        mPercentPaint.setTextSize(PERCENTAGE_TEXT_SIZE);
        mPercentPaint.setTextAlign(Paint.Align.CENTER);

        Rect rect = new Rect();
        mPercentPaint.getTextBounds(PERCENTAGE_SAMPLE, 0, PERCENTAGE_SAMPLE.length(), rect);
        mPercentagePerWidth = rect.width() / (PERCENTAGE_SAMPLE.length() * 1.0f);

        mSymbolPaint = new Paint();
        mSymbolPaint.setAntiAlias(true);
        mSymbolPaint.setColor(Color.GREEN);
        mSymbolPaint.setStyle(Paint.Style.FILL);
        SYMBOL_TEXT_SIZE = mContext.getResources().getDimension(
                R.dimen.wallet_loading_cricle_percent_text_size);
        mSymbolPaint.setTextSize(SYMBOL_TEXT_SIZE);
        mSymbolPaint.setTextAlign(Paint.Align.LEFT);
        mSymbolPaint.getTextBounds(PERCENTAGE_SYMBOL, 0, PERCENTAGE_SYMBOL.length(), rect);
        mSymbolWidth = rect.width() / (PERCENTAGE_SYMBOL.length() * 1.0f);

        mNoticePaint = new Paint();
        mNoticePaint.setAntiAlias(true);
        mNoticePaint.setColor(Color.WHITE);
        mNoticePaint.setStyle(Paint.Style.FILL);
        mNoticePaint.setTextSize(mContext.getResources().getDimension(
                R.dimen.wallet_loading_circle_notice_text_size));
        mNoticePaint.setTextAlign(Paint.Align.CENTER);

        mBubblePaint = new Paint();
        mBubblePaint.setAntiAlias(true);
        mBubblePaint.setStyle(Paint.Style.FILL);
        mBubblePaint.setColor(getResources().getColor(R.color.wallet_loading_bubble));

        mPercentageOffset = mContext.getResources().getDimension(
                R.dimen.wallet_loading_circle_percent_center_offset_size);
        mNoticeTextOffset = mContext.getResources().getDimension(
                R.dimen.wallet_loading_circle_notice_center_offset_size);
        mPercentageCenterOffset = mContext.getResources().getDimension(
                R.dimen.wallet_loading_percentage_center_x_offest);
        mSymbolOffset = mContext.getResources().getDimension(
                R.dimen.wallet_loading_symbol_offest);

        // mValueAnimator = ValueAnimator.ofInt(0, 99);
        // mValueAnimator.setDuration(1000 * 2 * 60);
        // mValueAnimator.addUpdateListener(new AnimatorUpdateListener() {
        //
        // @Override
        // public void onAnimationUpdate(ValueAnimator arg0) {
        // // TODO Auto-generated method stub
        // mPercentage = (Integer) arg0.getAnimatedValue();
        // }
        // });

        mDrawThread = new DrawThread(this, getHolder());
        mRunning = true;
        // mValueAnimator.start();
        mDrawThread.start();

        for (int i = 0; i < BUBBLE_NUM; i++) {
            mBubbles.add(new Bubble());
        }
    }

    private void doDraw(Canvas canvas) {
        QRomLog.d(TAG, "doDraw");

        canvas.translate(mCircleCenterX, mCircleCenterY);
        // 此处清理颜色，否则会造成实际颜色值与设值不一
        canvas.drawColor(mBackgroundColor, Mode.CLEAR);

        Iterator<Bubble> iterator = mBubbles.iterator();
        Bubble bubble = null;
        while (iterator.hasNext()) {
            bubble = iterator.next();
            bubble.doDraw(canvas);
        }

        canvas.rotate(mDegrees * 5, 0, 0);
        // mCirclePaint.setStyle(Paint.Style.FILL);
        // mCirclePaint.setColor(mBackgroundColor);
        // mCirclePaint.setShader(null);
        // canvas.drawCircle(0, 0, mCircleRadius, mCirclePaint);
        // mCirclePaint.setStyle(Paint.Style.STROKE);
        // mCirclePaint.setShader(mLinearGradient);
        // canvas.drawCircle(0, 0, mCircleRadius, mCirclePaint);
        // 用代码画圆圈,会出现一定的边缘波动，此处直接改为图片绘制
        canvas.drawBitmap(mLoadingRoundBg, -mCircleRadius, -mCircleRadius, null);
        canvas.rotate(-mDegrees * 5, 0, 0);

        // 显示百分比不是一个好的选择
        if (true) {
            canvas.drawText(mPerCentageString, -mPercentageCenterOffset, mPercentageOffset,
                    mPercentPaint);
            canvas.drawText(PERCENTAGE_SYMBOL, mPercentagePerWidth - mSymbolOffset,
                    mPercentageOffset,
                    mSymbolPaint);
        }

        canvas.drawText(mLoadingNotice, 0, mNoticeTextOffset, mNoticePaint);

        // canvas.rotate(mDegrees, 0, 0);
        // canvas.drawCircle(mCircleRadius + mPercentage, 0, 15.f, mBubblePaint);
        // TODO remove
        mDegrees++;
        // mPercentage++;
        // mPercentage %= 101;
        updatePercentage(mPercentage);
    }

    public class Bubble {

        private static final long MAX_ALIVE_TIME = 1800;

        private float mBubbleDegrees = 0.f;

        private float mRadius = 0.f;

        private int mAlpah = 0;

        private long mAliveBegin = 0;

        private long mAliveEnd = 0;

        public Bubble() {
            newborn();
        }

        private void newborn() {
            mBubbleDegrees = mRandom.nextInt(360);
            mRadius = mBubbleRadius * (0.3f + 0.7f * mRandom.nextFloat());
            mAlpah = mRandom.nextInt(255);
            mAliveBegin = mCurrentTimeMillis + (long) (MAX_ALIVE_TIME * mRandom.nextFloat());
            mAliveEnd = mAliveBegin + (int) (MAX_ALIVE_TIME * (0.25 + 0.75 * mRandom.nextFloat()));
        }

        public void doDraw(Canvas canvas) {

            if (mCurrentTimeMillis >= mAliveBegin && mCurrentTimeMillis <= mAliveEnd) {
                float ratio = ((float) (mCurrentTimeMillis - mAliveBegin))
                        / (mAliveEnd - mAliveBegin);

                canvas.rotate(mBubbleDegrees, 0, 0);

                mBubblePaint.setAlpha((int) (mAlpah * (1.f - ratio)));

                canvas.drawCircle(mCircleRadius + mCircleMarginTop * ratio, 0, mRadius,
                        mBubblePaint);

                canvas.rotate(-mBubbleDegrees, 0, 0);
            } else {
                newborn();
            }
        }
    }

    public class DrawThread extends Thread {

        private LoadingBubble mLoadingBubble = null;

        private SurfaceHolder mSurfaceHolder = null;

        public DrawThread(LoadingBubble loadingBuddle, SurfaceHolder holder) {
            mLoadingBubble = loadingBuddle;
            mSurfaceHolder = holder;
        }

        @Override
        public void run() {
            super.run();

            long current = 0;
            long duration = 0;
            Canvas canvas = null;

            while (mLoadingBubble.mRunning) {

                current = System.currentTimeMillis();
                duration = current - mCurrentTimeMillis;

                if (duration >= FRAME_PERIOID) {
                    mCurrentTimeMillis = current;
                    canvas = mSurfaceHolder.lockCanvas();
                    if (canvas != null) {
                        mLoadingBubble.doDraw(canvas);
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                    }
                    duration = FRAME_PERIOID - (System.currentTimeMillis() - mCurrentTimeMillis);
                }
                if (duration < 0) {
                    duration = FRAME_PERIOID;
                }

                try {
                    Thread.sleep(duration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
