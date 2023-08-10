package com.purui.service.result;

import com.purui.service.Utils.Utils;


/**
 * 开关总体状态，分别为：0--拉开、1--合上、2--给上、3--取下、4--无效 stateA – A相状态，同上 stateB – B相状态，同上 stateC – C相状态，同上
 */
public class StateResultJSON {
    private final boolean success;
    private final String detail;
    private final int state;
    private final int stateA;
    private final int stateB;
    private final int stateC;
    private final String logImg;
    private static final String helper =
            " * success: 是否识别成功 \n"+
                    " * detail: 识别结果 \n"+
            " * state: 开关总体状态，序号分别对应：\n" +
            " *    0--拉开\n" +
                    "      1--合上\n      2--给上\n      3--取下\n      4--无效\n" +
            " * stateA:  A相状态，同上\n" +
            " * stateB:  B相状态，同上\n" +
            " * stateC:  C相状态，同上\n" +
            " * logImg:  识别结果：即带框图像(base64码)";

    public StateResultJSON(StateResult sr) {
        this.success = sr.isSuccess();
        this.detail = sr.getDetails();
        this.state = sr.getState();
        this.stateA = sr.getStateA();
        this.stateB = sr.getStateB();
        this.stateC = sr.getStateC();
        this.logImg = sr.getBitmap()==null?"NULL":Utils.getBase64ImgCode(sr.getBitmap());
    }
}
