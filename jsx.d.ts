declare namespace JSX {
    type IBobrilChild = boolean | string | IBobrilNode | null | undefined;
    type IBobrilChildren = IBobrilChild | IBobrilChildArray | undefined;
    interface IBobrilChildArray extends Array<IBobrilChildren> {
    }
    type IBobrilCacheChildren = string | IBobrilCacheNode[] | undefined;
    interface IBobrilAttributes {
        [name: string]: any;
    }
    interface IBobrilComponent {
        id?: string;
        init?(ctx: IBobrilCtx, me: IBobrilCacheNode): void;
        render?(ctx: IBobrilCtx, me: IBobrilNode, oldMe?: IBobrilCacheNode): void;
        postRender?(ctx: IBobrilCtx, me: IBobrilNode, oldMe?: IBobrilCacheNode): void;
        shouldChange?(ctx: IBobrilCtx, me: IBobrilNode, oldMe: IBobrilCacheNode): boolean;
        destroy?(ctx: IBobrilCtx, me: IBobrilNode): void;
        [name: string]: any;
    }
    interface IBobrilNodeCommon {
        tag?: string;
        key?: string;
        style?: any;
        attrs?: IBobrilAttributes;
        children?: IBobrilChildren;
        ref?: [IBobrilCtx, string] | ((node: IBobrilCacheNode) => void);
        cfg?: any;
        component?: IBobrilComponent;
        data?: any;
    }
    interface IBobrilNodeWithTag extends IBobrilNodeCommon {
        tag: string;
    }
    interface IBobrilNodeWithComponent extends IBobrilNodeCommon {
        component: IBobrilComponent;
    }
    interface IBobrilNodeWithChildren extends IBobrilNodeCommon {
        children: IBobrilChildren;
    }
    type IBobrilNode = IBobrilNodeWithTag | IBobrilNodeWithComponent | IBobrilNodeWithChildren;
    interface IBobrilCacheNode {
        tag: string;
        key: string;
        style: any;
        attrs: IBobrilAttributes;
        children: IBobrilCacheChildren;
        ref: [IBobrilCtx, string] | ((node: IBobrilCacheNode) => void);
        cfg: any;
        component: IBobrilComponent;
        data: any;
        parent: IBobrilCacheNode;
        ctx: IBobrilCtx;
    }
    interface IBobrilCtx {
        data?: any;
        me?: IBobrilCacheNode;
        cfg?: any;
        refs?: {
            [name: string]: IBobrilCacheNode;
        };
    }

    type Element = IBobrilNode;

    interface IntrinsicAttributes {
        key?: string;
        ref?: [IBobrilCtx, string] | ((node: IBobrilCacheNode) => void);
    }

    interface IntrinsicClassAttributes<T> {
    }

    interface IntrinsicElements {
        [name: string]: any;
    }
}
