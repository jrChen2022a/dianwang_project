package com.example.client_demo_aidl;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.TextureView;
import android.view.View;
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

import com.purui.service.IPuruiCallback;
import com.purui.service.IPuruiService;
import com.purui.service.PuruiResult;
import com.purui.service.PuruiServiceManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DemoActivity extends AppCompatActivity {
    private IPuruiService PSM;
    private char selectPhase;
    private  CheckBox cbIdReco, cbStateDetect, cbElectroTest, cbUnlock, cbLock;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_demo);

        cbIdReco = findViewById(R.id.checkBoxIdReco);            //是否已完成 设备识别
        cbStateDetect = findViewById(R.id.checkBoxStateDetect);  //是否已完成 状态检测
        cbElectroTest = findViewById(R.id.checkBoxElectroTest);  //是否已完成 验电
        cbUnlock = findViewById(R.id.checkBoxUnlock);            //是否已完成 解锁
        cbLock = findViewById(R.id.checkBoxLock);                //是否已完成 闭锁

        ImageView ivExecutor = findViewById(R.id.imageViewExecutor);      //执行人 人脸识别结果图片
        TextView tvExecutor = findViewById(R.id.textViewExecutor);        //执行人 人脸识别结果姓名
        ImageView ivSupervisor = findViewById(R.id.imageViewSupervisor);  //监护人 人脸识别结果图片
        TextView tvSupervisor = findViewById(R.id.textViewSupervisor);    //监护人 人脸识别结果姓名

        Spinner spiPhaseType = findViewById(R.id.spinnerPhaseType);       //验电相 下拉框
        Button btnScan = findViewById(R.id.buttonScan);                   //扫描 按钮
        Button btnIdReco = findViewById(R.id.buttonIdReco);               //设备识别 按钮
        Button btnStateDetect = findViewById(R.id.buttonStateDetect);     //状态检测 按钮
        Button btnElectroTest = findViewById(R.id.buttonElectroTest);     //验电 按钮
        Button btnUnlock = findViewById(R.id.buttonUnlock);               //解锁 按钮
        Button btnLock = findViewById(R.id.buttonLock);                   //闭锁 按钮
        Button btnReset = findViewById(R.id.buttonReset);                 //重置 按钮
        Button btnFace = findViewById(R.id.buttonFace);                   //人脸识别 按钮
        Button btnFaceManage = findViewById(R.id.buttonFaceManage);       //人脸管理 按钮

        ImageView ivOri = findViewById(R.id.image2);            //摄像头实时画面
        ImageView ivDet = findViewById(R.id.imageView2);        //检测结果图
        TextureView textureView = findViewById(R.id.tuv_cam);   //本地摄像头实时画面
        TextView tvRec = findViewById(R.id.textViewReco);       //识别设备ID的结果显示

        CheckBox cbIsOn = findViewById(R.id.checkBoxIsOn);              //识别结果中“已合上”CheckBox
        CheckBox cbIsOff = findViewById(R.id.checkBoxIsOff);            //识别结果中“已拉开”CheckBox
        CheckBox cbIsTakeOff = findViewById(R.id.checkBoxIsTakeOff);    //识别结果中“已取下”CheckBox
        CheckBox cbIsTakeOn = findViewById(R.id.checkBoxIsTakeOn);      //识别结果中“已给上”CheckBox
        RadioGroup rgCameraType = findViewById(R.id.rgCameraType);      //摄像头选择

        ImageView ivA = findViewById(R.id.ivA);        //三相指示“A”
        ImageView ivB = findViewById(R.id.ivB);        //三相指示“B”
        ImageView ivC = findViewById(R.id.ivC);        //三相指示“C”

        TextView tvEleA = findViewById(R.id.tvEleA);        //验电指示“A”
        TextView tvEleB = findViewById(R.id.tvEleB);        //验电指示“B”
        TextView tvEleC = findViewById(R.id.tvEleC);        //验电指示“C”

        ImageView ivDevice = findViewById(R.id.imageViewDevice);            //设备图片
        CheckBox cbDeviceUnlock = findViewById(R.id.checkBoxDeviceUnlock);  //设备解锁指示
        CheckBox cbDeviceLock = findViewById(R.id.checkBoxDeviceLock);      //设备闭锁指示

        // 摄像头选项
        List<RadioButton> rbsCameraTypeList  = new ArrayList<>();
        Collections.addAll(rbsCameraTypeList,
                findViewById(R.id.rbClose), // 关闭
                findViewById(R.id.rbPad),   // 平板
                findViewById(R.id.rbOpe),   // 操作杆
                findViewById(R.id.rbEle));  // 验电器


        // 按控件顺序注册ServiceManager
        PSM = new PuruiServiceManager(this, ivOri, ivDet, textureView,
                        tvRec, cbIsOn, cbIsOff,
                        ivA, ivB, ivC,
                        tvEleA, tvEleB,tvEleC,
                        ivDevice,cbDeviceUnlock,cbDeviceLock,
                        rgCameraType, cbIsTakeOff, cbIsTakeOn);
        PSM.createService();

        // 摄像头选择
        rgCameraType.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton checkedRadioButton = group.findViewById(checkedId);
            if (checkedRadioButton != null) {
                PSM.selectCamera(checkedRadioButton.getText().toString());
            }
        });
        // 验电相选择
        spiPhaseType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectPhase = parent.getItemAtPosition(position).toString().toCharArray()[0]; 	//获取选择项的值
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        //扫描摄像头
        btnScan.setOnClickListener(v-> PSM.scanCameras());

        // 调用系统相机进行变焦拍照
        ivOri.setOnClickListener(v -> {
            if(rgCameraType.getCheckedRadioButtonId() == R.id.rbPad){
                dispatchTakePictureIntent();
            }
        });

        //设备识别
        btnIdReco.setOnClickListener(v -> PSM.preRecognizeID(
                "10kV前光（后光）一线 10#（3713K009）",
                res -> {
                    cbIdReco.setChecked(res.isDone());
                    Bitmap bitmap = res.getBitmap();
                    Toast.makeText(getApplicationContext(), res.getDetails(), Toast.LENGTH_SHORT).show();
                }
        ));
        //开关状态检测
        btnStateDetect.setOnClickListener(v -> PSM.preDetectStates(
                "跌落保险", "给上",
                res -> {
                    Bitmap bitmap = res.getBitmap();
                    cbStateDetect.setChecked(res.isDone());
                    Toast.makeText(getApplicationContext(), res.getDetails(), Toast.LENGTH_SHORT).show();
                }
        ));

        // 测试验电接口 没改过
        btnElectroTest.setOnClickListener(view -> {
            PuruiResult res = PSM.whetherToTestElectro(selectPhase);
            Toast.makeText(getApplicationContext(), res.getDetails(), Toast.LENGTH_SHORT).show();
            if(res.isDone()){
                Bitmap bitmap = res.getBitmap();
                PuruiResult res1 = PSM.testElectricity();
                cbElectroTest.setChecked(res1.isDone());
                Toast.makeText(getApplicationContext(), res1.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });
        // 测试闭锁接口 没改过
        btnLock.setOnClickListener(view -> {
            PuruiResult res = PSM.lockDevice();
            cbLock.setChecked(res.isDone());
            Toast.makeText(getApplicationContext(), res.getDetails(), Toast.LENGTH_SHORT).show();
        });
        // 测试解锁接口 没改过
        btnUnlock.setOnClickListener(view -> {
            PuruiResult res = PSM.unlockDevice();
            cbUnlock.setChecked(res.isDone());
            Toast.makeText(getApplicationContext(), res.getDetails(), Toast.LENGTH_SHORT).show();
        });
        // 测试重置接口 没改过
        btnReset.setOnClickListener(v -> {
            PSM.resetAll();
            cbIdReco.setChecked(false);
            cbStateDetect.setChecked(false);
            cbElectroTest.setChecked(false);
            cbLock.setChecked(false);
            cbUnlock.setChecked(false);
        });
        // 测试人脸识别接口 没改过
        btnFace.setOnClickListener(v ->{
            Bitmap demo = null;
            try {
                demo = BitmapFactory.decodeStream(getAssets().open("test3.jpg"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            PuruiResult res = PSM.getFaceResult(demo);  //识别
            if(res.isDone()){
                String name = res.getDetails();
                Bitmap face = res.getBitmap();
                ivSupervisor.setImageBitmap(face);
                String text = "监护人: "+name;
                tvSupervisor.setText(text);
            }
            Toast.makeText(this.getApplicationContext(), res.getDetails(), Toast.LENGTH_SHORT).show();
        });
        // 测试人脸查看、删除、增加接口 没改过
        btnFaceManage.setOnClickListener(v -> {
            Bitmap demo = null;
            try {
                demo = BitmapFactory.decodeStream(getAssets().open("addFaceDemo.jpg"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            PuruiResult[] res = PSM.checkFaces();   //查看
            PuruiResult res1 = PSM.deleteFace("李鑫"); //删除
            Toast.makeText(this.getApplicationContext(), res1.getDetails(), Toast.LENGTH_SHORT).show();
            res = PSM.checkFaces(); //查看
            PuruiResult res2 = PSM.addFace("李鑫", demo); //增加
            Toast.makeText(this.getApplicationContext(), res2.getDetails(), Toast.LENGTH_SHORT).show();
            res = PSM.checkFaces();  //查看
        });
    }
    @Override
    public void onResume() {
        PSM.resumeService();
        super.onResume();
    }
    @Override
    public void onStop() {
        PSM.stopService();
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        PSM.destroyService();
        super.onDestroy();
    }

    // 处理系统相机返回结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            PSM.preRecognizeID(
                    imageBitmap, "10kV前光（后光）一线 10#（3713K009）",
                    res -> {
                        cbIdReco.setChecked(res.isDone());
                        Bitmap bitmap = res.getBitmap();
                        Toast.makeText(getApplicationContext(), res.getDetails(), Toast.LENGTH_SHORT).show();
                    }
            );
//            PSM.preDetectStates(
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