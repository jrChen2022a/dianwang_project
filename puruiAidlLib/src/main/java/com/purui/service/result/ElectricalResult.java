package com.purui.service.result;

import android.graphics.Bitmap;

/**
 * success: 当前操作是否成功
 * currentPhase: 当前选择相：A/B/C
 * currentPhaseResult: 验电结果：带电/不带电/无效
 * totalResult: 验满三相之后的验电结果：带电/不带电/无效
 * details: 验电细节：
 *          1. 到达验电处则返回："可以执行验电操作"，否则返回："不可以执行验电操作"
 *          2. 成功验电则返回："X相带电/不带电" (X=A/B/C)
 *  *                      若测满三相带电信息会返回：
 *  *                      "
 *  *                         X相带电/不带电
 *  *                         验电完毕
 *  *                         结果：带电/不带电
 *  *                      "
 *  *                      否则返回："X相验电超时，请重试"
 *  logImg: 记录验电时所拍摄图像，包括验电环的识别
 */
public class ElectricalResult {
    private final boolean success;
    private final String currentPhase;
    private final String currentPhaseResult;
    private final String totalResult;
    private final String details;
    private final Bitmap logImg;

    public ElectricalResult(boolean success, String currentPhase, String currentPhaseResult, String totalResult, String details, Bitmap logImg) {
        this.success = success;
        this.currentPhase = currentPhase;
        this.currentPhaseResult = currentPhaseResult;
        this.totalResult = totalResult;
        this.details = details;
        this.logImg = logImg;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getCurrentPhase() {
        return currentPhase;
    }

    public String getCurrentPhaseResult() {
        return currentPhaseResult;
    }

    public String getTotalResult() {
        return totalResult;
    }

    public String getDetails() {
        return details;
    }

    public Bitmap getLogImg() {
        return logImg;
    }
}
