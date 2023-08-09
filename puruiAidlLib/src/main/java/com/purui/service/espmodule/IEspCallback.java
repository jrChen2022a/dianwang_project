package com.purui.service.espmodule;

import android.content.Context;
import android.graphics.Bitmap;

public interface IEspCallback {
    Context getContext();
//    void makeToast(String content);
    void setCamPhotoBytes(byte[] bbs);
    void setCamPhoto(Bitmap showBitmap);
    void handleEspCallback(String recceive);
//    void showScanDialog();
//    void showScanResult(String res);

}
