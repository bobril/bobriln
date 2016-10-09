import * as b from './bobril';

export interface IViewData {
    style?: b.IBobrilStyles;
    children?: b.IBobrilChildren;
}

export function View(data?: IViewData, children?: b.IBobrilChildren) {
    if (children === undefined && data !== undefined) {
        children = data.children;
    }
    var res = { tag: "View", children };
    if (data && data.style !== undefined)
        b.style(res, data.style);
    return res;
}

export interface ITextData {
    style?: b.IBobrilStyles;
    children?: b.IBobrilChildren;
}

export function Text(data?: IViewData, children?: b.IBobrilChildren) {
    if (children === undefined && data !== undefined) {
        children = data.children;
    }
    var res = { tag: "Text", children };
    if (data && data.style !== undefined)
        b.style(res, data.style);
    return res;
}

export interface IImageData {
    /** pass result of b.asset("pathToImage") */
    source: string | (string | number)[];
    style?: b.IBobrilStyles;
    children?: b.IBobrilChildren;
}

export function Image(data?: IViewData, children?: b.IBobrilChildren) {
    if (children === undefined && data !== undefined) {
        children = data.children;
    }
    var res = { tag: "Image", children };
    if (data && data.style !== undefined)
        b.style(res, data.style);
    return res;
}

export interface ISwitchData extends b.IValueData<boolean> {
    style?: b.IBobrilStyles;
}

interface ISwitchCtx extends b.IBobrilCtx {
    data: ISwitchData;
}

export const Switch = b.createVirtualComponent<ISwitchData>({
    id: "Switch",
    render(ctx: ISwitchCtx, me: b.IBobrilNode) {
        const d = ctx.data;
        me.tag = "Switch";
        if (b.isFunction(d.value)) {
            me.attrs = { value: d.value() };
        } else if (d.value !== undefined) {
            me.attrs = { value: d.value };
        }
        b.style(me, d.style);
    },
    onChange(ctx: ISwitchCtx, value: boolean): boolean {
        const d = ctx.data;
        var attrs = ctx.me!.attrs;
        if (attrs === undefined) attrs = {};
        attrs.value = value;
        ctx.me!.attrs = attrs;
        if (b.isFunction(d.value)) {
            d.value(value);
        }
        if (d.onChange) {
            d.onChange(value);
        }
        return true;
    }
});

export interface ITextInputData {
    children?: b.IBobrilChildren;
    style?: b.IBobrilStyles;
    selectionStart?: number;
    selectionEnd?: number;
    onDetailChange?: (start: number, before: number, text: string) => void;
    onChange?: (value: string) => void;
    onSelectionChange?: (start: number, end: number) => void;
}

interface ITextInputCtx extends b.IBobrilCtx {
    data: ITextInputData;
}

function isTextLikeTag(tag: string | undefined) {
    return tag === undefined || tag === "Text" || tag === "text";
}

function extractText(node: b.IBobrilCacheNode | b.IBobrilCacheChildren): string {
    if (node === undefined) return "";
    if (b.isString(node)) return node;
    if (b.isArray(node)) {
        let res = "";
        for (let i = 0; i < node.length; i++) {
            res += extractText(node[i]);
        }
        return res;
    }
    if (isTextLikeTag(node.tag)) {
        return extractText(node.children);
    }
    return "\x10";
}

export const TextInput = b.createVirtualComponent<ITextInputData>({
    id: "TextInput",
    render(ctx: ITextInputCtx, me: b.IBobrilNode) {
        const d = ctx.data;
        me.tag = "TextInput";
        me.attrs = {};
        if (d.selectionStart !== undefined) {
            me.attrs!["selectionStart"] = d.selectionStart;
        }
        if (d.selectionEnd !== undefined) {
            me.attrs!["selectionEnd"] = d.selectionEnd;
        }
        me.children = d.children;
    },
    onChange(ctx: ITextInputCtx, value: { start: number, before: number, text: string }) {
        const d = ctx.data;
        if (d.onDetailChange) {
            d.onDetailChange(value.start, value.before, value.text);
        }
        if (d.onChange) {
            let currentText: string = extractText(ctx.me!);
            let newText = currentText.substr(0, value.start) + value.text + currentText.substr(value.start + value.before);
            d.onChange(newText);
        }
    },
    onSelectionChange(ctx: ITextInputCtx, event: b.ISelectionChangeEvent) {
        const d = ctx.data;
        var attrs = ctx.me!.attrs;
        if (attrs === undefined) {
            attrs = {};
        }
        attrs["selectionStart"] = event.startPosition;
        attrs["selectionEnd"] = event.endPosition;
        ctx.me!.attrs = attrs;
        if (d.onSelectionChange) {
            d.onSelectionChange(event.startPosition, event.endPosition);
        }
    }
});
