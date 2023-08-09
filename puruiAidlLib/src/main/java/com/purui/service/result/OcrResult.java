package com.purui.service.result;

import android.graphics.Bitmap;

/**
 * success: 是否识别成功
 * id: 识别出的文字
 * details: 同上
 * retBitmap: 识别图像反馈
 */
public class OcrResult {
    private final boolean success;
    private final String id;
    private final String details;
    private final Bitmap retBitmap;

    public Bitmap getBitmap(){return retBitmap;}

    public String getId() {
        return id;
    }

    public boolean isSuccess() {
        return success;
    }

    public OcrResult(boolean success, String id, String details, Bitmap retBitmap) {
        this.success = success;
        this.id = id;
        this.details = details;
        this.retBitmap = retBitmap;
    }
}
