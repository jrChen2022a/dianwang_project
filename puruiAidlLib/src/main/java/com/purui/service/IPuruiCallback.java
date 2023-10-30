package com.purui.service;

import android.graphics.Bitmap;
import android.view.TextureView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 在回调接口中进行必要的UI操作
 */
public interface IPuruiCallback {
    // 获取对象
    /**
     * 获取调用IPuruiService接口的Activity对象
     */
    AppCompatActivity getActivity();

    /**
     * 获取用于摄像头实时显示的TextureView对象
     */
    TextureView getTextureView();

    // 回调UI
    /**
     * 设置相机关闭的显示UI
     */
    void setDefaultCamUI();

    /**
     * 操作实时画面UI，播放实时摄像画面
     * @param bitmap 实时回传图片
     */
    void setCamUI(Bitmap bitmap);

    /**
     * 操作检测画面的UI，显示检测结果
     * @param bitmap 检验结果的回传图片
     */
    void setDetUI(Bitmap bitmap);

    /**
     * 操作摄像头设备选择UI，在连接摄像头失败时或使用接口设置摄像头时回调
     * @param index 摄像头设备：
     *         0--关闭
     *         1--平板
     *         2--操作杆
     *         3--验电器
     */
    void setCamSelectUI(int index);

    /**
     * 文字识别的UI设置
     * @param recoText 识别到的文字
     */
    void setIDRecognitionUI(String recoText);

    /**
     * 状态检测的UI设置
     * @param state 开关总体状态，分别为：0--拉开、1--合上、2--给上、3--取下、4--无效
     * @param stateA A相状态，同上
     * @param stateB B相状态，同上
     * @param stateC C相状态，同上
     */
    void setStatesDetectionUI(int state, int stateA, int stateB, int stateC);

    /**
     * 操作验电UI，显示验电结果
     * @param phase 对应相：
     *             0--A相
     *             1--B相
     *             2--C相
     * @param eleState 验电结果：
     *             0--无电
     *             1--有电
     *             2--无效
     */
    void setEleTestUI(int phase, int eleState);

    /**
     * 操作解锁闭锁UI，显示解闭锁状态
     * @param state 解闭锁状态：
     *            0--解锁
     *            1--闭锁
     */
    void setLockStateUI(int state);

    /**
     *  重置所有相关UI
     */
    void resetUI();

    /**
     *  内部测试接口，请跳过
     */
    void setVideoBtnText(String text);
}
