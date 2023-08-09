package com.purui.service.ocrmodule;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;

class Predictor {
    private static final String TAG = Predictor.class.getSimpleName();
    private boolean isLoaded = false;
    private int warmupIterNum = 1;
    private int inferIterNum = 1;
    private int cpuThreadNum = 4;
    private String cpuPowerMode = "LITE_POWER_HIGH";
    private String modelPath = "";
    private String modelName = "";
    private OCRPredictorNative paddlePredictor = null;
    private float inferenceTime = 0;
    // Only for object detection
    private Vector<String> wordLabels = new Vector<String>();
    private int detLongSize = 960;
    private float scoreThreshold = 0.1f;
    private Bitmap inputImage = null;
    private Bitmap outputImage = null;
    private volatile String outputResult = "";
    private List<Point> outputCoordinate = new LinkedList<>();
    private List<String> outputLabel = new LinkedList<>();
    private float postprocessTime = 0;


    protected boolean init(Context appCtx, String modelPath, String labelPath, int useOpencl, int cpuThreadNum, String cpuPowerMode) {
        isLoaded = loadModel(appCtx, modelPath, useOpencl, cpuThreadNum, cpuPowerMode);
        if (!isLoaded) {
            return false;
        }
        isLoaded = loadLabel(appCtx, labelPath);
        return isLoaded;
    }


    protected boolean init(Context appCtx, String modelPath, String labelPath, int useOpencl, int cpuThreadNum, String cpuPowerMode,
                        int detLongSize, float scoreThreshold) {
        boolean isLoaded = init(appCtx, modelPath, labelPath, useOpencl, cpuThreadNum, cpuPowerMode);
        if (!isLoaded) {
            return false;
        }
        this.detLongSize = detLongSize;
        this.scoreThreshold = scoreThreshold;
        return true;
    }

    protected boolean loadModel(Context appCtx, String modelPath, int useOpencl, int cpuThreadNum, String cpuPowerMode) {
        // Release model if exists
        releaseModel();

        // Load model
        if (modelPath.isEmpty()) {
            return false;
        }
        String realPath = modelPath;
        if (!modelPath.substring(0, 1).equals("/")) {
            // Read model files from custom path if the first character of mode path is '/'
            // otherwise copy model to cache from assets
            realPath = appCtx.getCacheDir() + "/" + modelPath;
            Utils.copyDirectoryFromAssets(appCtx, modelPath, realPath);
        }
        if (realPath.isEmpty()) {
            return false;
        }

        OCRPredictorNative.Config config = new OCRPredictorNative.Config();
        config.useOpencl = useOpencl;
        config.cpuThreadNum = cpuThreadNum;
        config.cpuPower = cpuPowerMode;
        config.detModelFilename = realPath + File.separator + "det_db.nb";
        config.recModelFilename = realPath + File.separator + "rec_crnn.nb";
        config.clsModelFilename = realPath + File.separator + "cls.nb";
        Log.i("Predictor", "model path" + config.detModelFilename + " ; " + config.recModelFilename + ";" + config.clsModelFilename);
        paddlePredictor = new OCRPredictorNative(config);

        this.cpuThreadNum = cpuThreadNum;
        this.cpuPowerMode = cpuPowerMode;
        this.modelPath = realPath;
        this.modelName = realPath.substring(realPath.lastIndexOf("/") + 1);
        return true;
    }

    protected void releaseModel() {
        if (paddlePredictor != null) {
            paddlePredictor.destory();
            paddlePredictor = null;
        }
        isLoaded = false;
        cpuThreadNum = 1;
        cpuPowerMode = "LITE_POWER_HIGH";
        modelPath = "";
        modelName = "";
    }

