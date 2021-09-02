package com.impl.recoder.widget.recordviews;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;
import android.widget.OverScroller;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.impl.recoder.R;
import com.impl.recoder.RecordCallBack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: BaseAudioRecord
 * Description:
 */
public abstract class BaseAudioRecordView extends View {


    /**
     * 采样时间
     */
    protected int recordDelayMillis;

    /**
     * 录音时长 单位分钟
     */
    protected int recordTimeInMinutes = 1;

    /**
     * 录音采样频率 每秒钟采样个数
     */
    protected int recordSamplingFrequency = 10;
    /**
     * 是否显示刻度尺上
     */
    protected boolean showRule = true;
    /**
     * 是否显示刻度尺上的文字
     */
    protected boolean showRuleText = false;
    /**
     * 一格大刻度多少格小刻度
     */
    protected int intervalCount = 10;

    /**
     * 刻度间隔
     */
    protected int scaleIntervalLength = 18;

    /**
     * 尺子 小刻度 高
     */
    protected int smallScaleStrokeLength = 30;

    /**
     * 尺子 小刻度 宽
     */
    protected int smallScaleStrokeWidth = 3;
    /**
     * 尺子 大刻度 高
     */
    protected int bigScaleStrokeLength = (int) (smallScaleStrokeLength * 2.5);
    /**
     * 尺子 大刻度 宽
     */
    protected int bigScaleStrokeWidth = 5;

    /**
     * 刻度尺 底部直线颜色
     */
    protected @ColorInt
    int ruleHorizontalLineColor;

    /**
     * 刻度尺 底部直线 宽度
     */
    protected int ruleHorizontalLineStrokeWidth = 10;
    /**
     * 刻度尺 底部直线 高度
     */
    protected int ruleHorizontalLineHeight = bigScaleStrokeLength + 50;

    /**
     * 刻度尺 垂直刻度线的颜色
     */
    protected @ColorInt
    int ruleVerticalLineColor;


    /**
     * 刻度尺 上文字颜色
     */
    protected @ColorInt
    int ruleTextColor;

    /**
     * 刻度尺 上文字 大小
     */
    protected int ruleTextSize = 28;

    /**
     * 水平线 颜色
     */
    protected @ColorInt
    int middleHorizontalLineColor;

    /**
     * 水平线 stroke width
     */
    protected int middleHorizontalLineStrokeWidth = 5;

    /**
     * 垂直线 颜色
     */
    protected @ColorInt
    int middleVerticalLineColor;

    /**
     * 垂直线 stroke width
     */
    protected int middleVerticalLineStrokeWidth = 5;
    /**
     * 垂直线 两个圆的圆心 半径
     */
    protected int middleCircleRadius = 12;
    /**
     * 矩形 颜色
     */
    protected @ColorInt
    int rectColor;
    /**
     * 矩形倒影 颜色
     */
    protected @ColorInt
    int rectInvertColor;


    /**
     * 声波 采样样本 宽
     */
    protected int lineWidth = 8;

    /**
     * 底部文字颜色
     */
    protected @ColorInt
    int bottomTextColor;
    /**
     * 底部文字 大小
     */
    protected int bottomTextSize = 60;
    /**
     * 底部 文字区域 背景颜色
     */
    protected @ColorInt
    int bottomRectColor;

    /**
     * 是否显示上一段可删除
     */
    protected boolean showStopFlag = false;
    /**
     * 是否支持手势滑动
     */
    protected boolean canTouch = true;
    /**
     * 是否显示垂直竖线
     */
    protected boolean showVerticalLine = true;
    /**
     * 是否显示水平竖线
     */
    protected boolean showHorizontalLine = true;

    /**
     * 最小可滑动值
     */
    protected int minScrollX = 0;
    /**
     * 最大可滑动值
     */
    protected int maxScrollX = 0;

    /**
     * 正在录制
     */
    protected volatile boolean isRecording;


    /**
     * 控制滑动
     */
    protected OverScroller overScroller;
    /**
     * 速度获取
     */
    protected VelocityTracker velocityTracker;
    /**
     * 惯性最大速度
     */
    protected int maxVelocity;
    /**
     * 惯性最小速度
     */
    protected int minVelocity;

    protected long maxLength;

    protected RecordCallBack recordCallBack;

