package com.purui.service.result;

import com.purui.service.Utils.Utils;

public class OcrResultJSON {
    private final boolean success;
    private final String id;
    private final String detail;
    private final String logImg;
    private static final String helper = " * success: 是否识别成功\n" +
            " * id: 识别出的文字\n" +
            " * details: 识别结果\n" +
            " * logImg: 识别图像反馈(base64码)";

    public OcrResultJSON(OcrResult or) {
        this.success = or.isSuccess();
        this.detail = success?"识别成功：":"识别失败："+or.getId();
        this.id = or.getId();
        this.logImg = or.getBitmap()==null?"NULL":Utils.getBase64ImgCode(or.getBitmap());
    }
}
