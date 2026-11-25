package com.wavelord.wavebuddy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
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

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private String selectedAvatar = "clippy";

    public class AndroidMini {
        @JavascriptInterface
        public void expand() {
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
        public void setWindowSize(final int wDp, final int hDp) {
            if (floatingView != null) {
                floatingView.post(new Runnable() {
                    @Override
                    public void run() {
                        params.width = dpToPx(wDp);
                        params.height = dpToPx(hDp);
                        try {
                            windowManager.updateViewLayout(floatingView, params);
                        } catch (Exception ignored) {}
                    }
                });
            }
        }

        @JavascriptInterface
        public void setChatOpen(final boolean open) {
            if (floatingView != null) {
                floatingView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (open) {
                            params.width = dpToPx(280);
                            params.height = dpToPx(300);
                            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
                        } else {
                            params.width = dpToPx(190);
                            params.height = dpToPx(240);
                            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                        }
                        try {
                            windowManager.updateViewLayout(floatingView, params);
                        } catch (Exception ignored) {}
                    }
                });
            }
        }

        @JavascriptInterface
        public void setMiniChatOpen(final boolean open) {
            if (floatingView != null) {
                floatingView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (open) {
                            params.width = dpToPx(340);
                            params.height = dpToPx(500);
                            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
                        } else {
                            params.width = dpToPx(190);
                            params.height = dpToPx(240);
                            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                        }
                        try {
                            windowManager.updateViewLayout(floatingView, params);
                        } catch (Exception ignored) {}
                    }
                });
            }
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

        params = new WindowManager.LayoutParams(
                dpToPx(190),
                dpToPx(240),
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.format = PixelFormat.TRANSLUCENT;
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = dpToPx(24);
        params.y = dpToPx(120);
        params.alpha = 1.0f;

        FrameLayout drag = new FrameLayout(this);
        drag.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        drag.setBackgroundColor(Color.TRANSPARENT);
        floatingView = drag;

        webView = new WebView(this);
        webView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setBackgroundColor(0x00000000);

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
                String js = "document.documentElement.style.background='transparent';";
                js += "document.body.style.background='transparent';";
                js += "document.body.classList.add('mini-mode');";
                js += "document.documentElement.classList.add('mini-mode');";
                js += "document.body.classList.add('overlay');";
                js += "var r=document.getElementById('room');";
                js += "if(r){r.style.background='transparent';r.style.backgroundImage='none';}";
                js += "var l=document.getElementById('landing');";
                js += "if(l) l.style.display='none';";
                js += "var c=document.getElementById('chatPanel');";
                js += "if(c) c.style.display='none';";
                view.evaluateJavascript(js, null);
            }
        });

        webView.setOnTouchListener(new View.OnTouchListener() {
            private float downX = 0;
            private float downY = 0;
            private int startX = 0;
            private int startY = 0;
            private boolean dragging = false;

            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                    downX = ev.getRawX();
                    downY = ev.getRawY();
                    startX = params.x;
                    startY = params.y;
                    dragging = false;
                    return false;
                }
                if (ev.getAction() == MotionEvent.ACTION_MOVE) {
                    float dx = ev.getRawX() - downX;
                    float dy = ev.getRawY() - downY;
                    if (!dragging && (Math.abs(dx) > 10 || Math.abs(dy) > 10)) {
                        dragging = true;
                    }
                    if (dragging) {
                        params.x = startX + (int) dx;
                        params.y = startY + (int) dy;
                        try {
                            windowManager.updateViewLayout(floatingView, params);
                        } catch (Exception ignored) {}
                        return true;
                    }
                    return false;
                }
                if (ev.getAction() == MotionEvent.ACTION_UP) {
                    boolean wasDrag = dragging;
                    dragging = false;
                    if (wasDrag) {
                        return true;
                    }
                    return false;
                }
                return false;
            }
        });

        drag.addView(webView);
        windowManager.addView(drag, params);
        webView.loadUrl("file:///android_asset/public/index.html?mode=mini&overlay=1&avatar=" + Uri.encode(selectedAvatar));
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
        if (intent != null && intent.hasExtra("avatar")) {
            String av = intent.getStringExtra("avatar");
            if (av != null && !av.isEmpty()) {
                selectedAvatar = av;
                if (webView != null) {
                    webView.loadUrl("file:///android_asset/public/index.html?mode=mini&overlay=1&avatar=" + Uri.encode(selectedAvatar));
                }
            }
        }
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
