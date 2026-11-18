package com.wavelord.wavebuddy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

public class FloatingBuddy extends Service {
    private static final String CHANNEL_ID = "wavebuddy_floating";
    private static final int NOTIF_ID = 1;
    private WindowManager windowManager;
    private DragLayout container;
    private WebView web;
    private WindowManager.LayoutParams params;

    // --- THIS IS THE MAGIC CLASS ---
    public class DragLayout extends FrameLayout {
        private int initialX, initialY;
        private float initialTouchX, initialTouchY;
        private boolean isDragging = false;

        public DragLayout(android.content.Context ctx) { super(ctx); setBackgroundColor(Color.TRANSPARENT); }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent e) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = params.x; initialY = params.y;
                    initialTouchX = e.getRawX(); initialTouchY = e.getRawY();
                    isDragging = false;
                    return false; // let WebView see DOWN for button press
                case MotionEvent.ACTION_MOVE:
                    int dx = (int)(e.getRawX() - initialTouchX);
                    int dy = (int)(e.getRawY() - initialTouchY);
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                        isDragging = true;
                        params.x = initialX + dx;
                        params.y = initialY + dy;
                        try { windowManager.updateViewLayout(container, params); } catch (Exception ignored) {}
                        return true; // STEAL it - we are dragging
                    }
                    return false;
                case MotionEvent.ACTION_UP:
                    if (isDragging) { isDragging = false; return true; }
                    return false;
            }
            return false;
        }
    }

    public class AndroidMini {
        @JavascriptInterface public void minimize() {}
        @JavascriptInterface public void expand() {
            Intent i = new Intent(FloatingBuddy.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            stopSelf();
        }
        @JavascriptInterface public void exit() { stopSelf(); }
        @JavascriptInterface public void speak(String text) {}
        @JavascriptInterface public void testVoice() {}
        @JavascriptInterface public void setWindowSize(int w, int h) {
            try {
                float d = getResources().getDisplayMetrics().density;
                params.width = (int)(w * d);
                params.height = (int)(h * d);
                windowManager.updateViewLayout(container, params);
            } catch (Exception ignored) {}
        }
        @JavascriptInterface public void setChatOpen(boolean o) { setWindowSize(o ? 340 : 200, o ? 460 : 260); }
        @JavascriptInterface public void setMiniChatOpen(boolean o) { setWindowSize(o ? 320 : 200, o ? 620 : 260); }
    }

    @Override public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Notification notif = buildNotification();
        try {
            if (Build.VERSION.SDK_INT >= 29) startForeground(NOTIF_ID, notif, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            else startForeground(NOTIF_ID, notif);
        } catch (Exception e) { try { startForeground(NOTIF_ID, notif); } catch (Exception ignored) {} }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Allow overlay to float Wave Buddy", Toast.LENGTH_LONG).show();
            stopSelf(); return;
        }
        try {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

            container = new DragLayout(this);
            container.setBackgroundColor(Color.TRANSPARENT);

            web = new WebView(this);
            web.setBackgroundColor(Color.TRANSPARENT);
            web.setBackgroundColor(0);
            web.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            web.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            web.getSettings().setJavaScriptEnabled(true);
            web.getSettings().setDomStorageEnabled(true);
            web.getSettings().setAllowFileAccess(true);
            web.getSettings().setMediaPlaybackRequiresUserGesture(false);
            web.addJavascriptInterface(new AndroidMini(), "AndroidMini");

            // IMPORTANT: set client BEFORE loadUrl, or it never fires
            web.setWebViewClient(new WebViewClient() {
                @Override public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    view.evaluateJavascript(
                        "document.documentElement.style.background='transparent';" +
                        "document.body.style.background='transparent';" +
                        "document.body.style.backgroundColor='transparent';" +
                        "document.body.classList.add('mini-mode');" +
                        "document.documentElement.classList.add('mini-mode');",
                        null
                    );
                }
            });

            container.addView(web);

            int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
            float density = getResources().getDisplayMetrics().density;
            params = new WindowManager.LayoutParams(
                (int)(200 * density), (int)(260 * density),
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 0; params.y = 100;
            params.format = PixelFormat.TRANSLUCENT;

            windowManager.addView(container, params);
            web.loadUrl("file:///android_asset/public/index.html?mode=mini&overlay=1");
        } catch (Exception e) {
            Toast.makeText(this, "Floating buddy failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            stopSelf();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Wave Buddy Floating", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }
    private Notification buildNotification() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);
        Notification.Builder b = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? new Notification.Builder(this, CHANNEL_ID) : new Notification.Builder(this);
        return b.setContentTitle("Wave Buddy is floating").setContentText("Tap to open Wave Buddy").setSmallIcon(android.R.drawable.ic_dialog_info).setContentIntent(pi).setOngoing(true).build();
    }
    @Override public int onStartCommand(Intent intent, int flags, int startId) { return START_NOT_STICKY; }
    @Override public void onDestroy() {
        super.onDestroy();
        try { if (windowManager != null && container != null) windowManager.removeView(container); } catch (Exception ignored) {}
        try { if (web != null) { web.stopLoading(); web.destroy(); } } catch (Exception ignored) {}
        try { stopForeground(true); } catch (Exception ignored) {}
    }
    @Override public IBinder onBind(Intent intent) { return null; }
            }
