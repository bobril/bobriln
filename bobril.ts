import * as gw from './gateway';

function gwInsertBefore(nodeId: number, createInto: number, createBefore: number, tag?: string) {
    let p = gw.prepareToCallNativeByName("b.insert");
    p.writeUInt(nodeId);
    p.writeUInt(createInto);
    p.writeUInt(createBefore);
    if (tag != null) p.writeString(tag); else p.writeNull();
    gw.callNativeIgnoreResult();
}

function gwSetAttr(nodeId: number, attrName: string, newAttr: any) {
    let p = gw.prepareToCallNativeByName("b.setAttr");
    p.writeUInt(nodeId);
    p.writeString(attrName);
    p.writeAny(newAttr);
    gw.callNativeIgnoreResult();
}

function gwUnsetAttr(nodeId: number, attrName: string) {
    let p = gw.prepareToCallNativeByName("b.unsetAttr");
    p.writeUInt(nodeId);
    p.writeString(attrName);
    gw.callNativeIgnoreResult();
}

function gwSetStyle(nodeId: number, style: IBobrilCacheStyles) {
    let p = gw.prepareToCallNativeByName("b.setStyle");
    p.writeUInt(nodeId);
    p.writeAny(style);
    gw.callNativeIgnoreResult();
}

function gwSetStringChild(nodeId: number, text: string) {
    let p = gw.prepareToCallNativeByName("b.setStringChild");
    p.writeUInt(nodeId);
    p.writeString(text);
    gw.callNativeIgnoreResult();
}

function gwUnsetStringChild(nodeId: number) {
    let p = gw.prepareToCallNativeByName("b.unsetStringChild");
    p.writeUInt(nodeId);
    gw.callNativeIgnoreResult();
}

function gwRemoveNode(nodeId: number) {
    let p = gw.prepareToCallNativeByName("b.removeNode");
    p.writeUInt(nodeId);
    gw.callNativeIgnoreResult();
}

function gwMoveBefore(nodeId: number, before: number) {
    let p = gw.prepareToCallNativeByName("b.moveBeforeNode");
    p.writeUInt(nodeId);
    p.writeUInt(before);
    gw.callNativeIgnoreResult();
}

function gwSetStyleDef(name: string, style: Object, flattenPseudo: Object) {
    let p = gw.prepareToCallNativeByName("b.setStyleDef");
    p.writeString(name);
    p.writeAny(style);
    p.writeAny(flattenPseudo);
    gw.callNativeIgnoreResult();
}

// Bobril.Core
export type IBobrilChild = boolean | number | string | IBobrilNode | null | undefined;
export type IBobrilChildren = IBobrilChild | IBobrilChildArray | undefined;
export interface IBobrilChildArray extends Array<IBobrilChildren> { };
export type IBobrilCacheChildren = string | IBobrilCacheNode[] | undefined;

export type IBobrilCacheStyles = (string | number | boolean | undefined)[];

export interface IDisposable {
    dispose(): void;
}

export type IDisposeFunction = (ctx?: any) => void;
export type IDisposableLike = IDisposable | IDisposeFunction;

export interface IBobrilRoot {
    // Factory function
    f: () => IBobrilChildren;
    // Virtual Dom Cache
    c: IBobrilCacheNode[];
    // Optional Logical parent
    p: IBobrilCacheNode | undefined;
}

export type IBobrilRoots = { [id: string]: IBobrilRoot };

export interface IBobrilAttributes {
    id?: string;
    href?: string;
    value?: boolean | string | string[] | IProp<boolean | string | string[]>;
    tabindex?: number;
    [name: string]: any;
}

export interface IBobrilComponent {
    // parent component of devired/overriding component
    super?: IBobrilComponent;
    // if id of old node is different from new node it is considered completely different so init will be called before render directly
    // it does prevent calling render method twice on same node
    id?: string;
    // called before new node in vdom should be created, me members (tag, attrs, children, ...) could be modified, ctx is initialized to { data: me.data||{}, me: me, cfg: fromparent }
    init?(ctx: IBobrilCtx, me: IBobrilCacheNode): void;
    // in case of update after shouldChange returns true, you can do any update/init tasks, ctx.data is updated to me.data and oldMe.component updated to me.component before calling this
    // in case of init this is called after init method, oldMe is equal to undefined in that case
    render?(ctx: IBobrilCtx, me: IBobrilNode, oldMe?: IBobrilCacheNode): void;
    // called after all children are rendered, but before updating own attrs
    // so this is useful for kind of layout in JS features
    postRender?(ctx: IBobrilCtx, me: IBobrilNode, oldMe?: IBobrilCacheNode): void;
    // return false when whole subtree should not be changed from last time, you can still update any me members except key, default implementation always return true
    shouldChange?(ctx: IBobrilCtx, me: IBobrilNode, oldMe: IBobrilCacheNode): boolean;
    // called just before removing node from dom
    destroy?(ctx: IBobrilCtx, me: IBobrilNode): void;
    // called when bubling event to parent so you could stop bubling without preventing default handling
    shouldStopBubble?(ctx: IBobrilCtx, name: string, param: Object): boolean;
    // called when broadcast wants to dive in this node so you could silence broadcast for you and your children
    shouldStopBroadcast?(ctx: IBobrilCtx, name: string, param: Object): boolean;

    // called on input element after any change with new value (string|boolean)
    onChange?(ctx: IBobrilCtx, value: any): void;
    // called on string input element when selection or caret position changes
    onSelectionChange?(ctx: IBobrilCtx, event: ISelectionChangeEvent): void;

    onKeyDown?(ctx: IBobrilCtx, event: IKeyDownUpEvent): boolean;
    onKeyUp?(ctx: IBobrilCtx, event: IKeyDownUpEvent): boolean;
    onKeyPress?(ctx: IBobrilCtx, event: IKeyPressEvent): boolean;

    // called on input element after click/tap
    onClick?(ctx: IBobrilCtx, event: IBobrilMouseEvent): boolean;
    onDoubleClick?(ctx: IBobrilCtx, event: IBobrilMouseEvent): boolean;
    onContextMenu?(ctx: IBobrilCtx, event: IBobrilMouseEvent): boolean;
    onMouseDown?(ctx: IBobrilCtx, event: IBobrilMouseEvent): boolean;
    onMouseUp?(ctx: IBobrilCtx, event: IBobrilMouseEvent): boolean;
    onMouseOver?(ctx: IBobrilCtx, event: IBobrilMouseEvent): boolean;
    onMouseEnter?(ctx: IBobrilCtx, event: IBobrilMouseEvent): void;
    onMouseLeave?(ctx: IBobrilCtx, event: IBobrilMouseEvent): void;
    onMouseIn?(ctx: IBobrilCtx, event: IBobrilMouseEvent): void;
    onMouseOut?(ctx: IBobrilCtx, event: IBobrilMouseEvent): void;
    onMouseMove?(ctx: IBobrilCtx, event: IBobrilMouseEvent): boolean;
    onMouseWheel?(ctx: IBobrilCtx, event: IBobrilMouseWheelEvent): boolean;
    onPointerDown?(ctx: IBobrilCtx, event: IBobrilPointerEvent): boolean;
    onPointerMove?(ctx: IBobrilCtx, event: IBobrilPointerEvent): boolean;
    onPointerUp?(ctx: IBobrilCtx, event: IBobrilPointerEvent): boolean;
    onPointerCancel?(ctx: IBobrilCtx, event: IBobrilPointerEvent): boolean;

    // this component gained focus
    onFocus?(ctx: IBobrilCtx): void;
    // this component lost focus
    onBlur?(ctx: IBobrilCtx): void;
    // focus moved from outside of this element to some child of this element
    onFocusIn?(ctx: IBobrilCtx): void;
    // focus moved from inside of this element to some outside element
    onFocusOut?(ctx: IBobrilCtx): void;

    // if drag should start, bubbled
    onDragStart?(ctx: IBobrilCtx, dndCtx: IDndStartCtx): boolean;

    // broadcasted after drag started/moved/changed
    onDrag?(ctx: IBobrilCtx, dndCtx: IDndCtx): boolean;
    // broadcasted after drag ended even if without any action
    onDragEnd?(ctx: IBobrilCtx, dndCtx: IDndCtx): boolean;

