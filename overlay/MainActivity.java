package com.wavelord.wavebuddy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.WebView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQ_OVERLAY = 1234;
    private WebView web;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.loadUrl("file:///android_asset/public/index.html");
        setContentView(web);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Allow WaveBuddy to draw over other apps", Toast.LENGTH_LONG).show();
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(i, REQ_OVERLAY);
        } else {
            startBuddy();
        }
    }

    private void startBuddy() {
        try {
            Intent s = new Intent(this, FloatingBuddy.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(s);
            } else {
                startService(s);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Could not start floating buddy: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_OVERLAY) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                startBuddy();
            } else {
                Toast.makeText(this, "Overlay permission needed for WaveBuddy to float", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        try { if (web != null) web.destroy(); } catch (Exception ignored) {}
    }
}
