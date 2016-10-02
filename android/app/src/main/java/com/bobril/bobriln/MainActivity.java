package com.bobril.bobriln;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class MainActivity extends Activity implements Gateway.EventResultCallback {
    static GlobalApp globalApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (globalApp == null) {
            globalApp = new GlobalApp(getApplicationContext());
        }
        NViewRoot rootView = new NViewRoot(this,globalApp);
        setContentView(rootView);
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

    private MotionEvent curEv;
    Queue<EventItem> evQueue = new ArrayDeque<>();
    class EventItem {
        MotionEvent me;
        String nName;
        Map<String, Object> nParams;
        long time;
    }

    @Override
    public void EventResult(boolean result) {
        EventItem ei = evQueue.remove();
        if (!result) {
            Log.d("BobrilN", "dispatching "+ei.nName);
            super.dispatchTouchEvent(ei.me);
        }
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

    void emitPointerMove(int pointerId, float x, float y, long time, boolean nodelay) {
        PointerInfo pi = getPointerInfoByPointerId(pointerId);
        if ((pi.state & PointerInfo.STATE_CANCELLED) != 0)
            return;
        if (!nodelay && x == pi.x && y == pi.y) {
            if (pi.lastTime < time) {
                pi.lastTime = time;
                pi.state = pi.state | PointerInfo.STATE_DELAYED_FREEZE;
            }
            return;
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
        param.put("x", Float.valueOf(x));
        param.put("y", Float.valueOf(y));

        emitHelper("pointerMove", param, pi2NodeId(x, y), time);
    }

    void emitHelper(String name, Map<String, Object> params, int nodeId, long time) {
        if (curEv!=null) {
            EventItem ei = new EventItem();
            ei.me = curEv;
            ei.nName = name;
            ei.nParams = params;
            ei.time = time;
            evQueue.add(ei);
            globalApp.emitJSEvent(name, params, nodeId, time, this);
        } else {
            globalApp.emitJSEvent(name, params, nodeId, time);
        }
    }

    void emitPointerDown(int pointerId, float x, float y, long time) {
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
        param.put("x", Float.valueOf(x));
        param.put("y", Float.valueOf(y));
        emitHelper("pointerDown", param, pi2NodeId(x, y), time);
    }

    void emitPointerUp(int pointerId, float x, float y, long time) {
        PointerInfo pi = getPointerInfoByPointerId(pointerId);
        if ((pi.state & PointerInfo.STATE_CANCELLED) != 0) {
            pi.state = pi.state & ~PointerInfo.STATE_ACTIVE;
            return;
        }
        if ((pi.state & PointerInfo.STATE_DELAYED_FREEZE) != 0) {
            emitPointerMove(pointerId, pi.x, pi.y, pi.lastTime, true);
        }
        if ((pi.state & PointerInfo.STATE_DOWN) == 0) {
            emitPointerMove(pointerId, x, y, time, false);
            return;
        }
        pi.state = pi.state & ~(PointerInfo.STATE_DOWN | PointerInfo.STATE_ACTIVE);
        pi.x = x;
        pi.y = y;
        pi.lastTime = time;
        HashMap<String, Object> param = new HashMap<>(1);
        param.put("id", Integer.valueOf(pointerId));
        param.put("x", Float.valueOf(x));
        param.put("y", Float.valueOf(y));
        emitHelper("pointerUp", param, pi2NodeId(x, y), time);
    }

    void emitPointerCancel(int pointerId) {
        PointerInfo pi = getPointerInfoByPointerId(pointerId);
        HashMap<String, Object> param = new HashMap<>(1);
        param.put("id", Integer.valueOf(pointerId));
        emitHelper("pointerCancel", param, pi2NodeId(pi.x, pi.y), -1);
        pi.state = pi.state & ~PointerInfo.STATE_ACTIVE;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int pc = ev.getPointerCount();
        int hc = ev.getHistorySize();
        for (int hi = 0; hi < hc; hi++) {
            long time = ev.getHistoricalEventTime(hi);
            for (int pi = 0; pi < pc; pi++) {
                emitPointerMove(ev.getPointerId(pi), ev.getHistoricalX(pi, hi), ev.getHistoricalY(pi, hi), time, false);
            }
        }
        int mainpi = ev.getActionIndex();
        long time = ev.getEventTime();
        for (int pi = 0; pi < pc; pi++) {
            if (mainpi == pi) continue;
            emitPointerMove(ev.getPointerId(pi), ev.getX(pi), ev.getY(pi), time, false);
        }
        curEv = ev;
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                emitPointerMove(ev.getPointerId(mainpi), ev.getX(mainpi), ev.getY(mainpi), time, false);
                break;
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                emitPointerDown(ev.getPointerId(mainpi), ev.getX(mainpi), ev.getY(mainpi), time);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                emitPointerUp(ev.getPointerId(mainpi), ev.getX(mainpi), ev.getY(mainpi), time);
                break;
            case MotionEvent.ACTION_CANCEL:
                emitPointerCancel(ev.getPointerId(mainpi));
                break;
            default:
                Log.d("BobrilN", "Ignoring unknown MotionEvent " + MotionEvent.actionToString(ev.getActionMasked()));
                break;
        }
        curEv = null;
        return true;
    }
}

