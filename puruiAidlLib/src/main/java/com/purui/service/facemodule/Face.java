package com.purui.service.facemodule;

public class Face {
    public native boolean FaceModelInit(String faceDetectionModelPath);

    public native int[] FaceDetect(byte[] imageDate, int imageWidth , int imageHeight, int imageChannel);

    public native boolean FaceModelUnInit();

    public native double FaceRecognize(byte[] faceDate1,int w1,int h1,byte[] faceDate2,int w2,int h2);

    public native String FaceFeatureRestore (byte[] faceDate, int w, int h);

//    public native int[] getIsAlive(byte[] imageDate, int imageWidth , int imageHeight, int imageChannel);
    static {
        System.loadLibrary("Face");

    }
}
