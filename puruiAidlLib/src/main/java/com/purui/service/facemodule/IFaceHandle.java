package com.purui.service.facemodule;

import android.graphics.Bitmap;

import com.purui.service.result.PuruiResult;

public interface IFaceHandle {
    boolean isModelLoaded();
    boolean initModel();
    PuruiResult runModel(Bitmap inBitmap);
    void releaseModel();

    PuruiResult[] checkFaces();
    PuruiResult addFace(String name, Bitmap photo);
    PuruiResult deleteFace(String name);
}
