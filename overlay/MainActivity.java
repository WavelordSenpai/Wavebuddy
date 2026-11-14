package com.wavelord.wavebuddy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ask for overlay, but DON'T close the app
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 123);
        } else {
            startService(new Intent(this, FloatingBuddy.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && Settings.canDrawOverlays(this)) {
            startService(new Intent(this, FloatingBuddy.class));
        }
        // DO NOT finish() here - keep app open
    }
}
