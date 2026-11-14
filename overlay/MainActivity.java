package com.wavelord.wavebuddy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;

public class MainActivity extends Activity {
    private WebView web;
    private android.speech.tts.TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // FIX - DO NOT go under status bar. This was causing white bar + cut off button
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().getDecorView().setSystemUiVisibility(0);
        getWindow().setStatusBarColor(android.graphics.Color.BLACK);
        getWindow().setNavigationBarColor(android.graphics.Color.BLACK);
        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        if (!Settings.canDrawOverlays(this)) {
            Intent perm = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
            startActivityForResult(perm, 123);
        }

        web = new WebView(this);
        web.setBackgroundColor(android.graphics.Color.BLACK);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);

        web.addJavascriptInterface(new Object(){
            @JavascriptInterface
            public void minimize(){
                runOnUiThread(() -> {
                    try { startForegroundService(new Intent(MainActivity.this, FloatingBuddy.class)); } catch(Exception e){ try{ startService(new Intent(MainActivity.this, FloatingBuddy.class)); }catch(Exception ignored){} }
                    moveTaskToBack(true);
                });
            }
            @JavascriptInterface
            public void expand(){
                runOnUiThread(() -> {
                    Intent i = new Intent(MainActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(i);
                });
            }
            @JavascriptInterface
            public void speak(String text){
                if(text == null) return;
                runOnUiThread(() -> {
                    try{
                        if(tts == null){
                            tts = new android.speech.tts.TextToSpeech(MainActivity.this, status -> {
                                if(status == android.speech.tts.TextToSpeech.SUCCESS){
                                    tts.setLanguage(java.util.Locale.US);
                                    tts.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "buddy");
                                }
                            });
                        } else {
                            tts.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "buddy");
                        }
                    }catch(Exception ignored){}
                });
            }
        }, "AndroidMini");

        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView v, String url){
                return handleWaveBuddyUrl(url);
            }
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest request){
                return handleWaveBuddyUrl(request.getUrl().toString());
            }
            private boolean handleWaveBuddyUrl(String url){
                if(url == null) return false;
                if(url.startsWith("wavebuddy://minimize")){
                    try { startForegroundService(new Intent(MainActivity.this, FloatingBuddy.class)); } catch(Exception e){ try{ startService(new Intent(MainActivity.this, FloatingBuddy.class)); }catch(Exception ignored){} }
                    moveTaskToBack(true);
                    return true;
                }
                if(url.startsWith("wavebuddy://expand")){
                    return false;
                }
                return false;
            }
        });
        web.loadUrl("file:///android_asset/public/index.html");
        setContentView(web);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        if(web != null){
            web.evaluateJavascript("document.body.classList.remove('mini-mode')", null);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try{ if(tts != null){ tts.stop(); tts.shutdown(); } }catch(Exception ignored){}
        if(web != null){ web.destroy(); }
    }
}
