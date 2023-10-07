package com.example.client_demo_aidl;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.gson.Gson;
import com.purui.service.IPuruiService;
import com.purui.service.PuruiServiceManager;
import com.purui.service.result.CamSelectResult;
import com.purui.service.result.ElectricalResult;
import com.purui.service.result.ElectricalResultJSON;
import com.purui.service.result.LockResult;
import com.purui.service.result.OcrResult;
import com.purui.service.result.OcrResultJSON;
import com.purui.service.result.StateResult;
import com.purui.service.result.StateResultJSON;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipFile;

public class DemoActivity2 extends AppCompatActivity {
    private IPuruiService iPS;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private char selectPhase;
    private ImageView ivOri;
    private TextView tvKey,tvCam;
    private final Gson gson = new Gson();
    private final ThreadPoolExecutor mThreadPool = new ThreadPoolExecutor(1,2,4000, TimeUnit.MILLISECONDS,new LinkedBlockingDeque<>());
    private ProgressDialog mProgressDialog;
    private Button btnPho = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_demo2);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        ivOri = findViewById(R.id.image2);            //摄像头实时画面
        btnPho = findViewById(R.id.buttonPho);
        RadioGroup rgCameraType = findViewById(R.id.rgCameraType);      //摄像头选择 单选框组
        tvKey = findViewById(R.id.tvKeyInfo);
        tvCam = findViewById(R.id.tvCam);
        List<RadioButton> rbsCameraTypeList  = new ArrayList<>();
        Collections.addAll(rbsCameraTypeList,
                findViewById(R.id.rbClose),
                findViewById(R.id.rbPad),
                findViewById(R.id.rbOpe),
                findViewById(R.id.rbEle));

//        if(iPS == null){
        iPS = new PuruiServiceManager(this);
        iPS.createService(new IPuruiService.ServiceConnectionListener() {
            @Override
            public void onConnected() {
//                Toast.makeText(getApplicationContext(),"服务已连接上",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onDisconnected() {
//                Toast.makeText(getApplicationContext(),"服务已断开",Toast.LENGTH_SHORT).show();
            }
        });
//        }


        rgCameraType.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton checkedRadioButton = group.findViewById(checkedId);
            if (checkedRadioButton != null) {
                CamSelectResult res = iPS.selectCamera(checkedRadioButton.getText().toString(), new IPuruiService.CameraShowListener() {
                    @Override
                    public void onCamShow(Bitmap bitmap) {
                        ivOri.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onCamShow(byte[] bitmapbytes) {
                        Log.v("activity", String.valueOf(bitmapbytes.length));
                    }
                });
                if(res.getSelectCamId()==0 || !res.isSuccess()){
                    rbsCameraTypeList.get(0).setChecked(true);
                    setDefaultCamUI();
                }
            }
        });


        //设备识别
        btnPho.setOnClickListener(v ->{
            mThreadPool.execute(()->{
                switch (invokeType){
                    case 1:
                        OcrResult res1 = iPS.recognizeID(iPS.getCamFrame(), targetId);
                        if(res1.getBitmap()==null){
                            handler.post(()-> Toast.makeText(getApplicationContext(),"请打开摄像头",Toast.LENGTH_SHORT).show());
                        }else{
                            handler.post(()->{
                                goBackApp(gson.toJson(new OcrResultJSON(res1)));
                                targetId = "";
                            });
                        }
                        break;
                    case 2:
                        StateResult res = iPS.detectStates(iPS.getCamFrame(), targetType, targetState);
                        if(res.getBitmap()==null){
                            handler.post(()-> Toast.makeText(getApplicationContext(),"请打开摄像头",Toast.LENGTH_SHORT).show());
                        }else{
                            handler.post(()->{
                                goBackApp(gson.toJson(new StateResultJSON(res)));
                                targetType = "";
                                targetState = "";
//                            Toast.makeText(getApplicationContext(), res.getId(), Toast.LENGTH_SHORT).show();
                            });
                        }
                        break;
                    case 3:
                        LockResult lr = iPS.unlockDevice();
                        if(lr.isSuccess()){
                            goBackApp(gson.toJson(lr));
                        }else{
                            handler.post(()->Toast.makeText(getApplicationContext(),"连接操作杆失败，请先扫描操作杆",Toast.LENGTH_SHORT).show());
                        }
                        break;
                    case 4:
                        LockResult lr1 = iPS.lockDevice();
                        if(lr1.isSuccess()){
                            goBackApp(gson.toJson(lr1));
                        }else{
                            handler.post(()->Toast.makeText(getApplicationContext(),"连接操作杆失败，请先扫描操作杆",Toast.LENGTH_SHORT).show());
                        }
                        break;
                    case 5:
                        //验电 状态
                        iPS.testElectricity(selectPhase, new IPuruiService.TestEleCallback() {
                            @Override
                            public void onSuccess(ElectricalResult res) {
                                handler.post(()-> goBackApp(gson.toJson(new ElectricalResultJSON(res))));
                            }

                            @Override
                            public void onFail(ElectricalResult res) {
                                handler.post(()-> Toast.makeText(getApplicationContext(),"请打开验电器",Toast.LENGTH_SHORT).show());
                            }
                        });
                        break;
                    default:
                        break;
                }
                handler.post(()->mProgressDialog.dismiss());
//                iPS.selectCamera("关闭",null);
//                invokeType = -1;
            });
            mProgressDialog.setTitle(null);
            mProgressDialog.setMessage("正在执行...");
            mProgressDialog.show();
        });

