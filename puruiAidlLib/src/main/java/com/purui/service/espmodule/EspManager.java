//package com.purui.service.espmodule;
//
//import android.content.Context;
//import android.content.res.Resources;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.os.Environment;
//import android.os.Handler;
//import android.os.Looper;
//import android.os.Message;
//import android.util.Log;
//
//import com.purui.service.result.PuruiResult;
//
//import org.bytedeco.ffmpeg.global.avutil;
//import org.bytedeco.javacv.AndroidFrameConverter;
//import org.bytedeco.javacv.FFmpegFrameRecorder;
//import org.bytedeco.javacv.FFmpegLogCallback;
//import org.bytedeco.javacv.Frame;
//
//import java.io.BufferedReader;
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.io.OutputStreamWriter;
//import java.lang.ref.SoftReference;
//import java.net.Inet4Address;
//import java.net.InetAddress;
//import java.net.NetworkInterface;
//import java.net.Socket;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Enumeration;
//import java.util.TimerTask;
//import java.util.concurrent.Callable;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.FutureTask;
//
//public class EspManager implements IEspHandle {
//
//    private final String TAG = "esp";
//    private String[] ips = {null, null};
//    private Socket socket;
//    private InputStream inputStream;
//    private OutputStream outputStream;
//    private Socket testSocket;
//    private InputStream testInputStream;
//    private OutputStream testOutputStream;
////    private boolean espNeedTest = true;
////    private SharedPreferences sharedPreferences;
//
//    private byte[] RevBuff = new byte[1024];  //定义接收数据流的包的大小
//    private final MyHandler myHandler = new MyHandler();;
//    private byte[] temp = new byte[0];  //存放一帧图像的数据
//    private int headFlag = 0;    // 0 数据流不是图像数据   1 数据流是图像数据
//    private final IEspCallback mainService;
//    private int linking_id; //存储当前连接摄像头id
//    private boolean show_cam = false;
//    private boolean testingIp = false;
//    private boolean pingingIp = false;
//    private boolean isYuanchengIp;
//    private boolean isYandianIp;
//    private final File ipLogFile;
//    private final Context ctx;
////    private int temp_count = 0;
//
//    public EspManager(IEspCallback mainService){
//        this.mainService = mainService;
////        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainService.getContext());
//        ctx = mainService.getContext();
//        String ipLogPath = ctx.getCacheDir().toString();
//        String ipLogName = "espips.txt";
//        ipLogFile = new File(ipLogPath, ipLogName);
//
//        if(ipLogFile.exists()) {
//            readCamIpFromTXT();
//        }
////        }else {
////            if(!isHarmonyOs(ctx) || !isWifiOpen(ctx)){
////                if(isWifiApOpen(ctx)){
////                    scanEsp();
////                }
////            }else{
////                mainService.makeToast("请先关闭WIFI并开启热点，再扫描摄像头");
////            }
////        }
//
//    }
//
//    @Override
//    public void scanEsp(){
//        if(!((isHarmonyOs(ctx) || isWifiApOpen(ctx)) && (isWifiOpen(ctx) || isWifiApOpen(ctx)))){ //这一步是经过逻辑推理来的
//            mainService.makeToast("请先开启热点\"CAMERA\"");
//        }else {
//            if(!pingingIp){ // find esp ip and create log
//                pingingIp = true;
//                mainService.showScanDialog();
//                new Thread(()-> scanCameraByPing()).start();
//            }
//        }
//
//    }
//
//    private final TimerTask scanEspTask = new TimerTask(){
//        public void run(){
//            scanEsp();
//        }
//    };
//
//    @Override
//    public PuruiResult getEspReady(int CAMID){
//        //find connected ip
//        PuruiResult ret;
//        if(!isHarmonyOs(ctx) || !isWifiOpen(ctx)){
//            if(!isWifiApOpen(ctx)){
//                return new PuruiResult(false,"请开启热点“CAMERA”");
//            }
//        }
//        if(ips[CAMID-1] == null){
//            if(pingingIp){
//                return new PuruiResult(false,"正在寻找摄像头，请耐心等待");
//            }else {
//                return new PuruiResult(false,"当前摄像头未找到，请先扫描摄像头");
//            }
////            if(!readCamIpFromTXT()){
////                try {
////                    Timer timer = new Timer();
////                    timer.schedule(scanEspTask,4000);
////                }catch (IllegalStateException e){
////                    e.printStackTrace();
////                }
////
////                return new PuruiResult(false,"正在扫描摄像头...");
////            }
//        }
////        if(espNeedTest){
//////            File ipLogFile = new File(ipLogPath, ipLogName);
//////            if(!ipLogFile.exists()){
//////                return new PuruiResult(false,"请重新扫描摄像头");
//////            }
////
//////            String ipsInFile = null;
//////            try {
//////                //创建一个带缓冲区的输入流
//////                FileInputStream bis = new FileInputStream(ipLogFile);
//////                InputStreamReader reader=new InputStreamReader(bis, StandardCharsets.UTF_8);
//////                char[] buffer = new char[bis.available()];
//////                while (reader.read() != -1) {
//////                    reader.read(buffer);
//////                }
//////                ipsInFile = new String(buffer);
//////                reader.close();
//////                bis.close();
//////            } catch (Exception e) {
//////                e.printStackTrace();
//////            }
//////
////////            ArrayList<String[]> ip_mac = new ArrayList<>();
//////            ArrayList<String> ip_ping = new ArrayList<>();
////////            for(int i=0;i<ip_mac.size();i++){
////////                getCameraByIp(ip_mac.get(i)[0]);
////////            }
//////            if(ipsInFile != null){
//////                String[] ips = ipsInFile.split("\n");
//////                for(String ip:ips){
//////                    if(ip.matches("^((25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))$"))
//////                    {
//////                        ip_ping.add(ip);
//////                    }
//////                }
//////            }
//////            if(ip_ping.size()<2){
//////                return new PuruiResult(false, "请重新扫描摄像头");
//////            }
//////            for(int i=0;i<ip_ping.size();i++){
//////                getCameraByIp(ip_ping.get(i));
//////            }
//////            if(ips[0] != null && ips[1] != null){
//////                ret = new PuruiResult(true,"热点接收到2个设备");
//////                espNeedTest = false;
//////            }else {
//////                System.out.println("连接热点设备不是2个");
//////                ret = new PuruiResult(false, "连接热点设备不是2个");
//////            }
////
////        }else{
////            ret = new PuruiResult(true,"已检查摄像头设备");
////        }
//        System.out.println("ip1: "+ips[0]);
//        System.out.println("ip2: "+ips[1]);
//        boolean ipConnected = false;
//        TestIPTask task = new TestIPTask(ips[CAMID-1]);
//        new Thread(task).start();
//        try{
//            ipConnected = task.get();
//        } catch (ExecutionException | InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        String camType = (CAMID==1)?"远程":"验电";
//        ret = ipConnected? new PuruiResult(true,"已检查摄像头设备"):new PuruiResult(false,camType+"摄像头连接失败");
////        if(!ipConnected){
//////            ips[CAMID-1] = null;
////            if(ipLogFile.exists()){
////                ipLogFile.delete();
////            }
////        }
//        return ret;
//    }
//
//    @Override
//    public boolean setCamera(int id, boolean show_cam){
//        if (socket != null) {
//            releaseCamera();
//        }
//        linking_id = id-1;
//        new Thread(() -> {
//            Message msg = myHandler.obtainMessage();
//            try {
//                socket = new Socket(ips[linking_id], 8080);
//                if(socket.isConnected()){
//                    msg.what = 0;//显示连接服务器成功信息
//                    mainService.handleEspCallback("camera conneted id"+id);
//                    inputStream = socket.getInputStream();
//                    outputStream = socket.getOutputStream();
//                    this.show_cam = show_cam;
//                    Recv();//接收数据
//                }else{
//                    msg.what = 1;//显示连接服务器失败信息
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                msg.what = 1;//显示连接服务器失败信息
//                mainService.handleEspCallback("fail to connet cam id"+id);
////                ips = new String[]{null, null};
////                espNeedTest = true;
//            }
//            myHandler.sendMessage(msg);
//        }).start();
//
//        return false;
//    }
//
//    private String getCameraByIp(String ip){
//        testingIp = true;
//        isYuanchengIp = false;
//        isYandianIp = false;
//        new Thread(() -> {
//            try {
//                testSocket = new Socket(ip, 8080);
//                if (testSocket.isConnected()) {
//                    testInputStream = testSocket.getInputStream();
//                    testOutputStream = testSocket.getOutputStream();
//                    RecvTestMsg(ip);//接收数据
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).start();
//        Date start = new Date();
//        long waitDuration = 0;
//        while(testingIp && waitDuration<5000){waitDuration = new Date().getTime() - start.getTime();}
//        if (testSocket != null) {
//            releaseTestCamera();
//        }
//        if(isYuanchengIp){
//            return "yuancheng:";
//        }else if(isYandianIp){
//            return "yandian:";
//        }else{
//            return "";
//        }
//    }
//    @Override
//    public void releaseCamera(){
//        try { socket.close(); } catch (Exception e) { e.printStackTrace(); }
//        try { inputStream.close(); }catch (Exception e) { e.printStackTrace(); }
//        try { outputStream.close(); }catch (Exception e) { e.printStackTrace(); }
//        socket = null;
//    }
//    private void releaseTestCamera(){
//        try { testSocket.close(); } catch (Exception e) { e.printStackTrace(); }
//        try { testInputStream.close(); }catch (Exception e) { e.printStackTrace(); }
//        try { testOutputStream.close(); }catch (Exception e) { e.printStackTrace(); }
//        testSocket = null;
//    }
//    @Override
//    public void sendPulse(String pulseCMD){
//        new Thread(() -> {
//            try {
//                outputStream.write(pulseCMD.getBytes(StandardCharsets.UTF_8));
////                if(!pulseCMD.equals("receivepulse")){
////                    mainService.handleEspCallback("esp send pulse");
////                }
//            } catch (Exception e) {
//                e.printStackTrace();
////                if(!socket.isConnected()){
////                    new Thread(() -> {
////                        try {
////                            if(socket.isConnected()){
////                                inputStream = socket.getInputStream();
////                                outputStream = socket.getOutputStream();
////                            }
////                        } catch (IOException en) {
////                            en.printStackTrace();
////                        }
////                    }).start();
////                }
////                if(!pulseCMD.equals("receivepulse")){
//                mainService.handleEspCallback("fail to call esp");
////                ips = new String[]{null, null};
////                espNeedTest = true;
////                }
//
//            }
//        }).start();
//    }
//
//    //    接收数据方法
//    private void Recv(){
//        new Thread(() -> {
//            while(socket != null && socket.isConnected()){
//                try {
//                    int Len = inputStream.read(RevBuff);
//                    if(Len != -1){
////                          图像数据包的头  FrameBegin
//                        boolean begin_cam_flag = RevBuff[0] == 70 && RevBuff[1] == 114 && RevBuff[2] == 97 && RevBuff[3] == 109 && RevBuff[4] == 101
//                                && RevBuff[5] == 66 && RevBuff[6] == 101 && RevBuff[7] == 103 && RevBuff[8] == 105 && RevBuff[9] == 110 ;
////                            图像数据包的尾  FrameOverr
//                        boolean end_cam_flag = RevBuff[0] == 70 && RevBuff[1] == 114 && RevBuff[2] == 97 && RevBuff[3] == 109 && RevBuff[4] == 101
//                                && RevBuff[5] == 79 && RevBuff[6] == 118 && RevBuff[7] == 101 && RevBuff[8] == 114 && RevBuff[9] == 114;
////                            判断接收的包是不是图片的开头数据 是的话s说明下面的数据属于图片数据 将headFlag置1
//                        if(headFlag == 0 && begin_cam_flag){
//                            headFlag = 1;
//                        }else if(end_cam_flag){  //判断包是不是图像的结束包 是的话 将数据传给 myHandler  3 同时将headFlag置0
//                            Message msg = myHandler.obtainMessage();
////                            Log.v(TAG, "pic:" + temp_count);
////                            temp_count = 0;
//                            msg.what = 3;
//                            myHandler.sendMessage(msg);
//                            headFlag = 0;
//                        }else if(headFlag == 1){ //如果 headFlag == 1 说明包是图像数据  将数据发给byteMerger方法 合并一帧图像
//                            temp = byteMerger(temp,RevBuff);
////                            temp_count++;
////                            Log.v(TAG,"current:"+temp_count);
//                        }
////                            定义包头 Esp32Msg  判断包头 在向myHandler  2 发送数据    eadFlag == 0 && !end_cam_flag没用 会展示图像的数据
//                        boolean begin_msg_begin = RevBuff[0] == 69 && RevBuff[1] == 115 && RevBuff[2] == 112 && RevBuff[3] == 51 && RevBuff[4] == 50
//                                && RevBuff[5] == 77 && RevBuff[6] == 115 && RevBuff[7] == 103 ;
//                        if(begin_msg_begin){//处理接收到的非图像数据
//                            byte[] Buffer = new byte[Len];
//                            System.arraycopy((byte[]) RevBuff, 0, Buffer, 0, Len);
//                            String receive = byteToString(Buffer);
//                            Log.v(TAG,receive);
//                            mainService.handleEspCallback(receive);
////                                Message msg = myHandler.obtainMessage();
////                                msg.what = 2;
////                                msg.arg1 = Len;
////                                msg.obj = RevBuff;
////                                myHandler.sendMessage(msg);
//                        }
//                    }else{
////                            如果Len = -1 说明接受异常  显示连接服务器失败信息  跳出循环
//                        Message msg = myHandler.obtainMessage();
//                        msg.what = 1;
//                        myHandler.sendMessage(msg);
//                        break;
//                    }
//                } catch (IOException e) {
////                        如果接受数据inputStream.read(RevBuff)语句执行失败 显示连接服务器失败信息  跳出循环
//                    e.printStackTrace();
//                    Message msg = myHandler.obtainMessage();
//                    msg.what = 1;
//                    myHandler.sendMessage(msg);
//                    break;
//                }
//            }
//        }).start();
//    }
//
//    //    接收数据方法
//    private void RecvTestMsg(String save_ip){
//            while(testSocket != null && testSocket.isConnected()){
//                try {
//                    int Len = testInputStream.read(RevBuff);
//                    if(Len != -1){
////                            定义包头 Esp32Msg  判断包头 在向myHandler  2 发送数据    eadFlag == 0 && !end_cam_flag没用 会展示图像的数据
//                        boolean begin_msg_begin = RevBuff[0] == 69 && RevBuff[1] == 115 && RevBuff[2] == 112 && RevBuff[3] == 51 && RevBuff[4] == 50
//                                && RevBuff[5] == 77 && RevBuff[6] == 115 && RevBuff[7] == 103 ;
//                        if(begin_msg_begin){//处理接收到的非图像数据
//                            byte[] Buffer = new byte[Len];
//                            System.arraycopy((byte[]) RevBuff, 0, Buffer, 0, Len);
//                            String receive = byteToString(Buffer);
//                            if(receive.contains("Esp32MsgClient is Connect!")){
//                                if(receive.contains("id1")){
//                                    ips[0] = save_ip;
//                                    isYuanchengIp = true;
//                                }else if(receive.contains("id2")){
//                                    ips[1] = save_ip;
//                                    isYandianIp = true;
//                                }
//                                testingIp = false;
//                                break;
//                            }
//                        }
//                    }else{
////                            如果Len = -1 说明接受异常  显示连接服务器失败信息  跳出循环
//                        break;
//                    }
//                } catch (IOException e) {
////                        如果接受数据inputStream.read(RevBuff)语句执行失败 显示连接服务器失败信息  跳出循环
//                    e.printStackTrace();
//                    break;
//                }
//            }
//    }
//
//    //处理一些不能在线程里面执行的信息
//    class MyHandler extends Handler {
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case 0:
////                    连接服务器成功信息
////                    Toast.makeText(MainActivity.this, "连接服务器成功！", Toast.LENGTH_SHORT).show();
////                    connect_button.setText("断开");
////                    mainService.handleEspCallback("camera conneted");
//                    break;
//                case 1:
////                    连接服务器失败信息
////                    Toast.makeText(MainActivity.this, "连接服务器失败！", Toast.LENGTH_SHORT).show();
////                    mainService.handleEspCallback("fail to connet cam");
//                    break;
//                case 2:
////                    处理接收到的非图像数据
////                    byte[] Buffer = new byte[msg.arg1];
////                    System.arraycopy((byte[]) msg.obj, 0, Buffer, 0, msg.arg1);
////                    String receive = byteToString(Buffer);
////                    Log.v(TAG,receive);
////                    mainService.handleEspCallback(receive);
//                    break;
//                case 3:
////                    处理接受到的图像数据 并展示
//                    if(show_cam){
////                        try{
//////                            Bitmap bitmap = BitmapFactory.decodeByteArray(temp, 0, temp.length);
////                            Bitmap bitmap = byteToBitmap(temp);
////                            if (bitmap != null) {
////                                mainService.setBitmap(bitmap);
////                            }
////                        }catch (Exception e){
////                            e.printStackTrace();
////                        }
//                        mainService.setBitmapBytes(temp);
//                        recordBytes = temp;
//                        if(isRecording){
//                            frameIndex++;
//                        }
//                    }
//                    temp = new byte[0];
//                    break;
//                default:
//                    break;
//            }
//        }
//    }
//    private byte[] byteMerger(byte[] a,byte[] b){
//        int i = a.length + b.length;
//        byte[] t = new byte[i]; //定义一个长度为 全局变量temp  和 数据包RevBuff 一起大小的字节数组 t
//        System.arraycopy(a,0,t,0,a.length);  //先将 temp（先传过来的数据包）放进  t
//        System.arraycopy(b,0,t,a.length,b.length);//然后将后进来的这各数据包放进t
//        return t; //返回t给全局变量 temp
//    }
//    private ArrayList<String[]> get_connected_ip_mac() throws Exception {
//        ArrayList<String[]> result = new ArrayList<String[]>();
//        String local_ip = getMobileIpAddress();
//        String[] local_ip_split = local_ip.split("\\.");
//        System.out.println("local_ip_split:"+local_ip_split[0]+"."+local_ip_split[1]+"."+local_ip_split[2]+"."+local_ip_split[3]);
//        String ip;
//        String mac;
//        Process ipProc = Runtime.getRuntime().exec("ip neigh show");
//        ipProc.waitFor();
//        if (ipProc.exitValue() != 0) {
//            throw new Exception("Unable to access ARP entries");
//        }
//        try{
//            BufferedReader br = new BufferedReader(new InputStreamReader(ipProc.getInputStream(), StandardCharsets.UTF_8));
//            String line;
//            while ((line = br.readLine()) != null) {
//                String[] splitted = line.split(" +");
//                if (splitted.length == 6) {
//                    System.out.println(line);
//                    ip = splitted[0];
//                    String[] ip_splitted = ip.split("\\.");
//                    mac = splitted[4];
//                    if (ip_splitted[0].equals(local_ip_split[0]) && ip_splitted[1].equals(local_ip_split[1]) && ip_splitted[2].equals(local_ip_split[2])) {
//                        String[] couplet = new String[2];
//                        couplet[0] = ip;
//                        couplet[1] = mac;
//                        result.add(couplet);
//                    }
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return result;
//    }
//
////    private ArrayList<String[]> get_connected_ip_mac() throws Exception {
////
////        ArrayList<String[]> result = new ArrayList<String[]>();
////        String local_ip = getMobileIpAddress();
////        String[] local_ip_split = local_ip.split("\\.");
////        System.out.println("local_ip_split:"+local_ip_split[0]+"."+local_ip_split[1]+"."+local_ip_split[2]+"."+local_ip_split[3]);
////        String ip = null;
////        String mac = null;
////        String[] couplet0 = new String[2];
////        String[] couplet1 = new String[2];
////    //        String[] couplet = new String[2];
////        String[] out_ip = new String[2];
////        Process ipProc = Runtime.getRuntime().exec("ip neigh");
////        ipProc.waitFor();
////        if (ipProc.exitValue() != 0) {
////            throw new Exception("Unable to access ARP entries");
////        }
////        try{
////            BufferedReader br = new BufferedReader(new InputStreamReader(ipProc.getInputStream(), StandardCharsets.UTF_8));
////            String line;
////            while ((line = br.readLine()) != null) {
////                String[] splitted = line.split(" +");
////                if (splitted != null && splitted.length == 6) {
////                    System.out.println(line);
////                    ip = splitted[0];
////                    String[] ip_splitted = ip.split("\\.");
////                    String flag = splitted[2];
////                    mac = splitted[4];
////                    String state = splitted[5];
////                    // 1: 远程 40:22:d8:74:82:40   验电 40:22:d8:76:d0:c4
////                    // 2: 远程 c0:49:ef:f1:14:08   验电 f4:cf:a2:aa:20:c8
////                    // 3: 远程 c4:4f:33:75:8c:61   验电 84:0d:8e:3c:80:14
////                    // cunhuo: 远程 7c:9e:bd:e3:8d:b0  验电 c8:f0:9e:9d:3a:a0
////                    String yuancheng_mac = sharedPreferences.getString(mainService.getContext().getString(R.string.yuancheng),
////                            mainService.getContext().getString(R.string.yuancheng_default));
////                    String yandian_mac = sharedPreferences.getString(mainService.getContext().getString(R.string.yandian),
////                            mainService.getContext().getString(R.string.yandian_default));
////                    if(mac.equals(yuancheng_mac)){
////                        couplet0[0] = ip;
////                        couplet0[1] = mac;
////                    }
////                    if(mac.equals(yandian_mac)){
////                        couplet1[0] = ip;
////                        couplet1[1] = mac;
////                    }
////                }
////            }
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////    //        return out_ip;
////        result.add(couplet0);
////        result.add(couplet1);
////        return result;
////    }
//    /** 移动网络 IP **/
//    private static String getMobileIpAddress() {
//        try {
//            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
//                NetworkInterface intf = (NetworkInterface) en.nextElement();
//                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
//                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
//                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
//                        return inetAddress.getHostAddress().toString();
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//    private String byteToString(byte[] data) {
//        int index = data.length;
//        for (int i = 0; i < data.length; i++) {
//            if (data[i] == 0) {
//                index = i;
//                break;
//            }
//        }
//        byte[] temp = new byte[index];
//        Arrays.fill(temp, (byte) 0);
//        System.arraycopy(data, 0, temp, 0, index);
//        String str;
//        str = new String(temp, StandardCharsets.UTF_8);
//        return str;
//    }
////    private Bitmap byteToBitmap(byte[] imgByte) {
////        InputStream input;
////        Bitmap bitmap;
////        BitmapFactory.Options options = new BitmapFactory.Options();
////        options.inSampleSize = 1;
//////        options.inMutable = false;
////        options.inPreferredConfig = Bitmap.Config.ARGB_4444;
////        input = new ByteArrayInputStream(imgByte);
////        SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(
////                input, null, options));
////        bitmap = (Bitmap) softRef.get();
//////        if (imgByte != null) {
//////            imgByte = null;
//////        }
////
////        try {
////            input.close();
////        } catch (IOException e) {
////            // TODO Auto-generated catch block
////            e.printStackTrace();
////        }
////        return bitmap;
////    }
//    private boolean isWifiOpen(Context ctx) {
//        ConnectivityManager conMan = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo.State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
//        return NetworkInfo.State.CONNECTED == wifi;
//    }
//    private boolean isWifiApOpen(Context context) {
//
////        WifiApManager manager = new WifiApManager(context);
////        return manager.isWifiApEnabled();
//
//        WifiApManage manager = new WifiApManage(context);
////        boolean res = manager.isWifiApEnabled();
////        Log.v(TAG, "yidakai:"+res);
//        return manager.isWifiApEnabled();
//
////        try {
////            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
////
////            //通过放射获取 getWifiApState()方法
////            Method method = manager.getClass().getDeclaredMethod("getWifiApState");
////            method.setAccessible(true);
////            //调用getWifiApState() ，获取返回值
////            int state = (int) method.invoke(manager);
////            //通过放射获取 WIFI_AP的开启状态属性
////            Field field = manager.getClass().getDeclaredField("WIFI_AP_STATE_ENABLED");
////            //获取属性值
////            int value = (int) field.get(manager);
////            //判断是否开启
////            if (state == value) {
////                return true;
////            } else {
////                return false;
////            }
////        } catch (NoSuchMethodException e) {
////            e.printStackTrace();
////        } catch (IllegalAccessException e) {
////            e.printStackTrace();
////        } catch (InvocationTargetException e) {
////            e.printStackTrace();
////        } catch (NoSuchFieldException e) {
////            e.printStackTrace();
////        }
////        return false;
//    }
//
//    private void scanCameraByPing(){
//        ArrayList<String> result = new ArrayList<>();
//        String local_ip = getMobileIpAddress();
//        String[] local_ip_split = local_ip.split("\\.");
//        byte[] ip = new byte[4];
//        ip[0] = (byte) Integer.parseInt(local_ip_split[0]);
//        ip[1] = (byte) Integer.parseInt(local_ip_split[1]);
//        ip[2] = (byte) Integer.parseInt(local_ip_split[2]);
//        int self = Integer.parseInt(local_ip_split[3]);
//        ip[3] = (byte)self;
//        try{
//            for(int i = 1; i <= 255; i++)
//            {
//                if(i == self){
//                    continue;
//                }
//                ip[3] = (byte) i;
//                InetAddress address = InetAddress.getByAddress(ip);
//                if(address.isReachable(20))
//                {
//                    result.add(address.toString().split("/")[1]);
//                    System.out.println(address + " machine is turned on and can be pinged");
//                }
////                else if(!address.getHostAddress().equals(address.getHostName()))
////                {
////                    System.out.println(address + " machine is known in a DNS lookup");
////                }
//            }
//        } catch(IOException e1) {
//            e1.printStackTrace();
//        }
//        for(int i=0;i<result.size();i++){
//            String canIp = result.get(i);
//            result.set(i,getCameraByIp(canIp)+canIp) ;
//        }
//        String[] ip_strs = new String[result.size()];
//        StringBuilder cat_str = new StringBuilder();
//        boolean getYandianIP = false;
//        boolean getYuanchengIP = false;
//        for(int i=0;i<result.size();i++){
//            String temp = result.get(i);
//            ip_strs[i] = temp;
//            if(!getYandianIP && temp.contains("yandian")){
//                getYandianIP = true;
//                cat_str.append("扫描到验电摄像头，IP地址为：").append(temp.split(":")[1]).append("\n");
//            }
//            if(!getYuanchengIP && temp.contains("yuancheng")){
//                getYuanchengIP = true;
//                cat_str.append("扫描到远程摄像头，IP地址为：").append(temp.split(":")[1]).append("\n");
//            }
//        }
//        if(getYandianIP | getYuanchengIP){
//            if(ipLogFile.exists()){
//                ipLogFile.delete();
//            }
//            try{
//                writeTXT(ip_strs, ipLogFile);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        pingingIp = false;
////        String toast;
////        if(ips[0] == null && ips[1] == null){
////            toast ="摄像头未找到，请检查摄像头是否已连接上热点";
////        }else if(ips[0] == null){
////            toast = "验电摄像头已找到，远程摄像头未找到，请检查摄像头是否已连接上热点";
////        }else if(ips[1] == null){
////            toast =  "远程摄像头已找到，验电摄像头未找到，请检查摄像头是否已连接上热点";
////        }else {
////            toast = "摄像头已找到";
////        }
//        if(cat_str.length() == 0){
//            cat_str.append("找不到摄像头，请确保摄像头已连接上热点“CAMERA”");
//        }
//        Looper.prepare();
////        mainService.makeToast(toast);
//        mainService.showScanResult(cat_str.toString());
//        Looper.loop();
//
////        if(ips[0] == null || ips[1] == null){
////            readCamIpFromTXT();
////        }
//
////                    return result;
//    }
//
//    //将输入框中的外部设备写入txt文件中
//    private void writeTXT(String[] content, File file) throws IOException {
//        //        //创建一个带缓冲区的输出流
//        //        String state= Environment.getExternalStorageState();
//        //
//        //        if(state.equals(Environment.MEDIA_MOUNTED)){
//        //            File SDPath=Environment.getExternalStorageDirectory();//SD根目录
////        File file=new File(filePath, fileName);
//        FileOutputStream fos=new FileOutputStream(file);
//        OutputStreamWriter writer=new OutputStreamWriter(fos, StandardCharsets.UTF_8);
//        for(String str:content){
//            writer.write("\n"+str+"\n");
//        }
//        writer.close();
//        fos.close();
//        //        }
//    }
//
//    private void readCamIpFromTXT(){
//        if(!ipLogFile.exists()){
//            return;
//        }
//        String ipsInFile = null;
//        try {
//            //创建一个带缓冲区的输入流
//            FileInputStream bis = new FileInputStream(ipLogFile);
//            InputStreamReader reader=new InputStreamReader(bis, StandardCharsets.UTF_8);
//            char[] buffer = new char[bis.available()];
//            while (reader.read() != -1) {
//                reader.read(buffer);
//            }
//            ipsInFile = new String(buffer);
//            reader.close();
//            bis.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        if(ipsInFile != null){
//            String[] read_ips = ipsInFile.split("\n");
//            for(String ip:read_ips){
////                if(ip.find("192\\.168(\\.([2][0-4]\\d|[2][5][0-5]|[01]?\\d?\\d)){2}"))
////                {
//                if(ip.contains("yuancheng")){
//                    ips[0] = ip.split(":")[1];
//                }else if(ip.contains("yandian")){
//                    ips[1] = ip.split(":")[1];
//                }
////                }
//            }
//            if(ips[0] == null || ips[1] == null){
//                ipLogFile.delete();
//            }
//        }
//    }
//
//    private class TestIPTask extends FutureTask<Boolean> {
//        public TestIPTask(String ip) {
//            super(new Callable<Boolean>() {
//                @Override
//                public Boolean call() {
//                    try {
//                        InetAddress address = InetAddress.getByName(ip);
//                        if(address.isReachable(100))
//                        {
//                           return true;
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return false;
//                }
//            });
//        }
//    }
//    private boolean isHarmonyOs(Context context) {
//        try {
//            int id = Resources.getSystem().getIdentifier("config_os_brand", "string", "android");
//            return context.getString(id).equals("harmony");
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//
//    private byte[] recordBytes;
//    private boolean isRecording = false;
//    private int frameIndex = 1;
//    private final Callable<String> recordCall = new Callable<String>(){
//        String newFileName;
//
//        private final double frameRate = 8;//1表示1秒8个照片，
////        private final AndroidFrameConverter converter = new AndroidFrameConverter();
//        final AndroidFrameConverter converter = new AndroidFrameConverter();
//        @Override
//        public String call() {
//            newFileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/数据记录" + getTimeFormat() + ".mp4";
//            FFmpegLogCallback.set();
//            String showText = "";
//            File file = new File(newFileName);
//            if (!file.exists()) {
//                try {
//                    file.createNewFile();
//                    Log.d("main", "创建");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            Log.e("main", "luzhi" + file.getPath());
//            Bitmap bitmap = null;
//            while(bitmap == null){
//                bitmap = byteToBitmap(EspManager.this.recordBytes, Bitmap.Config.ARGB_8888,1);
//            }
//            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(file, bitmap.getWidth(), bitmap.getHeight(), 0);//设置视频的宽高，这里设置的是以第一张照片为宽高为基准的。
//            recorder.setFormat("mp4");
//            // 录像帧率
//            recorder.setFrameRate(frameRate);
//            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
//            try {
//                // 记录开始
//                recorder.start();
////                isRecording = true;
//                int recordFrame = EspManager.this.frameIndex-1;
//                Frame frame;
//                while (isRecording) {
//                    if(recordFrame >= EspManager.this.frameIndex){
//                        continue;
//                    }
//                    Bitmap bm = byteToBitmap(recordBytes, Bitmap.Config.ARGB_8888,1);
//                    frame = converter.convert(bm);
//                    if(frame!=null){
//                        frame.imageChannels = 4;
//                        recorder.record(frame);
//                        recordFrame++;
//                    }
//                }
//                Log.d("test", "录制完成....");
//                // 录制结束
//                recorder.stop();
//                showText = "录制完成，已保存到"+ newFileName +" 1";
//            } catch (FFmpegFrameRecorder.Exception | NullPointerException e) {
//                e.printStackTrace();
//            }
//            return showText;
//        }
//    };
//    private FutureTask<String> ft;
//    @Override
//    public String startRecord() {
//        if(isRecording){
//            return "正在录制中...";
//        }
//        isRecording = true;
//        if(recordBytes != null){
//            ft = new FutureTask<>(recordCall);
//            new Thread(ft).start();
//            return "开始录像 1";
//        }else {
//            return "fail to record";
//        }
//    }
//    @Override
//    public String endRecord() {
//        if(!isRecording){
//            return "不在录制中!";
//        }
//        isRecording = false;
//        String res;
//        try{
//            res = ft.get();
//        } catch (ExecutionException | InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        frameIndex = 1;
//        return res;
//    }
//
//    private Bitmap byteToBitmap(byte[] imgBytes, Bitmap.Config preferredConfig, int sampleSize) {
//        InputStream input;
//        Bitmap bitmap;
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = sampleSize;
//        options.inPreferredConfig = preferredConfig; //Bitmap.Config.ARGB_4444
//        input = new ByteArrayInputStream(imgBytes);
//        SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(
//                input, null, options));
//        bitmap = (Bitmap) softRef.get();
//        try {
//            input.close();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return bitmap;
//    }
//    private String getTimeFormat() {
//        //获取系统的日期
//        Calendar calendar = Calendar.getInstance();
//        int year = calendar.get(Calendar.YEAR);
//        int month = calendar.get(Calendar.MONTH)+1;
//        int day = calendar.get(Calendar.DAY_OF_MONTH);
//        int hour = calendar.get(Calendar.HOUR_OF_DAY);
//        int minute = calendar.get(Calendar.MINUTE);
//        int second = calendar.get(Calendar.SECOND);
//        long timeStamp = calendar.getTimeInMillis();
//        return year + "_" + month + "_" + day + "_" +hour + "_" + minute + "_" + second + "_" + timeStamp;
//    }
//
//}
//
