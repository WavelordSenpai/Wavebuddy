package com.wavelord.wavebuddy;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.speech.tts.TextToSpeech;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import java.util.Locale;

public class FloatingBuddy extends Service {
    private WindowManager wm;
    private WindowManager.LayoutParams params;
    private WebView web;
    private TextToSpeech tts;
    private boolean isDragging = false;
    private float downX = 0, downY = 0;
    private int startX = 0, startY = 0;
    private int screenW = 1080, screenH = 1920;
    private boolean chatOpen = false;
    private boolean miniChatOpen = false;
    private float density = 2.6f;
    private int dp(int v){ return (int)(v * density + 0.5f); }

    @Override public IBinder onBind(Intent intent) { return null; }

    @Override public void onCreate() {
        super.onCreate();
        try {
            String chId = "wavebuddy_floating";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel ch = new NotificationChannel(chId, "Wave Buddy", NotificationManager.IMPORTANCE_LOW);
                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                if (nm != null) nm.createNotificationChannel(ch);
            }
            Notification n;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                n = new Notification.Builder(this, chId).setContentTitle("Wave Buddy").setContentText("Buddy is floating").setSmallIcon(android.R.drawable.ic_dialog_info).build();
            } else {
                n = new Notification.Builder(this).setContentTitle("Wave Buddy").setContentText("Buddy is floating").setSmallIcon(android.R.drawable.ic_dialog_info).build();
            }
            if (Build.VERSION.SDK_INT >= 29) {
                startForeground(1, n, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            } else {
                startForeground(1, n);
            }
        } catch (Exception ignored) {}
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        density = getResources().getDisplayMetrics().density;
        screenW = getResources().getDisplayMetrics().widthPixels;
        screenH = getResources().getDisplayMetrics().heightPixels;
        web = new WebView(this);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setMediaPlaybackRequiresUserGesture(false);
        web.setBackgroundColor(0);
        web.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        web.addJavascriptInterface(new MiniBridge(), "AndroidMini");
        web.setWebViewClient(new WebViewClient(){
            @Override public void onPageFinished(WebView view, String url){
                super.onPageFinished(view, url);
                try{ view.evaluateJavascript("document.body.classList.add('mini-mode'); window.__forceMini=true; window.IS_MINI=true;", null); }catch(Exception ignored){}
            }
            @Override public boolean shouldOverrideUrlLoading(WebView v, String u){ return false; }
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r){ return false; }
        });
        web.loadUrl("file:///android_asset/public/index.html?mode=mini&overlay=1");
        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        params = new WindowManager.LayoutParams(
            dp(200), dp(260), type,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = screenW - dp(220);
        params.y = dp(400);

        // === FIXED TOUCH: TAP vs DRAG ===
        web.setOnTouchListener(new View.OnTouchListener(){
            @Override public boolean onTouch(View v, MotionEvent e){
                if (miniChatOpen) {
                    isDragging = false;
                    return false; // let JS handle chat box
                }
                switch(e.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        isDragging = false;
                        downX = e.getRawX();
                        downY = e.getRawY();
                        startX = params.x;
                        startY = params.y;
                        return false; // don't consume yet, let tap work
                    case MotionEvent.ACTION_MOVE: {
                        float dx = e.getRawX() - downX;
                        float dy = e.getRawY() - downY;
                        // Only start dragging after 12px - this is the tap fix
                        if (!isDragging && Math.hypot(dx, dy) > 12) {
                            isDragging = true;
                        }
                        if (isDragging) {
                            params.x = startX + (int)dx;
                            params.y = startY + (int)dy;
                            // Keep on screen
                            params.x = Math.max(0, Math.min(screenW - dp(200), params.x));
                            params.y = Math.max(0, Math.min(screenH - dp(260), params.y));
                            try { wm.updateViewLayout(web, params); } catch(Exception ignored){}
                            return true; // we are dragging, consume it
                        }
                        return false;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        boolean wasDragging = isDragging;
                        isDragging = false;
                        // If it was a drag, consume UP so JS doesn't fire a tap
                        // If it was a tap, return false so JS onBuddyTap fires
                        return wasDragging;
                    }
                }
                return false;
            }
        });
        wm.addView(web, params);
    }

    public class MiniBridge {
        @JavascriptInterface public void minimize(){}
        @JavascriptInterface public void expand(){}
        @JavascriptInterface public void exit(){ stopSelf(); }
        @JavascriptInterface public void setWindowSize(int w, int h){
            try {
                params.width = dp(w); params.height = dp(h);
                wm.updateViewLayout(web, params);
            } catch(Exception ignored){}
        }
        @JavascriptInterface public void setChatOpen(boolean open){ chatOpen = open; }
        @JavascriptInterface public void setMiniChatOpen(boolean open){ miniChatOpen = open; }
        @JavascriptInterface public void setWindowSize(int w, int h, boolean dummy){ setWindowSize(w,h); }
        @JavascriptInterface public void speak(String text){
            try {
                if (tts == null) {
                    tts = new TextToSpeech(FloatingBuddy.this, status -> {
                        if (status == TextToSpeech.SUCCESS) {
                            tts.setLanguage(Locale.US);
                            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "wb1");
                        }
                    });
                } else tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "wb1");
            } catch(Exception ignored){}
        }
        @JavascriptInterface public void testVoice(){ speak("Hello! This is Wave Buddy. Can you hear me now?"); }
    }

    @Override public void onDestroy(){
        super.onDestroy();
        try { if (web != null) wm.removeView(web); } catch(Exception ignored){}
        try { if (tts != null) tts.shutdown(); } catch(Exception ignored){}
    }
}
