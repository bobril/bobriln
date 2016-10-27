package com.bobril.bobriln;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;

import com.facebook.csslayout.CSSLayoutContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class VDom {
    GlobalApp globalApp;
    NViewRoot rootView;
    VNodeRoot rootVNode;
    Context ctx;
    List<VNode> nodes;
    public Map<String, List<Object>> styleDefs;
    int width;
    int height;
    int rotation;
    float density;
    CSSLayoutContext cssLayoutContext = new CSSLayoutContext();
    public TextStyleAccumulator textStyleAccu = new TextStyleAccumulator();

    VDom(GlobalApp globalApp) {
        this.globalApp = globalApp;
        this.styleDefs = new HashMap<>();
        this.ctx = globalApp.applicationContext;
        rootVNode = new VNodeRoot(globalApp.tag2factory);
        final VDom that = this;
        globalApp.RegisterResetMethod(new Runnable() {
            @Override
            public void run() {
                that.reset();
            }
        });
        globalApp.RegisterNativeMethod("b.insert", new Gateway.NativeCall() {
            @Override
            public void Run(Decoder params, Encoder result) {
                int nodeId = params.readInt();
                int createInto = params.readInt();
                int createBefore = params.readInt();
                String tag = (String) params.readAny();
                //Log.d("BobrilN", "b.insert nodeId: "+nodeId+" createInto: "+createInto+" createBefore: "+createBefore+" tag: "+tag);
                that.insertBefore(nodeId, createInto, createBefore, tag);
            }
        });
        globalApp.RegisterNativeMethod("b.setStringChild", new Gateway.NativeCall() {
            @Override
            public void Run(Decoder params, Encoder result) {
                int nodeId = params.readInt();
                String content = params.readString();
                that.setStringChild(nodeId, content);
            }
        });
        globalApp.RegisterNativeMethod("b.setAttr", new Gateway.NativeCall() {
            @Override
            public void Run(Decoder params, Encoder result) {
                int nodeId = params.readInt();
                String attrName = params.readString();
                Object attrValue = params.readAny();
                that.setAttr(nodeId, attrName, attrValue);
            }
        });
        globalApp.RegisterNativeMethod("b.setStyle", new Gateway.NativeCall() {
            @Override
            public void Run(Decoder params, Encoder result) {
                int nodeId = params.readInt();
                List<Object> style = (List<Object>) params.readAny();
                that.setStyle(nodeId, style);
            }
        });
        globalApp.RegisterNativeMethod("b.setStyleDef", new Gateway.NativeCall() {
            @Override
            public void Run(Decoder params, Encoder result) {
                String name = params.readString();
                Map<String, Object> style = (Map<String, Object>) params.readAny();
                Map<String, Map<String, Object>> pseudo = (Map<String, Map<String, Object>>) params.readAny();
                that.setStyleDef(name, style, pseudo);
            }
        });
        globalApp.RegisterNativeMethod("b.removeNode", new Gateway.NativeCall() {
            @Override
            public void Run(Decoder params, Encoder result) {
                int nodeId = params.readInt();
                that.removeNode(nodeId);
            }
        });
        globalApp.RegisterTag("Text", new IVNodeFactory() {
            @Override
            public VNode create() {
                return new VNodeText();
            }
        });
        globalApp.RegisterTag("View", new IVNodeFactory() {
            @Override
            public VNode create() {
                return new VNodeView();
            }
        });
        globalApp.RegisterTag("Image", new IVNodeFactory() {
            @Override
            public VNode create() {
                return new VNodeImage();
            }
        });
        globalApp.RegisterTag("Switch", new IVNodeFactory() {
            @Override
            public VNode create() {
                return new VNodeSwitch();
            }
        });
        globalApp.RegisterTag("TextInput", new IVNodeFactory() {
            @Override
            public VNode create() {
                return new VNodeTextInput();
            }
        });
        globalApp.RegisterTag("ScrollView", new IVNodeFactory() {
            @Override
            public VNode create() {
                return new VNodeScrollView();
            }
        });
    }

    private void removeNode(int nodeId) {
        VNode n = nodes.get(nodeId);
        n.lparent.remove(n);
    }

    private void setStyleDef(String name, Map<String, Object> style, Map<String, Map<String, Object>> pseudo) {
        // pseudo is currently ignored
        Iterator<Map.Entry<String, Object>> iterator = style.entrySet().iterator();
        List<Object> list = new ArrayList<Object>();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> item = iterator.next();
            list.add(item.getKey());
            list.add(item.getValue());
        }
        List<Object> oldList = this.styleDefs.get(name);
        if (oldList == null || !oldList.equals(list)) {
            this.styleDefs.put(name, list);
            rootVNode.updateStyleDef(name);
        }
    }

    public void setStyle(int nodeId, List<Object> style) {
        VNode n = nodes.get(nodeId);
        n.setStyleList(style);
    }

    public void reset() {
        rootVNode = new VNodeRoot(globalApp.tag2factory);
        rootVNode.view = rootView;
        if (rootView != null) rootView.removeAllViews();
        nodes = new ArrayList<>();
        nodes.add(rootVNode);
        rootVNode.setScreenSize(width, height);
        emitOnResize();
    }

    public void insertBefore(int nodeId, int createInto, int createBefore, String tag) {
        VNode parent = nodes.get(createInto);
        VNode n = parent.createByTag(tag);
        n.vdom = this;
        n.tag = tag;
        n.nodeId = nodeId;
        n.lparent = parent;
        while (nodes.size() <= nodeId) {
            nodes.add(null);
        }
        nodes.set(nodeId, n);
        VNode before = null;
        if (createBefore > 0) {
            before = nodes.get(createBefore);
        }
        parent.insertBefore(n, before);
    }

    public VNode replace(VNode what, String tag) {
        VNode parent = what.lparent;
        VNode n = parent.createByTag(tag);
        n.vdom = this;
        n.tag = tag;
        n.nodeId = what.nodeId;
        n.lparent = parent;
        nodes.set(n.nodeId, n);
        what.nodeId = -1;
        parent.replace(what, n);
        return n;
    }

    public void setStringChild(int nodeId, String content) {
        VNode n = nodes.get(nodeId);
        n.setStringChild(content);
    }

    public void setAttr(int nodeId, String attrName, Object attrValue) {
        VNode n = nodes.get(nodeId);
        n.setAttr(attrName, attrValue);
    }

    public boolean WantIdle() {
        return rootVNode.needValidate() || rootVNode.isDirty();
    }

    public void RunIdle() {
        rootVNode.validateView(0);
        rootVNode.setScreenSize(width, height);
        rootVNode.doLayout(cssLayoutContext);
        rootVNode.flushLayout();
    }

    public void SetRootView(NViewRoot rootView) {
        this.rootView = rootView;
        if (rootVNode != null) {
            rootVNode.unsetView();
            rootVNode.view = rootView;
            rootVNode.validateView(0);
        }
    }

    public void setSize(int x, int y, int rotation, float density) {
        if (this.width != x || this.height != y || this.rotation != rotation) {
            this.width = x;
            this.height = y;
            this.rotation = rotation;
            this.density = density;
            emitOnResize();
            if (rootVNode != null) RunIdle();
        }
    }

    private void emitOnResize() {
        Map<String,Object> param = new HashMap<String,Object>();
        param.put("width", Math.round(width/density));
        param.put("height", Math.round(height/density));
        param.put("rotation", rotation);
        param.put("density", density);
        globalApp.emitJSEvent("onResize", param, 0, -1);
    }

    int lastColorCache = Color.TRANSPARENT;
    Paint lastPaintCache;
    Path helperPath = new Path();
    float[] tempFloatArray = new float[8];
    int[] tempIntArray = new int[1];
    RectF tempRectF = new RectF();

    public Paint color2Paint(int color) {
        if (color==Color.TRANSPARENT) return null;
        if (color==lastColorCache) return lastPaintCache;
        Paint paint = new Paint();
        paint.setColor(color);
        lastColorCache = color;
        lastPaintCache = paint;
        return paint;
    }
}
