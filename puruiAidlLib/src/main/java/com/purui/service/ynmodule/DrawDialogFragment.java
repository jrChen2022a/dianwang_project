package com.purui.service.ynmodule;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.purui.service.IInnerService;
import com.purui.service.R;


public class DrawDialogFragment extends DialogFragment {
    private String result;
    private Bitmap oribm;
    private IInnerService iPS;
    private ImageView ivBm;
    private boolean drawed = false;
    private int mode = 0;
    public static final int MODE_OCR = 1;
    public static final int MODE_STATE = 2;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create the dialog object
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        // Inflate the layout for the dialog
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.draw_rectangle_dialog, null);
        builder.setView(view);

        // Create the dialog object and set an OnShowListener
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        view.findViewById(R.id.iv_close3).setOnClickListener(v -> dismiss());
        ivBm = view.findViewById(R.id.iv_bm);
        Button btnDraw = view.findViewById(R.id.btn_draw);
        Button btnClear = view.findViewById(R.id.btn_clear);
        Button btnOK = view.findViewById(R.id.btn_OK);
        // 定义矩形框的Rect对象
        Rect[] rect = {new Rect()};

        // 按钮1：对大ImageView进行绘制矩形框
        btnDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivBm.setOnTouchListener(new View.OnTouchListener() {
                    float startX, startY, stopX, stopY;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                startX = event.getX();
                                startY = event.getY();
                                break;
                            case MotionEvent.ACTION_MOVE:
                                stopX = event.getX();
                                stopY = event.getY();
                                drawRect();
                                break;
                            case MotionEvent.ACTION_UP:
                                stopX = event.getX();
                                stopY = event.getY();
                                drawRect();
                                ivBm.setOnTouchListener(null);
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                    private void drawRect() {
                        Bitmap bitmap = Bitmap.createBitmap(ivBm.getWidth(), ivBm.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        canvas.drawBitmap(oribm, null, new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), null);

                        Paint paint = new Paint();
                        paint.setColor(Color.RED);
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(5);
                        rect[0].left = Math.max((int) Math.min(startX, stopX),0);
                        rect[0].top = Math.max((int) Math.min(startY, stopY),0);
                        rect[0].right = Math.min((int) Math.max(startX, stopX),canvas.getWidth());
                        rect[0].bottom = Math.min((int) Math.max(startY, stopY),canvas.getHeight());
                        canvas.drawRect(rect[0], paint);
                        ivBm.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
                        drawed = true;
                    }
                });
            }
        });

        // 按钮2：清除绘制的矩形框
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivBm.setImageBitmap(oribm);
                rect[0] = new Rect();
                drawed = false;
            }
        });

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bm;
                if(drawed){
                    int left = rect[0].left*oribm.getWidth()/ivBm.getWidth();
                    int top = rect[0].top*oribm.getHeight()/ivBm.getHeight();
                    int width = rect[0].width()*oribm.getWidth()/ivBm.getWidth();
                    int height = rect[0].height()*oribm.getHeight()/ivBm.getHeight();
                    bm = Bitmap.createBitmap(oribm, left, top, width, height);
                }else {
                    bm = oribm;
                }
                if(mode == MODE_STATE){
                    iPS.postDetectStatesAfterDraw(bm);
                }else if(mode == MODE_OCR){
                    iPS.postRecognizeID(bm);
                }
                dismiss();
            }
        });
        // Return the dialog object
        return dialog;
    }
    private boolean waiting = false;
    public String showDialogAndWaitForResult(FragmentManager fragmentManager, Bitmap bm, IInnerService iPS, int mode) throws InterruptedException {
        this.iPS = iPS;
        this.mode = mode;
        result = null;
        this.oribm = bm;
        show(fragmentManager, "my_dialog");

        synchronized (this){
            if(!waiting){
                waiting = true;
                wait();
            }
        }

        return result;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
            lp.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.8);
            dialog.getWindow().setAttributes(lp);
        }
        ivBm.setImageBitmap(oribm);
    }
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {

        synchronized (this) {
            if(waiting){
                waiting = false;
                notifyAll();
            }
        }

    }
}

