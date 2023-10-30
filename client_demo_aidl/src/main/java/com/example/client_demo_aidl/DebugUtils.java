package com.example.client_demo_aidl;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DebugUtils {
    private static final String TAG = "DebugLog";

    public static void writeDebugLog(Context context, String logMessage) {
        try {
            String logFileName = "debug_log.txt";
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String logLine = timeStamp + " : " + logMessage;

            BufferedWriter writer = new BufferedWriter(new FileWriter(context.getExternalFilesDir(null) + "/" + logFileName, true));
            writer.write(logLine);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, "Error writing to debug log file: " + e.getMessage());
        }
    }
}
