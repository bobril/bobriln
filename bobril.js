"use strict";
var gw = require('./gateway');
function gwInsertBefore(nodeId, createInto, createBefore, tag) {
    var p = gw.prepareToCallNativeByName("b.insert");
    p.writeUInt(nodeId);
    p.writeUInt(createInto);
    p.writeUInt(createBefore);
    if (tag != null)
        p.writeString(tag);
    else
        p.writeNull();
    gw.callNativeIgnoreResult();
}
function gwSetAttr(nodeId, attrName, newAttr) {
    var p = gw.prepareToCallNativeByName("b.setAttr");
    p.writeUInt(nodeId);
    p.writeString(attrName);
    p.writeAny(newAttr);
    gw.callNativeIgnoreResult();
}
function gwUnsetAttr(nodeId, attrName) {
    var p = gw.prepareToCallNativeByName("b.unsetAttr");
    p.writeUInt(nodeId);
    p.writeString(attrName);
    gw.callNativeIgnoreResult();
}
function gwSetStyle(nodeId, style) {
    var p = gw.prepareToCallNativeByName("b.setStyle");
    p.writeUInt(nodeId);
    p.writeAny(style);
    gw.callNativeIgnoreResult();
}
function gwSetStringChild(nodeId, text) {
    var p = gw.prepareToCallNativeByName("b.setStringChild");
    p.writeUInt(nodeId);
    p.writeString(text);
    gw.callNativeIgnoreResult();
}
function gwUnsetStringChild(nodeId) {
    var p = gw.prepareToCallNativeByName("b.unsetStringChild");
    p.writeUInt(nodeId);
    gw.callNativeIgnoreResult();
}
function gwRemoveNode(nodeId) {
    var p = gw.prepareToCallNativeByName("b.removeNode");
    p.writeUInt(nodeId);
    gw.callNativeIgnoreResult();
}
function gwMoveBefore(nodeId, before) {
    var p = gw.prepareToCallNativeByName("b.moveBeforeNode");
    p.writeUInt(nodeId);
    p.writeUInt(before);
    gw.callNativeIgnoreResult();
}
function gwSetStyleDef(name, style, flattenPseudo) {
    var p = gw.prepareToCallNativeByName("b.setStyleDef");
    p.writeString(name);
    p.writeAny(style);
    p.writeAny(flattenPseudo);
    gw.callNativeIgnoreResult();
}
;
if (typeof DEBUG === "undefined")
    DEBUG = true;
