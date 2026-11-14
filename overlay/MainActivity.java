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
        // No UI. No background. Just Crystal.
        if (!Settings.canDrawOverlays(this)) {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivity(i);
            finish();
            return;
        }
        startService(new Intent(this, FloatingBuddy.class));
        finish(); // Close immediately - only Crystal stays
    }
}
