package com.purui.service.cam;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.purui.service.Utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;

public class NativeCamManager {
    private static final String TAG = "CAMERA";
    private final Context ctx;
    private final ICamCallback iCamCallback;

    private android.hardware.camera2.CameraManager cameraManager;
//    private CameraCharacteristics mCameraCharacteristics;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;
//    private Handler mBackgroundHandler;
    private int mCameraId;
    private ImageReader mImageReader;
//    private int mOri;

    public NativeCamManager(ICamCallback iCamCallback, Context ctx){
        this.ctx = ctx;
        this.iCamCallback = iCamCallback;
        init();
    }

    public void closeCam() {
        // 关闭摄像头设备
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

//        // 清除BackgroundHandler中的消息
//        if (mBackgroundHandler != null) {
//            mBackgroundHandler.removeCallbacksAndMessages(null);
//            mBackgroundHandler = null;
//        }

        // 关闭会话
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }

        // 关闭图像读取器
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
    }


    private void requestCameraPermission(){
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            iCamCallback.requestCameraPermission();
        }
    }

    private void init() {
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        } else {
            cameraManager = (android.hardware.camera2.CameraManager) ctx.getSystemService(Context.CAMERA_SERVICE);
        }
    }

    public void openCam(boolean frontCam){
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        } else {
            try {
                //根据屏幕的显示方向调整预览窗口大小
//                Configuration mConfiguration = ctx.getResources().getConfiguration(); //获取设置的配置信息
//                mOri = mConfiguration.orientation; //获取屏幕方向
//                if (mOri == Configuration.ORIENTATION_LANDSCAPE) {
//                    //横屏
//                    mTextureView.setRotation(-90);
//                }
//                Thread.sleep(1000);
                int cameraId_BACK = CameraCharacteristics.LENS_FACING_BACK;
                int cameraId_FRONT = CameraCharacteristics.LENS_FACING_FRONT;
                mCameraId = frontCam? cameraId_FRONT : cameraId_BACK;
                cameraManager.openCamera(String.valueOf(mCameraId), mStateCallback, null);
//                String[] cameraIdList = cameraManager.getCameraIdList();
//                mCameraCharacteristics = cameraManager.getCameraCharacteristics(String.valueOf(mCameraId));
//                initZoomParameter();
//                initDisplayRotation();
            } catch (CameraAccessException e) {
                e.printStackTrace();
                Log.d(TAG, "相机访问异常");
            }
        }

    }
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
//            SurfaceTexture texture = mTextureView.getSurfaceTexture();
//            texture.setDefaultBufferSize(mTextureView.getWidth(), mTextureView.getHeight());
//            Surface previewSurface = new Surface(texture);
            try {
                mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mImageReader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 2);//w320;h240
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, null);
//                mPreviewBuilder.addTarget(previewSurface);
                mPreviewBuilder.addTarget(mImageReader.getSurface());
                // 设置连续自动对焦
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                // 设置自动曝光
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                // 设置自动白平衡
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
                //            mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                //            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE,CameraMetadata.CONTROL_AE_MODE_ON);
