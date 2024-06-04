package com.purui.service;

import android.graphics.Bitmap;

public interface IInnerService extends IPuruiService{
    // 解闭锁接口
    void postRecognizeID(Bitmap draw);

    /**
     * 操作设备解锁接口
     * @return PuruiResult:
     *      .isDone(): 当前操作成功与否
     *      .getDetails(): 成功则返回："设备已解锁"
     *                     失败则返回："设备解锁失败"
     */
    PuruiResult whetherToTestElectro(char selectPhase);

    void postDetectStatesAfterDraw(Bitmap bm);
}