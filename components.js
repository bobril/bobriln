"use strict";
var b = require('./bobril');
function View(data, children) {
    if (children === undefined && data !== undefined) {
        children = data.children;
    }
    var res = { tag: "View", children: children };
    if (data && data.style !== undefined)
        b.style(res, data.style);
    return res;
}
exports.View = View;
function Text(data, children) {
    if (children === undefined && data !== undefined) {
        children = data.children;
    }
    var res = { tag: "Text", children: children };
    if (data && data.style !== undefined)
        b.style(res, data.style);
    return res;
}
exports.Text = Text;
function Image(data, children) {
    if (children === undefined) {
        children = data.children;
    }
    var res = { tag: "Image", attrs: { source: data.source }, children: children };
    if (data && data.style !== undefined)
        b.style(res, data.style);
    return res;
}
exports.Image = Image;
exports.Switch = b.createVirtualComponent({
    id: "Switch",
    render: function (ctx, me) {
        var d = ctx.data;
        me.tag = "Switch";
        if (b.isFunction(d.value)) {
            me.attrs = { value: d.value() };
        }
        else if (d.value !== undefined) {
            me.attrs = { value: d.value };
        }
        b.style(me, d.style);
    },
    onChange: function (ctx, value) {
        var d = ctx.data;
        var attrs = ctx.me.attrs;
        if (attrs === undefined)
            attrs = {};
        attrs.value = value;
        ctx.me.attrs = attrs;
        if (b.isFunction(d.value)) {
            d.value(value);
        }
        if (d.onChange) {
            d.onChange(value);
        }
        return true;
    }
});
function isTextLikeTag(tag) {
    return tag === undefined || tag === "Text" || tag === "text";
}
function extractText(node) {
    if (node === undefined)
        return "";
    if (b.isString(node))
        return node;
    if (b.isArray(node)) {
        var res = "";
        for (var i = 0; i < node.length; i++) {
            res += extractText(node[i]);
        }
        return res;
    }
    if (isTextLikeTag(node.tag)) {
        return extractText(node.children);
    }
    return "\x10";
}
exports.TextInput = b.createVirtualComponent({
    id: "TextInput",
    render: function (ctx, me) {
        var d = ctx.data;
        me.tag = "TextInput";
        me.attrs = {};
        if (d.selectionStart !== undefined) {
            me.attrs["selectionStart"] = d.selectionStart;
        }
        if (d.selectionEnd !== undefined) {
            me.attrs["selectionEnd"] = d.selectionEnd;
        }
        if (d.value) {
            if (b.isFunction(d.value)) {
                me.children = d.value();
            }
            else {
                me.children = d.value;
            }
        }
        else {
            me.children = d.children;
        }
        b.style(me, d.style);
    },
    onChange: function (ctx, value) {
        var d = ctx.data;
        if (d.onDetailChange) {
            d.onDetailChange(value.start, value.before, value.text);
        }
        if (d.onChange || b.isFunction(d.value)) {
            var currentText = extractText(ctx.me);
            var newText = currentText.substr(0, value.start) + value.text + currentText.substr(value.start + value.before);
            if (d.onChange)
                d.onChange(newText);
            if (b.isFunction(d.value))
                d.value(newText);
        }
    },
    onSelectionChange: function (ctx, event) {
        var d = ctx.data;
        var attrs = ctx.me.attrs;
        if (attrs === undefined) {
            attrs = {};
        }
        attrs["selectionStart"] = event.startPosition;
        attrs["selectionEnd"] = event.endPosition;
        ctx.me.attrs = attrs;
        if (d.onSelectionChange) {
            d.onSelectionChange(event.startPosition, event.endPosition);
        }
    }
});
function ScrollView(data, children) {
    if (children === undefined) {
        children = data.children;
    }
    var res = { tag: "ScrollView", children: children };
    if (data.horizontal) {
        res.attrs = { horizontal: true };
    }
    if (data.style !== undefined)
        b.style(res, data.style);
    return res;
}
exports.ScrollView = ScrollView;
