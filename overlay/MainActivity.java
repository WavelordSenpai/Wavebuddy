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
import android.widget.Toast;
public class MainActivity extends Activity {
    private WebView web;
    private android.speech.tts.TextToSpeech tts;
    private void doSpeak(String text){
        if(text == null) return;
        String clean = text.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim();
        if(clean.isEmpty()) return;
        if(clean.length() > 250) clean = clean.substring(0, 250);
        final String toSay = clean;
        runOnUiThread(() -> {
            // DEBUG: you should SEE this toast every time you tap 🔊
            Toast.makeText(MainActivity.this, "Speaking: " + toSay.substring(0, Math.min(30, toSay.length())), Toast.LENGTH_SHORT).show();
            try{
                if(tts == null){
                    tts = new android.speech.tts.TextToSpeech(MainActivity.this, status -> {
                        try{
                            if(status == android.speech.tts.TextToSpeech.SUCCESS){
                                try{ tts.setEngineByPackageName("com.google.android.tts"); }catch(Exception ignored){}
                                int r = tts.setLanguage(java.util.Locale.US);
                                if(r == android.speech.tts.TextToSpeech.LANG_MISSING_DATA || r == android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED){
                                    tts.setLanguage(java.util.Locale.ENGLISH);
                                }
                                tts.setSpeechRate(0.95f); tts.setPitch(1.0f);
                                tts.speak(toSay, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "buddy");
                            } else {
                                Toast.makeText(MainActivity.this, "TTS engine missing - install Google TTS", Toast.LENGTH_LONG).show();
                            }
                        }catch(Exception e){ Toast.makeText(MainActivity.this, "TTS error: " + e.getMessage(), Toast.LENGTH_SHORT).show(); }
                    });
                } else {
                    try{ tts.stop(); }catch(Exception ignored){}
                    tts.setLanguage(java.util.Locale.US);
                    tts.speak(toSay, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "buddy");
                }
            }catch(Exception e){ Toast.makeText(MainActivity.this, "Speak fail: " + e.getMessage(), Toast.LENGTH_SHORT).show(); }
        });
    }
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().getDecorView().setSystemUiVisibility(0);
        getWindow().setStatusBarColor(android.graphics.Color.BLACK);
        getWindow().setNavigationBarColor(android.graphics.Color.BLACK);
        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        if (!Settings.canDrawOverlays(this)) {
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 123);
        }
        web = new WebView(this);
        web.setBackgroundColor(android.graphics.Color.BLACK);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setAllowFileAccess(true); s.setAllowContentAccess(true); s.setMediaPlaybackRequiresUserGesture(false);
        web.addJavascriptInterface(new Object(){
            @JavascriptInterface public void minimize(){
                runOnUiThread(() -> {
                    try { startForegroundService(new Intent(MainActivity.this, FloatingBuddy.class)); } catch(Exception e){ try{ startService(new Intent(MainActivity.this, FloatingBuddy.class)); }catch(Exception ignored){} }
                    moveTaskToBack(true);
                });
            }
            @JavascriptInterface public void expand(){
                runOnUiThread(() -> {
                    Intent i = new Intent(MainActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(i);
                });
            }
            @JavascriptInterface public void speak(String text){ doSpeak(text); }
            @JavascriptInterface public void testVoice(){ doSpeak("Hello! This is Wave Buddy. Can you hear me now?"); }
        }, "AndroidMini");
        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView v, String url){ return handle(url); }
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r){ return handle(r.getUrl().toString()); }
            private boolean handle(String url){
                if(url != null && url.startsWith("wavebuddy://minimize")){
                    try { startForegroundService(new Intent(MainActivity.this, FloatingBuddy.class)); } catch(Exception e){ try{ startService(new Intent(MainActivity.this, FloatingBuddy.class)); }catch(Exception ignored){} }
                    moveTaskToBack(true); return true;
                } return false;
            }
        });
        web.loadUrl("file:///android_asset/public/index.html");
        setContentView(web);
    }
    @Override protected void onActivityResult(int c, int r, Intent d) { super.onActivityResult(c,r,d); }
    @Override protected void onNewIntent(Intent i){ super.onNewIntent(i); if(web != null) web.evaluateJavascript("document.body.classList.remove('mini-mode')", null); }
    @Override protected void onDestroy(){ super.onDestroy(); try{ if(tts != null){ tts.stop(); tts.shutdown(); } }catch(Exception ignored){} if(web != null) web.destroy(); }
}
