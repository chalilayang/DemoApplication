package com.example.mi.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

/**
 * TODO: document your custom view class.
 */
public class SketchView extends View {

    private static final String TAG = "SketchView";

    public static final int OUTER_CIRCLE_WIDTH = 6;
    public static final int OUTER_CIRCLE_RADIUS = 378;

    private static final int TRACK_WIDTH = 122;

    private static final int ANCHOR_POINT_TRACK_TOP = 475;
    private static final int ANCHOR_POINT_SEC_TRACK_TOP = 455;
    private static final int ANCHOR_POINT_THR_TRACK_TOP = 435;

    private static final int OUTER_SEC_CIRCLE_WIDTH = 4;
    private static final int OUTER_THR_CIRCLE_WIDTH = 4;

    private static final int OUTER_SEC_CIRCLE_RADIUS = OUTER_CIRCLE_RADIUS - 20;
    private static final int OUTER_THR_CIRCLE_RADIUS = OUTER_CIRCLE_RADIUS - 40;

    private static final int OUTER_CIRCLE_CENTER_X = OUTER_CIRCLE_WIDTH / 2 + OUTER_CIRCLE_RADIUS;
    private static final int OUTER_CIRCLE_CENTER_Y = OUTER_CIRCLE_WIDTH / 2 + OUTER_CIRCLE_RADIUS;

    private static final int TRACK_LEFT = OUTER_CIRCLE_CENTER_X - TRACK_WIDTH / 2;
    private static final int TRACK_RIGHT = OUTER_CIRCLE_CENTER_X + TRACK_WIDTH / 2;
    private static final int TRACK_TOP = OUTER_CIRCLE_CENTER_Y + ANCHOR_POINT_TRACK_TOP;
    private static final int TRACK_SEC_TOP = OUTER_CIRCLE_CENTER_Y + ANCHOR_POINT_SEC_TRACK_TOP;
    private static final int TRACK_THR_TOP = OUTER_CIRCLE_CENTER_Y + ANCHOR_POINT_THR_TRACK_TOP;

    private static float ANGLE_ARC_DEGREE;
    private static float ARC_CIRCLE_RADIUS;
    private static float ARC_CIRCLE_LEFT_CENTER_X;
    private static float ARC_CIRCLE_RIGHT_CENTER_X;
    private static float ARC_CIRCLE_CENTER_Y;

    private static float ANGLE_SEC_ARC_DEGREE;
    private static float ARC_SEC_CIRCLE_RADIUS;
    private static float ARC_SEC_CIRCLE_LEFT_CENTER_X;
    private static float ARC_SEC_CIRCLE_RIGHT_CENTER_X;
    private static float ARC_SEC_CIRCLE_CENTER_Y;

    private static float ANGLE_THR_ARC_DEGREE;
    private static float ARC_THR_CIRCLE_RADIUS;
    private static float ARC_THR_CIRCLE_LEFT_CENTER_X;
    private static float ARC_THR_CIRCLE_RIGHT_CENTER_X;
    private static float ARC_THR_CIRCLE_CENTER_Y;

