package com.purui.service;

import android.graphics.Bitmap;

import com.purui.service.PuruiResult;

public interface IPuruiService {
    // 服务相关
    /**
     * 创建服务，在onCreate()以及调用接口前使用
     */
    void createService();

    /**
     * 销毁服务，在onDestroy()调用
     */
    void destroyService();

    /**
     * 在onStop()处调用
     */
    void stopService();

    /**
     * 在onResume处调用
     */
    void resumeService();


    // 人脸接口
    /**
     * 获取人脸识别结果
     * @param photo 待识别照片
     * @return PuruiResult:
     *      .isDone(): 当前操作成功与否
     *      .getBitmap(): 识别到的人脸图片
     *      .getDetails(): 识别成功则返回： “XXX” (姓名)
     *                          否则返回： “识别失败，请重试！”
     */
    PuruiResult getFaceResult(Bitmap photo);

    /**
     * 获取人脸库
     * @return PuruiResult[]:人脸信息，同上
     *      .getBitmap(): 人脸图片
     *      .getDetails(): “XXX” (姓名)
     */
    PuruiResult[] checkFaces();

    /**
     * 添加人脸
     * @param name 姓名
     * @param photo 待入库照片
     * @return PuruiResult:
     *      .isDone(): 当前操作成功与否
     *      .getBitmap(): 识别到的人脸图片
     *      .getDetails():  添加成功则返回：“XXX 录入成功”
     *                    失败则有两种情况：1.无存储权限，此时返回： "录入失败，请检查权限"
     *                                   2.人脸库中已存在，此时返回："数据库中已存在该用户！"
     */
    PuruiResult addFace(String name, Bitmap photo);

    /**
     * 删除人脸
     * @param name 姓名
     * @return PuruiResult:
     *      .isDone(): 当前操作成功与否
     *      .getDetails(): 删除成功返回："XXX 删除成功！"
     *                       失败则返回："XXX 删除失败！"
     */
    PuruiResult deleteFace(String name);

    // 摄像头接口
    /**
     * 扫描 操作杆和验电器
     */
    void scanCameras();

    /**
     * 选择摄像头
     * @param cameraType 摄像头类型： 关闭、平板、操作杆、验电器
     *      .isDone(): 当前操作成功与否
     *      .getDetails(): 打开 操作杆/验电器 成功时返回：“摄像头打开成功”
     *                                      失败时返回：“摄像头连接失败”
     *                              关闭摄像头成功时返回：”关闭成功“
     */
    PuruiResult selectCamera(String cameraType);

    // 状态检测接口
    /**
     * 状态检测接口，在iPuruiCallback中onPostDetectStates接口处返回检测结果
     * @param switchType 开关类型：跌落保险、刀闸（隔离开关）、开关（断路器）
     * @param targetState 开关状态：合上、拉开、取下、给上
     */
    void preDetectStates(String switchType, String targetState, DetectStatesListener listener);

    /**
     * 同上，需要在使用前确保平板已开启
     * @param inBitmap 输入待检测图片
     */
    void preDetectStates(Bitmap inBitmap, String switchType, String targetState, DetectStatesListener listener);

    // 杆塔ID识别接口
    /**
     * 杆塔ID识别接口，在iPuruiCallback中onPostRecognizeID接口处返回识别结果
     * @param inID 需验证的设备ID
     */
    void preRecognizeID(String inID, RecognizeIDListener listener);

    /**
     * 同上，需要在使用前确保平板已开启
     * @param inBitmap 输入待识别图片
     */
    void preRecognizeID(Bitmap inBitmap, String inID, RecognizeIDListener listener);

    // 解闭锁接口
    /**
     * 操作设备解锁接口
     * @return PuruiResult:
     *      .isDone(): 当前操作成功与否
     *      .getDetails(): 成功则返回："设备已解锁"
     *                     失败则返回："设备解锁失败"
     */
    PuruiResult unlockDevice();

    /**
     * 操作设备闭锁接口
     * @return PuruiResult:
     *      .isDone(): 当前操作成功与否
     *      .getDetails(): 成功则返回："设备已闭锁"
     *                     失败则返回："设备闭锁失败"
     */
    PuruiResult lockDevice();

    // 验电接口，分两步完成。第一步：确认是否到达验电区域；第二步：测试验电相是否带电
    /**
     * 第一步
     * @param selectPhase 当前选择相
     * @return PuruiResult:
     *      .isDone(): 是否到达验电处
     *      .getBitmap(): 拍照图片返回
     *      .getDetails(): 到达则返回："可以执行验电操作"
     *                     否则返回：  "不可以执行验电操作"
     */
    void whetherToTestElectro(char selectPhase, TestElectricityCallback cb);
    interface TestElectricityCallback{
        void onContacted(PuruiResult res);
        void onFail(PuruiResult res);
    }

    /**
     * 第二步
     * @return PuruiResult:
     *      .isDone(): 是否成功验证带电信息
     *      .getDetails():  成功验证则返回："X相带电/不带电" (X=A/B/C)
     *                      若测满三相带电信息会返回：
     *                      "
     *                         X相带电/不带电
     *                         验电完毕
     *                         结果：带电/不带电
     *                      "
     *                      否则返回："X相验电超时，请重试"
     */
    PuruiResult testElectricity();

    /**
     * 重置接口
     */
    void resetAll();
}