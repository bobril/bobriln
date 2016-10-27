import * as b from './bobril';

b.addEvent("onChange", 10, (ev: { value: any }, node: b.IBobrilCacheNode) => {
    return b.bubble(node, "onChange", ev.value) !== undefined;
});

b.addEvent("backPressed", 10, () => {
    return b.broadcast("onBackPressed", undefined) !== undefined;
});

b.addEvent("onScroll", 10, (ev: { left: number, top: number }, node: b.IBobrilCacheNode) => {
    return b.bubble(node, "onScroll", ev) !== undefined;
});
