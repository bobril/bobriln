import * as b from './bobril';

b.addEvent("onChange", 10, (ev: { value: any }, node: b.IBobrilCacheNode) => {
    return b.bubble(node, "onChange", ev.value) !== undefined;
});
