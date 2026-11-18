package com.wavelord.wavebuddy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Toast;

public class FloatingBuddy extends Service {

    private static final String CHANNEL_ID = "wavebuddy_floating";
    private static final int NOTIF_ID = 1;

    private WindowManager windowManager;
    private WebView web;
    private WindowManager.LayoutParams params;

    public class AndroidMini {
        @JavascriptInterface public void minimize() {}
        @JavascriptInterface
        public void expand() {
            Intent i = new Intent(FloatingBuddy.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            stopSelf();
        }
        @JavascriptInterface public void exit() { stopSelf(); }
        @JavascriptInterface public void speak(String text) {}
        @JavascriptInterface public void testVoice() {}
        @JavascriptInterface
        public void setWindowSize(int w, int h) {
            try {
                float density = getResources().getDisplayMetrics().density;
                int widthPx = (int)(w * density);
                int heightPx = (int)(h * density);
                if (windowManager != null && params != null) {
                    params.width = widthPx;
                    params.height = heightPx;
                    windowManager.updateViewLayout(web, params);
                }
            } catch (Exception ignored) {}
        }
        @JavascriptInterface public void setChatOpen(boolean open) { setWindowSize(open ? 340 : 200, open ? 460 : 260); }
        @JavascriptInterface public void setMiniChatOpen(boolean open) { setWindowSize(open ? 320 : 200, open ? 620 : 260); }
    }

    @Override public void onCreate() {
        super.onCreate();

        // MUST call startForeground immediately or Android kills app
        createNotificationChannel();
        Notification notif = buildNotification();
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                startForeground(NOTIF_ID, notif, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            } else {
                startForeground(NOTIF_ID, notif);
            }
        } catch (Exception e) {
            try { startForeground(NOTIF_ID, notif); } catch (Exception ignored) {}
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Allow overlay to float Wave Buddy", Toast.LENGTH_LONG).show();
            stopSelf();
            return;
        }

        try {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            web = new WebView(this);
            web.getSettings().setJavaScriptEnabled(true);
            web.getSettings().setDomStorageEnabled(true);
            web.getSettings().setAllowFileAccess(true);
            web.getSettings().setMediaPlaybackRequiresUserGesture(false);
            web.addJavascriptInterface(new AndroidMini(), "AndroidMini");

            int type;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                type = WindowManager.LayoutParams.TYPE_PHONE;
            }

            float density = getResources().getDisplayMetrics().density;
            params = new WindowManager.LayoutParams(
                (int)(200 * density),
                (int)(260 * density),
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 0;
            params.y = 100;

            windowManager.addView(web, params);
            web.loadUrl("file:///android_asset/public/index.html?mode=mini&overlay=1");
        } catch (Exception e) {
            Toast.makeText(this, "Floating buddy failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            stopSelf();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Wave Buddy Floating", NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("Keeps Wave Buddy floating");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    private Notification buildNotification() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);
        Notification.Builder b;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            b = new Notification.Builder(this, CHANNEL_ID);
        } else {
            b = new Notification.Builder(this);
        }
        return b.setContentTitle("Wave Buddy is floating 💎")
            .setContentText("Tap to open Wave Buddy")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pi)
            .setOngoing(true)
            .build();
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override public void onDestroy() {
        super.onDestroy();
        try { if (windowManager != null && web != null) windowManager.removeView(web); } catch (Exception ignored) {}
        try { if (web != null) { web.stopLoading(); web.destroy(); } } catch (Exception ignored) {}
        try { stopForeground(true); } catch (Exception ignored) {}
    }

    @Override public IBinder onBind(Intent intent) { return null; }
    }
