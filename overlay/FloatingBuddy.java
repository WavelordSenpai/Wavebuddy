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
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.widget.FrameLayout;
import java.util.Locale;

public class FloatingBuddy extends Service {
    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;
    private WebView web;
    private TextToSpeech tts;

    @Override public IBinder onBind(Intent intent) { return null; }

    @Override public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel("buddy", "Wave Buddy", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);
            Notification n = new Notification.Builder(this, "buddy").setContentTitle("Wave Buddy is floating").setContentText("Tap buddy to talk or go home").setSmallIcon(android.R.drawable.sym_def_app_icon).build();
            startForeground(1, n);
        }
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i); stopSelf(); return;
        }
        try {
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

            // FIXED - AndroidMini bridge so index.html minimize/expand/speak works
            web.addJavascriptInterface(new Object(){
                @JavascriptInterface
                public void minimize(){
                    // Already mini in this service, just ensure mini-mode class
                    if(web != null) web.post(() -> web.evaluateJavascript("document.body.classList.add('mini-mode')", null));
                }
                @JavascriptInterface
                public void expand(){
                    // FULL APP button - go back to MainActivity, kill floating
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
                        return true; // swallow, already mini
                    }
                    return false;
                }
            });
            web.loadUrl("file:///android_asset/public/index.html?mode=mini&overlay=1");
            frame.addView(web, new FrameLayout.LayoutParams(320, 420));
            floatingView = frame;

            int type = Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
            params = new WindowManager.LayoutParams(320, 420, type,
                // FIXED - REMOVED FLAG_NOT_FOCUSABLE, that's why keyboard never opened
                // Keep NOT_TOUCH_MODAL so you can still use WhatsApp behind it
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.TOP | Gravity.LEFT;
            params.x = 100; params.y = 300;
            windowManager.addView(floatingView, params);

            // Drag to move - tap is handled by web page itself
            // IMPORTANT: return false on DOWN so WebView still gets taps/typing
            floatingView.setOnTouchListener(new View.OnTouchListener() {
                private int initialX, initialY;
                private float initialTouchX, initialTouchY;
                private boolean moved = false;
                @Override public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x; initialY = params.y;
                            initialTouchX = event.getRawX(); initialTouchY = event.getRawY();
                            moved = false;
                            return false;
                        case MotionEvent.ACTION_MOVE:
                            int dx = (int)(event.getRawX() - initialTouchX);
                            int dy = (int)(event.getRawY() - initialTouchY);
                            if (Math.abs(dx) > 10 || Math.abs(dy) > 10) moved = true;
                            if (moved) {
                                params.x = initialX + dx; params.y = initialY + dy;
                                windowManager.updateViewLayout(floatingView, params);
                                return true;
                            }
                            return false;
                        case MotionEvent.ACTION_UP:
                            return false;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
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
        try { if(tts != null){ tts.stop(); tts.shutdown(); } } catch(Exception ignored){}
        try { if (floatingView != null && windowManager != null) windowManager.removeView(floatingView); } catch (Exception e) {}
        try { if(web != null) web.destroy(); } catch(Exception ignored){}
    }
}
