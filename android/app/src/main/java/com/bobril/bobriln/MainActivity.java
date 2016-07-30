package com.bobril.bobriln;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

public class MainActivity extends Activity {
    static GlobalApp globalApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RootView rootView = new RootView(this);
        setContentView(rootView);
        if (globalApp == null) {
            globalApp = new GlobalApp(getApplicationContext());
        }
        globalApp.OnCreate(rootView,this);
        updateMediumSize();
    }

    private void updateMediumSize() {
        Point p = new Point();
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(p);
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int resource = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resource > 0) {
            p.y -= getResources().getDimensionPixelSize(resource);
        }
        globalApp.setSize(p.x,p.y,display.getRotation()*90,metrics.density);
    }

    @Override
    protected void onDestroy() {
        globalApp.OnDestroy();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateMediumSize();
    }
}

