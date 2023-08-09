package com.example.client_demo_aidl;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity3 extends AppCompatActivity {
    private static final String TAG = "BluetoothServerActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION = 2;

    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String PERMISSION_BLUETOOTH = Manifest.permission.BLUETOOTH;
    private static final String PERMISSION_BLUETOOTH_ADMIN = Manifest.permission.BLUETOOTH_ADMIN;

    private BluetoothAdapter bluetoothAdapter;
    private AcceptThread acceptThread;
    private ConnectedThread connectedThread;
    private Handler handler;

    private final static int MESSAGE_READ = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                switch (message.what) {
                    case MESSAGE_READ:
                        byte[] buffer = (byte[]) message.obj;
                        int bytesRead = message.arg1;
                        String receivedData = new String(buffer, 0, bytesRead);
                        // 在这里处理接收到的数据
                        Toast.makeText(MainActivity3.this, "Received data: " + receivedData, Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });

        // 检查蓝牙权限
        if (checkBluetoothPermissions()) {
            // 检查蓝牙状态
            checkBluetoothStatus();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (acceptThread != null) {
            acceptThread.cancel();
        }
        if (connectedThread != null) {
            connectedThread.cancel();
        }
    }

    private boolean checkBluetoothPermissions() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, PERMISSION_BLUETOOTH);
        int adminPermissionCheck = ContextCompat.checkSelfPermission(this, PERMISSION_BLUETOOTH_ADMIN);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED ||
                adminPermissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{PERMISSION_BLUETOOTH, PERMISSION_BLUETOOTH_ADMIN},
                    REQUEST_PERMISSION);
            return false;
        }
        return true;
    }

    private void checkBluetoothStatus() {
        if (bluetoothAdapter == null) {
            // 设备不支持蓝牙
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        } else if (!bluetoothAdapter.isEnabled()) {
            // 蓝牙未启用，请求用户启用蓝牙
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            // 蓝牙已启用，开始监听连接
            startServer();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // 用户已启用蓝牙，开始监听连接
                startServer();
            } else {
                // 用户未启用蓝牙，关闭Activity
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void startServer() {
        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("MyApp", MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            serverSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }
                if (socket != null) {
                    // 在这里处理连接建立后的操作，如传输数据
                    connectedThread = new ConnectedThread(socket);
                    connectedThread.start();
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Could not close the server socket", e);
                    }
                    break;
                }
            }
        }

        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the server socket", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating I/O streams", e);
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytesRead;

            while (true) {
                try {
                    bytesRead = inputStream.read(buffer);
                    if (bytesRead > 0) {
                        // 将读取到的数据发送给主线程处理
                        handler.obtainMessage(MESSAGE_READ, bytesRead, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error occurred when reading from input stream", e);
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                outputStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
            }
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}