    // Do you want to allow to drop here? bubbled
    onDragOver?(ctx: IBobrilCtx, dndCtx: IDndOverCtx): boolean;
    // User want to drop draged object here - do it - onDragOver before had to set you target
    onDrop?(ctx: IBobrilCtx, dndCtx: IDndCtx): boolean;
}

// new node should atleast have tag or component or children member
export interface IBobrilNodeCommon {
    tag?: string;
    key?: string;
    style?: IBobrilCacheStyles;
    attrs?: IBobrilAttributes;
    children?: IBobrilChildren;
    ref?: [IBobrilCtx, string] | ((node: IBobrilCacheNode) => void);
    // set this for children to be set to their ctx.cfg, if undefined your own ctx.cfg will be used anyway
    cfg?: any;
    component?: IBobrilComponent;
    // Bobril does not touch this, it is completely for user passing custom data to component
    // It is very similar to props in ReactJs, it must be immutable, you have access to this through ctx.data
    data?: any;
}

export interface IBobrilNodeWithTag extends IBobrilNodeCommon {
    tag: string;
}

export interface IBobrilNodeWithComponent extends IBobrilNodeCommon {
    component: IBobrilComponent;
}

export interface IBobrilNodeWithChildren extends IBobrilNodeCommon {
    children: IBobrilChildren;
}

export type IBobrilNode = IBobrilNodeWithTag | IBobrilNodeWithComponent | IBobrilNodeWithChildren;

export interface IBobrilCacheNode {
    tag?: string;
    key: string;
    style?: IBobrilCacheStyles;
    attrs?: IBobrilAttributes;
    children: IBobrilCacheChildren;
    ref: [IBobrilCtx, string] | ((node: IBobrilCacheNode) => void);
    cfg: any;
    component?: IBobrilComponent;
    data: any;
    nodeId: number;
    parent: IBobrilCacheNode | undefined;
    ctx: IBobrilCtx | undefined;
}

export interface IBobrilCtx {
    // properties passed from parent component, treat it as immutable
    data?: any;
    me?: IBobrilCacheNode;
    // properties passed from parent component automatically, but could be extended for children to IBobrilNode.cfg
    cfg?: any;
    refs?: { [name: string]: IBobrilCacheNode | undefined };
    disposables?: IDisposableLike[];
}

export interface IBobrilScroll {
    node: IBobrilCacheNode;
}

export interface ISelectionChangeEvent {
    startPosition: number;
    // endPosition tries to be also caret position (does not work on any IE or Edge 12)
    endPosition: number;
}

declare var DEBUG: boolean;
if (typeof DEBUG === "undefined") DEBUG = true;

// PureFuncs: assert, isArray, isObject, flatten

function assert(shoudBeTrue: boolean, messageIfFalse?: string) {
    if (DEBUG && !shoudBeTrue)
        throw Error(messageIfFalse || "assertion failed");
}

export const isArray = Array.isArray;

export function isNumber(val: any): val is number {
    return typeof val == "number";
}

export function isString(val: any): val is string {
    return typeof val == "string";
}

export function isFunction(val: any): val is Function {
    return typeof val == "function";
}

export function isObject(val: any): val is Object {
    return typeof val === "object";
}

if (Object.assign == null) {
    Object.assign = function assign(target: Object, ..._sources: Object[]): Object {
        if (target == null) throw new TypeError('Target in assign cannot be undefined or null');
        let totalArgs = arguments.length;
        for (let i = 1; i < totalArgs; i++) {
            let source = arguments[i];
            if (source == null) continue;
            let keys = Object.keys(source);
            let totalKeys = keys.length;
            for (let j = 0; j < totalKeys; j++) {
                let key = keys[j];
                (<any>target)[key] = (<any>source)[key];
            }
        }
        return target;
    }
}

export let assign = Object.assign;

