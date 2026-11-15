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
import android.webkit.WebResourceRequest;
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
    private boolean dragging = false;
    private long downTime = 0;
    private float downX = 0, downY = 0;
    private int startX = 0, startY = 0;
    private int screenW = 1080, screenH = 1920;
    private boolean chatOpen = false;
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
        web.setBackgroundColor(android.graphics.Color.TRANSPARENT);
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
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = screenW - dp(220);
        params.y = dp(400);
        web.setOnTouchListener(new View.OnTouchListener(){
            @Override public boolean onTouch(View v, MotionEvent e){
                switch(e.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        dragging = false;
                        downTime = System.currentTimeMillis();
                        downX = e.getRawX();
                        downY = e.getRawY();
                        startX = params.x;
                        startY = params.y;
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        float dx = e.getRawX() - downX;
                        float dy = e.getRawY() - downY;
                        if(!dragging && (Math.abs(dx) > 22 || Math.abs(dy) > 22)){
                            dragging = true;
                        }
                        if(dragging){
                            params.x = startX + (int)dx;
                            params.y = startY + (int)dy;
                            if(params.x < 0) params.x = 0;
                            if(params.y < 0) params.y = 0;
                            if(params.x + params.width > screenW) params.x = screenW - params.width;
                            if(params.y + params.height > screenH) params.y = screenH - params.height;
                            try{ wm.updateViewLayout(web, params); }catch(Exception ignored){}
                            return true;
                        }
                        return false;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        boolean wasDragging = dragging;
                        dragging = false;
                        if(!wasDragging && (System.currentTimeMillis() - downTime) < 400){
                            try{ web.evaluateJavascript("if(window.onBuddyTap) onBuddyTap(new Event('tap'));", null); }catch(Exception ignored){}
                            return true;
                        }
                        return wasDragging;
                }
                return false;
            }
        });
        try{ wm.addView(web, params); }catch(Exception ignored){}
        tts = new TextToSpeech(this, status -> {
            if(status == TextToSpeech.SUCCESS){
                try{ tts.setLanguage(Locale.US); }catch(Exception ignored){}
            }
        });
    }
    private void openApp(){
        try{
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }catch(Exception ignored){}
        stopSelf();
    }
    private void updateFocus(){
        try{
            if(chatOpen) params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            else params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            wm.updateViewLayout(web, params);
        }catch(Exception ignored){}
    }
    public class MiniBridge {
        @JavascriptInterface public void minimize(){}
        @JavascriptInterface public void expand(){ openApp(); }
        @JavascriptInterface public void exit(){ stopSelf(); }
        @JavascriptInterface public void speak(final String text){
            try{ if(tts != null) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "wb1"); }catch(Exception ignored){}
        }
        @JavascriptInterface public void testVoice(){ speak("Hello! This is Wave Buddy. Can you hear me now?"); }
        @JavascriptInterface public void setWindowSize(final int w, final int h){
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                try{
                    chatOpen = (w > 250);
                    int nw = w;
                    int nh = h;
                    if(!chatOpen){
                        if(nw < 200) nw = 200;
                        if(nh < 260) nh = 260;
                    }
                    params.width = dp(nw); params.height = dp(nh);
                    if(params.y + params.height > screenH) params.y = screenH - params.height;
                    if(params.y < 0) params.y = 0;
                    if(params.x + params.width > screenW) params.x = screenW - params.width;
                    wm.updateViewLayout(web, params);
                    updateFocus();
                }catch(Exception ignored){}
            });
        }
    }
    @Override public void onDestroy(){
        super.onDestroy();
        try{ if(tts != null){ tts.stop(); tts.shutdown(); } }catch(Exception ignored){}
        try{ if(web != null) wm.removeView(web); }catch(Exception ignored){}
    }
        }
