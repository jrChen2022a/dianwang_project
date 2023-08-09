package com.purui.service.cam;

import android.graphics.Bitmap;

public interface ICamCallback {
    /**
     * 由本地摄像头设置图片
     * @param bitmap 本地相机
     */
    void setCamPhoto(Bitmap bitmap);
    void setCamPhotoBytes(byte[] bytes);
    void requestCameraPermission();
}
