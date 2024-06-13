// IPuAidlInterface.aidl
package com.purui.service;

// Declare any non-default types here with import statements
import com.purui.service.IPuAidlCallback;
import com.purui.service.parcel.ParcelDetectResult;
import com.purui.service.parcel.ParcelFaceResult;

interface IPuAidlInterface {
    void registerCallBack(IPuAidlCallback iAidlCallBack);
    void unregisterCallBack(IPuAidlCallback iAidlCallBack);

    String scanCameras();
    boolean selectCamera(in int camType);
    String startRecord();
    String endRecord();

    Bitmap getCamPhoto();
    byte[] getCamPhotoBytes();

    void setAdditionalBitmap(in Bitmap bm);
    ParcelDetectResult getOcrRes();
    ParcelDetectResult getDetectedRes();

    ParcelDetectResult getWhetherToTestElectro(char selectPhase);
    String getElectroTestRes();
    boolean lockDevice();
    boolean unLockDevice();

    ParcelFaceResult getFaceResult(in Bitmap bm);
    ParcelFaceResult[] checkAllFaces();
    ParcelFaceResult addFace(in String name, in Bitmap bm);
    ParcelFaceResult deleteFace(in String name);

    void onCamZoomChanged(float zoomLevel);
}