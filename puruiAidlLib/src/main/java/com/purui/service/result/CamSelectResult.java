package com.purui.service.result;

/**
 * success: 当前切换摄像头操作是否成功
 * selectCam: 关闭/平板/操作杆/验电器
 * selectCamId: 0/1/2/3
 * details:   打开 操作杆/验电器 成功时返回：“摄像头打开成功”
 *                            失败时返回：“摄像头连接失败”
 *                            关闭摄像头成功时返回：”关闭成功“
 */
public class CamSelectResult {
    private final boolean success;
    private final String selectCam;
    private final int selectCamId;
    private final String details;

    public CamSelectResult(boolean success, String selectCam, int selectCamId, String details) {
        this.success = success;
        this.selectCam = selectCam;
        this.selectCamId = selectCamId;
        this.details = details;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getSelectCamId() {
        return selectCamId;
    }
}
