package com.purui.service;

import android.graphics.Bitmap;

import androidx.annotation.WorkerThread;
import com.purui.service.result.CamSelectResult;
import com.purui.service.result.ElectricalResult;
import com.purui.service.result.FaceResult;
import com.purui.service.result.LockResult;
import com.purui.service.result.OcrResult;
import com.purui.service.result.StateResult;

public interface IPuruiService {
    // 服务相关
    /**
     * 创建服务，在onCreate()以及调用接口前使用
     */
    void createService(ServiceConnectionListener listener);

    /**
     * 销毁服务，在onDestroy()调用
     */
    void destroyService();


    // 人脸接口
    /**
     * 获取人脸识别结果
     * @param photo 待识别照片
     */
    FaceResult getFaceResult(Bitmap photo);

    /**
     * 获取人脸库
     */
    FaceResult[] checkFaces();

    /**
     * 添加人脸
     * @param name 姓名
     * @param photo 待入库照片
     */
    FaceResult addFace(String name, Bitmap photo);

    /**
     * 删除人脸
     * @param name 姓名
     */
    FaceResult deleteFace(String name);

    // 摄像头接口
    /**
     * 扫描 操作杆和验电器
     * 耗时操作，需开启线程
     * @return 扫描结果：扫描到的设备及其IP地址信息或扫描失效信息
     */
    @WorkerThread
    String scanCameras();

    /**
     * 选择摄像头
     * @param cameraType 摄像头类型： 关闭、平板、操作杆、验电器
     * @param listener 连接摄像头后的回调（如实时传图片）
     */
    CamSelectResult selectCamera(String cameraType, CameraShowListener listener);

    /**
     * 获取摄像头图像
     * @return 摄像头当前帧
     */
    Bitmap getCamFrame();

    /**
     * 状态检测接口
     * @param inBitmap 输入待检测图片
     * @param switchType 开关类型：跌落保险、刀闸（隔离开关）、开关（断路器）
     * @param targetState 开关状态：合上、拉开、取下、给上
     */
    StateResult detectStates(Bitmap inBitmap, String switchType, String targetState);

    /**
     * 状态检测接口
     * @param inBitmap 输入待识别图片
     * @param inID 需验证的设备ID
     */
    OcrResult recognizeID(Bitmap inBitmap, String inID);

    // 解闭锁接口
    /**
     * 操作设备解锁接口
     */
    LockResult unlockDevice();

    /**
     * 操作设备闭锁接口
     */
    LockResult lockDevice();

    /**
     * 验电接口，分两步完成。第一步：确认是否到达验电区域；第二步：测试验电相是否带电
     * @param selectPhase 当前验电相
     */
     ElectricalResult testElectricity(char selectPhase);

    /**
     * 重置接口
     */
    void resetAll();

    /**
     * 这个是内部app用来录制数据集用的
     * 录像接口，分两次使用，当没有录象时使用会开始录像，已经录像时会停止录像
     * @param listener 录像时回调函数
     */
    void recordVideo(RecordListener listener);


    /**
     * Service连接回调
     */
    interface ServiceConnectionListener {
        /**
         * Service已启动
         */
        void onConnected();

        /**
         * Service已断开
         */
        void onDisconnected();
    }

    /**
     * 连接摄像头的回调
     */
    interface CameraShowListener {
        /**
         * 实时回调Bitmap图像
         */
        void onCamShow(Bitmap bitmap);

        /**
         * 将Bitmap转成二进制数组回调
         */
        void onCamShow(byte[] bitmapbytes);
    }

    /**
     * 录像回调
     */
    interface RecordListener {
        /**
         * 触发录像
         * @param state 录像状态（已开始）
         */
        void onStartRecord(String state);

        /**
         * 显示录像动画（弹窗显示）
         * @param bitmap 录像的实时过程
         */
        void onRecording(Bitmap bitmap);

        /**
         * 触发录像终止
         * @param state 录像状态（已完成）
         */
        void onEndRecord(String state);

        /**
         * 录像功能失败
         * @param reason 失败原因
         */
        void onFail(String reason);
    }

}