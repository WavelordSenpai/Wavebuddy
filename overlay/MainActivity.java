package com.wavelord.wavebuddy;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;

public class FloatingBuddy extends Service {
    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;

    @Override public IBinder onBind(Intent intent) { return null; }

    @Override public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        FrameLayout frame = new FrameLayout(this);
        WebView web = new WebView(this);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.setBackgroundColor(Color.TRANSPARENT);
        // WebView shows the friend, but frame handles drag/tap
        web.setClickable(false);
        web.setFocusable(false);
        // Load SAME friend, but in overlay mode
        web.loadUrl("file:///android_asset/public/index.html?overlay=1");
        frame.addView(web, new FrameLayout.LayoutParams(300, 360));
        floatingView = frame;

        params = new WindowManager.LayoutParams(
            300, 360,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 100; params.y = 300;
        windowManager.addView(floatingView, params);

        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private long downTime; private boolean moved = false;
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x; initialY = params.y;
                        initialTouchX = event.getRawX(); initialTouchY = event.getRawY();
                        downTime = System.currentTimeMillis(); moved = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int)(event.getRawX() - initialTouchX);
                        int dy = (int)(event.getRawY() - initialTouchY);
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) moved = true;
                        params.x = initialX + dx; params.y = initialY + dy;
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!moved && System.currentTimeMillis() - downTime < 300) {
                            Intent i = new Intent(FloatingBuddy.this, MainActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(i);
                        }
                        return true;
                }
                return false;
            }
        });
    }
    @Override public void onDestroy() {
        super.onDestroy();
        try { if (floatingView != null) windowManager.removeView(floatingView); } catch (Exception e) {}
    }
}