export function flatten(a: any | any[]): any[] {
    if (!isArray(a)) {
        if (a == null || a === false || a === true)
            return [];
        return [a];
    }
    a = a.slice(0);
    let alen = a.length;
    for (let i = 0; i < alen;) {
        let item = a[i];
        if (isArray(item)) {
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

var inNotFocusable: boolean = false;

function newHashObj() {
    return Object.create(null);
}

export function ieVersion() {
    return undefined;
}

const tabindexStr = "tabindex";
const tvalue = "value";

function updateElement(nodeId: number, newAttrs: IBobrilAttributes | undefined, oldAttrs: IBobrilAttributes, notFocusable: boolean): IBobrilAttributes {
    var attrName: string, newAttr: any, oldAttr: any;
    let wasTabindex = false;
    if (newAttrs != null) for (attrName in newAttrs) {
        newAttr = newAttrs[attrName];
        oldAttr = oldAttrs[attrName];
        if (notFocusable && attrName === tabindexStr) {
            newAttr = -1;
            wasTabindex = true;
        } else if (attrName === tvalue) {
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
                if (notFocusable && attrName === tabindexStr) continue;
                gwUnsetAttr(nodeId, attrName);
                oldAttrs[attrName] = undefined;
            }
        }
    } else {
        for (attrName in oldAttrs) {
            if (oldAttrs[attrName] !== undefined && !(attrName in newAttrs)) {
                if (notFocusable && attrName === tabindexStr) continue;
                gwUnsetAttr(nodeId, attrName);
                oldAttrs[attrName] = undefined;
            }
        }
    }
    return oldAttrs;
}

function findCfg(parent: IBobrilCacheNode | undefined): any {
    var cfg: any;
    while (parent) {
        cfg = parent.cfg;
        if (cfg !== undefined) break;
        if (parent.ctx) {
            cfg = parent.ctx.cfg;
            break;
        }
        parent = parent.parent;
    }
    return cfg;
}

function setRef(ref: [IBobrilCtx, string] | ((node: IBobrilCacheNode) => void), value: IBobrilCacheNode | undefined) {
    if (ref == null) return;
    if (isFunction(ref)) {
        (<(node: IBobrilCacheNode | undefined) => void>ref)(value);
        return;
    }
    var ctx = (<[IBobrilCtx, string]>ref)[0];
    var refs = ctx.refs;
    if (!refs) {
        refs = newHashObj();
        ctx.refs = refs;
    }
    refs![(<[IBobrilCtx, string]>ref)[1]] = value;
}

let focusRootStack: IBobrilCacheNode[] = [];
let focusRootTop: IBobrilCacheNode | undefined = undefined;

export function registerFocusRoot(ctx: IBobrilCtx) {
    focusRootStack.push(ctx.me!);
    addDisposable(ctx, unregisterFocusRoot);
    ignoreShouldChange();
}

export function unregisterFocusRoot(ctx: IBobrilCtx) {
    let idx = focusRootStack.indexOf(ctx.me!);
    if (idx !== -1) {
        focusRootStack.splice(idx, 1);
        ignoreShouldChange();
    }
}

let lastNodeId = 1;
const freeNodeIds: number[] = [];
const nodeId2Node: (IBobrilCacheNode | undefined)[] = [undefined];

function allocNodeId(): number {
    if (freeNodeIds.length > 0) {
        return freeNodeIds.pop()!;
    }
    return lastNodeId++;
}

function freeNodeId(nodeId: number) {
    if (lastNodeId - 1 === nodeId) {
        lastNodeId--;
    } else {
        freeNodeIds.push(nodeId);
    }
}

export function createNode(n: IBobrilNode, parentNode: IBobrilCacheNode | undefined, createInto: number, createBefore: number): IBobrilCacheNode {
    var c = <IBobrilCacheNode>{ // This makes CacheNode just one object class = fast
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
        var ctx: IBobrilCtx = { data: c.data || {}, me: c, cfg: findCfg(parentNode) };
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
            component.postRender(c.ctx!, c);
        }
    }
    if (inNotFocusable && focusRootTop === c)
        inNotFocusable = false;
    if (c.attrs || inNotFocusable) c.attrs = updateElement(c.nodeId, c.attrs, {}, inNotFocusable);
    if (c.style != null) gwSetStyle(c.nodeId, c.style);
    inNotFocusable = backupInNotFocusable;
    return c;
}

function normalizeNode(n: any): IBobrilNode | undefined {
    if (n === false || n === true) return undefined;
    if (isString(n)) {
        return { children: n };
    }
    if (isNumber(n)) {
        return { children: "" + n };
    }
    return <IBobrilNode>n;
}

function createChildren(c: IBobrilCacheNode, createInto: number, createBefore: number): void {
    var ch = c.children;
    if (!ch)
        return;
    if (!isArray(ch)) {
        if (isString(ch)) {
            gwSetStringChild(createInto, ch);
            return;
        }
        if (isNumber(ch)) {
            gwSetStringChild(createInto, "" + ch);
            return;
        }
        ch = <any>[ch];
    }
    ch = (<any[]>ch).slice(0);
    var i = 0, l = (<any[]>ch).length;
    while (i < l) {
        var item = (<any[]>ch)[i];
        if (isArray(item)) {
            (<IBobrilCacheNode[]>ch).splice.apply(ch, (<any>[i, 1]).concat(item));
            l = (<IBobrilCacheNode[]>ch).length;
            continue;
        }
        item = normalizeNode(item);
        if (item == null) {
            (<any[]>ch).splice(i, 1);
            l--;
            continue;
        }
        (<IBobrilCacheNode[]>ch)[i] = createNode(item, c, createInto, createBefore);
        i++;
    }
    c.children = ch;
}

function destroyNode(c: IBobrilCacheNode) {
    setRef(c.ref, undefined);
    let ch = c.children;
    if (isArray(ch)) {
        for (let i = 0, l = ch.length; i < l; i++) {
            destroyNode(ch[i]);
        }
    }
    let component = c.component;
    if (component) {
        let ctx = c.ctx!;
        if (component.destroy)
            component.destroy(ctx, c);
        let disposables = ctx.disposables;
        if (isArray(disposables)) {
            for (let i = disposables.length; i-- > 0;) {
                let d = disposables[i];
                if (isFunction(d)) d(ctx); else d.dispose();
            }
        }
    }
    nodeId2Node[c.nodeId] = undefined;
    freeNodeId(c.nodeId);
    c.nodeId = -1;
}

export function addDisposable(ctx: IBobrilCtx, disposable: IDisposableLike) {
    let disposables = ctx.disposables;
    if (disposables == null) {
        disposables = [];
        ctx.disposables = disposables;
    }
    disposables.push(disposable);
}

function removeNode(c: IBobrilCacheNode) {
    let nodeId = c.nodeId;
    destroyNode(c);
    gwRemoveNode(nodeId);
}

var roots: IBobrilRoots = newHashObj();

export function vdomPath(n: number): IBobrilCacheNode[] {
    var res: IBobrilCacheNode[] = [];
    let top = nodeId2Node[n];
    while (top != null) {
        res.push(top);
        top = top.parent;
    }
    res.reverse();
    return res;
}

// PureFuncs: deref
export function deref(n: number): IBobrilCacheNode | undefined {
    return nodeId2Node[n];
}

function finishUpdateNode(n: IBobrilNode, c: IBobrilCacheNode, component: IBobrilComponent | undefined) {
    if (component) {
        if (component.postRender) {
            component.postRender(c.ctx!, n, c);
        }
    }
    c.data = n.data;
}

function updateStyle(nodeId: number, newStyle: IBobrilCacheStyles | undefined, oldStyle: IBobrilCacheStyles | undefined) {
    if (newStyle === oldStyle) return;
    if (newStyle == null) {
        if (oldStyle == null) return;
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
    for (let i = 0; i < newStyle.length; i++) {
        if (newStyle[i] !== oldStyle[i]) {
            gwSetStyle(nodeId, newStyle);
            return;
        }
    }
}

export function updateNode(n: IBobrilNode, c: IBobrilCacheNode, createInto: number, createBefore: number, deepness: number): IBobrilCacheNode {
    var component = n.component;
    var backupInNotFocusable = inNotFocusable;
    var bigChange = false;
    var ctx = c.ctx;
    if (component && ctx != null) {
        if ((<any>ctx)[ctxInvalidated] === frameCounter) {
            deepness = Math.max(deepness, (<any>ctx)[ctxDeepness]);
        }
        if (component.id !== c.component!.id) {
            bigChange = true;
        } else {
            if (c.parent != undefined)
                ctx.cfg = findCfg(c.parent);
            if (component.shouldChange)
                if (!component.shouldChange(ctx, n, c) && !ignoringShouldChange) {
                    if (isArray(c.children)) {
                        if (inNotFocusable && focusRootTop === c)
                            inNotFocusable = false;
                        selectedUpdate(<IBobrilCacheNode[]>c.children, c.nodeId, 0);
                        inNotFocusable = backupInNotFocusable;
                    }
                    return c;
                }
            ctx.data = n.data || {};
            c.component = component;
            if (component.render) {
                n = assign({}, n); // need to clone me because it should not be modified for next updates
                component.render(ctx, n, c);
            }
            c.cfg = n.cfg;
        }
    }
    if (DEBUG) {
        if (!((n.ref == null && c.ref == null) ||
            ((n.ref != null && c.ref != null && (isFunction(n.ref) || isFunction(c.ref) ||
                n.ref[0] === c.ref[0] && n.ref[1] === c.ref[1]))))) {
            if (window.console && console.warn) console.warn("ref changed in child in update");
        }
    }
    var newChildren = n.children;
    var cachedChildren = c.children;
    var tag = n.tag;
    if (isNumber(newChildren)) {
        newChildren = "" + newChildren;
    }
    if (bigChange || (component && ctx == null)) {
        // it is big change of component.id or old one was not even component => recreate
    } else if (tag === c.tag) {
        if (inNotFocusable && focusRootTop === c)
            inNotFocusable = false;
        var nodeId = c.nodeId;
        if ((isString(newChildren)) && !isArray(cachedChildren)) {
            if (newChildren !== cachedChildren) {
                gwSetStringChild(nodeId, newChildren);
                cachedChildren = newChildren;
            }
        } else {
            if (deepness <= 0) {
                if (isArray(cachedChildren))
                    selectedUpdate(<IBobrilCacheNode[]>c.children, nodeId, createBefore);
            } else {
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
    var r: IBobrilCacheNode = createNode(n, c.parent, createInto, c.nodeId);
    removeNode(c);
    return r;
}

function findNextNode(a: IBobrilCacheNode[], i: number, len: number, def: number): number {
    while (++i < len) {
        var ai = a[i];
        if (ai == null) continue;
        return ai.nodeId;
    }
    return def;
}

function updateNodeInUpdateChildren(newNode: IBobrilNode, cachedChildren: IBobrilCacheNode[], cachedIndex: number, cachedLength: number, createBefore: number, element: number, deepness: number) {
    cachedChildren[cachedIndex] = updateNode(newNode, cachedChildren[cachedIndex], element,
        findNextNode(cachedChildren, cachedIndex, cachedLength, createBefore), deepness);
}

function reorderInUpdateChildrenRec(c: IBobrilCacheNode, before: number): void {
    gwMoveBefore(c.nodeId, before);
}

function reorderInUpdateChildren(cachedChildren: IBobrilCacheNode[], cachedIndex: number, cachedLength: number, createBefore: number) {
    var before = findNextNode(cachedChildren, cachedIndex, cachedLength, createBefore);
    var cur = cachedChildren[cachedIndex];
    if (cur.nodeId !== before) {
        reorderInUpdateChildrenRec(cur, before);
    }
}

function reorderAndUpdateNodeInUpdateChildren(newNode: IBobrilNode, cachedChildren: IBobrilCacheNode[], cachedIndex: number, cachedLength: number, createBefore: number, element: number, deepness: number) {
    var before = findNextNode(cachedChildren, cachedIndex, cachedLength, createBefore);
    var cur = cachedChildren[cachedIndex];
    if (cur.nodeId !== before) {
        reorderInUpdateChildrenRec(cur, before);
    }
    cachedChildren[cachedIndex] = updateNode(newNode, cur, element, before, deepness);
}

export function updateChildren(element: number, newChildren: IBobrilChildren, cachedChildren: IBobrilCacheChildren, parentNode: IBobrilCacheNode | undefined, createBefore: number, deepness: number): IBobrilCacheNode[] {
    if (newChildren == null) newChildren = <IBobrilNode[]>[];
    if (!isArray(newChildren)) {
        newChildren = [newChildren];
    }
    if (cachedChildren == null) cachedChildren = [];
    if (!isArray(cachedChildren)) {
        gwUnsetStringChild(element);
        cachedChildren = <any>[];
    }
    let newCh = <IBobrilChildArray>newChildren;
    newCh = newCh.slice(0);
    var newLength = newCh.length;
    var newIndex: number;
    for (newIndex = 0; newIndex < newLength;) {
        var item = newCh[newIndex];
        if (isArray(item)) {
            newCh.splice.apply(newCh, [newIndex, 1].concat(<any>item));
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
    return updateChildrenCore(element, <IBobrilNode[]>newCh, <IBobrilCacheNode[]>cachedChildren, parentNode, createBefore, deepness);
}

function updateChildrenCore(element: number, newChildren: IBobrilNode[], cachedChildren: IBobrilCacheNode[], parentNode: IBobrilCacheNode | undefined, createBefore: number, deepness: number): IBobrilCacheNode[] {
    let newEnd = newChildren.length;
    var cachedLength = cachedChildren.length;
    let cachedEnd = cachedLength;
    let newIndex = 0;
    let cachedIndex = 0;
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
            cachedChildren.splice(cachedIndex, 0, createNode(newChildren[newIndex], parentNode, element,
                findNextNode(cachedChildren, cachedIndex - 1, cachedLength, createBefore)));
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
    var cachedKeys: { [keyName: string]: number } = newHashObj();
    var newKeys: { [keyName: string]: number } = newHashObj();
    var key: string | undefined;
    var node: IBobrilNode;
    var backupNewIndex = newIndex;
    var backupCachedIndex = cachedIndex;
    var deltaKeyless = 0;
    for (; cachedIndex < cachedEnd; cachedIndex++) {
        node = cachedChildren[cachedIndex];
        key = node.key;
        if (key != null) {
            assert(!(key in <any>cachedKeys));
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
            assert(!(key in <any>newKeys));
            newKeys[key] = newIndex;
        }
        else
            deltaKeyless++;
    }
    keyLess += deltaKeyless;
    var delta = 0;
    newIndex = backupNewIndex;
    cachedIndex = backupCachedIndex;
    var cachedKey: string;
    while (cachedIndex < cachedEnd && newIndex < newEnd) {
        if (cachedChildren[cachedIndex] === null) { // already moved somethere else
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
            cachedChildren.splice(cachedIndex, 0, createNode(newChildren[newIndex], parentNode, element,
                findNextNode(cachedChildren, cachedIndex - 1, cachedLength, createBefore)));
            delta++;
            newIndex++;
            cachedIndex++;
            cachedEnd++;
            cachedLength++;
            continue;
        }
        if (!(cachedKey in <any>newKeys)) {
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
        } else {
            // Move
            cachedChildren.splice(cachedIndex, 0, cachedChildren[akpos + delta]);
            delta++;
            (cachedChildren as any)[akpos + delta] = null;
            reorderAndUpdateNodeInUpdateChildren(newChildren[newIndex], cachedChildren, cachedIndex, cachedLength, createBefore, element, deepness);
            cachedIndex++;
            cachedEnd++;
            cachedLength++;
            newIndex++;
        }
    }
    // remove old keyed cached nodes
    while (cachedIndex < cachedEnd) {
        if (cachedChildren[cachedIndex] === null) { // already moved somethere else
            cachedChildren.splice(cachedIndex, 1);
            cachedEnd--;
            cachedLength--;
            continue;
        }
        if (cachedChildren[cachedIndex].key != null) { // this key is only in old
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
            cachedChildren.splice(cachedIndex, 0, createNode(newChildren[newIndex], parentNode, element,
                findNextNode(cachedChildren, cachedIndex - 1, cachedLength, createBefore)));
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
        } else {
            cachedChildren.splice(newIndex, 0, createNode(newChildren[newIndex], parentNode, element,
                findNextNode(cachedChildren, newIndex - 1, cachedLength, createBefore)));
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
    nativeRaf((param) => { if (param === +param) hasNativeRaf = true; });
}

const truenow = Date.now || (() => (new Date).getTime());
var startTime = truenow();
var lastTickTime = 0;

var mockedTime: number = -1;

export function now() {
    if (mockedTime === -1) return truenow();
    return mockedTime;
}

function requestAnimationFrame(callback: (time: number) => void) {
    if (hasNativeRaf) {
        nativeRaf(callback);
    } else {
        var delay = 50 / 3 + lastTickTime - truenow();
        if (delay < 0) delay = 0;
        window.setTimeout(() => {
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

var regEvents: { [name: string]: Array<(ev: any, node: IBobrilCacheNode | undefined) => boolean> } = {};
var registryEvents: { [name: string]: Array<{ priority: number; callback: (ev: any, node: IBobrilCacheNode | undefined) => boolean }> } | undefined;

export function addEvent(name: string, priority: number, callback: (ev: any, node: IBobrilCacheNode) => boolean): void {
    if (registryEvents == null) registryEvents = {};
    var list = registryEvents[name] || [];
    list.push({ priority: priority, callback: callback });
    registryEvents[name] = list;
}

gw.setEventHandler((name: string, param: Object, nodeId: number, time: number) => {
    let node = nodeId2Node[nodeId];
    let backupMockedTime = mockedTime;
    if (time != -1) mockedTime = time;
    let res = emitEvent(name, param, node);
    mockedTime = backupMockedTime;
    return res;
});

export function emitEvent(name: string, ev: any, node: IBobrilCacheNode | undefined): boolean {
    var events = regEvents[name];
    if (events) for (var i = 0; i < events.length; i++) {
        if (events[i](ev, node))
            return true;
    }
    return false;
}

function initEvents() {
    if (registryEvents == null)
        return;
    var eventNames = Object.keys(registryEvents);
    for (var j = 0; j < eventNames.length; j++) {
        var eventName = eventNames[j];
        var arr = registryEvents[eventName];
        arr = arr.sort((a, b) => a.priority - b.priority);
        regEvents[eventName] = arr.map(v => v.callback);
    }
    registryEvents = undefined;
}

function selectedUpdate(cache: IBobrilCacheNode[], element: number, createBefore: number) {
    var len = cache.length;
    for (var i = 0; i < len; i++) {
        var node = cache[i];
        var ctx = node.ctx;
        if (ctx != null && (<any>ctx)[ctxInvalidated] === frameCounter) {
            var cloned: IBobrilNode = { data: ctx.data, component: node.component! };
            cache[i] = updateNode(cloned, node, element, createBefore, (<any>ctx)[ctxDeepness]);
        } else if (isArray(node.children)) {
            var backupInNotFocusable = inNotFocusable;
            if (inNotFocusable && focusRootTop === node)
                inNotFocusable = false;
            selectedUpdate(node.children, node.nodeId, findNextNode(cache, i, len, createBefore));
            inNotFocusable = backupInNotFocusable;
        }
    }
}

var beforeFrameCallback: () => void = () => { };
var afterFrameCallback: (root: IBobrilCacheChildren) => void = () => { };

export function setBeforeFrame(callback: () => void): () => void {
    var res = beforeFrameCallback;
    beforeFrameCallback = callback;
    return res;
}

export function setAfterFrame(callback: (root: IBobrilCacheChildren) => void): (root: IBobrilCacheChildren) => void {
    var res = afterFrameCallback;
    afterFrameCallback = callback;
    return res;
}

function isLogicalParent(parent: IBobrilCacheNode | undefined, child: IBobrilCacheNode | undefined, rootIds: string[]): boolean {
    while (child != null) {
        if (parent === child) return true;
        let p = child.parent;
        if (p == null) {
            for (var i = 0; i < rootIds.length; i++) {
                var r = roots[rootIds[i]];
                if (!r) continue;
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

export function syncUpdate() {
    internalUpdate(truenow() - startTime);
}

function update(time: number) {
    scheduled = false;
    internalUpdate(time);
}

function internalUpdate(time: number) {
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
        if (!r) continue;
        var rc = r.c;
        if (focusRootTop) inNotFocusable = !isLogicalParent(focusRootTop, r.p, rootIds);
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
    let r0 = roots["0"];
    afterFrameCallback(r0 ? r0.c : undefined);
    lastFrameDurationMs = truenow() - renderFrameBegin;
}

var nextIgnoreShouldChange = false;
var ignoringShouldChange = false;

export function ignoreShouldChange() {
    nextIgnoreShouldChange = true;
    invalidate();
}

export function setInvalidate(inv: (ctx?: Object, deepness?: number) => void): (ctx?: Object, deepness?: number) => void {
    let prev = invalidate;
    invalidate = inv;
    return prev;
}

export var invalidate = (ctx?: Object, deepness?: number) => {
    if (ctx != null) {
        if (deepness == undefined) deepness = 1e6;
        if ((<any>ctx)[ctxInvalidated] !== frameCounter + 1) {
            (<any>ctx)[ctxInvalidated] = frameCounter + 1;
            (<any>ctx)[ctxDeepness] = deepness;
        } else {
            if (deepness > (<any>ctx)[ctxDeepness])
                (<any>ctx)[ctxDeepness] = deepness;
        }
    } else {
        fullRecreateRequested = true;
    }
    if (scheduled)
        return;
    scheduled = true;
    requestAnimationFrame(update);
}

function forceInvalidate() {
    if (!scheduled)
        fullRecreateRequested = false;
    invalidate();
}

var lastRootId = 0;

export function addRoot(factory: () => IBobrilChildren, parent?: IBobrilCacheNode): string {
    lastRootId++;
    var rootId = "" + lastRootId;
    roots[rootId] = { f: factory, c: [], p: parent };
    forceInvalidate();
    return rootId;
}

export function removeRoot(id: string): void {
    var root = roots[id];
    if (!root) return;
    if (root.c.length) {
        root.c = updateChildren(0, <any>[], root.c, undefined, 0, 1e9);
    }
    delete roots[id];
}

export function getRoots(): IBobrilRoots {
    return roots;
}

var beforeInit: () => void = forceInvalidate;

export function init(factory: () => any) {
    removeRoot("0");
    roots["0"] = { f: factory, c: [], p: undefined };
    gw.readyPromise.then(() => {
        beforeInit();
        beforeInit = forceInvalidate;
    });
}

export function setBeforeInit(callback: (cb: () => void) => void): void {
    let prevBeforeInit = beforeInit;
    beforeInit = () => {
        callback(prevBeforeInit);
    }
}

export function bubble(node: IBobrilCacheNode | undefined, name: string, param: any): IBobrilCtx | undefined {
    while (node) {
        var c = node.component;
        if (c) {
            var ctx = node.ctx!;
            var m = (<any>c)[name];
            if (m) {
                if (m.call(c, ctx, param))
                    return ctx;
            }
            m = (<any>c).shouldStopBubble;
            if (m) {
                if (m.call(c, ctx, name, param))
                    break;
            }
        }
        node = node.parent;
    }
    return undefined;
}

function broadcastEventToNode(node: IBobrilCacheNode | undefined, name: string, param: any): IBobrilCtx | undefined {
    if (!node)
        return undefined;
    var c = node.component;
    if (c) {
        var ctx = node.ctx;
        var m = (<any>c)[name];
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
    if (isArray(ch)) {
        for (var i = 0; i < ch.length; i++) {
            var res = broadcastEventToNode(ch[i], name, param);
            if (res != null)
                return res;
        }
    }
    return undefined;
}

export function broadcast(name: string, param: any): IBobrilCtx | undefined {
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

function merge(f1: Function, f2: Function): Function {
    return function (this: any, ...params: any[]) {
        var result = f1.apply(this, params);
        if (result) return result;
        return f2.apply(this, params);
    }
}

var emptyObject = {};

function mergeComponents(c1: IBobrilComponent, c2: IBobrilComponent): IBobrilComponent {
    let res: IBobrilComponent = Object.create(c1);
    res.super = c1;
    for (var i in c2) {
        if (!(i in <any>emptyObject)) {
            var m = (<any>c2)[i];
            var origM = (<any>c1)[i];
            if (i === "id") {
                (res as any)[i] = ((origM != null) ? origM : "") + "/" + m;
            } else if (isFunction(m) && origM != null && isFunction(origM)) {
                (res as any)[i] = merge(origM, m);
            } else {
                (res as any)[i] = m;
            }
        }
    }
    return res;
}

function overrideComponents(originalComponent: IBobrilComponent, overridingComponent: IBobrilComponent) {
    let res: IBobrilComponent = Object.create(originalComponent);
    res.super = originalComponent;
    for (let i in overridingComponent) {
        if (!(i in <any>emptyObject)) {
            let m = (<any>overridingComponent)[i];
            let origM = (<any>originalComponent)[i];
            if (i === 'id') {
                (res as any)[i] = ((origM != null) ? origM : '') + '/' + m;
            } else {
                (res as any)[i] = m;
            }
        }
    }
    return res;
}

export function preEnhance(node: IBobrilNode, methods: IBobrilComponent): IBobrilNode {
    var comp = node.component;
    if (!comp) {
        node.component = methods;
        return node;
    }
    node.component = mergeComponents(methods, comp);
    return node;
}

export function postEnhance(node: IBobrilNode, methods: IBobrilComponent): IBobrilNode {
    var comp = node.component;
    if (!comp) {
        node.component = methods;
        return node;
    }
    node.component = mergeComponents(comp, methods);
    return node;
}

export function preventDefault(event: Event) {
    var pd = event.preventDefault;
    if (pd) pd.call(event); else (<any>event).returnValue = false;
}

function cloneNodeArray(a: IBobrilChild[]): IBobrilChild[] {
    a = a.slice(0);
    for (var i = 0; i < a.length; i++) {
        var n = a[i];
        if (isArray(n)) {
            a[i] = <any>cloneNodeArray(<any>n);
        } else if (isObject(n)) {
            a[i] = cloneNode(<IBobrilNode>n);
        }
    }
    return a;
}

export function cloneNode(node: IBobrilNode): IBobrilNode {
    var r = <IBobrilNode>assign({}, node);
    if (r.attrs) {
        r.attrs = <IBobrilAttributes>assign({}, r.attrs);
    }
    if (isObject(r.style)) {
        r.style = assign({}, r.style);
    }
    var ch = r.children;
    if (ch) {
        if (isArray(ch)) {
            r.children = cloneNodeArray(<IBobrilChild[]>ch);
        } else if (isObject(ch)) {
            r.children = cloneNode(<IBobrilNode>ch);
        }
    }
    return r;
}

// PureFuncs: uptime, lastFrameDuration, frame, invalidated

export function uptime() { return uptimeMs; }

export function lastFrameDuration() { return lastFrameDurationMs; }

export function frame() { return frameCounter; }

export function invalidated() { return scheduled; }

// Bobril.Media
export const enum BobrilDeviceCategory {
    Mobile = 0,
    Tablet = 1,
    Desktop = 2,
    LargeDesktop = 3
}

export interface IBobrilMedia {
    width: number;
    height: number;
    orientation: number;
    deviceCategory: BobrilDeviceCategory;
    portrait: boolean;
    density: number;
}

var media: IBobrilMedia | undefined = undefined;
var breaks = [
    [414, 800, 900], //portrait widths
    [736, 1280, 1440] //landscape widths
];

let lastResizeInfo = {
    width: 600,
    height: 800,
    rotation: 0,
    density: 1
};

function onResize(ev: { width: number, height: number, rotation: number, density: number }) {
    lastResizeInfo = ev;
    media = undefined;
    invalidate();
    return false;
}

addEvent("resize", 10, onResize);

export function accDeviceBreaks(newBreaks?: number[][]): number[][] {
    if (newBreaks != null) {
        breaks = newBreaks;
        media = undefined;
        invalidate();
    }
    return breaks;
}

export function getMedia(): IBobrilMedia {
    if (media == null) {
        var w = lastResizeInfo.width;
        var h = lastResizeInfo.height;
        var o = lastResizeInfo.rotation;
        var p = h >= w;
        var device = 0;
        while (w > breaks[+!p][device]) device++;
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

// Bobril.OnKey

export interface IKeyDownUpEvent {
    shift: boolean;
    ctrl: boolean;
    alt: boolean;
    meta: boolean;
    which: number;
}

export interface IKeyPressEvent {
    charCode: number;
}

// Bobril.Mouse

export interface IBobrilMouseEvent {
    x: number;
    y: number;
    /// 1 - left (or touch), 2 - middle, 3 - right <- it does not make sense but that's W3C
    button: number;
    /// 1 - single click, 2 - double click, 3+ - multiclick
    count: number;
    shift: boolean;
    ctrl: boolean;
    alt: boolean;
    meta: boolean;
}

export const enum BobrilPointerType {
    Mouse = 0,
    Touch = 1,
    Pen = 2
}

export interface IBobrilPointerEvent extends IBobrilMouseEvent {
    id: number;
    type: BobrilPointerType;
}

export interface IBobrilMouseWheelEvent extends IBobrilMouseEvent {
    dx: number;
    dy: number;
}

addEvent("pointerDown", 10, (ev, node) => {
    let event: IBobrilPointerEvent = {
        x: ev.x,
        y: ev.y,
        button: 1,
        count: 1,
        shift: false,
        ctrl: false,
        alt: false,
        meta: false,
        id: ev.id,
        type: BobrilPointerType.Touch
    }
    return bubble(node, "onPointerDown", event) != null;
});

addEvent("pointerMove", 10, (ev, node) => {
    let event: IBobrilPointerEvent = {
        x: ev.x,
        y: ev.y,
        button: 1,
        count: 1,
        shift: false,
        ctrl: false,
        alt: false,
        meta: false,
        id: ev.id,
        type: BobrilPointerType.Touch
    }
    return bubble(node, "onPointerMove", event) != null;
});

addEvent("pointerUp", 10, (ev, node) => {
    let event: IBobrilPointerEvent = {
        x: ev.x,
        y: ev.y,
        button: 1,
        count: 1,
        shift: false,
        ctrl: false,
        alt: false,
        meta: false,
        id: ev.id,
        type: BobrilPointerType.Touch
    }
    return bubble(node, "onPointerUp", event) != null;
});

addEvent("pointerCancel", 10, (ev, node) => {
    let event: IBobrilPointerEvent = {
        x: 0,
        y: 0,
        button: 1,
        count: 1,
        shift: false,
        ctrl: false,
        alt: false,
        meta: false,
        id: ev.id,
        type: BobrilPointerType.Touch
    }
    return bubble(node, "pointerCancel", event) != null;
});

// Bobril.Focus

// Bobril.Scroll

// Bobril.Dnd
export const enum DndOp {
    None = 0,
    Link = 1,
    Copy = 2,
    Move = 3
}

export const enum DndEnabledOps {
    None = 0,
    Link = 1,
    Copy = 2,
    LinkCopy = 3,
    Move = 4,
    MoveLink = 5,
    MoveCopy = 6,
    MoveCopyLink = 7
}

export interface IDndCtx {
    id: number;
    listData(): string[];
    hasData(type: string): boolean;
    getData(type: string): any;
    enabledOperations: DndEnabledOps;
    operation: DndOp;
    overNode: IBobrilCacheNode;
    // way to overrride mouse cursor, leave null to emulate dnd cursor
    cursor: string;
    // dnd is wating for activation by moving atleast distanceToStart pixels
    started: boolean;
    beforeDrag: boolean;
    system: boolean;
    local: boolean;
    ended: boolean;
    // default value is 10, but you can assign to this >=0 number in onDragStart
    distanceToStart: number;
    // drag started at this pointer position
    startX: number;
    startY: number;
    // distance moved - only increasing
    totalX: number;
    totalY: number;
    // previous mouse/touch pointer position
    lastX: number;
    lastY: number;
    // actual mouse/touch pointer position
    x: number;
    y: number;
    // delta of left top position of dragged object when drag started, usually negative
    deltaX: number;
    deltaY: number;
    shift: boolean;
    ctrl: boolean;
    alt: boolean;
    meta: boolean;
}

export interface IDndStartCtx extends IDndCtx {
    addData(type: string, data: any): boolean;
    setEnabledOps(ops: DndEnabledOps): void;
    setDragNodeView(view: (dnd: IDndCtx) => IBobrilNode): void;
}

export interface IDndOverCtx extends IDndCtx {
    setOperation(operation: DndOp): void;
}

// Bobril.Router

export interface Params {
    [name: string]: string
}

// Just marker interface
export interface IRoute {
    name?: string;
    url?: string;
    data?: Object;
    handler: IRouteHandler;
    keyBuilder?: (params: Params) => string;
    children?: Array<IRoute>;
    isDefault?: boolean;
    isNotFound?: boolean;
}

export const enum RouteTransitionType {
    Push,
    Replace,
    Pop
}

export interface IRouteTransition {
    inApp: boolean;
    type: RouteTransitionType;
    name: string;
    params: Params;
    distance?: number;
}

export interface Thenable<R> {
    then<U>(onFulfilled?: (value: R) => U | Thenable<U>, onRejected?: (error: any) => U | Thenable<U>): Thenable<U>;
    then<U>(onFulfilled?: (value: R) => U | Thenable<U>, onRejected?: (error: any) => void): Thenable<U>;
}

export type IRouteCanResult = boolean | Thenable<boolean> | IRouteTransition | Thenable<IRouteTransition>;

export type IRouteHandler = IBobrilComponent | ((data: any) => IBobrilNode);

export interface IRouteConfig {
    // name cannot contain ":" or "/"
    name?: string;
    url?: string;
    data?: Object;
    handler: IRouteHandler;
    keyBuilder?: (params: Params) => string;
}

// Bobril.Style

// definition for Bobril defined class
export type IBobrilStyleDef = string;
// object case if for inline style declaration, undefined, null, true and false values are ignored
export type IBobrilStyle = Object | IBobrilStyleDef | boolean | null | undefined;
// place inline styles at end for optimal speed
export type IBobrilStyles = IBobrilStyle | IBobrilStyle[];

interface ISprite {
    styleid: IBobrilStyleDef;
    url: string;
    width?: number;
    height?: number;
    left: number;
    top: number;
}

interface IDynamicSprite extends ISprite {
    color: () => string;
    lastColor: string;
    lastUrl: string;
}

interface IInternalStyle {
    name: string;
    realname: string;
    parent?: IBobrilStyleDef | IBobrilStyleDef[];
    style: any;
    pseudo?: { [name: string]: string };
}

var allStyles: { [id: string]: IInternalStyle } = newHashObj();
var allSprites: { [key: string]: ISprite } = newHashObj();
var allNameHints: { [name: string]: boolean } = newHashObj();
var dynamicSprites: IDynamicSprite[] = [];
var imageCache: { [url: string]: HTMLImageElement | null } = newHashObj();
var rebuildStyles = false;
var globalCounter: number = 0;

var chainedBeforeFrame = setBeforeFrame(beforeFrame);

function flattenStyle(cur: any, curPseudo: any, style: any, stylePseudo: any): void {
    if (isString(style)) {
        let externalStyle = allStyles[style];
        if (externalStyle === undefined) {
            throw new Error("uknown style " + style);
        }
        flattenStyle(cur, curPseudo, externalStyle.style, externalStyle.pseudo);
    } else if (isFunction(style)) {
        style(cur, curPseudo);
    } else if (isArray(style)) {
        for (let i = 0; i < style.length; i++) {
            flattenStyle(cur, curPseudo, style[i], undefined);
        }
    } else if (typeof style === "object") {
        for (let key in style) {
            if (!Object.prototype.hasOwnProperty.call(style, key)) continue;
            let val = style[key];
            if (isFunction(val)) {
                val = val(cur, key);
            }
            cur[key] = val;
        }
    }
    if (stylePseudo != null && curPseudo != null) {
        for (let pseudoKey in stylePseudo) {
            let curPseudoVal = curPseudo[pseudoKey];
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
        for (let i = 0; i < dynamicSprites.length; i++) {
            let dynSprite = dynamicSprites[i];
            let image = imageCache[dynSprite.url];
            if (image == null) continue;
            let colorStr = dynSprite.color();
            if (colorStr !== dynSprite.lastColor) {
                dynSprite.lastColor = colorStr;
                if (dynSprite.width == null) dynSprite.width = image.width;
                if (dynSprite.height == null) dynSprite.height = image.height;
                let lastUrl = recolorAndClip(image, colorStr, dynSprite.width, dynSprite.height, dynSprite.left, dynSprite.top);
                var stDef = allStyles[dynSprite.styleid];
                stDef.style = { backgroundImage: `url(${lastUrl})`, width: dynSprite.width, height: dynSprite.height, backgroundPosition: 0 };
            }
        }
        for (var key in allStyles) {
            var ss = allStyles[key];
            let name = ss.name;
            let sspseudo = ss.pseudo;
            let ssstyle = ss.style;
            if (isFunction(ssstyle) && ssstyle.length === 0) {
                [ssstyle, sspseudo] = ssstyle();
            }
            if (isString(ssstyle) && sspseudo == null) {
                ss.realname = ssstyle;
                assert(name != null, "Cannot link existing class to selector");
                continue;
            }
            ss.realname = name;
            let style = newHashObj();
            let flattenPseudo = newHashObj();
            flattenStyle(undefined, flattenPseudo, undefined, sspseudo);
            flattenStyle(style, flattenPseudo, ssstyle, undefined);
            gwSetStyleDef(name, style, flattenPseudo);
        }
        rebuildStyles = false;
    }
    chainedBeforeFrame();
}

export function style(node: IBobrilNode, ...styles: IBobrilStyles[]): IBobrilNode {
    let inlineStyle = node.style;
    let stack = <(IBobrilStyles | number)[] | undefined>undefined;
    let i = 0;
    let ca = styles;
    while (true) {
        if (ca.length === i) {
            if (stack === undefined || stack.length === 0) break;
            ca = <IBobrilStyles[]>stack.pop();
            i = <number>stack.pop() + 1;
            continue;
        }
        let s = ca[i];
        if (s == null || s === true || s === false || s === '') {
            // skip
        } else if (isString(s)) {
            var sd = allStyles[s];
            if (inlineStyle == null) inlineStyle = [];
            inlineStyle.push(sd.realname, undefined);
        } else if (isArray(s)) {
            if (ca.length > i + 1) {
                if (stack == null) stack = [];
                stack.push(i, ca);
            }
            ca = <IBobrilStyles[]>s; i = 0;
            continue;
        } else {
            if (inlineStyle == null) inlineStyle = [];
            for (let key in s) {
                if (s.hasOwnProperty(key)) {
                    let val = (s as any)[key];
                    if (isFunction(val)) val = val();
                    if (val == null) continue;
                    inlineStyle.push(key, val);
                }
            }
        }
        i++;
    }
    node.style = inlineStyle;
    return node;
}

// PureFuncs: styleDef, styleDefEx, sprite, spriteb, spritebc, asset

export function styleDef(style: any, pseudo?: { [name: string]: any }, nameHint?: string): IBobrilStyleDef {
    return styleDefEx(undefined, style, pseudo, nameHint);
}

function styleDefEx(parent: IBobrilStyleDef | IBobrilStyleDef[] | undefined, style: any, pseudo?: { [name: string]: any }, nameHint?: string): IBobrilStyleDef {
    if (nameHint && nameHint !== "b-") {
        if (allNameHints[nameHint]) {
            var counter = 1;
            while (allNameHints[nameHint + counter]) counter++;
            nameHint = nameHint + counter;
        }
        allNameHints[nameHint] = true;
    } else {
        nameHint = "b-" + globalCounter++;
    }
    allStyles[nameHint] = { name: nameHint, realname: nameHint, parent, style, pseudo };
    invalidateStyles();
    return nameHint;
}

export function invalidateStyles(): void {
    rebuildStyles = true;
    invalidate();
}

function updateSprite(spDef: ISprite): void {
    var stDef = allStyles[spDef.styleid];
    var style: any = { backgroundImage: `url(${spDef.url})`, width: spDef.width, height: spDef.height };
    style.backgroundPosition = `${-spDef.left}px ${-spDef.top}px`;
    stDef.style = style;
    invalidateStyles();
}

function emptyStyleDef(url: string): IBobrilStyleDef {
    return styleDef({ width: 0, height: 0 }, undefined, url.replace(/[^a-z0-9_-]/gi, '_'));
}

const rgbaRegex = /\s*rgba\(\s*(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*(\d+|\d*\.\d+)\s*\)\s*/;

function recolorAndClip(image: HTMLImageElement, colorStr: string, width: number, height: number, left: number, top: number): string {
    var canvas = document.createElement("canvas");
    canvas.width = width;
    canvas.height = height;
    var ctx = <CanvasRenderingContext2D>canvas.getContext("2d");
    ctx.drawImage(image, -left, -top);
    var imgdata = ctx.getImageData(0, 0, width, height);
    var imgd = imgdata.data;
    let rgba = rgbaRegex.exec(colorStr);
    let cred: number, cgreen: number, cblue: number, calpha: number;
    if (rgba) {
        cred = parseInt(rgba[1], 10);
        cgreen = parseInt(rgba[2], 10);
        cblue = parseInt(rgba[3], 10);
        calpha = Math.round(parseFloat(rgba[4]) * 255);
    } else {
        cred = parseInt(colorStr.substr(1, 2), 16);
        cgreen = parseInt(colorStr.substr(3, 2), 16);
        cblue = parseInt(colorStr.substr(5, 2), 16);
        calpha = parseInt(colorStr.substr(7, 2), 16) || 0xff;
    }
    if (calpha === 0xff) {
        for (var i = 0; i < imgd.length; i += 4) {
            // Horrible workaround for imprecisions due to browsers using premultiplied alpha internally for canvas
            let red = imgd[i];
            if (red === imgd[i + 1] && red === imgd[i + 2] && (red === 0x80 || imgd[i + 3] < 0xff && red > 0x70)) {
                imgd[i] = cred; imgd[i + 1] = cgreen; imgd[i + 2] = cblue;
            }
        }
    } else {
        for (var i = 0; i < imgd.length; i += 4) {
            let red = imgd[i];
            let alpha = imgd[i + 3];
            if (red === imgd[i + 1] && red === imgd[i + 2] && (red === 0x80 || alpha < 0xff && red > 0x70)) {
                if (alpha === 0xff) {
                    imgd[i] = cred; imgd[i + 1] = cgreen; imgd[i + 2] = cblue; imgd[i + 3] = calpha;
                } else {
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

let lastFuncId = 0;
const funcIdName = "b@funcId";
export function sprite(url: string, color?: string | (() => string), width?: number, height?: number, left?: number, top?: number): IBobrilStyleDef {
    left = left || 0;
    top = top || 0;
    let colorId = color || "";
    let isVarColor = false;
    if (isFunction(color)) {
        isVarColor = true;
        colorId = (color as any)[funcIdName];
        if (colorId == null) {
            colorId = "" + (lastFuncId++);
            (color as any)[funcIdName] = colorId;
        }
    }
    var key = url + ":" + colorId + ":" + (width || 0) + ":" + (height || 0) + ":" + left + ":" + top;
    var spDef = allSprites[key];
    if (spDef) return spDef.styleid;
    var styleid = emptyStyleDef(url);
    spDef = { styleid, url, width, height, left, top };
    if (isVarColor) {
        (<IDynamicSprite>spDef).color = <() => string>color;
        (<IDynamicSprite>spDef).lastColor = '';
        (<IDynamicSprite>spDef).lastUrl = '';
        dynamicSprites.push(<IDynamicSprite>spDef);
        if (imageCache[url] === undefined) {
            imageCache[url] = null;
            var image = new Image();
            image.addEventListener("load", () => {
                imageCache[url] = image;
                invalidateStyles();
            });
            image.src = url;
        }
        invalidateStyles();
    } else if (width == null || height == null || color != null) {
        var image = new Image();
        image.addEventListener("load", () => {
            if (spDef.width == null) spDef.width = image.width;
            if (spDef.height == null) spDef.height = image.height;
            if (color != null) {
                spDef.url = recolorAndClip(image, <string>color, spDef.width, spDef.height, spDef.left, spDef.top);
                spDef.left = 0;
                spDef.top = 0;
            }
            updateSprite(spDef);
        });
        image.src = url;
    } else {
        updateSprite(spDef);
    }
    allSprites[key] = spDef;
    return styleid;
}

var bundlePath = (window as any)['bobrilBPath'] || 'bundle.png';

export function setBundlePngPath(path: string) {
    bundlePath = path;
}

export function spriteb(width: number, height: number, left: number, top: number): IBobrilStyleDef {
    let url = bundlePath;
    var key = url + "::" + width + ":" + height + ":" + left + ":" + top;
    var spDef = allSprites[key];
    if (spDef) return spDef.styleid;
    var styleid = styleDef({ width: 0, height: 0 });
    spDef = { styleid: styleid, url: url, width: width, height: height, left: left, top: top };
    updateSprite(spDef);
    allSprites[key] = spDef;
    return styleid;
}

export function spritebc(color: () => string, width: number, height: number, left: number, top: number): IBobrilStyleDef {
    return sprite(bundlePath, color, width, height, left, top);
}

export function asset(path: string): string {
    return path;
}

// Bobril.svgExtensions

function polarToCartesian(centerX: number, centerY: number, radius: number, angleInDegrees: number): { x: number; y: number } {
    var angleInRadians = angleInDegrees * Math.PI / 180.0;
    return {
        x: centerX + (radius * Math.sin(angleInRadians)), y: centerY - (radius * Math.cos(angleInRadians))
    };
}

function svgDescribeArc(x: number, y: number, radius: number, startAngle: number, endAngle: number, startWithLine: boolean) {
    var absDeltaAngle = Math.abs(endAngle - startAngle);
    var close = false;
    if (absDeltaAngle > 360 - 0.01) {
        if (endAngle > startAngle) endAngle = startAngle - 359.9;
        else endAngle = startAngle + 359.9;
        if (radius === 0) return "";
        close = true;
    } else {
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
    if (close) d += "Z";
    return d;
}

export function svgPie(x: number, y: number, radiusBig: number, radiusSmall: number, startAngle: number, endAngle: number): string {
    var p = svgDescribeArc(x, y, radiusBig, startAngle, endAngle, false);
    var nextWithLine = true;
    if (p[p.length - 1] === "Z") nextWithLine = false;
    if (radiusSmall === 0) {
        if (!nextWithLine) return p;
    }
    return p + svgDescribeArc(x, y, radiusSmall, endAngle, startAngle, nextWithLine) + "Z";
}

export function svgCircle(x: number, y: number, radius: number): string {
    return svgDescribeArc(x, y, radius, 0, 360, false);
}

export function svgRect(x: number, y: number, width: number, height: number): string {
    return "M" + x + " " + y + "h" + width + "v" + height + "h" + (-width) + "Z";
}

// Bobril.helpers

export function withKey(node: IBobrilNode, key: string): IBobrilNode {
    node.key = key;
    return node;
}

// PureFuncs: styledDiv, createVirtualComponent, createComponent, createDerivedComponent, createOverridingComponent, prop, propi, propim, propa, getValue

export function styledDiv(children: IBobrilChildren, ...styles: any[]): IBobrilNode {
    return style({ tag: 'View', children }, styles);
}

export interface IComponentFactory<TData extends Object> {
    (data?: TData, children?: IBobrilChildren): IBobrilNode;
}

export function createVirtualComponent<TData>(component: IBobrilComponent): IComponentFactory<TData> {
    return (data?: TData, children?: IBobrilChildren): IBobrilNode => {
        if (children !== undefined) {
            if (data == null) data = <any>{};
            (<any>data).children = children;
        }
        return { data, component: component };
    };
}

export function createOverridingComponent<TData>(
    original: IComponentFactory<TData>, after: IBobrilComponent
): IComponentFactory<TData> {
    const originalComponent = original().component!;
    const overriding = overrideComponents(originalComponent, after);
    return createVirtualComponent<TData>(overriding);
}

export function createComponent<TData extends Object>(component: IBobrilComponent): IComponentFactory<TData> {
    const originalRender = component.render;
    if (originalRender) {
        component.render = function (ctx: any, me: IBobrilNode, oldMe?: IBobrilCacheNode) {
            me.tag = 'View';
            return originalRender.call(component, ctx, me, oldMe);
        }
    } else {
        component.render = (_ctx: any, me: IBobrilNode) => { me.tag = 'div'; };
    }
    return createVirtualComponent<TData>(component);
}

export function createDerivedComponent<TData>(original: (data?: any, children?: IBobrilChildren) => IBobrilNode, after: IBobrilComponent): IComponentFactory<TData> {
    const originalComponent = original().component!;
    const merged = mergeComponents(originalComponent, after);
    return createVirtualComponent<TData>(merged);
}

export type IProp<T> = (value?: T) => T;
export type IPropAsync<T> = (value?: T | PromiseLike<T>) => T;

export interface IValueData<T> {
    value: T | IProp<T>;
    onChange?: (value: T) => void;
}

export function prop<T>(value: T, onChange?: (value: T, old: T) => void): IProp<T> {
    return (val?: T) => {
        if (val !== undefined) {
            if (onChange !== undefined)
                onChange(val, value);
            value = val;
        }
        return value;
    };
}

export function propi<T>(value: T): IProp<T> {
    return (val?: T) => {
        if (val !== undefined) {
            value = val;
            invalidate();
        }
        return value;
    };
}

export function propim<T>(value: T, ctx?: IBobrilCtx, onChange?: (value: T, old: T) => void): IProp<T> {
    return (val?: T) => {
        if (val !== undefined && val !== value) {
            const oldVal = val;
            value = val;
            if (onChange !== undefined)
                onChange(val, oldVal);

            invalidate(ctx);
        }
        return value;
    };
}

export function propa<T>(prop: IProp<T>): IPropAsync<T> {
    return (val?: T | PromiseLike<T>) => {
        if (val !== undefined) {
            if (typeof val === "object" && isFunction((<PromiseLike<T>>val).then)) {
                (<PromiseLike<T>>val).then((v) => {
                    prop(v);
                }, (err) => {
                    if (window["console"] && console.error)
                        console.error(err);
                });
            } else {
                return prop(<T>val);
            }
        }
        return prop();
    };
}

export function getValue<T>(value: T | IProp<T> | IPropAsync<T>): T {
    if (isFunction(value)) {
        return (<IProp<T>>value)();
    }
    return <T>value;
}

export function emitChange<T>(data: IValueData<T>, value: T) {
    if (isFunction(data.value)) {
        (<IProp<T>>data.value)(value);
    }
    if (data.onChange !== undefined) {
        data.onChange(value);
    }
}

// bobril-clouseau needs this
if (!(<any>window).b) (<any>window).b = { deref, getRoots, setInvalidate, invalidateStyles, ignoreShouldChange, setAfterFrame, setBeforeFrame };

// TSX reactNamespace emulation
// PureFuncs: createElement

export function createElement(name: any, props: any): IBobrilNode {
    var children: IBobrilChild[] = [];
    for (var i = 2; i < arguments.length; i++) {
        var ii = arguments[i];
        children.push(ii);
    }
    if (isString(name)) {
        var res: IBobrilNode = { tag: name, children: children };
        if (props == null) {
            return res;
        }
        var attrs: IBobrilAttributes = {};
        var someattrs = false;
        for (var n in props) {
            if (!props.hasOwnProperty(n)) continue;
            if (n === "style") {
                style(res, props[n]);
                continue;
            }
            if (n === "key" || n === "ref" || n === "className" || n === "component" || n === "data") {
                (res as any)[n] = props[n];
                continue;
            }
            someattrs = true;
            attrs[n] = props[n];
        }
        if (someattrs)
            res.attrs = attrs;

        return res;
    } else {
        let res = name(props, children);
        if (props != null) {
            if (props.key != null) res.key = props.key;
            if (props.ref != null) res.ref = props.ref;
        }
        return res;
    }
}

export const __spread = assign;