    protected boolean loadLabel(Context appCtx, String labelPath) {
        wordLabels.clear();
        wordLabels.add("black");
        // Load word labels from file
        try {
            InputStream assetsInputStream = appCtx.getAssets().open(labelPath);
            int available = assetsInputStream.available();
            byte[] lines = new byte[available];
            assetsInputStream.read(lines);
            assetsInputStream.close();
            String words = new String(lines);
            String[] contents = words.split("\n");
            for (String content : contents) {
                wordLabels.add(content);
            }
            wordLabels.add(" ");
            Log.i(TAG, "Word label size: " + wordLabels.size());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
        return true;
    }


    protected boolean runModel(int run_det, int run_cls, int run_rec) {
        if (inputImage == null || !isLoaded()) {
            return false;
        }

        // Warm up
        for (int i = 0; i < warmupIterNum; i++) {
            paddlePredictor.runImage(inputImage, detLongSize, run_det, run_cls, run_rec);
        }
        warmupIterNum = 0; // do not need warm
        // Run inference
        Date start = new Date();
        ArrayList<OcrResultModel> results = paddlePredictor.runImage(inputImage, detLongSize, run_det, run_cls, run_rec);
        Date end = new Date();
        inferenceTime = (end.getTime() - start.getTime()) / (float) inferIterNum;

        results = postprocess(results);
        Log.i(TAG, "[stat] Inference Time: " + inferenceTime + " ;Box Size " + results.size());
        drawResults(results);

        return true;
    }

    protected boolean isLoaded() {
        return paddlePredictor != null && isLoaded;
    }

    protected String modelPath() {
        return modelPath;
    }

    protected String modelName() {
        return modelName;
    }

    protected int cpuThreadNum() {
        return cpuThreadNum;
    }

    protected String cpuPowerMode() {
        return cpuPowerMode;
    }

    protected float inferenceTime() {
        return inferenceTime;
    }

    protected Bitmap inputImage() {
        return inputImage;
    }

    protected Bitmap outputImage() {
        return outputImage;
    }

    protected String outputResult() {
        return outputResult;
    }

    protected float postprocessTime() {
        return postprocessTime;
    }


    protected void setInputImage(Bitmap image) {
        if (image == null) {
            return;
        }
        this.inputImage = image.copy(Bitmap.Config.ARGB_8888, true);
    }

    private ArrayList<OcrResultModel> postprocess(ArrayList<OcrResultModel> results) {
        for (OcrResultModel r : results) {
            StringBuffer word = new StringBuffer();
            for (int index : r.getWordIndex()) {
                if (index >= 0 && index < wordLabels.size()) {
                    word.append(wordLabels.get(index));
                } else {
                    Log.e(TAG, "Word index is not in label list:" + index);
                    word.append("×");
                }
            }
            r.setLabel(word.toString());
            r.setClsLabel(r.getClsIdx() == 1 ? "180" : "0");
        }
        return results;
    }

    private void drawResults(ArrayList<OcrResultModel> results) {
        outputLabel = new LinkedList<>();
        outputCoordinate = new LinkedList<>();
        StringBuffer outputResultSb = new StringBuffer("");
        for (int i = 0; i < results.size(); i++) {
            OcrResultModel result = results.get(i);
            if(result.getConfidence()<0.4){
                continue;
            }
            if(result.getLabel().length()<4 && (result.getLabel().contains("左")||result.getLabel().contains("右"))){
                continue;
            }
            StringBuilder sb = new StringBuilder("");
//            if(result.getPoints().size()>0){
//                sb.append("Det: ");
//                for (Point p : result.getPoints()) {
//                    sb.append("(").append(p.x).append(",").append(p.y).append(") ");
//                }
//            }
            if(result.getLabel().length() > 0){
                sb.append(result.getLabel());
                outputLabel.add(result.getLabel());
                Point mp = new Point(0,0);
                int pointLen = result.getPoints().size();
                for (Point p : result.getPoints()) {
                    mp.x += p.x/pointLen;
                    mp.y += p.y/pointLen;
                }
                outputCoordinate.add(mp);
//                sb.append("\n Rec: ").append(result.getLabel());
//                sb.append(",").append(result.getConfidence());
            }
//            if(result.getClsIdx()!=-1){
//                sb.append(" Cls: ").append(result.getClsLabel());
//                sb.append(",").append(result.getClsConfidence());
//            }
            Log.i(TAG, sb.toString()); // show LOG in Logcat panel
//            outputResultSb.append(i + 1).append(": ").append(sb.toString()).append("\n");
            outputResultSb.append(sb.toString()).append("\t");
        }
        outputResult = outputResultSb.toString();
//
//        int resLen  = outputLabel.size();
//        for(int i=0;i<resLen;i++){
//            for(int j=i+1;j<resLen;j++)
//            if(outputCoordinate.get(i).x > outputCoordinate.get(j).x){
//                Collections.swap(outputCoordinate,i,j);
//                Collections.swap(outputLabel,i,j);
//            }
//        }
//
//        for(int i = 0;i <resLen; i++){
//            if(outputLabel.get(i).length()<4 && (outputLabel.get(i).contains("左")||outputLabel.get(i).contains("右"))){
//                continue;
//            }
//        }
        outputImage = inputImage;
        Canvas canvas = new Canvas(outputImage);
        Paint paintFillAlpha = new Paint();
        paintFillAlpha.setStyle(Paint.Style.FILL);
        paintFillAlpha.setColor(Color.parseColor("#3B85F5"));
        paintFillAlpha.setAlpha(50);

        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#3B85F5"));
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);

        for (OcrResultModel result : results) {
            Path path = new Path();
            List<Point> points = result.getPoints();
            if(points.size()==0){
                continue;
            }
            path.moveTo(points.get(0).x, points.get(0).y);
            for (int i = points.size() - 1; i >= 0; i--) {
                Point p = points.get(i);
                path.lineTo(p.x, p.y);
            }
            canvas.drawPath(path, paint);
            canvas.drawPath(path, paintFillAlpha);
        }
    }

}
