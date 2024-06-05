package com.purui.service.ocrmodule;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;

import com.purui.service.ynmodule.DetectResult;
import com.purui.service.ynmodule.IModelHandle;
import com.purui.service.R;

public class OcrManager implements IModelHandle {
    protected Predictor predictor = new Predictor();
    @Override
    public boolean isModelLoaded() {
        return predictor.isLoaded();
    }
    @Override
    public boolean initModel(Context ctx){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        String modelPath = sharedPreferences.getString(ctx.getString(R.string.MODEL_PATH_KEY),
                ctx.getString(R.string.MODEL_PATH_DEFAULT));
        String labelPath = sharedPreferences.getString(ctx.getString(R.string.LABEL_PATH_KEY),ctx.getString(R.string.LABEL_PATH_DEFAULT));
        int cpuThreadNum = Integer.parseInt(sharedPreferences.getString(ctx.getString(R.string.CPU_THREAD_NUM_KEY),
                ctx.getString(R.string.CPU_THREAD_NUM_DEFAULT)));
        String cpuPowerMode =
                sharedPreferences.getString(ctx.getString(R.string.CPU_POWER_MODE_KEY),
                        ctx.getString(R.string.CPU_POWER_MODE_DEFAULT));
        int detLongSize = Integer.parseInt(sharedPreferences.getString(ctx.getString(R.string.DET_LONG_SIZE_KEY),
                ctx.getString(R.string.DET_LONG_SIZE_DEFAULT)));
        float scoreThreshold =
                Float.parseFloat(sharedPreferences.getString(ctx.getString(R.string.SCORE_THRESHOLD_KEY),
                        ctx.getString(R.string.SCORE_THRESHOLD_DEFAULT)));

        if (predictor.isLoaded()) {
            predictor.releaseModel();
        }
        return predictor.init(ctx, modelPath, labelPath, 0, cpuThreadNum,
                cpuPowerMode,
                detLongSize, scoreThreshold);
    }

    @Override
    public DetectResult runModel(Bitmap srcBitmap){
        if(predictor.isLoaded()){
            predictor.setInputImage(srcBitmap);
            predictor.runModel(1, 1, 1);
            String id = predictor.outputResult();//.replace('(','（').replace(')','）')
            Bitmap bm = predictor.outputImage();
            return new DetectResult(bm,id);
        }
        return new DetectResult(null, "");
    }

    @Override
    public DetectResult runModel(Bitmap inBitmap, int mode) {
        return null;
    }

    @Override
    public void releaseModel(){
        if (predictor != null) {
            predictor.releaseModel();
        }
    }
    @Override
    public boolean initModel(String localPath){
        return false;
    }
}
