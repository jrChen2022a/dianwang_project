package com.purui.service.cam;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ZoomView extends View {
    private static final float MAX_ZOOM_LEVEL = 50f;
    private float fingerSpacing = 0;
    private float zoomLevel = 1f;
    private ZoomListener zoomListener;

    public ZoomView(Context context) {
        super(context);
        init();
    }

    public ZoomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ZoomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 设置View为透明
        setBackgroundColor(0x00000000);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                fingerSpacing = 0;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                fingerSpacing = getFingerSpacing(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 2) {
                    float currentFingerSpacing = getFingerSpacing(event);
                    if (fingerSpacing != 0) {
                        if (currentFingerSpacing > fingerSpacing && zoomLevel < MAX_ZOOM_LEVEL) {
                            zoomLevel += 0.5f;
                        } else if (currentFingerSpacing < fingerSpacing && zoomLevel > 1) {
                            zoomLevel -= 0.5f;
                        }
                        zoomLevel = Math.max(1, Math.min(zoomLevel, MAX_ZOOM_LEVEL));
                        if (zoomListener != null) {
                            zoomListener.onZoomChanged(zoomLevel);
                        }
                        fingerSpacing = currentFingerSpacing;
                    }
                }
                break;
        }
        return true;
    }

    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    public void setZoomListener(ZoomListener listener) {
        this.zoomListener = listener;
    }

    public interface ZoomListener {
        void onZoomChanged(float zoomLevel);
    }
}