    /**
     * 矩形间距
     */
    protected int rectGap = 2;
    /**
     * 声波矩形 距离顶部垂直间距
     */
    protected int rectMarginTop = 50;
    protected List<RecordSampleLineModel> sampleLineList = new ArrayList<>();
    /**
     * 删除上一段的Index值
     */
    protected List<Integer> deleteIndexList = new ArrayList<>();


    protected volatile boolean isAutoScroll;

    protected float mLastX = 0;
    /**
     * 当前录音总时长
     */
    protected long currentRecordTime;

    /**
     * 是否在播放录音
     */
    private boolean isPlayingRecord = false;

    /**
     * 中心点的位置
     */
    protected float centerLineX = 0;


    protected long centerTimeMillis;

    /**
     * 采样最后的位置
     */
    protected int lineLocationX;


    protected boolean isTouching;
    protected long recordTimeInMillis;

    /**
     * 提前刻画量
     */
    protected int mDrawOffset;

    /**
     * 画布移动距离
     */
    protected float translateX = 0;

    /**
     * 垂直线x坐标
     */
    protected float translateVerticalLineX = middleCircleRadius / 2;


    protected Paint ruleHorizontalLinePaint = new Paint();
    protected Paint smallScalePaint = new Paint();
    protected Paint bigScalePaint = new Paint();
    protected TextPaint ruleTextPaint = new TextPaint();
    protected Paint middleHorizontalLinePaint = new Paint();
    protected Paint middleVerticalLinePaint = new Paint();
    protected Paint linePaint = new Paint();
    protected Paint lineInvertedPaint = new Paint();
    protected Paint lineDeletePaint = new Paint();
    protected TextPaint bottomTextPaint = new TextPaint();
    protected Paint bottomRectPaint = new Paint();


    public BaseAudioRecordView(Context context) {
        this(context, null);
    }

