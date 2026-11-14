package com.wavelord.wavebuddy;
import android.app.Notification; import android.app.NotificationChannel; import android.app.NotificationManager; import android.app.Service;
import android.content.Intent; import android.graphics.Color; import android.graphics.PixelFormat; import android.os.Build; import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity; import android.view.MotionEvent; import android.view.View; import android.view.WindowManager;
import android.webkit.JavascriptInterface; import android.webkit.WebResourceRequest; import android.webkit.WebSettings; import android.webkit.WebView; import android.webkit.WebViewClient;
import android.widget.LinearLayout; import android.widget.TextView; import android.speech.tts.TextToSpeech; import android.widget.Toast; import java.util.Locale;
public class FloatingBuddy extends Service {
    private WindowManager wm; private View fv; private WindowManager.LayoutParams params; private WebView web; private TextToSpeech tts;
    private int initX, initY; private float downX, downY; private boolean dragging = false;
    private int screenW = 1080;
    private void doSpeak(String t){
        if(t == null) return; String clean = t.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim(); if(clean.isEmpty()) return; if(clean.length()>250) clean = clean.substring(0,250); final String toSay = clean;
        try{
            android.os.Handler h = new android.os.Handler(android.os.Looper.getMainLooper());
            h.post(() -> Toast.makeText(FloatingBuddy.this, "Speaking: " + toSay.substring(0, Math.min(30, toSay.length())), Toast.LENGTH_SHORT).show());
            if(tts == null){ tts = new TextToSpeech(FloatingBuddy.this, st -> { try{ if(st == TextToSpeech.SUCCESS){ try{ tts.setEngineByPackageName("com.google.android.tts"); }catch(Exception ignored){} int r = tts.setLanguage(Locale.US); if(r==TextToSpeech.LANG_MISSING_DATA||r==TextToSpeech.LANG_NOT_SUPPORTED) tts.setLanguage(Locale.ENGLISH); tts.setSpeechRate(0.95f); tts.speak(toSay, TextToSpeech.QUEUE_FLUSH, null, "buddy"); } }catch(Exception ignored){} }); }
            else { try{ tts.stop(); }catch(Exception ignored){} tts.setLanguage(Locale.US); tts.speak(toSay, TextToSpeech.QUEUE_FLUSH, null, "buddy"); }
        }catch(Exception ignored){}
    }
    @Override public IBinder onBind(Intent i) { return null; }
    @Override public void onCreate() {
        super.onCreate();
        String chId = "wavebuddy_floating";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(chId, "WaveBuddy", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE); if (nm != null) nm.createNotificationChannel(ch);
        }
        Notification n = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ? new Notification.Builder(this, chId).setContentTitle("WaveBuddy is running").setContentText("Drag the top bar to move").setSmallIcon(android.R.drawable.sym_def_app_icon).build() : new Notification.Builder(this).setContentTitle("WaveBuddy is running").setContentText("Drag top bar").setSmallIcon(android.R.drawable.sym_def_app_icon).build();
        try { startForeground(1, n); } catch (Exception ignored) {}
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.TRANSPARENT);
        TextView dragBar = new TextView(this);
        dragBar.setText("⋮⋮  DRAG ME TO MOVE  ⋮⋮");
        dragBar.setTextSize(12); dragBar.setTextColor(Color.WHITE); dragBar.setGravity(Gravity.CENTER);
        dragBar.setBackgroundColor(Color.parseColor("#DD222222"));
        dragBar.setClickable(true); dragBar.setFocusable(true);
        root.addView(dragBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 64));
        web = new WebView(this);
        WebSettings ws = web.getSettings(); ws.setJavaScriptEnabled(true); ws.setDomStorageEnabled(true); ws.setAllowFileAccess(true); ws.setAllowContentAccess(true); ws.setMediaPlaybackRequiresUserGesture(false);
        web.setBackgroundColor(Color.TRANSPARENT);
        web.addJavascriptInterface(new Object(){
            @JavascriptInterface public void minimize(){}
            @JavascriptInterface public void expand(){ openApp(); }
            @JavascriptInterface public void speak(String t){ doSpeak(t); }
            @JavascriptInterface public void testVoice(){ doSpeak("Hello! This is Wave Buddy. Can you hear me now?"); }
            @JavascriptInterface public void moveWindow(int dx, int dy){
                try{
                    android.os.Handler h = new android.os.Handler(android.os.Looper.getMainLooper());
                    h.post(() -> {
                        try{
                            params.x += dx; params.y += dy;
                            if(params.x < -220) params.x = -220;
                            if(params.x > screenW - 100) params.x = screenW - 100;
                            if(params.y < 0) params.y = 0;
                            wm.updateViewLayout(fv, params);
                        }catch(Exception ignored){}
                    });
                }catch(Exception ignored){}
            }
        }, "AndroidMini");
        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView v, String u){ return handle(u); }
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r){ return handle(r.getUrl().toString()); }
            private boolean handle(String u){ if(u != null && u.startsWith("wavebuddy://expand")){ openApp(); return true; } return false; }
        });
        web.loadUrl("file:///android_asset/public/index.html?mode=mini&overlay=1");
        root.addView(web, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        fv = root;
        int type = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        DisplayMetrics dm = new DisplayMetrics(); try{ wm.getDefaultDisplay().getMetrics(dm); }catch(Exception ignored){}
        screenW = dm.widthPixels > 0 ? dm.widthPixels : 1080;
        int winW = 320; int winH = 620;
        int centerX = (screenW - winW) / 2; if(centerX < 0) centerX = 0;
        params = new WindowManager.LayoutParams(winW, winH, type, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = centerX; params.y = 80;
        try { wm.addView(fv, params); } catch (Exception e) {}
        dragBar.setOnTouchListener(new View.OnTouchListener(){
            @Override public boolean onTouch(View v, MotionEvent e){
                if(e.getAction() == MotionEvent.ACTION_DOWN){ initX = params.x; initY = params.y; downX = e.getRawX(); downY = e.getRawY(); dragging = false; return true; }
                if(e.getAction() == MotionEvent.ACTION_MOVE){
                    float dx = e.getRawX() - downX; float dy = e.getRawY() - downY;
                    if(Math.abs(dx) > 3 || Math.abs(dy) > 3) dragging = true;
                    if(dragging){
                        params.x = initX + (int)dx; params.y = initY + (int)dy;
                        int maxX = screenW - 100; if(params.x < -220) params.x = -220; if(params.x > maxX) params.x = maxX; if(params.y < 0) params.y = 0;
                        try{ wm.updateViewLayout(fv, params); }catch(Exception ignored){}
                    }
                    return true;
                }
                if(e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_CANCEL){ dragging = false; return true; }
                return true;
            }
        });
    }
    private void openApp(){ Intent i = new Intent(FloatingBuddy.this, MainActivity.class); i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); startActivity(i); stopSelf(); }
    @Override public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }
    @Override public void onDestroy() {
        super.onDestroy();
        try { if(tts != null){ tts.stop(); tts.shutdown(); } } catch(Exception ignored){}
        try { if (fv != null && wm != null) wm.removeView(fv); } catch (Exception e) {}
        try { if(web != null) web.destroy(); } catch(Exception ignored){}
    }
}
