package com.wavelord.wavebuddy;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebSettings;

public class FloatingBuddy extends Service {
    private WindowManager windowManager;
    private View floatingView;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            WebView webView = new WebView(this);
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            webView.loadUrl("file:///android_asset/public/index.html?overlay=1");
            floatingView = webView;

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                320, 420,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.TOP | Gravity.LEFT;
            params.x = 100;
            params.y = 200;
            windowManager.addView(floatingView, params);

            floatingView.setOnTouchListener(new View.OnTouchListener() {
                private int initialX, initialY;
                private float initialTouchX, initialTouchY;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            params.x = initialX + (int)(event.getRawX() - initialTouchX);
                            params.y = initialY + (int)(event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(floatingView, params);
                            return true;
                    }
                    return false;
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try { if (floatingView != null) windowManager.removeView(floatingView); } catch (Exception e) {}
    }
}
