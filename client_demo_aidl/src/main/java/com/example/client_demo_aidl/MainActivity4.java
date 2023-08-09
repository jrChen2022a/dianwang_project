package com.example.client_demo_aidl;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity4 extends AppCompatActivity {
    private static final String TAG = "BluetoothClientActivity";
    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private Handler handler;

    private final static int MESSAGE_READ = 1;

    private EditText editText;
    private Button sendButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MESSAGE_READ:
                        byte[] buffer = (byte[]) message.obj;
                        int bytesRead = message.arg1;
                        String receivedData = new String(buffer, 0, bytesRead);
                        // 处理接收到的数据
                        Toast.makeText(MainActivity4.this, "Received data: " + receivedData, Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });

        editText = findViewById(R.id.edit_text);
        sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editText.getText().toString();
                sendMessage(message);
            }
        });
        // 检查蓝牙状态
        checkBluetoothStatus();
    }
    private final ArrayList<BluetoothDevice> devices = new ArrayList<>(10);

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
            // 蓝牙已启用，开始连接
            connectToDevice();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // 用户已启用蓝牙，开始连接
                connectToDevice();
            } else {
                // 用户未启用蓝牙，关闭Activity
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void connectToDevice() {
        // 获取A手机的BluetoothDevice对象
//        while(devices.isEmpty());
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        BluetoothDevice device = null; // 根据需要获取A手机的蓝牙设备对象
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice dev : pairedDevices) {
                // 处理每个已配对设备，例如通过名称或地址匹配设备
                String deviceName = dev.getName();
                String deviceAddress = dev.getAddress();
                device = dev;
                // 执行相应操作
            }
        } else {
            // 没有已配对的设备
        }


        if (device != null) {
            bluetoothDevice = device;
            connectThread = new ConnectThread(bluetoothDevice);
            connectThread.start();
        } else {
            Toast.makeText(this, "Bluetooth device not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            try {
                // 根据UUID创建一个与A手机的蓝牙设备建立连接的BluetoothSocket对象
                tmp = device.createRfcommSocketToServiceRecord(MainActivity3.MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            socket = tmp;
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();

            try {
                socket.connect();
            } catch (IOException connectException) {
                Log.e(TAG, "Could not connect to the server socket", connectException);
                try {
                    socket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // 连接建立后，在这里进行传输数据的操作
            connectedThread = new ConnectedThread(socket);
            connectedThread.start();
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
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

    private void sendMessage(String message) {
        if (connectedThread != null) {
            connectedThread.write(message.getBytes());
        }
    }

}
