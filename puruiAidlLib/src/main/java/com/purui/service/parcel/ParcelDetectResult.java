package com.purui.service.parcel;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelDetectResult implements Parcelable {
    private Bitmap resBitmap;
    private String recoID;
    private Integer stateA;// 0 represent to Off, 1 to On, 2 to unknown
    private Integer stateB;
    private Integer stateC;
    private Integer state;
    private String stateResult;
    private Boolean isOpenYanCam;
    private Boolean whetherToTest;


    protected ParcelDetectResult(Parcel in) {
        resBitmap = in.readParcelable(Bitmap.class.getClassLoader());
        recoID = in.readString();
        if (in.readByte() == 0) {
            stateA = null;
        } else {
            stateA = in.readInt();
        }
        if (in.readByte() == 0) {
            stateB = null;
        } else {
            stateB = in.readInt();
        }
        if (in.readByte() == 0) {
            stateC = null;
        } else {
            stateC = in.readInt();
        }
        if (in.readByte() == 0) {
            state = null;
        } else {
            state = in.readInt();
        }
        stateResult = in.readString();
        byte tmpIsOpenYanCam = in.readByte();
        isOpenYanCam = tmpIsOpenYanCam == 0 ? null : tmpIsOpenYanCam == 1;
        byte tmpPrepareForEleTest = in.readByte();
        whetherToTest = tmpPrepareForEleTest == 0 ? null : tmpPrepareForEleTest == 1;
    }

    public static final Creator<ParcelDetectResult> CREATOR = new Creator<ParcelDetectResult>() {
        @Override
        public ParcelDetectResult createFromParcel(Parcel in) {
            return new ParcelDetectResult(in);
        }

        @Override
        public ParcelDetectResult[] newArray(int size) {
            return new ParcelDetectResult[size];
        }
    };

    public Bitmap getResBitmap() {
        return resBitmap;
    }
    public String getRecoID() {
        return recoID;
    }
    public Integer getStateA() {
        return stateA;
    }
    public Integer getStateB() {
        return stateB;
    }
    public Integer getStateC() {
        return stateC;
    }
    public Integer getState() {
        return state;
    }
    public String getStateResult() {
        return stateResult;
    }
    public Boolean isOpenYanCam() {
        return isOpenYanCam;
    }
    public Boolean getWhetherToTest() {
        return whetherToTest;
    }

    public ParcelDetectResult(Bitmap resBitmap, String recoID) {
        this.resBitmap = resBitmap;
        this.recoID = recoID;
    }
    public ParcelDetectResult(Bitmap resBitmap, int stateA, int stateB, int stateC, int state, String stateResult) {
        this.resBitmap = resBitmap;
        this.stateA = stateA;
        this.stateB = stateB;
        this.stateC = stateC;
        this.state = state;
        this.stateResult = stateResult;
    }
    public ParcelDetectResult(Bitmap resBitmap, boolean isOpenYanCam, boolean whetherToTest) {
        this.resBitmap = resBitmap;
        this.isOpenYanCam = isOpenYanCam;
        this.whetherToTest = whetherToTest;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(resBitmap, flags);
        dest.writeString(recoID);
        if (stateA == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(stateA);
        }
        if (stateB == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(stateB);
        }
        if (stateC == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(stateC);
        }
        if (state == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(state);
        }
        dest.writeString(stateResult);
        dest.writeByte((byte) (isOpenYanCam == null ? 0 : isOpenYanCam ? 1 : 2));
        dest.writeByte((byte) (whetherToTest == null ? 0 : whetherToTest ? 1 : 2));
    }
}
