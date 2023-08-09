package com.purui.service.ynmodule;

import android.content.Context;
import android.graphics.Bitmap;

public interface IModelHandle {
    boolean isModelLoaded();
    boolean initModel(Context ctx);
    boolean initModel(String localPath);
    DetectResult runModel(Bitmap inBitmap);
    DetectResult runModel(Bitmap inBitmap, int mode);
    void releaseModel();
}
