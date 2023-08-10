package com.purui.service.result;

import com.purui.service.Utils.Utils;

public class ElectricalResultJSON {
    private final boolean success;
    private final String currentPhase;
    private final String currentPhaseResult;
//    private final String totalResult;
    private final String detail;
    private final String logImg;
    private static final String helper =
            " * success: 当前操作是否成功\n" +
            " * currentPhase: 当前选择相：A/B/C\n" +
            " * currentPhaseResult: 验电结果：带电/不带电/无效\n" +
            " * totalResult: 验满三相之后的验电结果：带电/不带电/无效\n" +
            " * details: 验电细节：\n" +
            "           1. 到达验电处则返回：\"可以执行验电操作\"，否则返回：\"不可以执行验电操作\"\n" +
            "           2. 成功验电则返回：\"X相带电/不带电\" (X=A/B/C)\n" +
            "                         若测满三相带电信息会返回：\n" +
            "                         \"\n" +
            "                            X相带电/不带电\n" +
            "                            验电完毕\n" +
            "                            结果：带电/不带电\n" +
            "                         \"\n" +
            "                         否则返回：\"X相验电超时，请重试\"\n" +
            " *  logImg: 记录验电时所拍摄图像，包括验电环的识别(base64码)";
    public ElectricalResultJSON(ElectricalResult er){
        this.success = er.isSuccess();
        this.currentPhase = er.getCurrentPhase();
        this.currentPhaseResult = er.getCurrentPhaseResult();
//        this.totalResult = er.getTotalResult();
        this.logImg = er.getLogImg()==null?"NULL":Utils.getBase64ImgCode(er.getLogImg());
        this.detail = er.getDetails();
    }
}
