package com.purui.service.result;

import com.purui.service.Utils.Utils;

public class ElectricalResultJSON {
    private final boolean success;
    private final String currentPhase;
    private final String currentPhaseResult;
    private final String totalResult;
    private final String details;
    private final String logImgInBase64;
    public ElectricalResultJSON(ElectricalResult er){
        this.success = er.isSuccess();
        this.currentPhase = er.getCurrentPhase();
        this.currentPhaseResult = er.getCurrentPhaseResult();
        this.totalResult = er.getTotalResult();
        this.logImgInBase64 = er.getLogImg()==null?"NULL":Utils.getBase64ImgCode(er.getLogImg());
        this.details = er.getDetails();
    }
}
