package com.purui.service.result;

/**
 *      success: 当前操作成功与否
 *      details: 成功则返回："设备已解锁/闭锁"
 *               失败则返回："设备解锁/闭锁失败"
 */
public class LockResult {
    private boolean success;
    private String detail;

    public LockResult(boolean success, String details) {
        this.success = success;
        this.detail = details;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getDetails() {
        return detail;
    }
}
