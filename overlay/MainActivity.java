package com.wavelord.wavebuddy;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;
import java.util.Locale;
public class MainActivity extends Activity {
    private WebView web; private TextToSpeech tts;
    private void doSpeak(String t){
        if(t==null) return; String clean=t.replaceAll("<[^>]*>","").replaceAll("\\s+"," ").trim(); if(clean.isEmpty()) return; if(clean.length()>250) clean=clean.substring(0,250); final String s=clean;
        try{
            if(tts==null){ tts=new TextToSpeech(this, st->{ try{ if(st==TextToSpeech.SUCCESS){ try{ tts.setEngineByPackageName("com.google.android.tts"); }catch(Exception ignored){} int r=tts.setLanguage(Locale.US); if(r==TextToSpeech.LANG_MISSING_DATA||r==TextToSpeech.LANG_NOT_SUPPORTED) tts.setLanguage(Locale.ENGLISH); tts.setSpeechRate(0.95f); tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, "buddy"); } }catch(Exception ignored){} }); }
            else { try{ tts.stop(); }catch(Exception ignored){} tts.setLanguage(Locale.US); tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, "buddy"); }
        }catch(Exception ignored){}
    }
    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        web=new WebView(this);
        web.setBackgroundColor(Color.BLACK);
        web.setLongClickable(false);
        web.setOnLongClickListener(v -> true);
        WebSettings ws=web.getSettings(); ws.setJavaScriptEnabled(true); ws.setDomStorageEnabled(true); ws.setAllowFileAccess(true); ws.setAllowContentAccess(true); ws.setMediaPlaybackRequiresUserGesture(false);
        web.addJavascriptInterface(new Object(){
            @JavascriptInterface public void minimize(){
                try{
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(MainActivity.this)){
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Allow 'Draw over other apps' for WaveBuddy, then tap MINIMIZE again", Toast.LENGTH_LONG).show();
                            try{ Intent p=new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:"+getPackageName())); startActivity(p); }catch(Exception ignored){}
                        });
                        return;
                    }
                    Intent i=new Intent(MainActivity.this, FloatingBuddy.class);
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) startForegroundService(i); else startService(i);
                }catch(Exception e){
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Could not start floating buddy: "+e.getMessage(), Toast.LENGTH_LONG).show());
                    return;
                }
                runOnUiThread(() -> { try{ finish(); }catch(Exception ignored){} });
            }
            @JavascriptInterface public void expand(){}
            @JavascriptInterface public void speak(String t){ doSpeak(t); }
            @JavascriptInterface public void testVoice(){ doSpeak("Hello! This is Wave Buddy. Can you hear me now?"); }
            @JavascriptInterface public void exit(){
                try{ doSpeak("Bye! See you soon!"); }catch(Exception ignored){}
                runOnUiThread(() -> { try{ stopService(new Intent(MainActivity.this, FloatingBuddy.class)); }catch(Exception ignored){} finish(); });
            }
            @JavascriptInterface public void setWindowSize(int w,int h){}
            @JavascriptInterface public void moveWindow(int dx,int dy){}
        }, "AndroidMini");
        web.setWebViewClient(new WebViewClient());
        setContentView(web);
        web.loadUrl("file:///android_asset/public/index.html");
    }
    @Override protected void onNewIntent(Intent intent){ super.onNewIntent(intent); setIntent(intent); }
    @Override protected void onDestroy(){
        super.onDestroy();
        try{ if(tts!=null){ tts.stop(); tts.shutdown(); } }catch(Exception ignored){}
        try{ if(web!=null) web.destroy(); }catch(Exception ignored){}
    }
    @Override public void onBackPressed(){
        try{ web.evaluateJavascript("if(document.body.classList.contains('mini-mode')){document.body.classList.remove('mini-mode');var w=document.getElementById('buddyWrap');w.style.left='50%';w.style.top='58%';w.style.transform='translate(-50%,-50%)';}", null); }catch(Exception ignored){}
        if(web!=null && web.canGoBack()){ web.goBack(); return; }
        super.onBackPressed();
    }
}
