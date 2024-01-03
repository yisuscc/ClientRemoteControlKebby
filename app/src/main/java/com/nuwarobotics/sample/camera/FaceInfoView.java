package com.nuwarobotics.sample.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import org.json.JSONObject;

import java.util.Locale;
// Unused class, you can safely delete it
//only left as an example on  how it works with only one face data
public class FaceInfoView extends View {
    // THis class is provided by Will Lin
    // i only did a few modifications
    // to get the ratios right
    private final String TAG = FaceInfoView.class.getSimpleName();

    private final Paint mPaint;
    private final Rect mRect;

    private String mAge = "";
    private String mGender = "";
    private int mBottom, mLeft, mRight, mTop;
    private int horizontalRatio;
    private int verticalRatio;
    public FaceInfoView(Context context, AttributeSet attr) {
        super(context, attr);
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.YELLOW);
        horizontalRatio = getResources().getDisplayMetrics().widthPixels / 640;
        verticalRatio = getResources().getDisplayMetrics().heightPixels / 480;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final float DENSITY = getResources().getDisplayMetrics().density;

        final float FONT_SIZE = 24 * DENSITY;

        final String FORMAT_AGE = "Age: %1$s";
        final String FORMAT_GENDER = "Gender: %1$s";

        mRect.set(mLeft, mTop, mRight, mBottom);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(mRect, mPaint);

        mPaint.setTextSize(FONT_SIZE);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawText(String.format(Locale.ENGLISH, FORMAT_AGE, mAge), mLeft, (mBottom - FONT_SIZE), mPaint);
        canvas.drawText(String.format(Locale.ENGLISH, FORMAT_GENDER, mGender), mLeft, mBottom, mPaint);
    }

    public void setData(JSONObject data) {
        if (null != data) {
            final String FIELD_AGE = "age";
            final String FIELD_GENDER = "gender";
            final String FIELD_RECT = "rect";
            final String FIELD_RECT_BOTTOM = "bottom";
            final String FIELD_RECT_LEFT = "left";
            final String FIELD_RECT_RIGHT = "right";
            final String FIELD_RECT_TOP = "top";

            try {
                if (data.has(FIELD_AGE)) {
                    mAge = data.getString(FIELD_AGE);
                }

                if (data.has(FIELD_GENDER)) {
                    mGender = data.getString(FIELD_GENDER);
                }

                if (data.has(FIELD_RECT)) {

                    JSONObject rect = data.getJSONObject(FIELD_RECT);
                    mTop = rect.getInt(FIELD_RECT_TOP)*verticalRatio;
                    mBottom = rect.getInt(FIELD_RECT_BOTTOM)*verticalRatio ;
                    mLeft = rect.getInt(FIELD_RECT_LEFT)*horizontalRatio ;
                    mRight = rect.getInt(FIELD_RECT_RIGHT)*horizontalRatio;
                }
            } catch (Exception e) {
                Log.w(TAG, "parse data exception message = " + e.getMessage());
            }
        }
        invalidate();
    }
}