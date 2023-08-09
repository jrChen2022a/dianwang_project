package com.purui.service.result;

import com.purui.service.Utils.Utils;


/**
 * 开关总体状态，分别为：0--拉开、1--合上、2--给上、3--取下、4--无效 stateA – A相状态，同上 stateB – B相状态，同上 stateC – C相状态，同上
 */
public class StateResultJSON {
    private final boolean success;
    private final String details;
    private final int state;
    private final int stateA;
    private final int stateB;
    private final int stateC;
    private final String base64Image;

    public StateResultJSON(StateResult sr) {
        this.success = sr.isSuccess();
        this.details = sr.getDetails();
        this.state = sr.getState();
        this.stateA = sr.getStateA();
        this.stateB = sr.getStateB();
        this.stateC = sr.getStateC();
        this.base64Image = sr.getBitmap()==null?"NULL":Utils.getBase64ImgCode(sr.getBitmap());
    }
}
