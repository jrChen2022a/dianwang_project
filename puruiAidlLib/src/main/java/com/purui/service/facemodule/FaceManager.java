package com.purui.service.facemodule;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.purui.service.result.PuruiResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FaceManager implements IFaceHandle {
    private static final String tag = "facemanager";
    private boolean modelLoaded = false;
    private final String facePath;
    private final String modelPath;
    private final Context mainContext;
    private final Face mFace = new Face();

    public FaceManager(Context mainContext, File sdDir, boolean withDefaultFace){
        this.mainContext = mainContext;
        modelPath = sdDir.toString() + "/faceDetect/";
        facePath = sdDir.toString() + "/face/";
//        if(checkNeedPermissions()){
            writeFaceModel();
            if(withDefaultFace){
                writeFace();
            }
//        }
    }

    @Override
    public boolean isModelLoaded(){
        return modelLoaded;
    }
    @Override
    public boolean initModel(){
        if(!modelLoaded){
            boolean ret_init = mFace.FaceModelInit(modelPath);
            if (!ret_init)
            {
                Log.e("puruiService", "faceModule Init failed");
            }
            modelLoaded = ret_init;
        }
        return modelLoaded;
    }
    @Override
    public PuruiResult runModel(Bitmap bm){
        PuruiResult ret;
        if(!modelLoaded){
            initModel();
        }
        Bitmap face = cutface(bm);
        String s = recognition(bm);
        if (s.contains("识别成功") && face != null) {
            String name = s.split("：")[1];
            ret = new PuruiResult(true, name, face);
        }else {
            ret = new PuruiResult(false,"识别失败，请重试！");
        }
        return ret;
    }
    @Override
    public void releaseModel(){
        if(modelLoaded){
            mFace.FaceModelUnInit();
            modelLoaded = false;
        }
    }
    @Override
    public PuruiResult[] checkFaces(){
        PuruiResult[] ret = null;
        String file_name, user_name;
        String[] file_name_splitted;
        Bitmap user_bmp;
        int id=0;
        File file = new File(facePath);
        File[] files = file.listFiles();
        if(files != null){
            int num = files.length;
            if(num>0){
                ret = new PuruiResult[num];
                for(int i=0; i<num; i++){
                    file_name = files[i].getName();
                    file_name_splitted = file_name.split("\\.");
                    if(file_name_splitted[1].equals("jpg")){ //|| file_name_splitted[1].equals("png") || file_name_splitted[1].equals("jpeg") || file_name_splitted[1].equals("bmp")
                        user_name = file_name_splitted[0];
                        FileInputStream fis = null;
                        try {
                            fis = new FileInputStream(files[i].getPath());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        user_bmp = BitmapFactory.decodeStream(fis);
                        if(user_bmp != null){
                            //如果是图片
                            ret[id++] = new PuruiResult(true,user_name,user_bmp);
//                        System.out.println("face_width: "+user_bmp.getWidth()+" face_height: "+user_bmp.getHeight());
                        }
                    }
                }
            }
        }
        return ret;
    }
    @Override
    public PuruiResult addFace(String name, Bitmap photo){
        PuruiResult ret;
        if(!modelLoaded){
            initModel();
        }
        Bitmap face = cutface(photo);
        String input_path = facePath + name +".jpg";
        File file = new File(input_path);
        if (!file.exists()) {
            FileOutputStream fileOutputStream = null;
            try {
                if(file.createNewFile())Log.v(tag,"create file");
                fileOutputStream = new FileOutputStream(file);
                face.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
//                Toast.makeText(mainContext, "录入成功", Toast.LENGTH_SHORT).show();
                ret = new PuruiResult(true,name+ " 录入成功",face);
            } catch (Exception e) {
                e.printStackTrace();
//                Toast.makeText(mainContext, "录入失败", Toast.LENGTH_SHORT).show();
                ret = new PuruiResult(false,"录入失败，请检查权限",face);
            }
        }else{
//            Toast.makeText(mainContext,"数据库中已存在该用户！",Toast.LENGTH_SHORT).show();
            ret = new PuruiResult(false,"数据库中已存在该用户！",face);
        }
        return ret;
    }
    @Override
    public PuruiResult deleteFace(String name){
        boolean ret = false;
        String retMsg = name;
        String finalFile_path = facePath+"/"+name+".jpg";
        File img = new File(finalFile_path);
        if (img.isFile()) {
            ret = img.delete();
        }
        if(ret){
            retMsg += " 删除成功！";
        }else{
            retMsg += " 删除失败！";
        }
        return new PuruiResult(ret,retMsg);
    }


    private boolean checkNeedPermissions(){
        if (ContextCompat.checkSelfPermission(mainContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(mainContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //多个权限一起申请
            ActivityCompat.requestPermissions((Activity) mainContext, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 1);
            return false;
        }else {
            return true;
        }
    }
    private void writeFace() {
        try{
            copyBigDataToSD(facePath, "张奇文_2.jpg");
            copyBigDataToSD(facePath, "李鑫_2.jpg");
            copyBigDataToSD(facePath, "李鑫_1.jpg");
            copyBigDataToSD(facePath, "张奇文_1.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void writeFaceModel() {
        try {
            copyBigDataToSD(modelPath, "det1.bin");
            copyBigDataToSD(modelPath, "det2.bin");
            copyBigDataToSD(modelPath, "det3.bin");
            copyBigDataToSD(modelPath, "det1.param");
            copyBigDataToSD(modelPath, "det2.param");
            copyBigDataToSD(modelPath, "det3.param");
            copyBigDataToSD(modelPath, "recognition.bin");
            copyBigDataToSD(modelPath, "recognition.param");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void copyBigDataToSD(String sdPath, String strOutFileName) throws IOException {
        File file = new File(sdPath);
        if (!file.exists()) {
            if(file.mkdir())Log.v(tag,"make dir");
        }
        String tmpFile = sdPath + strOutFileName;
        File f = new File(tmpFile);
        if (f.exists()) {
//            Log.i(TAG, "file exists " + strOutFileName);
            return;
        }
        InputStream myInput = mainContext.getAssets().open(strOutFileName);
        OutputStream myOutput = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            myOutput = Files.newOutputStream(Paths.get(sdPath + strOutFileName));
        }
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            if(myOutput != null){
                myOutput.write(buffer, 0, length);
            }
            length = myInput.read(buffer);
        }
        if(myOutput!=null){
            myOutput.flush();
            myOutput.close();
        }
        myInput.close();
    }
    //人脸检测，截取最大人脸
    private Bitmap cutface(Bitmap img){
        Bitmap face = null;
        int width = img.getWidth();
        int height = img.getHeight();
        byte[] imageData = getPixelsRGBA(img);
        int[] faceInfo = mFace.FaceDetect(imageData, width, height, 4);
        int num = faceInfo[0];
        if (num > 0) {
            //截取最大的人脸
            int maxWidth = 0;
            int j = 0;
            for (int i = 0; i < num; i++) {
                int w = faceInfo[14 * i + 3] - faceInfo[14 * i + 1];
                if (w > maxWidth) {
                    maxWidth = w;
                    j = i;
                }
            }
            int left = faceInfo[14 * j + 1];
            int top = faceInfo[14 * j + 2];
            int right = faceInfo[14 * j + 3];
            int bottom = faceInfo[14 * j + 4];
//            Rect rect = new Rect(left, top, right, bottom);
//            System.out.println("rect: " + rect);
            face = Bitmap.createBitmap(img, left, top, right - left, bottom - top);
        }
        return face;
    }
    private byte[] getPixelsRGBA(Bitmap image) {
        int bytes = image.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        image.copyPixelsToBuffer(buffer);
        return buffer.array();
    }
    private String recognition(Bitmap img){
        String result = "验证失败";
        Bitmap face = cutface(img);
        if(face == null){
            result = "没有检测到人脸";
            return result;
        }else{
            result = calculateSimilar(face);
        }
        return result;
    }
    private String calculateSimilar(Bitmap face){
        byte[] faceData = getPixelsRGBA(face);
        int face_w = face.getWidth();
        int face_h = face.getHeight();
        double similar = 0.0;
        double msim = 0.0;
        File file = new File(facePath);
        File[] files = file.listFiles();
        String file_path = null;//文件路径
        String file_name = null;//文件名称
        String[] file_name_splitted = null;
        String user_name = null;
        Bitmap user_bmp = null;
        String recognition_name = null;
        String result = "识别成功，结果为：";
        if(files != null){
            for(int i=0; i<files.length; i++){
                file_path = files[i].getPath();
                file_name = files[i].getName();
                file_name_splitted = file_name.split("\\.");
                if(file_name_splitted[1].equals("jpg")){// || file_name_splitted[1].equals("png") || file_name_splitted[1].equals("jpeg") || file_name_splitted[1].equals("bmp")
                    //如果是图片
                    user_name = file_name_splitted[0];
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(files[i].getPath());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    user_bmp = BitmapFactory.decodeStream(fis);
                    if(user_bmp != null){
                        Bitmap user_face = cutface(user_bmp);
                        if(user_face!=null){
                            byte[] user_faceData = getPixelsRGBA(user_face);
                            int user_face_w = user_face.getWidth();
                            int user_face_h = user_face.getHeight();
                            similar = mFace.FaceRecognize(faceData, face_w, face_h, user_faceData, user_face_w, user_face_h);
                            System.out.println("user_name: "+user_name+" similar: "+similar);
                            if(similar>msim){
                                msim = similar;
                                recognition_name = user_name;
                            }
                        }
                    }
                }
            }
            if(msim>0.4){
                result += recognition_name.contains("_")?recognition_name.split("_")[0]:recognition_name;
            }else{
                result = "识别失败";
            }
        }


        return result;
    }


}
