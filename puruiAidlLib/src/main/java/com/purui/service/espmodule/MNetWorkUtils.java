package com.purui.service.espmodule;

import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

public class mNetWorkUtils {
    static String TAG = "mNetWorkUtils";
    //查看本地IP
    private static String getMobileIpAddress() {
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    //查看连接热点设备的ip
    private static ArrayList<String> SearchHotIP(){
        ArrayList<String> IP_list = new ArrayList<>();
        String local_ip = getMobileIpAddress();
        String[] local_ip_split = local_ip.split("\\.");
        System.out.println("local_ip: "+local_ip);

//        int version = Build.VERSION.SDK_INT;
//        Log.d(TAG, " android version: "+String.valueOf(version));
//        if(version <= Build.VERSION_CODES.Q){
//            //安卓版本小于等于10
//            try{
//                Process ipProc = Runtime.getRuntime().exec("ip neigh");
//                ipProc.waitFor();
//                if (ipProc.exitValue() != 0) {
//                    throw new Exception("Unable to access ARP entries");
//                }
//                BufferedReader br = new BufferedReader(new InputStreamReader(ipProc.getInputStream(), "UTF-8"));
//                String line;
//                while ((line = br.readLine()) != null) {
//                    String[] splitted = line.split(" +");
//                    if (splitted != null && splitted.length == 6) {
//                        String ip = splitted[0];
//                        String[] ip_splitted = ip.split("\\.");
//                        String flag = splitted[2];
//                        String mac = splitted[4];
//                        String state = splitted[5];
//                        if(ip_splitted[0].equals(local_ip_split[0]) && ip_splitted[1].equals(local_ip_split[1]) && ip_splitted[2].equals(local_ip_split[2])){
//                            //如果ip网关和本地IP网关相同，认为是连接了热点的设备的ip
//                            IP_list.add(ip);
//                            Log.d(TAG, "find a hot ip: "+ip);
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }else{
            //安卓版本大于10时不允许访问"/proc/net/arp"
            Thread find = new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] ip = new byte[4];
                    ip[0] = (byte) Integer.parseInt(local_ip_split[0]);
                    ip[1] = (byte) Integer.parseInt(local_ip_split[1]);
                    ip[2] = (byte) Integer.parseInt(local_ip_split[2]);
                    int self = Integer.parseInt(local_ip_split[3]);
                    ip[3] = (byte)self;
                    try{
                        for(int i = 1; i <= 255; i++)
                        {
                            if(i == self){
                                continue;
                            }
                            ip[3] = (byte) i;
                            InetAddress address = InetAddress.getByAddress(ip);
                            if(address.isReachable(20))
                            {
                                IP_list.add(address.toString().split("/")[1]);
//                                Log.d(TAG, address + " machine is turned on and can be pinged");
//                                System.out.println(address + " machine is turned on and can be pinged");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            find.start();
            try {
                find.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//        }
        return IP_list;
    }
    //测试ip地址是否为esp32cam
    private static String IsEsp(String ip){
        final String[] result = {""};
//        String result = null;
        String testUrl = "http://" + ip + "/test";
        Thread testIP = new Thread(new Runnable() {
            @Override
            public void run() {
                int TIMEOUT = 2000;//设置超时时间
                try {
                    URL url = new URL(testUrl);
                    Log.d(TAG, "testUrl: "+testUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(TIMEOUT);
                    connection.setReadTimeout(TIMEOUT);
                    // Get response
                    int responseCode = connection.getResponseCode();
                    if(responseCode == HttpURLConnection.HTTP_OK){
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        result[0] = response.toString();
                        Log.d(TAG,testUrl+" result: "+ result[0]);
                    }else{
                        //连接失败
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        testIP.start();
        try {testIP.join();} catch (InterruptedException e) {e.printStackTrace();}
        return result[0];
    }
    //列出所有esp32cam
    public static ArrayList<String[]> liseESP(){
        ArrayList<String> hotIPList = SearchHotIP();
        ArrayList<String[]> allresult = new ArrayList<>();
        for(int i = 0; i < hotIPList.size(); i++){
            String ip = hotIPList.get(i);
            String result = IsEsp(ip);//测试连接，判断该ip是否为行车记录仪
            if(result.contains("this is esp32cam, yandian")){
//                Log.d(TAG, "找到验电esp");
                String[] qianduan = new String[]{ip, "验电"};
                allresult.add(qianduan);
            }
            if(result.contains("this is esp32cam, yuancheng")){
//                Log.d(TAG, "找到远程esp");
                String[] qianduan = new String[]{ip, "远程"};
                allresult.add(qianduan);
            }
        }
        return allresult;
    }
}