    public BaseAudioRecordView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseAudioRecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        init(context);
        initPaint();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AudioRecordView, 0, 0);
        recordTimeInMinutes = typedArray.getInteger(R.styleable.AudioRecordView_recordTimeInMinutes, recordTimeInMinutes);
        recordSamplingFrequency = typedArray.getInteger(R.styleable.AudioRecordView_recordSamplingFrequency, recordSamplingFrequency);
        showRule = typedArray.getBoolean(R.styleable.AudioRecordView_showRule, showRule);
        showRuleText = typedArray.getBoolean(R.styleable.AudioRecordView_showRuleText, showRuleText);
        intervalCount = typedArray.getInteger(R.styleable.AudioRecordView_intervalCount, intervalCount);
        scaleIntervalLength = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_scaleIntervalLength, scaleIntervalLength);

        smallScaleStrokeLength = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_smallScaleStrokeLength, smallScaleStrokeLength);
        smallScaleStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_smallScaleStrokeWidth, smallScaleStrokeWidth);
        bigScaleStrokeLength = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_bigScaleStrokeLength, bigScaleStrokeLength);
        bigScaleStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_bigScaleStrokeWidth, bigScaleStrokeWidth);


        ruleVerticalLineColor = typedArray.getColor(R.styleable.AudioRecordView_ruleVerticalLineColor, Color.DKGRAY);

        ruleHorizontalLineColor = typedArray.getColor(R.styleable.AudioRecordView_ruleHorizontalLineColor, Color.RED);
        ruleHorizontalLineStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_ruleHorizontalLineStrokeWidth, ruleHorizontalLineStrokeWidth);
        ruleHorizontalLineHeight = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_ruleHorizontalLineHeight, ruleHorizontalLineHeight);

        ruleTextColor = typedArray.getColor(R.styleable.AudioRecordView_ruleTextColor, Color.DKGRAY);
        ruleTextSize = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_ruleTextSize, ruleTextSize);

        middleHorizontalLineColor = typedArray.getColor(R.styleable.AudioRecordView_middleHorizontalLineColor, Color.RED);
        middleHorizontalLineStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_middleHorizontalLineStrokeWidth, middleHorizontalLineStrokeWidth);
        middleVerticalLineColor = typedArray.getColor(R.styleable.AudioRecordView_middleVerticalLineColor, Color.RED);
        middleVerticalLineStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_middleVerticalLineStrokeWidth, middleVerticalLineStrokeWidth);
        middleCircleRadius = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_middleCircleRadius, middleCircleRadius);


        rectColor = typedArray.getColor(R.styleable.AudioRecordView_rectColor, Color.RED);
        rectInvertColor = typedArray.getColor(R.styleable.AudioRecordView_rectInvertColor, Color.DKGRAY);
        rectGap = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_rectGap, rectGap);
        rectMarginTop = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_rectMarginTop, rectMarginTop);

        bottomTextColor = typedArray.getColor(R.styleable.AudioRecordView_bottomTextColor, Color.WHITE);
        bottomTextSize = typedArray.getDimensionPixelSize(R.styleable.AudioRecordView_bottomTextSize, bottomTextSize);

        bottomRectColor = typedArray.getColor(R.styleable.AudioRecordView_bottomRectColor, Color.YELLOW);
        showStopFlag = typedArray.getBoolean(R.styleable.AudioRecordView_showStopFlag, showStopFlag);
        canTouch = typedArray.getBoolean(R.styleable.AudioRecordView_canTouch, true);
        showVerticalLine = typedArray.getBoolean(R.styleable.AudioRecordView_showVerticalLine, true);
        showHorizontalLine = typedArray.getBoolean(R.styleable.AudioRecordView_showHorizontalLine, true);

        typedArray.recycle();
    }

    private void initPaint() {
        ruleHorizontalLinePaint.setAntiAlias(true);
        ruleHorizontalLinePaint.setStrokeWidth(ruleHorizontalLineStrokeWidth);
        ruleHorizontalLinePaint.setColor(ruleHorizontalLineColor);

        smallScalePaint.setStrokeWidth(smallScaleStrokeWidth);
        smallScalePaint.setColor(ruleVerticalLineColor);
        smallScalePaint.setStrokeCap(Paint.Cap.ROUND);

        bigScalePaint.setColor(ruleVerticalLineColor);
        bigScalePaint.setStrokeWidth(bigScaleStrokeWidth);
        bigScalePaint.setStrokeCap(Paint.Cap.ROUND);

        ruleTextPaint.setAntiAlias(true);
        ruleTextPaint.setColor(ruleTextColor);
        ruleTextPaint.setTextSize(ruleTextSize);
        ruleTextPaint.setTextAlign(Paint.Align.LEFT);

        middleHorizontalLinePaint.setAntiAlias(true);
        middleHorizontalLinePaint.setStrokeWidth(middleHorizontalLineStrokeWidth);
        middleHorizontalLinePaint.setColor(middleHorizontalLineColor);

        middleVerticalLinePaint.setAntiAlias(true);
        middleVerticalLinePaint.setStrokeWidth(middleVerticalLineStrokeWidth);
        middleVerticalLinePaint.setColor(middleVerticalLineColor);

        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(lineWidth);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setColor(rectColor);

        lineInvertedPaint.setAntiAlias(true);
        lineInvertedPaint.setStrokeWidth(lineWidth);
        lineInvertedPaint.setStrokeCap(Paint.Cap.ROUND);
        lineInvertedPaint.setColor(rectInvertColor);

        lineDeletePaint.setAntiAlias(true);
        lineDeletePaint.setStrokeWidth(lineWidth);
        lineDeletePaint.setStrokeCap(Paint.Cap.ROUND);
        lineDeletePaint.setColor(Color.parseColor("#F55A5A"));


        bottomTextPaint.setAntiAlias(true);
        bottomTextPaint.setColor(bottomTextColor);
        bottomTextPaint.setTextSize(bottomTextSize);
        bottomTextPaint.setTextAlign(Paint.Align.CENTER);

        bottomRectPaint.setAntiAlias(true);
        bottomRectPaint.setColor(bottomRectColor);
    }

    private void init(Context context) {
        recordTimeInMillis = TimeUnit.MINUTES.toMillis(recordTimeInMinutes);
        maxLength = TimeUnit.MINUTES.toSeconds(recordTimeInMinutes) * intervalCount * scaleIntervalLength;
        recordDelayMillis = 1000 / recordSamplingFrequency;
        lineWidth = (intervalCount * scaleIntervalLength) / recordSamplingFrequency - rectGap;
        overScroller = new OverScroller(context);
        velocityTracker = VelocityTracker.obtain();
        maxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        minVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
        mDrawOffset = scaleIntervalLength;
        checkAPILevel();
    }

    private void checkAPILevel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            setLayerType(LAYER_TYPE_NONE, null);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isPlayingRecord || isRecording || !canTouch) {
            return false;
        }
        isTouching = true;
        getParent().requestDisallowInterceptTouchEvent(true);
        float currentX = event.getX();
        //开始速度检测
        startVelocityTracker(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (!overScroller.isFinished()) {
                    overScroller.abortAnimation();
                }
                velocityTracker.clear();
                mLastX = currentX;
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = mLastX - currentX;
                mLastX = currentX;
                scrollBy((int) (moveX), 0);
                break;
            case MotionEvent.ACTION_UP:
                isTouching = false;
                //手指离开屏幕，开始处理惯性滑动Fling
                velocityTracker.computeCurrentVelocity(1000, maxVelocity);
                float velocityX = velocityTracker.getXVelocity();
                if (Math.abs(velocityX) > minVelocity) {
                    fling(-velocityX);
                }
                finishVelocityTracker();
                break;
            case MotionEvent.ACTION_CANCEL:
                isTouching = false;
                if (!overScroller.isFinished()) {
                    overScroller.abortAnimation();
                }
                finishVelocityTracker();
                break;
            default:
                break;
        }
        return true;
    }


    private void startVelocityTracker(MotionEvent event) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);
    }

    private void finishVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private void fling(float velocity) {
        overScroller.fling(getScrollX(), 0, (int) velocity, 0, minScrollX, maxScrollX, 0, 0);
    }

    @Override
    public void computeScroll() {
        //滑动处理
        if (isPlayingRecord || isRecording) {
            return;
        }
        if (overScroller.computeScrollOffset()) {
            scrollTo(overScroller.getCurrX(), overScroller.getCurrY());
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        if (x < minScrollX) {
            x = minScrollX;
        } else if (x > maxScrollX) {
            x = maxScrollX;
        }
        super.scrollTo(x, y);
        final int finalX = x;
        post(new Runnable() {
            @Override
            public void run() {
                if (recordCallBack != null) {
                    long centerStartTimeMillis;
                    if (finalX == minScrollX) {
                        centerStartTimeMillis = 0;
                    } else if (finalX == maxScrollX) {
                        centerStartTimeMillis = currentRecordTime;
                    } else {
                        centerStartTimeMillis = (long) (centerLineX * 1000L / (intervalCount * scaleIntervalLength));
                    }
                    recordCallBack.onScroll(centerStartTimeMillis);
                }

            }
        });
    }


    public float getTranslateX() {
        return translateX;
    }

    protected volatile boolean isStartRecordTranslateCanvas;

    public void setTranslateX(float translateX) {
        this.translateX = translateX;
        scrollTo((int) translateX, 0);
        if (isStartRecordTranslateCanvas) {
            translateVerticalLineX = getCenterVerticalLineXWhileTranslateRecord();
        }
        invalidate();
        onTick(getOnTickTranslateXWhileTranslateRecord());
    }

    /**
     * 在View移动的过程中，垂直中心线的位置
     *
     * @return 垂直中心线的位置
     */
    protected abstract float getCenterVerticalLineXWhileTranslateRecord();


    /**
     * View移动的过程中，所走过的距离
     *
     * @return 所走过的距离
     */
    protected abstract float getOnTickTranslateXWhileTranslateRecord();


    public float getTranslateVerticalLineX() {
        return translateVerticalLineX;
    }

    public void setTranslateVerticalLineX(float translateVerticalLineX) {
        this.translateVerticalLineX = translateVerticalLineX;
        invalidate();
        onTick(translateVerticalLineX);
    }

    /**
     * 采样打点
     *
     * @param translateX 移动距离
     */
    protected abstract void onTick(float translateX);

    /**
     * 开始录音
     */
    public abstract void startRecord();


    public void stopRecord() {
    }

    public void deleteLastRecord() {

        invalidate();
        if (recordCallBack != null) {
            recordCallBack.onRecordCurrent(currentRecordTime, currentRecordTime);
        }
    }


    protected float getLastSampleLineRightX() {
        return lineLocationX;
    }


    /**
     * 生成矩形
     *
     * @param percent 矩形高度 百分比
     */
    protected void makeSampleLine(float percent) {
        if (lineLocationX >= maxLength) {
            //超出采样时间
            stopRecord();
            return;
        }
        RecordSampleLineModel sampleLineModel = new RecordSampleLineModel();
        int rectBottom = getMeasuredHeight() / 2;
        int lineTop = (int) (rectBottom - (rectBottom - ruleHorizontalLineHeight - rectMarginTop) * percent);
        sampleLineModel.startX = lineLocationX + lineWidth / 2;
        sampleLineModel.stopX = sampleLineModel.startX;
        sampleLineModel.startY = lineTop;
        sampleLineModel.stopY = rectBottom;
        lineLocationX = lineLocationX + lineWidth + rectGap;
        sampleLineList.add(sampleLineModel);
    }

    protected void initDefaultSampleLine(@NonNull List<Float> sampleList) {
        if (!sampleList.isEmpty()) {
            for (Float percent : sampleList) {
                RecordSampleLineModel sampleLineModel = new RecordSampleLineModel();
                int rectBottom = getMeasuredHeight() / 2;
                int lineTop = (int) (rectBottom - (rectBottom - ruleHorizontalLineHeight - rectMarginTop) * percent);
                sampleLineModel.startX = lineLocationX + lineWidth / 2;
                sampleLineModel.stopX = sampleLineModel.startX;
                sampleLineModel.startY = lineTop;
                sampleLineModel.stopY = rectBottom;
                lineLocationX = lineLocationX + lineWidth + rectGap;
                sampleLineList.add(sampleLineModel);
            }
        }
        requestLayout();
    }

    /**
     * 获取采样点个数
     *
     * @return 获取采样点个数
     */
    protected int getSampleCount() {
        return sampleLineList.size();
    }


    /**
     * 初始化 可以滑动距离
     */
    protected abstract void setCanScrollX();


    public void setRecordCallBack(RecordCallBack recordCallBack) {
        this.recordCallBack = recordCallBack;
    }


    /**
     * 是否正在录音
     *
     * @return isRecording
     */
    public boolean isRecording() {
        return isRecording;
    }


    public void startPlayRecord(long timeInMillis) {
        if (!isPlayingRecord) {
            isTouching = false;
            stopRecord();
            if (timeInMillis <= 0) {
                //从0开始播放
                scrollTo(minScrollX, 0);
            }
            startPlayTranslateCanvas();
            if (recordCallBack != null) {
                if (timeInMillis <= 0) {
                    recordCallBack.onStartPlayRecord(0);
                } else {
                    recordCallBack.onStartPlayRecord(centerTimeMillis);
                }
            }

        }
    }

    /**
     * 暂停播放录音
     */
    public void stopPlayRecord() {
        if (isPlayingRecord) {
            isTouching = false;
            isPlayingRecord = false;
            isAutoScroll = false;
            animator.removeAllListeners();
            animator.cancel();
            if (recordCallBack != null) {
                recordCallBack.onStopPlayRecode();
            }

        }
    }


    protected ObjectAnimator animator;

    private void startPlayTranslateCanvas() {
        float startX = getScrollX();
        //小于半屏的时候，要重新计算偏移量，因为有个左滑的动作
        float endX = maxScrollX;
        float dx = Math.abs(maxScrollX - getScrollX());
        final long duration = (long) (1000 * dx / (recordSamplingFrequency * (lineWidth + rectGap)));
        animator = ObjectAnimator.ofFloat(this, "translateX", startX, endX);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(duration);
        isPlayingRecord = true;
        isAutoScroll = true;
        animator.removeAllListeners();
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                //回调播放结束
                invalidate();
                //播放结束
                stopPlayRecord();
                animator.removeAllListeners();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //回调播放结束
                invalidate();
                //播放结束
                stopPlayRecord();
                animator.removeAllListeners();
            }
        });
        animator.start();
    }


    /**
     * 是否正在播放录音
     *
     * @return 是否正在播放录音
     */
    public boolean isPlayingRecord() {
        return isPlayingRecord;
    }

    public int getRecordSamplingFrequency() {
        return recordSamplingFrequency;
    }

    public boolean hasTwoDeleteFragment() {
        return deleteIndexList.size() > 1;
    }

    /**
     * 重置
     */
    public void reset() {
        stopRecord();
        stopPlayRecord();
        centerLineX = 0;
        currentRecordTime = 0;
        mLastX = 0;
        sampleLineList.clear();
        deleteIndexList.clear();
        lineLocationX = 0;
        minScrollX = 0;
        maxScrollX = 0;
        translateX = 0;
        translateVerticalLineX = middleCircleRadius / 2;
        scrollTo(minScrollX, 0);
        invalidate();
        if (recordCallBack != null) {
            recordCallBack.onRecordCurrent(currentRecordTime, currentRecordTime);
        }
    }
}
