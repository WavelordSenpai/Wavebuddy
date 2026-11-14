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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

public class FloatingBuddy extends Service {
    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;
    private WebView web;
    private TextToSpeech tts;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Foreground notification - required on Android 12+ or service gets killed
        String channelId = "wavebuddy_floating";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(channelId, "WaveBuddy", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(ch);
        }
        Notification notif;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notif = new Notification.Builder(this, channelId)
                .setContentTitle("WaveBuddy is running")
                .setContentText("Tap to expand")
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .build();
        } else {
            notif = new Notification.Builder(this)
                .setContentTitle("WaveBuddy is running")
                .setContentText("Tap to expand")
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .build();
        }
        try { startForeground(1, notif); } catch (Exception ignored) {}

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        FrameLayout frame = new FrameLayout(this);
        web = new WebView(this);
        WebSettings ws = web.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setAllowContentAccess(true);
        ws.setMediaPlaybackRequiresUserGesture(false);
        web.setBackgroundColor(Color.TRANSPARENT);
        web.setClickable(true);
        web.setFocusable(true);
        web.setFocusableInTouchMode(true);

        // FIXED - AndroidMini bridge (same name as MainActivity)
        web.addJavascriptInterface(new Object(){
            @JavascriptInterface
            public void minimize(){
                if(web != null) web.post(() -> web.evaluateJavascript("document.body.classList.add('mini-mode')", null));
            }
            @JavascriptInterface
            public void expand(){
                Intent i = new Intent(FloatingBuddy.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                stopSelf();
            }
            @JavascriptInterface
            public void speak(String text){
                if(text == null) return;
                try{
                    if(tts == null){
                        tts = new TextToSpeech(FloatingBuddy.this, status -> {
                            if(status == TextToSpeech.SUCCESS){
                                tts.setLanguage(Locale.US);
                                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "buddy");
                            }
                        });
                    } else {
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "buddy");
                    }
                }catch(Exception ignored){}
            }
        }, "AndroidMini");

        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView v, String url){
                return handleUrl(url);
            }
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest request){
                return handleUrl(request.getUrl().toString());
            }
            private boolean handleUrl(String url){
                if(url == null) return false;
                if(url.startsWith("wavebuddy://expand")){
                    Intent i = new Intent(FloatingBuddy.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(i);
                    stopSelf();
                    return true;
                }
                if(url.startsWith("wavebuddy://minimize")){
                    return true;
                }
                return false;
            }
        });

        web.loadUrl("file:///android_asset/public/index.html");

        FrameLayout.LayoutParams webParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT);
        frame.addView(web, webParams);
        floatingView = frame;

        int type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        // FIX - NO FLAG_NOT_FOCUSABLE, so keyboard works in floating buddy
        params = new WindowManager.LayoutParams(320, 420, type,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;

        try { windowManager.addView(floatingView, params); } catch (Exception e) {}

        // Drag to move
        floatingView.setOnTouchListener(new View.OnTouchListener(){
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            @Override public boolean onTouch(View v, MotionEvent event){
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int)(event.getRawX() - initialTouchX);
                        params.y = initialY + (int)(event.getRawY() - initialTouchY);
                        try { windowManager.updateViewLayout(floatingView, params); } catch(Exception ignored){}
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try { if(tts != null){ tts.stop(); tts.shutdown(); } } catch(Exception ignored){}
        try { if (floatingView != null && windowManager != null) windowManager.removeView(floatingView); } catch (Exception e) {}
        try { if(web != null) web.destroy(); } catch(Exception ignored){}
    }
            }
