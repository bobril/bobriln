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
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

public class GlobalApp implements AccSensorListener.Listener, Gateway {
    final Context applicationContext;
    AccSensorListener shakeDetector;
    WebView webView;
    NViewRoot rootView;
    TextView errorView;
    VDom vdom;
    boolean jsReady;
    ArrayList<String> nativeMethodNames = new ArrayList<>(32);
    ArrayList<Gateway.NativeCall> nativeMethodImpls = new ArrayList<>(32);
    final Decoder nativeParamsDecoder = new Decoder();
    final Encoder nativeResultsEncoder = new Encoder();
    final Semaphore uiSync = new Semaphore(0);
    final Encoder eventEncoder = new Encoder();
    final Semaphore eventSync = new Semaphore(0);
    ArrayList<Runnable> resetMethods = new ArrayList();
    Map<String, IVNodeFactory> tag2factory = new HashMap<>();
    private MainActivity mainActivity;
    public ImageCache imageCache;
    private boolean eventResult;
    final Queue<String> sendToJsQueue = new ArrayBlockingQueue<String>(64);

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
            @SuppressWarnings("deprecation")
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return super.shouldInterceptRequest(view, url);
            }

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
        imageCache = new ImageCache() {
            @Override
            protected void updated() {
                rootView.post(new Runnable() {
                    @Override
                    public void run() {
                        invalidateRecursive(rootView);
                    }
                });
            }
        };
    }

    public void invalidateRecursive(ViewGroup layout) {
        int count = layout.getChildCount();
        View child;
        for (int i = 0; i < count; i++) {
            child = layout.getChildAt(i);
            if(child instanceof ViewGroup)
                invalidateRecursive((ViewGroup) child);
            child.invalidate();
        }
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
            String res = sendToJsQueue.poll();
            if (res==null) return "";
            return res;
        }
        final GlobalApp that = this;
        if (param.length()==1) {
            char ch = param.charAt(0);
            if (ch==213 || ch==214) {
                eventResult = ch==214;
                eventSync.release();
                return "";
            }
        }
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
                            nativeResultsEncoder.writeNumber(1);
                            nativeResultsEncoder.writeString("Bobril Native");
                            nativeResultsEncoder.writeString("Android");
                            for (int i = 0; i < nativeMethodNames.size(); i++) {
                                nativeResultsEncoder.writeString(nativeMethodNames.get(i));
                            }
                            nativeResultsEncoder.writeEndOfBlock();
                            jsReady = true;
                        } else if (methodIdx == -3) {
                            // Special Method to reload JS
                            reloadJS();
                            return;
                        } else {
                            nativeResultsEncoder.writeNumber(1);
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
                    String res = nativeResultsEncoder.toLatin1String();
                    nativeResultsEncoder.reset();
                    triggerJS(res);
                    if (vdom.WantIdle()) {
                        vdom.RunIdle();
                    }
                } catch (Exception e) {
                    that.ShowError(e.toString());
                    e.printStackTrace();
                }
            }
        });
        return "";
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

    public void OnCreate(NViewRoot rootView, MainActivity mainActivity) {
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
            if (Objects.equals(nativeMethodNames.get(i), name))
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
        tag2factory.put(tag.substring(0,1).toLowerCase()+tag.substring(1), factory);
    }

    public void setSize(int x, int y, int rotation, float density) {
        imageCache.setDensity(density);
        vdom.setSize(x,y,rotation,density);
    }

    @Override
    public void emitJSEvent(String name, Map<String, Object> param, int nodeId, long time) {
        eventEncoder.writeNumber(-1);
        eventEncoder.writeString(name);
        eventEncoder.writeObject(param);
        eventEncoder.writeNumber(nodeId);
        if (time!=-1) {
            eventEncoder.writeNumber(System.currentTimeMillis()+time-SystemClock.uptimeMillis());
        eventEncoder.writeEndOfBlock();
        }
        triggerJS(eventEncoder.toLatin1String());
        eventEncoder.reset();
    }

    @Override
    public boolean emitJSEventSync(String name, Map<String, Object> param, int nodeId, long time) {
        long start = System.nanoTime();
        if (!jsReady) return false;
        eventEncoder.writeNumber(0);
        eventEncoder.writeString(name);
        eventEncoder.writeObject(param);
        eventEncoder.writeNumber(nodeId);
        if (time!=-1) {
            eventEncoder.writeNumber(System.currentTimeMillis()+time-SystemClock.uptimeMillis());
        }
        eventEncoder.writeEndOfBlock();
        triggerJS(eventEncoder.toLatin1String());
        eventEncoder.reset();
        try {
            eventSync.acquire();
        } catch (InterruptedException e) {
            return false;
        }
        //Log.d("BobrilN", "nanos "+name+" "+(System.nanoTime()-start));
        return eventResult;
    }

    private void triggerJS(String param) {
        if (!sendToJsQueue.offer(param)) {
            this.ShowError("SendToJsQueue overflow");
            return;
        }
        if (jsReady)
            webView.evaluateJavascript("__bobrilncb()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                // Ignore
            }
        });
    }
}
