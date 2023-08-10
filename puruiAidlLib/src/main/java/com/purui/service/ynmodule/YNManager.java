package com.purui.service.ynmodule;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class YNManager extends YNNative implements IModelHandle {
    private final YNNative yolov5ncnn = new YNNative();
    private boolean modelLoaded = false;
    private final Context This;
    public YNManager(Context ctx){
        This = ctx;
    }

    @Override
    public boolean isModelLoaded() {
        return modelLoaded;
    }

    @Override
    public boolean initModel(Context ctx){
        return false;
    }

    @Override
    public boolean initModel(String localPath){
        try {
            copyBigDataToSD(localPath,"best.bin");
            copyBigDataToSD(localPath,"best.param");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(!modelLoaded){

            boolean ret_init = yolov5ncnn.Init(localPath);;
            if (!ret_init)
            {
                Log.e("puruiService", "ynModule Init failed");
            }
            modelLoaded = ret_init;
        }
        return modelLoaded;
    }

    @Override
    public DetectResult runModel(Bitmap inBitmap) {
        return null;
    }
    public static final int STATE_DETECTION = 1;
    public static final int YANDIAN = 2;
    @Override
    public DetectResult runModel(Bitmap inBitmap, int mode){
        Obj[] objects = null;
        if(modelLoaded){
            objects = yolov5ncnn.Detect(inBitmap, false);
        }
        if(mode == STATE_DETECTION){
            if (objects == null)
            {
                return new DetectResult(inBitmap, null, new int[]{2, 2, 2},0);
            }
            // draw objects on bitmap
            Bitmap rgba = inBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(rgba);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.min(inBitmap.getWidth()/160,inBitmap.getHeight()/160));
//        Paint textbgpaint = new Paint();
//        textbgpaint.setColor(Color.WHITE);
//        textbgpaint.setStyle(Paint.Style.FILL);
//        Paint textpaint = new Paint();
//        textpaint.setColor(Color.BLACK);
//        textpaint.setTextSize(26);
//        textpaint.setTextAlign(Paint.Align.LEFT);
            SwitchType deSwitchType = null;
            int[] deStateABC = {2,2,2}; // 0 stands for On, 1 for Off, 2 for Null, 3 for Away
            int index = 0;
            for (Obj object : objects){
                if(index > 2){
                    break;
                }
                if (object.prob < 0.7 || object.label.equals("huan")) {
                    continue;
                }
                switch (object.label) {
                    case "Off":
                        deSwitchType = SwitchType.dieLuo;
                        deStateABC[index] = 0;
                        paint.setColor(Color.GREEN);
                        break;
                    case "On":
                        deSwitchType = SwitchType.dieLuo;
                        deStateABC[index] = 1;
                        paint.setColor(Color.RED);
                        break;
                    case "aOff":
                        deSwitchType = SwitchType.daoZha;
                        deStateABC[index] = 0;
                        paint.setColor(Color.GREEN);
                        break;
                    case "aOn":
                        deSwitchType = SwitchType.daoZha;
                        deStateABC[index] = 1;
                        paint.setColor(Color.RED);
                        break;
                    case "bOff":
                        deSwitchType = SwitchType.ZW32daoKai;
                        deStateABC = new int[]{0, 0, 0};
                        paint.setColor(Color.GREEN);
                        break;
                    case "bOn":
                        deSwitchType = SwitchType.ZW32daoKai;
                        deStateABC = new int[]{1, 1, 1};
                        paint.setColor(Color.RED);
                        break;
                    case "cOff":
                        deSwitchType = SwitchType.ZW32wuDao;
                        deStateABC = new int[]{0, 0, 0};
                        paint.setColor(Color.GREEN);
                        break;
                    case "cOn":
                        deSwitchType = SwitchType.ZW32wuDao;
                        deStateABC = new int[]{1, 1, 1};
                        paint.setColor(Color.RED);
                        break;
                    case "away":
                        deSwitchType = SwitchType.dieLuo;
                        deStateABC[index] = 3;
                        paint.setColor(Color.BLUE);
                        break;
                }

                index++;
//            boolean switchState = object.label.contains("On");
//            switchesStates += (switchState)?"1":"0";
//            switType = (!object.label.contains("b") && !object.label.contains("c"))?0:1;
//            paint.setColor((switchState)?Color.RED:Color.GREEN);
                canvas.drawRect(object.x, object.y, object.x + object.w, object.y + object.h, paint);
////             draw filled text inside image
//            {
//                String text = objects[i].label + " = " + String.format("%.1f", objects[i].prob * 100) + "%";
//
//                float text_width = textpaint.measureText(text);
//                float text_height = - textpaint.ascent() + textpaint.descent();
//
//                float x = objects[i].x;
//                float y = objects[i].y - text_height;
//                if (y < 0)
//                    y = 0;
//                if (x + text_width > rgba.getWidth())
//                    x = rgba.getWidth() - text_width;
//
//                canvas.drawRect(x, y, x + text_width, y + text_height, textbgpaint);
//
//                canvas.drawText(text, x, y - textpaint.ascent(), textpaint);
//            }
            }
//        String retStr;
//        if(switType == 0){//qian liang lei (du li de )
//            retStr = switchesStates;
//        }else{
//            if (switchesStates.length() == 1) {
//                if (switchesStates.equals("1")) {
//                    retStr = "111";
//                } else {
//                    retStr = "000";
//                }
//            } else {
//                retStr = "";
//            }
//        }
            return new DetectResult(rgba,deSwitchType,deStateABC,index);
        }else{
            if (objects == null)
            {
                return new DetectResult(inBitmap, false);
            }
            //验电检测逻辑
            // draw objects on bitmap
            Bitmap rgba = inBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(rgba);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4);
            paint.setColor(Color.GREEN);
            for (Obj object : objects){
                if (object.prob < 0.7) {
                    continue;
                }
                if(object.label.equals("huan")){
                    canvas.drawRect(object.x, object.y, object.x + object.w, object.y + object.h, paint);
                    return new DetectResult(rgba, true);
                }
            }
            return new DetectResult(inBitmap, false);
        }
    }

    @Override
    public void releaseModel(){
        if(modelLoaded){
            yolov5ncnn.Release();
        }
        modelLoaded = false;
    }

    private void copyBigDataToSD(String sdPath, String strOutFileName) throws IOException {
//        Log.i(TAG, "start copy file " + strOutFileName);
        File file = new File(sdPath);
        if (!file.exists()) {
            file.mkdir();
        }

        String tmpFile = sdPath + strOutFileName;
        File f = new File(tmpFile);
        if (f.exists()) {
//            Log.i(TAG, "file exists " + strOutFileName);
            return;
        }
        InputStream myInput;
        java.io.OutputStream myOutput = new FileOutputStream(sdPath+ strOutFileName);
        myInput = This.getAssets().open(strOutFileName);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
//        Log.i(TAG, "end copy file " + strOutFileName);

    }
}
