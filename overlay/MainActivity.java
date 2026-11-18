package com.wavelord.wavebuddy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

    private WebView webView;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private static final int OVERLAY_REQ = 1234;

    public class AndroidMini {
        @JavascriptInterface public void expand() {
            // Already in full app - do nothing
        }
        @JavascriptInterface public void exit() { finish(); }
        @JavascriptInterface public void speak(String text) {
            if (text == null) return;
            if (ttsReady && tts != null) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "buddy");
        }
        @JavascriptInterface public void testVoice() { speak("Hello! This is Wave Buddy. Can you hear me now?"); }
        @JavascriptInterface public void setWindowSize(int w, int h) { /* full app - ignore */ }
        @JavascriptInterface public void setChatOpen(boolean open) { /* full app - ignore */ }
        @JavascriptInterface public void setMiniChatOpen(boolean open) { /* full app - ignore */ }

        // Called when user taps MINIMIZE in full app
        @JavascriptInterface public void minimize() { startFloating(); }
        @JavascriptInterface public void startFloating() { startFloating(); }
    }

    private void startFloating() {
        // Check overlay permission - Android 6+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(i, OVERLAY_REQ);
            return;
        }
        Intent svc = new Intent(this, FloatingBuddy.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(svc);
        else startService(svc);
        // Go home - let Buddy float over other apps
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(home);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tts = new TextToSpeech(this, this);

        FrameLayout root = new FrameLayout(this);
        root.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);

        webView = new WebView(this);
        webView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);

        webView.addJavascriptInterface(new AndroidMini(), "AndroidMini");
        webView.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // In full app, make sure MINIMIZE button actually calls minimize()
                // index.html tryMinimize() only adds mini-mode class, so we hook it here:
                view.evaluateJavascript(
                        "(function(){"
                        + "var btn=document.getElementById('minimizeBtn');"
                        + "if(btn && !btn.__hooked){"
                        + "btn.__hooked=true;"
                        + "btn.addEventListener('click', function(e){"
                        + "e.preventDefault(); e.stopPropagation();"
                        + "if(window.AndroidMini && window.AndroidMini.minimize){ window.AndroidMini.minimize(); }"
                        + "});"
                        + "}"
                        + "})();", null);
            }
        });

        // Load YOUR one index.html - full mode, no ?mode=mini
        webView.loadUrl("file:///android_asset/public/index.html");
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_REQ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                startFloating();
            }
        }
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Coming back from floating - reload full app fresh
        if (webView != null) webView.loadUrl("file:///android_asset/public/index.html");
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) { tts.setLanguage(Locale.US); tts.setSpeechRate(0.95f); ttsReady = true; }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (tts != null) { tts.stop(); tts.shutdown(); }
        if (webView != null) webView.destroy();
    }
}
