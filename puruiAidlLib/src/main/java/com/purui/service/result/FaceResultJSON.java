package com.purui.service.result;
import com.purui.service.Utils.Utils;

public class FaceResultJSON {
        private final boolean success;
        private final String name;
        private final String logImg;
        private static final String helper =
                " * success: 识别/获取操作是否成功\n" +
                        " * name: 识别/获取的人脸对应人名\n" +
                        " * face: 识别/获取到的人脸";
        public FaceResultJSON(FaceResult fr) {
            this.success = fr.isSuccess();
            this.name = fr.getName();
            this.logImg = fr.getFace()==null?"NULL": Utils.getBase64ImgCode(fr.getFace());
        }


}
