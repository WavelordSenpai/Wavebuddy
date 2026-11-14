package com.wavelord.wavebuddy;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView v, String url){
                if(url.equals("wavebuddy://minimize")){
                    startService(new Intent(MainActivity.this, FloatingBuddy.class));
                    finish();
                    return true;
                }
                return false;
            }
        });
        web.loadUrl("file:///android_asset/public/index.html");
        setContentView(web);
    }
}
