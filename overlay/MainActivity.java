package com.wavelord.wavebuddy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQ_OVERLAY = 1234;
    private WebView web;

    public class AndroidMini {
        @JavascriptInterface
        public void minimize() {
            runOnUiThread(() -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
                    Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    startActivityForResult(i, REQ_OVERLAY);
                    return;
                }
                startBuddy();
            });
        }
        @JavascriptInterface
        public void expand() {
            runOnUiThread(() -> {
                Intent i = new Intent(MainActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }
        @JavascriptInterface public void exit() {
            runOnUiThread(() -> {
                try { stopService(new Intent(MainActivity.this, FloatingBuddy.class)); } catch (Exception ignored) {}
                finish();
            });
        }
        @JavascriptInterface public void speak(String text) {}
        @JavascriptInterface public void testVoice() {}
        @JavascriptInterface public void setWindowSize(int w, int h) {}
        @JavascriptInterface public void setChatOpen(boolean open) {}
        @JavascriptInterface public void setMiniChatOpen(boolean open) {}
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowFileAccess(true);
        web.getSettings().setMediaPlaybackRequiresUserGesture(false);
        web.addJavascriptInterface(new AndroidMini(), "AndroidMini");
        web.loadUrl("file:///android_asset/public/index.html");
        setContentView(web);
    }

    private void startBuddy() {
        try {
            Intent s = new Intent(this, FloatingBuddy.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(s);
            else startService(s);
            moveTaskToBack(true);
        } catch (Exception e) {
            Toast.makeText(this, "Could not start floating buddy: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // FIXED: when you tap Talk/Home and come back, kill the floating one so you don't get duplicates
    @Override protected void onResume() {
        super.onResume();
        try { stopService(new Intent(this, FloatingBuddy.class)); } catch (Exception ignored) {}
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_OVERLAY) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) startBuddy();
            else Toast.makeText(this, "Overlay permission needed to float", Toast.LENGTH_LONG).show();
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        try { if (web != null) { web.stopLoading(); web.destroy(); } } catch (Exception ignored) {}
    }
}
