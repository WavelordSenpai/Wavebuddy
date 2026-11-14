package com.wavelord.wavebuddy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.net.Uri;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // If no permission yet, ask for it
        if (!Settings.canDrawOverlays(this)) {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivity(i);
            finish();
            return;
        }
        // Turn ON the one buddy, then close. No screen, no second buddy.
        startService(new Intent(this, FloatingBuddy.class));
        finish();
    }
}