// PureFuncs: assert, isArray, isObject, flatten
function assert(shoudBeTrue, messageIfFalse) {
    if (DEBUG && !shoudBeTrue)
        throw Error(messageIfFalse || "assertion failed");
}
exports.isArray = Array.isArray;
function isNumber(val) {
    return typeof val == "number";
}
exports.isNumber = isNumber;
function isString(val) {
    return typeof val == "string";
}
exports.isString = isString;
function isFunction(val) {
    return typeof val == "function";
}
exports.isFunction = isFunction;
function isObject(val) {
    return typeof val === "object";
}
exports.isObject = isObject;
if (Object.assign == null) {
    Object.assign = function assign(target) {
        var _sources = [];
        for (var _i = 1; _i < arguments.length; _i++) {
            _sources[_i - 1] = arguments[_i];
        }
        if (target == null)
            throw new TypeError('Target in assign cannot be undefined or null');
        var totalArgs = arguments.length;
        for (var i = 1; i < totalArgs; i++) {
            var source = arguments[i];
            if (source == null)
                continue;
            var keys = Object.keys(source);
            var totalKeys = keys.length;
            for (var j = 0; j < totalKeys; j++) {
                var key = keys[j];
                target[key] = source[key];
            }
        }
        return target;
    };
}
exports.assign = Object.assign;
function flatten(a) {
    if (!exports.isArray(a)) {
        if (a == null || a === false || a === true)
            return [];
        return [a];
    }
    a = a.slice(0);
    var alen = a.length;
    for (var i = 0; i < alen;) {
        var item = a[i];
        if (exports.isArray(item)) {
            a.splice.apply(a, [i, 1].concat(item));
            alen = a.length;
            continue;
        }
        if (item == null || item === false || item === true) {
            a.splice(i, 1);
            alen--;
            continue;
        }
        i++;
    }
    return a;
}
exports.flatten = flatten;
var inNotFocusable = false;
function newHashObj() {
    return Object.create(null);
}
function ieVersion() {
    return undefined;
}
exports.ieVersion = ieVersion;
var tabindexStr = "tabindex";
var tvalue = "value";
function updateElement(nodeId, newAttrs, oldAttrs, notFocusable) {
    var attrName, newAttr, oldAttr;
    var wasTabindex = false;
    if (newAttrs != null)
        for (attrName in newAttrs) {
            newAttr = newAttrs[attrName];
            oldAttr = oldAttrs[attrName];
            if (notFocusable && attrName === tabindexStr) {
                newAttr = -1;
                wasTabindex = true;
            }
            else if (attrName === tvalue) {
                if (isFunction(newAttr)) {
                    newAttr = newAttr();
                }
            }
            if (oldAttr !== newAttr) {
                gwSetAttr(nodeId, attrName, newAttr);
                oldAttrs[attrName] = newAttr;
            }
        }
    if (notFocusable && !wasTabindex) {
        oldAttrs[tabindexStr] = -1;
    }
    if (newAttrs == null) {
        for (attrName in oldAttrs) {
            if (oldAttrs[attrName] !== undefined) {
                if (notFocusable && attrName === tabindexStr)
                    continue;
                gwUnsetAttr(nodeId, attrName);
                oldAttrs[attrName] = undefined;
            }
        }
    }
    else {
        for (attrName in oldAttrs) {
            if (oldAttrs[attrName] !== undefined && !(attrName in newAttrs)) {
                if (notFocusable && attrName === tabindexStr)
                    continue;
                gwUnsetAttr(nodeId, attrName);
                oldAttrs[attrName] = undefined;
            }
        }
    }
    return oldAttrs;
}
function findCfg(parent) {
    var cfg;
    while (parent) {
        cfg = parent.cfg;
        if (cfg !== undefined)
            break;
        if (parent.ctx) {
            cfg = parent.ctx.cfg;
            break;
        }
        parent = parent.parent;
    }
    return cfg;
}
function setRef(ref, value) {
    if (ref == null)
        return;
    if (isFunction(ref)) {
        ref(value);
        return;
    }
    var ctx = ref[0];
    var refs = ctx.refs;
    if (!refs) {
        refs = newHashObj();
        ctx.refs = refs;
    }
    refs[ref[1]] = value;
}
var focusRootStack = [];
var focusRootTop = undefined;
function registerFocusRoot(ctx) {
    focusRootStack.push(ctx.me);
    addDisposable(ctx, unregisterFocusRoot);
    ignoreShouldChange();
}
exports.registerFocusRoot = registerFocusRoot;
function unregisterFocusRoot(ctx) {
    var idx = focusRootStack.indexOf(ctx.me);
    if (idx !== -1) {
        focusRootStack.splice(idx, 1);
        ignoreShouldChange();
    }
}
exports.unregisterFocusRoot = unregisterFocusRoot;
var lastNodeId = 1;
var freeNodeIds = [];
var nodeId2Node = [undefined];
function allocNodeId() {
    if (freeNodeIds.length > 0) {
        return freeNodeIds.pop();
    }
    return lastNodeId++;
}
function freeNodeId(nodeId) {
    if (lastNodeId - 1 === nodeId) {
        lastNodeId--;
    }
    else {
        freeNodeIds.push(nodeId);
    }
}
function createNode(n, parentNode, createInto, createBefore) {
    var c = {
        tag: n.tag,
        key: n.key,
        ref: n.ref,
        style: n.style,
        attrs: n.attrs,
        children: n.children,
        component: n.component,
        data: n.data,
        cfg: n.cfg,
        parent: parentNode,
        nodeId: allocNodeId(),
        ctx: undefined
    };
    nodeId2Node[c.nodeId] = c;
    var backupInNotFocusable = inNotFocusable;
    var component = c.component;
    setRef(c.ref, c);
    if (component) {
        var ctx = { data: c.data || {}, me: c, cfg: findCfg(parentNode) };
        c.ctx = ctx;
        if (component.init) {
            component.init(ctx, c);
        }
        if (component.render) {
            component.render(ctx, c);
        }
    }
    var tag = c.tag;
    var children = c.children;
    if (isNumber(children))
        c.children = "" + children;
    gwInsertBefore(c.nodeId, createInto, createBefore, tag);
    createChildren(c, c.nodeId, 0);
    if (component) {
        if (component.postRender) {
            component.postRender(c.ctx, c);
        }
    }
    if (inNotFocusable && focusRootTop === c)
        inNotFocusable = false;
    if (c.attrs || inNotFocusable)
        c.attrs = updateElement(c.nodeId, c.attrs, {}, inNotFocusable);
    if (c.style != null)
        gwSetStyle(c.nodeId, c.style);
    inNotFocusable = backupInNotFocusable;
    return c;
}
exports.createNode = createNode;
function normalizeNode(n) {
    if (n === false || n === true)
        return undefined;
    if (isString(n)) {
        return { children: n };
    }
    if (isNumber(n)) {
        return { children: "" + n };
    }
    return n;
}
function createChildren(c, createInto, createBefore) {
    var ch = c.children;
    if (!ch)
        return;
    if (!exports.isArray(ch)) {
        if (isString(ch)) {
            gwSetStringChild(createInto, ch);
            return;
        }
        if (isNumber(ch)) {
            gwSetStringChild(createInto, "" + ch);
            return;
        }
        ch = [ch];
    }
    ch = ch.slice(0);
    var i = 0, l = ch.length;
    while (i < l) {
        var item = ch[i];
        if (exports.isArray(item)) {
            ch.splice.apply(ch, [i, 1].concat(item));
            l = ch.length;
            continue;
        }
        item = normalizeNode(item);
        if (item == null) {
            ch.splice(i, 1);
            l--;
            continue;
        }
        ch[i] = createNode(item, c, createInto, createBefore);
        i++;
    }
    c.children = ch;
}
function destroyNode(c) {
    setRef(c.ref, undefined);
    var ch = c.children;
    if (exports.isArray(ch)) {
        for (var i = 0, l = ch.length; i < l; i++) {
            destroyNode(ch[i]);
        }
    }
    var component = c.component;
    if (component) {
        var ctx = c.ctx;
        if (component.destroy)
            component.destroy(ctx, c);
        var disposables = ctx.disposables;
        if (exports.isArray(disposables)) {
            for (var i = disposables.length; i-- > 0;) {
                var d = disposables[i];
                if (isFunction(d))
                    d(ctx);
                else
                    d.dispose();
            }
        }
    }
    nodeId2Node[c.nodeId] = undefined;
    freeNodeId(c.nodeId);
    c.nodeId = -1;
}
function addDisposable(ctx, disposable) {
    var disposables = ctx.disposables;
    if (disposables == null) {
        disposables = [];
        ctx.disposables = disposables;
    }
    disposables.push(disposable);
}
exports.addDisposable = addDisposable;
function removeNode(c) {
    var nodeId = c.nodeId;
    destroyNode(c);
    gwRemoveNode(nodeId);
}
var roots = newHashObj();
function vdomPath(n) {
    var res = [];
    var top = nodeId2Node[n];
    while (top != null) {
        res.push(top);
        top = top.parent;
    }
    res.reverse();
    return res;
}
exports.vdomPath = vdomPath;
// PureFuncs: deref
function deref(n) {
    return nodeId2Node[n];
}
exports.deref = deref;
function finishUpdateNode(n, c, component) {
    if (component) {
        if (component.postRender) {
            component.postRender(c.ctx, n, c);
        }
    }
    c.data = n.data;
}
function updateStyle(nodeId, newStyle, oldStyle) {
    if (newStyle === oldStyle)
        return;
    if (newStyle == null) {
        if (oldStyle == null)
            return;
        gwSetStyle(nodeId, []);
        return;
    }
    if (oldStyle == null) {
        gwSetStyle(nodeId, newStyle);
        return;
    }
    if (newStyle.length != oldStyle.length) {
        gwSetStyle(nodeId, newStyle);
        return;
    }
    for (var i = 0; i < newStyle.length; i++) {
        if (newStyle[i] !== oldStyle[i]) {
            gwSetStyle(nodeId, newStyle);
            return;
        }
    }
}
function updateNode(n, c, createInto, createBefore, deepness) {
    var component = n.component;
    var backupInNotFocusable = inNotFocusable;
    var bigChange = false;
    var ctx = c.ctx;
    if (component && ctx != null) {
        if (ctx[ctxInvalidated] === frameCounter) {
            deepness = Math.max(deepness, ctx[ctxDeepness]);
        }
        if (component.id !== c.component.id) {
            bigChange = true;
        }
        else {
            if (c.parent != undefined)
                ctx.cfg = findCfg(c.parent);
            if (component.shouldChange)
                if (!component.shouldChange(ctx, n, c) && !ignoringShouldChange) {
                    if (exports.isArray(c.children)) {
                        if (inNotFocusable && focusRootTop === c)
                            inNotFocusable = false;
                        selectedUpdate(c.children, c.nodeId, 0);
                        inNotFocusable = backupInNotFocusable;
                    }
                    return c;
                }
            ctx.data = n.data || {};
            c.component = component;
            if (component.render) {
                n = exports.assign({}, n); // need to clone me because it should not be modified for next updates
                component.render(ctx, n, c);
            }
            c.cfg = n.cfg;
        }
    }
    if (DEBUG) {
        if (!((n.ref == null && c.ref == null) ||
            ((n.ref != null && c.ref != null && (isFunction(n.ref) || isFunction(c.ref) ||
                n.ref[0] === c.ref[0] && n.ref[1] === c.ref[1]))))) {
            if (window.console && console.warn)
                console.warn("ref changed in child in update");
        }
    }
    var newChildren = n.children;
    var cachedChildren = c.children;
    var tag = n.tag;
    if (isNumber(newChildren)) {
        newChildren = "" + newChildren;
    }
    if (bigChange || (component && ctx == null)) {
    }
    else if (tag === c.tag) {
        if (inNotFocusable && focusRootTop === c)
            inNotFocusable = false;
        var nodeId = c.nodeId;
        if ((isString(newChildren)) && !exports.isArray(cachedChildren)) {
            if (newChildren !== cachedChildren) {
                gwSetStringChild(nodeId, newChildren);
                cachedChildren = newChildren;
            }
        }
        else {
            if (deepness <= 0) {
                if (exports.isArray(cachedChildren))
                    selectedUpdate(c.children, nodeId, createBefore);
            }
            else {
                cachedChildren = updateChildren(nodeId, newChildren, cachedChildren, c, 0, deepness - 1);
            }
        }
        c.children = cachedChildren;
        finishUpdateNode(n, c, component);
        if (c.attrs || n.attrs || inNotFocusable)
            c.attrs = updateElement(nodeId, n.attrs, c.attrs || {}, inNotFocusable);
        updateStyle(nodeId, n.style, c.style);
        c.style = n.style;
        inNotFocusable = backupInNotFocusable;
        return c;
    }
    var r = createNode(n, c.parent, createInto, c.nodeId);
    removeNode(c);
    return r;
}
exports.updateNode = updateNode;
function findNextNode(a, i, len, def) {
    while (++i < len) {
        var ai = a[i];
        if (ai == null)
            continue;
        return ai.nodeId;
    }
    return def;
}
function updateNodeInUpdateChildren(newNode, cachedChildren, cachedIndex, cachedLength, createBefore, element, deepness) {
    cachedChildren[cachedIndex] = updateNode(newNode, cachedChildren[cachedIndex], element, findNextNode(cachedChildren, cachedIndex, cachedLength, createBefore), deepness);
}
function reorderInUpdateChildrenRec(c, before) {
    gwMoveBefore(c.nodeId, before);
}
function reorderInUpdateChildren(cachedChildren, cachedIndex, cachedLength, createBefore) {
    var before = findNextNode(cachedChildren, cachedIndex, cachedLength, createBefore);
    var cur = cachedChildren[cachedIndex];
    if (cur.nodeId !== before) {
        reorderInUpdateChildrenRec(cur, before);
    }
}
function reorderAndUpdateNodeInUpdateChildren(newNode, cachedChildren, cachedIndex, cachedLength, createBefore, element, deepness) {
    var before = findNextNode(cachedChildren, cachedIndex, cachedLength, createBefore);
    var cur = cachedChildren[cachedIndex];
    if (cur.nodeId !== before) {
        reorderInUpdateChildrenRec(cur, before);
    }
    cachedChildren[cachedIndex] = updateNode(newNode, cur, element, before, deepness);
}
function updateChildren(element, newChildren, cachedChildren, parentNode, createBefore, deepness) {
    if (newChildren == null)
        newChildren = [];
    if (!exports.isArray(newChildren)) {
        newChildren = [newChildren];
    }
    if (cachedChildren == null)
        cachedChildren = [];
    if (!exports.isArray(cachedChildren)) {
        gwUnsetStringChild(element);
        cachedChildren = [];
    }
    var newCh = newChildren;
    newCh = newCh.slice(0);
    var newLength = newCh.length;
    var newIndex;
    for (newIndex = 0; newIndex < newLength;) {
        var item = newCh[newIndex];
        if (exports.isArray(item)) {
            newCh.splice.apply(newCh, [newIndex, 1].concat(item));
            newLength = newCh.length;
            continue;
        }
        item = normalizeNode(item);
        if (item == null) {
            newCh.splice(newIndex, 1);
            newLength--;
            continue;
        }
        newCh[newIndex] = item;
        newIndex++;
    }
    return updateChildrenCore(element, newCh, cachedChildren, parentNode, createBefore, deepness);
}
exports.updateChildren = updateChildren;
function updateChildrenCore(element, newChildren, cachedChildren, parentNode, createBefore, deepness) {
    var newEnd = newChildren.length;
    var cachedLength = cachedChildren.length;
    var cachedEnd = cachedLength;
    var newIndex = 0;
    var cachedIndex = 0;
    while (newIndex < newEnd && cachedIndex < cachedEnd) {
        if (newChildren[newIndex].key === cachedChildren[cachedIndex].key) {
            updateNodeInUpdateChildren(newChildren[newIndex], cachedChildren, cachedIndex, cachedLength, createBefore, element, deepness);
            newIndex++;
            cachedIndex++;
            continue;
        }
        while (true) {
            if (newChildren[newEnd - 1].key === cachedChildren[cachedEnd - 1].key) {
                newEnd--;
                cachedEnd--;
                updateNodeInUpdateChildren(newChildren[newEnd], cachedChildren, cachedEnd, cachedLength, createBefore, element, deepness);
                if (newIndex < newEnd && cachedIndex < cachedEnd)
                    continue;
            }
            break;
        }
        if (newIndex < newEnd && cachedIndex < cachedEnd) {
            if (newChildren[newIndex].key === cachedChildren[cachedEnd - 1].key) {
                cachedChildren.splice(cachedIndex, 0, cachedChildren[cachedEnd - 1]);
                cachedChildren.splice(cachedEnd, 1);
                reorderAndUpdateNodeInUpdateChildren(newChildren[newIndex], cachedChildren, cachedIndex, cachedLength, createBefore, element, deepness);
                newIndex++;
                cachedIndex++;
                continue;
            }
            if (newChildren[newEnd - 1].key === cachedChildren[cachedIndex].key) {
                cachedChildren.splice(cachedEnd, 0, cachedChildren[cachedIndex]);
                cachedChildren.splice(cachedIndex, 1);
                cachedEnd--;
                newEnd--;
                reorderAndUpdateNodeInUpdateChildren(newChildren[newEnd], cachedChildren, cachedEnd, cachedLength, createBefore, element, deepness);
                continue;
            }
        }
        break;
    }
    if (cachedIndex === cachedEnd) {
        if (newIndex === newEnd) {
            return cachedChildren;
        }
        // Only work left is to add new nodes
        while (newIndex < newEnd) {
            cachedChildren.splice(cachedIndex, 0, createNode(newChildren[newIndex], parentNode, element, findNextNode(cachedChildren, cachedIndex - 1, cachedLength, createBefore)));
            cachedIndex++;
            cachedEnd++;
            cachedLength++;
            newIndex++;
        }
        return cachedChildren;
    }
    if (newIndex === newEnd) {
        // Only work left is to remove old nodes
        while (cachedIndex < cachedEnd) {
            cachedEnd--;
            removeNode(cachedChildren[cachedEnd]);
            cachedChildren.splice(cachedEnd, 1);
        }
        return cachedChildren;
    }
    // order of keyed nodes ware changed => reorder keyed nodes first
    var cachedKeys = newHashObj();
    var newKeys = newHashObj();
    var key;
    var node;
    var backupNewIndex = newIndex;
    var backupCachedIndex = cachedIndex;
    var deltaKeyless = 0;
    for (; cachedIndex < cachedEnd; cachedIndex++) {
        node = cachedChildren[cachedIndex];
        key = node.key;
        if (key != null) {
            assert(!(key in cachedKeys));
            cachedKeys[key] = cachedIndex;
        }
        else
            deltaKeyless--;
    }
    var keyLess = -deltaKeyless - deltaKeyless;
    for (; newIndex < newEnd; newIndex++) {
        node = newChildren[newIndex];
        key = node.key;
        if (key != null) {
            assert(!(key in newKeys));
            newKeys[key] = newIndex;
        }
        else
            deltaKeyless++;
    }
    keyLess += deltaKeyless;
    var delta = 0;
    newIndex = backupNewIndex;
    cachedIndex = backupCachedIndex;
    var cachedKey;
    while (cachedIndex < cachedEnd && newIndex < newEnd) {
        if (cachedChildren[cachedIndex] === null) {
            cachedChildren.splice(cachedIndex, 1);
            cachedEnd--;
            cachedLength--;
            delta--;
            continue;
        }
        cachedKey = cachedChildren[cachedIndex].key;
        if (cachedKey == null) {
            cachedIndex++;
            continue;
        }
        key = newChildren[newIndex].key;
        if (key == null) {
            newIndex++;
            while (newIndex < newEnd) {
                key = newChildren[newIndex].key;
                if (key != null)
                    break;
                newIndex++;
            }
            if (key == null)
                break;
        }
        var akpos = cachedKeys[key];
        if (akpos === undefined) {
            // New key
            cachedChildren.splice(cachedIndex, 0, createNode(newChildren[newIndex], parentNode, element, findNextNode(cachedChildren, cachedIndex - 1, cachedLength, createBefore)));
            delta++;
            newIndex++;
            cachedIndex++;
            cachedEnd++;
            cachedLength++;
            continue;
        }
        if (!(cachedKey in newKeys)) {
            // Old key
            removeNode(cachedChildren[cachedIndex]);
            cachedChildren.splice(cachedIndex, 1);
            delta--;
            cachedEnd--;
            cachedLength--;
            continue;
        }
        if (cachedIndex === akpos + delta) {
            // Inplace update
            updateNodeInUpdateChildren(newChildren[newIndex], cachedChildren, cachedIndex, cachedLength, createBefore, element, deepness);
            newIndex++;
            cachedIndex++;
        }
        else {
            // Move
            cachedChildren.splice(cachedIndex, 0, cachedChildren[akpos + delta]);
            delta++;
            cachedChildren[akpos + delta] = null;
            reorderAndUpdateNodeInUpdateChildren(newChildren[newIndex], cachedChildren, cachedIndex, cachedLength, createBefore, element, deepness);
            cachedIndex++;
            cachedEnd++;
            cachedLength++;
            newIndex++;
        }
    }
    // remove old keyed cached nodes
    while (cachedIndex < cachedEnd) {
        if (cachedChildren[cachedIndex] === null) {
            cachedChildren.splice(cachedIndex, 1);
            cachedEnd--;
            cachedLength--;
            continue;
        }
        if (cachedChildren[cachedIndex].key != null) {
            removeNode(cachedChildren[cachedIndex]);
            cachedChildren.splice(cachedIndex, 1);
            cachedEnd--;
            cachedLength--;
            continue;
        }
        cachedIndex++;
    }
    // add new keyed nodes
    while (newIndex < newEnd) {
        key = newChildren[newIndex].key;
        if (key != null) {
            cachedChildren.splice(cachedIndex, 0, createNode(newChildren[newIndex], parentNode, element, findNextNode(cachedChildren, cachedIndex - 1, cachedLength, createBefore)));
            cachedEnd++;
            cachedLength++;
            delta++;
            cachedIndex++;
        }
        newIndex++;
    }
    // Without any keyless nodes we are done
    if (!keyLess)
        return cachedChildren;
    // calculate common (old and new) keyless
    keyLess = (keyLess - Math.abs(deltaKeyless)) >> 1;
    // reorder just nonkeyed nodes
    newIndex = backupNewIndex;
    cachedIndex = backupCachedIndex;
    while (newIndex < newEnd) {
        if (cachedIndex < cachedEnd) {
            cachedKey = cachedChildren[cachedIndex].key;
            if (cachedKey != null) {
                cachedIndex++;
                continue;
            }
        }
        key = newChildren[newIndex].key;
        if (newIndex < cachedEnd && key === cachedChildren[newIndex].key) {
            if (key != null) {
                newIndex++;
                continue;
            }
            updateNodeInUpdateChildren(newChildren[newIndex], cachedChildren, newIndex, cachedLength, createBefore, element, deepness);
            keyLess--;
            newIndex++;
            cachedIndex = newIndex;
            continue;
        }
        if (key != null) {
            assert(newIndex === cachedIndex);
            if (keyLess === 0 && deltaKeyless < 0) {
                while (true) {
                    removeNode(cachedChildren[cachedIndex]);
                    cachedChildren.splice(cachedIndex, 1);
                    cachedEnd--;
                    cachedLength--;
                    deltaKeyless++;
                    assert(cachedIndex !== cachedEnd, "there still need to exist key node");
                    if (cachedChildren[cachedIndex].key != null)
                        break;
                }
                continue;
            }
            while (cachedChildren[cachedIndex].key == null)
                cachedIndex++;
            assert(key === cachedChildren[cachedIndex].key);
            cachedChildren.splice(newIndex, 0, cachedChildren[cachedIndex]);
            cachedChildren.splice(cachedIndex + 1, 1);
            reorderInUpdateChildren(cachedChildren, newIndex, cachedLength, createBefore);
            // just moving keyed node it was already updated before
            newIndex++;
            cachedIndex = newIndex;
            continue;
        }
        if (cachedIndex < cachedEnd) {
            cachedChildren.splice(newIndex, 0, cachedChildren[cachedIndex]);
            cachedChildren.splice(cachedIndex + 1, 1);
            reorderAndUpdateNodeInUpdateChildren(newChildren[newIndex], cachedChildren, newIndex, cachedLength, createBefore, element, deepness);
            keyLess--;
            newIndex++;
            cachedIndex++;
        }
        else {
            cachedChildren.splice(newIndex, 0, createNode(newChildren[newIndex], parentNode, element, findNextNode(cachedChildren, newIndex - 1, cachedLength, createBefore)));
            cachedEnd++;
            cachedLength++;
            newIndex++;
            cachedIndex++;
        }
    }
    while (cachedEnd > newIndex) {
        cachedEnd--;
        removeNode(cachedChildren[cachedEnd]);
        cachedChildren.splice(cachedEnd, 1);
    }
    return cachedChildren;
}
var hasNativeRaf = false;
var nativeRaf = window.requestAnimationFrame;
if (nativeRaf) {
    nativeRaf(function (param) { if (param === +param)
        hasNativeRaf = true; });
}
var truenow = Date.now || (function () { return (new Date).getTime(); });
var startTime = truenow();
var lastTickTime = 0;
var mockedTime = -1;
function now() {
    if (mockedTime === -1)
        return truenow();
    return mockedTime;
}
exports.now = now;
function requestAnimationFrame(callback) {
    if (hasNativeRaf) {
        nativeRaf(callback);
    }
    else {
        var delay = 50 / 3 + lastTickTime - truenow();
        if (delay < 0)
            delay = 0;
        window.setTimeout(function () {
            lastTickTime = truenow();
            callback(lastTickTime - startTime);
        }, delay);
    }
}
var ctxInvalidated = "$invalidated";
var ctxDeepness = "$deepness";
var fullRecreateRequested = true;
var scheduled = false;
var uptimeMs = 0;
var frameCounter = 0;
var lastFrameDurationMs = 0;
var renderFrameBegin = 0;
var regEvents = {};
var registryEvents;
function addEvent(name, priority, callback) {
    if (registryEvents == null)
        registryEvents = {};
    var list = registryEvents[name] || [];
    list.push({ priority: priority, callback: callback });
    registryEvents[name] = list;
}
exports.addEvent = addEvent;
gw.setEventHandler(function (name, param, nodeId, time) {
    var node = nodeId2Node[nodeId];
    var backupMockedTime = mockedTime;
    if (time != -1)
        mockedTime = time;
    var res = emitEvent(name, param, node);
    mockedTime = backupMockedTime;
    return res;
});
function emitEvent(name, ev, node) {
    var events = regEvents[name];
    if (events)
        for (var i = 0; i < events.length; i++) {
            if (events[i](ev, node))
                return true;
        }
    return false;
}
exports.emitEvent = emitEvent;
function initEvents() {
    if (registryEvents == null)
        return;
    var eventNames = Object.keys(registryEvents);
    for (var j = 0; j < eventNames.length; j++) {
        var eventName = eventNames[j];
        var arr = registryEvents[eventName];
        arr = arr.sort(function (a, b) { return a.priority - b.priority; });
        regEvents[eventName] = arr.map(function (v) { return v.callback; });
    }
    registryEvents = undefined;
}
function selectedUpdate(cache, element, createBefore) {
    var len = cache.length;
    for (var i = 0; i < len; i++) {
        var node = cache[i];
        var ctx = node.ctx;
        if (ctx != null && ctx[ctxInvalidated] === frameCounter) {
            var cloned = { data: ctx.data, component: node.component };
            cache[i] = updateNode(cloned, node, element, createBefore, ctx[ctxDeepness]);
        }
        else if (exports.isArray(node.children)) {
            var backupInNotFocusable = inNotFocusable;
            if (inNotFocusable && focusRootTop === node)
                inNotFocusable = false;
            selectedUpdate(node.children, node.nodeId, findNextNode(cache, i, len, createBefore));
            inNotFocusable = backupInNotFocusable;
        }
    }
}
var beforeFrameCallback = function () { };
var afterFrameCallback = function () { };
function setBeforeFrame(callback) {
    var res = beforeFrameCallback;
    beforeFrameCallback = callback;
    return res;
}
exports.setBeforeFrame = setBeforeFrame;
function setAfterFrame(callback) {
    var res = afterFrameCallback;
    afterFrameCallback = callback;
    return res;
}
exports.setAfterFrame = setAfterFrame;
function isLogicalParent(parent, child, rootIds) {
    while (child != null) {
        if (parent === child)
            return true;
        var p = child.parent;
        if (p == null) {
            for (var i = 0; i < rootIds.length; i++) {
                var r = roots[rootIds[i]];
                if (!r)
                    continue;
                var rc = r.c;
                if (rc.indexOf(child) >= 0) {
                    p = r.p;
                    break;
                }
            }
        }
        child = p;
    }
    return false;
}
function syncUpdate() {
    internalUpdate(truenow() - startTime);
}
exports.syncUpdate = syncUpdate;
function update(time) {
    scheduled = false;
    internalUpdate(time);
}
function internalUpdate(time) {
    renderFrameBegin = truenow();
    initEvents();
    frameCounter++;
    ignoringShouldChange = nextIgnoreShouldChange;
    nextIgnoreShouldChange = false;
    uptimeMs = time;
    beforeFrameCallback();
    focusRootTop = focusRootStack.length === 0 ? undefined : focusRootStack[focusRootStack.length - 1];
    inNotFocusable = false;
    var fullRefresh = false;
    if (fullRecreateRequested) {
        fullRecreateRequested = false;
        fullRefresh = true;
    }
    var rootIds = Object.keys(roots);
    for (var i = 0; i < rootIds.length; i++) {
        var r = roots[rootIds[i]];
        if (!r)
            continue;
        var rc = r.c;
        if (focusRootTop)
            inNotFocusable = !isLogicalParent(focusRootTop, r.p, rootIds);
        // TODO: track root order and calculated insertBefore
        if (fullRefresh) {
            var newChildren = r.f();
            if (newChildren === undefined)
                break;
            r.c = updateChildren(0, newChildren, rc, undefined, 0, 1e6);
        }
        else {
            selectedUpdate(rc, 0, 0);
        }
    }
    var r0 = roots["0"];
    afterFrameCallback(r0 ? r0.c : undefined);
    lastFrameDurationMs = truenow() - renderFrameBegin;
}
var nextIgnoreShouldChange = false;
var ignoringShouldChange = false;
function ignoreShouldChange() {
    nextIgnoreShouldChange = true;
    exports.invalidate();
}
exports.ignoreShouldChange = ignoreShouldChange;
function setInvalidate(inv) {
    var prev = exports.invalidate;
    exports.invalidate = inv;
    return prev;
}
exports.setInvalidate = setInvalidate;
exports.invalidate = function (ctx, deepness) {
    if (ctx != null) {
        if (deepness == undefined)
            deepness = 1e6;
        if (ctx[ctxInvalidated] !== frameCounter + 1) {
            ctx[ctxInvalidated] = frameCounter + 1;
            ctx[ctxDeepness] = deepness;
        }
        else {
            if (deepness > ctx[ctxDeepness])
                ctx[ctxDeepness] = deepness;
        }
    }
    else {
        fullRecreateRequested = true;
    }
    if (scheduled)
        return;
    scheduled = true;
    requestAnimationFrame(update);
};
function forceInvalidate() {
    if (!scheduled)
        fullRecreateRequested = false;
    exports.invalidate();
}
var lastRootId = 0;
function addRoot(factory, parent) {
    lastRootId++;
    var rootId = "" + lastRootId;
    roots[rootId] = { f: factory, c: [], p: parent };
    forceInvalidate();
    return rootId;
}
exports.addRoot = addRoot;
function removeRoot(id) {
    var root = roots[id];
    if (!root)
        return;
    if (root.c.length) {
        root.c = updateChildren(0, [], root.c, undefined, 0, 1e9);
    }
    delete roots[id];
}
exports.removeRoot = removeRoot;
function getRoots() {
    return roots;
}
exports.getRoots = getRoots;
var beforeInit = forceInvalidate;
function init(factory) {
    removeRoot("0");
    roots["0"] = { f: factory, c: [], p: undefined };
    gw.readyPromise.then(function () {
        beforeInit();
        beforeInit = forceInvalidate;
    });
}
exports.init = init;
function setBeforeInit(callback) {
    var prevBeforeInit = beforeInit;
    beforeInit = function () {
        callback(prevBeforeInit);
    };
}
exports.setBeforeInit = setBeforeInit;
function bubble(node, name, param) {
    while (node) {
        var c = node.component;
        if (c) {
            var ctx = node.ctx;
            var m = c[name];
            if (m) {
                if (m.call(c, ctx, param))
                    return ctx;
            }
            m = c.shouldStopBubble;
            if (m) {
                if (m.call(c, ctx, name, param))
                    break;
            }
        }
        node = node.parent;
    }
    return undefined;
}
exports.bubble = bubble;
function broadcastEventToNode(node, name, param) {
    if (!node)
        return undefined;
    var c = node.component;
    if (c) {
        var ctx = node.ctx;
        var m = c[name];
        if (m) {
            if (m.call(c, ctx, param))
                return ctx;
        }
        m = c.shouldStopBroadcast;
        if (m) {
            if (m.call(c, ctx, name, param))
                return undefined;
        }
    }
    var ch = node.children;
    if (exports.isArray(ch)) {
        for (var i = 0; i < ch.length; i++) {
            var res = broadcastEventToNode(ch[i], name, param);
            if (res != null)
                return res;
        }
    }
    return undefined;
}
function broadcast(name, param) {
    var k = Object.keys(roots);
    for (var i = 0; i < k.length; i++) {
        var ch = roots[k[i]].c;
        if (ch != null) {
            for (var j = 0; j < ch.length; j++) {
                var res = broadcastEventToNode(ch[j], name, param);
                if (res != null)
                    return res;
            }
        }
    }
    return undefined;
}
exports.broadcast = broadcast;
function merge(f1, f2) {
    return function () {
        var params = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            params[_i - 0] = arguments[_i];
        }
        var result = f1.apply(this, params);
        if (result)
            return result;
        return f2.apply(this, params);
    };
}
var emptyObject = {};
function mergeComponents(c1, c2) {
    var res = Object.create(c1);
    res.super = c1;
    for (var i in c2) {
        if (!(i in emptyObject)) {
            var m = c2[i];
            var origM = c1[i];
            if (i === "id") {
                res[i] = ((origM != null) ? origM : "") + "/" + m;
            }
            else if (isFunction(m) && origM != null && isFunction(origM)) {
                res[i] = merge(origM, m);
            }
            else {
                res[i] = m;
            }
        }
    }
    return res;
}
function overrideComponents(originalComponent, overridingComponent) {
    var res = Object.create(originalComponent);
    res.super = originalComponent;
    for (var i in overridingComponent) {
        if (!(i in emptyObject)) {
            var m = overridingComponent[i];
            var origM = originalComponent[i];
            if (i === 'id') {
                res[i] = ((origM != null) ? origM : '') + '/' + m;
            }
            else {
                res[i] = m;
            }
        }
    }
    return res;
}
function preEnhance(node, methods) {
    var comp = node.component;
    if (!comp) {
        node.component = methods;
        return node;
    }
    node.component = mergeComponents(methods, comp);
    return node;
}
exports.preEnhance = preEnhance;
function postEnhance(node, methods) {
    var comp = node.component;
    if (!comp) {
        node.component = methods;
        return node;
    }
    node.component = mergeComponents(comp, methods);
    return node;
}
exports.postEnhance = postEnhance;
function preventDefault(event) {
    var pd = event.preventDefault;
    if (pd)
        pd.call(event);
    else
        event.returnValue = false;
}
exports.preventDefault = preventDefault;
function cloneNodeArray(a) {
    a = a.slice(0);
    for (var i = 0; i < a.length; i++) {
        var n = a[i];
        if (exports.isArray(n)) {
            a[i] = cloneNodeArray(n);
        }
        else if (isObject(n)) {
            a[i] = cloneNode(n);
        }
    }
    return a;
}
function cloneNode(node) {
    var r = exports.assign({}, node);
    if (r.attrs) {
        r.attrs = exports.assign({}, r.attrs);
    }
    if (isObject(r.style)) {
        r.style = exports.assign({}, r.style);
    }
    var ch = r.children;
    if (ch) {
        if (exports.isArray(ch)) {
            r.children = cloneNodeArray(ch);
        }
        else if (isObject(ch)) {
            r.children = cloneNode(ch);
        }
    }
    return r;
}
exports.cloneNode = cloneNode;
// PureFuncs: uptime, lastFrameDuration, frame, invalidated
function uptime() { return uptimeMs; }
exports.uptime = uptime;
function lastFrameDuration() { return lastFrameDurationMs; }
exports.lastFrameDuration = lastFrameDuration;
function frame() { return frameCounter; }
exports.frame = frame;
function invalidated() { return scheduled; }
exports.invalidated = invalidated;
var media = undefined;
var breaks = [
    [414, 800, 900],
    [736, 1280, 1440] //landscape widths
];
var lastResizeInfo = {
    width: 600,
    height: 800,
    rotation: 0,
    density: 1
};
function onResize(ev) {
    lastResizeInfo = ev;
    media = undefined;
    exports.invalidate();
    return false;
}
addEvent("onResize", 10, onResize);
function accDeviceBreaks(newBreaks) {
    if (newBreaks != null) {
        breaks = newBreaks;
        media = undefined;
        exports.invalidate();
    }
    return breaks;
}
exports.accDeviceBreaks = accDeviceBreaks;
function getMedia() {
    if (media == null) {
        var w = lastResizeInfo.width;
        var h = lastResizeInfo.height;
        var o = lastResizeInfo.rotation;
        var p = h >= w;
        var device = 0;
        while (w > breaks[+!p][device])
            device++;
        media = {
            width: w,
            height: h,
            orientation: o,
            deviceCategory: device,
            portrait: p,
            density: lastResizeInfo.density
        };
    }
    return media;
}
exports.getMedia = getMedia;
addEvent("pointerDown", 10, function (ev, node) {
    var event = {
        x: ev.x,
        y: ev.y,
        button: 1,
        count: 1,
        shift: false,
        ctrl: false,
        alt: false,
        meta: false,
        id: ev.id,
        type: 1 /* Touch */
    };
    return bubble(node, "onPointerDown", event) != null;
});
addEvent("pointerMove", 10, function (ev, node) {
    var event = {
        x: ev.x,
        y: ev.y,
        button: 1,
        count: 1,
        shift: false,
        ctrl: false,
        alt: false,
        meta: false,
        id: ev.id,
        type: 1 /* Touch */
    };
    return bubble(node, "onPointerMove", event) != null;
});
addEvent("pointerUp", 10, function (ev, node) {
    var event = {
        x: ev.x,
        y: ev.y,
        button: 1,
        count: 1,
        shift: false,
        ctrl: false,
        alt: false,
        meta: false,
        id: ev.id,
        type: 1 /* Touch */
    };
    return bubble(node, "onPointerUp", event) != null;
});
addEvent("pointerCancel", 10, function (ev, node) {
    var event = {
        x: 0,
        y: 0,
        button: 1,
        count: 1,
        shift: false,
        ctrl: false,
        alt: false,
        meta: false,
        id: ev.id,
        type: 1 /* Touch */
    };
    return bubble(node, "pointerCancel", event) != null;
});
var allStyles = newHashObj();
var allSprites = newHashObj();
var allNameHints = newHashObj();
var dynamicSprites = [];
var imageCache = newHashObj();
var rebuildStyles = false;
var globalCounter = 0;
var chainedBeforeFrame = setBeforeFrame(beforeFrame);
function flattenStyle(cur, curPseudo, style, stylePseudo) {
    if (isString(style)) {
        var externalStyle = allStyles[style];
        if (externalStyle === undefined) {
            throw new Error("uknown style " + style);
        }
        flattenStyle(cur, curPseudo, externalStyle.style, externalStyle.pseudo);
    }
    else if (isFunction(style)) {
        style(cur, curPseudo);
    }
    else if (exports.isArray(style)) {
        for (var i = 0; i < style.length; i++) {
            flattenStyle(cur, curPseudo, style[i], undefined);
        }
    }
    else if (typeof style === "object") {
        for (var key in style) {
            if (!Object.prototype.hasOwnProperty.call(style, key))
                continue;
            var val = style[key];
            if (isFunction(val)) {
                val = val(cur, key);
            }
            cur[key] = val;
        }
    }
    if (stylePseudo != null && curPseudo != null) {
        for (var pseudoKey in stylePseudo) {
            var curPseudoVal = curPseudo[pseudoKey];
            if (curPseudoVal === undefined) {
                curPseudoVal = newHashObj();
                curPseudo[pseudoKey] = curPseudoVal;
            }
            flattenStyle(curPseudoVal, undefined, stylePseudo[pseudoKey], undefined);
        }
    }
}
function beforeFrame() {
    if (rebuildStyles) {
        for (var i = 0; i < dynamicSprites.length; i++) {
            var dynSprite = dynamicSprites[i];
            var image = imageCache[dynSprite.url];
            if (image == null)
                continue;
            var colorStr = dynSprite.color();
            if (colorStr !== dynSprite.lastColor) {
                dynSprite.lastColor = colorStr;
                if (dynSprite.width == null)
                    dynSprite.width = image.width;
                if (dynSprite.height == null)
                    dynSprite.height = image.height;
                var lastUrl = recolorAndClip(image, colorStr, dynSprite.width, dynSprite.height, dynSprite.left, dynSprite.top);
                var stDef = allStyles[dynSprite.styleid];
                stDef.style = { backgroundImage: "url(" + lastUrl + ")", width: dynSprite.width, height: dynSprite.height, backgroundPosition: 0 };
            }
        }
        for (var key in allStyles) {
            var ss = allStyles[key];
            var name_1 = ss.name;
            var sspseudo = ss.pseudo;
            var ssstyle = ss.style;
            if (isFunction(ssstyle) && ssstyle.length === 0) {
                _a = ssstyle(), ssstyle = _a[0], sspseudo = _a[1];
            }
            if (isString(ssstyle) && sspseudo == null) {
                ss.realname = ssstyle;
                assert(name_1 != null, "Cannot link existing class to selector");
                continue;
            }
            ss.realname = name_1;
            var style_1 = newHashObj();
            var flattenPseudo = newHashObj();
            flattenStyle(undefined, flattenPseudo, undefined, sspseudo);
            flattenStyle(style_1, flattenPseudo, ssstyle, undefined);
            gwSetStyleDef(name_1, style_1, flattenPseudo);
        }
        rebuildStyles = false;
    }
    chainedBeforeFrame();
    var _a;
}
function style(node) {
    var styles = [];
    for (var _i = 1; _i < arguments.length; _i++) {
        styles[_i - 1] = arguments[_i];
    }
    var inlineStyle = node.style;
    var stack = undefined;
    var i = 0;
    var ca = styles;
    while (true) {
        if (ca.length === i) {
            if (stack === undefined || stack.length === 0)
                break;
            ca = stack.pop();
            i = stack.pop() + 1;
            continue;
        }
        var s = ca[i];
        if (s == null || s === true || s === false || s === '') {
        }
        else if (isString(s)) {
            var sd = allStyles[s];
            if (inlineStyle == null)
                inlineStyle = [];
            inlineStyle.push(sd.realname, undefined);
        }
        else if (exports.isArray(s)) {
            if (ca.length > i + 1) {
                if (stack == null)
                    stack = [];
                stack.push(i, ca);
            }
            ca = s;
            i = 0;
            continue;
        }
        else {
            if (inlineStyle == null)
                inlineStyle = [];
            for (var key in s) {
                if (s.hasOwnProperty(key)) {
                    var val = s[key];
                    if (isFunction(val))
                        val = val();
                    if (val == null)
                        continue;
                    inlineStyle.push(key, val);
                }
            }
        }
        i++;
    }
    node.style = inlineStyle;
    return node;
}
exports.style = style;
// PureFuncs: styleDef, styleDefEx, sprite, spriteb, spritebc, asset
function styleDef(style, pseudo, nameHint) {
    return styleDefEx(undefined, style, pseudo, nameHint);
}
exports.styleDef = styleDef;
function styleDefEx(parent, style, pseudo, nameHint) {
    if (nameHint && nameHint !== "b-") {
        if (allNameHints[nameHint]) {
            var counter = 1;
            while (allNameHints[nameHint + counter])
                counter++;
            nameHint = nameHint + counter;
        }
        allNameHints[nameHint] = true;
    }
    else {
        nameHint = "b-" + globalCounter++;
    }
    allStyles[nameHint] = { name: nameHint, realname: nameHint, parent: parent, style: style, pseudo: pseudo };
    invalidateStyles();
    return nameHint;
}
function invalidateStyles() {
    rebuildStyles = true;
    exports.invalidate();
}
exports.invalidateStyles = invalidateStyles;
function updateSprite(spDef) {
    var stDef = allStyles[spDef.styleid];
    var style = { backgroundImage: "url(" + spDef.url + ")", width: spDef.width, height: spDef.height };
    style.backgroundPosition = -spDef.left + "px " + -spDef.top + "px";
    stDef.style = style;
    invalidateStyles();
}
function emptyStyleDef(url) {
    return styleDef({ width: 0, height: 0 }, undefined, url.replace(/[^a-z0-9_-]/gi, '_'));
}
var rgbaRegex = /\s*rgba\(\s*(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*(\d+|\d*\.\d+)\s*\)\s*/;
function recolorAndClip(image, colorStr, width, height, left, top) {
    var canvas = document.createElement("canvas");
    canvas.width = width;
    canvas.height = height;
    var ctx = canvas.getContext("2d");
    ctx.drawImage(image, -left, -top);
    var imgdata = ctx.getImageData(0, 0, width, height);
    var imgd = imgdata.data;
    var rgba = rgbaRegex.exec(colorStr);
    var cred, cgreen, cblue, calpha;
    if (rgba) {
        cred = parseInt(rgba[1], 10);
        cgreen = parseInt(rgba[2], 10);
        cblue = parseInt(rgba[3], 10);
        calpha = Math.round(parseFloat(rgba[4]) * 255);
    }
    else {
        cred = parseInt(colorStr.substr(1, 2), 16);
        cgreen = parseInt(colorStr.substr(3, 2), 16);
        cblue = parseInt(colorStr.substr(5, 2), 16);
        calpha = parseInt(colorStr.substr(7, 2), 16) || 0xff;
    }
    if (calpha === 0xff) {
        for (var i = 0; i < imgd.length; i += 4) {
            // Horrible workaround for imprecisions due to browsers using premultiplied alpha internally for canvas
            var red = imgd[i];
            if (red === imgd[i + 1] && red === imgd[i + 2] && (red === 0x80 || imgd[i + 3] < 0xff && red > 0x70)) {
                imgd[i] = cred;
                imgd[i + 1] = cgreen;
                imgd[i + 2] = cblue;
            }
        }
    }
    else {
        for (var i = 0; i < imgd.length; i += 4) {
            var red = imgd[i];
            var alpha = imgd[i + 3];
            if (red === imgd[i + 1] && red === imgd[i + 2] && (red === 0x80 || alpha < 0xff && red > 0x70)) {
                if (alpha === 0xff) {
                    imgd[i] = cred;
                    imgd[i + 1] = cgreen;
                    imgd[i + 2] = cblue;
                    imgd[i + 3] = calpha;
                }
                else {
                    alpha = alpha * (1.0 / 255);
                    imgd[i] = Math.round(cred * alpha);
                    imgd[i + 1] = Math.round(cgreen * alpha);
                    imgd[i + 2] = Math.round(cblue * alpha);
                    imgd[i + 3] = Math.round(calpha * alpha);
                }
            }
        }
    }
    ctx.putImageData(imgdata, 0, 0);
    return canvas.toDataURL();
}
var lastFuncId = 0;
var funcIdName = "b@funcId";
function sprite(url, color, width, height, left, top) {
    left = left || 0;
    top = top || 0;
    var colorId = color || "";
    var isVarColor = false;
    if (isFunction(color)) {
        isVarColor = true;
        colorId = color[funcIdName];
        if (colorId == null) {
            colorId = "" + (lastFuncId++);
            color[funcIdName] = colorId;
        }
    }
    var key = url + ":" + colorId + ":" + (width || 0) + ":" + (height || 0) + ":" + left + ":" + top;
    var spDef = allSprites[key];
    if (spDef)
        return spDef.styleid;
    var styleid = emptyStyleDef(url);
    spDef = { styleid: styleid, url: url, width: width, height: height, left: left, top: top };
    if (isVarColor) {
        spDef.color = color;
        spDef.lastColor = '';
        spDef.lastUrl = '';
        dynamicSprites.push(spDef);
        if (imageCache[url] === undefined) {
            imageCache[url] = null;
            var image = new Image();
            image.addEventListener("load", function () {
                imageCache[url] = image;
                invalidateStyles();
            });
            image.src = url;
        }
        invalidateStyles();
    }
    else if (width == null || height == null || color != null) {
        var image = new Image();
        image.addEventListener("load", function () {
            if (spDef.width == null)
                spDef.width = image.width;
            if (spDef.height == null)
                spDef.height = image.height;
            if (color != null) {
                spDef.url = recolorAndClip(image, color, spDef.width, spDef.height, spDef.left, spDef.top);
                spDef.left = 0;
                spDef.top = 0;
            }
            updateSprite(spDef);
        });
        image.src = url;
    }
    else {
        updateSprite(spDef);
    }
    allSprites[key] = spDef;
    return styleid;
}
exports.sprite = sprite;
var bundlePath = window['bobrilBPath'] || 'bundle.png';
function setBundlePngPath(path) {
    bundlePath = path;
}
exports.setBundlePngPath = setBundlePngPath;
function spriteb(width, height, left, top) {
    var url = bundlePath;
    var key = url + "::" + width + ":" + height + ":" + left + ":" + top;
    var spDef = allSprites[key];
    if (spDef)
        return spDef.styleid;
    var styleid = styleDef({ width: 0, height: 0 });
    spDef = { styleid: styleid, url: url, width: width, height: height, left: left, top: top };
    updateSprite(spDef);
    allSprites[key] = spDef;
    return styleid;
}
exports.spriteb = spriteb;
function spritebc(color, width, height, left, top) {
    return sprite(bundlePath, color, width, height, left, top);
}
exports.spritebc = spritebc;
function asset(path) {
    return path;
}
exports.asset = asset;
// Bobril.svgExtensions
function polarToCartesian(centerX, centerY, radius, angleInDegrees) {
    var angleInRadians = angleInDegrees * Math.PI / 180.0;
    return {
        x: centerX + (radius * Math.sin(angleInRadians)), y: centerY - (radius * Math.cos(angleInRadians))
    };
}
function svgDescribeArc(x, y, radius, startAngle, endAngle, startWithLine) {
    var absDeltaAngle = Math.abs(endAngle - startAngle);
    var close = false;
    if (absDeltaAngle > 360 - 0.01) {
        if (endAngle > startAngle)
            endAngle = startAngle - 359.9;
        else
            endAngle = startAngle + 359.9;
        if (radius === 0)
            return "";
        close = true;
    }
    else {
        if (radius === 0) {
            return [
                startWithLine ? "L" : "M", x, y
            ].join(" ");
        }
    }
    var start = polarToCartesian(x, y, radius, endAngle);
    var end = polarToCartesian(x, y, radius, startAngle);
    var arcSweep = (absDeltaAngle <= 180) ? "0" : "1";
    var largeArg = (endAngle > startAngle) ? "0" : "1";
    var d = [
        (startWithLine ? "L" : "M"), start.x, start.y, "A", radius, radius, 0, arcSweep, largeArg, end.x, end.y
    ].join(" ");
    if (close)
        d += "Z";
    return d;
}
function svgPie(x, y, radiusBig, radiusSmall, startAngle, endAngle) {
    var p = svgDescribeArc(x, y, radiusBig, startAngle, endAngle, false);
    var nextWithLine = true;
    if (p[p.length - 1] === "Z")
        nextWithLine = false;
    if (radiusSmall === 0) {
        if (!nextWithLine)
            return p;
    }
    return p + svgDescribeArc(x, y, radiusSmall, endAngle, startAngle, nextWithLine) + "Z";
}
exports.svgPie = svgPie;
function svgCircle(x, y, radius) {
    return svgDescribeArc(x, y, radius, 0, 360, false);
}
exports.svgCircle = svgCircle;
function svgRect(x, y, width, height) {
    return "M" + x + " " + y + "h" + width + "v" + height + "h" + (-width) + "Z";
}
exports.svgRect = svgRect;
// Bobril.helpers
function withKey(node, key) {
    node.key = key;
    return node;
}
exports.withKey = withKey;
// PureFuncs: styledDiv, createVirtualComponent, createComponent, createDerivedComponent, createOverridingComponent, prop, propi, propim, propa, getValue
function styledDiv(children) {
    var styles = [];
    for (var _i = 1; _i < arguments.length; _i++) {
        styles[_i - 1] = arguments[_i];
    }
    return style({ tag: 'View', children: children }, styles);
}
exports.styledDiv = styledDiv;
function createVirtualComponent(component) {
    return function (data, children) {
        if (children !== undefined) {
            if (data == null)
                data = {};
            data.children = children;
        }
        return { data: data, component: component };
    };
}
exports.createVirtualComponent = createVirtualComponent;
function createOverridingComponent(original, after) {
    var originalComponent = original().component;
    var overriding = overrideComponents(originalComponent, after);
    return createVirtualComponent(overriding);
}
exports.createOverridingComponent = createOverridingComponent;
function createComponent(component) {
    var originalRender = component.render;
    if (originalRender) {
        component.render = function (ctx, me, oldMe) {
            me.tag = 'View';
            return originalRender.call(component, ctx, me, oldMe);
        };
    }
    else {
        component.render = function (_ctx, me) { me.tag = 'div'; };
    }
    return createVirtualComponent(component);
}
exports.createComponent = createComponent;
function createDerivedComponent(original, after) {
    var originalComponent = original().component;
    var merged = mergeComponents(originalComponent, after);
    return createVirtualComponent(merged);
}
exports.createDerivedComponent = createDerivedComponent;
function prop(value, onChange) {
    return function (val) {
        if (val !== undefined) {
            if (onChange !== undefined)
                onChange(val, value);
            value = val;
        }
        return value;
    };
}
exports.prop = prop;
function propi(value) {
    return function (val) {
        if (val !== undefined) {
            value = val;
            exports.invalidate();
        }
        return value;
    };
}
exports.propi = propi;
function propim(value, ctx, onChange) {
    return function (val) {
        if (val !== undefined && val !== value) {
            var oldVal = val;
            value = val;
            if (onChange !== undefined)
                onChange(val, oldVal);
            exports.invalidate(ctx);
        }
        return value;
    };
}
exports.propim = propim;
function propa(prop) {
    return function (val) {
        if (val !== undefined) {
            if (typeof val === "object" && isFunction(val.then)) {
                val.then(function (v) {
                    prop(v);
                }, function (err) {
                    if (window["console"] && console.error)
                        console.error(err);
                });
            }
            else {
                return prop(val);
            }
        }
        return prop();
    };
}
exports.propa = propa;
function getValue(value) {
    if (isFunction(value)) {
        return value();
    }
    return value;
}
exports.getValue = getValue;
function emitChange(data, value) {
    if (isFunction(data.value)) {
        data.value(value);
    }
    if (data.onChange !== undefined) {
        data.onChange(value);
    }
}
exports.emitChange = emitChange;
// bobril-clouseau needs this
if (!window.b)
    window.b = { deref: deref, getRoots: getRoots, setInvalidate: setInvalidate, invalidateStyles: invalidateStyles, ignoreShouldChange: ignoreShouldChange, setAfterFrame: setAfterFrame, setBeforeInit: setBeforeInit, setBeforeFrame: setBeforeFrame };
// TSX reactNamespace emulation
// PureFuncs: createElement
function createElement(name, props) {
    var children = [];
    for (var i = 2; i < arguments.length; i++) {
        var ii = arguments[i];
        children.push(ii);
    }
    if (isString(name)) {
        var res = { tag: name, children: children };
        if (props == null) {
            return res;
        }
        var attrs = {};
        var someattrs = false;
        for (var n in props) {
            if (!props.hasOwnProperty(n))
                continue;
            if (n === "style") {
                style(res, props[n]);
                continue;
            }
            if (n === "key" || n === "ref" || n === "className" || n === "component" || n === "data") {
                res[n] = props[n];
                continue;
            }
            someattrs = true;
            attrs[n] = props[n];
        }
        if (someattrs)
            res.attrs = attrs;
        return res;
    }
    else {
        var res_1 = name(props, children);
        if (props != null) {
            if (props.key != null)
                res_1.key = props.key;
            if (props.ref != null)
                res_1.ref = props.ref;
        }
        return res_1;
    }
}
exports.createElement = createElement;
exports.__spread = exports.assign;
