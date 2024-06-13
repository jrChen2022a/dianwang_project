package com.purui.service.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.SoftReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utils {

    public static double calculateJaccardSimilarity(String str1, String str2) {
        Set<Character> set1 = new HashSet<>();
        Set<Character> set2 = new HashSet<>();

        for (char c : str1.toCharArray()) {
            set1.add(c);
        }

        for (char c : str2.toCharArray()) {
            set2.add(c);
        }

        Set<Character> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<Character> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }
    public static String getBase64ImgCode(Bitmap bitmap){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return getBase64ImgCode(byteArray);
    }
    public static String getBase64ImgCode(byte[] byteArray){
        return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
    public static void checkButtonInViewGroup(ViewGroup father, String cameraType) {
        int childCount = father.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = father.getChildAt(i);
            if (view instanceof RadioButton) {
                RadioButton radioButton = (RadioButton) view;
                radioButton.setChecked(radioButton.getText().equals(cameraType));
            }else if(view instanceof ViewGroup){
                checkButtonInViewGroup((ViewGroup) view,cameraType);
            }
        }
    }
    public static List<String> splitByParentheses(String str) {
        char labelStart = '（';
        char labelEnd = '）';
        List<String> result = new ArrayList<>();
        int start, end = 0;
        while (end < str.length()) {
            if (str.charAt(end) == labelStart) {
                end++;
                start = end;
                while (end < str.length()) { // && count > 0
                    if (str.charAt(end) == labelEnd) {
//                        count++;
                        result.add(str.substring(start, end));
                        break;
                    }
                    end++;
                }
            } else {
                end++;
            }
        }
        return result;
    }
    public static boolean isAlphanumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }
    public static String removeSpecialChars(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (Character.isLetterOrDigit(c) || isChinese(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A;
    }
    public static Bitmap resizeBitmap(Bitmap bm, ImageView iv){
        if(bm == null){
            return null;
        }
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 设置想要的大小
        int newWidth = iv.getWidth();
        int newHeight = iv.getHeight();
        // 计算缩放比例
        float scale;
        if(width>height){
            scale = ((float) newWidth) / width;
        }else {
            scale = ((float) newHeight) / height;
        }

        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        // 得到新的图片
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
                true);
    }
    public static Bitmap rotate(Bitmap b, float degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) b.getWidth() / 2,
                    (float) b.getHeight() / 2);
            Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
                    b.getHeight(), m, true);
            if (b != b2) {
                b.recycle();
                b = b2;
            }
        }
        return b;
    }

    public static Bitmap bytes2bitmap(byte[] imgBytes, Bitmap.Config preferredConfig, int sampleSize) {
        InputStream input;
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        options.inPreferredConfig = preferredConfig; //Bitmap.Config.ARGB_4444
        input = new ByteArrayInputStream(imgBytes);
        SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(
                input, null, options));
        bitmap = (Bitmap) softRef.get();
        try {
            input.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bitmap;
    }
    public static byte[] bitmap2bytes(Bitmap bitmap){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return outputStream.toByteArray();
    }

    public static String getTimeFormat() {
        //获取系统的日期
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        long timeStamp = calendar.getTimeInMillis();
        return year + "_" + month + "_" + day + "_" +hour + "_" + minute + "_" + second + "_" + timeStamp;
    }

    public static boolean isHarmonyOs(Context context) {
        try {
            int id = Resources.getSystem().getIdentifier("config_os_brand", "string", "android");
            return context.getString(id).equals("harmony");
        } catch (Exception e) {
            return false;
        }
    }

    //将输入框中的外部设备写入txt文件中
    public static void writeTXT(String[] content, File file) throws IOException {
        FileOutputStream fos=new FileOutputStream(file);
        OutputStreamWriter writer=new OutputStreamWriter(fos, StandardCharsets.UTF_8);
        for(String str:content){
            writer.write("\n"+str+"\n");
        }
        writer.close();
        fos.close();
    }
}