//                mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()), previewSessionStateCallback, mBackgroundHandler);
                mCameraDevice.createCaptureSession(Collections.singletonList(mImageReader.getSurface()), previewSessionStateCallback, null);

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
        }
    };

    private final CameraCaptureSession.StateCallback previewSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
            mPreviewSession = cameraCaptureSession;
            try {
                mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
        }
    };

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            try{
                Image image = imageReader.acquireNextImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
//                Bitmap bitmap = Utils.byteToBitmap(bytes, Bitmap.Config.ARGB_4444,1);
//                if (mOri == Configuration.ORIENTATION_PORTRAIT) {
//                    //shu ping
//                    bitmap = Utils.rotate(bitmap, 90);
//                }
//                iCamCallback.setCamPhoto(bitmap);
                iCamCallback.setCamPhotoBytes(bytes);
                image.close();

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    };

    public void onCamZoomChanged(float zoomLevel) {
        try{
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(mCameraDevice.getId());
            Rect activeRect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            float maximumZoomLevel = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) * 10;
            int minW = (int) (activeRect.width() / maximumZoomLevel);
            int minH = (int) (activeRect.height() / maximumZoomLevel);
            int difW = activeRect.width() - minW;
            int difH = activeRect.height() - minH;
            int cropW = difW / 100 * (int) zoomLevel;
            int cropH = difH / 100 * (int) zoomLevel;
            Rect zoom = new Rect(cropW, cropH, activeRect.width() - cropW, activeRect.height() - cropH);
            mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    //Rotate Bitmap
//    private int mZoom = 0; // 0~mMaxZoom之间变化
//    private float mStepWidth; // 每次改变的宽度大小
//    private float mStepHeight; // 每次改变的高度大小
//    private final int MAX_ZOOM = 200;
//    public void handleZoom(boolean isZoomIn) {
//        if (mCameraDevice == null || mCameraCharacteristics == null || mPreviewBuilder == null) {
//            return;
//        }
//        if (isZoomIn && mZoom < MAX_ZOOM) { // 放大
//            mZoom++;
//        } else if (mZoom > 0) { // 缩小
//            mZoom--;
//        }
//        Log.v(TAG, "handleZoom: mZoom: " + mZoom);
//        Rect rect = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
//        int cropW = (int) (mStepWidth * mZoom);
//        int cropH = (int) (mStepHeight * mZoom);
//        Rect zoomRect = new Rect(rect.left + cropW, rect.top + cropH, rect.right - cropW, rect.bottom - cropH);
//        Log.d(TAG, "zoomRect: " + zoomRect);
//        mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomRect);
//        try {
//            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }

//    private void initZoomParameter() {
//        Rect rect = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
////        Log.d(TAG, "sensor_info_active_array_size: " + rect);
//        // max_digital_zoom 表示 active_rect 除以 crop_rect 的最大值
//        float max_digital_zoom = mCameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
////        Log.d(TAG, "max_digital_zoom: " + max_digital_zoom);
//        // crop_rect的最小宽高
//        float minWidth = rect.width() / max_digital_zoom;
//        float minHeight = rect.height() / max_digital_zoom;
//        // 因为缩放时两边都要变化，所以要除以2
//        mStepWidth = (rect.width() - minWidth) / MAX_ZOOM / 2;
//        mStepHeight = (rect.height() - minHeight) / MAX_ZOOM / 2;
//    }

//    private void initDisplayRotation() {
//        int displayRotation = ctx.getWindowManager().getDefaultDisplay().getRotation();
//        switch (displayRotation) {
//            case Surface.ROTATION_0:
//                displayRotation = 90;
//                break;
//            case Surface.ROTATION_90:
//                displayRotation = 0;
//                break;
//            case Surface.ROTATION_180:
//                displayRotation = 270;
//                break;
//            case Surface.ROTATION_270:
//                displayRotation = 180;
//                break;
//        }
//        int sensorOrientation = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
//        mDisplayRotation = (displayRotation + sensorOrientation + 270) % 360;
//        Log.d(TAG, "mDisplayRotation: " + mDisplayRotation);
//    }
//    public void triggerFocusAtPoint(float x, float y, int width, int height) {
//        Log.d(TAG, "triggerFocusAtPoint (" + x + ", " + y + ")");
//        Rect cropRegion = mPreviewBuilder.get(CaptureRequest.SCALER_CROP_REGION);
//        MeteringRectangle afRegion = getAFAERegion(x, y, width, height, 1f, cropRegion);
//        // ae的区域比af的稍大一点，聚焦的效果比较好
//        MeteringRectangle aeRegion = getAFAERegion(x, y, width, height, 1.5f, cropRegion);
//        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{afRegion});
//        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[]{aeRegion});
//        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
//        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
//        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
//        try {
//            mPreviewSession.capture(mPreviewBuilder.build(), mAfCaptureCallback, mBackgroundHandler);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//    private int mDisplayRotation = 0; // 原始Sensor画面顺时针旋转该角度后，画面朝上
//    private MeteringRectangle getAFAERegion(float x, float y, int viewWidth, int viewHeight, float multiple, Rect cropRegion) {
//        Log.v(TAG, "getAFAERegion enter");
//        Log.d(TAG, "point: [" + x + ", " + y + "], viewWidth: " + viewWidth + ", viewHeight: " + viewHeight);
//        Log.d(TAG, "multiple: " + multiple);
//        // do rotate and mirror
//        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
//        Matrix matrix1 = new Matrix();
//        matrix1.setRotate(mDisplayRotation);
//        matrix1.postScale(isFrontCamera() ? -1 : 1, 1);
//        matrix1.invert(matrix1);
//        matrix1.mapRect(viewRect);
//        // get scale and translate matrix
//        Matrix matrix2 = new Matrix();
//        RectF cropRect = new RectF(cropRegion);
//        matrix2.setRectToRect(viewRect, cropRect, Matrix.ScaleToFit.CENTER);
//        Log.d(TAG, "viewRect: " + viewRect);
//        Log.d(TAG, "cropRect: " + cropRect);
//        // get out region
//        int side = (int) (Math.max(viewWidth, viewHeight) / 8 * multiple);
//        RectF outRect = new RectF(x - side / 2, y - side / 2, x + side / 2, y + side / 2);
//        Log.d(TAG, "outRect before: " + outRect);
//        matrix1.mapRect(outRect);
//        matrix2.mapRect(outRect);
//        Log.d(TAG, "outRect after: " + outRect);
//        // 做一个clamp，测光区域不能超出cropRegion的区域
//        Rect meteringRect = new Rect((int) outRect.left, (int) outRect.top, (int) outRect.right, (int) outRect.bottom);
//        meteringRect.left = clamp(meteringRect.left, cropRegion.left, cropRegion.right);
//        meteringRect.top = clamp(meteringRect.top, cropRegion.top, cropRegion.bottom);
//        meteringRect.right = clamp(meteringRect.right, cropRegion.left, cropRegion.right);
//        meteringRect.bottom = clamp(meteringRect.bottom, cropRegion.top, cropRegion.bottom);
//        Log.d(TAG, "meteringRegion: " + meteringRect);
//        return new MeteringRectangle(meteringRect, 1000);
//    }
//    public boolean isFrontCamera() {
//        return  mCameraId == CameraCharacteristics.LENS_FACING_BACK;
//    }
//    private int clamp(int x, int min, int max) {
//        if (x > max) return max;
//        if (x < min) return min;
//        return x;
//    }
//    private final CameraCaptureSession.CaptureCallback mAfCaptureCallback = new CameraCaptureSession.CaptureCallback() {
//        private void process(CaptureResult result) {
//            Integer state = result.get(CaptureResult.CONTROL_AF_STATE);
//            Log.d(TAG, "CONTROL_AF_STATE: " + state);
//            if (state == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED || state == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
//                Log.d(TAG, "process: start normal preview");
//                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
//                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.FLASH_MODE_OFF);
//                try {
//                    mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
//                } catch (CameraAccessException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        @Override
//        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
//                                        @NonNull CaptureRequest request,
//                                        @NonNull CaptureResult partialResult) {
//            process(partialResult);
//        }
//
//        @Override
//        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
//                                       @NonNull CaptureRequest request,
//                                       @NonNull TotalCaptureResult result) {
//            process(result);
//        }
//    };
}
