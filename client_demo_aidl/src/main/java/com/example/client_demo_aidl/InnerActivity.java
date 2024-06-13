package com.example.client_demo_aidl;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.purui.service.IPuruiService;
import com.purui.service.result.CamSelectResult;
import com.purui.service.result.ElectricalResult;
import com.purui.service.result.FaceResult;
import com.purui.service.result.LockResult;
import com.purui.service.result.OcrResult;
import com.purui.service.PuruiServiceManager;
import com.purui.service.result.StateResult;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class InnerActivity extends AppCompatActivity {
    private IPuruiService iPS;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private char selectPhase;
    private CheckBox cbIdReco, cbStateDetect;
    private ImageView ivOri,ivDet;
    private RadioGroup rgCameraType;
    private final ThreadPoolExecutor mThreadPool = new ThreadPoolExecutor(1,2,4000, TimeUnit.MILLISECONDS,new LinkedBlockingDeque<>());
    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main2);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        ImageView ivExecutor = findViewById(R.id.imageViewExecutor);            //执行人 人脸识别结果图片
        TextView tvExecutor = findViewById(R.id.textViewExecutor);              //执行人 人脸识别结果姓名
        ImageView ivSupervisor = findViewById(R.id.imageViewSupervisor);        //监护人 人脸识别结果图片
        TextView tvSupervisor = findViewById(R.id.textViewSupervisor);          //监护人 人脸识别结果姓名

        Spinner spiPhaseType = findViewById(R.id.spinnerPhaseType);             //验电相 下拉框
        Button btnIdReco = findViewById(R.id.buttonIdReco);                     //设备识别 按钮
        Button btnStateDetect = findViewById(R.id.buttonStateDetect);           //状态检测 按钮
        Button btnElectroTest = findViewById(R.id.buttonElectroTest);           //验电 按钮
        Button btnUnlock = findViewById(R.id.buttonUnlock);                     //解锁 按钮
        Button btnLock = findViewById(R.id.buttonLock);                         //闭锁 按钮
        Button btnReset = findViewById(R.id.buttonReset);                       //重置 按钮
        Button btnFace = findViewById(R.id.buttonFace);                //人脸识别 按钮
        Button btnFaceManage = findViewById(R.id.buttonFaceManage);    //人脸管理 按钮
        Button btnVideo = findViewById(R.id.buttonVideo);   //录像按钮

        cbIdReco = findViewById(R.id.checkBoxIdReco);            //是否已完成 设备识别
        cbStateDetect = findViewById(R.id.checkBoxStateDetect);  //是否已完成 状态检测
        CheckBox cbElectroTest = findViewById(R.id.checkBoxElectroTest);  //是否已完成 验电
        CheckBox cbUnlock = findViewById(R.id.checkBoxUnlock);            //是否已完成 解锁
        CheckBox cbLock = findViewById(R.id.checkBoxLock);                //是否已完成 闭锁

        ivOri = findViewById(R.id.image2);            //摄像头实时画面
        ivDet = findViewById(R.id.imageView2);        //状态检测结果图
        TextView tvRec = findViewById(R.id.textViewReco);       //识别操作设备的结果显示

        CheckBox cbIsOn = findViewById(R.id.checkBoxIsOn);    //识别结果中“已合上”CheckBox
        CheckBox cbIsOff = findViewById(R.id.checkBoxIsOff);  //识别结果中“已拉开”CheckBox
        ImageView ivA = findViewById(R.id.ivA);        //三相指示中“A”
        ImageView ivB = findViewById(R.id.ivB);        //三相指示中“B”
        ImageView ivC = findViewById(R.id.ivC);        //三相指示中“C”

        TextView tvEleA = findViewById(R.id.tvEleA);        //验电指示中“A”
        TextView tvEleB = findViewById(R.id.tvEleB);        //验电指示中“B”
        TextView tvEleC = findViewById(R.id.tvEleC);        //验电指示中“C”

        ImageView ivDevice = findViewById(R.id.imageViewDevice);            //设备图片
        CheckBox cbDeviceUnlock = findViewById(R.id.checkBoxDeviceUnlock);  //设备解锁指示
        CheckBox cbDeviceLock = findViewById(R.id.checkBoxDeviceLock);      //设备闭锁指示

        CheckBox cbIsTakeOff = findViewById(R.id.checkBoxIsTakeOff);    //识别结果中“已取下”CheckBox
        CheckBox cbIsTakeOn = findViewById(R.id.checkBoxIsTakeOn);      //识别结果中“已给上”CheckBox
        rgCameraType = findViewById(R.id.rgCameraType);      //摄像头选择 单选框组

        List<RadioButton> rbsCameraTypeList  = new ArrayList<>();
        Collections.addAll(rbsCameraTypeList,
                findViewById(R.id.rbClose),
                findViewById(R.id.rbPad),
                findViewById(R.id.rbOpe),
                findViewById(R.id.rbEle));

        iPS = new PuruiServiceManager(this);
        iPS.createService(new IPuruiService.ServiceConnectionListener() {
            @Override
            public IPuruiService.UIHandler onConnected() {
                Toast.makeText(InnerActivity.this,"服务已连接上",Toast.LENGTH_SHORT).show();
                return () -> {
                    handler.post(()->{
                        rgCameraType.check(0);
                        iPS.selectCamera("关闭",null);
                        setDefaultCamUI();
                    });
                };
            }
            @Override
            public void onDisconnected() {
                Toast.makeText(InnerActivity.this,"服务已断开",Toast.LENGTH_SHORT).show();
            }
        });

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

        spiPhaseType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectPhase = parent.getItemAtPosition(position).toString().toCharArray()[0]; 	//获取选择项的值
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        //设备识别
        btnIdReco.setOnClickListener(v ->{
            mThreadPool.execute(()->{  //"10kV前光（后光）一线 10#（3713K009）"
                OcrResult res = iPS.recognizeID(iPS.getCamFrame(), "10kV观井线96号支83号");
                handler.post(()->{
                    ivDet.setImageBitmap(res.getBitmap());
                    tvRec.setText(res.getId());
                    cbIdReco.setChecked(res.isSuccess());
                    Toast.makeText(getApplicationContext(), res.getId(), Toast.LENGTH_SHORT).show();
                });
            });

        });
        //开关状态检测
        btnStateDetect.setOnClickListener(v -> {
            mThreadPool.execute(()->{
                StateResult res = iPS.detectStates(iPS.getCamFrame(), "跌落保险", "合上");
                handler.post(()->{
                    ivDet.setImageBitmap(res.getBitmap());
                    cbStateDetect.setChecked(res.isSuccess());
                    // 回调UI
                    List<CheckBox> cbs = new ArrayList<>();
                    Collections.addAll(cbs, cbIsOff, cbIsOn, cbIsTakeOn, cbIsTakeOff);
                    for (CheckBox checkBox : cbs) {
                        checkBox.setChecked(false);
                    }
                    if(res.getState()>-1&& res.getStateC()<4) cbs.get(res.getState()).setChecked(true);

                    Bitmap a = BitmapFactory.decodeResource(getResources(),R.drawable.result_off_03);
                    Bitmap b = BitmapFactory.decodeResource(getResources(),R.drawable.result_on_03);
                    Bitmap c = BitmapFactory.decodeResource(getResources(),R.drawable.result_take_03);
                    Bitmap d = BitmapFactory.decodeResource(getResources(),R.drawable.result_invalid_03);
                    List<Bitmap> bms = new ArrayList<>();
                    Collections.addAll(bms, a,b,a,c,d);
                    ivA.setImageBitmap(bms.get(res.getStateA()));
                    ivB.setImageBitmap(bms.get(res.getStateB()));
                    ivC.setImageBitmap(bms.get(res.getStateC()));
                    Toast.makeText(getApplicationContext(), res.getDetails(), Toast.LENGTH_SHORT).show();
                });
            });

        });

        // 验电的代码有点乱，忘了当时怎么写的，可以参考forYoute分支
        btnElectroTest.setOnClickListener(view -> {
            mThreadPool.execute(()->{
                //验电 状态
                iPS.testElectricity(selectPhase, new IPuruiService.TestEleCallback() {

                    @Override
                    public void onSuccess(ElectricalResult res) {
                        String[] texts = {"无电","有电","未验电"};
                        int[] colors = {Color.GREEN,Color.RED,Color.BLACK};
                        List<TextView> tvs = new ArrayList<>();
                        Collections.addAll(tvs,tvEleA,tvEleB,tvEleC);
                        int phase = res.getCurrentPhase().equals("A")?0:res.getCurrentPhase().equals("B")?1:2;
                        handler.post(()->{
                            mProgressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), res.getDetails(), Toast.LENGTH_SHORT).show();
                            tvs.get(phase).setText(res.getCurrentPhaseResult());
                            ivDet.setImageBitmap(res.getLogImg());
                            tvs.get(phase).setTextColor(colors[res.getCurrentPhaseResult().equals("无电")?0:1]);
                        });
                    }

                    @Override
                    public void onFail(ElectricalResult res) {
                        handler.post(()->{
                            mProgressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), res.getDetails(), Toast.LENGTH_SHORT).show();

                        });
                    }
                });

            });
            mProgressDialog.setTitle(null);
            mProgressDialog.setMessage("请稍候");
            mProgressDialog.show();
        });

        btnLock.setOnClickListener(view -> {
            mThreadPool.execute(()->{
                // 闭锁操作
                LockResult res = iPS.lockDevice();
                handler.post(()->{
                    mProgressDialog.dismiss();
                    cbLock.setChecked(res.isSuccess());
                    Toast.makeText(getApplicationContext(), res.getDetails(), Toast.LENGTH_SHORT).show();
                    ivDevice.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.lock));
                    cbDeviceLock.setChecked(res.isSuccess());
                    cbDeviceUnlock.setChecked(!res.isSuccess());
                });
            });
            mProgressDialog.setTitle(null);
            mProgressDialog.setMessage("请稍候");
            mProgressDialog.show();
        });

        btnUnlock.setOnClickListener(view -> {
            mThreadPool.execute(()->{
                // 解锁操作
                LockResult res = iPS.unlockDevice();
                handler.post(()->{
                    cbUnlock.setChecked(res.isSuccess());
                    Toast.makeText(getApplicationContext(), res.getDetails(), Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();
                    ivDevice.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.unlock));
                    cbDeviceUnlock.setChecked(res.isSuccess());
                    cbDeviceLock.setChecked(!res.isSuccess());
                });
            });
            mProgressDialog.setMessage("请稍候");
            mProgressDialog.show();
        });

        btnReset.setOnClickListener(v -> {
            // 重置
            iPS.resetAll();
            cbIdReco.setChecked(false);
            cbStateDetect.setChecked(false);
            cbElectroTest.setChecked(false);
            cbLock.setChecked(false);
            cbUnlock.setChecked(false);
            tvRec.setText("");
            cbIsOff.setChecked(false);
            cbIsOn.setChecked(false);
            cbIsTakeOff.setChecked(false);
            cbIsTakeOn.setChecked(false);
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.result_invalid_03);
            ivA.setImageBitmap(bmp);
            ivB.setImageBitmap(bmp);
            ivC.setImageBitmap(bmp);
            tvEleA.setText("未验电");
            tvEleB.setText("未验电");
            tvEleC.setText("未验电");
            tvEleA.setTextColor(Color.BLACK);
            tvEleB.setTextColor(Color.BLACK);
            tvEleC.setTextColor(Color.BLACK);
        });

        btnFace.setOnClickListener(v ->{
            // 人脸识别接口测试
            Bitmap demo = null;
            try {
                demo = BitmapFactory.decodeStream(getAssets().open("test2.jpg"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            FaceResult res = iPS.getFaceResult(demo);  //识别
            if(res.isSuccess()){
                String name = res.getName();
                Bitmap face = res.getFace();
                ivSupervisor.setImageBitmap(face);
                tvSupervisor.setText("监护人: "+name);
            }
            Toast.makeText(this.getApplicationContext(), res.getName(), Toast.LENGTH_SHORT).show();
        });

        btnFaceManage.setOnClickListener(v -> {
            // 人脸管理接口测试
            Bitmap demo = null;
            try {
                demo = BitmapFactory.decodeStream(getAssets().open("addFaceDemo.jpg"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            FaceResult[] res = iPS.checkFaces();   //查看
            FaceResult res1 = iPS.deleteFace("李鑫"); //删除
            Toast.makeText(this.getApplicationContext(), res1.getName(), Toast.LENGTH_SHORT).show();
            res = iPS.checkFaces(); //查看
            FaceResult res2 = iPS.addFace("李鑫", demo); //增加
            Toast.makeText(this.getApplicationContext(), res2.getName(), Toast.LENGTH_SHORT).show();
            res = iPS.checkFaces();  //查看

        });

        //扫描摄像头 按钮
        findViewById(R.id.buttonScan).setOnClickListener(v-> {
            mThreadPool.execute(() -> {
                String s = iPS.scanCameras();
                handler.post(() -> {
                    mProgressDialog.dismiss();
                    new AlertDialog.Builder(InnerActivity.this)
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

        btnVideo.setOnClickListener(v -> {
            Dialog mDialog = new Dialog(this);
            mDialog.setCancelable(false);
            mDialog.setContentView(R.layout.record_dialog);
            // 获取Window对象，并设置其属性
            Window dialogWindow = mDialog.getWindow();
            if (dialogWindow != null) {
                dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
            // 获取ImageView和Button
            ImageView imageView = mDialog.findViewById(R.id.imageView);
            Button closeButton = mDialog.findViewById(R.id.closeButton);
            // 设置按钮点击事件
            closeButton.setOnClickListener(view -> {
                mDialog.dismiss();
                iPS.recordVideo(new IPuruiService.RecordListener() {
                    @Override
                    public void onStartRecord(String state) {
                    }
                    @Override
                    public void onRecording(Bitmap bitmap) {
                    }
                    @Override
                    public void onEndRecord(String state) {
                        btnVideo.setText("录像");
                        Toast.makeText(InnerActivity.this,state,Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFail(String reason) {
                        Toast.makeText(InnerActivity.this,reason,Toast.LENGTH_SHORT).show();

                    }
                });
            });

            // 显示Dialog
            mDialog.show();
            iPS.recordVideo(new IPuruiService.RecordListener() {
                @Override
                public void onStartRecord(String state) {
                    Toast.makeText(InnerActivity.this,state,Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRecording(Bitmap bitmap) {
                    btnVideo.setText("停止");
                    imageView.setImageBitmap(bitmap);
                }

                @Override
                public void onEndRecord(String state) {
                }

                @Override
                public void onFail(String reason) {
                    Toast.makeText(InnerActivity.this,reason,Toast.LENGTH_SHORT).show();
                    mDialog.dismiss();
                }
            });
        });
        ivOri.setOnClickListener(v -> {
            if(rgCameraType.getCheckedRadioButtonId() == R.id.rbPad){
                if(rgCameraType.getCheckedRadioButtonId() == R.id.rbPad){
                    dispatchTakePictureIntent();
                }
            }
        });
        setDefaultCamUI();
    }
    private void setDefaultCamUI() {
        Bitmap bm = BitmapFactory.decodeResource(getResources(),R.drawable.camera_off);
        ivOri.setImageBitmap(bm);
        ivDet.setImageBitmap(bm);
    }
    @Override
    protected void onDestroy() {
        //结束服务
        iPS.destroyService();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        iPS.selectCamera("关闭",null);
//        rgCameraType.check(0);
//        setDefaultCamUI();
    }
    @Override
    protected void onResume() {
        super.onResume();
//        iPS.resumeService();
    }
    // 处理系统相机返回结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            iPS.selectCamera("平板", new IPuruiService.CameraShowListener() {
                @Override
                public void onCamShow(Bitmap bitmap) {
                    ivOri.setImageBitmap(bitmap);
                }
                @Override
                public void onCamShow(byte[] bytes) { }
            });
            Bitmap imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            OcrResult res = iPS.recognizeID(imageBitmap, "10kV前光（后光）一线 10#（3713K009）");
            handler.post(()->{
                ivDet.setImageBitmap(res.getBitmap());
//                tvRec.setText(res.getId());
                cbIdReco.setChecked(res.isSuccess());
                Toast.makeText(getApplicationContext(), res.getId(), Toast.LENGTH_SHORT).show();
            });

//            iPS.detectStates(
//                imageBitmap, "跌落保险", "合上",
//                res -> {
//                    Bitmap bitmap = res.getBitmap();
//                    cbStateDetect.setChecked(res.isDone());
//                    Toast.makeText(getApplicationContext(), res.getDetails(), Toast.LENGTH_SHORT).show();
//                }
//            );
        }
    }

    // 以下为处理调用系统相机部分，可忽略
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String mCurrentPhotoPath;
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // 处理异常
        }
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this, "com.example.client_demo_aidl.fileprovider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg",storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
}