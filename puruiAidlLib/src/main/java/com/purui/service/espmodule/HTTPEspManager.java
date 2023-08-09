package com.purui.service.espmodule;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.purui.service.result.PuruiResult;
import com.purui.service.Utils.Utils;

import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class HTTPEspManager implements IEspHandle {
    private final String[] ips = {null, null};
    private final IEspCallback mainService;
    private boolean show_cam = false;
    private int linking_id;
    private final File ipLogFile;
    private final Context ctx;
    private GetImg getImg;
    private ExecutorService executorService;

    public HTTPEspManager(IEspCallback mainService){
        this.mainService = mainService;
        ctx = mainService.getContext();
        String ipLogPath = ctx.getCacheDir().toString();
        String ipLogName = "espips.txt";
        ipLogFile = new File(ipLogPath, ipLogName);
        if(ipLogFile.exists()) {
            readCamIpFromTXT();
        }
    }

    @Override
    public String scanEsp(){
        String msg;
        if(!((Utils.isHarmonyOs(ctx) || isWifiApOpen(ctx)) && (isWifiOpen(ctx) || isWifiApOpen(ctx)))){ //这一步是经过逻辑推理来的
            msg = "请先开启热点\"CAMERA\"";
        }else {
            ArrayList<String[]> result = mNetWorkUtils.liseESP();
            String[] ip_strs = new String[result.size()];
            StringBuilder cat_str = new StringBuilder();
            boolean getYandianIP = false;
            boolean getYuanchengIP = false;
            for(int i=0;i<result.size();i++){
                String[] curESP = result.get(i);
                if (!getYandianIP && curESP[1].equals("验电")) {
                    ip_strs[i] = "yandian:" + curESP[0];
                    ips[1] = curESP[0];
                    getYandianIP = true;
                    cat_str.append("扫描到验电摄像头，IP地址为：").append(curESP[0]).append("\n");
                }
                if (!getYuanchengIP && curESP[1].equals("远程")) {
                    ip_strs[i] = "yuancheng:" + curESP[0];
                    ips[0] = curESP[0];
                    getYuanchengIP = true;
                    cat_str.append("扫描到远程摄像头，IP地址为：").append(curESP[0]).append("\n");
                }
            }
            if(getYandianIP | getYuanchengIP){
                if(ipLogFile.exists()){
                    ipLogFile.delete();
                }
                try{
                    Utils.writeTXT(ip_strs, ipLogFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(cat_str.length() == 0){
                cat_str.append("找不到摄像头，请确保摄像头已连接上热点“CAMERA”");
            }
            msg = cat_str.toString();
        }
        return msg;
    }

    @Override
    public PuruiResult getEspReady(int CAMID){
        //find connected ip
        PuruiResult ret;
        if(!Utils.isHarmonyOs(ctx) || !isWifiOpen(ctx)){
            if(!isWifiApOpen(ctx)){
                return new PuruiResult(false,"请开启热点“CAMERA”");
            }
        }
        if(ips[CAMID-1] == null) {
            return new PuruiResult(false, "当前摄像头未找到，请先扫描摄像头");
        }
        boolean ipConnected = false;
        TestIPTask task = new TestIPTask(ips[CAMID - 1]);
        new Thread(task).start();
        try{
            ipConnected = task.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        String camType = (CAMID==1)?"远程":"验电";
        ret = ipConnected? new PuruiResult(true,"已检查摄像头设备"):new PuruiResult(false,camType+"摄像头连接失败");
        return ret;
    }

    @Override
    public boolean setCamera(int id, boolean show_cam){
        if (getImg != null) {
            getImg.cancel(true);
            getImg = null;
        }
        if(executorService == null){
            executorService = Executors.newFixedThreadPool(5);
        }
        getImg = new GetImg();
        linking_id = id-1;
        String url = "http://" + ips[linking_id] + "/capture";
        getImg.executeOnExecutor(executorService, url);
        this.show_cam = show_cam;
        mainService.handleEspCallback("camera connected id"+(id));
        return false;
    }
    @Override
    public void releaseCamera(){
        if (getImg != null) {
            getImg.cancel(true);
            getImg = null;
        }
        if(executorService != null){
            executorService.shutdown();
            executorService = null;
        }
    }
    @Override
    public void sendPulse(String pulseCMD){
        String url = "http://" + ips[linking_id] + "/" + pulseCMD;
        Communication comn = new Communication();
        comn.executeOnExecutor(executorService, url);
    }
    private boolean isWifiOpen(Context ctx) {
        ConnectivityManager conMan = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        return NetworkInfo.State.CONNECTED == wifi;
    }
    private boolean isWifiApOpen(Context context) {
        WifiApManage manager = new WifiApManage(context);
        return manager.isWifiApEnabled();
    }

    private void readCamIpFromTXT(){
        if(!ipLogFile.exists()){
            return;
        }
        String ipsInFile = null;
        try {
            //创建一个带缓冲区的输入流
            FileInputStream bis = new FileInputStream(ipLogFile);
            InputStreamReader reader=new InputStreamReader(bis, StandardCharsets.UTF_8);
            char[] buffer = new char[bis.available()];
            while (reader.read() != -1) {
                reader.read(buffer);
            }
            ipsInFile = new String(buffer);
            reader.close();
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(ipsInFile != null){
            String[] read_ips = ipsInFile.split("\n");
            for(String ip:read_ips){
                if(ip.contains("yuancheng")){
                    ips[0] = ip.split(":")[1];
                }else if(ip.contains("yandian")){
                    ips[1] = ip.split(":")[1];
                }
            }
            if(ips[0] == null || ips[1] == null){
                ipLogFile.delete();
            }
        }
    }
    private static class TestIPTask extends FutureTask<Boolean> {
        public TestIPTask(String ip) {
            super(() -> {
                try {
                    InetAddress address = InetAddress.getByName(ip);
                    if(address.isReachable(100)){
                       return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            });
        }
    }

    private Bitmap recordBitmap;
    private boolean isRecording = false;
    private int frameIndex = 1;
    private final Callable<String> recordCall = new Callable<String>(){
        String newFileName;
        private final double frameRate = 8;//1表示1秒8个照片，
        final AndroidFrameConverter converter = new AndroidFrameConverter();
        @Override
        public String call() {
            newFileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/数据记录" + Utils.getTimeFormat() + ".mp4";
            FFmpegLogCallback.set();
            String showText = "";
            File file = new File(newFileName);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    Log.d("main", "创建");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.e("main", "luzhi" + file.getPath());
            Bitmap bitmap = null;
            while(bitmap == null){
                bitmap = recordBitmap;
            }
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(file, bitmap.getWidth(), bitmap.getHeight(), 0);//设置视频的宽高，这里设置的是以第一张照片为宽高为基准的。
            recorder.setFormat("mp4");
            // 录像帧率
            recorder.setFrameRate(frameRate);
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            try {
                // 记录开始
                recorder.start();
                int recordFrame = HTTPEspManager.this.frameIndex-1;
                Frame frame;
                while (isRecording) {
                    if(recordFrame >= HTTPEspManager.this.frameIndex){
                        continue;
                    }
                    Bitmap bm = recordBitmap;
                    frame = converter.convert(bm);
                    if(frame!=null){
                        frame.imageChannels = 4;
                        recorder.record(frame);
                        recordFrame++;
                    }
                }
                Log.d("test", "录制完成....");
                // 录制结束
                recorder.stop();
                showText = "录制完成，已保存到"+ newFileName +" 1";
            } catch (FFmpegFrameRecorder.Exception | NullPointerException e) {
                e.printStackTrace();
            }
            return showText;
        }
    };
    private FutureTask<String> ft;
    @Override
    public String startRecord() {
        if(isRecording){
            return "正在录制中...";
        }
        isRecording = true;
        if(recordBitmap != null){
            ft = new FutureTask<>(recordCall);
            new Thread(ft).start();
            return "开始录像 1";
        }else {
            return "fail to record";
        }
    }
    @Override
    public String endRecord() {
        if(!isRecording){
            return "不在录制中!";
        }
        isRecording = false;
        String res;
        try{
            res = ft.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        frameIndex = 1;
        return res;
    }
    private volatile boolean sendingPulse = false;
    //异步任务，循环获取esp的图片,将图片传递给检测和录制的异步任务
    private class GetImg extends AsyncTask<String, Bitmap, Void> {
        String url = null;
        private volatile boolean isRunning = true;
        private final OkHttpClient okclient = new OkHttpClient();
        @Override
        protected Void doInBackground(String... urls) {
            url = urls[0];
            Request request = new Request.Builder().url(url).get().build();
            while(isRunning && !isCancelled()){//一直跑，被打断立即停止
                synchronized (HTTPEspManager.class){
                    if(sendingPulse){
                        try {
                            HTTPEspManager.class.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try{
                        Response response = okclient.newCall(request).execute();
                        if (response.isSuccessful()) { // 检查响应是否成功
                            ResponseBody responseBody = response.body();// 获取响应体
                            if (responseBody != null) {
                                Bitmap bitmap = BitmapFactory.decodeStream(responseBody.byteStream());// 读取响应体中的数据流
                                if (bitmap != null) {
                                    publishProgress(bitmap);
                                }
                                responseBody.close();// 关闭响应体
                            }
                        } else {
                            Log.e("ESP32","Response failed: " + response.code());// 响应失败
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
        //在调用'publishProgress()'方法后被调用，可以更新UI
        @Override
        protected void onProgressUpdate(Bitmap... bitmaps){
            if(show_cam){
                mainService.setCamPhoto(bitmaps[0]);
                recordBitmap = bitmaps[0];
                if(isRecording){
                    frameIndex++;
                }
            }
        }
        @Override
        protected void onCancelled() {
            super.onCancelled();
            isRunning = false; // 中断正在执行的线程
            if(show_cam){
                mainService.handleEspCallback("camera closed");
            }
        }
    }

    private class Communication extends AsyncTask<String, Void, Void>{
        String url= null;
        private final OkHttpClient okclient = new OkHttpClient.Builder()
                .callTimeout(7000, java.util.concurrent.TimeUnit.MILLISECONDS).build();
        @Override
        protected Void doInBackground(String... urls){
            sendingPulse = true;
            url = urls[0];
            Request request = new Request.Builder().url(url).get().build();
            Response response = null;
            synchronized (HTTPEspManager.class){
                try{
                    response = okclient.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                HTTPEspManager.class.notify();
                sendingPulse = false;
            }
            if (response != null && response.isSuccessful()) { // 检查响应是否成功
                ResponseBody responseBody = response.body();// 获取响应体
                if (responseBody != null) {
                    try{
                        String result = responseBody.string();
                        if(!"".equals(result)){
                            mainService.handleEspCallback(result);
                        }else{
                            mainService.handleEspCallback("fail to connect cam id"+(linking_id+1));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if(response != null) Log.e("ESP32","Response failed: " + response.code());// 响应失败
            }
            return null;
        }
    }
}