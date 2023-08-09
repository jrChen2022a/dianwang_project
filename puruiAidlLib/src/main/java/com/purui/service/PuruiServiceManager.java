package com.purui.service;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.TextureView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.purui.service.Utils.Utils;
import com.purui.service.parcel.ParcelDetectResult;
import com.purui.service.parcel.ParcelFaceResult;
import com.purui.service.ynmodule.DrawDialogFragment;
import com.purui.service.ynmodule.DrawDialogTask;

import java.util.HashMap;
import java.util.List;

public class PuruiServiceManager implements IPuruiService, IInnerService{
    private final AppCompatActivity mainCtx;
    private boolean[] isABCtested = {false,false,false};
    private int isAElectro = 2; // 0 stands for no, 1 for yes, 2 for null
    private int isBElectro = 2;
    private int isCElectro = 2;
    private int camType = CAM_WU;
    private IPuAidlInterface iAidlInterface;
    private IPuruiCallback iPuruiCallback;
    private final ProgressDialog mProgressDialog;
    private final Handler handler;
    private final int mode;
    private TextView tvRec;
    private CheckBox cbIsOff;
    private CheckBox cbIsOn;
    private CheckBox cbDeviceUnlock;
    private CheckBox cbDeviceLock;
    private ImageView ivOri;
    private ImageView ivDet;
    private ImageView ivA;
    private ImageView ivB;
    private ImageView ivC;
    private TextView tvElectroA;
    private TextView tvElectroB;
    private TextView tvElectroC;
    private ImageView ivDevice;
    private RadioGroup rbCameraType;
    private CheckBox cbIsTakeOff;
    private CheckBox cbIsTakeOn;
    private boolean changeOffToTakeOn = false;
    public PuruiServiceManager(AppCompatActivity mainActivity, ImageView imageViewOri, ImageView ivDet, TextureView textureView,
                               TextView tvRec, CheckBox cbIsOn, CheckBox cbIsOff,
                               ImageView ivA, ImageView ivB, ImageView ivC,
                               TextView tvEleA, TextView tvEleB, TextView tvEleC,
                               ImageView ivDevice, CheckBox cbDeviceUnlock, CheckBox cbDeviceLock,
                               RadioGroup rbCameraType, CheckBox cbIsTakeOff, CheckBox cbIsTakeOn) {
        this.mainCtx = mainActivity;
        this.ivOri = imageViewOri;
        this.ivDet = ivDet;
        this.tvRec = tvRec;
        this.cbIsOn = cbIsOn;
        this.cbIsOff = cbIsOff;
        this.cbDeviceLock = cbDeviceLock;
        this.cbDeviceUnlock = cbDeviceUnlock;
        this.ivA = ivA;
        this.ivB = ivB;
        this.ivC = ivC;
        this.tvElectroA = tvEleA;
        this.tvElectroB = tvEleB;
        this.tvElectroC = tvEleC;
        this.ivDevice = ivDevice;
        this.rbCameraType = rbCameraType;
        this.cbIsTakeOff = cbIsTakeOff;
        this.cbIsTakeOn = cbIsTakeOn;
        mProgressDialog = new ProgressDialog(mainCtx);
        mProgressDialog.setCancelable(false);
        handler = new Handler();
        mode = 0;
    }
    public PuruiServiceManager(@NonNull IPuruiCallback iPuruiCallback){
        this.iPuruiCallback = iPuruiCallback;
        this.mainCtx = iPuruiCallback.getActivity();
        mProgressDialog = new ProgressDialog(mainCtx);
        mProgressDialog.setCancelable(false);
        handler = new Handler();
        mode = 1;
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            iAidlInterface = IPuAidlInterface.Stub.asInterface(iBinder);
            try {
                resetAll();
                if(mode == 0){
                    Bitmap bmp = iAidlInterface.getServiceBitmap("camera_off");
                    ivDevice.setImageBitmap(Utils.resizeBitmap(iAidlInterface.getServiceBitmap("lock"),ivDevice));
                    ivOri.setImageBitmap(Utils.resizeBitmap(bmp,ivOri));
                    ivDet.setImageBitmap(Utils.resizeBitmap(bmp,ivDet));
                }else if(mode == 1){
                    iPuruiCallback.setLockStateUI(1);
                    iPuruiCallback.setDefaultCamUI();
                }
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
//            iPuruiCallback.setDefaultCamUI();
            //serviceConnectionListener.onConnected();
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Toast.makeText(mainCtx.getApplicationContext(), "检测服务已断开", Toast.LENGTH_SHORT).show();
            //serviceConnectionListener.onDisconnected();
        }
    };
    private final IPuAidlCallback iAidlCallBack=new IPuAidlCallback.Stub() {
        @Override
        public void setNoCamImage(){
            if(mode ==0){
                try{
                    Bitmap bm = iAidlInterface.getServiceBitmap("camera_off");
                    ivOri.setImageBitmap(Utils.resizeBitmap(bm,ivOri));
                    ivDet.setImageBitmap(Utils.resizeBitmap(bm,ivDet));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }else {
                iPuruiCallback.setDefaultCamUI();
            }
        }

        @Override
        public void setCamPhoto(Bitmap bitmap){
            if(mode == 0){
                ivOri.setImageBitmap(Utils.resizeBitmap(bitmap, ivOri));
            }else if(mode == 1){
                iPuruiCallback.setCamUI(bitmap);
            }
        }

        @Override
        public void setCamPhotoBytes(byte[] bytes){
            //if(camListener != null) camListener.onCamShow(bytes);
            setCamPhoto(BitmapFactory.decodeByteArray(bytes,0,bytes.length));
        }

    };

    @Override
    public void createService() {
        Intent intent = new Intent(mainCtx, PuService.class);
        mainCtx.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void destroyService(){
        //解除注册
        if (null != iAidlInterface && iAidlInterface.asBinder().isBinderAlive()) {
            try {
                iAidlInterface.unregisterCallBack(iAidlCallBack);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mainCtx.unbindService(serviceConnection);
//        serviceConnectionListener.onDisconnected();
    }

    @Override
    public void stopService() {
        selectCamera("关闭");
    }

    @Override
    public void resumeService() {

    }

    @Override
    public PuruiResult getFaceResult(@NonNull Bitmap photo){
        ParcelFaceResult ret = null;
        if (iAidlInterface != null) {
            try {
                ret = iAidlInterface.getFaceResult(photo);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return ret == null?null:new PuruiResult(ret.getSuccess(),ret.getName(),ret.getFace());
    }
    @Override
    public PuruiResult[] checkFaces(){
        ParcelFaceResult[] serviceRes = new ParcelFaceResult[0];
        if (iAidlInterface != null) {
            try {
                serviceRes = iAidlInterface.checkAllFaces();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        PuruiResult[] ret = new PuruiResult[serviceRes.length];
        int i=0;
        for(ParcelFaceResult pfr:serviceRes){
            ret[i++] = new PuruiResult(pfr.getSuccess(),pfr.getName(),pfr.getFace());
        }
        return ret;
    }
    @Override
    public PuruiResult addFace(@NonNull String name, @NonNull Bitmap photo){
        ParcelFaceResult ret = null;
        if (iAidlInterface != null) {
            try {
                ret = iAidlInterface.addFace(name, photo);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if(ret == null)return null;
        return new PuruiResult(ret.getSuccess(), ret.getName(), ret.getFace());
    }
    @Override
    public PuruiResult deleteFace(@NonNull String name){
        ParcelFaceResult ret = null;
        if (iAidlInterface != null) {
            try {
                ret = iAidlInterface.deleteFace(name);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if(ret == null)return null;
        return new PuruiResult(ret.getSuccess(), ret.getName(),null);
    }

    @Override
    public void scanCameras(){
        selectCamera("关闭");
        if (iAidlInterface != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String msg="";
                    try {
                        msg = iAidlInterface.scanCameras();
                        String finalMsg = msg;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                new AlertDialog.Builder( mainCtx)
                                        .setTitle("扫描结果")//设置对话框的标题
                                        .setMessage(finalMsg)//设置对话框的内容
                                        .setPositiveButton("确定", (dialog1, which) -> dialog1.dismiss())//设置对话框的按钮
                                        .create().show();
                            }
                        });
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            mProgressDialog.setTitle("正在扫描摄像头...");
            mProgressDialog.setMessage("请确保摄像头已连接上热点，并尽量靠近当前设备");
            mProgressDialog.show();
        }
    }

    @Override
    public PuruiResult selectCamera(String cameraType) {
        int newCamType = cameraType.equals("验电器") ? CAM_YAN : cameraType.equals("操作杆") ? CAM_REMOTE : cameraType.equals("平板") ? CAM_PAD : CAM_WU;
        if (camType == newCamType) {
            return new PuruiResult(true, "摄像头状态未更改");
        }
        camType = newCamType;
        if(mode == 0){
            Utils.checkButtonInViewGroup(rbCameraType, cameraType);
        }else if(mode == 1){
            iPuruiCallback.setCamSelectUI(newCamType);
        }
        boolean retRes = false;
        String retMsg = "远程服务已断开，摄像头连接失败";
        if (iAidlInterface != null) {
            try {
                retRes = iAidlInterface.selectCamera(camType);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (retRes) {
                retMsg = camType == CAM_WU ? "关闭成功" : "摄像头打开成功";
            } else {
                retMsg = camType == CAM_WU ? "关闭失败" : "摄像头连接失败";
            }
        }
        if(!retRes){
            if(mode == 0){
                Utils.checkButtonInViewGroup(rbCameraType,"关闭");
            }else if(mode == 1){
                iPuruiCallback.setCamSelectUI(0);
                iPuruiCallback.setDefaultCamUI();
            }
        }
        return new PuruiResult(retRes, retMsg);
    }

    private String innerSwitchType;
    private String innerTargetState;
    private DetectStatesListener detectStatesListener;
    @Override
    public void preDetectStates(String switchType, String targetState, DetectStatesListener listener) {
//        getDetectMode = DETECT_MODE_CAM;
        detectStatesListener = listener;
        if(camType == CAM_WU){
            Toast.makeText(mainCtx.getApplicationContext(), "请打开摄像头", Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap bm = null;
        try {
            bm = iAidlInterface.getCamPhoto();//获取图片
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        innerSwitchType = switchType;
        innerTargetState = targetState;
        DrawDialogFragment dialog = new DrawDialogFragment();
        DrawDialogTask task = new DrawDialogTask(mainCtx.getSupportFragmentManager(), dialog,bm,this,DrawDialogFragment.MODE_STATE);//
        task.execute();
    }

    @Override
    public void preDetectStates(Bitmap inBitmap, String switchType, String targetState, DetectStatesListener listener) {
//        getDetectMode = DETECT_MODE_INBITMAP;
        detectStatesListener = listener;
        selectCamera("平板");
        innerSwitchType = switchType;
        innerTargetState = targetState;
        DrawDialogFragment dialog = new DrawDialogFragment();
        DrawDialogTask task = new DrawDialogTask(mainCtx.getSupportFragmentManager(), dialog,inBitmap,this,DrawDialogFragment.MODE_STATE);//
        task.execute();
    }
    @Override
    public void postDetectStatesAfterDraw(Bitmap draw){
        if (iAidlInterface != null) {
            try {
                iAidlInterface.setAdditionalBitmap(draw);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        innerDetect(innerSwitchType,innerTargetState);
    }

    private PuruiResult innerDetect(String switchType, String targetState) {
        changeOffToTakeOn ="给上".equals(targetState);
        ParcelDetectResult resback = null;
        if (iAidlInterface != null) {
            try {
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
        if(mode == 0){
            ivDet.setImageBitmap(Utils.resizeBitmap(resback.getResBitmap(),ivDet));
            setStatus(resback.getStateA(),resback.getStateB(),resback.getStateC(),resback.getState());
        }else if(mode == 1) {
            int stateA = (resback.getStateA() == 2) ? 4 : resback.getStateA();
            int stateB = (resback.getStateB() == 2) ? 4 : resback.getStateB();
            int stateC = (resback.getStateC() == 2) ? 4 : resback.getStateC();
            int state = 4;
            switch (resback.getState()) {
                case 0:
                    state = changeOffToTakeOn ? 2 : resback.getState();
                    break;
                case 1:
                case 3:
                    state = resback.getState();
                    break;
                default:
                    break;
            }
            iPuruiCallback.setDetUI(resback.getResBitmap());
            iPuruiCallback.setStatesDetectionUI(state, stateA, stateB, stateC);
        }
        PuruiResult res = new PuruiResult(needChecked, resstr, resback.getResBitmap());
        detectStatesListener.onDetected(res);
        return res;
    }
    private void setStatus(int stateA, int stateB, int stateC, int state){
        /*
         * state(ABC)
         *  0: 拉开（给上）
         *  1: 合上
         *  2: 无效
         *  3: 取下
         */
        if(mode == 0){
            switch (state){
                case 1:
                    cbIsOn.setChecked(true);
                    cbIsOff.setChecked(false);
                    cbIsTakeOff.setChecked(false);
                    cbIsTakeOn.setChecked(false);
                    break;
                case 0:
                    cbIsOn.setChecked(false);
                    if(changeOffToTakeOn){
                        cbIsTakeOn.setChecked(true);
                        cbIsOff.setChecked(false);
                    }else {
                        cbIsOff.setChecked(true);
                        cbIsTakeOn.setChecked(false);
                    }
                    cbIsTakeOff.setChecked(false);
                    break;
                case 3:
                    cbIsOn.setChecked(false);
                    cbIsOff.setChecked(false);
                    cbIsTakeOff.setChecked(true);
                    cbIsTakeOn.setChecked(false);
                    break;
                default:
                    cbIsOn.setChecked(false);
                    cbIsOff.setChecked(false);
                    cbIsTakeOff.setChecked(false);
                    cbIsTakeOn.setChecked(false);
                    break;
            }
            setIconAccordingToState(ivA,stateA);
            setIconAccordingToState(ivB,stateB);
            setIconAccordingToState(ivC,stateC);
        }
    }
    private void setIconAccordingToState(ImageView iv, int state){
        Bitmap icon = null;
        try {
            if(state == 0){
                icon = iAidlInterface.getServiceBitmap("result_off");
            }else if(state == 1){
                icon = iAidlInterface.getServiceBitmap("result_on");
            }else if(state == 3){
                icon = iAidlInterface.getServiceBitmap("result_take");
            }else{
                icon = iAidlInterface.getServiceBitmap("result_invalid");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        iv.setImageBitmap(Utils.resizeBitmap(icon,iv));
    }
//    private int getDetectMode = 0;
    private static final int DETECT_MODE_CAM = 1;
    private static final int DETECT_MODE_INBITMAP = 2;
    private String innerOcrID;
    @Override
    public void preRecognizeID(String inID, RecognizeIDListener listener) {
//        getDetectMode = DETECT_MODE_CAM;
        recoIDListener = listener;
        if(camType == CAM_WU){
            Toast.makeText(mainCtx, "请打开摄像头", Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap bm = null;
        try {
            bm = iAidlInterface.getCamPhoto();//获取图片
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        innerOcrID = inID;
        DrawDialogFragment dialog = new DrawDialogFragment();
        DrawDialogTask task = new DrawDialogTask(mainCtx.getSupportFragmentManager(), dialog,bm,this,DrawDialogFragment.MODE_OCR);//
        task.execute();
    }
    @Override
    public void preRecognizeID(Bitmap inBitmap, String inID, RecognizeIDListener listener) {
//        getDetectMode = DETECT_MODE_INBITMAP;
        recoIDListener = listener;
        this.selectCamera("平板");
        innerOcrID = inID;
        DrawDialogFragment dialog = new DrawDialogFragment();
        DrawDialogTask task = new DrawDialogTask(mainCtx.getSupportFragmentManager(), dialog,inBitmap,this,DrawDialogFragment.MODE_OCR);//
        task.execute();
    }
    @Override
    public void postRecognizeID(Bitmap draw) {
        if (iAidlInterface != null) {
            try {
                iAidlInterface.setAdditionalBitmap(draw);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        innerRecognize(innerOcrID);
    }
    private RecognizeIDListener recoIDListener;
    private PuruiResult innerRecognize(String inID) {
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
        if(mode == 0){
            tvRec.setText(resID);
            ivDet.setImageBitmap(Utils.resizeBitmap(retBitmap,ivDet));
        }else if(mode == 1) {
            iPuruiCallback.setDetUI(retBitmap);
            iPuruiCallback.setIDRecognitionUI(resID);
        }
        PuruiResult res1 = new PuruiResult(res, resID, retBitmap);
        recoIDListener.onRecognized(res1);
        return res1;
    }
    @Override
    public PuruiResult unlockDevice() {
        boolean res = false;
        if (iAidlInterface != null) {
            try {
                res = iAidlInterface.unLockDevice();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if(mode == 0){
            cbDeviceUnlock.setChecked(res);
        }
        if(res) {
            if(mode == 0){
                cbDeviceLock.setChecked(false);
                try{
                    ivDevice.setImageBitmap(Utils.resizeBitmap(iAidlInterface.getServiceBitmap("unlock"),ivDevice));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }else if(mode == 1){
                iPuruiCallback.setLockStateUI(0);
            }
        }
        return new PuruiResult(res, res?"设备已解锁":"设备解锁失败");
    }
    @Override
    public PuruiResult lockDevice() {
        boolean res = true;
        if (iAidlInterface != null) {
            try {
                res = iAidlInterface.lockDevice();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if(mode == 0){
            cbDeviceLock.setChecked(res);
        }
        if(res) {
            if (mode == 0) {
                cbDeviceUnlock.setChecked(false);
                try {
                    ivDevice.setImageBitmap(Utils.resizeBitmap(iAidlInterface.getServiceBitmap("lock"), ivDevice));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else if (mode == 1) {
                iPuruiCallback.setLockStateUI(res ? 1 : 0);
            }
        }
        return new PuruiResult(res, res?"设备已闭锁":"设备闭锁失败");
    }

    @Override
    public PuruiResult whetherToTestElectro(char selectPhase) {
        boolean whetherToTest = true;
        boolean openYandianCam = true;
        Bitmap retBitmap = null;
        if (iAidlInterface != null) {
            try {
                ParcelDetectResult pdr = iAidlInterface.getWhetherToTestElectro(selectPhase);
                if(pdr != null){
                    whetherToTest = pdr.getWhetherToTest();
                    openYandianCam = pdr.isOpenYanCam();
                    retBitmap = pdr.getResBitmap();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if(camType == CAM_YAN && !openYandianCam){
            selectCamera("关闭");
        }
        if(retBitmap != null){
            if(mode == 0){
                ivDet.setImageBitmap(retBitmap);
            }else{
                iPuruiCallback.setDetUI(retBitmap);
            }
        }
        if (whetherToTest) {
            return new PuruiResult(true, "可以执行验电操作", retBitmap);
        } else {
            if(mode == 0){
                if(selectPhase == 'A'){
                    tvElectroA.setText("未验电");
                    tvElectroA.setTextColor(Color.BLACK);
                }else if(selectPhase == 'B'){
                    tvElectroB.setText("未验电");
                    tvElectroB.setTextColor(Color.BLACK);
                }else {
                    tvElectroC.setText("未验电");
                    tvElectroC.setTextColor(Color.BLACK);
                }
            }else if(mode == 1){
                iPuruiCallback.setEleTestUI(selectPhase-'A',2);
            }
//            Toast.makeText(mainActivity.getApplicationContext(), "不可以执行验电操作", Toast.LENGTH_SHORT).show();
            if(camType == CAM_YAN && !openYandianCam){
                selectCamera("关闭");
            }
            return new PuruiResult(false, "不可以执行验电操作");
        }
//        String detail = !openYandianCam?"验电摄像头未开启":"未满足验电条件，不可以执行验电操作";
//        if(!whetherToTest)iPuruiCallback.setEleTestUI(currentPhase-'A',2);
//        return new PuruiResult(whetherToTest,whetherToTest?"可以验电":detail,retBitmap);
    }

    @Override
    public PuruiResult testElectricity() {
        String resback = null;
        PuruiResult res;
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
            if(mode == 0){
                if(phase == 0){
                    tvElectroA.setText("未验电");
                    tvElectroA.setTextColor(Color.BLACK);
                    isAElectro = 2;
                }else if(phase ==1){
                    tvElectroB.setText("未验电");
                    tvElectroB.setTextColor(Color.BLACK);
                    isBElectro = 2;
                }else {
                    tvElectroC.setText("未验电");
                    tvElectroC.setTextColor(Color.BLACK);
                    isCElectro = 2;
                }
            }else if(mode == 1){
                iPuruiCallback.setEleTestUI(phase,2);
            }
            res = new PuruiResult(false,resback);
        }else {
            if(mode == 0){
                String eleResult;
                int eleColor;
                int ABCeleRes;
                if(resback.contains("不带电")){
                    eleResult = "无电";
                    eleColor = Color.GREEN;
                    ABCeleRes = 0;
                }else {
                    eleResult = "有电";
                    eleColor = Color.RED;
                    ABCeleRes = 1;
                }
                if(phase == 0){
                    tvElectroA.setText(eleResult);
                    tvElectroA.setTextColor(eleColor);
                    isAElectro = ABCeleRes;
                }else if(phase == 1){
                    tvElectroB.setText(eleResult);
                    tvElectroB.setTextColor(eleColor);
                    isBElectro = ABCeleRes;
                }else{
                    tvElectroC.setText(eleResult);
                    tvElectroC.setTextColor(eleColor);
                    isCElectro = ABCeleRes;
                }
            }else if(mode == 1){
                int ABCeleRes = (resback.contains("不带电"))?0:1;
                iPuruiCallback.setEleTestUI(phase,ABCeleRes);
                if(phase == 0){
                    isAElectro = ABCeleRes;
                }else if(phase == 1){
                    isBElectro = ABCeleRes;
                }else{
                    isCElectro = ABCeleRes;
                }
            }
            if(isABCtested[0]&isABCtested[1]&isABCtested[2]){
                resback += "\n验电完毕";
                if(isAElectro == 0 && isBElectro == 0 && isCElectro == 0){
                    resback += "\n结果：不带电";
                }else{
                    resback += "\n结果：带电";
                }
            }
            res = new PuruiResult(true,resback);
        }
        return res;
    }


    @Override
    public void resetAll() {
        isABCtested = new boolean[]{false,false,false};
        isAElectro = 2;
        isBElectro = 2;
        isCElectro = 2;
        if(mode == 0){
            tvRec.setText("");
            cbIsOff.setChecked(false);
            cbIsOn.setChecked(false);
            cbIsTakeOff.setChecked(false);
            cbIsTakeOn.setChecked(false);
            try{
                ivA.setImageBitmap(Utils.resizeBitmap(iAidlInterface.getServiceBitmap("result_invalid"),ivA));
                ivB.setImageBitmap(Utils.resizeBitmap(iAidlInterface.getServiceBitmap("result_invalid"),ivB));
                ivC.setImageBitmap(Utils.resizeBitmap(iAidlInterface.getServiceBitmap("result_invalid"),ivC));
                tvElectroA.setText("未验电");   tvElectroA.setTextColor(Color.BLACK);
                tvElectroB.setText("未验电");   tvElectroB.setTextColor(Color.BLACK);
                tvElectroC.setText("未验电");   tvElectroC.setTextColor(Color.BLACK);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else if(mode == 1){
            iPuruiCallback.resetUI();
        }
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