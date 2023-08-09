package com.purui.service.result;

import android.graphics.Bitmap;

public class PuruiResult {
    private final boolean checked;
    private final String details;
    private Bitmap retBitmap;

    public Bitmap getBitmap(){return retBitmap;}

    public String getDetails() {
        return details;
    }

    public boolean isDone() {
        return checked;
    }

    public PuruiResult(boolean ckd, String more){
        this.checked = ckd;
        this.details = more;
    }
    public PuruiResult(boolean ckd, String more, Bitmap bm){
        this.checked = ckd;
        this.details = more;
        this.retBitmap = bm;
    }
}
