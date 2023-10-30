package com.purui.service.result;
import com.purui.service.Utils.Utils;

public class FaceResultJSON {
        private final boolean success;
        private final String name;
        private final String faceInBase64Img;

        public FaceResultJSON(FaceResult fr) {
            this.success = fr.isSuccess();
            this.name = fr.getName();
            this.faceInBase64Img = fr.getFace()==null?"NULL": Utils.getBase64ImgCode(fr.getFace());
        }


}
