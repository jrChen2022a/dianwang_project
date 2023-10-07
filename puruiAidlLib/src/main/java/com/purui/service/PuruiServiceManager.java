package com.purui.service;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.purui.service.Utils.Utils;
import com.purui.service.parcel.ParcelDetectResult;
import com.purui.service.parcel.ParcelFaceResult;
import com.purui.service.result.CamSelectResult;
import com.purui.service.result.ElectricalResult;
import com.purui.service.result.FaceResult;
import com.purui.service.result.LockResult;
import com.purui.service.result.OcrResult;
import com.purui.service.result.StateResult;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class PuruiServiceManager implements IPuruiService{
    private final Context mainCtx;
    private boolean[] isABCtested = {false,false,false};
    private int isAElectro = 2; // 0 stands for no, 1 for yes, 2 for null
    private int isBElectro = 2;
    private int isCElectro = 2;
    private int camType = CAM_WU;
    private IPuAidlInterface iAidlInterface;

    public PuruiServiceManager(@NonNull Context ctx){
        this.mainCtx = ctx;
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            iAidlInterface = IPuAidlInterface.Stub.asInterface(iBinder);
            try {
                resetAll();
                iAidlInterface.registerCallBack(iAidlCallBack);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (ContextCompat.checkSelfPermission(mainCtx, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(mainCtx, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(mainCtx, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {//多个权限一起申请
                ActivityCompat.requestPermissions((Activity) mainCtx, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                }, 1);
            }
            serviceConnectionListener.onConnected();
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceConnectionListener.onDisconnected();
        }
    };
    private final IPuAidlCallback iAidlCallBack=new IPuAidlCallback.Stub() {
        @Override
        public void setNoCamImage(){

        }

        @Override
        public void setCamPhoto(Bitmap bitmap){
            if(isRecording){
                if(recordListener!=null){
                    recordListener.onRecording(bitmap);
                }
            }else{
                if(camListener != null) {
//                    camListener.onCamShow(Utils.getBase64ImgCode(bitmap));
                    camListener.onCamShow(bitmap);
                }
            }
        }

        @Override
        public void setCamPhotoBytes(byte[] bytes){
            if(camListener != null) camListener.onCamShow(bytes);
            setCamPhoto(BitmapFactory.decodeByteArray(bytes,0,bytes.length));
        }

    };

    private ServiceConnectionListener serviceConnectionListener;
    
    @Override
    public void createService(ServiceConnectionListener listener) {
        serviceConnectionListener = listener;
        Intent intent = new Intent(mainCtx, PuService.class);
        mainCtx.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void destroyService(){
        testingElectricity = false;
        if(testElectricityThread != null){
            testElectricityThread.interrupt();
        }
        selectCamera(Objects.requireNonNull(camType2cameraType.get(CAM_WU)),null);
        //解除注册
        if (null != iAidlInterface && iAidlInterface.asBinder().isBinderAlive()) {
            try {
                iAidlInterface.unregisterCallBack(iAidlCallBack);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mainCtx.unbindService(serviceConnection);
        serviceConnectionListener.onDisconnected();
    }


    @Override
    public FaceResult getFaceResult(@NonNull Bitmap photo){
        ParcelFaceResult ret = null;
        if (iAidlInterface != null) {
            try {
                ret = iAidlInterface.getFaceResult(photo);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return ret == null?null:new FaceResult(ret.getSuccess(),ret.getName(),ret.getFace());
    }
    @Override
    public FaceResult[] checkFaces(){
        ParcelFaceResult[] serviceRes = new ParcelFaceResult[0];
        if (iAidlInterface != null) {
            try {
                serviceRes = iAidlInterface.checkAllFaces();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        FaceResult[] ret = new FaceResult[serviceRes.length];
        int i=0;
        for(ParcelFaceResult pfr:serviceRes){
            ret[i++] = new FaceResult(pfr.getSuccess(),pfr.getName(),pfr.getFace());
        }
        return ret;
    }
    @Override
    public FaceResult addFace(@NonNull String name, @NonNull Bitmap photo){
        ParcelFaceResult ret = null;
        if (iAidlInterface != null) {
            try {
                ret = iAidlInterface.addFace(name, photo);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if(ret == null)return null;
        return new FaceResult(ret.getSuccess(), ret.getName(), ret.getFace());
    }
    @Override
    public FaceResult deleteFace(@NonNull String name){
        ParcelFaceResult ret = null;
        if (iAidlInterface != null) {
            try {
                ret = iAidlInterface.deleteFace(name);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if(ret == null)return null;
        return new FaceResult(ret.getSuccess(), ret.getName(),null);
    }

    @Override
    public String scanCameras(){
        selectCamera("关闭", (CameraShowListener) null);
        String msg="";
        if (iAidlInterface != null) {
            try {
                msg = iAidlInterface.scanCameras();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return msg;
    }



    private boolean isRecording = false;
    private CameraShowListener camListener;
    @Override
    public CamSelectResult selectCamera(String cameraType, CameraShowListener listener) {
        int newCamType = cameraType.equals("验电器") ? CAM_YAN : cameraType.equals("操作杆") ? CAM_REMOTE : cameraType.equals("平板") ? CAM_PAD : CAM_WU;
        if(camType == newCamType){
            return new CamSelectResult(true, cameraType, camType, "摄像头状态未更改");
        }
        if(isRecording){
            stopRecord();
        }
        camType = newCamType;
        camListener = listener;
        boolean retRes = false;
        String retMsg = "远程服务已断开，摄像头连接失败";
        if (iAidlInterface != null) {
            try {
                retRes = iAidlInterface.selectCamera(camType);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if(retRes){
                retMsg = camType==CAM_WU?"关闭成功":"摄像头打开成功";
            }else{
                retMsg = camType==CAM_WU?"关闭失败":"摄像头连接失败";
            }
        }
        return new CamSelectResult(retRes,cameraType,camType,retMsg);
    }

    private RecordListener recordListener;
    public void recordVideo(RecordListener listener) {
        recordListener = listener;
        if(camType<2) {
            recordListener.onFail("未开启操作杆/验电器的摄像头");
            return;
        }
        if(!isRecording){
            startRecord();
        }else {
            stopRecord();
        }
    }
    private void startRecord() {
        String resback = "";
        //操作杆摄像头是否已开启
        if(camType != CAM_REMOTE && camType != CAM_YAN){
            recordListener.onFail("未开启操作杆摄像头");
        }
        //未录像，开始录像
        if (iAidlInterface != null) {
            try {
                resback = iAidlInterface.startRecord();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        boolean success=false;
        if(!resback.equals("")){
            success = resback.charAt(resback.length()-1) == '1';
        }
        isRecording = success;
        if(success){
            recordListener.onStartRecord(resback);
        }else{
            recordListener.onFail(resback);
        }
    }
    private void stopRecord(){
        //录像中，退出录像并保存
        String resback="";
        if (iAidlInterface != null) {
            try {
                resback = iAidlInterface.endRecord();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        boolean success=false;
        if(!resback.equals("")){
            success = resback.charAt(resback.length()-1) == '1';
        }
        isRecording = !success;
        if(success){
            recordListener.onEndRecord(resback);
        }else{
            recordListener.onFail(resback);
        }
    }

    @Override
    public Bitmap getCamFrame() {
        try {
            return iAidlInterface.getCamPhoto();//获取图片
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
//    @Override
//    public byte[] getCamFrameBytes() {
//        try {
//            return iAidlInterface.getCamPhotoBytes();//获取图片
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    @Override
    public StateResult detectStates(@NonNull Bitmap inBitmap, String switchType, String targetState) {
        if(camType == CAM_WU){
            return new StateResult(false,"请打开摄像头",4,4,4,4,null);
        }
        boolean changeOffToTakeOn = "给上".equals(targetState);
        ParcelDetectResult resback = null;
        if (iAidlInterface != null) {
            try {
                iAidlInterface.setAdditionalBitmap(inBitmap);
                resback = iAidlInterface.getDetectedRes();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        boolean needChecked;
        if(resback == null){
            return null;
        }
        String resstr = resback.getStateResult();
        if("给上".equals(targetState)){
            resstr = resstr.replaceAll("拉开","给上");
        }
        needChecked = resstr.contains("开关类型："+switchType) && resstr.contains("状态：已"+targetState);
        int stateA = (resback.getStateA() == 2)?4:resback.getStateA();
        int stateB = (resback.getStateB() == 2)?4:resback.getStateB();
        int stateC = (resback.getStateC() == 2)?4:resback.getStateC();
        int state = 4;
        switch(resback.getState()){
            case 0:
                state = changeOffToTakeOn ?2:resback.getState();
                break;
            case 1: case 3:
                state = resback.getState();
                break;
            default:
                break;
        }
        return new StateResult(needChecked,resstr,state,stateA,stateB,stateC, resback.getResBitmap());
    }

    @Override
    public OcrResult recognizeID(@NonNull Bitmap inBitmap, String inID) {
        if(camType == CAM_WU){
            return new OcrResult(false,"","请打开摄像头",null);
        }
        String serialKey= null;
        // 提取括号中的关键串
        List<String> inKeys = Utils.splitByParentheses(inID);
        for (int i = inKeys.size() - 1; i >= 0; i--) {
            String inKey = inKeys.get(i);
            if(Utils.isAlphanumeric(inKey) && inKey.length()>4){
                serialKey = inKey;
                break;
            }
        }
        if(serialKey != null){
            inID = inID.replace(serialKey,"");
        }
        inID = Utils.removeSpecialChars(inID).toUpperCase();
        boolean res = false;
        Bitmap retBitmap = null;
        String resID = "";
        if (iAidlInterface != null) {
            try {
                iAidlInterface.setAdditionalBitmap(inBitmap);
                ParcelDetectResult pdr = iAidlInterface.getOcrRes();
                resID = pdr.getRecoID();
                retBitmap = pdr.getResBitmap();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        String ID = Utils.removeSpecialChars(resID).toUpperCase();
        if (serialKey != null && ID.contains(serialKey) || (serialKey == null && ID.contains(inID))) {
            res = true;
        }
        return new OcrResult(res, resID, "识别成功", retBitmap);
    }

    @Override
    public LockResult unlockDevice() {
        boolean res = false;
        if (iAidlInterface != null) {
            try {
                res = iAidlInterface.unLockDevice();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return new LockResult(res, res?"设备已解锁":"设备解锁失败");
    }
    @Override
    public LockResult lockDevice() {
        boolean res = false;
        if (iAidlInterface != null) {
            try {
                res = iAidlInterface.lockDevice();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return new LockResult(res, res?"设备已闭锁":"设备闭锁失败");
    }
    private Thread testElectricityThread = null;
    private volatile boolean testingElectricity = false;

    @Override
    public void testElectricity(char currentPhase, TestEleCallback cb) {
        if(camType != CAM_YAN){
            cb.onFail(new ElectricalResult(false,currentPhase+"","无效","无效","请打开验电摄像头",null));
        }
        testElectricityThread = new Thread(()->{
            boolean whetherToTest = false;
            boolean openYandianCam = true;
            Bitmap retBitmap = null;
            testingElectricity = true;
            while(!whetherToTest && testingElectricity) {
                if (iAidlInterface != null) {
                    try {
                        ParcelDetectResult pdr = iAidlInterface.getWhetherToTestElectro(currentPhase);
                        if (pdr != null) {
                            whetherToTest = pdr.getWhetherToTest();
                            openYandianCam = pdr.isOpenYanCam();
                            retBitmap = pdr.getResBitmap();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(whetherToTest){
                String resback = null;
                if (iAidlInterface != null) {
                    try {
                        resback = iAidlInterface.getElectroTestRes();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                boolean validCheck = resback != null && resback.contains("带电");
                int phase = -1;
                if(resback != null){
                    phase = resback.contains("A相")?0:resback.contains("B相")?1:2;
                    isABCtested[phase] = validCheck;
                }
                if(!validCheck){
                    cb.onFail(new ElectricalResult(false, currentPhase + "", "无效", "无效", resback, retBitmap));
                }else {
                    int ABCeleRes = (resback.contains("不带电"))?0:1;
                    String eleRes = ABCeleRes==0?"不带电":"带电";
                    if(phase == 0){
                        isAElectro = ABCeleRes;
                    }else if(phase == 1){
                        isBElectro = ABCeleRes;
                    }else{
                        isCElectro = ABCeleRes;
                    }
                    String totalRes = "无效";
                    if(isABCtested[0]&isABCtested[1]&isABCtested[2]){
                        resback += "\n验电完毕";
                        if(isAElectro == 0 && isBElectro == 0 && isCElectro == 0){
                            resback += "\n结果：不带电";
                            totalRes = "不带电";
                        }else{
                            resback += "\n结果：带电";
                            totalRes = "带电";
                        }
                    }
                    cb.onSuccess(new ElectricalResult(true, currentPhase + "", eleRes, totalRes, resback, retBitmap));
                }
            } else {
                if(camType == CAM_YAN && !openYandianCam){
                    selectCamera("关闭", (CameraShowListener) null);
                }
                String detail = !openYandianCam?"验电摄像头未开启":"未满足验电条件，不可以执行验电操作";
                cb.onFail(new ElectricalResult(false,currentPhase+"","无效","无效",detail,retBitmap));
            }
        });
        testElectricityThread.setDaemon(true);
        testElectricityThread.start();
    }

    @Override
    public void resetAll() {
        isABCtested = new boolean[]{false,false,false};
        isAElectro = 2;
        isBElectro = 2;
        isCElectro = 2;
    }

    private static final HashMap<Integer, String> camType2cameraType = new HashMap<>();
    private static final int CAM_WU = 0;
    private static final int CAM_PAD = 1;
    private static final int CAM_REMOTE = 2;
    private static final int CAM_YAN = 3;
    static {
        camType2cameraType.put(CAM_WU,"无");
        camType2cameraType.put(CAM_PAD,"平板");
        camType2cameraType.put(CAM_REMOTE,"远程摄像头");
        camType2cameraType.put(CAM_YAN,"验电摄像头");
    }

}