package com.wavelord.wavebuddy;

import 【entity-android¦canonical_name=Android】.app.Service;
import 【entity-android¦canonical_name=Android】.content.Intent;
import 【entity-android¦canonical_name=Android】.graphics.PixelFormat;
import 【entity-android¦canonical_name=Android】.os.Build;
import 【entity-android¦canonical_name=Android】.os.IBinder;
import 【entity-android¦canonical_name=Android】.view.Gravity;
import 【entity-android¦canonical_name=Android】.view.MotionEvent;
import 【entity-android¦canonical_name=Android】.view.View;
import 【entity-android¦canonical_name=Android】.view.WindowManager;
import 【entity-android¦canonical_name=Android】.webkit.JavascriptInterface;
import 【entity-android¦canonical_name=Android】.webkit.WebResourceRequest;
import 【entity-android¦canonical_name=Android】.webkit.WebView;
import 【entity-android¦canonical_name=Android】.webkit.WebViewClient;
import 【entity-android¦canonical_name=Android】.speech.tts.TextToSpeech;
import 【entity-android¦canonical_name=Android】.app.Notification;
import 【entity-android¦canonical_name=Android】.app.NotificationChannel;
import 【entity-android¦canonical_name=Android】.app.NotificationManager;
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
                n = new Notification.Builder(this, chId).setContentTitle("Wave Buddy").setContentText("Buddy is floating").setSmallIcon(【entity-android¦canonical_name=Android】.R.drawable.ic_dialog_info).build();
            } else {
                n = new Notification.Builder(this).setContentTitle("Wave Buddy").setContentText("Buddy is floating").setSmallIcon(【entity-android¦canonical_name=Android】.R.drawable.ic_dialog_info).build();
            }
            if (Build.VERSION.SDK_INT >= 29) {
                startForeground(1, n, 【entity-android¦canonical_name=Android】.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
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
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
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

        web.setOnTouchListener(new View.OnTouchListener(){
            @Override public boolean onTouch(View v, MotionEvent e){
                if (miniChatOpen) {
                    isDragging = false;
                    return false;
                }
                switch(e.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        isDragging = false;
                        downX = e.getRawX();
                        downY = e.getRawY();
                        startX = params.x;
                        startY = params.y;
                        return false;
                    case MotionEvent.ACTION_MOVE: {
                        float dx = e.getRawX() - downX;
                        float dy = e.getRawY() - downY;
                        if (!isDragging && Math.hypot(dx, dy) > 12) {
                            isDragging = true;
                        }
                        if (isDragging) {
                            params.x = startX + (int)dx;
                            params.y = startY + (int)dy;
                            params.x = Math.max(0, Math.min(screenW - dp(200), params.x));
                            params.y = Math.max(0, Math.min(screenH - dp(260), params.y));
                            try { wm.updateViewLayout(web, params); } catch(Exception ignored){}
                            return true;
                        }
                        return false;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        boolean wasDragging = isDragging;
                        isDragging = false;
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