    static {
        float tempA = ANCHOR_POINT_TRACK_TOP;
        float tempD = TRACK_WIDTH / 2.0f;
        float tempR = OUTER_CIRCLE_RADIUS;
        float temp = 2 * (tempR - tempD);
        ARC_CIRCLE_RADIUS = (tempA * tempA + tempD * tempD - tempR * tempR) / temp;
        float ANGLE_ARC = (float) Math.atan((ARC_CIRCLE_RADIUS + tempD) / tempA);
        ANGLE_ARC_DEGREE = (float) (ANGLE_ARC * 180 / Math.PI);
        ARC_CIRCLE_LEFT_CENTER_X = TRACK_LEFT - ARC_CIRCLE_RADIUS;
        ARC_CIRCLE_RIGHT_CENTER_X = TRACK_RIGHT + ARC_CIRCLE_RADIUS;
        ARC_CIRCLE_CENTER_Y = TRACK_TOP;

        tempA = ANCHOR_POINT_SEC_TRACK_TOP;
        tempR = OUTER_SEC_CIRCLE_RADIUS;
        temp = 2 * (tempR - tempD);
        ARC_SEC_CIRCLE_RADIUS = (tempA * tempA + tempD * tempD - tempR * tempR) / temp;
        float ANGLE_SEC_ARC = (float) Math.atan((ARC_SEC_CIRCLE_RADIUS + tempD) / tempA);
        ANGLE_SEC_ARC_DEGREE = (float) (ANGLE_SEC_ARC * 180 / Math.PI);
        ARC_SEC_CIRCLE_LEFT_CENTER_X = TRACK_LEFT - ARC_SEC_CIRCLE_RADIUS;
        ARC_SEC_CIRCLE_RIGHT_CENTER_X = TRACK_RIGHT + ARC_SEC_CIRCLE_RADIUS;
        ARC_SEC_CIRCLE_CENTER_Y = TRACK_SEC_TOP;

        tempA = ANCHOR_POINT_THR_TRACK_TOP;
        tempR = OUTER_THR_CIRCLE_RADIUS;
        temp = 2 * (tempR - tempD);
        ARC_THR_CIRCLE_RADIUS = (tempA * tempA + tempD * tempD - tempR * tempR) / temp;
        float ANGLE_THR_ARC = (float) Math.atan((ARC_THR_CIRCLE_RADIUS + tempD) / tempA);
        ANGLE_THR_ARC_DEGREE = (float) (ANGLE_THR_ARC * 180 / Math.PI);
        ARC_THR_CIRCLE_LEFT_CENTER_X = TRACK_LEFT - ARC_THR_CIRCLE_RADIUS;
        ARC_THR_CIRCLE_RIGHT_CENTER_X = TRACK_RIGHT + ARC_THR_CIRCLE_RADIUS;
        ARC_THR_CIRCLE_CENTER_Y = TRACK_THR_TOP;
    }

    private static final int OUTER_CIRCLE_START_COLOR = Color.parseColor("#d013ff");
    private static final int OUTER_CIRCLE_MIDDLE_COLOR = Color.parseColor("#0e5dff");
    private static final int OUTER_CIRCLE_END_COLOR = Color.parseColor("#3216a5");

    private final int mViewWidth = 2 * (OUTER_CIRCLE_RADIUS + OUTER_CIRCLE_WIDTH);
    private int mViewHeight = 1548;
    private Paint mOutCirclePaint;
    private Paint mOutSecCirclePaint;
    private Paint mOutThrCirclePaint;

    public SketchView(Context context) {
        this(context, null);
    }

    public SketchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SketchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (manager != null) {
            Point point = new Point();
            manager.getDefaultDisplay().getRealSize(point);
            int screenHeight = point.y;
            mViewHeight = OUTER_CIRCLE_CENTER_Y + screenHeight / 2;
        }
        mOutCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOutCirclePaint.setStyle(Paint.Style.STROKE);
        mOutCirclePaint.setStrokeWidth(OUTER_CIRCLE_WIDTH);
        LinearGradient mLinearGradient = new LinearGradient(0, 0, 0, mViewHeight,
                new int[]{OUTER_CIRCLE_START_COLOR, OUTER_CIRCLE_MIDDLE_COLOR, OUTER_CIRCLE_END_COLOR},
                new float[]{0, 0.34f, 1}, Shader.TileMode.CLAMP);
        mOutCirclePaint.setShader(mLinearGradient);

        mOutSecCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOutSecCirclePaint.setStyle(Paint.Style.STROKE);
        mOutSecCirclePaint.setStrokeWidth(OUTER_SEC_CIRCLE_WIDTH);
        mOutSecCirclePaint.setAlpha((int) (255 * 0.7f));
        mOutSecCirclePaint.setShader(mLinearGradient);

        mOutThrCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOutThrCirclePaint.setStyle(Paint.Style.STROKE);
        mOutThrCirclePaint.setStrokeWidth(OUTER_THR_CIRCLE_WIDTH);
        mOutThrCirclePaint.setAlpha((int) (255 * 0.4f));
        mOutThrCirclePaint.setShader(mLinearGradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawOutCircle(canvas);
    }

    private void drawOutCircle(Canvas canvas) {
        int left = OUTER_CIRCLE_CENTER_X - OUTER_CIRCLE_RADIUS;
        int top = OUTER_CIRCLE_CENTER_Y - OUTER_CIRCLE_RADIUS;
        int right = OUTER_CIRCLE_CENTER_X + OUTER_CIRCLE_RADIUS;
        int bottom = OUTER_CIRCLE_CENTER_Y + OUTER_CIRCLE_RADIUS;
        canvas.drawArc(left, top, right, bottom,
                ANGLE_ARC_DEGREE - 270, 360 - ANGLE_ARC_DEGREE * 2,
                false, mOutCirclePaint);

        left = OUTER_CIRCLE_CENTER_X - OUTER_SEC_CIRCLE_RADIUS;
        top = OUTER_CIRCLE_CENTER_Y - OUTER_SEC_CIRCLE_RADIUS;
        right = OUTER_CIRCLE_CENTER_X + OUTER_SEC_CIRCLE_RADIUS;
        bottom = OUTER_CIRCLE_CENTER_Y + OUTER_SEC_CIRCLE_RADIUS;
        canvas.drawArc(left, top, right, bottom,
                ANGLE_SEC_ARC_DEGREE - 270, 360 - ANGLE_SEC_ARC_DEGREE * 2,
                false, mOutSecCirclePaint);

        left = OUTER_CIRCLE_CENTER_X - OUTER_THR_CIRCLE_RADIUS;
        top = OUTER_CIRCLE_CENTER_Y - OUTER_THR_CIRCLE_RADIUS;
        right = OUTER_CIRCLE_CENTER_X + OUTER_THR_CIRCLE_RADIUS;
        bottom = OUTER_CIRCLE_CENTER_Y + OUTER_THR_CIRCLE_RADIUS;
        canvas.drawArc(left, top, right, bottom,
                ANGLE_THR_ARC_DEGREE - 270, 360 - ANGLE_THR_ARC_DEGREE * 2,
                false, mOutThrCirclePaint);

        float tempLeft = ARC_CIRCLE_LEFT_CENTER_X - ARC_CIRCLE_RADIUS;
        float tempTop = ARC_CIRCLE_CENTER_Y - ARC_CIRCLE_RADIUS;
        float tempRight = ARC_CIRCLE_LEFT_CENTER_X + ARC_CIRCLE_RADIUS;
        float tempBottom = ARC_CIRCLE_CENTER_Y + ARC_CIRCLE_RADIUS;
        canvas.drawArc(tempLeft, tempTop, tempRight, tempBottom,
                ANGLE_ARC_DEGREE - 90, 90 - ANGLE_ARC_DEGREE,
                false, mOutCirclePaint);

        tempLeft = ARC_SEC_CIRCLE_LEFT_CENTER_X - ARC_SEC_CIRCLE_RADIUS;
        tempTop = ARC_SEC_CIRCLE_CENTER_Y - ARC_SEC_CIRCLE_RADIUS;
        tempRight = ARC_SEC_CIRCLE_LEFT_CENTER_X + ARC_SEC_CIRCLE_RADIUS;
        tempBottom = ARC_SEC_CIRCLE_CENTER_Y + ARC_SEC_CIRCLE_RADIUS;
        canvas.drawArc(tempLeft, tempTop, tempRight, tempBottom,
                ANGLE_SEC_ARC_DEGREE - 90, 90 - ANGLE_SEC_ARC_DEGREE,
                false, mOutSecCirclePaint);

        tempLeft = ARC_THR_CIRCLE_LEFT_CENTER_X - ARC_THR_CIRCLE_RADIUS;
        tempTop = ARC_THR_CIRCLE_CENTER_Y - ARC_THR_CIRCLE_RADIUS;
        tempRight = ARC_THR_CIRCLE_LEFT_CENTER_X + ARC_THR_CIRCLE_RADIUS;
        tempBottom = ARC_THR_CIRCLE_CENTER_Y + ARC_THR_CIRCLE_RADIUS;
        canvas.drawArc(tempLeft, tempTop, tempRight, tempBottom,
                ANGLE_THR_ARC_DEGREE - 90, 90 - ANGLE_THR_ARC_DEGREE,
                false, mOutThrCirclePaint);

        tempLeft = ARC_CIRCLE_RIGHT_CENTER_X - ARC_CIRCLE_RADIUS;
        tempTop = ARC_CIRCLE_CENTER_Y - ARC_CIRCLE_RADIUS;
        tempRight = ARC_CIRCLE_RIGHT_CENTER_X + ARC_CIRCLE_RADIUS;
        tempBottom = ARC_CIRCLE_CENTER_Y + ARC_CIRCLE_RADIUS;
        canvas.drawArc(tempLeft, tempTop, tempRight, tempBottom,
                180, 90 - ANGLE_ARC_DEGREE, false, mOutCirclePaint);

        tempLeft = ARC_SEC_CIRCLE_RIGHT_CENTER_X - ARC_SEC_CIRCLE_RADIUS;
        tempTop = ARC_SEC_CIRCLE_CENTER_Y - ARC_SEC_CIRCLE_RADIUS;
        tempRight = ARC_SEC_CIRCLE_RIGHT_CENTER_X + ARC_SEC_CIRCLE_RADIUS;
        tempBottom = ARC_SEC_CIRCLE_CENTER_Y + ARC_SEC_CIRCLE_RADIUS;
        canvas.drawArc(tempLeft, tempTop, tempRight, tempBottom,
                180, 90 - ANGLE_SEC_ARC_DEGREE, false, mOutSecCirclePaint);

        tempLeft = ARC_THR_CIRCLE_RIGHT_CENTER_X - ARC_THR_CIRCLE_RADIUS;
        tempTop = ARC_THR_CIRCLE_CENTER_Y - ARC_THR_CIRCLE_RADIUS;
        tempRight = ARC_THR_CIRCLE_RIGHT_CENTER_X + ARC_THR_CIRCLE_RADIUS;
        tempBottom = ARC_THR_CIRCLE_CENTER_Y + ARC_THR_CIRCLE_RADIUS;
        canvas.drawArc(tempLeft, tempTop, tempRight, tempBottom,
                180, 90 - ANGLE_THR_ARC_DEGREE, false, mOutThrCirclePaint);

        canvas.drawLine(TRACK_LEFT, TRACK_TOP, TRACK_LEFT, getMeasuredHeight(), mOutCirclePaint);
        canvas.drawLine(TRACK_RIGHT, TRACK_TOP, TRACK_RIGHT, getMeasuredHeight(), mOutCirclePaint);

        canvas.drawLine(TRACK_LEFT, TRACK_SEC_TOP, TRACK_LEFT, getMeasuredHeight(), mOutSecCirclePaint);
        canvas.drawLine(TRACK_RIGHT, TRACK_SEC_TOP, TRACK_RIGHT, getMeasuredHeight(), mOutSecCirclePaint);

        canvas.drawLine(TRACK_LEFT, TRACK_THR_TOP, TRACK_LEFT, getMeasuredHeight(), mOutThrCirclePaint);
        canvas.drawLine(TRACK_RIGHT, TRACK_THR_TOP, TRACK_RIGHT, getMeasuredHeight(), mOutThrCirclePaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mViewWidth, mViewHeight);
    }
}
