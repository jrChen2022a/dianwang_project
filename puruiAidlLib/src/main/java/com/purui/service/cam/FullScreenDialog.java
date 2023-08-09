//package com.purui.service.cam;
//
//import android.app.Activity;
//import android.app.Dialog;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Point;
//import android.os.Bundle;
//import android.view.Display;
//import android.view.MotionEvent;
//import android.view.TextureView;
//import android.view.View;
//import android.view.WindowManager;
//import androidx.annotation.NonNull;
//
//import com.purui.service.PuruiServiceManager;
//import com.purui.service.R;
//
//public class FullScreenDialog extends Dialog implements ICamCallback {
//    private int cameraType = 1;
//    private final Activity mActivity;
//    private float mOldDistance;
//    private PuruiServiceManager PSM;
//    private NativeCamManager CM = null;
//    private boolean takePhoto = false;
//    public FullScreenDialog(@NonNull Context context) {
//        super(context);
//        this.mActivity = (Activity) context;
//    }
//    public void setiPS(PuruiServiceManager PSM) {
//        this.PSM = PSM;
//    }
//    public void setCameraType(int cameraType) {
//        this.cameraType = cameraType;
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.fullscreendisplay);
//        //自定义Dialog宽度
//        WindowManager m = getWindow().getWindowManager();
//        Display d = m.getDefaultDisplay();
//        WindowManager.LayoutParams p = getWindow().getAttributes();
//        Point size = new Point();
//        d.getSize(size);
//        p.width = (int) (size.x);        //设置为屏幕的0.7倍宽度
//        p.height = (int) (size.y);
//        getWindow().setAttributes(p);
//        findViewById(R.id.iv_close).setOnClickListener(
//                v -> {
//                    if (CM != null) {
//                        CM.closeCam();
//                        CM = null;
//                    }
//                    PSM.setFullDisplayBack();
//                    this.dismiss();
//                });
//        findViewById(R.id.btn_photo).setOnClickListener(v -> takePhoto = true);
//
//        TextureView tuv = findViewById(R.id.tuv_cam);
//        tuv.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getPointerCount() == 1) {
////                    CM.triggerFocusAtPoint((int) event.getX(), (int) event.getY(), tuv.getWidth(), tuv.getHeight());
//                    return true;
//                }
//                switch (event.getAction() & MotionEvent.ACTION_MASK) {
//                    case MotionEvent.ACTION_POINTER_DOWN:
//                        mOldDistance = getFingerSpacing(event);
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        float newDistance = getFingerSpacing(event);
//                        if (newDistance > mOldDistance) {
////                            CM.handleZoom(true);
//                        } else if (newDistance < mOldDistance) {
////                            CM.handleZoom(false);
//                        }
//                        mOldDistance = newDistance;
//                        break;
//                    default:
//                        break;
//                }
//                return true;
//            }
//        });
//
//        if(cameraType == 1){
//            if(CM == null){
//                CM = new NativeCamManager(this, mActivity);
//                CM.openCam(true);
//            }
//        }
//
//    }
//    private static float getFingerSpacing(MotionEvent event) {
//        float x = event.getX(0) - event.getX(1);
//        float y = event.getY(0) - event.getY(1);
//        return (float) Math.sqrt(x * x + y * y);
//    }
//
//    @Override
//    public void setBitmap(Bitmap bitmap) {
//        if(takePhoto){
//            PSM.setBitmap(bitmap);
//            if(CM != null){
//                CM.closeCam();
//                CM = null;
//            }
//            this.dismiss();
//        }
//    }
//}
