package com.purui.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.purui.service.Utils.Utils;
import com.purui.service.cam.ICamCallback;
import com.purui.service.cam.NativeCamManager;
import com.purui.service.espmodule.HTTPEspManager;
import com.purui.service.espmodule.IEspCallback;
import com.purui.service.espmodule.IEspHandle;
import com.purui.service.facemodule.FaceManager;
import com.purui.service.facemodule.IFaceHandle;
import com.purui.service.ocrmodule.OcrManager;
import com.purui.service.parcel.ParcelDetectResult;
import com.purui.service.parcel.ParcelFaceResult;
import com.purui.service.result.PuruiResult;
import com.purui.service.ynmodule.DetectResult;
import com.purui.service.ynmodule.IModelHandle;
import com.purui.service.ynmodule.SwitchType;
import com.purui.service.ynmodule.YNManager;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

public class PuService extends Service implements IEspCallback, ICamCallback {
    private static final String TAG = "PuruiService";
    private static final int WAITING_TIME = 5000;  //5s
    private static final int REMOTE_CAMERA = 1;
    private static final  int YANDIAN_CAMERA = 2;
    private static final int CONNECT_CAM_REMOTE = 1;
    private static final int CONNECT_CAM_YANDIAN = 2;
    private static final int DISCONNECT_CAM_REMOTE = 3;
    private static final int DISCONNECT_CAM_YANDIAN = 4;
    private static final int CLOSE_CAM = 5;
    private static final int OUT_SHORT_PULSE = 6;
    private static final int OUT_LONG_PULSE = 7;
    private static final int RECEIVE_COUNT_5 = 8;
    private static final int RECEIVE_COUNT_0 = 9;
    private static final int VOLTAGE_LEVEL_1 = 10;
    private static final int VOLTAGE_LEVEL_0 = 11;
    private static final HashMap<String, Integer> getInfoIndex = new HashMap<>();
    static {
        getInfoIndex.put("camera connected id1",CONNECT_CAM_REMOTE);
        getInfoIndex.put("camera connected id2",CONNECT_CAM_YANDIAN);
        getInfoIndex.put("fail to connect cam id1",DISCONNECT_CAM_REMOTE);
        getInfoIndex.put("fail to connect cam id2",DISCONNECT_CAM_YANDIAN);
        getInfoIndex.put("camera closed",CLOSE_CAM);
        getInfoIndex.put("OUT short pulse!",OUT_LONG_PULSE);
        getInfoIndex.put("OUT long pulse!",OUT_SHORT_PULSE);
        getInfoIndex.put("receive pulse count:5",RECEIVE_COUNT_5);
        getInfoIndex.put("receive pulse count:4",RECEIVE_COUNT_5);
        getInfoIndex.put("receive pulse count:3",RECEIVE_COUNT_0);
        getInfoIndex.put("receive pulse count:2",RECEIVE_COUNT_0);
        getInfoIndex.put("receive pulse count:1",RECEIVE_COUNT_0);
        getInfoIndex.put("receive pulse count:0",RECEIVE_COUNT_0);
        getInfoIndex.put("the voltage level is:1",VOLTAGE_LEVEL_1);
        getInfoIndex.put("the voltage level is:0",VOLTAGE_LEVEL_0);
    }
    private final PuService This = this;
    private boolean remoteCamLinked = false;
    private boolean yandianCamLinked = false;
    private volatile boolean testingElectro = false;
    private volatile boolean testingContact;
    private volatile boolean waitingEspCall = false;
    private boolean espSentPulse;
    private boolean isDeviceLocked = false;
    private boolean isDeviceUnlocked = false;
    private char phase;
    private boolean phaseElectried;
    private boolean phaseContacted;
    private volatile byte[] camPhotoBytes;
    private volatile Bitmap camPhoto;

    private final RemoteCallbackList<IPuAidlCallback> callbackList= new RemoteCallbackList<>();
    private IModelHandle iOcrHandle;
    private IModelHandle iYNhandle;
    private IEspHandle iEspHandle;
    private IFaceHandle iFaceHandle;
    private String yoloPath;
    private boolean additionalReco = false; //额外的功能：输入Bitmap识别
    private Bitmap additionalBm;
    private NativeCamManager CM = null;
    @Override
    public Context getContext(){
        return This;
    }

