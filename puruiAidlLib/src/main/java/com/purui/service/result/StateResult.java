package com.purui.service.result;

import android.graphics.Bitmap;

/**
 * 开关总体状态state，分别为：
 *    0--拉开、1--合上、2--给上、3--取下、4--无效
 * stateA – A相状态，同上
 * stateB – B相状态，同上
 * stateC – C相状态，同上
 * retBitmap - 识别结果：即带框图像
 */
public class StateResult {
    private final boolean success;
    private final String details;
    private final int state;
    private final int stateA;
    private final int stateB;
    private final int stateC;
    private final Bitmap retBitmap;

    public boolean isSuccess() {
        return success;
    }

    public int getState() {
        return state;
    }

    public int getStateA() {
        return stateA;
    }

    public int getStateB() {
        return stateB;
    }

    public int getStateC() {
        return stateC;
    }

    public Bitmap getBitmap(){return retBitmap;}

    public String getDetails() {
        return details;
    }


    public StateResult(boolean checked, String details, int state, int stateA, int stateB, int stateC, Bitmap retBitmap) {
        this.success = checked;
        this.details = details;
        this.state = state;
        this.stateA = stateA;
        this.stateB = stateB;
        this.stateC = stateC;
        this.retBitmap = retBitmap;
    }
}
