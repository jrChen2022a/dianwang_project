package com.purui.service.result;

import com.purui.service.Utils.Utils;

public class OcrResultJSON {
    private final boolean checked;
    private final String id;
    private final String base64Image;

    public OcrResultJSON(OcrResult or) {
        this.checked = or.isSuccess();
        this.id = or.getId();
        this.base64Image = or.getBitmap()==null?"NULL":Utils.getBase64ImgCode(or.getBitmap());
    }
}
