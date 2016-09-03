package com.bobril.bobriln;

import android.content.Context;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class GlobalApp implements AccSensorListener.Listener, Gateway {
    final Context applicationContext;
    AccSensorListener shakeDetector;
    WebView webView;
    RootView rootView;
    TextView errorView;
    VDom vdom;
    boolean jsReady;
    ArrayList<String> nativeMethodNames = new ArrayList<>(32);
    ArrayList<Gateway.NativeCall> nativeMethodImpls = new ArrayList<>(32);
    final Decoder nativeParamsDecoder = new Decoder();
    final Encoder nativeResultsEncoder = new Encoder();
    final Semaphore uiSync = new Semaphore(0);
    final Encoder eventEncoder = new Encoder();
    final Map<Integer, Gateway.EventResultCallback> eventCallbacks = new HashMap<>();
    final List<Integer> freeEventIds = new ArrayList<>();
    int lastEventId = 0;
    ArrayList<Runnable> resetMethods = new ArrayList();
    Map<String, IVNodeFactory> tag2factory = new HashMap<>();
    private MainActivity mainActivity;

    public class Jsiface {
        GlobalApp globalApp;

        public Jsiface(GlobalApp owner) {
            globalApp = owner;
        }

        @JavascriptInterface
        public String c(String param) {
            return globalApp.c(param);
        }
    }

    public GlobalApp(Context applicationContext) {
        this.applicationContext = applicationContext;
        if (BobrilnConfig.ShakeToMenu) {
            shakeDetector = new AccSensorListener(this);
            shakeDetector.start((SensorManager) applicationContext.getSystemService(Context.SENSOR_SERVICE));
        }
        webView = new WebView(applicationContext);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new Jsiface(this), "__bobriln");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d("BobrilN", "PageFinished " + url);
                super.onPageFinished(view, url);
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                ShowError("Cannot load " + failingUrl + "\nMake sure you have started Bobril-build and \nadb reverse tcp:8080 tcp:8080");
                Log.d("BobrilN", "WebView Error " + String.valueOf(errorCode) + " " + description + " " + failingUrl);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                if (consoleMessage.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                    ShowError("JS Error: " + consoleMessage.message() + " " + consoleMessage.sourceId() + ":" + String.valueOf(consoleMessage.lineNumber()));
                }
                Log.d("BobrilN", consoleMessage.messageLevel().name() + ": " + consoleMessage.message() + " " + consoleMessage.sourceId() + ":" + String.valueOf(consoleMessage.lineNumber()));
                return true;
            }
        });
        vdom = new VDom(this);
    }

    public void ShowError(final String text) {
        rootView.post(new Runnable() {
            @Override
            public void run() {
                HideErrorCore();
                errorView = new TextView(rootView.getContext());
                errorView.setBackgroundColor(Color.argb(128, 255, 0, 0));
                errorView.append(text);
                errorView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        HideError();
                    }
                });
                rootView.addView(errorView);
            }
        });
    }

    void HideErrorCore() {
        if (errorView != null) {
            rootView.removeView(errorView);
            errorView = null;
        }
    }

    void HideError() {
        rootView.post(new Runnable() {
            @Override
            public void run() {
                HideErrorCore();
            }
        });
    }

    void reloadJS() {
        if (BobrilnConfig.LoadFromLocalhostUrl) {
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            webView.loadUrl("http://localhost:8080/index.html");
        } else {
            webView.loadUrl("file:///android_asset/index.html");
        }
    }

    String c(final String param) {
        if (param.length()==0) {
            synchronized (eventEncoder) {
                String res = eventEncoder.toLatin1String();
                eventEncoder.reset();
                return res;
            }
        }
        final GlobalApp that = this;
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    nativeParamsDecoder.initFromString(param);
                    while (!nativeParamsDecoder.isAnyEnd()) {
                        int methodIdx = nativeParamsDecoder.readInt();
                        if (methodIdx == -1) {
                            // Special Method to Reset Everything - call only during JS start
                            // String FrameworkName, String platformName, [String nativeMethodName]* run()
                            for (int i = 0; i < resetMethods.size(); i++) {
                                resetMethods.get(i).run();
                            }
                            nativeResultsEncoder.writeString("Bobril Native");
                            nativeResultsEncoder.writeString("Android");
                            for (int i = 0; i < nativeMethodNames.size(); i++) {
                                nativeResultsEncoder.writeString(nativeMethodNames.get(i));
                            }
                            nativeResultsEncoder.writeEndOfBlock();
                            jsReady = true;
                        } else if (methodIdx == -2) {
                            // Special Method to return event result
                            int id = nativeParamsDecoder.readInt();
                            boolean eventRes = nativeParamsDecoder.readBoolean();
                            Gateway.EventResultCallback cb = null;
                            synchronized (eventEncoder) {
                                cb = eventCallbacks.remove(id);
                                if (id==lastEventId-1) {
                                    lastEventId--;
                                } else {
                                    freeEventIds.add(id);
                                }
                            }
                            cb.EventResult(eventRes);
                        } else {
                            if (methodIdx < 0 || methodIdx >= nativeMethodImpls.size()) {
                                that.ShowError("JS tried to call unknown method number " + String.valueOf(methodIdx));
                            } else {
                                final NativeCall nc = nativeMethodImpls.get(methodIdx);
                                nc.Run(nativeParamsDecoder, nativeResultsEncoder);
                            }
                            nativeResultsEncoder.writeEndOfBlock();
                        }
                        while (!nativeParamsDecoder.isAnyEnd()) nativeParamsDecoder.next();
                        nativeParamsDecoder.next();
                    }
                    if (vdom.WantIdle()) {
                        vdom.RunIdle();
                    }
                } catch (Exception e) {
                    that.ShowError(e.toString());
                    e.printStackTrace();
                }
                uiSync.release();
            }
        });
        try {
            uiSync.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String res = nativeResultsEncoder.toLatin1String();
        nativeResultsEncoder.reset();
        return res;
    }

    @Override
    public void onShake() {
        rootView.post(new Runnable() {
            @Override
            public void run() {
                HideErrorCore();
                final View v = new View(rootView.getContext());
                v.setLayoutParams(new ViewGroup.LayoutParams(1, 1));
                v.setBackgroundColor(Color.TRANSPARENT);
                rootView.addView(v);
                v.setX(5);
                v.setY(rootView.getHeight() / 2);
                PopupMenu pm = new PopupMenu(rootView.getContext(), v, Gravity.CENTER);
                pm.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu menu) {
                        rootView.removeView(v);
                    }
                });
                pm.getMenu().add(0, 1, 1, "Reload JS");
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == 1) {
                            reloadJS();
                        }
                        return false;
                    }
                });
                pm.show();
            }
        });
    }

    public void OnCreate(RootView rootView, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        boolean first = this.rootView == null;
        this.rootView = rootView;
        if (first)
            reloadJS();
        vdom.SetRootView(rootView);
    }

    public void OnDestroy() {
        this.HideErrorCore();
        this.rootView = null;
    }

    @Override
    public void RegisterNativeMethod(String name, NativeCall implementation) {
        for (int i = 0; i < nativeMethodNames.size(); i++) {
            if (nativeMethodNames.get(i) == name)
                throw new Error("Trying to register " + name + " second time");
        }
        nativeMethodNames.add(name);
        nativeMethodImpls.add(implementation);
    }

    @Override
    public void RegisterResetMethod(Runnable implementation) {
        resetMethods.add(implementation);
    }

    @Override
    public void RegisterTag(String tag, IVNodeFactory factory) {
        tag2factory.put(tag, factory);
    }

    public void setSize(int x, int y, int rotation, float density) {
        Map<String,Object> param = new HashMap<String,Object>();
        param.put("width", Math.round(x/density));
        param.put("height", Math.round(y/density));
        param.put("rotation", rotation);
        param.put("density", density);
        emitJSEvent("onResize", param, 0);
        vdom.setSize(x,y,density);
    }

    @Override
    public void emitJSEvent(String name, Map<String, Object> param, int nodeId) {
        synchronized (eventEncoder) {
            eventEncoder.writeNumber(-1);
            eventEncoder.writeString(name);
            eventEncoder.writeObject(param);
            eventEncoder.writeNumber(nodeId);
            eventEncoder.writeEndOfBlock();
        }
        if (jsReady) triggerJS();
    }

    @Override
    public void emitJSEvent(String name, Map<String, Object> param, int nodeId, long time, EventResultCallback callback) {
        synchronized (eventEncoder) {
            int id = 0;
            if (freeEventIds.isEmpty()) {
                id = lastEventId++;
            } else {
                id = freeEventIds.remove(freeEventIds.size()-1);
            }
            eventCallbacks.put(id, callback);
            eventEncoder.writeNumber(id);
            eventEncoder.writeString(name);
            eventEncoder.writeObject(param);
            eventEncoder.writeNumber(nodeId);
            if (time!=-1) {
                eventEncoder.writeNumber(System.currentTimeMillis()+time-SystemClock.uptimeMillis());
            }
            eventEncoder.writeEndOfBlock();
        }
        if (jsReady) triggerJS();
    }

    private void triggerJS() {
        webView.evaluateJavascript("__bobrilncb()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                // Ignore
            }
        });
    }
}