    @Override
    public void handleEspCallback(String receive){
        int info = getInfoIndex.get(receive);
        switch (info) {
            case CONNECT_CAM_REMOTE:
                remoteCamLinked = true;
                waitingEspCall = false;
                break;
            case CONNECT_CAM_YANDIAN:
                yandianCamLinked = true;
                waitingEspCall = false;
                break;
            case DISCONNECT_CAM_REMOTE:
                remoteCamLinked = false;
                waitingEspCall = false;
                break;
            case DISCONNECT_CAM_YANDIAN:
                yandianCamLinked = false;
                waitingEspCall = false;
                break;
            case CLOSE_CAM:
                setNoCamImage();
                break;
            case OUT_LONG_PULSE:
            case OUT_SHORT_PULSE:
                espSentPulse = true;
                waitingEspCall = false;
                break;
            case RECEIVE_COUNT_0:
                phaseElectried = true;
                testingElectro = false;
                break;
            case RECEIVE_COUNT_5:
                phaseElectried = false;
                testingElectro = false;
                break;
            case VOLTAGE_LEVEL_0:
                phaseContacted = false;
                testingContact = false;
                break;
            case VOLTAGE_LEVEL_1:
                phaseContacted = true;
                testingContact = false;
                break;
            default:
                break;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    @Override
    public void onCreate(){
        File sdDir = this.getCacheDir();
        yoloPath = sdDir.toString() + "/detect/";
        iYNhandle = new YNManager(This);
        iOcrHandle = new OcrManager();
        iEspHandle = new HTTPEspManager(This);
        iFaceHandle = new FaceManager(This, sdDir,true);
        Log.v(TAG, "Initialize done!");
    }
    @Override
    public void onDestroy(){
        if(iYNhandle.isModelLoaded()){
            iYNhandle.releaseModel();
        }
        if(iOcrHandle.isModelLoaded()){
            iOcrHandle.releaseModel();
        }
    }
    private final IPuAidlInterface.Stub binder=new IPuAidlInterface.Stub() {
        @Override
        public void registerCallBack(IPuAidlCallback iAidlCallBack) {
            callbackList.register(iAidlCallBack);
        }
        @Override
        public void unregisterCallBack(IPuAidlCallback iAidlCallBack) {
            callbackList.unregister(iAidlCallBack);
        }

        @Override
        public String scanCameras(){
            String msg = "";
            if(iEspHandle!=null){
                msg = iEspHandle.scanEsp();
            }
            return msg;
        }
        @Override
        public boolean selectCamera(int camType) {
            canSetNoCam = false; // 提高流畅体验
            if(camType == 3) {
                if(CM != null){
                    CM.closeCam();
                    CM = null;
                }
                boolean espReady = getEspStates(YANDIAN_CAMERA,true);
                if(espReady){
                    waitingEspCall = true;
                    iEspHandle.setCamera(YANDIAN_CAMERA,true);
                    Date start = new Date();
                    long waitDuration = 0;
                    while(waitingEspCall && waitDuration<WAITING_TIME){waitDuration = new Date().getTime() - start.getTime();}
                    remoteCamLinked = false;
                    if(!yandianCamLinked){
                        setNoCamImage();
                    }
                }
                return yandianCamLinked;
            }else if(camType == 2) {
                if(CM != null){
                    CM.closeCam();
                    CM = null;
                }
                boolean espReady = getEspStates(REMOTE_CAMERA,true);
                if(espReady){
                    waitingEspCall = true;
                    iEspHandle.setCamera(REMOTE_CAMERA,true);
                    Date start = new Date();
                    long waitDuration = 0;
                    while(waitingEspCall && waitDuration<WAITING_TIME){waitDuration = new Date().getTime() - start.getTime();}
                    yandianCamLinked = false;
                    if(!remoteCamLinked){
                        setNoCamImage();
                    }
                }
                return remoteCamLinked;
            }else if(camType == 1){
                // 这里写本地摄像头
                if(remoteCamLinked || yandianCamLinked){
                    iEspHandle.releaseCamera();
                }
                remoteCamLinked = false;
                yandianCamLinked = false;
                if(CM == null){
                    CM = new NativeCamManager(This, This);
                    CM.openCam(true);
                }
                return true;
            }else{
                canSetNoCam = true;
                if(remoteCamLinked || yandianCamLinked){
                   iEspHandle.releaseCamera();
                   camPhoto = null;
                   camPhotoBytes = null;
                }
                remoteCamLinked = false;
                yandianCamLinked = false;
                if(CM != null){
                    CM.closeCam();
                    CM = null;
                }


                //无摄像头
                setNoCamImage();
                return true;
            }
        }
        @Override
        public Bitmap getCamPhoto(){ // 获取图片
            return camPhoto!=null?camPhoto:camPhotoBytes!=null?Utils.bytes2bitmap(camPhotoBytes, Bitmap.Config.ARGB_4444,1):null;
        }
        @Override
        public byte[] getCamPhotoBytes(){ // 获取图片bytes
            return camPhotoBytes!=null?camPhotoBytes:camPhoto!=null?Utils.bitmap2bytes(camPhoto):null;
        }
        @Override
        public String startRecord(){return iEspHandle.startRecord();}
        @Override
        public String endRecord(){
            return iEspHandle.endRecord();
        }

        @Override
        public void setAdditionalBitmap(Bitmap bm){
            additionalBm = bm;
            additionalReco = true;
        }
        @Override
        public ParcelDetectResult getDetectedRes(){
            ParcelDetectResult pdr = null;
            String resultStr;
            Bitmap bitmap;
            if(additionalReco){
                bitmap = additionalBm;
            }else{
                bitmap = getCamPhoto();
            }
            if (bitmap != null) {
                if(!iYNhandle.isModelLoaded()){
                    iYNhandle.initModel(yoloPath);
                }
                DetectResult dr = iYNhandle.runModel(bitmap,YNManager.STATE_DETECTION);
                if(dr.getSwitchCount() == 0){
                    resultStr = "未检测到开关";
                }else if((dr.getSwitchCount() == 3 && dr.getState() != 2) ||
                            (dr.getSwitchCount() == 1 &&
                                    !(dr.getSwitchType().equals(SwitchType.dieLuo) ||
                                            dr.getSwitchType().equals(SwitchType.daoZha)))){
                    String st;
                    if(dr.getSwitchType() == SwitchType.dieLuo){
                        st = "跌落保险";
                    }else if(dr.getSwitchType() == SwitchType.daoZha || dr.getSwitchType() == SwitchType.ZW32daoKai){
                        st = "刀闸（隔离开关）";
                    }else{
                        st = "开关（断路器）";
                    }
                    resultStr = "开关类型：" + st+'\n';
                    resultStr += "状态：" + ((dr.getState() == 3) ? "已取下" : (dr.getState() == 1) ? "已合上" :  "已拉开"
                    ) +
                            "\nA相：" + ((dr.getStateA() == 3) ? "取下" :(dr.getStateA() == 1) ? "合上" : "拉开") +
                            "\nB相：" + ((dr.getStateB() == 3) ? "取下" :(dr.getStateB() == 1) ? "合上" : "拉开") +
                            "\nC相：" + ((dr.getStateC() == 3) ? "取下" :(dr.getStateC() == 1) ? "合上" : "拉开");
                }else if(dr.getSwitchCount()<3 &&
                        (dr.getSwitchType().equals(SwitchType.dieLuo) ||
                        dr.getSwitchType().equals(SwitchType.daoZha))){
                    String st;
                    if(dr.getSwitchType() == SwitchType.dieLuo){
                        st = "跌落保险";
                    }else{
                        st = "刀闸（隔离开关）";
                    }
                    resultStr = "开关类型：" + st+'\n';
                    resultStr += "未检测齐3相开关";
                }else{
                    resultStr = "检测结果包含多种开关，请重试";
                }
                // 返回Parcelable对象
                pdr = new ParcelDetectResult(dr.getBitmap(),dr.getStateA(),dr.getStateB(),dr.getStateC(),dr.getState(),resultStr);
            }
            if(additionalReco){
                additionalReco = false;
                additionalBm = null;
            }
            return pdr;
        }
        @Override
        public ParcelDetectResult getOcrRes(){
            ParcelDetectResult pdr = null;
            //处理OCR
            StringBuilder resultStr = new StringBuilder();
            Bitmap bitmap;
            if(additionalReco){
                bitmap = additionalBm;
            }else{
                bitmap = getCamPhoto();
            }
            if(bitmap != null){
                if(!iOcrHandle.isModelLoaded()){
                    iOcrHandle.initModel(This);
                }
                DetectResult dr = iOcrHandle.runModel(bitmap);
                String[] strArr = dr.getRecoID().split("\t");
                for(int i=strArr.length-1; i>-1 ;i--){
                    resultStr.append(strArr[i]);
                }
                pdr = new ParcelDetectResult(dr.getBitmap(),resultStr.toString());
            }
            if(additionalReco){
                additionalReco = false;
                additionalBm = null;
            }
            return  pdr;
        }

        @Override
        public ParcelDetectResult getWhetherToTestElectro(char selectPhase){
            // handle whether detect the phase
            boolean res = false;
            boolean openCam = false;
            Bitmap bitmap = null;
            boolean espReady = getEspStates(YANDIAN_CAMERA,false);
            if(yandianCamLinked && espReady){
                phase = selectPhase;
                bitmap = getCamPhoto();
                openCam = true;
            }
            if(openCam){
                iEspHandle.sendPulse("newReceivePulse");
                Date start = new Date();
                long waitDuration = 0;
                testingContact = true;
                while(testingContact && waitDuration<WAITING_TIME){waitDuration = new Date().getTime() - start.getTime();}
                if(!testingContact){
//                    if(bitmap != null) {
//                        if (!iYNhandle.isModelLoaded()) {
//                            iYNhandle.initModel(yoloPath);
//                        }
//                        DetectResult dr = iYNhandle.runModel(bitmap, YNManager.YANDIAN);
//                        res = phaseContacted && dr.getOnYanDian();
//                        bitmap = dr.getBitmap();
//                    }
                    res = phaseContacted;
                }
            }
            return new ParcelDetectResult(bitmap,openCam,res);
        }
        @Override
        public String getElectroTestRes(){
            //处理验电
            iEspHandle.sendPulse("receivePulse");
            testingElectro = true;
            Date start = new Date();
            long waitDuration = 0;
            while(testingElectro && waitDuration<WAITING_TIME){waitDuration = new Date().getTime() - start.getTime();}
            String res;
            if(testingElectro){
                res = phase + "相验电超时，请重试";
            }else{
                if(phaseElectried){
                    res = phase + "相带电";
                }else{
                    res = phase + "相不带电";
                }
            }
            phase = ' ';
            return res;
        }
        @Override
        public boolean lockDevice(){//闭锁处理
            return deviceLockOrUnlock(true);
        }
        @Override
        public boolean unLockDevice(){//解锁处理
            return deviceLockOrUnlock(false);
        }

        @Override
        public ParcelFaceResult getFaceResult(Bitmap bm){
            if(!iFaceHandle.isModelLoaded()){
                iFaceHandle.initModel();
            }
            PuruiResult res = iFaceHandle.runModel(bm);
            return new ParcelFaceResult(res.isDone(),res.getBitmap(),res.getDetails());
        }
        @Override
        public ParcelFaceResult[] checkAllFaces(){
            PuruiResult[] facesInfo = iFaceHandle.checkFaces();
            ParcelFaceResult[] res = new ParcelFaceResult[facesInfo.length];
            int i=0;
            for(PuruiResult face:facesInfo){
                res[i++] = new ParcelFaceResult(true,face.getBitmap(),face.getDetails());
            }
            return res;
        }
        @Override
        public ParcelFaceResult addFace(String name, Bitmap bm){
            PuruiResult res  = iFaceHandle.addFace(name,bm);
            return new ParcelFaceResult(res.isDone(),res.getBitmap(),res.getDetails());
        }
        @Override
        public ParcelFaceResult deleteFace(String name){
            PuruiResult res  = iFaceHandle.deleteFace(name);
            return new ParcelFaceResult(res.isDone(),res.getBitmap(),res.getDetails());
        }


    };

    private boolean getEspStates(int CAMID, boolean showErrorMsg) {
        PuruiResult espInit = iEspHandle.getEspReady(CAMID);
//        if(showErrorMsg && !espInit.isDone()){
//            makeToast(espInit.getDetails());
//        }
        return espInit.isDone();
    }
    private void setNoCamImage(){
        if(!canSetNoCam){
            return;
        }
        final int num = callbackList.beginBroadcast();
        try {
            for (int i = 0; i < num; i++) {
                callbackList.getBroadcastItem(i).setNoCamImage();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        callbackList.finishBroadcast();
    }
    private boolean canSetNoCam = true;
    private boolean deviceLockOrUnlock(boolean cmdLockDevice){ //true represents LOCK else UNLOCK
        boolean espReady = getEspStates(REMOTE_CAMERA,false);
        boolean ret = false;
        boolean tempLinking;
        boolean isCurrentConditionEqual2Target = cmdLockDevice?isDeviceLocked:isDeviceUnlocked;
        if(espReady){
            if(!isCurrentConditionEqual2Target){ //the current state is not equal to the target state
                String pulse = cmdLockDevice?"longPulse":"shortPulse";
                tempLinking = false; //link remote cam for sending pulse without showing video
                if(!remoteCamLinked){  // not linked before:
                    waitingEspCall = true;
                    iEspHandle.setCamera(REMOTE_CAMERA,false);
                    Date start = new Date();
                    long waitDuration = 0;
                    while(waitingEspCall && waitDuration<WAITING_TIME){waitDuration = new Date().getTime() - start.getTime();}
                    tempLinking = true;
                }
                waitingEspCall = true;
                Date start = new Date();
                long waitDuration = 0;
                int waitingTime = WAITING_TIME+8000;
                iEspHandle.sendPulse(pulse);
                while(waitingEspCall && waitDuration<waitingTime){waitDuration = new Date().getTime() - start.getTime();}
                if(espSentPulse){
                    espSentPulse = false;
                    if(cmdLockDevice){ //lock success!
                        isDeviceLocked = true;
                        isDeviceUnlocked = false;
                    }else{ //unlock success!
                        isDeviceUnlocked = true;
                        isDeviceLocked = false;
                    }
                    ret = true;
                }
                if(tempLinking){
                    canSetNoCam = false;
                    if(yandianCamLinked){ // 重启验电摄像头
                        waitingEspCall = true;
                        iEspHandle.setCamera(YANDIAN_CAMERA,true);
                        start = new Date();
                        waitDuration = 0;
                        while(waitingEspCall && waitDuration<WAITING_TIME){waitDuration = new Date().getTime() - start.getTime();}
                    }else{
                        iEspHandle.releaseCamera();
                    }
                    remoteCamLinked = false;
                }
            }else {
                ret = true;
            }
        }
        return ret;
    }

    @Override
    public void setCamPhoto(Bitmap bitmap) {
        camPhoto = bitmap;
        final int num= callbackList.beginBroadcast();
        for (int i=0;i<num;i++){
            IPuAidlCallback iAidlCallBack=callbackList.getBroadcastItem(i);
            try {
                iAidlCallBack.setCamPhoto(bitmap);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        callbackList.finishBroadcast();
    }

    @Override
    public void setCamPhotoBytes(byte[] bytes) {
        camPhotoBytes=bytes.clone();
        camPhoto = Utils.bytes2bitmap(bytes,Bitmap.Config.ARGB_4444,1);
        final int num= callbackList.beginBroadcast();
        for (int i=0;i<num;i++){
            IPuAidlCallback iAidlCallBack=callbackList.getBroadcastItem(i);
            try {
                iAidlCallBack.setCamPhotoBytes(bytes);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        callbackList.finishBroadcast();
    }

    @Override
    public void requestCameraPermission() {

    }
}
