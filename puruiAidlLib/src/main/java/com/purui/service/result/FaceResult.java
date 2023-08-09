package com.purui.service.result;

import android.graphics.Bitmap;

/**
 * success: 识别/获取操作是否成功
 * name: 识别/获取的人脸对应人名
 * face: 识别/获取到的人脸
 */
public class FaceResult {
    private final boolean success;
    private final String name;
    private final Bitmap face;

    public FaceResult(boolean success, String name, Bitmap face) {
        this.success = success;
        this.name = name;
        this.face = face;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getName() {
        return name;
    }

    public Bitmap getFace() {
        return face;
    }

}
