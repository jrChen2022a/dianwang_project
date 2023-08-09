//package com.example.client_demo_aidl;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.content.FileProvider;
//
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.icu.text.SimpleDateFormat;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Environment;
//import android.provider.MediaStore;
//import android.util.Log;
//import android.view.TextureView;
//import android.view.View;
//import android.view.Window;
//import android.widget.AdapterView;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.ImageView;
//import android.widget.RadioButton;
//import android.widget.RadioGroup;
//import android.widget.Spinner;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.purui.service.IInnerService;
//import com.purui.service.result.PuruiResult;
//import com.purui.service.PuruiServiceManager;
////import com.purui.service.facemodule.CustomDialog;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.Date;
//import java.util.Random;
//
//public class MainActivity extends AppCompatActivity {
//    private static final String TAG = "CLIENT";
//    private IInnerService PSM;
//    private char selectPhase;
//    private ImageView ivExecutor;
//    private TextView tvExecutor;
//    private ImageView ivSupervisor;
//    private TextView tvSupervisor;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//
//        super.onCreate(savedInstanceState);
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
//        setContentView(R.layout.activity_main2);
//
//        ivExecutor = findViewById(R.id.imageViewExecutor);          //执行人 人脸识别结果图片
//        tvExecutor = findViewById(R.id.textViewExecutor);           //执行人 人脸识别结果姓名
//        ivSupervisor = findViewById(R.id.imageViewSupervisor);      //监护人 人脸识别结果图片
//        tvSupervisor = findViewById(R.id.textViewSupervisor);       //监护人 人脸识别结果姓名
//
//        Spinner spinnerPhaseType = findViewById(R.id.spinnerPhaseType);         //验电相 下拉框
//        Button buttIdReco = findViewById(R.id.buttonIdReco);                    //设备识别 按钮
//        Button buttStateDetect = findViewById(R.id.buttonStateDetect);          //状态检测 按钮
//        Button buttElectroTest = findViewById(R.id.buttonElectroTest);          //验电 按钮
//        Button buttUnlock = findViewById(R.id.buttonUnlock);                    //解锁 按钮
//        Button buttLock = findViewById(R.id.buttonLock);                        //闭锁 按钮
//        Button buttReset = findViewById(R.id.buttonReset);                      //重置 按钮
//        Button buttFace = findViewById(R.id.buttonFace);                //人脸识别 按钮
//        Button buttFaceManage = findViewById(R.id.buttonFaceManage);    //人脸管理 按钮
//        Button buttVideo = findViewById(R.id.buttonVideo);   //录像按钮
//
//        CheckBox checkBoxIdReco = findViewById(R.id.checkBoxIdReco);            //是否已完成 设备识别
//        CheckBox checkBoxStateDetect = findViewById(R.id.checkBoxStateDetect);  //是否已完成 状态检测
//        CheckBox checkBoxElectroTest = findViewById(R.id.checkBoxElectroTest);  //是否已完成 验电
//        CheckBox checkBoxUnlock = findViewById(R.id.checkBoxUnlock);            //是否已完成 解锁
//        CheckBox checkBoxLock = findViewById(R.id.checkBoxLock);                //是否已完成 闭锁
//
//        // 需要注册进ServiceManager的组件
//        ImageView imageShowOri = findViewById(R.id.image2);         //摄像头实时画面
//        ImageView imageShowDet = findViewById(R.id.imageView2);     //状态检测结果图
//        TextureView textureView = findViewById(R.id.tuv_cam);   //本地摄像头实时画面
//        TextView textViewRec = findViewById(R.id.textViewReco);     //识别操作设备的结果显示
//
//        CheckBox checkBoxIsOn = findViewById(R.id.checkBoxIsOn);    //识别结果中“已合上”CheckBox
//        CheckBox checkBoxIsOff = findViewById(R.id.checkBoxIsOff);  //识别结果中“已拉开”CheckBox
//        ImageView ivA = findViewById(R.id.ivA);        //三相指示中“A”
//        ImageView ivB = findViewById(R.id.ivB);        //三相指示中“B”
//        ImageView ivC = findViewById(R.id.ivC);        //三相指示中“C”
//
//        TextView tvEleA = findViewById(R.id.tvEleA);        //验电指示中“A”
//        TextView tvEleB = findViewById(R.id.tvEleB);        //验电指示中“B”
//        TextView tvEleC = findViewById(R.id.tvEleC);        //验电指示中“C”
//
//        ImageView ivDevice = findViewById(R.id.imageViewDevice);            //设备图片
//        CheckBox cbDeviceUnlock = findViewById(R.id.checkBoxDeviceUnlock);  //设备解锁指示
//        CheckBox cbDeviceLock = findViewById(R.id.checkBoxDeviceLock);      //设备闭锁指示
//
//        CheckBox cbIsTakeOff = findViewById(R.id.checkBoxIsTakeOff);    //识别结果中“已取下”CheckBox
//        CheckBox cbIsTakeOn = findViewById(R.id.checkBoxIsTakeOn);      //识别结果中“已给上”CheckBox
//        RadioGroup rgCameraType = findViewById(R.id.rgCameraType);          //摄像头选择 单选框组
//
//        // 按控件顺序注册ServiceManager
//        PSM= new PuruiServiceManager(this,imageShowOri,imageShowDet,textureView,
//                textViewRec, checkBoxIsOn,checkBoxIsOff, ivA, ivB, ivC,
//                tvEleA, tvEleB, tvEleC,
//                ivDevice, cbDeviceUnlock, cbDeviceLock,
//                rgCameraType, cbIsTakeOff, cbIsTakeOn);
//        //启动服务
//        PSM.createService();
//        rgCameraType.setOnCheckedChangeListener((group, checkedId) -> {
//            RadioButton checkedRadioButton = group.findViewById(checkedId);
//            if (checkedRadioButton != null) {
//                PSM.selectCamera(checkedRadioButton.getText().toString());
//            }
//        });
//
//        spinnerPhaseType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                selectPhase = parent.getItemAtPosition(position).toString().toCharArray()[0]; 	//获取选择项的值
//            }
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {}
//        });
//
//        buttIdReco.setOnClickListener(view -> {
//            //设备识别
//            PuruiResult res = PSM.recognizeID("10kV前光一线 10#（3713K009）"); // 输入需验证的设备ID
//            Bitmap bitmap = res.getBitmap();
//            checkBoxIdReco.setChecked(res.isDone());
//            Toast.makeText(getApplicationContext(), res.getDetails(), Toast.LENGTH_SHORT).show();
//        });
//
//        String[] targetStates = new String[]{"合上","拉开","取下","给上"};
//        Random a = new Random();
//        buttStateDetect.setOnClickListener(view -> {
//            //开关状态检测
//            //参数一：开关类型，分别为：跌落保险、刀闸（隔离开关）、开关（断路器）
//            //参数二：开关状态，分别为：合上、拉开、取下、给上
//            PuruiResult res = PSM.detectStates("跌落保险",targetStates[a.nextInt(4)]);
//            Bitmap bitmap = res.getBitmap();
//            checkBoxStateDetect.setChecked(res.isDone());
//            Toast.makeText(getApplicationContext(), res.getDetails(), Toast.LENGTH_SHORT).show();
//        });
//
//        buttElectroTest.setOnClickListener(view -> {
//            //验电 状态
//            PuruiResult res = PSM.whetherToTestElectro(selectPhase);
//            Toast.makeText(getApplicationContext(), res.getDetails(), Toast.LENGTH_SHORT).show();
//            if(res.isDone()){
//                Bitmap bitmap = res.getBitmap();
//                PuruiResult res1 = PSM.testElectricity();
//                checkBoxElectroTest.setChecked(res1.isDone());
//                Log.e(TAG,res1.getDetails());
//                Toast.makeText(getApplicationContext(), res1.getDetails(), Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        buttLock.setOnClickListener(view -> {
//            // 闭锁操作
//            PuruiResult res = PSM.lockDevice();
//            checkBoxLock.setChecked(res.isDone());
//            Toast.makeText(getApplicationContext(), res.getDetails(), Toast.LENGTH_SHORT).show();
//        });
//
//        buttUnlock.setOnClickListener(view -> {
//            // 解锁操作
//            PuruiResult res = PSM.unlockDevice();
//            checkBoxUnlock.setChecked(res.isDone());
//            Toast.makeText(getApplicationContext(), res.getDetails(), Toast.LENGTH_SHORT).show();
//        });
//
//
//        buttReset.setOnClickListener(v -> {
//            // 重置
////            PSM.selectCamera("平板");
//            PSM.resetAll();
//            checkBoxIdReco.setChecked(false);
//            checkBoxStateDetect.setChecked(false);
//            checkBoxElectroTest.setChecked(false);
//            checkBoxLock.setChecked(false);
//            checkBoxUnlock.setChecked(false);
//        });
//
//        buttFace.setOnClickListener(v ->{
//            // 人脸识别
//            PSM.selectCamera("关闭");
////            //人脸接口测试部分,接口可用
//            Bitmap demo = null;
//            try {
//                demo = BitmapFactory.decodeStream(getAssets().open("test2.jpg"));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            PuruiResult res = PSM.getFaceResult(demo);  //识别
//            if(res.isDone()){
//                String name = res.getDetails();
//                Bitmap face = res.getBitmap();
//                ivSupervisor.setImageBitmap(face);
//                tvSupervisor.setText("监护人: "+name);
//            }
//            Toast.makeText(this.getApplicationContext(), res.getDetails(), Toast.LENGTH_SHORT).show();
//        });
//
//        buttFaceManage.setOnClickListener(v -> {
//            // 人脸管理接口测试部分,接口可用
//            Bitmap demo = null;
//            try {
//                demo = BitmapFactory.decodeStream(getAssets().open("addFaceDemo.jpg"));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            PuruiResult[] res = PSM.checkFaces();   //查看
//            PuruiResult res1 = PSM.deleteFace("李鑫"); //删除
//            Toast.makeText(this.getApplicationContext(), res1.getDetails(), Toast.LENGTH_SHORT).show();
//            res = PSM.checkFaces(); //查看
//            PuruiResult res2 = PSM.addFace("李鑫", demo); //增加
//            Toast.makeText(this.getApplicationContext(), res2.getDetails(), Toast.LENGTH_SHORT).show();
//            res = PSM.checkFaces();  //查看
//
//        });
//
//        //扫描摄像头 按钮
//        findViewById(R.id.buttonScan).setOnClickListener(v-> PSM.scanCameras());
//        buttVideo.setOnClickListener(v -> PSM.recordVideo());
//
//        imageShowOri.setOnClickListener(v -> {
//            if(rgCameraType.getCheckedRadioButtonId() == R.id.rbPad){
//                dispatchTakePictureIntent();
//            }
//        });
//        Log.d(TAG,"onCreate");
//    }
//    private static final int REQUEST_IMAGE_CAPTURE = 1;
//    private String mCurrentPhotoPath;
//    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        // 确保有应用程序可以处理该 Intent
////        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            // 创建一个用于保存照片的文件
//            File photoFile = null;
//            try {
//                photoFile = createImageFile();
//            } catch (IOException ex) {
//                // 处理异常
//            }
//            // 如果文件创建成功，将其作为参数传递给 Intent
//            if (photoFile != null) {
//                Uri photoURI = FileProvider.getUriForFile(this, "com.example.client_demo_aidl.fileprovider", photoFile);
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//            }
////        }
//    }
//    private File createImageFile() throws IOException {
//        // 创建一个唯一的文件名
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "JPEG_" + timeStamp + "_";
//        // 获取应用程序的外部存储目录
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        // 创建一个临时文件
//        File image = File.createTempFile(
//                imageFileName,  /* 前缀 */
//                ".jpg",         /* 后缀 */
//                storageDir      /* 目录 */
//        );
//        // 保存文件路径以供以后使用
//        mCurrentPhotoPath = image.getAbsolutePath();
//        return image;
//    }
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bitmap imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
////            PSM.recognizeID(imageBitmap,"aaaaaa");
//            PSM.recognizeID("aaa");
//            PSM.detectStates(imageBitmap,"kaiguan","ddd");
//        }
//    }
//
//    @Override
//    public void onStart(){
//        Log.d(TAG,"onStart");
//        super.onStart();
//    }
//    @Override
//    public void onResume() {
//        Log.d(TAG,"onResume");
//        PSM.resumeService();
//        super.onResume();
//    }
//    @Override
//    public void onStop() {
//        Log.d(TAG,"onStop");
////        PSM.selectCamera("无");
//        PSM.stopService();
//        super.onStop();
//    }
//    @Override
//    public void onPause(){
//        Log.d(TAG,"onPause");
//        super.onPause();
//    }
//
//    @Override
//    protected void onDestroy() {
//        //结束服务
//        PSM.destroyService();
//        super.onDestroy();
//        Log.d(TAG,"onDestroy");
//    }
//
//}