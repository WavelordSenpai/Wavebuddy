package com.wavebuddy.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

public class FloatingBuddy extends Service implements TextToSpeech.OnInitListener {

    private WindowManager windowManager;
    private FrameLayout floatingView;
    private WebView webView;
    private WindowManager.LayoutParams params;
    private TextToSpeech tts;
    private boolean ttsReady = false;

    private int currentW = 200;
    private int currentH = 260;

    // ---------- Drag Layout - THIS is what moves Buddy ----------
    public static class DragLayout extends FrameLayout {
        private WindowManager wm;
        private WindowManager.LayoutParams p;
        private float downX, downY;
        private int startX, startY;
        private boolean isDragging = false;
        private long downTime = 0;

        public DragLayout(Context ctx, WindowManager wm, WindowManager.LayoutParams p) {
            super(ctx);
            this.wm = wm;
            this.p = p;
            setBackgroundColor(Color.TRANSPARENT);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            // If touch is on input / button / chat, let WebView handle it
            // Otherwise we will drag
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                downX = ev.getRawX();
                downY = ev.getRawY();
                startX = p.x;
                startY = p.y;
                downTime = System.currentTimeMillis();
                isDragging = false;
            }
            return false; // don't intercept yet, let child try
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            // This is called when WebView doesn't consume the touch
            // i.e. dragging on Buddy body
            switch (ev.getAction()) {
                case MotionEvent.ACTION_MOVE: {
                    float dx = ev.getRawX() - downX;
                    float dy = ev.getRawY() - downY;
                    if (!isDragging && (Math.abs(dx) > 8 || Math.abs(dy) > 8)) {
                        isDragging = true;
                    }
                    if (isDragging) {
                        p.x = startX + (int) dx;
                        p.y = startY + (int) dy;
                        try { wm.updateViewLayout(this, p); } catch (Exception ignored) {}
                        return true;
                    }
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    boolean wasDrag = isDragging;
                    isDragging = false;
                    // If it was a drag, consume it so WebView doesn't also get a click
                    return wasDrag;
                }
            }
            return super.onTouchEvent(ev);
        }
    }

    // ---------- JS Bridge ----------
    public class AndroidMini {
        @JavascriptInterface
        public void expand() {
            // Go back to full app
            Intent i = new Intent(FloatingBuddy.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            stopSelf();
        }

        @JavascriptInterface
        public void exit() {
            stopSelf();
        }

        @JavascriptInterface
        public void speak(String text) {
            if (text == null) return;
            if (ttsReady && tts != null) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "buddy");
            }
        }

        @JavascriptInterface
        public void testVoice() {
            speak("Hello! This is Wave Buddy. Can you hear me now?");
        }

        @JavascriptInterface
        public void setWindowSize(final int w, final int h) {
            // Called from index.html when miniChat opens / closes
            // Run on main thread
            if (floatingView != null) {
                floatingView.post(new Runnable() {
                    @Override public void run() {
                        currentW = w;
                        currentH = h;
                        params.width = w;
                        params.height = h;
                        try { windowManager.updateViewLayout(floatingView, params); } catch (Exception ignored) {}
                    }
                });
            }
        }

        @JavascriptInterface
        public void setChatOpen(final boolean open) {
            // When Talk / miniOptions open, make window bigger + focusable so keyboard works
            if (floatingView != null) {
                floatingView.post(new Runnable() {
                    @Override public void run() {
                        if (open) {
                            params.width = 340;
                            params.height = 460;
                            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
                        } else {
                            params.width = 200;
                            params.height = 260;
                            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                        }
                        currentW = params.width;
                        currentH = params.height;
                        try { windowManager.updateViewLayout(floatingView, params); } catch (Exception ignored) {}
                    }
                });
            }
        }

        @JavascriptInterface
        public void setMiniChatOpen(final boolean open) {
            setChatOpen(open);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        tts = new TextToSpeech(this, this);
        createFloatingView();
        startForegroundNotification();
    }

    private void startForegroundNotification() {
        String channelId = "wave_buddy_float";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(channelId, "Wave Buddy Floating", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(ch);
        }
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification n = new Notification.Builder(this, channelId)
                .setContentTitle("Wave Buddy is floating")
                .setContentText("Tap to open")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pi)
                .build();
        startForeground(1, n);
    }

    private void createFloatingView() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // ===== FIX WHITE PAGE #1: Window must be TRANSLUCENT, not OPAQUE =====
        params = new WindowManager.LayoutParams(
                200, 260,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.format = PixelFormat.TRANSLUCENT;
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 300;
        params.alpha = 1.0f;

        // ===== FIX WHITE PAGE #2: Container TRANSPARENT =====
        DragLayout drag = new DragLayout(this, windowManager, params);
        drag.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        drag.setBackgroundColor(Color.TRANSPARENT);
        floatingView = drag;

        // ===== FIX WHITE PAGE #3: WebView TRANSPARENT =====
        webView = new WebView(this);
        webView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setBackgroundColor(0x00000000);
        // Make WebView not draw white flash while loading
        webView.setAlpha(1.0f);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setMediaPlaybackRequiresUserGesture(false);

        webView.addJavascriptInterface(new AndroidMini(), "AndroidMini");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Force transparent + mini-mode, kill any white bg from CSS
                view.evaluateJavascript(
                        "(function(){"
                        + "document.documentElement.style.background='transparent';"
                        + "document.documentElement.style.backgroundColor='transparent';"
                        + "document.body.style.background='transparent';"
                        + "document.body.style.backgroundColor='transparent';"
                        + "document.body.classList.add('mini-mode');"
                        + "document.documentElement.classList.add('mini-mode');"
                        + "document.body.classList.add('overlay');"
                        + "document.documentElement.classList.add('overlay');"
                        + "var r=document.getElementById('room'); if(r){r.style.background='transparent'; r.style.backgroundImage='none'; r.style.boxShadow='none';}"
                        + "var l=document.getElementById('landing'); if(l) l.style.display='none';"
                        + "var c=document.getElementById('chatPanel'); if(c) c.style.display='none';"
                        + "})();",
                        null);
            }
        });

        // Touch handling: drag on Buddy body, tap through on buttons/inputs
        webView.setOnTouchListener(new View.OnTouchListener() {
            private float downX, downY;
            private int startX, startY;
            private boolean dragging = false;
            private long downTime = 0;

            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = ev.getRawX();
                        downY = ev.getRawY();
                        startX = params.x;
                        startY = params.y;
                        downTime = System.currentTimeMillis();
                        dragging = false;
                        break;
                    case MotionEvent.ACTION_MOVE: {
                        float dx = ev.getRawX() - downX;
                        float dy = ev.getRawY() - downY;
                        if (!dragging && (Math.abs(dx) > 10 || Math.abs(dy) > 10)) {
                            dragging = true;
                        }
                        if (dragging) {
                            params.x = startX + (int) dx;
                            params.y = startY + (int) dy;
                            try { windowManager.updateViewLayout(floatingView, params); } catch (Exception ignored) {}
                            return true; // consume drag, don't pass to WebView
                        }
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        boolean wasDrag = dragging;
                        dragging = false;
                        // If it was a long drag, consume it
                        // If it was a quick tap, let WebView handle click
                        if (wasDrag) return true;
                        break;
                    }
                }
                return false; // let WebView handle taps / typing
            }
        });

        drag.addView(webView);
        windowManager.addView(drag, params);

        // Load the SAME one index.html, in mini mode
        webView.loadUrl("file:///android_asset/public/index.html?mode=mini&overlay=1");
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
            tts.setSpeechRate(0.95f);
            ttsReady = true;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (floatingView != null) windowManager.removeView(floatingView);
        } catch (Exception ignored) {}
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (webView != null) webView.destroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
                       }
