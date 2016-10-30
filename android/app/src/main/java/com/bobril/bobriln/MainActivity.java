package com.bobril.bobriln;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class MainActivity extends Activity {
    static GlobalApp globalApp;
    private boolean syncEv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MainActivity that = this;
        if (globalApp == null) {
            globalApp = new GlobalApp(getApplicationContext());
            globalApp.RegisterNativeMethod("b.dismissKeyboard", new Gateway.NativeCall() {
                @Override
                public void Run(Decoder params, Encoder result) {
                    that.dismissKeyboard();
                }
            });
        }
        NViewRoot rootView = new NViewRoot(this, globalApp);
        setContentView(rootView);
        globalApp.OnCreate(rootView, this);
        updateMediumSize();
    }

    float invDensity = 1;

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
        invDensity = 1f / metrics.density;
        globalApp.setSize(p.x, p.y, display.getRotation() * 90, metrics.density);
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

    class PointerInfo {
        long lastTime;
        float x;
        float y;
        int state;
        int pointerId;
        static final int STATE_ACTIVE = 1;
        static final int STATE_DOWN = 2;
        static final int STATE_DELAYED_FREEZE = 4;
        // was CANCELLED by Android OS
        static final int STATE_CANCELLED = 8;
        // was CANCELLED by User JS code
        static final int STATE_CAPTURED = 16;
    }

    final List<PointerInfo> pointers = new ArrayList<>();

    PointerInfo getPointerInfoByPointerId(int pointerId) {
        int p = pointers.size();
        for (int i = 0; i < p; i++) {
            PointerInfo pi = pointers.get(i);
            if ((pi.state & PointerInfo.STATE_ACTIVE) != 0 && pi.pointerId == pointerId) {
                return pi;
            }
        }
        for (int i = 0; i < p; i++) {
            PointerInfo pi = pointers.get(i);
            if ((pi.state & PointerInfo.STATE_ACTIVE) == 0) {
                pi.lastTime = 0;
                pi.x = 0;
                pi.y = 0;
                pi.state = PointerInfo.STATE_ACTIVE;
                pi.pointerId = pointerId;
                return pi;
            }
        }
        PointerInfo pi = new PointerInfo();
        pi.lastTime = 0;
        pi.x = 0;
        pi.y = 0;
        pi.state = PointerInfo.STATE_ACTIVE;
        pi.pointerId = pointerId;
        pointers.add(pi);
        return pi;
    }

    int pi2NodeId(float x, float y) {
        return globalApp.vdom.rootVNode.pos2NodeId(x, y);
    }

    boolean emitPointerMove(int pointerId, float x, float y, long time, boolean nodelay) {
        PointerInfo pi = getPointerInfoByPointerId(pointerId);
        if ((pi.state & PointerInfo.STATE_CANCELLED) != 0)
            return false;
        if (!nodelay && x == pi.x && y == pi.y) {
            if (pi.lastTime < time) {
                pi.lastTime = time;
                pi.state = pi.state | PointerInfo.STATE_DELAYED_FREEZE;
            }
            return false;
        }
        if ((pi.state & PointerInfo.STATE_DELAYED_FREEZE) != 0) {
            pi.state = pi.state & ~PointerInfo.STATE_DELAYED_FREEZE;
            emitPointerMove(pointerId, pi.x, pi.y, pi.lastTime, true);
        }
        pi.x = x;
        pi.y = y;
        pi.lastTime = time;
        HashMap<String, Object> param = new HashMap<>(1);
        param.put("id", Integer.valueOf(pointerId));
        param.put("x", Float.valueOf(x * invDensity));
        param.put("y", Float.valueOf(y * invDensity));
        return emitHelper("pointerMove", param, pi2NodeId(x, y), time);
    }

    boolean emitHelper(String name, Map<String, Object> params, int nodeId, long time) {
        if (syncEv) {
            return globalApp.emitJSEventSync(name, params, nodeId, time);
        } else {
            globalApp.emitJSEvent(name, params, nodeId, time);
            return false;
        }
    }

    boolean emitPointerDown(int pointerId, float x, float y, long time) {
        PointerInfo pi = getPointerInfoByPointerId(pointerId);
        if ((pi.state & PointerInfo.STATE_DELAYED_FREEZE) != 0) {
            emitPointerMove(pointerId, pi.x, pi.y, pi.lastTime, true);
        }
        if ((pi.state & PointerInfo.STATE_DOWN) != 0) {
            emitPointerUp(pointerId, x, y, time);
        }
        pi.state = pi.state | PointerInfo.STATE_DOWN;
        pi.state = pi.state & ~PointerInfo.STATE_CANCELLED;
        pi.x = x;
        pi.y = y;
        pi.lastTime = time;
        HashMap<String, Object> param = new HashMap<>(1);
        param.put("id", Integer.valueOf(pointerId));
        param.put("x", Float.valueOf(x * invDensity));
        param.put("y", Float.valueOf(y * invDensity));
        return emitHelper("pointerDown", param, pi2NodeId(x, y), time);
    }

    boolean emitPointerUp(int pointerId, float x, float y, long time) {
        PointerInfo pi = getPointerInfoByPointerId(pointerId);
        if ((pi.state & PointerInfo.STATE_CANCELLED) != 0) {
            pi.state = pi.state & ~PointerInfo.STATE_ACTIVE;
            return false;
        }
        if ((pi.state & PointerInfo.STATE_DELAYED_FREEZE) != 0) {
            emitPointerMove(pointerId, pi.x, pi.y, pi.lastTime, true);
        }
        if ((pi.state & PointerInfo.STATE_DOWN) == 0) {
            emitPointerMove(pointerId, x, y, time, false);
            return false;
        }
        pi.state = pi.state & ~(PointerInfo.STATE_DOWN | PointerInfo.STATE_ACTIVE);
        pi.x = x;
        pi.y = y;
        pi.lastTime = time;
        HashMap<String, Object> param = new HashMap<>(1);
        param.put("id", Integer.valueOf(pointerId));
        param.put("x", Float.valueOf(x * invDensity));
        param.put("y", Float.valueOf(y * invDensity));
        return emitHelper("pointerUp", param, pi2NodeId(x, y), time);
    }

    boolean emitPointerCancel(int pointerId) {
        PointerInfo pi = getPointerInfoByPointerId(pointerId);
        HashMap<String, Object> param = new HashMap<>(1);
        param.put("id", Integer.valueOf(pointerId));
        boolean res = emitHelper("pointerCancel", param, pi2NodeId(pi.x, pi.y), -1);
        pi.state = pi.state & ~PointerInfo.STATE_ACTIVE;
        return res;
    }

    Rect tempRect = new Rect();

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(tempRect);
        int statusBarHeight = tempRect.top;
        int pc = ev.getPointerCount();
        int hc = ev.getHistorySize();
        for (int hi = 0; hi < hc; hi++) {
            long time = ev.getHistoricalEventTime(hi);
            for (int pi = 0; pi < pc; pi++) {
                emitPointerMove(ev.getPointerId(pi), ev.getHistoricalX(pi, hi), ev.getHistoricalY(pi, hi) - statusBarHeight, time, false);
            }
        }
        int mainpi = ev.getActionIndex();
        long time = ev.getEventTime();
        for (int pi = 0; pi < pc; pi++) {
            if (mainpi == pi) continue;
            emitPointerMove(ev.getPointerId(pi), ev.getX(pi), ev.getY(pi) - statusBarHeight, time, false);
        }
        syncEv = true;
        boolean res = false;
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                res = emitPointerMove(ev.getPointerId(mainpi), ev.getX(mainpi), ev.getY(mainpi) - statusBarHeight, time, false);
                break;
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                res = emitPointerDown(ev.getPointerId(mainpi), ev.getX(mainpi), ev.getY(mainpi) - statusBarHeight, time);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                res = emitPointerUp(ev.getPointerId(mainpi), ev.getX(mainpi), ev.getY(mainpi) - statusBarHeight, time);
                break;
            case MotionEvent.ACTION_CANCEL:
                res = emitPointerCancel(ev.getPointerId(mainpi));
                break;
            default:
                Log.d("BobrilN", "Ignoring unknown MotionEvent " + MotionEvent.actionToString(ev.getActionMasked()));
                break;
        }
        syncEv = false;
        if (res)
            return true;
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        if (globalApp.emitJSEventSync("backPressed", new HashMap<String,Object>(), 0, -1))
            return;
        super.onBackPressed();
    }

    public void dismissKeyboard() {
        Window window = this.getWindow();
        if (window != null) {
            View v = window.getCurrentFocus();
            if (v != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm!=null){
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
    }
}
