package com.bobril.bobriln;

import com.facebook.csslayout.CSSLayoutContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class VNode {
    static final Map<String, Object> emptyMap = Collections.emptyMap();
    public int nodeId;
    public String tag;
    public VNode lparent;
    public VDom vdom;
    boolean needValidate;

    public VNode getParent() {
        VNode p = this.lparent;
        while (p != null) {
            if (!(p instanceof VNodeVirtual)) return p;
            p = p.lparent;
        }
        return null;
    }

    public Map<String, Object> getStyle() {
        if (lstyle != null) return lstyle;
        return emptyMap;
    }

    public Map<String, Object> getAttrs() {
        if (lattrs != null) return lattrs;
        return emptyMap;
    }

    List<Object> stylelist;
    Map<String, Object> lstyle;
    Map<String, Object> lattrs;
    public String content;
    public List<VNode> children;

    abstract VNode createByTag(String tag);

    int validateView(int indexInParent) {
        needValidate = false;
        return indexInParent;
    }

    void insertBefore(VNode what, VNode before) {
        invalidate();
        if (before == null) {
            if (children == null) children = new ArrayList<>();
            children.add(what);
        } else {
            int idx = children.indexOf(before);
            children.add(idx, what);
        }
    }

    void replace(VNode what, VNode by) {
        invalidate();
        int idx = children.indexOf(what);
        children.set(idx, by);
    }

    void setStringChild(String content) {
        this.content = content;
    }

    public void setAttr(String attrName, Object attrValue) {
        if (lattrs == null) lattrs = new HashMap<>();
        lattrs.put(attrName, attrValue);
    }

    public void setStyle(String styleName, Object styleValue) {
        if (styleValue == null) {
            if (lstyle == null) return;
            lstyle.remove(styleName);
            if (lstyle.size() == 0) lstyle = null;
        } else {
            if (lstyle == null) lstyle = new HashMap<>();
            lstyle.put(styleName, styleValue);
        }
    }

    public void unsetView() {
        if (children == null) return;
        for (int i = 0; i < children.size(); i++) {
            children.get(i).unsetView();
        }
    }

    public void invalidate() {
        if (needValidate) return;
        needValidate = true;
        if (lparent != null) lparent.invalidate();
    }

    public boolean needValidate() {
        return needValidate;
    }

    public boolean isDirty() {
        return false;
    }

    public void doLayout(CSSLayoutContext ctx) {
    }

    public void flushLayout() {
    }

    public void updateStyleDef(String name) {
        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                children.get(i).updateStyleDef(name);
            }
        }
        if (stylelist == null) return;
        for (int i = 0; i < stylelist.size(); i += 2) {
            String n = (String) stylelist.get(i);
            if (stylelist.get(i + 1) == null && name.equals(n)) {
                setStyleList(stylelist);
                return;
            }
        }
    }

    public void setStyleList(List<Object> nlist) {
        stylelist = nlist;
        if (lstyle == null) {
            for (int i = 0; i < nlist.size(); i += 2) {
                String name = (String) nlist.get(i);
                Object value = nlist.get(i + 1);
                if (value == null) { // Expand StyleDef
                    List<Object> nestedList = this.vdom.styleDefs.get(name);
                    for (int j = 0; j < nestedList.size(); j += 2) {
                        name = (String) nestedList.get(j);
                        value = nestedList.get(j + 1);
                        setStyle(name, value);
                    }
                } else {
                    setStyle(name, value);
                }
            }
        } else {
            // TODO optimize
            Map<String, Object> prevlstyle = lstyle;
            lstyle = null;
            Iterator<String> iterator = prevlstyle.keySet().iterator();
            while (iterator.hasNext()) {
                setStyle(iterator.next(), null);
            }
            for (int i = 0; i < nlist.size(); i += 2) {
                String name = (String) nlist.get(i);
                Object value = nlist.get(i + 1);
                if (value == null) { // Expand StyleDef
                    List<Object> nestedList = this.vdom.styleDefs.get(name);
                    for (int j = 0; j < nestedList.size(); j += 2) {
                        name = (String) nestedList.get(j);
                        value = nestedList.get(j + 1);
                        setStyle(name, value);
                    }
                } else {
                    setStyle(name, value);
                }
            }
        }
    }

    public void setScreenSize(int width, int height) {
    }

    public int pos2NodeId(float x, float y) {
        return 0;
    }

    public void remove(VNode child) {
        if (children != null) {
            children.remove(child);
            if (children.size() == 0) {
                children = null;
            }
        }
    }
}
