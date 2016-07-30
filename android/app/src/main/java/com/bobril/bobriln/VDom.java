package com.bobril.bobriln;

import android.content.Context;
import android.util.Log;
import android.view.WindowManager;

import com.facebook.csslayout.CSSLayoutContext;

import java.util.ArrayList;
import java.util.List;

public class VDom {
    GlobalApp globalApp;
    RootView rootView;
    RootVNode rootVNode;
    Context ctx;
    List<VNode> nodes;
    int width;
    int height;

    VDom(GlobalApp globalApp) {
        this.globalApp = globalApp;
        this.ctx = globalApp.applicationContext;
        rootVNode = new RootVNode(globalApp.tag2factory);
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
                String tag = (String)params.readAny();
                that.insertBefore(nodeId,createInto,createBefore,tag);
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
                that.setAttr(nodeId,attrName,attrValue);
            }
        });
        globalApp.RegisterNativeMethod("b.setStyle", new Gateway.NativeCall() {
            @Override
            public void Run(Decoder params, Encoder result) {
                int nodeId = params.readInt();
                List<Object> style = (List<Object>)params.readAny();
                that.setStyle(nodeId,style);
            }
        });
        globalApp.RegisterTag("Text", new IVNodeFactory() {
            @Override
            public VNode create() {
                return new TextVNode();
            }
        });
        globalApp.RegisterTag("View", new IVNodeFactory() {
            @Override
            public VNode create() {
                return new ViewVNode();
            }
        });
    }

    public void setStyle(int nodeId, List<Object> style) {
        VNode n=nodes.get(nodeId);
        n.setStyleList(style);
    }

    public void reset() {
        rootVNode = new RootVNode(globalApp.tag2factory);
        rootVNode.view = rootView;
        if (rootView!=null) rootView.removeAllViews();
        nodes = new ArrayList<>();
        nodes.add(rootVNode);
        rootVNode.setScreenSize(width, height);
    }

    public void insertBefore(int nodeId, int createInto, int createBefore, String tag) {
        VNode parent = nodes.get(createInto);
        VNode n=parent.createByTag(tag);
        n.vdom = this;
        n.tag = tag;
        n.nodeId = nodeId;
        n.lparent = parent;
        while (nodes.size()<=nodeId) {
            nodes.add(null);
        }
        nodes.set(nodeId, n);
        VNode before = null;
        if (createBefore>0) {
            before = nodes.get(createBefore);
        }
        parent.insertBefore(n, before);
    }

    public VNode replace(VNode what, String tag) {
        VNode parent = what.lparent;
        VNode n=parent.createByTag(tag);
        n.vdom = this;
        n.tag = tag;
        n.nodeId = what.nodeId;
        n.lparent = parent;
        nodes.set(n.nodeId, n);
        what.nodeId = -1;
        parent.replace(what,n);
        return n;
    }

    public void setStringChild(int nodeId, String content) {
        VNode n=nodes.get(nodeId);
        n.setStringChild(content);
    }

    public void setAttr(int nodeId, String attrName, Object attrValue) {
        VNode n=nodes.get(nodeId);
        n.setAttr(attrName, attrValue);
    }

    public boolean WantIdle() {
        return rootVNode.needValidate() || rootVNode.isDirty();
    }

    public void RunIdle() {
        rootVNode.validateView(0);
        rootVNode.setScreenSize(width, height);
        CSSLayoutContext context = new CSSLayoutContext();
        rootVNode.doLayout(context);
        rootVNode.flushLayout();
    }

    public void SetRootView(RootView rootView) {
        this.rootView = rootView;
        if (rootVNode!=null) {
            rootVNode.unsetView();
            rootVNode.view = rootView;
            rootVNode.validateView(0);
        }
    }

    public void setSize(int x, int y, float density) {
        if (this.width!=x || this.height!=y) {
            this.width = x;
            this.height = y;
            if (rootVNode!=null) RunIdle();
        }
    }
}
