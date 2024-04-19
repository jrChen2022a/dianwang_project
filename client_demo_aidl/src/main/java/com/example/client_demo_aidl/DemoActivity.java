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
import android.view.WindowManager;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_lunwen);


        Button btnStateDetect = findViewById(R.id.buttonStateDetect);     //状态检测 按钮


        ImageView ivOri = findViewById(R.id.image2);            //摄像头实时画面
        ImageView ivDet = findViewById(R.id.imageView2);        //检测结果图
        TextureView textureView = findViewById(R.id.tuv_cam);   //本地摄像头实时画面

        CheckBox cbIsOn = findViewById(R.id.checkBoxIsOn);              //识别结果中“已合上”CheckBox
        CheckBox cbIsOff = findViewById(R.id.checkBoxIsOff);            //识别结果中“已拉开”CheckBox
        CheckBox cbIsTakeOff = findViewById(R.id.checkBoxIsTakeOff);    //识别结果中“已取下”CheckBox
        CheckBox cbIsTakeOn = findViewById(R.id.checkBoxIsTakeOn);      //识别结果中“已给上”CheckBox
        RadioGroup rgCameraType = findViewById(R.id.rgCameraType);      //摄像头选择

        ImageView ivA = findViewById(R.id.ivA);        //三相指示“A”
        ImageView ivB = findViewById(R.id.ivB);        //三相指示“B”
        ImageView ivC = findViewById(R.id.ivC);        //三相指示“C”


        // 摄像头选项
        List<RadioButton> rbsCameraTypeList  = new ArrayList<>();
        Collections.addAll(rbsCameraTypeList,
                findViewById(R.id.rbClose), // 关闭
                findViewById(R.id.rbPad),   // 平板
                findViewById(R.id.rbOpe),   // 操作杆
                findViewById(R.id.rbEle));  // 验电器


        // 按控件顺序注册ServiceManager
        // 按控件顺序注册ServiceManager
        PSM = new PuruiServiceManager(this, ivOri, ivDet, textureView,
                null, cbIsOn, cbIsOff,
                ivA, ivB, ivC,
                null, null,null,
                null, null,null,
                rgCameraType, cbIsTakeOff, cbIsTakeOn);
        PSM.createService();
        PSM.createService();

        // 摄像头选择
        rgCameraType.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton checkedRadioButton = group.findViewById(checkedId);
            if (checkedRadioButton != null) {
                PSM.selectCamera(checkedRadioButton.getText().toString());
            }
        });

        //开关状态检测
        btnStateDetect.setOnClickListener(v -> PSM.preDetectStates(
                "跌落保险", "拉开",
                res -> {
                    Bitmap bitmap = res.getBitmap();
                    Toast.makeText(getApplicationContext(), res.getDetails(), Toast.LENGTH_SHORT).show();
                }
        ));
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