package com.purui.service.espmodule;


import com.purui.service.result.PuruiResult;

public interface IEspHandle {
    String scanEsp();
    PuruiResult getEspReady(int CAMID);
    boolean setCamera(int id, boolean show_cam);
    void releaseCamera();

    /**
     *
     * @param pulseCMD
     */
    void sendPulse(String pulseCMD);

    String startRecord();
    String endRecord();
}
