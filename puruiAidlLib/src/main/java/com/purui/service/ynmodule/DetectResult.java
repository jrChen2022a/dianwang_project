package com.purui.service.ynmodule;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.purui.service.ynmodule.SwitchType;

import java.util.Arrays;

public class DetectResult{
    private final Bitmap resBitmap;
    private String recoID = null;
    private SwitchType switchType = null;
    private int stateA = 2;
    private int stateB = 2;
    private int stateC = 2; // 0 represent to Off, 1 to On, 2 to unknown
    private int state = 2;
    private int switchCount = 0;
    private boolean onYandian = false;

    public boolean getOnYanDian(){
        return onYandian;
    }
    public Bitmap getBitmap() {
        return resBitmap;
    }
    public String getRecoID() {
        return recoID;
    }
    public SwitchType getSwitchType() {
        return switchType;
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
    public int getState() {
        return state;
    }
    public int getSwitchCount() {
        return switchCount;
    }


    public DetectResult(Bitmap resBit, String recoID){ //result for OCR
        this.resBitmap = resBit;
        this.recoID = recoID;
    }

    /**
     * 状态检测逻辑返回结果
     * @param resBit
     * @param switchType
     * @param stateABC
     * @param switchCount
     */
    public DetectResult(Bitmap resBit, SwitchType switchType, int[] stateABC, int switchCount){//result for detection
        this.resBitmap = resBit;
        this.switchType = switchType;
        this.stateA = stateABC[0];
        this.stateB = stateABC[1];
        this.stateC = stateABC[2];
        this.switchCount = switchCount;
        if(Arrays.equals(stateABC, new int[]{1, 1, 1})){
            this.state = 1;
        }else if(Arrays.equals(stateABC, new int[]{0, 0, 0})){
            this.state = 0;
        }else if(Arrays.equals(stateABC, new int[]{3, 3, 3})){
            this.state = 3;
        }else{
            this.state = 2;
        }
    }

    /**
     * 验电逻辑返回结果
     * @param resBit
     * @param onYandian
     */
    public DetectResult(Bitmap resBit, boolean onYandian){//result for detection
        this.resBitmap = resBit;
        this.onYandian = onYandian;
    }


}
