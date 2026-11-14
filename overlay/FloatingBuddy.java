package com.wavelord.wavebuddy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
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
        // Android 12+ REQUIRES a foreground notification or it kills the service
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel("buddy", "Wave Buddy", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);
            Notification n = new Notification.Builder(this, "buddy").setContentTitle("Wave Buddy is floating").setContentText("Tap buddy to come back").setSmallIcon(android.R.drawable.sym_def_app_icon).build();
            startForeground(1, n);
        }
        // If user hasn't granted Draw over apps yet, DON'T crash - just open main app instead
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            stopSelf();
            return;
        }
        try {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            FrameLayout frame = new FrameLayout(this);
            WebView web = new WebView(this);
            web.getSettings().setJavaScriptEnabled(true);
            web.getSettings().setDomStorageEnabled(true);
            web.setBackgroundColor(Color.TRANSPARENT);
            // FIXED: buddy must be clickable to come back
            web.setClickable(true);
            web.setFocusable(true);
            web.loadUrl("file:///android_asset/public/index.html?mode=mini");
            frame.addView(web, new FrameLayout.LayoutParams(320, 380));
            floatingView = frame;

            int type = Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
            params = new WindowManager.LayoutParams(320, 380, type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
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
                                stopSelf();
                            }
                            return true;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            // Never crash - fall back to full app
            try {
                Intent i = new Intent(this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            } catch (Exception ignored) {}
            stopSelf();
        }
    }
    @Override public void onDestroy() {
        super.onDestroy();
        try { if (floatingView != null && windowManager != null) windowManager.removeView(floatingView); } catch (Exception e) {}
    }
}
