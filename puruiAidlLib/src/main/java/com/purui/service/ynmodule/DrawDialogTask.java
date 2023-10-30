package com.purui.service.ynmodule;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import androidx.fragment.app.FragmentManager;

import com.purui.service.IInnerService;

public class DrawDialogTask extends AsyncTask<Void, Void, String> {
    private final FragmentManager fragmentManager;
    private final DrawDialogFragment dialog;
    private final Bitmap bm;
    private final IInnerService iPS;
    private final int mode;
    public DrawDialogTask(FragmentManager fragmentManager, DrawDialogFragment dialog, Bitmap bm, IInnerService iPS, int mode) {
        this.fragmentManager = fragmentManager;
        this.dialog = dialog;
        this.bm = bm;
        this.iPS = iPS;
        this.mode = mode;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            return dialog.showDialogAndWaitForResult(fragmentManager, bm, iPS, mode);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
