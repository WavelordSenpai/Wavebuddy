package com.wavelord.wavebuddy;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.content.Intent;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView v, String url){
                if(url.equals("wavebuddy://minimize")){
                    try { startForegroundService(new Intent(MainActivity.this, FloatingBuddy.class)); } catch (Exception e) {}
                    finish();
                    return true;
                }
                if(url.equals("wavebuddy://expand")){ return false; }
                return false;
            }
        });
        web.loadUrl("file:///android_asset/public/index.html");
        setContentView(web);
    }
}
