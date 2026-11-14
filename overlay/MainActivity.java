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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // FIX - FULLSCREEN, kill that white/blue bar at top
    getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
    getWindow().setNavigationBarColor(android.graphics.Color.BLACK);
    getWindow().getDecorView().setSystemUiVisibility(
        android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    );
    // This stops the window from resizing / leaving a white gap when keyboard opens
    getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    

        // Ask for overlay permission FIRST, or minimize will never work
        if (!Settings.canDrawOverlays(this)) {
            Intent perm = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
            startActivityForResult(perm, 123);
        }

                web = new WebView(this);
        web.setBackgroundColor(android.graphics.Color.BLACK); // FIX - no white flash
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);

        // FIXED BRIDGE - this is what index.html now calls, no more location.href crash
        web.addJavascriptInterface(new Object(){
            @JavascriptInterface
            public void minimize(){
                runOnUiThread(() -> {
                    try { startForegroundService(new Intent(MainActivity.this, FloatingBuddy.class)); } catch(Exception e){ try{ startService(new Intent(MainActivity.this, FloatingBuddy.class)); }catch(Exception ignored){} }
                    // DON'T finish() - just go to background so FULL APP can come back
                    moveTaskToBack(true);
                });
            }
            @JavascriptInterface
            public void expand(){
                runOnUiThread(() -> {
                    // Already in full app, just ensure we are front
                    Intent i = new Intent(MainActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(i);
                });
            }
            @JavascriptInterface
            public void speak(String text){
                // TTS handled in JS fallback, keep stub so it doesn't crash
            }
        }, "AndroidMini");

        web.setWebViewClient(new WebViewClient(){
            // Old API (your old code only had this, Android 12+ ignores it)
            @Override public boolean shouldOverrideUrlLoading(WebView v, String url){
                return handleWaveBuddyUrl(url);
            }
            // New API - THIS is the one that actually runs now
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
        // User came back from permission screen, nothing to do - they can tap MINIMIZE now
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        // When FloatingBuddy says EXPAND, we come back here
        if(web != null){
            web.evaluateJavascript("document.body.classList.remove('mini-mode')", null);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(web != null){ web.destroy(); }
    }
}