//        btnFace.setOnClickListener(v ->{
//            // 人脸识别接口测试
//            Bitmap demo = null;
//            try {
//                demo = BitmapFactory.decodeStream(getAssets().open("test2.jpg"));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            FaceResult res = iPS.getFaceResult(demo);  //识别
//            if(res.isSuccess()){
//                String name = res.getName();
//                Bitmap face = res.getFace();
//                ivSupervisor.setImageBitmap(face);
//                tvSupervisor.setText("监护人: "+name);
//            }
//            Toast.makeText(this.getApplicationContext(), res.getName(), Toast.LENGTH_SHORT).show();
//        });
//
//        btnFaceManage.setOnClickListener(v -> {
//            // 人脸管理接口测试
//            Bitmap demo = null;
//            try {
//                demo = BitmapFactory.decodeStream(getAssets().open("addFaceDemo.jpg"));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            FaceResult[] res = iPS.checkFaces();   //查看
//            FaceResult res1 = iPS.deleteFace("李鑫"); //删除
//            Toast.makeText(this.getApplicationContext(), res1.getName(), Toast.LENGTH_SHORT).show();
//            res = iPS.checkFaces(); //查看
//            FaceResult res2 = iPS.addFace("李鑫", demo); //增加
//            Toast.makeText(this.getApplicationContext(), res2.getName(), Toast.LENGTH_SHORT).show();
//            res = iPS.checkFaces();  //查看
//
//        });

        //扫描摄像头 按钮
        findViewById(R.id.buttonScan).setOnClickListener(v-> {
            mThreadPool.execute(() -> {
                String s = iPS.scanCameras();
                handler.post(() -> {
                    mProgressDialog.dismiss();
                    new AlertDialog.Builder(DemoActivity2.this)
                            .setTitle("扫描结果")//设置对话框的标题
                            .setMessage(s)//设置对话框的内容
                            .setPositiveButton("确定", (dialog1, which) -> dialog1.dismiss())//设置对话框的按钮
                            .create().show();
                });
            });
            mProgressDialog.setTitle("正在扫描摄像头...");
            mProgressDialog.setMessage("请确保摄像头已连接上热点，并尽量靠近当前设备");
            mProgressDialog.show();
        });

        setDefaultCamUI();
        // 解析i国网透传的参数信息
        parseScheme(getIntent());
    }
    String seq=null;
    int invokeType=-1;
    String targetId = "";
    String targetType = "";
    String targetState = "";

    private  void parseScheme(Intent intent) {
        Uri uri = intent.getData();
        if(uri!=null) {
            seq = uri.getQueryParameter("seq");
            String param = uri.getQueryParameter("param");
            try {
                byte[] decodeData = Base64.decode(param.getBytes(), Base64.DEFAULT);
                JSONObject jsonObject = new JSONObject(new String(decodeData));
                invokeType = Integer.parseInt(jsonObject.getString("type"));

                if(jsonObject.getBoolean("reset"))iPS.resetAll();

                switch (invokeType){
                    case 1: // recognizeID
                        btnPho.setText("拍照");
                        targetId = jsonObject.getString("targetId");
                        tvKey.setText("请检查是否到达：\n"+targetId);
                        break;
                    case 2://detectState
                        btnPho.setText("拍照");
                        targetType = jsonObject.getString("targetType");
                        targetState = jsonObject.getString("targetState");
                        tvKey.setText("请检查"+targetType+"的状态是否为"+targetState);
                        break;
                    case 3: //unlock
                        btnPho.setText("解锁");
                        tvKey.setText("请执行操作杆解锁");
                        break;
                    case 4: //lock
                        btnPho.setText("闭锁");
                        tvKey.setText("请执行操作杆闭锁");
                        break;
                    case 5://detectState
                        btnPho.setText("验电");
                        selectPhase = jsonObject.getString("currentPhase").charAt(0);
                        tvKey.setText("请验明"+selectPhase+"相是否带电");
                        break;
                    default:
                        break;
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private  void  goBackApp(String data) {
        String errCode = "0"; // 错误码，0 表示成功，其他表示失败
        String encodedData = Base64.encodeToString(data.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
        Uri uri = Uri.parse("wxworklocal://jsapi/requst3rdapp_result?errcode=0&seq=" + seq + "&data=" + encodedData);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
    private void setDefaultCamUI() {
        Bitmap bm = BitmapFactory.decodeResource(getResources(),R.drawable.camera_off);
        ivOri.setImageBitmap(bm);
    }
    @Override
    protected void onDestroy() {
        //结束服务
        iPS.destroyService();
        super.onDestroy();
    }
}